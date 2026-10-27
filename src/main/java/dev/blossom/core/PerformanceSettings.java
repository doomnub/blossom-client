package dev.blossom.core;
import java.util.Properties;
/** Independent switches make performance changes reversible and comparable. */
public final class PerformanceSettings {
    public boolean modelBatching=true,chunkScheduling=true,worldGc=true,earlyInput=true;
    public float chunkBudgetMs=2;
    public int chunkWorkers=0; // 0 = automatic, bounded by the existing five-buffer pool
    public static int workers(int configured,int processors){return configured==0?Math.max(1,Math.min(4,processors/2)):Math.max(1,Math.min(4,configured));}
    public void load(Properties p){
        modelBatching=flag(p,"models");chunkScheduling=flag(p,"chunks");worldGc=flag(p,"worldGc");earlyInput=flag(p,"input");
        chunkWorkers=ClientSettings.integer(p,"performance.chunkWorkers",0,0,4);
        chunkBudgetMs=ClientSettings.number(p,"performance.chunkBudget",2,.5f,8);
    }
    private boolean flag(Properties p,String key){return Boolean.parseBoolean(p.getProperty("performance."+key,"true"));}
    public void save(Properties p){
        p.setProperty("performance.chunkWorkers",Integer.toString(chunkWorkers));
        p.setProperty("performance.models",Boolean.toString(modelBatching));p.setProperty("performance.chunks",Boolean.toString(chunkScheduling));
        p.setProperty("performance.worldGc",Boolean.toString(worldGc));p.setProperty("performance.input",Boolean.toString(earlyInput));p.setProperty("performance.chunkBudget",Float.toString(chunkBudgetMs));
    }
}
