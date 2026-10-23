import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import net.java.games.input.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
/** Replaces only camera deltas; cursor, buttons, sensitivity and inversion stay vanilla. */
public final class BlossomMouse {
    interface Source { double[] poll(); void close(); }
    private static Source source,windows;
    private static boolean attempted,active;
    private static double remainderX,remainderY;
    private static String state="Enable to use raw mouse motion";
    public static String status(){return state;}
    public static void idle(){
        if(source!=null&&(!BlossomHooks.modules.on("rawmouse")||ave.A().h==null||ave.A().m!=null||!Display.isActive()||!Mouse.isGrabbed())){active=false;remainderX=remainderY=0;try{source.poll();}catch(Throwable ignored){}if(!BlossomHooks.modules.on("rawmouse")){source.close();source=null;attempted=false;state="Raw input disabled";}}
    }
    public static void sample(avf helper){
        boolean wanted=BlossomHooks.modules.on("rawmouse");
        if(!wanted){if(source!=null){source.close();source=null;}attempted=false;active=false;remainderX=remainderY=0;state="Raw input disabled";return;}
        if(!attempted){attempted=true;try{String os=System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT);source=os.contains("win")?(windows==null?(windows=new Windows()):windows):os.contains("linux")?new Linux():null;state=source==null?"Unsupported platform; using vanilla":"Raw input ready";}catch(Throwable e){state="Raw input unavailable; using vanilla";System.err.println("[Blossom] "+state+": "+e.getClass().getSimpleName());}}
        if(source==null)return;
        try{
            double[] delta=source.poll();boolean focused=ave.A().h!=null&&ave.A().m==null&&Display.isActive()&&Mouse.isGrabbed();
            if(!focused||!active){remainderX=remainderY=0;helper.a=helper.b=0;active=focused;return;}
            remainderX+=delta[0];remainderY-=delta[1];int x=(int)remainderX,y=(int)remainderY;remainderX-=x;remainderY-=y;helper.a=x;helper.b=y;state="Raw input active";
        }catch(Throwable e){source.close();source=null;active=false;state="Raw input lost; using vanilla";}
    }
    private static final class Windows implements Source {
        private final Controller[] mice;
        Windows()throws Exception{
            Class<?> type=Class.forName("net.java.games.input.RawInputEnvironmentPlugin");java.lang.reflect.Constructor<?> ctor=type.getDeclaredConstructor();ctor.setAccessible(true);
            ControllerEnvironment env=(ControllerEnvironment)ctor.newInstance();java.util.List<Controller> found=new java.util.ArrayList<Controller>();
            for(Controller c:env.getControllers())if(c.getType()==Controller.Type.MOUSE)found.add(c);
            if(found.isEmpty())throw new IllegalStateException("No raw mouse device");mice=found.toArray(new Controller[0]);
        }
        public double[] poll(){double x=0,y=0;boolean ok=false;for(Controller mouse:mice){if(!mouse.poll())continue;ok=true;for(Component c:mouse.getComponents()){if(!c.isRelative())continue;if(c.getIdentifier()==Component.Identifier.Axis.X)x+=c.getPollData();if(c.getIdentifier()==Component.Identifier.Axis.Y)y+=c.getPollData();}}if(!ok)throw new IllegalStateException("Mouse disconnected");return new double[]{x,y};}
        public void close(){} // JInput owns its process-wide event thread and shutdown hook.
    }
    public interface X11 extends Library {
        Pointer XOpenDisplay(String display);NativeLong XDefaultRootWindow(Pointer display);int XQueryExtension(Pointer display,String name,IntByReference opcode,IntByReference event,IntByReference error);
        int XPending(Pointer display);int XNextEvent(Pointer display,Pointer event);int XGetEventData(Pointer display,Pointer cookie);void XFreeEventData(Pointer display,Pointer cookie);int XFlush(Pointer display);int XCloseDisplay(Pointer display);
    }
    public interface Xi extends Library {int XIQueryVersion(Pointer display,IntByReference major,IntByReference minor);int XISelectEvents(Pointer display,NativeLong window,Pointer masks,int count);}
    /** LP64 Xlib ABI, matching the launcher's supported Linux x86_64 target. */
    private static final class Linux implements Source {
        final X11 x=(X11)Native.loadLibrary("X11",X11.class);final Xi xi=(Xi)Native.loadLibrary("Xi",Xi.class);
        Pointer display;final Memory event=new Memory(192);final int opcode;
        Linux(){
            if(Native.POINTER_SIZE!=8||Native.LONG_SIZE!=8)throw new IllegalStateException("Requires Linux x86_64");
            display=x.XOpenDisplay(null);if(display==null)throw new IllegalStateException("X11 / XWayland unavailable");
            IntByReference op=new IntByReference(),major=new IntByReference(2),minor=new IntByReference(1);
            try{if(x.XQueryExtension(display,"XInputExtension",op,new IntByReference(),new IntByReference())==0||xi.XIQueryVersion(display,major,minor)!=0||(major.getValue()==2&&minor.getValue()<1))throw new IllegalStateException("XInput 2.1 required");
                opcode=op.getValue();Memory bits=new Memory(4);bits.clear();bits.setByte(2,(byte)2); // XI_RawMotion = 17
                Memory mask=new Memory(16);mask.clear();mask.setInt(0,1);mask.setInt(4,4);mask.setPointer(8,bits); // XIAllMasterDevices: no duplicate slave events
                if(xi.XISelectEvents(display,x.XDefaultRootWindow(display),mask,1)!=0)throw new IllegalStateException("Raw motion selection failed");x.XFlush(display);
            }catch(RuntimeException e){close();throw e;}
        }
        public double[] poll(){double dx=0,dy=0;int limit=65536;
            while(x.XPending(display)>0&&limit-->0){x.XNextEvent(display,event);if(event.getInt(0)!=35||event.getInt(32)!=opcode||event.getInt(36)!=17)continue;
                if(x.XGetEventData(display,event)==0)continue;
                try{Pointer data=event.getPointer(48);int length=data.getInt(64);Pointer mask=data.getPointer(72),values=data.getPointer(88);if(length<1||length>1024||mask==null||values==null)continue;int index=0;for(int axis=0;axis<2;axis++)if((mask.getByte(axis/8)&(1<<(axis%8)))!=0){double value=values.getDouble(8L*index++);if(Double.isFinite(value)){if(axis==0)dx+=value;else dy+=value;}}}
                finally{x.XFreeEventData(display,event);}
            }return new double[]{dx,dy};
        }
        public void close(){if(display!=null){x.XCloseDisplay(display);display=null;}}
    }
}
