import dev.blossom.core.cloth.*;
import java.util.*;
import org.lwjgl.opengl.GL11;
/** Render-thread-only cloth instances. The vanilla layer supplies the body transform and lighting. */
public final class BlossomCloth {
    private static final Map<bet,State> states=new WeakHashMap<bet,State>();
    private static jy white;
    private static final class State {Cloth mesh;java.lang.ref.WeakReference<Object> world;long last;double x,y,z,yaw,anchorY,anchorZ;State(int n){mesh=new Cloth(n);}}
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
        bbr model=renderer.g();double anchorY=model.g.d/16.-Math.sin(model.g.f)*.145,anchorZ=model.g.e/16.+Math.cos(model.g.f)*.145-.145;
        double dt=(now-state.last)*1e-9;
        if(state.last==0||dt>.5||(state.world==null||state.world.get()!=mc.f))state.mesh.reset();
        else {
            double wx=state.x-x,wz=state.z-z,c=Math.cos(state.yaw),s=Math.sin(state.yaw);
            state.mesh.moveFrame(-wx*c-wz*s,y-state.y+state.anchorY-anchorY,wx*s-wz*c+state.anchorZ-anchorZ,wrapRadians(state.yaw-yaw));
        }
        state.x=x;state.y=y;state.z=z;state.yaw=yaw;if(state.world==null||state.world.get()!=mc.f)state.world=new java.lang.ref.WeakReference<Object>(mc.f);state.last=now;state.anchorY=anchorY;state.anchorZ=anchorZ;
        body(state.mesh,model);
        for(int n=0;n<state.mesh.capsuleCount;n++){int o=n*7;state.mesh.capsules[o+1]-=anchorY;state.mesh.capsules[o+2]-=anchorZ;state.mesh.capsules[o+4]-=anchorY;state.mesh.capsules[o+5]-=anchorZ;}
        state.mesh.advance(dt>.5?0:dt,settings);
        mc.P().a(texture);
        GL11.glPushMatrix();GL11.glTranslated(0,anchorY,anchorZ);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_CURRENT_BIT|GL11.GL_POLYGON_BIT);
        try{
            GL11.glDisable(GL11.GL_CULL_FACE);GL11.glColor4f(1,1,1,1);
            draw(state.mesh);
        }finally{GL11.glPopAttrib();GL11.glPopMatrix();}
        return true;
    }
    private static float wrap(float a){while(a>180)a-=360;while(a< -180)a+=360;return a;}
    private static double wrapRadians(double a){while(a>Math.PI)a-=2*Math.PI;while(a< -Math.PI)a+=2*Math.PI;return a;}
    private static void body(Cloth mesh,bbr model){
        mesh.capsuleCount=0;
        // Two torso capsules preserve its rectangular width; limbs follow the actual model rotations.
        capsule(mesh,-.13,.12,-.045,-.13,.62,-.045,.165);capsule(mesh,.13,.12,-.045,.13,.62,-.045,.165);
        limb(mesh,model.h,.65,.115);limb(mesh,model.i,.65,.115);limb(mesh,model.j,.7,.12);limb(mesh,model.k,.7,.12);
        capsule(mesh,-.24,.08,-.02,.24,.08,-.02,.105);
    }
    private static void limb(Cloth mesh,bct part,double length,double radius){
        double sx=Math.sin(part.f),cx=Math.cos(part.f),sy=Math.sin(part.g),cy=Math.cos(part.g),sz=Math.sin(part.h),cz=Math.cos(part.h);
        double x=part.c/16.,y=part.d/16.,z=part.e/16.;
        capsule(mesh,x,y,z,x+length*(cz*sy*sx-sz*cx),y+length*(sz*sy*sx+cz*cx),z+length*cy*sx,radius);
    }
    private static void capsule(Cloth c,double x,double y,double z,double ex,double ey,double ez,double r){int o=c.capsuleCount++*7;c.capsules[o]=x;c.capsules[o+1]=y;c.capsules[o+2]=z;c.capsules[o+3]=ex;c.capsules[o+4]=ey;c.capsules[o+5]=ez;c.capsules[o+6]=r;}
    private static void draw(Cloth c){
        GL11.glBegin(GL11.GL_TRIANGLES);
        try{for(int t=0;t<c.triangles.length;t+=3){face(c,c.triangles[t],c.triangles[t+1],c.triangles[t+2],true);face(c,c.triangles[t+2],c.triangles[t+1],c.triangles[t],false);}}finally{GL11.glEnd();}
        GL11.glBegin(GL11.GL_QUADS);
        try{for(int y=0;y<c.rows;y++){edge(c,y*(c.cols+1),(y+1)*(c.cols+1),0,1,y/(double)c.rows,(y+1)/(double)c.rows);edge(c,y*(c.cols+1)+c.cols,(y+1)*(c.cols+1)+c.cols,11,12,y/(double)c.rows,(y+1)/(double)c.rows);}
            for(int x=0;x<c.cols;x++){edge(c,x,x+1,1+x*10.0/c.cols,1+(x+1)*10.0/c.cols,0,0);edge(c,c.rows*(c.cols+1)+x,c.rows*(c.cols+1)+x+1,11+x*10.0/c.cols,11+(x+1)*10.0/c.cols,0,0);}
        }finally{GL11.glEnd();}
    }
    private static void face(Cloth c,int a,int b,int d,boolean front){
        double ax=c.rendered(3*b)-c.rendered(3*a),ay=c.rendered(3*b+1)-c.rendered(3*a+1),az=c.rendered(3*b+2)-c.rendered(3*a+2),bx=c.rendered(3*d)-c.rendered(3*a),by=c.rendered(3*d+1)-c.rendered(3*a+1),bz=c.rendered(3*d+2)-c.rendered(3*a+2);
        double nx=ay*bz-az*by,ny=az*bx-ax*bz,nz=ax*by-ay*bx,n=Math.sqrt(nx*nx+ny*ny+nz*nz);if(n<1e-10)return;
        GL11.glNormal3d(nx/n,ny/n,nz/n);vertex(c,a,front);vertex(c,b,front);vertex(c,d,front);
    }
    private static void vertex(Cloth c,int v,boolean front){double u=(v%(c.cols+1))/(double)c.cols,w=(v/(c.cols+1))/(double)c.rows;GL11.glTexCoord2d((front?1+u*10:22-u*10)/64.,(1+w*16)/32.);GL11.glVertex3d(c.rendered(v*3),c.rendered(v*3+1),c.rendered(v*3+2)+(front?-.001:.001));}
    private static void edge(Cloth c,int a,int b,double u0,double u1,double v0,double v1){GL11.glNormal3f(0,0,1);point(c,a,u0,(v0==0&&v1==0)?0:1+v0*16,-.001);point(c,b,u1,(v0==0&&v1==0)?0:1+v1*16,-.001);point(c,b,u1,(v0==0&&v1==0)?1:1+v1*16,.001);point(c,a,u0,(v0==0&&v1==0)?1:1+v0*16,.001);}
    private static void point(Cloth c,int v,double u,double w,double z){GL11.glTexCoord2d(u/64.,w/32.);GL11.glVertex3d(c.rendered(v*3),c.rendered(v*3+1),c.rendered(v*3+2)+z);}
}
