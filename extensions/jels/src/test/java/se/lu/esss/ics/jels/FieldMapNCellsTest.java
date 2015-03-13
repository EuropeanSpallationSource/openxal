package se.lu.esss.ics.jels;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import se.lu.esss.ics.jels.model.elem.jels.FieldMap;
import se.lu.esss.ics.jels.model.elem.jels.FieldMapNCells;
import se.lu.esss.ics.jels.model.elem.jels.JElsElementMapping;
import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;

public class FieldMapNCellsTest {
	
	@Test
	public void fieldMapNCellsTest() throws ModelException, IOException {
		AcceleratorSeq acc = XMLDataManager.acceleratorWithUrlSpec(JElsDemo.class.getResource("lattice1/main.xal").toString());
		
		for (int i = 0; i<2; i++) {
			Probe probe = GeneralTest.loadProbeFromXML(JElsDemo.class.getResource("lattice1/probe."+i+".xml").toString());
			probe.initialize();
			
			acc.getAccelerator().setElementMapping(new JElsElementMapping() {
				@Override
				protected void initialize()
				{
					putMap("fm", FieldMap.class);
					super.initialize();
				}
			});
		
			double dataOXFM[][] = GeneralTest.run(probe, acc);
			probe.reset();
			
			acc.getAccelerator().setElementMapping(new JElsElementMapping() {
				@Override
				protected void initialize()
				{
					putMap("fm", FieldMapNCells.class);
					super.initialize();
				}
			});	
			double dataOXNC[][] = GeneralTest.run(probe, acc);
			probe.reset();
			
			System.out.printf("%s\t", probe.getComment());
			StringBuilder message = new StringBuilder();
			boolean ok = true;
			for (int j = 1; j < dataOXFM.length; j++) {
				double e = GeneralTest.compare(dataOXFM[0], dataOXNC[0], dataOXFM[j], dataOXNC[j]);
				//System.out.printf("%s: %E %c %E\n",allCols[j].name(), e, e < allCols[j].allowedError ? '<' : '>', allCols[j].allowedError);
				System.out.printf("%E\t", e);
				if (e >= 0.05) {
					message.append(j).append(" ");
					ok = false;
				}
				//System.out.printf("%E %E\n",dataOX[allCols[j].openxal][0], dataTW[allCols[j].tracewin][0]);
			}
			System.out.println();
			assertTrue(message.append("are not within the allowed error").toString(), ok);
		}
	}
}
