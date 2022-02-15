/*
 * Copyright (C) 2017 European Spallation Source ERIC.
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
package xal.extension.tracewinimporter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to log during import. This one only prints to the standard output, but
 * can be extended for GUIs.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class ImportLogger {

    private static final Logger LOGGER = Logger.getLogger(ImportLogger.class.getName());


    public void log(String string) {
        LOGGER.log(Level.INFO, string);
    }

    public void close() {
        // Do nothing
    }
}
