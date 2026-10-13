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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class to get the right {@link xal.extension.jels.smf.impl.FieldMap}
 * subclass.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class FieldMapFactory {

    private static final Map<String, FieldMap> instances = new ConcurrentHashMap<>();

    public enum FieldType {
        ELECTRIC,
        MAGNETIC
    }

    public static Map<String, FieldMap> getInstances() {
        return instances;
    }

    private static String key(String path, String filename, FieldType fieldType, int dimensions, int numberOfPoints) {
        return FieldMap.resolve(path, filename) + "#" + fieldType + "-" + dimensions + "D#" + numberOfPoints;
    }

    /**
     * Static factory method to add a field profile without the need to have it
     * on disk.
     *
     * @param key key used to store the fieldmap
     * @param fieldmap The FieldMap object
     */
    public static void putInstance(String key, FieldMap fieldmap) throws FieldMapException {
        if (instances.putIfAbsent(key, fieldmap) != null) {
            throw new FieldMapException("There is already a fieldmap that uses the same key: " + key);
        }
    }

    /**
     * Static factory method to add a field profile without the need to have it
     * on disk, so that {@link #getInstance} returns it for the given file.
     *
     * @param path not necessarily a path, used as a key
     * @param filename not necessarily a filename, used as a key
     * @param fieldType electric or magnetic
     * @param dimensions 1D, 2D, or 3D (only integer)
     * @param numberOfPoints number of points to use for integration. It can be
     * lower or higher than the number of points in the field map
     * @param fieldmap The FieldMap object
     */
    public static void putInstance(String path, String filename, FieldType fieldType, int dimensions,
            int numberOfPoints, FieldMap fieldmap) throws FieldMapException {
        putInstance(key(path, filename, fieldType, dimensions, numberOfPoints), fieldmap);
    }

    /**
     * Static factory method to add a field profile without the need to have it
     * on disk. The field type and dimensions are taken from the class of the
     * field map, which must be one of the built-in implementations; use
     * {@link #putInstance(String, String, FieldType, int, int, FieldMap)} for
     * other classes.
     *
     * @param path not necessarily a path, used as a key
     * @param filename not necessarily a filename, used as a key
     * @param numberOfPoints number of points to use for integration. It can be
     * lower or higher than the number of points in the field map
     * @param fieldmap The FieldMap object
     */
    public static void putInstance(String path, String filename, int numberOfPoints, FieldMap fieldmap) throws FieldMapException {
        if (fieldmap instanceof RfFieldMap1D) {
            putInstance(path, filename, FieldType.ELECTRIC, 1, numberOfPoints, fieldmap);
        } else if (fieldmap instanceof RfFieldMap3D) {
            putInstance(path, filename, FieldType.ELECTRIC, 3, numberOfPoints, fieldmap);
        } else if (fieldmap instanceof MagFieldMap2D) {
            putInstance(path, filename, FieldType.MAGNETIC, 2, numberOfPoints, fieldmap);
        } else if (fieldmap instanceof MagFieldMap3D) {
            putInstance(path, filename, FieldType.MAGNETIC, 3, numberOfPoints, fieldmap);
        } else {
            throw new FieldMapException("Cannot tell the field type and dimensions of " + fieldmap.getClass().getName()
                    + "; pass them explicitly.");
        }
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
     * @return field profile, or null if the combination of parameters is not
     * supported
     * @throws FieldMapException if the field map files cannot be loaded
     */
    public static FieldMap getInstance(String path, String filename, boolean dynamic,
            FieldType fieldType, int dimensions, int numberOfPoints) {
        if (!isSupported(dynamic, fieldType, dimensions)) {
            return null;
        }

        return instances.computeIfAbsent(key(path, filename, fieldType, dimensions, numberOfPoints),
                key -> create(path, filename, fieldType, dimensions, numberOfPoints));
    }

    private static boolean isSupported(boolean dynamic, FieldType fieldType, int dimensions) {
        switch (fieldType) {
            case ELECTRIC:
                return dynamic && (dimensions == 1 || dimensions == 3);
            case MAGNETIC:
                return !dynamic && (dimensions == 2 || dimensions == 3);
            default:
                return false;
        }
    }

    private static FieldMap create(String path, String filename, FieldType fieldType, int dimensions, int numberOfPoints) {
        if (fieldType == FieldType.ELECTRIC) {
            return dimensions == 1
                    ? new RfFieldMap1D(path, filename, numberOfPoints)
                    : new RfFieldMap3D(path, filename, numberOfPoints);
        }
        return dimensions == 2
                ? new MagFieldMap2D(path, filename, numberOfPoints)
                : new MagFieldMap3D(path, filename, numberOfPoints);
    }
}
