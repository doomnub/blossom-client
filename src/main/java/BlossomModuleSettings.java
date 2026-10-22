import dev.blossom.core.*;
import org.lwjgl.input.Keyboard;
/** Buttons expose only settings actually consumed by active modules. */
public final class BlossomModuleSettings extends axu {
    private final axu previous;
    private final Modules.Module module;
    private boolean binding;
    public BlossomModuleSettings(axu previous,Modules.Module module){this.previous=previous;this.module=module;}
    private void add(int id,String label,int row){n.add(new avs(id,l/2-140,48+row*23,280,20,label));}
    @Override public void b(){
        n.clear();int row=0;
        add(0,"Enabled: "+module.enabled,row++);
        if(module.category.equals("HUD")) {
            add(1,String.format(java.util.Locale.ROOT,"Scale: %.2f (cycle)",module.scale),row++);
            add(2,String.format(java.util.Locale.ROOT,"Opacity: %.0f%% (cycle)",module.opacity*100),row++);
            add(3,"Text color: "+Integer.toHexString(module.textColor),row++);
            add(4,"Background: "+Integer.toHexString(module.backgroundColor),row++);
            add(5,"Text shadow: "+module.shadow+" | Locked: "+module.locked,row++);
            add(6,"Reset layout and style",row++);
        }
        if(module.id.equals("zoom")) {
            add(10,"Zoom factor: "+BlossomHooks.settings.zoomFactor,row++);
            add(11,binding?"Press a key (Esc cancels)":"Zoom key: "+Keyboard.getKeyName(BlossomHooks.settings.zoomKey),row++);
        }
        if(module.id.equals("crosshair")) {
            add(20,"Length: "+BlossomHooks.settings.crosshairLength,row++);
            add(21,"Gap: "+BlossomHooks.settings.crosshairGap,row++);
            add(22,"Thickness: "+BlossomHooks.settings.crosshairThickness,row++);
            add(23,"Center dot: "+BlossomHooks.settings.crosshairDot,row++);
            add(24,"Color: "+Integer.toHexString(BlossomHooks.settings.crosshairColor),row++);
        }
        if(module.id.equals("coords"))add(30,"Privacy: "+BlossomHooks.settings.privacy,row++);
        n.add(new avs(99,l/2-140,Math.max(48+row*23,m-30),280,20,"Back"));
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
            case 5:if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))module.locked=!module.locked;else module.shadow=!module.shadow;break;
            case 6:module.resetLayout();break;
            case 10:s.zoomFactor=s.zoomFactor>=10?2:s.zoomFactor+1;break;
            case 11:binding=true;break;
            case 20:s.crosshairLength=s.crosshairLength%20+1;break;
            case 21:s.crosshairGap=(s.crosshairGap+1)%17;break;
            case 22:s.crosshairThickness=s.crosshairThickness%6+1;break;
            case 23:s.crosshairDot=!s.crosshairDot;break;
            case 24:j.a(new BlossomTextInput(this,"Crosshair color: AARRGGBB",String.format("%08x",s.crosshairColor),text->{if(!text.matches("[0-9a-fA-F]{8}"))throw new IllegalArgumentException("Use 8 hex digits");s.crosshairColor=(int)Long.parseLong(text,16);}));return;
            case 30:s.privacy=!s.privacy;break;
            case 99:j.a(previous);return;
            default:break;
        }
        b();
    }
    @Override public void a(int x,int y,float delta){c();q.a(module.name,l/2-140,16,BlossomHooks.accent);q.a(module.category.equals("HUD")?"Shift-click shadow to lock/unlock position":module.description,l/2-140,32,0xffcccccc);super.a(x,y,delta);}
    @Override protected void a(char typed,int key){
        if(binding){if(key!=Keyboard.KEY_ESCAPE&&key>0){if(key==BlossomHooks.settings.menuKey){BlossomHooks.message="Key conflicts with mod menu";binding=false;b();return;}BlossomHooks.settings.zoomKey=key;}binding=false;b();return;}
        if(key==Keyboard.KEY_ESCAPE)j.a(previous);
    }
    @Override public void m(){BlossomHooks.save("default");}
    @Override public boolean d(){return false;}
}
