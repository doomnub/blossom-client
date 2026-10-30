import dev.blossom.core.PerformanceSettings;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

/** All OpenGL work remains on the render thread. Simulation and packets stay vanilla. */
public final class BlossomPerformance {
    private static boolean batching;
    private static int immediateBuilds;
    private static long immediateNanos;
    private static int workers=2;
    private static final dev.blossom.core.AdaptiveBudget budget=new dev.blossom.core.AdaptiveBudget();
    private static long lastFrame,waitNanos;
    private static Object world;
    private static boolean cameraReady;
    private static double cameraX,cameraY,cameraZ,forwardX,forwardY,forwardZ;
    public static void camera(pk entity,double partial){
        if(!enabled()||!settings().prioritize){cameraReady=false;return;}
        cameraX=entity.P+(entity.s-entity.P)*partial;cameraY=entity.Q+(entity.t-entity.Q)*partial;cameraZ=entity.R+(entity.u-entity.R)*partial;
        double yaw=entity.y*Math.PI/180,pitch=entity.z*Math.PI/180;
        forwardX=-Math.sin(yaw)*Math.cos(pitch);forwardY=-Math.sin(pitch);forwardZ=Math.cos(yaw)*Math.cos(pitch);cameraReady=true;
    }
    public static java.util.concurrent.BlockingQueue<?> dispatcherQueue(int capacity){return capacity==100?chunkQueue(capacity):new java.util.concurrent.ArrayBlockingQueue<Object>(capacity);}
    public static java.util.concurrent.BlockingQueue<bhn> chunkQueue(int capacity){return new dev.blossom.core.BoundedWorkQueue<bhn>(capacity,task->{
        if(!cameraReady||!enabled()||!settings().prioritize)return 0;
        cj origin=task.b().j();double x=origin.n()+8-cameraX,y=origin.o()+8-cameraY,z=origin.p()+8-cameraZ;
        double distance=Math.sqrt(x*x+y*y+z*z);double facing=distance<1?1:(x*forwardX+y*forwardY+z*forwardZ)/distance;
        return distance*(1-.35*facing);
    });}
    public static void displayUpdate(){
        if(!enabled()||!settings().adaptive){Display.update();return;}
        long start=System.nanoTime();try{Display.update();}finally{waitNanos+=System.nanoTime()-start;}
    }
    public static void sync(int fps){
        if(!enabled()||!settings().adaptive){Display.sync(fps);return;}
        long start=System.nanoTime();try{Display.sync(fps);}finally{waitNanos+=System.nanoTime()-start;}
    }
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
        BlossomVisibility.reset();
        if(!enabled()){world=null;cameraReady=false;lastFrame=waitNanos=0;immediateBuilds=0;immediateNanos=0;budget.reset();BlossomLightmap.clear();BlossomBenchmark.frame();return;}
        Object currentWorld=ave.A().f;
        if(currentWorld!=world){world=currentWorld;lastFrame=0;budget.reset();cameraReady=false;BlossomLightmap.clear();}
        if(enabled()&&settings().adaptive){long now=System.nanoTime();if(lastFrame!=0)budget.sample(Math.max(1,now-lastFrame-waitNanos));lastFrame=now;}else{lastFrame=0;budget.reset();}
        waitNanos=0;immediateBuilds=0;immediateNanos=0;
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
    public static boolean drainUploads(java.util.Queue<? extends Runnable> queue,long deadline){
        boolean ran=false;
        do {
            Runnable task;
            synchronized(queue){task=queue.poll();}
            if(task==null)break;
            // GPU submission must not hold the producer lock used by chunk workers.
            long begin=BlossomBenchmark.begin("blossom.chunk.uploadTask");boolean success=false;
            try{task.run();success=true;}finally{BlossomBenchmark.end("blossom.chunk.uploadTask",begin,success);}
            ran=true;
        }while(deadline!=0&&deadline-System.nanoTime()>=0);
        return ran;
    }
    public static long uploadDeadline(long vanilla){
        if(!enabled()||!settings().chunkScheduling||vanilla==0)return vanilla;
        long budget=settings().adaptive?BlossomPerformance.budget.nanos(settings().chunkBudgetMs,settings().targetFps):(long)(settings().chunkBudgetMs*1000000L);
        // Upload at least one ready task (vanilla behavior), then yield to rendering.
        return System.nanoTime()+Math.max(0,budget-immediateNanos);
    }
}
