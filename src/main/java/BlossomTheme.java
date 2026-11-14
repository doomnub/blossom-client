import dev.blossom.core.UiStyle;
import dev.blossom.core.SmoothPanelMesh;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
/** Rounded panels, emitted in one batch with cached row insets. */
public final class BlossomTheme {
    private static final SmoothPanelMesh smoothMesh=new SmoothPanelMesh();
    public static boolean modern(){return BlossomHooks.settings.uiStyle==UiStyle.MODERN;}
    public static String styleLabel(){return "Style: "+BlossomHooks.settings.uiStyle.label;}
    public static void toggle(){BlossomHooks.settings.uiStyle=BlossomHooks.settings.uiStyle.next();BlossomHooks.save("default");}
    public static int glass(int pixel,int alpha){return modern()?(Math.min(alpha,pixel>>>24)<<24)|(pixel&0xffffff):pixel;}
    /** Closed contour plus transparent fringe, independent of driver MSAA support. */
    private static void smooth(int x,int y,int right,int bottom,int radius,int color){
        smoothMesh.set(x,y,right,bottom,radius,.65f);
        // Raw GL is isolated so Minecraft's cached state remains valid after this draw.
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_CURRENT_BIT|GL11.GL_LIGHTING_BIT);
        try{
            GL11.glEnable(GL11.GL_BLEND);GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_LIGHTING);GL11.glShadeModel(GL11.GL_SMOOTH);
            GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA,GL11.GL_ONE,GL11.GL_ZERO);
            float red=(color>>16&255)/255f,green=(color>>8&255)/255f,blue=(color&255)/255f,alpha=(color>>>24)/255f;
            GL11.glColor4f(red,green,blue,alpha);
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glVertex2f((x+right)*.5f,(y+bottom)*.5f);
            for(int i=0;i<=SmoothPanelMesh.COUNT;i++){int n=i%SmoothPanelMesh.COUNT;GL11.glVertex2f(smoothMesh.x[n],smoothMesh.y[n]);}
            GL11.glEnd();
            GL11.glBegin(GL11.GL_QUAD_STRIP);
            for(int i=0;i<=SmoothPanelMesh.COUNT;i++){
                int n=i%SmoothPanelMesh.COUNT;
                GL11.glColor4f(red,green,blue,alpha);GL11.glVertex2f(smoothMesh.x[n],smoothMesh.y[n]);
                GL11.glColor4f(red,green,blue,0);GL11.glVertex2f(smoothMesh.outerX[n],smoothMesh.outerY[n]);
            }
            GL11.glEnd();
        }finally{GL11.glPopAttrib();}
    }
    private static final int[][] insets=new int[65][];
    public static void rounded(int x,int y,int right,int bottom,int radius,int color){
        if(right<=x||bottom<=y||(color>>>24)==0)return;
        if(modern()){smooth(x,y,right,bottom,radius,color);return;}
        int r=Math.max(0,Math.min(radius,Math.min(right-x,bottom-y)/2));
        int[] rows=r<insets.length?insets[r]:null;
        if(rows==null){rows=new int[r];for(int row=0;row<r;row++)rows[row]=(int)Math.ceil(r-Math.sqrt((double)r*r-(r-row-.5)*(r-row-.5)));if(r<insets.length)insets[r]=rows;}
        bfx tess=bfx.a();bfd buffer=tess.c();
        bfl.l();bfl.x();bfl.a(770,771,1,0);bfl.c((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,(color>>>24)/255f);
        buffer.a(7,bms.e);
        rect(buffer,x+r,y,right-r,bottom);
        for(int row=0;row<r;row++){int inset=rows[row];rect(buffer,x+inset,y+row,x+r,y+row+1);rect(buffer,right-r,y+row,right-inset,y+row+1);rect(buffer,x+inset,bottom-row-1,x+r,bottom-row);rect(buffer,right-r,bottom-row-1,right-inset,bottom-row);}
        rect(buffer,x,y+r,x+r,bottom-r);rect(buffer,right-r,y+r,right,bottom-r);
        tess.b();bfl.w();bfl.k();
    }
    private static void rect(bfd b,int left,int top,int right,int bottom){b.b((double)right,top,0).d();b.b((double)left,top,0).d();b.b((double)left,bottom,0).d();b.b((double)right,bottom,0).d();}
    public static final int SURFACE=0xf5111218, BORDER=0xff33343f, TEXT=0xfff4f1f7, MUTED=0xffa4a1b1;
    public static void surface(int left,int top,int right,int bottom){
        rounded(left,top+3,right,bottom+3,9,0x50000000);
        rounded(left,top,right,bottom,9,glass(BORDER,45));
        rounded(left+1,top+1,right-1,bottom-1,8,glass(SURFACE,180));
    }
    public static String fit(avn font,String text,int width){
        if(font.a(text)<=width)return text;
        while(text.length()>0&&font.a(text+"...")>width)text=text.substring(0,text.length()-1);
        return text+"...";
    }
    public static void panel(int width,int height){
        int left=Math.max(4,width/2-155),right=Math.min(width-4,width/2+155);
        surface(left,6,right,height-6);
        rounded(left,20,left+3,30,1,BlossomHooks.accent);
        avp.a(left+12,40,right-12,41,0xff2b2c35);
    }
}
