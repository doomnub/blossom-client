package dev.blossom.agent;
import java.io.DataInputStream;
/** Keeps the game session token out of the OS process command line. */
public final class GameBootstrap {
    public static void main(String[] args)throws Exception {
        String token=new DataInputStream(System.in).readUTF();
        if(token.isEmpty()||token.length()>16000)throw new IllegalArgumentException("Invalid session token");
        int substitutions=0;
        for(int i=0;i<args.length;i++)if(args[i].equals("@BLOSSOM_SESSION@")){args[i]=token;substitutions++;}
        if(substitutions!=1)throw new IllegalArgumentException("Missing session transport placeholder");
        Class.forName("net.minecraft.client.main.Main").getMethod("main",String[].class).invoke(null,(Object)args);
    }
}
