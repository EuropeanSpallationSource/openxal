package edu.stanford.slac.util.zplot.ui;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DevicePlotsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6263878049932899886L;

	public static final String PLOT_LABEL_PREFIX = "Show ";
	
	private final String[] dataPlotLabels;
	public final JCheckBox[] dataPlotLabelCheckBoxes;
	public JCheckBox showCartoonCheckBox;

	protected void addComponents() {
		JPanel p = new JPanel();
		for (JCheckBox checkBox : this.dataPlotLabelCheckBoxes) {
			p.add(checkBox);
		}

		setLayout(new BorderLayout(0, 10));
		add(p, BorderLayout.CENTER);
		add(this.showCartoonCheckBox, BorderLayout.SOUTH);
	}

	protected void createComponents() {
		for (int i = 0; i < this.dataPlotLabels.length; i++) {
			this.dataPlotLabelCheckBoxes[i] = new JCheckBox(PLOT_LABEL_PREFIX
					+ this.dataPlotLabels[i]);
		}
		this.showCartoonCheckBox = new JCheckBox("Show Beamline Cartoon");
		this.showCartoonCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.showCartoonCheckBox.setSelected(true);

	}

	public DevicePlotsPanel(String[] dataPlotLabels) {
		this.dataPlotLabels = dataPlotLabels;
		this.dataPlotLabelCheckBoxes = new JCheckBox[this.dataPlotLabels.length];
		createComponents();
		addComponents();
	}
}
