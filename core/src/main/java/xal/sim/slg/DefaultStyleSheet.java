package xal.sim.slg;

import java.io.*;

/**
 * @author wdklotz
 *
 * created May 21, 2003
 *
 * Default XSL stylesheet (more or less identical to the file '2LANL.xsl') to
 * transform our native lattice to a lattice compatible with the on-line model.
 *
 */
public class DefaultStyleSheet {

    private static String xlsSheet;

    static {
        xlsSheet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        xlsSheet += "<!--  -->";
        xlsSheet += "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">";
        xlsSheet += "            <!--xmlns=\"http://www.w3.org/TR/xslt\"-->";
        xlsSheet += "";
        xlsSheet += "<xsl:output method=\"xml\" doctype-system=\"Lattice.mod.xal.dtd\" indent=\"yes\"/>";
        xlsSheet += "";
        xlsSheet += "<!-- suppress text nodes unless requested -->";
        xlsSheet += "<xsl:template match=\"text()\"/>";
        xlsSheet += "";
        xlsSheet += "<!-- default: copy the element with all its attributes -->";
        xlsSheet += "<xsl:template match=\"*|/\">";
        xlsSheet += "    <xsl:copy>";
        xlsSheet += "        <xsl:copy-of select=\"@*\"/>";
        xlsSheet += "        <xsl:apply-templates/>";
        xlsSheet += "    </xsl:copy>";
        xlsSheet += "</xsl:template>";
        xlsSheet += "";
        xlsSheet += "<!-- all mappings to Marker -->";
        xlsSheet += "<xsl:template match=\"Element[@type='marker'] |";
        xlsSheet += "                     Element[@type='pmarker'] |";
        xlsSheet += "                     Element[@type='beampositionmonitor'] | ";
        xlsSheet += "                     Element[@type='beamcurrentmonitor'] |";
        xlsSheet += "                     Element[@type='beamlossmonitor'] |";
        xlsSheet += "                     Element[@type='wirescanner']\">";
        xlsSheet += "     <xsl:element name=\"Element\">";
        xlsSheet += "         <xsl:attribute name=\"type\">Marker</xsl:attribute>";
        xlsSheet += "         <xsl:attribute name=\"id\"><xsl:value-of select=\"@id\"/></xsl:attribute>";
        xlsSheet += "     </xsl:element>";
        xlsSheet += "</xsl:template>";
        xlsSheet += "";
        xlsSheet += "<xsl:template match=\"Element[@type='drift']\">";
        xlsSheet += "    <!-- use drift element counting to generate drift ids DRx (x=1,2,3,...) -->";
        xlsSheet += "    <xsl:element name=\"Element\">";
        xlsSheet += "        <xsl:attribute name=\"type\">IdealDrift</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"id\">DR<xsl:number count=\"Element[@type='drift']\"/></xsl:attribute>";
        xlsSheet += "        <xsl:element name=\"Parameter\">";
        xlsSheet += "            <xsl:attribute name=\"name\">Length</xsl:attribute>";
        xlsSheet += "            <xsl:attribute name=\"type\">double</xsl:attribute>";
        xlsSheet += "            <xsl:attribute name=\"value\"><xsl:value-of select=\"@length\"/></xsl:attribute>";
        xlsSheet += "        </xsl:element>";
        xlsSheet += "        <xsl:element name=\"Parameter\">";
        xlsSheet += "            <xsl:attribute name=\"name\">SubCount</xsl:attribute>";
        xlsSheet += "            <xsl:attribute name=\"type\">int</xsl:attribute>";
        xlsSheet += "            <xsl:attribute name=\"value\">3</xsl:attribute>";
        xlsSheet += "        </xsl:element>";
        xlsSheet += "    </xsl:element>";
        xlsSheet += "</xsl:template>";
        xlsSheet += "";
        xlsSheet += "<xsl:template match=\"Element[@type='dipole']\">";
        xlsSheet += "    <xsl:element name=\"Element\" use-attribute-sets=\"dipole\">";
        xlsSheet += "    <xsl:element name=\"Parameter\">";
        xlsSheet += "        <xsl:attribute name=\"name\">Length</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"type\">double</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"value\"><xsl:value-of select=\"@length\"/></xsl:attribute>";
        xlsSheet += "    </xsl:element>";
        xlsSheet += "    <xsl:element name=\"Parameter\">";
        xlsSheet += "        <xsl:attribute name=\"name\">SubCount</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"type\">int</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"value\">3</xsl:attribute>";
        xlsSheet += "    </xsl:element>";
        xlsSheet += "    <xsl:apply-templates/>";
        xlsSheet += "    </xsl:element>";
        xlsSheet += "</xsl:template>";
        xlsSheet += "";
        xlsSheet += "<xsl:template match=\"Element[@type='quadrupole']\">";
        xlsSheet += "    <xsl:element name=\"Element\" use-attribute-sets=\"quadrupole\">";
        xlsSheet += "    <xsl:element name=\"Parameter\">";
        xlsSheet += "        <xsl:attribute name=\"name\">Length</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"type\">double</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"value\"><xsl:value-of select=\"@length\"/></xsl:attribute>";
        xlsSheet += "    </xsl:element>";
        xlsSheet += "    <xsl:element name=\"Parameter\">";
        xlsSheet += "        <xsl:attribute name=\"name\">SubCount</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"type\">int</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"value\">3</xsl:attribute>";
        xlsSheet += "    </xsl:element>";
        xlsSheet += "    <xsl:apply-templates/>";
        xlsSheet += "    </xsl:element>";
        xlsSheet += "</xsl:template>";
        xlsSheet += "";
        xlsSheet += "<xsl:template match=\"Element[@type='hsteerer']\">";
        xlsSheet += "    <xsl:element name=\"Element\" use-attribute-sets=\"dipole\">";
        xlsSheet += "    <xsl:element name=\"Parameter\">";
        xlsSheet += "        <xsl:attribute name=\"name\">Length</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"type\">double</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"value\"><xsl:value-of select=\"@length\"/></xsl:attribute>";
        xlsSheet += "    </xsl:element>";
        xlsSheet += "    <xsl:apply-templates/>";
        xlsSheet += "    </xsl:element>";
        xlsSheet += "</xsl:template>";
        xlsSheet += "";
        xlsSheet += "<xsl:template match=\"Element[@type='vsteerer']\">";
        xlsSheet += "    <xsl:element name=\"Element\" use-attribute-sets=\"dipole\">";
        xlsSheet += "    <xsl:element name=\"Parameter\">";
        xlsSheet += "        <xsl:attribute name=\"name\">Length</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"type\">double</xsl:attribute>";
        xlsSheet += "        <xsl:attribute name=\"value\"><xsl:value-of select=\"@length\"/></xsl:attribute>";
        xlsSheet += "    </xsl:element>";
        xlsSheet += "    <xsl:apply-templates/>";
        xlsSheet += "    </xsl:element>";
        xlsSheet += "</xsl:template>";
        xlsSheet += "";
        xlsSheet += "<xsl:template match=\"Element[@type='rfgap']\">";
        xlsSheet += "    <xsl:element name=\"Element\" use-attribute-sets=\"rfgap\">";
        xlsSheet += "    <xsl:apply-templates/>";
        xlsSheet += "    </xsl:element>";
        xlsSheet += "</xsl:template>";
        xlsSheet += "";
        xlsSheet += "<xsl:template match=\"Parameter[@name='StartPosition']\"><!--ignore this parameter--></xsl:template>";
        xlsSheet += "<xsl:template match=\"Parameter[@name='Position']\"><!--ignore this parameter--></xsl:template>";
        xlsSheet += "";
        xlsSheet += "<xsl:attribute-set name=\"dipole\">";
        xlsSheet += "    <xsl:attribute name=\"type\">IdealMagSteeringDipole</xsl:attribute>";
        xlsSheet += "    <xsl:attribute name=\"id\"><xsl:value-of select=\"@id\"/></xsl:attribute>";
        xlsSheet += "</xsl:attribute-set>";
        xlsSheet += "";
        xlsSheet += "<xsl:attribute-set name=\"quadrupole\">";
        xlsSheet += "    <xsl:attribute name=\"type\">IdealMagQuad</xsl:attribute>";
        xlsSheet += "    <xsl:attribute name=\"id\"><xsl:value-of select=\"@id\"/></xsl:attribute>";
        xlsSheet += "</xsl:attribute-set>";
        xlsSheet += "";
        xlsSheet += "<xsl:attribute-set name=\"rfgap\">";
        xlsSheet += "    <xsl:attribute name=\"type\">IdealRfGap</xsl:attribute>";
        xlsSheet += "    <xsl:attribute name=\"id\"><xsl:value-of select=\"@id\"/></xsl:attribute>";
        xlsSheet += "</xsl:attribute-set>";
        xlsSheet += "";
        xlsSheet += "</xsl:stylesheet>";
    }

    private DefaultStyleSheet() {
        throw new IllegalStateException("Utility class");
    }

    public static StringReader toReader() {
        return new StringReader(xlsSheet);
    }
}
