import dev.blossom.core.*;
import java.util.*;
/** Text HUD presentation; expensive inventory/effect strings refresh at tick cadence. */
public final class BlossomHud {
    private static final Map<String,String[]> cached=new HashMap<String,String[]>();
    private static int ticks;
    public static void invalidate(){cached.clear();ticks=3;tick();}
    public static void tick() {
        if(++ticks%4!=0)return;
        ave mc=ave.A();
        if(mc.h==null){cached.clear();return;}
        if(BlossomHooks.modules.on("armor")) {
            List<String> lines=new ArrayList<String>();
            for(int i=3;i>=0;i--)addItem(lines,mc.h.bi.b[i]);
            addItem(lines,mc.h.bA());
            cached.put("armor",lines.isEmpty()?new String[]{"Armor: empty"}:lines.toArray(new String[0]));
        }
        if(BlossomHooks.modules.on("potions")) {
            List<pf> effects=new ArrayList<pf>();
            for(Object effect:mc.h.bl())effects.add((pf)effect);
            effects.sort(Comparator.comparingInt(pf::b));
            List<String> lines=new ArrayList<String>();
            for(pf effect:effects) {
                if(effect.a()<0||effect.a()>=pe.a.length||pe.a[effect.a()]==null)continue;
                String warning=effect.b()<=200?"§c":"";
                lines.add(warning+di.a(pe.a[effect.a()].a())+HudText.potionLevel(effect.c(),BlossomHooks.settings.hidePotionOne)+"  "+pe.a(effect));
            }
            cached.put("potions",lines.isEmpty()?(BlossomHooks.settings.showEmptyPotions?new String[]{"No potion effects"}:new String[0]):lines.toArray(new String[0]));
        }
        if(BlossomHooks.modules.on("resources")) {
            int[] ids={265,266,264,388},counts=new int[4];
            for(zx stack:mc.h.bi.a)if(stack!=null)for(int i=0;i<ids.length;i++)if(zw.b(stack.b())==ids[i])counts[i]+=stack.b;
            cached.put("resources",new String[]{"Iron "+counts[0]+" | Gold "+counts[1],"Diamond "+counts[2]+" | Emerald "+counts[3]});
        }
    }
    private static void addItem(List<String> lines,zx stack) {
        if(stack==null)return;
        if(stack.e()) {
            int remaining=Math.max(0,stack.j()-stack.h());
            int percent=stack.j()==0?0:Math.round(100f*remaining/stack.j());
            lines.add((percent<=15?"§c":"")+stack.q()+" "+remaining+"/"+stack.j()+" ("+percent+"%)");
        } else lines.add(stack.q()+" x"+stack.b);
    }
    public static String[] lines(Modules.Module module) {
        if(module.id.equals("coords")&&BlossomHooks.settings.privacy)return new String[]{"Coordinates hidden"};
        String[] lines=cached.get(module.id);
        if(lines==null)lines=module.id.equals("potions")&&!BlossomHooks.settings.showEmptyPotions?new String[0]:new String[]{BlossomHooks.text(module)};
        if(lines.length==0 && ave.A().m instanceof BlossomHudEditor)lines=new String[]{"Potion effects (preview)"};
        if(!module.extraText){lines=lines.clone();for(int i=0;i<lines.length;i++)lines[i]=HudText.compact(module.id,lines[i]);}
        return lines;
    }
    public static int width(Modules.Module module) {
        int w=0;for(String line:lines(module))w=Math.max(w,ave.A().k.a(line));
        return (int)Math.ceil((w+8)*module.scale);
    }
    public static int height(Modules.Module module){return (int)Math.ceil((lines(module).length*11+5)*module.scale);}
    public static int x(Modules.Module module,int screen){return HudLayout.position(module.x,screen,width(module));}
    public static int y(Modules.Module module,int screen){return HudLayout.position(module.y,screen,height(module));}
    public static void draw() {
        ave mc=ave.A();avr resolution=new avr(mc);
        for(Modules.Module module:BlossomHooks.modules.all())if(module.enabled&&module.category.equals("HUD")) {
            bfl.E();
            try {
                int x=x(module,resolution.a()),y=y(module,resolution.b());
                bfl.b(x,y,0);bfl.a(module.scale,module.scale,1);
                String[] lines=lines(module);int w=0;
                for(String line:lines)w=Math.max(w,mc.k.a(line));
                if(lines.length>0&&BlossomHooks.background)avp.a(0,0,w+8,lines.length*11+5,alpha(module.backgroundColor,module.opacity));
                for(int i=0;i<lines.length;i++)mc.k.a(lines[i],4f,4f+i*11,alpha(module.textColor,module.opacity),module.shadow);
            }catch(RuntimeException e){module.enabled=false;BlossomHooks.message="Disabled failing HUD: "+module.name;}
            finally{bfl.F();bfl.c(1,1,1,1);}
        }
    }
    private static int alpha(int color,float opacity){return (Math.round((color>>>24)*opacity)<<24)|(color&0xffffff);}
}
