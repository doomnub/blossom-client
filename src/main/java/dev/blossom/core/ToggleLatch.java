package dev.blossom.core;
/** Tracks deliberate press edges, with optional persistence across menu/world changes. */
public final class ToggleLatch {
    private boolean active;
    public void press() { active = !active; }
    public void set(boolean value) { active=value; }
    public void reset() { active = false; }
    public void context(boolean validPlayer,boolean menu,boolean preserveContext){if(!preserveContext&&(!validPlayer||menu))reset();}
    public boolean requested(boolean menu){return active&&!menu;}
    public boolean active() { return active; }
}
