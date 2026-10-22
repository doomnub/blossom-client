package dev.blossom;
import dev.blossom.agent.BlossomTransformer;
import java.util.jar.*;
import java.io.*;
import org.objectweb.asm.*;
/** Exercises real Mojang bytecode, then verifies hook instruction presence. No fake game launch. */
public final class TransformTest {
    public static void main(String[] args)throws Exception {
        BlossomTransformer transformer=new BlossomTransformer();
        try(JarFile game=new JarFile(args[0])) {
            for(String name:new String[]{"ave","avo","bet"}) {
                byte[] bytes;
                try(InputStream in=game.getInputStream(game.getJarEntry(name+".class"));ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                    byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);bytes=out.toByteArray();
                }
                byte[] patched=transformer.transform(null,name,null,null,bytes);
                if(patched==null)throw new AssertionError("Missing target "+name);
                int[] hooks={0};new ClassReader(patched).accept(new ClassVisitor(Opcodes.ASM9){
                    public MethodVisitor visitMethod(int a,String m,String d,String s,String[] e){return new MethodVisitor(Opcodes.ASM9){
                        public void visitMethodInsn(int op,String owner,String method,String desc,boolean itf){if(owner.equals("BlossomHooks"))hooks[0]++;}
                    };}
                },0);
                if(hooks[0]<(name.equals("ave")?2:1))throw new AssertionError("Hook missing: "+name);
            }
        }
        System.out.println("Real 1.8.9 bytecode transform checks passed: 3 classes");
    }
}
