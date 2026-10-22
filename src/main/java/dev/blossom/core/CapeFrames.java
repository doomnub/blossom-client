package dev.blossom.core;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
/** PNG frames only; header limits are enforced BEFORE image allocation. */
public final class CapeFrames {
    public final BufferedImage[] images;
    public final Timeline timeline;
    public final boolean pingPong;
    private final int[] durations;
    public Timeline timeline(boolean reverse,int fps){
        int[] timing=durations.clone();
        if(fps!=0){if(fps<1||fps>100)throw new IllegalArgumentException("FPS must be 1–100");Arrays.fill(timing,1000/fps);}
        return new Timeline(timing,reverse);
    }
    private CapeFrames(BufferedImage[] images,int[] durations,boolean pingPong) {
        this.images=images;this.durations=durations.clone();this.pingPong=pingPong; timeline=new Timeline(durations,pingPong);
    }
    public static BufferedImage png(Path file) throws IOException {
        if(Files.size(file)>16*1024*1024) throw new IOException("PNG exceeds 16 MiB");
        try(ImageInputStream input=ImageIO.createImageInputStream(file.toFile())) {
            if(input==null) throw new IOException("Cannot open image");
            Iterator<ImageReader> readers=ImageIO.getImageReaders(input);
            if(!readers.hasNext()) throw new IOException("Not an image");
            ImageReader reader=readers.next();
            try {
                if(!reader.getFormatName().equalsIgnoreCase("png")) throw new IOException("Only PNG is supported");
                reader.setInput(input,true,true);
                int w=reader.getWidth(0), h=reader.getHeight(0);
                if(w<1||h<1||w>4096||h>4096||(long)w*h>4194304) throw new IOException("Image dimensions exceed limits");
                return reader.read(0);
            } finally { reader.dispose(); }
        }
    }
    public static CapeFrames load(Path selected) throws IOException {
        List<BufferedImage> frames=new ArrayList<BufferedImage>(); Properties p=new Properties();
        if(Files.isDirectory(selected)) {
            Path config=selected.resolve("animation.properties");
            if(Files.exists(config)) {
                if(Files.size(config)>65536) throw new IOException("Animation config too large");
                try(Reader r=Files.newBufferedReader(config)) { p.load(r); }
            }
            try(java.util.stream.Stream<Path> stream=Files.list(selected)) {
                Path[] files=stream.filter(f->f.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png")).sorted().limit(257).toArray(Path[]::new);
                if(files.length<1||files.length>256) throw new IOException("Use 1-256 PNG frames");
                long pixels=0;
                for(Path file:files) {
                    if(Thread.currentThread().isInterrupted())throw new IOException("Cancelled");
                    BufferedImage image=png(file); validateFrame(image);
                    pixels+=(long)image.getWidth()*image.getHeight();
                    if(pixels>8388608) throw new IOException("Animation exceeds 32 MiB decoded pixels");
                    frames.add(image);
                }
            }
        } else {
            BufferedImage sheet=png(selected);
            // Infer static 2:1, vertical strips, or horizontal strips by aspect ratio.
            int w=sheet.getWidth(),h=sheet.getHeight();
            boolean horizontal=w>h*2;
            int fw=horizontal?h*2:w,fh=horizontal?h:w/2;
            if(fw<64||fw%64!=0||fw>1024||fh!=fw/2||w%fw!=0||h%fh!=0)throw new IOException("Use 64x32 multiples or aligned horizontal/vertical strips");
            int count=horizontal?w/fw:h/fh;
            if(count<1||count>256)throw new IOException("Use 1–256 frames");
            for(int i=0;i<count;i++)frames.add(sheet.getSubimage(horizontal?i*fw:0,horizontal?0:i*fh,fw,fh));
        }
        int w=frames.get(0).getWidth(),h=frames.get(0).getHeight();
        for(BufferedImage f:frames) if(f.getWidth()!=w||f.getHeight()!=h) throw new IOException("Frame sizes differ");
        int[] durations=new int[frames.size()];
        try {
            int fps=Integer.parseInt(p.getProperty("fps","20"));
            if(fps<1||fps>100) throw new IllegalArgumentException("FPS must be 1-100");
            for(int i=0;i<durations.length;i++) durations[i]=Integer.parseInt(p.getProperty("duration."+i,Integer.toString(1000/fps)));
            return new CapeFrames(frames.toArray(new BufferedImage[0]),durations,Boolean.parseBoolean(p.getProperty("pingPong","false")));
        } catch(IllegalArgumentException e) { throw new IOException("Invalid animation timing",e); }
    }
    private static void validateFrame(BufferedImage f) throws IOException {
        if(f.getWidth()%64!=0||f.getWidth()>1024||f.getHeight()!=f.getWidth()/2) throw new IOException("Frame must be 64x32 or an integer multiple up to 1024x512");
    }
}
