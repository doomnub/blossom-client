/** Rounded panels, emitted in one batch with cached row insets. */
public final class BlossomTheme {
    private static final int[][] insets=new int[65][];
    public static void rounded(int x,int y,int right,int bottom,int radius,int color){
        int r=Math.max(0,Math.min(radius,Math.min(right-x,bottom-y)/2));
        int[] rows=r<insets.length?insets[r]:null;
        if(rows==null){rows=new int[r];for(int row=0;row<r;row++)rows[row]=(int)Math.ceil(r-Math.sqrt((double)r*r-(r-row-.5)*(r-row-.5)));if(r<insets.length)insets[r]=rows;}
        bfx tess=bfx.a();bfd buffer=tess.c();
        bfl.l();bfl.x();bfl.a(770,771,1,0);bfl.c((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,(color>>>24)/255f);
        buffer.a(7,bms.e);
        rect(buffer,x+r,y,right-r,bottom);
        for(int row=0;row<r;row++){int inset=rows[row];rect(buffer,x+inset,y+row,x+r,y+row+1);rect(buffer,right-r,y+row,right-inset,y+row+1);rect(buffer,x+inset,bottom-row-1,x+r,bottom-row);rect(buffer,right-r,bottom-row-1,right-inset,bottom-row);}
        rect(buffer,x,y+r,x+r,bottom-r);rect(buffer,right-r,y+r,right,bottom-r);
        tess.b();bfl.w();bfl.k();
    }
    private static void rect(bfd b,int left,int top,int right,int bottom){b.b((double)right,top,0).d();b.b((double)left,top,0).d();b.b((double)left,bottom,0).d();b.b((double)right,bottom,0).d();}
    public static void panel(int width,int height){
        int left=Math.max(4,width/2-155),right=Math.min(width-4,width/2+155);
        rounded(left,6,right,height-6,8,0xf2201726);
        rounded(left+5,6,right-5,9,1,BlossomHooks.accent);
        rounded(left+2,10,right-2,40,6,0xf2382034);
        avp.a(left,height-8,right,height-6,0xff9c4e78);
    }
}
