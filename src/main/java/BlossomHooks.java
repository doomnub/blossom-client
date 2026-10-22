import dev.blossom.core.*;
import java.nio.file.*;
import java.util.*;
import org.lwjgl.input.*;
/** Thin 1.8.9 integration layer. Named core classes contain game-independent logic. */
public final class BlossomHooks {
    public static final Modules modules=new Modules();
    public static final ClickWindow left=new ClickWindow(), right=new ClickWindow();
    public static final FrameStats frames=new FrameStats(10000);
    public static final EventBus<Long> ticks=new EventBus<Long>();
    public static BlossomCapes capes;
    public static int accent=0xffff83b5;
    public static boolean background=true;
    public static String message="";
    private static boolean initialized,failed,shift,zooming,bright;
    private static boolean profileWritable=true;
    private static float savedFov,savedGamma;
    private static ProfileStore store;
    private static long lastFrame,refresh;
    private static String fps="Collecting frame samples";
    private static final java.util.concurrent.ExecutorService saves=java.util.concurrent.Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"Blossom profile writer");t.setDaemon(true);return t;});
    static {
        modules.add("fps","Frame statistics","HUD","Average FPS and slowest 1% / 0.1% frames",true,.03f);
        modules.add("cps","Clicks per second","HUD","Actual left/right mouse input events",true,.09f);
        modules.add("keys","Keystrokes","HUD","Movement bindings and mouse state",true,.15f);
        modules.add("ping","Server ping","HUD","Server-provided player-list latency",true,.21f);
        modules.add("coords","Coordinates","HUD","Local player position",false,.27f);
        modules.add("direction","Direction","HUD","Camera heading",false,.33f);
        modules.add("memory","Heap memory","HUD","JVM used and maximum heap",false,.39f);
        modules.add("clock","Clock","HUD","Local system time",false,.45f);
        modules.add("zoom","Zoom (hold C)","Visual","Hold C to divide FOV by four",true,0);
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
        load("default");
    }
    public static void load(String name) {
        try {
            Properties p=store.load(name);
            for(Modules.Module m:modules.all()) {
                m.enabled=Boolean.parseBoolean(p.getProperty(m.id+".enabled",Boolean.toString(m.defaultEnabled)));
                m.x=ProfileStore.fraction(p,m.id+".x",m.x);m.y=ProfileStore.fraction(p,m.id+".y",m.y);
            }
            background=Boolean.parseBoolean(p.getProperty("hud.background","true"));
            capes.select(p.getProperty("cape.selected",""));message="Loaded "+name;
        } catch(Exception e){if(name.equals("default"))profileWritable=false;message="Profile not loaded: "+e.getMessage();}
    }
    public static void save(String name) {
        if(name.equals("default")&&!profileWritable){message="Corrupt default profile preserved; rename it before restart";return;}
        Properties p=new Properties();
        for(Modules.Module m:modules.all()) {
            p.setProperty(m.id+".enabled",Boolean.toString(m.enabled));
            p.setProperty(m.id+".x",Float.toString(m.x));p.setProperty(m.id+".y",Float.toString(m.y));
        }
        p.setProperty("cape.selected",capes.selected);p.setProperty("hud.background",Boolean.toString(background));
        saves.execute(()->{try{store.save(name,p);}catch(Exception e){System.err.println("[Blossom] Profile save failed: "+e.getMessage());}});
        message="Saving "+name;
    }
    public static void tick() {
        if(failed) return;
        try {
            init();ave mc=ave.A();boolean down=Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            if(down&&!shift&&!(mc.m instanceof BlossomMenu)&&!(mc.m instanceof BlossomHudEditor)) mc.a(new BlossomMenu(mc.m));
            shift=down;
            boolean zoom=modules.on("zoom")&&mc.m==null&&mc.h!=null&&Keyboard.isKeyDown(Keyboard.KEY_C);
            if(zoom&&!zooming){savedFov=mc.t.aI;mc.t.aI=savedFov/4f;zooming=true;}
            if(!zoom&&zooming){mc.t.aI=savedFov;zooming=false;}
            boolean wantBright=modules.on("bright")&&mc.h!=null;
            if(wantBright&&!bright){savedGamma=mc.t.aJ;mc.t.aJ=12f;bright=true;}
            if(!wantBright&&bright){mc.t.aJ=savedGamma;bright=false;}
            capes.uploadReady();ticks.post(System.nanoTime());
        } catch(Exception|LinkageError e){failed=true;restoreSettings();System.err.println("[Blossom] Disabled after integration error");e.printStackTrace();}
    }
    private static void restoreSettings() {
        ave mc=ave.A();
        if(zooming){mc.t.aI=savedFov;zooming=false;}
        if(bright){mc.t.aJ=savedGamma;bright=false;}
    }
    public static void mouseEvent(boolean available) {
        if(!available||!Mouse.getEventButtonState()) return;
        if(Mouse.getEventButton()==0) left.click(System.nanoTime());
        if(Mouse.getEventButton()==1) right.click(System.nanoTime());
    }
    public static jy cape(jy original,bet player) {
        if(!initialized||failed||player!=ave.A().h||!modules.on("cape")) return original;
        jy selected=capes.current();return selected==null?original:selected;
    }
    public static String text(Modules.Module m) {
        ave mc=ave.A();long now=System.nanoTime();
        switch(m.id) {
            case "fps":return fps;
            case "cps":return left.count(now,1000000000L)+" | "+right.count(now,1000000000L)+" CPS";
            case "keys":return (mc.t.Y.d()?"[W]":" W ")+(mc.t.ab.d()?"[A]":" A ")+(mc.t.Z.d()?"[S]":" S ")+(mc.t.aa.d()?"[D]":" D ")+(mc.t.ac.d()?" [SPACE]":" SPACE")+(Mouse.isButtonDown(0)?" [L]":" L")+(Mouse.isButtonDown(1)?" [R]":" R");
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
        if(lastFrame!=0&&now-lastFrame<1000000000L) frames.record(now-lastFrame);
        lastFrame=now;
        if(now-refresh>1000000000L){double[] s=frames.snapshot();fps=String.format(Locale.ROOT,"%.0f FPS | 1%% %.0f | 0.1%% %.0f%s",s[0],s[1],s[2],s[5]<1000?" (warming)":"");refresh=now;}
        if(ave.A().m instanceof BlossomHudEditor) return;
        drawHud();
    }
    public static void drawHud() {
        ave mc=ave.A();avr size=new avr(mc);
        for(Modules.Module m:modules.all()) if(m.enabled&&m.category.equals("HUD")) {
            String text=text(m);int width=mc.k.a(text)+8, x=x(m,size.a(),width),y=y(m,size.b());
            if(background) avp.a(x,y,x+width,y+16,0x99201a23);
            mc.k.a(text,(float)x+4,(float)y+4,accent);
        }
    }
}
