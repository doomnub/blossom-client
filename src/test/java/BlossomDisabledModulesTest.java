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
            BlossomLegacy.beginGlint(null);BlossomLegacy.glintUV();BlossomLegacy.endGlint();BlossomLegacy.begin(null, .5f);BlossomLegacy.end();BlossomLegacy.tick();
            if(BlossomLegacy.eye(1.54f,.5f)!=1.54f||BlossomLegacy.swing(.3f)!=.3f||BlossomLegacy.useSwing(.2f)!=.2f||BlossomLegacy.equipStack(null)!=null||BlossomLegacy.damage(false)||BlossomLegacy.glintColor(123)!=123)throw new AssertionError("Disabled classic visuals changed vanilla values");
            BlossomHands.begin(null);BlossomHands.end();BlossomHands.pose(null,null);BlossomHands.beginThird(null);BlossomHands.endThird();
            BlossomHud.tick();BlossomNetwork.tick();BlossomPing.tick();BlossomVisuals.idle();BlossomVisuals.added(null);
            if(BlossomBenchmark.begin("inactive")!=0)throw new AssertionError("Inactive capture is doing work");
            java.util.Queue<Runnable> queue=new java.util.ArrayDeque<Runnable>();final int[] ran={0};
            queue.add(()->{if(Thread.holdsLock(queue))throw new AssertionError("Upload holds producer lock");ran[0]++;});queue.add(()->ran[0]++);
            if(!BlossomPerformance.drainUploads(queue,0)||ran[0]!=1||queue.size()!=1)throw new AssertionError("Zero deadline must run one upload");
            BlossomPerformance.drainUploads(queue,System.nanoTime()+1000000000L);
            if(ran[0]!=2||!queue.isEmpty())throw new AssertionError("Upload queue did not drain");
            BlossomHooks.frames.record(1000);BlossomHooks.frame();
            BlossomHooks.modules.get("hittilt").enabled=true;BlossomHooks.settings.hitTilt=0;
            if(BlossomHooks.hitTilt(14)!=0)throw new AssertionError("Zero tilt changed");
            BlossomHooks.modules.get("hittilt").enabled=false;
            if(BlossomHooks.hitTilt(14)!=14)throw new AssertionError("Disabled tilt not vanilla");
        }finally{initialized.setBoolean(null,old);BlossomHooks.settings.hitTilt=1;for(dev.blossom.core.Modules.Module m:BlossomHooks.modules.all())m.enabled=saved.get(m.id);}
        System.out.println("Disabled rendering/input/network hooks passed without Minecraft or OpenGL");
    }
}
