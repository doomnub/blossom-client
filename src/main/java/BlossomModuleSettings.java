import dev.blossom.core.*;
import org.lwjgl.input.Keyboard;
/** Separate appearance and behavior pages, with pagination at small GUI sizes. */
public final class BlossomModuleSettings extends axu {
    private final axu previous;
    private final Modules.Module module;
    private boolean binding,visuals=true;
    private avb bindingTarget;
    private final boolean global;
    private int page,rows,particleCategory;
    private boolean rebuildPending;
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
            VisualSettings v=BlossomHooks.settings.visual;
            if(module.id.equals("legacy"))for(int i=0;i<LegacySettings.KEYS.length;i++)add(200+i,LegacySettings.LABELS[i]+": "+BlossomHooks.settings.legacy.enabled[i]);
            if(module.id.equals("hittilt"))add(130,"Damage tilt: "+Math.round(BlossomHooks.settings.hitTilt*100)+"% (0 = none)");
            if(module.id.equals("hitcolor")){add(110,"Hit color: "+Integer.toHexString(v.hitColor));add(111,"Opacity: "+v.hitOpacity);add(112,"Duration (ticks): "+v.hitDuration);add(113,"Smooth fade: "+v.hitFade);add(114,"Players: "+v.hitPlayers);add(115,"Mobs: "+v.hitMobs);add(116,"Armor tint: "+v.hitArmor);}
            if(module.id.equals("particles")){int c=particleCategory;add(120,"Category: "+VisualSettings.CATEGORIES[c]);add(121,"Show: "+v.enabled[c]);add(122,"Density / multiplier: "+v.density[c]);add(123,"Scale: "+v.scale[c]);add(124,"Opacity: "+v.opacity[c]);add(125,"Lifetime: "+v.lifetime[c]);add(126,"Color (white = original): "+Integer.toHexString(v.color[c]));add(127,"Reduce all particles to 50%");}

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
            if(module.id.equals("potions")){add(34,"Hide level 1: "+BlossomHooks.settings.hidePotionOne);add(35,"Show when empty: "+BlossomHooks.settings.showEmptyPotions);}
            if(module.id.equals("capephysics")){
                dev.blossom.core.cloth.ClothSettings f=safeCloth();
                add(60,"Subdivisions: "+f.subdivisions);add(61,"Gravity: "+f.gravity);add(62,"Stiffness: "+f.stiffness);add(63,"Damping: "+f.damping);add(64,"Physics iterations: "+f.iterations);add(65,"Self-collision: "+f.selfCollision);add(66,"Player collision: "+f.playerCollision);add(67,"Wind: "+f.wind);add(68,"Wind strength: "+f.windStrength);add(69,"Simulation distance: "+f.distance);
            }
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
    private dev.blossom.core.cloth.ClothSettings safeCloth(){return BlossomHooks.settings.cloth;}
    private void color(boolean bg){
        int value=bg?module.backgroundColor:module.textColor;
        j.a(new BlossomTextInput(this,"Color: 8 hex digits AARRGGBB",String.format("%08x",value),text->{
            if(!text.matches("[0-9a-fA-F]{8}"))throw new IllegalArgumentException("Use 8 hex digits, e.g. ffff83b5");
            int color=(int)Long.parseLong(text,16);if(bg)module.backgroundColor=color;else module.textColor=color;apply(bg?"backgroundColor":"textColor");
        }));
    }
    @Override protected void a(avs button){
        ClientSettings s=BlossomHooks.settings;
        if(button.k>=200&&button.k<200+LegacySettings.KEYS.length){int i=button.k-200;s.legacy.enabled[i]=!s.legacy.enabled[i];rebuildPending=true;return;}
        switch(button.k){
            case 130:j.a(new BlossomSliderScreen(this,"Damage camera tilt",s.hitTilt,0,2,false,v->s.hitTilt=v));return;
            case 110:case 126:
                final int colorId=button.k,category=particleCategory;final VisualSettings vs=s.visual;
                j.a(new BlossomTextInput(this,"Color: AARRGGBB",String.format("%08x",colorId==110?vs.hitColor:vs.color[category]),text->{if(!text.matches("[0-9a-fA-F]{8}"))throw new IllegalArgumentException("Use 8 hex digits");int color=(int)Long.parseLong(text,16);if(colorId==110)vs.hitColor=color;else vs.color[category]=color;}));return;
            case 111:case 112:case 122:case 123:case 124:case 125:{
                final int setting=button.k,cat=particleCategory;final VisualSettings v=s.visual;
                float current=setting==111?v.hitOpacity:setting==112?v.hitDuration:setting==122?v.density[cat]:setting==123?v.scale[cat]:setting==124?v.opacity[cat]:v.lifetime[cat];
                float low=setting==112?1:setting==123?.1f:setting==125?.25f:0,high=setting==112?40:setting==122?4:setting==123||setting==125?3:1;
                j.a(new BlossomSliderScreen(this,button.j,current,low,high,setting==112,value->{switch(setting){case 111:v.hitOpacity=value;break;case 112:v.hitDuration=Math.round(value);break;case 122:v.density[cat]=value;break;case 123:v.scale[cat]=value;break;case 124:v.opacity[cat]=value;break;case 125:v.lifetime[cat]=value;}}));return;
            }
            case 113:s.visual.hitFade=!s.visual.hitFade;break;case 114:s.visual.hitPlayers=!s.visual.hitPlayers;break;case 115:s.visual.hitMobs=!s.visual.hitMobs;break;case 116:s.visual.hitArmor=!s.visual.hitArmor;break;
            case 120:particleCategory=(particleCategory+1)%VisualSettings.CATEGORIES.length;break;case 121:s.visual.enabled[particleCategory]=!s.visual.enabled[particleCategory];break;case 127:java.util.Arrays.fill(s.visual.density,.5f);break;
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
            case 35:s.showEmptyPotions=!s.showEmptyPotions;BlossomHud.invalidate();break;
            case 60:case 61:case 62:case 63:case 64:case 68:case 69:
                final int clothId=button.k;final dev.blossom.core.cloth.ClothSettings c=s.cloth;
                float val=clothId==60?c.subdivisions:clothId==61?c.gravity:clothId==62?c.stiffness:clothId==63?c.damping:clothId==64?c.iterations:clothId==68?c.windStrength:c.distance;
                float min=clothId==60||clothId==69?4:clothId==64?2:0,max=clothId==60||clothId==69?64:clothId==61?30:clothId==62?1:clothId==63?.5f:clothId==64?12:3;
                j.a(new BlossomSliderScreen(this,button.j,val,min,max,clothId==60||clothId==64,v->{switch(clothId){case 60:c.subdivisions=Math.round(v);break;case 61:c.gravity=v;break;case 62:c.stiffness=v;break;case 63:c.damping=v;break;case 64:c.iterations=Math.round(v);break;case 68:c.windStrength=v;break;case 69:c.distance=v;}}));return;
            case 65:s.cloth.selfCollision=!s.cloth.selfCollision;break;
            case 66:s.cloth.playerCollision=!s.cloth.playerCollision;break;
            case 67:s.cloth.wind=!s.cloth.wind;break;
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
            case 92:page=SettingsPages.move(page,rows,perPage(),-1);break;
            case 93:page=SettingsPages.move(page,rows,perPage(),1);break;
            case 99:j.a(previous);return;
            default:break;
        }
        rebuildPending=true;
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
    @Override protected void a(int x,int y,int button) {if(binding){BlossomBindings.set(bindingTarget,button-100);binding=false;b();return;}super.a(x,y,button);if(rebuildPending){rebuildPending=false;b();}}
    @Override public void m(){BlossomHooks.save("default");}
    @Override public boolean d(){return false;}
}
