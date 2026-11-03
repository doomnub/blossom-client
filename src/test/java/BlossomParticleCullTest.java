import java.util.Random;
public final class BlossomParticleCullTest {
    private static final class Camera extends bic {
        double x0,y0,z0,x1,y1,z1;int calls;
        Camera(){super(new bid());}
        public boolean b(double a,double b,double c,double d,double e,double f){x0=a;y0=b;z0=c;x1=d;y1=e;z1=f;calls++;return false;}
    }
    public static void run(){
        Camera camera=new Camera();Random r=new Random(9128);
        for(int i=0;i<10000;i++){
            double x=r.nextDouble()*1000,y=r.nextDouble()*1000,z=r.nextDouble()*1000;
            float scale=r.nextFloat()*20-10,rx=r.nextFloat()*2-1,ry=r.nextFloat()*2-1,rz=r.nextFloat()*2-1,ryz=r.nextFloat()*2-1,rxy=r.nextFloat()*2-1;
            if(BlossomVisibility.quad(camera,x,y,z,scale,rx,ry,rz,ryz,rxy))throw new AssertionError("Rejected box still drawn");
            // Same signed corner combinations used by the vanilla billboard.
            for(int a:new int[]{-1,1})for(int b:new int[]{-1,1}){
                double s=.1*scale,vx=x+a*rx*s+b*ryz*s,vy=y+b*ry*s,vz=z+a*rz*s+b*rxy*s;
                if(vx<camera.x0||vx>camera.x1||vy<camera.y0||vy>camera.y1||vz<camera.z0||vz>camera.z1)throw new AssertionError("Billboard corner escaped bounds");
            }
        }
        int calls=camera.calls;
        if(!BlossomVisibility.quad(camera,Double.NaN,0,0,1,1,1,1,1,1)||camera.calls!=calls)throw new AssertionError("Nonfinite input should draw conservatively");
        BlossomVisibility.reset();
        if(!BlossomVisibility.particle(null,1,0,1,1,1,1,1))throw new AssertionError("Stale camera used after reset");
        System.out.println("Particle culling contains all billboard corners, handles nonfinite input and resets camera state");
    }
}
