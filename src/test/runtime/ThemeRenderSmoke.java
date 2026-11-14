import java.nio.ByteBuffer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.*;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
/** Exercises the shipped theme renderer in a real LWJGL 2 context, without a game account. */
public final class ThemeRenderSmoke {
    private static Method rounded,surface,glass;
    public static void main(String[] args)throws Exception{
        Display.setDisplayMode(new DisplayMode(640,400));Display.create();
        try{
            GL11.glViewport(0,0,640,400);GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(0,320,200,0,-1,1);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glDisable(GL11.GL_DEPTH_TEST);
            Class<?> theme=Class.forName("dev.blossom.integration.BlossomTheme");
            rounded=theme.getMethod("rounded",int.class,int.class,int.class,int.class,int.class,int.class);
            surface=theme.getMethod("surface",int.class,int.class,int.class,int.class);
            glass=theme.getMethod("glass",int.class,int.class);
            Object settings=Class.forName("dev.blossom.integration.BlossomHooks").getField("settings").get(null);
            Field style=settings.getClass().getField("uiStyle");
            for(Object value:style.getType().getEnumConstants()){
                style.set(settings,value);
                GL11.glClearColor(.35f,.4f,.45f,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                surface.invoke(null,12,12,308,188);
                draw(24,28,296,54,6,0xffde2ab7,130);
                draw(24,66,152,130,7,0xff292630,115);draw(164,66,296,130,7,0xff292630,115);
                draw(24,144,296,170,5,0xff292630,125);
                ByteBuffer pixels=read();
                BufferedImage image=new BufferedImage(640,400,BufferedImage.TYPE_INT_ARGB);
                for(int y=0;y<400;y++)for(int x=0;x<640;x++){int i=((399-y)*640+x)*4;image.setRGB(x,y,0xff000000|((pixels.get(i)&255)<<16)|((pixels.get(i+1)&255)<<8)|(pixels.get(i+2)&255));}
                ImageIO.write(image,"png",new File(args[0]+"/theme-"+value.toString().toLowerCase(java.util.Locale.ROOT)+".png"));
                if(value.toString().equals("MODERN")){
                    // Isolated white half-opacity shape on black: center ~128, edge smoothly between 0 and 128.
                    GL11.glClearColor(0,0,0,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                    GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glEnable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_BLEND);GL11.glShadeModel(GL11.GL_FLAT);
                    rounded.invoke(null,50,50,150,100,12,0x80ffffff);
                    if(!GL11.glIsEnabled(GL11.GL_ALPHA_TEST)||!GL11.glIsEnabled(GL11.GL_CULL_FACE)||GL11.glIsEnabled(GL11.GL_BLEND)||GL11.glGetInteger(GL11.GL_SHADE_MODEL)!=GL11.GL_FLAT)throw new AssertionError("Leaked GL state");
                    pixels=read();int mid=pixels.get(((400-150)*640+200)*4)&255;int fringe=0;
                    for(int y=195;y<303;y++)for(int x=95;x<305;x++){int v=pixels.get((y*640+x)*4)&255;if(v>5&&v<120)fringe++;}
                    if(mid<125||mid>131||fringe<20)throw new AssertionError("Missing translucency / AA fringe: "+mid+" / "+fringe);
                }
                if(GL11.glGetError()!=GL11.GL_NO_ERROR)throw new AssertionError("OpenGL error");
            }
            System.out.println("Real LWJGL 2 theme render, transparency, smooth edges and GL state restoration passed");
        }finally{Display.destroy();}
    }
    private static void draw(int x,int y,int r,int b,int radius,int color,int alpha)throws Exception{rounded.invoke(null,x,y,r,b,radius,glass.invoke(null,color,alpha));}
    private static ByteBuffer read(){ByteBuffer pixels=BufferUtils.createByteBuffer(640*400*4);GL11.glReadPixels(0,0,640,400,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);return pixels;}
}
