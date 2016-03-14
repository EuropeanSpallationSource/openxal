package edu.stanford.slac.Message;

import java.text.SimpleDateFormat;

public class PackageSettings {
	
	private String name = null;
	
	private boolean Info2Console = true;    // Do INFO messages come out on the java console?
	private boolean Warning2Console = true; // Do WARNING messages come out on the java console?
	private boolean Error2Console = true;   // Do ERROR messages come out on the java console?
	private boolean Fatal2Console = true;   // Do FATAL messages come out on the java console?
	private boolean Debug2Console = false;  // Do DEBUG messages come out on the java console?
	
	private boolean Info2Swing = true;     // Do INFO messages come out on the supplied Swing Widget?
	private boolean Warning2Swing = true;  // Do WARNING messages come out on supplied Swing Widget?
	private boolean Error2Swing = true;    // Do ERROR messages come out on the supplied Swing Widget?
	private boolean Fatal2Swing = true;    // Do FATAL messages come out on the supplied Swing Widget?
	private boolean Debug2Swing = false;   // Do DEBUG messages come out on the supplied Swing Widget?
	
	private boolean Info2cmLog = false;    // Do INFO messages come out on the java console?
	private boolean Warning2cmLog = false; // Do WARNING messages come out on the java console?
	private boolean Error2cmLog = false;   // Do ERROR messages come out on the java console?
	private boolean Fatal2cmLog = false;   // Do FATAL messages come out on the java console?
	private boolean Debug2cmLog = false;   // Do DEBUG messages come out on the java console?

	private boolean InfoSeverity = true;    // Do INFO messages display colored INFO:?
	private boolean WarningSeverity = true; // Do WARNING messages display colored WARNING:?
	private boolean ErrorSeverity = true;   // Do ERROR messages display colored ERROR:?
	private boolean FatalSeverity = true;   // Do FATAL messages display colored FATAL:?
	private boolean DebugSeverity = true;   // Do DEBUG messages display colored DEBUG:?
	
	private boolean InfoBold = false;    // Do INFO messages display in BOLD?
	private boolean WarningBold = false; // Do WARNING messages display in BOLD?
	private boolean ErrorBold = true;    // Do ERROR messages display in BOLD?
	private boolean FatalBold = true;    // Do FATAL messages display in BOLD?
	private boolean DebugBold = false;   // Do DEBUG messages display in BOLD?
	
	public boolean isInfoBold() {
		return InfoBold;
	}

	public void setInfoBold(boolean infoBold) {
		InfoBold = infoBold;
	}

	public boolean isWarningBold() {
		return WarningBold;
	}

	public void setWarningBold(boolean warningBold) {
		WarningBold = warningBold;
	}

	public boolean isErrorBold() {
		return ErrorBold;
	}

	public void setErrorBold(boolean errorBold) {
		ErrorBold = errorBold;
	}

	public boolean isFatalBold() {
		return FatalBold;
	}

	public void setFatalBold(boolean fatalBold) {
		FatalBold = fatalBold;
	}

	public boolean isDebugBold() {
		return DebugBold;
	}

	public void setDebugBold(boolean debugBold) {
		DebugBold = debugBold;
	}

	public boolean isInfoSeverity() {
		return InfoSeverity;
	}

	public void setInfoSeverity(boolean infoSeverity) {
		InfoSeverity = infoSeverity;
	}

	public boolean isWarningSeverity() {
		return WarningSeverity;
	}

	public void setWarningSeverity(boolean warningSeverity) {
		WarningSeverity = warningSeverity;
	}

	public boolean isErrorSeverity() {
		return ErrorSeverity;
	}

	public void setErrorSeverity(boolean errorSeverity) {
		ErrorSeverity = errorSeverity;
	}

	public boolean isFatalSeverity() {
		return FatalSeverity;
	}

	public void setFatalSeverity(boolean fatalSeverity) {
		FatalSeverity = fatalSeverity;
	}

	public boolean isDebugSeverity() {
		return DebugSeverity;
	}

	public void setDebugSeverity(boolean debugSeverity) {
		DebugSeverity = debugSeverity;
	}

	private boolean InfoTrace = false;    // Do INFO messages tagged with calling method?
	private boolean WarningTrace = false; // Do WARNING messages tagged with calling method?
	private boolean ErrorTrace = false;   // Do ERROR messages tagged with calling method?
	private boolean FatalTrace = false;   // Do FATAL messages tagged with calling method?
	private boolean DebugTrace = false;   // Do DEBUG messages tagged with calling method?

