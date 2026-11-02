import java.util.*;
public final class BlossomItemModelsTest {
    private static final class Sprite extends bmi {
        Sprite(int width,int height,int[]... frames){super("test");f=width;g=height;for(int[] pixels:frames)a.add(new int[][]{pixels});}
    }
    public static void run(){
        int[] pixels={0xffffffff,0,0xffffffff,0xffffffff};
        List<bgh> parts=BlossomItemModels.sides(new Sprite(2,2,pixels),"layer0",0);
        int fronts=0,sides=0;
        for(bgh part:parts){
            if(part.c.containsKey(cq.c)){fronts++;if(part.b.z!=8.5f||part.a.z!=7.5f)throw new AssertionError("Front/back thickness mismatch");if(part.a.y==8&&part.b.x>8)throw new AssertionError("Front covers transparent pixel");}
            else sides++;
        }
        if(fronts!=2||sides!=8)throw new AssertionError("Expected two front runs and eight exposed edges: "+fronts+"/"+sides);
        if(BlossomItemModels.sides(new Sprite(2,2,new int[4]),"layer0",0).size()!=0)throw new AssertionError("Transparent texture creates geometry");
        List<bgh> animated=BlossomItemModels.sides(new Sprite(2,1,new int[]{-1,0},new int[]{0,-1}),"layer0",0);
        if(animated.isEmpty())throw new AssertionError("Animation union lost geometry");
        int[] checker=new int[256*256];for(int y=0;y<256;y++)for(int x=0;x<256;x++)if((x+y)%2==0)checker[y*256+x]=-1;
        if(BlossomItemModels.sides(new Sprite(256,256,checker),"layer0",0)!=null)throw new AssertionError("Pathological geometry must fall back");
        System.out.println("Generated item fronts, edges, transparency, animation and geometry bounds passed");
    }
}
