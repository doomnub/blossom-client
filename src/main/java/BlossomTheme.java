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
    public static final int SURFACE=0xf5111218, BORDER=0xff33343f, TEXT=0xfff4f1f7, MUTED=0xffa4a1b1;
    public static void surface(int left,int top,int right,int bottom){
        rounded(left,top+3,right,bottom+3,9,0x50000000);
        rounded(left,top,right,bottom,9,BORDER);
        rounded(left+1,top+1,right-1,bottom-1,8,SURFACE);
    }
    public static String fit(avn font,String text,int width){
        if(font.a(text)<=width)return text;
        while(text.length()>0&&font.a(text+"...")>width)text=text.substring(0,text.length()-1);
        return text+"...";
    }
    public static void panel(int width,int height){
        int left=Math.max(4,width/2-155),right=Math.min(width-4,width/2+155);
        surface(left,6,right,height-6);
        rounded(left,20,left+3,30,1,BlossomHooks.accent);
        avp.a(left+12,40,right-12,41,0xff2b2c35);
    }
}
