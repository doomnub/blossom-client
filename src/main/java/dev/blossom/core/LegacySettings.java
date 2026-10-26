package dev.blossom.core;
import java.util.Properties;
/** Independent, preset-backed cosmetic switches. Always-swing is opt-in. */
public final class LegacySettings {
    public static final String[] KEYS={"blocking","blockHit","sneak","rod","bow","equip","eating","holding","damage","glint","alwaysSwing"};
    public static final String[] LABELS={"Sword blocking","Block + swing","Smooth sneak camera","Fishing rod position","Bow draw","Item switching","Eating / drinking","Held item position","Damage tint on armor","Classic item glint","Always swing (visual only)"};
    public final boolean[] enabled=new boolean[KEYS.length];
    public LegacySettings(){load(new Properties());}
    public void load(Properties p){for(int i=0;i<KEYS.length;i++)enabled[i]=Boolean.parseBoolean(p.getProperty("legacy."+KEYS[i],Boolean.toString(i!=10)));}
    public void save(Properties p){for(int i=0;i<KEYS.length;i++)p.setProperty("legacy."+KEYS[i],Boolean.toString(enabled[i]));}
}
