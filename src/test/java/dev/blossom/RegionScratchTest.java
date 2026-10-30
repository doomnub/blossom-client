package dev.blossom;
import dev.blossom.core.RegionScratch;
import java.util.Arrays;
public final class RegionScratchTest {
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void run(){
        RegionScratch<Object> scratch=new RegionScratch<Object>(Object[]::new);
        scratch.enter(true);Object[] first=scratch.states(8000);int[] lights=scratch.lights(8000);
        first[7]=new Object();lights[7]=99;
        check(scratch.states(8000)!=first,"Multiple caches in one scope must not alias");
        scratch.enter(true);check(scratch.states(8000)!=first&&scratch.lights(8000)!=lights,"Nested scopes must not alias");scratch.leave();check(first[7]!=null,"Nested exit must not clear outer cache");
        scratch.leave();check(first[7]==null,"Exit clears world references");
        scratch.enter(true);check(scratch.states(8000)==first&&scratch.lights(8000)==lights,"Sequential rebuilds reuse storage");
        // The unchanged vanilla constructor resets the light cache to this sentinel.
        Arrays.fill(lights,-1);check(lights[7]==-1,"Light sentinel reset");scratch.leave();
        scratch.enter(false);check(scratch.states(8000)!=first&&scratch.lights(8000)!=lights,"Disabled scope uses independent arrays");scratch.leave();
        check(scratch.states(8000)!=first,"Out-of-scope cache must not borrow");
        try{scratch.enter(true);scratch.states(8000)[0]=new Object();throw new IllegalStateException();}catch(IllegalStateException expected){}finally{scratch.leave();}
        check(first[0]==null,"Exceptional rebuild clears references");
        RegionScratch<Object> worker=new RegionScratch<Object>(Object[]::new);worker.enter(true);check(worker.states(8000)!=first,"Separate workers own separate arrays");worker.leave();
        System.out.println("Region scratch reuse, nested/disabled fallbacks and reference cleanup passed");
    }
}
