package edu.stanford.lcls.modelmanager.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import se.lu.esss.ics.jels.ImporterHelpers;
import se.lu.esss.ics.jels.smf.impl.ESSFieldMap;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlTableIO;

/**
 * Exporting of Accelerator to the OpenXAL format with all all required files.
 * 
 * @author Blaz Kranjc
 */
public class AcceleratorExporter {
	private Accelerator acc;
	private String filename;
	private File dir;
	private File mainFile;
	private File implFile;
	private File paramsFile;
	private File opticsFile;
	private File modelConfigFile;

	/**
	 * Constructor.
	 * @param acc Accelerator to export.
	 * @param path Path to the main file.
	 */
	public AcceleratorExporter(Accelerator acc, File mainFile) {
		this.acc = acc;
		this.mainFile = mainFile;
		filename = mainFile.getName().replaceFirst("[.][^.]+$", "");
		dir = mainFile.getParentFile();
		implFile = new File(dir, filename+".impl");
		paramsFile = new File(dir, filename+"-model.params");
		opticsFile = new File(dir, filename+".xdxf");
		modelConfigFile = new File(dir, filename+"ModelConfig.xml");
	};
	
	/**
	 * Export the Accelerator to files.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void export() throws IOException, URISyntaxException {
		dir.mkdir();
		exportOptics();
		exportModelParams();
		exportImplementations();
		exportModelConfigs();
		exportMain();
	}

	/**
	 * Export the main OpenXAL accelerator file.
	 * @throws IOException
	 */
	private void exportMain() throws IOException {
		XmlDataAdaptor docAdaptor = XmlDataAdaptor.newEmptyDocumentAdaptor();
		
        DataAdaptor sourceAdaptor = docAdaptor.createChild("sources");
		sourceAdaptor.setValue("version", "2.0");

        final DataAdaptor modelConfigAdaptor = sourceAdaptor.createChild("modelElementConfig_source");
        modelConfigAdaptor.setValue("name", "modelElementConfig");
        modelConfigAdaptor.setValue("url", modelConfigFile.getName());

        final DataAdaptor implementationAdaptor = sourceAdaptor.createChild("deviceMapping_source");
        implementationAdaptor.setValue("name", "deviceMapping");
        implementationAdaptor.setValue("url", implFile.getName());

        DataAdaptor opticsAdaptor = sourceAdaptor.createChild("optics_source");
        opticsAdaptor.setValue("name", "optics");
        opticsAdaptor.setValue("url", opticsFile.getName());

        final DataAdaptor paramsAdaptor = sourceAdaptor.createChild("tablegroup_source");
        paramsAdaptor.setValue("name", "modelparams");
        paramsAdaptor.setValue("url", paramsFile.getName());

        docAdaptor.writeTo(mainFile);
	}

	/**
	 * Export the optics file.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private void exportOptics() throws IOException, URISyntaxException {
		XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
		Document xml = da.document();
		da.writeNode(acc);
		ImporterHelpers.xmlCleanup(xml);
		Element root = xml.getDocumentElement();
		root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", 
				"xsi:noNamespaceSchemaLocation", "http://sourceforge.net/p/xaldev/openxal/ci/master/tree/core/resources/xal/schemas/xdxf.xsd?format=raw");

		// Field Maps
		List<AcceleratorNode> fieldMapNodes = acc.getAllNodesOfType("FM");
		for (AcceleratorNode fieldMapNode : fieldMapNodes) {
			ESSFieldMap fieldMap = (ESSFieldMap)fieldMapNode;
			String destinationFile = new URL(dir.toURI().toURL(), fieldMap.getFieldMapFile()+".edz").toString(); //TODO
			fieldMap.getFieldProfile().saveFile(destinationFile);
		}

		da.writeTo(opticsFile);
	}

	/**
	 * Export model parameters.
	 * @throws IOException
	 */
	private void exportModelParams() throws IOException {
		XmlTableIO.writeTableGroupToFile(acc.editContext(), "modelparams", paramsFile);
	}
	
	/**
	 * Creates implementation file.
	 * @throws IOException
	 */
	private void exportImplementations() throws IOException {
		Map<String, String> impls = new HashMap<>();
		getElementImplementations(acc, impls);

		XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
		
        DataAdaptor mappingAdaptor = da.createChild("deviceMapping");

		for (Entry<String, String> impl : impls.entrySet()) {
			DataAdaptor deviceAdaptor = mappingAdaptor.createChild("device");
			deviceAdaptor.setValue("class", impl.getValue());
			deviceAdaptor.setValue("type", impl.getKey());
		};
        da.writeTo(implFile);
	}

	/**
	 * Creates model configuration file.
	 * TODO This just copies the resource. Might be problematic on MAC.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private void exportModelConfigs() throws IOException, URISyntaxException {
		File input = new File(getClass().getResource("/edu/stanford/lcls/modelmanager/util/ModelConfig.xml").toURI());
		Files.copy(input.toPath(), modelConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * Recursively traverses the accelerator node and includes implementation details for each unique node. 
	 * @param node Node on which to start traversing.
	 * @param impls Implementation classes of nodes.
	 */
	private static void getElementImplementations(AcceleratorNode node, Map<String, String> impls) {
		impls.put(node.getType(), node.getClass().getCanonicalName());
		if (node instanceof AcceleratorSeq) {
			for (AcceleratorNode n : ((AcceleratorSeq) node).getNodes()) {
				getElementImplementations(n, impls);
			}
		}
	}
}
