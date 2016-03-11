package edu.stanford.slac.logapi;

// TODO OPENXAL
public class MessageLogAPI {

	public static MessageLogAPI getInstance(String string) {
		return new MessageLogAPI();
	}

	public void log(String s) {
		System.err.println(s);
	}

}
