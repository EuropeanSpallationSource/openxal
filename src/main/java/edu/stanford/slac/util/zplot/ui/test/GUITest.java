package edu.stanford.slac.util.zplot.ui.test;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.stanford.slac.util.zplot.ui.DevicesPanel;

public class GUITest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final JFrame f = new JFrame("Test");
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.add(new DevicesPanel());

		f.pack();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				f.setVisible(true);
			}
		});

	}

}
