package edu.stanford.slac.Message;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.stanford.slac.logapi.MessageLogAPI;
import edu.stanford.slac.swing.util.SwingWorker;


public class Message implements MouseListener {
	
	// Please see package README for instructions on using this package

	public static final int INFO = 0;
	public static final int WARNING = 1;
	public static final int ERROR = 2;
	public static final int FATAL = 3;
	public static final int DEBUG = 4;

	private static Color[] ColorCode = { 
			Color.GREEN,  // INFO
			Color.ORANGE, // WARNING
			new Color(0xff7676), // ERROR
			Color.RED,    // FATAL
			Color.BLUE }; // DEBUG

	private static String[] prefix = {
			"INFO: ",
			"WARNING: ", 
			"ERROR: ",
			"FATAL: ",
	        "DEBUG: " };

	private static Date now;
	private static Message instance = null;
	private static Object SwingWidget = null;
	private static MessageLogAPI ErrInstance = null;
	private static ArrayList<PackageSettings> p = new ArrayList();
	private CheckBoxListener myListener = new CheckBoxListener();
	private static Color[] bgColorChoices = {new Color(0xeeeeee), new Color(0xffffff)};
	private static Color bgColor = bgColorChoices[0];
	private Color TableColor = new Color(0xf5f5f5);
	private static StringBuilder sb = null;
	private static int MessageCount = 0;
	private static int SwingMessageCount = 0;
	private static int SwingMessageMax = 250;
	private static int SwingMessageDelete = 50; // Number of messages to remove from sb when SwingMessageMax exceeded

	protected Message() {
		// Exists only to defeat instantiation.
	}

	private static Message getInstance() {
		if(instance == null) {
			instance = new Message();
		}
		return instance;
	}

	public static void setSwingWidget(Object swingWidget) {
		SwingWidget = swingWidget;
		if (swingWidget instanceof JTextPane) {
			JTextPane jTextPane = (JTextPane) swingWidget;
			jTextPane.addMouseListener(getInstance());
		}
		if (swingWidget instanceof JTextArea) {
			JTextArea jTextArea = (JTextArea) swingWidget;
			jTextArea.addMouseListener(getInstance());
		}
	}

	public static Object getSwingWidget() {
		return SwingWidget;
	}
	
	protected static ArrayList<PackageSettings> getPackageSettings() {
		return p;
	}

	public static void info (String s) {
		log(s);
	}

	public static void info (String s, boolean console) {
		log (s, Message.INFO, console);
	}

	public static void info (String s, boolean console, boolean CMlog) {
		log (s, Message.INFO, console, CMlog);
	}

	public static void info (String s, boolean console, boolean CMlog, boolean toSwingWidget) {
		log (s, Message.INFO, console, CMlog, toSwingWidget);
	}

	public static void warning (String s) {
		log (s, Message.WARNING);
	}

	public static void warning (String s, boolean console) {
		log (s, Message.WARNING, console);
	}

	public static void warning (String s, boolean console, boolean CMlog) {
		log (s, Message.WARNING, console, CMlog);
	}

	public static void warning (String s, boolean console, boolean CMlog, boolean toSwingWidget) {
		log (s, Message.WARNING, console, CMlog, toSwingWidget);
	}

	public static void error (String s) {
		log (s, Message.ERROR);
	}

	public static void error (String s, boolean console) {
		log (s, Message.ERROR, console);
	}

	public static void error (String s, boolean console, boolean CMlog) {
		log (s, Message.ERROR, console, CMlog);
	}

	public static void error (String s, boolean console, boolean CMlog, boolean toSwingWidget) {
		log (s, Message.ERROR, console, CMlog, toSwingWidget);
	}

	public static void fatal (String s) {
		log (s, Message.FATAL);
	}

	public static void fatal (String s, boolean console) {
		log (s, Message.FATAL, console);
	}

	public static void fatal (String s, boolean console, boolean CMlog) {
		log (s, Message.FATAL, console, CMlog);
	}

	public static void fatal (String s, boolean console, boolean CMlog, boolean toSwingWidget) {
		log (s, Message.FATAL, console, CMlog, toSwingWidget);
	}

	public static void debug (String s) {
		log (s, Message.DEBUG);
	}

	public static void debug (String s, boolean console) {
		log (s, Message.DEBUG, console);
	}

	public static void debug (String s, boolean console, boolean CMlog) {
		log (s, Message.DEBUG, console, CMlog);
	}

	public static void debug (String s, boolean console, boolean CMlog, boolean toSwingWidget) {
		log (s, Message.DEBUG, console, CMlog, toSwingWidget);
	}

	public static void log(String s) {
		log (s, Message.INFO);
	}

