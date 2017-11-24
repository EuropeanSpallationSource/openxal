/*
 * Copyright (C) 2017 European Spallation Source ERIC
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

package xal.app.trajectorydisplay2;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import org.apache.commons.io.FileUtils;


public class MakeElogPostController implements Initializable {

    @FXML
    private TextField textAuthor;
    @FXML
    private ComboBox<String> comboBoxType;
    @FXML
    private RadioButton rbimageYes;
    @FXML
    private ToggleGroup groupImage;
    @FXML
    private RadioButton rbimageNo;
    @FXML
    private TextField textSubject;
    @FXML
    private TextArea textBody;
    @FXML
    private Button buttonCancel;
    @FXML
    private Button buttonSubmit;

    private final BooleanProperty loggedIn = new SimpleBooleanProperty();
    private File imageFile = null;
    @FXML
    private ComboBox<String> comboBoxCategory;
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboBoxType.getItems().add("Routine");
        comboBoxType.getItems().add("Software Installation");
        comboBoxType.getItems().add("Problem Fixed");
        comboBoxType.getItems().add("Configuration");
        comboBoxType.getItems().add("Other");
        comboBoxType.setValue("Routine");
        
        comboBoxCategory.getItems().add("General");
        comboBoxCategory.getItems().add("Hardware");
        comboBoxCategory.getItems().add("Software");
        comboBoxCategory.getItems().add("Network");
        comboBoxCategory.getItems().add("Other");
        comboBoxCategory.getItems().add("IS");
        comboBoxCategory.setValue("General");
    }    
    
    public BooleanProperty loggedInProperty() {
        return loggedIn ;
    }

    public final boolean isLoggedIn() {
        return loggedInProperty().get();
    }

    public final void setLoggedIn(boolean loggedIn) {
        loggedInProperty().set(loggedIn);
    } 
    
    public final void setImagePath(File imageFile) {
        this.imageFile = imageFile;
    } 
    
    @FXML
    public void handleButtonCancel(){
        setLoggedIn(true);
    }

    @FXML
    private void handleButtonSubmit(ActionEvent event) {
        buttonSubmit.setDisable(true);
        buttonCancel.setDisable(true);
        try {
            String command = null;
                           
            FileUtils.writeStringToFile(new File("author.txt"), textAuthor.getText());
            FileUtils.writeStringToFile(new File("type.txt"), comboBoxType.getValue());
            FileUtils.writeStringToFile(new File("category.txt"), comboBoxCategory.getValue());
            FileUtils.writeStringToFile(new File("subject.txt"), textSubject.getText());
            FileUtils.writeStringToFile(new File("textInput.txt"), textBody.getText());
            
            if (rbimageYes.isSelected()){
                command = "curl -F cmd=Submit -F Author=@author.txt -F Type=@type.txt -F Category=@category.txt -F Subject=@subject.txt -F Text=@textInput.txt -F attfile=@"+imageFile.getAbsolutePath()+" http://elog.esss.lu.se/demo";
            } else {
                command = "curl -F cmd=Submit -F Author=@author.txt -F Type=@type.txt -F Category=@category.txt -F Subject=@subject.txt -F Text=@textInput.txt http://elog.esss.lu.se/demo";        
            }
            //System.out.print(command+"\n");
            try {
                //System.out.println("Making elog entry");
                Runtime runTime = Runtime.getRuntime();
                Process process = runTime.exec(command);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                process.destroy();
            } catch (IOException e) {
            }
            setLoggedIn(true);
        } catch (IOException ex) {
            Logger.getLogger(MakeElogPostController.class.getName()).log(Level.SEVERE, null, ex);
        }
        buttonSubmit.setDisable(false);
        buttonCancel.setDisable(false);
    }
    
}
