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
            for(String name:new String[]{"beb","bly","nt","bht","bct","bbs","bfr","bho","bjh","bfn","bbj","bky","bec","bjl","bkp","ave","avo","bet","avb","aya","bfk","axp","avf"}) {
                byte[] bytes;
                try(InputStream in=game.getInputStream(game.getJarEntry(name+".class"));ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                    byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);bytes=out.toByteArray();
                }
                byte[] patched=transformer.transform(null,name,null,null,bytes);
                if(patched==null)throw new AssertionError("Missing target "+name);
                java.util.Set<String> calls=new java.util.HashSet<String>();
                int[] hooks={0};new ClassReader(patched).accept(new ClassVisitor(Opcodes.ASM9){
                    public MethodVisitor visitMethod(int a,String m,String d,String s,String[] e){return new MethodVisitor(Opcodes.ASM9){
                        public void visitMethodInsn(int op,String owner,String method,String desc,boolean itf){if(owner.startsWith("dev/blossom/integration/Blossom")){hooks[0]++;calls.add(method);}}
                    };}
                },0);
                if(name.equals("nt")&&!calls.containsAll(java.util.Arrays.asList("sectionStart","sectionEnd")))throw new AssertionError("Profiler section hooks missing");
                if(name.equals("bht")&&!calls.containsAll(java.util.Arrays.asList("begin","end")))throw new AssertionError("Chunk timing wrappers missing");
                if(name.equals("bho")&&!calls.containsAll(java.util.Arrays.asList("queues","drainUploads")))throw new AssertionError("Upload queue optimization/timing missing");
                if(name.equals("beb")&&!calls.contains("particle"))throw new AssertionError("Particle culling hook missing");
                if(name.equals("bly")&&!calls.contains("deleted"))throw new AssertionError("Texture invalidation hook missing");
                if(name.equals("bfk")&&!calls.contains("upload"))throw new AssertionError("Lightmap hook missing");
                if(name.equals("bho")&&!calls.contains("dispatcherQueue"))throw new AssertionError("Chunk priority hook missing");
                if(name.equals("ave")&&!calls.containsAll(java.util.Arrays.asList("displayUpdate","sync","render","capture")))throw new AssertionError("Frame/GPU/screenshot hooks missing");
                String[] required=name.equals("bct")?new String[]{"compiled","clearModel","beginModels","endModels"}:name.equals("bbs")?new String[]{"beginFace","endFace"}:name.equals("bfr")?new String[]{"near","buildNear","uploadDeadline"}:name.equals("bho")?new String[]{"workerCount"}:name.equals("ave")?new String[]{"worldGc"}:new String[0];
                if(!calls.containsAll(java.util.Arrays.asList(required)))throw new AssertionError("Missing performance hook: "+name);
                if(name.equals("bfn")&&!calls.containsAll(java.util.Arrays.asList("useSwing","render","swing","bowScale","equipStack")))throw new AssertionError("Legacy item hook missing");
                if(name.equals("bfk")&&!calls.contains("eye"))throw new AssertionError("Sneak camera hook missing");
                if(name.equals("bjh")&&!calls.containsAll(java.util.Arrays.asList("glintColor","glintTranslate","glintScale","glintUV")))throw new AssertionError("Glint hook missing");
                if(name.equals("bjl")&&!calls.contains("damage"))throw new AssertionError("Legacy damage hook missing");
                if(name.equals("bfn")&&!(calls.contains("begin")&&calls.contains("end")&&calls.contains("arm")))throw new AssertionError("First-person left hand hooks missing");
                if(name.equals("bbj")&&!calls.contains("pose"))throw new AssertionError("Left arm pose hook missing");
                if(name.equals("bky")&&!(calls.contains("attach")&&calls.contains("endThird")))throw new AssertionError("Third-person left hand hook missing");
                if(name.equals("bfk")&&!calls.contains("hitTilt"))throw new AssertionError("Hit tilt amplitude hook missing");
                if(name.equals("axp")&&!calls.contains("pauseMenuInit"))throw new AssertionError("Pause menu hook missing");
                if(name.equals("avf")&&!calls.contains("rawMouse"))throw new AssertionError("Raw mouse hook missing");
                if(name.equals("bfk")&&!calls.contains("cameraFov"))throw new AssertionError("Camera FOV hook missing");
                if(name.equals("ave")&&!calls.contains("mouseWheel"))throw new AssertionError("Zoom scroll hook missing");
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
                        public void visitMethodInsn(int op,String owner,String method,String desc,boolean itf){if(owner.equals("avb")&&method.equals("h"))throw new AssertionError("Controls must read remapped keys, not defaults");if(owner.equals("bew")&&method.equals("d"))throw new AssertionError("Do not force player sprint state");}
                    };}
                },0);
            }
            if(sprintReads[0]<2)throw new AssertionError("Sprint binding must be used for input and held-key query");
            if(transformer.transform(null,"bew",null,null,new byte[0])!=null)throw new AssertionError("Vanilla player mechanics must remain unmodified");
        }
        try(InputStream in=TransformTest.class.getResourceAsStream("/dev/blossom/integration/BlossomLegacy.class")){
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9){
                public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){return new MethodVisitor(Opcodes.ASM9){
                    public void visitFieldInsn(int op,String owner,String field,String desc){if((op==Opcodes.PUTFIELD||op==Opcodes.PUTSTATIC)&&!owner.startsWith("dev/blossom/"))throw new AssertionError("Legacy visual wrote game state: "+owner+"."+field);}
                    public void visitMethodInsn(int op,String owner,String method,String desc,boolean itf){if(owner.startsWith("io/netty/")||owner.equals("bcy")||owner.equals("ek")||owner.equals("bda")||owner.equals("bew")&&(method.equals("bw")||method.equals("d")))throw new AssertionError("Legacy visual invoked gameplay/network mutation");}
                };}
            },0);
        }
        System.out.println("Render-only legacy animation regression checks passed");
        System.out.println("Sprint binding and vanilla player-mechanics regression checks passed");
        System.out.println("Real 1.8.9 bytecode transform checks passed: 15 classes");
    }
}
