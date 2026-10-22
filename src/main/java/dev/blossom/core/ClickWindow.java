package dev.blossom.core;
/** Bounded timestamp ring; input events, not per-frame button polls. */
public final class ClickWindow {
    private final long[] times = new long[4096];
    private int head, size;
    public void click(long now) {
        times[(head + size) % times.length] = now;
        if (size == times.length) head = (head + 1) % times.length;
        else size++;
    }
    public int count(long now, long window) {
        if (window <= 0) throw new IllegalArgumentException("window");
        while (size > 0 && now - times[head] >= window) {
            head = (head + 1) % times.length; size--;
        }
        return size;
    }
}
