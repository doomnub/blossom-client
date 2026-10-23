package dev.blossom;
/** JVM verifier links transformed game classes without initializing Minecraft or graphics. */
public final class LinkageTest {
    public static void main(String[] args)throws Exception {
        for(String name:new String[]{"bec","bjl","bkp","ave","avo","bet","avb","aya","bfk","axp","avf","dev.blossom.integration.BlossomCloth","dev.blossom.integration.BlossomVisuals","dev.blossom.integration.BlossomNetwork","dev.blossom.integration.BlossomSliderScreen","dev.blossom.integration.BlossomHooks","dev.blossom.integration.BlossomCapes","dev.blossom.integration.BlossomMenu","dev.blossom.integration.BlossomHudEditor","dev.blossom.integration.BlossomControls","dev.blossom.integration.BlossomHud","dev.blossom.integration.BlossomModuleSettings","dev.blossom.integration.BlossomTextInput","dev.blossom.integration.BlossomCapeMenu","dev.blossom.integration.BlossomConfirm","dev.blossom.integration.BlossomCamera","dev.blossom.integration.BlossomButton","dev.blossom.integration.BlossomTheme","dev.blossom.integration.BlossomBindings","dev.blossom.integration.BlossomMouse"}) {
            Class<?> type=Class.forName(name,false,LinkageTest.class.getClassLoader());
            type.getDeclaredMethods();type.getDeclaredFields();type.getDeclaredConstructors();
            System.out.println("Verified and linked "+name);
        }
    }
}
