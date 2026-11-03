/** Camera construction samples the toggle; clipping planes remain live. Stateless support-corner test against the current vanilla clipping planes. */
public final class BlossomFrustum {
    private static final boolean ALLOWED=!"false".equalsIgnoreCase(System.getProperty("blossom.fastFrustum","true"));
    public static boolean enabled(){return ALLOWED&&BlossomHooks.settings.performance.fastFrustum&&BlossomHooks.modules.on("performance");}
    public static boolean visible(bid clip,double x0,double y0,double z0,double x1,double y1,double z1,boolean active){
        if(!active)return clip.b(x0,y0,z0,x1,y1,z1);
        return test(clip,x0,y0,z0,x1,y1,z1);
    }
    public static boolean test(bid clip,double x0,double y0,double z0,double x1,double y1,double z1){
        if(x0>x1||y0>y1||z0>z1)return clip.b(x0,y0,z0,x1,y1,z1);
        for(int i=0;i<6;i++){
            float[] p=clip.a[i];
            // Most visible boxes pass vanilla's first corner already.
            if((double)p[0]*x0+(double)p[1]*y0+(double)p[2]*z0+(double)p[3]>0)continue;
            // Same multiply/add order and strict > 0 boundary as vanilla.
            double distance=(double)p[0]*(p[0]>=0?x1:x0)+(double)p[1]*(p[1]>=0?y1:y0)+(double)p[2]*(p[2]>=0?z1:z0)+(double)p[3];
            if(!Double.isFinite(distance))return clip.b(x0,y0,z0,x1,y1,z1);
            if(distance<=0){
                if(!Double.isFinite(x0)||!Double.isFinite(y0)||!Double.isFinite(z0)||
                   !Double.isFinite(x1)||!Double.isFinite(y1)||!Double.isFinite(z1))
                    return clip.b(x0,y0,z0,x1,y1,z1);
                return false;
            }
        }
        return true;
    }
}
