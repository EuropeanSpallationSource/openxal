/*
 * LatticeParser.java
 *
 * Created on February 21, 2003, 10:23 AM
 */
package xal.model.xml;

import java.util.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.data.*;
import xal.tools.xml.*;

import xal.model.*;
import xal.model.elem.*;

/**
 * Utility class for building an XAL Model Lattice from a corresponding XML
 * file.
 *
 *
 * @author Christopher Allen
 */
public class LatticeXmlParser {

    private static final Logger LOGGER = Logger.getLogger(LatticeXmlParser.class.getName());

    /*
     *  Global Attributes
     */
    /**
     * Attributes for XAL/MODEL/LATTICE DTD
     */
    public static final String ELEM_LATT = "Lattice";
    public static final String ELEM_SEQ = "Sequence";
    public static final String ELEM_ELEM = "Element";
    public static final String ELEM_PARAM = "Parameter";

    public static final String ELEM_COMM = "comment";

    public static final String ATTR_ID = "id";
    public static final String ATTR_LEN = "len";

    public static final String ATTR_VER = "ver";
    public static final String ATTR_AUTH = "author";
    public static final String ATTR_DATE = "date";
    public static final String ATTR_TEXT = "text";

    public static final String ATTR_NAME = "name";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_VAL = "value";

    public static final String ATTR_SEP = "|";

    // ********* constructors    
    /**
     * Creates a new instance of LatticeXmlParser
     */
    public LatticeXmlParser() {
    }

    // ********* static parsing methods
    /**
     * Parses the XML file specified by the supplied URI. Return a <code>
     * Lattice</code> object configured according to the file.
     *
     * @param fileUri the URI specifying the XML file to parse
     * @param bolValidate apply XML DTD validation
     * @return the lattice object described by the XML file
     * @exception ParsingException An exception was encountered in parsing
     */
    public static Lattice parse(String fileUri, boolean bolValidate)
            throws ParsingException {
        LatticeXmlParser parser = new LatticeXmlParser();
        return parser.parseUrl(fileUri, bolValidate);
    }

    /**
     * Parses the supplied DataAdaptor and return a <code>
     * Lattice</code> object configured according to the Adaptor.
     *
     * @param adaptor the DataAdaptor containing the Lattice definition
     * @return the lattice object described by the XML file
     * @exception ParsingException An exception was encountered in parsing
     */
    public static Lattice parseDataAdaptor(DataAdaptor adaptor)
            throws ParsingException {
        LatticeXmlParser parser = new LatticeXmlParser();
        return parser.parseAdaptor(adaptor);
    }

    // ********** instance based parsing methods
    /**
     * Parse an XAL Model lattice file and build the corresponding Lattice
     * object. The lattice file can be validated by setting the DTD validation
     * flag. The file indicated must be a properly formated XML file.
     *
     * @param strFile URL of lattice description file
     * @param bolValidate apply XML DTD validation
     *
     * @return Lattice object built according to file contents
     *
     * @exception ParsingException An exception was encountered in parsing
     */
    public Lattice parseUrl(String strFile, boolean bolValidate)
            throws ParsingException {
        // Attach a data adaptor to the XML file then build the lattice from it
        XmlDataAdaptor daptUrl = XmlDataAdaptor.adaptorForUrl(strFile, bolValidate);
        return parseAdaptor(daptUrl);
    }

    /**
     * Parses the given data source for modeling lattice information and creates
     * the corresponding lattice.
     *
     * @param adaptor data source containing the modeling lattice
     * @return model lattice object created from the given data source
     *
     * @throws ParsingException general format exception
     *
     * @author Christopher K. Allen
     * @since Apr 13, 2011
     */
    public Lattice parseAdaptor(DataAdaptor adaptor) throws ParsingException {

        DataAdaptor daptLat = adaptor.childAdaptor(ELEM_LATT);

        // Extract lattice attributes and set them
        // lattice id
        String strId = daptLat.stringValue(ATTR_ID);
        // lattice version
        String strVer = daptLat.stringValue(ATTR_VER);
        // lattice author
        String strAuth = daptLat.stringValue(ATTR_AUTH);
        // lattice date
        String strDate = daptLat.stringValue(ATTR_DATE);

        Lattice latUrl = new Lattice();

        latUrl.setId(strId);
        latUrl.setVersion(strVer);
        latUrl.setAuthor(strAuth);
        latUrl.setDate(strDate);

        try {
            this.loadComposite(latUrl, daptLat);

        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException e) {
            LOGGER.log(Level.SEVERE, null, e);
            throw new ParsingException(e.getMessage());
        }

        return latUrl;
    }

