/*
 * Copyright (C) 2021 European Spallation Source ERIC.
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
package xal.extension.logbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;

/**
 * This class supports different types of attachments: File, InputStream, and
 * WritableImage objects.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Attachment {

    private String fileName;
    private final boolean isImage;
    private final Image image;
    private String mimeType;
    ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

    public String getMimeType() {
        return mimeType;
    }

    public boolean isImage() {
        return isImage;
    }

    public Image getImage() {
        return image;
    }

    public String getFileName() {
        return fileName;
    }

    public Attachment(File file) throws IOException {
        this(file.getName(), Files.newInputStream(file.toPath()));
    }

    /**
     *
     * @param fileName Filename, including extension.
     * @param fileContent An InputStream object. If it is an image, it will
     * recognize it.
     */
    public Attachment(String fileName, InputStream fileContent) {
        this.fileName = fileName;

        // Convert to byteArray to be able to reuse the data
        int d;
        try {
            while ((d = fileContent.read()) != -1) {
                byteOutput.write(d);
            }
            byteOutput.flush();
        } catch (IOException ex) {
            Logger.getLogger(Attachment.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Try to load the image, if possible
        image = new Image(new ByteArrayInputStream(byteOutput.toByteArray()));
        isImage = !image.isError();

        try {
            mimeType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(byteOutput.toByteArray()));
        } catch (IOException ex) {
            Logger.getLogger(Attachment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param fileName Filename, including extension.
     * @param image Screenshot
     */
    public Attachment(String fileName, Image image) {
        this.fileName = fileName;
        this.image = image;
        isImage = true;
        mimeType = "image/png";

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", byteOutput);
            byteOutput.flush();
        } catch (IOException ex) {
            Logger.getLogger(Attachment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write the attachment to an OutputStream object, typically an HTTP
     * connection to the elog server.
     *
     * @param os
     * @throws java.io.IOException
     */
    public void writeTo(OutputStream os) throws IOException {
        byteOutput.writeTo(os);
        os.flush();
    }
}
