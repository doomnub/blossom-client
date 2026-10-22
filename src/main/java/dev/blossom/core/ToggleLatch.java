package dev.blossom.core;
/** Tracks deliberate press edges, with explicit reset when focus/world changes. */
public final class ToggleLatch {
    private boolean active;
    public void press() { active = !active; }
    public void reset() { active = false; }
    public boolean active() { return active; }
}
