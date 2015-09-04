package eu.ess.bled.devices.lattice;

import eu.ess.bled.BLEDEntity;
import eu.ess.bled.Subsystem;

/**
 * <code>LatticeCommands</code> represents the elements from the commands table,
 * which stores all the commands in the system. The command is an entity which
 * has a position in and relation to the lattice, however, the command itself is
 * not part of the lattice, because practically it is not an element - it is
 * just a convenience for the calculation of the lattice. Nevertheless, in BLED
 * it is still represented as a {@link eu.ess.bled.Subsystem}, so it can be
 * ordered into the lattice hierarchy, but it is always virtual.
 * <p>
 * Each {@link LatticeCommand} is identified by its name and location. The
 * location is given through the position in the lattice via the
 * {@link eu.ess.bled.Subsystem} properties.
 * </p>
 * <p>
 * Due to vast number of different commands that need to be supported and
 * because the commands only play the role during the design of the lattice, the
 * command parameters are not parsed and digested. Instead the commands are only
 * stored for later export of the lattice. Therefore, the command is stored as a
 * String in a single field called {@link #value} as it was given when imported.
 * </p>
 * 
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */

public class LatticeCommand extends Subsystem implements BLEDEntity {

	private static final long serialVersionUID = -7952962015154581512L;
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public Boolean isVirtual() {
		return Boolean.TRUE;
	}

}
