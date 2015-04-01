package edu.stanford.slac.util;

import java.awt.Color;

/**
 * General SLAC colors
 */
public class SLAColors {
	private SLAColors(){
		//not to be instantiated
	}
	
	//Default values
	public static Color OK = Color.GREEN;
	public static Color NOT_OK = Color.RED;
	public static Color OLD = Color.GRAY;
	public static Color NO_ALARM = Color.GREEN;
	public static Color MINOR_ALARM = Color.YELLOW;
	public static Color MAJOR_ALARM = Color.RED;
	public static Color INVALID_ALARM = Color.WHITE;
	public static Color OFFLINE = Color.WHITE;
	
	/**
	 * BPM colors
	 */
	public static class BPM extends SLAColors{
		private BPM(){
			//not to be instantiated
		}
	}
	
	/**
	 * Toroid colors
	 */
	public static class Toroid extends SLAColors{
		private Toroid(){
			//not to be instantiated
		}
		public static Color OK = Color.CYAN;
	}
	
	/**
	 * Profile monitor screen colors
	 */
	public static class ProfMonScreen extends SLAColors{
		private ProfMonScreen(){
			//not to be instantiated
		}
		public static Color OK = Color.MAGENTA;
		public static Color NOT_OK = Color.GRAY;
	}
	
	/**
	 * Marker colors
	 */
	public static class Marker extends SLAColors{
		private Marker(){
			//not to be instantiated
		}
		public static Color OK = Color.MAGENTA;
		public static Color NOT_OK = Color.GRAY;
	}
}
