import dev.blossom.core.*;
import org.lwjgl.opengl.*;
/** Optional render-thread GPU timestamps. No glFinish, polling loops or blocking query reads. */
public final class BlossomGpu {
    private static BenchmarkCapture owner;
    private static GpuTimerPool pool;
    private static GpuTimerPool.Sink sink;
    public static void prepare(BenchmarkCapture capture){
        owner=capture;sink=capture::gpu;
        try{
            ContextCapabilities caps=GLContext.getCapabilities();
            capture.gpuStatus="unsupported";
            capture.gpuCapabilities="VBO="+caps.OpenGL15+", VAO="+(caps.OpenGL30||caps.GL_ARB_vertex_array_object)+", persistent buffers="+(caps.OpenGL44||caps.GL_ARB_buffer_storage)+", indirect multidraw="+(caps.OpenGL43||caps.GL_ARB_multi_draw_indirect);
            if(!caps.OpenGL15||!(caps.OpenGL33||caps.GL_ARB_timer_query))return;
            pool=new GpuTimerPool(new GpuTimerPool.Driver(){
                public int create(){return GL15.glGenQueries();}
                public void delete(int id){GL15.glDeleteQueries(id);}
                public void stamp(int id){ARBTimerQuery.glQueryCounter(id,ARBTimerQuery.GL_TIMESTAMP);}
                public boolean ready(int id){return GL15.glGetQueryObjecti(id,GL15.GL_QUERY_RESULT_AVAILABLE)!=0;}
                public long value(int id){return ARBTimerQuery.glGetQueryObjectui64(id,GL15.GL_QUERY_RESULT);}
            },16);
            capture.gpuStatus="GPU timestamp pairs around gameRenderer; includes GPU idle/dependencies within that interval";
        }catch(RuntimeException e){fail(e);}
    }
    private static void fail(RuntimeException error){
        if(owner!=null)owner.gpuStatus="disabled after "+error.getClass().getSimpleName();
        if(pool!=null)try{pool.close();}catch(RuntimeException ignored){}pool=null;
    }
    public static void render(bfk renderer,float partial,long deadline){
        BenchmarkCapture c=BlossomBenchmark.current();
        if(c==null||c!=owner||pool==null){renderer.a(partial,deadline);return;}
        int slot=-1;try{pool.poll(sink);slot=pool.begin(c.size());}catch(RuntimeException e){fail(e);}
        try{renderer.a(partial,deadline);}finally{if(pool!=null)try{pool.end(slot);}catch(RuntimeException e){fail(e);}}
    }
    public static void finish(BenchmarkCapture capture){
        if(capture!=owner)return;
        if(pool!=null){try{pool.poll(sink);pool.close();capture.gpuSkipped=pool.skipped;capture.gpuDiscarded=pool.discarded;}catch(RuntimeException e){fail(e);}}
        pool=null;owner=null;sink=null;
    }
}
