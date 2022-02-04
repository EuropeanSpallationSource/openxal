/*
 * Copyright (C) 2018 European Spallation Source ERIC.
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
package xal.plugin.jelog;

import java.io.IOException;
import java.util.List;
import eu.ess.jelog.Attachment;
import eu.ess.jelog.PostEntryDialog;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to submit new entries using a JavaFX dialog. Useful for logging data
 * together with some files or images attached directly from an Open XAL
 * application. It is basically a wrapper around PostEntryDialog to set the
 * default ESS elog server address and the CKEditor path.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class XALPostEntryDialog {

    private static String elogURL = null;

    private XALPostEntryDialog() {
        throw new IllegalStateException("Utility class");
    }

    public static void setElogServer(String elogURL) {
        XALPostEntryDialog.elogURL = elogURL;
    }

    public static int post(Map<String, List<String>> defaultAttributes) throws IOException {
        return post(null, null, defaultAttributes);
    }

    public static int post(String defaultLogbook, Map<String, List<String>> defaultAttributes) throws IOException {
        return post(null, defaultLogbook, defaultAttributes);
    }

    public static int post(List<Attachment> attachments, Map<String, List<String>> defaultAttributes) throws IOException {
        return post(attachments, null, defaultAttributes);
    }

    public static int post(List<Attachment> attachments, String defaultLogbook, Map<String, List<String>> defaultAttributes) throws IOException {
        // Trick to locate CKEditor files. Only works on LCR-type installations, where
        // the html folder is located next to the library.jar file.
        String ckeditorPath = PostEntryDialog.class.getResource("PostEntryDialog.class").toExternalForm();
        ckeditorPath = ckeditorPath.substring("jar:file:".length(), ckeditorPath.indexOf("!"));
        ckeditorPath = ckeditorPath.substring(0, ckeditorPath.lastIndexOf('/'));
        ckeditorPath += "/html/ckeditor.html";
        System.setProperty("eu.ess.jelog.ckeditor_path", ckeditorPath);

        PostEntryDialog.setElogServer(elogURL);

        try {
            return PostEntryDialog.post(attachments, defaultLogbook, defaultAttributes);
        } catch (Exception ex) {
            Logger.getLogger(XALPostEntryDialog.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }
}
