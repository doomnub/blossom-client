package dev.blossom.core;
/** Time-based FOV-ratio interpolation; scroll adjustment belongs only to one hold. */
public final class SmoothZoom {
    private static final long DURATION=80000000L;
    private double from=1,target=1,factor=4;
    private long start;
    private boolean held;
    public double value(long now){double t=Math.max(0,Math.min(1,(now-start)/(double)DURATION));return from+(target-from)*t;}
    private void target(double next,long now){from=value(now);target=next;start=now;}
    public double update(boolean down,double initialFactor,long now){
        if(down!=held){held=down;if(down){factor=Math.max(2,Math.min(20,initialFactor));target(1/factor,now);}else target(1,now);}
        return value(now);
    }
    public void scroll(int direction,long now){if(held&&direction!=0){factor=Math.max(2,Math.min(20,factor*(direction>0?1.25:.8)));target(1/factor,now);}}
    public double factor(){return factor;}
    public void reset(){from=target=1;held=false;start=0;}
}
