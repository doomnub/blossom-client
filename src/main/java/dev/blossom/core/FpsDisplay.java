package dev.blossom.core;
import java.util.Locale;
/** Current uses vanilla's recent FPS counter; average uses collected frame timings. */
public final class FpsDisplay {
    public static String format(int current,double[] stats,boolean average,boolean lows){
        String text=average?String.format(Locale.ROOT,"%.0f FPS (average)",stats[0]):Math.max(0,current)+" FPS";
        if(lows)text+=String.format(Locale.ROOT," | 1%% %.0f | 0.1%% %.0f%s",stats[1],stats[2],stats[5]<1000?" (warming)":"");
        return text;
    }
}
