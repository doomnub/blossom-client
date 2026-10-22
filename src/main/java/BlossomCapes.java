import dev.blossom.core.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
/** Decode/import off-thread; stage GL uploads and swap atomically when all frames are ready. */
public final class BlossomCapes {
    public final CapeLibrary library;
    private final ExecutorService worker=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"Blossom cape worker");t.setDaemon(true);return t;});
    private final ConcurrentLinkedQueue<Runnable> completions=new ConcurrentLinkedQueue<Runnable>();
    private Future<CapeFrames> pending;
    private CapeFrames frames,staged;
    private jy[] textures=new jy[0], stagedTextures=new jy[0];
    private int uploadIndex;
    private Timeline timeline;
    public final PlaybackClock clock=new PlaybackClock(System.nanoTime());
    public String selected="", mode="default";
    public boolean loop=true;
    public int fps=0, pingPongMode=0; // 0=file, 1=on, 2=off
    public volatile List<String> entries=Collections.emptyList();
    public volatile String status="No custom cape selected";
    public BlossomCapes(Path folder)throws java.io.IOException {library=new CapeLibrary(folder);refresh();}
    public void refresh(){worker.execute(()->{try{entries=library.list();}catch(Exception e){status="Cannot read cape library: "+e.getMessage();}});}
    public void select(String name) {
        if(pending!=null){pending.cancel(true);pending=null;}
        discardStaging();
        if(name.isEmpty()){selected="";mode="default";release();status="Default Minecraft cape";return;}
        final Path file;
        try{file=library.entry(name);}catch(IllegalArgumentException e){status=e.getMessage();return;}
        selected=name;mode="custom";pending=worker.submit(()->CapeFrames.load(file));status="Decoding "+name;
    }
    public void cycle(){int i=entries.indexOf(selected)+1;select(i>=entries.size()?"":entries.get(i));}
    public void uploadReady() {
        Runnable task;while((task=completions.poll())!=null)task.run();
        if(pending!=null&&pending.isDone()) {
            Future<CapeFrames> result=pending;pending=null;
            try{staged=result.get();stagedTextures=new jy[staged.images.length];uploadIndex=0;}
            catch(Exception e){status="Cape rejected: "+(e.getCause()==null?e.getMessage():e.getCause().getMessage());}
        }
        if(staged==null)return;
        try {
            // At most two uploads per client tick; previous cape stays visible until complete.
            for(int n=0;n<2&&uploadIndex<stagedTextures.length;n++,uploadIndex++)
                stagedTextures[uploadIndex]=ave.A().P().a("blossom/cape_"+uploadIndex,new blz(staged.images[uploadIndex]));
            status="Uploading "+uploadIndex+" / "+stagedTextures.length;
            if(uploadIndex==stagedTextures.length) {
                release();frames=staged;textures=stagedTextures;staged=null;stagedTextures=new jy[0];
                rebuildTimeline();clock.restart(System.nanoTime());status=selected+" ("+textures.length+" frames)";
            }
        }catch(RuntimeException e){discardStaging();status="Cape upload failed: "+e.getMessage();}
    }
    public void rebuildTimeline(){if(frames!=null)timeline=frames.timeline(pingPongMode==0?frames.pingPong:pingPongMode==1,fps);}
    public jy preview(){return timeline==null?null:textures[timeline.frame(clock.elapsed(System.nanoTime()),loop)];}
    public jy current(){return mode.equals("custom")?preview():null;}
    public boolean hidden(){return mode.equals("none");}
    public int frameCount(){return textures.length;}
    public int frameIndex(){return timeline==null?0:timeline.frame(clock.elapsed(System.nanoTime()),loop);}
    private static void delete(jy[] items){for(jy t:items)if(t!=null)ave.A().P().c(t);}
    private void release(){delete(textures);textures=new jy[0];frames=null;timeline=null;}
    private void discardStaging(){delete(stagedTextures);stagedTextures=new jy[0];staged=null;}
    public void importDialog() {
        SwingUtilities.invokeLater(()->{
            try {
                JFileChooser chooser=new JFileChooser();chooser.setDialogTitle("Import cape PNG or frame directory");chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
                    Path file=chooser.getSelectedFile().toPath();status="Importing "+file.getFileName();
                    worker.execute(()->{try{String name=library.importEntry(file);entries=library.list();completions.add(()->select(name));}
                        catch(Exception e){status="Import failed: "+e.getMessage();}});
                }
            }catch(Exception e){status="File chooser unavailable; use cape folder";}
        });
    }
    public void rename(String newName) {
        final String old=selected;
        worker.execute(()->{try{String name=library.rename(old,newName);entries=library.list();completions.add(()->select(name));}
            catch(Exception e){status="Rename failed: "+e.getMessage();}});
    }
    public void remove() {
        final String old=selected;
        worker.execute(()->{try{library.trash(old);entries=library.list();completions.add(()->select(""));}
            catch(Exception e){status="Remove failed: "+e.getMessage();}});
    }
    public void openFolder() {
        worker.execute(()->{try{if(java.awt.Desktop.isDesktopSupported())java.awt.Desktop.getDesktop().open(library.root().toFile());else status="Cape folder: "+library.root();}
            catch(Exception e){status="Cape folder: "+library.root();}});
    }
    public void load(Properties p) {
        select(p.getProperty("cape.selected",""));
        String requested=p.getProperty("cape.mode",selected.isEmpty()?"default":"custom");
        mode=Arrays.asList("default","none","custom").contains(requested)?requested:"default";
        fps=ClientSettings.integer(p,"cape.fps",0,0,100);pingPongMode=ClientSettings.integer(p,"cape.pingPongMode",0,0,2);
        loop=Boolean.parseBoolean(p.getProperty("cape.loop","true"));
        clock.speed(ClientSettings.number(p,"cape.speed",1,.1f,4),System.nanoTime());
        clock.paused(Boolean.parseBoolean(p.getProperty("cape.paused","false")),System.nanoTime());
    }
    public void save(Properties p) {
        p.setProperty("cape.selected",selected);p.setProperty("cape.mode",mode);p.setProperty("cape.fps",Integer.toString(fps));
        p.setProperty("cape.pingPongMode",Integer.toString(pingPongMode));p.setProperty("cape.loop",Boolean.toString(loop));
        p.setProperty("cape.speed",Double.toString(clock.speed()));p.setProperty("cape.paused",Boolean.toString(clock.paused()));
    }
}
