package edu.stanford.slac.util.zplot.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class CartoonFilterPanel extends AbstractPanel {

	private static final int ICON_HEIGHT = 30;

	private static void configure(JCheckBox checkBox, JLabel label) {
		checkBox.setEnabled(false);
		checkBox.setSelected(false);

		Dimension checkBoxSize = checkBox.getPreferredSize();
		checkBoxSize.height = ICON_HEIGHT;
		checkBox.setPreferredSize(checkBoxSize);

		Dimension labelSize = new Dimension(ICON_HEIGHT, ICON_HEIGHT);
		label.setPreferredSize(labelSize);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6263878049932899886L;
	public JCheckBox bpmCheckBox;
	public JLabel bpmIconLabel;
	public JCheckBox cavityCheckBox;
	public JLabel cavityIconLabel;
	public JCheckBox dipoleCheckBox;
	public JLabel dipoleIconLabel;
	public JCheckBox dQuadCheckBox;
	public JLabel dQuadIconLabel;
	public JCheckBox fQuadCheckBox;
	public JLabel fQuadIconLabel;
	public JCheckBox lossMonitorCheckBox;;
	public JLabel lossMonitorIconLabel;
	public JCheckBox markerCheckBox;;
	public JLabel markerIconLabel;
	public JCheckBox profileMonitorCheckBox;;
	public JLabel profileMonitorIconLabel;
	public JCheckBox solenoidCheckBox;
	public JLabel solenoidIconLabel;
	public JCheckBox toroidCheckBox;
	public JLabel toroidIconLabel;
	public JCheckBox undulatorCheckBox;
	public JLabel undulatorIconLabel;
	public JCheckBox wireScannerCheckBox;;
	public JLabel wireScannerIconLabel;
	public JCheckBox xCollimatorCheckBox;
	public JLabel xCollimatorIconLabel;
	public JCheckBox xCorCheckBox;
	public JLabel xCorIconLabel;
	public JCheckBox yCollimatorCheckBox;
	public JLabel yCollimatorIconLabel;
	public JCheckBox yCorCheckBox;
	public JLabel yCorIconLabel;

	private void addGridCell(JCheckBox checkBox, JLabel label) {
		JPanel p = new JPanel(new BorderLayout());
		p.add(checkBox, BorderLayout.CENTER);
		p.add(label, BorderLayout.WEST);
		add(p);
	}

	@Override
	protected void addComponents() {

		setLayout(new GridLayout(0, 3, 0, 3));

		addGridCell(this.bpmCheckBox, this.bpmIconLabel);
		addGridCell(this.cavityCheckBox, this.cavityIconLabel);
		addGridCell(this.dipoleCheckBox, this.dipoleIconLabel);
		addGridCell(this.dQuadCheckBox, this.dQuadIconLabel);
		addGridCell(this.fQuadCheckBox, this.fQuadIconLabel);
		addGridCell(this.lossMonitorCheckBox, this.lossMonitorIconLabel);
		addGridCell(this.markerCheckBox, this.markerIconLabel);
		addGridCell(this.profileMonitorCheckBox, this.profileMonitorIconLabel);
		addGridCell(this.solenoidCheckBox, this.solenoidIconLabel);
		addGridCell(this.toroidCheckBox, this.toroidIconLabel);
		addGridCell(this.undulatorCheckBox, this.undulatorIconLabel);
		addGridCell(this.wireScannerCheckBox, this.wireScannerIconLabel);
		addGridCell(this.xCollimatorCheckBox, this.xCollimatorIconLabel);
		addGridCell(this.xCorCheckBox, this.xCorIconLabel);
		addGridCell(this.yCollimatorCheckBox, this.yCollimatorIconLabel);
		addGridCell(this.yCorCheckBox, this.yCorIconLabel);

		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Cartoon Devices:");
		//border.setTitleJustification(TitledBorder.CENTER);
		setBorder(border);

	}

	@Override
	protected void createComponents() {

		this.bpmCheckBox = new JCheckBox("BPM");
		this.bpmIconLabel = new JLabel();
		configure(this.bpmCheckBox, this.bpmIconLabel);

		this.cavityCheckBox = new JCheckBox("Cavity");
		this.cavityIconLabel = new JLabel();
		configure(this.cavityCheckBox, this.cavityIconLabel);

		this.dipoleCheckBox = new JCheckBox("Dipole");
		this.dipoleIconLabel = new JLabel();
		configure(this.dipoleCheckBox, this.dipoleIconLabel);

		this.dQuadCheckBox = new JCheckBox("DQuad");
		this.dQuadIconLabel = new JLabel();
		configure(this.dQuadCheckBox, this.dQuadIconLabel);

		this.fQuadCheckBox = new JCheckBox("FQuad");
		this.fQuadIconLabel = new JLabel();
		configure(this.fQuadCheckBox, this.fQuadIconLabel);

		this.lossMonitorCheckBox = new JCheckBox("Loss Monitor");
		this.lossMonitorIconLabel = new JLabel();
		configure(this.lossMonitorCheckBox, this.lossMonitorIconLabel);

		this.markerCheckBox = new JCheckBox("Marker");
		this.markerIconLabel = new JLabel();
		configure(this.markerCheckBox, this.markerIconLabel);

		this.profileMonitorCheckBox = new JCheckBox("Profile Monitor");
		this.profileMonitorIconLabel = new JLabel();
		configure(this.profileMonitorCheckBox, this.profileMonitorIconLabel);

		this.solenoidCheckBox = new JCheckBox("Solenoid");
		this.solenoidIconLabel = new JLabel();
		configure(this.solenoidCheckBox, this.solenoidIconLabel);

		this.toroidCheckBox = new JCheckBox("Toroid");
		this.toroidIconLabel = new JLabel();
		configure(this.toroidCheckBox, this.toroidIconLabel);

		this.undulatorCheckBox = new JCheckBox("Undulator");
		this.undulatorIconLabel = new JLabel();
		configure(this.undulatorCheckBox, this.undulatorIconLabel);

		this.wireScannerCheckBox = new JCheckBox("Wire Scanner");
		this.wireScannerIconLabel = new JLabel();
		configure(this.wireScannerCheckBox, this.wireScannerIconLabel);

		this.xCollimatorCheckBox = new JCheckBox("XCollimator");
		this.xCollimatorIconLabel = new JLabel();
		configure(this.xCollimatorCheckBox, this.xCollimatorIconLabel);

		this.xCorCheckBox = new JCheckBox("XCor");
		this.xCorIconLabel = new JLabel();
		configure(this.xCorCheckBox, this.xCorIconLabel);

		this.yCollimatorCheckBox = new JCheckBox("YCollimator");
		this.yCollimatorIconLabel = new JLabel();
		configure(this.yCollimatorCheckBox, this.yCollimatorIconLabel);

		this.yCorCheckBox = new JCheckBox("YCor");
		this.yCorIconLabel = new JLabel();
		configure(this.yCorCheckBox, this.yCorIconLabel);
	}
}
