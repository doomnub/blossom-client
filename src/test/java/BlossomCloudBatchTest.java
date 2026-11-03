import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
/** Exercise real vanilla vertex packing with a CPU-only draw sink. */
public final class BlossomCloudBatchTest {
    private static final class Sink extends bfx {
        final ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        int draws,maxVertices;
        Sink(){super(65536);}
        public void b(){
            bfd b=c();maxVertices=Math.max(maxVertices,b.h());b.e();
            ByteBuffer data=b.f().duplicate();while(data.hasRemaining())bytes.write(data.get());
            draws++;b.b();
        }
    }
    private static void cell(bfd b,int cell){
        for(int v=0;v<128;v++)b.b((double)cell,(double)(v%4),(double)(v/4)).a(v*.001,cell*.01).a(.2f,.4f,.6f,.8f).c(0,1,0).d();
    }
    public static void run(){
        boolean module=BlossomHooks.modules.get("performance").enabled,setting=BlossomHooks.settings.performance.cloudBatching;
        try{
            BlossomHooks.modules.get("performance").enabled=true;
            Sink vanilla=new Sink(),batched=new Sink();
            for(int pass=0;pass<2;pass++)for(int cell=0;cell<64;cell++){vanilla.c().a(7,bms.l);cell(vanilla.c(),cell);vanilla.b();}
            BlossomHooks.settings.performance.cloudBatching=true;BlossomCloudBatch.enter();
            for(int pass=0;pass<2;pass++){
                BlossomCloudBatch.flush(); // before depth/color mask change
                for(int cell=0;cell<64;cell++){BlossomCloudBatch.begin(batched.c(),7,bms.l);cell(batched.c(),cell);BlossomCloudBatch.draw(batched);}
                BlossomCloudBatch.flush();
                if(batched.bytes.size()!=vanilla.bytes.size()*(pass+1)/2)throw new AssertionError("Pass boundary not flushed");
            }
            BlossomCloudBatch.exit();
            if(!Arrays.equals(vanilla.bytes.toByteArray(),batched.bytes.toByteArray()))throw new AssertionError("Cloud vertices/attributes/order changed");
            if(vanilla.draws!=128||batched.draws!=4||batched.maxVertices>4096)throw new AssertionError("Batch reduction/bound");
            BlossomCloudBatch.enter();BlossomCloudBatch.begin(batched.c(),7,bms.l);cell(batched.c(),0);BlossomCloudBatch.abort();
            // Abort must leave the builder usable without submitting incomplete geometry.
            batched.c().a(7,bms.l);cell(batched.c(),0);batched.b();
            BlossomHooks.modules.get("performance").enabled=false;
            Sink disabled=new Sink();BlossomCloudBatch.enter();
            for(int i=0;i<8;i++){BlossomCloudBatch.begin(disabled.c(),7,bms.l);cell(disabled.c(),i);BlossomCloudBatch.draw(disabled);}
            BlossomCloudBatch.exit();if(disabled.draws!=8)throw new AssertionError("Module-off changed draws");
            System.out.println("Cloud batching: identical vertex bytes/order, pass boundaries, abort cleanup, fallback; synthetic 128 draws -> 4");
        }finally{BlossomCloudBatch.abort();BlossomHooks.modules.get("performance").enabled=module;BlossomHooks.settings.performance.cloudBatching=setting;}
    }
}
