package dev.blossom;
import dev.blossom.core.TapHold;
public final class TapHoldTest {
    public static void run(){
        TapHold t=new TapHold();
        check(!t.update(false,true,0));check(t.update(true,true,1));check(t.update(false,true,249_000_000));
        check(t.update(false,true,900_000_000));check(!t.update(true,true,1_000_000_000L));check(!t.update(false,true,1_010_000_000L));
        check(t.update(true,true,2_000_000_000L));check(t.update(true,true,2_300_000_000L));check(!t.update(false,true,2_400_000_000L));
        check(t.update(true,true,3_000_000_000L));check(!t.update(false,true,3_250_000_000L));
        check(t.update(true,true,4_000_000_000L));check(!t.update(true,false,4_100_000_000L));check(!t.update(true,true,4_200_000_000L));check(!t.update(false,true,4_210_000_000L));
        check(t.update(true,true,5_000_000_000L));check(t.update(false,true,5_100_000_000L));check(!t.update(false,false,5_200_000_000L));check(!t.update(false,true,5_300_000_000L));
        System.out.println("Free Look tap/hold: threshold, toggle-off, hold, focus loss and reset passed");
    }
    private static void check(boolean v){if(!v)throw new AssertionError("Tap/hold gesture");}
}
