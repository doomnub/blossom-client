package dev.blossom;
import dev.blossom.agent.BlossomAgent;
public final class AgentSafeModeTest {
    public static void run(){
        String previous=System.getProperty("blossom.safeMode");
        try{
            for(String value:new String[]{"1","true","TRUE"}){
                System.setProperty("blossom.safeMode",value);
                if(!BlossomAgent.safeMode())throw new AssertionError("Safe mode not recognized");
                // A missing Instrumentation instance is intentional. Recovery must return
                // before registration or any attempt to initialize Minecraft/classes.
                BlossomAgent.premain(null,null);
            }
        }finally{if(previous==null)System.clearProperty("blossom.safeMode");else System.setProperty("blossom.safeMode",previous);}
        System.out.println("Agent safe-mode startup bypass passed");
    }
}
