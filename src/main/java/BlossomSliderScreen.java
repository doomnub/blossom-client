import java.util.function.Consumer;
/** Drag slider with immediate application and bounded keyboard nudges. */
public final class BlossomSliderScreen extends axu {
    private final axu back;private final String title;private final float min,max;private final boolean integer;private final Consumer<Float> apply;private float value;private boolean dragging;
    public BlossomSliderScreen(axu back,String title,float value,float min,float max,boolean integer,Consumer<Float> apply){this.back=back;this.title=title;this.value=value;this.min=min;this.max=max;this.integer=integer;this.apply=apply;}
    public void b(){n.clear();n.add(new BlossomButton(0,l/2-140,m/2+45,280,20,"Done"));}
    protected void a(avs b){j.a(back);}
    public void a(int x,int y,float f){c();BlossomTheme.panel(l,m);q.a(title,l/2-140,m/2-55,BlossomHooks.accent);avp.a(l/2-140,m/2-3,l/2+140,m/2+3,0xff473348);int knob=l/2-140+Math.round(280*(value-min)/(max-min));avp.a(knob-4,m/2-9,knob+4,m/2+9,BlossomHooks.accent);q.a(String.format(java.util.Locale.ROOT,"%.2f",value),l/2-20,m/2+20,0xffffffff);super.a(x,y,f);}
    private void set(float v){value=Math.max(min,Math.min(max,integer?Math.round(v):v));apply.accept(value);}
    private void drag(int x){set(min+(max-min)*(x-(l/2-140))/280f);}
    protected void a(int x,int y,int b){if(b==0&&Math.abs(y-m/2)<16&&Math.abs(x-l/2)<=145){dragging=true;drag(x);}else super.a(x,y,b);}
    protected void a(int x,int y,int b,long t){if(dragging)drag(x);}
    protected void b(int x,int y,int b){dragging=false;super.b(x,y,b);}
    protected void a(char ch,int key){if(key==1)j.a(back);if(key==203)set(value-(integer?1:(max-min)/100));if(key==205)set(value+(integer?1:(max-min)/100));}
    public void m(){BlossomHooks.save("default");}public boolean d(){return false;}
}
