import dev.blossom.core.cloth.*;
import java.util.*;
import org.lwjgl.opengl.GL11;
/** Render-thread-only cloth instances. The vanilla layer supplies the body transform and lighting. */
public final class BlossomCloth {
    private static final Map<bet,State> states=new WeakHashMap<bet,State>();
    private static jy white;
    private static final class State {Cloth mesh;java.lang.ref.WeakReference<Object> world;long last;double x,y,z,yaw,anchorY,anchorZ;State(int n){mesh=new Cloth(n);}}
    public static void clear(){if(!states.isEmpty())states.clear();}
    public static boolean render(bet player,bln renderer,float partial){
        if(!BlossomHooks.modules.on("capephysics")){states.clear();return false;}
        ave mc=ave.A();ClothSettings settings=BlossomHooks.settings.cloth;
        if(player.ax()||!player.a(wo.a))return true;
        if(mc.h==null)return false;
        double dx=player.s-mc.h.s,dy=player.t-mc.h.t,dz=player.u-mc.h.u;
        if(dx*dx+dy*dy+dz*dz>settings.distance*settings.distance){states.remove(player);return false;}
        jy texture=player.k();
        if(player==mc.h&&BlossomHooks.modules.on("cape")&&BlossomHooks.capes.hidden())return true;
        if(texture==null){
            if(player!=mc.h)return false;
            if(white==null){java.awt.image.BufferedImage image=new java.awt.image.BufferedImage(64,32,java.awt.image.BufferedImage.TYPE_INT_ARGB);for(int y=0;y<32;y++)for(int x=0;x<64;x++)image.setRGB(x,y,0xffffffff);white=mc.P().a("blossom/white_cape",new blz(image));}
            texture=white;
        }
        int resolution=player==mc.h?settings.subdivisions:Math.min(settings.subdivisions,dx*dx+dy*dy+dz*dz>64?8:16);
        long now=System.nanoTime();State state=states.get(player);
        if(state==null||state.mesh.rows!=resolution){if(states.size()>=32){bet oldest=null;long last=Long.MAX_VALUE;for(Map.Entry<bet,State> e:states.entrySet())if(e.getValue().last<last){oldest=e.getKey();last=e.getValue().last;}states.remove(oldest);}state=new State(resolution);states.put(player,state);}
        double x=player.p+(player.s-player.p)*partial,y=player.q+(player.t-player.q)*partial,z=player.r+(player.u-player.r)*partial;
        double yaw=(player.aJ+wrap(player.aI-player.aJ)*partial)*Math.PI/180;
        bbr model=renderer.g();double crouch=player.av()?.2:0,anchorY=crouch+model.g.d/16.-Math.sin(model.g.f)*Cloth.ANCHOR_Z,anchorZ=model.g.e/16.+Math.cos(model.g.f)*Cloth.ANCHOR_Z-Cloth.ANCHOR_Z;
        double dt=(now-state.last)*1e-9;
        if(state.last==0||dt>.5||(state.world==null||state.world.get()!=mc.f))state.mesh.reset();
        else {
            double wx=state.x-x,wz=state.z-z,c=Math.cos(state.yaw),s=Math.sin(state.yaw);
            state.mesh.moveFrame(wx*c+wz*s,y-state.y+state.anchorY-anchorY,wx*s-wz*c+state.anchorZ-anchorZ,wrapRadians(state.yaw-yaw));
        }
        state.x=x;state.y=y;state.z=z;state.yaw=yaw;if(state.world==null||state.world.get()!=mc.f)state.world=new java.lang.ref.WeakReference<Object>(mc.f);state.last=now;state.anchorY=anchorY;state.anchorZ=anchorZ;
        body(state.mesh,model);
        state.mesh.torso[1]+=crouch-anchorY;state.mesh.torso[2]-=anchorZ;
        for(int n=0;n<state.mesh.capsuleCount;n++){int o=n*7;state.mesh.capsules[o+1]+=crouch-anchorY;state.mesh.capsules[o+2]-=anchorZ;state.mesh.capsules[o+4]+=crouch-anchorY;state.mesh.capsules[o+5]-=anchorZ;}
        state.mesh.advance(dt>.5?0:dt,settings);
        mc.P().a(texture);
        GL11.glPushMatrix();GL11.glTranslated(0,anchorY,anchorZ);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_CURRENT_BIT|GL11.GL_POLYGON_BIT|GL11.GL_LIGHTING_BIT);
        try{
            GL11.glShadeModel(GL11.GL_SMOOTH);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glColor4f(1,1,1,1);
            draw(state.mesh);
        }finally{GL11.glPopAttrib();GL11.glPopMatrix();}
        return true;
    }
    private static float wrap(float a){while(a>180)a-=360;while(a< -180)a+=360;return a;}
    private static double wrapRadians(double a){while(a>Math.PI)a-=2*Math.PI;while(a< -Math.PI)a+=2*Math.PI;return a;}
    private static void body(Cloth mesh,bbr model){
        mesh.capsuleCount=0;mesh.torsoCollision=true;
        double angle=model.g.f;mesh.torso[0]=model.g.c/16.;mesh.torso[1]=model.g.d/16.+.375*Math.cos(angle);mesh.torso[2]=model.g.e/16.+.375*Math.sin(angle);
        mesh.torso[3]=.265625;mesh.torso[4]=.390625;mesh.torso[5]=.140625;mesh.torso[6]=angle;
        // Two torso capsules preserve its rectangular width; limbs follow the actual model rotations.
        torso(mesh,model.g,-.13,.12,-.045,-.13,.62,-.045,.165);torso(mesh,model.g,.13,.12,-.045,.13,.62,-.045,.165);
        limb(mesh,model.h,.65,.19);limb(mesh,model.i,.65,.19);limb(mesh,model.j,.7,.18);limb(mesh,model.k,.7,.18);
        torso(mesh,model.g,-.24,.08,-.02,.24,.08,-.02,.105);
    }
    private static void torso(Cloth mesh,bct part,double x,double y,double z,double ex,double ey,double ez,double radius){
        double sin=Math.sin(part.f),cos=Math.cos(part.f),px=part.c/16.,py=part.d/16.,pz=part.e/16.;
        capsule(mesh,px+x,py+y*cos-z*sin,pz+y*sin+z*cos,px+ex,py+ey*cos-ez*sin,pz+ey*sin+ez*cos,radius);
    }
    private static void limb(Cloth mesh,bct part,double length,double radius){
        double sx=Math.sin(part.f),cx=Math.cos(part.f),sy=Math.sin(part.g),cy=Math.cos(part.g),sz=Math.sin(part.h),cz=Math.cos(part.h);
        double x=part.c/16.,y=part.d/16.,z=part.e/16.;
        capsule(mesh,x,y,z,x+length*(cz*sy*sx-sz*cx),y+length*(sz*sy*sx+cz*cx),z+length*cy*sx,radius);
    }
    private static void capsule(Cloth c,double x,double y,double z,double ex,double ey,double ez,double r){int o=c.capsuleCount++*7;c.capsules[o]=x;c.capsules[o+1]=y;c.capsules[o+2]=z;c.capsules[o+3]=ex;c.capsules[o+4]=ey;c.capsules[o+5]=ez;c.capsules[o+6]=r;}
    private static void draw(Cloth c){
        c.prepareRender();
        GL11.glBegin(GL11.GL_TRIANGLES);
        try{for(int t=0;t<c.triangles.length;t+=3){face(c,c.triangles[t],c.triangles[t+1],c.triangles[t+2],true);face(c,c.triangles[t+2],c.triangles[t+1],c.triangles[t],false);}}finally{GL11.glEnd();}
        GL11.glBegin(GL11.GL_QUADS);
        try{for(int y=0;y<c.rows;y++){edge(c,y*(c.cols+1),(y+1)*(c.cols+1),0,1,y/(double)c.rows,(y+1)/(double)c.rows);edge(c,y*(c.cols+1)+c.cols,(y+1)*(c.cols+1)+c.cols,11,12,y/(double)c.rows,(y+1)/(double)c.rows);}
            for(int x=0;x<c.cols;x++){edge(c,x,x+1,1+x*10.0/c.cols,1+(x+1)*10.0/c.cols,0,0);edge(c,c.rows*(c.cols+1)+x,c.rows*(c.cols+1)+x+1,11+x*10.0/c.cols,11+(x+1)*10.0/c.cols,0,0);}
        }finally{GL11.glEnd();}
    }
    private static void face(Cloth c,int a,int b,int d,boolean front){
        vertex(c,a,front);vertex(c,b,front);vertex(c,d,front);
    }
    private static void vertex(Cloth c,int v,boolean front){int i=v*3;double sign=front?1:-1;GL11.glNormal3d(sign*c.renderNormal[i],sign*c.renderNormal[i+1],sign*c.renderNormal[i+2]);double u=(v%(c.cols+1))/(double)c.cols,w=(v/(c.cols+1))/(double)c.rows;point(c,v,front?1+u*10:22-u*10,1+w*16,front?Cloth.RENDER_HALF_THICKNESS:-Cloth.RENDER_HALF_THICKNESS);}
    private static void edge(Cloth c,int a,int b,double u0,double u1,double v0,double v1){
        int ai=a*3,bi=b*3;double tx=c.renderPosition[bi]-c.renderPosition[ai],ty=c.renderPosition[bi+1]-c.renderPosition[ai+1],tz=c.renderPosition[bi+2]-c.renderPosition[ai+2];
        double nx=ty*c.renderNormal[ai+2]-tz*c.renderNormal[ai+1],ny=tz*c.renderNormal[ai]-tx*c.renderNormal[ai+2],nz=tx*c.renderNormal[ai+1]-ty*c.renderNormal[ai],len=Math.sqrt(nx*nx+ny*ny+nz*nz);
        if(len>1e-10)GL11.glNormal3d(nx/len,ny/len,nz/len);
        if(v0==0&&v1==0){point(c,a,u0,0,Cloth.RENDER_HALF_THICKNESS);point(c,b,u1,0,Cloth.RENDER_HALF_THICKNESS);point(c,b,u1,1,-Cloth.RENDER_HALF_THICKNESS);point(c,a,u0,1,-Cloth.RENDER_HALF_THICKNESS);}
        else{point(c,a,u0,1+v0*16,Cloth.RENDER_HALF_THICKNESS);point(c,b,u0,1+v1*16,Cloth.RENDER_HALF_THICKNESS);point(c,b,u1,1+v1*16,-Cloth.RENDER_HALF_THICKNESS);point(c,a,u1,1+v0*16,-Cloth.RENDER_HALF_THICKNESS);}
    }
    private static void point(Cloth c,int v,double u,double w,double offset){int i=v*3;GL11.glTexCoord2d(u/64.,w/32.);GL11.glVertex3d(c.renderPosition[i]+c.renderNormal[i]*offset,c.renderPosition[i+1]+c.renderNormal[i+1]*offset,c.renderPosition[i+2]+c.renderNormal[i+2]*offset);}
}
