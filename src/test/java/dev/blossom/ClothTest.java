package dev.blossom;
import dev.blossom.core.cloth.*;
public final class ClothTest {
    public static void run(){
        ClothSettings s=new ClothSettings();
        for(int resolution:new int[]{4,16,64}){
            Cloth c=new Cloth(resolution);long start=System.nanoTime();
            c.capsuleCount=1;c.capsules[1]=.1;c.capsules[4]=.65;c.capsules[6]=.12;
            for(int frame=0;frame<120;frame++){c.moveFrame(.001*Math.sin(frame*.1),0,0,.002);c.advance(1./120,s);}
            for(double v:c.position)if(!Double.isFinite(v)||Math.abs(v)>2)throw new AssertionError("Unstable cloth "+resolution);
            for(int x=0;x<=c.cols;x++)if(c.position[x*3+1]!=0)throw new AssertionError("Unpinned anchor");
            c.moveFrame(100,0,0,0);if(Math.abs(c.position[c.position.length-2]-1)>1e-9)throw new AssertionError("Teleport reset");
            System.out.println("Cloth "+resolution+" subdivisions, "+c.count+" vertices: "+(System.nanoTime()-start)/1000000+" ms for 120 steps (headless CPU benchmark)");
        }
        Cloth a=new Cloth(12),b=new Cloth(12);s.wind=true;
        for(int i=0;i<120;i++)a.advance(1./120,s);for(int i=0;i<60;i++)b.advance(1./60,s);
        for(int i=0;i<a.position.length;i++)if(Math.abs(a.position[i]-b.position[i])>1e-8)throw new AssertionError("Frame-dependent physics");
        a.advance(5,s);for(double v:a.position)if(!Double.isFinite(v))throw new AssertionError("Lag handling");
        System.out.println("Cloth stability, anchors, teleport, fixed-step and lag checks passed");
    }
}
