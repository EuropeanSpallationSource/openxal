package eu.ess.lt.parser.tracewin;

/**
 * All the TraceWin tags use in the TraceWin files.
 * 
 * @author <a href="mailto:jakob.battelino@cosylab.com">Jakob Battelino
 *         Prelog</a>
 */
public interface TraceWinTags {

	public static final String DATE_PATTERN = "EEE MMM dd HH:mm:ss yyyy";

	public static final String COMMENT_MARKER = ";";
	/** Binding of name value separator in TraceWin files. */
	public static final char NAME_VALUE_SEPARATOR = ':';
	/** Element parameters separator in TraceWin files. */
	public static final String PART_SEPARATOR = " ";
	public static final char NUMBER_MARKER = '#';
	public static final char MULTIPLE_VALUE_SEPARATOR = ',';

	public static final String TRUE_VALUE = "YES";

	public static final Double C = 299792468d;

	// file header tags
	public static final String H_DATE = "DATE";
	public static final String H_PROJECT = "PROJECT";
	public static final String H_INPUT_ENERGY = "INPUT ENERGY";
	public static final String H_OUTPUT_ENERGY = "OUTPUT ENERGY";
	public static final String H_PARTICLE = "PARTICLE";
	public static final String H_MASS = "MASS";
	public static final String H_CHARGE = "CHARGE";
	public static final String H_BEAM_CURRENT = "BEAM CURRENT";
	public static final String H_PRECEDING_FREQUENCY = "PRECEDING STRUCTURE FREQUENCY";
	public static final String H_INPUT_PHASE_ADVANCE = "INPUT PHASE ADVANCE";
	public static final String H_NUM_SECTIONS = "NBR_SECTION";

	// file header - section description
	public static final String H_SECT = "SECT:";
	public static final String H_CELL_CAV = "CELL/CAV";
	public static final String H_CAV_CRYO = "CAV/CRYO";
	public static final String H_CRYO_PER = "CRYO/PER";
	public static final String H_LENGTH = "L";
	public static final String H_BETA_G = "BG";
	public static final String H_BETA_TRANS = "BTRANS";
	public static final String H_ENERGY_OUT = "EO";

	// lattice header - section meta information
	public static final String H_SECTION = "SECTION";
	public static final String H_PERIOD_TYPE = "PERIOD TYPE";
	public static final String H_SYNC_PHASE = "SYNCH. PHASE";
	public static final String H_INPUT = "INPUT";
	public static final String H_OUTPUT = "OUTPUT";
	public static final String H_MAX = "MAX";
	public static final String H_STEP_MAX = "STEPMAX";
	public static final String H_CONTINUITY = "CONTINUITY";
	public static final String H_CONSTANT_ACCEPTANCE = "CONSTANT ACCEPTANCE";
	public static final String H_PHASE_ADVANCE = "PHASE ADV";
	public static final String H_STEP = "STEP";
	public static final String H_CAVITY = "CAVITY";
	public static final String H_FREQUENCY = "FREQ";
	public static final String H_MAX_POWER = "MAX POWER";
	public static final String H_ACC_ELECTRIC_FIELD = "EACC";
	public static final String H_ANALYTIC_MODEL = "ANALYTIC MODEL";
	public static final String H_CAVITY_FILE = "CAVITY FILE";
	public static final String H_RADIUS = "RADIUS";

	// lattice meta information
	public static final String M_SLOT = "SLOT";
	public static final String M_BEAMLINE = "BEAMLINE";
	public static final String M_MARKER = "MARKER";
	public static final String M_BEGINBEAMLINE = "BEGIN_BEAMLINE";

	// lattice commands
	public static final String C_LATTICE_BEGIN = "LATTICE";
	public static final String C_LATTICE_END = "LATTICE_END";
	public static final String C_SET_ADVANCE = "SET_ADV";

