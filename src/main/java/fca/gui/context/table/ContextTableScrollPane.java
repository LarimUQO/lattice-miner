package fca.gui.context.table;

import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import fca.gui.context.table.model.ContextTableModel;

/**
 * Panneau avec barre de défilement pour afficher une table de contexte
 * @author Geneviève Roberge
 * @version 1.0
 */
public class ContextTableScrollPane extends JScrollPane {

	/**
	 *
	 */
	private static final long serialVersionUID = -4137280267054942122L;
	private ContextTable table; //La table contenue dans ce composant

	//private JTable rowHeader;            //L'entête pour les rangées
	//private JTableHeader columnHeader;   //L'entête pour les colonnes

	/**
	 * Constructeur
	 * @param ct La ContextTable contenue dans ce composant
	 */
	public ContextTableScrollPane(ContextTable ct) {
		super(ct, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table = ct;

		/* Assignation de l'entête des rangées */
		setRowHeaderView(((ContextTableModel) table.getModel()).getRowHeader());
		int height = (int) getRowHeader().getSize().getHeight();
		getRowHeader().setPreferredSize(new Dimension(75, height));
	}

	public ContextTable getContextTable() {
		return table;
	}
}