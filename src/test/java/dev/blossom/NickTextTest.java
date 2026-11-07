package dev.blossom;
import dev.blossom.core.*;
import java.util.Properties;
public final class NickTextTest {
    private static void check(String input,String name,String alias,String expected){
        String actual=NickText.replace(input,name,alias);
        if(!java.util.Objects.equals(actual,expected))throw new AssertionError("Unexpected masked text: "+actual);
    }
    public static void run(){
        check(null,"Account","Rose",null);
        check("[VIP] Account: ACCOUNT killed Account!","Account","Rose","[VIP] Rose: Rose killed Rose!");
        check("§aAcc§bount§r joined","Account","Rose","§aRose§b§r joined");
        check("Account 12","Account","Account_2","Account_2 12");
        check("Account_2 12","Account","Account_2","Account_2 12");
        check("§aAccount's bed","Account","$Rose\\","§a$Rose\\'s bed");
        String unchanged="unrelated";if(NickText.replace(unchanged,"Account","Rose")!=unchanged)throw new AssertionError("Unrelated text allocated");
        for(String bad:new String[]{""," ","x\ny","§cRose",new String(new char[33]).replace('\0','a')}){
            try{NickText.validate(bad);throw new AssertionError("Invalid alias accepted");}catch(IllegalArgumentException expected){}
        }
        ClientSettings settings=new ClientSettings();settings.nickName="Rose";Properties p=new Properties();settings.save(p);
        ClientSettings loaded=new ClientSettings();loaded.load(p);if(!"Rose".equals(loaded.nickName))throw new AssertionError("Alias not persisted");
        p.setProperty("nickhider.name","§kbad");loaded.load(p);if(!"Player".equals(loaded.nickName))throw new AssertionError("Invalid saved alias not bounded");
        System.out.println("Nick Hider repeated/case/formatted replacements, idempotency, validation and persistence passed");
    }
}
