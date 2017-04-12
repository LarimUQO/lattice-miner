package fca.gui.context.table.model;

import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import fca.core.context.Context;
import fca.exception.AlreadyExistsException;
import fca.exception.LMLogger;
import fca.gui.context.table.RowHeaderTable;

/**
 * Modèle d'une table de contexte
 * @author Geneviève Roberge
 * @version 1.0
 */
public abstract class ContextTableModel extends AbstractTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -2744215895161765243L;

	protected Context context;

	protected JTable rowHeader; //Table contenant l'entête des rangées de la table

	protected JButton contextName; //Étiquette contenant le nom du contexte

	protected boolean moveRowAllowed;

	protected boolean moveColAllowed;


	/**
	 * Constructeur
	 */
	public ContextTableModel(Context c) {
		super();
		context = c;
		rowHeader = new RowHeaderTable(new RowHeaderModel(c));
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return context.getObjectCount();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return context.getAttributeCount();
	}

	public void setColumnName(int colIdx, String name) {
		try {
			context.setAttributeAt(name, colIdx);
		} catch (AlreadyExistsException e) {
			// If there, a message has already been show and log
			LMLogger.logWarning(e, false);
		}
	}

	@Override
	public String getColumnName(int colIdx) {
		return context.getAttributeAt(colIdx);
	}

	public void setRowName(int rowIdx, String name) {
		try {
			context.setObjectAt(name, rowIdx);
		} catch (AlreadyExistsException e) {
			// If there, a message has already been show and log
			LMLogger.logWarning(e, false);
		}
	}

	public String getRowName(int rowIdx) {
		return context.getObjectAt(rowIdx);
	}

	/**
	 * Modifie la table d'entête des rangées
	 * @param header La JTable contenant la nouvelle colonne d'entête pour les rangées
	 */
	public void setRowHeader(JTable header) {
		MouseListener[] listeners = rowHeader.getMouseListeners();
		if (listeners.length > 0) {
			for (int i = 0; i < listeners.length; i++)
				header.addMouseListener(listeners[i]);
		}

		rowHeader = header;
	}

	/**
	 * Permet d'obtenir la table d'entête des rangées
	 * @return La JTable contenant l'entête pour les rangées
	 */
	public JTable getRowHeader() {
		return rowHeader;
	}

	/**
	 * Permet d'indiquer si le déplacement des rangees est permis.
	 * @param b Un boolean indiquant si le déplacement est permis.
	 */
	public void setMoveRowAllowed(boolean b) {
		moveRowAllowed = b;
	}

	/**
	 * Permet d'indiquer si le déplacement des colonnes est permis.
	 * @param b Un boolean indiquant si le déplacement est permis.
	 */
	public void setMoveColAllowed(boolean b) {
		moveColAllowed = b;
	}

	/**
	 * Permet de savoir si le déplacement des rangees est permis.
	 * @return Un boolean indiquant si le déplacement est permis.
	 */
	public boolean isMoveRowAllowed() {
		return moveRowAllowed;
	}

	/**
	 * Permet de savoir si le déplacement des colonnes est permis.
	 * @return Un boolean indiquant si le déplacement est permis.
	 */
	public boolean isMoveColAllowed() {
		return moveColAllowed;
	}

	/**
	 * Permet de déplacer une rangée.
	 * @param startIdx Un int indiquant la rangée à déplacer
	 * @param endIdx Un int indiquant la destination de la rangée à déplacer
	 * @return vrai si le deplacement s'est effectué correctement, faux sinon
	 */
	public boolean hasMovedRow(int startIdx, int endIdx) {
		if (moveRowAllowed) {
			return context.hasMovedObject(startIdx, endIdx);
		} else
			return false;
	}

	/**
	 * Permet de déplacer une rangée.
	 * @param startIdx Un int indiquant la rangée à déplacer
	 * @param endIdx Un int indiquant la destination de la rangée à déplacer
	 * @return vrai si le deplacement s'est effectué correctement, faux sinon
	 */
	public boolean hasMovedCol(int startIdx, int endIdx) {
		if (moveColAllowed) {
			return context.hasMovedAttributes(startIdx, endIdx);
		} else
			return false;
	}

	/**
	 * Modifie le nom du contexte
	 * @param name String contenant le nouveau nom pour le contexte
	 */
	public void setContextName(String name) {
		contextName.setText(name);
	}

	/**
	 * Permet d'obtenir l'étiquette qui contient le nom du contexte
	 * @return Le JLabel contenant le nom du contexte
	 */
	public JButton getContextName() {
		return contextName;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIdx, int colIdx) {
		return context.getValueAt(rowIdx, colIdx);
	}

	@Override
	public boolean isCellEditable(int rowIdx, int colIdx) {
		return true;
	}

	/**
	 * @return le context de la table
	 */
	public Context getContext() {
		return context;
	}

}