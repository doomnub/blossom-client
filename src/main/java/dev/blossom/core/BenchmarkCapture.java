package dev.blossom.core;
import java.lang.management.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.zip.*;
/** Capture storage is bounded. Sampling is opt-in; output/compression runs after detaching it. */
public final class BenchmarkCapture {
    public long started;public final Thread owner=Thread.currentThread();
    public final SectionTimings sections=new SectionTimings();
    private final long[][] frames=new long[3+SectionTimings.COLUMNS.length][120000];
    private final long[] gpuFrame=new long[120000],gpuNanos=new long[120000];
    private int gpuCount;public long gpuSkipped,gpuDiscarded;public String gpuStatus="not started",gpuCapabilities="not queried";
    public void gpu(long frame,long nanos){if(gpuCount<gpuFrame.length){gpuFrame[gpuCount]=frame;gpuNanos[gpuCount++]=nanos;}}
    private int size;private long previous,previousCpu=-1,nextResource;
    private final ThreadMXBean threads=ManagementFactory.getThreadMXBean();
    private final List<GarbageCollectorMXBean> collectors=ManagementFactory.getGarbageCollectorMXBeans();
    private final MemoryMXBean memory=ManagementFactory.getMemoryMXBean();
    private final List<BufferPoolMXBean> buffers=ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
    private final java.lang.reflect.Method processCpu;
    private final OperatingSystemMXBean os=ManagementFactory.getOperatingSystemMXBean();
    private long gcCountStart,gcMsStart;
    private final ArrayList<long[]> resources=new ArrayList<long[]>(256);
    private final long[] jobs=new long[32]; // calls, elapsed ns, maximum ns, failures per category and thread type
    public static final String[] JOBS={"main_chunk_rebuild","worker_chunk_rebuild","main_translucent_sort","worker_translucent_sort","main_upload_task","worker_upload_task","main_chunk_drain","worker_chunk_drain"};
    private int pending=-1,uploads=-1,freeBuffers=-1;private boolean closed;
    private long gcCountEnd,gcMsEnd;
    public BenchmarkCapture(long now){
        started=previous=now;previousCpu=cpu();gcCountStart=gcCount();gcMsStart=gcMs();
        java.lang.reflect.Method method=null;try{method=Class.forName("com.sun.management.OperatingSystemMXBean").getMethod("getProcessCpuTime");}catch(Exception ignored){}processCpu=method;
    }
    public void arm(long now){started=previous=now;previousCpu=cpu();gcCountStart=gcCount();gcMsStart=gcMs();nextResource=now;}
    private long cpu(){return threads.isCurrentThreadCpuTimeSupported()&&threads.isThreadCpuTimeEnabled()?threads.getCurrentThreadCpuTime():-1;}
    private long gcCount(){long n=0;for(GarbageCollectorMXBean g:collectors){long v=g.getCollectionCount();if(v>=0)n+=v;}return n;}
    private long gcMs(){long n=0;for(GarbageCollectorMXBean g:collectors){long v=g.getCollectionTime();if(v>=0)n+=v;}return n;}
    public void queues(int pending,int uploads,int free){this.pending=pending;this.uploads=uploads;this.freeBuffers=free;}
    public synchronized void job(int kind,long begin,long end,boolean success){if(closed||begin<started||kind<0||kind>=4)return;int i=(kind*2+(Thread.currentThread()==owner?0:1))*4;long dt=Math.max(0,end-begin);jobs[i]++;jobs[i+1]+=dt;jobs[i+2]=Math.max(jobs[i+2],dt);if(!success)jobs[i+3]++;}
    public boolean frame(long now){
        if(size>=frames[0].length)return true;
        sections.boundary(now);long currentCpu=cpu();
        frames[0][size]=now-started;frames[1][size]=now-previous;frames[2][size]=currentCpu<0||previousCpu<0?-1:Math.max(0,currentCpu-previousCpu);
        for(int i=0;i<sections.frame.length;i++)frames[3+i][size]=sections.frame[i];
        sections.resetFrame();size++;previous=now;previousCpu=currentCpu;
        if(now>=nextResource){sample(now);nextResource=now+250000000L;}
        return size==frames[0].length;
    }
    private void sample(long now){
        if(resources.size()>=512)return;
        MemoryUsage heap=memory.getHeapMemoryUsage(),nonheap=memory.getNonHeapMemoryUsage();
        long direct=-1,mapped=-1,process=-1;for(BufferPoolMXBean pool:buffers){if(pool.getName().equals("direct"))direct=pool.getMemoryUsed();if(pool.getName().equals("mapped"))mapped=pool.getMemoryUsed();}
        if(processCpu!=null)try{process=((Number)processCpu.invoke(os)).longValue();}catch(Exception ignored){}
        resources.add(new long[]{now-started,heap.getUsed(),heap.getCommitted(),heap.getMax(),nonheap.getUsed(),direct,mapped,gcCount()-gcCountStart,gcMs()-gcMsStart,threads.getThreadCount(),process,pending,uploads,freeBuffers});
    }
    public synchronized void close(long now){closed=true;sections.boundary(now);sample(now);gcCountEnd=gcCount();gcMsEnd=gcMs();}
    public int size(){return size;}
    private static void entry(ZipOutputStream zip,String name,String value)throws IOException{zip.putNextEntry(new ZipEntry(name));zip.write(value.getBytes(StandardCharsets.UTF_8));zip.closeEntry();}
    public void write(Path file,String metadata)throws IOException{
        long[] times=Arrays.copyOf(frames[1],size);
        String summary=metadata+"\nGPU capabilities (availability, not active renderer features): "+gpuCapabilities+"\nGPU timer: "+gpuStatus+"\nGPU samples: "+gpuCount+"\nGPU skipped (pool full): "+gpuSkipped+"\nGPU pending/invalid discarded: "+gpuDiscarded+"\n"+"\n"+FrameCapture.summary(times)+"\nGC collections: "+(gcCountEnd-gcCountStart)+"\nGC collection time ms: "+(gcMsEnd-gcMsStart)+"\nProfiler forced closes: "+sections.forced+"\nProfiler unmatched ends: "+sections.unmatched+"\nProfiler overflow events: "+sections.overflow+"\n";
        long peakHeap=0,peakCommitted=0,peakDirect=0;for(long[] r:resources){peakHeap=Math.max(peakHeap,r[1]);peakCommitted=Math.max(peakCommitted,r[2]);peakDirect=Math.max(peakDirect,r[5]);}
        summary+="\nSampled peak heap used bytes: "+peakHeap+"\nSampled peak heap committed bytes: "+peakCommitted+"\nSampled peak direct buffer bytes: "+peakDirect+"\nResource samples: "+resources.size()+"\n";
        List<SectionTimings.Node> ranked=new ArrayList<SectionTimings.Node>(sections.nodes);ranked.sort((a,b)->Long.compare(b.exclusive,a.exclusive));
        summary+="\nLargest measured self-time sections (wall time; waits included):\n";
        for(int i=0;i<Math.min(20,ranked.size());i++){SectionTimings.Node n=ranked.get(i);summary+=String.format(Locale.ROOT,"%.3f ms  %s  (%d calls)\n",n.exclusive/1e6,n.path,n.calls);}
        summary+="\nSection timing is instrumented CPU-side wall time. gpu.csv records asynchronous GPU timestamp intervals around the game renderer, including GPU dependencies and idle gaps; it is not per-stage execution time or physical input latency.\nInclusive nested sections overlap; do not add them together. Worker times overlap frames and other workers.\nChunk job counts count completed method invocations, including early-out/cancelled work; failures are thrown exceptions.\nGC collection time is JVM-reported collector time, not a measurement of all stop-the-world pauses.\nResources sampled at most every 250 ms; bytes are bytes. -1 means unavailable. CPU counters are nanoseconds.\nNo process RSS, VRAM, per-stage GPU execution time, disk IO, server generation or network latency measurement is claimed.\nCapture retains stalls/menu/world transitions. Instrumentation adds overhead. Profiles/configuration contain no tokens, chat, server address or coordinates.\n";
        try(ZipOutputStream zip=new ZipOutputStream(Files.newOutputStream(file))){
            entry(zip,"summary.txt",summary);
            StringBuilder gpuCsv=new StringBuilder("frame_index,game_renderer_gpu_interval_ns\n");for(int i=0;i<gpuCount;i++)gpuCsv.append(gpuFrame[i]).append(',').append(gpuNanos[i]).append('\n');entry(zip,"gpu.csv",gpuCsv.toString());entry(zip,"sections.csv",sections.csv());
            StringBuilder jobsCsv=new StringBuilder("job,calls,total_ns,max_ns,exceptions\n");for(int i=0;i<JOBS.length;i++){jobsCsv.append(JOBS[i]);for(int j=0;j<4;j++)jobsCsv.append(',').append(jobs[4*i+j]);jobsCsv.append('\n');}entry(zip,"chunk-jobs.csv",jobsCsv.toString());
            StringBuilder data=new StringBuilder("frame_index,elapsed_ns,frame_ns,main_thread_cpu_ns");for(String c:SectionTimings.COLUMNS)data.append(',').append(c).append("_inclusive_ns");data.append('\n');
            for(int i=0;i<size;i++){data.append(i).append(',');for(int c=0;c<frames.length;c++){if(c>0)data.append(',');data.append(frames[c][i]);}data.append('\n');}entry(zip,"frames.csv",data.toString());
            data=new StringBuilder("elapsed_ns,heap_used_bytes,heap_committed_bytes,heap_max_bytes,nonheap_used_bytes,direct_buffer_bytes,mapped_buffer_bytes,gc_count_delta,gc_ms_delta,thread_count,process_cpu_ns,pending_chunk_jobs,pending_uploads,free_chunk_buffers\n");for(long[] row:resources){for(int i=0;i<row.length;i++){if(i>0)data.append(',');data.append(row[i]);}data.append('\n');}entry(zip,"resources.csv",data.toString());
        }
    }
}
