import dev.blossom.core.NickText;
/** Display only: never edits sessions, components, player profiles or network packets. */
public final class BlossomNick {
    public static String text(String text){
        if(text==null||!BlossomHooks.nickEnabled())return text;
        ave mc=ave.A();
        if(mc==null||mc.L()==null)return text;
        return NickText.replace(text,mc.L().c(),BlossomHooks.settings.nickName);
    }
}
