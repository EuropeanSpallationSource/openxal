/*
 * Copyright (C) 2019 European Spallation Source ERIC.
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
package xal.extension.jels.smf.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FieldMapFactory {

    private static Map<String, FieldMap> instances = new HashMap<>();

    public enum FieldType {
        ELECTRIC,
        MAGNETIC
    }

    public static Map<String, FieldMap> getInstances() {
        return instances;
    }

    /**
     * Static factory method to give field profile for a specific file
     *
     * @param path path to the field map file
     * @param filename file name without extension
     * @param dynamic whether the field changes over time or not
     * @param fieldType electric or magnetic
     * @param dimensions 1D, 2D, or 3D (only integer)
     * @param numberOfPoints number of points to use for integration. It can be
     * lower or higher than the number of points in the field map
     * @return field profile
     */
    public static FieldMap getInstance(String path, String filename, boolean dynamic,
            FieldType fieldType, int dimensions, int numberOfPoints) {
        String key = null;
        try {
            key = new URI(path).resolve(filename).toString() + numberOfPoints;
        } catch (URISyntaxException ex) {
            Logger.getLogger(FieldMapFactory.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (instances.containsKey(key)) {
            return instances.get(key);
        }

        FieldMap fieldMap = null;

        switch (fieldType) {
            case ELECTRIC:
                switch (dimensions) {
                    case 1:
                        if (dynamic) {
                            fieldMap = new RfFieldMap1D(path, filename, numberOfPoints);
                        }
                        break;
//                    case 2:
//                        if (dynamic) {
//                            fieldMap = new RfFieldMap2D(path, filename, numberOfPoints);
//                        }
//                        break;
//                    case 3:
//                        if (dynamic) {
//                            fieldMap = new RfFieldMap3D(path, filename, numberOfPoints);
//                        }
//                        break;
                    default:
                        return null;
                }
                break;
            case MAGNETIC:
                switch (dimensions) {
//                    case 1:
//                        if (!dynamic) {
//                            fieldMap = new MagFieldMap1D(path, filename, numberOfPoints);
//                        }
//                        break;
                    case 2:
                        if (!dynamic) {
                            fieldMap = new MagFieldMap2D(path, filename, numberOfPoints);
                        }
                        break;
                    case 3:
                        if (!dynamic) {
                            fieldMap = new MagFieldMap3D(path, filename, numberOfPoints);
                        }
                        break;
                    default:
                        return null;
                }
                break;
            default:
                return null;

        }

        instances.put(key, fieldMap);
        return fieldMap;
    }
}
