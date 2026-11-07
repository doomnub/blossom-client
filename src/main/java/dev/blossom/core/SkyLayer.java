package dev.blossom.core;
import java.util.*;
/** OptiFine sky.properties data, parsed once; no world or graphics dependencies. */
public final class SkyLayer {
    public final String source,blend;
    public final boolean rotate;
    public final float speed,axisX,axisY,axisZ,transition;
    public final boolean clear,rain,thunder;
    private final int start,inEnd,outStart,outEnd,loop;
    private final int[][] days,heights;
    private final Set<String> biomes;
    private final boolean invertBiomes;
    public SkyLayer(Properties p,String file){
        source=resolve(file,p.getProperty("source",file.substring(file.indexOf(':')+1,file.length()-11)+".png"));
        blend=p.getProperty("blend","add").trim();
        if(!Arrays.asList("add","alpha","subtract","multiply","dodge","burn","screen","overlay","replace").contains(blend))throw new IllegalArgumentException("Unknown sky blend");
        rotate=Boolean.parseBoolean(p.getProperty("rotate","true"));
        speed=finite(p.getProperty("speed","1"));if(speed<0)throw new IllegalArgumentException("Negative sky speed");
        String[] axis=p.getProperty("axis","0 0 1").trim().split("\\s+");
        if(axis.length!=3)throw new IllegalArgumentException("Sky axis needs three values");
        axisX=finite(axis[0]);axisY=finite(axis[1]);axisZ=finite(axis[2]);
        if(axisX==0&&axisY==0&&axisZ==0)throw new IllegalArgumentException("Zero sky axis");
        transition=finite(p.getProperty("transition","1"));if(transition<0)throw new IllegalArgumentException("Negative transition");
        boolean fade=!p.getProperty("fade","true").equals("false")&&(p.containsKey("startFadeIn")||p.containsKey("endFadeIn")||p.containsKey("endFadeOut"));
        if(fade){
            start=time(p.getProperty("startFadeIn"));inEnd=time(p.getProperty("endFadeIn"));outEnd=time(p.getProperty("endFadeOut"));
            outStart=p.containsKey("startFadeOut")?time(p.getProperty("startFadeOut")):Math.floorMod(outEnd-Math.floorMod(inEnd-start,24000),24000);
            int span=Math.floorMod(inEnd-start,24000)+Math.floorMod(outStart-inEnd,24000)+Math.floorMod(outEnd-outStart,24000)+Math.floorMod(start-outEnd,24000);
            if(span!=24000&&span!=0)throw new IllegalArgumentException("Overlapping sky fades");
        }else{start=inEnd=outStart=outEnd=-1;}
        loop=Integer.parseInt(p.getProperty("daysLoop","8"));if(loop<1)throw new IllegalArgumentException("Invalid day loop");
        days=ranges(p.getProperty("days"));heights=ranges(p.getProperty("heights"));
        String weather=" "+p.getProperty("weather","clear").trim()+" ";
        clear=weather.contains(" clear ");rain=weather.contains(" rain ");thunder=weather.contains(" thunder ");
        String names=p.getProperty("biomes","").trim();invertBiomes=names.startsWith("!");if(invertBiomes)names=names.substring(1);
        biomes=new HashSet<String>();for(String b:names.split("\\s+"))if(!b.isEmpty())biomes.add(normalizeBiome(b));
    }
    private static float finite(String s){float f=Float.parseFloat(s);if(!Float.isFinite(f))throw new IllegalArgumentException("Non-finite sky value");return f;}
    public static int time(String value){
        if(value==null)throw new IllegalArgumentException("Incomplete fade times");
        String[] t=value.trim().split(":");if(t.length!=2)throw new IllegalArgumentException("Invalid sky time");
        int h=Integer.parseInt(t[0]),m=Integer.parseInt(t[1]);if(h<0||h>23||m<0||m>59)throw new IllegalArgumentException("Invalid sky time");
        return Math.floorMod((h*60+m)*1000/60-6000,24000);
    }
    public static String resolve(String file,String path){
        path=path.trim();String namespace=file.substring(0,file.indexOf(':')),base=file.substring(file.indexOf(':')+1);
        if(path.contains(":"))return path;
        if(path.startsWith("~/"))path=(base.startsWith("mcpatcher/")?"mcpatcher/":"optifine/")+path.substring(2);
        else if(path.startsWith("./"))path=base.substring(0,base.lastIndexOf('/')+1)+path.substring(2);
        else if(!path.contains("/"))path=base.substring(0,base.lastIndexOf('/')+1)+path;
        if(path.startsWith("/"))path=path.substring(1);
        return namespace+":"+path;
    }
    public static String normalizeBiome(String s){return s.toLowerCase(Locale.ROOT).replace("minecraft:","").replace("_","").replace(" ","");}
    public static int[][] ranges(String value){
        if(value==null||value.trim().isEmpty())return new int[0][];
        String[] tokens=value.trim().split("\\s+");int[][] result=new int[tokens.length][2];
        java.util.regex.Pattern pattern=java.util.regex.Pattern.compile("\\(?(-?\\d+)\\)?(?:-\\(?(-?\\d+)\\)?)?");
        for(int i=0;i<tokens.length;i++){java.util.regex.Matcher m=pattern.matcher(tokens[i]);if(!m.matches())throw new IllegalArgumentException("Invalid sky range");result[i][0]=Integer.parseInt(m.group(1));result[i][1]=m.group(2)==null?result[i][0]:Integer.parseInt(m.group(2));if(result[i][1]<result[i][0])throw new IllegalArgumentException("Reversed sky range");}
        return result;
    }
    private static boolean includes(int[][] ranges,int v){if(ranges.length==0)return true;for(int[] r:ranges)if(v>=r[0]&&v<=r[1])return true;return false;}
    public boolean location(int height,String biome,int biomeId){return includes(heights,height)&&(biomes.isEmpty()||((biomes.contains(normalizeBiome(biome))||biomes.contains(Integer.toString(biomeId)))!=invertBiomes));}
    public float brightness(long ticks,float partial,float wet,float storm){
        float t=Math.floorMod(ticks,24000L)+partial;
        if(!includes(days,(int)Math.floorMod(Math.floorDiv(ticks-(start<0?0:start),24000L),(long)loop)))return 0;
        float fade=1;
        if(start>=0){
            float age=(t-start+24000)%24000;
            int rise=Math.floorMod(inEnd-start,24000),hold=Math.floorMod(outStart-inEnd,24000),fall=Math.floorMod(outEnd-outStart,24000);
            if(age<rise)fade=age/rise;else if(age<rise+hold)fade=1;else if(age<rise+hold+fall)fade=1-(age-rise-hold)/fall;else fade=rise+hold+fall==0?1:0;
        }
        return fade*Math.max(0,Math.min(1,(clear?1-wet:0)+(rain?wet-storm:0)+(thunder?storm:0)));
    }
}
