import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
/** GL readback remains on the render thread; row conversion, PNG encoding and disk IO do not. */
public final class BlossomScreenshots {
    private static final AtomicBoolean busy=new AtomicBoolean();
    private static IntBuffer buffer;private static int[] pixels;
    private static final ExecutorService writer=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"Blossom screenshot encoder");t.setDaemon(true);return t;});
    public static eu capture(File root,int width,int height,bfw framebuffer){
        if(!BlossomPerformance.enabled())return avj.a(root,width,height,framebuffer);
        if(!busy.compareAndSet(false,true))return new fa("Previous screenshot is still saving");
        try{
            boolean fbo=bqs.i();int readWidth=fbo?framebuffer.a:width,readHeight=fbo?framebuffer.b:height;
            final int imageWidth=fbo?framebuffer.c:width,imageHeight=fbo?framebuffer.d:height,stride=readWidth;
            long count=(long)readWidth*readHeight;
            if(count<1||count>33554432||imageWidth>readWidth||imageHeight>readHeight)throw new IOException("Invalid screenshot dimensions");
            int length=(int)count;
            if(buffer==null||buffer.capacity()<length){buffer=BufferUtils.createIntBuffer(length);pixels=new int[length];}
            buffer.clear();buffer.limit(length);
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT,1);GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT,1);
            if(fbo){bfl.i(framebuffer.g);GL11.glGetTexImage(GL11.GL_TEXTURE_2D,0,32993,33639,buffer);}else GL11.glReadPixels(0,0,readWidth,readHeight,32993,33639,buffer);
            buffer.get(pixels,0,length);final int[] source=pixels;
            writer.execute(()->save(root,imageWidth,imageHeight,stride,source));
            return new fa("Saving screenshot...");
        }catch(Exception e){busy.set(false);return new fb("screenshot.failure",e.getMessage());}
    }
    private static void save(File root,int width,int height,int stride,int[] source){
        Path output=null;
        try{
            BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            int[] destination=((DataBufferInt)image.getRaster().getDataBuffer()).getData();
            for(int y=0;y<height;y++)System.arraycopy(source,(height-1-y)*stride,destination,y*width,width);
            Path dir=root.toPath().resolve("screenshots");Files.createDirectories(dir);
            String stem=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss"));
            for(int n=1;;n++){
                Path candidate=dir.resolve(stem+(n==1?"":"_"+n)+".png");
                OutputStream stream;
                try{stream=Files.newOutputStream(candidate,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE);}catch(FileAlreadyExistsException exists){continue;}
                output=candidate;
                try(OutputStream out=stream){if(!ImageIO.write(image,"png",out))throw new IOException("No PNG encoder");}break;
            }
            final Path saved=output;
            ave.A().a((Runnable)()->{fa name=new fa(saved.getFileName().toString());name.b().a(new et(et.a.b,saved.toAbsolutePath().toString()));name.b().d(true);ave.A().q.d().a(new fb("screenshot.success",name));});
        }catch(Exception e){
            if(output!=null)try{Files.deleteIfExists(output);}catch(IOException ignored){}
            final String message=e.getMessage();ave.A().a((Runnable)()->ave.A().q.d().a(new fb("screenshot.failure",message)));
        }finally{if(pixels!=null&&pixels.length>8388608){pixels=null;buffer=null;}busy.set(false);}
    }
}
