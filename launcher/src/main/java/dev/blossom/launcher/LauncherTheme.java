package dev.blossom.launcher;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.*;
final class LauncherTheme {
    static final Color PANEL=new Color(39,25,40),PINK=new Color(255,131,181);
    static final class Card extends JPanel {
        Card(LayoutManager layout){super(layout);setOpaque(false);setBorder(new EmptyBorder(22,22,22,22));}
        protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(PANEL);g.fillRoundRect(0,0,getWidth(),getHeight(),22,22);g.setColor(new Color(89,49,72));g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,22,22);g.dispose();super.paintComponent(graphics);}
    }
    static void button(JButton button){
        button.setUI(new BasicButtonUI(){public void paint(Graphics graphics,JComponent component){AbstractButton b=(AbstractButton)component;Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(!b.isEnabled()?new Color(48,35,48):b.getModel().isRollover()?b.getBackground().brighter():b.getBackground());g.fillRoundRect(0,0,b.getWidth(),b.getHeight(),14,14);g.setColor(b.hasFocus()?PINK:new Color(124,68,101));g.drawRoundRect(0,0,b.getWidth()-1,b.getHeight()-1,14,14);g.dispose();super.paint(graphics,component);}
            protected void paintText(Graphics g,AbstractButton b,Rectangle rect,String text){g.setFont(b.getFont());g.setColor(b.isEnabled()?b.getForeground():new Color(172,142,163));g.drawString(text,rect.x,rect.y+g.getFontMetrics().getAscent());}});
        button.setOpaque(false);button.setContentAreaFilled(false);button.setRolloverEnabled(true);button.setBorder(new EmptyBorder(10,16,10,16));
    }
    static void tabs(JTabbedPane tabs){
        tabs.setUI(new BasicTabbedPaneUI(){
            protected void installDefaults(){super.installDefaults();tabInsets=new Insets(10,24,10,24);contentBorderInsets=new Insets(12,0,0,0);}
            protected void paintTabBackground(Graphics graphics,int placement,int index,int x,int y,int w,int h,boolean selected){Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(selected?new Color(103,45,78):new Color(39,25,40));g.fillRoundRect(x+2,y+2,w-4,h-2,14,14);g.dispose();}
            protected void paintText(Graphics g,int placement,Font font,FontMetrics metrics,int index,String title,Rectangle rect,boolean selected){g.setFont(font);g.setColor(new Color(255,237,247));g.drawString(title,rect.x,rect.y+metrics.getAscent());}
            protected void paintTabBorder(Graphics g,int p,int i,int x,int y,int w,int h,boolean s){}
            protected void paintContentBorder(Graphics g,int p,int i){}
            protected void paintFocusIndicator(Graphics g,int p,Rectangle[] r,int i,Rectangle icon,Rectangle text,boolean selected){if(selected&&tabPane.hasFocus()){g.setColor(PINK);g.drawLine(text.x,text.y+text.height+2,text.x+text.width,text.y+text.height+2);}}
        });tabs.setOpaque(false);
    }
}
