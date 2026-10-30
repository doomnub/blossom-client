package dev.blossom;
import dev.blossom.core.*;
import java.util.*;
import java.util.concurrent.*;
public final class RendererPerformanceTest {
    private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    public static void run()throws Exception{
        AdaptiveBudget budget=new AdaptiveBudget();for(int i=0;i<30;i++)budget.sample(20000000L);long stressed=budget.nanos(4,120);check(stressed>=250000&&stressed<4000000,"Overload reduces bounded budget");for(int i=0;i<60;i++)budget.sample(1000000);check(budget.nanos(4,120)>stressed,"Budget recovers with spare time");
        BoundedWorkQueue<Integer> q=new BoundedWorkQueue<Integer>(3,x->x);
        q.offer(9);q.offer(2);q.offer(5);check(!q.offer(6),"Queue stays bounded");check(q.offer(2)&&q.size()==3,"Duplicate task is not enqueued");check(q.take()==2&&q.take()==5&&q.take()==9,"Near work wins");check(q.poll(1,TimeUnit.MILLISECONDS)==null,"Timed empty poll");
        q.put(7);check(q.remove(7)&&q.isEmpty(),"Cancellation releases capacity");
        Thread producer=new Thread(()->{try{q.put(42);}catch(InterruptedException e){throw new RuntimeException(e);}});producer.start();check(q.poll(2,TimeUnit.SECONDS)==42,"Worker wakes on enqueue");producer.join();
        BoundedWorkQueue<Integer> aging=new BoundedWorkQueue<Integer>(100,x->x);aging.offer(1000);for(int i=0;i<65;i++)aging.offer(i);check(aging.poll()==1000,"Old work cannot starve");
        class Driver implements GpuTimerPool.Driver {int next,deletes,reads;long clock;boolean ready;Map<Integer,Long> timestamps=new HashMap<Integer,Long>();public int create(){return ++next;}public void delete(int id){deletes++;}public void stamp(int id){timestamps.put(id,++clock*100);}public boolean ready(int id){return ready;}public long value(int id){if(!ready)throw new AssertionError("GPU read stalled");reads++;return timestamps.get(id);}}
        Driver d=new Driver();GpuTimerPool pool=new GpuTimerPool(d,2);int a=pool.begin(7);pool.end(a);int b=pool.begin(8);pool.end(b);check(pool.begin(9)==-1,"Full query ring skips instead of stalls");final long[] result={0};pool.poll((frame,time)->result[0]+=time);check(d.reads==0,"Pending queries are never read");d.ready=true;pool.poll((frame,time)->result[0]+=time);check(result[0]==200,"Timestamp intervals recorded");pool.close();check(d.deletes==4,"All GPU objects deleted");
        FrameStats frames=new FrameStats(1000);for(int i=0;i<1000;i++)frames.record(10000000);frames.snapshot();frames.clear();frames.record(20000000);check(frames.snapshot()[0]==50,"Reused scratch does not leak old samples");
        EventBus<Integer> bus=new EventBus<Integer>();check(!bus.hasListeners(),"Idle event bus");AutoCloseable listener=bus.subscribe(x->{});check(bus.hasListeners(),"Listener snapshot published");listener.close();check(!bus.hasListeners(),"Last listener removed");
        System.out.println("Adaptive scheduling, bounded priorities, GPU timer lifecycle and hot-path reuse passed");
    }
}
