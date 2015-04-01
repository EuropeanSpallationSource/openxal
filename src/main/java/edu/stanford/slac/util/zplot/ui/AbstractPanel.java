package edu.stanford.slac.util.zplot.ui;

import javax.swing.JPanel;

/**
 * MainPanel
 *
 * @author Sergei Chevtsov
 * @version %I%, %G%
 * @since
 */
public abstract class AbstractPanel extends JPanel{
	
	protected abstract void createComponents();
	protected abstract void addComponents();
	
	protected AbstractPanel(){
		createComponents();
		addComponents();
	}

}
