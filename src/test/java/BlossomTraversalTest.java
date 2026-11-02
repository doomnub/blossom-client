import java.util.*;
/** Equivalent FIFO traversal, stable direction order, and real allocation accounting. */
public final class BlossomTraversalTest {
    private static volatile int consumed;
    @SuppressWarnings("unchecked")
    private static void workload(boolean enabled,int rounds){
        BlossomHooks.settings.performance.traversalAllocations=enabled;
        for(int r=0;r<rounds;r++){
            Queue<Object> queue=(Queue<Object>)BlossomTraversal.queue();
            for(int i=0;i<1000;i++)queue.add(BlossomTraversalTest.class);
            while(queue.poll()!=null)for(cq direction:BlossomTraversal.directions())consumed+=direction.ordinal();
        }
    }
    @SuppressWarnings("unchecked") public static void run()throws Exception {
        boolean module=BlossomHooks.modules.get("performance").enabled,setting=BlossomHooks.settings.performance.traversalAllocations;
        try{
            BlossomHooks.modules.get("performance").enabled=true;
            BlossomHooks.settings.performance.traversalAllocations=true;
            Queue<Object> queue=(Queue<Object>)BlossomTraversal.queue();
            if(!(queue instanceof ArrayDeque))throw new AssertionError("Expected contiguous FIFO");
            cq[] first=BlossomTraversal.directions();
            if(first!=BlossomTraversal.directions()||!Arrays.equals(first,cq.values()))throw new AssertionError("Direction order/reuse changed");
            for(int i=0;i<20000;i++)queue.add(i);
            for(int i=0;i<12000;i++)if(!queue.remove().equals(i))throw new AssertionError("FIFO order");
            for(int i=20000;i<30000;i++)queue.add(i);
            for(int i=12000;i<30000;i++)if(!queue.remove().equals(i))throw new AssertionError("Wrapped FIFO order");
            if(!queue.isEmpty()||queue.poll()!=null)throw new AssertionError("Queue retained entries");
            BlossomHooks.modules.get("performance").enabled=false;
            if(!(BlossomTraversal.queue() instanceof LinkedList)||BlossomTraversal.directions()==BlossomTraversal.directions())throw new AssertionError("Module-off must use vanilla allocation");
            BlossomHooks.modules.get("performance").enabled=true;
            workload(false,10);workload(true,10);
            try{
                Class<?> type=Class.forName("com.sun.management.ThreadMXBean");Object bean=java.lang.management.ManagementFactory.getThreadMXBean();
                java.lang.reflect.Method read=type.getMethod("getThreadAllocatedBytes",long.class);
                long id=Thread.currentThread().getId(),start=((Number)read.invoke(bean,id)).longValue();
                workload(false,100);long vanilla=((Number)read.invoke(bean,id)).longValue()-start;
                start=((Number)read.invoke(bean,id)).longValue();workload(true,100);long optimized=((Number)read.invoke(bean,id)).longValue()-start;
                if(vanilla>0&&optimized>=0)System.out.println("Traversal synthetic allocation bytes: vanilla="+vanilla+", optimized="+optimized+" (100 x 1000 entries; not gameplay FPS)");
            }catch(ReflectiveOperationException ignored){}
        }finally{BlossomHooks.modules.get("performance").enabled=module;BlossomHooks.settings.performance.traversalAllocations=setting;}
        System.out.println("Traversal FIFO growth/wraparound, immutable direction order and vanilla fallback passed");
    }
}
