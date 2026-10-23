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
            for(String name:new String[]{"ave","avo","bet","avb","aya"}) {
                byte[] bytes;
                try(InputStream in=game.getInputStream(game.getJarEntry(name+".class"));ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                    byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);bytes=out.toByteArray();
                }
                byte[] patched=transformer.transform(null,name,null,null,bytes);
                if(patched==null)throw new AssertionError("Missing target "+name);
                java.util.Set<String> calls=new java.util.HashSet<String>();
                int[] hooks={0};new ClassReader(patched).accept(new ClassVisitor(Opcodes.ASM9){
                    public MethodVisitor visitMethod(int a,String m,String d,String s,String[] e){return new MethodVisitor(Opcodes.ASM9){
                        public void visitMethodInsn(int op,String owner,String method,String desc,boolean itf){if(owner.equals("dev/blossom/integration/BlossomHooks")){hooks[0]++;calls.add(method);}}
                    };}
                },0);
                if(name.equals("aya")&&!(calls.contains("mainMenuInit")&&calls.contains("mainMenuClick")))throw new AssertionError("Title-screen button hooks missing");
                if(hooks[0]<(name.equals("ave")?2:1))throw new AssertionError("Hook missing: "+name);
            }
        }
        // Read the actual vanilla constructor mapping instead of trusting obfuscated field names.
        try(JarFile game=new JarFile(args[0])) {
            final String[] binding={null};
            new ClassReader(game.getInputStream(game.getJarEntry("avh.class"))).accept(new ClassVisitor(Opcodes.ASM9){
                public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){return new MethodVisitor(Opcodes.ASM9){boolean sprint;
                    public void visitLdcInsn(Object value){if("key.sprint".equals(value))sprint=true;}
                    public void visitFieldInsn(int op,String owner,String field,String desc){if(sprint&&op==Opcodes.PUTFIELD&&owner.equals("avh")&&desc.equals("Lavb;")){binding[0]=field;sprint=false;}}
                };}
            },0);
            if(!"ae".equals(binding[0]))throw new AssertionError("Unexpected vanilla sprint binding");
            final int[] sprintReads={0};
            try(InputStream in=TransformTest.class.getResourceAsStream("/dev/blossom/integration/BlossomControls.class")){
                new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9){
                    public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){return new MethodVisitor(Opcodes.ASM9){
                        public void visitFieldInsn(int op,String owner,String field,String desc){if(owner.equals("avh")){if(field.equals("ai"))throw new AssertionError("Toggle controls must not intercept attack binding");if(field.equals(binding[0]))sprintReads[0]++;}}
                        public void visitMethodInsn(int op,String owner,String method,String desc,boolean itf){if(owner.equals("bew")&&method.equals("d"))throw new AssertionError("Do not force player sprint state");}
                    };}
                },0);
            }
            if(sprintReads[0]<2)throw new AssertionError("Sprint binding must be used for input and held-key query");
            if(transformer.transform(null,"bew",null,null,new byte[0])!=null)throw new AssertionError("Vanilla player mechanics must remain unmodified");
        }
        System.out.println("Sprint binding and vanilla player-mechanics regression checks passed");
        System.out.println("Real 1.8.9 bytecode transform checks passed: 5 classes");
    }
}
