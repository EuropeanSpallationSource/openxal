/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

/**
 *
 * @author nataliamilas
 */
public interface RepRateListener {
	/**
	 * Notification that the rep-rate has changed.
	 *
	 * @param monitor  The monitor announcing the new rep-rate.
	 * @param repRate  The new rep-rate.
	 */
	public void repRateChanged( RepRateMonitor monitor, double repRate );
}
