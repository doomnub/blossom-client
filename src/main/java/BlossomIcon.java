import java.nio.ByteBuffer;
import java.awt.image.BufferedImage;
import org.lwjgl.opengl.Display;
/** Original Blossom icon for the LWJGL game window; fallback retains vanilla icons. */
public final class BlossomIcon {
    public static int set(ByteBuffer[] original){
        try(java.io.InputStream stream=BlossomIcon.class.getResourceAsStream("/blossom.png")){
            BufferedImage source=javax.imageio.ImageIO.read(stream);ByteBuffer[] icons=new ByteBuffer[2];int n=0;
            for(int size:new int[]{32,128}){
                BufferedImage image=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g=image.createGraphics();g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);g.drawImage(source,0,0,size,size,null);g.dispose();
                ByteBuffer pixels=ByteBuffer.allocateDirect(size*size*4);
                for(int y=0;y<size;y++)for(int x=0;x<size;x++){int c=image.getRGB(x,y);pixels.put((byte)(c>>16)).put((byte)(c>>8)).put((byte)c).put((byte)(c>>24));}
                pixels.flip();icons[n++]=pixels;
            }
            return Display.setIcon(icons);
        }catch(Exception e){return Display.setIcon(original);}
    }
}
