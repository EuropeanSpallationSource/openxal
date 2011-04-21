/*
 * Created on Oct 21, 2003
 */
package xal.model.scenario;

import xal.model.IComposite;
import xal.model.LineModel;
import xal.model.ModelException;
import xal.model.RingModel;
import xal.model.elem.IElectromagnet;
import xal.model.elem.IdealMagSkewQuad3;
import xal.model.sync.SynchronizationManager;
import xal.model.xml.LatticeXmlParser;

import xal.slg.BCMonitor;
import xal.slg.BLMonitor;
import xal.slg.BPMonitor;
import xal.slg.BSMonitor;
import xal.slg.Dipole;
import xal.slg.Drift;
import xal.slg.EKicker;
import xal.slg.Element;
import xal.slg.HSteerer;
import xal.slg.LatticeError;
import xal.slg.LatticeFactory;
import xal.slg.LatticeIterator;
import xal.slg.Marker;
import xal.slg.Octupole;
import xal.slg.PermMarker;
import xal.slg.Quadrupole;
import xal.slg.RFGap;
import xal.slg.Sextupole;
import xal.slg.SkewQuad;
import xal.slg.SkewSext;
import xal.slg.Solenoid;
import xal.slg.VSteerer;
import xal.slg.Visitor;
import xal.slg.WScanner;

import xal.smf.AcceleratorSeq;
import xal.smf.Ring;
import xal.smf.impl.Bend;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.Magnet;
import xal.smf.impl.RfGap;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates an on-line model scenario from the intermediate slg lattice.
 * 
 * @author Craig McChesney
 */
public class ScenarioGenerator implements Visitor {

	// Instance Variables ======================================================

	private xal.model.Lattice lattice;
	private int driftCount = 0;
	private final IComposite elementContainer;
	private SynchronizationManager syncManager;
	private final AcceleratorSeq sequence;
    
//        private IComposite      m_ifcLattice;
	
	protected int dipoleInd = 0;

	// Constructors ============================================================

	/**
	 * Default constructor, creates an empty Lattice.
	 */
	protected ScenarioGenerator(AcceleratorSeq aSequence) {
            sequence = aSequence;
//            m_ifcLattice = new LineModel(aSequence.getId());
            elementContainer = new LineModel(aSequence.getId());
            lattice = new xal.model.Lattice();
//            lattice.addChild(m_ifcLattice);
            lattice.addChild(elementContainer);
            syncManager = new SynchronizationManager();
	}
    
        protected ScenarioGenerator(Ring smfRing)   {
            sequence = smfRing;
//            m_ifcLattice = new RingModel(smfRing.getId());
            elementContainer = new RingModel(smfRing.getId());
            lattice = new xal.model.Lattice();
//            lattice.addChild(m_ifcLattice);
            lattice.addChild(elementContainer);
            syncManager = new SynchronizationManager();
        }
	
	// Public interface ========================================================

	/**
	 * Generates a Scenario from AcceleratorSeq supplied in the constructor.
	 * 
	 * @return a Secenario for the supplied accelerator sequence
	 * @throws ModelException if there is an error building the Scenario
	 */
	public Scenario generateScenario() throws ModelException {
		
		// make intermediate lattice
		xal.slg.Lattice slgLattice = makeIntermediateLattice();

		// set attributes
		lattice.setId(slgLattice.getName());
		lattice.setVersion( " " );
		lattice.setAuthor("W.-D. Klotz");
		StringBuffer commentBuf = new StringBuffer();
		commentBuf.append(lattice.getAuthor());
		commentBuf.append(LatticeXmlParser.s_strAttrSep);
		commentBuf.append(new Date());
		commentBuf.append(LatticeXmlParser.s_strAttrSep);
		commentBuf.append(
			"document generated from " + xal.slg.Lattice.version());
		lattice.setComments(commentBuf.toString());
//		elementContainer = new Sector(slgLattice.getName());
//		lattice.addChild(elementContainer);
		// the intermediate lattice doesn't specify a date attribute

		// visit each lattice node
		LatticeIterator lit = slgLattice.latticeIterator();
		while (lit.hasNext()) {
			//lit.next().accept(this);
			Element el = lit.next();
			el.accept(this);
			//System.out.println(" accept " + el.getName());
		}
		
		return new Scenario(sequence, lattice, syncManager);
	}

