package dev.blossom.core.cloth;
import java.util.Properties;
import dev.blossom.core.ClientSettings;
public final class ClothSettings {
    public int subdivisions=16,iterations=6;
    public float gravity=9.8f,stiffness=.55f,damping=.04f,windStrength=.2f,distance=24;
    public boolean selfCollision=true,playerCollision=true,wind=false;
    public void load(Properties p){
        subdivisions=ClientSettings.integer(p,"physics.subdivisions",16,4,64);iterations=ClientSettings.integer(p,"physics.iterations",6,2,12);
        gravity=ClientSettings.number(p,"physics.gravity",9.8f,0,30);stiffness=ClientSettings.number(p,"physics.stiffness",.55f,0,1);damping=ClientSettings.number(p,"physics.damping",.04f,0,.5f);
        windStrength=ClientSettings.number(p,"physics.windStrength",.2f,0,3);distance=ClientSettings.number(p,"physics.distance",24,4,64);
        selfCollision=Boolean.parseBoolean(p.getProperty("physics.selfCollision","true"));playerCollision=Boolean.parseBoolean(p.getProperty("physics.playerCollision","true"));wind=Boolean.parseBoolean(p.getProperty("physics.wind","false"));
    }
    public void save(Properties p){
        p.setProperty("physics.subdivisions",""+subdivisions);p.setProperty("physics.iterations",""+iterations);p.setProperty("physics.gravity",""+gravity);p.setProperty("physics.stiffness",""+stiffness);p.setProperty("physics.damping",""+damping);p.setProperty("physics.windStrength",""+windStrength);p.setProperty("physics.distance",""+distance);p.setProperty("physics.selfCollision",""+selfCollision);p.setProperty("physics.playerCollision",""+playerCollision);p.setProperty("physics.wind",""+wind);
    }
}
