package dev.blossom.media;
import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** One opt-in OS reader. All IPC, artwork IO and decoding remain off the render thread. */
public final class MediaBridge {
    public static final class Snapshot {
        public final String title,artist;public final double position,duration,rate;public final boolean playing;public final long sampled;public final BufferedImage art;
        Snapshot(String title,String artist,double position,double duration,double rate,boolean playing,BufferedImage art){this.title=title;this.artist=artist;this.position=position;this.duration=duration;this.rate=rate;this.playing=playing;this.art=art;sampled=System.nanoTime();}
        public double elapsed(){return Math.min(duration>0?duration:Double.MAX_VALUE,Math.max(0,position+(playing?Math.min(3,(System.nanoTime()-sampled)/1e9)*rate:0)));}
    }
    private volatile boolean enabled;private volatile Process process;private Thread worker;private volatile Snapshot snapshot;private volatile String status="No media playing";private long retry;private boolean shutdownHook;
    public Snapshot snapshot(){Snapshot s=snapshot;return s!=null&&System.nanoTime()-s.sampled<5000000000L?s:null;}
    public String status(){return status;}
    public synchronized void enabled(boolean on){
        enabled=on;
        if(!on){snapshot=null;Process p=process;if(p!=null)p.destroyForcibly();return;}
        if((worker==null||!worker.isAlive())&&System.nanoTime()>=retry){if(!shutdownHook){Runtime.getRuntime().addShutdownHook(new Thread(()->enabled(false),"Blossom media shutdown"));shutdownHook=true;}worker=new Thread(this::run,"Blossom media reader");worker.setDaemon(true);worker.start();}
    }
    private void run(){
        Process child=null;Path script=null;
        try{
            String os=System.getProperty("os.name","").toLowerCase(Locale.ROOT);boolean windows=os.startsWith("windows");
            if(!windows&&!os.contains("linux")){status="Media unavailable on this OS";return;}
            String resource=windows?"windows.ps1":"linux.py";
            script=Files.createTempFile("blossom-media-",windows?".ps1":".py");
            try(InputStream in=MediaBridge.class.getResourceAsStream("/media/"+resource)){if(in==null)throw new IOException("Missing media helper");Files.copy(in,script,StandardCopyOption.REPLACE_EXISTING);}
            ProcessBuilder builder=windows?new ProcessBuilder("powershell.exe","-NoLogo","-NoProfile","-NonInteractive","-ExecutionPolicy","Bypass","-File",script.toString()):new ProcessBuilder("python3","-u",script.toString());
            builder.redirectError(new File(windows?"NUL":"/dev/null"));
            child=builder.start();process=child;
            if(!enabled){child.destroyForcibly();return;}
            status="No media playing";String lastArt="";BufferedImage artwork=null;
            try(InputStream in=child.getInputStream()){
                ByteArrayOutputStream line=new ByteArrayOutputStream();long last=System.nanoTime();
                while(enabled&&child.isAlive()){
                    int available=in.available();
                    if(available==0){if(System.nanoTime()-last>20000000000L)throw new IOException("Media API timed out");Thread.sleep(40);continue;}
                    for(int i=0;i<Math.min(available,65536);i++){
                        int c=in.read();if(c<0)break;
                        if(c=='\n'){
                            JsonObject j=new JsonParser().parse(new String(line.toByteArray(),StandardCharsets.UTF_8)).getAsJsonObject();line.reset();last=System.nanoTime();
                            String title=text(j,"title",512),artist=text(j,"artist",512),url=j.has("art")?text(j,"art",3000000):(title.isEmpty()?"":lastArt);
                            if(!url.equals(lastArt)){lastArt=url;artwork=null;try{artwork=art(url);}catch(Exception ignored){}}
                            snapshot=title.isEmpty()?null:new Snapshot(title,artist,number(j,"position",0),number(j,"duration",0),Math.max(0,Math.min(8,number(j,"rate",1))),j.has("playing")&&j.get("playing").getAsBoolean(),artwork);
                        }else{if(line.size()>=3100000)throw new IOException("Oversized media response");line.write(c);}
                    }
                }
            }
        }catch(Exception e){status="Media API unavailable";snapshot=null;}
        finally{if(child!=null)child.destroyForcibly();process=null;snapshot=null;retry=System.nanoTime()+10000000000L;if(script!=null)try{Files.deleteIfExists(script);}catch(IOException ignored){}}
    }
    public static String text(JsonObject j,String key,int max){if(!j.has(key)||j.get(key).isJsonNull())return "";String s=j.get(key).getAsString();return s.substring(0,Math.min(max,s.length())).replace('\n',' ').replace('\r',' ').replace('§',' ');}
    private static double number(JsonObject j,String key,double fallback){try{double n=j.get(key).getAsDouble();return Double.isFinite(n)?n:fallback;}catch(Exception e){return fallback;}}
    private static BufferedImage art(String url)throws Exception{
        if(url.isEmpty())return null;
        InputStream source;
        if(url.startsWith("data:;base64,"))source=new ByteArrayInputStream(Base64.getDecoder().decode(url.substring(13)));
        else{
            URI uri=new URI(url);String scheme=uri.getScheme();if(!"file".equals(scheme)&&!"https".equals(scheme)&&!"http".equals(scheme))return null;
            URLConnection connection=uri.toURL().openConnection();connection.setConnectTimeout(1500);connection.setReadTimeout(1500);
            if(connection.getContentLengthLong()>2097152)return null;source=connection.getInputStream();
        }
        byte[] bytes;
        try(InputStream in=source;ByteArrayOutputStream out=new ByteArrayOutputStream()){long deadline=System.nanoTime()+4000000000L;byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(System.nanoTime()>deadline||out.size()+n>2097152)return null;out.write(buffer,0,n);}bytes=out.toByteArray();}
        try(ImageInputStream in=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){
            Iterator<ImageReader> readers=ImageIO.getImageReaders(in);if(!readers.hasNext())return null;ImageReader reader=readers.next();
            try{reader.setInput(in);int w=reader.getWidth(0),h=reader.getHeight(0);if(w<1||h<1||w>4096||h>4096)return null;
                ImageReadParam param=reader.getDefaultReadParam();int sub=Math.max(1,Math.min(w,h)/128);param.setSourceSubsampling(sub,sub,0,0);BufferedImage raw=reader.read(0,param);
                BufferedImage result=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);Graphics2D g=result.createGraphics();try{g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(Color.WHITE);g.fill(new RoundRectangle2D.Float(0,0,64,64,10,10));g.setComposite(AlphaComposite.SrcIn);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);int side=Math.min(raw.getWidth(),raw.getHeight()),x=(raw.getWidth()-side)/2,y=(raw.getHeight()-side)/2;g.drawImage(raw,0,0,64,64,x,y,x+side,y+side,null);}finally{g.dispose();}return result;
            }finally{reader.dispose();}
        }
    }
}
