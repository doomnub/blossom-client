import java.util.*;
import org.lwjgl.util.vector.Vector3f;

/** Generated sprites only: contiguous edge runs, recessed side walls and corrected UVs. */
public final class BlossomItemModels {
    private static boolean baked;
    private static final int MAX_PARTS=8192;
    public static boolean enabled(){return BlossomHooks.modules.on("itemgaps");}
    public static void tick(){
        boolean enabled=enabled();
        if(baked==enabled)return;
        baked=enabled;
        ave.A().e();
    }
    // 1.8.9's atlas interpolation removes .01 pixels from both ends of a sprite.
    // Undo that for these generated faces only, without touching block/pack models.
    static float uv(float pixel,int size){return (pixel-.01f)*16f/(size-.02f);}
    public static List<bgh> sides(bmi sprite,String texture,int tint){
        int width=sprite.c(),height=sprite.d();
        if(width<=0||height<=0||(long)width*height>1048576)return null;
        boolean[][] edges=new boolean[4][width*height];
        int[] dx={0,0,-1,1},dy={-1,1,0,0};
        boolean any=false;
        for(int frame=0;frame<sprite.k();frame++){
            int[][] levels=sprite.a(frame);
            if(levels==null||levels.length==0||levels[0]==null||levels[0].length<width*height)return null;
            int[] pixels=levels[0];
            for(int y=0;y<height;y++)for(int x=0;x<width;x++){
                if((pixels[y*width+x]>>>24)==0)continue;
                any=true;
                for(int side=0;side<4;side++){
                    int nx=x+dx[side],ny=y+dy[side];
                    if(nx<0||ny<0||nx>=width||ny>=height||(pixels[ny*width+nx]>>>24)==0)
                        edges[side][y*width+x]=true;
                }
            }
        }
        List<bgh> parts=new ArrayList<bgh>();
        if(!any)return parts;
        // Keep one front/back rectangle. Its texture supplies the alpha silhouette;
        // subdividing it into rows introduced unnecessary seams and extra geometry.
        Map<cq,bgi> front=new EnumMap<cq,bgi>(cq.class);
        front.put(cq.d,face(texture,tint,uv(0,width),uv(0,height),uv(width,width),uv(height,height)));
        front.put(cq.c,face(texture,tint,uv(width,width),uv(0,height),uv(0,width),uv(height,height)));
        parts.add(new bgh(new Vector3f(0,0,7.5f),new Vector3f(16,16,8.5f),front,null,true));
        float sx=16f/width,sy=16f/height;
        // Subpixel overlap/recess, capped for high-resolution textures so holes survive.
        float recess=Math.min(.007f,Math.min(sx,sy)*.1f);
        float expand=Math.min(.008f,Math.min(sx,sy)*.1f);
        cq[] directions={cq.b,cq.a,cq.e,cq.f};
        for(int side=0;side<4;side++){
            boolean horizontal=side<2;
            int rows=horizontal?height:width,columns=horizontal?width:height;
            for(int row=0;row<rows;row++)for(int start=0;start<columns;start++){
                int index=horizontal?row*width+start:start*width+row;
                if(!edges[side][index])continue;
                int end=start+1;
                while(end<columns&&edges[side][horizontal?row*width+end:end*width+row])end++;
                if(parts.size()>=MAX_PARTS)return null;
                float x0,x1,y0,y1,u0,u1,v0,v1;
                if(horizontal){
                    x0=start*sx-expand;x1=end*sx+expand;
                    y0=y1=16-(row+(side==1?1:0))*sy+(side==0?-recess:recess);
                    u0=uv(start+.01f,width);u1=uv(end-.01f,width);
                    v0=v1=uv(row+.5f,height);
                }else{
                    x0=x1=(row+(side==3?1:0))*sx+(side==2?recess:-recess);
                    y0=16-end*sy-expand;y1=16-start*sy+expand;
                    u0=u1=uv(row+.5f,width);
                    v0=uv(start+.01f,height);v1=uv(end-.01f,height);
                }
                Map<cq,bgi> wall=new EnumMap<cq,bgi>(cq.class);
                wall.put(directions[side],face(texture,tint,u0,v0,u1,v1));
                parts.add(new bgh(new Vector3f(x0,y0,7.5f-expand),new Vector3f(x1,y1,8.5f+expand),wall,null,true));
                start=end-1;
            }
        }
        return parts;
    }
    private static bgi face(String texture,int tint,float u0,float v0,float u1,float v1){
        return new bgi(null,tint,texture,new bgk(new float[]{u0,v0,u1,v1},0));
    }
}
