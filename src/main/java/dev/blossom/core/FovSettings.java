package dev.blossom.core;
import java.util.Properties;
/** Camera-only overrides. No speed, movement attributes or server state are changed. */
public final class FovSettings {
    public boolean constant=true,dynamicSprint=false;
    public float base=70,sprint=80,speed=80,speedTwo=90,slowness=60,flying=80,bow=55;
    public void setConstant(boolean value){constant=value;dynamicSprint=!value;}
    public float calculate(boolean sprinting,int speedLevel,boolean slowed,boolean flight,float bowProgress){
        if(constant)return base;
        float f=slowed?slowness:speedLevel>=2?speedTwo:speedLevel==1?speed:base;
        if(sprinting)f+=sprint-base;
        if(flight)f+=flying-base;
        float draw=Math.max(0,Math.min(1,bowProgress));f+=(bow-f)*draw*draw;
        return Math.max(10,Math.min(170,f));
    }
    public void load(Properties p){
        setConstant(Boolean.parseBoolean(p.getProperty("fov.constant","true")));
        base=ClientSettings.number(p,"fov.base",70,30,110);sprint=ClientSettings.number(p,"fov.sprint",80,30,110);
        speed=ClientSettings.number(p,"fov.speed",80,30,110);speedTwo=ClientSettings.number(p,"fov.speedTwo",90,30,110);
        slowness=ClientSettings.number(p,"fov.slowness",60,30,110);flying=ClientSettings.number(p,"fov.flying",80,30,110);bow=ClientSettings.number(p,"fov.bow",55,30,110);
    }
    public void save(Properties p){
        p.setProperty("fov.constant",Boolean.toString(constant));p.setProperty("fov.dynamicSprint",Boolean.toString(!constant));
        String[] keys={"base","sprint","speed","speedTwo","slowness","flying","bow"};float[] values={base,sprint,speed,speedTwo,slowness,flying,bow};
        for(int i=0;i<keys.length;i++)p.setProperty("fov."+keys[i],Float.toString(values[i]));
    }
}
