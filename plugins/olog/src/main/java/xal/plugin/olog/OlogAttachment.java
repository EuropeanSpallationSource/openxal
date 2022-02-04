/*
 * Copyright (C) 2022 European Spallation Source ERIC.
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
package xal.plugin.olog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import xal.extension.logbook.Attachment;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class OlogAttachment extends Attachment {

    private String uuid;

    public OlogAttachment(Attachment attachment) {
        this.setContent(attachment.getContent());
        this.setFileName(attachment.getFileName());
        this.setMimeType(attachment.getMimeType());
    }

    public OlogAttachment(File file) throws IOException {
        super(file);
    }

    public OlogAttachment(String fileName, InputStream fileContent) throws IOException {
        super(fileName, fileContent);
    }

    public String getUuid() {
        return uuid;
    }

    public void generateUuid() {
        uuid = UUID.randomUUID().toString();
    }

    public boolean hasUuid() {
        return uuid != null;
    }
}
