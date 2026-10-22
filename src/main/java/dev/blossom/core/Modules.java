package dev.blossom.core;
import java.util.*;
public final class Modules {
    public static final class Module {
        public final String id, name, category, description;
        public final boolean defaultEnabled;
        public boolean enabled;
        public float x, y;
        public Module(String id,String name,String category,String description,boolean enabled,float y) {
            this.id=id;this.name=name;this.category=category;this.description=description;
            this.enabled=enabled;this.defaultEnabled=enabled;this.x=.02f;this.y=y;
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
