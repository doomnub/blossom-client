package dev.blossom.core;

/** Bounded, nonblocking timestamp pairs. Never reads a result before availability. */
public final class GpuTimerPool {
    public interface Sink {void accept(long frame,long nanos);}
    public interface Driver {int create();void delete(int id);void stamp(int id);boolean ready(int id);long value(int id);}
    private final Driver driver;private final int[] start,end;private final long[] frame;private final boolean[] pending;
    private int cursor;public long skipped,discarded;
    public GpuTimerPool(Driver driver,int capacity){this.driver=driver;start=new int[capacity];end=new int[capacity];frame=new long[capacity];pending=new boolean[capacity];try{for(int i=0;i<capacity;i++){start[i]=driver.create();end[i]=driver.create();}}catch(RuntimeException e){close();throw e;}}
    public int begin(long frameId){int i=cursor;if(pending[i]){skipped++;return -1;}frame[i]=frameId;driver.stamp(start[i]);return i;}
    public void end(int i){if(i<0)return;driver.stamp(end[i]);pending[i]=true;cursor=(i+1)%start.length;}
    public void poll(Sink sink){for(int i=0;i<start.length;i++)if(pending[i]&&driver.ready(end[i])&&driver.ready(start[i])){long elapsed=driver.value(end[i])-driver.value(start[i]);pending[i]=false;if(elapsed>=0)sink.accept(frame[i],elapsed);else discarded++;}}
    public void close(){for(int i=0;i<start.length;i++){if(pending[i])discarded++;pending[i]=false;if(start[i]!=0){driver.delete(start[i]);start[i]=0;}if(end[i]!=0){driver.delete(end[i]);end[i]=0;}}}
}
