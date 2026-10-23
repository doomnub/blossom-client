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
        int perPage=Math.max(1,(m-164)/24);page=Math.min(page,Math.max(0,(visible.size()-1)/perPage));
        for(int index=page*perPage;index<Math.min(visible.size(),(page+1)*perPage);index++) {
            Modules.Module module=visible.get(index);
            n.add(new BlossomButton(index,l/2-145,52+(index-page*perPage)*24,232,20,(module.enabled?"§dON  ":"§7OFF ")+module.name));
            n.add(new BlossomButton(200+index,l/2+91,52+(index-page*perPage)*24,54,20,"Settings"));
        }
        int y=m-100;
        n.add(new BlossomButton(100,l/2-145,y,94,20,"Edit HUD"));
        n.add(new BlossomButton(101,l/2-47,y,94,20,"Capes"));
        n.add(new BlossomButton(102,l/2+51,y,94,20,"Cape folder"));
        n.add(new BlossomButton(103,l/2-145,y+24,94,20,"Save preset"));
        n.add(new BlossomButton(104,l/2-47,y+24,94,20,"Load preset"));
        n.add(new BlossomButton(105,l/2+51,y+24,94,20,"Background"));
        n.add(new BlossomButton(107,l/2-145,y+48,192,20,"All HUD visuals"));
        n.add(new BlossomButton(108,l/2+51,y+48,94,20,"Back"));
        if(visible.size()>perPage) n.add(new BlossomButton(106,l/2+95,28,50,20,"Next"));
    }
    @Override protected void a(avs button) {
        if(button.k>=200){Modules.Module module=visible.get(button.k-200);j.a(new BlossomModuleSettings(this,module));return;}
        if(button.k<100){Modules.Module module=visible.get(button.k);module.enabled=!module.enabled;}
        else switch(button.k) {
            case 100:j.a(new BlossomHudEditor(this));return;
            case 101:j.a(new BlossomCapeMenu(this));return;
            case 102:BlossomHooks.capes.openFolder();break;
            case 103:BlossomHooks.save("preset");break;
            case 104:BlossomHooks.load("preset");break;
            case 105:BlossomHooks.background=!BlossomHooks.background;break;
            case 107:j.a(new BlossomModuleSettings(this));return;
            case 108:j.a(previous);return;
            case 106:page++;if(page*Math.max(1,(m-164)/24)>=visible.size()) page=0;break;
            default:break;
        }
        BlossomHooks.save("default");b();
    }
    @Override public void a(int mx,int my,float partial) {
        c();BlossomTheme.panel(l,m);
        q.a("BLOSSOM / Modules",(float)l/2-145,17f,BlossomHooks.accent);
        q.a("Search: "+query+"_",l/2-145,35,0xffeeeeee);
        super.a(mx,my,partial);
        String status=BlossomHooks.capes.status;
        if(status.length()>48) status=status.substring(0,45)+"...";
        q.a(status,l/2-145,m-25,0xffd8a9c4);
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
