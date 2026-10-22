package dev.blossom.core;
/** Immutable frame-duration animation. Never derives time from FPS or wall-clock changes. */
public final class Timeline {
    private final long[] ends;
    private final int[] order;
    private final long duration;
    public Timeline(int[] millis, boolean pingPong) {
        if (millis.length < 1 || millis.length > 256) throw new IllegalArgumentException("frame count");
        int n = pingPong && millis.length > 1 ? 2*millis.length-2 : millis.length;
        ends = new long[n]; order = new int[n]; long sum = 0;
        for (int i=0; i<n; i++) {
            int f = i < millis.length ? i : 2*millis.length-2-i;
            if (millis[f] < 10 || millis[f] > 60000) throw new IllegalArgumentException("duration");
            order[i]=f; sum += millis[f]*1000000L; ends[i]=sum;
        }
        duration=sum;
    }
    public int frame(long elapsedNanos, boolean loop) {
        long t=Math.max(0,elapsedNanos);
        if (loop) t %= duration;
        else if(t>=duration) return order[order.length-1];
        for(int i=0;i<ends.length;i++) if(t<ends[i]) return order[i];
        return order[order.length-1];
    }
}
