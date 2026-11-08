/** Render values only: never writes XP, world time, or sends commands. */
public final class BlossomView {
    public static float xp(float original){return BlossomHooks.modules.on("xpchanger")?BlossomHooks.settings.xpPercent/100f:original;}
    public static long skyTime(long original){return BlossomHooks.modules.on("timechanger")?Math.floorDiv(original,24000L)*24000L+(Math.round(BlossomHooks.settings.visualTime*1000.0)+18000L)%24000L:original;}
    public static float angle(float original,adm world){
        if(!world.D||world!=ave.A().f||!BlossomHooks.modules.on("timechanger"))return original;
        return world.t.a(skyTime(world.L()),0);
    }
}
