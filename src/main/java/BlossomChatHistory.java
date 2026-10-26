import java.util.*;
import org.lwjgl.input.Keyboard;

/** Bounded, searchable local history. Copy actions never issue chat packets. */
public final class BlossomChatHistory extends axu {
    private final axu previous;
    private avw search;
    private String query="",selected="";
    private List<String> results=Collections.emptyList();
    private int page;
    public BlossomChatHistory(axu previous){this.previous=previous;}
    private int pageSize(){return Math.max(1,(m-130)/22);}
    @Override public void b(){
        n.clear();Keyboard.enableRepeatEvents(true);
        search=new avw(0,q,20,38,l-40,20);search.f(200);search.a(query);search.b(true);
        results=BlossomChat.search(query);page=Math.min(page,Math.max(0,(results.size()-1)/pageSize()));
        for(int i=0;i<pageSize()&&page*pageSize()+i<results.size();i++){
            String line=results.get(page*pageSize()+i);
            n.add(new BlossomButton(100+i,20,66+i*22,l-40,20,q.a(line,l-56)));
        }
        int w=(l-46)/4;
        n.add(new BlossomButton(1,20,m-58,w,20,"Previous"));n.add(new BlossomButton(2,22+w,m-58,w,20,"Next"));
        n.add(new BlossomButton(3,24+2*w,m-58,w,20,"Copy message"));n.add(new BlossomButton(4,26+3*w,m-58,w,20,"Copy links"));
        n.add(new BlossomButton(5,20,m-32,(l-44)/2,20,"Erase local history"));n.add(new BlossomButton(6,24+(l-44)/2,m-32,(l-44)/2,20,"Back"));
    }
    @Override protected void a(avs button){
        if(button.k>=100){selected=results.get(page*pageSize()+button.k-100);return;}
        switch(button.k){
            case 1:page=Math.max(0,page-1);b();break;
            case 2:page++;b();break;
            case 3:axu.e(selected);break;
            case 4:axu.e(BlossomChat.links(selected));break;
            case 5:BlossomChat.clear();selected="";b();break;
            case 6:j.a(previous);break;
        }
    }
    @Override protected void a(char typed,int key){
        if(key==Keyboard.KEY_ESCAPE){j.a(previous);return;}
        if(search.a(typed,key)){query=search.b();page=0;b();}
    }
    @Override protected void a(int x,int y,int button){search.a(x,y,button);super.a(x,y,button);}
    @Override public void a(int x,int y,float partial){c();BlossomTheme.panel(l,m);q.a("Local chat history — select a message to copy",20,14,BlossomHooks.accent);search.g();super.a(x,y,partial);}
    @Override public void e(){search.a();}
    @Override public void m(){Keyboard.enableRepeatEvents(false);}
    @Override public boolean d(){return false;}
}
