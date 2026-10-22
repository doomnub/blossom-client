import org.lwjgl.input.Keyboard;
public final class BlossomConfirm extends axu {
    private final axu previous;
    private final String question;
    private final Runnable accept;
    public BlossomConfirm(axu previous,String question,Runnable accept){this.previous=previous;this.question=question;this.accept=accept;}
    @Override public void b(){n.clear();n.add(new avs(0,l/2-100,m/2,96,20,"Confirm"));n.add(new avs(1,l/2+4,m/2,96,20,"Cancel"));}
    @Override public void a(int x,int y,float partial){c();q.a(question,l/2-q.a(question)/2,m/2-24,0xffffffff);super.a(x,y,partial);}
    @Override protected void a(avs button){if(button.k==0)accept.run();j.a(previous);}
    @Override protected void a(char c,int key){if(key==Keyboard.KEY_ESCAPE)j.a(previous);}
    @Override public boolean d(){return false;}
}
