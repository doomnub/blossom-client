package dev.blossom.agent;
import org.objectweb.asm.*;
/** Central font draw and width entry points cover standard in-game textual surfaces. */
public final class NickTransformer {
    public static byte[] transform(String name,byte[] bytes){
        if(!"avn".equals(name))return null;
        ClassReader reader=new ClassReader(bytes);ClassWriter writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
        final int[] sites={0};
        reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
            public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){
                MethodVisitor base=super.visitMethod(a,n,d,s,e);
                if(!((n.equals("b")&&d.equals("(Ljava/lang/String;FFIZ)I"))||(n.equals("a")&&d.equals("(Ljava/lang/String;)I"))||(n.equals("a")&&d.equals("(Ljava/lang/String;IIII)V"))))return base;
                sites[0]++;
                return new MethodVisitor(Opcodes.ASM9,base){
                    public void visitCode(){super.visitCode();super.visitVarInsn(Opcodes.ALOAD,1);super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomNick","text","(Ljava/lang/String;)Ljava/lang/String;",false);super.visitVarInsn(Opcodes.ASTORE,1);}
                };
            }
        },0);
        if(sites[0]!=3){System.err.println("[Blossom] Nick Hider skipped: unexpected font layout");return null;}
        return writer.toByteArray();
    }
}
