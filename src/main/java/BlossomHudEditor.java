import dev.blossom.core.*;
import org.lwjgl.input.Keyboard;
public final class BlossomHudEditor extends axu {
    private final axu previous;
    private Modules.Module dragging;
    private int offsetX,offsetY;
    public BlossomHudEditor(axu previous){this.previous=previous;}
    @Override public void a(int mx,int my,float partial) {
        BlossomHud.draw();
        for(Modules.Module module:BlossomHooks.modules.all())if(module.enabled&&module.category.equals("HUD")) {
            int x=BlossomHud.x(module,l),y=BlossomHud.y(module,m),w=BlossomHud.width(module),h=BlossomHud.height(module);
            if(module==dragging||mx>=x&&mx<x+w&&my>=y&&my<y+h) {
                int color=module.locked?0xff888888:BlossomHooks.accent;
                a(x,y,x+w,y+1,color);a(x,y+h-1,x+w,y+h,color);
                a(x,y,x+1,y+h,color);a(x+w-1,y,x+w,y+h,color);
            }
        }
        q.a("Drag | Shift: grid | Right click: style | Esc: save",8,m-12,0xffffffff);
    }
    @Override protected void a(int mx,int my,int button) {
        for(Modules.Module module:BlossomHooks.modules.all())if(module.enabled&&module.category.equals("HUD")) {
            int x=BlossomHud.x(module,l),y=BlossomHud.y(module,m);
            if(mx>=x&&mx<x+BlossomHud.width(module)&&my>=y&&my<y+BlossomHud.height(module)) {
                if(button==1){j.a(new BlossomModuleSettings(this,module));return;}
                if(button==0&&!module.locked){dragging=module;offsetX=mx-x;offsetY=my-y;}
            }
        }
    }
    @Override protected void a(int mx,int my,int button,long elapsed) {
        if(dragging==null)return;
        int w=BlossomHud.width(dragging),h=BlossomHud.height(dragging);
        boolean grid=Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
        dragging.x=HudLayout.fraction(HudLayout.snap(mx-offsetX,l,w,grid),l,w);
        dragging.y=HudLayout.fraction(HudLayout.snap(my-offsetY,m,h,grid),m,h);
    }
    @Override protected void b(int x,int y,int button){dragging=null;}
    @Override protected void a(char c,int key){if(key==Keyboard.KEY_ESCAPE)j.a(previous);}
    @Override public void m(){BlossomHooks.save("default");}
    @Override public boolean d(){return false;}
}
