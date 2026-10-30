package dev.blossom.core;
import java.nio.*;
/** Stable back-to-front quad sorting with bounded per-thread reusable scratch storage. */
public final class QuadSort {
    private static final int RETAIN_QUADS=65536;
    private static final ThreadLocal<Scratch> LOCAL=ThreadLocal.withInitial(Scratch::new);
    private static final class Scratch {
        float[] distance=new float[0];int[] order=new int[0],merge=new int[0],quad=new int[0];
        void ensure(int count,int words){if(order.length<count){int capacity=Math.max(count,Math.min(RETAIN_QUADS,Math.max(16,order.length*2)));distance=new float[capacity];order=new int[capacity];merge=new int[capacity];}if(quad.length<words)quad=new int[words];}
    }
    public static void sort(FloatBuffer positions,IntBuffer data,int vertices,int stride,float x,float y,float z){
        if(vertices<0||vertices%4!=0||stride<3||((long)vertices*stride)>data.capacity()||((long)vertices*stride)>positions.limit())throw new IllegalArgumentException("Invalid quad buffer");
        int count=vertices/4,words=stride*4;if(count<2)return;
        Scratch s=count<=RETAIN_QUADS?LOCAL.get():new Scratch();s.ensure(count,words);
        for(int i=0;i<count;i++){
            int p=i*words;
            float dx=(positions.get(p)+positions.get(p+stride)+positions.get(p+2*stride)+positions.get(p+3*stride))*.25f-x;
            float dy=(positions.get(p+1)+positions.get(p+stride+1)+positions.get(p+2*stride+1)+positions.get(p+3*stride+1))*.25f-y;
            float dz=(positions.get(p+2)+positions.get(p+stride+2)+positions.get(p+2*stride+2)+positions.get(p+3*stride+2))*.25f-z;
            s.distance[i]=dx*dx+dy*dy+dz*dz;s.order[i]=i;
        }
        int[] order=s.order,merge=s.merge;
        for(int width=1;width<count;width*=2){
            for(int start=0;start<count;start+=width*2){
                int middle=Math.min(start+width,count),end=Math.min(start+2*width,count),a=start,b=middle;
                for(int out=start;out<end;out++)merge[out]=b==end||(a<middle&&Float.compare(s.distance[order[a]],s.distance[order[b]])>=0)?order[a++]:order[b++];
            }
            int[] swap=order;order=merge;merge=swap;
        }
        int limit=data.limit(),position=data.position();data.limit(data.capacity());
        try{
            for(int start=0;start<count;start++){
                if(order[start]<0)continue;
                if(order[start]==start){order[start]=-1;continue;}
                for(int k=0;k<words;k++)s.quad[k]=data.get(start*words+k);
                int destination=start;
                while(true){int source=order[destination];order[destination]=-1;
                    if(source==start){for(int k=0;k<words;k++)data.put(destination*words+k,s.quad[k]);break;}
                    for(int k=0;k<words;k++)data.put(destination*words+k,data.get(source*words+k));destination=source;
                }
            }
        }finally{data.limit(limit);data.position(position);}
    }
}
