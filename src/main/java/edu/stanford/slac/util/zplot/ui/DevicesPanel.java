package edu.stanford.slac.util.zplot.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class DevicesPanel extends AbstractPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6383957303371561247L;

	public DeviceRangePanel deviceRangePanel;
	public JPanel devicePlotsPanelParent;
	public CartoonFilterPanel cartoonFilterPanel;

	@Override
	protected void addComponents() {
		setLayout(new GridBagLayout());
		//3x1
		{
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.NORTH;
			c.fill = GridBagConstraints.NONE;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 0;
			c.insets = new Insets(5, 5, 5, 5);
			c.weightx = 1;
			c.weighty = 0;
			add(this.deviceRangePanel, c);
		}
		{
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.NORTH;
			c.fill = GridBagConstraints.NONE;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 1;
			c.insets = new Insets(5, 5, 5, 5);
			c.weightx = 1;
			c.weighty = 1;
			add(this.devicePlotsPanelParent, c);
		}
		{
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.NONE;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 2;
			c.insets = new Insets(0, 5, 5, 5);
			c.weightx = 1;
			c.weighty = 0;
			add(this.cartoonFilterPanel, c);
		}
	}

	@Override
	protected void createComponents() {
		this.deviceRangePanel = new DeviceRangePanel();
		this.devicePlotsPanelParent = new JPanel();		
		this.cartoonFilterPanel = new CartoonFilterPanel();
	}

}
