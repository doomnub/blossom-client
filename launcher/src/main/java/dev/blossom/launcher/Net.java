package dev.blossom.launcher;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** HTTPS, bounded JSON, verified downloads; never logs HTTP bodies or credentials. */
public final class Net {
    private static final Set<String> DOWNLOAD_HOSTS=new HashSet<String>(Arrays.asList(
        "cdn.azul.com","piston-meta.mojang.com","launchermeta.mojang.com","launcher.mojang.com",
        "piston-data.mojang.com","libraries.minecraft.net","resources.download.minecraft.net","repo.maven.apache.org"));
    public static final class Failure extends IOException {
        public final int status;public final String code;
        Failure(int status,String code){super("Service returned HTTP "+status+(code.isEmpty()?"":" ("+code+")"));this.status=status;this.code=code;}
    }
    public static JsonObject request(String address,String method,String contentType,String body,String bearer)throws IOException {
        HttpURLConnection connection=open(address);
        try {
            connection.setRequestMethod(method);connection.setRequestProperty("Accept","application/json");
            if(bearer!=null)connection.setRequestProperty("Authorization","Bearer "+bearer);
            if(body!=null){connection.setDoOutput(true);connection.setRequestProperty("Content-Type",contentType);try(OutputStream out=connection.getOutputStream()){out.write(body.getBytes(StandardCharsets.UTF_8));}}
            int status=connection.getResponseCode();InputStream in=status>=200&&status<300?connection.getInputStream():connection.getErrorStream();
            JsonObject result=new JsonObject();
            if(in!=null)try(InputStream stream=in){result=JsonParser.parseString(new String(readLimited(stream,2*1024*1024),StandardCharsets.UTF_8)).getAsJsonObject();}
            catch(JsonParseException|IllegalStateException e){throw new IOException("Service returned invalid JSON");}
            if(status<200||status>=300){String code=result.has("error")&&result.get("error").isJsonPrimitive()?result.get("error").getAsString():"";if(!code.matches("[a-zA-Z0-9_ .-]{0,80}"))code="";throw new Failure(status,code);}
            return result;
        }finally{connection.disconnect();}
    }
    public static JsonObject post(String url,JsonObject body)throws IOException{return request(url,"POST","application/json",body.toString(),null);}
    public static JsonObject form(String url,Map<String,String> values)throws IOException {
        StringBuilder body=new StringBuilder();for(Map.Entry<String,String> e:values.entrySet()){if(body.length()>0)body.append('&');body.append(URLEncoder.encode(e.getKey(),"UTF-8")).append('=').append(URLEncoder.encode(e.getValue(),"UTF-8"));}
        return request(url,"POST","application/x-www-form-urlencoded",body.toString(),null);
    }
    private static HttpURLConnection open(String address)throws IOException {
        URL url=new URL(address);if(!url.getProtocol().equals("https"))throw new IOException("HTTPS required");
        String proxyUrl=System.getenv("HTTPS_PROXY");
        Proxy proxy=Proxy.NO_PROXY;
        if(proxyUrl!=null&&!proxyUrl.isEmpty()){try{URI uri=URI.create(proxyUrl);if(!"http".equals(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null)throw new IllegalArgumentException();proxy=new Proxy(Proxy.Type.HTTP,new InetSocketAddress(uri.getHost(),uri.getPort()<0?80:uri.getPort()));}catch(IllegalArgumentException e){throw new IOException("HTTPS_PROXY must be an HTTP proxy URL without credentials");}}
        HttpURLConnection c=(HttpURLConnection)(proxy==Proxy.NO_PROXY?url.openConnection():url.openConnection(proxy));c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setInstanceFollowRedirects(false);c.setRequestProperty("User-Agent","Blossom-Launcher/0.2");return c;
    }
    public static void download(String address,Path target,String digest,long size)throws IOException {
        if(!DOWNLOAD_HOSTS.contains(new URL(address).getHost()))throw new IOException("Unexpected download host");
        if(!digest.matches("[a-f0-9]{40}|[a-f0-9]{64}"))throw new IOException("Missing download checksum");
        String algorithm=digest.length()==40?"SHA-1":"SHA-256";
        if(Files.isRegularFile(target)&&hash(target,algorithm).equals(digest))return;
        Files.createDirectories(target.getParent());Path temp=Files.createTempFile(target.getParent(),"download-",".part");
        HttpURLConnection c=open(address);
        try {
            if(c.getResponseCode()!=200)throw new IOException("Download returned HTTP "+c.getResponseCode());
            long count=0,limit=size>0?size:256L*1024*1024;
            try(InputStream in=c.getInputStream();OutputStream out=Files.newOutputStream(temp)){
                byte[] buffer=new byte[32768];int n;
                while((n=in.read(buffer))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Cancelled");count+=n;if(count>limit)throw new IOException("Download exceeds declared size");out.write(buffer,0,n);}
            }
            if(size>0&&count!=size||!hash(temp,algorithm).equals(digest))throw new IOException("Download checksum/size mismatch");
            move(temp,target);
        }finally{c.disconnect();Files.deleteIfExists(temp);}
    }
    public static void move(Path from,Path to)throws IOException {
        try{Files.move(from,to,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
        catch(AtomicMoveNotSupportedException e){Files.move(from,to,StandardCopyOption.REPLACE_EXISTING);}
    }
    public static String hash(Path file,String algorithm)throws IOException {
        try{MessageDigest digest=MessageDigest.getInstance(algorithm);try(InputStream in=Files.newInputStream(file)){byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1)digest.update(b,0,n);}
            StringBuilder s=new StringBuilder();for(byte b:digest.digest())s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();
        }catch(java.security.NoSuchAlgorithmException e){throw new IOException(e);}
    }
    public static byte[] readLimited(InputStream in,int limit)throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] bytes=new byte[8192];int n;
        while((n=in.read(bytes))!=-1){if(out.size()+n>limit)throw new IOException("Response exceeds limit");out.write(bytes,0,n);}return out.toByteArray();
    }
    public static Path safeResolve(Path root,String name)throws IOException {
        if(name.indexOf('\\')>=0||name.indexOf(':')>=0)throw new IOException("Invalid relative path");
        Path base=root.toAbsolutePath().normalize(),p=base.resolve(name).normalize();
        if(!p.startsWith(base)||p.equals(base))throw new IOException("Path leaves installation directory");return p;
    }
}
