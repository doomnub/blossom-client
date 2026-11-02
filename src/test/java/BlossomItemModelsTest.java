import java.util.*;
public final class BlossomItemModelsTest {
    private static final class Sprite extends bmi {
        Sprite(int width,int height,int[]... frames){super("test");f=width;g=height;for(int[] pixels:frames)a.add(new int[][]{pixels});}
    }
    private static void near(float a,float b){if(Math.abs(a-b)>.00001f)throw new AssertionError(a+" != "+b);}
    public static void run(){
        Sprite sprite=new Sprite(2,2,new int[]{-1,0,-1,-1});
        List<bgh> parts=BlossomItemModels.sides(sprite,"layer0",0);
        int fronts=0,sides=0;
        for(bgh part:parts){
            if(part.c.containsKey(cq.c)){fronts++;near(part.a.x,0);near(part.b.x,16);near(part.a.z,7.5f);near(part.b.z,8.5f);}
            else {sides++;if(part.a.z>=7.5f||part.b.z<=8.5f)throw new AssertionError("Side must overlap front/back planes");}
        }
        if(fronts!=1||sides!=6)throw new AssertionError("Expected one front and six contiguous edges: "+fronts+"/"+sides);
        List<bgh> square=BlossomItemModels.sides(new Sprite(1,1,new int[]{-1}),"layer0",0);
        for(bgh part:square){
            if(part.c.containsKey(cq.b))near(part.a.y,16-.007f);
            if(part.c.containsKey(cq.a))near(part.a.y,.007f);
            if(part.c.containsKey(cq.e))near(part.a.x,.007f);
            if(part.c.containsKey(cq.f))near(part.a.x,16-.007f);
        }
        // Real atlas interpolation must land on the original sprite boundaries.
        sprite.a(256,256,32,48,false);
        near(sprite.a(BlossomItemModels.uv(0,2)),32f/256);
        near(sprite.a(BlossomItemModels.uv(2,2)),34f/256);
        near(sprite.b(BlossomItemModels.uv(0,2)),48f/256);
        List<bgh> split=BlossomItemModels.sides(new Sprite(3,1,new int[]{-1,0,-1}),"layer0",0);
        int tops=0;for(bgh part:split)if(part.c.containsKey(cq.b)){tops++;if(part.b.x-part.a.x>6)throw new AssertionError("Edge bridged transparent gap");}
        if(tops!=2)throw new AssertionError("Disconnected edges were merged");
        if(!BlossomItemModels.sides(new Sprite(2,2,new int[4]),"layer0",0).isEmpty())throw new AssertionError("Transparent texture creates geometry");
        if(BlossomItemModels.sides(new Sprite(2,1,new int[]{-1,0},new int[]{0,-1}),"layer0",0).isEmpty())throw new AssertionError("Animation union lost geometry");
        int[] checker=new int[256*256];for(int y=0;y<256;y++)for(int x=0;x<256;x++)if((x+y)%2==0)checker[y*256+x]=-1;
        if(BlossomItemModels.sides(new Sprite(256,256,checker),"layer0",0)!=null)throw new AssertionError("Pathological geometry must fall back");
        System.out.println("Item side recess/overlap, contiguous spans, atlas UV correction and geometry bounds passed");
    }
}
