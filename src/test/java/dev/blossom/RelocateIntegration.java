package dev.blossom;
import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
/** Build step: Java cannot import notch/default-package classes from a named package.
 * Compile the small bridge in the default package, then move ONLY our classes.
 * This preserves Mojang's signatures and avoids a signed-package collision.
 */
public final class RelocateIntegration {
    public static void main(String[] args)throws Exception {
        Path root=Paths.get(args[0]);
        Remapper mapper=new Remapper(){
            @Override public String map(String name){return name.startsWith("Blossom")?"dev/blossom/integration/"+name:name;}
        };
        List<Path> classes=new ArrayList<Path>();
        try(java.util.stream.Stream<Path> stream=Files.walk(root)){stream.filter(p->p.toString().endsWith(".class")).forEach(classes::add);}
        for(Path file:classes){
            ClassReader reader=new ClassReader(Files.readAllBytes(file));ClassWriter writer=new ClassWriter(0);
            reader.accept(new ClassRemapper(writer,mapper),0);
            Path target=root.resolve(mapper.map(reader.getClassName())+".class");Files.createDirectories(target.getParent());Files.write(target,writer.toByteArray());
            if(!target.equals(file))Files.delete(file);
        }
    }
}
