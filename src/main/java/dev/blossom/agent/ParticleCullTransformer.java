package dev.blossom.agent;
import org.objectweb.asm.*;
/** Independent billboard culling; does not install the legacy scheduling/buffer patch group. */
public final class ParticleCullTransformer {
    private static final String V="dev/blossom/integration/BlossomVisibility";
    public static byte[] transform(String name,byte[] bytes){
        if(!"bfr".equals(name)&&!"beb".equals(name))return null;
        ClassReader reader=new ClassReader(bytes);ClassWriter writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
        final int[] sites={0};
        reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
            public MethodVisitor visitMethod(int access,String method,String desc,String sig,String[] exceptions){
                MethodVisitor base=super.visitMethod(access,method,desc,sig,exceptions);
                final boolean setup=name.equals("bfr")&&method.equals("a")&&desc.equals("(Lpk;DLbia;IZ)V");
                final boolean particle=name.equals("beb")&&method.equals("a")&&desc.equals("(Lbfd;Lpk;FFFFFF)V");
                if(!setup&&!particle)return base;
                sites[0]++;
                return new MethodVisitor(Opcodes.ASM9,base){
                    public void visitCode(){
                        super.visitCode();
                        if(setup){super.visitVarInsn(Opcodes.ALOAD,1);super.visitVarInsn(Opcodes.DLOAD,2);super.visitVarInsn(Opcodes.ALOAD,4);super.visitMethodInsn(Opcodes.INVOKESTATIC,V,"camera","(Lpk;DLbia;)V",false);}
                        if(particle){
                            super.visitVarInsn(Opcodes.ALOAD,0);super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"beb","h","F");
                            for(int i=3;i<=8;i++)super.visitVarInsn(Opcodes.FLOAD,i);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,V,"particle","(Lbeb;FFFFFFF)Z",false);
                            Label draw=new Label();super.visitJumpInsn(Opcodes.IFNE,draw);super.visitInsn(Opcodes.RETURN);super.visitLabel(draw);super.visitFrame(Opcodes.F_SAME,0,null,0,null);
                        }
                    }
                };
            }
        },0);
        if(sites[0]!=1)return null;
        return writer.toByteArray();
    }
}
