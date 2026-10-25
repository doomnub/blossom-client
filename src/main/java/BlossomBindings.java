import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** One vanilla KeyBinding object per action; Controls and module UI share it. */
public final class BlossomBindings {
    public static avb zoom,menu;
    public static void init() throws java.io.IOException {
        if(zoom!=null)return;
        ave mc=ave.A();zoom=new avb("Blossom zoom",BlossomHooks.settings.zoomKey,"Blossom");menu=new avb("Blossom settings",BlossomHooks.settings.menuKey,"Blossom");
        avb[] keys=Arrays.copyOf(mc.t.ax,mc.t.ax.length+2);keys[keys.length-2]=zoom;keys[keys.length-1]=menu;mc.t.ax=keys;
        Path options=mc.v.toPath().resolve("options.txt");
        if(Files.isRegularFile(options))for(String line:Files.readAllLines(options,StandardCharsets.UTF_8))for(avb key:new avb[]{zoom,menu})if(line.startsWith("key_"+key.g()+":")){
            try{int code=Integer.parseInt(line.substring(line.indexOf(':')+1));if(code>=-100&&code<=255)key.b(code);}catch(NumberFormatException ignored){}
        }
        avb.b();sync();
    }
    public static void apply(){if(zoom!=null){zoom.b(BlossomHooks.settings.zoomKey);menu.b(BlossomHooks.settings.menuKey);avb.b();}}
    public static void sync(){if(zoom!=null){BlossomHooks.settings.zoomKey=zoom.i();BlossomHooks.settings.menuKey=menu.i();}}
    public static void set(avb key,int code){ave.A().t.a(key,code);avb.b();sync();}
    public static boolean held(avb key){if(key==null)return false;int code=key.i();return code<0?code>=-100&&Mouse.isButtonDown(code+100):code>0&&code<Keyboard.KEYBOARD_SIZE&&Keyboard.isKeyDown(code);}
    public static String label(avb key){int code=key.i();return code==0?"Unbound":code<0?"Mouse "+(code+101):Keyboard.getKeyName(code);}
}
