package edu.stanford.lcls.modelmanager.util;

import java.io.File;

import eu.ess.lt.tracewin.TraceWin;
import se.lu.esss.linaclego.LinacLego;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;


/* Loader of Accelerator from file for different Accelerator types.
 *
 * @author Blaz Kranjc
 */
public enum AcceleratorLoader {
	/* Loader for  OpenXal accelerator files*/
	OPEN_XAL("xal") {
		@Override
		public Accelerator loadAccelerator(String url) {
		    return XMLDataManager.acceleratorWithUrlSpec(new File(url).toURI().toString());
		}
	},
	/* Loader for  LinacLego accelerator files*/
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

	private AcceleratorLoader(String suffix) {
		this.suffix = suffix;
	}
	
	protected abstract Accelerator loadAccelerator(String url);

	public Accelerator getAccelerator(String url) {
            Accelerator acc = loadAccelerator(url);
            DefaultTableLoader tableLoader = new DefaultTableLoader();
            tableLoader.loadDefaultTables(acc.editContext());
	    return acc;
	}

	public static AcceleratorLoader findAcceleratorBySuffix(String suffix) throws UnsupportedOperationException {
		for (AcceleratorLoader t : values()) {
			if (t.suffix.equals(suffix)) {
				return t;
			}
		}
		throw new UnsupportedOperationException("No accelerator loader for type " + suffix + ".");
	}

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

