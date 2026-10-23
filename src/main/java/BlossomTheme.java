/** Shared quiet pink panel treatment; no textures, shaders, or animation overhead. */
public final class BlossomTheme {
    public static void rounded(int x,int y,int right,int bottom,int radius,int color){
        int r=Math.max(0,Math.min(radius,Math.min(right-x,bottom-y)/2));
        avp.a(x+r,y,right-r,bottom,color);
        for(int row=0;row<r;row++){int inset=(int)Math.ceil(r-Math.sqrt(r*r-(r-row-.5)*(r-row-.5)));avp.a(x+inset,y+row,x+r,y+row+1,color);avp.a(right-r,y+row,right-inset,y+row+1,color);avp.a(x+inset,bottom-row-1,x+r,bottom-row,color);avp.a(right-r,bottom-row-1,right-inset,bottom-row,color);}
        avp.a(x,y+r,x+r,bottom-r,color);avp.a(right-r,y+r,right,bottom-r,color);
    }

    public static void panel(int width,int height){
        int left=Math.max(4,width/2-155),right=Math.min(width-4,width/2+155);
        rounded(left,6,right,height-6,8,0xf2201726);
        rounded(left+5,6,right-5,9,1,BlossomHooks.accent);
        rounded(left+2,10,right-2,40,6,0xf2382034);
        avp.a(left,height-8,right,height-6,0xff9c4e78);
    }
}
