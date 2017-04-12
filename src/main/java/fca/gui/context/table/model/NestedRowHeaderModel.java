package fca.gui.context.table.model;

import javax.swing.table.AbstractTableModel;

import fca.core.context.Context;
import fca.exception.AlreadyExistsException;
import fca.messages.GUIMessages;

/**
 * Modèle de table contenant l'entête de rangées d'un éditeur de contexte imbriqué Outaouais
 * @author Geneviève Roberge
 * @version 1.0
 */
public class NestedRowHeaderModel extends AbstractTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 78105587410502211L;
	protected Context context; //Le contexte représenté par ce modèle

	/**
	 * Constructeur
	 * @param ctx Le contexte à partir duquel sera construit le modèle
	 */
	public NestedRowHeaderModel(Context ctx) {
		super();
		context = ctx;
	}

	/**
	 * Permet de connaître le nombre de rangées de ce modèle
	 * @return Le int contenant le nombre de rangées de la table
	 */
	public int getRowCount() {
		/* La table contient une rangée par objet de la relation plus une rangée d'entête */
		return context.getObjectCount() + 1;
	}

	/**
	 * Permet d'obtenir le nombre de colonnes de la table
	 * @return Le int contenant le nombre de colonnes de la table (toujours 1)
	 */
	public int getColumnCount() {
		return 1;
	}

	/**
	 * Ajoute une rangée dans la relation associée au modèle
	 */
	public void addRow() {
		context.addObject();
	}

	/**
	 * Ajuste le nom de la rangée spécifiée
	 * @param rowIdx Le int contenant la position de la rangée choisie
	 * @param name La String contenant le nouveau nom de la rangée
	 */
	public void setRowName(int rowIdx, String name) throws AlreadyExistsException {
		context.setObjectAt(name, rowIdx);
	}

	/**
	 * Permet d'obtenir le nom d'une rangée donnée
	 * @param rowIdx Le int contenant la position de la rangée choisie
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
		if (rowIdx == 0)
			return new String(GUIMessages.getString("GUI.attributes")); //$NON-NLS-1$
		else
			return context.getObjectAt(rowIdx - 1);
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