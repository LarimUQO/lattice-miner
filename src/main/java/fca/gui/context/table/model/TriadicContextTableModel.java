package fca.gui.context.table.model;

import fca.core.context.Context;

import javax.swing.*;

public class TriadicContextTableModel extends ContextTableModel {
	/**
	 * Constructeur
	 *
	 * @param c
	 */
	public TriadicContextTableModel(Context c) {
		super(c);
	}

	@Override
	public int getRowCount() {
		return super.getRowCount();
	}

	@Override
	public int getColumnCount() {
		return super.getColumnCount();
	}

	@Override
	public void setColumnName(int colIdx, String name) {
		super.setColumnName(colIdx, name);
	}

	@Override
	public String getColumnName(int colIdx) {
		return super.getColumnName(colIdx);
	}

	@Override
	public void setRowName(int rowIdx, String name) {
		super.setRowName(rowIdx, name);
	}

	@Override
	public String getRowName(int rowIdx) {
		return super.getRowName(rowIdx);
	}

	@Override
	public void setRowHeader(JTable header) {
		super.setRowHeader(header);
	}

	@Override
	public JTable getRowHeader() {
		return super.getRowHeader();
	}

	@Override
	public void setMoveRowAllowed(boolean b) {
		super.setMoveRowAllowed(b);
	}

	@Override
	public void setMoveColAllowed(boolean b) {
		super.setMoveColAllowed(b);
	}

	@Override
	public boolean isMoveRowAllowed() {
		return super.isMoveRowAllowed();
	}

	@Override
	public boolean isMoveColAllowed() {
		return super.isMoveColAllowed();
	}

	@Override
	public boolean hasMovedRow(int startIdx, int endIdx) {
		return super.hasMovedRow(startIdx, endIdx);
	}

	@Override
	public boolean hasMovedCol(int startIdx, int endIdx) {
		return super.hasMovedCol(startIdx, endIdx);
	}

	@Override
	public void setContextName(String name) {
		super.setContextName(name);
	}

	@Override
	public JButton getContextName() {
		return super.getContextName();
	}

	@Override
	public Object getValueAt(int rowIdx, int colIdx) {
		return super.getValueAt(rowIdx, colIdx);
	}

	@Override
	public boolean isCellEditable(int rowIdx, int colIdx) {
		return super.isCellEditable(rowIdx, colIdx);
	}

	@Override
	public Context getContext() {
		return super.getContext();
	}
}
