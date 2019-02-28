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
package xal.extension.jels.model.elem;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.extension.jels.smf.impl.FieldMap;
import xal.extension.jels.smf.impl.RfFieldMap;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ThickElement;
import xal.model.elem.sync.IRfCavityCell;
import xal.model.elem.sync.IRfGap;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

/**
 * Thick element version for RF field map implementation. This class is supposed
 * to be faster than the {@link xal.extension.jels.model.elem.ThinRfFieldMap}
 * for fieldmaps with many data points, since it removes the overhead of
 * creating an element for every point in the fieldmap. The drawback is that it
 * can't be superposed to other ThickElements.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 *
 */
public class ThickRfFieldMap extends ThickElement implements IRfGap, IRfCavityCell {

    private FieldMap rfFieldmap;

    private double cellLength = 0;
    private double startPosition = 0;
    private double deltaPhi = 0;
    private double energyGain = 0;

    private double[] a_deltaPhi = null;
    private double[] a_energyGain = null;

    /**
     * ETL product of gap
     */
    private double m_dblETL = 0.0;

    private double m_dblE0 = 0.0;

    /**
     * phase delay of gap w.r.t. the synchronous particle
     */
    private double m_dblPhase = 0.0;

    /**
     * operating frequency of the gap
     */
    private double m_dblFreq = 0.0;

    /**
     * flag indicating that this is the leading gap of a cavity
     */
    private boolean initialGap = false;
    private double sliceStartPosition;

    public ThickRfFieldMap() {
        this(null);
    }

    public ThickRfFieldMap(String strId) {
        super("FieldMap", strId);
    }

    public double getCellLength() {
        return cellLength;
    }

    public void setCellLength(double cellLength) {
        this.cellLength = cellLength;
    }

    @Override
    public void initializeFrom(LatticeElement element) {
        super.initializeFrom(element);

        final RfFieldMap fieldmap = (RfFieldMap) element.getHardwareNode();

        if (Math.abs(element.getStartPosition() - (fieldmap.getPosition() - fieldmap.getLength() / 2.0)) < 1e-6) {
            initialGap = true;
        }
        sliceStartPosition = element.getStartPosition() - (fieldmap.getPosition() - fieldmap.getLength() / 2.0);
        rfFieldmap = fieldmap.getFieldMap();
        cellLength = fieldmap.getSliceLength();
    }

    /**
     * Method calculates the phase drift and the energy gain on the current
     * range (i.e from probe.getPosition, and for dblLength).
     *
     * @throws xal.model.ModelException
     */
    public void computePhaseDriftAndEnergyGain(IProbe probe, double dblLen)
            throws ModelException {

        startPosition = getLatticePosition() - getLength() / 2. - sliceStartPosition;

        double phiS;
        if (Math.abs(probe.getPosition() - startPosition) < 1e-6 || !probe.getAlgorithm().getRfGapPhaseCalculation()) {
            phiS = getPhase();
        } else {
            phiS = probe.getLongitinalPhase();
        }

        // Find the field map points included in the current slice.
        List<Double> fieldMapPointPositions = rfFieldmap.getFieldMapPointPositions(probe.getPosition() - startPosition, dblLen);

        int numberOfPoints = fieldMapPointPositions.size();

        // First a drift from the slice start to the first point of the field 
        // map. It could be the end point and only a drift space is calculated.
        double dz = (numberOfPoints > 0 ? fieldMapPointPositions.get(0) - (probe.getPosition() - startPosition) : 0.0);

        double gamma;
        double beta;

        a_deltaPhi = new double[numberOfPoints + 1];
        a_energyGain = new double[numberOfPoints + 1];

        a_deltaPhi[0] = 0;
        a_energyGain[0] = 0;

        for (int i = 0; i < numberOfPoints; i++) {
            gamma = (probe.getKineticEnergy() + a_energyGain[i]) / probe.getSpeciesRestEnergy() + 1.0;
            beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));

            a_deltaPhi[i + 1] = a_deltaPhi[i] + 2 * Math.PI * getFrequency() * dz / (beta * LightSpeed);

            // Set the length of the following drift spaces.
            dz = getCellLength();

            FieldMapPoint fieldMapPoint = rfFieldmap.getFieldAt(fieldMapPointPositions.get(i));

