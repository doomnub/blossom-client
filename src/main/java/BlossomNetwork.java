import io.netty.channel.*;
import io.netty.buffer.ByteBuf;
import java.util.concurrent.atomic.AtomicLong;
/** Passive observers: no payload inspection, packet mutation, extra traffic or persistent logs. */
public final class BlossomNetwork {
    private static Channel channel;private static long sample;
    private static final AtomicLong incoming=new AtomicLong(),outgoing=new AtomicLong(),inPackets=new AtomicLong(),outPackets=new AtomicLong();
    private static long oldIn,oldOut,oldIP,oldOP;private static String summary="Disconnected";
    private static final int[] history=new int[100];private static int cursor,count,disconnects;
    public static void tick(){
        if(!BlossomHooks.modules.on("network")){detach();return;}
        try{
            ave mc=ave.A();Channel current=null;
            if(mc.u()!=null){java.lang.reflect.Field field=ek.class.getDeclaredField("k");field.setAccessible(true);current=(Channel)field.get(mc.u().a());}
            if(current!=channel){if(channel!=null)disconnects++;detach();channel=current;if(channel!=null){Channel c=channel;c.eventLoop().execute(()->{if(c.pipeline().get("blossom-bytes")==null)c.pipeline().addFirst("blossom-bytes",observer(true));if(c.pipeline().get("packet_handler")!=null&&c.pipeline().get("blossom-packets")==null)c.pipeline().addBefore("packet_handler","blossom-packets",observer(false));});}}
            long now=System.nanoTime();if(now-sample<1000000000L)return;double seconds=sample==0?1:(now-sample)/1e9;sample=now;
            long in=incoming.get(),out=outgoing.get(),ip=inPackets.get(),op=outPackets.get();
            bdc info=mc.h==null||mc.u()==null?null:mc.u().a(mc.h.aK());int ping=info==null?-1:info.c();history[cursor]=ping;cursor=(cursor+1)%history.length;count=Math.min(history.length,count+1);
            summary=channel==null?"Disconnected ("+disconnects+" changes)":"Server latency: "+(ping<0?"unknown":ping+" ms")+"\nIn "+Math.round((in-oldIn)/seconds)+" B/s | Out "+Math.round((out-oldOut)/seconds)+" B/s\nPackets/s "+Math.round((ip-oldIP)/seconds)+" in / "+Math.round((op-oldOP)/seconds)+" out";
            oldIn=in;oldOut=out;oldIP=ip;oldOP=op;
        }catch(Exception e){summary="Connection diagnostics unavailable";detach();}
    }
    private static ChannelDuplexHandler observer(final boolean bytes){return new ChannelDuplexHandler(){
        public void channelRead(ChannelHandlerContext ctx,Object message)throws Exception{if(bytes&&message instanceof ByteBuf)incoming.addAndGet(((ByteBuf)message).readableBytes());else if(!bytes)inPackets.incrementAndGet();super.channelRead(ctx,message);}
        public void write(ChannelHandlerContext ctx,Object message,ChannelPromise promise)throws Exception{if(bytes&&message instanceof ByteBuf)outgoing.addAndGet(((ByteBuf)message).readableBytes());else if(!bytes)outPackets.incrementAndGet();super.write(ctx,message,promise);}
    };}
    private static void detach(){if(channel!=null){Channel c=channel;c.eventLoop().execute(()->{if(c.pipeline().get("blossom-bytes")!=null)c.pipeline().remove("blossom-bytes");if(c.pipeline().get("blossom-packets")!=null)c.pipeline().remove("blossom-packets");});channel=null;}}
    public static String[] lines(){return summary.split("\n");}
    public static void graph(int y,int color){int max=1;for(int v:history)max=Math.max(max,v);for(int n=0;n<count;n++){int v=history[(cursor-count+n+history.length)%history.length];if(v<0)continue;int h=Math.max(1,v*25/max);avp.a(4+n*2,y+25-h,5+n*2,y+25,color);}}
}
