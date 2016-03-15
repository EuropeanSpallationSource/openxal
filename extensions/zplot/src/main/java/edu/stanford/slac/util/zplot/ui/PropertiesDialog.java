package edu.stanford.slac.util.zplot.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class PropertiesDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6673002712951110285L;

	public PropertiesDialog(final Component parent, final String title,
			final JPanel propertiesPanel, final Runnable applyRunnable) {
		super(JOptionPane.getFrameForComponent(parent), true);

		JButton okButton = new JButton("OK");
		JButton applyButton = new JButton("Apply");
		JButton cancelButton = new JButton("Cancel");

		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				new Thread(applyRunnable).start();
				dispose();
			}
		});

		applyButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				new Thread(applyRunnable).start();
			}

		});

		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		JPanel buttonsPanel1 = new JPanel(new GridLayout(1, 0));
		buttonsPanel1.add(okButton);
		buttonsPanel1.add(applyButton);
		buttonsPanel1.add(cancelButton);

		JPanel buttonsPanel2 = new JPanel();
		buttonsPanel2.add(buttonsPanel1);

		setLayout(new BorderLayout());
		add(propertiesPanel, BorderLayout.CENTER);
		add(buttonsPanel2, BorderLayout.SOUTH);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(title);
		setResizable(false);
		
		pack();
		setLocationRelativeTo(parent);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setVisible(true);
			}
		});

	}

}
