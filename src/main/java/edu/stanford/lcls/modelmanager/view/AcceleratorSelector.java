package edu.stanford.lcls.modelmanager.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.UnsupportedOperationException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import xal.smf.Accelerator;
import xal.tools.apputils.files.FileFilterFactory;

import edu.stanford.lcls.modelmanager.ModelManagerDocument;
import edu.stanford.lcls.modelmanager.util.AcceleratorLoader;

/**
 * View for selection of accelerator file or URL.
 * 
 * @author Blaz Kranjc
 */
public class AcceleratorSelector extends JDialog {
	private JFrame parent;
	private JPanel container;
	private JPanel buttons;
	private JButton browseButton;
	private JButton acceptButton;
	private JButton cancelButton;
	private JTextField input;
	private Accelerator acc = null;
	private String path;

	public AcceleratorSelector(JFrame _parent) {
		super(_parent);
		parent = _parent;

		setTitle("Accelerator selection");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);
		setResizable(false);		

		container = new JPanel();
		
		input = new JTextField(30);
		container.add(input);

		browseButton = new JButton("Browse...");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);
				FileFilterFactory.applyFileFilters(chooser, AcceleratorLoader.getSupportedTypes(),
						AcceleratorLoader.getSupportedTypesDescriptions());
				int status = chooser.showOpenDialog(parent);
				try {
					switch (status) {
					case JFileChooser.CANCEL_OPTION:
						break;
					case JFileChooser.APPROVE_OPTION:
						final File fileSelection = chooser.getSelectedFile();
						final String filePath = fileSelection.getAbsolutePath();
						input.setText(filePath);
						break;
					case JFileChooser.ERROR_OPTION:
						break;
					}
				} catch (Exception exception) {
				}
			}
		});
		container.add(browseButton);

		buttons = new JPanel();
		
		acceptButton = new JButton("Accept");
		acceptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				path = input.getText();
				final String suffix = path.substring(path.lastIndexOf(".") + 1);
				try {
					AcceleratorLoader loader = AcceleratorLoader.findAcceleratorBySuffix(suffix);
					acc = loader.getAccelerator(path);
					if (acc != null) {
						// Accelerator selected we can let go of this dialog
						dispose();
					} else {
						JOptionPane.showMessageDialog(null, "Accelerator load failed!", "Load error",
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (UnsupportedOperationException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Load error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		buttons.add(acceptButton);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});
		buttons.add(cancelButton);		
		add(container);
		add(buttons, BorderLayout.SOUTH);
		pack();
	}

	public Accelerator getSelectedAccelerator() {
		return acc;
	}

	public String getAcceleratorPath() {
		return path;
	}
}
