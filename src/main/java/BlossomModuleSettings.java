import dev.blossom.core.*;
import org.lwjgl.input.Keyboard;
/** Separate appearance and behavior pages, with pagination at small GUI sizes. */
public final class BlossomModuleSettings extends axu {
    private final axu previous;
    private final Modules.Module module;
    private boolean binding,visuals=true;
    private int page,rows;
    public BlossomModuleSettings(axu previous,Modules.Module module){this.previous=previous;this.module=module;}
    private int perPage(){return Math.max(1,(m-124)/22);}
    private void add(int id,String label){int row=rows++;if(row/perPage()==page)n.add(new avs(id,l/2-140,72+(row%perPage())*22,280,20,label));}
    @Override public void b(){
        n.clear();rows=0;
        avs visual=new avs(90,l/2-140,44,138,20,"Visuals"),functional=new avs(91,l/2+2,44,138,20,"Functionality");
        visual.l=!visuals;functional.l=visuals;n.add(visual);n.add(functional);
        if(visuals){
            if(module.category.equals("HUD")){
                add(1,String.format(java.util.Locale.ROOT,"Text size: %.2fx",module.scale));
                add(2,String.format(java.util.Locale.ROOT,"Opacity: %.0f%%",module.opacity*100));
                add(3,"Text color: "+Integer.toHexString(module.textColor));
                add(4,"Background color: "+Integer.toHexString(module.backgroundColor));
                add(5,"Text shadow: "+module.shadow);add(7,"Lock position: "+module.locked);add(6,"Reset layout and style");
            }
            if(module.id.equals("crosshair")){
                add(20,"Length: "+BlossomHooks.settings.crosshairLength);add(21,"Gap: "+BlossomHooks.settings.crosshairGap);
                add(22,"Thickness: "+BlossomHooks.settings.crosshairThickness);add(23,"Center dot: "+BlossomHooks.settings.crosshairDot);
                add(24,"Color: "+Integer.toHexString(BlossomHooks.settings.crosshairColor));
            }
            if(module.id.equals("cape"))add(40,"Choose cape / library");
        }else{
            add(0,"Enabled: "+module.enabled);
            if(module.id.equals("fps")){add(31,"FPS display: "+(BlossomHooks.settings.fpsAverage?"Average":"Current"));add(32,"Show 1% / 0.1% lows: "+BlossomHooks.settings.fpsLows);}
            if(module.id.equals("zoom")){add(10,"Zoom factor: "+BlossomHooks.settings.zoomFactor);add(11,binding?"Press a key (Esc cancels)":"Zoom key: "+Keyboard.getKeyName(BlossomHooks.settings.zoomKey));}
            if(module.id.equals("coords"))add(30,"Privacy: "+BlossomHooks.settings.privacy);
            if(module.id.equals("cape"))add(40,"Animation playback / library");
        }
        if(rows>perPage()){
            n.add(new avs(92,l/2-140,m-50,138,20,"Previous page"));n.add(new avs(93,l/2+2,m-50,138,20,"Next page"));
        }
        n.add(new avs(99,l/2-140,m-26,280,20,"Back"));
    }
    private void color(boolean bg){
        int value=bg?module.backgroundColor:module.textColor;
        j.a(new BlossomTextInput(this,"Color: 8 hex digits AARRGGBB",String.format("%08x",value),text->{
            if(!text.matches("[0-9a-fA-F]{8}"))throw new IllegalArgumentException("Use 8 hex digits, e.g. ffff83b5");
            int color=(int)Long.parseLong(text,16);if(bg)module.backgroundColor=color;else module.textColor=color;
        }));
    }
    @Override protected void a(avs button){
        ClientSettings s=BlossomHooks.settings;
        switch(button.k){
            case 0:module.enabled=!module.enabled;break;
            case 1:module.scale=module.scale>=3?.5f:Math.min(3,module.scale+.25f);break;
            case 2:module.opacity=module.opacity<=.11f?1:module.opacity-.1f;break;
            case 3:color(false);return;
            case 4:color(true);return;
            case 5:module.shadow=!module.shadow;break;
            case 6:module.resetLayout();break;
            case 7:module.locked=!module.locked;break;
            case 10:s.zoomFactor=s.zoomFactor>=10?2:s.zoomFactor+1;break;
            case 11:binding=true;break;
            case 20:s.crosshairLength=s.crosshairLength%20+1;break;
            case 21:s.crosshairGap=(s.crosshairGap+1)%17;break;
            case 22:s.crosshairThickness=s.crosshairThickness%6+1;break;
            case 23:s.crosshairDot=!s.crosshairDot;break;
            case 24:j.a(new BlossomTextInput(this,"Crosshair color: AARRGGBB",String.format("%08x",s.crosshairColor),text->{if(!text.matches("[0-9a-fA-F]{8}"))throw new IllegalArgumentException("Use 8 hex digits");s.crosshairColor=(int)Long.parseLong(text,16);}));return;
            case 30:s.privacy=!s.privacy;break;
            case 31:s.fpsAverage=!s.fpsAverage;break;
            case 32:s.fpsLows=!s.fpsLows;break;
            case 40:j.a(new BlossomCapeMenu(this));return;
            case 90:visuals=true;page=0;binding=false;break;
            case 91:visuals=false;page=0;binding=false;break;
            case 92:page=(page+(rows-1)/perPage())%((rows-1)/perPage()+1);break;
            case 93:page=(page+1)%((rows-1)/perPage()+1);break;
            case 99:j.a(previous);return;
            default:break;
        }
        b();
    }
    @Override public void a(int x,int y,float delta){
        c();q.a(module.name,l/2-140,12,BlossomHooks.accent);
        q.a(visuals?"Appearance and HUD layout":"Module behavior and displayed information",l/2-140,28,0xffcccccc);
        if(rows==0)q.a("No visual options for this module.",l/2-140,76,0xffcccccc);
        super.a(x,y,delta);
    }
    @Override protected void a(char typed,int key){
        if(binding){if(key!=Keyboard.KEY_ESCAPE&&key>0){if(key==BlossomHooks.settings.menuKey){BlossomHooks.message="Key conflicts with mod menu";binding=false;b();return;}BlossomHooks.settings.zoomKey=key;}binding=false;b();return;}
        if(key==Keyboard.KEY_ESCAPE)j.a(previous);
    }
    @Override public void m(){BlossomHooks.save("default");}
    @Override public boolean d(){return false;}
}
