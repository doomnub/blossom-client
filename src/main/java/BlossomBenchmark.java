import dev.blossom.core.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.lwjgl.opengl.GL11;

/** Opt-in capture. Never enables vanilla's allocating debug profiler or forces GPU synchronization. */
public final class BlossomBenchmark {
    private static volatile BenchmarkCapture capture;
    private static volatile boolean armed;
    private static String metadata;
    private static Path folder;
    public static BenchmarkCapture current(){return armed?capture:null;}
    public static boolean active(){return capture!=null;}
    public static void start(){
        if(active())return;
        ave mc=ave.A();
        folder=mc.v.toPath().resolve("blossom/benchmarks");
        metadata="Blossom 0.2.9.1 detailed benchmark\nJava: "+System.getProperty("java.version")+"\nVM: "+System.getProperty("java.vm.name")+"\nOS: "+System.getProperty("os.name")+" "+System.getProperty("os.arch")+"\nCPU logical processors: "+Runtime.getRuntime().availableProcessors()+"\nGPU: "+GL11.glGetString(GL11.GL_RENDERER)+"\nGL: "+GL11.glGetString(GL11.GL_VERSION)+"\nResolution: "+mc.d+"x"+mc.e+"\nRender distance: "+mc.t.c+"\nFPS limit: "+mc.t.g+"\nVSync: "+mc.t.t+"\nActive chunk workers: "+BlossomPerformance.activeWorkers()+"\n";
        Properties p=new Properties();BlossomHooks.settings.performance.save(p);
        for(Modules.Module module:BlossomHooks.modules.all())p.setProperty("module."+module.id,Boolean.toString(module.enabled));
        p.setProperty("cloth.subdivisions",Integer.toString(BlossomHooks.settings.cloth.subdivisions));
        for(String key:new TreeSet<String>(p.stringPropertyNames()))metadata+=key+"="+p.getProperty(key)+"\n";
        // Allocate storage while the menu is open, before measured gameplay begins.
        armed=false;capture=new BenchmarkCapture(System.nanoTime());BlossomGpu.prepare(capture);
        BlossomHooks.message="Benchmark starts on return to world; captures 60 seconds";
    }
    public static void frame(){
        BenchmarkCapture c=capture;if(c==null)return;
        long now=System.nanoTime();
        if(!armed){if(ave.A().h==null||ave.A().m!=null)return;c.arm(now);armed=true;return;}
        boolean full=c.frame(now);
        if(full||now-c.started>=60000000000L)finish();
    }
    public static void sectionStart(Object profiler,String name){BenchmarkCapture c=capture;if(c!=null&&armed&&Thread.currentThread()==c.owner&&profiler==ave.A().A)c.sections.begin(name,System.nanoTime());}
    public static void sectionEnd(Object profiler){BenchmarkCapture c=capture;if(c!=null&&armed&&Thread.currentThread()==c.owner&&profiler==ave.A().A)c.sections.end(System.nanoTime());}
    public static long begin(String name){
        BenchmarkCapture c=capture;if(c==null||!armed)return 0;
        long now=System.nanoTime();if(Thread.currentThread()==c.owner)c.sections.begin(name,now);return now;
    }
    public static void end(String name,long begin,boolean success){
        if(begin==0)return;BenchmarkCapture c=capture;if(c==null||begin<c.started)return;
        long now=System.nanoTime();
        if(Thread.currentThread()==c.owner)c.sections.end(now);
        int kind=name.equals("blossom.chunk.rebuild")?0:name.equals("blossom.chunk.sort")?1:name.equals("blossom.chunk.uploadTask")?2:name.equals("blossom.chunk.uploads")?3:-1;
        if(kind>=0)c.job(kind,begin,now,success);
    }
    public static void queues(java.util.concurrent.BlockingQueue<?> jobs,Queue<?> uploads,java.util.concurrent.BlockingQueue<?> buffers){
        BenchmarkCapture c=capture;if(c==null||!armed)return;
        int pending; synchronized(uploads){pending=uploads.size();}c.queues(jobs.size(),pending,buffers.size());
    }
    public static void finish(){
        final BenchmarkCapture c=capture;if(c==null)return;
        BlossomGpu.finish(c);capture=null;armed=false;c.close(System.nanoTime());
        final String meta=metadata;final Path dir=folder;
        BlossomHooks.message="Saving benchmark...";
        Thread writer=new Thread(()->{
            Path partial=null;
            try{
                Files.createDirectories(dir);
                String name=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"))+"-"+java.util.UUID.randomUUID().toString().substring(0,8);
                Path report=dir.resolve(name+".benchmark.zip");partial=dir.resolve(name+".partial");
                c.write(partial,meta);
                Files.move(partial,report);
                System.out.println("[Blossom] Benchmark saved: "+report);
                BlossomHooks.message="Benchmark saved in blossom/benchmarks (see Logs)";
            }catch(Exception e){BlossomHooks.message="Benchmark save failed; see Logs";System.err.println("[Blossom] Benchmark save failed: "+e);if(partial!=null)try{Files.deleteIfExists(partial);}catch(Exception ignored){}}
        },"Blossom benchmark writer");writer.setDaemon(true);writer.start();
    }
}
