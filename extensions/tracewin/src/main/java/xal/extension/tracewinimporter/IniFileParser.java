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

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;

/**
 * This class parses initial parameters from TraceWin project files (.ini). In
 * particular, it retrieves the beam current, initial kinetic energy and the
 * Twiss parameters for the 3 planes.
 * <br><br>
 * NOTE: the position in the .ini files was found by reverse engineering, as
 * there is no documentation about them. The format is believed to be standard
 * for different versions of TraceWin, but that can change.
 *
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class IniFileParser {

    private double kineticEnergy;       // in eV
    private PhaseVector initialCentroid = null;
    private Twiss[] initialTwiss = null;
    private double beamCurrent = 0;         // in A
    private double bunchFrequency = 0;  // in MHz

    public double getBunchFrequency() {
        return bunchFrequency;
    }

    public double getBeamCurrent() {
        return beamCurrent;
    }

    public double getKineticEnergy() {
        return kineticEnergy;
    }

    public PhaseVector getInitialCentroid() {
        return initialCentroid;
    }

    public Twiss[] getInitialTwiss() {
        return initialTwiss;
    }

    /**
     * Load initial parameters from TraceWin project files (.ini).
     *
     * @param iniFilePath The path of the .ini file.
     */
    public void loadTwissFromIni(String iniFilePath) {

        BufferedInputStream iniFile = null;

        double emittanceX = 0;
        double emittanceY = 0;
        double emittanceZ = 0;
        double alphaX = 0;
        double alphaY = 0;
        double alphaZ = 0;
        double betaX = 0;
        double betaY = 0;
        double betaZ = 0;
        double centerX = 0;
        double centerpX = 0;
        double centerY = 0;
        double centerpY = 0;
        double centerZ = 0;
        double centerpZ = 0;

        try {
            iniFile = new BufferedInputStream(new URL(iniFilePath).openStream());

            int nBytes = 8;
            byte[] doubleAux = new byte[8];

            long currentOffset = 0;

            // Read bunch frequency (MHz)
            long offsetFrequency = 0x2f24;
            iniFile.skip(offsetFrequency);
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                bunchFrequency = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            currentOffset += offsetFrequency + nBytes;

            // Read beam current (A)
            long offsetCurrent = 0x2f34;
            iniFile.skip(offsetCurrent - currentOffset);
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                beamCurrent = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            currentOffset = offsetCurrent + nBytes;

            // Read kinetic energy (eV) and emittances (normalized)
            long offsetKineticEnergy = 0x2f44;

            iniFile.skip(offsetKineticEnergy - currentOffset);
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                kineticEnergy = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            iniFile.skip(nBytes);
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                emittanceX = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            iniFile.skip(nBytes);
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                emittanceY = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            iniFile.skip(nBytes);
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                emittanceZ = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }

            currentOffset = offsetKineticEnergy + 7 * nBytes;

            // Now move to the area where beam centroid parameters are stored
            long offsetCenterX = 0x3024;

            iniFile.skip(offsetCenterX - currentOffset);
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                centerX = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                centerY = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                centerpX = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                centerpY = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                centerZ = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                centerpZ = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }

            currentOffset = offsetCenterX + 6 * nBytes;

            // Now move to the area where alpha and beta parameters are stored
            long offsetAlphaX = 0x30dc;

            iniFile.skip(offsetAlphaX - currentOffset);
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                alphaX = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                betaX = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            iniFile.skip(2 * nBytes);
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                alphaY = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                betaY = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            iniFile.skip(2 * nBytes);
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                alphaZ = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
            if (iniFile.read(doubleAux, 0, nBytes) == nBytes) {
                betaZ = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TraceWin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IniFileParser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (iniFile != null) {
                try {
                    iniFile.close();
                } catch (IOException ex) {
                    Logger.getLogger(IniFileParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        initialTwiss = new Twiss[]{new Twiss(alphaX, betaX, emittanceX),
            new Twiss(alphaY, betaY, emittanceY),
            new Twiss(alphaZ, betaZ, emittanceZ)};

        initialCentroid = new PhaseVector(centerX, centerpX, centerY, centerpY, centerZ, centerpZ);
    }
}