	// matching commands
	public static final String C_MIN_ENVELOPE_VARIATION = "MIN_ENV_VARIATION";
	public static final String C_MATCH_FAM_GRAD = "MATCH_FAM_GRAD";
	public static final String C_MATCH_FAM_FIELD = "MATCH_FAM_FIELD";
	public static final String C_MATCH_FAM_PHASE = "MATCH_FAM_PHASE";
	public static final String C_MATCH_FAM_LFOC = "MATCH_FAM_LFOC";
	public static final String C_MATCH_FAM_LENGTH = "MATCH_FAM_LENGTH";
	public static final String C_MIN_EMITTANCE_GROWTH = "MIN_EMIT_GROW";
	public static final String C_MIN_FIELD_VARIATION = "MIN_FIELD_VARIATION";
	public static final String C_MIN_PHASE_VARIATION = "MIN_PHASE_VARIATION";
	public static final String C_SET_ACHROMAT = "SET_ACHROMAT";
	public static final String C_SET_POSITION = "SET_POSITION";
	public static final String C_SET_BEAM_E_P = "SET_BEAM_E0_P0";
	public static final String C_SET_BEAM_PHASE_ADVANCE = "SET_BEAM_PHASE_ADV";
	public static final String C_SET_BEAM_PHASE_ERROR = "SET_BEAM_PHASE_ERROR";
	public static final String C_SET_BEAM_SEPARATION = "SET_SEPARATION";
	public static final String C_SET_BEAM_SIZE = "SET_SIZE";
	public static final String C_SET_BEAM_SIZE_MAX = "SET_SIZE_MAX";
	public static final String C_SET_SYNC_PHASE = "SET_SYNC_PHASE";
	public static final String C_SET_TWISS = "SET_TWISS";
	public static final String C_SET_BEAM_ENERGY = "SET_BEAM_ENERGY";

	public static final String C_START_ACHROMAT = "START_ACHROMAT";

	// error commands
	public static final String C_ERROR_BEAM_STAT = "ERROR_BEAM_STAT";
	public static final String C_ERROR_BEAM_DYN = "ERROR_BEAM_DYN";
	public static final String C_ERROR_BEND_NCPL_STAT = "ERROR_BEND_NCPL_STAT";
	public static final String C_ERROR_BEND_NCPL_DYN = "ERROR_BEND_NCPL_DYN";
	public static final String C_ERROR_BEND_CPL_STAT = "ERROR_BEND_CPL_STAT";
	public static final String C_ERROR_BEND_CPL_DYN = "ERROR_BEND_CPL_DYN";
	public static final String C_ERROR_CAV_NCPL_STAT = "ERROR_CAV_NCPL_STAT";
	public static final String C_ERROR_CAV_NCPL_DYN = "ERROR_CAV_NCPL_DYN";
	public static final String C_ERROR_CAV_CPL_STAT = "ERROR_CAV_CPL_STAT";
	public static final String C_ERROR_CAV_CPL_DYN = "ERROR_CAV_CPL_DYN";
	public static final String C_ERROR_RFQ_NCPL_STAT = "ERROR_RFQ_CEL_NCPL_STAT";
	public static final String C_ERROR_RFQ_NCPL_DYN = "ERROR_RFQ_CEL_NCPL_DYN";
	public static final String C_ERROR_QUAD_NCPL_STAT = "ERROR_QUAD_NCPL_STAT";
	public static final String C_ERROR_QUAD_NCPL_DYN = "ERROR_QUAD_NCPL_DYN";
	public static final String C_ERROR_QUAD_CPL_STAT = "ERROR_QUAD_CPL_STAT";
	public static final String C_ERROR_QUAD_CPL_DYN = "ERROR_QUAD_CPL_DYN";

	// change elements parameters commands
	// commands are not implemented
	public static final String C_CHOPPER = "CHOPPER";
	public static final String C_CHANGE_FREQUENCY = "FREQ";
	public static final String C_DUPLICATE_ELEMENTS = "REPEAT_ELE";
	public static final String C_MAGNETIC_STEERER = "STEERER";
	public static final String C_RFQ_COUPLING_GAP = "RFQ_GAP";
	public static final String C_RFQ_ELECTRODE_FOUR = "FOUR_RODS";
	public static final String C_RFQ_ELECTRODE_TWO = "TWOTERMS";
	public static final String C_RFQ_VANE_GEOMETRY = "RFQ_GEOM";
	public static final String C_SHIFT = "SHIFT";
	public static final String C_END = "END";
	public static final String C_SUPERPOSE_FIELD_MAP = "SUPERPOSE_MAP";
	public static final String C_CHANGE_BEAM = "CHANGE_BEAM";
	public static final String C_FIELD = "FIELD";

