import java.nio.ByteBuffer;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.status.*;
/** Executes the formerly failing paths on Java 8; no display or account needed. */
public final class StartupSmoke {
    public static void main(String[] args)throws Exception {
        if(!System.getProperty("java.specification.version").equals("1.8"))throw new AssertionError("Run on actual Java 8");
        if(args.length==1){
            Path config=(Path)Class.forName("dev.blossom.launcher.GameInstall").getMethod("writeLogging",Path.class).invoke(null,Paths.get(args[0]));
            System.setProperty("log4j.configurationFile",config.toString());
        }
        Class<?> icon=Class.forName("dev.blossom.integration.BlossomIcon");
        ByteBuffer[] icons=(ByteBuffer[])icon.getMethod("load").invoke(null);
        if(icons.length!=2)throw new AssertionError("Missing window icons");
        for(int i=0;i<icons.length;i++){
            ByteBuffer b=icons[i];int size=i==0?32:128;
            if(!b.isDirect()||b.position()!=0||b.remaining()!=size*size*4)throw new AssertionError("Incorrect icon buffer bounds");
            boolean visible=false;for(int at=3;at<b.limit();at+=4)visible|=(b.get(at)&255)>0;
            if(!visible)throw new AssertionError("Blank icon");
        }
        BufferedImage sample=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);sample.setRGB(0,0,0xff123456);
        ByteBuffer pixel=(ByteBuffer)icon.getMethod("pixels",BufferedImage.class,int.class).invoke(null,sample,1);
        for(int value:new int[]{0x12,0x34,0x56,0xff})if((pixel.get()&255)!=value)throw new AssertionError("Wrong RGBA channel order");
        org.apache.logging.log4j.Logger log=LogManager.getLogger("BlossomStartupSmoke");
        LoggerContext context=(LoggerContext)LogManager.getContext(false);
        for(String name:new String[]{"Console","File"}){
            Appender appender=context.getConfiguration().getAppender(name);
            if(appender==null||!appender.isStarted()||!(appender.getLayout() instanceof org.apache.logging.log4j.core.layout.PatternLayout))throw new AssertionError("Broken appender "+name);
        }
        log.info("blossom-startup-smoke");
        log.warn("blossom-stack-smoke",new IllegalStateException("expected probe exception"));
        LogManager.shutdown();
        for(StatusData entry:StatusLogger.getLogger().getStatusData())if(entry.getLevel().isMoreSpecificThan(Level.WARN))throw new AssertionError(entry.getFormattedStatus());
        System.out.println("Java 8 icon pixels and console/file logging passed.");
    }
}
