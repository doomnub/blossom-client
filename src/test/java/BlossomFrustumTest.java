import java.util.Random;
/** Compare directly with Mojang's eight-corner implementation, including degenerate inputs. */
public final class BlossomFrustumTest {
    private static void compare(bid clip,double[] b){
        boolean expected=clip.b(b[0],b[1],b[2],b[3],b[4],b[5]);
        boolean actual=BlossomFrustum.test(clip,b[0],b[1],b[2],b[3],b[4],b[5]);
        if(expected!=actual)throw new AssertionError("Frustum visibility differs from vanilla");
    }
    public static void run(){
        Random random=new Random(817231);bid clip=new bid();
        for(int n=0;n<100000;n++){
            for(float[] p:clip.a)for(int i=0;i<4;i++)p[i]=(random.nextFloat()-.5f)*20;
            double scale=n%2==0?32:30000000;
            double x=(random.nextDouble()-.5)*scale,y=(random.nextDouble()-.5)*scale,z=(random.nextDouble()-.5)*scale;
            compare(clip,new double[]{x,y,z,x+random.nextDouble()*32,y+random.nextDouble()*32,z+random.nextDouble()*32});
        }
        clip.a=new float[][]{{1,0,0,1},{-1,0,0,1},{0,1,0,1},{0,-1,0,1},{0,0,1,1},{0,0,-1,1}};
        double[] values={-2,-1,Math.nextDown(-1d),Math.nextUp(-1d),-0d,0,1,Math.nextDown(1d),Math.nextUp(1d),2,Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.MAX_VALUE};
        for(double a:values)for(double b:values)compare(clip,new double[]{a,a,a,b,b,b});
        // Re-read live planes after a projection/zoom change; no stale visibility cache.
        double[] box={2,0,0,3,.5,.5};compare(clip,box);
        clip.a[1][3]=4;compare(clip,box);
        if(!BlossomFrustum.test(clip,2,0,0,3,.5,.5))throw new AssertionError("Projection change was cached");
        for(float invalid:new float[]{Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY}){
            clip.a[0][0]=invalid;compare(clip,box);
        }
        // Negative and nonfinite plane coefficients are compared on each axis.
        for(int axis=0;axis<4;axis++)for(float value:new float[]{0,-0f,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.MAX_VALUE}){
            clip.a=new float[][]{{1,0,0,1},{-1,0,0,1},{0,1,0,1},{0,-1,0,1},{0,0,1,1},{0,0,-1,1}};
            clip.a[0][axis]=value;
            for(double a:values)compare(clip,new double[]{a,-2,0,2,1,1});
        }
        boolean module=BlossomHooks.modules.get("performance").enabled,setting=BlossomHooks.settings.performance.fastFrustum;
        try{
            BlossomHooks.modules.get("performance").enabled=false;
            BlossomHooks.settings.performance.fastFrustum=true;
            if(BlossomFrustum.enabled())throw new AssertionError("Module-off ignored");
            BlossomHooks.modules.get("performance").enabled=true;
            BlossomHooks.settings.performance.fastFrustum=false;
            if(BlossomFrustum.enabled())throw new AssertionError("Setting-off ignored");
        }finally{BlossomHooks.modules.get("performance").enabled=module;BlossomHooks.settings.performance.fastFrustum=setting;}
        System.out.println("Frustum matches vanilla for 100000 randomized boxes, boundaries, malformed inputs and live plane changes");
    }
}
