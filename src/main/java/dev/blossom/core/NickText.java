package dev.blossom.core;
/** Literal, case-insensitive visual substitution, including names interrupted by color codes. */
public final class NickText {
    public static String validate(String value){
        if(value==null||value.trim().isEmpty()||value.length()>32)throw new IllegalArgumentException("Use 1 to 32 visible characters");
        for(int i=0;i<value.length();i++)if(Character.isISOControl(value.charAt(i))||value.charAt(i)=='\u00a7')throw new IllegalArgumentException("Formatting and control characters are not allowed");
        return value;
    }
    public static String replace(String text,String name,String alias){
        if(text==null||name==null||name.isEmpty()||alias==null||name.equals(alias))return text;
        StringBuilder out=null;int copied=0;
        for(int i=0;i<text.length();){
            // Idempotent if the chosen alias itself contains the account name.
            if(text.startsWith(alias,i)){i+=Math.max(1,alias.length());continue;}
            if(text.charAt(i)=='\u00a7'&&i+1<text.length()){i+=2;continue;}
            int end=i,n=0;
            while(end<text.length()&&n<name.length()){
                char c=text.charAt(end);
                if(c=='\u00a7'&&end+1<text.length()){end+=2;continue;}
                if(Character.toLowerCase(c)!=Character.toLowerCase(name.charAt(n)))break;
                end++;n++;
            }
            if(n==name.length()){
                if(out==null)out=new StringBuilder(text.length()+alias.length());
                out.append(text,copied,i).append(alias);
                // Restore inline formatting for the text following the replacement.
                for(int k=i;k<end;k++)if(text.charAt(k)=='\u00a7'&&k+1<end){out.append(text,k,k+2);k++;}
                copied=end;i=end;
            }else i++;
        }
        return out==null?text:out.append(text,copied,text.length()).toString();
    }
}
