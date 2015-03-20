package edu.stanford.lcls.modelmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class TestSysCall extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	BorderLayout bl = new BorderLayout();
	
	JButton testBtn1 = new JButton();
	JButton testBtn2 = new JButton();

	public TestSysCall() {
		this.setLayout(bl);
		this.setSize(new Dimension(200, 100));
		this.setTitle("Test Buttons");
		testBtn1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				testBtnActionPerformed(e);
			}
		});
		
		testBtn1.setText("Model Data Upload");
		this.add(testBtn1, BorderLayout.CENTER);
		
		testBtn2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				testBtn2ActionPerformed(e);
			}
		});
		
		testBtn2.setText("Run LEMLite");
		this.add(testBtn2, BorderLayout.SOUTH);
		
		setVisible(true);
		
	}
	
	void testBtnActionPerformed(ActionEvent e) {
		ExecShellCmd esc = new ExecShellCmd();
		String[] myCmd = new String[]{"ssh", "lcls-prod02", "firefox", "https://oraweb.slac.stanford.edu/apex/slacprod/f?p=400"};
		esc.setCommand(myCmd);
		esc.start();
	}
	
	void testBtn2ActionPerformed(ActionEvent e) {
		ExecShellCmd esc = new ExecShellCmd();
		String[] myCmd = new String[]{"ssh", "-Y", "lcls-prod02", "\'ssh -Y softegr@lcls-builder /home/softegr/mlTest.sh\'"};
		esc.setCommand(myCmd);
		esc.start();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestSysCall ti = new TestSysCall();
	}

}
