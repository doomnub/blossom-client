package dev.blossom.core;
import java.util.Properties;
/** Independent switches make performance changes reversible and comparable. */
public final class PerformanceSettings {
    public boolean cloudBatching=true,chatTextCache=true;
    public boolean traversalAllocations=true;
    public boolean fastFrustum=false; // opt-in until hardware comparison
    public boolean modelBatching=true,chunkScheduling=true,worldGc=false,earlyInput=true;
    public boolean adaptive=true,prioritize=true,particleCull=true,lightmapCache=true;
    public boolean primitiveSort=true,regionBuffers=true;
    public int targetFps=120;
    public float chunkBudgetMs=2;
    public int chunkWorkers=0; // 0 = automatic, bounded by the existing five-buffer pool
    public static int workers(int configured,int processors){return configured==0?Math.max(1,Math.min(2,processors/2)):Math.max(1,Math.min(4,configured));}
    public void load(Properties p){
        cloudBatching=flag(p,"cloudBatching");chatTextCache=flag(p,"chatTextCache");
        traversalAllocations=flag(p,"traversalAllocations");
        fastFrustum=Boolean.parseBoolean(p.getProperty("performance.fastFrustum","false"));
        regionBuffers=flag(p,"regionBuffers");primitiveSort=flag(p,"primitiveSort");adaptive=flag(p,"adaptive");prioritize=flag(p,"prioritize");particleCull=flag(p,"particleCull");lightmapCache=flag(p,"lightmapCache");targetFps=ClientSettings.integer(p,"performance.targetFps",120,30,360);
        modelBatching=flag(p,"models");chunkScheduling=flag(p,"chunks");worldGc=false;earlyInput=flag(p,"input");
        chunkWorkers=ClientSettings.integer(p,"performance.chunkWorkers",0,0,4);
        chunkBudgetMs=ClientSettings.number(p,"performance.chunkBudget",2,.5f,8);
    }
    private boolean flag(Properties p,String key){return Boolean.parseBoolean(p.getProperty("performance."+key,"true"));}
    public void save(Properties p){
        p.setProperty("performance.cloudBatching",Boolean.toString(cloudBatching));p.setProperty("performance.chatTextCache",Boolean.toString(chatTextCache));
        p.setProperty("performance.traversalAllocations",Boolean.toString(traversalAllocations));
        p.setProperty("performance.fastFrustum",Boolean.toString(fastFrustum));
        p.setProperty("performance.regionBuffers",Boolean.toString(regionBuffers));p.setProperty("performance.primitiveSort",Boolean.toString(primitiveSort));p.setProperty("performance.adaptive",Boolean.toString(adaptive));p.setProperty("performance.prioritize",Boolean.toString(prioritize));p.setProperty("performance.particleCull",Boolean.toString(particleCull));p.setProperty("performance.lightmapCache",Boolean.toString(lightmapCache));p.setProperty("performance.targetFps",Integer.toString(targetFps));
        p.setProperty("performance.chunkWorkers",Integer.toString(chunkWorkers));
        p.setProperty("performance.models",Boolean.toString(modelBatching));p.setProperty("performance.chunks",Boolean.toString(chunkScheduling));
        p.setProperty("performance.worldGc",Boolean.toString(worldGc));p.setProperty("performance.input",Boolean.toString(earlyInput));p.setProperty("performance.chunkBudget",Float.toString(chunkBudgetMs));
    }
}
