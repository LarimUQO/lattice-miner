package fca.gui.lattice.tool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import fca.core.context.Context;
import fca.core.context.binary.BinaryContext;
import fca.core.lattice.DataCel;
import fca.core.lattice.NestedLattice;
import fca.core.lattice.operator.search.ApproximationSimple;
import fca.core.util.BasicSet;
import fca.exception.LatticeMinerException;
import fca.gui.lattice.LatticePanel;
import fca.gui.lattice.element.GraphicalConcept;
import fca.gui.lattice.element.GraphicalLattice;
import fca.gui.util.DialogBox;
import fca.gui.util.constant.LMIcons;
import fca.messages.GUIMessages;

/**
 * Panneau affichant les attributs / objets d'un treillis pour en permettre la selection dans le but
 * de faire une opération de recherche qui sera affichée dans le LatticeViewer associé.
 * @author Ludovic Thomas, Bennis Aicha
 * @version 1.0
 */

public abstract class Search extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Treillis pour lequel l'operation est effectuée
	 */
	protected GraphicalLattice lattice;
	
	/**
	 * Le viewer qui affiche le treillis
	 */
	protected LatticePanel viewer;
	
	/**
	 * Bouton pour exécuter l'operation
	 */
	private JButton updateBtn;
	
	/**
	 * Bouton pour ouvrir le resultat dans une nouvelle fenetre
	 */
	protected JButton openResultButton;
	
	/**
	 * Specifie la recherche d'attributs
	 */
	private JCheckBox attributesBtn;
	
	/**
	 * Panneau affichant la liste des attributs
	 */
	private JPanel attributesPanel;
	
	/** Boîtes de sélection/désélection des attributs */
	private Vector<JCheckBox> attributeCheckBoxes;
	
	/**
	 * Specifie la recherche d'objets
	 */
	private JCheckBox objectsBtn;
	
	/**
	 * Panneau affichant la liste des objets
	 */
	private JPanel objectsPanel;
	
	/** Boîtes de sélection/désélection des objets */
	private Vector<JCheckBox> objectCheckBoxes;
	
	/**
	 * Permet d'activer l'operation à la volée
	 */
	private JCheckBox searchOnTheFly;
	
	/**
	 * Permet d'activer l'operation couple decomposition
	 */
	private JCheckBox intervalle;
	
	/**
	 * Permet d'activer l'operation couple decomposition
	 */
	private JCheckBox alpha;
	
	/**
	 * Permet d'activer l'operation couple decomposition
	 */
	private JCheckBox beta;
	/**
	 * Permet d'activer l'operation couple decomposition
	 */
	private JCheckBox type;
	
	/**
	 * Permet de rechercher le concept avec les éléments choisis exactement
	 */
	protected JCheckBox exactMatchOnly;
	
	/**
	 * Permet de savoir si c'est la creéation ou la mise à jour du panel
	 */
	private boolean alreadyCreated;
	
	/**
	 * Le resultat de l'operation
	 */
	protected GraphicalLattice resultOperation;
	
	/** Permet de changer rapidement la valeur par défaut de l'état du panneau */
	private static final boolean DEFAULT_ENABLE = false;
	
	/** Phrase affichée lors d'un résultat vide */
	protected static final String NO_RESULT = GUIMessages.getString("GUI.noConceptFound"); //$NON-NLS-1$
	
	/** Phrase affichée lors d'une recherche vide */
	protected static final String NO_SEARCH = GUIMessages.getString("GUI.nothingSelectedInSearch"); //$NON-NLS-1$
	
	
	/** Phrase affichée lors d'une recherche vide */
	protected static final String NOT_ALL = GUIMessages.getString("GUI.SelectAttributeandObjects"); //$NON-NLS-1$
	
	/**
	 * Constructeur
	 * @param l Le {@link GraphicalLattice} pour lequel ce panneau affiche les attributs et objets
	 * @param lp Le {@link LatticePanel} dans lequel est affiché le treillis
	 */
	public Search(GraphicalLattice l, LatticePanel lp) {
		
		setBackground(Color.WHITE);
		lattice = l;
		viewer = lp;
		resultOperation = null;
		
		// Create checkBoxes and the panel
		createCheckboxes();
		createPanel();
		alreadyCreated = true;
	}
	
	protected void createCheckboxes() {
		
		// Recupère le contexte binaire
		NestedLattice firstLattice = lattice.getNestedLattice();
		NestedLattice lastLattice = firstLattice;
		while (lastLattice.getTopNestedConcept().getInternalNestedLattice() != null)
			lastLattice = lastLattice.getTopNestedConcept().getInternalNestedLattice();
		BinaryContext globalContext = lastLattice.getGlobalContext();
		
		// Mise a jour des attributs et objets
		Vector<String> attributes = globalContext.getAttributes();
		Vector<String> objects = globalContext.getObjects();
		
		// Creation du vecteur de checkboxes
		if (!alreadyCreated) {
			// Construction des boîtes de sélection des attributs
			attributeCheckBoxes = new Vector<JCheckBox>();
			for (String intention : attributes) {
				JCheckBox newCheckBox = new JCheckBox(intention);
				newCheckBox.setSelected(false);
				newCheckBox.setEnabled(DEFAULT_ENABLE);
				newCheckBox.setBackground(Color.WHITE);
				newCheckBox.setPreferredSize(new Dimension(130, 20));
				newCheckBox.addActionListener(this);
				attributeCheckBoxes.add(newCheckBox);
			}
			
			// Construction des boîtes de sélection des objets
			objectCheckBoxes = new Vector<JCheckBox>();
			for (String extension : objects) {
				JCheckBox newCheckBox = new JCheckBox(extension);
				newCheckBox.setSelected(false);
				newCheckBox.setEnabled(DEFAULT_ENABLE);
				newCheckBox.setBackground(Color.WHITE);
				newCheckBox.setPreferredSize(new Dimension(130, 20));
				newCheckBox.addActionListener(this);
				objectCheckBoxes.add(newCheckBox);
			}
		}
		// Mise a jour du vecteur de checkboxes
		else {
			// Mise a jour des checkboxes des attributs
			for (JCheckBox attributeBox : attributeCheckBoxes) {
				boolean valid = attributes.contains(attributeBox.getText());
				attributeBox.setVisible(valid);
				if (!valid)
					attributeBox.setSelected(false);
			}
			
			// Mise a jour des checkboxes des objects
			for (JCheckBox objectBox : objectCheckBoxes) {
				boolean valid = objects.contains(objectBox.getText());
				objectBox.setVisible(valid);
				if (!valid)
					objectBox.setSelected(false);
			}
		}
		
		// ATTRIBUTS PANEL : Construction du panneau
		attributesPanel = new JPanel();
		attributesPanel.setLayout(new GridBagLayout());
		attributesPanel.setBackground(Color.WHITE);
		
		// Sous-panneau de selection des attributs
		TitledBorder attPanelTitle = new TitledBorder(BorderFactory.createLineBorder(Color.BLACK), GUIMessages.getString("GUI.attributes")); //$NON-NLS-1$
		attPanelTitle.setTitleColor(Color.BLACK);
		attributesPanel.setBorder(attPanelTitle);
		
		GridBagConstraints gcAtt = new GridBagConstraints();
		gcAtt.insets = new Insets(2, 5, 2, 5);
		gcAtt.gridwidth = 1;
		gcAtt.gridheight = 1;
		gcAtt.anchor = GridBagConstraints.WEST;
		gcAtt.fill = GridBagConstraints.NONE;
		
		// Ajout des boîtes de sélection des attributs
		for (JCheckBox attributeBox : attributeCheckBoxes) {
			gcAtt.gridy++;
			attributesPanel.add(attributeBox, gcAtt);
		}
		
		// OBJECTS PANEL : Construction du panneau
		objectsPanel = new JPanel();
		objectsPanel.setLayout(new GridBagLayout());
		objectsPanel.setBackground(Color.WHITE);
		
		// Sous-panneau de selection des objets
		TitledBorder objPanelTitle = new TitledBorder(BorderFactory.createLineBorder(Color.BLACK), GUIMessages.getString("GUI.objects")); //$NON-NLS-1$
		objPanelTitle.setTitleColor(Color.BLACK);
		objectsPanel.setBorder(objPanelTitle);
		
		GridBagConstraints gcObj = new GridBagConstraints();
		gcObj.insets = new Insets(2, 5, 2, 5);
		gcObj.gridwidth = 1;
		gcObj.gridheight = 1;
		gcObj.anchor = GridBagConstraints.WEST;
		gcObj.fill = GridBagConstraints.NONE;
		
		// Ajout des boîtes de sélection des objets
		for (JCheckBox objectBox : objectCheckBoxes) {
			gcObj.gridy++;
			objectsPanel.add(objectBox, gcObj);
		}
	}
	
	protected void createPanel() {
		
		// Si panneau déjà créé, supprime tout
		if (alreadyCreated) {
			removeAll();
		}
		
		// Créer les checkboxes
		createCheckboxes();
		
		// Si pas dejà créé, on créer les boutons
		if (!alreadyCreated) {
			
			// Construction du panneau global
			setLayout(new GridBagLayout());
			
			// Bouton d'approximation
			searchOnTheFly = new JCheckBox(GUIMessages.getString("GUI.searchOnTheFly")); //$NON-NLS-1$
			searchOnTheFly.setBackground(Color.WHITE);
			searchOnTheFly.addActionListener(this);
			searchOnTheFly.setToolTipText(GUIMessages.getString("GUI.performWhenSelectionChanged")); //$NON-NLS-1$
			searchOnTheFly.setSelected(false);
			
			intervalle = new JCheckBox(GUIMessages.getString("GUI.intervalle")); //$NON-NLS-1$
			intervalle.setBackground(Color.WHITE);
			intervalle.addActionListener(this);
			intervalle.setToolTipText(GUIMessages.getString("GUI.performWhenSelectionChanged")); //$NON-NLS-1$
			intervalle.setSelected(false);
			
			
			//couple decomposition basee sur les objets
			alpha = new JCheckBox(GUIMessages.getString("GUI.alpha")); //$NON-NLS-1$
			alpha.setBackground(Color.WHITE);
			alpha.addActionListener(this);
			alpha.setToolTipText(GUIMessages.getString("GUI.performWhenSelectionChanged")); //$NON-NLS-1$
			alpha.setSelected(false);
			
			//bouton decomposition basee sur les attributs
			beta = new JCheckBox(GUIMessages.getString("GUI.beta")); //$NON-NLS-1$
			beta.setBackground(Color.WHITE);
			beta.addActionListener(this);
			beta.setToolTipText(GUIMessages.getString("GUI.performWhenSelectionChanged")); //$NON-NLS-1$
			beta.setSelected(false);
			
			//bouton decomposition basee sur les attributs
			type = new JCheckBox(GUIMessages.getString("GUI.type")); //$NON-NLS-1$
			type.setBackground(Color.WHITE);
			type.addActionListener(this);
			type.setToolTipText(GUIMessages.getString("GUI.performWhenSelectionChanged")); //$NON-NLS-1$
			type.setSelected(false);
			
			exactMatchOnly = new JCheckBox(GUIMessages.getString("GUI.exactSearchOnly")); //$NON-NLS-1$
			exactMatchOnly.setBackground(Color.WHITE);
			exactMatchOnly.addActionListener(this);
			exactMatchOnly.setSelected(false);
			exactMatchOnly.setToolTipText(GUIMessages.getString("GUI.findConceptContainingExactly")); //$NON-NLS-1$
			
			// Bouton d'update
			updateBtn = new JButton(LMIcons.getOK());
			updateBtn.addActionListener(this);
			updateBtn.setToolTipText(GUIMessages.getString("GUI.execute")); //$NON-NLS-1$
			updateBtn.setEnabled(!searchOnTheFly.isSelected());
			
			// Bouton de resultat
			openResultButton = new JButton(LMIcons.getFind());
			openResultButton.addActionListener(this);
			openResultButton.setToolTipText(GUIMessages.getString("GUI.showTheApproximationInNewWindow")); //$NON-NLS-1$
			openResultButton.setEnabled(false);
			
			// Bouton de recherche sur les attributs
			attributesBtn = new JCheckBox(GUIMessages.getString("GUI.operationOnAttributes")); //$NON-NLS-1$
			attributesBtn.setSelected(false);
			attributesBtn.setBackground(Color.WHITE);
			attributesBtn.addActionListener(this);
			
			// Bouton de recherche sur les objets
			objectsBtn = new JCheckBox(GUIMessages.getString("GUI.operationOnObjects")); //$NON-NLS-1$
			objectsBtn.setSelected(false);
			objectsBtn.setBackground(Color.WHITE);
			objectsBtn.addActionListener(this);
		}
		
		// Ajout des panneaux
		GridBagConstraints gcGlobal = new GridBagConstraints();
		gcGlobal.fill = GridBagConstraints.NONE;
		gcGlobal.anchor = GridBagConstraints.PAGE_START;
		
		// Bouton "Approximation on the fly" et updateBtn
		gcGlobal.insets = new Insets(5, -135, 5, 0);
		
		gcGlobal.gridy++;
		add(searchOnTheFly, gcGlobal);
		
		gcGlobal.insets = new Insets(5, -125, 5, 0);
		gcGlobal.gridy++;
		add(intervalle, gcGlobal);
		
		gcGlobal.insets = new Insets(5, 27, 5, 0);
		gcGlobal.gridy++;
		add(alpha, gcGlobal);
		
		gcGlobal.insets = new Insets(5, 38, 5, 0);
		gcGlobal.gridy++;
		add(beta, gcGlobal);
		
		gcGlobal.insets = new Insets(5, -95, 5, 0);
		gcGlobal.gridy++;
		add(type, gcGlobal);
		
		
		gcGlobal.insets = new Insets(5, -130, 5, 0);
		gcGlobal.gridy++;
		add(exactMatchOnly, gcGlobal);
		
		gcGlobal.gridy++;
		add(updateBtn, gcGlobal);
		
		gcGlobal.gridy++;
		add(openResultButton, gcGlobal);
		
		// Ajout des panneaux pour les attributs et pour les objets
		gcGlobal.insets = new Insets(2, 5, 2, 5);
		gcGlobal.anchor = GridBagConstraints.NORTHWEST;
		
		gcGlobal.gridy++;
		add(attributesBtn, gcGlobal);
		
		gcGlobal.gridy++;
		add(attributesPanel, gcGlobal);
		
		gcGlobal.gridy++;
		add(objectsBtn, gcGlobal);
		
		gcGlobal.weighty = 1.0;
		gcGlobal.gridy++;
		add(objectsPanel, gcGlobal);
	}
	
	/**
	 * @return les attributs choisis dans les checkboxes
	 */
	protected BasicSet getSelectedIntent() {
		BasicSet intent = new BasicSet();
		if (attributesBtn.isSelected()) {
			for (JCheckBox intentionBox : attributeCheckBoxes) {
				if (intentionBox.isSelected())
					intent.add(intentionBox.getText());
			}
		}
		return intent;
	}
	
	/**
	 * @return les objets choisis dans les checkboxes
	 */
	protected BasicSet getSelectedExtent() {
		BasicSet extent = new BasicSet();
		if (objectsBtn.isSelected()) {
			for (JCheckBox extensionBox : objectCheckBoxes) {
				if (extensionBox.isSelected())
					extent.add(extensionBox.getText());
			}
		}
		return extent;
	}
	
	/**
	 * @return vrai si on recherche effectivement des objets, faux sinon
	 */
	protected boolean isSearchOnObjects() {
		return objectsBtn.isSelected() && getSelectedExtent().size() != 0;
	}
	
	/**
	 * @return vrai si on recherche effectivement des attributs, faux sinon
	 */
	protected boolean isSearchOnAttributes() {
		return attributesBtn.isSelected() && getSelectedIntent().size() != 0;
	}
	
	/**
	 * Mets à jour le panneau avec un nouveau treillis
	 * @param gl le nouveau treillis a prendre en compte
	 */
	public void setNewLattice(GraphicalLattice gl) {
		lattice = gl;
		createCheckboxes();
		createPanel();
	}
	
	/**
	 * Retourne le concept qui correspond à la recherche exacte sur les attributs et objets
	 * sélectionnés
	 * @return Le GraphicalLattice créé par la recherche exacte
	 */
	public GraphicalConcept exactMatch() {
		
		GraphicalConcept result = null;
		
		// Recupere l'extension et l'intetion selectionnée
		BasicSet extent = getSelectedExtent();
		BasicSet intent = getSelectedIntent();
		
		// Recherche sur attributs et vérification des objets 
		if (isSearchOnObjects() && isSearchOnAttributes()) {
			GraphicalConcept resultApprox = searchExacteNodeWithIntent(intent);
			if (resultApprox != null && resultApprox.getNestedConcept().getExtent().equals(extent))
				result = resultApprox;
		}
		// Recherche sur objets uniquement
		else if (isSearchOnObjects()) {
			result = searchExacteNodeWithExtent(extent);
		}
		// Recherche sur attributs uniquement
		else if (isSearchOnAttributes()) {
			result = searchExacteNodeWithIntent(intent);
		}
		
		return result;
	}
	
	/**
	 * Opération appelée par l'action Listener pour une recherche exacte
	 */
	public void exactMatchAction() {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalConcept result = exactMatch();
			showAndShakeResult(result);
		}
	}
	
	/**
	 * Retourne le treillis ou le concept qui correspond à la recherche approximative sur les
	 * attributs et objets sélectionnés
	 * @return Le GraphicalLattice créé par la recherche approximative
	 */
	@SuppressWarnings("unused") //$NON-NLS-1$
	public Object approximateMatch() throws LatticeMinerException {
		GraphicalConcept result = null;
		
		// Recupere l'extension et l'intetion selectionnée
		BasicSet extent = getSelectedExtent();
		BasicSet intent = getSelectedIntent();
		
		// Recherche sur attributs et vérification des objets 
		if (isSearchOnObjects() && isSearchOnAttributes()) {
			GraphicalConcept resultApprox = searchApproximateNodeWithIntent(intent);
			if (resultApprox != null && resultApprox.getNestedConcept().getExtent().isIncluding(extent))
				result = resultApprox;
		}
		// Recherche sur objets uniquement
		else if (isSearchOnObjects()) {
			result = searchApproximateNodeWithExtent(extent);
		}
		// Recherche sur attributs uniquement
		else if (isSearchOnAttributes()) {
			result = searchApproximateNodeWithIntent(intent);
		}
		
		return result;
	}
	
	/**
	 * Opération appelée par l'action Listener pour une recherche approxmative
	 */
	public void approximateMatchAction() throws LatticeMinerException {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalConcept result = (GraphicalConcept) approximateMatch();
			showAndShakeResult(result);
		}
	}
	
	
	/**
	 * Retourne le treillis ou le concept qui correspond à la recherche decomposition alpha
	 * @return Le GraphicalLattice créé par la recherche approximative alpha
	 */
	@SuppressWarnings("unused") //$NON-NLS-1$
	public Object Coupledecomposition() throws LatticeMinerException {
		GraphicalConcept result = null;
		
		// Recupere l'extension et l'intetion selectionnée
		BasicSet extent = getSelectedExtent();
		BasicSet intent = getSelectedIntent();
		
		// Recherche sur attributs et vérification des objets 
		if (isSearchOnObjects() && isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
			}
		// Recherche sur objets uniquement
		else if (isSearchOnObjects()) {
			result = searchApproximateNodeWithExtent(extent);
		}
		// Recherche sur attributs uniquement
		else if (isSearchOnAttributes()) {
			result = searchApproximateNodeWithIntent(intent);
		}
		
		return result;
	}

	/**
	 * Retourne le treillis ou le concept qui correspond à la recherche decomposition alpha
	 * @return Le GraphicalLattice créé par la recherche approximative alpha et beta
	 */
	@SuppressWarnings("unused") //$NON-NLS-1$
	public Object Coupledecomposition2() throws LatticeMinerException {
		GraphicalConcept result = null;
		
		// Recupere l'extension et l'intetion selectionnée
		BasicSet extent = getSelectedExtent();
		BasicSet intent = getSelectedIntent();
		
		// Recherche sur attributs et vérification des objets 
		if (isSearchOnObjects() && isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
			}
		// Recherche sur objets uniquement
		else if (isSearchOnObjects()) {
			result = searchApproximateNodeWithExtent(extent);
		}
		// Recherche sur attributs uniquement
		else if (isSearchOnAttributes()) {
			result = searchApproximateNodeWithIntent(intent);
		}
		
		return result;
	}
	/**
	 * Opération appelée par l'action Listener pour recherche couple decomposition alpha
	 */
	public void Coupledecomposition2Action() throws LatticeMinerException {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalConcept result = (GraphicalConcept) Coupledecomposition2();
			showAndShakeResult(result);
			
		}
	}
	
	/**
	 * Opération appelée par l'action Listener pour recherche couple decomposition alpha
	 */
	public void CoupledecompositionAction() throws LatticeMinerException {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalConcept result = (GraphicalConcept) Coupledecomposition();
			showAndShakeResult(result);
			
		}
	}
	
	/**
	 * Retourne le treillis ou le concept qui correspond à la recherche approximative beta
	 * @return Le GraphicalLattice créé par la recherche approximative beta
	 */
	@SuppressWarnings("unused") //$NON-NLS-1$
	public Object Beta() throws LatticeMinerException {
GraphicalConcept result = null;
		
		// Recupere l'extension et l'intetion selectionnée
		BasicSet extent = getSelectedExtent();
		BasicSet intent = getSelectedIntent();
		
		// Recherche sur attributs et vérification des objets 
		if (isSearchOnObjects() && isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
			}
		// Recherche sur objets uniquement
		else if (isSearchOnObjects()) {
			result = searchApproximateNodeWithExtent(extent);
		}
		// Recherche sur attributs uniquement
		else if (isSearchOnAttributes()) {
			result = searchApproximateNodeWithIntent(intent);
		}
		
		return result;
	}
	
	/**
	 * Opération appelée par l'action Listener pour recherche couple decomposition alpha
	 */
	public void BetaAction() throws LatticeMinerException {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalConcept result = (GraphicalConcept) Beta();
			showAndShakeResult(result);
			
		}
	}
	
	/**
	 * Dans le cas ou le type de la cellule est N-concept
	 * @return
	 * @throws LatticeMinerException
	 */
	public Object typeNConcept() throws LatticeMinerException {
		GraphicalConcept result = null;
		
		// Recherche sur attributs et vérification des objets 
		if (isSearchOnObjects() && isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
			}
		// Recherche sur objets uniquement
		else if (isSearchOnObjects()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
			return null;
		}
		// Recherche sur attributs uniquement
		else if (isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
			return null;
		}
		
		return result;
	}
	

	/**
	 * Opération appelée par l'action Listener pour le cas du N-concept
	 */
	public void typeNConceptAction() throws LatticeMinerException {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalConcept result = (GraphicalConcept) typeNConcept();
			showAndShakeResult(result);
		}
	}

	/**
	 * Affiche le resultat et le mets en evidence via selectAndShakeNode() ou bien affiche un
	 * message concernant le resultat
	 * @param result le resultat {@link GraphicalConcept} a afficher
	 */
	protected void showAndShakeResult(GraphicalConcept result) {
		// Affiche le résultat ou une boîte de dialogue "Not found"
		if (result != null) {
			if (viewer.getFrame().getTreePanel().getSelectedNode() != result)
				viewer.getFrame().getTreePanel().selectPathNode(result);
			else
				viewer.selectAndShakeNode(result);
		} else {
			viewer.setSelectedNodes(new Vector<GraphicalConcept>());
			viewer.getRootLattice().setOutOfFocus(true);
			viewer.repaint();
			DialogBox.showMessageInformation(viewer, NO_RESULT, GUIMessages.getString("GUI.resultForSearch")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Recherche LE concept qui contient exactement l'intention fournie
	 * @param intent l'intention que l'on recherche
	 * @return le plus petit concept qui contient l'intention
	 */
	public GraphicalConcept searchExacteNodeWithIntent(BasicSet intent) {
		GraphicalConcept resultExact = null;
		GraphicalConcept resultApprox = searchApproximateNodeWithIntent(intent);
		if (resultApprox != null && resultApprox.getNestedConcept().getIntent().equals(intent))
			resultExact = resultApprox;
		return resultExact;
	}
	
	/**
	 * Recherche LE concept qui contient exactement l'extension fournie
	 * @param extent l'extension que l'on recherche
	 * @return le plus petit concept qui contient l'extension
	 */
	public GraphicalConcept searchExacteNodeWithExtent(BasicSet extent) {
		GraphicalConcept resultExact = null;
		GraphicalConcept resultApprox = searchApproximateNodeWithExtent(extent);
		if (resultApprox != null && resultApprox.getNestedConcept().getExtent().equals(extent))
			resultExact = resultApprox;
		return resultExact;
	}
	
	/**
	 * Recherche le plus petit concept qui contient (donc recherche pas exacte) l'intention fournie
	 * @param intent l'intention que l'on recherche
	 * @return le plus petit concept qui contient l'intention
	 */
	protected abstract GraphicalConcept searchApproximateNodeWithIntent(BasicSet intent);
	protected abstract DataCel Alpha1(DataCel cel,Context ctx);
	
	/**
	 * Recherche le plus petit concept qui contient (donc recherche pas exacte) l'extension fournie
	 * @param extent l'extension que l'on recherche
	 * @return le plus petit concept qui contient l'extension
	 */
	protected abstract GraphicalConcept searchApproximateNodeWithExtent(BasicSet extent);
	
	/* ======== ACTIONLISTENER INTERFACE ======== */

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
	
		

		GraphicalLattice res = (GraphicalLattice) lattice.clone();
		ApproximationSimple ap = new ApproximationSimple(res.getNestedLattice().getConceptLattice());
		NestedLattice res2=res.getNestedLattice(); 
		BinaryContext ctx=res2.getGlobalContext();
		
		
		// Bouton de visualisation du resultat
		if (e.getSource() == openResultButton) {
			viewer.getFrame().duplicateViewer(resultOperation);
		}
		// Bouton d'exécution de l'approximation OU une case a été cochée et approximation "a la volée" actif
		else if ((e.getSource() == updateBtn) || ((e.getSource() instanceof JCheckBox) && searchOnTheFly.isSelected())) {
			
			try {
				if (exactMatchOnly.isSelected()) {
					exactMatchAction();
					
				} else
					{
						if(searchOnTheFly.isSelected())
						{
							if(type.isSelected()){
								openResultButton.setEnabled(false);

								
								if(!isSearchOnObjects() || !isSearchOnAttributes()){
									DialogBox.showMessageInformation(viewer, "Select attribute(s) and object(s)", GUIMessages.getString("Not found")); //$NON-NLS-1$
									openResultButton.setEnabled(false);
									
								}else{
									if(isSearchOnObjects() && isSearchOnAttributes()){
										DataCel cel = new DataCel(getSelectedExtent(), getSelectedIntent());
										int t=ap.CelluleType(ctx, cel);
										
											if(t==1){
												DialogBox.showMessageInformation(viewer, "The pair is a Concept", GUIMessages.getString("Cellule type")); //$NON-NLS-1$
												CoupledecompositionAction();

											}else{
												if(t==2){
													DialogBox.showMessageInformation(viewer, "The pair is a Protoconcept", GUIMessages.getString("Cellule type")); //$NON-NLS-1$
													approximateMatchAction();
												}else{
													if(t==3){
														DialogBox.showMessageInformation(viewer, "The pair is a Semi-concept", GUIMessages.getString("Cellule type")); //$NON-NLS-1$
														approximateMatchAction();
													}else{
														if(t==4){
															DialogBox.showMessageInformation(viewer, "The pair is a Preconcept", GUIMessages.getString("Cellule type")); //$NON-NLS-1$
															approximateMatchAction();

														}else{
															if(t==5){
																DialogBox.showMessageInformation(viewer, "The pair is a n-concept", GUIMessages.getString("Cellule type")); //$NON-NLS-1$
																typeNConceptAction();
															
																
															}else{
																if(t==6 ){
																	Coupledecomposition2Action();
																	DialogBox.showMessageInformation(viewer, "Concept in YELLOW: alpha1"+"\n"+"Concept in GREEN: beta1"+"\n"+"Concepts in PINK: alpha2 & beta2"+"\n"+"Concepts in ORANGE: alpha3 & beta3", GUIMessages.getString("Informations")); //$NON-NLS-1$

																	

																}else{
																	if(t==7){
																		DialogBox.showMessageInformation(viewer, "Not Found", GUIMessages.getString("Cellule type")); //$NON-NLS-1$
	
																		
																	}
																}
															}
														}
													}
												}
											}
									}
								}

								
								}
								
							if(intervalle.isSelected())
								{
								approximateMatchAction();
								updateBtn.setEnabled(false);
								}
							
							
							if(alpha.isSelected() && (e.getSource() == updateBtn) )
								{
								if(!isSearchOnObjects() || !isSearchOnAttributes())
									{
										approximateMatchAction();
										openResultButton.setEnabled(false);
									}else{
											CoupledecompositionAction();
											openResultButton.setEnabled(false);
											DialogBox.showMessageInformation(viewer, "Concept in YELLOW: alpha1"+"\n"+"Concept in PINK: alpha2"+"\n"+"Concept in ORANGE: alpha3", GUIMessages.getString("Informations")); //$NON-NLS-1$

									
										}
								}
							if(beta.isSelected() && (e.getSource() == updateBtn) )
							{
							if(!isSearchOnObjects() || !isSearchOnAttributes())
								{
									approximateMatchAction();
									openResultButton.setEnabled(false);
								}else{
										BetaAction();
										openResultButton.setEnabled(false);
										DialogBox.showMessageInformation(viewer, "Concept in GREEN: beta1"+"\n"+"Concept in PINK: beta2"+"\n"+"Concept in ORANGE: beta3", GUIMessages.getString("Informations")); //$NON-NLS-1$

									}
							}
								
							}
							
					}	
			} catch (LatticeMinerException error) {
				DialogBox.showMessageError(this, error);
			}
			
			viewer.repaint();
		}
		
		
		// CAS PARTICULIERS pour mise à jour
		
		// Bouton de choix d'approximation "a la volée"
		if (e.getSource() == searchOnTheFly) {
			updateBtn.setEnabled(searchOnTheFly.isSelected());
		}
		
		// Bouton pour la recherche d'attributs
		else if (e.getSource() == attributesBtn) {
			for (JCheckBox attributeBox : attributeCheckBoxes)
				attributeBox.setEnabled(attributesBtn.isSelected());
		}
		// Bouton pour la recherche d'objets
		else if (e.getSource() == objectsBtn) {
			for (JCheckBox objectBox : objectCheckBoxes)
				objectBox.setEnabled(objectsBtn.isSelected());
		}
		
	}

}
