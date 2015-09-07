package eu.ess.lt.parser.tracewin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import eu.ess.bled.Subsystem;
import eu.ess.bled.devices.lattice.Aperture;
import eu.ess.bled.devices.lattice.BPM;
import eu.ess.bled.devices.lattice.BeamlineElement.ApertureType;
import eu.ess.bled.devices.lattice.Bend;
import eu.ess.bled.devices.lattice.Bend.Orientation;
import eu.ess.bled.devices.lattice.Corrector;
import eu.ess.bled.devices.lattice.DTLCell;
import eu.ess.bled.devices.lattice.Drift;
import eu.ess.bled.devices.lattice.ElectrostaticAcceleration;
import eu.ess.bled.devices.lattice.ElectrostaticBend;
import eu.ess.bled.devices.lattice.ElectrostaticBend.BendType;
import eu.ess.bled.devices.lattice.ElectrostaticQuadropole;
import eu.ess.bled.devices.lattice.FieldMap;
import eu.ess.bled.devices.lattice.LatticeCommand;
import eu.ess.bled.devices.lattice.Marker;
import eu.ess.bled.devices.lattice.MultipoleMagnet;
import eu.ess.bled.devices.lattice.MultipoleMagnet.MagnetType;
import eu.ess.bled.devices.lattice.NCell;
import eu.ess.bled.devices.lattice.RFCavity;
import eu.ess.bled.devices.lattice.RFCavity.CavityType;
import eu.ess.bled.devices.lattice.RFQCell;
import eu.ess.bled.devices.lattice.Solenoid;
import eu.ess.bled.devices.lattice.SpaceChargeCompensation;
import eu.ess.bled.devices.lattice.ThinLens;
import eu.ess.lt.parser.ComponentFactory;
import eu.ess.lt.parser.Importer;
import eu.ess.lt.parser.ValidationResult;

/**
 * Converter from TraceWin to BLED
 * 
 *
 * @version 0.1 4 Sep 2015
 * @author Blaz Kranjc
 */
public class TraceWinImporter implements Importer, TraceWinTags {

	/** Speed of light */
	public static final double C = 299792468;
	private static final Logger LOG = Logger.getLogger("eu.ess.bled.import");

	private static final String COMMAND_PREFIX = "C_";
	private static final String LATTICE_END_SUFFIX = "-END";
	private static final String FILE_END_SUFFIX = "-END";

	private Section section;
	private double lastFrequency;
	private Integer lastSubsystem;
	private PrintWriter responseWriter;
	private LatticeCommand currentLattice;

	private int fileLineNumber;

	private Subsystem currentBeamline;
	private HashMap<String, Integer> counters;
	private HashMap<String, String> shortNames;
	private ComponentFactory bledComponentFactory;

	/**
	 * Method that sets the private fields and retrieves a factory instance.
	 */
	private void initClassVariables() {
		section = new Section(1);
		// latticeNumber = 1;
		lastFrequency = 0;
		lastSubsystem = 0;
		currentLattice = null;
		fileLineNumber = 0;
		currentBeamline = null;
		counters = new HashMap<>();
		shortNames = new HashMap<>();
		shortNames.put(E_DRIFT, "DR");
		shortNames.put(E_QUADRUPOLE, "QP");
		shortNames.put(E_BUNCHED_CAVITY, "GAP");
		shortNames.put(E_DTL_CELL, "DTL");
		shortNames.put(E_CAVITY_MULTIGAP, "NC");
		shortNames.put(E_EDGE_BENDING_MAGNET, "EDG");
		shortNames.put(E_BEND_ELE, "DIP");
		shortNames.put(E_THIN_STEERING_MAGNET, "TS");
		shortNames.put(C_MAGNETIC_STEERER, "ST");
		shortNames.put(D_POSITION, "BPM");
		shortNames.put(E_FIELD_MAP, "FM");

		bledComponentFactory = new ComponentFactory();
	}

	@Override
	public Collection<Subsystem> importFromTraceWin(String fileName, PrintWriter responseWriter)
			throws FileNotFoundException, IOException {
		// Initializing
		initClassVariables();
		this.responseWriter = responseWriter;
		BufferedReader br = new BufferedReader(new FileReader(fileName));

		// Parsing
		parseFromBufferedReader(br);
		br.close();

		// Putting it all together
		buildHierarchy();

		Collection<Subsystem> allSubsystems = new ArrayList<Subsystem>();
		allSubsystems.addAll(Arrays.asList(section.getBeamlines()));
		allSubsystems.addAll(Arrays.asList(section.getSlots()));
		allSubsystems.addAll(Arrays.asList(section.getComponents()));
		// Done
		return allSubsystems;
	}

