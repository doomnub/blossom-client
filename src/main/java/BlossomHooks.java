import dev.blossom.core.*;
import java.nio.file.*;
import java.util.*;
import org.lwjgl.input.*;
/** Thin 1.8.9 integration layer. Named core classes contain game-independent logic. */
public final class BlossomHooks {
    public static final Modules modules=new Modules();
    public static final ClientSettings settings=new ClientSettings();
    private static boolean crosshairAllowed;
    public static final ClickWindow left=new ClickWindow(), right=new ClickWindow();
    public static final FrameStats frames=new FrameStats(10000);
    public static final EventBus<Long> ticks=new EventBus<Long>();
    public static BlossomCapes capes;
    public static int accent=0xffff83b5;
    public static boolean background=true;
    public static String message="";
    private static boolean initialized,failed,shift,bright;
    private static boolean profileWritable=true;
    private static float savedGamma;
    private static ProfileStore store;
    private static long lastFrame,refresh;
    private static double[] fpsStats=new double[6];
    private static final java.util.concurrent.ExecutorService saves=java.util.concurrent.Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"Blossom profile writer");t.setDaemon(true);return t;});
    static {
        modules.add("fps","Frame statistics","HUD","Current or average FPS, with optional slow-frame statistics",true,.03f);
        modules.add("cps","Clicks per second","HUD","Actual left/right mouse input events",true,.09f);
        modules.add("keys","Keystrokes","HUD","Movement bindings and mouse state",true,.15f);
        modules.add("ping","Server ping","HUD","Server-provided player-list latency",true,.21f);
        modules.add("coords","Coordinates","HUD","Local player position",false,.27f);
        modules.add("direction","Direction","HUD","Camera heading",false,.33f);
        modules.add("memory","Heap memory","HUD","JVM used and maximum heap",false,.39f);
        modules.add("clock","Clock","HUD","Local system time",false,.45f);
        modules.add("armor","Armor and held item","HUD","Item names, durability and low-durability warnings",false,.51f);
        modules.add("potions","Potion effects","HUD","Active effects, levels and remaining durations",false,.65f);
        modules.add("resources","Resource counter","HUD","Iron, gold, diamond and emerald counts in your inventory",false,.78f);
        modules.add("movement","Toggle status","HUD","Local sprint/sneak latch status",false,.87f);
        modules.add("sprint","Toggle sprint","Controls","Press your vanilla sprint key to toggle; vanilla restrictions remain",false,0);
        modules.add("sneak","Toggle sneak","Controls","Press your vanilla sneak key to toggle; menus clear the latch",false,0);
        modules.add("crosshair","Custom crosshair","Visual","Original plus/dot crosshair with configurable dimensions",false,0);
        modules.add("zoom","Zoom","Visual","Smooth hold-to-zoom; scroll adjusts this hold only",true,0);
        modules.add("rawmouse","Raw mouse input","Controls","Unaccelerated Windows Raw Input / Linux XInput2 motion",false,0);
        modules.add("fov","FOV changer","Visual","Constant FOV or per-condition camera settings",false,0);
        modules.add("bright","Fullbright","Visual","Temporarily raises gamma; restores on disable",false,0);
        modules.add("cape","Local cape","Cosmetics","Replace your cape with a local texture animation",true,0);
    }
    private static void init() throws Exception {
        if(initialized) return;
        Path root=ave.A().v.toPath().resolve("blossom");
        store=new ProfileStore(root.resolve("profiles"));capes=new BlossomCapes(root.resolve("capes"));
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            saves.shutdown();
            try{saves.awaitTermination(3,java.util.concurrent.TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}
        },"Blossom save flush"));
        initialized=true;
        load("default");BlossomBindings.init();
    }
    public static void load(String name) {
        try {
            Properties p=store.load(name);
            for(Modules.Module m:modules.all()) {
                m.enabled=Boolean.parseBoolean(p.getProperty(m.id+".enabled",Boolean.toString(m.defaultEnabled)));
                m.x=ProfileStore.fraction(p,m.id+".x",m.x);m.y=ProfileStore.fraction(p,m.id+".y",m.y);m.loadStyle(p);
            }
            settings.load(p);
            background=Boolean.parseBoolean(p.getProperty("hud.background","true"));
            capes.load(p);message="Loaded "+name;
        } catch(Exception e){if(name.equals("default"))profileWritable=false;message="Profile not loaded: "+e.getMessage();}
    }
    public static void save(String name) {
        if(name.equals("default")&&!profileWritable){message="Corrupt default profile preserved; rename it before restart";return;}
        Properties p=new Properties();
        for(Modules.Module m:modules.all()) {
            p.setProperty(m.id+".enabled",Boolean.toString(m.enabled));
            p.setProperty(m.id+".x",Float.toString(m.x));p.setProperty(m.id+".y",Float.toString(m.y));m.saveStyle(p);
        }
        BlossomBindings.sync();settings.save(p);
        capes.save(p);p.setProperty("hud.background",Boolean.toString(background));
        saves.execute(()->{try{store.save(name,p);}catch(Exception e){System.err.println("[Blossom] Profile save failed: "+e.getMessage());}});
        message="Saving "+name;
    }
    public static void tick() {
        if(failed) return;
        try {
            init();BlossomBindings.sync();ave mc=ave.A();boolean down=BlossomBindings.held(BlossomBindings.menu);
            if(down&&!shift&&(mc.m==null||mc.m instanceof aya||mc.m instanceof axp)) mc.a(new BlossomMenu(mc.m));
            shift=down;
            boolean wantBright=modules.on("bright")&&mc.h!=null;
            if(wantBright&&!bright){savedGamma=mc.t.aJ;mc.t.aJ=12f;bright=true;}
            if(!wantBright&&bright){mc.t.aJ=savedGamma;bright=false;}
            BlossomControls.updateContext();BlossomHud.tick();
            capes.uploadReady();ticks.post(System.nanoTime());
        } catch(Exception|LinkageError e){failed=true;restoreSettings();System.err.println("[Blossom] Disabled after integration error");e.printStackTrace();}
    }
    private static void restoreSettings() {
        ave mc=ave.A();
        if(bright){mc.t.aJ=savedGamma;bright=false;}
    }
    public static void mainMenuInit(axu screen,java.util.List<avs> buttons,int width,int height) {
        try {init();buttons.add(new BlossomButton(42069,Math.max(4,width-108),6,104,20,"Blossom settings"));}
        catch(Exception|LinkageError e){System.err.println("[Blossom] Main menu entry unavailable: "+e.getMessage());}
    }
    public static void pauseMenuInit(axu screen,java.util.List<avs> buttons,int width,int height){
        try{init();buttons.add(new BlossomButton(42069,width/2-100,pauseButtonY(buttons,height),200,20,"Blossom settings"));}
        catch(Exception|LinkageError e){System.err.println("[Blossom] Pause entry unavailable");}
    }
    private static int pauseButtonY(java.util.List<avs> buttons,int height){for(avs b:buttons)if(b.k==0)return b.i-24;return height/4+56;}
    public static void rawMouse(avf helper){if(initialized&&!failed)BlossomMouse.sample(helper);}
    public static void mainMenuClick(axu screen,avs button) {
        if(button.k==42069&&initialized&&!failed)ave.A().a(new BlossomMenu(screen));
    }
    public static void mouseEvent(boolean available) {
        if(!available||!Mouse.getEventButtonState()) return;
        if(initialized&&!failed)BlossomControls.press(Mouse.getEventButton()-100);
        if(Mouse.getEventButton()==0) left.click(System.nanoTime());
        if(Mouse.getEventButton()==1) right.click(System.nanoTime());
    }
    public static void keyboardEvent(boolean available) {
        if(initialized&&!failed&&available&&Keyboard.getEventKeyState()&&!Keyboard.isRepeatEvent())BlossomControls.press(Keyboard.getEventKey());
    }
    public static float cameraFov(float vanilla,boolean world){return !initialized||failed?vanilla:BlossomCamera.fov(vanilla,world);}
    public static int mouseWheel(int delta){return !initialized||failed?delta:BlossomCamera.wheel(delta);}
    public static boolean keyDown(boolean original,avb binding) {
        return !initialized||failed?original:BlossomControls.down(original,binding);
    }
    public static boolean vanillaCrosshair(boolean original) {
        crosshairAllowed=original;
        return original&&(!initialized||failed||!modules.on("crosshair"));
    }
    public static void frame() {
        if(!initialized||failed)return;
        BlossomMouse.idle();
        long now=System.nanoTime();
        if(lastFrame!=0)frames.record(now-lastFrame);
        lastFrame=now;
    }
    public static jy cape(jy original,bet player) {
        if(!initialized||failed||player!=ave.A().h||!modules.on("cape")) return original;
        if(capes.hidden())return null;
        jy selected=capes.current();return selected==null?original:selected;
    }
    public static String text(Modules.Module m) {
        ave mc=ave.A();long now=System.nanoTime();
        switch(m.id) {
            case "fps":return FpsDisplay.format(ave.ai(),fpsStats,settings.fpsAverage,settings.fpsLows);
            case "cps":return left.count(now,1000000000L)+" | "+right.count(now,1000000000L)+" CPS";
            case "keys":return (mc.t.Y.d()?"[W]":" W ")+(mc.t.ab.d()?"[A]":" A ")+(mc.t.Z.d()?"[S]":" S ")+(mc.t.aa.d()?"[D]":" D ")+(mc.t.ac.d()?" [SPACE]":" SPACE")+(Mouse.isButtonDown(0)?" [L]":" L")+(Mouse.isButtonDown(1)?" [R]":" R");
            case "movement":return BlossomControls.status();
            case "armor":return "Armor: empty";
            case "potions":return "No potion effects";
            case "resources":return "Iron 0 | Gold 0 | Diamond 0 | Emerald 0";
            case "coords":return mc.h==null?"XYZ —":String.format(Locale.ROOT,"XYZ %.1f / %.1f / %.1f",mc.h.s,mc.h.t,mc.h.u);
            case "direction":return mc.h==null?"Direction —":"Heading "+Math.round(((mc.h.y%360)+360)%360)+"°";
            case "memory":Runtime r=Runtime.getRuntime();return "Heap "+((r.totalMemory()-r.freeMemory())/1048576)+" / "+r.maxMemory()/1048576+" MiB";
            case "clock":return java.time.LocalTime.now().withNano(0).toString();
            case "ping":bdc info=mc.h==null||mc.u()==null?null:mc.u().a(mc.h.aK());return info==null?"Ping unknown":info.c()+" ms (server)";
            default:return m.name;
        }
    }
    public static int x(Modules.Module m,int width,int elementWidth){return Math.round(m.x*Math.max(0,width-elementWidth));}
    public static int y(Modules.Module m,int height){return Math.round(m.y*Math.max(0,height-16));}
    public static void hud() {
        if(!initialized||failed) return;
        long now=System.nanoTime();
        if(now-refresh>1000000000L){fpsStats=frames.snapshot();refresh=now;}
        if(ave.A().m instanceof BlossomHudEditor) return;
        drawHud();
    }
    public static void drawHud() {
        BlossomHud.draw();
        if(modules.on("crosshair")&&crosshairAllowed&&ave.A().m==null) {
            avr r=new avr(ave.A());int x=r.a()/2,y=r.b()/2,t=settings.crosshairThickness;
            int start=-t/2,length=settings.crosshairLength,gap=settings.crosshairGap,color=settings.crosshairColor;
            avp.a(x-gap-length,y+start,x-gap,y+start+t,color);
            avp.a(x+gap+1,y+start,x+gap+length+1,y+start+t,color);
            avp.a(x+start,y-gap-length,x+start+t,y-gap,color);
            avp.a(x+start,y+gap+1,x+start+t,y+gap+length+1,color);
            if(settings.crosshairDot)avp.a(x+start,y+start,x+start+t,y+start+t,color);
        }
    }
}
