package edu.stanford.lcls.modelmanager.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import eu.ess.lt.tracewin.TraceWin;
import se.lu.esss.ics.jels.ImporterHelpers;
import se.lu.esss.ics.jels.model.elem.jels.JElsElementMapping;
import se.lu.esss.linaclego.LinacLego;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;


/**
 * Loader of Accelerator from file for different Accelerator types.
 *
 * @version 0.1 3 Sep 2015
 * @author Blaz Kranjc
 */
public enum AcceleratorLoader {
	// Loader for  OpenXal accelerator files. 
	OPEN_XAL("xal", "OpenXAL files") {
		@Override
		public Accelerator loadAccelerator(URI uri) {
		    return XMLDataManager.acceleratorWithUrlSpec(uri.toString());

		}
	},
	// Loader for  LinacLego accelerator files.
	LINAC_LEGO("xml", "LinacLego files") {
		@Override
		public Accelerator loadAccelerator(URI uri) {
			return LinacLego.loadAcceleatorWithInitialConditions(uri.toString(), JElsElementMapping.getInstance());			
		}
	},
	TRACE_WIN("dat", "TraceWin files"){
		@Override
		public Accelerator loadAccelerator(URI uri) {
			try {
				Accelerator accelerator = TraceWin.loadAcceleator(uri);
				ImporterHelpers.addHardcodedInitialParameters(accelerator);				
				return accelerator;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	};
	
	private String suffix, description;
	private static final int size = AcceleratorLoader.values().length;

	/**
	 * Class constructor.
	 * 
	 * @param suffix Suffix of file to load.
	 */
	private AcceleratorLoader(String suffix, String description) {
		this.suffix = suffix;
		this.description = description;
	}

	/**
	 * Method dedicated to loading of Accelerator from file.
         * Every loader must implement this method.
	 * 
	 * @param url Url of accelerator file.
	 * @return Loaded Accelerator object.
	 */
	protected abstract Accelerator loadAccelerator(URI uri);

	/**
         * Accelerator serving method. Loads the accelerator from file
	 * and injects missing default tables.
	 * 
	 * @param url Url of accelerator file.
	 * @return Loaded Accelerator object.
	 * @throws URISyntaxException 
	 */
	public Accelerator getAccelerator(URI url) {
        Accelerator acc = loadAccelerator(url);     
	    return acc;
	}

	/**
         * Method that serves the correct AcceleratorLoader implementation.
	 * 
	 * @param suffix Suffix of file to load.
	 * @return Concrete loader.
	 */
	public static AcceleratorLoader findAcceleratorBySuffix(String suffix) throws UnsupportedOperationException {
		for (AcceleratorLoader t : values()) {
			if (t.suffix.equals(suffix)) {
				return t;
			}
		}
		throw new UnsupportedOperationException("No accelerator loader for type " + suffix + ".");
	}

	/**
         * Provides all suffixes for which loaders are implemented.
	 * 
	 * @return Array of supported suffixes.
	 */
	public static String [] getSupportedTypes() {
		String[] suffixes = new String[size];
		int i = 0;
		for (AcceleratorLoader loader : values()) {
			suffixes[i] = loader.suffix;
			i++;
		}
		return suffixes;
	}

	/**
     * Provides all suffixes for which loaders are implemented.
     * 
     * @return Array of supported suffixes.
     */
	public static String [] getSupportedTypesDescriptions() {
		String[] descs = new String[size];
		int i = 0;
		for (AcceleratorLoader loader : values()) {
			descs[i] = loader.description;
			i++;
		}
		return descs;
	}
	

	
}

