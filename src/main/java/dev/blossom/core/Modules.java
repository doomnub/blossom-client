package dev.blossom.core;
import java.util.*;
public final class Modules {
    public static final class Module {
        public final String id, name, category, description;
        public final boolean defaultEnabled;
        public boolean enabled;
        public float x, y;
        public float scale=1, opacity=1;
        public int textColor=0xffff83b5, backgroundColor=0x99201a23;
        public boolean shadow=true, locked=false;
        public final float defaultY;
        public void resetLayout(){x=.02f;y=defaultY;scale=1;opacity=1;textColor=0xffff83b5;backgroundColor=0x99201a23;shadow=true;locked=false;}
        public void loadStyle(Properties p){
            scale=ClientSettings.number(p,id+".scale",1,.5f,3);opacity=ClientSettings.number(p,id+".opacity",1,.1f,1);
            textColor=ClientSettings.color(p,id+".textColor",0xffff83b5);backgroundColor=ClientSettings.color(p,id+".backgroundColor",0x99201a23);
            shadow=Boolean.parseBoolean(p.getProperty(id+".shadow","true"));locked=Boolean.parseBoolean(p.getProperty(id+".locked","false"));
        }
        public void saveStyle(Properties p){
            p.setProperty(id+".scale",Float.toString(scale));p.setProperty(id+".opacity",Float.toString(opacity));
            p.setProperty(id+".textColor",Integer.toHexString(textColor));p.setProperty(id+".backgroundColor",Integer.toHexString(backgroundColor));
            p.setProperty(id+".shadow",Boolean.toString(shadow));p.setProperty(id+".locked",Boolean.toString(locked));
        }
        public Module(String id,String name,String category,String description,boolean enabled,float y) {
            this.id=id;this.name=name;this.category=category;this.description=description;
            this.enabled=enabled;this.defaultEnabled=enabled;this.x=.02f;this.y=y;this.defaultY=y;
        }
    }
    private final LinkedHashMap<String,Module> modules = new LinkedHashMap<String,Module>();
    public void add(String id,String name,String category,String description,boolean enabled,float y) {
        if(modules.containsKey(id)) throw new IllegalArgumentException("Duplicate module: "+id);
        modules.put(id,new Module(id,name,category,description,enabled,y));
    }
    public Module get(String id) { return modules.get(id); }
    public boolean on(String id) { Module m=get(id); return m!=null && m.enabled; }
    public Collection<Module> all() { return Collections.unmodifiableCollection(modules.values()); }
}
