import dev.blossom.core.Modules;
import java.util.*;
import org.lwjgl.input.Keyboard;

/** Responsive module library with category filters and explicit toggle states. */
public final class BlossomMenu extends axu {
    private static final String[] CATEGORIES={"All","HUD","Controls","Visual","Cosmetics"};
    private final axu previous;
    private final List<Modules.Module> visible=new ArrayList<Modules.Module>();
    private String query="";
    private int page,category,left,top,width,height,body,columns,perPage,cardWidth;
    public BlossomMenu(axu previous){this.previous=previous;}

    @Override public void b(){
        width=Math.min(560,l-16);height=Math.min(430,m-16);left=(l-width)/2;top=(m-height)/2;
        body=left+108;columns=width>=470?2:1;cardWidth=(width-120-(columns-1)*8)/columns;
        perPage=Math.max(1,(height-132)/43)*columns;
        n.clear();visible.clear();
        n.add(new BlossomButton(111,left+10,top+31,86,23,BlossomTheme.styleLabel()));
        String search=query.toLowerCase(Locale.ROOT);
        for(Modules.Module module:BlossomHooks.modules.all()){
            if((category==0||module.category.equals(CATEGORIES[category]))&&(module.name+" "+module.category).toLowerCase(Locale.ROOT).contains(search))visible.add(module);
        }
        page=Math.min(page,Math.max(0,(visible.size()-1)/perPage));
        for(int c=0;c<CATEGORIES.length;c++)n.add(new BlossomButton(300+c,left+10,top+61+c*(height<280?20:25),86,height<280?18:21,CATEGORIES[c]).selected(category==c));
        for(int index=page*perPage;index<Math.min(visible.size(),(page+1)*perPage);index++){
            int slot=index-page*perPage,x=body+(slot%columns)*(cardWidth+8),y=top+62+(slot/columns)*43;
            n.add(new ModuleCard(index,x,y,cardWidth-30,37,visible.get(index)));
            n.add(new BlossomButton(200+index,x+cardWidth-28,y,28,37,"..."));
        }
        int y=top+height-59,half=(width-32)/2;
        n.add(new BlossomButton(100,left+10,y,half,21,"Edit HUD layout"));
        n.add(new BlossomButton(107,left+22+half,y,half,21,"HUD appearance"));
        int quarter=(width-44)/4;
        n.add(new BlossomButton(103,left+10,y+26,quarter,21,"Presets"));
        n.add(new BlossomButton(109,left+18+quarter,y+26,quarter,21,"Keybinds"));
        n.add(new BlossomButton(105,left+26+2*quarter,y+26,quarter,21,"Background").selected(BlossomHooks.background));
        n.add(new BlossomButton(108,left+34+3*quarter,y+26,quarter,21,"Done"));
        if(visible.size()>perPage){
            n.add(new BlossomButton(110,left+width-58,top+33,21,20,"<"));
            n.add(new BlossomButton(106,left+width-33,top+33,21,20,">"));
        }
    }
    @Override protected void a(avs button){
        int id=button.k;
        if(id>=300){category=id-300;page=0;b();return;}
        if(id>=200){j.a(new BlossomModuleSettings(this,visible.get(id-200)));return;}
        if(id<100){Modules.Module module=visible.get(id);module.enabled=!module.enabled;}
        else switch(id){
            case 111:BlossomTheme.toggle();b();return;
            case 100:j.a(new BlossomHudEditor(this));return;
            case 103:j.a(new BlossomPresets(this));return;
            case 105:BlossomHooks.background=!BlossomHooks.background;break;
            case 107:j.a(new BlossomModuleSettings(this));return;
            case 109:j.a(new ayj(this,j.t));return;
            case 108:j.a(previous);return;
            case 106:page=(page+1)%Math.max(1,(visible.size()+perPage-1)/perPage);b();return;
            case 110:page=(page+Math.max(1,(visible.size()+perPage-1)/perPage)-1)%Math.max(1,(visible.size()+perPage-1)/perPage);b();return;
            default:return;
        }
        BlossomHooks.save("default");b();
    }
    @Override public void a(int mx,int my,float partial){
        c();BlossomTheme.surface(left,top,left+width,top+height);
        q.a("blossom",left+12,top+13,BlossomTheme.TEXT);
        q.a("MODULE LIBRARY",body,top+13,BlossomHooks.accent);
        String pagination=(page+1)+" / "+Math.max(1,(visible.size()+perPage-1)/perPage);
        q.a(pagination,left+width-12-q.a(pagination),top+13,BlossomTheme.MUTED);

        int searchRight=left+width-(visible.size()>perPage?66:12);
        BlossomTheme.rounded(body,top+32,searchRight,top+54,5,BlossomTheme.glass(0xff23242e,130));
        q.a(BlossomTheme.fit(q,query.length()==0?"Type to search...":query+"_",searchRight-body-16),body+8,top+39,query.length()==0?BlossomTheme.MUTED:BlossomTheme.TEXT);
        if(visible.isEmpty())q.a("No matching modules",body+8,top+75,BlossomTheme.MUTED);
        avp.a(left+10,top+height-66,left+width-10,top+height-65,0xff30313b);
        super.a(mx,my,partial);
    }
    @Override protected void a(char typed,int key){
        if(key==Keyboard.KEY_ESCAPE||key==BlossomBindings.menu.i()){j.a(previous);return;}
        if(key==Keyboard.KEY_BACK&&query.length()>0)query=query.substring(0,query.length()-1);
        else if(typed>=32&&typed!=127&&query.length()<40)query+=typed;
        page=0;b();
    }
    @Override public void m(){BlossomHooks.save("default");}
    @Override public boolean d(){return false;}

    private static final class ModuleCard extends avs {
        private final Modules.Module module;
        ModuleCard(int id,int x,int y,int width,int height,Modules.Module module){super(id,x,y,width,height,module.name);this.module=module;}
        @Override public void a(ave mc,int mx,int my){
            if(!m)return;
            n=mx>=h&&my>=i&&mx<h+f&&my<i+g;
            BlossomTheme.rounded(h,i,h+f,i+g,6,BlossomTheme.glass(module.enabled?0xff633050:n?0xff5b5666:0xff33343f,module.enabled?100:40));
            BlossomTheme.rounded(h+1,i+1,h+f-1,i+g-1,5,BlossomTheme.glass(n?0xff2d2b36:0xff20212a,n?155:115));
            mc.k.a(BlossomTheme.fit(mc.k,module.name,f-16),h+8,i+7,BlossomTheme.TEXT);
            mc.k.a(module.enabled?"Enabled":"Disabled",h+8,i+22,module.enabled?0xffff8cdd:BlossomTheme.MUTED);
            int x=h+f-29,y=i+22;
            BlossomTheme.rounded(x,y,x+21,y+9,4,module.enabled?BlossomHooks.accent:0xff52515e);
            BlossomTheme.rounded(x+(module.enabled?13:2),y+2,x+(module.enabled?19:8),y+7,2,0xfff4f1f7);
        }
    }
}
