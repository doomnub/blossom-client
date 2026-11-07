import dev.blossom.core.SkyLayer;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.*;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.lwjgl.opengl.GL11;
/** Bounded, pack-driven sky layers. CPU decoding is isolated; all GL ownership stays on the client thread. */
public final class BlossomSky {
    private static final int MAX_LAYERS=128,MAX_PIXELS=32*1024*1024;
    private static final ThreadPoolExecutor worker=new ThreadPoolExecutor(1,1,10,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(1),r->{Thread t=new Thread(r,"Blossom sky decoder");t.setDaemon(true);return t;});
    static {worker.allowCoreThreadTimeOut(true);}
    private static Future<Pack> pending;
    private static Pack pack;
    private static Object world;
    private static volatile boolean dirty=true;
    private static boolean celestial;
    private static long lastFrame;
    private static final Map<String,Integer> textures=new HashMap<String,Integer>();
    private static final class Layer {
        final SkyLayer rule;float location=Float.NaN;
        Layer(SkyLayer rule){this.rule=rule;}
    }
    private static final class Pack {
        final List<Layer> layers=new ArrayList<Layer>();
        final Map<String,Layer> objects=new HashMap<String,Layer>();
        final Map<String,BufferedImage> images=new LinkedHashMap<String,BufferedImage>();
        long pixels;
    }
    public static void reload(){dirty=true;}
    private static void clear(){
        if(pending!=null){pending.cancel(true);pending=null;}worker.getQueue().clear();
        for(int id:textures.values())bml.a(id);
        textures.clear();pack=null;lastFrame=0;
    }
    public static void tick(){
        ave mc=ave.A();boolean enabled=BlossomHooks.modules.on("customskies")&&mc.f!=null;
        if(!enabled){if(pack!=null||pending!=null||!textures.isEmpty())clear();world=null;dirty=true;return;}
        if(dirty||world!=mc.f){
            clear();dirty=false;world=mc.f;
            final bni resources=mc.Q();final int dimension=mc.f.t.q();
            pending=worker.submit(()->load(resources,dimension));
        }
        if(pending!=null&&pending.isDone()){
            try{pack=pending.get();}catch(Exception e){pack=null;System.err.println("[Blossom] Custom sky load stopped");}finally{pending=null;}
        }
        // One texture per tick avoids an unbounded upload burst. CPU pixels are discarded immediately.
        if(pack!=null&&!pack.images.isEmpty()){
            Iterator<Map.Entry<String,BufferedImage>> it=pack.images.entrySet().iterator();Map.Entry<String,BufferedImage> entry=it.next();
            int id=bml.a();
            try{int max=GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);if(entry.getValue().getWidth()>max||entry.getValue().getHeight()>max)throw new IllegalArgumentException("GPU texture limit");bml.a(id,entry.getValue(),true,true);textures.put(entry.getKey(),id);}catch(RuntimeException e){bml.a(id);System.err.println("[Blossom] Sky texture upload failed");}
            finally{entry.getValue().flush();it.remove();}
        }
    }
    private static Pack load(bni resources,int dimension){
        Pack result=new Pack();
        for(int index=-1;index<MAX_LAYERS&&!Thread.currentThread().isInterrupted();index++){
            String name="sky"+(index<0?"":index);Layer layer=loadLayer(resources,result,dimension,name,true);
            if(layer!=null)result.layers.add(layer);
            // Probe a bounded set, allowing malformed or missing layers without hiding later valid ones.
        }
        for(String name:new String[]{"sun","moon_phases"}){
            if(Thread.currentThread().isInterrupted())break;
            Layer layer=loadLayer(resources,result,dimension,name,false);if(layer!=null)result.objects.put("minecraft:textures/environment/"+name+".png",layer);
        }
        return result;
    }
    private static Layer loadLayer(bni resources,Pack result,int dim,String name,boolean cube){
        for(String root:new String[]{"optifine","mcpatcher"}){
            String file="minecraft:"+root+"/sky/world"+dim+"/"+name+".properties";
            Properties props=new Properties();
            try(InputStream stream=open(resources,new jy(file))){
                props.load(new ByteArrayInputStream(readLimited(stream,65536)));
            }catch(FileNotFoundException e){continue;}catch(IOException e){continue;}
            try{
                if(!cube)props.setProperty("fade","false");
                SkyLayer rule=new SkyLayer(props,file);
                if(!result.images.containsKey(rule.source)){
                    BufferedImage image=decode(resources,rule.source,MAX_PIXELS-result.pixels,cube);
                    result.pixels+=(long)image.getWidth()*image.getHeight();result.images.put(rule.source,image);
                }
                return new Layer(rule);
            }catch(IOException|RuntimeException e){System.err.println("[Blossom] Skipped invalid or oversized custom sky layer: "+name);return null;}
        }
        return null;
    }
    private static InputStream open(bni resources,jy path)throws IOException{
        bnh resource=resources.a(path);InputStream stream=resource.b();
        try{
            // 1.8.9 SimpleResource owns a separate metadata stream. Its metadata reader
            // closes that stream; closing only the PNG/properties stream would leave it open.
            if(resource.c())resource.a("texture");
            return stream;
        }catch(RuntimeException e){stream.close();throw new IOException("Invalid sky resource metadata",e);}
    }
    private static byte[] readLimited(InputStream stream,int limit)throws IOException{
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
        while((n=stream.read(buffer))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();if(out.size()+n>limit)throw new IOException("Sky resource limit");out.write(buffer,0,n);}return out.toByteArray();
    }
    private static BufferedImage decode(bni resources,String source,long remaining,boolean cube)throws IOException{
        try(InputStream raw=open(resources,new jy(source));MemoryCacheImageInputStream input=new MemoryCacheImageInputStream(new ByteArrayInputStream(readLimited(raw,32*1024*1024)))){
            Iterator<ImageReader> readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw new IOException("Invalid sky image");
            ImageReader reader=readers.next();
            try{reader.setInput(input,true,true);int w=reader.getWidth(0),h=reader.getHeight(0);
                if(w<1||h<1||w>8192||h>8192||(long)w*h>remaining||(cube&&(w%3!=0||h%2!=0||w/3!=h/2)))throw new IOException("Sky image dimensions/budget");
                return reader.read(0);
            }finally{reader.dispose();}
        }
    }
    public static void render(float partial){
        ave mc=ave.A();if(!BlossomHooks.modules.on("customskies")||pack==null||mc.f!=world||pack.layers.isEmpty())return;
        long now=System.nanoTime();float dt=lastFrame==0?0:Math.min(1,(now-lastFrame)/1e9f);lastFrame=now;
        pk camera=mc.ac();if(camera==null)return;
        ady biome=mc.f.b(new cj(camera));long time=BlossomView.skyTime(mc.f.L());
        float fraction=BlossomHooks.modules.on("timechanger")?0:partial;
        float wet=mc.f.j(partial),storm=mc.f.h(partial);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_TEXTURE_BIT|GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        try{
            GL11.glDisable(GL11.GL_FOG);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(false);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glRotatef(-90,0,1,0);
            for(Layer layer:pack.layers){
                SkyLayer rule=layer.rule;Integer texture=textures.get(rule.source);if(texture==null)continue;
                float target=rule.location((int)Math.floor(camera.t),biome.ah,biome.az)?1:0;
                if(Float.isNaN(layer.location)||rule.transition==0)layer.location=target;
                else layer.location+=Math.max(-dt/rule.transition,Math.min(dt/rule.transition,target-layer.location));
                float alpha=layer.location*rule.brightness(time,fraction,wet,storm);if(alpha<=0)continue;
                blend(rule.blend,alpha);GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
                GL11.glPushMatrix();
                try{
                    if(rule.rotate){double cycles=Math.floorDiv(time,24000L)*rule.speed+mc.f.c(partial)*rule.speed;GL11.glRotatef((float)((cycles%1)*360),rule.axisZ,rule.axisY,-rule.axisX);}
                    cube();
                }finally{GL11.glPopMatrix();}
            }
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();}
    }
    // Atlas faces: bottom, top, south, east, north, west. Vertices are in the legacy sky coordinate frame.
    private static final float[][][] FACES={
        {{-1,-1,1},{1,-1,1},{1,-1,-1},{-1,-1,-1}},
        {{1,1,1},{-1,1,1},{-1,1,-1},{1,1,-1}},
        {{1,1,-1},{1,-1,-1},{1,-1,1},{1,1,1}},
        {{1,1,1},{1,-1,1},{-1,-1,1},{-1,1,1}},
        {{-1,1,1},{-1,-1,1},{-1,-1,-1},{-1,1,-1}},
        {{-1,1,-1},{-1,-1,-1},{1,-1,-1},{1,1,-1}}
    };
    private static void cube(){
        GL11.glBegin(GL11.GL_QUADS);
        for(int face=0;face<6;face++){double u=(face%3)/3.0,v=(face/3)/2.0;
            for(int corner=0;corner<4;corner++){
                GL11.glTexCoord2d(u+(corner>=2?1.0/3:0),v+(corner==1||corner==2?.5:0));
                float[] p=FACES[face][corner];GL11.glVertex3f(p[0]*100,p[1]*100,p[2]*100);
            }
        }
        GL11.glEnd();
    }
    private static void blend(String mode,float alpha){
        GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glEnable(GL11.GL_BLEND);
        int src=GL11.GL_SRC_ALPHA,dst=GL11.GL_ONE;float rgb=1,a=alpha;
        switch(mode){
            case "alpha":dst=GL11.GL_ONE_MINUS_SRC_ALPHA;break;
            case "subtract":src=GL11.GL_ONE_MINUS_DST_COLOR;dst=GL11.GL_ZERO;rgb=alpha;a=1;break;
            case "multiply":src=GL11.GL_DST_COLOR;dst=GL11.GL_ONE_MINUS_SRC_ALPHA;rgb=alpha;break;
            case "dodge":src=GL11.GL_ONE;rgb=alpha;a=1;break;
            case "burn":src=GL11.GL_ZERO;dst=GL11.GL_ONE_MINUS_SRC_COLOR;rgb=alpha;a=1;break;
            case "screen":src=GL11.GL_ONE;dst=GL11.GL_ONE_MINUS_SRC_COLOR;rgb=alpha;a=1;break;
            case "overlay":src=GL11.GL_DST_COLOR;dst=GL11.GL_SRC_COLOR;rgb=alpha;a=1;break;
            case "replace":GL11.glDisable(GL11.GL_BLEND);GL11.glEnable(GL11.GL_ALPHA_TEST);a=1;break;
        }
        GL11.glBlendFunc(src,dst);GL11.glColor4f(rgb,rgb,rgb,a);
    }
    public static void bind(bmj manager,jy resource){
        afterCelestial();Layer layer=pack==null?null:pack.objects.get(resource.toString());
        Integer id=layer==null?null:textures.get(layer.rule.source);
        if(!BlossomHooks.modules.on("customskies")||ave.A().f!=world||id==null){manager.a(resource);return;}
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_TEXTURE_BIT|GL11.GL_CURRENT_BIT);celestial=true;
        blend(layer.rule.blend,1-ave.A().f.j(1));GL11.glBindTexture(GL11.GL_TEXTURE_2D,id);
    }
    public static void afterCelestial(){if(celestial){GL11.glPopAttrib();celestial=false;}}
}