	public static void log(String s, int severity) {
		if (Message.INFO == severity) log (s, severity, getPackage().isInfo2Console());
		if (Message.WARNING == severity) log (s, severity, getPackage().isWarning2Console());
		if (Message.ERROR == severity) log (s, severity, getPackage().isError2Console());
		if (Message.FATAL == severity) log (s, severity, getPackage().isFatal2Console());
		if (Message.DEBUG == severity) log (s, severity, getPackage().isDebug2Console());
	}

	public static void log(String s, Color c) {
		log (s, c, getPackage().isInfo2Console());
	}

	public static void log(String s, int severity, boolean console) {
		if (Message.INFO == severity) log (s, severity, console, getPackage().isInfo2cmLog());
		if (Message.WARNING == severity) log (s, severity, console, getPackage().isWarning2cmLog());
		if (Message.ERROR == severity) log (s, severity, console, getPackage().isError2cmLog());
		if (Message.FATAL == severity) log (s, severity, console, getPackage().isFatal2cmLog());
		if (Message.DEBUG == severity) log (s, severity, console, getPackage().isDebug2cmLog());
	}

	public static void log(String s, Color c, boolean console) {
		log(s, c, console, getPackage().isInfo2cmLog());
	}

	public static void log(String s, int severity, boolean console, boolean CMlog) {
		if (Message.INFO == severity) log (s, severity, console, CMlog, getPackage().isInfo2Swing());
		if (Message.WARNING == severity) log (s, severity, console, CMlog, getPackage().isWarning2Swing());
		if (Message.ERROR == severity) log (s, severity, console, CMlog, getPackage().isError2Swing());
		if (Message.FATAL == severity) log (s, severity, console, CMlog, getPackage().isFatal2Swing());
		if (Message.DEBUG == severity) log (s, severity, console, CMlog, getPackage().isDebug2Swing());
	}

	public static void log(String s, Color c, boolean console, boolean CMlog) {
		log(s, c, getPackage().isInfoSeverity(), console, CMlog, getPackage().isInfo2Swing(), getPackage().isInfoBold());
	}

	public static void log(String s, int severity, boolean console, boolean CMlog, boolean toSwing) {
		if (severity >= Message.INFO) {
			if (severity <= Message.DEBUG) {
				boolean trace = false;
				boolean displaySeverity = false;
				boolean bold = false;
				PackageSettings myPackageSettings = getPackage();
				if (Message.INFO == severity) {
					trace = myPackageSettings.isInfoTrace();
					displaySeverity = myPackageSettings.isInfoSeverity();
					bold = myPackageSettings.isInfoBold();
				}
				if (Message.WARNING == severity) {
					trace = myPackageSettings.isWarningTrace();
					displaySeverity = myPackageSettings.isWarningSeverity();
					bold = myPackageSettings.isWarningBold();
				}
				if (Message.ERROR == severity) {
					trace = myPackageSettings.isErrorTrace();
					displaySeverity = myPackageSettings.isErrorSeverity();
					bold = myPackageSettings.isErrorBold();
				}
				if (Message.FATAL == severity) {
					trace = myPackageSettings.isFatalTrace();
					displaySeverity = myPackageSettings.isFatalSeverity();
					bold = myPackageSettings.isFatalBold();
				}
				if (Message.DEBUG == severity) {
					trace = myPackageSettings.isDebugTrace();
					displaySeverity = myPackageSettings.isDebugSeverity();
					bold = myPackageSettings.isDebugBold();
				}

				String CallingRoutine = "";
				if (trace) {
					CallingRoutine = getCallingRoutine();
				}
				log (prefix[severity] + s + CallingRoutine, ColorCode[severity], displaySeverity, console, CMlog, toSwing, bold);
			}
		}
	}

