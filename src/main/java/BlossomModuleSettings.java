import dev.blossom.core.*;
import org.lwjgl.input.Keyboard;
/** Separate appearance and behavior pages, with pagination at small GUI sizes. */
public final class BlossomModuleSettings extends axu {
    private final axu previous;
    private final Modules.Module module;
    private boolean binding,visuals=true;
    private avb bindingTarget;
    private final boolean global;
    private int page,rows;
    public BlossomModuleSettings(axu previous,Modules.Module module){this.previous=previous;this.module=module;this.global=false;}
    public BlossomModuleSettings(axu previous){
        this.previous=previous;this.global=true;this.module=new Modules.Module("all","All HUD visuals","HUD","",true,0);
        Modules.Module source=BlossomHooks.modules.get("fps");module.scale=source.scale;module.opacity=source.opacity;module.textColor=source.textColor;module.backgroundColor=source.backgroundColor;module.shadow=source.shadow;
    }
    private void apply(String setting){if(global)BlossomHooks.modules.applyVisual(setting,module);}
    private int perPage(){return Math.max(1,(m-124)/22);}
    private void add(int id,String label){int row=rows++;if(row/perPage()==page)n.add(new BlossomButton(id,l/2-140,72+(row%perPage())*22,280,20,label));}
    @Override public void b(){
        n.clear();rows=0;
        avs visual=new BlossomButton(90,l/2-140,44,138,20,"Visuals"),functional=new BlossomButton(91,l/2+2,44,138,20,"Functionality");
        visual.l=!visuals;functional.l=visuals;n.add(visual);if(!global)n.add(functional);
        if(visuals){
            if(module.category.equals("HUD")){
                add(1,String.format(java.util.Locale.ROOT,"Text size: %.2fx",module.scale));
                add(2,String.format(java.util.Locale.ROOT,"Opacity: %.0f%%",module.opacity*100));
                add(3,"Text color: "+Integer.toHexString(module.textColor));
                add(4,"Background color: "+Integer.toHexString(module.backgroundColor));
                add(5,"Text shadow: "+module.shadow);if(!global){add(7,"Lock position: "+module.locked);add(6,"Reset layout and style");}
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
            if(module.id.equals("zoom")){add(10,"Zoom factor: "+BlossomHooks.settings.zoomFactor);add(11,binding?"Press key / mouse (Esc unbinds)":"Zoom key: "+BlossomBindings.label(BlossomBindings.zoom));}
            if(module.id.equals("sprint")||module.id.equals("sneak"))add(12,binding?"Press key / mouse (Esc unbinds)":"Key: "+BlossomBindings.label(module.id.equals("sprint")?j.t.ae:j.t.ad));
            if(module.id.equals("keys"))add(13,"Edit movement / mouse controls");
            if(module.id.equals("rawmouse"))add(14,BlossomMouse.status());
            if(module.id.equals("coords"))add(30,"Privacy: "+BlossomHooks.settings.privacy);
            if(module.id.equals("cape"))add(40,"Animation playback / library");
            if(HudText.supportsCompact(module.id))add(33,"Extra text / units: "+module.extraText);
            if(module.id.equals("potions"))add(34,"Hide level 1: "+BlossomHooks.settings.hidePotionOne);
            if(module.id.equals("fov")){
                FovSettings f=BlossomHooks.settings.fov;
                add(50,"FOV mode: "+(f.constant?"Static":"Dynamic"));add(51,"Base FOV: "+f.base);
                add(53,"Sprint FOV: "+f.sprint);add(54,"Speed I FOV: "+f.speed);add(55,"Speed II+ FOV: "+f.speedTwo);
                add(56,"Slowness FOV: "+f.slowness);add(57,"Flying FOV: "+f.flying);add(58,"Drawn bow FOV: "+f.bow);
            }
        }
        if(rows>perPage()){
            n.add(new BlossomButton(92,l/2-140,m-50,138,20,"Previous page"));n.add(new BlossomButton(93,l/2+2,m-50,138,20,"Next page"));
        }
        n.add(new BlossomButton(99,l/2-140,m-26,280,20,"Back"));
    }
    private void color(boolean bg){
        int value=bg?module.backgroundColor:module.textColor;
        j.a(new BlossomTextInput(this,"Color: 8 hex digits AARRGGBB",String.format("%08x",value),text->{
            if(!text.matches("[0-9a-fA-F]{8}"))throw new IllegalArgumentException("Use 8 hex digits, e.g. ffff83b5");
            int color=(int)Long.parseLong(text,16);if(bg)module.backgroundColor=color;else module.textColor=color;apply(bg?"backgroundColor":"textColor");
        }));
    }
    @Override protected void a(avs button){
        ClientSettings s=BlossomHooks.settings;
        switch(button.k){
            case 0:module.enabled=!module.enabled;break;
            case 1:module.scale=module.scale>=3?.5f:Math.min(3,module.scale+.25f);apply("scale");break;
            case 2:module.opacity=module.opacity<=.11f?1:module.opacity-.1f;apply("opacity");break;
            case 3:color(false);return;
            case 4:color(true);return;
            case 5:module.shadow=!module.shadow;apply("shadow");break;
            case 6:module.resetLayout();break;
            case 7:module.locked=!module.locked;break;
            case 10:s.zoomFactor=s.zoomFactor>=10?2:s.zoomFactor+1;break;
            case 11:bindingTarget=BlossomBindings.zoom;binding=true;break;
            case 12:bindingTarget=module.id.equals("sprint")?j.t.ae:j.t.ad;binding=true;break;
            case 13:j.a(new ayj(this,j.t));return;
            case 20:s.crosshairLength=s.crosshairLength%20+1;break;
            case 21:s.crosshairGap=(s.crosshairGap+1)%17;break;
            case 22:s.crosshairThickness=s.crosshairThickness%6+1;break;
            case 23:s.crosshairDot=!s.crosshairDot;break;
            case 24:j.a(new BlossomTextInput(this,"Crosshair color: AARRGGBB",String.format("%08x",s.crosshairColor),text->{if(!text.matches("[0-9a-fA-F]{8}"))throw new IllegalArgumentException("Use 8 hex digits");s.crosshairColor=(int)Long.parseLong(text,16);}));return;
            case 30:s.privacy=!s.privacy;break;
            case 31:s.fpsAverage=!s.fpsAverage;break;
            case 32:s.fpsLows=!s.fpsLows;break;
            case 33:module.extraText=!module.extraText;break;
            case 34:s.hidePotionOne=!s.hidePotionOne;break;
            case 50:s.fov.setConstant(!s.fov.constant);break;
            
            case 51:case 53:case 54:case 55:case 56:case 57:case 58:
                final int id=button.k;final FovSettings f=s.fov;
                float current=id==51?f.base:id==53?f.sprint:id==54?f.speed:id==55?f.speedTwo:id==56?f.slowness:id==57?f.flying:f.bow;
                j.a(new BlossomTextInput(this,"FOV in degrees (30 to 110)",Float.toString(current),text->{
                    float v;try{v=Float.parseFloat(text);}catch(NumberFormatException e){throw new IllegalArgumentException("Enter a number from 30 to 110");}
                    if(!Float.isFinite(v)||v<30||v>110)throw new IllegalArgumentException("Enter a number from 30 to 110");
                    switch(id){case 51:f.base=v;break;case 53:f.sprint=v;break;case 54:f.speed=v;break;case 55:f.speedTwo=v;break;case 56:f.slowness=v;break;case 57:f.flying=v;break;default:f.bow=v;}
                }));return;
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
        c();BlossomTheme.panel(l,m);q.a(module.name,l/2-140,12,BlossomHooks.accent);
        q.a(global?"Applies to every HUD, including disabled ones":visuals?"Appearance and HUD layout":"Module behavior and displayed information",l/2-140,28,0xffcccccc);
        if(rows==0)q.a("No visual options for this module.",l/2-140,76,0xffcccccc);
        super.a(x,y,delta);
    }
    @Override protected void a(char typed,int key){
        if(binding){BlossomBindings.set(bindingTarget,key==Keyboard.KEY_ESCAPE?0:key);binding=false;b();return;}
        if(key==Keyboard.KEY_ESCAPE)j.a(previous);
    }
    @Override protected void a(int x,int y,int button) {if(binding){BlossomBindings.set(bindingTarget,button-100);binding=false;b();return;}super.a(x,y,button);}
    @Override public void m(){BlossomHooks.save("default");}
    @Override public boolean d(){return false;}
}
