package se.lu.esss.linaclego;

import java.util.ArrayList;
import java.util.List;

import xal.tools.data.GenericRecord;
import xal.tools.data.DataAttribute;
import xal.tools.data.DataTable;


/**
 * Importer for linacLego linacData parameters into OpenXAL format tables.
 *
 * @version 0.1 3 Sep 2015
 * @author Blaz Kranjc
 */
public class LinacParameterImporter {

	private final Linac linac;
	private final Parameters params; 

	/**
	 * Class constructor.
	 *
	 * @param linac Linac object from which parameters are read.
	 */
	public LinacParameterImporter(final Linac linac) {
		this.linac = linac;
		this.params = linac.getLinacData();
	}

	/**
	 * Generates the openxal "beam" table from linacLego parameters.
	 * The units are converted to suit the ones used by openxal.
	 *
	 * @return generated table
	 */
	private DataTable generateBeamTable() {
		ArrayList<DataAttribute> attributes = new ArrayList<DataAttribute>();
		attributes.add(new DataAttribute("name", String.class, true));
		attributes.add(new DataAttribute("current", Double.class, false));
		attributes.add(new DataAttribute("bunchFreq", Double.class, false));
		attributes.add(new DataAttribute("phase", String.class, false));

		DataTable table = new DataTable("beam", attributes);
		GenericRecord record = new GenericRecord(table);
		record.setValueForKey("default", "name");
		record.setValueForKey(params.getDoubleValue("beamCurrent")*1E-3, "current");
		record.setValueForKey(params.getDoubleValue("beamFrequency")*1E+6, "bunchFreq");
		record.setValueForKey("(0,0,0)", "phase"); // TODO: get real phase value from linac lego params
		table.add(record);
		return table;
	}

	/**
	 * Generates the openxal "twiss" table from linacLego parameters.
	 * The units are converted to suit the ones used by openxal.
	 *
	 * @return generated table
	 */
	private DataTable generateTwissTable() {
		ArrayList<DataAttribute> attributes = new ArrayList<DataAttribute>();
		attributes.add(new DataAttribute("name", String.class, true));
		attributes.add(new DataAttribute("coordinate", String.class, true));
		attributes.add(new DataAttribute("alpha", Double.class, false));
		attributes.add(new DataAttribute("beta", Double.class, false));
		attributes.add(new DataAttribute("emittance", Double.class, false));

		DataTable table = new DataTable("twiss", attributes);
		List<Section> sections = linac.getSections();
		// TODO: load params for each section, don't just copy.
		for (Section section : sections) {
			GenericRecord record = new GenericRecord(table);
			record.setValueForKey(section.getId(), "name");
			record.setValueForKey("x", "coordinate");
			record.setValueForKey(params.getDoubleValue("alphaX"), "alpha");
			record.setValueForKey(params.getDoubleValue("betaX"), "beta");
			record.setValueForKey(params.getDoubleValue("emitX")*1E-5, "emittance");
			table.add(record);
			record = new GenericRecord(table);
			record.setValueForKey(section.getId(), "name");
			record.setValueForKey("y", "coordinate");
			record.setValueForKey(params.getDoubleValue("alphaY"), "alpha");
			record.setValueForKey(params.getDoubleValue("betaY"), "beta");
			record.setValueForKey(params.getDoubleValue("emitY")*1E-5, "emittance");
			table.add(record);
			record = new GenericRecord(table);
			record.setValueForKey(section.getId(), "name");
			record.setValueForKey("z", "coordinate");
			record.setValueForKey(params.getDoubleValue("alphaZ"), "alpha");
			record.setValueForKey(params.getDoubleValue("betaZ"), "beta");
			record.setValueForKey(params.getDoubleValue("emitZ")*1E-5, "emittance");
			table.add(record);
		}
		return table;
	}

	/**
	 * Generates the openxal "location" table from linacLego parameters.
	 * The units are converted to suit the ones used by openxal.
	 *
	 * @return generated table
	 */
	private DataTable generateLocationTable() {
		ArrayList<DataAttribute> attributes = new ArrayList<DataAttribute>();
		attributes.add(new DataAttribute("name", String.class, true));
		attributes.add(new DataAttribute("species", String.class, false));
		attributes.add(new DataAttribute("W", Double.class, false));
		attributes.add(new DataAttribute("elem", String.class, false, ""));
		attributes.add(new DataAttribute("s", Double.class, false, "0"));
		attributes.add(new DataAttribute("t", Double.class, false, "0"));

		DataTable table = new DataTable("location", attributes);
		GenericRecord record = new GenericRecord(table);
		record.setValueForKey("Accelerator", "name");
		record.setValueForKey("PROTON", "species");
		record.setValueForKey(params.getDoubleValue("ekin")*1E+6, "W");
		record.setValueForKey("", "elem");
		record.setValueForKey(0.0, "s");
		record.setValueForKey(0.0, "t");
		table.add(record);
		List<Section> sections = linac.getSections();
		// TODO: load parameters for each section, don't just copy.
		for (Section section : sections) {
			record = new GenericRecord(table);
			record.setValueForKey(section.getId(), "name");
			record.setValueForKey("PROTON", "species");
			record.setValueForKey(params.getDoubleValue("ekin")*1E+6, "W");
			record.setValueForKey("", "elem");
			record.setValueForKey(0.0, "s");
			record.setValueForKey(0.0, "t");
			table.add(record);
		}
		return table;
	}

	/**
	 * Generates the tables from linacData.
	 *
	 * @return collection of generated table
	 */
	public ArrayList<DataTable> getTables() {
		ArrayList<DataTable> tables = new ArrayList<DataTable>();
		tables.add(generateBeamTable());
		tables.add(generateTwissTable());
		tables.add(generateLocationTable());
		return tables;
	}

}
