package dev.blossom.launcher;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.zip.*;

/** Installs only Mojang's pinned 1.8.9 manifest and checksum-addressed dependencies. */
public final class GameInstall {
    private static final String META="https://piston-meta.mojang.com/v1/packages/d546f1707a3f2b7d034eece5ea2e311eda875787/1.8.9.json";
    public static final class Installed {
        public final List<Path> classpath;public final Path assets,natives,logging;public final String assetIndex;
        Installed(List<Path> cp,Path assets,Path natives,Path logging,String index){this.classpath=cp;this.assets=assets;this.natives=natives;this.logging=logging;assetIndex=index;}
    }
    public static String os(){String value=System.getProperty("os.name").toLowerCase(Locale.ROOT);return value.startsWith("windows")?"windows":value.contains("linux")?"linux":"unsupported";}
    public static boolean allowed(JsonObject library,String os,String arch,String version) {
        if(!library.has("rules"))return true;boolean allowed=false;
        for(JsonElement item:library.getAsJsonArray("rules")) {
            JsonObject rule=item.getAsJsonObject();boolean match=!rule.has("features");
            if(rule.has("os")){JsonObject filter=rule.getAsJsonObject("os");if(filter.has("name")&&!os.equals(filter.get("name").getAsString()))match=false;
                if(filter.has("arch")&&!arch.matches(filter.get("arch").getAsString()))match=false;if(filter.has("version")&&!version.matches(filter.get("version").getAsString()))match=false;}
            if(match)allowed=rule.get("action").getAsString().equals("allow");
        }return allowed;
    }
    public static Installed install(Path cache,Path game,Consumer<String> progress)throws Exception {
        String os=os(),arch=System.getProperty("os.arch");
        if(os.equals("unsupported")||!(arch.equals("amd64")||arch.equals("x86_64")))throw new IOException("This build supports 64-bit Windows and Linux on x86_64.");
        Files.createDirectories(game);Path manifest=cache.resolve("versions/1.8.9.json");
        progress.accept("Checking Minecraft 1.8.9 files...");Net.download(META,manifest,"d546f1707a3f2b7d034eece5ea2e311eda875787",0);
        JsonObject meta=read(manifest);Path client=cache.resolve("versions/1.8.9.jar");artifact(meta.getAsJsonObject("downloads").getAsJsonObject("client"),client);
        List<Path> cp=new ArrayList<Path>();Path libraries=cache.resolve("libraries"),natives=game.resolve(".blossom-natives");Files.createDirectories(natives);
        for(JsonElement item:meta.getAsJsonArray("libraries")) {
            if(Thread.currentThread().isInterrupted())throw new InterruptedException("Cancelled");
            JsonObject lib=item.getAsJsonObject();if(!allowed(lib,os,arch,System.getProperty("os.version")))continue;
            String name=lib.get("name").getAsString();if(name.startsWith("org.apache.logging.log4j:"))continue;
            JsonObject downloads=lib.getAsJsonObject("downloads");
            if(downloads.has("artifact")){JsonObject a=downloads.getAsJsonObject("artifact");Path file=Net.safeResolve(libraries,a.get("path").getAsString());artifact(a,file);cp.add(file);}
            if(lib.has("natives")&&lib.getAsJsonObject("natives").has(os)) {
                String classifier=lib.getAsJsonObject("natives").get(os).getAsString().replace("${arch}","64");
                JsonObject a=downloads.getAsJsonObject("classifiers").getAsJsonObject(classifier);Path file=Net.safeResolve(libraries,a.get("path").getAsString());artifact(a,file);extract(file,natives);
            }
        }
        // Replace vulnerable historical logging libraries instead of launching 2.0-beta9.
        String[] logNames={"log4j-api","log4j-core"};
        String[] hashes={"6c80ea65e41f7858f7e15cb390c98c36167c7b3a","c194a13e18b0a36014e867d2d323b989bf212151"};
        for(int i=0;i<logNames.length;i++){String path="org/apache/logging/log4j/"+logNames[i]+"/2.25.5/"+logNames[i]+"-2.25.5.jar";Path file=Net.safeResolve(libraries,path);Net.download("https://repo.maven.apache.org/maven2/"+path,file,hashes[i],0);cp.add(file);}
        cp.add(client);
        JsonObject assetInfo=meta.getAsJsonObject("assetIndex");String index=assetInfo.get("id").getAsString();Path assets=cache.resolve("assets"),indexFile=Net.safeResolve(assets,"indexes/"+index+".json");artifact(assetInfo,indexFile);
        JsonObject assetData=read(indexFile);List<Map.Entry<String,JsonElement>> objects=new ArrayList<Map.Entry<String,JsonElement>>(assetData.getAsJsonObject("objects").entrySet());
        ExecutorService pool=Executors.newFixedThreadPool(6);CompletionService<Void> completion=new ExecutorCompletionService<Void>(pool);
        try {
            for(Map.Entry<String,JsonElement> entry:objects)completion.submit(()->{
                JsonObject object=entry.getValue().getAsJsonObject();String hash=object.get("hash").getAsString();if(!hash.matches("[a-f0-9]{40}"))throw new IOException("Invalid asset hash");
                Path target=Net.safeResolve(assets,"objects/"+hash.substring(0,2)+"/"+hash);Net.download("https://resources.download.minecraft.net/"+hash.substring(0,2)+"/"+hash,target,hash,object.get("size").getAsLong());
                if(assetData.has("virtual")&&assetData.get("virtual").getAsBoolean())copyAsset(target,Net.safeResolve(assets.resolve("virtual").resolve(index),entry.getKey()));
                if(assetData.has("map_to_resources")&&assetData.get("map_to_resources").getAsBoolean())copyAsset(target,Net.safeResolve(game.resolve("resources"),entry.getKey()));return null;
            });
            for(int i=1;i<=objects.size();i++){completion.take().get();if(i%25==0||i==objects.size())progress.accept("Checking assets: "+i+" / "+objects.size());}
        }finally{pool.shutdownNow();}
        JsonObject logging=meta.getAsJsonObject("logging").getAsJsonObject("client").getAsJsonObject("file");Path logConfig=Net.safeResolve(cache,"logging/"+logging.get("id").getAsString());artifact(logging,logConfig);
        return new Installed(cp,assets,natives,logConfig,index);
    }
    private static void copyAsset(Path source,Path target)throws IOException{Files.createDirectories(target.getParent());Files.copy(source,target,StandardCopyOption.REPLACE_EXISTING);}
    private static JsonObject read(Path path)throws IOException{try(Reader r=Files.newBufferedReader(path)){return JsonParser.parseReader(r).getAsJsonObject();}}
    private static void artifact(JsonObject artifact,Path target)throws IOException{Net.download(artifact.get("url").getAsString(),target,artifact.get("sha1").getAsString(),artifact.has("size")?artifact.get("size").getAsLong():0);}
    public static void extract(Path archive,Path root)throws IOException {
        long total=0;
        try(ZipInputStream zip=new ZipInputStream(Files.newInputStream(archive))){ZipEntry entry;byte[] buffer=new byte[8192];
            while((entry=zip.getNextEntry())!=null){Path target=Net.safeResolve(root,entry.getName());if(entry.isDirectory()||entry.getName().startsWith("META-INF/"))continue;
                String name=entry.getName().toLowerCase(Locale.ROOT);if(!name.endsWith(".so")&&!name.endsWith(".dll")&&!name.endsWith(".dylib"))continue;
                Files.createDirectories(target.getParent());try(OutputStream out=Files.newOutputStream(target)){int n;while((n=zip.read(buffer))!=-1){total+=n;if(total>128L*1024*1024)throw new IOException("Native archive exceeds size limit");out.write(buffer,0,n);}}
            }
        }
    }
}
