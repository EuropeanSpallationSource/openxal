/*
 * Created on Oct 21, 2003
 */
package xal.sim.sync;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.model.IComponent;
import xal.model.elem.IdealPermMagQuad;
import xal.model.elem.sync.IElectromagnet;
import xal.model.elem.sync.IRfCavity;
import xal.sim.scenario.ModelInput;
import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorNode;
import xal.smf.proxy.PrimaryPropertyAccessor;

/**
 * Manages synchronization mappings between accelerator node proxies and
 * lattice elements.
 *
 * @author Craig McChesney
 */
public class SynchronizationManager {

	public static final String DEFAULT_SYNC_MODE = Scenario.SYNC_MODE_DESIGN;
	// key = accelerator node concrete class, value = access instance


	// Class Variables =========================================================

	private static Map<Class<?>,Synchronizer> nodeSynchronizerMap = new HashMap<Class<?>,Synchronizer>();
	// key = element class, value = synchronization factory class

	private static List<String> syncModes = new ArrayList<String>();


	// Static Initialization ===================================================

	static {
		// Synchronizer registration
		registerSynchronizer(IElectromagnet.class, new ElectromagnetSynchronizer());
		registerSynchronizer(IRfCavity.class, new RfCavitySynchronizer());
		registerSynchronizer(IdealPermMagQuad.class, new PermanentMagnetSynchronizer());

		// Synch mode registration
		SynchronizationManager.syncModes.add(Scenario.SYNC_MODE_LIVE);
		SynchronizationManager.syncModes.add(Scenario.SYNC_MODE_DESIGN);
		SynchronizationManager.syncModes.add(Scenario.SYNC_MODE_RF_DESIGN);
	}


	// Static Accessors ========================================================

	public static List<String> syncModes() {
		return syncModes;
	}


	// Instance Variables ======================================================

	private Map<AcceleratorNode,List<IComponent>> synchronizedNodeComps = new HashMap<>();
	// key = accelerator node, value = list of components sync'ed to that node

        private Map<AcceleratorNode,List<IComponent>> allNodeComps = new HashMap<>();
	// a map of all accelerator nodes and a list of the components corresponding
	// to them, whether synchronized or not

	private PrimaryPropertyAccessor propertyAccessor;

	private String syncMode = SynchronizationManager.DEFAULT_SYNC_MODE;


	// Public State ============================================================

	/** Set the synchronization mode */
	public void setSynchronizationMode( final String newMode ) {
		if ( !SynchronizationManager.syncModes.contains( newMode ) )  throw new IllegalArgumentException( "unknown synchronization mode: " + newMode );
		syncMode = newMode;
	}


	/** Get the synchronization mode */
	public String getSynchronizationMode() {
		return syncMode;
	}


	// Constructors ============================================================

	public SynchronizationManager() {
		propertyAccessor = new PrimaryPropertyAccessor();
	}


	// Public Synchronization operations =======================================

	public void resync() throws SynchronizationException {
		final Collection<AcceleratorNode> nodes = synchronizedNodeComps.keySet();
		propertyAccessor.requestValuesForNodes( nodes, syncMode );

		for ( final AcceleratorNode node : nodes ) {
			final Map<String,Double> valueMap = propertyAccessor.valueMapFor( node );

			for ( final IComponent component : synchronizedNodeComps.get( node ) ) {
				resync(component, valueMap);
			}
		}
	}


	/** use the cached values modified by the model inputs and resync the model */
	public void resyncFromCache() throws SynchronizationException {
		for ( final AcceleratorNode node : synchronizedNodeComps.keySet() ) {
			final Map<String,Double> valueMap = propertyAccessor.getWhatifValueMapFromCache( node );
			for ( final IComponent component : synchronizedNodeComps.get( node ) ) {
				resync(component, valueMap );
			}
		}
	}


	/**
	 * Synchronizes anElem to the property values contained in valueMap.
	 * @param aComp element to synchronize
	 * @param valueMap a Map whose keys are property names and values are String property values
	 */
	public static void resync( final IComponent aComp, final Map<String,Double> valueMap ) throws SynchronizationException {
		Synchronizer synchronizer = getSynchronizer(aComp );
		synchronizer.resync(aComp, valueMap );
	}

