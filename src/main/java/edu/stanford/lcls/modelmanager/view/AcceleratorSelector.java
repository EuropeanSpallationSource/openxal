package edu.stanford.lcls.modelmanager.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.lcls.modelmanager.util.AcceleratorLoader;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;
import xal.tools.apputils.files.FileFilterFactory;

/**
 * View for selection of accelerator file or URL.
 * 
 * @author Blaz Kranjc
 */
@SuppressWarnings("serial")
public class AcceleratorSelector extends JDialog {
	private JFrame parent;
	private JPanel container;
	private JPanel buttons;
	private JButton browseButton;
	private JButton acceptButton;
	private JButton cancelButton;
	private JTextField input;
	private Accelerator acc = null;
	private URI path;
	private JFileChooser fileChooser;
	
	public AcceleratorSelector(JFrame _parent) {
		super(_parent);
		parent = _parent;

		setTitle("Accelerator selection");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);
		setResizable(false);		

		container = new JPanel();
		
		input = new JTextField(30);
		input.setText(new File(XMLDataManager.defaultPath()).toURI().toString());
		container.add(input);

		browseButton = new JButton("Browse...");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				JFileChooser chooser = getJFileChooser(); 
				int status = chooser.showOpenDialog(parent);
				try {
					switch (status) {
					case JFileChooser.CANCEL_OPTION:
						break;
					case JFileChooser.APPROVE_OPTION:
						final File fileSelection = chooser.getSelectedFile();
						input.setText(fileSelection.toURI().toString());
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
				try {
					path = new URI(input.getText());
					final String suffix = path.getPath().substring(path.getPath().lastIndexOf(".") + 1);
					
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
				} catch (URISyntaxException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "URL error", JOptionPane.ERROR_MESSAGE);					
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
		return path.toString();
	}
	
	private JFileChooser getJFileChooser()
	{
		if (fileChooser == null) {
			fileChooser = new JFileChooser(XMLDataManager.defaultPath());
			fileChooser.setMultiSelectionEnabled(false);
			FileFilterFactory.applyFileFilters(fileChooser, AcceleratorLoader.getSupportedTypes(),
					AcceleratorLoader.getSupportedTypesDescriptions());
		}
		return fileChooser;
	}
	
}
