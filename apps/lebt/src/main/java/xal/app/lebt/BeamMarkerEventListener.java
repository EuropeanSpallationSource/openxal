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
public interface BeamMarkerEventListener {
	/**
	 * The Node's monitored state has changed.
	 *
	 * @param agent   The Node agent with the channel whose value has changed
	 * @param record  The record of the new Node state
	 */
	public void stateChanged( BeamAgent agent, BeamMarkerRecord record );


	/**
	 * The channel's connection has changed. Either it has established a new connection or the
	 * existing connection has dropped.
	 *
	 * @param agent      The Node agent with the channel whose connection has changed
	 * @param handle     The handle of the Node channel whose connection has changed.
	 * @param connected  The channel's new connection state
	 */
	public void connectionChanged( BeamAgent agent, String handle, boolean connected );
}

