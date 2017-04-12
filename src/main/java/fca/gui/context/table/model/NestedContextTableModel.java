package fca.gui.context.table.model;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;

import fca.core.context.binary.BinaryContext;
import fca.core.context.nested.NestedContext;
import fca.exception.AlreadyExistsException;
import fca.exception.LMLogger;
import fca.gui.context.table.NestedRowHeaderTable;
import fca.gui.context.table.SubContextTable;
import fca.gui.util.ColorSet;
import fca.messages.GUIMessages;

/**
 * Modèle de table pour l'éditeur de contexte imbriqué
 * @author Geneviève Roberge
 * @version 1.0
 */
public class NestedContextTableModel extends ContextTableModel {
	/**
	 *
	 */
	private static final long serialVersionUID = 5560714171623605184L;

	Vector<SubContextTable> editors; //Liste des éditeurs contenus dans les colonnes de ce modèle

	NestedContext firstContext; //Contexte imbriqué contenant tous les sous-contextes

	ColorSet colors; //Ensemble de couleurs à associer aux éditeurs

	private JTable rowHeader; //Table contenant les entêtes des colonnes

	private JLabel contextLabel; //Étiquette contenant le nom du contexte imbriqué

	/**
	 * Constructeur
	 * @param nesCtx Le NestedContext à partir duquel est construit ce modèle
	 * @param levels Le int contenant le nombre de colonnes que doit contenir ce modèle
	 */
	public NestedContextTableModel(NestedContext nesCtx, int levels) {
		super(nesCtx);

		firstContext = nesCtx;
		colors = new ColorSet();

		firstContext.setName(GUIMessages.getString("GUI.level1")); //$NON-NLS-1$
		SubContextTable firstEd = new SubContextTable(firstContext, colors.getNextColor());

		/*
		 * Ajout des relations manquantes à la relation de base pour qu'elle contienne le même
		 * nombre de niveaux que l'éditeur et construction de chacun des sous-éditeurs
		 */
		editors = new Vector<SubContextTable>();
		editors.add(firstEd);
		for (int i = 1; i < levels; i++) {
			NestedContext emptyCtx = new NestedContext(new BinaryContext(firstContext.getName(), 0, 0));
			Vector<String> objects = nesCtx.getObjects();
			for (int j = 0; j < objects.size(); j++) {
				String obj = new String(objects.elementAt(j));
				try {
					emptyCtx.addObject(obj);
				} catch (AlreadyExistsException e) {
					// If there, a message has already been show and log
					LMLogger.logWarning(e, false);
				}
			}
			firstContext.addNextContext(emptyCtx);
			emptyCtx.setName(GUIMessages.getString("GUI.level") + (i + 1)); //$NON-NLS-1$
			SubContextTable emptyEd = new SubContextTable(emptyCtx, colors.getNextColor());
			editors.add(emptyEd);
		}

		contextLabel = new JLabel(firstContext.getName());
		rowHeader = new NestedRowHeaderTable(new NestedRowHeaderModel(nesCtx));
	}

	/**
	 * Constructeur
	 * @param nesCtx Le NestedContext à partir de laquelle est construit ce modèle qui contiendra le
	 *        même nombre de niveau que ce context
	 */
	public NestedContextTableModel(NestedContext nesCtx) {
		super(nesCtx);

		firstContext = nesCtx;
		firstContext.setName(GUIMessages.getString("GUI.level1")); //$NON-NLS-1$
		colors = new ColorSet();

		SubContextTable firstEd = new SubContextTable(firstContext, colors.getNextColor());

		/* Construction de chacun des sous-éditeurs */
		int level = 1;
		editors = new Vector<SubContextTable>();
		editors.add(firstEd);
		NestedContext currentContext = firstContext;
		while (currentContext.getNextContext() != null) {
			currentContext = currentContext.getNextContext();
			currentContext.setName(GUIMessages.getString("GUI.level") + (++level)); //$NON-NLS-1$
			SubContextTable currentEd = new SubContextTable(currentContext, colors.getNextColor());
			editors.add(currentEd);
		}

		contextLabel = new JLabel(nesCtx.getNestedContextName());
		rowHeader = new NestedRowHeaderTable(new NestedRowHeaderModel(nesCtx));
	}

	/**
	 * Permet d'obtenir le sous-éditeur d'une colonne donnée
	 * @param colIdx Le int contenant la position de l'éditeur recherché
	 * @return Le SubContextTable à la position indiquée
	 */
	public SubContextTable getEditorAt(int colIdx) {
		if (colIdx < editors.size())
			return editors.elementAt(colIdx);
		else
			return null;
	}

	/**
	 * Permet de connaître le nom d'une colonne choisie
	 * @param colIdx Le int contenant la position de la colonne recherchée
	 * @return La String contenant le nom de la colonne indiquée
	 */
	@Override
	public String getColumnName(int colIdx) {
		return getEditorAt(colIdx).getBinaryContext().getName();
	}

	/**
	 * Permet de connaître le nombre de colonnes que contient ce modèle
	 */
	@Override
	public int getColumnCount() {
		return editors.size();
	}

	/**
	 * Permet de connaître le nombre de rangées que contient ce modèle
	 */
	@Override
	public int getRowCount() {
		/*
		 * Un NestedContextTableModel a toujours une seule rangée contenant un sous-éditeur pour
		 * chaque niveau
		 */
		return 1;
	}

