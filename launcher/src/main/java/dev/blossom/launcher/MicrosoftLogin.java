package dev.blossom.launcher;

import com.google.gson.*;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/** Public-client device authorization. Passwords and tokens are never written to disk. */
public final class MicrosoftLogin {
    private static final String OAUTH="https://login.microsoftonline.com/consumers/oauth2/v2.0/";
    public static final class Session {
        public final String username,uuid;
        final String token;
        final long expiresAt;
        public final boolean offline;
        Session(String username,String uuid,String token,long expiresAt){this(username,uuid,token,expiresAt,false);}
        private Session(String username,String uuid,String token,long expiresAt,boolean offline){this.username=username;this.uuid=uuid;this.token=token;this.expiresAt=expiresAt;this.offline=offline;}
        public static Session offline(String username){
            if(username==null||!username.matches("[A-Za-z0-9_]{1,16}"))throw new IllegalArgumentException("Offline username must contain 1–16 letters, numbers or underscores.");
            String uuid=UUID.nameUUIDFromBytes(("OfflinePlayer:"+username).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString().replace("-","");
            return new Session(username,uuid,"0",Long.MAX_VALUE,true);
        }
        public boolean valid(){return offline||System.currentTimeMillis()<expiresAt-60000;}
    }
    public static final class DeviceCode {
        public final String code,url;
        DeviceCode(String code,String url){this.code=code;this.url=url;}
    }
    public static Session login(String clientId,Consumer<DeviceCode> display,Consumer<String> status)throws Exception {
        if(!clientId.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"))throw new IOException("Set Blossom's registered Microsoft application ID in Settings first.");
        JsonObject device=Net.form(OAUTH+"devicecode",map("client_id",clientId,"scope","XboxLive.signin"));
        String url=device.get("verification_uri").getAsString();java.net.URI uri=java.net.URI.create(url);
        if(!"https".equals(uri.getScheme())||!("microsoft.com".equals(uri.getHost())||"www.microsoft.com".equals(uri.getHost())||"login.microsoftonline.com".equals(uri.getHost())))throw new IOException("Unexpected Microsoft verification URL");
        display.accept(new DeviceCode(device.get("user_code").getAsString(),url));
        long deadline=System.nanoTime()+Math.min(1800,device.get("expires_in").getAsInt())*1000000000L;
        int interval=Math.max(5,device.has("interval")?device.get("interval").getAsInt():5);String microsoft=null;
        while(System.nanoTime()<deadline){
            Thread.sleep(Math.min(interval,60)*1000L);
            try{JsonObject reply=Net.form(OAUTH+"token",map("client_id",clientId,"grant_type","urn:ietf:params:oauth:grant-type:device_code","device_code",device.get("device_code").getAsString()));microsoft=reply.get("access_token").getAsString();break;}
            catch(Net.Failure e){if(e.code.equals("authorization_pending"))continue;if(e.code.equals("slow_down")){interval+=5;continue;}throw new IOException("Microsoft sign-in ended: "+e.code);}
        }
        if(microsoft==null)throw new IOException("Sign-in code expired. Try again.");
        status.accept("Verifying Xbox account...");
        JsonObject props=new JsonObject();props.addProperty("AuthMethod","RPS");props.addProperty("SiteName","user.auth.xboxlive.com");props.addProperty("RpsTicket","d="+microsoft);
        JsonObject user=Net.post("https://user.auth.xboxlive.com/user/authenticate",ticket(props,"http://auth.xboxlive.com"));
        JsonArray tokens=new JsonArray();tokens.add(user.get("Token").getAsString());props=new JsonObject();props.addProperty("SandboxId","RETAIL");props.add("UserTokens",tokens);
        JsonObject xsts=Net.post("https://xsts.auth.xboxlive.com/xsts/authorize",ticket(props,"rp://api.minecraftservices.com/"));
        String hash=xsts.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
        props=new JsonObject();props.addProperty("identityToken","XBL3.0 x="+hash+";"+xsts.get("Token").getAsString());
        status.accept("Checking Minecraft Java access...");
        JsonObject game;
        try{game=Net.post("https://api.minecraftservices.com/authentication/login_with_xbox",props);}
        catch(Net.Failure e){if(e.status==403)throw new IOException("Minecraft refused this application. Its Microsoft client ID may need Minecraft API approval.");throw e;}
        String token=game.get("access_token").getAsString();
        JsonObject ownership=Net.request("https://api.minecraftservices.com/entitlements/mcstore","GET",null,null,token);
        boolean owns=false;
        for(JsonElement item:ownership.getAsJsonArray("items")){String name=item.getAsJsonObject().get("name").getAsString();if(name.equals("game_minecraft")||name.equals("product_minecraft"))owns=true;}
        if(!owns)throw new IOException("This account has no recognized Minecraft Java entitlement.");
        JsonObject profile=Net.request("https://api.minecraftservices.com/minecraft/profile","GET",null,null,token);
        String name=profile.get("name").getAsString(),uuid=profile.get("id").getAsString();
        if(!name.matches("[A-Za-z0-9_]{1,16}")||!uuid.matches("[a-fA-F0-9]{32}"))throw new IOException("Invalid Minecraft profile response");
        long ttl=Math.max(60,Math.min(86400,game.get("expires_in").getAsLong()));
        return new Session(name,uuid,token,System.currentTimeMillis()+ttl*1000);
    }
    private static JsonObject ticket(JsonObject props,String party){JsonObject o=new JsonObject();o.add("Properties",props);o.addProperty("RelyingParty",party);o.addProperty("TokenType","JWT");return o;}
    private static Map<String,String> map(String...values){Map<String,String> map=new LinkedHashMap<String,String>();for(int i=0;i<values.length;i+=2)map.put(values[i],values[i+1]);return map;}
}
