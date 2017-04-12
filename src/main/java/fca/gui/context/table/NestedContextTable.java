package fca.gui.context.table;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import fca.core.context.Context;
import fca.core.context.binary.BinaryContext;
import fca.core.context.nested.NestedContext;
import fca.gui.context.table.model.NestedContextTableModel;
import fca.gui.util.DialogBox;
import fca.messages.GUIMessages;

/**
 * Éditeur de contexte imbriqué
 * @author Geneviève Roberge
 * @version 1.0
 */
public class NestedContextTable extends ContextTable implements MouseListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -3787630860999590751L;

	private Vector<JScrollPane> panelsList; //Liste des panneaux contenant les sous-contextes du la context imbriqué

	private int rowHeight; //Hauteur d'une rangée de la table

	private boolean isCTRLPressed; //Indique si la touche CTRL est enfoncée

	/* Popup menu pour les changements de position */

	private JPopupMenu attributesMenu; //Menu pour changer les attributs de niveau

	private JPopupMenu levelsMenu; //Menu pour changer le niveau d'un sous-contexte

	/**
	 * Constructeur
	 * @param nesCtx La NestedContext à afficher dans l'éditeur
	 * @param nbLevels Le int contenant le nombre de niveaux que doit contenir l'éditeur
	 */
	public NestedContextTable(NestedContext nesCtx, int nbLevels) {
		super(new NestedContextTableModel(nesCtx, nbLevels));

		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setCellSelectionEnabled(true);
		setColumnSelectionAllowed(true);
		setRowSelectionAllowed(false);
		getTableHeader().setReorderingAllowed(false);

		panelsList = new Vector<JScrollPane>();
		//addMouseListener(this);  -> deja ajoute dans le parent ContextTable
		getTableHeader().addMouseListener(this);

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new CtrlKeyDetector());
		isCTRLPressed = false;

		/*
		 * Création de chacun des panneaux de sous-contexte du contexte imbriqué, en les ajustant à
		 * la bonne largeur selon leur nombre d'attributs
		 */
		for (int i = 0; i < getModel().getColumnCount(); i++) {
			SubContextTable tableCell = (SubContextTable) getValueAt(0, i);
			int totalWidth = tableCell.getColumnModel().getTotalColumnWidth();
			TableColumn currentColumn = getColumnModel().getColumn(i);
			currentColumn.setPreferredWidth(totalWidth);
			currentColumn.setResizable(false);
			tableCell.getTableHeader().setReorderingAllowed(false);

			JScrollPane currentPanel = new JScrollPane(tableCell, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			panelsList.add(currentPanel);
		}

		/* Obtention de la largeur des rangées */
		SubContextTable firstCell = (SubContextTable) getValueAt(0, 0);
		if (firstCell != null)
			rowHeight = (firstCell.getRowCount() + 1) * (firstCell.getRowHeight());
		else
			rowHeight = 0;

		/* Création du popup menu pour les attributs */
		attributesMenu = createAttributesPopupMenu();
		add(attributesMenu);
		attributesMenu.addMouseListener(this);

		/* Création du popup menu pour les niveaux */
		levelsMenu = createLevelsPopupMenu();
		add(levelsMenu);
		levelsMenu.addMouseListener(this);
	}

	/**
	 * Constructeur
	 * @param nesCtx Le NestedContext qui sera affiché dans cet éditeur qui aura le même nombre de
	 *        niveaux que le contexte
	 */
	public NestedContextTable(NestedContext nesCtx) {
		super(new NestedContextTableModel(nesCtx));

		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setCellSelectionEnabled(true);
		setColumnSelectionAllowed(true);
		setRowSelectionAllowed(false);
		getTableHeader().setReorderingAllowed(false);

		panelsList = new Vector<JScrollPane>();
		//addMouseListener(this); -> deja ajoute dans le parent ContextTable
		getTableHeader().addMouseListener(this);

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new CtrlKeyDetector());
		isCTRLPressed = false;

		/*
		 * Création de chacun des panneaux de sous-contexte du contexte imbriqué, en les ajustant à
		 * la bonne largeur selon leur nombre d'attributs
		 */
		for (int i = 0; i < getModel().getColumnCount(); i++) {
			SubContextTable tableCell = (SubContextTable) getValueAt(0, i);
			int totalWidth = tableCell.getColumnModel().getTotalColumnWidth();
			TableColumn currentColumn = getColumnModel().getColumn(i);
			currentColumn.setPreferredWidth(totalWidth);
			currentColumn.setResizable(false);
			tableCell.getTableHeader().setReorderingAllowed(false);

			JScrollPane currentPanel = new JScrollPane(tableCell, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			panelsList.add(currentPanel);
		}

		/* Obtention de la largeur des rangées */
		SubContextTable firstCell = (SubContextTable) getValueAt(0, 0);
		if (firstCell != null)
			rowHeight = (firstCell.getRowCount() + 1) * (firstCell.getRowHeight());
		else
			rowHeight = 0;

		/* Création du popup menu pour les attributs */
		attributesMenu = createAttributesPopupMenu();
		add(attributesMenu);
		attributesMenu.addMouseListener(this);

		/* Création du popup menu pour les niveaux */
		levelsMenu = createLevelsPopupMenu();
		add(levelsMenu);
		levelsMenu.addMouseListener(this);
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.ContextTable#getCellRenderer(int, int)
	 */
	@Override
	public TableCellRenderer getCellRenderer(int rowIdx, int colIdx) {
		TableCellRenderer renderer = new TableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
														   boolean hasFocus, int row, int column) {
				//SubContextTable editor = (SubContextTable)value;
				JScrollPane cellPanel = panelsList.elementAt(convertColumnIndexToModel(column));
				return cellPanel;
			}
		};

		return renderer;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JTable#getRowHeight()
	 */
	@Override
	public int getRowHeight() {
		return rowHeight;
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.ContextTable#setRowHeader(javax.swing.JTable)
	 */
	@Override
	public void setRowHeader(JTable header) {
		((NestedContextTableModel) getModel()).setRowHeader(header);
	}

	/**
	 * Permet d'obtenir la table d'entête des rangées
	 * @return La JTable contenant les nouvelles entêtes des rangées
	 */
	@Override
	public JTable getRowHeader() {
		return ((NestedContextTableModel) getModel()).getRowHeader();
	}

	/**
	 * Ajoute un niveau vide à la suite des autres niveaux de l'éditeur
	 */
	public void addLevel() {
		/* Ajout d'une colonne dans la table */
		((NestedContextTableModel) getModel()).addLevel();
		TableColumn newColumn = new TableColumn(getModel().getColumnCount() - 1);
		addColumn(newColumn);
		validate();

		/* Ajustement de la largeur de la nouvelle colonne */
		SubContextTable tableCell = (SubContextTable) getValueAt(0, getModel().getColumnCount() - 1);
		int totalWidth = tableCell.getColumnModel().getTotalColumnWidth();
		TableColumn currentColumn = getColumnModel().getColumn(getModel().getColumnCount() - 1);
		currentColumn.setPreferredWidth(totalWidth);
		currentColumn.setResizable(false);
		tableCell.validate();

		/* Ajout du nouveau sous-éditeur dans la liste des sous-éditeurs */
		JScrollPane currentPanel = new JScrollPane(tableCell, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panelsList.add(currentPanel);

		/* Création des popup menus pour les attributs et les niveaux */
		attributesMenu = createAttributesPopupMenu();
		levelsMenu = createLevelsPopupMenu();
	}

	/**
	 * Ajoute un niveau à la suite des niveaux exitants, avec un contexte à l'intérieur
	 * @param binCtx La BinaryContext qui sera représentée dans le nouveau niveau
	 */
	public void addLevel(BinaryContext binCtx) {
		/* Ajout d'une nouvelle colonne contenant le niveau */
		((NestedContextTableModel) getModel()).addLevel(binCtx);
		TableColumn newColumn = new TableColumn(getModel().getColumnCount() - 1);
		addColumn(newColumn);
		validate();

		/* Ajustement de la largeur de la nouvelle colonne */
		SubContextTable tableCell = (SubContextTable) getValueAt(0, getModel().getColumnCount() - 1);
		int totalWidth = tableCell.getColumnModel().getTotalColumnWidth();
		TableColumn currentColumn = getColumnModel().getColumn(getModel().getColumnCount() - 1);
		currentColumn.setPreferredWidth(totalWidth);
		currentColumn.setResizable(false);
		tableCell.validate();

		/* Ajout du nouveau sous-éditeur dans la liste des sous-éditeurs */
		JScrollPane currentPanel = new JScrollPane(tableCell, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panelsList.add(currentPanel);

		/* Création des popup menus pour les attributs et les niveaux */
		attributesMenu = createAttributesPopupMenu();
		levelsMenu = createLevelsPopupMenu();

		/*
		 * Obtention de la largeur des rangées (qui aura augmenté si l'ajout du nouveau contexte a
		 * ajoute des objets dans le contexte imbriqué)
		 */
		SubContextTable firstCell = (SubContextTable) getValueAt(0, 0);
		if (firstCell != null)
			rowHeight = (firstCell.getRowCount() + 1) * (firstCell.getRowHeight());
		else
			rowHeight = 0;
	}

	/**
	 * Enlève le dernier niveau de l'éditeur, s'il ne contient aucun attribut
	 */
	public void removeLevel() {
		/* Obtention du nombre de colonne du dernier niveau */
		TableColumn lastColumn = getColumnModel().getColumn(getColumnCount() - 1);
		int nbColumns = getModel().getColumnCount();
		((NestedContextTableModel) getModel()).removeLevel();

		if (getModel().getColumnCount() == nbColumns) {
			DialogBox.showMessageWarning(this, GUIMessages.getString("GUI.levelMustBeEmptyToBeRemoved"), GUIMessages.getString("GUI.cannotRemovedLevel")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			removeColumn(lastColumn);
			validate();

			panelsList.removeElementAt(panelsList.size() - 1);
			attributesMenu = createAttributesPopupMenu();
			levelsMenu = createLevelsPopupMenu();
		}
	}

	/**
	 * Ajuste le nom du contexte imbriqué, en l'écrivant dans l'étiquette prévue à cet effet
	 * @param label La String contenant le nouveau nom du contexte
	 */
	public void setContextLabel(String label) {
		((NestedContextTableModel) getModel()).setContextLabel(label);
	}

	/**
	 * Permet d'obtenir l'étiquette contenant le nom du contexte imbriqué
	 * @return Le JLabel contenant le nom du contexte imbriqué
	 */
	public JLabel getContextLabel() {
		return ((NestedContextTableModel) getModel()).getContextLabel();
	}

	/**
	 * Reconstruit le modèle de la table en fonction d'une nouvelle relation
	 */
	@Override
	public void setModelFromContext(Context ctx) {
		setModel(new NestedContextTableModel((NestedContext) ctx));

		/* Ajustement des colonnes de l'éditeur */
		panelsList = new Vector<JScrollPane>();
		for (int i = 0; i < getModel().getColumnCount(); i++) {
			SubContextTable tableCell = (SubContextTable) getValueAt(0, i);
			int totalWidth = tableCell.getColumnModel().getTotalColumnWidth();
			TableColumn currentColumn = getColumnModel().getColumn(i);
			currentColumn.setPreferredWidth(totalWidth);
			currentColumn.setResizable(false);
			tableCell.getTableHeader().setReorderingAllowed(true);

			JScrollPane currentPanel = new JScrollPane(tableCell, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			panelsList.add(currentPanel);
		}

		/* Obtention de la largeur des rangées */
		SubContextTable firstCell = (SubContextTable) getValueAt(0, 0);
		if (firstCell != null)
			rowHeight = (firstCell.getRowCount() + 1) * (firstCell.getRowHeight());
		else
			rowHeight = 0;

		/* Création du popup menu pour les attributs */
		attributesMenu = createAttributesPopupMenu();
		add(attributesMenu);
		attributesMenu.addMouseListener(this);

		/* Création du popup menu pour les niveaux */
		levelsMenu = createLevelsPopupMenu();
		add(levelsMenu);
		levelsMenu.addMouseListener(this);

		isCTRLPressed = false;
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.ContextTable#setColumnName(int, java.lang.String)
	 */
	@Override
	public void setColumnName(int colIdx, String value) {
	}

	/**
	 * Crée le popup menu pour le changement de niveau des attributs
	 * @return Le JPopupMenu pour changer les attributs de niveau
	 */
	public JPopupMenu createAttributesPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenu titleMenu = new JMenu(GUIMessages.getString("GUI.moveAttributeTo")); //$NON-NLS-1$
		popupMenu.add(titleMenu);

		/* Création d'un élément de menu pour chaque niveau de l'éditeur */
		JMenuItem item;
		for (int i = 0; i < getModel().getColumnCount(); i++) {
			Level currentLevel = new Level(i);
			SubContextTable currentEditor = (SubContextTable) getModel().getValueAt(0, i);
			item = new JMenuItem(currentLevel.toString());
			item.addActionListener(new AttributePopupListener(currentEditor, currentLevel.intValue()));
			titleMenu.add(item);
		}

		return popupMenu;
	}

	/**
	 * Crée le popup menu pour le changement de niveau des contextes
	 * @return Le JPopupMenu pour changer les contextes de niveau
	 */
	public JPopupMenu createLevelsPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenu titleMenu = new JMenu(GUIMessages.getString("GUI.moveLevelTo")); //$NON-NLS-1$
		popupMenu.add(titleMenu);

		/* Création d'un élément de menu pour chaque niveau de l'éditeur */
		JMenuItem item;
		for (int i = 0; i < getModel().getColumnCount(); i++) {
			Level currentLevel = new Level(i);
			item = new JMenuItem(currentLevel.toString());
			item.addActionListener(new LevelPopupListener(currentLevel.intValue()));
			titleMenu.add(item);
		}

		return popupMenu;
	}

	/**
	 * Déplace un attribut d'un niveau vers un autre
	 * @param srcIdx Le int contenant l'index de l'éditeur dans lequel est l'attribut à déplacer
	 * @param colIdx Le int contenant l'index de l'attribut dans son éditeur
	 * @param destIdx Le int contenant l'index de l'éditeur dans lequel doit être déplacé l'attribut
	 */
	private void moveAttribute(int srcIdx, int colIdx, int destIdx) {
		((NestedContextTableModel) getModel()).getEditorAt(0).getNestedContext().setNestedLattice(null);

		/* Obtention des éditeurs */
		SubContextTable sourceEditor = ((NestedContextTableModel) getModel()).getEditorAt(srcIdx);
		SubContextTable targetedEditor = ((NestedContextTableModel) getModel()).getEditorAt(destIdx);

		/* Suppression de l'attribut dans un contexte et ajout dans l'autre */
		String attribute = sourceEditor.getAttributeAt(colIdx);
		Vector<String> relations = sourceEditor.getRelationsForAttributeAt(colIdx);

		if (targetedEditor.hasAddedColumn(attribute, relations))
			sourceEditor.removeColumnAt(colIdx);
		else
			DialogBox.showInputQuestion(thisTable, GUIMessages.getString("GUI.attributeAttributeNamed")+" " + attribute, GUIMessages.getString("GUI.enterAnotherAttibuteName")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Désélectionne toutes les colones sélectionnées dans cet éditeur
	 */
	private void removeAllSelections() {
		for (int i = 0; i < getColumnCount(); i++) {
			SubContextTable editor = ((NestedContextTableModel) getModel()).getEditorAt(i);
			int columnCount = editor.getColumnCount();
			if (columnCount > 0)
				editor.removeColumnSelectionInterval(0, columnCount - 1);
		}
	}

	/* ======== MOUSELISTENER INTERFACE ======== */

	/* (non-Javadoc)
	 * @see fca.gui.context.table.ContextTable#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		int column = columnAtPoint(e.getPoint());
		int row = rowAtPoint(e.getPoint());

		int sourceColumnIndex = column;
		//int modelColumnIndex = convertColumnIndexToModel(column);

		/* Trouve l'éditeur touché par le clic de la souris */
		SubContextTable clickedEditor = (SubContextTable) getValueAt(row, column);

		if (e.getSource() == getTableHeader()) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				removeColumnSelectionInterval(0, getColumnCount() - 1);
				addColumnSelectionInterval(sourceColumnIndex, sourceColumnIndex);
				levelsMenu.show(getTableHeader(), e.getX(), e.getY());
			}
		}

		else {
			/* Repositionne l'événement MouseEvent à l'intérieur d'un éditeur interne */
			Rectangle cellRect = getCellRect(row, column, false);
			Point clickedPoint = new Point((int) (e.getPoint().getX() - cellRect.getLocation().getX()),
					(int) (e.getPoint().getY() - cellRect.getLocation().getY()));

			int clickedColumnIndex = clickedEditor.columnAtPoint(clickedPoint);
			int clickedRowIndex = clickedEditor.rowAtPoint(clickedPoint);

			if (e.getButton() == MouseEvent.BUTTON3) {
				if (clickedColumnIndex != -1 && clickedRowIndex == 0
						&& clickedEditor.isColumnSelected(clickedColumnIndex)) {
					attributesMenu.show(this, e.getX(), e.getY());
				} else if (clickedColumnIndex != -1 && clickedRowIndex == 0) {
					removeAllSelections();
					clickedEditor.addColumnSelectionInterval(clickedColumnIndex, clickedColumnIndex);
					repaint();
					attributesMenu.show(this, e.getX(), e.getY());
				}
			}

			else if (e.getButton() == MouseEvent.BUTTON1 && !isCTRLPressed) {
				if (clickedColumnIndex != -1 && clickedRowIndex == 0) {
					if (clickedEditor.isColumnSelected(clickedColumnIndex)) {
						removeAllSelections();
					} else {
						removeAllSelections();
						clickedEditor.addColumnSelectionInterval(clickedColumnIndex, clickedColumnIndex);
					}
					repaint();
				}
			}

			else if (e.getButton() == MouseEvent.BUTTON1 && isCTRLPressed) {
				if (clickedColumnIndex != -1 && clickedRowIndex == 0) {
					if (clickedEditor.isColumnSelected(clickedColumnIndex)) {
						clickedEditor.removeColumnSelectionInterval(clickedColumnIndex, clickedColumnIndex);
					} else {
						clickedEditor.addColumnSelectionInterval(clickedColumnIndex, clickedColumnIndex);
					}
					repaint();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.ContextTable#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.ContextTable#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.ContextTable#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see fca.gui.context.table.ContextTable#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * Contient les information sur un niveau, soit sa valeur numérique et la chaîne qui le
	 * représente
	 */
	private class Level {
		private int intValue; //La valeur numérique du niveau
		private String stringValue; //La chaîne de caractères représentant le niveau

		/**
		 * Constructeur
		 * @param l Le int contenant la valeur numérique du niveau
		 */
		public Level(int l) {
			stringValue = GUIMessages.getString("GUI.level") + (l + 1); //$NON-NLS-1$
			intValue = l;
		}

		/**
		 * Permet de connaître la valeur numérique du niveau
		 * @return Le int indiquant la valeur numérique du niveau
		 */
		public int intValue() {
			return intValue;
		}

		/**
		 * Permet de connaître la chaîne de caractères représentant le niveau
		 */
		@Override
		public String toString() {
			return stringValue;
		}
	}

	/**
	 * Vérifie les événements du clavier pour savoir si la touche CTRL est enfoncée
	 */
	private class CtrlKeyDetector implements KeyEventDispatcher {
		/* ======== KEYEVENTDISPATCHER INTERFACE ======== */

		/*
		 * (non-Javadoc)
		 * @see java.awt.KeyEventDispatcher#dispatchKeyEvent(java.awt.event.KeyEvent)
		 */
		public boolean dispatchKeyEvent(KeyEvent e) {
			/* Ajuste le drapeau indiquant si la touche CTRL est enfoncée */
			if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
				switch (e.getID()) {
					case KeyEvent.KEY_PRESSED:
						isCTRLPressed = true;
						break;
					case KeyEvent.KEY_RELEASED:
						isCTRLPressed = false;
						break;
					default:
						isCTRLPressed = false;
						break;
				}
			}
			return false;
		}
	}

	/**
	 * Gère les événements dans le menu popup pour les attributs
	 */
	private class AttributePopupListener implements ActionListener {
		//private SubContextTable targetedEditor; //L'éditeur destination
		private int level; //Le niveau de destination

		/**
		 * Constructeur
		 * @param ed Le SubContextTable ciblé par ce menu
		 * @param l Le int contenant le niveau ciblé par ce menu
		 */
		public AttributePopupListener(SubContextTable ed, int l) {
			//this.targetedEditor = ed;
			this.level = l;
		}

		/* ======== ACTIONLISTENER INTERFACE ======== */

		/*
		 * (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			/* Déplacement des attributs sélectionnés vers l'éditeur destination de ce menu */
			for (int i = 0; i < getColumnCount(); i++) {
				SubContextTable sourceEditor = ((NestedContextTableModel) getModel()).getEditorAt(i);
				int[] selectedAttributes = sourceEditor.getSelectedColumns();
				int selectedAttributesCount = sourceEditor.getSelectedColumnCount();
				for (int j = 0; j < selectedAttributesCount; j++) {
					int currentIndex = selectedAttributes[j];
					moveAttribute(i, currentIndex - j, level);
					repaint();
				}
			}

			/* Ajuste la largeur des cellules */
			for (int i = 0; i < getColumnCount(); i++) {
				SubContextTable editor = ((NestedContextTableModel) getModel()).getEditorAt(i);
				int columnWidth = editor.getColumnModel().getTotalColumnWidth();
				TableColumn column = getColumnModel().getColumn(i);
				column.setPreferredWidth(columnWidth);
				editor.validate();
			}
		}
	}

	/**
	 * Gère les événements dans le menu popup pour les relations
	 */
	private class LevelPopupListener implements ActionListener {
		private int level; //Le niveau de destination de ce menu

		/**
		 * Constructeur
		 * @param l Le int contenant le niveau ciblé par ce menu
		 */
		public LevelPopupListener(int l) {
			this.level = l;
		}

		/* ======== ACTIONLISTENER INTERFACE ======== */

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			/* Déplacement de la sous-relation à l'intérieur même de la relation imbriquée */
			NestedContext firstContext = ((NestedContextTableModel) getModel()).getNestedContextForLevel(1);
			int selectedLevel = getSelectedColumn();
			firstContext.moveLevel(selectedLevel, level);

			/* Reconstruction de la table */
			setModelFromContext(firstContext);
		}
	}

}