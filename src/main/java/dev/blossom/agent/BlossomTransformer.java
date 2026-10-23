package dev.blossom.agent;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import org.objectweb.asm.*;
/** Exact vanilla 1.8.9 signatures; no networking or player-physics patches. */
public final class BlossomTransformer implements ClassFileTransformer {
    private static final String HOOKS="dev/blossom/integration/BlossomHooks";
    @Override public byte[] transform(ClassLoader loader,String name,Class<?> type,ProtectionDomain domain,byte[] bytes) {
        if(!"ave".equals(name)&&!"avo".equals(name)&&!"bet".equals(name)&&!"avb".equals(name)&&!"aya".equals(name)&&!"bfk".equals(name)&&!"axp".equals(name)&&!"avf".equals(name))return null;
        ClassReader reader=new ClassReader(bytes);
        ClassWriter writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
        final int[] hooks={0};
        reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
            @Override public MethodVisitor visitMethod(int access,String method,String desc,String sig,String[] ex){
                MethodVisitor base=super.visitMethod(access,method,desc,sig,ex);
                final boolean raw=name.equals("avf")&&method.equals("c")&&desc.equals("()V");
                final boolean fov=name.equals("bfk")&&method.equals("a")&&desc.equals("(FZ)F");
                final boolean tick=name.equals("ave")&&method.equals("s")&&desc.equals("()V");
                final boolean frame=name.equals("ave")&&method.equals("av")&&desc.equals("()V");
                final boolean hud=name.equals("avo")&&method.equals("a")&&desc.equals("(F)V");
                final boolean crosshair=name.equals("avo")&&method.equals("b")&&desc.equals("()Z");
                final boolean cape=name.equals("bet")&&method.equals("k")&&desc.equals("()Ljy;");
                final boolean key=name.equals("avb")&&method.equals("d")&&desc.equals("()Z");
                final boolean menuInit=(name.equals("aya")||name.equals("axp"))&&method.equals("b")&&desc.equals("()V");
                final boolean menuClick=(name.equals("aya")||name.equals("axp"))&&method.equals("a")&&desc.equals("(Lavs;)V");
                if(tick||frame||hud||crosshair||cape||key||menuInit||menuClick||fov||raw)hooks[0]++;
                return new MethodVisitor(Opcodes.ASM9,base){
                    @Override public void visitCode(){
                        super.visitCode();
                        if(menuClick){super.visitVarInsn(Opcodes.ALOAD,0);super.visitVarInsn(Opcodes.ALOAD,1);super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"mainMenuClick","(Laxu;Lavs;)V",false);}
                        if(frame)super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"frame","()V",false);
                    }
                    @Override public void visitMethodInsn(int op,String owner,String m,String d,boolean itf){
                        super.visitMethodInsn(op,owner,m,d,itf);
                        if(tick&&owner.equals("org/lwjgl/input/Mouse")&&m.equals("getEventDWheel")&&d.equals("()I"))super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"mouseWheel","(I)I",false);
                        if(tick&&m.equals("next")&&d.equals("()Z")&&(owner.equals("org/lwjgl/input/Mouse")||owner.equals("org/lwjgl/input/Keyboard"))){
                            super.visitInsn(Opcodes.DUP);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,owner.endsWith("Mouse")?"mouseEvent":"keyboardEvent","(Z)V",false);
                        }
                    }
                    @Override public void visitInsn(int op){
                        if(op==Opcodes.RETURN&&raw){super.visitVarInsn(Opcodes.ALOAD,0);super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"rawMouse","(Lavf;)V",false);}
                        if(op==Opcodes.FRETURN&&fov){super.visitVarInsn(Opcodes.ILOAD,2);super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"cameraFov","(FZ)F",false);}
                        if(op==Opcodes.RETURN&&menuInit){
                            super.visitVarInsn(Opcodes.ALOAD,0);
                            super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,name,"n","Ljava/util/List;");
                            super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,name,"l","I");
                            super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,name,"m","I");
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,name.equals("axp")?"pauseMenuInit":"mainMenuInit","(Laxu;Ljava/util/List;II)V",false);
                        }
                        if(op==Opcodes.RETURN&&(tick||hud))super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,tick?"tick":"hud","()V",false);
                        if(op==Opcodes.ARETURN&&cape){super.visitVarInsn(Opcodes.ALOAD,0);super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"cape","(Ljy;Lbet;)Ljy;",false);}
                        if(op==Opcodes.IRETURN&&crosshair)super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"vanillaCrosshair","(Z)Z",false);
                        if(op==Opcodes.IRETURN&&key){super.visitVarInsn(Opcodes.ALOAD,0);super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"keyDown","(ZLavb;)Z",false);}
                        super.visitInsn(op);
                    }
                };
            }
        },0);
        int expected=name.equals("ave")||name.equals("avo")||name.equals("aya")||name.equals("axp")?2:1;
        if(hooks[0]!=expected){System.err.println("[Blossom] Unsupported class layout: "+name);return null;}
        System.out.println("[Blossom] Patched "+name);
        return writer.toByteArray();
    }
}
