package dev.blossom.agent;
import org.objectweb.asm.*;
import java.util.*;
/** Exact vanilla hooks plus exception-safe wrappers. Original method frames are retained. */
public final class BenchmarkTransformer {
    private static final String BENCH="dev/blossom/integration/BlossomBenchmark";
    private static final class Wrap {int access;String name,desc,sig,section;String[] exceptions;Wrap(int a,String n,String d,String s,String[] e,String section){access=a;name=n;desc=d;sig=s;exceptions=e;this.section=section;}}
    public static byte[] transform(String name,byte[] bytes){
        if(name==null)return null;
        boolean integration=name.equals("dev/blossom/integration/BlossomHud")||name.equals("dev/blossom/core/cloth/Cloth")||name.equals("dev/blossom/integration/BlossomCloth");
        if(!name.equals("nt")&&!name.equals("bht")&&!name.equals("bho")&&!integration)return null;
        ClassReader reader=new ClassReader(bytes);ClassWriter writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);List<Wrap> wrappers=new ArrayList<Wrap>();
        reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
            public MethodVisitor visitMethod(int access,String method,String desc,String sig,String[] exceptions){
                String section=null;
                if(name.equals("bht")&&desc.equals("(FFFLbhn;)V")){if(method.equals("b"))section="blossom.chunk.rebuild";if(method.equals("a"))section="blossom.chunk.sort";}
                if(name.equals("bho")&&method.equals("a")&&desc.equals("(J)Z"))section="blossom.chunk.uploads";
                if(name.equals("dev/blossom/integration/BlossomHud")&&method.equals("draw")&&desc.equals("()V"))section="blossom.hud";
                if(name.equals("dev/blossom/integration/BlossomCloth")&&method.equals("draw")&&desc.equals("(Ldev/blossom/core/cloth/Cloth;)V"))section="blossom.cape.draw";
                if(name.equals("dev/blossom/core/cloth/Cloth")&&method.equals("advance")&&desc.equals("(DLdev/blossom/core/cloth/ClothSettings;)V"))section="blossom.cape.simulation";
                if(section!=null){wrappers.add(new Wrap(access,method,desc,sig,exceptions,section));return super.visitMethod(access, "blossom$timed$"+method,desc,sig,exceptions);}
                MethodVisitor base=super.visitMethod(access,method,desc,sig,exceptions);
                if(!name.equals("nt"))return base;
                boolean begin=method.equals("a")&&desc.equals("(Ljava/lang/String;)V"),end=method.equals("b")&&desc.equals("()V");
                if(!begin&&!end)return base;
                return new MethodVisitor(Opcodes.ASM9,base){public void visitCode(){super.visitCode();super.visitVarInsn(Opcodes.ALOAD,0);if(begin)super.visitVarInsn(Opcodes.ALOAD,1);super.visitMethodInsn(Opcodes.INVOKESTATIC,BENCH,begin?"sectionStart":"sectionEnd",begin?"(Ljava/lang/Object;Ljava/lang/String;)V":"(Ljava/lang/Object;)V",false);}};
            }
            public void visitEnd(){for(Wrap w:wrappers)wrap(writer,name,w);super.visitEnd();}
        },0);
        return writer.toByteArray();
    }
    private static void wrap(ClassWriter writer,String owner,Wrap w){
        MethodVisitor m=writer.visitMethod(w.access,w.name,w.desc,w.sig,w.exceptions);m.visitCode();
        boolean stat=(w.access&Opcodes.ACC_STATIC)!=0;Type[] args=Type.getArgumentTypes(w.desc);Type result=Type.getReturnType(w.desc);
        int local=stat?0:1;List<Object> locals=new ArrayList<Object>();if(!stat)locals.add(owner);
        for(Type t:args){local+=t.getSize();locals.add(frameType(t));}
        int timer=local;locals.add(Opcodes.LONG);
        m.visitLdcInsn(w.section);m.visitMethodInsn(Opcodes.INVOKESTATIC,BENCH,"begin","(Ljava/lang/String;)J",false);m.visitVarInsn(Opcodes.LSTORE,timer);
        Label start=new Label(),end=new Label(),handler=new Label();m.visitTryCatchBlock(start,end,handler,null);m.visitLabel(start);
        if(owner.equals("bho")){
            for(String field:new String[]{"d","h","e"}){m.visitVarInsn(Opcodes.ALOAD,0);m.visitFieldInsn(Opcodes.GETFIELD,owner,field,field.equals("h")?"Ljava/util/Queue;":"Ljava/util/concurrent/BlockingQueue;");}
            m.visitMethodInsn(Opcodes.INVOKESTATIC,BENCH,"queues","(Ljava/util/concurrent/BlockingQueue;Ljava/util/Queue;Ljava/util/concurrent/BlockingQueue;)V",false);
        }
        int slot=0;if(!stat){m.visitVarInsn(Opcodes.ALOAD,0);slot=1;}for(Type t:args){m.visitVarInsn(t.getOpcode(Opcodes.ILOAD),slot);slot+=t.getSize();}
        m.visitMethodInsn(stat?Opcodes.INVOKESTATIC:Opcodes.INVOKESPECIAL,owner,"blossom$timed$"+w.name,w.desc,false);
        int output=timer+2;if(result.getSort()!=Type.VOID)m.visitVarInsn(result.getOpcode(Opcodes.ISTORE),output);
        m.visitLabel(end);finish(m,w.section,timer,true);
        if(result.getSort()!=Type.VOID)m.visitVarInsn(result.getOpcode(Opcodes.ILOAD),output);
        m.visitInsn(result.getOpcode(Opcodes.IRETURN));
        m.visitLabel(handler);m.visitFrame(Opcodes.F_FULL,locals.size(),locals.toArray(),1,new Object[]{"java/lang/Throwable"});
        m.visitVarInsn(Opcodes.ASTORE,output);finish(m,w.section,timer,false);m.visitVarInsn(Opcodes.ALOAD,output);m.visitInsn(Opcodes.ATHROW);m.visitMaxs(0,0);m.visitEnd();
    }
    private static void finish(MethodVisitor m,String section,int timer,boolean success){m.visitLdcInsn(section);m.visitVarInsn(Opcodes.LLOAD,timer);m.visitInsn(success?Opcodes.ICONST_1:Opcodes.ICONST_0);m.visitMethodInsn(Opcodes.INVOKESTATIC,BENCH,"end","(Ljava/lang/String;JZ)V",false);}
    private static Object frameType(Type t){switch(t.getSort()){case Type.FLOAT:return Opcodes.FLOAT;case Type.LONG:return Opcodes.LONG;case Type.DOUBLE:return Opcodes.DOUBLE;case Type.ARRAY:return t.getDescriptor();case Type.OBJECT:return t.getInternalName();default:return Opcodes.INTEGER;}}
}
