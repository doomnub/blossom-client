package dev.blossom.core;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
/** Local-only staged import; no overwrite and no traversal outside the cape library. */
public final class CapeLibrary {
    private final Path root;
    public CapeLibrary(Path root)throws IOException {this.root=root.toAbsolutePath().normalize();Files.createDirectories(this.root);}
    public Path root(){return root;}
    public Path entry(String name) {
        if(name==null||name.isEmpty()||name.startsWith(".")||name.contains("/")||name.contains("\\"))throw new IllegalArgumentException("Invalid cape name");
        Path p=root.resolve(name).normalize();if(!p.getParent().equals(root))throw new IllegalArgumentException("Invalid cape path");
        return p;
    }
    public List<String> list()throws IOException {
        try(Stream<Path> stream=Files.list(root)){
            List<String> names=new ArrayList<String>();
            stream.filter(p->!p.getFileName().toString().startsWith(".")&&!Files.isSymbolicLink(p))
                .filter(p->Files.isDirectory(p)||p.toString().toLowerCase(Locale.ROOT).endsWith(".png"))
                .map(p->p.getFileName().toString()).sorted().limit(256).forEach(names::add);
            return Collections.unmodifiableList(names);
        }
    }
    public String importEntry(Path source)throws IOException {
        if(Files.isSymbolicLink(source))throw new IOException("Choose a regular file or directory");
        Path stage=Files.createTempDirectory(root,".import-");
        try {
            String raw=source.getFileName().toString().replaceAll("[^a-zA-Z0-9._-]","_");
            if(raw.isEmpty()||raw.startsWith("."))raw="imported-cape";
            if(raw.length()>60)raw=raw.substring(0,60);
            boolean directory=Files.isDirectory(source);
            if(!directory&&!raw.toLowerCase(Locale.ROOT).endsWith(".png"))raw+=".png";
            Path payload=stage.resolve(directory?"frames":"cape.png");
            if(directory){
                Files.createDirectory(payload);long bytes=0;int count=0;
                try(Stream<Path> stream=Files.list(source)) {
                    for(Path file:(Iterable<Path>)stream::iterator){
                        String name=file.getFileName().toString();
                        if(!name.toLowerCase(Locale.ROOT).endsWith(".png")&&!name.equals("animation.properties"))continue;
                        if(++count>257)throw new IOException("Too many frame files");
                        bytes+=copyBounded(file,payload.resolve(name));
                        if(bytes>64L*1024*1024)throw new IOException("Animation exceeds 64 MiB encoded data");
                    }
                }
            }else copyBounded(source,payload);
            CapeFrames.load(payload); // validate the staged snapshot, never the mutable source
            String name=raw;int suffix=1;
            while(Files.exists(entry(name))) {
                int dot=raw.toLowerCase(Locale.ROOT).endsWith(".png")?raw.length()-4:raw.length();
                name=raw.substring(0,dot)+"-"+(suffix++)+raw.substring(dot);
            }
            Files.move(payload,entry(name));return name;
        }finally{removeStage(stage);}
    }
    private static long copyBounded(Path from,Path to)throws IOException {
        if(!Files.isRegularFile(from,LinkOption.NOFOLLOW_LINKS))throw new IOException("Not a regular file");
        long limit=from.getFileName().toString().equals("animation.properties")?65536:16L*1024*1024;
        if(Files.size(from)>limit)throw new IOException("Input file too large");
        long total=0;byte[] buffer=new byte[8192];
        try(InputStream in=Files.newInputStream(from);OutputStream out=Files.newOutputStream(to,StandardOpenOption.CREATE_NEW)) {
            int n;while((n=in.read(buffer))!=-1){total+=n;if(total>limit)throw new IOException("Input grew beyond size limit");out.write(buffer,0,n);}
        }
        return total;
    }
    private static void removeStage(Path stage)throws IOException {
        try(Stream<Path> stream=Files.walk(stage)){for(Path p:(Iterable<Path>)stream.sorted(Comparator.reverseOrder())::iterator)Files.deleteIfExists(p);}
    }
    public String rename(String old,String name)throws IOException {
        if(!name.matches("[a-zA-Z0-9_-]{1,48}"))throw new IllegalArgumentException("Use 1–48 letters, digits, _ or -");
        Path source=entry(old);String target=name+(Files.isDirectory(source)?"":".png");
        Files.move(source,entry(target));return target;
    }
    public void trash(String name)throws IOException {
        Path trash=root.resolve(".trash");Files.createDirectories(trash);
        Files.move(entry(name),trash.resolve(UUID.randomUUID()+"-"+name));
    }
}
