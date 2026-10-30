package dev.blossom.core;
/** Smoothed CPU-side work budget; display/limiter waits are excluded by the caller. */
public final class AdaptiveBudget {
    private double average;private int samples;
    public void reset(){average=0;samples=0;}
    public void sample(long workNanos){if(workNanos<=0)return;double n=Math.min(workNanos,250000000L);average=samples++==0?n:average+(n-average)*.125;}
    public long nanos(float maximumMs,int targetFps){
        long maximum=(long)(maximumMs*1000000L);if(samples<8)return maximum;
        double target=1e9/Math.max(30,Math.min(360,targetFps));
        double fraction=Math.max(.125,Math.min(1,(target-average)/target+.5));
        return Math.max(250000L,Math.min(maximum,(long)(maximum*fraction)));
    }
}
