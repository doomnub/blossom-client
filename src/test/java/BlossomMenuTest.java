import java.util.*;
/** Check interaction targets at Minecraft's smallest supported scaled UI and larger sizes. */
public final class BlossomMenuTest {
    public static void run() throws Exception {
        java.lang.reflect.Field targets=axu.class.getDeclaredField("n");targets.setAccessible(true);
        avb previousBinding=BlossomBindings.menu;
        try {
        BlossomBindings.menu=new avb("Menu test",54,"Blossom test");
        for(int[] size:new int[][]{{320,240},{427,240},{480,270},{640,360},{960,540}}){
            BlossomMenu menu=new BlossomMenu(null);menu.l=size[0];menu.m=size[1];menu.b();
            for(int category=0;category<5;category++){
                menu.a(new avs(300+category,0,0,""));
                for(int page=0;page<30;page++){
                    List<avs> buttons=new ArrayList<avs>((List<avs>)targets.get(menu));
                    for(int i=0;i<buttons.size();i++){
                        avs a=buttons.get(i);
                        check(a.h>=0&&a.i>=0&&a.h+dimension(a,"f")<=size[0]&&a.i+dimension(a,"g")<=size[1],"offscreen: "+a.j);
                        for(int k=i+1;k<buttons.size();k++){
                            avs b=buttons.get(k);
                            check(!(a.h<b.h+dimension(b,"f")&&b.h<a.h+dimension(a,"f")&&a.i<b.i+dimension(b,"g")&&b.i<a.i+dimension(a,"g")),"overlap: "+a.j+" / "+b.j);
                        }
                    }
                    menu.a(new avs(106,0,0,""));
                }
                menu.a('z',44);menu.a('z',44);menu.a('z',44);
                check(((List<avs>)targets.get(menu)).stream().noneMatch(b->b.k<100),"empty search has module targets");
                menu.a('\b',14);menu.a('\b',14);menu.a('\b',14);
            }
        }
        List<avs> buttons=new ArrayList<avs>();avs old=new avs(3,10,20,80,20,"Example");old.l=false;old.m=false;buttons.add(old);
        BlossomHooks.styleMenuButtons(buttons);avs styled=buttons.get(0);
        check(styled instanceof BlossomButton&&styled.k==3&&styled.h==10&&styled.i==20&&dimension(styled,"f")==80&&!styled.l&&!styled.m,"native menu state preservation");
        System.out.println("Menu target bounds, pagination, category/search and native control state passed");
        } finally {BlossomBindings.menu=previousBinding;}
    }
    private static int dimension(avs button,String name) throws Exception {java.lang.reflect.Field f=avs.class.getDeclaredField(name);f.setAccessible(true);return f.getInt(button);}
    private static void check(boolean value,String detail){if(!value)throw new AssertionError(detail);}
}
