package dev.blossom.agent;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import org.objectweb.asm.*;
/** Exact vanilla 1.8.9 signatures; no networking or player-physics patches. */
public final class BlossomTransformer implements ClassFileTransformer {
    private static final String CHAT="dev/blossom/integration/BlossomChat";
    private static final String LEGACY="dev/blossom/integration/BlossomLegacy";
    private static final String HOOKS="dev/blossom/integration/BlossomHooks";
    @Override public byte[] transform(ClassLoader loader,String name,Class<?> type,ProtectionDomain domain,byte[] bytes) {
        byte[] measured=BenchmarkTransformer.transform(name,bytes);
        if(measured!=null)bytes=measured;
        byte[] optimized=PerformanceTransformer.transform(name,bytes);
        if(optimized==null)optimized=measured;
        if(optimized!=null)bytes=optimized;
        if(!"bgp".equals(name)&&!"avt".equals(name)&&!"bjh".equals(name)&&!"bfn".equals(name)&&!"bbj".equals(name)&&!"bky".equals(name)&&!"bec".equals(name)&&!"bjl".equals(name)&&!"bkp".equals(name)&&!"ave".equals(name)&&!"avo".equals(name)&&!"bet".equals(name)&&!"avb".equals(name)&&!"aya".equals(name)&&!"bfk".equals(name)&&!"axp".equals(name)&&!"avf".equals(name))return optimized;
        ClassReader reader=new ClassReader(bytes);
        ClassWriter writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
        final int[] hooks={0};
        reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
            @Override public MethodVisitor visitMethod(int access,String method,String desc,String sig,String[] ex){
                MethodVisitor base=super.visitMethod(access,method,desc,sig,ex);
                final boolean chatDraw=name.equals("avt")&&method.equals("a")&&desc.equals("(I)V");
                final boolean chatLayout=name.equals("avt")&&method.equals("a")&&desc.equals("(Leu;IIZ)V");
                final boolean chatPick=name.equals("avt")&&method.equals("a")&&desc.equals("(II)Leu;");
                final boolean chatReceive=name.equals("avt")&&method.equals("a")&&desc.equals("(Leu;I)V");
                final boolean itemSides=name.equals("bgp")&&method.equals("a")&&desc.equals("(Lbmi;Ljava/lang/String;I)Ljava/util/List;");
                final boolean hand=name.equals("bfn")&&method.equals("a")&&desc.equals("(F)V");
                final boolean bow=name.equals("bfn")&&method.equals("a")&&desc.equals("(FLbet;)V");
                final boolean equip=name.equals("bfn")&&method.equals("a")&&desc.equals("()V");
                final boolean camera=name.equals("bfk")&&method.equals("f")&&desc.equals("(F)V");
                final boolean glint=name.equals("bjh")&&method.equals("a")&&desc.equals("(Lboq;)V");
                final boolean arm=name.equals("bfn")&&method.equals("a")&&desc.equals("(Lbet;FF)V");
                final boolean pose=name.equals("bbj")&&method.equals("a")&&desc.equals("(FFFFFFLpk;)V");
                final boolean held=name.equals("bky")&&method.equals("a")&&desc.equals("(Lpr;FFFFFFF)V");
                final boolean tilt=name.equals("bfk")&&method.equals("d")&&desc.equals("(F)V");
                final boolean tint=name.equals("bjl")&&method.equals("a")&&desc.equals("(Lpr;FZ)Z");
                final boolean particles=name.equals("bec")&&method.equals("a")&&desc.equals("(IDDDDDD[I)Lbeb;");
                final boolean added=name.equals("bec")&&method.equals("a")&&desc.equals("(Lbeb;)V");
                final boolean cloth=name.equals("bkp")&&method.equals("a")&&desc.equals("(Lbet;FFFFFFF)V");
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
                if(chatDraw||chatLayout||chatPick||chatReceive||itemSides||bow||equip||camera||glint||hand||arm||pose||held||tilt||tint||particles||added||cloth||tick||frame||hud||crosshair||cape||key||menuInit||menuClick||fov||raw)hooks[0]++;
                return new MethodVisitor(Opcodes.ASM9,base){
                    @Override public void visitCode(){
                        super.visitCode();
                        if(chatDraw||chatLayout||chatPick)super.visitMethodInsn(Opcodes.INVOKESTATIC,CHAT,"beginDraw","()V",false);
                        if(chatReceive){
                            super.visitVarInsn(Opcodes.ALOAD,0);super.visitVarInsn(Opcodes.ALOAD,1);super.visitVarInsn(Opcodes.ILOAD,2);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,CHAT,"receive","(Lavt;Leu;I)Z",false);
                            Label vanilla=new Label();super.visitJumpInsn(Opcodes.IFEQ,vanilla);super.visitInsn(Opcodes.RETURN);super.visitLabel(vanilla);super.visitFrame(Opcodes.F_SAME,0,null,0,null);
                        }
                        if(itemSides){
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomItemModels","enabled","()Z",false);
                            Label vanilla=new Label();super.visitJumpInsn(Opcodes.IFEQ,vanilla);
                            super.visitVarInsn(Opcodes.ALOAD,1);super.visitVarInsn(Opcodes.ALOAD,2);super.visitVarInsn(Opcodes.ILOAD,3);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomItemModels","sides","(Lbmi;Ljava/lang/String;I)Ljava/util/List;",false);
                            super.visitInsn(Opcodes.ARETURN);super.visitLabel(vanilla);super.visitFrame(Opcodes.F_SAME,0,null,0,null);
                        }
                        if(hand){super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"bfn","d","Lzx;");super.visitVarInsn(Opcodes.FLOAD,1);super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"begin","(Lzx;F)V",false);}
                        if(glint){super.visitVarInsn(Opcodes.ALOAD,1);super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"beginGlint","(Lboq;)V",false);}
                        if(equip){super.visitVarInsn(Opcodes.ALOAD,0);super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"bfn","d","Lzx;");super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"equipStack","(Lzx;)Lzx;",false);super.visitFieldInsn(Opcodes.PUTFIELD,"bfn","d","Lzx;");}
                        if(tint){super.visitVarInsn(Opcodes.ILOAD,3);super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"damage","(Z)Z",false);super.visitVarInsn(Opcodes.ISTORE,3);}
                        if(hand){super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"bfn","d","Lzx;");super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomHands","begin","(Lzx;)V",false);}
                        if(held){super.visitVarInsn(Opcodes.ALOAD,1);super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomHands","beginThird","(Lpr;)V",false);}
                        if(added){super.visitVarInsn(Opcodes.ALOAD,1);super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomVisuals","added","(Lbeb;)V",false);}
                        if(particles){super.visitVarInsn(Opcodes.ILOAD,1);super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomVisuals","begin","(I)Z",false);Label yes=new Label();super.visitJumpInsn(Opcodes.IFNE,yes);super.visitInsn(Opcodes.ACONST_NULL);super.visitInsn(Opcodes.ARETURN);super.visitLabel(yes);super.visitFrame(Opcodes.F_SAME,0,null,0,null);}
                        if(cloth){
                            super.visitVarInsn(Opcodes.ALOAD,1);super.visitVarInsn(Opcodes.ALOAD,0);super.visitFieldInsn(Opcodes.GETFIELD,"bkp","a","Lbln;");super.visitVarInsn(Opcodes.FLOAD,4);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"cloth","(Lbet;Lbln;F)Z",false);
                            Label vanilla=new Label();super.visitJumpInsn(Opcodes.IFEQ,vanilla);super.visitInsn(Opcodes.RETURN);super.visitLabel(vanilla);super.visitFrame(Opcodes.F_SAME,0,null,0,null);
                        }
                        if(menuClick){super.visitVarInsn(Opcodes.ALOAD,0);super.visitVarInsn(Opcodes.ALOAD,1);super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"mainMenuClick","(Laxu;Lavs;)V",false);}
                        if(frame)super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"frame","()V",false);
                    }
                    @Override public void visitTypeInsn(int op,String type){
                        super.visitTypeInsn(op,name.equals("ave")&&op==Opcodes.NEW&&type.equals("awv")?"dev/blossom/integration/BlossomChatScreen":type);
                    }
                    @Override public void visitIntInsn(int op,int operand){
                        super.visitIntInsn(op,operand);
                        if(chatDraw&&op==Opcodes.SIPUSH&&operand==200)super.visitMethodInsn(Opcodes.INVOKESTATIC,CHAT,"fade","(I)I",false);
                    }
                    @Override public void visitMethodInsn(int op,String owner,String m,String d,boolean itf){
                        if(name.equals("ave")&&owner.equals("awv")&&m.equals("<init>")){super.visitMethodInsn(op,"dev/blossom/integration/BlossomChatScreen",m,d,itf);return;}
                        if(chatDraw&&owner.equals("avt")&&m.equals("a")&&d.equals("(IIIII)V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,CHAT,"background",d,false);return;}
                        if(name.equals("ave")&&owner.equals("org/lwjgl/opengl/Display")&&m.equals("setIcon")&&d.equals("([Ljava/nio/ByteBuffer;)I")){super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomIcon","set","([Ljava/nio/ByteBuffer;)I",false);return;}

                        if(hand&&owner.equals("bfn")&&m.equals("b")&&d.equals("(FF)V"))super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"useSwing","(F)F",false);
                        if(hand&&owner.equals("bfn")&&m.equals("a")&&d.equals("(Lpr;Lzx;Lbgr$b;)V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"render","(Lbfn;Lpr;Lzx;Lbgr$b;)V",false);return;}
                        if(bow&&owner.equals("bfl")&&m.equals("a")&&d.equals("(FFF)V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"bowScale",d,false);return;}
                        if(glint&&owner.equals("bjh")&&m.equals("a")&&d.equals("(Lboq;I)V"))super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"glintUV","()V",false);
                        if(glint&&owner.equals("bfl")&&m.equals("a")&&d.equals("(FFF)V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"glintScale",d,false);return;}
                        if(glint&&owner.equals("bfl")&&m.equals("b")&&d.equals("(FFF)V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"glintTranslate",d,false);return;}
                        if(arm&&owner.equals("bln")&&m.equals("b")&&d.equals("(Lbet;)V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomHands","arm","(Lbln;Lbet;)V",false);return;}
                        if(held&&owner.equals("bbj")&&m.equals("a")&&d.equals("(F)V")){super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomHands","attach","(Lbbj;F)V",false);return;}
                        if(tint&&owner.equals("org/lwjgl/opengl/GL11")&&m.equals("glTexEnv")&&d.equals("(IILjava/nio/FloatBuffer;)V")){super.visitVarInsn(Opcodes.ALOAD,1);super.visitVarInsn(Opcodes.ILOAD,3);super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomVisuals","tint","(Ljava/nio/FloatBuffer;Lpr;Z)Ljava/nio/FloatBuffer;",false);}
                        super.visitMethodInsn(op,owner,m,d,itf);
                        if(hand&&owner.equals("bet")&&m.equals("l")&&d.equals("(F)F"))super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"swing","(F)F",false);
                        if(camera&&owner.equals("pk")&&m.equals("aS")&&d.equals("()F")){super.visitVarInsn(Opcodes.FLOAD,1);super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"eye","(FF)F",false);}
                        if(tick&&owner.equals("org/lwjgl/input/Mouse")&&m.equals("getEventDWheel")&&d.equals("()I"))super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"mouseWheel","(I)I",false);
                        if(tick&&m.equals("next")&&d.equals("()Z")&&(owner.equals("org/lwjgl/input/Mouse")||owner.equals("org/lwjgl/input/Keyboard"))){
                            super.visitInsn(Opcodes.DUP);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,owner.endsWith("Mouse")?"mouseEvent":"keyboardEvent","(Z)V",false);
                        }
                    }
                    @Override public void visitLdcInsn(Object value){super.visitLdcInsn(value);if(chatDraw&&Double.valueOf(200d).equals(value))super.visitMethodInsn(Opcodes.INVOKESTATIC,CHAT,"fadeDouble","(D)D",false);if(glint&&Integer.valueOf(-8372020).equals(value))super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"glintColor","(I)I",false);if(tilt&&Float.valueOf(14f).equals(value))super.visitMethodInsn(Opcodes.INVOKESTATIC,HOOKS,"hitTilt","(F)F",false);}
                    @Override public void visitFieldInsn(int op,String owner,String field,String descriptor){super.visitFieldInsn(op,owner,field,descriptor);if(chatDraw&&op==Opcodes.GETFIELD&&owner.equals("avh")&&field.equals("q"))super.visitMethodInsn(Opcodes.INVOKESTATIC,CHAT,"opacity","(F)F",false);if(tint&&op==Opcodes.GETFIELD&&owner.equals("pr")&&field.equals("au")){super.visitVarInsn(Opcodes.ALOAD,1);super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomVisuals","hurt","(ILpr;)I",false);}}
                    @Override public void visitInsn(int op){
                        if(((chatDraw||chatLayout)&&op==Opcodes.RETURN)||(chatPick&&op==Opcodes.ARETURN))super.visitMethodInsn(Opcodes.INVOKESTATIC,CHAT,"endDraw","()V",false);
                        if(name.equals("avt")&&desc.equals("()I")&&op==Opcodes.IRETURN&&(method.equals("f")||method.equals("g")))super.visitMethodInsn(Opcodes.INVOKESTATIC,CHAT,method.equals("f")?"width":"height","(I)I",false);
                        if(name.equals("avt")&&method.equals("h")&&desc.equals("()F")&&op==Opcodes.FRETURN)super.visitMethodInsn(Opcodes.INVOKESTATIC,CHAT,"scale","(F)F",false);
                        if(op==Opcodes.RETURN&&glint)super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"endGlint","()V",false);
                        if(op==Opcodes.RETURN&&hand)super.visitMethodInsn(Opcodes.INVOKESTATIC,LEGACY,"end","()V",false);
                        if(op==Opcodes.RETURN&&hand)super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomHands","end","()V",false);
                        if(op==Opcodes.RETURN&&held)super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomHands","endThird","()V",false);
                        if(op==Opcodes.RETURN&&pose){super.visitVarInsn(Opcodes.ALOAD,0);super.visitVarInsn(Opcodes.ALOAD,7);super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomHands","pose","(Lbbj;Lpk;)V",false);}
                        if(particles&&op==Opcodes.ARETURN){super.visitVarInsn(Opcodes.ALOAD,0);super.visitVarInsn(Opcodes.ILOAD,1);for(int v=2;v<=12;v+=2)super.visitVarInsn(Opcodes.DLOAD,v);super.visitVarInsn(Opcodes.ALOAD,14);super.visitMethodInsn(Opcodes.INVOKESTATIC,"dev/blossom/integration/BlossomVisuals","finish","(Lbeb;Lbec;IDDDDDD[I)Lbeb;",false);}
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
        int expected=name.equals("avt")?4:name.equals("bfn")?4:name.equals("bfk")?3:name.equals("bec")||name.equals("ave")||name.equals("avo")||name.equals("aya")||name.equals("axp")?2:1;
        if(hooks[0]!=expected){System.err.println("[Blossom] Unsupported class layout: "+name);return null;}
        System.out.println("[Blossom] Patched "+name);
        return writer.toByteArray();
    }
}
