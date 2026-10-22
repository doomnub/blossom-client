package dev.blossom.launcher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Standalone Swing launcher. All network/disk/game work runs off the event thread. */
public final class Launcher extends JPanel {
    private static final Color BG=new Color(20,18,25),CARD=new Color(31,28,37),PINK=new Color(255,131,181),TEXT=new Color(239,235,242),MUTED=new Color(170,163,182);
    private final Path root;
    private final Properties config=new Properties();
    private final ExecutorService worker=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"Blossom launcher worker");t.setDaemon(true);return t;});
    private final JTextArea log=new JTextArea();
    private final JLabel status=new JLabel("Ready"),account=new JLabel("Not signed in"),authHint=new JLabel();
    private final JProgressBar progress=new JProgressBar();
    private final JButton signIn=new JButton("Sign in with Microsoft"),play=new JButton("PLAY  1.8.9"),install=new JButton("Install / repair"),cancel=new JButton("Cancel task"),signOut=new JButton("Sign out");
    private final JTextField javaPath=new JTextField(),gamePath=new JTextField(),clientId=new JTextField();
    private final JSpinner ram=new JSpinner(new SpinnerNumberModel(4096,1024,16384,512));
    private final JSpinner width=new JSpinner(new SpinnerNumberModel(1280,640,7680,160)),height=new JSpinner(new SpinnerNumberModel(720,360,4320,90));
    private volatile MicrosoftLogin.Session session;
    private volatile Process game;
    private volatile Future<?> task;
    private JDialog deviceDialog;
    private boolean busy;

    public Launcher(Path root)throws IOException {
        this.root=root;Files.createDirectories(root);loadConfig();
        setLayout(new BorderLayout(0,20));setBorder(new EmptyBorder(28,32,24,32));setBackground(BG);
        JPanel header=new JPanel(new BorderLayout());header.setOpaque(false);
        JLabel title=new JLabel("BLOSSOM");title.setFont(new Font("SansSerif",Font.BOLD,32));title.setForeground(PINK);header.add(title,BorderLayout.NORTH);
        JLabel tagline=new JLabel("Minecraft 1.8.9  /  Your client. Your capes.");tagline.setForeground(MUTED);tagline.setBorder(new EmptyBorder(8,0,0,0));header.add(tagline,BorderLayout.SOUTH);add(header,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.addTab("Play",playPanel());tabs.addTab("Settings",settingsPanel());tabs.addTab("Logs",logsPanel());add(tabs,BorderLayout.CENTER);
        JPanel footer=new JPanel(new BorderLayout(12,8));footer.setOpaque(false);status.setForeground(MUTED);footer.add(status,BorderLayout.NORTH);
        progress.setIndeterminate(false);progress.setPreferredSize(new Dimension(200,6));footer.add(progress,BorderLayout.CENTER);cancel.setEnabled(false);footer.add(cancel,BorderLayout.EAST);add(footer,BorderLayout.SOUTH);
        signIn.addActionListener(e->login());play.addActionListener(e->launch());install.addActionListener(e->prepare());cancel.addActionListener(e->{cancelTask();});
        signOut.addActionListener(e->{session=null;account.setText("Not signed in");updateButtons();});
        theme(this);setBackground(BG);title.setForeground(PINK);tagline.setForeground(MUTED);play.setBackground(PINK);play.setForeground(BG);play.setFont(new Font("SansSerif",Font.BOLD,20));updateButtons();
    }
    private JPanel playPanel(){
        JPanel panel=new JPanel(new BorderLayout(20,18));panel.setBorder(new EmptyBorder(24,24,24,24));
        JPanel info=new JPanel();info.setOpaque(false);info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        JLabel label=new JLabel("A little pink. A lot of possibility.");label.setFont(new Font("SansSerif",Font.BOLD,24));label.setAlignmentX(0);info.add(label);info.add(Box.createVerticalStrut(18));
        info.add(new JLabel("Forge-free PvP HUDs, editable layouts and animated local capes."));info.add(Box.createVerticalStrut(10));
        info.add(new JLabel("Alpha build: gameplay and live Microsoft sign-in still need verification."));info.add(Box.createVerticalStrut(26));
        account.setFont(new Font("SansSerif",Font.BOLD,16));info.add(account);info.add(Box.createVerticalStrut(10));
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));buttons.setOpaque(false);buttons.add(signIn);buttons.add(Box.createHorizontalStrut(10));buttons.add(signOut);buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));info.add(buttons);
        info.add(Box.createVerticalStrut(12));authHint.setForeground(MUTED);authHint.setText(clientId.getText().isEmpty()?"First setup: add Blossom's Microsoft application ID in Settings.":"Sign-in opens Microsoft's device-code page.");info.add(authHint);
        info.add(Box.createVerticalStrut(24));info.add(new JLabel("Tokens stay in memory. Sign in again after closing the launcher."));
        for(Component c:info.getComponents())if(c instanceof JComponent)((JComponent)c).setAlignmentX(0);
        panel.add(info,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(12,0));bottom.setOpaque(false);play.setPreferredSize(new Dimension(250,58));bottom.add(play,BorderLayout.CENTER);bottom.add(install,BorderLayout.EAST);panel.add(bottom,BorderLayout.SOUTH);return panel;
    }
    private JPanel settingsPanel(){
        JPanel form=new JPanel(new GridBagLayout());form.setBorder(new EmptyBorder(18,20,18,20));GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(6,6,6,6);g.fill=GridBagConstraints.HORIZONTAL;
        row(form,g,0,"Java 8 executable",javaPath,()->choose(javaPath,false));row(form,g,1,"Game directory",gamePath,()->choose(gamePath,true));row(form,g,2,"Microsoft application ID",clientId,null);
        row(form,g,3,"Memory (MiB)",ram,null);row(form,g,4,"Window width",width,null);row(form,g,5,"Window height",height,null);
        JLabel note=new JLabel("Use Blossom's registered public-client ID; Microsoft may require Minecraft API approval.");g.gridx=0;g.gridy=6;g.gridwidth=3;form.add(note,g);
        JButton save=new JButton("Save settings");save.addActionListener(e->{try{saveConfig();authHint.setText(clientId.getText().trim().isEmpty()?"First setup: add Blossom's Microsoft application ID in Settings.":"Sign-in opens Microsoft's device-code page.");setStatus("Settings saved");}catch(Exception ex){error(ex);}});g.gridy=7;form.add(save,g);
        g.gridy=8;g.weighty=1;form.add(new JPanel(),g);return form;
    }
    private void row(JPanel panel,GridBagConstraints g,int row,String label,JComponent field,Runnable browse){
        g.gridy=row;g.gridwidth=1;g.gridx=0;g.weightx=0;panel.add(new JLabel(label),g);g.gridx=1;g.weightx=1;panel.add(field,g);g.gridx=2;g.weightx=0;
        if(browse!=null){JButton button=new JButton("Browse");button.addActionListener(e->browse.run());panel.add(button,g);}
    }
    private JPanel logsPanel(){
        JPanel panel=new JPanel(new BorderLayout(8,8));panel.setBorder(new EmptyBorder(12,12,12,12));log.setEditable(false);log.setFont(new Font("Monospaced",Font.PLAIN,12));panel.add(new JScrollPane(log),BorderLayout.CENTER);
        JButton open=new JButton("Open launcher folder");open.addActionListener(e->{try{Desktop.getDesktop().open(root.toFile());}catch(Exception ex){error(ex);}});panel.add(open,BorderLayout.SOUTH);return panel;
    }
    private void choose(JTextField field,boolean directory){JFileChooser chooser=new JFileChooser();chooser.setFileSelectionMode(directory?JFileChooser.DIRECTORIES_ONLY:JFileChooser.FILES_ONLY);if(chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)field.setText(chooser.getSelectedFile().getAbsolutePath());}
    private void loadConfig()throws IOException {
        Path file=root.resolve("launcher.properties");if(Files.exists(file)){if(Files.size(file)>65536)throw new IOException("Launcher settings exceed size limit");try(Reader r=Files.newBufferedReader(file)){config.load(r);}}
        String java8=System.getenv("JAVA8_HOME");String exe=GameInstall.os().equals("windows")?"java.exe":"java";
        javaPath.setText(config.getProperty("java",Paths.get(java8==null?System.getProperty("java.home"):java8,"bin",exe).toString()));
        gamePath.setText(config.getProperty("game",root.resolve("instances/default").toString()));
        String id=System.getenv("BLOSSOM_MICROSOFT_CLIENT_ID");clientId.setText(config.getProperty("clientId",id==null?"":id));
        ram.setValue(number("memory",4096,1024,16384));width.setValue(number("width",1280,640,7680));height.setValue(number("height",720,360,4320));
    }
    private int number(String key,int fallback,int min,int max){try{return Math.max(min,Math.min(max,Integer.parseInt(config.getProperty(key))));}catch(RuntimeException e){return fallback;}}
    private void saveConfig()throws IOException {
        config.setProperty("java",javaPath.getText().trim());config.setProperty("game",gamePath.getText().trim());config.setProperty("clientId",clientId.getText().trim());
        config.setProperty("memory",ram.getValue().toString());config.setProperty("width",width.getValue().toString());config.setProperty("height",height.getValue().toString());
        Path temp=Files.createTempFile(root,"settings-",".tmp");try{try(Writer w=Files.newBufferedWriter(temp)){config.store(w,"Blossom launcher settings; no credentials");}Net.move(temp,root.resolve("launcher.properties"));}finally{Files.deleteIfExists(temp);}
    }
    private interface Job{void run()throws Exception;}
    private void runTask(Job job){
        if(busy)return;busy=true;progress.setIndeterminate(true);cancel.setEnabled(true);updateButtons();
        FutureTask<Void> pending=new FutureTask<Void>(()->{try{job.run();}catch(InterruptedException|InterruptedIOException e){setStatus("Cancelled");Thread.currentThread().interrupt();}catch(Exception e){SwingUtilities.invokeLater(()->error(e));}
            finally{finishTask();}return null;}){protected void done(){if(isCancelled())SwingUtilities.invokeLater(()->setStatus("Cancelled"));}};
        task=pending;worker.execute(()->{try{pending.run();}finally{if(pending.isCancelled())finishTask();}});
    }
    private void finishTask(){SwingUtilities.invokeLater(()->{busy=false;progress.setIndeterminate(false);cancel.setEnabled(false);if(deviceDialog!=null){deviceDialog.dispose();deviceDialog=null;}updateButtons();});
    }
    private synchronized void cancelTask(){if(game==null&&task!=null){task.cancel(true);setStatus("Cancelling...");}}
    private void login(){
        try{saveConfig();}catch(IOException e){error(e);return;}final String id=clientId.getText().trim();
        runTask(()->{setStatus("Starting Microsoft sign-in...");MicrosoftLogin.Session signed=MicrosoftLogin.login(id,code->SwingUtilities.invokeLater(()->showDeviceCode(code)),this::setStatus);session=signed;SwingUtilities.invokeLater(()->account.setText("Signed in as "+signed.username));setStatus("Signed in. Ready to play.");});
    }
    private void showDeviceCode(MicrosoftLogin.DeviceCode code){
        deviceDialog=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),"Microsoft sign-in",false);JPanel p=new JPanel(new GridLayout(0,1,8,10));p.setBorder(new EmptyBorder(18,18,18,18));p.add(new JLabel("Enter this code on Microsoft's sign-in page:"));
        JTextField value=new JTextField(code.code);value.setEditable(false);value.setFont(new Font("Monospaced",Font.BOLD,24));p.add(value);p.add(new JLabel(code.url));
        JButton open=new JButton("Open Microsoft login");open.addActionListener(e->{try{Desktop.getDesktop().browse(java.net.URI.create(code.url));}catch(Exception ex){setStatus("Open the displayed Microsoft URL in your browser.");}});p.add(open);
        JButton stop=new JButton("Cancel sign-in");stop.addActionListener(e->{cancelTask();deviceDialog.dispose();});p.add(stop);theme(p);deviceDialog.setContentPane(p);deviceDialog.pack();deviceDialog.setLocationRelativeTo(this);deviceDialog.setVisible(true);
    }
    private void prepare(){
        final Path dir;
        try{saveConfig();dir=Paths.get(gamePath.getText().trim()).toAbsolutePath();}catch(Exception e){error(e);return;}
        runTask(()->{GameInstall.install(root.resolve("cache"),dir,this::setStatus);agent();setStatus("Installation verified. Sign in and select Java 8 to play.");});
    }
    private void launch(){
        final MicrosoftLogin.Session signed=session;final Path java,dir;final int memory,w,h;
        try{saveConfig();if(signed==null||!signed.valid())throw new IOException("Sign in with Microsoft first.");java=Paths.get(javaPath.getText().trim());dir=Paths.get(gamePath.getText().trim()).toAbsolutePath();memory=(Integer)ram.getValue();w=(Integer)width.getValue();h=(Integer)height.getValue();}catch(Exception e){error(e);return;}
        runTask(()->{
            LaunchPlan.verifyJava8(java);GameInstall.Installed installed=GameInstall.install(root.resolve("cache"),dir,this::setStatus);Path agent=agent();
            List<String> command=LaunchPlan.command(java,agent,dir,installed,signed,memory,w,h);setStatus("Launching Minecraft...");
            synchronized(this){if(Thread.currentThread().isInterrupted())throw new InterruptedException();
            game=LaunchPlan.start(command,dir,signed);}SwingUtilities.invokeLater(()->cancel.setEnabled(false));
            try(BufferedReader reader=new BufferedReader(new InputStreamReader(game.getInputStream(),StandardCharsets.UTF_8))){String line;while((line=reader.readLine())!=null)appendLog(LaunchPlan.redact(line,signed));}
            int code=game.waitFor();game=null;setStatus("Minecraft exited with code "+code);
        });
    }
    private Path agent()throws IOException {
        Properties build=new Properties();try(InputStream in=Launcher.class.getResourceAsStream("/build.properties")){if(in==null)throw new IOException("Missing launcher build metadata");build.load(in);}
        String version=build.getProperty("version");if(!version.matches("[a-zA-Z0-9.-]+"))throw new IOException("Invalid embedded version");Path target=root.resolve("client/blossom-client-"+version+".jar");Files.createDirectories(target.getParent());String hash=build.getProperty("agent.sha256");
        if(!Files.exists(target)||!Net.hash(target,"SHA-256").equals(hash)){
            Path temp=Files.createTempFile(target.getParent(),"agent-",".tmp");try(InputStream in=Launcher.class.getResourceAsStream("/client/blossom-agent.jar")){if(in==null)throw new IOException("Missing bundled client");Files.copy(in,temp,StandardCopyOption.REPLACE_EXISTING);if(!Net.hash(temp,"SHA-256").equals(hash))throw new IOException("Bundled client checksum mismatch");Net.move(temp,target);}finally{Files.deleteIfExists(temp);}
        }return target;
    }
    private void updateButtons(){signIn.setEnabled(!busy);install.setEnabled(!busy);play.setEnabled(!busy&&(game==null||!game.isAlive())&&session!=null&&session.valid());signOut.setEnabled(!busy&&session!=null);}
    private void setStatus(String text){SwingUtilities.invokeLater(()->status.setText(text));appendLog(text);}
    private void appendLog(String text){
        SwingUtilities.invokeLater(()->{if(log.getDocument().getLength()>150000)log.setText(log.getText().substring(75000));log.append(text+"\n");log.setCaretPosition(log.getDocument().getLength());});
        // Do not persist game/API output: UI log is bounded and session-only.
    }
    private void error(Exception e){Throwable cause=e;while(cause instanceof ExecutionException&&cause.getCause()!=null)cause=cause.getCause();String text=cause.getMessage()==null?cause.getClass().getSimpleName():cause.getMessage();setStatus(text);JOptionPane.showMessageDialog(this,text,"Blossom",JOptionPane.ERROR_MESSAGE);}
    private static void theme(Component component){
        component.setForeground(TEXT);if(!(component instanceof JProgressBar))component.setBackground(component instanceof javax.swing.text.JTextComponent?BG:CARD);
        if(component instanceof JLabel&&((JLabel)component).getFont().getSize()<20)component.setFont(new Font("SansSerif",Font.PLAIN,13));
        if(component instanceof JButton){((JButton)component).setFocusPainted(false);component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
        if(component instanceof Container)for(Component child:((Container)component).getComponents())theme(child);
    }
    public static void main(String[] args)throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        UIManager.put("Panel.background",CARD);UIManager.put("TextField.background",BG);UIManager.put("TextField.foreground",TEXT);UIManager.put("TextArea.background",BG);UIManager.put("TextArea.foreground",TEXT);
        Path root=Paths.get(System.getProperty("user.home"),".blossom");
        if(args.length==2&&args[0].equals("--install-only")){Path dir=Paths.get(args[1]).toAbsolutePath();GameInstall.install(dir.resolve("cache"),dir.resolve("game"),System.out::println);return;}
        if(args.length==2&&args[0].equals("--render-preview")){
            Path temporary=Files.createTempDirectory("blossom-preview-");SwingUtilities.invokeAndWait(()->{try{Launcher panel=new Launcher(temporary);panel.setSize(980,680);layoutAll(panel);java.awt.image.BufferedImage image=new java.awt.image.BufferedImage(980,680,java.awt.image.BufferedImage.TYPE_INT_RGB);Graphics2D graphics=image.createGraphics();panel.printAll(graphics);graphics.dispose();javax.imageio.ImageIO.write(image,"png",Paths.get(args[1]).toFile());panel.worker.shutdownNow();}catch(Exception e){throw new RuntimeException(e);}});return;
        }
        if(GraphicsEnvironment.isHeadless()){System.err.println("Blossom Launcher requires a graphical desktop.");return;}
        Files.createDirectories(root);FileChannel channel=FileChannel.open(root.resolve("launcher.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);FileLock lock=channel.tryLock();
        if(lock==null){channel.close();JOptionPane.showMessageDialog(null,"Blossom Launcher is already open.");return;}
        SwingUtilities.invokeLater(()->{try{
            Launcher panel=new Launcher(root);JFrame frame=new JFrame("Blossom Launcher • 0.2.0 alpha");frame.setContentPane(panel);frame.setMinimumSize(new Dimension(880,640));frame.setSize(980,680);frame.setLocationRelativeTo(null);frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent event){synchronized(panel){if(panel.game!=null&&panel.game.isAlive()){JOptionPane.showMessageDialog(frame,"Close Minecraft before closing the launcher.");return;}if(panel.task!=null)panel.task.cancel(true);panel.worker.shutdownNow();try{lock.release();channel.close();}catch(IOException ignored){}frame.dispose();System.exit(0);}}});frame.setVisible(true);
        }catch(Exception e){JOptionPane.showMessageDialog(null,e.getMessage(),"Blossom",JOptionPane.ERROR_MESSAGE);System.exit(1);}});
    }
    private static void layoutAll(Container parent){parent.doLayout();for(Component c:parent.getComponents())if(c instanceof Container)layoutAll((Container)c);}
}
