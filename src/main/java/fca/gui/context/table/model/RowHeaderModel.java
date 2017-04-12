package fca.gui.context.table.model;

import javax.swing.table.AbstractTableModel;

import fca.core.context.Context;
import fca.exception.AlreadyExistsException;
import fca.exception.LMLogger;

/**
 * Modèle utilisé pour construire les tables d'entête des éditeurs de contexte
 * @author Geneviève Roberge
 * @version 1.0
 */
public class RowHeaderModel extends AbstractTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 389252417445714521L;
	protected Context context; //Le contexte pour lequel le modèle est construit

	/**
	 * Constructeur
	 * @param c Le Context à partir de laquelle le modèle sera construit
	 */
	public RowHeaderModel(Context c) {
		super();
		context = c;
	}

	/**
	 * Permet de connaître le nombre de rangée de cette entête
	 * @return Le int contenant le nombre de rangées
	 */
	public int getRowCount() {
		return context.getObjectCount();
	}

	/**
	 * Permet de connaître le nombre de colonnes de cette entête
	 * @return Le int contenant le nombre de colonnes (toujours 1)
	 */
	public int getColumnCount() {
		return 1;
	}

	/**
	 * Ajuste le nom d'une rangée
	 * @param rowIdx Le int contenant la position de la rangée
	 * @param name La String contenant le nouveau nom de la rangée
	 */
	public void setRowName(int rowIdx, String name) {
		try {
			context.setObjectAt(name, rowIdx);
		} catch (AlreadyExistsException e) {
			// If there, a message has already been show and log
			LMLogger.logWarning(e, false);
		}
	}

	/**
	 * Permet d'obtenir le nom d'une rangée de l'entête
	 * @param rowIdx Le int contenant la position de la rangée
	 * @return La String contenant le nom de la rangée
	 */
	public String getRowName(int rowIdx) {
		return context.getObjectAt(rowIdx);
	}

	/**
	 * Retourne la valeur d'une position dans le modèle
	 * @param rowIdx Le int contenant la rangée de la valeur recherchée
	 * @param colIdx Le int contenant la colonne de la valeur recherchée
	 * @return L'Object de type String contenant le nom de la rangée rowIdx dans le modèle
	 */
	public Object getValueAt(int rowIdx, int colIdx) {
		return context.getObjectAt(rowIdx);
	}

	/**
	 * Permet de savoir si une position donnée est modifiable par l'interface
	 * @param rowIdx Le int contenant la rangée de la position recherchée
	 * @param colIdx Le int contenant la colonne de la position recherchée
	 * @return Le boolean indiquant si la position est modifiable (toujours faux)
	 */
	@Override
	public boolean isCellEditable(int rowIdx, int colIdx) {
		return false;
	}

}