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
package xal.plugin.jelog;

import eu.ess.xaos.tools.annotation.ServiceProvider;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.extension.logbook.Attachment;
import xal.extension.logbook.LogbookException;
import xal.extension.logbook.LogbookProvider;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
@ServiceProvider(service = LogbookProvider.class, order = 20)
public class ElogProvider extends LogbookProvider {

    @Override
    public void setServer(String server) {
        super.setServer(server);
        PostEntry.setElogServer(server);
        XALPostEntryDialog.setElogServer(server);
    }

    @Override
    public boolean login(String username, char[] password) {
        try {
            return PostEntry.login(username, password);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ElogProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public void logout() {
        try {
            PostEntry.logout();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ElogProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public long post(String[] logbooks, Map<String, List<String>> fields, String textBody) throws LogbookException {
        return post(logbooks, fields, textBody, new ArrayList<>());
    }

    @Override
    public long post(String[] logbooks, Map<String, List<String>> fields, String textBody, List<Attachment> attachments) throws LogbookException {
        if (logbooks.length > 1) {
            throw new LogbookException("ELOG only supports posting an entry into a single logbook. To post on several logbooks, a new entry must be posted individually on each logbook.");
        }

        try {
            return PostEntry.post(fields, textBody, logbooks[0], convertAttachments(attachments));
        } catch (IOException ex) {
            Logger.getLogger(ElogProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public long post(Map<String, List<String>> defaultAttributes) throws LogbookException {
        try {
            XALPostEntryDialog.setElogServer(getServer());
            return XALPostEntryDialog.post(defaultAttributes);
        } catch (IOException ex) {
            throw new LogbookException(ex);
        }
    }

    @Override
    public long post(String[] defaultLogbooks, Map<String, List<String>> defaultAttributes) throws LogbookException {
        if (defaultLogbooks.length > 1) {
            throw new LogbookException("ELOG only supports posting an entry into a single logbook. To post on several logbooks, a new entry must be posted individually on each logbook.");
        }

        try {
            XALPostEntryDialog.setElogServer(getServer());
            return XALPostEntryDialog.post(defaultLogbooks[0], defaultAttributes);
        } catch (IOException ex) {
            throw new LogbookException(ex);
        }
    }

    @Override
    public long post(List<Attachment> attachments, Map<String, List<String>> defaultAttributes) throws LogbookException {
        try {
            XALPostEntryDialog.setElogServer(getServer());
            return XALPostEntryDialog.post(convertAttachments(attachments), defaultAttributes);
        } catch (IOException ex) {
            throw new LogbookException(ex);
        }
    }

    @Override
    public long post(List<Attachment> attachments, String[] defaultLogbooks, Map<String, List<String>> defaultAttributes) throws LogbookException {
        if (defaultLogbooks.length > 1) {
            throw new LogbookException("ELOG only supports posting an entry into a single logbook. To post on several logbooks, a new entry must be posted individually on each logbook.");
        }

        try {
            XALPostEntryDialog.setElogServer(getServer());
            return XALPostEntryDialog.post(convertAttachments(attachments), defaultLogbooks[0], defaultAttributes);
        } catch (IOException ex) {
            throw new LogbookException(ex);
        }
    }

    private List<eu.ess.jelog.Attachment> convertAttachments(List<Attachment> attachments) {
        List<eu.ess.jelog.Attachment> jelogAttachments = new ArrayList<>();
        if (!attachments.isEmpty()) {
            for (Attachment att : attachments) {
                eu.ess.jelog.Attachment newAtt = new eu.ess.jelog.Attachment(att.getFileName(), att.getMimeType(), att.getContent());
                jelogAttachments.add(newAtt);
            }
        }
        return jelogAttachments;
    }
}
