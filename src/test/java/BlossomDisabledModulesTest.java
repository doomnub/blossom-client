/** Disabled hooks must work with no Minecraft instance or OpenGL context. */
public final class BlossomDisabledModulesTest {
    public static void run()throws Exception{
        java.util.Map<String,Boolean> saved=new java.util.HashMap<String,Boolean>();
        for(dev.blossom.core.Modules.Module m:BlossomHooks.modules.all()){saved.put(m.id,m.enabled);m.enabled=false;}
        java.lang.reflect.Field initialized=BlossomHooks.class.getDeclaredField("initialized");initialized.setAccessible(true);boolean old=initialized.getBoolean(null);initialized.setBoolean(null,true);
        try{
            if(BlossomHooks.cloth(null,null,0))throw new AssertionError("Disabled cloth claimed rendering");
            if(BlossomCamera.fov(73,true)!=73)throw new AssertionError("Disabled FOV changed projection");
            if(!BlossomControls.down(true,null))throw new AssertionError("Disabled controls changed input");
            BlossomHands.begin();BlossomHands.end();BlossomHands.pose(null,null);BlossomHands.beginThird(null);BlossomHands.endThird();
            BlossomNetwork.tick();BlossomPing.tick();BlossomVisuals.idle();BlossomVisuals.added(null);
            BlossomHooks.frames.record(1000);BlossomHooks.frame();
            BlossomHooks.modules.get("hittilt").enabled=true;BlossomHooks.settings.hitTilt=0;
            if(BlossomHooks.hitTilt(14)!=0)throw new AssertionError("Zero tilt changed");
            BlossomHooks.modules.get("hittilt").enabled=false;
            if(BlossomHooks.hitTilt(14)!=14)throw new AssertionError("Disabled tilt not vanilla");
        }finally{initialized.setBoolean(null,old);BlossomHooks.settings.hitTilt=1;for(dev.blossom.core.Modules.Module m:BlossomHooks.modules.all())m.enabled=saved.get(m.id);}
        System.out.println("Disabled rendering/input/network hooks passed without Minecraft or OpenGL");
    }
}
