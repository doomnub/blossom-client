package dev.blossom.agent;
import org.objectweb.asm.*;
/** Small, independent visual hooks; no legacy renderer optimization dependencies. */
public final class ViewTransformer {
    private static final String VIEW="dev/blossom/integration/BlossomView",FREE="dev/blossom/integration/BlossomFreeLook",SKY="dev/blossom/integration/BlossomSky";
    public static byte[] transform(String name,byte[] bytes){
        if(!java.util.Arrays.asList("avo","adm","bfk","biu","auz","bfr","bnn").contains(name))return null;
        ClassReader r=new ClassReader(bytes);ClassWriter w=new ClassWriter(r,ClassWriter.COMPUTE_MAXS);final int[] sites={0};
        r.accept(new ClassVisitor(Opcodes.ASM9,w){
            public MethodVisitor visitMethod(int access,String n,String d,String sig,String[] ex){
                MethodVisitor base=super.visitMethod(access,n,d,sig,ex);
                final boolean xp=name.equals("avo")&&n.equals("b")&&d.equals("(Lavr;I)V");
                final boolean angle=name.equals("adm")&&n.equals("c")&&d.equals("(F)F");
                final boolean frame=name.equals("bfk")&&n.equals("a")&&d.equals("(FJ)V");
                final boolean camera=name.equals("bfk")&&n.equals("f")&&d.equals("(F)V")||name.equals("biu")&&d.equals("(Ladm;Lavn;Lpk;Lpk;Lavh;F)V")||name.equals("auz")&&d.equals("(Lwn;Z)V")||name.equals("bfr")&&(d.equals("(Lpk;DLbia;IZ)V")||d.equals("(Lpk;D)Lorg/lwjgl/util/vector/Vector3f;"));
                final boolean sky=name.equals("bfr")&&n.equals("a")&&d.equals("(FI)V");
                final boolean reload=name.equals("bnn")&&n.equals("a")&&d.equals("(Ljava/util/List;)V");
                return new MethodVisitor(Opcodes.ASM9,base){
                    private boolean skyInserted,horizon;
                    public void visitCode(){super.visitCode();if(frame){call(FREE,"update","()V");sites[0]++;}}
                    private void call(String owner,String method,String desc){super.visitMethodInsn(Opcodes.INVOKESTATIC,owner,method,desc,false);}
                    public void visitFieldInsn(int op,String owner,String field,String desc){
                        super.visitFieldInsn(op,owner,field,desc);
                        if(op!=Opcodes.GETFIELD||!desc.equals("F"))return;
                        if(xp&&field.equals("bD")){call(VIEW,"xp","(F)F");sites[0]++;}
                        if(camera&&(owner.equals("pk")||owner.equals("wn"))){
                            if(field.equals("y")||field.equals("A")){call(FREE,"yaw","(F)F");sites[0]++;}
                            if(field.equals("z")||field.equals("B")){call(FREE,"pitch","(F)F");sites[0]++;}
                        }
                    }
                    public void visitMethodInsn(int op,String owner,String method,String desc,boolean itf){
                        if(frame&&method.equals("c")&&desc.equals("(FF)V")&&owner.equals("bew")){call(FREE,"turn","(Lpk;FF)V");sites[0]++;return;}
                        if(sky&&!skyInserted&&owner.equals("bfl")&&method.equals("w")&&desc.equals("()V")){skyInserted=true;super.visitVarInsn(Opcodes.FLOAD,1);call(SKY,"render","(F)V");sites[0]++;}
                        if(sky&&owner.equals("bmj")&&method.equals("a")&&desc.equals("(Ljy;)V")){call(SKY,"bind","(Lbmj;Ljy;)V");sites[0]++;return;}
                        if(sky&&method.equals("X")&&desc.equals("()D")&&(owner.equals("bdb")||owner.equals("adm")))horizon=true;
                        if(horizon&&owner.equals("bfl")&&method.equals("o")&&desc.equals("(I)V")){call(SKY,"horizonList","(I)V");sites[0]++;return;}
                        if(horizon&&owner.equals("bmt")&&method.equals("a")&&desc.equals("(I)V")){call(SKY,"horizonVbo","(Lbmt;I)V");sites[0]++;return;}
                        if(horizon&&owner.equals("bfx")&&method.equals("b")&&desc.equals("()V")){call(SKY,"horizonDraw","(Lbfx;)V");sites[0]++;return;}
                        super.visitMethodInsn(op,owner,method,desc,itf);
                        if(sky&&owner.equals("bfx")&&method.equals("b")&&desc.equals("()V"))call(SKY,"afterCelestial","()V");
                        if(sky&&owner.equals("bfr")&&method.equals("r")&&desc.equals("()V")){super.visitVarInsn(Opcodes.FLOAD,1);call(SKY,"render","(F)V");sites[0]++;}
                    }
                    public void visitInsn(int op){
                        if(angle&&op==Opcodes.FRETURN){super.visitVarInsn(Opcodes.ALOAD,0);call(VIEW,"angle","(FLadm;)F");sites[0]++;}
                        if(reload&&op==Opcodes.RETURN){call(SKY,"reload","()V");sites[0]++;}
                        super.visitInsn(op);
                    }
                };
            }
        },0);
        int expected=name.equals("bfk")?21:name.equals("biu")?6:name.equals("auz")?2:name.equals("bfr")?18:1;
        if(sites[0]!=expected){System.err.println("[Blossom] Visual hooks skipped: unexpected "+name+" layout ("+sites[0]+")");return null;}
        return w.toByteArray();
    }
}
