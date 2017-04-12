package fca.gui.context.table;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import fca.core.context.Context;
import fca.core.context.valued.ValuedContext;
import fca.gui.context.table.model.ValuedContextTableModel;
import fca.gui.util.DialogBox;
import fca.messages.GUIMessages;

public class ValuedContextTable extends ContextTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2011840587589294524L;

	/**
	 * Constructeur.
	 * @param vc Le ValuedContext pour cette table
	 */
	public ValuedContextTable(ValuedContext vc) {
		super(new ValuedContextTableModel(vc));
		getTableHeader().addMouseListener(new ColumnHeaderListener());
		getRowHeader().addMouseListener(new RowHeaderListener());
	}
	
	public ValuedContextTable(ValuedContextTableModel model) {
		super(model);
		getTableHeader().addMouseListener(new ColumnHeaderListener());
		getRowHeader().addMouseListener(new RowHeaderListener());
	}
	
	@Override
	public void setModelFromContext(Context c) {
		setModel(new ValuedContextTableModel((ValuedContext) c));
		getRowHeader().repaint();
		getRowHeader().addMouseListener(new RowHeaderListener());
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	
	private class ColumnHeaderListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			int col = columnAtPoint(e.getPoint());
			if (col > -1 && e.getClickCount() == 2) {
				String newName = DialogBox.showInputQuestion(thisTable, GUIMessages.getString("GUI.enterNewAttributeName"), //$NON-NLS-1$
						GUIMessages.getString("GUI.attributeName")); //$NON-NLS-1$
				
				if (newName != null)
					thisTable.setColumnName(col, newName);
			}
		}
		
		public void mouseEntered(MouseEvent e) {
		}
		
		public void mouseExited(MouseEvent e) {
		}
		
		public void mousePressed(MouseEvent e) {
		}
		
		public void mouseReleased(MouseEvent e) {
		}
	}
	
	private class RowHeaderListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			int row = rowAtPoint(e.getPoint());
			if (row > -1 && e.getClickCount() == 2) {
				String newName = DialogBox.showInputQuestion(thisTable, GUIMessages.getString("GUI.enterNewObjectName"), GUIMessages.getString("GUI.objectName")); //$NON-NLS-1$ //$NON-NLS-2$
				
				if (newName != null)
					thisTable.setRowName(row, newName);
			}
		}
		
		public void mouseEntered(MouseEvent e) {
		}
		
		public void mouseExited(MouseEvent e) {
		}
		
		public void mousePressed(MouseEvent e) {
		}
		
		public void mouseReleased(MouseEvent e) {
		}
	}
}