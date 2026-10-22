package dev.blossom.core;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
/** Registration is rare. Dispatch uses a stable snapshot; failure disables the listener. */
public final class EventBus<T> {
    private final CopyOnWriteArrayList<Consumer<T>> listeners = new CopyOnWriteArrayList<Consumer<T>>();
    public AutoCloseable subscribe(final Consumer<T> listener) {
        listeners.add(listener); return () -> listeners.remove(listener);
    }
    public void post(T event) {
        for(Consumer<T> listener:listeners) try { listener.accept(event); }
        catch(RuntimeException e) { listeners.remove(listener); System.err.println("[Blossom] Listener disabled: "+e.getClass().getSimpleName()); }
    }
}
