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

import xal.model.IComponent;
import xal.model.IComposite;
import xal.model.ModelException;
import xal.model.Sector;
import xal.model.elem.IdealDrift;
import xal.model.elem.IdealMagQuad;
import xal.model.elem.IdealMagSteeringDipole;
import xal.model.elem.IdealRfCavity;
import xal.model.elem.IdealRfCavityDrift;
import xal.model.elem.Marker;
import xal.sim.scenario.ElementMapping;

/**
 * The default element mapping implemented as singleton.
 *
 * @author Ivo List
 *
 */
public class JElsElementMapping extends ElementMapping {

    protected static ElementMapping instance;

    public JElsElementMapping() {
        initialize();
        bolSubsectionCtrOrigin = false;
    }

    /**
     * Returns the default element mapping.
     *
     * @return the default element mapping
     */
    public static ElementMapping getInstance() {
        if (instance == null) {
            instance = new JElsElementMapping();
        }
        return instance;
    }

    @Override
    public Class<? extends IComponent> getDefaultElementType() {
        return Marker.class;
    }

    @Override
    public IComponent createDefaultDrift(String name, double len) {
        return new IdealDrift(name, len);
    }

    @Override
    public IComponent createRfCavityDrift(String name, double len, double freq, double mode) throws ModelException {
        return new IdealRfCavityDrift(name, len, freq, mode);
    }

    protected void initialize() {
        putMap("sfm", SolFieldMap.class);
        putMap("mfm", MagFieldMap3D.class);
        putMap("fm", FieldMap.class);
        putMap("dh", IdealMagWedgeDipole2.class);
        putMap("q", IdealMagQuad.class);
        putMap("qt", IdealMagQuad.class);
        putMap("pq", IdealMagQuad.class);
        putMap("rfgap", IdealRfGap.class);
        putMap("dch", IdealMagSteeringDipole.class);
        putMap("dcv", IdealMagSteeringDipole.class);
        putMap("marker", Marker.class);
        putMap("rf", IdealRfCavity.class);
    }

    @Override
    public Class<? extends IComposite> getDefaultSequenceType() {
        return Sector.class;
    }
}
