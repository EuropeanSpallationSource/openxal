package eu.ess.bled;

import java.io.Serializable;

/**
 * 
 * <code>BLEDEntity</code> this interface implies that the implementor is an entity
 * of BLED and can be stored into the database. The interface itself does not 
 * require any methods to be implemented, but servers only as an identifier.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public interface BLEDEntity extends Serializable {
}
