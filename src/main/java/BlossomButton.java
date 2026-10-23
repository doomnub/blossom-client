/** Pink-accented, high-contrast buttons using Minecraft's normal hit testing. */
public final class BlossomButton extends avs {
    public BlossomButton(int id,int x,int y,int width,int height,String label){super(id,x,y,width,height,label);}
    @Override public void a(ave mc,int mx,int my){
        if(!m)return;
        n=mx>=h&&my>=i&&mx<h+f&&my<i+g;
        int fill=!l?0xff362637:n?0xff69354f:0xff342337;
        avp.a(h,i,h+f,i+g,fill);
        avp.a(h,i,h+2,i+g,!l?0xff79536b:n?0xffffb3d5:BlossomHooks.accent);
        avp.a(h+2,i+g-1,h+f,i+g,n?0xffd879a6:0xff62405b);
        String label=j;while(mc.k.a(label)>f-10&&label.length()>1)label=label.substring(0,label.length()-1);
        mc.k.a(label,h+(f-mc.k.a(label))/2f,i+(g-8)/2f,!l?0xffad869f:0xffffedf7,true);
    }
}