	public static void log(final String s, Color c, final boolean displaySeverity, boolean console, boolean CMlog, boolean toSwing, final boolean bold) {
		try {
			if (null != s) {

				// Get timestamp
				SimpleDateFormat DateFormat = new SimpleDateFormat(getPackage().getDateFormat());
				now = Calendar.getInstance().getTime();
				final String ts = DateFormat.format(now.getTime());
				
				String m = ts + s;
				MessageCount++;

				if (null != SwingWidget) {
					if (toSwing) {
						final Color color = c;
						SwingWorker worker = new SwingWorker() {
							public Object construct() {
								return null;
							}
							public synchronized void finished() {
								long msStart = System.currentTimeMillis();
								SwingMessageCount++;
								try {
									if (bgColor.equals(bgColorChoices[0])) {
										bgColor = bgColorChoices[1];
									} else {
										bgColor = bgColorChoices[0];
									}
									if (SwingWidget instanceof JTextPane) {
										JTextPane jTextPane = (JTextPane) SwingWidget;
										jTextPane.setContentType("text/html");
										String boldBegin = "";
										String boldEnd = "";
										//if (Font.BOLD == jTextPane.getFont().getStyle()) {
										if (bold) {
											boldBegin = "<b>";
											boldEnd = "</b>";
										}
										String Prefix = "<html><table width='100%'>";
										String Suffix = "</table></html>";
										if (SwingMessageCount > SwingMessageMax) {
											int delEnd = 0;
											for (int i=0; i<=SwingMessageDelete; i++) delEnd = sb.indexOf("<tr", 1+delEnd);
											sb.delete(1+Prefix.length(), delEnd);
											SwingMessageCount = SwingMessageCount - SwingMessageDelete;
										}
										if (null == sb) {
											sb = new StringBuilder();
											sb.append(Prefix);
											sb.append(Suffix);
										}
										String coloredPart = s.substring(0, 1+s.indexOf(" "));
										sb.insert(sb.length()-Suffix.length(), String.format("<tr style='background-color:#%s'><td>%s%s%s</td>", Integer.toHexString(bgColor.getRGB()).substring(2), boldBegin, ts, boldEnd));
										int startIndex = 0;
										for (String each : prefix) {
											if (coloredPart.equals(each)) {
												startIndex = coloredPart.length();
												if (displaySeverity) sb.insert(sb.length()-Suffix.length(), String.format("<td style='color:#%s'>%s%s%s</td>", Integer.toHexString(color.getRGB()).substring(2), "<b>", coloredPart.subSequence(0, coloredPart.indexOf(":")), "</b>"));												
											}
										}
										if (0 == startIndex) {
											sb.insert(sb.length()-Suffix.length(), String.format("<td></td><td style='color:#%s'>%s%s%s</td></tr>", Integer.toHexString(color.getRGB()).substring(2), "<b>", s, "</b>"));
										} else {
											sb.insert(sb.length()-Suffix.length(), String.format("<td>%s%s%s</td></tr>", boldBegin, s.substring(startIndex), boldEnd));
										}
										jTextPane.setText(sb.toString());
										jTextPane.setCaretPosition(jTextPane.getDocument().getLength());
										jTextPane.setToolTipText(SwingMessageCount + " Messages. " + sb.length() + " Characters.");
										//jTextPane.repaint(); // may not be necessary
									}
									if (SwingWidget instanceof JTextArea) {
										JTextArea jTextArea = (JTextArea) SwingWidget;
										Document doc = jTextArea.getDocument();
										doc.insertString(doc.getLength(), "\n" + ts + s, null);
										jTextArea.setDocument(doc);
										jTextArea.setCaretPosition(doc.getLength());
										jTextArea.setToolTipText(SwingMessageCount + " Messages.");
										//jTextArea.repaint(); // may not be necessary
									}
								} catch (BadLocationException e) {
								}
								//System.out.println("took " + (System.currentTimeMillis()-msStart) + "ms");
							}
						};
						worker.start();
						worker.get();

					}
				}

				if (console) {

					// The following works on a Konsole & X-term (ANSI), but not an eclipse console

//					String textColor = "\033[30m";
//					if (c.equals(Color.BLACK)) textColor = "\033[30m";
//					if (c.equals(Color.ORANGE)) textColor = "\033[31m";
//					if (c.equals(Color.RED)) textColor = "\033[91m";
//					if (c.equals(Color.GREEN)) textColor = "\033[32m";
//					if (c.equals(Color.YELLOW)) textColor = "\033[33m";
//					if (c.equals(Color.BLUE)) textColor = "\033[34m";
//					if (c.equals(Color.MAGENTA)) textColor = "\033[35m";
//					if (c.equals(Color.CYAN)) textColor = "\033[36m";
//					if (c.equals(Color.WHITE)) textColor = "\033[37m";
//
//					System.out.println(textColor + m + "\033[30m");
					

					if (s.contains(prefix[Message.ERROR]) || s.contains(prefix[Message.FATAL])) {
						System.err.println(m);
						System.err.flush();
					} else {
						System.out.println(m);
						System.out.flush();
					}
				}

				if (CMlog) {
					try {
						ErrInstance = MessageLogAPI.getInstance("Message");
					} catch (Exception e) {
						// Not Fatal
					}
					if (null != ErrInstance) {
						ErrInstance.log(s);
					}
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String getCallingRoutine() {
		String CallingRoutine = "";
		try {
			Exception e = new Exception();
			StackTraceElement[] stack = e.getStackTrace();
			for (int i=0; i<stack.length; i++) {
				String className = stack[i].getClassName();
				//if (className.startsWith("edu.stanford.slac."))className = className.substring("edu.stanford.slac.".length());
				CallingRoutine = " (" + className + "." + stack[i].getMethodName() + ":" + stack[i].getLineNumber() + ")";
				if (stack[i].getClassName().startsWith("edu.stanford.slac.Message")) {
					// keep looking
				} else {
					return CallingRoutine;
				}
			}
		} catch (Exception argh) {
			// just forget it
		}
		return CallingRoutine;
	}
	
	public static PackageSettings getPackage() {
		String CallingRoutine = getCallingRoutine();
		String Method = CallingRoutine.substring(2, CallingRoutine.lastIndexOf("."));
		String Package = Method.substring(0, Method.lastIndexOf("."));
		if (Package.startsWith("edu.stanford.slac.")) Package = Package.substring("edu.stanford.slac.".length());
		for (PackageSettings element : p) {
			if (element.getName().equals(Package)) {
				return element;
			}
		}
		PackageSettings s = new PackageSettings(Package);
		p.add(s);
		return s;
	}
	
	public void mouseClicked(MouseEvent e) {
		if (MouseEvent.BUTTON3 == e.getButton()) {
			JTabbedPane tp = new JTabbedPane();
			JFrame f = new JFrame("Message Properties");
			tp.setBackground(Color.LIGHT_GRAY);
			for (PackageSettings element : p) {
				JPanel jp = new JPanel();
				jp.setBackground(Color.LIGHT_GRAY);
				jp.setName(element.getName());
				jp.setToolTipText("Message Settings for " + element.getName());
				JTextArea ta = new JTextArea();
				ta.setText("Message Settings for " + element.getName());
				ta.setBackground(Color.LIGHT_GRAY);
				ta.setForeground(Color.BLUE);
				jp.add(ta);
				JTable jt = new JTable(new MyTableModel(element));
				jt.getModel().addTableModelListener(myListener);
				jt.setBackground(TableColor);
				jt.getTableHeader().setBackground(TableColor);
				jp.add(jt.getTableHeader());
				jp.add(jt);
				tp.addTab(jp.getName(), jp);
			}
			f.setPreferredSize(new Dimension(550, 300));
			f.setLayout(new BorderLayout());
			f.add(tp);
			f.pack();
			f.setVisible(true);
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println("mouse entered");
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println("mouse exited");
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println("mouse pressed");
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println("mouse released");
		
	}

}

class MyTableModel extends AbstractTableModel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String columnNames[] = { "Msg Level", "Console", "cmLog", "Msg Window", "Severity", "Trace", "Bold" };
    private Object data[][] = {
			{"INFO", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)},	
			{"WARNING", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)},
			{"ERROR", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)},
			{"FATAL", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)},
			{"DEBUG", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)}
	};
    
    private PackageSettings packageSettings = null;

    public MyTableModel(PackageSettings ps) {
    	data[0][1] = ps.isInfo2Console();
    	data[0][2] = ps.isInfo2cmLog();
    	data[0][3] = ps.isInfo2Swing();
    	data[0][4] = ps.isInfoSeverity();
    	data[0][5] = ps.isInfoTrace();
    	data[0][6] = ps.isInfoBold();
    	
    	data[1][1] = ps.isWarning2Console();
    	data[1][2] = ps.isWarning2cmLog();
    	data[1][3] = ps.isWarning2Swing();
    	data[1][4] = ps.isWarningSeverity();
    	data[1][5] = ps.isWarningTrace();
    	data[1][6] = ps.isWarningBold();
    	
    	data[2][1] = ps.isError2Console();
    	data[2][2] = ps.isError2cmLog();
    	data[2][3] = ps.isError2Swing();
    	data[2][4] = ps.isErrorSeverity();
    	data[2][5] = ps.isErrorTrace();
    	data[2][6] = ps.isErrorBold();
    	
    	data[3][1] = ps.isFatal2Console();
    	data[3][2] = ps.isFatal2cmLog();
    	data[3][3] = ps.isFatal2Swing();
    	data[3][4] = ps.isFatalSeverity();
    	data[3][5] = ps.isFatalTrace();
    	data[3][6] = ps.isFatalBold();
    	
    	data[4][1] = ps.isDebug2Console();
    	data[4][2] = ps.isDebug2cmLog();
    	data[4][3] = ps.isDebug2Swing();
    	data[4][4] = ps.isDebugSeverity();
    	data[4][5] = ps.isDebugTrace();
    	data[4][6] = ps.isDebugBold();
    	
    	packageSettings = ps;
    }
    
    PackageSettings getPackageSettings() {
    	return packageSettings;
    }
    
    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
    	if (null == getValueAt(0, c)) return null;
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col > 0) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

}

