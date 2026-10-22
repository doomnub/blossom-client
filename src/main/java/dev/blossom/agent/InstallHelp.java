package dev.blossom.agent;
import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.io.File;
public final class InstallHelp {
    public static void main(String[] args) throws Exception {
        String path=new File(InstallHelp.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        String help="Blossom Client - experimental 1.8.9 development build\n\n"
            +"1. Launch vanilla 1.8.9 once with your authenticated launcher.\n"
            +"2. Create a separate 1.8.9 installation; select Java 8.\n"
            +"3. Add this to its JVM arguments (keep your existing arguments):\n\n"
            +"\"-javaagent:"+path+"\"\n\n"
            +"4. Launch. Press Right Shift for Blossom settings.\n"
            +"Use a separate game directory for testing.\n"
            +"Remove the JVM argument to uninstall. No game JAR is modified.\n\n"
            +"Forge, Fabric and Lunar are not required. OptiFine is unverified.\n"
            +"This build has not passed full gameplay acceptance testing.";
        System.out.println(help);
        if(!GraphicsEnvironment.isHeadless()) {
            JTextArea text=new JTextArea(help);text.setEditable(false);
            JOptionPane.showMessageDialog(null,new JScrollPane(text),"Blossom setup",JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
