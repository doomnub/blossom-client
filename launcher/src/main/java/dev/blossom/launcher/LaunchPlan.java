package dev.blossom.launcher;
import java.io.*;
import java.nio.file.*;
import java.util.*;
public final class LaunchPlan {
    public static List<String> command(Path java,Path agent,Path game,GameInstall.Installed install,MicrosoftLogin.Session session,int memory,int width,int height) {
        if(!session.valid())throw new IllegalArgumentException("Sign in again; the session has expired.");
        if(memory<1024||memory>16384||width<640||width>7680||height<360||height>4320)throw new IllegalArgumentException("Launch settings outside supported bounds");
        List<String> cp=new ArrayList<String>();for(Path file:install.classpath)cp.add(file.toAbsolutePath().toString());cp.add(agent.toAbsolutePath().toString());
        List<String> command=new ArrayList<String>(Arrays.asList(java.toString(),"-Xms512M","-Xmx"+memory+"M","-Djava.library.path="+install.natives.toAbsolutePath(),
            "-Dlog4j.configurationFile="+install.logging.toAbsolutePath(),"-javaagent:"+agent.toAbsolutePath(),"-cp",String.join(File.pathSeparator,cp),"dev.blossom.agent.GameBootstrap",
            "--username",session.username,"--version","1.8.9","--gameDir",game.toAbsolutePath().toString(),"--assetsDir",install.assets.toAbsolutePath().toString(),"--assetIndex",install.assetIndex,
            "--uuid",session.uuid,"--accessToken","@BLOSSOM_SESSION@","--userProperties","{}","--userType",session.offline?"legacy":"msa","--width",Integer.toString(width),"--height",Integer.toString(height)));
        return Collections.unmodifiableList(command);
    }
    public static void verifyJava8(Path executable)throws Exception {
        if(!Files.isRegularFile(executable))throw new IOException("Select the Java 8 java executable in Settings.");
        Process p=new ProcessBuilder(executable.toString(),"-XshowSettings:properties","-version").redirectErrorStream(true).start();
        String output=versionOutput(p);
        if(p.exitValue()!=0||!output.contains("\"1.8.")||!output.matches("(?s).*sun\\.arch\\.data\\.model\\s*=\\s*64.*"))throw new IOException("Minecraft 1.8.9 requires a 64-bit Java 8 runtime in this launcher.");
    }
    static String versionOutput(Process process)throws Exception {
        java.util.concurrent.FutureTask<byte[]> reader=new java.util.concurrent.FutureTask<byte[]>(()->Net.readLimited(process.getInputStream(),65536));
        Thread drain=new Thread(reader,"Blossom Java version output");drain.setDaemon(true);drain.start();
        try {
            if(!process.waitFor(30,java.util.concurrent.TimeUnit.SECONDS))throw new IOException("Java version check timed out");
            return new String(reader.get(5,java.util.concurrent.TimeUnit.SECONDS),java.nio.charset.StandardCharsets.UTF_8);
        } finally {if(process.isAlive())process.destroyForcibly();reader.cancel(true);process.getInputStream().close();}
    }
    public static Process start(List<String> command,Path game,MicrosoftLogin.Session session)throws IOException {
        Process process=new ProcessBuilder(command).directory(game.toFile()).redirectErrorStream(true).start();
        try(DataOutputStream out=new DataOutputStream(process.getOutputStream())){out.writeUTF(session.token);out.flush();}
        catch(IOException e){process.destroy();throw e;}return process;
    }
    public static String redact(String line,MicrosoftLogin.Session session){return session.offline?line:line.replace(session.token,"[session redacted]");}
}
