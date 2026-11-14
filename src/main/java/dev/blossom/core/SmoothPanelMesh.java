package dev.blossom.core;
/** Fixed-size, reusable rounded contour; no per-panel allocations or GPU resources. */
public final class SmoothPanelMesh {
    public static final int STEPS=12, COUNT=4*(STEPS+1);
    private static final float[] COS=new float[COUNT],SIN=new float[COUNT];
    public final float[] x=new float[COUNT],y=new float[COUNT],outerX=new float[COUNT],outerY=new float[COUNT];
    static{for(int i=0;i<COUNT;i++){double a=(-180+(i/(STEPS+1))*90+(i%(STEPS+1))*90.0/STEPS)*Math.PI/180;COS[i]=(float)Math.cos(a);SIN[i]=(float)Math.sin(a);}}
    public void set(float left,float top,float right,float bottom,float radius,float fringe){
        float r=Math.max(0,Math.min(radius,Math.min(right-left,bottom-top)/2));
        for(int i=0;i<COUNT;i++){
            int corner=i/(STEPS+1);
            float cx=corner==0||corner==3?left+r:right-r,cy=corner<2?top+r:bottom-r;
            x[i]=cx+COS[i]*r;y[i]=cy+SIN[i]*r;
            outerX[i]=cx+COS[i]*(r+fringe);outerY[i]=cy+SIN[i]*(r+fringe);
        }
    }
}
