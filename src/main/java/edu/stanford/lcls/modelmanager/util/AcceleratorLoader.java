package edu.stanford.lcls.modelmanager.util;

import java.io.File;

import eu.ess.lt.tracewin.TraceWin;
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
	OPEN_XAL("xal") {
		@Override
		public Accelerator loadAccelerator(String url) {
		    return XMLDataManager.acceleratorWithUrlSpec(new File(url).toURI().toString());
		}
	},
	// Loader for  LinacLego accelerator files.
	LINAC_LEGO("xml") {
		@Override
		public Accelerator loadAccelerator(String url) {
			// Test if url contains interface otherwise presume
			// it is a local file.
			if (!url.matches("^(https?|ftp|file)://.*$")) {
				url = "file://" + url;
			}
			return LinacLego.loadAcceleator(url);
		}
	},//TODO put TraceWin in openxal.extensions
	TRACE_WIN("dat"){
		@Override
		public Accelerator loadAccelerator(String url) {
			return TraceWin.loadAcceleator(url);
			}
	};
	
	private String suffix;
	private static final int size = AcceleratorLoader.values().length;

	/**
	 * Class constructor.
	 * 
	 * @param suffix Suffix of file to load.
	 */
	private AcceleratorLoader(String suffix) {
		this.suffix = suffix;
	}

	/**
	 * Method dedicated to loading of Accelerator from file.
         * Every loader must implement this method.
	 * 
	 * @param url Url of accelerator file.
	 * @return Loaded Accelerator object.
	 */
	protected abstract Accelerator loadAccelerator(String url);

	/**
         * Accelerator serving method. Loads the accelerator from file
	 * and injects missing default tables.
	 * 
	 * @param url Url of accelerator file.
	 * @return Loaded Accelerator object.
	 */
	public Accelerator getAccelerator(String url) {
            Accelerator acc = loadAccelerator(url);
            DefaultTableLoader tableLoader = new DefaultTableLoader();
            tableLoader.loadDefaultTables(acc.editContext());
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

}

