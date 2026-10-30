package dev.blossom.core;
import java.util.*;
import java.util.function.Consumer;
/** Immutable dispatch snapshots. Mutation allocates; hot-path dispatch does not. */
public final class EventBus<T> {
    private volatile Object[] listeners=new Object[0];
    public boolean hasListeners(){return listeners.length!=0;}
    public synchronized AutoCloseable subscribe(final Consumer<T> listener){Objects.requireNonNull(listener);Object[] next=Arrays.copyOf(listeners,listeners.length+1);next[next.length-1]=listener;listeners=next;return ()->remove(listener);}
    private synchronized void remove(Object listener){Object[] old=listeners;for(int i=0;i<old.length;i++)if(old[i]==listener){Object[] next=new Object[old.length-1];System.arraycopy(old,0,next,0,i);System.arraycopy(old,i+1,next,i,old.length-i-1);listeners=next;return;}}
    @SuppressWarnings("unchecked") public void post(T event){Object[] snapshot=listeners;for(int i=0;i<snapshot.length;i++)try{((Consumer<T>)snapshot[i]).accept(event);}catch(RuntimeException e){remove(snapshot[i]);System.err.println("[Blossom] Listener disabled: "+e.getClass().getSimpleName());}}
}
