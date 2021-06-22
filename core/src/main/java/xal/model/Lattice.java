/*
 * Lattice.java
 *
 * Created on August 11, 2002, 8:38 AM
 */

package xal.model;




import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;

import xal.model.elem.ElementSeq;
import xal.model.xml.LatticeXmlWriter;



/**
 * High-level container representing a machine model.
 *
 * @author  Christopher K. Allen
 * @author  Craig McChesney
 */
public class Lattice extends ElementSeq {
    
    
    /*
     *  Global Attributes
     */
    
    /** default number of element positions to reserve in list array */
    public static final int    SIZE_DEF_RESERVE = 100;
    
    /** the string type identifier for all Lattice objects */
    public static final String      TYPE = "Lattice";
    
    
    
    
    /*
     *  Local Attributes
     */
    
    /** version of lattice */
    private String      strVersion;
    
    /** lattice author */
    private String      strAuthor;
    
    /** lattice date */
    private String      strDate;
    
    
    /*
     *  Initialization
     */
    
    
    /**
     *  Creates a new instance of Lattice
     */
    public Lattice() {
        this(null, SIZE_DEF_RESERVE);
    }
 
    /**
     *  Creates a new instance of Lattice
     *
     *  @param  strId       identifier of the lattice
     */
    public Lattice(String strId) {
        this(strId, SIZE_DEF_RESERVE);
    }
 
    /**
     *  Creates a new instance of Lattice and reserves space for a 
     *  szReserve length lattice.
     *
     *  @param  strId       identifier of the lattice
     *  @param  szReserve   number of Element spaces to reserve
     */
    public Lattice(String strId, int szReserve) {
        super(TYPE, strId, szReserve);
    }
 
    /**
     *  Sets the version tag
     *
     *  @param  strVersion      revision number of lattice
     */
    public void setVersion(String strVersion)   {
        this.strVersion = strVersion;
    }
    
    /**
     *  Sets the author tag
     *
     *  @param  strAuthor       author of lattice description
     */
    public void setAuthor(String strAuthor)     {
        this.strAuthor = strAuthor;
    }
    
    /**
     *  Sets the date tag
     *
     *  @param  strDate         date string of lattice description
     */
    public void setDate(String strDate)         {
        this.strDate = strDate;
    }
    

    /*
     *  Attribute Queries
     */
    
    /**
     *  Get the version of the lattice
     *  
     *  @return     lattice revision number
     */
    public String   getVersion()        { return strVersion==null? "":strVersion; }
    
    /**
     *  Get the author of the lattice definition
     *
     *  @return     lattice author
     */
    public String   getAuthor()         { return strAuthor==null? "":strAuthor; }
    
    /**
     *  Get the date of lattice description
     *
     *  @return     lattice model date
     */
    public String   getDate()           { return strDate==null? "":strDate; }
    
    
    
    /**
     * Return a list of the <code>RingModel</code> objects contained in this model.
     *  
     * @return  ordered list of all <code>RingModel</code> objects within model
     * 
     * @deprecated  This method is never used
     */
    @Deprecated
    public List<RingModel> getRings()  {
        List<RingModel> lstRings = new LinkedList<>();
        
        Iterator<IComponent> iterLoc = this.localIterator();
        while (iterLoc.hasNext())   {
            IComponent  iComp = iterLoc.next();
            
            if (iComp instanceof RingModel) 
                lstRings.add((RingModel)iComp);
        }
        
        return lstRings;
    }

    /**
     * Return a list of the <code>LineModel</code> objects contained in this model.
     *  
     * @return  ordered list of all <code>LineModel</code> objects within model
     * 
     * @deprecated This method is never used.
     */
    @Deprecated
    public List<LineModel> getLines()  {
        List<LineModel> lstRings = new LinkedList<>();
        
        Iterator<IComponent> iterLoc = this.localIterator();
        while (iterLoc.hasNext())   {
            IComponent  iComp = iterLoc.next();
            
            if (iComp instanceof LineModel) 
                lstRings.add((LineModel)iComp);
        }
        
        return lstRings;
    }
        
    
    /*
     * Operations
     */
    
//    /**
//     * This does nothing at the moment.
//     * <strike>Add a new line to the model</strike>.
//     * 
//     * @param mdlLine   ignored
//     * 
//     * @see xal.model.IComponent#propagate(xal.model.IProbe)
//     */    
//    public void addLine(LineModel mdlLine)  {
//        
//    }
//    
    
    /*
     *  IComposite Interface
     */
    
//    /**  
//     *  Get the type identifier for ElementSeq
//     *
//     *  @return     type identifier for ElementSeq
//     */
//    public String getType() { return TYPE==null? "":TYPE; }
    
    /**
     * <p>
     *  Propagate a probe through the lattice.  The probe is first initialized by calling
     *  the <code>initialize()</code> method of the probe then updated by calling the
     *  <code>update()</code> method in order to save the initial state of the probe 
     *  into its trajectory.
     *  </p>
     *  <p>
     *  I have removed the pre- and post- process from this method and put it into
     *  the <code>{@link Scenario}</code> class, specifically the 
     *  <code>{@link Scenario#run()}</code> method.  Changing the state of a
     *  probe is not the agenda of a model element (or element sequence), only
     *  of an algorithm object.
     *  </p>  
     *
     *  @param  probe   the state of the probe will be advance using the elements dynamics
     *
     *  @exception  ModelException    an error occurred while advancing the probe state
     */
    @Override
    public void propagate(IProbe probe) throws ModelException {   

//        probe.initialize();
//        probe.update();
//        System.out.println("Lattice.propaget called");
        super.propagate(probe);
//		
//		probe.performPostProcessing();
    }
    
    /**
     *  <h2>Backward propagation of a probe through the lattice.</h2>
     *  <p>  
     *  The probe is first initialized by calling
     *  the <code>initialize()</code> method of the probe then updated by 
     *  calling the <code>update()</code> method in order to save the 
     *  initial state of the probe into its trajectory.
     *  </p>
     * <p>
     * <strong>NOTES</strong>: CKA
     * <br>
     * &middot; Support for backward propagation
     * February, 2009.
     * <br>
     * &middot; You must use the <em>proper algorithm</em> object
     * for this method to work correctly!
     * </p>
     *
     *  @param  probe   the state of the probe will be advance using the elements dynamics
     *
     *  @exception  ModelException    an error occurred while advancing the probe state
     */
    @Override
    public void backPropagate(IProbe probe) throws ModelException {   

        probe.initialize();
        probe.update();
        System.out.println("Lattice.backPropagate called");
        super.backPropagate(probe);
        
//        probe.performPostProcessing();
    }
    
    /*
     *  Testing and Debugging
     */
    
    /**
     * Returns a DOM document for the lattice.
     * 
     * @return a DOM document for the lattice
     * 
     * @throws IOException I guess this is thrown when <code>LatticeXmlWriter</code> is unable to parse this lattice
     */
	public Document asDocument() throws IOException {
	   return LatticeXmlWriter.documentForLattice(this);
	}
	
    /**
     *  Dump current state and content to output stream.
     *
     *  @param  os      output stream object
     */
    @Override
    public void print(PrintWriter os)    {
        
        os.println("LATTICE - " + this.getId() );
        os.println("Author    : " + this.getAuthor() );
        os.println("Date      : " + this.getDate() );
        os.println("Version   : " + this.getVersion() );
        os.println("Commments : " + this.getComments() );
        os.println("Count     : " + this.getChildCount() );
        os.println("Length    : " + this.getLength() );
        os.println("");

        super.print(os);
    }
        
        
    
    /*
     *  Internal Support
     */
    
}    
