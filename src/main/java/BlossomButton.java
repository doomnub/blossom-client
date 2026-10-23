/** Pink-accented, high-contrast buttons using Minecraft's normal hit testing. */
public final class BlossomButton extends avs {
    public BlossomButton(int id,int x,int y,int width,int height,String label){super(id,x,y,width,height,label);}
    @Override public void a(ave mc,int mx,int my){
        if(!m)return;
        n=mx>=h&&my>=i&&mx<h+f&&my<i+g;
        int fill=!l?0xff362637:n?0xff69354f:0xff342337;
        BlossomTheme.rounded(h,i,h+f,i+g,5,!l?0xff67435b:n?0xffffb3d5:0xff925374);
        BlossomTheme.rounded(h+1,i+1,h+f-1,i+g-1,4,fill);
        String label=j;while(mc.k.a(label)>f-10&&label.length()>1)label=label.substring(0,label.length()-1);
        mc.k.a(label,h+(f-mc.k.a(label))/2f,i+(g-8)/2f,!l?0xffad869f:0xffffedf7,true);
    }
}
