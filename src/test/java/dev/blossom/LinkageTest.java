package dev.blossom;
/** JVM verifier links transformed game classes without initializing Minecraft or graphics. */
public final class LinkageTest {
    public static void main(String[] args)throws Exception {
        for(String name:new String[]{"ave","avo","bet","dev.blossom.integration.BlossomHooks","dev.blossom.integration.BlossomCapes","dev.blossom.integration.BlossomMenu","dev.blossom.integration.BlossomHudEditor"}) {
            Class<?> type=Class.forName(name,false,LinkageTest.class.getClassLoader());
            type.getDeclaredMethods();type.getDeclaredFields();type.getDeclaredConstructors();
            System.out.println("Verified and linked "+name);
        }
    }
}
