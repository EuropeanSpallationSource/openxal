/*
 * Copyright (C) 2017 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.extension.tracewinimporter.openxalexporter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xal.extension.jels.ImporterHelpers;
import xal.extension.jels.smf.ESSAccelerator;
import xal.extension.jels.smf.impl.FieldMap;
import xal.extension.jels.smf.impl.MagFieldMap;
import xal.extension.jels.smf.impl.RfFieldMap;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlTableIO;

/**
 * Exporting of Accelerator to the OpenXAL format with all all required files.
 *
 * @author Blaz Kranjc
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class AcceleratorExporter {

    private ESSAccelerator acc;
    private File dir;
    private File mainFile;
    private File implFile;
    private File paramsFile;
    private File opticsFile;
    private File modelConfigFile;

    /**
     * Constructor.
     *
     * @param acc Accelerator to export.
     * @param dir Path to the main file.
     * @param mainFile Name of the main file.
     */
    public AcceleratorExporter(ESSAccelerator acc, String dir, String mainFile) {
        this.acc = acc;
        this.mainFile = new File(dir, mainFile + ".xal");
        this.dir = this.mainFile.getParentFile();
        implFile = new File(dir, mainFile + ".impl");
        paramsFile = new File(dir, mainFile + "-model.params");
        opticsFile = new File(dir, mainFile + ".xdxf");
        modelConfigFile = new File(dir, mainFile + "ModelConfig.xml");
    }

    /**
     * Return the main files that will be saved. Files for FieldProfile objects
     * are not included.
     *
     * @return Array of files.
     */
    public File[] getFiles() {
        return new File[]{mainFile, implFile, paramsFile, opticsFile, modelConfigFile};
    }

    /**
     * Export the Accelerator to files.
     *
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
     *
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
     *
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
                "xsi:noNamespaceSchemaLocation",
                "https://bitbucket.org/europeanspallationsource/openxal/raw/site.ess.master/core/resources/xal/schemas/xdxf.xsd");

        da.writeTo(opticsFile);

        // Field Maps - get a list of unique field maps.
        List<AcceleratorNode> fieldMapNodes = acc.getAllNodesOfType("RFM");
        Map<String,FieldMap> fieldMaps = new HashMap<>();
        for (AcceleratorNode fieldMapNode : fieldMapNodes) {
            RfFieldMap fieldMap = (RfFieldMap) fieldMapNode;
            if(!fieldMaps.containsValue(fieldMap.getFieldMap())){
                fieldMaps.put(fieldMap.getFieldMapFile(),fieldMap.getFieldMap());
            }
        }
        // Solenoid Field Maps
        fieldMapNodes = acc.getAllNodesOfType("MFM");
        for (AcceleratorNode fieldMapNode : fieldMapNodes) {
            MagFieldMap fieldMap = (MagFieldMap) fieldMapNode;
            if(!fieldMaps.containsValue(fieldMap.getFieldMap())){
                fieldMaps.put(fieldMap.getFieldMapFile(),fieldMap.getFieldMap());
            }
        }
        
        // Export each field map only once.
        for (String fieldMapFile: fieldMaps.keySet()){
            fieldMaps.get(fieldMapFile).saveFieldMap(dir.toURI().toURL().toString(), fieldMapFile);
        }
    }

    /**
     * Export model parameters.
     *
     * @throws IOException
     */
    private void exportModelParams() throws IOException {
        XmlTableIO.writeTableGroupToFile(acc.editContext(), "modelparams", paramsFile);
    }

    /**
     * Creates implementation file.
     *
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
        }
        da.writeTo(implFile);
    }

    /**
     * Creates model configuration file.
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    private void exportModelConfigs() throws IOException, URISyntaxException {
        InputStream modelConfigResource = this.getClass().getResourceAsStream("ModelConfig.xml");
        Files.copy(modelConfigResource, modelConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Recursively traverses the accelerator node and includes implementation
     * details for each unique node.
     *
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
