/** Five explicit save/load slots; slot 1 preserves the old preset file. */
public final class BlossomPresets extends axu {
    private final axu previous;
    private int overwrite;
    public BlossomPresets(axu previous){this.previous=previous;}
    @Override public void b(){
        n.clear();
        for(int slot=1;slot<=5;slot++){
            int y=48+(slot-1)*26;
            n.add(new BlossomButton(slot,l/2-140,y,136,20,overwrite==slot?"Confirm overwrite "+slot:"Save slot "+slot));
            avs load=new BlossomButton(10+slot,l/2+4,y,136,20,"Load slot "+slot+(BlossomHooks.hasPreset(slot)?"":" (empty)"));
            load.l=BlossomHooks.hasPreset(slot);n.add(load);
        }
        n.add(new BlossomButton(99,l/2-140,m-28,280,20,"Back"));
    }
    @Override protected void a(avs button){
        if(button.k==99){j.a(previous);return;}
        if(button.k<=5){
            if(BlossomHooks.hasPreset(button.k)&&overwrite!=button.k){overwrite=button.k;b();return;}
            BlossomHooks.savePreset(button.k);overwrite=0;
            // Flush before rebuilding availability so a newly saved slot is immediately usable.
            BlossomHooks.flushProfiles();
        }else{BlossomHooks.loadPreset(button.k-10);BlossomHooks.save("default");overwrite=0;}
        b();
    }
    @Override public void a(int x,int y,float partial){c();BlossomTheme.panel(l,m);q.a("BLOSSOM / Module presets",l/2-140,16,BlossomHooks.accent);q.a("Each slot saves modules, settings and HUD layout.",l/2-140,32,0xffcccccc);super.a(x,y,partial);q.a(BlossomHooks.message,l/2-140,m-44,0xffcccccc);}
    @Override protected void a(char c,int key){if(key==1)j.a(previous);}
    @Override public boolean d(){return false;}
}
