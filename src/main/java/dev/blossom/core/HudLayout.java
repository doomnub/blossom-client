package dev.blossom.core;
/** Shared renderer/editor geometry. Anchors describe available space, not full screen. */
public final class HudLayout {
    private HudLayout() {}
    public static int position(float fraction,int screen,int extent) {
        return Math.round(Math.max(0,Math.min(1,fraction))*Math.max(0,screen-extent));
    }
    public static float fraction(int pixel,int screen,int extent) {
        return Math.max(0,Math.min(1,pixel/(float)Math.max(1,screen-extent)));
    }
    public static int snap(int pixel,int screen,int extent,boolean grid) {
        if(Math.abs(pixel)<6)return 0;
        if(Math.abs(pixel-(screen-extent))<6)return Math.max(0,screen-extent);
        if(Math.abs(pixel-(screen-extent)/2)<6)return (screen-extent)/2;
        return grid?Math.round(pixel/8f)*8:pixel;
    }
}
