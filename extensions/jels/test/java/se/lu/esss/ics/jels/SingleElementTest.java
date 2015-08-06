package se.lu.esss.ics.jels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.Probe;
import xal.sim.scenario.ElementMapping;
import xal.smf.AcceleratorSeq;

public abstract class SingleElementTest extends TestCommon {
	protected SingleElementTestData data;
	
	public static class SingleElementTestData {
		String description;
		
		// input
		Probe probe;
		ElementMapping elementMapping;
		AcceleratorSeq sequence;
		
		// TW output
		double[][] TWTransferMatrix, TWCorrelationMatrix;
		double[] TWMean;
		double TWGamma;
		
		// ELS output
		double elsPosition;
		double[] elsSigma, elsBeta;
		
		double errTolerance = 1e-6;
		double ELSerrTolerance = errTolerance;
		double TMerrTolerance = errTolerance;
		double CMerrTolerance = errTolerance;
		
		
		@Override
		public String toString()
		{
			double I = ((EnvelopeProbe)probe).getBeamCurrent();
			String params = I == 0. ? String.format(Locale.ROOT, "E=%.1E", probe.getKineticEnergy())
					: String.format(Locale.ROOT, "E=%.1E, I=%.1E", probe.getKineticEnergy(), ((EnvelopeProbe)probe).getBeamCurrent());;
			if (description != null) return description+ ", " + params;
			return params;	
		}
	}
	
	public SingleElementTest(SingleElementTestData data) {
		super(data.probe, data.elementMapping);
		this.data = data;
	}

	@Test
	public void test() throws ModelException
	{
		run(data.sequence);
		
		System.out.printf("\nResults of %s:\n", data.toString());
		
		//printResults();
		if (data.elsSigma != null)
			checkELSResults(data.elsPosition, data.elsSigma, data.elsBeta, data.ELSerrTolerance);
		
		checkTWTransferMatrix(data.TWTransferMatrix, data.TMerrTolerance);
			
		if (data.TWMean == null)
			checkTWResults( data.TWGamma, data.TWCorrelationMatrix, data.CMerrTolerance);
		else
			checkTWResults( data.TWGamma, data.TWCorrelationMatrix, data.TWMean, data.CMerrTolerance);
	}
	
	@Parameters
	public static Collection<Object[]> probes() {
		return new ArrayList<Object[]>(0);
	}
}
