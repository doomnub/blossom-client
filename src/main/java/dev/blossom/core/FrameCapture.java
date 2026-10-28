package dev.blossom.core;
import java.util.Arrays;
/** Fixed storage during capture; all sorting and output happen after capture ends. */
public final class FrameCapture {
    private final long[] times=new long[120000];
    private int size;
    public void record(long nanos){if(nanos>0&&size<times.length)times[size++]=nanos;}
    public boolean full(){return size==times.length;}
    public int size(){return size;}
    public long[] copy(){return Arrays.copyOf(times,size);}
    public static String summary(long[] samples){
        if(samples.length==0)return "No frames captured";
        long[] sorted=samples.clone();Arrays.sort(sorted);double total=0;
        for(long n:sorted)total+=n;
        return String.format(java.util.Locale.ROOT,"Frames: %d%nAverage FPS: %.2f%n1%% low FPS: %.2f%n0.1%% low FPS: %.2f%nMedian ms: %.3f%np90 ms: %.3f%np95 ms: %.3f%np99 ms: %.3f%np99.9 ms: %.3f%nWorst ms: %.3f%nStddev ms: %.3f%nFrames >16.667ms: %d%nFrames >33.333ms: %d%nFrames >50ms: %d%nFrames >100ms: %d%nFrames >250ms: %d%n",samples.length,1e9*samples.length/total,low(sorted,.01),low(sorted,.001),at(sorted,.5)/1e6,at(sorted,.90)/1e6,at(sorted,.95)/1e6,at(sorted,.99)/1e6,at(sorted,.999)/1e6,sorted[sorted.length-1]/1e6,deviation(sorted,total/sorted.length)/1e6,over(sorted,16666667),over(sorted,33333333),over(sorted,50000000),over(sorted,100000000),over(sorted,250000000));
    }
    private static double deviation(long[] a,double mean){double sum=0;for(long n:a){double d=n-mean;sum+=d*d;}return Math.sqrt(sum/a.length);}
    private static int over(long[] a,long limit){int count=0;for(long n:a)if(n>limit)count++;return count;}
    private static long at(long[] a,double percentile){return a[Math.max(0,(int)Math.ceil(a.length*percentile)-1)];}
    private static double low(long[] a,double fraction){int count=Math.max(1,(int)Math.ceil(a.length*fraction));double sum=0;for(int i=a.length-count;i<a.length;i++)sum+=a[i];return 1e9*count/sum;}
}
