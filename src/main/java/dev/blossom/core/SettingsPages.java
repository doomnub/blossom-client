package dev.blossom.core;
/** Wrapping pagination shared by settings panels. */
public final class SettingsPages {
    public static int move(int page,int rows,int perPage,int direction){
        int count=Math.max(1,(Math.max(0,rows)+Math.max(1,perPage)-1)/Math.max(1,perPage));
        return Math.floorMod(page+direction,count);
    }
}
