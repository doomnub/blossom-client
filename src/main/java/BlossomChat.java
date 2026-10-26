import dev.blossom.core.ChatSettings;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/** Decorates incoming components without changing their click/hover actions or sending messages. */
public final class BlossomChat {
    private static final List<String> history=new ArrayList<String>();
    private static final ExecutorService disk=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"Blossom chat history");t.setDaemon(true);return t;});
    private static final Pattern URL=Pattern.compile("https?://[^\\s<>]+",Pattern.CASE_INSENSITIVE);
    private static boolean receiving,loaded,lastPersistent;
    private static String lastMessage="";
    private static int repeat,id=-1000000,lastWidth;
    private static float lastScale;
    private static Object world;
    private static boolean oldUnicode;
    public static boolean enabled(){return BlossomHooks.modules.on("chat");}
    private static ChatSettings settings(){return BlossomHooks.settings.chat;}
    private static Path file(){return ave.A().v.toPath().resolve("blossom/chat-history.txt");}
    public static void tick(){
        if(!enabled())return;
        if(world!=ave.A().f){world=ave.A().f;lastMessage="";repeat=0;}
        if(!loaded){
            loaded=true;
            if(settings().persistent){
                try{if(Files.isRegularFile(file())&&Files.size(file())<2000000){history.addAll(Files.readAllLines(file(),StandardCharsets.UTF_8));trim();}}catch(Exception e){BlossomHooks.message="Could not load chat history";}
            }
        }
        if(lastPersistent&&!settings().persistent){disk.execute(()->{try{Files.deleteIfExists(file());}catch(Exception e){System.err.println("[Blossom] Could not erase chat history");}});}
        lastPersistent=settings().persistent;
        if(lastWidth!=settings().width||lastScale!=settings().scale){lastWidth=settings().width;lastScale=settings().scale;ave.A().q.d().b();}
    }
    public static boolean receive(avt chat,eu original,int originalId){
        if(!enabled()||receiving)return false;
        String plain=original.c();
        String lower=plain.toLowerCase(Locale.ROOT);
        for(String filter:settings().filter.split(","))if(!filter.trim().isEmpty()&&lower.contains(filter.trim().toLowerCase(Locale.ROOT)))return true;
        boolean mention=ave.A().h!=null&&lower.contains(ave.A().h.e_().toLowerCase(Locale.ROOT));
        String time=LocalTime.now().format(DateTimeFormatter.ofPattern(settings().timestampFormat));
        String prefix=settings().timestamps?"§"+settings().timestampColor+"["+time+"] §r":"";
        if(mention&&settings().mentions)prefix+="§d[mention] §r";
        eu decorated=new fa(prefix).a(linkify(original));
        int messageId=originalId;
        if(settings().duplicates&&originalId==0){
            if(plain.equals(lastMessage)){repeat++;}else{repeat=1;id--;}
            messageId=id;
            if(repeat>1)decorated.a(new fa(" §7×"+repeat));
        }
        lastMessage=plain;
        history.add("["+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+"] "+plain);trim();
        if(settings().persistent)persist();
        if(mention&&settings().sounds&&ave.A().h!=null)ave.A().h.a("note.pling",.4f,1f);
        receiving=true;
        try{chat.a(decorated,messageId);}finally{receiving=false;}
        return true;
    }
    private static eu linkify(eu original){
        eu result;
        if(original instanceof fa&&original.b().h()==null){
            String text=((fa)original).g();Matcher urls=URL.matcher(text);int end=0;
            result=new fa("").a(original.b().m());
            while(urls.find()){
                if(urls.start()>end)result.a(new fa(text.substring(end,urls.start())));
                eu link=new fa(urls.group()).a(original.b().m().a(new et(et.a.a,urls.group())));
                result.a(link);end=urls.end();
            }
            if(end<text.length())result.a(new fa(text.substring(end)));
        }else{result=original.f();result.a().clear();}
        for(eu sibling:original.a())result.a(linkify(sibling));
        return result;
    }
    public static void forgetSaved(){disk.execute(()->{try{Files.deleteIfExists(file());}catch(Exception e){System.err.println("[Blossom] Could not erase saved chat history");}});}
    private static void trim(){while(history.size()>2000)history.remove(0);}
    private static void persist(){
        final List<String> snapshot=new ArrayList<String>(history);final Path target=file();
        disk.execute(()->{try{Files.createDirectories(target.getParent());Path temp=target.resolveSibling("chat-history.tmp");Files.write(temp,snapshot,StandardCharsets.UTF_8);Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}catch(Exception e){System.err.println("[Blossom] Chat history save failed");}});
    }
    public static List<String> search(String query){List<String> found=new ArrayList<String>();String needle=query.toLowerCase(Locale.ROOT);for(int i=history.size()-1;i>=0;i--)if(history.get(i).toLowerCase(Locale.ROOT).contains(needle))found.add(history.get(i));return found;}
    public static void clear(){history.clear();lastMessage="";disk.execute(()->{try{Files.deleteIfExists(file());}catch(Exception e){System.err.println("[Blossom] Could not erase chat history");}});}
    public static String links(String text){Matcher m=URL.matcher(text);List<String> links=new ArrayList<String>();while(m.find())links.add(m.group());return String.join("\n",links);}
    public static int width(int vanilla){return enabled()?settings().width:vanilla;}
    public static int height(int vanilla){return enabled()?settings().height:vanilla;}
    public static float scale(float vanilla){return enabled()?settings().scale:vanilla;}
    public static float opacity(float vanilla){return enabled()?(settings().textOpacity-.1f)/.9f:vanilla;}
    public static int fade(int vanilla){return enabled()?settings().fadeSeconds*20:vanilla;}
    public static double fadeDouble(double vanilla){return enabled()?settings().fadeSeconds*20d:vanilla;}
    public static void background(int x0,int y0,int x1,int y1,int color){
        if(enabled()){
            float text=settings().textOpacity;
            int alpha=Math.min(255,Math.round((color>>>24)*2/text*settings().background));
            color=(color&0xffffff)|(alpha<<24);
        }
        avp.a(x0,y0,x1,y1,color);
    }
    public static void beginDraw(){if(enabled()&&settings().unicode){oldUnicode=ave.A().k.a();ave.A().k.a(true);}}
    public static void endDraw(){if(enabled()&&settings().unicode)ave.A().k.a(oldUnicode);}
}
