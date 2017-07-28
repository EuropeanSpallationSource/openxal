/*
 * RefTrajectoryFile.java
 *
 * Created by Natalia Milas on 07.07.2017
 *
 * Copyright (c) 2017 European Spallation Source ERIC
 * Tunavägen 20
 * Lund, Sweden
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package xal.app.trajectorycorrection;

import javafx.beans.property.SimpleBooleanProperty;

/**
 * Class that holds the information about the reference trajectory files
 * @author nataliamilas
 * 06-2017
 */


public class RefTrajectoryFile {
    private String name, fileName;
    private final SimpleBooleanProperty select;

    public RefTrajectoryFile(String name, String fileName, Boolean choice) {
        this.name = name;
        this.fileName = fileName;
        this.select = new SimpleBooleanProperty(choice);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isSelected() {
        return select.get();
    }

    public SimpleBooleanProperty selectProperty() {
        return select;
    }

    public void setSelected(Boolean select) {
        this.select.set(select);
    }

    
}
