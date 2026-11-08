package dev.blossom;
import dev.blossom.core.*;
import java.util.Properties;
public final class SkyLayerTest {
    private static final String FILE="minecraft:optifine/sky/world0/sky1.properties";
    public static void run(){
        Properties p=new Properties();SkyLayer all=new SkyLayer(p,FILE);
        eq(all.brightness(0,0,0,0),1);eq(all.brightness(18000,0,0,0),1);eq(all.brightness(18000,0,1,0),0);
        check(all.source.equals("minecraft:optifine/sky/world0/sky1.png"));
        check(SkyLayer.resolve(FILE,"./night.png").equals("minecraft:optifine/sky/world0/night.png"));
        check(SkyLayer.resolve(FILE,"~/sky/common.png").equals("minecraft:optifine/sky/common.png"));
        check(SkyLayer.resolve(FILE,"other:skies/sky.png").equals("other:skies/sky.png"));
        check(SkyLayer.resolve(FILE,"textures/sky.png").equals("minecraft:textures/sky.png"));
        check(SkyLayer.resolve("minecraft:mcpatcher/sky/world0/sky1.properties","~/sky/common.png").equals("minecraft:mcpatcher/sky/common.png"));
        p.setProperty("startFadeIn","18:00");p.setProperty("endFadeIn","19:00");p.setProperty("endFadeOut","06:00");
        SkyLayer night=new SkyLayer(p,FILE);eq(night.brightness(12000,0,0,0),0);eq(night.brightness(12500,0,0,0),.5f);eq(night.brightness(18000,0,0,0),1);eq(night.brightness(23500,0,0,0),.5f);eq(night.brightness(0,0,0,0),0);
        p.setProperty("weather","clear rain thunder");p.setProperty("heights","70 100-115");p.setProperty("biomes","plains IcePlains 4");
        SkyLayer conditional=new SkyLayer(p,FILE);eq(conditional.brightness(18000,0,1,.7f),1);check(conditional.location(100,"Ice Plains",12));check(!conditional.location(99,"Ice Plains",12));check(!conditional.location(100,"Desert",2));check(conditional.location(70,"Forest",4));
        p.setProperty("days","0 2-4");p.setProperty("daysLoop","8");SkyLayer days=new SkyLayer(p,FILE);eq(days.brightness(18000+24000,0,0,0),0);eq(days.brightness(18000+48000,0,0,0),1);
        p.setProperty("axis","NaN 0 1");try{new SkyLayer(p,FILE);throw new AssertionError("NaN accepted");}catch(IllegalArgumentException expected){}
        for(String mode:new String[]{"add","alpha","subtract","multiply","dodge","burn","screen","overlay","replace"}){Properties blend=new Properties();blend.setProperty("blend",mode);check(new SkyLayer(blend,FILE).blend.equals(mode));}
        ClientSettings settings=new ClientSettings();settings.xpPercent=0;settings.visualTime=12.34f;settings.freeLookKey=-99;Properties saved=new Properties();settings.save(saved);ClientSettings restored=new ClientSettings();restored.load(saved);check(restored.xpPercent==0&&restored.visualTime==12.34f&&restored.freeLookKey==-99);
        saved.setProperty("xpchanger.percent","999");saved.setProperty("timechanger.hour","-1");restored.load(saved);check(restored.xpPercent==100&&restored.visualTime==0);
        System.out.println("Sky properties, fade boundaries, paths, filters and visual settings passed");
    }
    private static void check(boolean v){if(!v)throw new AssertionError();}
    private static void eq(float a,float b){if(Math.abs(a-b)>.001f)throw new AssertionError(a+" != "+b);}
}
