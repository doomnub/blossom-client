/** Reuses the current vanilla frustum. Specialized particle renderers remain unchanged. */
public final class BlossomVisibility {
    private static bic frustum;
    private static boolean active;
    public static void reset(){frustum=null;active=false;}
    public static void camera(pk entity,double partial,bia camera){
        frustum=camera instanceof bic?(bic)camera:null;
        active=frustum!=null&&BlossomHooks.modules.on("performance")&&BlossomHooks.settings.performance.particleCull;
        if(BlossomPerformance.enabled())BlossomPerformance.camera(entity,partial);
    }
    public static boolean particle(beb p,float scale,float partial,float rx,float rxz,float rz,float ryz,float rxy){
        if(!active)return true;
        double x=p.p+(p.s-p.p)*partial,y=p.q+(p.t-p.q)*partial,z=p.r+(p.u-p.r)*partial;
        return quad(frustum,x,y,z,scale,rx,rxz,rz,ryz,rxy);
    }
    public static boolean quad(bic camera,double x,double y,double z,float scale,float rx,float rxz,float rz,float ryz,float rxy){
        double s=Math.abs(scale)*.1,ex=s*(Math.abs(rx)+Math.abs(ryz))+.05,ey=s*Math.abs(rxz)+.05,ez=s*(Math.abs(rz)+Math.abs(rxy))+.05;
        if(!Double.isFinite(x+y+z+ex+ey+ez))return true;
        return camera.b(x-ex,y-ey,z-ez,x+ex,y+ey,z+ez);
    }
}
