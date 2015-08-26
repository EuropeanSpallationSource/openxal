package edu.stanford.lcls.modelmanager.util;

import java.io.File;
import java.lang.UnsupportedOperationException;

import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;

import se.lu.esss.linaclego.LinacLego;

/* Loader of Accelerator from file for different Accelerator types.
 *
 * @author Blaz Kranjc
 */
public enum AcceleratorLoader {
	OPEN_XAL("xal") {
		@Override
		public Accelerator getAccelerator(String url) {
		    return XMLDataManager.acceleratorWithUrlSpec(new File(url).toURI().toString());
		}
	},
	LINAC_LEGO("xml") {
		@Override
		public Accelerator getAccelerator(String url) {
			// Test if url contains interface otherwise presume
			// it is a file.
			if (!url.matches("^(https?|ftp|file)://.*$")) {
				url = "file://" + url;
			}
			return LinacLego.loadAcceleator(url);
		}
	};
	
	private String suffix;
	private static final int size = AcceleratorLoader.values().length;

	private AcceleratorLoader(String suffix) {
		this.suffix = suffix;
	}
	
	public abstract Accelerator getAccelerator(String url);

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

