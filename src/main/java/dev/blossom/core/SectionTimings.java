package dev.blossom.core;
import java.util.*;
/** Main-thread nested wall timings. Bounded nodes/depth, reused stacks, no per-call samples. */
public final class SectionTimings {
    public static final String[] COLUMNS={"tick","gameRenderer","display_update","fpslimit_wait","blossom.chunk.uploads","blossom.cape.simulation","blossom.cape.draw","blossom.hud"};
    public static final class Node {
        public final String path;final Map<String,Node> children=new HashMap<String,Node>();final int column;
        public long calls,inclusive,exclusive,max,failures;
        Node(String path,String name){this.path=path;int c=-1;for(int i=0;i<COLUMNS.length;i++)if(COLUMNS[i].equals(name))c=i;column=c;}
    }
    private final Node root=new Node("","");
    public final List<Node> nodes=new ArrayList<Node>();
    private final Node[] stack=new Node[128];private final long[] starts=new long[128],children=new long[128];
    public final long[] frame=new long[COLUMNS.length];
    private int depth,skipped;public long overflow,unmatched,forced;
    public void begin(String name,long now){
        if(skipped>0||depth==stack.length){skipped++;overflow++;return;}
        Node parent=depth==0?root:stack[depth-1],node=parent.children.get(name);
        if(node==null){if(nodes.size()>=1024){skipped++;overflow++;return;}node=new Node(parent.path.isEmpty()?name:parent.path+"/"+name,name);parent.children.put(name,node);nodes.add(node);}
        stack[depth]=node;starts[depth]=now;children[depth]=0;depth++;
    }
    public void end(long now){
        if(skipped>0){skipped--;return;}if(depth==0){unmatched++;return;}
        int d=--depth;Node n=stack[d];long elapsed=Math.max(0,now-starts[d]);
        n.calls++;n.inclusive+=elapsed;n.exclusive+=Math.max(0,elapsed-children[d]);n.max=Math.max(n.max,elapsed);
        if(n.column>=0)frame[n.column]+=elapsed;if(d>0)children[d-1]+=elapsed;
    }
    public void boundary(long now){skipped=0;while(depth>0){forced++;end(now);}}
    public void resetFrame(){Arrays.fill(frame,0);}
    public String csv(){StringBuilder b=new StringBuilder("section,calls,inclusive_ns,self_ns,max_call_ns\n");for(Node n:nodes)b.append('"').append(n.path.replace("\"","\"\"")).append("\",").append(n.calls).append(',').append(n.inclusive).append(',').append(n.exclusive).append(',').append(n.max).append('\n');return b.toString();}
}
