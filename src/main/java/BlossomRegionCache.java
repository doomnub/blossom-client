import dev.blossom.core.RegionScratch;
/** Cache contents never survive a rebuild; only empty storage is retained per worker. */
public final class BlossomRegionCache {
    private static final ThreadLocal<RegionScratch<alz>> LOCAL=new ThreadLocal<RegionScratch<alz>>();
    public static void enter(){
        boolean enabled=BlossomPerformance.enabled()&&BlossomHooks.settings.performance.regionBuffers;
        RegionScratch<alz> scratch=LOCAL.get();
        if(scratch==null){if(!enabled)return;scratch=new RegionScratch<alz>(alz[]::new);LOCAL.set(scratch);}
        scratch.enter(enabled);
    }
    public static void leave(){RegionScratch<alz> scratch=LOCAL.get();if(scratch!=null)scratch.leave();}
    public static int[] lights(int length){RegionScratch<alz> scratch=LOCAL.get();return scratch==null?new int[length]:scratch.lights(length);}
    public static alz[] states(int length){RegionScratch<alz> scratch=LOCAL.get();return scratch==null?new alz[length]:scratch.states(length);}
}
