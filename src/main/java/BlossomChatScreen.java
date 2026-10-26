import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Vanilla sending and tab completion remain intact; local tools only fill/copy text. */
public final class BlossomChatScreen extends awv {
    public BlossomChatScreen(){super();}
    public BlossomChatScreen(String initial){super(initial);}
    @Override public void b(){
        String draft=BlossomChat.enabled()&&a!=null?a.b():null;
        super.b();
        if(draft!=null)a.a(draft);
    }
    @Override protected void a(char typed,int key){
        boolean control=Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)||Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        if(BlossomChat.enabled()&&control){
            if(key==Keyboard.KEY_F){j.a(new BlossomChatHistory(this));return;}
            if(key==Keyboard.KEY_SPACE){a.b(BlossomHooks.settings.chat.shortcut);return;}
        }
        super.a(typed,key);
    }
    @Override protected void a(int x,int y,int button){
        if(BlossomChat.enabled()&&button==0&&(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)||Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))){
            eu component=j.q.d().a(Mouse.getX(),Mouse.getY());
            if(component!=null){axu.e(component.c());return;}
        }
        super.a(x,y,button);
    }
}
