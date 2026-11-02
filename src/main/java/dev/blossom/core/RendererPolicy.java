package dev.blossom.core;
import java.util.Locale;
/** Process-wide choice: profiles cannot silently reinstall unstable renderer hooks. */
public final class RendererPolicy {
    public static final boolean OPTIMIZATIONS = choose(System.getProperty("os.name", ""), System.getProperty("blossom.experimentalRenderer", ""));
    public static boolean choose(String os, String override) {
        if ("true".equalsIgnoreCase(override) || "1".equals(override)) return true;
        if ("false".equalsIgnoreCase(override) || "0".equals(override)) return false;
        return !os.toLowerCase(Locale.ROOT).contains("linux");
    }
}
