/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

/**
 *
 * @author nataliamilas
 */
import javafx.animation.AnimationTimer;

/**
 *
 * @author nataliamilas
 */
public class StatusAnimationTimer extends AnimationTimer {

    private volatile boolean running;

    @Override
    public void start() {
        super.start();
        running = true;
    }

    @Override
    public void stop() {
        super.stop();
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void handle(long now) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
