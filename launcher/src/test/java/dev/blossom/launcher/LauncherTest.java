package dev.blossom.launcher;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
public final class LauncherTest {
    static int checks;
    static void check(boolean ok){checks++;if(!ok)throw new AssertionError("Check "+checks);}
    interface Action{void run()throws Exception;}
    static void rejects(Action a)throws Exception{boolean failed=false;try{a.run();}catch(IOException|IllegalArgumentException e){failed=true;}check(failed);}
    public static void main(String[] args)throws Exception{
        Path root=Files.createTempDirectory("blossom-launcher-test-");
        try{
            check(Net.safeResolve(root,"libraries/a.jar").startsWith(root));
            rejects(()->Net.safeResolve(root,"../../escape.dll"));rejects(()->Net.safeResolve(root,"C:\\escape.dll"));rejects(()->Net.safeResolve(root,"/escape.dll"));
            rejects(()->Net.readLimited(new ByteArrayInputStream(new byte[20]),19));
            check(Net.readLimited(new ByteArrayInputStream(new byte[20]),20).length==20);
            Path zip=root.resolve("bad.zip");try(ZipOutputStream out=new ZipOutputStream(Files.newOutputStream(zip))){out.putNextEntry(new ZipEntry("../escape.dll"));out.write(1);out.closeEntry();}
            rejects(()->GameInstall.extract(zip,root.resolve("natives")));check(!Files.exists(root.resolve("escape.dll")));
            JsonObject lib=JsonParser.parseString("{\"rules\":[{\"action\":\"allow\"},{\"action\":\"disallow\",\"os\":{\"name\":\"windows\"}}]}").getAsJsonObject();
            check(GameInstall.allowed(lib,"linux","amd64","6"));check(!GameInstall.allowed(lib,"windows","amd64","10"));
            check(!GameInstall.allowed(JsonParser.parseString("{\"rules\":[]}").getAsJsonObject(),"linux","amd64","6"));
            MicrosoftLogin.Session session=new MicrosoftLogin.Session("Player","0123456789abcdef0123456789abcdef","secret-session",System.currentTimeMillis()+3600000);
            GameInstall.Installed installed=new GameInstall.Installed(Arrays.asList(root.resolve("space path/game.jar")),root,root,root.resolve("log.xml"),"1.8");
            List<String> command=LaunchPlan.command(root.resolve("java"),root.resolve("agent with spaces.jar"),root,installed,session,4096,1280,720);
            check(!command.toString().contains(session.token));check(command.contains("@BLOSSOM_SESSION@"));check(command.contains("-javaagent:"+root.resolve("agent with spaces.jar")));
            check(LaunchPlan.redact("token=secret-session",session).equals("token=[session redacted]"));
            rejects(()->LaunchPlan.command(root,root,root,installed,session,128,1280,720));
            MicrosoftLogin.Session expired=new MicrosoftLogin.Session("P","id","secret",0);rejects(()->LaunchPlan.command(root,root,root,installed,expired,4096,1280,720));
            rejects(()->MicrosoftLogin.login("not-an-app-id",c->{},s->{}));
            Path file=root.resolve("hash.txt");Files.write(file,"abc".getBytes("UTF-8"));check(Net.hash(file,"SHA-256").equals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"));
            rejects(()->Net.download("https://example.com/a",file,"0",0));
            rejects(()->Net.download("https://libraries.minecraft.net/a",file,"0",0));
            MicrosoftLogin.Session offline=MicrosoftLogin.Session.offline("Notch");
            check(offline.offline&&offline.valid());check(offline.uuid.equals("b50ad385829d3141a2167e7d7539ba7f"));
            check(offline.uuid.equals(MicrosoftLogin.Session.offline("Notch").uuid));
            check(!offline.uuid.equals(MicrosoftLogin.Session.offline("notch").uuid));
            rejects(()->MicrosoftLogin.Session.offline(""));rejects(()->MicrosoftLogin.Session.offline("name with space"));rejects(()->MicrosoftLogin.Session.offline("12345678901234567"));
            List<String> offlineCommand=LaunchPlan.command(root,root,root,installed,offline,4096,1280,720);
            check(offlineCommand.get(offlineCommand.indexOf("--userType")+1).equals("legacy"));
            check(offlineCommand.get(offlineCommand.indexOf("--username")+1).equals("Notch"));
            check(LaunchPlan.redact("1000 blocks",offline).equals("1000 blocks"));
            Net.download("https://libraries.minecraft.net/not-requested",file,Net.hash(file,"SHA-256"),3);check(Files.size(file)==3);
            System.out.println("Launcher tests passed: "+checks);
        }finally{try(java.util.stream.Stream<Path> paths=Files.walk(root)){for(Path p:(Iterable<Path>)paths.sorted(Comparator.reverseOrder())::iterator)Files.delete(p);}}
    }
}
