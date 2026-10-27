import dev.blossom.core.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;

/** User-started frame capture; no per-frame disk IO and no implied latency measurement. */
public final class BlossomBenchmark {
    private static FrameCapture capture;
    private static long start,last;
    private static String metadata;
    private static Path folder;
    public static boolean active(){return capture!=null;}
    public static void start(){
        if(active())return;
        ave mc=ave.A();
        capture=new FrameCapture();start=last=0;
        folder=mc.v.toPath().resolve("blossom/benchmarks");
        metadata="Blossom 0.2.9 frame capture\nJava: "+System.getProperty("java.version")+"\nOS: "+System.getProperty("os.name")+"\nResolution: "+mc.d+"x"+mc.e+"\nRender distance: "+mc.t.c+"\nFPS limit: "+mc.t.g+"\nVSync: "+mc.t.t+"\nPerformance module: "+BlossomPerformance.enabled()+"\nActive chunk workers: "+BlossomPerformance.activeWorkers()+"\n";
        java.util.Properties p=new java.util.Properties();BlossomHooks.settings.performance.save(p);
        for(String key:new java.util.TreeSet<String>(p.stringPropertyNames()))metadata+=key+"="+p.getProperty(key)+"\n";
        BlossomHooks.message="Capture starts when you return to the world; records 60 seconds";
    }
    public static void frame(){
        if(capture==null)return;
        ave mc=ave.A();long now=System.nanoTime();
        if(start==0){if(mc.h==null||mc.m!=null)return;start=last=now;return;}
        // Retain stalls and menu transitions after capture starts; never trim slow frames.
        capture.record(now-last);last=now;
        if(now-start>=60000000000L||capture.full())finish();
    }
    public static void finish(){
        if(capture==null)return;
        final long[] frames=capture.copy();capture=null;
        final String meta=metadata;final Path dir=folder;
        BlossomHooks.message="Saving capture...";
        Thread writer=new Thread(()->{
            try{
                Files.createDirectories(dir);
                String name=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
                Path csv=dir.resolve(name+".csv"),report=dir.resolve(name+".txt");
                try(java.io.BufferedWriter out=Files.newBufferedWriter(csv,StandardCharsets.UTF_8)){
                    out.write("frame,frame_time_ns\n");
                    for(int i=0;i<frames.length;i++){out.write(Integer.toString(i));out.write(',');out.write(Long.toString(frames[i]));out.newLine();}
                }
                Files.write(report,(meta+"\n"+FrameCapture.summary(frames)+"\nInput latency is not measured by frame times.\n").getBytes(StandardCharsets.UTF_8));
                System.out.println("[Blossom] Benchmark saved: "+report);
                BlossomHooks.message="Capture saved under blossom/benchmarks (see Logs)";
            }catch(Exception e){BlossomHooks.message="Capture could not be saved; see Logs";System.err.println("[Blossom] Benchmark could not be saved: "+e.getMessage());}
        },"Blossom benchmark writer");writer.setDaemon(true);writer.start();
    }
}
