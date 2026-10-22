import dev.blossom.core.CapeFrames;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
/** Decode on one worker; upload/delete textures only on Minecraft's render thread. */
public final class BlossomCapes {
    private final Path folder;
    private final ExecutorService worker=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"Blossom cape decoder");t.setDaemon(true);return t;});
    private Future<CapeFrames> pending;
    private CapeFrames frames;
    private jy[] textures=new jy[0];
    private long started;
    public String selected="";
    public volatile String status="No custom cape selected";
    public BlossomCapes(Path folder) throws java.io.IOException {this.folder=folder;Files.createDirectories(folder);}
    public void select(String name) {
        if(pending!=null) pending.cancel(true);
        selected=name;
        if(name.isEmpty()) {pending=null;release();status="Default Minecraft cape";return;}
        Path file=folder.resolve(name).normalize();
        if(!file.getParent().equals(folder)||name.contains("/")||name.contains("\\")) {status="Invalid cape name";return;}
        pending=worker.submit(()->CapeFrames.load(file));status="Loading "+name;
    }
    public void cycle() {
        try(java.util.stream.Stream<Path> files=Files.list(folder)) {
            String[] names=files.filter(f->Files.isDirectory(f)||f.toString().toLowerCase(Locale.ROOT).endsWith(".png"))
                .map(f->f.getFileName().toString()).sorted().limit(256).toArray(String[]::new);
            List<String> choices=new ArrayList<String>();choices.add("");choices.addAll(Arrays.asList(names));
            select(choices.get((choices.indexOf(selected)+1)%choices.size()));
        } catch(java.io.IOException e) {status="Cannot read cape folder";}
    }
    public void uploadReady() {
        if(pending==null||!pending.isDone()) return;
        Future<CapeFrames> done=pending;pending=null;
        try {
            CapeFrames next=done.get(); release();
            jy[] uploaded=new jy[next.images.length];
            try {
                for(int i=0;i<uploaded.length;i++) uploaded[i]=ave.A().P().a("blossom/cape_"+i,new blz(next.images[i]));
            } catch(RuntimeException e) {
                for(jy texture:uploaded) if(texture!=null) ave.A().P().c(texture);
                throw e;
            }
            textures=uploaded;frames=next;started=System.nanoTime();status=selected+" ("+textures.length+" frames)";
        } catch(Exception e) {status="Cape rejected: "+(e.getCause()==null?e.getMessage():e.getCause().getMessage());}
    }
    public jy current() {return frames==null?null:textures[frames.timeline.frame(System.nanoTime()-started,true)];}
    private void release() {for(jy texture:textures) ave.A().P().c(texture);textures=new jy[0];frames=null;}
    public void openFolder() {
        worker.execute(()->{try {if(java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(folder.toFile());else status="Cape folder: "+folder;}
            catch(Exception e){status="Cape folder: "+folder;}});
    }
}
