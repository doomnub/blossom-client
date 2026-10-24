import dev.blossom.core.LatencyState;
/** Resolves the player from the current connection, never an earlier world's player info. */
public final class BlossomPing {
    private static final LatencyState state=new LatencyState();
    private static boolean active;
    public static void tick(){
        boolean needed=BlossomHooks.modules.on("ping")||BlossomHooks.modules.on("network");
        if(!needed){if(active)state.clear();active=false;return;}active=true;
        ave mc=ave.A();bcy connection=mc.u();
        if(connection==null||mc.h==null){state.clear();return;}
        // Login profile belongs to this NetHandler, including offline/proxy UUID changes.
        bdc info=connection.a(connection.e().getId());
        if(info==null)info=connection.a(connection.e().getName());
        state.update(connection,mc.E(),info==null?-1:info.c());
    }
    public static String text(){return state.text();}
    public static int value(){return state.value();}
}
