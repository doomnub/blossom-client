/** Render-thread-only batching within one fancy-cloud pass; never owns GPU resources. */
public final class BlossomCloudBatch {
    private static final boolean ALLOWED=!"false".equalsIgnoreCase(System.getProperty("blossom.cloudBatching","true"));
    private static boolean active,building;
    private static bfd buffer;
    private static bfx tess;
    public static boolean enabled(){return ALLOWED&&BlossomHooks.settings.performance.cloudBatching&&BlossomHooks.modules.on("performance");}
    public static void enter(){abort();active=enabled();}
    public static void begin(bfd next,int mode,bmu format){
        if(!active){next.a(mode,format);return;}
        if(building&&(buffer!=next||buffer.i()!=mode||buffer.g()!=format))flush();
        if(!building){next.a(mode,format);buffer=next;building=true;}
    }
    public static void draw(bfx next){
        if(!active){next.b();return;}
        tess=next;
        // Bound accumulation using vanilla's existing buffer. Flush only at whole-cell boundaries.
        if(buffer.h()>=4096)flush();
    }
    public static void flush(){
        if(!building)return;
        bfx out=tess;
        if(out==null)throw new IllegalStateException("Cloud batch has no completed cell");
        building=false;buffer=null;tess=null;
        out.b();
    }
    public static void exit(){try{flush();}finally{active=false;buffer=null;tess=null;}}
    public static void abort(){
        bfd old=buffer;boolean pending=building;
        active=false;building=false;buffer=null;tess=null;
        if(pending&&old!=null){try{old.e();old.b();}catch(RuntimeException ignored){}}
    }
}
