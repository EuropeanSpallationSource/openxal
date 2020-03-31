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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import eu.ess.jelog.Attachment;
import eu.ess.jelog.Jelog;

/**
 * Class to submit new entries without a GUI. Useful for applications that
 * require to log some data regularly in an automatic manner.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class PostEntry {

    private static String elogServer = null;

    public static final Logger LOGGER = Logger.getLogger(PostEntry.class.getName());

    public static void setElogServer(String elogServer) {
        PostEntry.elogServer = elogServer;
    }

    public static int post(HashMap<String, String> fields, String textBody, String logbook) throws IOException, Exception {
        return post(fields, textBody, logbook, null);
    }

    public static int post(HashMap<String, String> fields, String textBody, String logbook, List<Attachment> attachments) throws IOException, Exception {
        if (elogServer == null) {
            elogServer = ElogServer.getElogURL();
        }
        
        Jelog jelog = new Jelog(elogServer);

        if (jelog.getUserName() == null) {
            LOGGER.severe("Not logged in. Please log in using the login() method.");
        }

        return jelog.submit(fields, textBody, null, attachments,
                logbook);
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
        Jelog jelog = new Jelog(elogServer);
    
        return jelog.login(user, password, true);
    }
}
