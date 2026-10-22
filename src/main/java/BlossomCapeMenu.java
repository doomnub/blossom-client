import java.util.*;
import org.lwjgl.input.Keyboard;
/** Local cape library with actual texture playback preview, not a placeholder model. */
public final class BlossomCapeMenu extends axu {
    private final axu previous;
    private final BlossomCapes capes=BlossomHooks.capes;
    private List<String> entries=Collections.emptyList();
    private int page;
    public BlossomCapeMenu(axu previous){this.previous=previous;capes.refresh();}
    private void action(int id,String label,int col,int row){n.add(new avs(id,l/2-140+col*94,m-111+row*24,90,20,label));}
    @Override public void b(){
        n.clear();entries=capes.entries;int count=Math.max(1,(m-180)/22);
        page=Math.min(page,Math.max(0,(entries.size()-1)/count));
        for(int i=page*count;i<Math.min(entries.size(),(page+1)*count);i++) {
            String name=entries.get(i);String label=(name.equals(capes.selected)?"> ":"")+name;
            if(label.length()>20)label=label.substring(0,17)+"...";
            n.add(new avs(1000+i,l/2-140,43+(i-page*count)*22,136,20,label));
        }
        if(entries.size()>count)n.add(new avs(120,l/2-140,43+count*22,136,20,"Next page"));
        action(100,"Default cape",0,0);action(101,"No cape",1,0);action(102,"Import",2,0);
        action(103,capes.clock.paused()?"Resume":"Pause",0,1);action(104,"Loop: "+capes.loop,1,1);
        action(105,"Bounce: "+new String[]{"file","on","off"}[capes.pingPongMode],2,1);
        action(106,"FPS: "+(capes.fps==0?"file":capes.fps),0,2);action(107,"Speed: "+capes.clock.speed()+"x",1,2);action(108,"Folder",2,2);
        action(109,"Rename",0,3);action(110,"Remove",1,3);action(111,"Back",2,3);
    }
    @Override public void e(){if(entries!=capes.entries)b();}
    @Override protected void a(avs button){
        if(button.k>=1000){capes.select(entries.get(button.k-1000));b();return;}
        switch(button.k){
            case 100:capes.mode="default";break;
            case 101:capes.mode="none";break;
            case 102:capes.importDialog();break;
            case 103:capes.clock.paused(!capes.clock.paused(),System.nanoTime());break;
            case 104:capes.loop=!capes.loop;break;
            case 105:capes.pingPongMode=(capes.pingPongMode+1)%3;capes.rebuildTimeline();break;
            case 106:int[] values={0,10,20,24,30,60};int i=0;while(i<values.length&&values[i]!=capes.fps)i++;capes.fps=values[(i+1)%values.length];capes.rebuildTimeline();break;
            case 107:double speed=capes.clock.speed();capes.clock.speed(speed>=4?.25:speed+.25,System.nanoTime());break;
            case 108:capes.openFolder();break;
            case 109:if(!capes.selected.isEmpty())j.a(new BlossomTextInput(this,"Rename cape (letters, digits, _ or -)",capes.selected.replaceFirst("\\.png$",""),name->{if(!name.matches("[a-zA-Z0-9_-]{1,48}"))throw new IllegalArgumentException("Use 1–48 letters, digits, _ or -");capes.rename(name);}));return;
            case 110:if(!capes.selected.isEmpty())j.a(new BlossomConfirm(this,"Move selected cape to local .trash folder?",capes::remove));return;
            case 111:j.a(previous);return;
            case 120:page++;if(page*Math.max(1,(m-180)/22)>=entries.size())page=0;break;
            default:break;
        }
        b();
    }
    @Override public void a(int x,int y,float partial){
        c();q.a("BLOSSOM / Local capes",l/2-140,12,BlossomHooks.accent);
        q.a("Mode: "+capes.mode+" | Override: "+BlossomHooks.modules.on("cape"),l/2-140,27,0xffcccccc);
        jy preview=capes.preview();
        if(preview!=null){bfl.c(1,1,1,1);j.P().a(preview);avp.a(l/2+6,43,0f,0f,128,64,128f,64f);q.a("Frame "+(capes.frameIndex()+1)+"/"+capes.frameCount(),l/2+6,110,0xffcccccc);}
        else q.a("Select or import a PNG",l/2+6,50,0xffcccccc);
        String status=capes.status;if(status.length()>49)status=status.substring(0,46)+"...";
        q.a(status,l/2-140,m-14,0xffcccccc);super.a(x,y,partial);
    }
    @Override protected void a(char c,int key){if(key==Keyboard.KEY_ESCAPE)j.a(previous);}
    @Override public void m(){BlossomHooks.save("default");}
    @Override public boolean d(){return false;}
}
