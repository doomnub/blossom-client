import dev.blossom.core.PerformanceSettings;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

/** All OpenGL work remains on the render thread. Simulation and packets stay vanilla. */
public final class BlossomPerformance {
    private static boolean batching;
    private static int immediateBuilds;
    private static long immediateNanos;
    private static int workers=2;
    public static int workerCount(){
        // Dispatcher construction precedes the first client tick/profile initialization.
        // Read only the saved startup choice; do not alter the fixed buffer pool or shutdown count.
        try{
            java.nio.file.Path file=ave.A().v.toPath().resolve("blossom/profiles/default.properties");
            java.util.Properties p=new java.util.Properties();
            if(java.nio.file.Files.isRegularFile(file)){
                if(java.nio.file.Files.size(file)>1048576)return 2;
                try(java.io.Reader reader=java.nio.file.Files.newBufferedReader(file)){p.load(reader);}
                if(!"1".equals(p.getProperty("schema")))return 2;
            }
            if(!Boolean.parseBoolean(p.getProperty("performance.enabled","true")))return 2;
            PerformanceSettings saved=new PerformanceSettings();saved.load(p);
            workers=PerformanceSettings.workers(saved.chunkWorkers,Runtime.getRuntime().availableProcessors());
            return workers;
        }catch(Exception e){return 2;}
    }
    public static int activeWorkers(){return workers;}
    public static boolean enabled(){return BlossomHooks.modules.on("performance");}
    private static PerformanceSettings settings(){return BlossomHooks.settings.performance;}
    public static boolean batchModels(){return enabled()&&settings().modelBatching;}
    public static void frame(){
        immediateBuilds=0;immediateNanos=0;
        // Poll after the preceding frame's limiter wait, before vanilla consumes input.
        if(enabled()&&settings().earlyInput&&Display.isCreated())Display.processMessages();
        BlossomBenchmark.frame();
    }
    public static boolean compiled(boolean compiled,boolean batched){return compiled&&batched==batchModels();}
    public static void clearModel(int list){if(list!=0)GL11.glDeleteLists(list,1);}
    public static void beginModels(){
        batching=batchModels();
        if(batching)bfx.a().c().a(GL11.GL_QUADS,bms.c);
    }
    public static void endModels(){if(batching){try{bfx.a().b();}finally{batching=false;}}}
    public static void beginFace(bfd buffer,int mode,bmu format){if(!batching)buffer.a(mode,format);}
    public static void endFace(bfx tessellator){if(!batching)tessellator.b();}
    public static void worldGc(){if(!enabled()||!settings().worldGc)System.gc();}
    public static boolean near(boolean vanilla){
        // One nearby section remains immediate for block feedback. The rest use vanilla workers.
        return vanilla&&(!enabled()||!settings().chunkScheduling||immediateBuilds++==0);
    }
    public static boolean buildNear(bho dispatcher,bht chunk){
        if(!enabled()||!settings().chunkScheduling)return dispatcher.b(chunk);
        long start=System.nanoTime();
        try{return dispatcher.b(chunk);}finally{immediateNanos+=System.nanoTime()-start;}
    }
    public static long uploadDeadline(long vanilla){
        if(!enabled()||!settings().chunkScheduling||vanilla==0)return vanilla;
        long budget=(long)(settings().chunkBudgetMs*1000000L);
        // Upload at least one ready task (vanilla behavior), then yield to rendering.
        return System.nanoTime()+Math.max(0,budget-immediateNanos);
    }
}
