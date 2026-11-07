import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
/** Synthetic selected-pack resources exercise the real loader without a GL context. */
public final class BlossomSkyTest {
    public static void run()throws Exception{
        final Map<String,byte[]> files=new HashMap<String,byte[]>();
        String base="minecraft:mcpatcher/sky/world0/";
        ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(3,2,BufferedImage.TYPE_INT_ARGB),"png",out);byte[] image=out.toByteArray();
        files.put(base+"sky1.properties","source=./shared.png\nrotate=false\n".getBytes("UTF-8"));
        files.put(base+"sky2.properties","source=./shared.png\nweather=rain thunder\n".getBytes("UTF-8"));
        files.put(base+"sky3.properties","axis=NaN 0 1\n".getBytes("UTF-8"));
        files.put(base+"sky4.properties","source=./huge.png\n".getBytes("UTF-8"));
        files.put(base+"sky5.properties","source=./shared.png\n".getBytes("UTF-8"));
        files.put(base+"shared.png",image);
        byte[] huge=image.clone();java.nio.ByteBuffer.wrap(huge).putInt(16,9000).putInt(20,6000);java.util.zip.CRC32 crc=new java.util.zip.CRC32();crc.update(huge,12,17);java.nio.ByteBuffer.wrap(huge).putInt(29,(int)crc.getValue());files.put(base+"huge.png",huge);
        final int[] opens={0},closed={0},probes={0};
        bni resources=new bni(){
            public Set<String>a(){return Collections.singleton("minecraft");}
            public List<bnh>b(jy r)throws IOException{return Collections.singletonList(a(r));}
            public bnh a(final jy r)throws IOException{
                probes[0]++;final byte[] data=files.get(r.toString());if(data==null)throw new FileNotFoundException();
                return new bnh(){public jy a(){return r;}public InputStream b(){opens[0]++;return new ByteArrayInputStream(data){public void close()throws IOException{closed[0]++;super.close();}};}public boolean c(){return false;}public <T extends bnw>T a(String n){return null;}public String d(){return "synthetic";}};
            }
        };
        java.lang.reflect.Method load=BlossomSky.class.getDeclaredMethod("load",bni.class,int.class);load.setAccessible(true);Object pack=load.invoke(null,resources,0);
        java.lang.reflect.Field layers=pack.getClass().getDeclaredField("layers"),images=pack.getClass().getDeclaredField("images");layers.setAccessible(true);images.setAccessible(true);
        if(((List<?>)layers.get(pack)).size()!=3)throw new AssertionError("Valid layers after malformed layers must survive");
        if(((Map<?,?>)images.get(pack)).size()!=1)throw new AssertionError("Shared texture must decode once; oversized image must be rejected");
        if(opens[0]!=closed[0]||probes[0]>270)throw new AssertionError("Unbounded probes or resource stream leak");
        System.out.println("Sky loader: legacy pack discovery, shared textures, invalid/oversized resources and closed streams passed");
    }
}
