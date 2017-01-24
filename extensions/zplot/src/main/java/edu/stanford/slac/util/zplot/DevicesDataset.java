package edu.stanford.slac.util.zplot;

import java.util.Arrays;

import org.jfree.data.DomainOrder;
import org.jfree.data.xy.AbstractIntervalXYDataset;

import edu.stanford.slac.util.zplot.model.Device;

public class DevicesDataset extends AbstractIntervalXYDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3573937737216803429L;

	private final Device[] devices;

	public DevicesDataset(Device[] devices) {
		if (devices == null)
			throw new IllegalArgumentException("Null argument");
		Arrays.sort(devices);
		this.devices = devices;
	}

	@Override
	public int getSeriesCount() {
		return 1;
	}

	@Override
	public Comparable<?> getSeriesKey(int series) {
		return this.toString();
	}

	public int getItemCount(int series) {
		return this.devices.length;
	}

	public Number getX(int series, int item) {
		return getDevice(item).getZ();
	}

	public Number getY(int series, int item) {
		return getDevice(item).getY();
	}
	
	public Device getDevice(int item){
		return this.devices[item];
	}

	@Override
	public DomainOrder getDomainOrder() {
		return DomainOrder.ASCENDING;
	}

	public Number getEndX(int series, int item) {
		return getStartX(series, item);
	}

	public Number getEndY(int series, int item) {
		return getY(series, item);
	}

	public Number getStartX(int series, int item) {
		return getX(series, item);
	}

	public Number getStartY(int series, int item) {
		return 0;
	}


}
