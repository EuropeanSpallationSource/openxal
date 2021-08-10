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
import xal.extension.jels.smf.impl.FieldMap;
import xal.extension.jels.smf.impl.RfFieldMap;
import xal.model.IProbe;
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
    private double synchronousPhase = 0;

    private double[] deltaPhiArr = null;
    private double[] energyGainArr = null;
    private double[] sinIntegralArr = null;

    private double dblAmpFactor;
    private double dblPhaseFactor;

    /**
     * ETL product of gap
     */
    private double dblETL = 0.0;

    private double dblE0 = 0.0;

    /**
     * phase delay of gap w.r.t. the synchronous particle
     */
    private double dblPhase = 0.0;

    /**
     * operating frequency of the gap
     */
    private double dblFreq = 0.0;

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

        dblETL = fieldmap.getGapDfltE0TL() * 1e6;
        dblFreq = fieldmap.getGapDfltFrequency() * 1e6;
        dblPhase = fieldmap.getGapDfltPhase() * Math.PI / 180.;
        dblE0 = fieldmap.getGapDfltAmp() * 1e6;

        dblAmpFactor = fieldmap.getRfGap().getAmpFactor();
        dblPhaseFactor = fieldmap.getRfGap().getPhaseFactor();
    }

    /**
     * Method calculates the phase drift and the energy gain on the current
     * range (i.e from probe.getPosition, and for dblLength).
     *
     * @throws xal.model.ModelException
     */
    public void computePhaseDriftAndEnergyGain(IProbe probe, double dblLen) {

        startPosition = getLatticePosition() - getLength() / 2. - sliceStartPosition;

        double initialPhase;
        if (Math.abs(probe.getPosition() - startPosition) < 1e-6 || !probe.getAlgorithm().getRfGapPhaseCalculation()) {
            initialPhase = getPhase();
        } else {
            initialPhase = probe.getLongitinalPhase();
        }

        // Find the field map points included in the current slice.
        List<Double> fieldMapPointPositions = rfFieldmap.getFieldMapPointPositions(probe.getPosition() - startPosition, dblLen);

        int numberOfPoints = fieldMapPointPositions.size();

        // First a drift from the slice start to the first point of the field 
        // map. It could be the end point and only a drift space is calculated.
        double dz = (numberOfPoints > 0 ? fieldMapPointPositions.get(0) - (probe.getPosition() - startPosition) : 0.0);

        double gamma;
        double beta;

        deltaPhiArr = new double[numberOfPoints + 1];
        energyGainArr = new double[numberOfPoints + 1];
        sinIntegralArr = new double[numberOfPoints + 1];

        deltaPhiArr[0] = 0;
        energyGainArr[0] = 0;
        sinIntegralArr[0] = 0;

        for (int i = 0; i < numberOfPoints; i++) {
            gamma = (probe.getKineticEnergy() + energyGainArr[i]) / probe.getSpeciesRestEnergy() + 1.0;
            beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));

            deltaPhiArr[i + 1] = deltaPhiArr[i] + 2 * Math.PI * getFrequency() * dz / (beta * LIGHT_SPEED);

            // Set the length of the following kick.
            dz = getCellLength();

            FieldMapPoint fieldMapPoint = rfFieldmap.getFieldAt(fieldMapPointPositions.get(i));

            if (fieldMapPoint == null) {
                energyGainArr[i + 1] = energyGainArr[i] + 0;
                sinIntegralArr[i + 1] = sinIntegralArr[i] + 0;
                deltaPhiArr[i + 1] = deltaPhiArr[i] + 0;
                continue;
            }

            fieldMapPoint.setAmplitudeFactorE(getE0());

            // First and last slices of the element get half a kick
            if ((Math.abs(fieldMapPointPositions.get(i) - startPosition) < 1e-6) || (Math.abs(fieldMapPointPositions.get(i) - startPosition - rfFieldmap.getLength()) < 1e-6)) {
                dz /= 2.;
            }

            energyGainArr[i + 1] = energyGainArr[i] + fieldMapPoint.getEz() * dz * Math.cos(initialPhase + deltaPhiArr[i + 1]);
            sinIntegralArr[i + 1] = sinIntegralArr[i] + fieldMapPoint.getEz() * dz * Math.sin(initialPhase + deltaPhiArr[i + 1]);

            // Set the length of the following drift spaces.
            dz = getCellLength();
        }

        dz = (numberOfPoints > 0 ? probe.getPosition() - startPosition + dblLen - fieldMapPointPositions.get(numberOfPoints - 1) : dblLen);

        gamma = (probe.getKineticEnergy() + energyGainArr[numberOfPoints]) / probe.getSpeciesRestEnergy() + 1.0;
        beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));

        deltaPhi = deltaPhiArr[numberOfPoints] + 2 * Math.PI * getFrequency() * dz / (beta * LIGHT_SPEED);
        energyGain = energyGainArr[numberOfPoints];
        synchronousPhase = Math.atan2(sinIntegralArr[numberOfPoints], energyGainArr[numberOfPoints]);
    }

    /**
     * Method calculates transfer matrix for the field map on the current range
     * (i.e from probe.getPosition, and for dblLength).
     *
     * @return
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen) {
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
        FieldMapIntegrator integrator = FieldMapIntegrator.identity();
        integrator.setCoupled(rfFieldmap.isCoupled());

        // Calculating the length of the first drift from the slice start to the
        // first point of the field map. It could be the end point and only a 
        // drift space is calculated.
        double dz = (numberOfPoints > 0 ? fieldMapPointPositions.get(0) - (probe.getPosition() - startPosition) : 0.0);

        // Add kicks and drifts for each intermediate point (could be none).
        for (int i = 0; i < numberOfPoints; i++) {
            integrator.timesDriftLeft(dz);

            // Set the length of the following kick.
            dz = getCellLength();

            // First and last slices of the element get half a kick
            if ((Math.abs(fieldMapPointPositions.get(i) - startPosition) < 1e-6) || (Math.abs(fieldMapPointPositions.get(i) - startPosition - rfFieldmap.getLength()) < 1e-6)) {
                dz /= 2.;
            }

            FieldMapPoint fieldMapPoint = rfFieldmap.getFieldAt(fieldMapPointPositions.get(i));

            fieldMapPoint.setAmplitudeFactorE(getE0() * Math.cos(phiS + deltaPhiArr[i + 1]));
            fieldMapPoint.setAmplitudeFactorB(2.0 * Math.PI * getFrequency() / (LIGHT_SPEED * LIGHT_SPEED) * getE0() * Math.sin(phiS + deltaPhiArr[i + 1]));

            // Kick
            integrator.timesKick(probe, dz, fieldMapPoint, energyGainArr[i]);

            // Set the length of the following drift spaces.
            dz = getCellLength();
        }

        // Last drift space (if any).
        dz = (numberOfPoints > 0 ? probe.getPosition() - startPosition + dblLen - fieldMapPointPositions.get(numberOfPoints - 1) : dblLen);

        integrator.timesDriftLeft(dz);

        // Jan 2019 - Natalia Milas
        // apply alignment and rotation errors   
        PhaseMatrix transferMatrix = applyErrors((PhaseMatrix) integrator, probe, dblLen);

        return new PhaseMap(transferMatrix);
    }

    @Override
    public double longitudinalPhaseAdvance(IProbe probe, double dblLen) {
        computePhaseDriftAndEnergyGain(probe, dblLen);

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
        return energyGain;
    }

    @Override
    public double elapsedTime(IProbe probe, double dblLen) {
        return longitudinalPhaseAdvance(probe, dblLen) / (2 * Math.PI * getFrequency());
    }

    @Override
    public void setETL(double dblETL) {
        this.dblETL = dblETL;
    }

    @Override
    public void setE0(double cavAmp) {
        dblE0 = cavAmp * dblAmpFactor;
    }

    /**
     *
     * @param cavPhase
     */
    @Override
    public void setPhase(double cavPhase) {
        dblPhase = cavPhase + dblPhaseFactor;
    }

    @Override
    public void setFrequency(double dblFreq) {
        this.dblFreq = dblFreq;
    }

    @Override
    public double getETL() {
        return dblETL;
    }

    @Override
    public double getPhase() {
        return dblPhase;
    }

    @Override
    public double getFrequency() {
        return dblFreq;
    }

    @Override
    public double getE0() {
        return dblE0;
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
        return isFirstGap();
    }

    @Override
    public void computeSynchronousPhaseAndEnergyGain(IProbe probe) {
        double dblLen = getLength();
        computePhaseDriftAndEnergyGain(probe, dblLen);
    }

    @Override
    public double getSynchronousPhase() {
        return synchronousPhase;
    }

    @Override
    public double getEnergyGain() {
        return energyGain;
    }
}
