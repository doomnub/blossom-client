package dev.blossom.agent;
import org.objectweb.asm.*;

/** Exact 1.8.9 performance hooks, separate from gameplay and cosmetic patches. */
public final class PerformanceTransformer {
    private static final String PERF="dev/blossom/integration/BlossomPerformance";
    public static byte[] transform(String name,byte[] bytes){
        if(name==null)return null;
        if(!name.equals("bho")&&!name.equals("bct")&&!name.equals("bbs")&&!name.equals("bfr")&&!name.equals("ave"))return null;
        ClassReader reader=new ClassReader(bytes);ClassWriter writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
        final int[] sites={0};
        reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
            @Override public void visitEnd(){
                if(name.equals("bct"))super.visitField(Opcodes.ACC_PRIVATE|Opcodes.ACC_SYNTHETIC,"blossomBatched","Z",null,null).visitEnd();
                super.visitEnd();
            }
            @Override public MethodVisitor visitMethod(int access,String method,String desc,String sig,String[] exceptions){
                MethodVisitor base=super.visitMethod(access,method,desc,sig,exceptions);
                if(name.equals("bho")&&method.equals("blossom$timed$a")&&desc.equals("(J)Z")){
                    base.visitCode();base.visitVarInsn(Opcodes.ALOAD,0);base.visitFieldInsn(Opcodes.GETFIELD,"bho","h","Ljava/util/Queue;");base.visitVarInsn(Opcodes.LLOAD,1);
                    base.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"drainUploads","(Ljava/util/Queue;J)Z",false);base.visitInsn(Opcodes.IRETURN);base.visitMaxs(3,3);base.visitEnd();return null;
                }
                boolean workers=name.equals("bho")&&method.equals("<init>")&&desc.equals("()V");
                boolean compile=name.equals("bct")&&method.equals("d")&&desc.equals("(F)V");
                boolean face=name.equals("bbs")&&method.equals("a")&&desc.equals("(Lbfd;F)V");
                boolean near=name.equals("bfr")&&method.equals("a")&&desc.equals("(Lcj;Lbht;)Z");
                boolean terrain=name.equals("bfr")&&method.equals("a")&&desc.equals("(Lpk;DLbia;IZ)V");
                boolean upload=name.equals("bfr")&&method.equals("a")&&desc.equals("(J)V");
                boolean world=name.equals("ave")&&method.equals("a")&&(desc.equals("(Lbdb;Ljava/lang/String;)V")||desc.equals("(Ljava/lang/String;Ljava/lang/String;Ladp;)V"));
                if(workers||compile||face||near||upload||terrain||world)sites[0]++;
                return new MethodVisitor(Opcodes.ASM9,base){
                    @Override public void visitCode(){
                        super.visitCode();
                        if(compile){super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"bct","u","I");super.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"clearModel","(I)V",false);}
                        if(upload){super.visitVarInsn(Opcodes.LLOAD,1);super.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"uploadDeadline","(J)J",false);super.visitVarInsn(Opcodes.LSTORE,1);}
                    }
                    @Override public void visitFieldInsn(int op,String owner,String field,String descriptor){
                        super.visitFieldInsn(op,owner,field,descriptor);
                        if(name.equals("bct")&&op==Opcodes.GETFIELD&&owner.equals("bct")&&field.equals("t")){
                            super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"bct","blossomBatched","Z");
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"compiled","(ZZ)Z",false);
                        }
                    }
                    @Override public void visitMethodInsn(int op,String owner,String m,String d,boolean itf){
                        if(face&&owner.equals("bfd")&&m.equals("a")&&d.equals("(ILbmu;)V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"beginFace","(Lbfd;ILbmu;)V",false);return;}
                        if(face&&owner.equals("bfx")&&m.equals("b")&&d.equals("()V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"endFace","(Lbfx;)V",false);return;}
                        if(world&&owner.equals("java/lang/System")&&m.equals("gc")){super.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"worldGc","()V",false);return;}
                        if(terrain&&owner.equals("bho")&&m.equals("b")&&d.equals("(Lbht;)Z")){super.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"buildNear","(Lbho;Lbht;)Z",false);return;}
                        if(compile&&owner.equals("org/lwjgl/opengl/GL11")&&m.equals("glEndList"))super.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"endModels","()V",false);
                        super.visitMethodInsn(op,owner,m,d,itf);
                        if(compile&&owner.equals("org/lwjgl/opengl/GL11")&&m.equals("glNewList"))super.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"beginModels","()V",false);
                    }
                    @Override public void visitInsn(int op){
                        if(workers&&op==Opcodes.ICONST_2){super.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"workerCount","()I",false);return;}
                        if(near&&op==Opcodes.IRETURN)super.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"near","(Z)Z",false);
                        if(compile&&op==Opcodes.RETURN){super.visitVarInsn(Opcodes.ALOAD,0);super.visitMethodInsn(Opcodes.INVOKESTATIC,PERF,"batchModels","()Z",false);super.visitFieldInsn(Opcodes.PUTFIELD,"bct","blossomBatched","Z");}
                        super.visitInsn(op);
                    }
                };
            }
        },0);
        int expected=name.equals("bfr")?3:name.equals("ave")?2:1;
        if(sites[0]!=expected){System.err.println("[Blossom] Skipped unmatched performance layout: "+name);return null;}
        return writer.toByteArray();
    }
}
