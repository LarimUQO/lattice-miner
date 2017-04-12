package fca.gui.rule;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class JTableCreatingByVector {
	public static void main(String args[]) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Vector<String> rowOne = new Vector<String>();
		rowOne.addElement("Row1-Column1");
		rowOne.addElement("Row1-Column2");
		rowOne.addElement("Row1-Column3");

		Vector<String> rowTwo = new Vector<String>();
		rowTwo.addElement("Row2-Column1");
		rowTwo.addElement("Row2-Column2");
		rowTwo.addElement("Row2-Column3");

		Vector<Vector> rowData = new Vector<Vector>();
		rowData.addElement(rowOne);
		rowData.addElement(rowTwo);

		Vector<String> columnNames = new Vector<String>();
		columnNames.addElement("Column One");
		columnNames.addElement("Column Two");
		columnNames.addElement("Column Three");
		JTable table = new JTable(rowData, columnNames);

		JScrollPane scrollPane = new JScrollPane(table);
		frame.add(scrollPane, BorderLayout.CENTER);
		frame.setSize(300, 150);
		frame.setVisible(true);

	}
}
