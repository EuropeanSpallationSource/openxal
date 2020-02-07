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
package xal.extension.jels.model.elem;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMap;
import xal.extension.jels.smf.impl.RfqDummy;
import xal.model.alg.EnvelopeTracker;
import xal.model.elem.ThickElement;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class RfqDummyModel extends ThickElement {

    public static final String STR_TYPEID = "RFQ";

    private CovarianceMatrix covMat = CovarianceMatrix.newIdentity();
    private double kinEnergy;

    public RfqDummyModel() {
        super(STR_TYPEID);
    }

    public RfqDummyModel(String strType) {
        super(STR_TYPEID, strType);
    }

    @Override
    public double elapsedTime(IProbe probe, double dblLen) {
        return 0.0;
    }

    @Override
    public double energyGain(IProbe probe, double dblLen) {
        return 0.0;
    }

    @Override
    public void initializeFrom(LatticeElement latticeElement) {
        super.initializeFrom(latticeElement);

        RfqDummy smfNode = (RfqDummy) latticeElement.getHardwareNode();

        covMat.setElem(0, 0, smfNode.getC11());
        covMat.setElem(0, 1, smfNode.getC12());
        covMat.setElem(1, 0, smfNode.getC12());
        covMat.setElem(1, 1, smfNode.getC22());

        covMat.setElem(2, 2, smfNode.getC33());
        covMat.setElem(2, 3, smfNode.getC34());
        covMat.setElem(3, 2, smfNode.getC34());
        covMat.setElem(3, 3, smfNode.getC44());

        covMat.setElem(4, 4, smfNode.getC55());
        covMat.setElem(4, 5, smfNode.getC56());
        covMat.setElem(5, 4, smfNode.getC56());
        covMat.setElem(5, 5, smfNode.getC66());

        kinEnergy = smfNode.getEnergy();
    }

    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException {
        EnvelopeProbe envProbe = ((EnvelopeProbe) probe);
        // Updating probe state
        CovarianceMatrix covMatCurrent = envProbe.getCovariance();
        covMatCurrent.setMatrix(covMat.toString());

        envProbe.setKineticEnergy(kinEnergy);

        // TODO: implement this in a separate element that converts unbunched beam into bunched
        ((EnvelopeTracker) probe.getAlgorithm()).setUseDCBeam(false);

        return PhaseMap.identity();
    }

}
