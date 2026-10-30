package dev.blossom;
import dev.blossom.core.QuadSort;
import java.nio.*;
import java.util.*;
/** Compare every vertex attribute against vanilla's stable boxed distance ordering. */
public final class QuadSortTest {
    public static void run(){
        Random random=new Random(731);
        for(int trial=0;trial<80;trial++){
            int count=trial<5?trial:random.nextInt(400),stride=trial%2==0?7:8,words=stride*4;
            ByteBuffer bytes=ByteBuffer.allocate(count*words*4).order(ByteOrder.nativeOrder());IntBuffer data=bytes.asIntBuffer();FloatBuffer positions=bytes.asFloatBuffer();
            int[] original=new int[count*words];float[] distances=new float[count];Integer[] expected=new Integer[count];
            float x=trial%3,y=-7,z=2;
            for(int i=0;i<count;i++){
                float px=random.nextInt(12),py=random.nextInt(12),pz=random.nextInt(12);
                if(trial==7&&i==0)px=Float.NaN;
                for(int v=0;v<4;v++){int p=i*words+v*stride;data.put(p,Float.floatToRawIntBits(px));data.put(p+1,Float.floatToRawIntBits(py));data.put(p+2,Float.floatToRawIntBits(pz));for(int k=3;k<stride;k++)data.put(p+k,random.nextInt());}
                float dx=(px+px+px+px)*.25f-x,dy=(py+py+py+py)*.25f-y,dz=(pz+pz+pz+pz)*.25f-z;
                distances[i]=dx*dx+dy*dy+dz*dz;expected[i]=i;
            }
            data.get(original);data.position(0);Arrays.sort(expected,(a,b)->Float.compare(distances[b],distances[a]));
            QuadSort.sort(positions,data,count*4,stride,x,y,z);
            if(data.position()!=0||data.limit()!=original.length)throw new AssertionError("Buffer state changed");
            for(int i=0;i<count;i++)for(int k=0;k<words;k++)if(data.get(i*words+k)!=original[expected[i]*words+k])throw new AssertionError("Quad ordering/attribute mismatch at "+trial);
            // A second sort must preserve stable ties and reuse scratch safely.
            int[] sorted=new int[original.length];data.get(sorted);data.position(0);QuadSort.sort(positions,data,count*4,stride,x,y,z);
            for(int i=0;i<sorted.length;i++)if(data.get(i)!=sorted[i])throw new AssertionError("Repeat sort changed order");
        }
        System.out.println("Primitive quad sort matches stable reference across formats, ties, NaN and reuse");
    }
}
