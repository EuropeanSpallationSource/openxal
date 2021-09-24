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

import java.util.List;
import java.util.ServiceLoader;
import eu.ess.xaos.tools.annotation.ServiceLoaderUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.tools.apputils.Preferences;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Logbook {

    private static final String LOGBOOK_PROVIDER = "defaultLogbookProvider";
    private static final Logger LOGGER = Logger.getLogger(Logbook.class.getName());

    private Logbook() {
        // Utility class
    }

    public static List<LogbookProvider> getLogbookProviders() {
        return ServiceLoaderUtilities.of(ServiceLoader.load(LogbookProvider.class));
    }

    public static LogbookProvider getDefaultLogbookProvider() throws LogbookException {
        List<LogbookProvider> logbooksProviders = getLogbookProviders();

        if (logbooksProviders.isEmpty()) {
            throw new LogbookException("No logbook service provider found.");
        }
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(Logbook.class);
        String providerName = defaults.get(LOGBOOK_PROVIDER, null);

        if (providerName != null) {
            for (LogbookProvider provider : logbooksProviders) {
                if (providerName.equals(provider.getClass().getName())) {
                    return provider;
                }
            }
        }

        LOGGER.log(Level.INFO, "Default logbook not found in preferences, using the provider with lower order.");
        return logbooksProviders.get(0);
    }

    public static void setDefaultLogbookProvider(LogbookProvider provider) throws LogbookException {
        List<LogbookProvider> logbooksProviders = getLogbookProviders();

        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(Logbook.class);
        for (LogbookProvider provider_i : logbooksProviders) {
            if (provider.getClass().equals(provider_i.getClass())) {
                defaults.put(LOGBOOK_PROVIDER, provider.getClass().getName());
                return;
            }
        }

        throw new LogbookException("The service provider is not recognised.");
    }
}
