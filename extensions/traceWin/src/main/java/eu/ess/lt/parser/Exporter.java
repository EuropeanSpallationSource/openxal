package eu.ess.lt.parser;

import java.util.Collection;

import eu.ess.bled.Subsystem;
import xal.smf.Accelerator;

/**
 * Interface defining an output generating object, that can read form BLED and
 * format the data. Output is an export of all subsystem hierarchy from the specified parent
 * system down.
 * 
 * @author <a href="mailto:jakob.battelino@cosylab.com">Jakob Battelino
 *         Prelog</a>
 * @author Blaz Kranjc
 */
public interface Exporter {
	
	/**
	 * /**
	 * Initiates export of BLED data for the specified system. All subsystems
	 * hierarchically tied to the the subsystem with the specified
	 * <code>systemID</code> as children will be exported.
	 * 
	 * @param parentSystem
	 *            id of the subsystem for which the data will be exported.
	 * @param subsystems all systems in BLED.
	 *            
	 * @return accelerator generated from BLED.
	 * 
	 * 
	 */
	public Accelerator exportToOpenxal(Subsystem parentSystem,Collection<Subsystem> subsystems);
}