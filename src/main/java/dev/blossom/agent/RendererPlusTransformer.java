package dev.blossom.agent;
import org.objectweb.asm.*;
/** Conservative additions around the vanilla renderer, preserving existing frames and fallbacks. */
public final class RendererPlusTransformer {
    private static final String P="dev/blossom/integration/BlossomPerformance",V="dev/blossom/integration/BlossomVisibility",L="dev/blossom/integration/BlossomLightmap";
    public static byte[] transform(String name,byte[] bytes){
        if(!"bff".equals(name)&&!"bfd".equals(name)&&!"bho".equals(name)&&!"bfr".equals(name)&&!"beb".equals(name)&&!"bfk".equals(name)&&!"bly".equals(name)&&!"ave".equals(name))return null;
        ClassReader reader=new ClassReader(bytes);ClassWriter writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
        reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
            public MethodVisitor visitMethod(int access,String method,String desc,String sig,String[] exceptions){
                MethodVisitor base=super.visitMethod(access,method,desc,sig,exceptions);
                boolean setup=name.equals("bfr")&&method.equals("a")&&desc.equals("(Lpk;DLbia;IZ)V");
                boolean particle=name.equals("beb")&&method.equals("a")&&desc.equals("(Lbfd;Lpk;FFFFFF)V");
                boolean sort=name.equals("bfd")&&method.equals("a")&&desc.equals("(FFF)V");
                boolean deletion=name.equals("bly")&&method.equals("c")&&desc.equals("()V");
                return new MethodVisitor(Opcodes.ASM9,base){
                    public void visitCode(){
                        super.visitCode();
                        if(sort){
                            super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"bfd","d","Ljava/nio/FloatBuffer;");
                            super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"bfd","b","Ljava/nio/IntBuffer;");
                            super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"bfd","e","I");
                            super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"bfd","m","Lbmu;");super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"bmu","f","()I",false);
                            String[] offsets={"j","k","l"};for(int i=0;i<3;i++){super.visitVarInsn(Opcodes.FLOAD,i+1);super.visitInsn(Opcodes.F2D);super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"bfd",offsets[i],"D");super.visitInsn(Opcodes.DADD);super.visitInsn(Opcodes.D2F);}
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomMesh","sort","(Ljava/nio/FloatBuffer;Ljava/nio/IntBuffer;IIFFF)Z",false);
                            Label fallback=new Label();super.visitJumpInsn(Opcodes.IFEQ,fallback);super.visitInsn(Opcodes.RETURN);super.visitLabel(fallback);super.visitFrame(Opcodes.F_SAME,0,null,0,null);
                        }
                        if(setup){super.visitVarInsn(Opcodes.ALOAD,1);super.visitVarInsn(Opcodes.DLOAD,2);super.visitVarInsn(Opcodes.ALOAD,4);super.visitMethodInsn(Opcodes.INVOKESTATIC,V,"camera","(Lpk;DLbia;)V",false);}
                        if(deletion){super.visitVarInsn(Opcodes.ALOAD,0);super.visitMethodInsn(Opcodes.INVOKESTATIC,L,"deleted","(Ljava/lang/Object;)V",false);}
                        if(particle){
                            super.visitVarInsn(Opcodes.ALOAD,0);super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"beb","h","F");
                            for(int i=3;i<=8;i++)super.visitVarInsn(Opcodes.FLOAD,i);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,V,"particle","(Lbeb;FFFFFFF)Z",false);
                            Label draw=new Label();super.visitJumpInsn(Opcodes.IFNE,draw);super.visitInsn(Opcodes.RETURN);super.visitLabel(draw);super.visitFrame(Opcodes.F_SAME,0,null,0,null);
                        }
                    }
                    public void visitIntInsn(int opcode,int operand){
                        if(name.equals("bff")&&method.equals("<init>")&&opcode==Opcodes.NEWARRAY&&operand==Opcodes.T_INT){super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomRegionCache","lights","(I)[I",false);return;}
                        super.visitIntInsn(opcode,operand);
                    }
                    public void visitTypeInsn(int opcode,String type){
                        if(name.equals("bff")&&method.equals("<init>")&&opcode==Opcodes.ANEWARRAY&&type.equals("alz")){super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomRegionCache","states","(I)[Lalz;",false);return;}
                        super.visitTypeInsn(opcode,type);
                    }
                    public void visitMethodInsn(int op,String owner,String m,String d,boolean itf){
                        if(name.equals("bho")&&method.equals("<init>")&&owner.equals("com/google/common/collect/Queues")&&m.equals("newArrayBlockingQueue")&&d.equals("(I)Ljava/util/concurrent/ArrayBlockingQueue;")){
                            // Both capacities are created here; only the 100-entry job queue is replaced.
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,P,"dispatcherQueue","(I)Ljava/util/concurrent/BlockingQueue;",false);return;
                        }
                        if(name.equals("bfk")&&owner.equals("blz")&&m.equals("d")&&d.equals("()V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,L,"upload","(Lblz;)V",false);return;}
                        if(name.equals("ave")&&owner.equals("org/lwjgl/opengl/Display")&&m.equals("update")&&d.equals("()V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,P,"displayUpdate",d,false);return;}
                        if(name.equals("ave")&&owner.equals("org/lwjgl/opengl/Display")&&m.equals("sync")&&d.equals("(I)V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,P,"sync",d,false);return;}
                        if(name.equals("ave")&&owner.equals("bfk")&&m.equals("a")&&d.equals("(FJ)V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomGpu","render","(Lbfk;FJ)V",false);return;}
                        if(name.equals("ave")&&owner.equals("avj")&&m.equals("a")&&d.equals("(Ljava/io/File;IILbfw;)Leu;")){super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomScreenshots","capture",d,false);return;}
                        super.visitMethodInsn(op,owner,m,d,itf);
                    }
                };
            }
        },0);return writer.toByteArray();
    }
}
