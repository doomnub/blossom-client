package dev.blossom.agent;
import org.objectweb.asm.*;
/** Allocation-only substitutions in vanilla terrain traversal; all visibility decisions stay intact. */
public final class TraversalTransformer {
    public static byte[] transform(String name,byte[] bytes){
        if(!"bfr".equals(name))return null;
        ClassReader reader=new ClassReader(bytes);ClassWriter writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
        final int[] sites=new int[2];
        reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
            public MethodVisitor visitMethod(int access,String method,String desc,String sig,String[] exceptions){
                MethodVisitor base=super.visitMethod(access,method,desc,sig,exceptions);
                if(!method.equals("a")||!desc.equals("(Lpk;DLbia;IZ)V"))return base;
                return new MethodVisitor(Opcodes.ASM9,base){
                    public void visitMethodInsn(int op,String owner,String m,String d,boolean itf){
                        if(op==Opcodes.INVOKESTATIC&&owner.equals("com/google/common/collect/Lists")&&m.equals("newLinkedList")&&d.equals("()Ljava/util/LinkedList;")){
                            sites[0]++;super.visitMethodInsn(op,"dev/blossom/integration/BlossomTraversal","queue","()Ljava/util/Queue;",false);return;
                        }
                        if(op==Opcodes.INVOKESTATIC&&owner.equals("cq")&&m.equals("values")&&d.equals("()[Lcq;")){
                            sites[1]++;super.visitMethodInsn(op,"dev/blossom/integration/BlossomTraversal","directions",d,false);return;
                        }
                        super.visitMethodInsn(op,owner,m,d,itf);
                    }
                    public void visitFrame(int type,int n,Object[] locals,int count,Object[] stack){
                        Object[] copy=locals==null?null:locals.clone();
                        for(int i=0;i<n;i++)if("java/util/LinkedList".equals(copy[i]))copy[i]="java/util/Queue";
                        super.visitFrame(type,n,copy,count,stack);
                    }
                };
            }
        },0);
        if(sites[0]!=1||sites[1]!=1){System.err.println("[Blossom] Terrain allocation hook skipped: unexpected layout");return null;}
        return writer.toByteArray();
    }
}
