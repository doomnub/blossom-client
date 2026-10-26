import java.nio.ByteBuffer;
import java.nio.Buffer;
import java.awt.image.BufferedImage;
import org.lwjgl.opengl.Display;
/** Original Blossom icon for the LWJGL game window; fallback retains vanilla icons. */
public final class BlossomIcon {
    public static int set(ByteBuffer[] original){
        try{return Display.setIcon(load());}
        catch(Exception|LinkageError e){return Display.setIcon(original);}
    }
    /** Separate pixel preparation so the actual Java 8 path is testable without a display. */
    public static ByteBuffer[] load() throws java.io.IOException {
        try(java.io.InputStream stream=BlossomIcon.class.getResourceAsStream("/blossom.png")){
            if(stream==null)throw new java.io.IOException("Missing Blossom icon");
            BufferedImage source=javax.imageio.ImageIO.read(stream);
            if(source==null)throw new java.io.IOException("Invalid Blossom icon");
            return new ByteBuffer[]{pixels(source,32),pixels(source,128)};
        }
    }
    public static ByteBuffer pixels(BufferedImage source,int size){
        BufferedImage image=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g=image.createGraphics();
        try{g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);g.drawImage(source,0,0,size,size,null);}finally{g.dispose();}
        ByteBuffer pixels=ByteBuffer.allocateDirect(size*size*4);
        for(int y=0;y<size;y++)for(int x=0;x<size;x++){int c=image.getRGB(x,y);pixels.put((byte)(c>>16)).put((byte)(c>>8)).put((byte)c).put((byte)(c>>24));}
        // Java 9 added covariant ByteBuffer.flip(); Java 8 only has Buffer.flip().
        ((Buffer)pixels).flip();return pixels;
    }
}
