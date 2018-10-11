/*
 * Copyright (C) 2018 European Spallation Source ERIC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.app.trajectorydisplay;

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
