/** Shared quiet pink panel treatment; no textures, shaders, or animation overhead. */
public final class BlossomTheme {
    public static void panel(int width,int height){
        int left=Math.max(4,width/2-155),right=Math.min(width-4,width/2+155);
        avp.a(left,6,right,height-6,0xf2201726);
        avp.a(left,6,right,9,BlossomHooks.accent);
        avp.a(left,9,right,40,0xf2382034);
        avp.a(left,height-8,right,height-6,0xff9c4e78);
    }
}
