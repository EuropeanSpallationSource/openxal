package edu.stanford.lcls.modelmanager.dbmodel;

/**
 * BrowserModelListener is a notification interface for browser model events.
 * 
 * @author tap
 */
public interface BrowserModelListener {
	/**
	 * The model's connection has changed
	 * 
	 * @param model
	 *            The model whose connection changed
	 */
	public void connectionChanged(BrowserModel model);

	/**
	 * event indicating that machine model have been fetched.
	 * 
	 * @param model
	 *            the source of this event
	 */
	public void machineModelFetched(BrowserModel model,
			MachineModel[] fetchedMachineModel, MachineModel referenceMachineModel,
			MachineModelDetail[] referenceMachineModelDetail,
			MachineModelDevice[] referenceMachineModelDevice);

	/**
	 * event indicating that a model has been selected
	 * 
	 * @param controller
	 *            The controller managing selection state
	 * @param model
	 *            The model that has been selected
	 */
	public void modelSelected(BrowserModel model,
			MachineModel selectedMachineModel,
			MachineModelDetail[] selectedMachineModelDetail,
			MachineModelDevice[] selectedMachineModelDevice);
	
	/**
	 * event indicating that a model has been selected
	 * 
	 * @param controller
	 *            The controller managing selection state
	 * @param model
	 *            The model that has been selected
	 */
	public void runModel(BrowserModel model,
			MachineModel[] fetchedMachineModel,
			MachineModel runMachineModel,
			MachineModelDetail[] runMachineModelDetail,
			MachineModelDevice[] runMachineModelDevice);

}
