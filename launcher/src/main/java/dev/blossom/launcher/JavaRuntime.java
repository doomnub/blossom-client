package dev.blossom.launcher;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.*;

/** App-private, pinned Java 8 installation; no registry, PATH or system Java changes. */
public final class JavaRuntime {
    public static JsonObject specification(String os,String arch)throws IOException {
        if(!(arch.equals("amd64")||arch.equals("x86_64")))throw new IOException("Automatic Java installation supports x86_64 Windows and Linux only.");
        try(InputStream in=JavaRuntime.class.getResourceAsStream("/runtimes.json")){
            JsonObject all=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
            if(!all.has(os))throw new IOException("Automatic Java installation supports Windows and Linux only.");
            return all.getAsJsonObject(os);
        }
    }
    public static Path ensure(Path root,Consumer<String> status)throws Exception {
        JsonObject spec=specification(GameInstall.os(),System.getProperty("os.arch"));
        String folder=spec.get("folder").getAsString(),hash=spec.get("sha256").getAsString();
        Path runtimes=root.resolve("runtimes"),target=Net.safeResolve(runtimes,folder),exe=target.resolve(spec.get("executable").getAsString());
        Path marker=target.resolve(".blossom-ready");
        if(Files.isRegularFile(marker)&&new String(Files.readAllBytes(marker),StandardCharsets.UTF_8).trim().equals(hash)){
            try{LaunchPlan.verifyJava8(exe);status.accept("Using "+spec.get("name").getAsString()+": "+exe);return exe;}
            catch(IOException e){status.accept("Managed Java needs repair: "+e.getMessage());}
        }
        status.accept("Installing "+spec.get("name").getAsString()+"...");
        Path archive=root.resolve("cache/runtimes").resolve(folder+".zip");
        Net.download(spec.get("url").getAsString(),archive,hash,0);
        Files.createDirectories(runtimes);Path stage=Files.createTempDirectory(runtimes,"install-");Path backup=null;
        try {
            extract(archive,stage,folder);Path installed=stage.resolve(folder),java=installed.resolve(spec.get("executable").getAsString());
            if(!GameInstall.os().equals("windows")){
                Path bin=installed.resolve("bin");try(java.util.stream.Stream<Path> entries=Files.list(bin)){for(Path file:(Iterable<Path>)entries::iterator)if(!file.toFile().setExecutable(true,true))throw new IOException("Cannot make Java executable: "+file);}
            }
            LaunchPlan.verifyJava8(java);
            if(Thread.currentThread().isInterrupted())throw new InterruptedException("Cancelled");
            Files.write(installed.resolve(".blossom-ready"),(hash+"\n").getBytes(StandardCharsets.UTF_8));
            if(Files.exists(target)){backup=runtimes.resolve(folder+"-previous-"+UUID.randomUUID());Files.move(target,backup);}
            try{Files.move(installed,target);}catch(IOException e){if(backup!=null)Files.move(backup,target);throw e;}
            status.accept("Installed "+spec.get("name").getAsString()+": "+exe);
            if(backup!=null)try{remove(backup);}catch(IOException e){status.accept("Previous runtime retained at "+backup);}
            return exe;
        }finally{remove(stage);}
    }
    static void extract(Path archive,Path stage,String folder)throws IOException {
        long total=0;int count=0;Set<Path> seen=new HashSet<Path>();
        try(ZipInputStream zip=new ZipInputStream(Files.newInputStream(archive))){ZipEntry entry;byte[] buffer=new byte[32768];
            while((entry=zip.getNextEntry())!=null){
                if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Cancelled");
                if(++count>10000)throw new IOException("Too many runtime archive entries");
                String name=entry.getName();if(!name.startsWith(folder+"/"))throw new IOException("Unexpected runtime archive root");
                Path file=Net.safeResolve(stage,name);if(!file.startsWith(stage.resolve(folder).toAbsolutePath().normalize()))throw new IOException("Runtime path escapes package");
                if(!seen.add(file))throw new IOException("Duplicate runtime archive entry");
                if(entry.isDirectory()){Files.createDirectories(file);continue;}
                Files.createDirectories(file.getParent());try(OutputStream out=Files.newOutputStream(file,StandardOpenOption.CREATE_NEW)){int n;while((n=zip.read(buffer))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Cancelled");total+=n;if(total>1024L*1024*1024)throw new IOException("Runtime exceeds size limit");out.write(buffer,0,n);}}
            }
        }
    }
    private static void remove(Path root)throws IOException{if(Files.exists(root))try(java.util.stream.Stream<Path> paths=Files.walk(root)){for(Path p:(Iterable<Path>)paths.sorted(Comparator.reverseOrder())::iterator)Files.deleteIfExists(p);}}
}
