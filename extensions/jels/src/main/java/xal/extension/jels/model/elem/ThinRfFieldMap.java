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

import xal.extension.jels.smf.impl.FieldMap;
import xal.extension.jels.smf.impl.RfFieldMap;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ThinElement;
import xal.model.elem.sync.IRfCavityCell;
import xal.model.elem.sync.IRfGap;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

/**
 * Thin element version for RF field map implementation. This class allows
 * superposition to other fieldmaps, but has the overhead of creating an element
 * for every point in the fieldmap.
 * <p>
 * You should use {@link xal.extension.jels.model.elem.ThickRfFieldMap} for big
 * non-superposed fieldmaps for high performance.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 *
 */
public class ThinRfFieldMap extends ThinElement implements IRfGap, IRfCavityCell {

    private FieldMap rfFieldmap;

    private double cellLength = 0;
    private double deltaPhi = 0;
    private double energyGain = 0;
    private double startPosition = 0;
    private double centerPosition = 0;
    private double position = 0;

    private double m_dblAmpFactor;
    private double m_dblPhaseFactor;

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

    public ThinRfFieldMap() {
        this(null);
    }

    public ThinRfFieldMap(String strId) {
        super("RfFieldMap", strId);
    }

    public double getCellLength() {
        return cellLength;
    }

    public void setCellLength(double cellLength) {
        this.cellLength = cellLength;
    }

    public double getDeltaPhi() {
        return deltaPhi;
    }

    public void setDeltaPhi(double deltaPhi) {
        this.deltaPhi = deltaPhi;
    }

    @Override
    public void initializeFrom(LatticeElement element) {
        super.initializeFrom(element);

        final RfFieldMap fieldmap = (RfFieldMap) element.getHardwareNode();

        startPosition = fieldmap.getPosition() - fieldmap.getLength() / 2.0;
        position = element.getStartPosition();
        centerPosition = fieldmap.getPosition();
        if (position == 0) {
            initialGap = true;
        }
        rfFieldmap = fieldmap.getFieldMap();
        cellLength = fieldmap.getSliceLength();
        // Always 0. This is valid only if the energy gain is small enough.
        deltaPhi = 0;

        m_dblETL = fieldmap.getGapDfltE0TL() * 1e6;
        m_dblFreq = fieldmap.getGapDfltFrequency() * 1e6;
        m_dblPhase = fieldmap.getGapDfltPhase() * Math.PI / 180.;
        m_dblE0 = fieldmap.getGapDfltAmp() * 1e6;

        m_dblAmpFactor = fieldmap.getRfGap().getAmpFactor();
        m_dblPhaseFactor = fieldmap.getRfGap().getPhaseFactor();
    }

    /**
     * Method calculates transfer matrix for the field map for a given data
     * point in the field map.Drift spaces are calculated separately.
     *
     * @param probe
     * @return
     * @throws xal.model.ModelException
     */
    @Override
    public PhaseMap transferMap(IProbe probe)
            throws ModelException {

        double phiS;
        if (isFirstGap() || !probe.getAlgorithm().getRfGapPhaseCalculation()) {
            phiS = getPhase();
        } else {
            phiS = probe.getLongitinalPhase();
        }

        double dz = getCellLength();

        FieldMapPoint fieldMapPoint = rfFieldmap.getFieldAt(position - startPosition);

        fieldMapPoint.setAmplitudeFactorE(getE0() * Math.cos(phiS));
        fieldMapPoint.setAmplitudeFactorB(2.0 * Math.PI * getFrequency() / (LightSpeed * LightSpeed) * getE0() * Math.sin(phiS));

        // First and last slices of the element get half a kick
        if ((Math.abs(position - startPosition) < 1e-6) || (Math.abs(position - startPosition - rfFieldmap.getLength()) < 1e-6)) {
            dz /= 2.;
        }

        // Set energy gain and phase
        energyGain = fieldMapPoint.getEz() * dz;
        deltaPhi = 0;

        FieldMapIntegrator integrator = FieldMapIntegrator.identity();
        integrator.setCoupled(rfFieldmap.isCoupled());
        integrator.timesKick(probe, dz, fieldMapPoint, energyGain);

        // Jan 2019 - Natalia Milas
        // apply alignment and rotation errors   
        double slicepos = centerPosition - position; // distance from the probe position and element center
        PhaseMatrix transferMatrix = applyErrors((PhaseMatrix) integrator, slicepos);

        return new PhaseMap(transferMatrix);
    }

    @Override
    protected double longitudinalPhaseAdvance(IProbe probe) {
        // WORKAROUND to set the initial phase
        if (isFirstGap()) {
            double phi0 = this.getPhase();
            double phi = probe.getLongitinalPhase();
            return deltaPhi - phi + phi0;
        }
        return deltaPhi;
    }

    @Override
    protected double elapsedTime(IProbe probe) {
        return 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation assumes that this method is always called after the
     * transferMap method. This is true for the
     * {@link xal.model.alg.EnvelopeTracker}, but might differ for other
     * algorithms.
     */
    @Override
    protected double energyGain(IProbe probe) {
        return energyGain;
    }

    @Override
    public void setETL(double dblETL) {
        m_dblETL = dblETL;
    }

    @Override
    public void setE0(double cavAmp) {
        m_dblE0 = cavAmp * m_dblAmpFactor;
    }

    @Override
    public void setPhase(double cavPhase) {
        m_dblPhase = cavPhase + m_dblPhaseFactor;
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