	/**
	 * Parses input form the parameter {@link BufferedReader}, line by line.
	 * 
	 * @param reader
	 *            {@link BufferedReader} providing the input.
	 * @throws IOException
	 *             if there is a problem reading form the stream.
	 */
	private void parseFromBufferedReader(BufferedReader reader) throws IOException {
		String line = null;
		String originalLine = null;
		int idx, trailinglen;
		String[] values;

		// first read the header
		while (reader.ready()) {
			String name = null;
			originalLine = reader.readLine().trim();
			fileLineNumber++;
			if (originalLine.length() == 0) {
				//Line is empty continue.
				continue;
			}

			originalLine = replace(originalLine);
			line = originalLine.toUpperCase();
			while (line.startsWith(COMMENT_MARKER))
				line = line.substring(1);

			// remove trailing line comments
			if (line.contains(COMMENT_MARKER)) {
				trailinglen = line.length() - line.indexOf(COMMENT_MARKER);
				line = line.substring(0, line.indexOf(COMMENT_MARKER));
			} else
				trailinglen = 0;

			idx = originalLine.indexOf(NAME_VALUE_SEPARATOR + PART_SEPARATOR);

			if (idx > 0 && idx < line.length() - 1) {
				line = line.substring(idx + 1).trim();
				name = originalLine.substring(0, idx);
			}
			;

			values = split(originalLine.substring(idx + 1, originalLine.length() - trailinglen).trim());

			if (name == null) {
				String cmd = values[0].toUpperCase();
				name = assignName(cmd);
			}

			// if lattice end command was read, abort
			if (isElement(originalLine)) {
				readElement(line, values, section, name);
				if (currentBeamline != null) {
					currentBeamline.setDescription(currentBeamline.getDescription() + " " + name);
				}
			} else if (isLatticeCommand(originalLine)) {
				if (line.startsWith(C_LATTICE_END)) {
					if (currentLattice != null)
						name = currentLattice.getName() + LATTICE_END_SUFFIX;
					else
						writeFeedback("Lattice end detected before lattice start!");
				}
				readLatticeCommand(reader, originalLine, section, name);
				if (currentBeamline != null) {
					currentBeamline.setDescription(currentBeamline.getDescription() + " " + name);
				}
			} else if (isEdge(originalLine)) {
				// Bend magnet consists of three consecutive entries: bend edge,
				// bend magnet and another bend edge
				String edge1Line = line;
				String originalBendLine = readNextUncommentedLine(reader);
				String originalEdge2Line = readNextUncommentedLine(reader);

				int bendIndex = originalBendLine.indexOf(NAME_VALUE_SEPARATOR + PART_SEPARATOR);
				int edge1Index = originalLine.indexOf(NAME_VALUE_SEPARATOR + PART_SEPARATOR);
				int edge2Index = originalEdge2Line.indexOf(NAME_VALUE_SEPARATOR + PART_SEPARATOR);

				originalBendLine = replace(originalBendLine);
				String bendLine = originalBendLine.toUpperCase().substring(bendIndex + 1).trim();
				originalEdge2Line = replace(originalEdge2Line);
				String edge2Line = originalEdge2Line.toUpperCase().substring(edge2Index + 1).trim();

				if (bendLine.startsWith(E_BENDING_MAGNET) && edge2Line.startsWith(E_EDGE_BENDING_MAGNET)) {

					String[] edge1Values = values;
					String[] bendValues = split(bendLine);
					String[] edge2Values = split(edge2Line);

					String bendName = null;
					String edge1Name = null;
					String edge2Name = null;

					if (bendIndex > 0) {
						bendLine = bendLine.substring(bendIndex + 1).trim();
						bendName = originalBendLine.substring(0, bendIndex);
					} else {
						bendName = assignName(E_BENDING_MAGNET);
					}

					if (edge1Index > 0) {
						edge1Name = originalLine.substring(0, edge1Index);
					} else {
						edge1Name = assignName(E_EDGE_BENDING_MAGNET);
					}

					if (edge2Index > 0) {
						edge2Line = edge2Line.substring(edge2Index + 1);
						edge2Name = originalEdge2Line.substring(0, edge2Index);
					} else {
						edge2Name = assignName(E_EDGE_BENDING_MAGNET);
					}

					readEdge(edge1Line, edge1Values, edge1Name, bendLine, bendValues, bendName, edge2Line, edge2Values,
							edge2Name, section);
					if (currentBeamline != null) {
						currentBeamline.setDescription(currentBeamline.getDescription() + " " + bendName);
					}
				}
			} else if (isESSMetaTag(originalLine)) {
				readESSMetaTag(originalLine, section);
				// } else if (isLatticeBoundaryCommand(originalLine)) {
				// readLatticeBoundary(originalLine);
			} else if (!originalLine.startsWith(COMMENT_MARKER)) {
				writeFeedback("Unknown TraceWin entry: " + originalLine);
			}
		}
	}

	private String assignName(String cmd) {
		String name;
		int c = counters.containsKey(cmd) ? counters.get(cmd) : 1;
		String shortName = shortNames.containsKey(cmd) ? shortNames.get(cmd) : cmd;
		counters.put(cmd, c + 1);
		name = shortName + c;
		return name;
	}

	/**
	 * Read the next uncommented line from the parameter {@link BufferedReader}.
	 * Returned line is trimmed and has only singular spaces.
	 * 
	 * @param reader
	 *            {@link BufferedReader} providing the input.
	 * @return {@link String} that is the next uncommented line.
	 * @throws IOException
	 *             if there is a problem reading form the stream.
	 */
	private String readNextUncommentedLine(BufferedReader reader) throws IOException {
		String line = null;
		String originalLine = null;
		// first read the header
		while (reader.ready()) {
			originalLine = reader.readLine().trim();
			fileLineNumber++;
			if (originalLine.length() == 0) {
				writeFeedback("Empty line detected.");
				continue;
			}
			originalLine = replace(originalLine);
			line = originalLine.toUpperCase();
			if (line.startsWith(COMMENT_MARKER))
				continue;

			break;
		}
		return originalLine;
	}

