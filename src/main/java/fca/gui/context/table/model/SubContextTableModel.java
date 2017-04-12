package fca.gui.context.table.model;

import java.util.Vector;

import fca.core.context.binary.BinaryContext;
import fca.core.context.nested.NestedContext;

/**
 * Modèle de table pour les éditeur de sous-contextes d'un éditeur de contexte imbriqué
 * @author Geneviève Roberge
 * @version 1.0
 */
public class SubContextTableModel extends BinaryContextTableModel {
	/**
	 *
	 */
	private static final long serialVersionUID = -8119831010936974951L;

	/**
	 * Constructeur
	 * @param bc Le BinaryContext qui permettra de contruire le modèle
	 */
	public SubContextTableModel(BinaryContext bc) {
		super(bc);
	}

	/**
	 * Permet d'obtenir le contexte binaire représenté dans cet éditeur
	 * @return Le BinaryContext représenté dans l'éditeur
	 */
	public BinaryContext getBinaryContext() {
		return (BinaryContext) context;
	}

	/**
	 * Permet d'obtenir le contexte imbriqué représenté dans cet éditeur
	 * @return La NestedRelation représentée dans l'éditeur
	 */
	public NestedContext getNestedContext() {
		return (NestedContext) context;
	}

	/**
	 * Permet d'obtenir l'attribut représenté dans une colonne donnée
	 * @param columnIndex La position dans l'éditeur de l'attribut recherché
	 * @return Le FormalAttribute contenant l'attribut de la colonne donnée
	 */
	public String getAttributeAt(int columnIndex) {
		String attribute = context.getAttributeAt(columnIndex);
		return attribute;
	}

	/**
	 * Permet d'obtenir la liste ordonée des relations de l'attribut dans la colonne donnée, pour
	 * chacun des objets
	 * @param columnIndex La position dans l'éditeur de l'attribut recherché
	 * @return Le Vector contenant les relation de l'attribut avec chacun des objets
	 */
	public Vector<String> getRelationsForAttributeAt(int columnIndex) {
		Vector<String> relations = new Vector<String>();
		for (int i = 0; i < getRowCount(); i++) {
			String value = context.getValueAt(i, columnIndex);
			relations.add(value);
		}
		return relations;
	}
}