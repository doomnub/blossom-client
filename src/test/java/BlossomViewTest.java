public final class BlossomViewTest {
    public static void run(){
        boolean xp=BlossomHooks.modules.on("xpchanger"),time=BlossomHooks.modules.on("timechanger");
        int value=BlossomHooks.settings.xpPercent,hour=BlossomHooks.settings.visualTime;
        try{
            BlossomHooks.modules.get("xpchanger").enabled=false;
            check(BlossomView.xp(.37f)==.37f);
            BlossomHooks.modules.get("xpchanger").enabled=true;
            for(int percent:new int[]{0,25,50,100}){BlossomHooks.settings.xpPercent=percent;check(BlossomView.xp(.37f)==percent/100f);}
            BlossomHooks.modules.get("timechanger").enabled=false;check(BlossomView.skyTime(56789)==56789);
            BlossomHooks.modules.get("timechanger").enabled=true;
            for(int h:new int[]{0,6,12,18,24}){BlossomHooks.settings.visualTime=h;check(BlossomView.skyTime(56789)==48000+(h*1000L+18000)%24000);}
            check(BlossomFreeLook.yaw(123)==123&&BlossomFreeLook.pitch(-25)==-25);
            System.out.println("XP/time overrides, disabled passthrough and day preservation passed");
        }finally{BlossomHooks.modules.get("xpchanger").enabled=xp;BlossomHooks.modules.get("timechanger").enabled=time;BlossomHooks.settings.xpPercent=value;BlossomHooks.settings.visualTime=hour;}
    }
    private static void check(boolean value){if(!value)throw new AssertionError();}
}