	/**
	 * Creates a synchronization between the specified element and accelerator
	 * node.  Request is ignored if there is no synchronizer for the specified
	 * element type.  Request is also ignored if there is no accessor for the
	 * specified node type, because the system doesn't know how to access data
	 * from that type of node.
	 *
	 * @param aComp the lattice component to create synchronization for
	 * @param aNode the accelerator node to synchronize the element with
	 */
	public void synchronize(IComponent aComp, AcceleratorNode aNode) {
		if ((hasSynchronizerFor(aComp)) && (propertyAccessor.hasAccessorFor(aNode)))
			addSynchronizedComponentMappedTo(aComp, aNode);
		addComponentMappedTo(aComp, aNode);
	}


// Queries =================================================================

	public Map<String,Double> propertiesForNode( final AcceleratorNode aNode ) {
		propertyAccessor.requestValuesForNodes( Collections.singleton( aNode ), syncMode );
		return propertyAccessor.valueMapFor( aNode );
	}

	// Node - Element Mapping ==================================================

	private void addComponentMappedTo(IComponent aComp, AcceleratorNode aNode) {
		List<IComponent> components = allComponentsMappedTo(aNode);
		if (components == null) {
			components = new ArrayList<IComponent>();
			allNodeComps.put(aNode, components);
		}
		components.add(aComp);
	}

	public List<IComponent> allComponentsMappedTo(AcceleratorNode aNode) {
		return allNodeComps.get(aNode);
	}

	private void addSynchronizedComponentMappedTo(IComponent aComp, AcceleratorNode aNode) {
		List<IComponent> components = synchronizedComponentsMappedTo(aNode);
		if (components == null) {
			components = new ArrayList<IComponent>();
			synchronizedNodeComps.put(aNode, components);
		}
		components.add(aComp);
	}

	protected List<IComponent> synchronizedComponentsMappedTo(AcceleratorNode aNode) {
		return synchronizedNodeComps.get(aNode);
	}


	// Private Synchronizer Support ============================================

	private static boolean hasSynchronizerFor(IComponent aComp) {
		return getSynchronizer(aComp) != null;
	}

	private static Synchronizer getSynchronizer(IComponent aComp) {
		for ( final Class<?> cl : nodeSynchronizerMap.keySet() ) {
			if ( cl.isInstance(aComp ) ) {
				return nodeSynchronizerMap.get( cl );
			}
		}
		return null;
	}

	private static void registerSynchronizer( final Class<?> nodeClass, final Synchronizer aSync ) {
		nodeSynchronizerMap.put( nodeClass, aSync );
	}


	// ModelInput Management ===================================================

	/**
	 * Sets the specified node's property to the specified value.  Replaces the
	 * existing value if there is one.
	 *
	 * @param aNode node whose property to set
	 * @param property name of property to set
	 * @param value double value for property
	 */
	public ModelInput setModelInput(AcceleratorNode aNode, String property, double value) {
		return propertyAccessor.setModelInput(aNode, property, value);
	}

	/**
	 * Returns the ModelInput for the specified node's property.
	 *
	 * @param aNode node whose property to get a ModelInput for
	 * @param propName name of property to get a ModelInput for
	 */
	public ModelInput getModelInput(AcceleratorNode aNode, String propName) {
		return propertyAccessor.getInput(aNode, propName);
	}

	public void removeModelInput(AcceleratorNode aNode, String property) {
		propertyAccessor.removeInput(aNode, property);
	}


	// Testing and Debugging ===================================================

	protected void debugPrint() {
		Logger.getLogger(getClass().getName()).log(Level.INFO, "Full Node - Element Map:");
		for ( final AcceleratorNode node : allNodeComps.keySet() ) {
			Logger.getLogger(getClass().getName()).log(Level.INFO, "\t{0}", node.getId());
			for ( final IComponent component : allNodeComps.get( node ) ) {
				Logger.getLogger(getClass().getName()).log(Level.INFO, "\t\t{0}", component);
			}
		}

		Logger.getLogger(getClass().getName()).log(Level.INFO, "Synchronized Node - Element Map:");
		for ( final AcceleratorNode node : synchronizedNodeComps.keySet() ) {
			Logger.getLogger(getClass().getName()).log(Level.INFO, "\t{0}", node.getId());
			for ( final IComponent component : synchronizedNodeComps.get( node ) ) {
				Logger.getLogger(getClass().getName()).log(Level.INFO, "\t\t{0}", component);
			}
		}
	}


	public boolean checkSynchronization( final AcceleratorNode aNode, final Map<String,Double> values ) throws SynchronizationException {
		final List<IComponent> components = synchronizedComponentsMappedTo( aNode );
		if (components == null) return true;
		for ( final IComponent component : components ) {
			getSynchronizer(component ).checkSynchronization(component, values );
		}
		return true;
	}

}
