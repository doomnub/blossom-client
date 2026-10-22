package dev.blossom.core;
import java.nio.file.*;
import java.io.*;
import java.util.*;
/** Versioned UTF-8 properties, with atomic replace and retained previous version. */
public final class ProfileStore {
    private final Path directory;
    public ProfileStore(Path directory) throws IOException { this.directory=directory; Files.createDirectories(directory); }
    private Path path(String name) {
        if(!name.matches("[a-zA-Z0-9_-]{1,48}")) throw new IllegalArgumentException("Invalid profile name");
        return directory.resolve(name+".properties");
    }
    public Properties load(String name) throws IOException {
        Properties p=new Properties(); Path file=path(name);
        if(!Files.exists(file)) return p;
        if(Files.size(file)>1024*1024) throw new IOException("Profile exceeds 1 MiB");
        try(Reader r=Files.newBufferedReader(file)) { p.load(r); }
        if(!"1".equals(p.getProperty("schema"))) throw new IOException("Unsupported profile schema");
        return p;
    }
    public void save(String name,Properties values) throws IOException {
        Path target=path(name), temp=Files.createTempFile(directory,"profile-",".tmp");
        Properties p=new Properties(); p.putAll(values); p.setProperty("schema","1");
        try {
            try(Writer w=Files.newBufferedWriter(temp)) { p.store(w,"Blossom profile"); }
            if(Files.exists(target)) Files.copy(target,directory.resolve(name+".backup"),StandardCopyOption.REPLACE_EXISTING);
            try { Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch(AtomicMoveNotSupportedException e) { Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
    public static float fraction(Properties p,String key,float fallback) {
        try { float v=Float.parseFloat(p.getProperty(key)); return Float.isFinite(v)?Math.max(0,Math.min(1,v)):fallback; }
        catch(RuntimeException e) { return fallback; }
    }
}
