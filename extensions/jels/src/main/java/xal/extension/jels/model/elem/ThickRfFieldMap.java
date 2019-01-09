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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.extension.jels.smf.impl.FieldMap;
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

    private static final Logger LOGGER = Logger.getLogger(ThickRfFieldMap.class.getName());

    private FieldMap rfFieldmap;

    private double cellLength = 0;
    private double position = 0;
    private double deltaPhi = 0;
    private double energyGain = 0;

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

        xal.extension.jels.smf.impl.RfFieldMap fieldmap = (xal.extension.jels.smf.impl.RfFieldMap) element.getHardwareNode();
        // This position is the start of the element inside the current
        // sequence. Using the first slice to be able to compute the probe 
        // position from the element entrance and not from the slice start.
        position = element.getFirstSlice().getStartPosition();
        if (element.getStartPosition() == 0) {
            initialGap = true;
        }
        rfFieldmap = fieldmap.getFieldMap();
        cellLength = fieldmap.getSliceLength();
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

        double phiS;
        if (probe.getPosition() == position || !probe.getAlgorithm().getRfGapPhaseCalculation()) {
            phiS = getPhase();
        } else {
            phiS = probe.getLongitinalPhase();
        }
        
        // Find the field map points included in the current slice.
        List<Double> fieldMapPointPositions = getFieldMapPointPositions(probe, dblLen);

        // Start building the transfer map
        PhaseMatrix transferMatrix = PhaseMatrix.identity();

        // First a drift from the slice start to the first point of the field 
        // map. It could be the end point and only a drift space is calculated.
        double dz = fieldMapPointPositions.get(1) - fieldMapPointPositions.get(0);
        transferMatrix.setElem(0, 1, dz);
        transferMatrix.setElem(2, 3, dz);
        transferMatrix.setElem(4, 5, dz);

        double gamma = probe.getKineticEnergy() / probe.getSpeciesRestEnergy() + 1.0;
        double beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));
        deltaPhi = 2 * Math.PI * getFrequency() * dz / (beta * LightSpeed);

        energyGain = 0;
        // Add kicks and drifts for each intermediate point (could be none).
        for (int i = 1; i < fieldMapPointPositions.size() - 1; i++) {
            FieldMapPoint fieldMapPoint = rfFieldmap.getFieldAt(fieldMapPointPositions.get(i));

            // Length to compute the kicks.
            dz = getCellLength();

            fieldMapPoint.setAmplitudeFactorE(getE0() * Math.cos(phiS + deltaPhi));
            fieldMapPoint.setAmplitudeFactorB(2.0 * Math.PI * getFrequency() / (LightSpeed * LightSpeed) * getE0() * Math.sin(phiS + deltaPhi));

            // Kick
            transferMatrix = FieldMapIntegrator.transferMap(probe, dz, fieldMapPoint).times(transferMatrix);

            energyGain += fieldMapPoint.getEz() * dz;

            // Drift space (can be 0 length).
            dz = fieldMapPointPositions.get(i + 1) - fieldMapPointPositions.get(i);

            PhaseMatrix driftMatrix = PhaseMatrix.identity();
            driftMatrix.setElem(0, 1, dz);
            driftMatrix.setElem(2, 3, dz);
            driftMatrix.setElem(4, 5, dz);

            transferMatrix = driftMatrix.times(transferMatrix);

            gamma = (probe.getKineticEnergy() + energyGain) / probe.getSpeciesRestEnergy() + 1.0;
            beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));

            deltaPhi += 2 * Math.PI * getFrequency() * dz / (beta * LightSpeed);
        }

        return new PhaseMap(transferMatrix);
    }

    @Override
    public double longitudinalPhaseAdvance(IProbe probe, double dblLen) {
        try {
            transferMap(probe, dblLen);
        } catch (ModelException ex) {
            Logger.getLogger(ThickRfFieldMap.class.getName()).log(Level.SEVERE, null, ex);
        }

        // WORKAROUND to set the initial phase
        if (probe.getPosition() == position) {
            double phi0 = this.getPhase();
            double phi = probe.getLongitinalPhase();
            return deltaPhi - phi + phi0;
        }
        return deltaPhi;
    }

    @Override
    public double energyGain(IProbe probe, double dblLen) {
        try {
            transferMap(probe, dblLen);
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

    private List<Double> getFieldMapPointPositions(IProbe probe, double dblLen) {
        // Find the field map points included in the current slice.
        List<Double> fieldMapPointPositions = new ArrayList<>();
        // Initial point can be repeated to take into account the initial energy kick.
        fieldMapPointPositions.add(probe.getPosition());
        for (double pos : rfFieldmap.getLongitudinalPositions()) {
            if (pos >= probe.getPosition() && pos < probe.getPosition() + dblLen) {
                fieldMapPointPositions.add(pos);
            }
        }
        fieldMapPointPositions.add(probe.getPosition() + dblLen);

        // If last point of the field map is included, add the last point a
        // second time to take into account for the energy kick.
        if (position + rfFieldmap.getLength() == probe.getPosition() + dblLen) {
            fieldMapPointPositions.add(probe.getPosition() + dblLen);
        }
        return fieldMapPointPositions;
    }
}