	private SimpleDateFormat DateFormat = null;
	private String DefaultDateFormat = "M/d H:mm:ss ";
	
	public PackageSettings() {
		
	}
	
	public PackageSettings(String PackageName) {
		name = new String(PackageName);
		//System.err.println("new Package Settings for `" + PackageName + "'");
	}
	
	public void setName(String s) {
		name = new String(s);
	}
	
	public String getName() {
		return name;
	}
	
	public void setDateFormat(String DateFormat) {
		try {
			this.DateFormat = new SimpleDateFormat(DateFormat);
		} catch (Exception e) {
			this.DateFormat = null;
		}
	}

	public String getDateFormat() {
		if (null == DateFormat) {
			DateFormat = new SimpleDateFormat(DefaultDateFormat);
		}
		return DateFormat.toPattern();
	}

	public boolean isInfo2Console() {
		return Info2Console;
	}

	public void setInfo2Console(boolean info2Console) {
		Info2Console = info2Console;
	}

	public boolean isWarning2Console() {
		return Warning2Console;
	}

	public void setWarning2Console(boolean warning2Console) {
		Warning2Console = warning2Console;
	}

	public boolean isError2Console() {
		return Error2Console;
	}

	public void setError2Console(boolean error2Console) {
		Error2Console = error2Console;
	}

	public boolean isFatal2Console() {
		return Fatal2Console;
	}

	public void setFatal2Console(boolean fatal2Console) {
		Fatal2Console = fatal2Console;
	}

	public boolean isDebug2Console() {
		return Debug2Console;
	}

	public void setDebug2Console(boolean debug2Console) {
		Debug2Console = debug2Console;
	}

	public boolean isInfo2Swing() {
		return Info2Swing;
	}

	public void setInfo2Swing(boolean info2Swing) {
		Info2Swing = info2Swing;
	}

	public boolean isWarning2Swing() {
		return Warning2Swing;
	}

	public void setWarning2Swing(boolean warning2Swing) {
		Warning2Swing = warning2Swing;
	}

	public boolean isError2Swing() {
		return Error2Swing;
	}

	public void setError2Swing(boolean error2Swing) {
		Error2Swing = error2Swing;
	}

	public boolean isFatal2Swing() {
		return Fatal2Swing;
	}

	public void setFatal2Swing(boolean fatal2Swing) {
		Fatal2Swing = fatal2Swing;
	}

	public boolean isDebug2Swing() {
		return Debug2Swing;
	}

	public void setDebug2Swing(boolean debug2Swing) {
		Debug2Swing = debug2Swing;
	}

	public boolean isInfo2cmLog() {
		return Info2cmLog;
	}

	public void setInfo2cmLog(boolean info2cmLog) {
		Info2cmLog = info2cmLog;
	}

	public boolean isWarning2cmLog() {
		return Warning2cmLog;
	}

	public void setWarning2cmLog(boolean warning2cmLog) {
		Warning2cmLog = warning2cmLog;
	}

	public boolean isError2cmLog() {
		return Error2cmLog;
	}

	public void setError2cmLog(boolean error2cmLog) {
		Error2cmLog = error2cmLog;
	}

	public boolean isFatal2cmLog() {
		return Fatal2cmLog;
	}

	public void setFatal2cmLog(boolean fatal2cmLog) {
		Fatal2cmLog = fatal2cmLog;
	}

	public boolean isDebug2cmLog() {
		return Debug2cmLog;
	}

	public void setDebug2cmLog(boolean debug2cmLog) {
		Debug2cmLog = debug2cmLog;
	}

	public boolean isInfoTrace() {
		return InfoTrace;
	}

	public void setInfoTrace(boolean infoTrace) {
		InfoTrace = infoTrace;
	}

	public boolean isWarningTrace() {
		return WarningTrace;
	}

	public void setWarningTrace(boolean warningTrace) {
		WarningTrace = warningTrace;
	}

	public boolean isErrorTrace() {
		return ErrorTrace;
	}

	public void setErrorTrace(boolean errorTrace) {
		ErrorTrace = errorTrace;
	}

	public boolean isFatalTrace() {
		return FatalTrace;
	}

	public void setFatalTrace(boolean fatalTrace) {
		FatalTrace = fatalTrace;
	}

	public boolean isDebugTrace() {
		return DebugTrace;
	}

	public void setDebugTrace(boolean debugTrace) {
		DebugTrace = debugTrace;
	}
	
}