	/**
	 * Ajoute un niveau vide à la suite des autres niveaux
	 */
	public void addLevel() {
		firstContext.setNestedLattice(null);

		/*
		 * Construction d'une nouvelle relation sans attributs mais avec les objets de la relation
		 * de base
		 */
		NestedContext emptyCtx = new NestedContext(new BinaryContext(GUIMessages.getString("GUI.newContext"), 0, 0)); //$NON-NLS-1$
		int rowNb = getEditorAt(0).getRowCount();
		for (int i = 0; i < rowNb; i++) {
			String obj = firstContext.getObjectAt(i);
			try {
				emptyCtx.addObject(new String(obj));
			} catch (AlreadyExistsException e) {
				// If there, a message has already been show and log
				LMLogger.logWarning(e, false);
			}
		}

		/* Ajout de la nouvelle relation vide et construction de son sous-éditeur */
		emptyCtx.setName(GUIMessages.getString("GUI.level") + (editors.size() + 1)); //$NON-NLS-1$
		firstContext.addNextContext(emptyCtx);
		SubContextTable emptyEd = new SubContextTable(emptyCtx, colors.getNextColor());
		editors.add(emptyEd);
	}

	/**
	 * Ajoute un niveau contenant une relation à la suite des autres niveaux
	 * @param binCtx Le BinaryContext qui sera représentée dans le niveau ajouté
	 */
	public void addLevel(BinaryContext binCtx) {
		NestedContext subCtx = new NestedContext(binCtx);
		subCtx.setName(GUIMessages.getString("GUI.level") + (editors.size() + 1)); //$NON-NLS-1$

		/* Ajout de la nouvelle relation et construction de son sous-éditeur */
		firstContext.addNextContext(subCtx);
		SubContextTable subEd = new SubContextTable(subCtx, colors.getNextColor());
		editors.add(subEd);
	}

	/**
	 * Supprime le dernier niveau du modèle, s'il ne contient aucun attribut
	 */
	public void removeLevel() {
		firstContext.setNestedLattice(null);

		/* La première relation ne peut pas être enlevée */
		if (editors.size() > 1) {
			SubContextTable lastEditor = editors.elementAt(editors.size() - 1);
			if (lastEditor.getColumnCount() == 0) {
				firstContext.hasRemovedLastContext();
				editors.removeElementAt(editors.size() - 1);
				colors.backOneColor();
			}
		}
	}

	/**
	 * Déplace une colone du modèle vers une autre position
	 * @param fromIdx Le int contenant la position de la colonne à déplacer
	 * @param toIdx Le int contenant la position de destination de la colonne à déplacée
	 */
	public void moveLevel(int fromIdx, int toIdx) {
		firstContext.setNestedLattice(null);
		firstContext.moveLevel(fromIdx, toIdx);
	}

	/**
	 * Ajuste l'entête des rangées
	 * @param header La JTable contenant la nouvelle entête pour les rangées
	 */
	@Override
	public void setRowHeader(JTable header) {
		rowHeader = header;
	}

	/**
	 * Permet d'obtenir l'entête des rangée de ce modèle
	 * @return La JTable contenant l'entête des rangées
	 */
	@Override
	public JTable getRowHeader() {
		return rowHeader;
	}

	/**
	 * Ajuste le nom de la relation représentée, en ajustant l'étiquette contenant son nom
	 * @param label La String contenant le nouveau nom du contexte
	 */
	public void setContextLabel(String label) {
		contextLabel.setText(label);
		firstContext.setNestedContextName(label);
	}

	/**
	 * Permet d'obtenir l'étiquette contenant le nom de la relation imbriquée représentée dans ce
	 * modèle
	 * @return Le JLabel contenant le nom du contexte imbriqué
	 */
	public JLabel getContextLabel() {
		return contextLabel;
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.model.ContextTableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIdx, int colIdx) {
		if (colIdx < editors.size())
			return editors.elementAt(colIdx);
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object value, int rowIdx, int colIdx) {
		if ((colIdx < editors.size()) && (value instanceof SubContextTable))
			editors.set(colIdx, (SubContextTable) value);
	}

	/**
	 * Permet d'obtenir la relation qui se trouve à un niveau (une colonne) donné
	 * @param level Le int contenant le niveau du contexte recherché
	 * @return La NestedContext au niveau indiqué
	 */
	public NestedContext getNestedContextForLevel(int level) {
		if (firstContext == null)
			return null;

		NestedContext currentContext = firstContext;
		for (int i = 2; i < level; i++)
			currentContext = currentContext.getNextContext();

		return currentContext;
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.model.ContextTableModel#getContextName()
	 */
	@Override
	public JButton getContextName() {
		return new JButton(firstContext.getNestedContextName());
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.model.ContextTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIdx, int colIdx) {
		return false;
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.model.ContextTableModel#setColumnName(int, java.lang.String)
	 */
	@Override
	public void setColumnName(int colIdx, String name) {
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.model.ContextTableModel#setRowName(int, java.lang.String)
	 */
	@Override
	public void setRowName(int rowIdx, String name) {
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.model.ContextTableModel#getRowName(int)
	 */
	@Override
	public String getRowName(int rowIdx) {
		return ""; //$NON-NLS-1$
	}

}