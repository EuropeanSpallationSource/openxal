package edu.stanford.lcls.modelmanager;

import java.io.IOException;

import edu.stanford.lcls.modelmanager.view.ModelManagerFeature;
import edu.stanford.slac.Message.Message;

public class ExecShellCmd extends Thread {
	
	protected Process lemLite;
	
	private String[] cmd;
	
	public void run() {
		Runtime runtime = Runtime.getRuntime();
		//final String[] cmd = { "ls" };
		try {
			lemLite = runtime.exec( cmd );
			
			runtime.addShutdownHook( new Thread() {
				public void run() {
					lemLite.destroy();
				}
			} ); 
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Message.error("IO Exception: " + e.getMessage());			
			e.printStackTrace();
		}
	}
	
	public void setCommand(String[] cmd) {
		this.cmd = cmd;
	}

}
