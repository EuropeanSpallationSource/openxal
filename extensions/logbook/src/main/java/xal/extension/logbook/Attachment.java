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

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class supports different types of attachments: File, InputStream, and
 * WritableImage objects.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Attachment {

    // create a magic utility using the internal magic file
    private static final ContentInfoUtil util = new ContentInfoUtil();

    private String fileName;
    private String mimeType;
    private ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ByteArrayOutputStream getContent() {
        return byteOutput;
    }

    public void setContent(ByteArrayOutputStream byteOutput) {
        this.byteOutput = byteOutput;
    }

    /**
     * Empty constructor to be used only by subclasses.
     */
    protected Attachment() {
    }

    /**
     * Creates a new Attachment instance from a File object.
     *
     * @param file
     * @throws IOException
     */
    public Attachment(File file) throws IOException {
        this(file.getName(), Files.newInputStream(file.toPath()));
    }

    /**
     * Creates a new Attachment from an InputStream.
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

        try {
            ContentInfo content = util.findMatch(new ByteArrayInputStream(byteOutput.toByteArray()));
            if (content != null) {
                mimeType = content.getMimeType();
            }
        } catch (IOException ex) {
            Logger.getLogger(Attachment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
