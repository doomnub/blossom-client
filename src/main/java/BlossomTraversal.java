import java.util.*;
/** Fresh per-traversal FIFO storage; no world cache, visibility shortcut or worker changes. */
public final class BlossomTraversal {
    private static final cq[] DIRECTIONS=cq.values();
    private static final boolean ALLOWED=!"false".equalsIgnoreCase(System.getProperty("blossom.traversalAllocations","true"));
    private static boolean active; // render thread only; sampled once at traversal entry
    public static boolean enabled(){return ALLOWED&&BlossomHooks.modules.on("performance")&&BlossomHooks.settings.performance.traversalAllocations;}
    public static Queue<?> queue(){active=enabled();return active?new ArrayDeque<Object>(128):new LinkedList<Object>();}
    // The injected foreach loop only reads this array; no caller stores or mutates it.
    public static cq[] directions(){return active?DIRECTIONS:cq.values();}
}