    /*
     *  Internal Support
     */
    /**
     * Build a sequence with its elements from a data source represented by a
     * DataAdaptor interface. The data adaptor should be configured according to
     * the XAL_MODEL.DTD definition. Therefore, the elements and sub-sequences
     * contained in this sequence need to be represented as child adaptors.
     *
     * @param daptSeq data source containing sequence structure
     *
     * @return new ElementSeq object built according to data adaptor
     *
     * @exception DataFormatException data does not conform to XAL_MODEL.DTD
     * specification
     * @exception NumberFormatException numeric parameter is malformed and
     * unparsable
     * @exception ClassNotFoundException an unknown Element type was encountered
     * @exception InstantiationException unable to instantiate an IElement
     * instance
     * @exception NoSuchMethodException unknown or invalid Parameter for an
     * Element was encountered
     */
    protected ElementSeq buildSequence(DataAdaptor daptSeq)
            throws DataFormatException, NumberFormatException, ClassNotFoundException, InstantiationException, NoSuchMethodException {
        // Create a new ElementSeq instance
        // sequence id
        String strId = daptSeq.stringValue(ATTR_ID);
        ElementSeq seqNew = new Sector(strId);

        this.loadComposite(seqNew, daptSeq);

        return seqNew;
    }

    /**
     * Return the comment string from a comment element within a sequence.
     *
     * @param daptComm data adaptor containing comment element
     *
     * @return comment string
     *
     * @exception MissingDataException an attribute was missing from the comment
     */
    protected String buildComment(DataAdaptor daptComm) {
        // author of comment
        String strAuth;
        // date of comment
        String strDate;
        // user comments for sequence
        String strText;
        // comment string
        String strComm;

        strAuth = daptComm.stringValue(ATTR_AUTH);
        strDate = daptComm.stringValue(ATTR_DATE);
        strText = daptComm.stringValue(ATTR_TEXT);

        strComm = strAuth + ATTR_SEP + strDate + ATTR_SEP + strText;

        return strComm;
    }

    /**
     * Build a IElement object according to parameters specified in a data
     * adaptor.
     *
     * @param daptElem data adaptor containing element parameters
     *
     * @return new IElement instance specified by the data adaptor
     *
     * @exception InstantiationException unable to instantiate an IElement
     * instance
     * @exception ClassNotFoundException an unknown IElement type was
     * encountered
     * @exception DataFormatException bad parameter format encountered
     * @exception NoSuchMethodException unknown or invalid Parameter for an
     * IElement was encountered
     */
    protected IElement buildElement(DataAdaptor daptElem)
            throws InstantiationException, ClassNotFoundException, DataFormatException, NoSuchMethodException {
        // Create a new element instance whose type is specified by its class type
        String strType = attrValue(daptElem, ATTR_TYPE);
        Class<?> clsElem = Class.forName(strType);

        Constructor<?> ctorElem = clsElem.getConstructor((Class<?>[]) null);
        IElement elemNew;
        try {
            elemNew = (IElement) ctorElem.newInstance((Object[]) null);

        } catch (IllegalArgumentException e) {
            throw new InstantiationException("No default element contructor.");

        } catch (IllegalAccessException e) {
            throw new InstantiationException("Unable to access element constructor");

        } catch (InvocationTargetException e) {
            throw new InstantiationException("Unable to instantiate element.");

        }

        // Set the optional element attributes
        if (daptElem.hasAttribute(ATTR_ID) && elemNew instanceof Element) {
            String strId = daptElem.stringValue(ATTR_ID);
            ((Element) elemNew).setId(strId);
        }
        this.loadElement(elemNew, daptElem);

        return elemNew;
    }

    /**
     * Load an ElementSeq object with it's components
     *
     * @param secNew ElementSeq object to be loaded
     * @param daptSeq data adaptor containing sequence information
     *
     * @exception ClassNotFoundException an unknown Element type was encountered
     * @exception DataFormatException bad parameter format
     * @exception NumberFormatException bad number format in parameter value
     * @exception NoSuchMethodException unknown or invalid Parameter for an
     * Element was encountered
     */
    protected void loadComposite(IComposite secNew, DataAdaptor daptSeq)
            throws DataFormatException, NumberFormatException, ClassNotFoundException, NoSuchMethodException, InstantiationException {
        // Build the sequence from its components
        Iterator<? extends DataAdaptor> iterChild = daptSeq.childAdaptors().iterator();
        while (iterChild.hasNext()) {
            DataAdaptor daptChild = iterChild.next();

            // Comments - Load any comments associated with sequence
            if (daptChild.name().equals(ELEM_COMM)) {
                String strComm = buildComment(daptChild);

                // Sequence - Load a subsequence within the sequence
            } else if (daptChild.name().equals(ELEM_SEQ)) {
                ElementSeq seqChild = buildSequence(daptChild);

                secNew.addChild(seqChild);

                // Element - Load an element into the sequence (a leaf node in tree)
            } else if (daptChild.name().equals(ELEM_ELEM)) {
                IElement elemNew = buildElement(daptChild);

                secNew.addChild(elemNew);

                // An error must have occurred
            } else {
                throw new DataFormatException("LatticeParser#buildSequence() - unrecognized XML element tag " + daptChild.name());

            }
        }
    }

