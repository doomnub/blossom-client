package dev.blossom;
import dev.blossom.core.*;
import java.nio.file.*;
import java.util.zip.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
public final class BenchmarkTest {
    private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    public static void run()throws Exception{
        SectionTimings s=new SectionTimings();s.begin("root",100);s.begin("child",120);s.end(150);s.end(200);
        check(s.nodes.get(0).inclusive==100&&s.nodes.get(0).exclusive==70,"Nested child time must be removed from parent's self time");
        check(s.nodes.get(1).inclusive==30,"Child duration");
        s.begin("unfinished",210);s.boundary(250);check(s.forced==1&&s.nodes.get(2).exclusive==40,"Boundary closes partial sections");
        for(int i=0;i<200;i++)s.begin("deep",300+i);for(int i=0;i<200;i++)s.end(1000+i);check(s.overflow>0,"Depth limit protects capture");
        BenchmarkCapture capture=new BenchmarkCapture(System.nanoTime());long now=System.nanoTime();capture.arm(now);
        capture.sections.begin("tick",now+100);capture.sections.end(now+1000);capture.queues(5,2,3);
        capture.frame(now+10000000);capture.frame(now+1000000000);
        capture.job(0,now+1,now+101,true);
        Thread worker=new Thread(()->capture.job(0,now+2,now+202,false));worker.start();worker.join();
        capture.gpu(0,12345);capture.close(now+1000000001);capture.job(0,now+1,now+9999,true);
        Path zip=Files.createTempFile("blossom-benchmark-", ".zip");
        try{
            capture.write(zip,"test metadata");
            try(ZipFile z=new ZipFile(zip.toFile())){
                check(z.size()==6,"One complete benchmark archive");
                check(read(z,"gpu.csv").contains("0,12345"),"GPU samples identify source frame");
                String jobs=read(z,"chunk-jobs.csv");check(jobs.contains("main_chunk_rebuild,1,100,100,0")&&jobs.contains("worker_chunk_rebuild,1,200,200,1"),"Worker/main/exception accounting separated and closed capture frozen");
                String frames=read(z,"frames.csv");check(frames.contains("1000000000,990000000"),"Long stalls retained");
                check(read(z,"resources.csv").contains(",5,2,3"),"Chunk queue depths sampled");
                check(read(z,"summary.txt").contains("p95 ms")&&read(z,"sections.csv").contains("tick"),"Frame statistics and section data exported");
            }
        }finally{Files.deleteIfExists(zip);}
        System.out.println("Benchmark nested timings, bounded capture, worker accounting and ZIP output passed");
    }
    private static String read(ZipFile z,String name)throws IOException{
        try(InputStream in=z.getInputStream(z.getEntry(name));ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return new String(out.toByteArray(),StandardCharsets.UTF_8);}
    }
}
