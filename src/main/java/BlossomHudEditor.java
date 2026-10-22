import dev.blossom.core.Modules;
import org.lwjgl.input.Keyboard;
public final class BlossomHudEditor extends axu {
    private final axu previous;
    private Modules.Module dragging;
    private int offsetX,offsetY;
    public BlossomHudEditor(axu previous){this.previous=previous;}
    @Override public void a(int mx,int my,float partial) {
        BlossomHooks.drawHud();
        q.a("Drag HUD elements | Hold Shift for 8px grid | Esc saves",8, m-12,0xffffffff);
    }
    @Override protected void a(int mx,int my,int button) {
        if(button!=0)return;
        for(Modules.Module module:BlossomHooks.modules.all()) if(module.enabled&&module.category.equals("HUD")) {
            int width=q.a(BlossomHooks.text(module))+8,x=BlossomHooks.x(module,l,width),y=BlossomHooks.y(module,m);
            if(mx>=x&&mx<x+width&&my>=y&&my<y+16){dragging=module;offsetX=mx-x;offsetY=my-y;}
        }
    }
    @Override protected void a(int mx,int my,int button,long elapsed) {
        if(dragging==null)return;
        int x=mx-offsetX,y=my-offsetY;
        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){x=Math.round(x/8f)*8;y=Math.round(y/8f)*8;}
        dragging.x=Math.max(0,Math.min(1,x/(float)Math.max(1,l-q.a(BlossomHooks.text(dragging))-8)));
        dragging.y=Math.max(0,Math.min(1,y/(float)Math.max(1,m-16)));
    }
    @Override protected void b(int x,int y,int button){dragging=null;}
    @Override protected void a(char c,int key) {if(key==Keyboard.KEY_ESCAPE){BlossomHooks.save("default");j.a(previous);}}
    @Override public void m(){BlossomHooks.save("default");}
    @Override public boolean d(){return false;}
}
