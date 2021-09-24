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

import java.util.Map;
import java.util.List;
import javafx.stage.Stage;
import javafx.util.Pair;
import xal.tools.apputils.Preferences;

/**
 * Abstract class that defines a logbook service provider.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public abstract class LogbookProvider {

    private static final String LOGBOOK_SERVER_PROPERTY = "serverURL";
    private String logbookServer;

    protected LogbookProvider() {
        logbookServer = getDefaultServerUrl();
    }

    public String getServer() {
        return logbookServer;
    }

    public void setServer(String server) {
        logbookServer = server;
    }

    public final String getDefaultServerUrl() {
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(this.getClass());
        return defaults.get(LOGBOOK_SERVER_PROPERTY, "");
    }

    public final void setDefaultServerUrl(String serverUrl) {
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(this.getClass());
        defaults.put(LOGBOOK_SERVER_PROPERTY, serverUrl);
        setServer(serverUrl);
    }

    public abstract void login(String username, char[] password);

    public abstract void logout();

    /* 
     * Methods to post new entries without UI
     */
    /**
     *
     * @param fields map containing metadata as pair of name of the attribute
     * and value
     * @param textBody entry body text
     * @param logbook Name of the logbook
     * @return the message ID
     */
    public abstract int post(Map<String, String> fields, String textBody, String logbook);

    public abstract int post(Map<String, String> fields, String textBody, String logbook, List<Attachment> attachments);

    /* 
     * Methods to post new entries using the UI
     */
    public abstract Stage post(Pair<String, String>... defaultAttributes);

    public abstract Stage post(String defaultLogbook, Pair<String, String>... defaultAttributes);

    public abstract Stage post(List<Attachment> attachments, Pair<String, String>... defaultAttributes);

    public abstract Stage post(List<Attachment> attachments, String defaultLogbook, Pair<String, String>... defaultAttributes);
}
