import dev.blossom.core.*;
import java.util.*;
import java.nio.FloatBuffer;
public final class BlossomVisuals {
    private static final Map<pr,Hit> hits=new WeakHashMap<pr,Hit>();
    private static final class Hit{int last;long start;}
    private static int forcedCategory=-1;
    private static final int[] NO_ARGS=new int[0];
    private static int category(int id){return forcedCategory>=0?forcedCategory:VisualSettings.category(id);}
    private static float hit(pr e){
        VisualSettings s=BlossomHooks.settings.visual;
        boolean tint=BlossomHooks.modules.on("hitcolor")&&(e instanceof bet?s.hitPlayers:s.hitMobs);
        if(!tint&&!BlossomHooks.modules.on("particles"))return -1;
        Hit h=hits.get(e);if(h==null){h=new Hit();hits.put(e,h);}
        long now=System.nanoTime();if(e.au>h.last){h.start=now;
            if(BlossomHooks.modules.on("particles")&&s.enabled[2]&&ave.A().j!=null){forcedCategory=2;try{for(int i=0;i<2;i++)ave.A().j.a(29,e.s+(i==0?-.08:.08),e.t+.8,e.u,0,.015,0,NO_ARGS);}finally{forcedCategory=-1;}}
        }h.last=e.au;
        if(!tint)return -1;
        float elapsed=(now-h.start)/50000000f;return h.start==0||elapsed>=s.hitDuration?0:s.hitFade?1-elapsed/s.hitDuration:1;
    }
    public static int hurt(int vanilla,pr e){float f=hit(e);return f<0?vanilla:f>0?1:0;}
    public static FloatBuffer tint(FloatBuffer buffer,pr e,boolean combine){
        float f=hit(e);VisualSettings s=BlossomHooks.settings.visual;if(f<=0||(!combine&&!s.hitArmor))return buffer;
        int c=s.hitColor;buffer.put(0,((c>>16)&255)/255f);buffer.put(1,((c>>8)&255)/255f);buffer.put(2,(c&255)/255f);buffer.put(3,s.hitOpacity*f);return buffer;
    }
    private static int particleId=-1;private static boolean extra;
    private static final Random random=new Random();
    private static java.lang.reflect.Field size,age;
    public static boolean begin(int id){particleId=id;if(!BlossomHooks.modules.on("particles"))return true;VisualSettings s=BlossomHooks.settings.visual;int c=category(id);boolean allowed=s.enabled[c]&&(extra||random.nextFloat()<s.density[c]);if(!allowed)particleId=-1;return allowed;}
    public static void added(beb p){
        if(p==null||!BlossomHooks.modules.on("particles"))return;
        try{
            VisualSettings s=BlossomHooks.settings.visual;int c=p instanceof beo?4:category(particleId);
            if(size==null){size=beb.class.getDeclaredField("h");size.setAccessible(true);age=beb.class.getDeclaredField("g");age.setAccessible(true);}
            size.setFloat(p,size.getFloat(p)*s.scale[c]);age.setInt(p,Math.max(1,Math.min(1200,Math.round(age.getInt(p)*s.lifetime[c]))));
            int color=s.color[c];if(color!=0xffffffff)p.b(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f);
            if(!s.enabled[c]||(particleId<0&&random.nextFloat()>s.density[c])){p.I=true;return;}p.i(s.opacity[c]);
        }catch(ReflectiveOperationException e){BlossomHooks.modules.get("particles").enabled=false;BlossomHooks.message="Particle mapping unavailable";}
    }
    public static beb finish(beb p,bec renderer,int id,double x,double y,double z,double vx,double vy,double vz,int[] args){
        particleId=-1;if(p==null||extra||!BlossomHooks.modules.on("particles"))return p;
        float amount=BlossomHooks.settings.visual.density[category(id)]-1;
        int n=(int)amount;if(random.nextFloat()<amount-n)n++;extra=true;
        try{for(int i=0;i<Math.min(3,n);i++)renderer.a(id,x,y,z,vx,vy,vz,args);}finally{extra=false;particleId=-1;}return p;
    }
}
