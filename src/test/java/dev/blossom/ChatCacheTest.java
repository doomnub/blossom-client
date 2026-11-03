package dev.blossom;
import dev.blossom.core.TickTextCache;
public final class ChatCacheTest {
    public static void run(){
        TickTextCache c=new TickTextCache();Object a=new String("same"),b=new String("same");c.begin(10);c.put(a,"formatted");
        if(!"formatted".equals(c.get(a))||c.get(b)!=null)throw new AssertionError("Cache must use identity");
        c.begin(10);if(c.get(a)==null)throw new AssertionError("Same tick discarded");
        c.begin(11);if(c.get(a)!=null||c.size()!=0)throw new AssertionError("Tick retained references");
        for(int i=0;i<1000;i++)c.put(new Object(),"entry");
        if(c.size()!=128)throw new AssertionError("Entry bound");
        c.clear();String large=new String(new char[8192]);for(int i=0;i<100;i++)c.put(new Object(),large);
        if(c.size()!=8)throw new AssertionError("Character bound");
        c.clear();c.put(a,large+"x");if(c.size()!=0)throw new AssertionError("Oversized entry retained");
        System.out.println("Chat text cache identity, tick expiration and hard retention limits passed");
    }
}
