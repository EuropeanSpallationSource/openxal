/*
 * Copyright (C) 2020 European Spallation Source ERIC.
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
package xal.extension.fxapplication;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

/**
 * This class must be subclassed to create the FXML controller of the main
 * application.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@ess.eu>
 */
public abstract class Controller implements Initializable {

    private FxApplication application;

    void setApplication(FxApplication application) {
        this.application = application;
    }

    public FxApplication getApplication() {
        return application;
    }

    /**
     * This method is executed right before showing the stage.
     */
    public void beforeStart() {
        // Does nothing
    }

    /**
     * This method has to be implemented by the controller class to setup the
     * stage.
     *
     * @param url
     * @param rb
     */
    @Override
    public abstract void initialize(URL url, ResourceBundle rb);
}
