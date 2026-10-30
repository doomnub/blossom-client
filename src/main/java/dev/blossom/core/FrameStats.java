package dev.blossom.core;
import java.util.Arrays;
/** Sampling is O(1); sorting occurs only when requesting a diagnostic snapshot. */
public final class FrameStats {
    private final long[] samples,scratch;
    private int cursor, size;
    public FrameStats(int capacity) {
        if (capacity < 1000 || capacity > 100000) throw new IllegalArgumentException("capacity");
        samples = new long[capacity];scratch=new long[capacity];
    }
    public void clear(){cursor=size=0;}
    public void record(long nanos) {
        if (nanos <= 0) return;
        samples[cursor] = nanos; cursor = (cursor + 1) % samples.length;
        size = Math.min(size + 1, samples.length);
    }
    public double[] snapshot() {
        if (size == 0) return new double[]{0,0,0,0,0,0};
        long[] sorted=scratch;System.arraycopy(samples,0,sorted,0,size);Arrays.sort(sorted,0,size);
        double total = 0; for(int i=0;i<size;i++)total+=sorted[i];
        return new double[]{1e9 * size / total, low(sorted, .01), low(sorted, .001),
            sorted[(int)Math.ceil(size*.50)-1]/1e6, sorted[(int)Math.ceil(size*.99)-1]/1e6, size};
    }
    private double low(long[] sorted, double fraction) {
        int count = Math.max(1, (int)Math.ceil(size * fraction));
        double total = 0;
        for(int i=size-count;i<size;i++) total += sorted[i];
        return 1e9 * count / total;
    }
}
