package dev.blossom.core;
import java.util.Arrays;
import java.util.function.IntFunction;
/** One cached pair per worker, leased only inside an exception-safe rebuild scope. */
public final class RegionScratch<T> {
    private final IntFunction<T[]> factory;
    private int depth;private boolean enabled,lightUsed,stateUsed;
    private int[] lights;private T[] states;
    public RegionScratch(IntFunction<T[]> factory){this.factory=factory;}
    public void enter(boolean use){if(depth++==0){enabled=use;lightUsed=stateUsed=false;}}
    public int[] lights(int length){
        if(depth!=1||!enabled||lightUsed||length!=8000)return new int[length];
        lightUsed=true;if(lights==null)lights=new int[length];return lights;
    }
    public T[] states(int length){
        if(depth!=1||!enabled||stateUsed||length!=8000)return factory.apply(length);
        stateUsed=true;if(states==null)states=factory.apply(length);return states;
    }
    public void leave(){if(depth==0)return;if(--depth==0){if(stateUsed)Arrays.fill(states,null);enabled=false;lightUsed=stateUsed=false;}}
}
