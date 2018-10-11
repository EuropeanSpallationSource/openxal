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
package xal.extension.jels.model.elem;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.extension.jels.smf.impl.ESSFieldMap;
import xal.extension.jels.smf.impl.FieldProfile;
import xal.extension.jels.tools.math.TTFIntegrator;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.IdealRfCavityDrift;
import xal.model.elem.ThickElement;
import xal.model.elem.sync.IRfCavityCell;
import xal.model.elem.sync.IRfGap;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.RfCavity;
import xal.tools.beam.PhaseMap;

/**
 * Implementation of NCells simulation of fieldmaps. Uses numeric integrator for
 * TTF function
 *
 * @author Ivo List <ivo.list@cosylab.com>
 *
 */
public class FieldMapNCells extends ThickElement implements IRfGap, IRfCavityCell {

    private static final Logger LOGGER = Logger.getLogger(FieldMapNCells.class.getName());

    private double frequency;

    private IdealRfGap[] gaps;
    private TTFIntegrator[] splitIntgrs;

    private double phi0;
    private double phipos;
    private boolean inverted;

    private Double startPosition;
    private Double sliceStartPosition;
    private double sliceLength;

    private FieldMapNCells firstSliceElement;

    private int indCell;
    private double dblCavModeConst;

    private double k0;

    public FieldMapNCells() {
        this(null);
    }

    public FieldMapNCells(String strId) {
        super("RfGapWithTTFIntegrator", strId, 3);
    }

    @Override
    public void initializeFrom(LatticeElement latticeElement) {
        super.initializeFrom(latticeElement);
        sliceLength = latticeElement.getLength();

        if (latticeElement.isFirstSlice()) {
            final ESSFieldMap fm = (ESSFieldMap) latticeElement.getHardwareNode();
            FieldProfile fp = fm.getFieldProfile();

            phipos = fm.getPhasePosition();

            if (fm.getParent() instanceof RfCavity) {
                //WORKAROUND difference between ESS and SNS lattice
                inverted = fm.getParent().getClass().equals(RfCavity.class) && fp.isFirstInverted();
                frequency = ((RfCavity) fm.getParent()).getCavFreq() * 1e6;
            } else {
                frequency = fm.getFrequency() * 1e6;
            }

            /*
             * Old implementation of IdealRfGap is used. First gap phase is calculated when the energy at the
             * entrance into the gap is known. Also TTF integrator is supplied with the necessary offset.
             */
            splitIntgrs = TTFIntegrator.getSplitIntegrators(fp, frequency);
            gaps = new IdealRfGap[splitIntgrs.length];

            for (int i = 0; i < splitIntgrs.length; i++) {
                gaps[i] = new IdealRfGap();
                gaps[i].setFrequency(frequency);
                gaps[i].setTTFFit(splitIntgrs[i]);
                gaps[i].setCellLength(1);
                gaps[i].setStructureMode(1);
            }

        }
        try {
            firstSliceElement = (FieldMapNCells) latticeElement.getFirstSlice().createModelingElement();
        } catch (ModelException e) {
            LOGGER.log(Level.INFO, "Couldn't load the first slice element.", e);
        }
    }

    @Override
    public void propagate(IProbe probe) throws ModelException {
        if (sliceStartPosition == null) {
            sliceStartPosition = probe.getPosition();
        }
        firstSliceElement.propagate(probe, sliceStartPosition, sliceStartPosition + sliceLength);
    }

    /**
     *
     * @param probe
     * @param sliceStartPosition
     * @param sliceEndPosition
     * @throws ModelException
     */
    public void propagate(IProbe probe, double sliceStartPosition, double sliceEndPosition) throws ModelException {
        if (startPosition == null) {
            startPosition = probe.getPosition();
        }

        double cellPos = startPosition;
        double pos = probe.getPosition();

        for (int i = 0; i < splitIntgrs.length; i++) {
            if (cellPos + splitIntgrs[i].getLength() < pos) {
                cellPos += splitIntgrs[i].getLength();
                continue;
            }
            // initialize the cell
            double phim = splitIntgrs[i].getSyncPhase(probe.getBeta());
            if (phim < 0) {
                phim += 2 * Math.PI;
            }
            double l1 = phim * probe.getBeta() * IElement.LightSpeed / (2 * Math.PI * frequency);
            double l2 = splitIntgrs[i].getLength() - l1;
            if (i == 0) {
                double phim0 = phipos / (probe.getBeta() * IElement.LightSpeed / (2 * Math.PI * frequency));
                double phiInput = Math.IEEEremainder(phi0 - phim0 - (inverted ? Math.PI : 0.), 2 * Math.PI);
                gaps[i].setPhase(phiInput + phim + (splitIntgrs[i].getInverted() ? Math.PI : 0));
            }

            // propagate
            // before gap
            if (pos - cellPos <= l1) {
                double plen = Math.min(l1 - (pos - cellPos), sliceEndPosition - pos);
                boolean doGap = (plen == l1 - (pos - cellPos));
                new IdealRfCavityDrift("", plen, frequency, 0).propagate(probe);
                pos += plen;

                // the gap
                if (doGap) {
                    gaps[i].setETL(splitIntgrs[i].getE0TL() * k0);
                    gaps[i].setCavityCellIndex(i + indCell);
                    gaps[i].setFirstGap(i == 0 && indCell == 0);
                    gaps[i].setE0(splitIntgrs[i].getE0TL() * k0);

                    gaps[i].propagate(probe);
                }

                if (pos >= sliceEndPosition) {
                    break;
                }
            }

            // after gap
            double plen2 = Math.min(l2 - (pos - cellPos - l1), sliceEndPosition - pos);
            new IdealRfCavityDrift("", plen2, frequency, 0).propagate(probe);
            pos += plen2;
            if (pos >= sliceEndPosition) {
                break;
            }
            cellPos += splitIntgrs[i].getLength();
        }
    }

    @Override
    public void setETL(double dblETL) {
        k0 = dblETL;
    }

    @Override
    public void setE0(double E) {
        // We ignore this value. gap amplitude
    }

    @Override
    public void setPhase(double dblPhase) {
        phi0 = dblPhase;
    }

    @Override
    public void setFrequency(double dblFreq) {
        frequency = dblFreq;

    }

    @Override
    public double getETL() {
        return k0;
    }

    @Override
    public double getPhase() {
        return phi0;
    }

    @Override
    public double getFrequency() {
        return frequency;
    }

    @Override
    public double getE0() {
        // We ignore cavity amplitude
        return 1;
    }

    @Override
    public boolean isFirstGap() {
        return indCell == 0;
    }

    @Override
    public void setCavityCellIndex(int indCell) {
        this.indCell = indCell;
    }

    @Override
    public void setCavityModeConstant(double dblCavModeConst) {
        this.dblCavModeConst = dblCavModeConst;
    }

    @Override
    public int getCavityCellIndex() {
        return indCell;
    }

    @Override
    public double getCavityModeConstant() {
        return dblCavModeConst;
    }

    @Override
    public boolean isEndCell() {
        // this is ignored
        return false;
    }

    @Override
    public boolean isFirstCell() {
        return indCell == 0;
    }

    @Override
    public double energyGain(IProbe probe, double dblLen) {
        // not used
        return 0;
    }

    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException {
        // not used
        return null;
    }

    @Override
    public double elapsedTime(IProbe probe, double dblLen) {
        // not used
        return 0;
    }
}
