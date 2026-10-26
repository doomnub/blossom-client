import org.lwjgl.opengl.GL11;
/** Classic rendering on vanilla 1.8.9 models. Never writes input, player or network state. */
public final class BlossomLegacy {
    private static zx rendered;
    private static bmi glintSprite;
    private static float partial,previousEye,eye;
    private static Object player;
    private static int swing=-1,previousSwing=-1;
    public static boolean on(int option){return BlossomHooks.modules.on("legacy")&&BlossomHooks.settings.legacy.enabled[option];}
    public static void begin(zx stack,float delta){
        if(!BlossomHooks.modules.on("legacy"))return;
        rendered=stack;partial=delta;
    }
    public static void end(){rendered=null;}
    public static void tick(){
        if(!BlossomHooks.modules.on("legacy")){player=null;rendered=null;swing=previousSwing=-1;return;}
        ave mc=ave.A();
        if(mc.h==null){player=null;swing=previousSwing=-1;return;}
        if(player!=mc.h){player=mc.h;eye=previousEye=mc.h.aS();swing=previousSwing=-1;}
        previousEye=eye;eye+=(mc.h.aS()-eye)*.6f;
        previousSwing=swing;
        if(swing>=0&&++swing>=6)swing=-1;
        zx held=mc.h.bA();
        boolean block=on(1)&&held!=null&&held.m()==aba.d&&mc.h.bR()>0;
        if(mc.m==null&&(block||on(10))&&BlossomBindings.held(mc.t.ai)&&swing<0)swing=0;
    }
    /** The synthetic swing belongs only to this renderer; it cannot create attack packets. */
    public static float swing(float vanilla){
        if(!BlossomHooks.modules.on("legacy"))return vanilla;
        if(vanilla>0||swing<0)return vanilla;
        float from=previousSwing<0?0:previousSwing,to=swing;
        if(to<from)from=0;
        return (from+(to-from)*partial)/6f;
    }
    public static float useSwing(float vanilla){
        if(!BlossomHooks.modules.on("legacy")||rendered==null)return vanilla;
        aba action=rendered.m();
        if(on(10)||(action==aba.d&&on(1))||((action==aba.b||action==aba.c)&&on(6))||action==aba.e&&on(4))
            return swing(ave.A().h.l(partial));
        return vanilla;
    }
    public static float eye(float vanilla,float delta){
        if(!on(2))return vanilla;
        ave mc=ave.A();
        if(player!=mc.h||mc.ac()!=mc.h||mc.h==null||mc.h.bJ()||vanilla<1.5f||vanilla>1.7f)return vanilla;
        return previousEye+(eye-previousEye)*delta;
    }
    public static void bowScale(float x,float y,float z){
        if(!on(4)){bfl.a(x,y,z);return;}
        // Draw along the old sprite's axes, about its upper half, rather than model-space Z.
        bfl.b(25f,0,0,1);bfl.b(-50f,0,1,0);bfl.b(0,.5f,0);
        bfl.a(x,y,z);
        bfl.b(0,-.5f,0);bfl.b(50f,0,1,0);bfl.b(-25f,0,0,1);
    }
    public static zx equipStack(zx old){
        if(!on(5))return old;
        zx selected=ave.A().h.bi.h();
        // Keep the latest stack's model/NBT while suppressing count-only re-equip animations.
        return on(5)&&old!=null&&selected!=null&&old.b()==selected.b()&&old.i()==selected.i()?selected:old;
    }
    public static void render(bfn renderer,pr entity,zx stack,bgr.b perspective){
        if(!BlossomHooks.modules.on("legacy")||stack==null||stack.b()==zy.bd||ave.A().ag().a(stack)){
            renderer.a(entity,stack,perspective);return;
        }
        int id=zw.b(stack.b());aba action=stack.m();
        boolean using=entity instanceof bet&&((bet)entity).bR()>0;
        boolean classic=id==346?on(3):id==261?on(4):action==aba.d&&using?on(0):(action==aba.b||action==aba.c)&&using?on(6):on(7);
        if(!classic){renderer.a(entity,stack,perspective);return;}
        GL11.glPushMatrix();
        try{
            if(id==346)bfl.b(180f,0,1,0);
            bfl.b(0,-.3f,0);bfl.a(1.5f,1.5f,1.5f);
            bfl.b(50f,0,1,0);bfl.b(-25f,0,0,1);bfl.b(-.9375f,-.0625f,0);
            // Convert the centered, atlas-backed 1.8 model into the old extruded sprite frame.
            bfl.b(.5f,.5f,-.03125f);bfl.b(180f,0,1,0);
            renderer.a(entity,stack,perspective.a);
        }finally{GL11.glPopMatrix();}
    }
    public static boolean damage(boolean combine){return combine||on(8)&&!BlossomHooks.modules.on("hitcolor");}
    public static int glintColor(int vanilla){return on(9)?0xff60309b:vanilla;}
    public static void beginGlint(boq model){glintSprite=on(9)&&!model.c()?model.e():null;}
    public static void endGlint(){glintSprite=null;}
    public static void glintScale(float x,float y,float z){
        if(glintSprite!=null)bfl.a(.125f,.125f,.125f);else bfl.a(x,y,z);
    }
    public static void glintTranslate(float x,float y,float z){bfl.b(glintSprite!=null?x*64:x,y,z);}
    public static void glintUV(){
        if(glintSprite==null)return;
        // The old renderer used sprite-local UVs. Undo atlas placement before its texture matrix.
        float u=glintSprite.f()-glintSprite.e(),v=glintSprite.h()-glintSprite.g();
        if(u>0&&v>0){bfl.a(1/u,1/v,1);bfl.b(-glintSprite.e(),-glintSprite.g(),0);}
    }
}