	/**
	 * Replaces all spaces with a single space.
	 * 
	 * @param source
	 *            the source string to be manipulated
	 * @return the string containing all the replacements
	 */
	private static String replace(String source) {
		return source.replaceAll("\\s", " ");
	}

	/**
	 * Splits the source by the spaces. Multiple spaces are eliminated.
	 * 
	 * @param source
	 *            the source string
	 * @return split values
	 */
	private static String[] split(String source) {
		String[] vals = source.split("\\s");
		ArrayList<String> list = new ArrayList<String>();
		for (String s : vals) {
			String val = s.trim();
			if (s.isEmpty())
				continue;

			while (val.endsWith(COMMENT_MARKER)) {
				val = val.substring(0, val.length() - 1);
			}
			while (val.startsWith(COMMENT_MARKER) && val.length() > 1) {
				val = val.substring(1, val.length() - 1);
			}
			list.add(val);
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Returns true if the given line describes a command or false otherwise.
	 * 
	 * @param line
	 *            the line
	 * @return
	 */
	private boolean isLatticeCommand(String line) {
		if (line.startsWith(COMMENT_MARKER))
			return false;
		int idx = line.indexOf(NAME_VALUE_SEPARATOR + PART_SEPARATOR);
		if (idx >= 0 && idx < line.length() - 1) {
			line = line.substring(idx + 1).trim();
		}
		line = line.toUpperCase();
		// @formatter:off
		if (line.startsWith(C_CHOPPER) || line.startsWith(C_CHANGE_FREQUENCY) || line.startsWith(C_DUPLICATE_ELEMENTS)
				|| line.startsWith(C_MAGNETIC_STEERER) || line.startsWith(C_MAGNETIC_STEERER)
				|| line.startsWith(C_RFQ_COUPLING_GAP) || line.startsWith(C_RFQ_ELECTRODE_FOUR)
				|| line.startsWith(C_RFQ_ELECTRODE_TWO) || line.startsWith(C_RFQ_VANE_GEOMETRY)
				|| line.startsWith(C_SHIFT) || line.startsWith(C_SUPERPOSE_FIELD_MAP) || line.startsWith(C_CHANGE_BEAM)
				|| line.startsWith(C_FIELD) || line.startsWith(C_MIN_ENVELOPE_VARIATION)
				|| line.startsWith(C_MATCH_FAM_GRAD) || line.startsWith(C_MATCH_FAM_FIELD)
				|| line.startsWith(C_MATCH_FAM_PHASE) || line.startsWith(C_MATCH_FAM_LFOC)
				|| line.startsWith(C_MATCH_FAM_LENGTH) || line.startsWith(C_MIN_EMITTANCE_GROWTH)
				|| line.startsWith(C_MIN_FIELD_VARIATION) || line.startsWith(C_MIN_PHASE_VARIATION)
				|| line.startsWith(C_SET_ACHROMAT) || line.startsWith(C_SET_POSITION) || line.startsWith(C_SET_BEAM_E_P)
				|| line.startsWith(C_SET_BEAM_PHASE_ADVANCE) || line.startsWith(C_SET_BEAM_PHASE_ERROR)
				|| line.startsWith(C_SET_BEAM_SEPARATION) || line.startsWith(C_SET_BEAM_SIZE)
				|| line.startsWith(C_SET_BEAM_SIZE_MAX) || line.startsWith(C_SET_SYNC_PHASE)
				|| line.startsWith(C_SET_TWISS) || line.startsWith(C_SET_BEAM_ENERGY)
				|| line.startsWith(C_START_ACHROMAT) || line.startsWith(C_LATTICE_BEGIN)
				|| line.startsWith(C_LATTICE_END) || C_END.equalsIgnoreCase(line)) {
			return true;
		}
		// @formatter:on
		return false;
	}

	/**
	 * Returns true if the line represents a tracewin element.
	 * 
	 * @param line
	 *            the line to be parsed
	 * @return true if the line represents an element or false otherwise
	 */
	private boolean isElement(String line) {
		if (line.startsWith(COMMENT_MARKER))
			return false;
		int idx = line.indexOf(NAME_VALUE_SEPARATOR + PART_SEPARATOR);
		if (idx >= 0 && idx < line.length() - 1) {
			line = line.substring(idx + 1).trim();
		}
		line = line.toUpperCase();
		// @formatter:off
		if (line.startsWith(E_ALPHA_MAGNET) || line.startsWith(E_APERTURE) || line.startsWith(E_BEAM_CURRENT)
				|| line.startsWith(E_BEAM_ROTATION) || line.startsWith(E_BENDING_MAGNET)
				|| line.startsWith(E_BUNCHED_CAVITY) || line.startsWith(E_CAVITY_MULTIGAP) || line.startsWith(E_CHFRAME)
				|| line.startsWith(E_DRIFT) || line.startsWith(E_DTL_CELL)
				|| line.startsWith(E_ELECTROSTATIC_ACCELERATION) || line.startsWith(E_ELECTROSTATIC_QUADRUPOLE)
				|| line.startsWith(E_FIELD_MAP) || line.startsWith(E_FUNNELING_GAP)
				|| line.startsWith(E_MULTIPOLE_FIELD_MAP) || line.startsWith(E_QUADRUPOLE)
				|| line.startsWith(E_RFQ_CELL) || line.startsWith(E_SINUS_CAVITY)
				|| line.startsWith(E_SPACE_CHARGE_COMPENSATION) || line.startsWith(E_SOLENOID)
				|| line.startsWith(E_THIN_LENS) || line.startsWith(E_THIN_MATRIX)
				|| line.startsWith(E_THIN_STEERING_MAGNET) || line.startsWith(E_BEND_ELE)
				|| line.startsWith(C_MAGNETIC_STEERER)) {
			return true;
		}

		// diagnostic elements
		else if (line.startsWith(D_ACHROMAT) || line.startsWith(D_CURRENT) || line.startsWith(D_DELTA_SIZE)
				|| line.startsWith(D_DELTA_SIZE2) || line.startsWith(D_DELTA_SIZE3) || line.startsWith(D_DIVERGENCE)
				|| line.startsWith(D_EMITTANCE) || line.startsWith(D_EMITTANCE99) || line.startsWith(D_LUMINOSITY)
				|| line.startsWith(D_PERFECT_ENERGY) || line.startsWith(D_PERFECT_PHASE) || line.startsWith(D_PHASE)
				|| line.startsWith(D_POSITION) || line.startsWith(D_SIZE) || line.startsWith(D_SIZE_DIVERGENCE)
				|| line.startsWith(D_TRANSFER_MATRIX) || line.startsWith(D_TWISS) || line.startsWith(D_WAIST)) {
			return true;
		}
		// @formatter:on

		return false;
	}

	/**
	 * Returns true if the line represents a tracewin bend edge element.
	 * 
	 * @param line
	 *            the line to be parsed
	 * @return true if the line represents a bend edge element or false
	 *         otherwise
	 */
	private boolean isEdge(String line) {
		int idx = line.indexOf(NAME_VALUE_SEPARATOR + PART_SEPARATOR);
		if (idx >= 0 && idx < line.length() - 1) {
			line = line.substring(idx + 1).trim();
		}
		line = line.toUpperCase();
		if (line.startsWith(COMMENT_MARKER))
			return false;
		else if (line.startsWith(E_EDGE_BENDING_MAGNET))
			return true;
		return false;
	}

	/**
	 * Returns true if the line represents a beam line, slot or a marker.
	 * 
	 * @param line
	 *            the line to be parsed.
	 * @return true if the line represents a beam line, slot or a marker.
	 */
	private boolean isESSMetaTag(String line) {
		line = line.toUpperCase();
		if (line.startsWith(COMMENT_MARKER + M_BEAMLINE) || line.startsWith(COMMENT_MARKER + M_MARKER)
				|| line.startsWith(COMMENT_MARKER + M_SLOT) || line.startsWith(COMMENT_MARKER + M_BEGINBEAMLINE)) {
			return true;
		}
		return false;
	}

	/**
	 * Reads a beam line, slot or a marker element from the line. The read
	 * element has the provided {@link Subsystem} set as the parent system and
	 * is automatically added to the provided section.
	 * 
	 * @param originalLine
	 *            {@link String} unchanged line containing all of the element's
	 *            information.
	 * @param section
	 *            {@link Section} where this element belongs to.
	 * @param parentSubsystem
	 *            {@link Subsystem} parent of the element.
	 * @return <code>true</code> if the line is processed successfully.
	 */
	private boolean readESSMetaTag(String originalLine, Section section) {
		LOG.finest("Importing ESS meta tag from line: " + originalLine);

		String line = originalLine.toUpperCase().trim();

		if (line.startsWith(COMMENT_MARKER)) {
			// truncate the comments;
			while (line.startsWith(COMMENT_MARKER)) {
				line = line.substring(1).trim();
			}
		}
		if (line.startsWith(M_SLOT) || line.startsWith(M_BEAMLINE) || line.startsWith(M_MARKER)
				|| line.startsWith(M_BEGINBEAMLINE)) {
			String[] values = split(originalLine);
			String[] elements = null;
			if (values.length > 2) {
				elements = new String[values.length - 2];
				System.arraycopy(values, 2, elements, 0, elements.length);
			} else {
				elements = new String[0];
			}
			if (line.startsWith(M_SLOT)) {
				Subsystem slot = bledComponentFactory.getSlot(values[1], originalLine, lastSubsystem);
				section.addSlot(slot);
				lastSubsystem = lastSubsystem + 1;

				ValidationResult validation = bledComponentFactory.validateFields(slot);
				if (!validation.isSuccessful())
					writeFeedback(validation.getMessage());
			} else if (line.startsWith(M_BEAMLINE)) {
				Subsystem beamline = bledComponentFactory.getBeamline(values[1], originalLine, lastSubsystem);
				section.addBeamline(beamline);
				lastSubsystem = lastSubsystem + 1;

				ValidationResult validation = bledComponentFactory.validateFields(beamline);
				if (!validation.isSuccessful())
					writeFeedback(validation.getMessage());
			} else if (line.startsWith(M_MARKER)) {
				Marker marker = bledComponentFactory.getMarker(values[1], lastSubsystem);
				section.addComponent(marker);
				lastSubsystem = lastSubsystem + 1;
			} else if (line.startsWith(M_BEGINBEAMLINE)) {
				currentBeamline = bledComponentFactory.getBeamline(values[1], originalLine, lastSubsystem);
				section.addBeamline(currentBeamline);
				lastSubsystem = lastSubsystem + 1;
			}
			return true;
		}
		return false;
	}

	/**
	 * Reads a command from the line.
	 */
	private void readLatticeCommand(BufferedReader reader, String originalLine, Section section, String name)
			throws IOException {
		LOG.finest("Importing command " + name + " from line: " + originalLine);
		LatticeCommand latticeCommand = bledComponentFactory.getLatticeCommand(name, originalLine, lastSubsystem);
		section.addComponent(latticeCommand);
		lastSubsystem = lastSubsystem + 1;

		String[] values = split(originalLine.toUpperCase());
		if (originalLine.toUpperCase().contains(C_LATTICE_BEGIN)) {
			if (values.length > 1 && C_LATTICE_BEGIN.equals(values[1]))
				currentLattice = latticeCommand;
		} else if (values.length > 1 && values[1].toUpperCase().equals(C_CHANGE_FREQUENCY)) {
			lastFrequency = Double.parseDouble(values[2]) * 1e6;
		}
	}

	/**
	 * Reads an element from the line.
	 * 
	 * @param line
	 *            the truncated, trimmed, uppered line
	 * @param values
	 *            the values extracted from the line
	 * @param originalLine
	 *            the original line
	 * @param section
	 *            the section to append the element to
	 * @param name
	 *            the name of the element
	 * @param frequency
	 *            the frequency at the element
	 */
	private void readElement(String line, String values[], Section section, String name) {
		LOG.finest("Importing element " + name + " from line: " + line);

		// check if it is an element and add it to the section
		if (line.startsWith(E_APERTURE)) {
			values = composeTableValues(values, 4, true);
			// @formatter:off
			Aperture aperture = bledComponentFactory.getAperture(name, Double.parseDouble(values[1]) * 1E-3,
					Double.parseDouble(values[2]) * 1E-3, ApertureType.toEnum(Integer.parseInt(values[3])),
					lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(aperture);
		} else if (line.startsWith(E_BUNCHED_CAVITY)) {
			values = composeTableValues(values, 11, true);
			// @formatter:off
			RFCavity rfCavity = bledComponentFactory.getRFCavity(name, Double.parseDouble(values[1]),
					Double.parseDouble(values[2]), Double.parseDouble(values[3]) * 1E-3,
					Integer.parseInt(values[4]) == 1, values.length > 5 ? Double.parseDouble(values[5]) : 0.,
					values.length > 6 ? Double.parseDouble(values[6]) : 0.,
					values.length > 7 ? Double.parseDouble(values[7]) : 0.,
					values.length > 8 ? Double.parseDouble(values[8]) : 0.,
					values.length > 9 ? Double.parseDouble(values[9]) : 0.,
					values.length > 10 ? Double.parseDouble(values[10]) : 0., CavityType.BUNCHED, lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(rfCavity);
		} else if (line.startsWith(E_CAVITY_MULTIGAP)) {
			values = composeTableValues(values, 22, true);
			int mode = Integer.parseInt(values[1]);
			int nCells = Integer.parseInt(values[2]);
			double betaG = Double.parseDouble(values[3]);
			double length = 0;
			if (lastFrequency != 0)
				length = mode / 2. * nCells * betaG * TraceWinTags.C / lastFrequency;
			else
				writeFeedback(name + ": Frequency required, but has not yet been set!");
			// @formatter:off
			NCell nCell = bledComponentFactory.getNCell(name, length, mode, nCells, betaG,
					Double.parseDouble(values[4]), Double.parseDouble(values[5]), Double.parseDouble(values[6]) * 1E-3,
					Integer.parseInt(values[7]) == 1, Double.parseDouble(values[8]), Double.parseDouble(values[9]),
					Double.parseDouble(values[10]) * 1E-3, Double.parseDouble(values[11]) * 1E-3,
					Double.parseDouble(values[12]), Double.parseDouble(values[13]), Double.parseDouble(values[14]),
					Double.parseDouble(values[15]), Double.parseDouble(values[16]), Double.parseDouble(values[17]),
					Double.parseDouble(values[18]), Double.parseDouble(values[19]), Double.parseDouble(values[20]),
					Double.parseDouble(values[21]), lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(nCell);
		} else if (line.startsWith(E_DRIFT)) {
			values = composeTableValues(values, 4, true);
			// @formatter:off
			Drift drift = bledComponentFactory.getDrift(name, Double.parseDouble(values[1]) * 1E-3,
					Double.parseDouble(values[2]) * 1E-3, Double.parseDouble(values[3]) * 1E-3, lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(drift);
		} else if (line.startsWith(E_DTL_CELL)) {
			values = composeTableValues(values, 15, true);
			// @formatter:off
			DTLCell dtlCell = bledComponentFactory.getDTLCell(name, Double.parseDouble(values[1]) * 1E-3,
					Double.parseDouble(values[2]) * 1E-3, Double.parseDouble(values[3]) * 1E-3,
					Double.parseDouble(values[4]) * 1E-3, Double.parseDouble(values[5]), Double.parseDouble(values[6]),
					Double.parseDouble(values[7]), Double.parseDouble(values[8]), Double.parseDouble(values[9]) * 1E-3,
					Integer.parseInt(values[10]) == 1, Double.parseDouble(values[11]), Double.parseDouble(values[12]),
					Double.parseDouble(values[13]), Double.parseDouble(values[14]), lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(dtlCell);
		} else if (line.startsWith(E_ELECTROSTATIC_ACCELERATION)) {
			values = composeTableValues(values, 5, true);
			// @formatter:off
			ElectrostaticAcceleration esAccel = bledComponentFactory.getElectrostaticAcceleration(name,
					Double.parseDouble(values[2]) * 1E-3, Double.parseDouble(values[1]),
					Double.parseDouble(values[3]) / 1E-6, Double.parseDouble(values[4]) * 1E-3, lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(esAccel);
		} else if (line.startsWith(E_ELECTROSTATIC_QUADRUPOLE)) {
			values = composeTableValues(values, 9, true);
			// @formatter:off
			ElectrostaticQuadropole esQuad = bledComponentFactory.getElectrostaticQuadrupole(name,
					Double.parseDouble(values[1]) * 1E-3, Double.parseDouble(values[2]),
					Double.parseDouble(values[3]) * 1E-3, Double.parseDouble(values[4]), Double.parseDouble(values[5]),
					Double.parseDouble(values[6]), Double.parseDouble(values[7]), Double.parseDouble(values[8]),
					lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(esQuad);
		} else if (line.startsWith(E_FIELD_MAP)) {
			values = composeTableValues(values, 10, true);
			// @formatter:off
			FieldMap fieldMap = bledComponentFactory.getFieldMap(name, Double.parseDouble(values[2]) * 1E-3,
					Integer.parseInt(values[1]), Double.parseDouble(values[3]), Double.parseDouble(values[4]) * 1E-3,
					Double.parseDouble(values[5]), Double.parseDouble(values[6]), Double.parseDouble(values[7]),
					Integer.parseInt(values[8]), values[9], lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(fieldMap);
		} else if (line.startsWith(E_MULTIPOLE_FIELD_MAP)) {
			values = composeTableValues(values, 8, true);
			// @formatter:off
			MultipoleMagnet multipoleMagnet = bledComponentFactory.getMultipoleFieldMap(name,
					Double.parseDouble(values[2]) * 1E-3, MagnetType.toEnum(Integer.parseInt(values[1])),
					Integer.parseInt(values[3]), Double.parseDouble(values[4]), Double.parseDouble(values[5]) * 1E-3,
					Double.parseDouble(values[7]) * 1E-3, Integer.parseInt(values[6]), lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(multipoleMagnet);
		} else if (line.startsWith(E_RFQ_CELL)) {
			values = composeTableValues(values, 10, true);
			// @formatter:off
			RFQCell rfqCell = bledComponentFactory.getRFQCell(name, Double.parseDouble(values[5]) * 1E-3,
					Double.parseDouble(values[1]), Double.parseDouble(values[2]) * 1E-3, Double.parseDouble(values[3]),
					Double.parseDouble(values[4]), Double.parseDouble(values[6]), values[7],
					Double.parseDouble(values[8]) * 1E-3, Double.parseDouble(values[9]), lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(rfqCell);
		} else if (line.startsWith(E_SINUS_CAVITY)) {
			values = composeTableValues(values, 7, true);
			// @formatter:off
			RFCavity rfCavity = bledComponentFactory.getRFCavity(name, Double.parseDouble(values[1]) * 1E-3,
					Integer.parseInt(values[2]), Double.parseDouble(values[3]), Double.parseDouble(values[4]),
					Double.parseDouble(values[5]) * 1E-3, Integer.parseInt(values[6]) == 1, CavityType.SINUS,
					lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(rfCavity);
		} else if (line.startsWith(E_SOLENOID)) {
			values = composeTableValues(values, 4, true);
			// @formatter:off
			Solenoid solenoid = bledComponentFactory.getSolenoid(name, Double.parseDouble(values[1]) * 1E-3,
					Double.parseDouble(values[2]), Double.parseDouble(values[3]) * 1E-3, lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(solenoid);
		} else if (line.startsWith(E_THIN_LENS)) {
			values = composeTableValues(values, 4, true);
			// @formatter:off
			ThinLens thinLens = bledComponentFactory.getThinLens(name, Double.parseDouble(values[1]) * 1E-3,
					Double.parseDouble(values[2]) * 1E-3, Double.parseDouble(values[3]) * 1E-3, lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(thinLens);
		} else if (line.startsWith(E_BEND_ELE)) {
			values = composeTableValues(values, 6, true);
			// @formatter:off
			ElectrostaticBend electrostaticBend = bledComponentFactory.getElectrostaticBend(name,
					Double.parseDouble(values[1]), Double.parseDouble(values[2]) * 1E-3,
					BendType.toEnum(Integer.parseInt(values[3])), Double.parseDouble(values[4]) * 1E-3,
					Integer.parseInt(values[5]) == 1, lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(electrostaticBend);
		} else if (line.startsWith(E_SPACE_CHARGE_COMPENSATION)) {
			values = composeTableValues(values, 2, true);
			// @formatter:off
			SpaceChargeCompensation spaceChargeComp = bledComponentFactory.getSpaceChargeCompensation(name,
					Double.parseDouble(values[1]), lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(spaceChargeComp);
		} else if (line.startsWith(E_QUADRUPOLE)) {
			values = composeTableValues(values, 9, true);
			// @formatter:off
			MultipoleMagnet quadrupole = bledComponentFactory.getQuadrupole(name, Double.parseDouble(values[1]) * 1E-3,
					Double.parseDouble(values[2]), Double.parseDouble(values[3]) * 1E-3, Double.parseDouble(values[4]),
					Double.parseDouble(values[5]), Double.parseDouble(values[6]), Double.parseDouble(values[7]),
					Double.parseDouble(values[8]), lastSubsystem);
			// @formatter:on
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(quadrupole);
		} else if (line.startsWith(E_THIN_STEERING_MAGNET)) {
			values = composeTableValues(values, 7, true);
			Corrector corrector = bledComponentFactory.getCorrector(name, false, Double.parseDouble(values[1]),
					Double.parseDouble(values[2]), null, values.length > 4 && Integer.parseInt(values[4]) == 1,
					values.length > 5 ? Double.parseDouble(values[5]) : null,
					values.length > 6 ? Double.parseDouble(values[6]) : null, Double.parseDouble(values[3]),
					lastSubsystem);
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(corrector);
		} else if (line.startsWith(C_MAGNETIC_STEERER)) {
			values = composeTableValues(values, 7, true);
			Corrector corrector = bledComponentFactory.getCorrector(name, true, Double.parseDouble(values[1]),
					Double.parseDouble(values[2]), Double.parseDouble(values[3]),
					values.length > 4 && Integer.parseInt(values[4]) == 1,
					values.length > 5 ? Double.parseDouble(values[5]) : null,
					values.length > 6 ? Double.parseDouble(values[6]) : null, null, lastSubsystem);
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(corrector);
		} else if (line.startsWith(D_POSITION)) {
			BPM bpm = bledComponentFactory.getBPM(name, lastSubsystem);
			lastSubsystem = lastSubsystem + 1;
			section.addComponent(bpm);
		} else {
			writeFeedback("Element not supported: " + name + NAME_VALUE_SEPARATOR + " " + line);
		}
	}

	/**
	 * Reads a bending magnet element and it's associated edge elements and adds
	 * them to the section. BLED element of the type {@link Bend} is written as
	 * three separate objects in TraceWin: preceding edge, bend and a following
	 * edge. Parameters denoted by <code>edge1</code> refer to the preceding
	 * edge and the ones denoted by <code>edge2</code> to the following edge.
	 * 
	 * @param edge1Line
	 *            {@link String} line containing preceding edge parameter
	 *            values.
	 * @param edge1Values
	 *            <code>String[]</code> containing preceding edge parameter
	 *            values.
	 * @param bendLine
	 *            {@link String} line containing bend parameter values.
	 * @param bendValues
	 *            <code>String[]</code> bend edge parameter values.
	 * @param bendName
	 *            {@link String} name of the bend.
	 * @param edge2Line
	 *            {@link String} line containing following edge parameter
	 *            values.
	 * @param edge2Values
	 *            <code>String[]</code> containing following edge parameter
	 *            values.
	 * @param section
	 *            {@link Section} where this element belongs to.
	 */
	private void readEdge(String edge1Line, String edge1Values[], String edge1Name, String bendLine,
			String bendValues[], String bendName, String edge2Line, String edge2Values[], String edge2Name,
			Section section) {
		LOG.finest("Importing bend element " + bendName + " from line: " + bendLine);

		edge1Values = composeTableValues(edge1Values, 8, true);
		bendValues = composeTableValues(bendValues, 8, true);
		edge2Values = composeTableValues(edge2Values, 8, true);
		// @formatter:off
		Bend bend = bledComponentFactory.getBend(bendName, edge1Name, edge2Name, Double.parseDouble(bendValues[1]),
				Double.parseDouble(bendValues[2]), Integer.parseInt(bendValues[3]), Double.parseDouble(bendValues[4]),
				Orientation.toEnum(Integer.parseInt(bendValues[5])), Double.parseDouble(edge1Values[3]),
				Double.parseDouble(edge1Values[1]), Double.parseDouble(edge1Values[2]),
				Double.parseDouble(edge2Values[1]), Double.parseDouble(edge2Values[2]), lastSubsystem);
		// @formatter:on
		lastSubsystem = lastSubsystem + 1;
		section.addComponent(bend);
	}

	/**
	 * Composes an array of appropriate length. The returned array will always
	 * have the length equal or greater than the <code>length</code> parameter.
	 * If the <code>length</code> is the same or smaller than the length of the
	 * values array, the values array is returned. Otherwise an array of
	 * length </code>length</code> is returned, with values from the
	 * <code>values</code> array in the beginning and empty strings in the rest
	 * of the array.
	 * 
	 * @param values
	 *            the original values array
	 * @param length
	 *            the desired length of the array
	 * @param appendZeros
	 *            true if the missing values should be set to 0 or false if left
	 *            emptyStrings
	 * @return an extended array of length <code>length</code> or greater
	 */
	private String[] composeTableValues(String[] values, int length, boolean appendZeros) {
		if (length <= values.length)
			return values;
		String[] retVal = new String[length];
		if (appendZeros)
			Arrays.fill(retVal, "0");
		else
			Arrays.fill(retVal, "");

		System.arraycopy(values, 0, retVal, 0, values.length);
		return retVal;
	}

	/**
	 * Creates a hierarchical orders between all beam lines, slots and
	 * components.</br>
	 * Only beam lines and slots are deemed as potential parent subsystems,
	 * since they list their child elements in their descriptions.</br>
	 * {@link #findChildren(Subsystem, List)} method is used to determine their
	 * children.
	 * 
	 * @see #findChildren(Subsystem, List)
	 */
	private void buildHierarchy() {
		List<Subsystem> allSubsystems = new ArrayList<Subsystem>();
		allSubsystems.addAll(Arrays.asList(section.getBeamlines()));
		allSubsystems.addAll(Arrays.asList(section.getSlots()));
		allSubsystems.addAll(Arrays.asList(section.getComponents()));

		for (Subsystem beamline : section.getBeamlines()) {
			List<Subsystem> children = findChildren(beamline, allSubsystems);
			for (Subsystem child : children)
				child.setParentSubsystem(beamline);
		}

		for (Subsystem slot : section.getSlots()) {
			List<Subsystem> children = findChildren(slot, allSubsystems);
			for (Subsystem child : children)
				child.setParentSubsystem(slot);
		}
	}

	@SuppressWarnings("unused")
	private void checkReferenceConsistency() {
		List<String> allSubsystemNames = new ArrayList<String>();
		for (Subsystem subsystem : section.getBeamlines())
			allSubsystemNames.add(subsystem.getName().toUpperCase());
		for (Subsystem subsystem : section.getSlots())
			allSubsystemNames.add(subsystem.getName().toUpperCase());
		for (Subsystem subsystem : section.getComponents())
			allSubsystemNames.add(subsystem.getName().toUpperCase());

		for (Subsystem beamline : section.getBeamlines()) {
			if (beamline.getDescription() != null) {
				String[] parts = split(beamline.getDescription().toUpperCase());
				for (int i = 2; i < parts.length; i++)
					if (!allSubsystemNames.contains(parts[i]))
						writeFeedback("Beam line " + beamline.getName() + " is referring to a non-existing element "
								+ parts[i]);
			}
		}

		for (Subsystem slot : section.getSlots()) {
			if (slot.getDescription() != null) {
				String[] parts = split(slot.getDescription().toUpperCase());
				for (int i = 2; i < parts.length; i++)
					if (!allSubsystemNames.contains(parts[i]))
						writeFeedback("Slot " + slot.getName() + " is referring to a non-existing element " + parts[i]);
			}
		}
	}

	/**
	 * Writes the specified string to the feedback {@link PrintWriter}.
	 * 
	 * @param feedback
	 *            {@link String} feedback to be written.
	 */
	private void writeFeedback(String feedback) {
		if (responseWriter != null) {
			StringBuilder feedbackBuilder = new StringBuilder(feedback);
			feedbackBuilder.append(" (line:").append(fileLineNumber).append(")");
			responseWriter.println(feedbackBuilder.toString());
		}
	}

	/**
	 * Returns a list of specified {@link Subsystem}'s children from a list of
	 * candidates.</br>
	 * Children names are stored in the parent's description.
	 * 
	 * @param parentSubsystem
	 *            {@link Subsystem} parent subsystem with children listed in its
	 *            description.
	 * @param candidates
	 *            {@link List}<code>&lt;Subsystem&gt;</code> list of child
	 *            candidates.
	 * @return List of child {@link Subsystem}s filtered from provided list of
	 *         candidates.
	 */
	private static List<Subsystem> findChildren(Subsystem parentSubsystem, List<Subsystem> candidates) {
		List<Subsystem> children = new ArrayList<Subsystem>();
		// Parent's hierarchy information is stored in the description,
		// therefore description must not be null.
		if (parentSubsystem != null && parentSubsystem.getDescription() != null) {
			// Children names are in the description.
			List<String> childrenNames = Arrays.asList(split(parentSubsystem.getDescription().toLowerCase()));
			String parentName = parentSubsystem.getName().toLowerCase();
			// Check children candidates by name.
			for (Subsystem candidate : candidates) {
				String candidateName = candidate.getName().toLowerCase();
				// Parent is not its own child.
				if (!candidateName.equals(parentName))
					if (childrenNames.contains(candidateName))
						children.add(candidate);
			}
		}
		return children;
	}
}
