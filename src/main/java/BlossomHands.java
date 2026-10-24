import org.lwjgl.opengl.GL11;
/** Local rendering only. Maps keep their readable, two-handed vanilla presentation. */
public final class BlossomHands {
    private static boolean first,third;
    private static int firstWinding;
    public static boolean enabled(){return BlossomHooks.modules.on("lefthand");}
    public static void begin(){
        first=false;if(!enabled()||ave.A().h==null)return;
        zx held=ave.A().h.bA();if(held!=null&&held.b()==zy.bd)return;
        first=true;GL11.glPushMatrix();GL11.glPushAttrib(GL11.GL_POLYGON_BIT);firstWinding=GL11.glGetInteger(GL11.GL_FRONT_FACE);
        GL11.glScalef(-1,1,1);GL11.glFrontFace(firstWinding==GL11.GL_CCW?GL11.GL_CW:GL11.GL_CCW);
    }
    public static void end(){if(first){GL11.glPopAttrib();GL11.glPopMatrix();first=false;}}
    public static void arm(bln renderer,bet player){
        if(!first){renderer.b(player);return;}
        // Use the actual left arm/sleeve UVs, not a mirrored right-arm skin.
        GL11.glPushMatrix();GL11.glPushAttrib(GL11.GL_POLYGON_BIT);
        try{GL11.glScalef(-1,1,1);GL11.glFrontFace(firstWinding);renderer.c(player);}
        finally{GL11.glPopAttrib();GL11.glPopMatrix();}
    }
    public static void pose(bbj model,pk player){
        if(!enabled()||player!=ave.A().h)return;
        bct r=model.h,l=model.i;
        float x=r.c,y=r.d,z=r.e,rx=r.f,ry=r.g,rz=r.h;
        r.c=-l.c;r.d=l.d;r.e=l.e;r.f=l.f;r.g=-l.g;r.h=-l.h;
        l.c=-x;l.d=y;l.e=z;l.f=rx;l.g=-ry;l.h=-rz;
        model.g.g=-model.g.g;
    }
    public static void beginThird(pr player){
        third=enabled()&&player==ave.A().h;
        if(third){GL11.glPushMatrix();GL11.glPushAttrib(GL11.GL_POLYGON_BIT);}
    }
    public static void attach(bbj model,float scale){
        if(!third){model.a(scale);return;}
        float x=model.i.c;
        try{if("slim".equals(ave.A().h.l()))model.i.c-=1;model.i.c(scale);}finally{model.i.c=x;}
        GL11.glScalef(-1,1,1);int winding=GL11.glGetInteger(GL11.GL_FRONT_FACE);GL11.glFrontFace(winding==GL11.GL_CCW?GL11.GL_CW:GL11.GL_CCW);
    }
    public static void endThird(){if(third){GL11.glPopAttrib();GL11.glPopMatrix();third=false;}}
}
