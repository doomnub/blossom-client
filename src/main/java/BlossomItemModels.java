import java.util.*;
import org.lwjgl.util.vector.Vector3f;

/** Builds only exposed side pixels, with UVs safely inside each opaque texel. */
public final class BlossomItemModels {
    private static boolean baked;
    public static boolean enabled(){return BlossomHooks.modules.on("itemgaps");}
    public static void tick(){
        boolean enabled=enabled();
        if(baked==enabled)return;
        baked=enabled;
        ave.A().e();
    }
    public static List<bgh> sides(bmi sprite,String texture,int tint){
        int width=sprite.c(),height=sprite.d();
        List<bgh> parts=new ArrayList<bgh>();
        // Union exposed edges across animation frames; each edge is emitted once.
        boolean[][] edges=new boolean[4][width*height];
        int[] dx={0,0,-1,1},dy={-1,1,0,0};
        for(int frame=0;frame<sprite.k();frame++){
            int[] pixels=sprite.a(frame)[0];
            for(int y=0;y<height;y++)for(int x=0;x<width;x++){
                if((pixels[y*width+x]>>>24)==0)continue;
                for(int side=0;side<4;side++){
                    int nx=x+dx[side],ny=y+dy[side];
                    if(nx<0||ny<0||nx>=width||ny>=height||(pixels[ny*width+nx]>>>24)==0)
                        edges[side][y*width+x]=true;
                }
            }
        }
        float sx=16f/width,sy=16f/height;
        cq[] directions={cq.b,cq.a,cq.e,cq.f};
        for(int side=0;side<4;side++)for(int y=0;y<height;y++)for(int x=0;x<width;x++){
            if(!edges[side][y*width+x])continue;
            float x0=x*sx,x1=(x+1)*sx,y0=16-(y+1)*sy,y1=16-y*sy;
            float ex=sx*.002f,ey=sy*.002f;
            if(side==0)y0=y1;
            if(side==1)y1=y0;
            if(side==2)x1=x0;
            if(side==3)x0=x1;
            // Overlap along the edge and through the thickness, never bridge transparent spans.
            if(side<2){x0-=ex;x1+=ex;}else{y0-=ey;y1+=ey;}
            float u=(x+.5f)*sx,v=(y+.5f)*sy;
            Map<cq,bgi> face=new EnumMap<cq,bgi>(cq.class);
            face.put(directions[side],new bgi(null,tint,texture,new bgk(new float[]{u,v,u,v},0)));
            parts.add(new bgh(new Vector3f(x0,y0,7.498f),new Vector3f(x1,y1,8.502f),face,null,true));
        }
        return parts;
    }
}
