package dev.blossom;
import dev.blossom.agent.ViewTransformer;
import org.objectweb.asm.*;
import java.util.*;
import java.util.jar.*;
public final class ViewTransformTest {
    public static void run() throws Exception {
        try(JarFile jar=new JarFile(".deps/client.jar")){
            for(String name:new String[]{"avo","adm","bfk","biu","auz","bfr","bnn"}){
                byte[] in=read(jar.getInputStream(jar.getJarEntry(name+".class")));byte[] out=ViewTransformer.transform(name,in);
                if(out==null)throw new AssertionError("No view hooks: "+name);
                final Map<String,Integer> calls=new HashMap<String,Integer>();
                new ClassReader(out).accept(new ClassVisitor(Opcodes.ASM9){
                    public MethodVisitor visitMethod(int a,String n,String d,String s,String[] ex){return new MethodVisitor(Opcodes.ASM9){
                        public void visitMethodInsn(int op,String owner,String m,String desc,boolean itf){if(owner.startsWith("dev/blossom/integration/"))calls.put(m,calls.getOrDefault(m,0)+1);}
                    };}
                },0);
                if(name.equals("bfk")){check(calls,"turn",2);check(calls,"update",1);}
                if(name.equals("avo"))check(calls,"xp",1);
                if(name.equals("adm"))check(calls,"angle",1);
                if(name.equals("bnn"))check(calls,"reload",1);
                if(name.equals("bfr")){check(calls,"render",2);check(calls,"bind",2);check(calls,"horizonList",2);check(calls,"horizonVbo",1);check(calls,"horizonDraw",1);}
                if(name.equals("biu")||name.equals("auz"))if(!calls.containsKey("yaw")||!calls.containsKey("pitch"))throw new AssertionError("Missing billboard angles");
                System.out.println("View hook coverage "+name+": "+calls);
            }
        }
    }
    private static void check(Map<String,Integer> calls,String key,int expected){if(calls.getOrDefault(key,0)!=expected)throw new AssertionError(key+": "+calls);}
    private static byte[] read(java.io.InputStream in)throws Exception{try(java.io.InputStream s=in;java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=s.read(b))!=-1)out.write(b,0,n);return out.toByteArray();}}
}
