package dev.blossom.core;
import java.util.Properties;
/** Appearance only: never changes module state, positions, hit boxes or text metrics. */
public enum UiStyle {
    PIXEL("Pixel"), MODERN("Modern");
    public final String label;
    UiStyle(String label){this.label=label;}
    public UiStyle next(){return this==PIXEL?MODERN:PIXEL;}
    public static UiStyle load(Properties p){return "modern".equals(p.getProperty("ui.style"))?MODERN:PIXEL;}
    public void save(Properties p){p.setProperty("ui.style",this==MODERN?"modern":"pixel");}
}