	// adjust commands
	public static final String C_ADJUST = "ADJUST";
	public static final String C_ADJUST_STEERER = "ADJUST_STEERER";
	public static final String C_ADJUST_STEERER_BX = "ADJUST_STEERER_BX";
	public static final String C_ADJUST_STEERER_BY = "ADJUST_STEERER_BY";
	public static final String C_ADJUST_BEAM_TWISS = "ADJUST_BEAM_TWISS";
	public static final String C_ADJUST_BEAM_EMITTANCE = "ADJUST_BEAM_EMIT";
	public static final String C_ADJUST_BEAM_CENTROID = "ADJUST_BEAM_CENTROID";
	public static final String C_ADJUST_BEAM_CURRENT = "ADJUST_BEAM_CURRENT";

	// other commands
	public static final String C_GAS_PRESSURE = "GAS";
	public static final String C_PLOT_DISTRIBUTION = "PLOT_DST";
	public static final String C_READ_MULTIPARTICLE_OUT_FILE = "READ_OUT";
	public static final String C_READ_PARTICLE_FILE = "READ_DST";
	public static final String C_PARTRAN_STEP = "PARTRAN_STEP";

	// lattice elements
	public static final String E_ALPHA_MAGNET = "ALPHA_MAGNET";
	public static final String E_APERTURE = "APERTURE";
	public static final String E_BEAM_CURRENT = "CURRENT";
	public static final String E_BEAM_ROTATION = "BEAM_ROT";
	public static final String E_BENDING_MAGNET = "BEND";
	public static final String E_BUNCHED_CAVITY = "GAP";
	public static final String E_CAVITY_MULTIGAP = "NCELLS";
	public static final String E_CHFRAME = "CHFRAME";
	public static final String E_DRIFT = "DRIFT";
	public static final String E_DTL_CELL = "DTL_CEL";
	public static final String E_EDGE_BENDING_MAGNET = "EDGE";
	public static final String E_ELECTROSTATIC_ACCELERATION = "ELECTROSTA_ACC";
	public static final String E_ELECTROSTATIC_QUADRUPOLE = "QUAD_ELE";
	public static final String E_FIELD_MAP_PATH = "FIELD_MAP_PATH";
	public static final String E_FIELD_MAP = "FIELD_MAP";
	public static final String E_FUNNELING_GAP = "FUNNEL_GAP";
	public static final String E_MULTIPOLE_FIELD_MAP = "MULTIPOLE";
	public static final String E_QUADRUPOLE = "QUAD";
	public static final String E_RFQ_CELL = "RFQ_CELL";
	public static final String E_SINUS_CAVITY = "CAVSIN";
	public static final String E_SOLENOID = "SOLENOID";
	public static final String E_SPACE_CHARGE_COMPENSATION = "SPACE_CHARGE_COMP";
	public static final String E_THIN_LENS = "THIN_LENS";
	public static final String E_THIN_MATRIX = "THIN_MATRIX";
	public static final String E_THIN_STEERING_MAGNET = "THIN_STEERING";
	public static final String E_BEND_ELE = "BEND_ELE";

	// diagnostic elements
	public static final String D_ACHROMAT = "DIAG_ACHROMAT";
	public static final String D_CURRENT = "DIAG_CURRENT";
	public static final String D_DELTA_SIZE = "DIAG_DSIZE";
	public static final String D_DELTA_SIZE2 = "DIAG_SIZE2";
	public static final String D_DELTA_SIZE3 = "DIAG_SIZE3";
	public static final String D_DIVERGENCE = "DIAG_DIVERGENCE";
	public static final String D_EMITTANCE = "DIAG_EMIT";
	public static final String D_EMITTANCE99 = "DIAG_EMIT_99";
	public static final String D_LUMINOSITY = "DIAG_LUMINOSITY";
	public static final String D_PERFECT_ENERGY = "DIAG_DENERGY";
	public static final String D_PERFECT_PHASE = "DIAG_DPHASE";
	public static final String D_PHASE = "DIAG_PHASE";
	public static final String D_POSITION = "DIAG_POSITION";
	public static final String D_SIZE = "DIAG_SIZE";
	public static final String D_SIZE_DIVERGENCE = "DIAG_SIZEP";
	public static final String D_TRANSFER_MATRIX = "DIAG_SET_MATRIX";
	public static final String D_TWISS = "DIAG_TWISS";
	public static final String D_WAIST = "DIAG_WAIST";
}
