import java.util.function.Consumer;
import org.lwjgl.input.Keyboard;
/** Reusable text entry with validation errors retained in the same screen. */
public final class BlossomTextInput extends axu {
    private final axu previous;
    private final String title;
    private final Consumer<String> accept;
    private String value,error="";
    public BlossomTextInput(axu previous,String title,String value,Consumer<String> accept){this.previous=previous;this.title=title;this.value=value;this.accept=accept;}
    @Override public void b(){n.clear();n.add(new BlossomButton(0,l/2-100,m/2+25,96,20,"Save"));n.add(new BlossomButton(1,l/2+4,m/2+25,96,20,"Cancel"));}
    @Override public void a(int x,int y,float delta){c();BlossomTheme.panel(l,m);q.a(title,l/2-140,m/2-40,BlossomHooks.accent);q.a(value+"_",l/2-140,m/2-15,0xffffffff);q.a(error,l/2-140,m/2+8,0xffff7777);super.a(x,y,delta);}
    @Override protected void a(avs button){if(button.k==1)j.a(previous);else submit();}
    private void submit(){try{accept.accept(value);j.a(previous);}catch(IllegalArgumentException e){error=e.getMessage();}}
    @Override protected void a(char ch,int key){
        if(key==Keyboard.KEY_ESCAPE){j.a(previous);return;}
        if(key==Keyboard.KEY_RETURN){submit();return;}
        if(key==Keyboard.KEY_BACK&&value.length()>0)value=value.substring(0,value.length()-1);
        else if(ch>=32&&ch!=127&&value.length()<80)value+=ch;
    }
    @Override public boolean d(){return false;}
}
