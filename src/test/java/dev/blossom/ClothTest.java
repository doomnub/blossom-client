package dev.blossom;
import dev.blossom.core.cloth.*;
public final class ClothTest {
    public static void run()throws Exception{
        ClothSettings s=new ClothSettings();
        for(int resolution:new int[]{4,16,64}){
            Cloth c=new Cloth(resolution);long start=System.nanoTime();
            c.capsuleCount=1;c.capsules[1]=.1;c.capsules[4]=.65;c.capsules[6]=.12;
            for(int frame=0;frame<120;frame++){c.moveFrame(.001*Math.sin(frame*.1),0,0,.002);c.advance(1./120,s);}
            for(double v:c.position)if(!Double.isFinite(v)||Math.abs(v)>2)throw new AssertionError("Unstable cloth "+resolution);
            for(int x=0;x<=c.cols;x++)if(c.position[x*3+1]!=0)throw new AssertionError("Unpinned anchor");
            c.prepareRender();
            for(int i=0;i<c.renderNormal.length;i+=3){double n=0;for(int k=0;k<3;k++)n+=c.renderNormal[i+k]*c.renderNormal[i+k];if(!Double.isFinite(n)||Math.abs(n-1)>1e-8)throw new AssertionError("Invalid folded-cloth render normal");}
            c.moveFrame(100,0,0,0);if(Math.abs(c.position[c.position.length-2]-1)>1e-9)throw new AssertionError("Teleport reset");
            System.out.println("Cloth "+resolution+" subdivisions, "+c.count+" vertices: "+(System.nanoTime()-start)/1000000+" ms for 120 steps (headless CPU benchmark)");
        }
        {Cloth collision=new Cloth(16);int moving=collision.count-1,i=moving*3,triangle=2*(collision.cols+1)+1,j=triangle*3;
        collision.position[i]=collision.position[j]-.01;collision.position[i+1]=collision.position[j+1]+.01;collision.position[i+2]=Cloth.ANCHOR_Z+.002;
        collision.previous[i]=collision.position[i];collision.previous[i+1]=collision.position[i+1];collision.previous[i+2]=Cloth.ANCHOR_Z+.015;
        java.lang.reflect.Method self=Cloth.class.getDeclaredMethod("selfCollide");self.setAccessible(true);self.invoke(collision);
        if(collision.position[i+2]<=Cloth.ANCHOR_Z+.002)throw new AssertionError("Self collision failed to separate a non-neighbor vertex from a triangle");
        collision.capsuleCount=1;collision.capsules[1]=.3;collision.capsules[4]=.7;collision.capsules[6]=.2;collision.position[i]=.001;collision.position[i+1]=.5;collision.position[i+2]=0;
        java.lang.reflect.Method body=Cloth.class.getDeclaredMethod("body");body.setAccessible(true);body.invoke(collision);
        if(collision.position[i]<.207)throw new AssertionError("Body capsule failed to separate cloth");
        }
        for(int resolution:new int[]{4,16,64}){
            Cloth fast=new Cloth(resolution);ClothSettings cfg=new ClothSettings();cfg.selfCollision=false;cfg.playerCollision=false;
            for(int frame=0;frame<90;frame++){fast.moveFrame(.08*Math.sin(frame*.1),-.025,.08,.015);fast.advance(1./60,cfg);if(fast.maxAttachmentStretch()>1.005001)throw new AssertionError("Fast movement stretches cape "+resolution);}
        }
        {Cloth jacket=new Cloth(16);jacket.torsoCollision=true;jacket.torso[1]=.375;jacket.torso[3]=.265625;jacket.torso[4]=.390625;jacket.torso[5]=.140625;
        int i=(jacket.cols+2)*3;jacket.position[i]=.25;jacket.position[i+1]=.5;jacket.position[i+2]=.14;
        java.lang.reflect.Method body=Cloth.class.getDeclaredMethod("body");body.setAccessible(true);body.invoke(jacket);
        if(jacket.position[i+2]<.140625+Cloth.RENDER_HALF_THICKNESS+Cloth.THICKNESS-1e-8)throw new AssertionError("Jacket corner lacks thickness clearance");
        }
        Cloth a=new Cloth(12),b=new Cloth(12);s.wind=true;
        for(int i=0;i<120;i++)a.advance(1./120,s);for(int i=0;i<60;i++)b.advance(1./60,s);
        for(int i=0;i<a.position.length;i++)if(Math.abs(a.position[i]-b.position[i])>1e-8)throw new AssertionError("Frame-dependent physics");
        a.advance(5,s);for(double v:a.position)if(!Double.isFinite(v))throw new AssertionError("Lag handling");
        System.out.println("Cloth stability, anchors, teleport, fixed-step and lag checks passed");
    }
}
