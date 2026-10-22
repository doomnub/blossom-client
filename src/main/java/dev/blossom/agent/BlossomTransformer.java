package dev.blossom.agent;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import org.objectweb.asm.*;
/** Exact 1.8.9 notch names verified from MCP SRG; does not modify networking. */
public final class BlossomTransformer implements ClassFileTransformer {
    @Override public byte[] transform(ClassLoader loader,String name,Class<?> type,ProtectionDomain domain,byte[] bytes) {
        if(!"ave".equals(name) && !"avo".equals(name) && !"bet".equals(name)) return null;
        ClassReader reader=new ClassReader(bytes);
        ClassWriter writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
        final int[] hooks={0};
        reader.accept(new ClassVisitor(Opcodes.ASM9,writer) {
            @Override public MethodVisitor visitMethod(int access,String method,String desc,String sig,String[] ex) {
                MethodVisitor base=super.visitMethod(access,method,desc,sig,ex);
                final boolean tick=name.equals("ave")&&method.equals("s")&&desc.equals("()V");
                final boolean hud=name.equals("avo")&&method.equals("a")&&desc.equals("(F)V");
                final boolean cape=name.equals("bet")&&method.equals("k")&&desc.equals("()Ljy;");
                if(tick||hud||cape) hooks[0]++;
                return new MethodVisitor(Opcodes.ASM9,base) {
                    @Override public void visitMethodInsn(int opcode,String owner,String m,String d,boolean itf) {
                        super.visitMethodInsn(opcode,owner,m,d,itf);
                        if(tick && owner.equals("org/lwjgl/input/Mouse") && m.equals("next") && d.equals("()Z")) {
                            super.visitInsn(Opcodes.DUP);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,"BlossomHooks","mouseEvent","(Z)V",false);
                        }
                    }
                    @Override public void visitInsn(int opcode) {
                        if(opcode==Opcodes.RETURN && (tick||hud))
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,"BlossomHooks",tick?"tick":"hud","()V",false);
                        if(opcode==Opcodes.ARETURN && cape) {
                            super.visitVarInsn(Opcodes.ALOAD,0);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,"BlossomHooks","cape","(Ljy;Lbet;)Ljy;",false);
                        }
                        super.visitInsn(opcode);
                    }
                };
            }
        },0);
        if(hooks[0]!=1) { System.err.println("[Blossom] Unsupported class layout: "+name+"; not patched"); return null; }
        System.out.println("[Blossom] Patched "+name);
        return writer.toByteArray();
    }
}
