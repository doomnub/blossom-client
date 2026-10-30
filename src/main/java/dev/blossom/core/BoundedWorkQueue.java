package dev.blossom.core;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.function.ToDoubleFunction;
/** Fixed-storage priority queue. Aging prevents starvation; cancellation removes by identity/equality. */
public final class BoundedWorkQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {
    private final Object[] tasks;private final double[] priorities;private final long[] tickets;
    private final ToDoubleFunction<E> score;private long sequence;private int size;
    private final ReentrantLock lock=new ReentrantLock();private final Condition available=lock.newCondition(),space=lock.newCondition();
    public BoundedWorkQueue(int capacity,ToDoubleFunction<E> score){if(capacity<1)throw new IllegalArgumentException();tasks=new Object[capacity];priorities=new double[capacity];tickets=new long[capacity];this.score=Objects.requireNonNull(score);}
    private double priority(E e){Objects.requireNonNull(e);double p=score.applyAsDouble(e);return Double.isFinite(p)?p:Double.MAX_VALUE;}
    private int find(Object e){for(int i=0;i<tasks.length;i++)if(tasks[i]!=null&&tasks[i].equals(e))return i;return -1;}
    private void insert(E e,double p){for(int i=0;i<tasks.length;i++)if(tasks[i]==null){tasks[i]=e;priorities[i]=p;tickets[i]=sequence++;size++;available.signal();return;}}
    private int best(){int best=-1,oldest=-1;for(int i=0;i<tasks.length;i++)if(tasks[i]!=null){if(oldest<0||tickets[i]<tickets[oldest])oldest=i;if(best<0||priorities[i]<priorities[best]||priorities[i]==priorities[best]&&tickets[i]<tickets[best])best=i;}return oldest>=0&&sequence-tickets[oldest]>64?oldest:best;}
    @SuppressWarnings("unchecked") private E removeAt(int i){E e=(E)tasks[i];tasks[i]=null;size--;space.signalAll();return e;}
    public boolean offer(E e){double p=priority(e);lock.lock();try{if(find(e)>=0)return true;if(size==tasks.length)return false;insert(e,p);return true;}finally{lock.unlock();}}
    public void put(E e)throws InterruptedException{double p=priority(e);lock.lockInterruptibly();try{while(size==tasks.length){if(find(e)>=0)return;space.await();}if(find(e)<0)insert(e,p);}finally{lock.unlock();}}
    public boolean offer(E e,long timeout,TimeUnit unit)throws InterruptedException{double p=priority(e);long left=unit.toNanos(timeout);lock.lockInterruptibly();try{while(size==tasks.length){if(find(e)>=0)return true;if(left<=0)return false;left=space.awaitNanos(left);}if(find(e)<0)insert(e,p);return true;}finally{lock.unlock();}}
    public E poll(){lock.lock();try{return size==0?null:removeAt(best());}finally{lock.unlock();}}
    public E take()throws InterruptedException{lock.lockInterruptibly();try{while(size==0)available.await();return removeAt(best());}finally{lock.unlock();}}
    public E poll(long timeout,TimeUnit unit)throws InterruptedException{long left=unit.toNanos(timeout);lock.lockInterruptibly();try{while(size==0){if(left<=0)return null;left=available.awaitNanos(left);}return removeAt(best());}finally{lock.unlock();}}
    @SuppressWarnings("unchecked") public E peek(){lock.lock();try{return size==0?null:(E)tasks[best()];}finally{lock.unlock();}}
    public int size(){lock.lock();try{return size;}finally{lock.unlock();}}
    public int remainingCapacity(){return tasks.length-size();}
    public boolean remove(Object o){lock.lock();try{int i=find(o);if(i<0)return false;removeAt(i);return true;}finally{lock.unlock();}}
    public void clear(){lock.lock();try{Arrays.fill(tasks,null);size=0;space.signalAll();}finally{lock.unlock();}}
    public int drainTo(Collection<? super E> c){return drainTo(c,Integer.MAX_VALUE);}
    public int drainTo(Collection<? super E> c,int maximum){Objects.requireNonNull(c);if(c==this)throw new IllegalArgumentException();lock.lock();try{int n=0;while(size>0&&n<maximum){int i=best();@SuppressWarnings("unchecked") E e=(E)tasks[i];c.add(e);removeAt(i);n++;}return n;}finally{lock.unlock();}}
    @SuppressWarnings("unchecked") public Iterator<E> iterator(){lock.lock();try{List<E> copy=new ArrayList<E>(size);for(Object e:tasks)if(e!=null)copy.add((E)e);return Collections.unmodifiableList(copy).iterator();}finally{lock.unlock();}}
}
