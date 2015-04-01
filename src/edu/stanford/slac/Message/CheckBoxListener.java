package edu.stanford.slac.Message;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class CheckBoxListener implements TableModelListener {

	public void tableChanged(TableModelEvent e) {

		int row = e.getFirstRow();
		int column = e.getColumn();
		MyTableModel model = (MyTableModel)e.getSource();
		Object data = model.getValueAt(row, column);

		for (PackageSettings element : Message.getPackageSettings()) { 
			if (model.getPackageSettings().getName().equals(element.getName())) { 

				if ((0 == row) && (1 == column)) element.setInfo2Console((Boolean)data);
				if ((0 == row) && (2 == column)) element.setInfo2cmLog((Boolean)data);
				if ((0 == row) && (3 == column)) element.setInfo2Swing((Boolean)data);
				if ((0 == row) && (4 == column)) element.setInfoSeverity((Boolean)data);
				if ((0 == row) && (5 == column)) element.setInfoTrace((Boolean)data);
				if ((0 == row) && (6 == column)) element.setInfoBold((Boolean)data);

				if ((1 == row) && (1 == column)) element.setWarning2Console((Boolean)data);
				if ((1 == row) && (2 == column)) element.setWarning2cmLog((Boolean)data);
				if ((1 == row) && (3 == column)) element.setWarning2Swing((Boolean)data);
				if ((1 == row) && (4 == column)) element.setWarningSeverity((Boolean)data);
				if ((1 == row) && (5 == column)) element.setWarningTrace((Boolean)data);
				if ((1 == row) && (6 == column)) element.setWarningBold((Boolean)data);

				if ((2 == row) && (1 == column)) element.setError2Console((Boolean)data);
				if ((2 == row) && (2 == column)) element.setError2cmLog((Boolean)data);
				if ((2 == row) && (3 == column)) element.setError2Swing((Boolean)data);
				if ((2 == row) && (4 == column)) element.setErrorSeverity((Boolean)data);
				if ((2 == row) && (5 == column)) element.setErrorTrace((Boolean)data);
				if ((2 == row) && (6 == column)) element.setErrorBold((Boolean)data);

				if ((3 == row) && (1 == column)) element.setFatal2Console((Boolean)data);
				if ((3 == row) && (2 == column)) element.setFatal2cmLog((Boolean)data);
				if ((3 == row) && (3 == column)) element.setFatal2Swing((Boolean)data);
				if ((3 == row) && (4 == column)) element.setFatalSeverity((Boolean)data);
				if ((3 == row) && (5 == column)) element.setFatalTrace((Boolean)data);
				if ((3 == row) && (6 == column)) element.setFatalBold((Boolean)data);

				if ((4 == row) && (1 == column)) element.setDebug2Console((Boolean)data);
				if ((4 == row) && (2 == column)) element.setDebug2cmLog((Boolean)data);
				if ((4 == row) && (3 == column)) element.setDebug2Swing((Boolean)data);
				if ((4 == row) && (4 == column)) element.setDebugSeverity((Boolean)data);
				if ((4 == row) && (5 == column)) element.setDebugTrace((Boolean)data);
				if ((4 == row) && (6 == column)) element.setDebugBold((Boolean)data);

				return;

			}
			
		}

	}

}

