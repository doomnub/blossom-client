/** Shared menu controls; normal Minecraft hit testing and no background animation. */
public final class BlossomButton extends avs {
    private boolean selected;
    public BlossomButton(int id,int x,int y,int width,int height,String label){super(id,x,y,width,height,label);}
    public BlossomButton selected(boolean value){selected=value;return this;}
    @Override public void a(ave mc,int mx,int my){
        if(!m)return;
        n=mx>=h&&my>=i&&mx<h+f&&my<i+g;
        int edge=selected?BlossomHooks.accent:n&&l?0xff777080:0xff34343f;
        int fill=!l?0xff191a21:selected?0xff442039:n?0xff30313c:0xff22232c;
        BlossomTheme.rounded(h,i,h+f,i+g,5,edge);
        BlossomTheme.rounded(h+1,i+1,h+f-1,i+g-1,4,fill);
        String label=BlossomTheme.fit(mc.k,j,f-12);
        mc.k.a(label,h+(f-mc.k.a(label))/2f,i+(g-8)/2f,!l?0xff9993a2:BlossomTheme.TEXT,false);
    }
}
