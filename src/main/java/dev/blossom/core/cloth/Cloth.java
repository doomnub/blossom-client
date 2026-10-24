package dev.blossom.core.cloth;
import java.util.Arrays;
/** Fixed 120 Hz position-based cloth. Coordinates are metres in the player model frame, Y down. */
public final class Cloth {
    public static final double STEP=1.0/120,THICKNESS=.008;
    public final int cols,rows,count;
    public final double[] position,previous,renderPrevious;
    public final int[] triangles;
    private final int[] a,b;
    private final double[] rest,weight;
    private int constraints;
    private double accumulator,time;
    private final int[] buckets=new int[16384],next,triangle,cx,cy,cz,seen;
    private int entries,stamp;
    private final double cell;
    public final double[] capsules=new double[7*7];
    public int capsuleCount;
    public Cloth(int subdivisions){
        rows=Math.max(4,Math.min(64,subdivisions));cols=Math.max(3,(int)Math.round(rows*.625));count=(rows+1)*(cols+1);
        position=new double[count*3];previous=new double[count*3];renderPrevious=new double[count*3];
        a=new int[count*6];b=new int[a.length];rest=new double[a.length];weight=new double[a.length];
        triangles=new int[rows*cols*6];int t=0;
        reset();
        for(int y=0;y<=rows;y++)for(int x=0;x<=cols;x++){
            int i=y*(cols+1)+x;
            if(x<cols)link(i,i+1,1);if(y<rows)link(i,i+cols+1,1);
            if(x<cols&&y<rows){link(i,i+cols+2,.8);link(i+1,i+cols+1,.8);triangles[t++]=i;triangles[t++]=i+cols+1;triangles[t++]=i+1;triangles[t++]=i+1;triangles[t++]=i+cols+1;triangles[t++]=i+cols+2;}
            if(x+2<=cols)link(i,i+2,-1);if(y+2<=rows)link(i,i+2*(cols+1),-1);
        }
        cell=Math.max(.04,2.0/rows);int capacity=triangles.length*24;
        next=new int[capacity];triangle=new int[capacity];cx=new int[capacity];cy=new int[capacity];cz=new int[capacity];seen=new int[triangles.length/3];
    }
    public void reset(){
        for(int y=0;y<=rows;y++)for(int x=0;x<=cols;x++){int i=3*(y*(cols+1)+x);position[i]=.3125-x*.625/cols;position[i+1]=y/(double)rows;position[i+2]=.145;}
        System.arraycopy(position,0,previous,0,position.length);System.arraycopy(position,0,renderPrevious,0,position.length);accumulator=0;
    }
    private void link(int i,int j,double strength){a[constraints]=i;b[constraints]=j;double sum=0;for(int k=0;k<3;k++){double d=position[3*i+k]-position[3*j+k];sum+=d*d;}rest[constraints]=Math.sqrt(sum);weight[constraints++]=strength;}
    public void moveFrame(double dx,double dy,double dz,double angle){
        if(dx*dx+dy*dy+dz*dz>16||Math.abs(angle)>2){reset();return;}
        double c=Math.cos(angle),s=Math.sin(angle);
        transform(position,dx,dy,dz,c,s);transform(previous,dx,dy,dz,c,s);transform(renderPrevious,dx,dy,dz,c,s);pin();
    }
    private void transform(double[] v,double dx,double dy,double dz,double c,double s){for(int i=0;i<v.length;i+=3){double x=v[i]+dx,z=v[i+2]+dz;v[i]=c*x-s*z;v[i+1]+=dy;v[i+2]=s*x+c*z;}}
    private void pin(){for(int x=0;x<=cols;x++){int i=x*3;position[i]=.3125-x*.625/cols;position[i+1]=0;position[i+2]=.145;System.arraycopy(position,i,previous,i,3);System.arraycopy(position,i,renderPrevious,i,3);}}
    public void advance(double seconds,ClothSettings settings){
        if(!Double.isFinite(seconds)||seconds<0||seconds>.5){reset();return;}
        accumulator=Math.min(accumulator+seconds,STEP*8);
        int steps=0;while(accumulator>=STEP&&steps++<8){System.arraycopy(position,0,renderPrevious,0,position.length);step(settings);accumulator-=STEP;}
    }
    public double rendered(int i){double f=accumulator/STEP;return renderPrevious[i]*(1-f)+position[i]*f;}
    private void step(ClothSettings s){
        time+=STEP;
        double wind=s.wind?s.windStrength*(Math.sin(time*.73)+.4*Math.sin(time*1.39)):.0;
        for(int i=(cols+1)*3;i<position.length;i+=3)for(int k=0;k<3;k++){
            double p=position[i+k],velocity=(p-previous[i+k])*(1-s.damping);
            double force=k==1?s.gravity:k==0?wind*.5:wind;
            position[i+k]=p+Math.max(-.12,Math.min(.12,velocity))+force*STEP*STEP;previous[i+k]=p;
        }
        for(int iteration=0;iteration<s.iterations;iteration++){
            for(int n=0;n<constraints;n++){
                int ai=a[n],bi=b[n],i=ai*3,j=bi*3;double dx=position[j]-position[i],dy=position[j+1]-position[i+1],dz=position[j+2]-position[i+2];double length=Math.sqrt(dx*dx+dy*dy+dz*dz);if(length<1e-8)continue;
                double strength=weight[n]<0?s.stiffness*.35:weight[n];double wa=ai<=cols?0:1,wb=bi<=cols?0:1;if(wa+wb==0)continue;
                double correction=(length-rest[n])/length*strength/(wa+wb);
                position[i]+=dx*correction*wa;position[i+1]+=dy*correction*wa;position[i+2]+=dz*correction*wa;
                position[j]-=dx*correction*wb;position[j+1]-=dy*correction*wb;position[j+2]-=dz*correction*wb;
            }
            if(s.playerCollision)body();pin();
        }
        if(s.selfCollision)selfCollide();if(s.playerCollision)body();pin();
        for(double v:position)if(!Double.isFinite(v)||Math.abs(v)>8){reset();break;}
    }
    private void body(){
        for(int i=(cols+1)*3;i<position.length;i+=3)for(int n=0;n<capsuleCount;n++){
            int o=n*7;double ax=capsules[o],ay=capsules[o+1],az=capsules[o+2],dx=capsules[o+3]-ax,dy=capsules[o+4]-ay,dz=capsules[o+5]-az;
            double len=dx*dx+dy*dy+dz*dz,t=len<1e-8?0:Math.max(0,Math.min(1,((position[i]-ax)*dx+(position[i+1]-ay)*dy+(position[i+2]-az)*dz)/len));
            double x=position[i]-ax-t*dx,y=position[i+1]-ay-t*dy,z=position[i+2]-az-t*dz,r=capsules[o+6]+THICKNESS,d=Math.sqrt(x*x+y*y+z*z);
            if(d<r){if(d<1e-8){z=1;d=1;}double f=(r-d)/d;position[i]+=x*f;position[i+1]+=y*f;position[i+2]+=z*f;}
        }
    }
    private int hash(int x,int y,int z){return ((x*73856093)^(y*19349663)^(z*83492791))&16383;}
    private int cell(double x){return (int)Math.floor(x/cell);}
    /** Triangle AABB spatial hash; point/triangle separation and swept plane crossing, excluding adjacent mesh cells. */
    private void selfCollide(){
        Arrays.fill(buckets,-1);entries=0;
        for(int t=0;t<triangles.length/3;t++){
            int i=triangles[t*3]*3,j=triangles[t*3+1]*3,k=triangles[t*3+2]*3;
            int x0=cell(Math.min(position[i],Math.min(position[j],position[k]))-THICKNESS),x1=cell(Math.max(position[i],Math.max(position[j],position[k]))+THICKNESS);
            int y0=cell(Math.min(position[i+1],Math.min(position[j+1],position[k+1]))-THICKNESS),y1=cell(Math.max(position[i+1],Math.max(position[j+1],position[k+1]))+THICKNESS);
            int z0=cell(Math.min(position[i+2],Math.min(position[j+2],position[k+2]))-THICKNESS),z1=cell(Math.max(position[i+2],Math.max(position[j+2],position[k+2]))+THICKNESS);
            if((long)(x1-x0+1)*(y1-y0+1)*(z1-z0+1)>256)continue;
            for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++){
                if(entries==next.length)return;int h=hash(x,y,z);next[entries]=buckets[h];buckets[h]=entries;triangle[entries]=t;cx[entries]=x;cy[entries]=y;cz[entries++]=z;
            }
        }
        int budget=250000;
        for(int v=cols+1;v<count;v++){
            int i=v*3,x=cell(position[i]),y=cell(position[i+1]),z=cell(position[i+2]);if(++stamp==Integer.MAX_VALUE){Arrays.fill(seen,0);stamp=1;}
            for(int e=buckets[hash(x,y,z)];e>=0;e=next[e]){
                if(cx[e]!=x||cy[e]!=y||cz[e]!=z)continue;int t=triangle[e];if(seen[t]==stamp)continue;seen[t]=stamp;
                if(--budget<=0)return;
                int va=triangles[t*3],vb=triangles[t*3+1],vc=triangles[t*3+2];if(near(v,va)||near(v,vb)||near(v,vc))continue;
                collide(v,va,vb,vc);
            }
        }
    }
    private boolean near(int a,int b){return Math.abs(a/(cols+1)-b/(cols+1))<=1&&Math.abs(a%(cols+1)-b%(cols+1))<=1;}
    private void collide(int v,int a,int b,int c){
        int i=v*3,j=a*3,k=b*3,l=c*3;
        double ux=position[k]-position[j],uy=position[k+1]-position[j+1],uz=position[k+2]-position[j+2],vx=position[l]-position[j],vy=position[l+1]-position[j+1],vz=position[l+2]-position[j+2];
        double nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx,n=Math.sqrt(nx*nx+ny*ny+nz*nz);if(n<1e-10)return;nx/=n;ny/=n;nz/=n;
        double px=position[i]-position[j],py=position[i+1]-position[j+1],pz=position[i+2]-position[j+2],d=px*nx+py*ny+pz*nz;
        double old=(previous[i]-previous[j])*nx+(previous[i+1]-previous[j+1])*ny+(previous[i+2]-previous[j+2])*nz;
        if(Math.abs(d)>=THICKNESS && (d*old>=0||Math.abs(d)>.04))return;
        double uu=ux*ux+uy*uy+uz*uz,vv=vx*vx+vy*vy+vz*vz,uv=ux*vx+uy*vy+uz*vz,pu=px*ux+py*uy+pz*uz,pv=px*vx+py*vy+pz*vz,den=uu*vv-uv*uv;if(den<1e-14)return;
        double wb=(pu*vv-pv*uv)/den,wc=(pv*uu-pu*uv)/den,wa=1-wb-wc;if(wa<0||wb<0||wc<0)return;
        double mass=1+(a>cols?wa*wa:0)+(b>cols?wb*wb:0)+(c>cols?wc*wc:0);
        double correction=Math.max(-.025,Math.min(.025,((old>=0?1:-1)*THICKNESS-d)/mass));
        position[i]+=nx*correction;position[i+1]+=ny*correction;position[i+2]+=nz*correction;
        displace(a,-wa*correction,nx,ny,nz);displace(b,-wb*correction,nx,ny,nz);displace(c,-wc*correction,nx,ny,nz);
    }
    private void displace(int v,double d,double x,double y,double z){if(v<=cols)return;int i=v*3;position[i]+=x*d;position[i+1]+=y*d;position[i+2]+=z*d;}
}
