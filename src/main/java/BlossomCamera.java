import dev.blossom.core.*;
import org.lwjgl.input.Keyboard;
/** Render-only FOV and zoom; preserves user game settings and vanilla movement. */
public final class BlossomCamera {
    private static final SmoothZoom zoom=new SmoothZoom();
    private static Object player;
    private static boolean zoomActive;
    private static float previousProjection=Float.NaN;
    private static float projection(float value){
        if(Float.compare(value,previousProjection)!=0){
            previousProjection=value;
            if(ave.A().g!=null)ave.A().g.m();
        }
        return value;
    }
    private static boolean held(){ave mc=ave.A();return mc.h!=null&&mc.m==null&&BlossomHooks.modules.on("zoom")&&BlossomBindings.held(BlossomBindings.zoom);}
    public static float fov(float vanilla,boolean worldProjection){
        boolean enabled=BlossomHooks.modules.on("zoom");
        if(!enabled&&zoomActive){zoom.reset();zoomActive=false;}
        if(!enabled&&!BlossomHooks.modules.on("fov")){
            if(worldProjection&&!Float.isNaN(previousProjection)){projection(vanilla);previousProjection=Float.NaN;}
            return vanilla;
        }
        zoomActive=enabled;
        ave mc=ave.A();if(mc.h!=player){zoom.reset();player=mc.h;}
        if(!worldProjection||mc.h==null)return vanilla;
        long now=System.nanoTime();double ratio=enabled?zoom.update(held(),BlossomHooks.settings.zoomFactor,now):1;
        float value=vanilla;
        if(BlossomHooks.modules.on("fov")){
            int speed=0;for(Object o:mc.h.bl()){pf effect=(pf)o;if(effect.a()==1)speed=effect.c()+1;}
            float bow=mc.h.bS()&&mc.h.bQ()!=null&&mc.h.bQ().b()==zy.f?Math.min(1,mc.h.bT()/20f):0;
            value=BlossomHooks.settings.fov.calculate(mc.h.aw(),speed,mc.h.a(pe.d),mc.h.bA.b,bow);
        }
        return projection((float)(value*ratio));
    }
    public static int wheel(int delta){
        if(delta==0||!held())return delta;
        long now=System.nanoTime();zoom.update(true,BlossomHooks.settings.zoomFactor,now);zoom.scroll(delta,now);return 0;
    }
}
