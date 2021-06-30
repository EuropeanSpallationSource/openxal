package xal.schemas;

import java.io.IOException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import org.w3c.dom.DOMException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Unit test case for <code>ModelConfig.xsd</code> XML schema using
 * <code>ModelConfig.xml</code> structure.
 *
 * @author <a href='jakob.battelino@cosylab.com'>Jakob Battelino Prelog</a>
 */
public class ModelConfigTest extends AbstractXMLValidation {

    @Override
    public void progressiveSchemaValidation() {
        //Get XML schema.
        Schema schema = null;
        try {
            schema = getSchema();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull(schema);

        //Create a new DOM document.
        Document document = null;
        try {
            document = getDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            fail(e.getMessage());
        }
        assertNotNull(document);

        Validator validator = schema.newValidator();

        //Blank document should be valid.
        try {
            validator.validate(new DOMSource(document));
        } catch (IOException | SAXException e) {
            fail("Blank document should be valid!");
        }

        //Add and test sources element.
        Element root = testRoot(document, validator);

        //Add and test elements element.
        testElements(document, root, validator);

        //Add and test hardware element.
        /*testHardware(document, root, validator);*/
        //Add and test associations element.
        testAssociations(document, root, validator);
    }

    @Override
    protected Document getTestDocument() throws Exception {
        return readDocument(DIR_TEST_XMLS + "modelconfig_test.xml");
    }

    @Override
    protected Schema getSchema() throws Exception {
        return readSchema(DIR_SCHEMAS + "model-impl.xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }

    @Override
    protected Document getExternalDocument() throws Exception {
        return readDocument(DIR_TEST_XMLS + "modelconfig_test.xml");
    }

    private static Element testRoot(Document document, Validator validator) {
        //Fake root element
        try {
            Document testDoc = (Document) document.cloneNode(true);
            Element fakeElement = testDoc.createElement("fake1");
            testDoc.appendChild(fakeElement);
            validator.validate(new DOMSource(testDoc));
            fail("Validation with incorrect root element should not be successful!");
        } catch (IOException | DOMException | SAXException e) {
            assertTrue(e.getMessage().contains("Cannot find the declaration of element 'fake1'."));
        }

        //Correct root element.
        Element root = document.createElement("configuration");
        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:noNamespaceSchemaLocation", "http://sourceforge.net/p/xaldev/openxal/ci/master/tree/core/resources/xal/schemas/ModelConfig.xsd?format=raw");
        
        document.appendChild(root);
        try {
            validator.validate(new DOMSource(document));
            fail("Validation with incomplete root element should not be successful!");
        } catch (IOException | SAXException e) {
            assertTrue(e.getMessage().contains("The content of element 'configuration' is not complete."));
        }

        //Fake root child
        try {
            Document testDoc = (Document) document.cloneNode(true);
            Element testRoot = (Element) testDoc.getElementsByTagName("configuration").item(0);
            Element fakeElement = testDoc.createElement("fake1");
            testRoot.appendChild(fakeElement);
            validator.validate(new DOMSource(testDoc));
            fail("Validation with incorrect root child element should not be successful!");
        } catch (IOException | DOMException | SAXException e) {
            assertTrue(e.getMessage().contains("Invalid content was found starting with element 'fake1'."));
        }

        return root;
    }

    private static Element testElements(Document document, Element root, Validator validator) {
        //Add elements element.
        Element elements = document.createElement("elements");
        root.appendChild(elements);
        try {
            validator.validate(new DOMSource(document));
            fail("Validation with incomplete elements element should not be successful!");
        } catch (IOException | SAXException e) {
            assertTrue(e.getMessage().contains("The content of element 'elements' is not complete."));
        }

        //Add default element.
        Element defaultElement = document.createElement("default");
        elements.appendChild(defaultElement);
        try {
            validator.validate(new DOMSource(document));
            fail("Validation with incomplete default element should not be successful!");
        } catch (IOException | SAXException e) {
            assertTrue(e.getMessage().contains("Attribute 'type' must appear on element"));
        }

        //Add 'type' attribute.
        defaultElement.setAttribute("type", "fake.package.model.Default");
        try {
            validator.validate(new DOMSource(document));
            fail("Validation with incomplete elements element should not be successful!");
        } catch (IOException | SAXException e) {
            assertTrue(e.getMessage().contains("The content of element 'elements' is not complete."));
            assertFalse(e.getMessage().contains("default"));
        }

        //Add drift element.
        Element driftElement = document.createElement("drift");
        elements.appendChild(driftElement);
        try {
            validator.validate(new DOMSource(document));
            fail("Validation with incomplete drift element should not be successful!");
        } catch (IOException | SAXException e) {
            assertTrue(e.getMessage().contains("Attribute 'type' must appear on element"));
        }

        //Add 'type' attribute.
        driftElement.setAttribute("type", "fake.package.model.Drift");
        try {
            validator.validate(new DOMSource(document));
            fail("Validation with incomplete configuration element should not be successful!");
        } catch (IOException | SAXException e) {
            assertFalse(e.getMessage().contains("{drift}"));
        }

        //Add RF cavity drift element.
        Element rfcavdriftElement = document.createElement("rfcavdrift");
        elements.appendChild(rfcavdriftElement);
        try {
            validator.validate(new DOMSource(document));
            fail("Validation with incomplete rfcavdrift element should not be successful!");
        } catch (IOException | SAXException e) {
            assertTrue(e.getMessage().contains("Attribute 'type' must appear on element"));
        }

        //Add 'type' attribute.
        rfcavdriftElement.setAttribute("type", "fake.package.model.RfCavDrift");
        try {
            validator.validate(new DOMSource(document));
            fail("Validation with incomplete configuration element should not be successful!");
        } catch (IOException | SAXException e) {
            assertFalse(e.getMessage().contains("rfcavdrift"));
        }

        //Add sequence element.
        Element seqElement = document.createElement("sequence");
        elements.appendChild(seqElement);
        try {
            validator.validate(new DOMSource(document));
            fail("Validation with incomplete sequence element should not be successful!");
        } catch (IOException | SAXException e) {
            assertTrue(e.getMessage().contains("Attribute 'type' must appear on element"));
        }

        //Add 'type' attribute.
        seqElement.setAttribute("type", "fake.package.model.Sector");
        try {
            validator.validate(new DOMSource(document));
            fail("Validation with incomplete configuration element should not be successful!");
        } catch (IOException | SAXException e) {
            assertTrue(e.getMessage().contains("The content of element 'configuration' is not complete."));
            assertFalse(e.getMessage().contains("sequence"));
        }

        return elements;
    }


    private static Element testAssociations(Document document, Element root, Validator validator) {
        //Add associations element.
        Element associations = document.createElement("associations");
        root.appendChild(associations);
        try {
            validator.validate(new DOMSource(document));
            fail("Validation with incomplete associations element should not be successful!");
        } catch (IOException | SAXException e) {
            assertTrue(e.getMessage().contains("The content of element 'associations' is not complete."));
        }

        //Add and test basic map element.
        testBasicMap(document, associations, validator);

        return associations;
    }

    private static Element testBasicMap(Document document, Element associations, Validator validator) {
        //Add basic map element.
        Element map = document.createElement("map");
        associations.appendChild(map);
        try {
            validator.validate(new DOMSource(document));
            fail("Validation with incomplete map element should not be successful!");
        } catch (IOException | SAXException e) {
            assertTrue(e.getMessage().contains("Attribute 'smf' must appear on element")
                    || e.getMessage().contains("Attribute 'model' must appear on element"));
        }

        //Add 'smf' and 'model' attributes.
        map.setAttribute("smf", "fake.package.BasicMap");
        map.setAttribute("model", "fake.package.model.BasicMap");
        try {
            validator.validate(new DOMSource(document));
        } catch (IOException | SAXException e) {
            fail("Document should now be valid!");
        }

        return map;
    }

}
