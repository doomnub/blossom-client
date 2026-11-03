package dev.blossom.agent;
import org.objectweb.asm.*;
/** Coalesce only fancy-cloud cells, flushing before every color mask and final state restore. */
public final class CloudBatchTransformer {
    private static final String H="dev/blossom/integration/BlossomCloudBatch";
    public static byte[] transform(String name,byte[] bytes){
        if(!"bfr".equals(name))return null;
        ClassReader reader=new ClassReader(bytes);ClassWriter writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
        final int[] sites=new int[4];
        reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
            public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){
                MethodVisitor base=super.visitMethod(a,n,d,s,e);
                if(!n.equals("c")||!d.equals("(FI)V"))return base;
                return new MethodVisitor(Opcodes.ASM9,base){
                    final Label start=new Label(),end=new Label(),handler=new Label();
                    private void hook(String method){super.visitMethodInsn(Opcodes.INVOKESTATIC,H,method,"()V",false);}
                    public void visitCode(){super.visitCode();hook("enter");super.visitLabel(start);}
                    public void visitMethodInsn(int op,String owner,String m,String desc,boolean itf){
                        if(owner.equals("bfd")&&m.equals("a")&&desc.equals("(ILbmu;)V")){
                            sites[0]++;super.visitMethodInsn(Opcodes.INVOKESTATIC,H,"begin","(Lbfd;ILbmu;)V",false);return;
                        }
                        if(owner.equals("bfx")&&m.equals("b")&&desc.equals("()V")){
                            sites[1]++;super.visitMethodInsn(Opcodes.INVOKESTATIC,H,"draw","(Lbfx;)V",false);return;
                        }
                        if(owner.equals("bfl")&&m.equals("a")&&desc.equals("(ZZZZ)V")){sites[2]++;hook("flush");}
                        if(owner.equals("bfl")&&m.equals("c")&&desc.equals("(FFFF)V")){sites[3]++;hook("flush");}
                        super.visitMethodInsn(op,owner,m,desc,itf);
                    }
                    public void visitInsn(int op){if(op==Opcodes.RETURN)hook("exit");super.visitInsn(op);}
                    public void visitMaxs(int stack,int locals){
                        super.visitLabel(end);super.visitTryCatchBlock(start,end,handler,null);super.visitLabel(handler);
                        super.visitFrame(Opcodes.F_FULL,3,new Object[]{"bfr",Opcodes.FLOAT,Opcodes.INTEGER},1,new Object[]{"java/lang/Throwable"});
                        hook("abort");super.visitInsn(Opcodes.ATHROW);super.visitMaxs(stack,locals);
                    }
                };
            }
        },0);
        if(sites[0]!=1||sites[1]!=1||sites[2]!=4||sites[3]!=1){
            System.err.println("[Blossom] Cloud batching skipped: unexpected layout");return null;
        }
        return writer.toByteArray();
    }
}