    /**
     * Load an IElement object with its parameters specified in the data
     * adaptor.
     *
     * @param elem IElement object to have parameter assigned
     * @param daptElem data adaptor containing all parameter information for
     * element
     *
     * @exception DataFormatException bad parameter format
     * @exception NumberFormatException numeric value was malformed and
     * unparseable
     * @exception NoSuchMethodException unknown or invalid Parameter for an
     * Element was encountered
     */
    protected void loadElement(IElement elem, DataAdaptor daptElem)
            throws DataFormatException, NoSuchMethodException, NumberFormatException {

        // Iterate through all parameter elements
        Iterator<? extends DataAdaptor> iterParam = daptElem.childAdaptors(ELEM_PARAM).iterator();
        while (iterParam.hasNext()) {

            // Get the name, type, and value of the parameter
            DataAdaptor daptParam = iterParam.next();

            String strName = daptParam.stringValue(ATTR_NAME);
            String strType = daptParam.stringValue(ATTR_TYPE);
            String strValue = daptParam.stringValue(ATTR_VAL);

            // Use bean introspection to find and set the appropriate property
            try {
                BeanInfo bi = Introspector.getBeanInfo(elem.getClass());
                PropertyDescriptor pd[] = bi.getPropertyDescriptors();
                PropertyDescriptor property = null;
                for (int i = 0; i < pd.length; i++) {
                    // match a property with the same name and type
                    if ((strName.equalsIgnoreCase(pd[i].getName()))
                            && (strType.equalsIgnoreCase(pd[i].getPropertyType().getName()))) {
                        property = pd[i];
                        break;
                    }
                }
                if (property != null) {

                    // get and invoke the set method
                    Method setter = property.getWriteMethod();

                    // make sure the property is writable
                    if (setter != null) {

                        // Identify the parameter class and pack the value into the 
                        // appropriate object
                        Class<?> clsParam;
                        Object objParam;

                        if (strType.equals("boolean")) {
                            clsParam = boolean.class;
                            objParam = Boolean.valueOf(strValue);

                        } else if (strType.equals("byte")) {
                            clsParam = byte.class;
                            objParam = Byte.valueOf(strValue);

                        } else if (strType.equals("int")) {
                            clsParam = int.class;
                            objParam = Integer.valueOf(strValue);

                        } else if (strType.equals("float")) {
                            clsParam = float.class;
                            objParam = Float.valueOf(strValue);

                        } else if (strType.equals("double")) {
                            clsParam = double.class;
                            objParam = Double.valueOf(strValue);

                        } else {
                            clsParam = Class.forName(strType);

                            Class<?> arrCtorSig[] = {String.class};
                            Object arrCtorArg[] = {strValue};
                            Constructor<?> ctorParam = clsParam.getConstructor(arrCtorSig);

                            objParam = ctorParam.newInstance(arrCtorArg);
                        }
                        setter.invoke(elem, new Object[]{objParam});
                    }
                }

            } catch (NumberFormatException e) {
                throw new NumberFormatException("LatticeParser#loadParameters() - bad parameter number format"
                        + strName + " for element " + elem.getId()
                );

            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodException("LatticeParser#loadParameters() - unknown parameter "
                        + strName + " for element " + elem.getId()
                );
            } catch (IntrospectionException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | SecurityException | InvocationTargetException e) {
                throw new DataFormatException("LatticeParser#loadParameters() - unable to set parameter "
                        + strName + " for element " + elem.getId()
                );
            }

        }

    }

    /**
     * Returns the attribute value string from a DataAdaptor interface. Performs
     * error checking in that a MissingDataException is thrown if attribute does
     * not exist.
     *
     * @param strAttrName attribute name
     *
     * @return string value of attribute
     *
     * @exception MissingDataException specified attribute not present in
     * DataAdaptor
     */
    protected String attrValue(DataAdaptor dapt, String strAttrName)
            throws MissingDataException {
        if (!dapt.hasAttribute(strAttrName)) {
            throw new MissingDataException("LatticeParser#attrValue() - DataAdaptor does not have attribute " + strAttrName);
        }

        return dapt.stringValue(strAttrName);
    }

    /**
     * Add a parameter child to a DataAdaptor
     */
    protected static void addParameter(DataAdaptor dapt, String strName, String strType, String strValue) {
        DataAdaptor daptParam = dapt.createChild("Parameter");

        daptParam.setValue("name", strName);
        daptParam.setValue("type", strType);
        daptParam.setValue("value", strValue);
    }

}
