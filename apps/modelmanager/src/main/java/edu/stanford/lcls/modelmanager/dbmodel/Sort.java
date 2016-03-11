/**
 * @version 18-Mar-2015 Greg White, Fix for case of null date.
 */
package edu.stanford.lcls.modelmanager.dbmodel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;

import edu.stanford.lcls.modelmanager.view.ModelManagerFeature;
import edu.stanford.slac.Message.Message;


//Sort by the ID column
public class Sort implements Comparator<MachineModel> {
	final static public SimpleDateFormat machineModelDateFormat = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss");
	public final static int UP = 1;
	public final static int DOWM = -1;
	private int state;

	public Sort(int state) {
		this.state = state;
	}

	public Sort() {
	}

	public int compare(MachineModel o1, MachineModel o2) {
		if (state == Sort.DOWM) {
			try {
				return sortDown(o1, o2);
			} catch (ParseException e) {
				Message.error("Parse Exception: " + e.getMessage());			
				e.printStackTrace();
			}
		}
		try {
			return sortUp(o1, o2);
		} catch (ParseException e) {
			Message.error("Parse Exception: " + e.getMessage());			
			e.printStackTrace();
		}
		return state;
	}

	private int sortUp(MachineModel o1, MachineModel o2)
			throws ParseException {
		java.util.Date d1 = machineModelDateFormat.parse(o1
				.getPropertyValue("RUN_ELEMENT_DATE").toString());
		java.util.Date d2 = machineModelDateFormat.parse(o2
				.getPropertyValue("RUN_ELEMENT_DATE").toString());
		if (d1.after(d2)) {
			return 1;
		} else if (d1.before(d2)) {
			return -1;
		} else {
			return 0;
		}
	}

	private int sortDown(MachineModel o1, MachineModel o2)
			throws ParseException {
		
		if ( o1 == null || o2 == null ) return 0;
		Object o1date = o1.getPropertyValue("RUN_ELEMENT_DATE");
		Object o2date = o2.getPropertyValue("RUN_ELEMENT_DATE");
		if ( o1date == null || o2date == null ) return 0;
		java.util.Date d1 = machineModelDateFormat.parse(o1date.toString());
		java.util.Date d2 = machineModelDateFormat.parse(o2date.toString());
		if (d1 == null || d2 == null) return 0;
		if (d1.after(d2)) {
			return -1;
		} else if (d1.before(d2)) {
			return 1;
		} else {
			return 0;
		}
	}
}
