/** Reuses the current vanilla frustum. Specialized particle renderers remain unchanged. */
public final class BlossomVisibility {
    private static bic frustum;
    public static void reset(){frustum=null;}
    public static void camera(pk entity,double partial,bia camera){
        frustum=camera instanceof bic?(bic)camera:null;
        BlossomPerformance.camera(entity,partial);
    }
    public static boolean particle(beb p,float scale,float partial,float rx,float rxz,float rz,float ryz,float rxy){
        if(frustum==null||!BlossomPerformance.enabled()||!BlossomHooks.settings.performance.particleCull)return true;
        double x=p.p+(p.s-p.p)*partial,y=p.q+(p.t-p.q)*partial,z=p.r+(p.u-p.r)*partial;
        double s=Math.abs(scale)*.1,ex=s*(Math.abs(rx)+Math.abs(ryz))+.05,ey=s*Math.abs(rxz)+.05,ez=s*(Math.abs(rz)+Math.abs(rxy))+.05;
        if(!Double.isFinite(x+y+z+ex+ey+ez))return true;
        return frustum.b(x-ex,y-ey,z-ez,x+ex,y+ey,z+ez);
    }
}
