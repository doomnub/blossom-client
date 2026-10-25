import dev.blossom.core.ToggleLatch;
import org.lwjgl.input.Keyboard;
/** Only changes the answer to vanilla sprint/sneak key queries. Vanilla enforces physics. */
public final class BlossomControls {
    private static final ToggleLatch sprint=new ToggleLatch(), sneak=new ToggleLatch();
    private static Object player;
    public static void restoreSprint(boolean value){sprint.set(value);}
    public static void updateContext() {
        ave mc=ave.A();
        // Sprint is a deliberate persistent toggle, independent of world/player identity.
        sprint.context(mc.h==player&&mc.h!=null,mc.m!=null,true);
        sneak.context(mc.h==player&&mc.h!=null,mc.m!=null,false);

        if(!BlossomHooks.modules.on("sneak"))sneak.reset();
        player=mc.h;
    }
    public static void press(int code) {
        updateContext();ave mc=ave.A();
        if(mc.m!=null||mc.h==null)return;
        if(BlossomHooks.modules.on("sprint")&&code==mc.t.ae.i()){sprint.press();BlossomHooks.saveSprint(sprint.active());}
        if(BlossomHooks.modules.on("sneak")&&code==mc.t.ad.i())sneak.press();
    }
    public static boolean down(boolean original,avb binding) {
        if(!BlossomHooks.modules.on("sprint")&&!BlossomHooks.modules.on("sneak"))return original;
        ave mc=ave.A();if(binding!=mc.t.ae&&binding!=mc.t.ad)return original;
        updateContext();
        if(binding==mc.t.ae&&BlossomHooks.modules.on("sprint"))return mc.h!=null&&sprint.requested(mc.m!=null);
        if(mc.m!=null||mc.h==null)return original;
        if(binding==mc.t.ad&&BlossomHooks.modules.on("sneak"))return sneak.active();
        return original;
    }
    public static String status(){return "Sprint "+(sprint.active()?"toggled":"off")+" | Sneak "+(sneak.active()?"toggled":"off");}
}
