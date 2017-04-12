package fca.gui.context.table;

import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import fca.core.context.binary.BinaryContext;
import fca.core.context.nested.NestedContext;
import fca.exception.LatticeMinerException;
import fca.gui.context.table.model.SubContextTableModel;
import fca.gui.util.DialogBox;

/**
 * Éditeur de contexte pour les sous-contextes d'un éditeur de contexte imbriqué
 * @author Geneviève Roberge
 * @version 1.0
 */
public class SubContextTable extends BinaryContextTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4399632443271255691L;
	private Color levelColor; //Couleur associée au niveau de la relation dans l'éditeur

	/**
	 * Constructeur
	 * @param binCtx Le BinaryContext représenté dans cet éditeur
	 * @param c La Color associée à ce contexte
	 */
	public SubContextTable(BinaryContext binCtx, Color c) {
		super(new SubContextTableModel(binCtx));
		setColumnSelectionAllowed(true);
		levelColor = c;
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.ContextTable#getCellRenderer(int, int)
	 */
	@Override
	public TableCellRenderer getCellRenderer(int rowIdx, int colIdx) {
		TableCellRenderer renderer = new TableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
														   boolean hasFocus, int row, int column) {
				String btnValue = getModel().getValueAt(row, column).toString();
				JButton cellBtn = new JButton(btnValue);
				cellBtn.setBorderPainted(false);

				if (table.isColumnSelected(column))
					cellBtn.setBackground(new Color(215, 215, 240));
				else
					cellBtn.setBackground(levelColor);

				return cellBtn;
			}
		};

		return renderer;
	}

	/**
	 * Permet d'obtenir l'attribut représenté dans une colonne donnée
	 * @param columnIdx La position dans l'éditeur de l'attribut recherché
	 * @return La String contenant l'attribut de la colonne donnée
	 */
	public String getAttributeAt(int columnIdx) {
		return ((SubContextTableModel) getModel()).getAttributeAt(convertColumnIndexToModel(columnIdx));
	}

	/**
	 * Permet d'obtenir la liste ordonée des relations de l'attribut dans la colonne donnée, pour
	 * chacun des objets
	 * @param columnIdx La position dans l'éditeur de l'attribut recherché
	 * @return Le Vector contenant les relation de l'attribut avec chacun des objets
	 */
	public Vector<String> getRelationsForAttributeAt(int columnIdx) {
		return ((SubContextTableModel) getModel()).getRelationsForAttributeAt(convertColumnIndexToModel(columnIdx));
	}

	/**
	 * Supprime la colonne à la position donnée, et donc aussi l'attribut associé à cette colonne
	 * @param columnIdx La position dans l'éditeur de l'attribut à supprimer
	 */
	public void removeColumnAt(int columnIdx) {
		BinaryContext ctx = getBinaryContext();
		ctx.removeAttribute(getAttributeAt(columnIdx));
		SubContextTableModel newModel = new SubContextTableModel(ctx);
		setModel(newModel);
		validate();
	}

	/**
	 * Ajoute une colonne attribut dans l'éditeur
	 * @param attribute La String contenant l'attribut représenté dans la nouvelle colonne
	 * @param relationValues La liste ordonnée des relations de l'attributs avec les objets du
	 *        contexte
	 * @return Le boolean indiquant si l'attribut a été ajouté
	 */
	public boolean hasAddedColumn(String attribute, Vector<String> relationValues) {
		BinaryContext ctx = getBinaryContext();

		/* Un attribut ne peut apparaître qu'une seule fois dans une relatin */
		if (ctx.getAttributes().contains(attribute))
			return false;

		/* Ajout de l'attribut et de ses relations avec les objets dans la relation binaire */
		String object = ""; //$NON-NLS-1$
		String value = ""; //$NON-NLS-1$

		try {
			ctx.addAttribute(attribute);
			for (int i = 0; i < relationValues.size(); i++) {
				object = ctx.getObjectAt(i);
				value = relationValues.elementAt(i);
				ctx.setValueAt(value, object, attribute);
			}
		} catch (LatticeMinerException e) {
			DialogBox.showMessageError(this, e);
		}

		/* Reconstruction du modèle de l'éditeur en fonction du nouveau contexte */
		SubContextTableModel newModel = new SubContextTableModel(ctx);
		setModel(newModel);
		validate();
		return true;
	}

	/**
	 * Permet d'obtenir le contexte binaire représenté dans cet éditeur
	 * @return Le BinaryContext contenu dans le NestedContext représenté dans l'éditeur
	 */
	public BinaryContext getBinaryContext() {
		return ((SubContextTableModel) getModel()).getBinaryContext();
	}

	/**
	 * Permet d'obtenir le contexte imbriqué représenté dans cet éditeur
	 * @return Le NestedContext représenté dans l'éditeur
	 */
	public NestedContext getNestedContext() {
		return ((SubContextTableModel) getModel()).getNestedContext();
	}
}