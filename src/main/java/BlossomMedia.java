import dev.blossom.core.Modules;
import dev.blossom.media.MediaBridge;
import java.awt.image.BufferedImage;
/** Render-thread texture ownership; background media snapshots are immutable. */
public final class BlossomMedia {
    private static final MediaBridge bridge=new MediaBridge();
    private static jy texture;private static BufferedImage uploaded;
    public static void tick(){
        boolean enabled=BlossomHooks.modules.on("media")&&ave.A().h!=null;
        bridge.enabled(enabled);
        MediaBridge.Snapshot s=enabled?bridge.snapshot():null;BufferedImage art=s==null?null:s.art;
        if(art!=uploaded){clear();if(art!=null){texture=ave.A().P().a("blossom/media",new blz(art));uploaded=art;}}
    }
    private static void clear(){if(texture!=null)ave.A().P().c(texture);texture=null;uploaded=null;}
    public static boolean visible(){return bridge.snapshot()!=null||ave.A().m instanceof BlossomHudEditor;}
    public static void draw(Modules.Module module){
        MediaBridge.Snapshot s=bridge.snapshot();if(s==null&&!(ave.A().m instanceof BlossomHudEditor))return;
        int background=(Math.round((module.backgroundColor>>>24)*module.opacity)<<24)|(module.backgroundColor&0xffffff);
        BlossomTheme.rounded(0,0,240,56,7,background);
        BlossomTheme.rounded(6,6,50,50,5,0x55333333);
        if(texture!=null&&s!=null){bfl.l();bfl.a(770,771,1,0);bfl.c(1,1,1,module.opacity);ave.A().P().a(texture);avp.a(6,6,0,0,44,44,44,44);bfl.k();}
        String title=s==null?"Currently playing":s.title,artist=s==null?bridge.status():s.artist;
        int alpha=Math.round(255*module.opacity)<<24;
        ave.A().k.a(trim(title,174),58,8,alpha|0xffffff,module.shadow);
        ave.A().k.a(trim(artist,174),58,21,alpha|0xbcbcbc,module.shadow);
        double duration=s==null?0:s.duration,elapsed=s==null?0:s.elapsed();
        BlossomTheme.rounded(58,35,232,38,1,alpha|0x444444);
        if(duration>0){int end=58+(int)Math.round(174*Math.max(0,Math.min(1,elapsed/duration)));if(end>58)BlossomTheme.rounded(58,35,end,38,1,alpha|(module.textColor&0xffffff));}
        ave.A().k.a(time(elapsed),58,43,alpha|0xbcbcbc,false);String right=duration>0?time(duration):"--:--";ave.A().k.a(right,232-ave.A().k.a(right),43,alpha|0xbcbcbc,false);
        bfl.c(1,1,1,1);
    }
    private static String trim(String text,int width){if(ave.A().k.a(text)<=width)return text;while(!text.isEmpty()&&ave.A().k.a(text+"…")>width)text=text.substring(0,text.length()-1);return text+"…";}
    private static String time(double seconds){long n=(long)Math.max(0,seconds);return (n/60)+":"+(n%60<10?"0":"")+(n%60);}
}
