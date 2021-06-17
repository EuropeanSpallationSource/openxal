/*
 * Copyright (C) 2021 European Spallation Source ERIC.
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
package xal.extension.jels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static xal.extension.jels.RfFieldMapTest.hbFieldMap;
import static xal.extension.jels.RfFieldMapTest.spokeFieldMap;
import xal.extension.jels.model.elem.JElsElementMapping;
import xal.extension.jels.model.elem.ThinRfFieldMap;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
@RunWith(Parameterized.class)
public class SynchronousPhaseTest extends SynchronousTrackerTest {

    public SynchronousPhaseTest(SynchronousTrackerTestData data) {
        super(data.probe, data.elementMapping);
        this.data = data;
    }

    @Parameters(name = "FieldMap {index}: {0}")
    public static Collection<Object[]> tests() {
        final double frequency = 352.21e6;

        List<Object[]> tests = new ArrayList<>();

        // 0: spoke thick rf fieldmap
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
        tests.add(new Object[]{new SynchronousTrackerTestData() {
            {
                description = "Spoke ThickRfFieldMap";
                probe = setupOpenXALProbe(89.88782e6, frequency);

                elementMapping = JElsElementMapping.getInstance();
                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293);

                // TW results
                TWSyncPhase = -40.801559; //deg
                TWEnergyGain = 3.61514; // MeV/m

                errTolerance = 3e-4;
            }
        }});

        // 1: spoke thin rf fieldmap
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
        tests.add(new Object[]{new SynchronousTrackerTestData() {
            {
                description = "Spoke ThinRfFieldMap";
                probe = setupOpenXALProbe(89.88782e6, frequency);
                elementMapping = new JElsElementMapping() {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        removeMap("rfm");
                        putMap("rfm", ThinRfFieldMap.class);
                    }
                };
                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293);

                // TW results
                TWSyncPhase = -40.801559; //deg
                TWEnergyGain = 3.61514; // MeV/m

                errTolerance = 3e-4;
            }
        }});

        // 2: hb thick rf fieldmap
        // FIELD_MAP 100 1500 66.7114 80 0 0.99941 0 1 HB_W_coupler
        tests.add(new Object[]{new SynchronousTrackerTestData() {
            {
                description = "HB ThickRfFieldMap";
                probe = setupOpenXALProbe(570.96562e6, frequency);

                elementMapping = JElsElementMapping.getInstance();
                sequence = hbFieldMap(1.5, 2 * frequency * 1e-6, 0.99941, 66.7114);

                // TW results
                TWSyncPhase = -14.165023; //deg
                TWEnergyGain = 8.95195; // MeV/m

                errTolerance = 1e-6;
            }
        }});

        // 3: hb thin rf fieldmap
        // FIELD_MAP 100 1500 66.7114 80 0 0.99941 0 1 HB_W_coupler
        tests.add(new Object[]{new SynchronousTrackerTestData() {
            {
                description = "HB ThinRfFieldMap";
                probe = setupOpenXALProbe(570.96562e6, frequency);
                elementMapping = new JElsElementMapping() {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        removeMap("rfm");
                        putMap("rfm", ThinRfFieldMap.class);
                    }
                };
                sequence = hbFieldMap(1.5, 2 * frequency * 1e-6, 0.99941, 66.7114);

                // TW results
                TWSyncPhase = -14.165023; //deg
                TWEnergyGain = 8.95195; // MeV/m

                errTolerance = 1e-6;
            }
        }});
        return tests;

    }
}
