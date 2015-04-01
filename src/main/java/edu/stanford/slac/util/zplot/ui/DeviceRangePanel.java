package edu.stanford.slac.util.zplot.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;

public class DeviceRangePanel extends AbstractPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -477843854929835371L;
	public JLabel firstLabel;
	public JComboBox firstComboBox;
	public JLabel lastLabel;
	public JComboBox lastComboBox;
	@Override
	protected void addComponents() {
		setLayout(new GridBagLayout());

		// 2x2
		{
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			c.fill = GridBagConstraints.NONE;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 0;
			c.insets = new Insets(0, 5, 5, 5);
			c.weightx = 0;
			c.weighty = 0;
			add(this.firstLabel, c);
		}
		{
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.gridx = 1;
			c.gridy = 0;
			c.insets = new Insets(5, 5, 5, 5);
			c.weightx = 1;
			c.weighty = 0;
			add(this.firstComboBox, c);
		}
		{
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			c.fill = GridBagConstraints.NONE;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 1;
			c.insets = new Insets(0, 5, 5, 5);
			c.weightx = 0;
			c.weighty = 0;
			add(this.lastLabel, c);
		}
		{
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.gridx = 1;
			c.gridy = 1;
			c.insets = new Insets(5, 5, 5, 5);
			c.weightx = 1;
			c.weighty = 0;
			add(this.lastComboBox, c);
		}

	}

	@Override
	protected void createComponents() {
		this.firstLabel = new JLabel("First Device");
		this.lastLabel = new JLabel("Last Device");

		this.firstComboBox = new JComboBox();
		this.lastComboBox = new JComboBox();
		
	}

}
