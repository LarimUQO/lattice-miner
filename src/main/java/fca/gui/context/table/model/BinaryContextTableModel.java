package fca.gui.context.table.model;

import fca.core.context.binary.BinaryContext;
import fca.exception.InvalidTypeException;
import fca.exception.LMLogger;

public class BinaryContextTableModel extends ContextTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 759468877481132094L;

	/**
	 * Constructeur
	 */
	public BinaryContextTableModel(BinaryContext bc) {
		super(bc);
		setMoveRowAllowed(true);
		setMoveColAllowed(true);
	}
	
	@Override
	public void setValueAt(Object value, int rowIdx, int colIdx) {
		super.setValueAt(value, rowIdx, colIdx);
		
		try {
			if (((String) value).equals("X")) //$NON-NLS-1$
				context.setValueAt(BinaryContext.TRUE, rowIdx, colIdx);
			
			/***    ***/
			else if (((String) value).equals("<-")) //$NON-NLS-1$
				context.setValueAt(BinaryContext.ARROWDOWN, rowIdx, colIdx);
			else if (((String) value).equals("->")) //$NON-NLS-1$
				context.setValueAt(BinaryContext.ARROWUP, rowIdx, colIdx);
			else if (((String) value).equals("<->")) //$NON-NLS-1$
				context.setValueAt(BinaryContext.DOUBLEARROW, rowIdx, colIdx);
			/***  ***/
			
			else
				context.setValueAt(BinaryContext.FALSE, rowIdx, colIdx);
		} catch (InvalidTypeException e) {
			// If there, a message has already been show and log
			LMLogger.logWarning(e, false);
		}
	}
	
	@Override
	public Object getValueAt(int rowIdx, int colIdx) {
		if (context.getValueAt(rowIdx, colIdx) == BinaryContext.TRUE)
			return "X"; //$NON-NLS-1$
		
		/***  ***/		
		else if (context.getValueAt(rowIdx, colIdx) == BinaryContext.ARROWDOWN)
			return "\u2199"; //<-";
		else if (context.getValueAt(rowIdx, colIdx) == BinaryContext.ARROWUP)
			return "\u2197"; //->";
		else if (context.getValueAt(rowIdx, colIdx) == BinaryContext.DOUBLEARROW)
			return "\u2195"; //<->";
		/***    ***/
		
		else	
			return ""; //$NON-NLS-1$
	}
	
	@Override
	public boolean isCellEditable(int rowIdx, int colIdx) {
		return false;
	}
}