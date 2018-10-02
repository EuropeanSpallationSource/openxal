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
package xal.extension.jelog;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import se.esss.jelog.Attachment;
import se.esss.jelog.Jelog;

/**
 * Class to submit new entries without a GUI. Useful for applications that
 * require to log some data regularly in an automatic manner.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class PostEntry {

    private static String elogServer = null;

    public static final Logger LOGGER = Logger.getLogger(PostEntry.class.getName());

    public static String getElogServer() {
        return elogServer;
    }

    public static void setElogServer(String elogServer) {
        PostEntry.elogServer = elogServer;
    }

    public static int post(HashMap<String, String> fields, String textBody, String logbook) throws IOException, Exception {
        return post(fields, textBody, logbook, null);
    }

    public static int post(HashMap<String, String> fields, String textBody, String logbook, List<Attachment> attachments) throws IOException, Exception {
        Jelog.setTrustAllCerts();

        if (elogServer == null) {
            elogServer = ElogServer.getElogURL();
        }

        String[] credentials = Jelog.retrieveUsernameAndPassword(new URL(new URL(elogServer), logbook).toString());

        if (credentials[0] == null) {
            LOGGER.severe("Not logged in. Please log in using the login() method.");
        }

        String author = credentials[0];
        String userName = credentials[1];
        String userPasswordHash = credentials[2];

        System.out.println(author);

        // Creates a new field for the author or overwrite the existing one with
        // the user's name returned from elog.
        fields.replace("Author", author);

        return Jelog.submit(fields, textBody, null, attachments,
                logbook, elogServer, userName, userPasswordHash);
    }

    /**
     * Login method for recurrent posts
     *
     * @param user username
     * @param password password
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static boolean login(String user, char[] password) throws IOException, Exception {
        Jelog.setTrustAllCerts();

        Jelog.login(user, password, true, elogServer);

        String logbook = Jelog.getLogbooks(new URL(new URL(elogServer), Jelog.getLogbooks(elogServer).get(0)).toString()).get(0);
        String[] credentials = Jelog.retrieveUsernameAndPassword(new URL(new URL(elogServer), logbook).toString());

        return credentials[0] != null;
    }
}
