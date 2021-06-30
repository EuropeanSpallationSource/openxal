/*
 * ScenarioGenerator2.java
 * 
 * Created on Oct 3, 2013
 */
package xal.sim.scenario;

import xal.model.Lattice;
import xal.model.ModelException;
import xal.sim.sync.SynchronizationManager;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;

/**
 * <p>
 * Generates an on-line model scenario from XAL accelerator smfSequence. Defers
 * model construction to <code>{@link LatticeElement}</code> class. The  <code>{@link SynchronizationManager}</<code> class is also created here and attached
 * to the scenario.
 * <p>
 * </p>
 * It is not necessary for the <code>Scenario</code> object to maintain a back
 * reference to the original <code>AcceleratorSeq</code> object that it models.
 * In fact it is undesirable since this represents a dependency of the online
 * model with the SMF hardware representation component of Open XAL.
 * <p>
 * <p>
 * In fact there is no need for this class to carry a reference to the
 * accelerator sequence object. It actually limits the usefulness of the class
 * since any class object is then dedicated to creating model scenarios for only
 * on accelerator sequence. By converter this class to creating model scenarios
 * on depend, this limitation is removed.
 * </p>
 *
 * @author Ivo List
 * @author Christopher K. Allen
 * @since Oct 3, 2013
 * @version Dec 5, 2014
 */
class ScenarioGenerator {

    /*
     * Global Constants
     */
    /**
     * Small number - usually the minimum drift length
     */
    public static final double EPS = 1.e-10d;

    /*
	 * Local Attributes
     */
    // External Objects
    /**
     * The associative mapping being hardware nodes and modeling elements
     */
    private ElementMapping mapNodeToModCls;

    /**
     * Creates an empty Lattice, uses given ElementMapping.
     *
     * @param aSequence accelerator smfSequence to create scenario for
     * @param mapNodeToModCls element mapping
     */
    public ScenarioGenerator(final ElementMapping elementMapping) {
        this.mapNodeToModCls = elementMapping;
    }

    /*
	 * Operations
     */
    /**
     * CKA
     * <p>
     * Generates a Scenario from the given AcceleratorSeq supplied in the
     * constructor using supplied ElementMapping.
     * </p>
     * <p>
     * This method will create a hierarchical model with the analogous structure
     * of the hardware object being provided. That is, the model returns is not
     * a "flattened" version of the provided hardware representation. For
     * example, if the hardware sequence contains nested sequences such as an <code>
     * AcceleratorSeqCombo</code> object would, then the returned model has
     * nested <code>ElementSeq</code> objects representing them. This is
     * convenient, and necessary, for correct treatment of RF cavities.
     * </p>
     *
     * @param smfSeq the hardware sequence to be modeled.
     *
     * @return new model Scenario for the supplied accelerator sequence
     *
     * @throws ModelException if there is an error building the Scenario
     *
     * @since Dec 5, 2014 @author Christopher K. Allen
     */
    public Scenario generateScenario(AcceleratorSeq smfSeq) throws ModelException {

        // Create a synchronization manager for the model
        SynchronizationManager mgrSync = new SynchronizationManager();

        // Create a model lattice generator object for the given accelerator hardware sequence,
        //  set any generation parameters, then create the model lattice
        LatticeSequence latSeq;
        if (smfSeq instanceof AcceleratorSeqCombo) {
            AcceleratorSeqCombo smfSeqCombo = (AcceleratorSeqCombo) smfSeq;

            latSeq = new LatticeSequenceCombo(smfSeqCombo, this.mapNodeToModCls);

        } else {

            latSeq = new LatticeSequence(smfSeq, this.mapNodeToModCls);
        }

        Lattice mdlLat = latSeq.createModelLattice(mgrSync);

        // Create the model scenario object from the accelerator sequence, 
        //  model lattice, and synchronization manager 
        Scenario mdlScenario = new Scenario(smfSeq, mdlLat, mgrSync);

        return mdlScenario;
    }
}
