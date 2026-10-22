import dev.blossom.core.Modules;
import java.util.*;
import org.lwjgl.input.Keyboard;
/** A native Minecraft screen, not an overlay stealing clicks from gameplay. */
public final class BlossomMenu extends axu {
    private final axu previous;
    private String query="";
    private final List<Modules.Module> visible=new ArrayList<Modules.Module>();
    private int page;
    public BlossomMenu(axu previous){this.previous=previous;}
    @Override public void b() {
        n.clear();visible.clear();
        for(Modules.Module module:BlossomHooks.modules.all()) if((module.name+" "+module.category).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) visible.add(module);
        int perPage=Math.max(1,(m-140)/24);page=Math.min(page,Math.max(0,(visible.size()-1)/perPage));
        for(int index=page*perPage;index<Math.min(visible.size(),(page+1)*perPage);index++) {
            Modules.Module module=visible.get(index);
            n.add(new avs(index,l/2-145,52+(index-page*perPage)*24,232,20,(module.enabled?"§aON  ":"§7OFF ")+module.name));
            n.add(new avs(200+index,l/2+91,52+(index-page*perPage)*24,54,20,"Settings"));
        }
        int y=m-76;
        n.add(new avs(100,l/2-145,y,94,20,"Edit HUD"));
        n.add(new avs(101,l/2-47,y,94,20,"Capes"));
        n.add(new avs(102,l/2+51,y,94,20,"Cape folder"));
        n.add(new avs(103,l/2-145,y+24,94,20,"Save preset"));
        n.add(new avs(104,l/2-47,y+24,94,20,"Load preset"));
        n.add(new avs(105,l/2+51,y+24,94,20,"Background"));
        if(visible.size()>perPage) n.add(new avs(106,l/2+150,52,38,20,"Next"));
    }
    @Override protected void a(avs button) {
        if(button.k>=200){Modules.Module module=visible.get(button.k-200);j.a(module.id.equals("cape")?new BlossomCapeMenu(this):new BlossomModuleSettings(this,module));return;}
        if(button.k<100){Modules.Module module=visible.get(button.k);module.enabled=!module.enabled;}
        else switch(button.k) {
            case 100:j.a(new BlossomHudEditor(this));return;
            case 101:j.a(new BlossomCapeMenu(this));return;
            case 102:BlossomHooks.capes.openFolder();break;
            case 103:BlossomHooks.save("preset");break;
            case 104:BlossomHooks.load("preset");break;
            case 105:BlossomHooks.background=!BlossomHooks.background;break;
            case 106:page++;if(page*Math.max(1,(m-140)/24)>=visible.size()) page=0;break;
            default:break;
        }
        BlossomHooks.save("default");b();
    }
    @Override public void a(int mx,int my,float partial) {
        c();a(l/2-155,8,l/2+155,m-12,0xe51a1720);
        q.a("BLOSSOM / Modules",(float)l/2-145,17f,BlossomHooks.accent);
        q.a("Search: "+query+"_",l/2-145,35,0xffeeeeee);
        super.a(mx,my,partial);
        String status=BlossomHooks.capes.status;
        if(status.length()>48) status=status.substring(0,45)+"...";
        q.a(status,l/2-145,m-25,0xffbbbbbb);
    }
    @Override protected void a(char typed,int key) {
        if(key==Keyboard.KEY_ESCAPE||key==Keyboard.KEY_RSHIFT){j.a(previous);return;}
        if(key==Keyboard.KEY_BACK&&query.length()>0)query=query.substring(0,query.length()-1);
        else if(typed>=32&&typed!=127&&query.length()<40)query+=typed;
        page=0;b();
    }
    @Override public void m(){BlossomHooks.save("default");}
    @Override public boolean d(){return false;}
}
