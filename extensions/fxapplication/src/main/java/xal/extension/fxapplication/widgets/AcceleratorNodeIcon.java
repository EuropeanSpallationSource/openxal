/*
 * Copyright (C) 2020 European Spallation Source ERIC.
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
package xal.extension.fxapplication.widgets;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 * This class loads a mapping of accelerator nodes with their corresponding icon
 * from XML.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@ess.eu>
 */
public class AcceleratorNodeIcon {

    private Map<String, String> map = new HashMap<>();

    private static AcceleratorNodeIcon instance = null;

    private AcceleratorNodeIcon() {
        try {
            XmlDataAdaptor document_adaptor = XmlDataAdaptor.adaptorForUrl(getClass().getResource("iconMapping.xml"), false);
            DataAdaptor iconMapping = document_adaptor.childAdaptor("iconMapping");
            for (DataAdaptor element : iconMapping.childAdaptors()) {
                map.put(element.stringValue("type"), element.stringValue("icon"));
            }
        } catch (XmlDataAdaptor.ParseException | XmlDataAdaptor.ResourceNotFoundException  ex) {
            Logger.getLogger(AcceleratorNodeIcon.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getIcon(String nodeType) {
        if (instance == null) {
            instance = new AcceleratorNodeIcon();
        }

        return instance.map.get(nodeType);
    }
}
