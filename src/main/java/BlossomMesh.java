import java.nio.*;
import dev.blossom.core.QuadSort;
/** Called only for vanilla's position-first quad format. Worker buffers stay thread-confined. */
public final class BlossomMesh {
    public static boolean sort(FloatBuffer positions,IntBuffer data,int vertices,int stride,float x,float y,float z){
        if(!BlossomPerformance.enabled()||!BlossomHooks.settings.performance.primitiveSort)return false;
        if(vertices<0||vertices%4!=0||stride<3||(long)vertices*stride>data.capacity()||(long)vertices*stride>positions.limit())return false;
        QuadSort.sort(positions,data,vertices,stride,x,y,z);return true;
    }
}
