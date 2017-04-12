package fca.gui.context.table.model;

import fca.core.context.valued.ValuedContext;
import fca.exception.InvalidTypeException;
import fca.exception.LMLogger;

public class ValuedContextTableModel extends ContextTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7394716944766868899L;

	/**
	 * Constructeur
	 */
	public ValuedContextTableModel(ValuedContext vc) {
		super(vc);
	}
	
	@Override
	public void setValueAt(Object value, int rowIdx, int colIdx) {
		super.setValueAt(value, rowIdx, colIdx);
		
		try {
			context.setValueAt(new String((String) value), rowIdx, colIdx);
		} catch (InvalidTypeException e) {
			// If there, a message has already been show and log
			LMLogger.logWarning(e, false);
		}
	}
	
	@Override
	public Object getValueAt(int rowIdx, int colIdx) {
		return new String(context.getValueAt(rowIdx, colIdx));
	}
	
	@Override
	public boolean isCellEditable(int rowIdx, int colIdx) {
		return true;
	}
}