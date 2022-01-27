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
import xal.tools.apputils.Preferences;

/**
 * Abstract class that defines a logbook service provider.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public abstract class LogbookProvider {

    private static final String LOGBOOK_SERVER_PROPERTY = "serverURL";
    private static final String DEFAULT_LOGBOOK = "defaultLogbook";

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

    /**
     *
     * @return the name of the default logbook
     */
    public String getDefaultLogbook() {
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(this.getClass());
        return defaults.get(DEFAULT_LOGBOOK, "");
    }

    public void setDefaultLogbook(String logbook) {
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(this.getClass());
        defaults.put(DEFAULT_LOGBOOK, logbook);
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

    public abstract boolean login(String username, char[] password);

    public abstract void logout();

    /* 
     * Methods to post new entries without UI
     */
    /**
     *
     * @param logbook Name of the logbooks where the entry should be posted
     * @param fields map containing metadata as pair of name of the attribute
     * and value
     * @param textBody entry body text
     * @return the message ID
     */
    public abstract long post(String[] logbooks, Map<String, List<String>> fields, String textBody) throws LogbookException;

    public abstract long post(String[] logbooks, Map<String, List<String>> fields, String textBody, List<Attachment> attachments) throws LogbookException;

    /* 
     * Methods to post new entries using the UI
     */
    public abstract long post(Map<String, List<String>> defaultAttributes) throws LogbookException;

    public abstract long post(String[] defaultLogbooks, Map<String, List<String>> defaultAttributes) throws LogbookException;

    public abstract long post(List<Attachment> attachments, Map<String, List<String>> defaultAttributes) throws LogbookException;

    public abstract long post(List<Attachment> attachments, String[] defaultLogbooks, Map<String, List<String>> defaultAttributes) throws LogbookException;
}
