package dev.blossom.core;
/** Continuous animation time across speed changes and pause/resume, using monotonic input. */
public final class PlaybackClock {
    private long anchor, accumulated;
    private double speed=1;
    private boolean paused;
    public PlaybackClock(long now){anchor=now;}
    public long elapsed(long now){
        double extra=paused?0:Math.max(0,now-anchor)*speed;
        return extra>=Long.MAX_VALUE-accumulated?Long.MAX_VALUE:accumulated+(long)extra;
    }
    private void settle(long now){accumulated=elapsed(now);anchor=now;}
    public void speed(double value,long now){
        if(!Double.isFinite(value)||value<.1||value>4)throw new IllegalArgumentException("Speed must be 0.1–4");
        settle(now);speed=value;
    }
    public void paused(boolean value,long now){settle(now);paused=value;}
    public void restart(long now){anchor=now;accumulated=0;}
    public boolean paused(){return paused;}
    public double speed(){return speed;}
}
