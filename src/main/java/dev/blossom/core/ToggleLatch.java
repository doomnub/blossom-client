package dev.blossom.core;
/** Tracks deliberate press edges, with explicit reset when focus/world changes. */
public final class ToggleLatch {
    private boolean active;
    public void press() { active = !active; }
    public void reset() { active = false; }
    public void context(boolean validPlayer,boolean menu,boolean preserveInMenu){if(!validPlayer||(menu&&!preserveInMenu))reset();}
    public boolean requested(boolean menu){return active&&!menu;}
    public boolean active() { return active; }
}
