package edu.stanford.lcls.modelmanager.dbmodel;

/**
 * BrowserModelListener is a notification interface for browser model events.
 * 
 * @author tap
 */
public interface BrowserModelListener {
	/**
	 * The model's state has changed
	 * 
	 * @param model
	 *            The model whose state changed
	 */
	public void modelStateChanged(BrowserModel model);
}
