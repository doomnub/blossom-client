package dev.blossom.core;
/** Keep actual content; remove only optional descriptive labels and units. */
public final class HudText {
    public static String compact(String id,String text){
        if(id.equals("fps"))return text.replace(" FPS (average)","").replace(" FPS","").replace(" | 1% "," | ").replace(" | 0.1% "," | ");
        if(id.equals("cps"))return text.replace(" CPS","");
        if(id.equals("ping"))return text.replace(" ms (server)","").replace("Ping unknown","—");
        if(id.equals("coords"))return text.replace("XYZ ","");
        if(id.equals("direction"))return text.replace("Heading ","");
        if(id.equals("memory"))return text.replace("Heap ","").replace(" MiB","");
        if(id.equals("resources"))return text.replace("Iron ","").replace("Gold ","").replace("Diamond ","").replace("Emerald ","");
        if(id.equals("movement"))return text.replace("Sprint ","").replace("Sneak ","");
        if(id.equals("armor")){
            java.util.regex.Matcher value=java.util.regex.Pattern.compile("([0-9]+/[0-9]+.*|x[0-9]+)$").matcher(text);
            return value.find()?value.group(1):text.replace("Armor: empty","—");
        }
        return text;
    }
    public static boolean supportsCompact(String id){return java.util.Arrays.asList("fps","cps","ping","coords","direction","memory","resources","movement","armor").contains(id);}
    public static String potionLevel(int amplifier,boolean hideOne){return amplifier==0&&hideOne?"":" "+(amplifier+1);}
}
