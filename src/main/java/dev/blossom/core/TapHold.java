package dev.blossom.core;
/** A short release latches; a held press is momentary. Pressing a latch cancels it immediately. */
public final class TapHold {
    private boolean down,latched,cancelPress;
    private long pressed;
    public boolean update(boolean key,boolean allowed,long now){
        if(!allowed){latched=false;cancelPress=true;down=key;return false;}
        if(key&&!down){pressed=now;cancelPress=latched;latched=false;}
        if(!key&&down){if(!cancelPress&&now-pressed<250_000_000L)latched=true;cancelPress=false;}
        down=key;
        return latched||(key&&!cancelPress);
    }
}
