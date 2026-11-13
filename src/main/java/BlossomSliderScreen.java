import java.util.function.Consumer;
/** Drag slider with immediate application and bounded keyboard nudges. */
public final class BlossomSliderScreen extends axu {
    private final axu back;private final String title;private final float min,max;private final boolean integer;private final Consumer<Float> apply;private float step;private float value;private boolean dragging;
    public BlossomSliderScreen(axu back,String title,float value,float min,float max,boolean integer,Consumer<Float> apply){this.back=back;this.title=title;this.value=value;this.min=min;this.max=max;this.integer=integer;this.apply=apply;}
    public BlossomSliderScreen(axu back,String title,float value,float min,float max,float step,Consumer<Float> apply){this(back,title,value,min,max,false,apply);this.step=step;}
    public void b(){n.clear();n.add(new BlossomButton(0,l/2-140,m/2+45,280,20,"Done"));}
    protected void a(avs b){j.a(back);}
    public void a(int x,int y,float f){
        c();BlossomTheme.panel(l,m);
        q.a(title,l/2-140,m/2-55,BlossomTheme.TEXT);
        int knob=l/2-140+Math.round(280*(value-min)/(max-min));
        BlossomTheme.rounded(l/2-140,m/2-3,l/2+140,m/2+3,3,0xff3a3945);
        if(knob>l/2-140)BlossomTheme.rounded(l/2-140,m/2-3,knob,m/2+3,3,BlossomHooks.accent);
        BlossomTheme.rounded(knob-5,m/2-8,knob+5,m/2+8,4,0xfff4f1f7);
        String label=integer?Integer.toString(Math.round(value)):String.format(java.util.Locale.ROOT,"%.2f",value);
        q.a(label,l/2-q.a(label)/2,m/2+19,BlossomTheme.TEXT);
        q.a("Drag to adjust / arrow keys to fine-tune",l/2-140,m/2-30,BlossomTheme.MUTED);
        super.a(x,y,f);
    }

    private void set(float v){value=Math.max(min,Math.min(max,step>0?Math.round(v/step)*step:integer?Math.round(v):v));apply.accept(value);}
    private void drag(int x){set(min+(max-min)*(x-(l/2-140))/280f);}
    protected void a(int x,int y,int b){if(b==0&&Math.abs(y-m/2)<16&&Math.abs(x-l/2)<=145){dragging=true;drag(x);}else super.a(x,y,b);}
    protected void a(int x,int y,int b,long t){if(dragging)drag(x);}
    protected void b(int x,int y,int b){dragging=false;super.b(x,y,b);}
    protected void a(char ch,int key){if(key==1)j.a(back);if(key==203)set(value-(step>0?step:integer?1:(max-min)/100));if(key==205)set(value+(step>0?step:integer?1:(max-min)/100));}
    public void m(){BlossomHooks.save("default");}public boolean d(){return false;}
}
