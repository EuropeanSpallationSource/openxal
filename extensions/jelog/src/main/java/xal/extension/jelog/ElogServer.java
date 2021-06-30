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

import xal.tools.apputils.Preferences;

/**
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class ElogServer {

    private static final String ELOG_SERVER = "elogURL";

    public static String getElogURL() {
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(ElogServer.class);
        return defaults.get(ELOG_SERVER, "https://logbook.esss.lu.se/");
    }

    public static void setElogURL(String elogServer) {
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(ElogServer.class);
        defaults.put(ELOG_SERVER, elogServer);
    }
}
