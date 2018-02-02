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
import xal.ca.*;


/**
 * ChannelEventListener is an interface for a listener of channel events.
 *
 * @author  tap
 */
public interface ChannelEventListener {
	/**
	 * The PV's monitored value has changed.
	 * @param channel the channel whose value has changed
	 * @param record The channel time record of the new value
	 */
	public void valueChanged(Channel channel, ChannelTimeRecord record);
	
	
	/**
	 * The channel's connection has changed.  Either it has established a new connection or
	 * the existing connection has dropped.
	 * @param channel The channel whose connection has changed.
	 * @param connected The channel's new connection state
	 */
	public void connectionChanged(Channel channel, boolean connected);
}

