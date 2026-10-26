package dev.blossom.core;
/** Connection-scoped server latency. A remote zero is an unmeasured tab-list value. */
public final class LatencyState {
    private Object connection;
    private int latency=-1;
    private boolean local;
    public void update(Object current,boolean integrated,int value){
        if(current!=connection){connection=current;latency=-1;}
        local=current!=null&&integrated;
        latency=current==null?-1:local?0:value>0?value:-1;
    }
    public void clear(){connection=null;latency=-1;local=false;}
    public int value(){return latency;}
    public String text(){return connection==null?"Ping disconnected":local?"0 ms (local)":latency<0?"0 ms (server)":latency+" ms (server)";}
}
