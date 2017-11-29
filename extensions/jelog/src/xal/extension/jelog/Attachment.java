/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.jelog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author juanfestebanmuller
 */

public class Attachment {

    private String fileName;
    private InputStream fileContent;

    public String getFileName() {
        return fileName;
    }

    public InputStream getFileContent() {
        return fileContent;
    }

    public Attachment(String fileName, InputStream fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    public Attachment(File file) {
        this.fileName = file.getName();
        try {
            this.fileContent = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Attachment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}