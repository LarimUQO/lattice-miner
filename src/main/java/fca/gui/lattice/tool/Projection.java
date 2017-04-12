package fca.gui.lattice.tool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import fca.core.context.binary.BinaryContext;
import fca.core.lattice.ConceptLattice;
import fca.core.lattice.NestedLattice;
import fca.core.lattice.operator.projection.BasicAttributeProjection;
import fca.core.lattice.operator.projection.BasicObjectProjection;
import fca.core.util.BasicSet;
import fca.exception.AlreadyExistsException;
import fca.exception.InvalidTypeException;
import fca.exception.LatticeMinerException;
import fca.gui.lattice.LatticePanel;
import fca.gui.lattice.element.GraphicalConcept;
import fca.gui.lattice.element.GraphicalLattice;
import fca.gui.lattice.element.LatticeStructure;
import fca.gui.util.DialogBox;
import fca.gui.util.constant.LMHistory;
import fca.gui.util.constant.LMIcons;
import fca.messages.GUIMessages;

/**
 * Panneau affichant les attributs/objets d'un treillis pour en permettre la selection dans le but
 * de faire une projection qui sera affichée dans le LatticeViewer associé
 * @author Geneviève Roberge
 * @version 1.0
 */
public class Projection extends JPanel implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1583150650476194423L;

	GraphicalLattice lattice; //Le treillis pour lequel les attributs / objets sont affichés

	LatticePanel viewer; //Le viewer qui affiche le treillis

	/* Composants graphiques */

	JButton updateBtn; //Bouton pour exécuter la projection

	JButton cancelBtn; //Bouton pour annuler la projection

	JPanel attributesPanel; //Panneau affichant la liste des attributs
	Vector<JCheckBox> attributeCheckBoxes; //Boîtes de sélection/désélection des attributs
	Vector<String> attributes; //Liste de tous les attributs du treillis

	BasicSet intent; //Intention contenant les attributs choisis

	JPanel objectsPanel; //Panneau affichant la liste des objets
	Vector<JCheckBox> objectCheckBoxes; //Boîtes de sélection/désélection des objets
	Vector<String> objects; //Liste de tous les objets du treillis

	BasicSet extent; //Extention contenant les objets choisis

	JCheckBox projectionOnTheFly; //Permet d'activer la projection à la volée

	/**
	 * Constructeur
	 * @param gl Le GraphicalLattice pour lequel ce panneau affiche les attributs et objets
	 * @param lp Le LatticePanel dans lequel est affiché le treillis
	 */
	public Projection(GraphicalLattice gl, LatticePanel lp) {
		//setDefaultLookAndFeelDecorated(true);
		//setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		//setTitle("Projection");

		setBackground(Color.WHITE);
		lattice = gl;
		viewer = lp;

		/*
		 * Recherche du contexte binaire contenant toutes les relations des treillis imbriqués, donc
		 * du contexte global du treillis le plus profond
		 */
		NestedLattice firstLattice = lattice.getNestedLattice();
		NestedLattice lastLattice = firstLattice;
		while (lastLattice.getTopNestedConcept().getInternalNestedLattice() != null)
			lastLattice = lastLattice.getTopNestedConcept().getInternalNestedLattice();
		BinaryContext globalContext = lastLattice.getGlobalContext();

		/* Initialisation de l'intention choisie avec tous les attributs du contexte */
		Vector<String> tempAttributes = globalContext.getAttributes();
		intent = new BasicSet();
		for (int i = 0; i < tempAttributes.size(); i++)
			intent.add(tempAttributes.elementAt(i));

		/* Initialisation de l'extention choisie avec tous les objets du contexte */
		Vector<String> tempObjects = globalContext.getObjects();
		extent = new BasicSet();
		for (int i = 0; i < tempObjects.size(); i++)
			extent.add(tempObjects.elementAt(i));

		/* Construction du panneau de sélection des attributs */
		attributesPanel = new JPanel();
		attributesPanel.setLayout(new GridBagLayout());
		attributesPanel.setBackground(Color.WHITE);

		TitledBorder attPanelTitle = new TitledBorder(BorderFactory.createLineBorder(Color.BLACK), GUIMessages.getString("GUI.selectAttributes")); //$NON-NLS-1$
		attPanelTitle.setTitleColor(Color.BLACK);
		attributesPanel.setBorder(attPanelTitle);

		GridBagConstraints gc = new GridBagConstraints();
		int gridy = -1;
		gc.insets = new Insets(2, 5, 2, 5);
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;

		/* Construction et ajout des boîtes de sélection des attributs */
		attributeCheckBoxes = new Vector<JCheckBox>();
		attributes = new Vector<String>();
		Iterator<String> attIt = intent.iterator();
		while (attIt.hasNext()) {
			String fa = attIt.next();
			JCheckBox newCheckBox = new JCheckBox(fa.toString());
			newCheckBox.setSelected(true);
			newCheckBox.setBackground(Color.WHITE);
			newCheckBox.setPreferredSize(new Dimension(130, 20));
			newCheckBox.addActionListener(this);
			attributeCheckBoxes.add(newCheckBox);
			gridy++;
			gc.gridy = gridy;
			attributesPanel.add(newCheckBox, gc);
			attributes.add(fa);
		}

		/* Construction du panneau de sélection des objets */
		objectsPanel = new JPanel();
		objectsPanel.setLayout(new GridBagLayout());
		objectsPanel.setBackground(Color.WHITE);

		TitledBorder objPanelTitle = new TitledBorder(BorderFactory.createLineBorder(Color.BLACK), GUIMessages.getString("GUI.projectObjects")); //$NON-NLS-1$
		objPanelTitle.setTitleColor(Color.BLACK);
		objectsPanel.setBorder(objPanelTitle);

		gc = new GridBagConstraints();
		gridy = -1;
		gc.insets = new Insets(2, 5, 2, 5);
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;

		/* Construction et ajout des boîtes de sélection des attributs */
		objectCheckBoxes = new Vector<JCheckBox>();
		objects = new Vector<String>();
		Iterator<String> objIt = extent.iterator();
		while (objIt.hasNext()) {
			String fo = objIt.next();
			JCheckBox newCheckBox = new JCheckBox(fo.toString());
			newCheckBox.setSelected(true);
			newCheckBox.setBackground(Color.WHITE);
			newCheckBox.setPreferredSize(new Dimension(130, 20));
			newCheckBox.addActionListener(this);
			objectCheckBoxes.add(newCheckBox);
			gridy++;
			gc.gridy = gridy;
			objectsPanel.add(newCheckBox, gc);
			objects.add(fo);
		}

		/* Construction du panneau global */
		//JPanel panel = new JPanel();
		//panel.setLayout(new GridBagLayout());
		setLayout(new GridBagLayout());

		//TitledBorder panelTitle = new TitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Select the desired elements : ");
		//panelTitle.setTitleColor(Color.BLACK);
		//panel.setBorder(panelTitle);

		projectionOnTheFly = new JCheckBox(GUIMessages.getString("GUI.projectOnTheFly")); //$NON-NLS-1$
		projectionOnTheFly.setBackground(Color.WHITE);
		projectionOnTheFly.addActionListener(this);
		projectionOnTheFly.setToolTipText(GUIMessages.getString("GUI.doProjectionWhenSelection")); //$NON-NLS-1$
		projectionOnTheFly.setSelected(true);

		updateBtn = new JButton(LMIcons.getOK());
		updateBtn.addActionListener(this);
		updateBtn.setToolTipText(GUIMessages.getString("GUI.execute")); //$NON-NLS-1$
		updateBtn.setEnabled(false);

		/*
		 * Ajout des panneaux pour les attributs et pour les objets au même endroit : un seul des
		 * deux sera visible à la fois
		 */
		gc = new GridBagConstraints();
		gc.gridwidth = 1;
		gc.weighty = 0.0;
		gc.fill = GridBagConstraints.NONE;

		gc.insets = new Insets(5, 5, 5, 0);
		gc.gridy = 0;
		gc.gridx = 0;
		//gc.anchor = GridBagConstraints.EAST;
		gc.anchor = GridBagConstraints.CENTER;
		add(projectionOnTheFly, gc);

		gc.gridy = 1;
		gc.gridx = 0;
		//gc.anchor = GridBagConstraints.EAST;
		gc.anchor = GridBagConstraints.CENTER;
		add(updateBtn, gc);

		//gc.insets = new Insets(5,0,5,5);
		//gc.gridy = 0;
		//gc.gridx = 1;
		//gc.anchor = GridBagConstraints.WEST;
		//panel.add(cancelBtn, gc);

		/* Ajout des panneaux pour les attributs et pour les objets */
		gc.insets = new Insets(2, 5, 2, 5);
		gc.weighty = 0.0;
		gc.weightx = 0.0;
		gc.anchor = GridBagConstraints.NORTHWEST;

		//gc.gridy = 1;
		//gc.gridx = 0;
		//add(attributesBtn, gc);

		//gc.gridy = 3;
		//gc.gridx = 0;
		//add(objectsBtn, gc);

		gc.gridx = 0;
		gc.gridy = 2;
		add(attributesPanel, gc);

		gc.weighty = 1.0;
		gc.gridx = 0;
		gc.gridy = 3;
		add(objectsPanel, gc);

		//JScrollPane scrollPane = new JScrollPane(panel);
		//scrollPane.setBounds(0,0,200,500);
		//getContentPane().setLayout(new BorderLayout(0,0));
		//getContentPane().add(scrollPane, BorderLayout.CENTER);
		//pack();
		//setVisible(false);
	}

	/**
	 * Crée le treillis qui correspond à la projection sur les attributs et objets sélectionnés
	 * @return Le NestedGraphLattice créé par projection
	 */
	public GraphicalLattice createGraphicalLattice() throws AlreadyExistsException, InvalidTypeException {
		/* Création de l'intention contenant les attributs choisis */
		intent = new BasicSet();
		for (int i = 0; i < attributeCheckBoxes.size(); i++) {
			if (attributeCheckBoxes.elementAt(i).isSelected())
				intent.add(attributes.elementAt(i));
		}

		/* Création de l'extention contenant les objets choisis */
		extent = new BasicSet();
		for (int i = 0; i < objectCheckBoxes.size(); i++) {
			if (objectCheckBoxes.elementAt(i).isSelected())
				extent.add(objects.elementAt(i));
		}

		if (intent.size() == 0 || extent.size() == 0)
			return lattice;

		/* Création d'un vecteur contenant la liste des treillis imbriqués avant la projection */
		ConceptLattice firstLattice = lattice.getNestedLattice().getConceptLattice();
		Vector<ConceptLattice> initialLatticesList = new Vector<ConceptLattice>();
		initialLatticesList.add(firstLattice);
		initialLatticesList.addAll(lattice.getNestedLattice().getInternalLattices());

		/* Création d'un vecteur contenant la liste des structures de treillis avant la projection */
		Vector<LatticeStructure> initialStructuresList = new Vector<LatticeStructure>();
		initialStructuresList.add(lattice.getLatticeStructure());
		initialStructuresList.addAll(lattice.getInternalLatticeStructures());

		/*
		 * Exécution de la projection sur chacun des treillis imbriqués et construction des vecteurs
		 * contenant les treillis et structures résultant de la projection
		 */
		Vector<ConceptLattice> latticesList = new Vector<ConceptLattice>();
		Vector<LatticeStructure> structuresList = new Vector<LatticeStructure>();
		for (int i = 0; i < initialLatticesList.size(); i++) {
			ConceptLattice currentLattice = initialLatticesList.elementAt(i);
			LatticeStructure currentStructure = initialStructuresList.elementAt(i);
			BasicSet bottomIntent = currentLattice.getBottomConcept().getIntent().intersection(intent);
			BasicSet topExtent = currentLattice.getTopConcept().getExtent().intersection(extent);

			/* Projection sur les attributs et sur les objets */
			if (!bottomIntent.equals(currentLattice.getBottomConcept().getIntent())
					&& !topExtent.equals(currentLattice.getTopConcept().getExtent())) {

				// Projection sur les attributs de currentLattice
				BasicAttributeProjection attProjection = new BasicAttributeProjection(currentLattice);
				ConceptLattice tempLattice = attProjection.perform(bottomIntent);

				// Projection sur les objets de tempLattice (projection des attributs effectuées)
				BasicObjectProjection objProjection = new BasicObjectProjection(tempLattice);
				//				ImprovedObjectProjection objProjection = new ImprovedObjectProjection(tempLattice);
				ConceptLattice newLattice = objProjection.perform(topExtent);

				if (newLattice.getContext().getObjectCount() > 0 && newLattice.getContext().getAttributeCount() > 0) {
					latticesList.add(newLattice);
					BinaryContext newContext = newLattice.getContext();
					structuresList.add(new LatticeStructure(newLattice, newContext, LatticeStructure.BEST));
				}
			}

			/* Projection sur les attributs seulement */
			else if (!bottomIntent.equals(currentLattice.getBottomConcept().getIntent())) {

				BasicAttributeProjection projection = new BasicAttributeProjection(currentLattice);
				//				ImprovedAttributeProjection projection = new ImprovedAttributeProjection(currentLattice);
				ConceptLattice newLattice = projection.perform(bottomIntent);

				if (newLattice.getContext().getObjectCount() > 0 && newLattice.getContext().getAttributeCount() > 0) {
					latticesList.add(newLattice);
					BinaryContext newContext = newLattice.getContext();
					structuresList.add(new LatticeStructure(newLattice, newContext, LatticeStructure.BEST));
				}
			}

			/* Projection sur les objets seulement */
			else if (!topExtent.equals(currentLattice.getTopConcept().getExtent())) {

				BasicObjectProjection projection = new BasicObjectProjection(currentLattice);
				//				ImprovedObjectProjection projection = new ImprovedObjectProjection(tempLattice);
				ConceptLattice newLattice = projection.perform(topExtent);

				if (newLattice.getContext().getObjectCount() > 0 && newLattice.getContext().getAttributeCount() > 0) {
					latticesList.add(newLattice);
					BinaryContext newContext = newLattice.getContext();
					structuresList.add(new LatticeStructure(newLattice, newContext, LatticeStructure.BEST));
				}
			}

			/* Aucune projection */
			else {
				latticesList.add(currentLattice);
				structuresList.add(currentStructure);
			}
		}

		/* Construction du nouveau treillis graphique résultant de la projection */
		NestedLattice newLattice = new NestedLattice(null, latticesList, null, GUIMessages.getString("GUI.projection")); //$NON-NLS-1$
		GraphicalLattice newGraphicalLattice = new GraphicalLattice(newLattice, null, structuresList);

		return newGraphicalLattice;
	}

	/* ======== ACTIONLISTENER INTERFACE ======== */

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == projectionOnTheFly) {
			if (projectionOnTheFly.isSelected())
				updateBtn.setEnabled(false);
			else
				updateBtn.setEnabled(true);
		}

		// Bouton d'exécution de l'approximation OU une case a été cochée et approximation "a la volée" actif
		else if ((e.getSource() == updateBtn)
				|| ((e.getSource() instanceof JCheckBox) && projectionOnTheFly.isSelected())) {
			try {
				GraphicalLattice displayedLattice = createGraphicalLattice();
				viewer.changeDisplayedLattice(displayedLattice, LMHistory.LATTICE_PROJECTION);
				viewer.lockHistory();
				viewer.setSelectedNodes(new Vector<GraphicalConcept>());
				viewer.unlockHistory();
				viewer.repaint();
			} catch (LatticeMinerException error) {
				DialogBox.showMessageError(this, error);
			}
		}
	}
}