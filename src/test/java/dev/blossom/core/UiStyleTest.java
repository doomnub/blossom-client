package dev.blossom.core;
import java.util.Properties;
public final class UiStyleTest {
    public static void run(){
        ClientSettings settings=new ClientSettings();check(settings.uiStyle==UiStyle.PIXEL);
        Properties p=new Properties();settings.uiStyle=UiStyle.MODERN;settings.save(p);
        ClientSettings restored=new ClientSettings();restored.load(p);check(restored.uiStyle==UiStyle.MODERN);
        p.setProperty("ui.style","unknown");restored.load(p);check(restored.uiStyle==UiStyle.PIXEL);
        check(UiStyle.PIXEL.next()==UiStyle.MODERN&&UiStyle.MODERN.next()==UiStyle.PIXEL);
        SmoothPanelMesh mesh=new SmoothPanelMesh();float[] original=mesh.x;
        for(int radius:new int[]{0,1,5,100}){
            mesh.set(10,20,110,60,radius,.65f);
            for(int i=0;i<SmoothPanelMesh.COUNT;i++){
                check(mesh.x[i]>=9.999&&mesh.x[i]<=110.001&&mesh.y[i]>=19.999&&mesh.y[i]<=60.001);
                float dx=mesh.outerX[i]-mesh.x[i],dy=mesh.outerY[i]-mesh.y[i];check(Math.abs(dx*dx+dy*dy-.65f*.65f)<.001);
            }
            check(mesh.x==original);
        }
        System.out.println("UI style defaults, persistence, fallback and bounded smooth contour passed");
    }
    private static void check(boolean b){if(!b)throw new AssertionError("UI style invariant");}
}