            if (fieldMapPoint == null) {
                a_energyGain[i + 1] = a_energyGain[i] + 0;
                a_deltaPhi[i + 1] = a_deltaPhi[i] + 0;
                continue;
            }
            a_energyGain[i + 1] = a_energyGain[i] + fieldMapPoint.getEz() * dz * getE0() * Math.cos(phiS + a_deltaPhi[i + 1]);
        }

        dz = (numberOfPoints > 0 ? probe.getPosition() - startPosition + dblLen - fieldMapPointPositions.get(numberOfPoints - 1) : dblLen);

        gamma = (probe.getKineticEnergy() + a_energyGain[numberOfPoints]) / probe.getSpeciesRestEnergy() + 1.0;
        beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));

        deltaPhi = a_deltaPhi[numberOfPoints] + 2 * Math.PI * getFrequency() * dz / (beta * LightSpeed);
        energyGain = a_energyGain[numberOfPoints];
    }

    /**
     * Method calculates transfer matrix for the field map on the current range
     * (i.e from probe.getPosition, and for dblLength).
     *
     * @return
     * @throws xal.model.ModelException
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen)
            throws ModelException {

        computePhaseDriftAndEnergyGain(probe, dblLen);

        double phiS;
        if (Math.abs(probe.getPosition() - startPosition) < 1e-6 || !probe.getAlgorithm().getRfGapPhaseCalculation()) {
            phiS = getPhase();
        } else {
            phiS = probe.getLongitinalPhase();
        }

        // Find the field map points included in the current slice.
        List<Double> fieldMapPointPositions = rfFieldmap.getFieldMapPointPositions(probe.getPosition() - startPosition, dblLen);

        int numberOfPoints = fieldMapPointPositions.size();
        PhaseMatrix driftMatrix = PhaseMatrix.identity();
        PhaseMatrix transferMatrix = PhaseMatrix.identity();

        // Calculating the length of the first drift from the slice start to the
        // first point of the field map. It could be the end point and only a 
        // drift space is calculated.
        double dz = (numberOfPoints > 0 ? fieldMapPointPositions.get(0) - (probe.getPosition() - startPosition) : 0.0);

        // Add kicks and drifts for each intermediate point (could be none).
        for (int i = 0; i < numberOfPoints; i++) {
            driftMatrix.setElem(0, 1, dz);
            driftMatrix.setElem(2, 3, dz);
            driftMatrix.setElem(4, 5, dz);

            transferMatrix = driftMatrix.times(transferMatrix);

            // Set the length of the following drift spaces.
            dz = getCellLength();

            FieldMapPoint fieldMapPoint = rfFieldmap.getFieldAt(fieldMapPointPositions.get(i));

            fieldMapPoint.setAmplitudeFactorE(getE0() * Math.cos(phiS + a_deltaPhi[i + 1]));
            fieldMapPoint.setAmplitudeFactorB(2.0 * Math.PI * getFrequency() / (LightSpeed * LightSpeed) * getE0() * Math.sin(phiS + a_deltaPhi[i + 1]));

            // Kick
            transferMatrix = FieldMapIntegrator.transferMap(probe, dz, fieldMapPoint, a_energyGain[i]).times(transferMatrix);
        }

        // Last drift space (if any).
        dz = (numberOfPoints > 0 ? probe.getPosition() - startPosition + dblLen - fieldMapPointPositions.get(numberOfPoints - 1) : dblLen);
        driftMatrix.setElem(0, 1, dz);
        driftMatrix.setElem(2, 3, dz);
        driftMatrix.setElem(4, 5, dz);

        transferMatrix = driftMatrix.times(transferMatrix);
        
        // Jan 2019 - Natalia Milas
        // apply alignment and rotation errors   
        transferMatrix = applySliceErrors(transferMatrix, probe, dblLen);


        return new PhaseMap(transferMatrix);
    }

    @Override
    public double longitudinalPhaseAdvance(IProbe probe, double dblLen) {
        try {
            computePhaseDriftAndEnergyGain(probe, dblLen);
        } catch (ModelException ex) {
            Logger.getLogger(ThickRfFieldMap.class.getName()).log(Level.SEVERE, null, ex);
        }

        // WORKAROUND to set the initial phase
        if (Math.abs(probe.getPosition() - startPosition) < 1e-6) {
            double phi0 = getPhase();
            double phi = probe.getLongitinalPhase();
            return deltaPhi - phi + phi0;
        }

        return deltaPhi;
    }

    @Override
    public double energyGain(IProbe probe, double dblLen) {
        try {
            computePhaseDriftAndEnergyGain(probe, dblLen);
        } catch (ModelException ex) {
            Logger.getLogger(ThickRfFieldMap.class.getName()).log(Level.SEVERE, null, ex);
        }

        return energyGain;
    }

    @Override
    public double elapsedTime(IProbe probe, double dblLen) {
        return longitudinalPhaseAdvance(probe, dblLen) / (2 * Math.PI * getFrequency());
    }

    @Override
    public void setETL(double dblETL) {
        m_dblETL = dblETL;
    }

    @Override
    public void setE0(double E) {
        m_dblE0 = E;
    }

    @Override
    public void setPhase(double dblPhase) {
        m_dblPhase = dblPhase;
    }

    @Override
    public void setFrequency(double dblFreq) {
        m_dblFreq = dblFreq;
    }

    @Override
    public double getETL() {
        return m_dblETL;
    }

    @Override
    public double getPhase() {
        return m_dblPhase;
    }

    @Override
    public double getFrequency() {
        return m_dblFreq;
    }

    @Override
    public double getE0() {
        return m_dblE0;
    }

    @Override
    public boolean isFirstGap() {
        return initialGap;
    }

    @Override
    public void setCavityCellIndex(int indCell) {
        // It does nothing so far, only one fieldmap is used per cavity.
    }

    @Override
    public void setCavityModeConstant(double dblCavModeConst) {
        // It does nothing so far, only one fieldmap is used per cavity.
    }

    @Override
    public int getCavityCellIndex() {
        return 0;
    }

    @Override
    public double getCavityModeConstant() {
        return 0;
    }

    @Override
    public boolean isEndCell() {
        return false;
    }

    @Override
    public boolean isFirstCell() {
        return initialGap;
    }
}
