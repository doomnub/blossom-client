/** Exact pixel comparison for the 16x16 lightmap only; no GL reads or hash collisions. */
public final class BlossomLightmap {
    private static blz texture;private static int id=-1;private static boolean anaglyph;private static final int[] previous=new int[256];
    public static void clear(){texture=null;id=-1;}
    public static void deleted(Object object){if(object==texture)clear();}
    public static void upload(blz current){
        if(!BlossomPerformance.enabled()||!BlossomHooks.settings.performance.lightmapCache){clear();current.d();return;}
        int[] pixels=current.e();int currentId=current.b();
        if(pixels.length!=previous.length){clear();current.d();return;}
        if(current==texture&&currentId==id&&anaglyph==ave.A().t.e&&java.util.Arrays.equals(pixels,previous)){
            // Preserve vanilla binding and sampler side effects even when pixels are unchanged.
            bfl.i(currentId);
            org.lwjgl.opengl.GL11.glTexParameteri(3553,10241,9728);org.lwjgl.opengl.GL11.glTexParameteri(3553,10240,9728);
            org.lwjgl.opengl.GL11.glTexParameteri(3553,10242,10497);org.lwjgl.opengl.GL11.glTexParameteri(3553,10243,10497);
            return;
        }
        current.d();System.arraycopy(pixels,0,previous,0,pixels.length);texture=current;id=currentId;anaglyph=ave.A().t.e;
    }
}
