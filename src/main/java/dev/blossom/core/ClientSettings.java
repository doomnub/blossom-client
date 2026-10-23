package dev.blossom.core;
import java.util.Properties;
/** Version-one settings with bounded parsing; malformed optional values use defaults. */
public final class ClientSettings {
    public int zoomKey=46, crosshairLength=5, crosshairGap=3, crosshairThickness=1;
    public int crosshairColor=0xffff83b5, menuKey=54;
    public float zoomFactor=4;
    public boolean crosshairDot=false, privacy=false, fpsAverage=false, fpsLows=false;
    public void load(Properties p) {
        zoomKey=integer(p,"zoom.key",46,1,255);
        menuKey=integer(p,"menu.key",54,1,255);
        zoomFactor=number(p,"zoom.factor",4,2,10);
        crosshairLength=integer(p,"crosshair.length",5,1,20);
        crosshairGap=integer(p,"crosshair.gap",3,0,16);
        crosshairThickness=integer(p,"crosshair.thickness",1,1,6);
        crosshairColor=color(p,"crosshair.color",0xffff83b5);
        crosshairDot=Boolean.parseBoolean(p.getProperty("crosshair.dot","false"));
        fpsAverage=Boolean.parseBoolean(p.getProperty("fps.average","false"));
        fpsLows=Boolean.parseBoolean(p.getProperty("fps.lows","false"));
        privacy=Boolean.parseBoolean(p.getProperty("privacy","false"));
    }
    public void save(Properties p) {
        p.setProperty("fps.average",Boolean.toString(fpsAverage));p.setProperty("fps.lows",Boolean.toString(fpsLows));
        p.setProperty("zoom.key",Integer.toString(zoomKey));p.setProperty("menu.key",Integer.toString(menuKey));
        p.setProperty("zoom.factor",Float.toString(zoomFactor));
        p.setProperty("crosshair.length",Integer.toString(crosshairLength));
        p.setProperty("crosshair.gap",Integer.toString(crosshairGap));
        p.setProperty("crosshair.thickness",Integer.toString(crosshairThickness));
        p.setProperty("crosshair.color",Integer.toHexString(crosshairColor));
        p.setProperty("crosshair.dot",Boolean.toString(crosshairDot));p.setProperty("privacy",Boolean.toString(privacy));
    }
    public static float number(Properties p,String key,float fallback,float min,float max) {
        try {float n=Float.parseFloat(p.getProperty(key));return Float.isFinite(n)?Math.max(min,Math.min(max,n)):fallback;}
        catch(RuntimeException e){return fallback;}
    }
    public static int integer(Properties p,String key,int fallback,int min,int max) {
        try{return Math.max(min,Math.min(max,Integer.parseInt(p.getProperty(key))));}
        catch(RuntimeException e){return fallback;}
    }
    public static int color(Properties p,String key,int fallback) {
        try{String s=p.getProperty(key);if(!s.matches("[0-9a-fA-F]{8}"))return fallback;return (int)Long.parseLong(s,16);}
        catch(RuntimeException e){return fallback;}
    }
}
