package dev.blossom.agent;
import org.objectweb.asm.*;
/** Changes only the camera's box test; projection updates and vanilla fallback stay intact. */
public final class FrustumTransformer {
    public static byte[] transform(String name,byte[] bytes){
        if(!"bic".equals(name))return null;
        ClassReader reader=new ClassReader(bytes);ClassWriter writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
        final int[] sites={0};
        reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
            public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){
                MethodVisitor base=super.visitMethod(a,n,d,s,e);
                if(n.equals("<init>")&&d.equals("(Lbid;)V"))return new MethodVisitor(Opcodes.ASM9,base){
                    public void visitInsn(int op){
                        if(op==Opcodes.RETURN){
                            super.visitVarInsn(Opcodes.ALOAD,0);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomFrustum","enabled","()Z",false);
                            super.visitFieldInsn(Opcodes.PUTFIELD,"bic","blossom$fast","Z");
                        }
                        super.visitInsn(op);
                    }
                };
                if(!n.equals("b")||!d.equals("(DDDDDD)Z"))return base;
                return new MethodVisitor(Opcodes.ASM9,base){
                    public void visitMethodInsn(int op,String owner,String method,String desc,boolean itf){
                        if(op==Opcodes.INVOKEVIRTUAL&&owner.equals("bid")&&method.equals("b")&&desc.equals("(DDDDDD)Z")){
                            sites[0]++;super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"bic","blossom$fast","Z");super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomFrustum","visible","(Lbid;DDDDDDZ)Z",false);return;
                        }
                        super.visitMethodInsn(op,owner,method,desc,itf);
                    }
                };
            }
            public void visitEnd(){super.visitField(Opcodes.ACC_PRIVATE|Opcodes.ACC_FINAL,"blossom$fast","Z",null,null).visitEnd();super.visitEnd();}
        },0);
        if(sites[0]!=1){System.err.println("[Blossom] Fast frustum hook skipped: unexpected layout");return null;}
        return writer.toByteArray();
    }
}
