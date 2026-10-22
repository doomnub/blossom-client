package dev.blossom;
import dev.blossom.core.*;
import java.nio.file.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
public final class CoreTest {
    private static int checks;
    private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        ClickWindow clicks=new ClickWindow();clicks.click(100);clicks.click(200);
        check(clicks.count(1099,1000)==2,"rolling timestamps");check(clicks.count(1100,1000)==1,"boundary expiration");
        check(clicks.count(10000,1000)==0,"idle expires clicks");
        for(int i=0;i<5000;i++)clicks.click(i);check(clicks.count(5000,10000)==4096,"bounded input storage");
        FrameStats f=new FrameStats(1000);for(int i=0;i<990;i++)f.record(10000000);for(int i=0;i<10;i++)f.record(50000000);
        double[] s=f.snapshot();check(Math.abs(s[0]-96.153846)<.001,"mean frame duration FPS");
        check(s[1]==20&&s[2]==20,"slow tail averages");check(s[3]==10,"median ms");
        Timeline t=new Timeline(new int[]{100,200,100},true);
        check(t.frame(0,true)==0,"first frame");check(t.frame(100000000,true)==1,"exact boundary");
        check(t.frame(400000000,true)==1,"ping pong reverse");check(t.frame(600000000,true)==0,"ping pong loop");
        check(t.frame(Long.MAX_VALUE,false)==1,"non looping holds last");
        boolean rejected=false;try{new Timeline(new int[]{0},false);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"bad timing");
        Path dir=Files.createTempDirectory("blossom-test");ProfileStore store=new ProfileStore(dir);
        Properties p=new Properties();p.setProperty("cape","pink");store.save("default",p);
        check(store.load("default").getProperty("cape").equals("pink"),"profile roundtrip");
        p.setProperty("cape","new");store.save("default",p);check(Files.exists(dir.resolve("default.backup")),"retained backup");
        p.setProperty("x","NaN");check(ProfileStore.fraction(p,"x",.5f)==.5f,"nonfinite rejected");
        rejected=false;try{store.load("../escape");}catch(IllegalArgumentException e){rejected=true;}check(rejected,"path traversal rejected");
        EventBus<Integer> bus=new EventBus<Integer>();int[] count={0};
        bus.subscribe(v->{throw new IllegalStateException();});AutoCloseable sub=bus.subscribe(v->count[0]+=v);bus.post(3);sub.close();bus.post(5);check(count[0]==3,"listener isolation and unsubscribe");
        Modules modules=new Modules();modules.add("a","A","HUD","test",true,0);check(modules.on("a"),"module enable");modules.get("a").enabled=false;check(!modules.on("a"),"module disable");
        Path image=dir.resolve("cape.png");ImageIO.write(new BufferedImage(64,96,BufferedImage.TYPE_INT_ARGB),"png",image.toFile());
        CapeFrames cape=CapeFrames.load(image);check(cape.images.length==3,"vertical sheet frames");
        ImageIO.write(new BufferedImage(63,32,BufferedImage.TYPE_INT_ARGB),"png",image.toFile());
        rejected=false;try{CapeFrames.load(image);}catch(java.io.IOException e){rejected=true;}check(rejected,"invalid cape dimensions");
        try(java.util.stream.Stream<Path> files=Files.walk(dir)){for(Path path:(Iterable<Path>)files.sorted(Comparator.reverseOrder())::iterator)Files.delete(path);}
        System.out.println("Core tests passed: "+checks);
    }
}
