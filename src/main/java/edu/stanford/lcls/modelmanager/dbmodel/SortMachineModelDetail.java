package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.Comparator;

//Sort by the "ORDINAL" or "ZPOS" column
public class SortMachineModelDetail implements Comparator<MachineModelDetail> {
	final static int UP = 1;
	final static int DOWM = -1;
	private int state;
	private int sortColumnNumber;

	public SortMachineModelDetail(String sortColumnName, int state) {
		this(state);
		sortColumnNumber = MachineModelDetail.getAllPropertyName().indexOf(
				sortColumnName);
	}

	public SortMachineModelDetail(int state) {
		this.state = state;
	}

	public SortMachineModelDetail() {
	}

	public int compare(MachineModelDetail o1, MachineModelDetail o2) {
		if (state == SortMachineModelDetail.DOWM) {
			return sortDown(o1, o2);
		}
		return sortUp(o1, o2);
	}

	private int sortUp(MachineModelDetail o1, MachineModelDetail o2) {
		Object value1 = o1.getPropertyValue(sortColumnNumber);
		Object value2 = o1.getPropertyValue(sortColumnNumber);
		if (value1 == null && value2 == null) {
			return 0;
		} else if (value1 == null) {
			return -1;
		} else if (value2 == null) {
			return 1;
		} else if (Double.valueOf(value1.toString()) < Double.valueOf(value2.toString())) {
			return -1;
		} else if (Double.valueOf(value1.toString()) > Double.valueOf(value2.toString())) {
			return 1;
		} else {
			return 0;
		}
	}

	private int sortDown(MachineModelDetail o1, MachineModelDetail o2) {
		return -1 * sortUp(o1, o2);
	}
}

