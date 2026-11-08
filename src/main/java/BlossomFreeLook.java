import org.lwjgl.opengl.Display;
/** Separate camera angles; player rotations and outgoing movement remain untouched. */
public final class BlossomFreeLook {
    private static boolean active;
    private static final dev.blossom.core.TapHold gesture=new dev.blossom.core.TapHold();
    private static pk player;
    private static Object world;
    private static int perspective;
    private static float yaw,pitch;
    public static void update(){
        ave mc=ave.A();
        boolean allowed=BlossomHooks.modules.on("freelook")&&mc.h!=null&&mc.f!=null&&mc.ac()==mc.h&&mc.m==null&&mc.w&&Display.isActive()&&!mc.h.bJ();
        boolean changed=active&&(player!=mc.h||world!=mc.f);
        boolean want=gesture.update(BlossomBindings.held(BlossomBindings.freeLook),allowed&&!changed,System.nanoTime());
        if(active&&!want)stop();
        if(want&&!active){player=mc.h;world=mc.f;perspective=mc.t.aB;yaw=player.y;pitch=player.z;active=true;}
        if(active)mc.t.aB=1;
    }
    private static void stop(){ave.A().t.aB=perspective;active=false;player=null;world=null;}
    public static void turn(pk entity,float x,float y){
        if(active&&entity==player){yaw+=x*.15f;pitch=Math.max(-90,Math.min(90,pitch-y*.15f));}
        else entity.c(x,y);
    }
    public static float yaw(float original){return active?yaw:original;}
    public static float pitch(float original){return active?pitch:original;}
}
