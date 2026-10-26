package dev.blossom.core;

import java.util.Properties;
import java.time.format.DateTimeFormatter;

/** Chat changes are local. Persistent history is opt-in and separately erasable. */
public final class ChatSettings {
    public float background=.5f,textOpacity=1,scale=1;
    public int width=320,height=180,fadeSeconds=10;
    public boolean timestamps=false,mentions=true,duplicates=false,persistent=false,sounds=false,unicode=false;
    public String timestampFormat="HH:mm",timestampColor="7",filter="",shortcut="";
    public void load(Properties p){
        background=ClientSettings.number(p,"chat.background",.5f,0,1);
        textOpacity=ClientSettings.number(p,"chat.textOpacity",1,.05f,1);
        scale=ClientSettings.number(p,"chat.scale",1,.5f,2);
        width=ClientSettings.integer(p,"chat.width",320,40,640);
        height=ClientSettings.integer(p,"chat.height",180,20,360);
        fadeSeconds=ClientSettings.integer(p,"chat.fade",10,1,120);
        timestamps=flag(p,"timestamps",false);mentions=flag(p,"mentions",true);
        duplicates=flag(p,"duplicates",false);persistent=flag(p,"persistent",false);
        sounds=flag(p,"sounds",false);unicode=flag(p,"unicode",false);
        timestampFormat=p.getProperty("chat.timestampFormat","HH:mm");
        try{java.time.LocalTime.now().format(DateTimeFormatter.ofPattern(timestampFormat));}catch(RuntimeException e){timestampFormat="HH:mm";}
        timestampColor=p.getProperty("chat.timestampColor","7");
        if(!timestampColor.matches("[0-9a-f]"))timestampColor="7";
        filter=p.getProperty("chat.filter","");shortcut=p.getProperty("chat.shortcut","");
    }
    private boolean flag(Properties p,String key,boolean fallback){return Boolean.parseBoolean(p.getProperty("chat."+key,Boolean.toString(fallback)));}
    public void save(Properties p){
        p.setProperty("chat.background",Float.toString(background));p.setProperty("chat.textOpacity",Float.toString(textOpacity));
        p.setProperty("chat.scale",Float.toString(scale));p.setProperty("chat.width",Integer.toString(width));p.setProperty("chat.height",Integer.toString(height));p.setProperty("chat.fade",Integer.toString(fadeSeconds));
        p.setProperty("chat.timestamps",Boolean.toString(timestamps));p.setProperty("chat.mentions",Boolean.toString(mentions));p.setProperty("chat.duplicates",Boolean.toString(duplicates));p.setProperty("chat.persistent",Boolean.toString(persistent));p.setProperty("chat.sounds",Boolean.toString(sounds));p.setProperty("chat.unicode",Boolean.toString(unicode));
        p.setProperty("chat.timestampFormat",timestampFormat);p.setProperty("chat.timestampColor",timestampColor);p.setProperty("chat.filter",filter);p.setProperty("chat.shortcut",shortcut);
    }
}
