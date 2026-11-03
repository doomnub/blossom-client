package dev.blossom.core;
import java.util.IdentityHashMap;
/** Bounded identity cache. Epoch changes discard every key and value, including old components. */
public final class TickTextCache {
    private final IdentityHashMap<Object,String> entries=new IdentityHashMap<Object,String>();
    private int epoch,characters;
    public void begin(int tick){if(epoch!=tick){clear();epoch=tick;}}
    public String get(Object key){return entries.get(key);}
    public void put(Object key,String text){
        if(entries.containsKey(key)||entries.size()>=128||text.length()>8192||characters+text.length()>65536)return;
        entries.put(key,text);characters+=text.length();
    }
    public void clear(){entries.clear();characters=0;}
    public int size(){return entries.size();}
}
