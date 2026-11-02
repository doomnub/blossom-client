package dev.blossom.core;

import java.io.IOException;
import java.lang.management.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** One sleeping observer; no game/GL calls, process spawning, or unbounded history. */
public final class StallReporter {
    private static final long THRESHOLD = 5000000000L;
    private static volatile long heartbeat;
    private static volatile Thread renderThread;
    private static boolean started;
    private StallReporter() {}
    public static synchronized void start(Path folder) {
        if (started) return;
        started = true;
        beat();
        Thread observer = new Thread(() -> observe(folder), "Blossom stall reporter");
        observer.setDaemon(true);
        observer.setPriority(Thread.MIN_PRIORITY);
        observer.start();
    }
    public static void beat() {
        renderThread = Thread.currentThread();
        heartbeat = System.nanoTime();
    }
    public static boolean due(long now, long last, long reported) {
        return last != 0 && last != reported && now - last >= THRESHOLD;
    }
    private static void observe(Path folder) {
        long reported = 0;
        int reports = 0;
        while (reports < 3) {
            try {
                Thread.sleep(1000);
                Thread main = renderThread;
                if (main == null || !main.isAlive()) return;
                long last = heartbeat, now = System.nanoTime();
                if (!due(now, last, reported)) continue;
                reported = last;
                String report = capture(now - last, main.getId());
                Files.createDirectories(folder);
                // Three fixed names, overwritten on subsequent sessions. No accumulating dumps.
                Path file = folder.resolve("stall-" + (++reports) + ".txt");
                Files.write(file, report.getBytes(StandardCharsets.UTF_8));
                System.err.println("[Blossom] Render thread stalled; report: " + file);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); return;
            } catch (IOException | RuntimeException e) {
                // Logging cannot become a repeated failure loop during memory/disk pressure.
                System.err.println("[Blossom] Stall report unavailable: " + e.getClass().getSimpleName());
                return;
            }
        }
    }
    private static String capture(long delay, long renderId) {
        StringBuilder out = new StringBuilder(16384);
        out.append("Blossom stall report\nNo frame heartbeat for ms: ").append(delay / 1000000)
           .append("\nRender thread id: ").append(renderId)
           .append("\nRenderer optimizations: ").append(RendererPolicy.OPTIMIZATIONS)
           .append("\nHeap: ").append(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()).append('\n');
        for (BufferPoolMXBean pool : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class))
            out.append("Buffer pool ").append(pool.getName()).append(": count=").append(pool.getCount()).append(" bytes=").append(pool.getMemoryUsed()).append('\n');
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans())
            out.append("GC ").append(gc.getName()).append(": count=").append(gc.getCollectionCount()).append(" ms=").append(gc.getCollectionTime()).append('\n');
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        // Main thread first; individual bounded snapshots avoid copying every stack at once.
        append(out, threads.getThreadInfo(renderId, 48));
        long[] ids = threads.getAllThreadIds();
        for (int i = 0; i < ids.length && i < 128 && out.length() < 240000; i++)
            if (ids[i] != renderId) append(out, threads.getThreadInfo(ids[i], 48));
        if (out.length() > 256000) out.setLength(256000);
        return out.toString();
    }
    private static void append(StringBuilder out, ThreadInfo info) {
        if (info == null) return;
        String name = info.getThreadName();
        out.append('\n').append(name, 0, Math.min(name.length(), 256)).append(" #").append(info.getThreadId()).append(' ').append(info.getThreadState())
           .append(" lock=").append(info.getLockName()).append(" owner=").append(info.getLockOwnerId()).append('\n');
        for (StackTraceElement frame : info.getStackTrace()) out.append("  at ").append(frame).append('\n');
    }
}
