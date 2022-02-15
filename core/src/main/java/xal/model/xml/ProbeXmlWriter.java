package xal.model.xml;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;

import xal.model.probe.Probe;
import xal.tools.xml.XmlDataAdaptor;

/**
 * Saves probe instances to an XML file.
 *
 * @author Craig McChesney
 * @version $id:
 *
 */
public class ProbeXmlWriter {

    private static final String DOC_TYPE = Probe.PROBE_LABEL;

    /**
     * Writes supplied <code>Probe</code> to the specified XML file.
     *
     * @param aProbe <code>Probe</code> to output
     * @param fileURI String URI of output file
     *
     * @throws IOException error writing to fileURI
     */
    public static void writeXml(Probe<?> aProbe, String fileURI)
            throws IOException {
        ProbeXmlWriter writer = new ProbeXmlWriter();
        XmlDataAdaptor doc = writer.writeProbeToDoc(aProbe);
        doc.writeTo(new File(fileURI));
    }

    /**
     * Returns a DOM document for the supplied probe.
     *
     * @param probe probe whose state will be recorded to a document
     *
     * @return a DOM for the supplied lattice
     */
    public static Document documentForProbe(Probe<?> probe) throws IOException {
        ProbeXmlWriter writer = new ProbeXmlWriter();
        XmlDataAdaptor doc = writer.writeProbeToDoc(probe);
        return doc.document();
    }

    /**
     * Writes supplied <code>Probe</code> to the specified XML file.
     *
     * @param aProbe <code>Probe</code> to write to XML file
     *
     * @throws IOException error writing to fileURI
     */
    public XmlDataAdaptor writeProbeToDoc(Probe<?> aProbe)
            throws IOException {
        XmlDataAdaptor document = XmlDataAdaptor.newEmptyDocumentAdaptor(DOC_TYPE, null);
        aProbe.save(document);
        return document;
    }
}
