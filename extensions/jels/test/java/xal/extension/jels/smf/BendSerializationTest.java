package xal.extension.jels.smf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import xal.extension.jels.smf.ESSElementFactory;

import xal.extension.jels.smf.impl.ESSBend;
import xal.model.ModelException;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorNodeFactory;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;
import xal.tools.data.TransientDataAdaptor;

@RunWith(JUnit4.class)
public class BendSerializationTest {
	@Test
	public void doBendSerializationTest() throws InstantiationException, ModelException, ClassNotFoundException {		
		AcceleratorSeq sequence = new AcceleratorSeq("BendTest");
		
		// input from TraceWin
		double entry_angle_deg = -5.5;
		double exit_angle_deg = -5.5;
		double alpha_deg = -11; // angle in degrees
		double rho = 9375.67*1e-3; // absolute curvature radius (in m)
		double N = 0.; // field Index
		 
		double G = 50;
		double entrK1 = 1.23;
		double entrK2 = 4.56;
		double exitK1 = 7.89;
		double exitK2 = 9.01;
		
		// calculations
		double alpha = alpha_deg * Math.PI/180.0;		
		double len = Math.abs(rho*alpha);
		double quadComp = N / (rho*rho);
		double B0 = 10;
		double k = B0 / rho * Math.signum(alpha);
		
	    for (int HV = 0; HV <= 1; HV++) {
			ESSBend bend = ESSElementFactory.createESSBend("b", alpha_deg, k, rho, entry_angle_deg,
					exit_angle_deg, entrK1, entrK2, exitK1, exitK2, quadComp, new ApertureBucket(), null, HV, G, len/2);
			
			sequence.addNode(bend);
			sequence.setLength(len);
					
			TransientDataAdaptor da = new TransientDataAdaptor("test");
			bend.write(da);
			
			AcceleratorNodeFactory factory = new AcceleratorNodeFactory();
			factory.registerNodeClass("DV", null, ESSBend.class);
			factory.registerNodeClass("DH", null, ESSBend.class);
			AcceleratorNode node = factory.createNode(da);
			node.update(da);
			
			assertTrue(node instanceof ESSBend);
			
			ESSBend bend2 = (ESSBend)node;
			assertEquals(bend.getOrientation(), bend2.getOrientation());
			assertEquals(bend.getGap(), bend2.getGap(), 1e-12);
			assertEquals(bend.getEntrK1(), bend2.getEntrK1(), 1e-12);
			assertEquals(bend.getEntrK2(), bend2.getEntrK2(), 1e-12);
			assertEquals(bend.getExitK1(), bend2.getExitK1(), 1e-12);
			assertEquals(bend.getExitK2(), bend2.getExitK2(), 1e-12);
	    }
		
	}
}