	// Private Supporting Methods ==============================================

	private void initialize() {
		lattice = new xal.model.Lattice();
		syncManager = new SynchronizationManager();
	}
	
	private xal.slg.Lattice makeIntermediateLattice() 
			throws ModelException {
		xal.slg.Lattice intermediateLattice = null;
		LatticeFactory factory = new LatticeFactory();
		factory.setDebug(false);
		factory.setVerbose(false);
		factory.setHalfMag(true);
		try {
			intermediateLattice = factory.getLattice(sequence);
			intermediateLattice.clearMarkers();
			intermediateLattice.joinDrifts();
		} catch (LatticeError e) {
			e.printStackTrace();
			throw new ModelException("LatticeError making intermediate lattice");
		}
		return intermediateLattice;
	}

	// Visitor Interface =======================================================

	private void primVisit(Element e) {
//		System.out.println(
//		"element: " + e.getName() + " position: " + e.getPosition());
	}

	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.BCMonitor)
	 */
	public void visit(BCMonitor e) {
		primVisit(e);
		xal.model.elem.Marker xalMarker =
			new xal.model.elem.Marker(e.getName());
		elementContainer.addChild(xalMarker);
		syncManager.synchronize(xalMarker, e.getAcceleratorNode());
	}

	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.BPMonitor)
	 */
	public void visit(BPMonitor e) {
		primVisit(e);
		xal.model.elem.Marker xalMarker =
			new xal.model.elem.Marker(e.getName());
		elementContainer.addChild(xalMarker);
		syncManager.synchronize(xalMarker, e.getAcceleratorNode());
	}

	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.BLMonitor)
	 */
	public void visit(BLMonitor e) {
		primVisit(e);
		xal.model.elem.Marker xalMarker =
			new xal.model.elem.Marker(e.getName());
		elementContainer.addChild(xalMarker);
		syncManager.synchronize(xalMarker, e.getAcceleratorNode());
	}
	
	
	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.BSMonitor)
	 */
	public void visit( final BSMonitor element ) {
		primVisit( element );
		final xal.model.elem.Marker xalMarker = new xal.model.elem.Marker( element.getName() );
		elementContainer.addChild( xalMarker );
		syncManager.synchronize( xalMarker, element.getAcceleratorNode() );
	}

	
	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.Dipole)
	 * Note: This is for bend dipole.
	 */
	public void visit(Dipole e) {
		primVisit(e);
		
		Bend magnet = (Bend) e.getAcceleratorNode();

//                gov.sns.xal.model.elem.ThickDipole xalDipole = 
//		        new gov.sns.xal.model.elem.ThickDipole();
//                xalDipole.setId(e.getName());
//                xalDipole.setLength(e.getLength());
//                xalDipole.setMagField(magnet.getDesignField());
//                xalDipole.setKQuad(magnet.getQuadComponent());
//                double angle = magnet.getDfltBendAngle()*Math.PI/180. * e.getLength() / magnet.getDfltPathLength();
//                xalDipole.setReferenceBendAngle(angle);

		// Replace ThickDipole object with an IdealMagWedgeDipole2
		//    First retrieve all the physical parameters for a bending dipole
                xal.model.elem.IdealMagWedgeDipole2 xalDipole = 
                    new xal.model.elem.IdealMagWedgeDipole2();

                String strId     = e.getName();
                double len_sect  = e.getLength();
                double fld_mag0  = magnet.getDesignField();
                double len_path0 = magnet.getDfltPathLength();
                double ang_bend0 = magnet.getDfltBendAngle()* Math.PI/180.0;
                double k_quad0   = magnet.getQuadComponent();

                //      Now compute the dependent parameters
                double R_bend0   = len_path0/ang_bend0;
                double fld_ind0  = - k_quad0*R_bend0*R_bend0;

                double ang_bend  = ang_bend0*(len_sect/len_path0); 
                double len_path  = R_bend0*ang_bend;
                
                //      Set the parameters for the new model element
                xalDipole.setId( strId );
                xalDipole.setPhysicalLength( len_sect );
                xalDipole.setDesignPathLength( len_path );
                xalDipole.setMagField( fld_mag0 );
                xalDipole.setFieldIndex( fld_ind0 );
                xalDipole.setDesignBendAngle( ang_bend );

		// New method for setting the pole face angles.
		//   The angles are determined directly according to whether or not the 
		//   element has been split at the midpoint, rather than
		//   by the internal state of the ScenarioGenerator (i.e.,
		//   the dipoleInd attribute).  If so, the angles
		//   are set according to the element's sectional representation
		//   (e.g., upstream, downstream, entire component).
		//   The angles are converted to radians for the online model.
		//
		//    C.K. Allen, 9/2009
		//
                double dblAngEntr = 0.0;
                double dblAngExit = 0.0;

                switch (e.getHarwareSection()) {
		
                case HALF_UP:
                    dblAngEntr = magnet.getEntrRotAngle()*Math.PI/180.0;
                    dblAngExit = 0.0;
                    break;
                    
                case HALF_DN:
                    dblAngEntr = 0.0;
                    dblAngExit = magnet.getExitRotAngle()*Math.PI/180.0;;
                    break;
                    
                case WHOLE:
                    dblAngEntr = magnet.getEntrRotAngle()*Math.PI/180.0;
                    dblAngExit = magnet.getExitRotAngle()*Math.PI/180.0;;

                default:
                    dblAngEntr = 0.0;
                    dblAngExit = 0.0;
		}
                
                xalDipole.setEntrPoleAngle(dblAngEntr);
                xalDipole.setExitPoleAngle(dblAngExit);
                
//                // get dipole entrance and exit angles and convert them from degrees to radians
//		if (dipoleInd == 0) {
//                   xalDipole.setEntranceAngle((magnet).getEntrRotAngle()*Math.PI/180.);
//		   xalDipole.setExitAngle(0.);
//		   dipoleInd = 1;
//		} else {
//		   xalDipole.setEntranceAngle(0.);
//                   xalDipole.setExitAngle((magnet).getExitRotAngle()*Math.PI/180.);
//		   dipoleInd = 0;
//		}

		int orientation = IElectromagnet.ORIENT_NONE;
		if (magnet.isHorizontal()) {
			orientation = IElectromagnet.ORIENT_HOR;
		}
		if (magnet.isVertical()) {
			orientation = IElectromagnet.ORIENT_VER;
		}
		xalDipole.setOrientation(orientation);
		elementContainer.addChild(xalDipole);
		syncManager.synchronize(xalDipole, magnet);
	}

	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.Drift)
	 */
	public void visit(Drift e) {
		primVisit(e);
		// append a drift counter to the drift name for an id
		String id = "DR" + (++driftCount);
		xal.model.elem.IdealDrift xalDrift =
			new xal.model.elem.IdealDrift(id, e.getLength());
		elementContainer.addChild(xalDrift);
		syncManager.synchronize(xalDrift, e.getAcceleratorNode());
	}

	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.HSteerer)
         * @deprecated we use visit(Dipole) instead
	 */
	public void visit(HSteerer e) {
		primVisit(e);
		xal.model.elem.IdealMagSteeringDipole xalDipole =
			new xal.model.elem.IdealMagSteeringDipole();
		xalDipole.setId(e.getName());
		Electromagnet magnet = (Electromagnet) e.getAcceleratorNode();
		xalDipole.setEffLength(magnet.getEffLength());
                xalDipole.setMagField(magnet.getDesignField());

		int orientation = IElectromagnet.ORIENT_NONE;
		if (magnet.isHorizontal()) {
			orientation = IElectromagnet.ORIENT_HOR;
		}
		if (magnet.isVertical()) {
			orientation = IElectromagnet.ORIENT_VER;
		}
		xalDipole.setOrientation(orientation);
		elementContainer.addChild(xalDipole);
		syncManager.synchronize(xalDipole, magnet);
	}

	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.Marker)
	 */
	public void visit(Marker e) {
		primVisit(e);
		xal.model.elem.Marker xalMarker =
			new xal.model.elem.Marker(e.getName());
		elementContainer.addChild(xalMarker);
		syncManager.synchronize(xalMarker, e.getAcceleratorNode());
	}

	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.Octupole)
	 */
	public void visit(Octupole e) {
//		primVisit(e);
	}

	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.PermMarker)
	 */
	public void visit(PermMarker e) {
		primVisit(e);
		xal.model.elem.Marker xalMarker =
			new xal.model.elem.Marker(e.getName());
		elementContainer.addChild(xalMarker);
		syncManager.synchronize(xalMarker, e.getAcceleratorNode());
	}

	/**
	 * <p>
	 * Instantiates  a <b>focusing</b> quadrupole modeling element
	 * and synchronize it with the hardware.
	 * </p>
	 * <p>
	 * CKA NOTES - January, 2008:
	 * <br>I am taking out the exceptional case instantiating a skew
	 * quadrupole; I think this is a dangerous implementation.  
	 * <br>- The attribute <code>IElectromagnet#getOrientation()</code> 
	 * is a property of the object.  Thus, the value
	 * <code>IElectroMagnet.ORIENTATION_NONE</code> is a
	 * property value - not a type specifier. 
	 * <br>- Determining an object's type by its 
	 * property values is risky business (an engineering no-no). 
	 * Better to determine type, by using type.
	 * <br>- A skew quadrupole is completely different
	 * object type, not a property of a quadrupole. 
	 * </p>
	 *  
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.Quadrupole)
	 * @see ScenarioGenerator#visit(SkewQuad)
	 */
	public void visit(Quadrupole e) {
		primVisit(e);
		Magnet magnet = (Magnet) e.getAcceleratorNode();
		
		int orientation = IElectromagnet.ORIENT_NONE;
		if (magnet.isHorizontal())    {
			orientation = IElectromagnet.ORIENT_HOR;
			
		} else if (magnet.isVertical()) {
			orientation = IElectromagnet.ORIENT_VER;
			
		} else    {
		    //  This is an exceptional condition!
		    //    Something went wrong if we made it here
		    //    Let's say so
		    String    strSrc = "gov.sns.xal.model.scenario.ScenarioGenerator#visit(Quadrupole)";
		    String    strMsg = "Encountered an un-oriented focusing 'Quadrupole' object";
		    Logger    logGbl = Logger.getLogger("global");
		    
		    logGbl.log(Level.WARNING, strMsg + " : " + strSrc);
            System.out.println("WARNING!: " + strSrc + " : " + strMsg);
		}
		    

		xal.model.elem.IdealMagQuad xalQuad = new xal.model.elem.IdealMagQuad();
		xalQuad.setId(e.getName());
		xalQuad.setLength(e.getLength());
		
		// need to initialize this because PermanentMagnets aren't synchronized
		xalQuad.setMagField(magnet.getDesignField());
		//      xalQuad.setEffLength(magnet.getEffLength() * e.getLength() / magnet.getLength());
		xalQuad.setOrientation(orientation);
		elementContainer.addChild(xalQuad);

		syncManager.synchronize(xalQuad, magnet);

		// for skew quads
		//
        
        // No, not for skew quads
        //  We are not doing this here
        //  See visit(SkewQuad)
        
//		if (orientation == IElectromagnet.ORIENT_NONE) {
//			gov.sns.xal.model.elem.IdealMagSkewQuad xalQuad = 
//				new gov.sns.xal.model.elem.IdealMagSkewQuad(e.getName(), orientation, 
//						magnet.getDesignField(), e.getLength(), 45.);
//			elementContainer.addChild(xalQuad);
//			
//			syncManager.synchronize(xalQuad, magnet);
//		}
//		else {
//			gov.sns.xal.model.elem.IdealMagQuad xalQuad =
//				new gov.sns.xal.model.elem.IdealMagQuad();
//			xalQuad.setId(e.getName());
//			xalQuad.setLength(e.getLength());
//			// need to initialize this because PermanentMagnets aren't synchronized
//			xalQuad.setMagField(magnet.getDesignField());
//			//		xalQuad.setEffLength(magnet.getEffLength() * e.getLength() / magnet.getLength());
//			xalQuad.setOrientation(orientation);
//			elementContainer.addChild(xalQuad);
//			
//			syncManager.synchronize(xalQuad, magnet);
//		}
//		
	}

	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.Solenoid)
	 */
	public void visit(Solenoid e) {
		primVisit(e);
		xal.model.elem.IdealMagSolenoid xalSol =
			new xal.model.elem.IdealMagSolenoid();
		xalSol.setId(e.getName());
		Magnet magnet = (Magnet) e.getAcceleratorNode();
		xalSol.setLength(e.getLength());
		// need to initialize this because PermanentMagnets aren't synchronized
		xalSol.setMagField(magnet.getDesignField());
		//		xalQuad.setEffLength(magnet.getEffLength() * e.getLength() / magnet.getLength());
/*		int orientation = IElectromagnet.ORIENT_NONE;
		if (magnet.isHorizontal()) {
			orientation = IElectromagnet.ORIENT_HOR;
		}
		if (magnet.isVertical()) {
			orientation = IElectromagnet.ORIENT_VER;
		}
		xalSol.setOrientation(orientation);
*/		elementContainer.addChild(xalSol);
		
		syncManager.synchronize(xalSol, magnet);
	}

	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.RFGap)
	 */
	public void visit(RFGap e) {
		primVisit(e);
		xal.model.elem.IdealRfGap xalRfGap =
			new xal.model.elem.IdealRfGap();
		xalRfGap.setId(e.getName());
		RfGap rfgap = (RfGap) e.getAcceleratorNode();
		elementContainer.addChild(xalRfGap);
		try {
			xalRfGap.initializeFrom(rfgap);
		} catch (ModelException excpt) {}
		
		syncManager.synchronize(xalRfGap, rfgap);
	}

	
	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.Sextupole)
	 * At this time, sextupoles are treated as drifts
	 */
	public void visit( final Sextupole element ) {		
		primVisit( element );
		final Magnet magnet = (Magnet) element.getAcceleratorNode();
		//int orientation = magnet.isHorizontal() ? IElectromagnet.ORIENT_HOR : magnet.isVertical() ? IElectromagnet.ORIENT_VER : IElectromagnet.ORIENT_NONE;		
		
		xal.model.elem.IdealMagSextupole xalSextupole = new xal.model.elem.IdealMagSextupole( element.getName(), element.getLength() );
		// need to initialize this because Permanent Magnets aren't synchronized
		// xalSextupole.setMagField( magnet.getDesignField() );
		// xalSextupole.setOrientation( orientation );
		elementContainer.addChild( xalSextupole );
		
		syncManager.synchronize( xalSextupole, magnet );
	}

	
	/**
	 * <p>
	 * Instantiate a skew quadrupole modeling element and 
	 * synchronize it with the hardware.
	 * </p>
	 * <p>
	 * CKA NOTES:
	 * <br>- I think we need a better design for 
	 * <code>IElectromagnet</code>.  For example,
	 * I don't think <code>getOrientation()</code> makes
	 * sense, since this value is not dynamic.
	 * <br>- I am connecting this with the 
	 * <code>IdealMagSkewQuad3</code> modeling element
	 * since <code>IdealMagSkewQuad</code> will not
	 * treat space charge correctly and 
	 * <code>IdealMagSkewQuad2</code> produces unaesthetic simulation 
	 * data (the output with the magnet is w.r.t. natural
	 * skew quadrupole coordinate system).
	 * 
	 * <br>- On the SMF side, <code>Magnet</code> has parameters
	 * that do not make sense for all its children 
	 * (i.e., the hierarchy is questionable).  For example,
	 * <code>isHorizontal()</code> and <code>isVertical</code>
	 * methods do not make sense for a skew quadrupole.
	 * </p>
	 * 
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.SkewQuad)
	 * @see gov.sns.xal.model.elem.IdealMagSkewQuad2
	 * @see gov.sns.xal.model.elem.IdealMagSkewQuad3
	 */
	public void visit(SkewQuad e) {
	    primVisit(e);
	    Magnet magnet = (Magnet) e.getAcceleratorNode();

	    // TODO
	    //  CKA: Is there anything we should do about this?
	    //	    if (magnet.isHorizontal())    {
	    //	    } else if (magnet.isVertical()) {
	    //	    } else    {
	    //	    }

	    IdealMagSkewQuad3 xalSkwQuad = new xal.model.elem.IdealMagSkewQuad3(
	            e.getName(), 
	            magnet.getDesignField(), 
	            e.getLength() 
	    );

	    elementContainer.addChild(xalSkwQuad);
	    syncManager.synchronize(xalSkwQuad, magnet);
	}

	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.SkewSext)
	 */
	public void visit(SkewSext e) {
		primVisit(e);
	}

	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.VSteerer)
         * @deprecated we use visit(Dipole) instead
	 */
	public void visit(VSteerer e) {
		primVisit(e);
		xal.model.elem.IdealMagSteeringDipole xalDipole =
			new xal.model.elem.IdealMagSteeringDipole();
		xalDipole.setId(e.getName());
		Electromagnet magnet = (Electromagnet) e.getAcceleratorNode();
		xalDipole.setEffLength(magnet.getEffLength());
                xalDipole.setMagField(magnet.getDesignField());

                int orientation = IElectromagnet.ORIENT_NONE;
		if (magnet.isHorizontal()) {
			orientation = IElectromagnet.ORIENT_HOR;
		}
		if (magnet.isVertical()) {
			orientation = IElectromagnet.ORIENT_VER;
		}
		xalDipole.setOrientation(orientation);
		elementContainer.addChild(xalDipole);
		syncManager.synchronize(xalDipole, magnet);
	}
	
	
	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.EKicker)
	 * @deprecated we use visit(Dipole) instead
	 */
	public void visit( final EKicker element ) {
		primVisit( element );
		final xal.model.elem.IdealMagSteeringDipole xalDipole = new xal.model.elem.IdealMagSteeringDipole();
		xalDipole.setId( element.getName() );
		final Electromagnet magnet = (Electromagnet)element.getAcceleratorNode();
		xalDipole.setEffLength( magnet.getEffLength() );
		xalDipole.setMagField( magnet.getDesignField() );
		
		int orientation = IElectromagnet.ORIENT_NONE;
		if ( magnet.isHorizontal() ) {
			orientation = IElectromagnet.ORIENT_HOR;
		}
		if ( magnet.isVertical() ) {
			orientation = IElectromagnet.ORIENT_VER;
		}
		xalDipole.setOrientation( orientation );
		elementContainer.addChild( xalDipole );
		syncManager.synchronize( xalDipole, magnet );
	}
	
	
	/*
	 * @see gov.sns.xal.slg.Visitor#visit(gov.sns.xal.slg.WScanner)
	 */
	public void visit(WScanner e) {
		primVisit(e);
		xal.model.elem.Marker xalMarker =
			new xal.model.elem.Marker(e.getName());
		elementContainer.addChild(xalMarker);
		syncManager.synchronize(xalMarker, e.getAcceleratorNode());
	}

}