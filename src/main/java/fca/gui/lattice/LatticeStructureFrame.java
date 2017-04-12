package fca.gui.lattice;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;

import fca.core.context.nested.NestedContext;
import fca.core.lattice.ConceptLattice;
import fca.core.util.BasicSet;
import fca.gui.lattice.element.ConceptLabel;
import fca.gui.lattice.element.ConceptPosition;
import fca.gui.lattice.element.GraphicalConcept;
import fca.gui.lattice.element.GraphicalLattice;
import fca.gui.lattice.element.LatticeStructure;
import fca.gui.util.DialogBox;
import fca.gui.util.constant.LMIcons;
import fca.gui.util.constant.LMOptions;
import fca.messages.GUIMessages;

/**
 * Cette classe ouvre une fenêtre pour visualiser/modifier de la structure de chacun des treillis
 * d'un treillis imbriqué
 * @author Geneviève Roberge
 * @version 1.0
 */
public class LatticeStructureFrame extends JPanel implements MouseListener, MouseMotionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 386535487394260308L;

	private JFrame frame; //La fenêtre contenant ce panneau

	private LatticePanel baseViewer; //Le viewer de base dans lequel est affiché le treillis imbriqué

	LatticeStructureFrame thisPanel;

	private Rectangle2D selectedArea; //Le rectangle de sélection du panneau

	/* Variables de treillis */

	private GraphicalLattice currentLattice; //Le treillis actuellement en traitement
	private Vector<GraphicalLattice> latticesList; //La liste des treillis imbriqués

	private int currentLatticeIdx; //L'index du treillis en traitement dans la liste des treillis

	/* Variables de gestion des déplacements */

	private Point mousePos; //Position du clic souris

	private boolean dragLattice; //Indique si le treillis est en déplacement ou non

	private ConceptLabel labelMoving; //Étiquette en déplacement

	private GraphicalConcept nodeMoving; //Noeud en déplacement

	private double upperLimit; //Position maximale en x du noeud déplacé

	private double lowerLimit; //Position minimale en x du noeud déplacé

	private Point2D basePoint; //Position de la racine avant le déplacement

	/* Popup menu pour le zoom et les étiquettes */

	private JPopupMenu labelsPopupMenu; //Le popup menu de la zone sélectionnée

	private JMenuItem zoomAreaItem; //Le menu pour le grossissement de la zone sélectionnée

	private JMenuItem showAreaLabelsItem; //Le menu pour l'affichage des étiquettes

	private JMenuItem hideAreaLabelsItem; //Le menu pour cacher les étiquettes

	/* Composants graphiques pour la barre de contrôle */

	private ControlListener listener; //Le gestionnaire d'evenements

	private JButton previousBtn; //Bouton de naviagation vers le treillis précédant

	private JButton nextBtn; //Bouton de naviagation vers le treillis suivant

	private JButton finishBtn; //Bouton d'acceptation des modifications

	private JButton cancelBtn; //Bouton d'annulation des modifications

	private JButton zoomInBtn; //Bouton de grossissement du treillis

	private JButton zoomOutBtn; //Bouton de réduction du treillis

	private JButton zoomAreaBtn; //Bouton de grossissement de la sélection

	private JButton noZoomBtn; //Bouton de vue entiere du treillis

	private JComboBox objValueTypeList; //Liste des valeurs d'étiquette pour les objets

	private JComboBox attValueTypeList; //Liste des valeurs d'étiquette pour les attributs

	private JComboBox<String> templateTypeList; //Liste des template possibles pour la structure du treillis

	/* Variables pour les animations */

	private Timer timer; //Timer pour la vitesse des animations

	private int animCounter; //Compteur pour le nombre d'itérations

	/**
	 * Contructeur
	 * @param viewer Le NestedLatticeViewer qui contient le treillis imbriqué à traiter
	 * @param list Le Vector contenant la liste des treillis imbriqués
	 */
	public LatticeStructureFrame(LatticePanel viewer, Vector<GraphicalLattice> list) {
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(575, 575));
		thisPanel = this;

		baseViewer = viewer;
		latticesList = list;
		currentLatticeIdx = 0;
		currentLattice = latticesList.elementAt(currentLatticeIdx);
		/* Permet de s'assurer que la taille des concepts est la bonne */
		currentLattice.setRootPosition(currentLattice.getRootPosition().getX(), currentLattice.getRootPosition().getY());

		listener = null;
		mousePos = new Point(0, 0);
		dragLattice = false;

		addMouseListener(this);
		addMouseMotionListener(this);

		/* Crée le popup menu pour les étiquettes d'un rectangle choisi */
		labelsPopupMenu = new JPopupMenu();
		zoomAreaItem = new JMenuItem(GUIMessages.getString("GUI.zoomInSelectedArea")); //$NON-NLS-1$
		labelsPopupMenu.add(zoomAreaItem);
		zoomAreaItem.addActionListener(new PopupListener());
		showAreaLabelsItem = new JMenuItem(GUIMessages.getString("GUI.showLabelsInSelectedArea")); //$NON-NLS-1$
		labelsPopupMenu.add(showAreaLabelsItem);
		showAreaLabelsItem.addActionListener(new PopupListener());
		hideAreaLabelsItem = new JMenuItem(GUIMessages.getString("GUI.hideLabelsInSelectedArea")); //$NON-NLS-1$
		labelsPopupMenu.add(hideAreaLabelsItem);
		hideAreaLabelsItem.addActionListener(new PopupListener());
		labelsPopupMenu.setLightWeightPopupEnabled(false);

		/* Ouvre une fenêtre contenant ce panneau */
		openViewerFrame();
	}

	JToolBar buildExecutionBar() {
		JToolBar controlBar = new JToolBar(GUIMessages.getString("GUI.controls")); //$NON-NLS-1$
		if (listener == null)
			listener = new ControlListener();
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

		previousBtn = new JButton(LMIcons.getPrevious());
		//previousBtn.setPreferredSize(new Dimension(75,25));
		previousBtn.addActionListener(listener);
		previousBtn.setToolTipText(GUIMessages.getString("GUI.previousLattice")); //$NON-NLS-1$
		previousBtn.setEnabled(false);
		controlBar.add(previousBtn);

		nextBtn = new JButton(LMIcons.getNext());
		//nextBtn.setPreferredSize(new Dimension(75,25));
		nextBtn.addActionListener(listener);
		nextBtn.setToolTipText(GUIMessages.getString("GUI.nextLattice")); //$NON-NLS-1$
		if (latticesList.size() < 2)
			nextBtn.setEnabled(false);
		controlBar.add(nextBtn);

		controlBar.add(Box.createHorizontalGlue());
		controlBar.addSeparator();

		zoomInBtn = new JButton(LMIcons.getZoomIn());
		zoomInBtn.addActionListener(listener);
		zoomInBtn.setToolTipText(GUIMessages.getString("GUI.zoomIn")); //$NON-NLS-1$
		controlBar.add(zoomInBtn);

		zoomOutBtn = new JButton(LMIcons.getZoomOut());
		zoomOutBtn.addActionListener(listener);
		zoomOutBtn.setToolTipText(GUIMessages.getString("GUI.zoomOut")); //$NON-NLS-1$
		controlBar.add(zoomOutBtn);

		zoomAreaBtn = new JButton(LMIcons.getFitZoomToSelection());
		zoomAreaBtn.addActionListener(listener);
		zoomAreaBtn.setToolTipText(GUIMessages.getString("GUI.zoomInSelectedArea")); //$NON-NLS-1$
		controlBar.add(zoomAreaBtn);

		noZoomBtn = new JButton(LMIcons.getNoZoom());
		noZoomBtn.addActionListener(listener);
		noZoomBtn.setToolTipText(GUIMessages.getString("GUI.showEntireLattice")); //$NON-NLS-1$
		controlBar.add(noZoomBtn);

		controlBar.addSeparator();
		controlBar.add(Box.createHorizontalGlue());

		finishBtn = new JButton(LMIcons.getOK());
		finishBtn.addActionListener(listener);
		finishBtn.setToolTipText(GUIMessages.getString("GUI.applyChangesAndQuit")); //$NON-NLS-1$
		controlBar.add(finishBtn);

		cancelBtn = new JButton(LMIcons.getCancel());
		cancelBtn.addActionListener(listener);
		cancelBtn.setToolTipText(GUIMessages.getString("GUI.cancelChangesAndQuit")); //$NON-NLS-1$
		controlBar.add(cancelBtn);

		return controlBar;
	}

	JToolBar buildControlBar() {
		JToolBar controlBar = new JToolBar(GUIMessages.getString("GUI.controls")); //$NON-NLS-1$
		if (listener == null)
			listener = new ControlListener();
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

		String[] objValueTypeOptions = { GUIMessages.getString("GUI.dontShowObjects"), GUIMessages.getString("GUI.list"), GUIMessages.getString("GUI.reducedLabelling"), GUIMessages.getString("GUI.count"), GUIMessages.getString("GUI.purcentOfObjects") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		objValueTypeList = new JComboBox<>(objValueTypeOptions);
		objValueTypeList.setSelectedIndex(4);
		objValueTypeList.setLightWeightPopupEnabled(false);
		objValueTypeList.addActionListener(listener);
		controlBar.add(objValueTypeList);

		controlBar.addSeparator();

		String[] attValueTypeOptions = { GUIMessages.getString("GUI.dontShowAttributes"), GUIMessages.getString("GUI.list"), GUIMessages.getString("GUI.reducedLabelling") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		attValueTypeList = new JComboBox<>(attValueTypeOptions);
		attValueTypeList.setSelectedIndex(2);
		attValueTypeList.setLightWeightPopupEnabled(false);
		attValueTypeList.addActionListener(listener);
		controlBar.add(attValueTypeList);

		controlBar.addSeparator();

		ConceptLattice currentConceptLattice = currentLattice.getNestedLattice().getConceptLattice();
		Vector<String> templateTypeOptions = new Vector<String>();
		templateTypeOptions.add(GUIMessages.getString("GUI.noTemplate")); //$NON-NLS-1$
		templateTypeOptions.add(GUIMessages.getString("GUI.automatic")); //$NON-NLS-1$
		if (LatticeStructure.isCubicAvailable(currentConceptLattice))
			templateTypeOptions.add(GUIMessages.getString("GUI.cubicShape")); //$NON-NLS-1$
		//if(LatticeStructure.isAdditiveAvailable(currentConceptLattice))
		//  templateTypeOptions.add("Additive Line Diagram");
		if (LatticeStructure.isParentDrivenAvailable(currentConceptLattice))
			templateTypeOptions.add(GUIMessages.getString("GUI.parentPosition")); //$NON-NLS-1$
		if (LatticeStructure.isIntersectionAvailable(currentConceptLattice))
			templateTypeOptions.add(GUIMessages.getString("GUI.intersections")); //$NON-NLS-1$
		if (LatticeStructure.isIntentAvailable(currentConceptLattice))
			templateTypeOptions.add(GUIMessages.getString("GUI.sortedByIntentSize")); //$NON-NLS-1$
		if (LatticeStructure.isHighestAvailable(currentConceptLattice))
			templateTypeOptions.add(GUIMessages.getString("GUI.highestPossibleLevel")); //$NON-NLS-1$

		templateTypeList = new JComboBox<>(templateTypeOptions);
		templateTypeList.setSelectedIndex(0);
		templateTypeList.setLightWeightPopupEnabled(false);
		templateTypeList.addActionListener(listener);
		controlBar.add(templateTypeList);

		return controlBar;
	}

	/**
	 * Permet d'ajuster le facteur de grossissement du treillis à la zone choisie
	 * @param area Le Rectangle2D qui délimite la zone du treillis à grossir
	 */
	public void zoomInArea(Rectangle2D area) {
		double currentScale = currentLattice.getScale();
		Point2D refPoint = new Point2D.Double(currentLattice.getTopNode().getShape().getX(),
				currentLattice.getTopNode().getShape().getY());

		/* Rend fixe la position relative actuelle des noeuds en créant une nouvelle structure */
		Vector<ConceptPosition> currentLatticePositions = new Vector<ConceptPosition>();
		Vector<GraphicalConcept> currentNodesList = currentLattice.getNodesList();
		for (int j = 0; j < currentNodesList.size(); j++) {
			GraphicalConcept currentNode = currentNodesList.elementAt(j);
			BasicSet intent = currentNode.getNestedConcept().getConcept().getIntent();
			double relX = (currentNode.getShape().getX() - refPoint.getX()) / currentScale;
			double relY = (currentNode.getShape().getY() - refPoint.getY()) / currentScale;

			ConceptPosition newPosition = new ConceptPosition(intent, relX, relY);
			currentLatticePositions.add(newPosition);
		}

		LatticeStructure newStructure = new LatticeStructure(currentLatticePositions);
		Vector<LatticeStructure> newStructuresList = new Vector<LatticeStructure>();
		newStructuresList.add(newStructure);
		currentLattice.setLatticeStructures(newStructuresList);
		currentLattice.resetLatticeImage(refPoint.getX(), refPoint.getY(), currentScale);

		/* Grossissement du treillis au premier niveau pour voir la zone choisie */
		//currentLattice.zoomInArea(new Rectangle2D.Double(0,0,getSize().getWidth(), getSize().getHeight()), area, this);
		/*
		 * Ajustement du rectangle de la nouvelle zone pour le rendre proportionnelle à l'espace
		 * d'affichage
		 */
		double scaleX = getWidth() / area.getWidth();
		double scaleY = getHeight() / area.getHeight();

		//double finalScale;
		if (scaleX < scaleY) {
			double ajustY = (getHeight() / scaleX) - area.getHeight();
			area.setRect(area.getX(), area.getY() - (ajustY / 2), area.getWidth(), area.getHeight() + ajustY);
		} else {
			double ajustX = (getWidth() / scaleY) - area.getWidth();
			area.setRect(area.getX() - (ajustX / 2), area.getY(), area.getWidth() + ajustX, area.getHeight());
		}

		final JPanel panel = this;
		final GraphicalLattice lattice = currentLattice;

		final double stepX = (area.getX() / lattice.getScale()) / 10.0;
		final double stepY = (area.getY() / lattice.getScale()) / 10.0;
		final double stepWidth = ((getWidth() - area.getWidth()) / lattice.getScale()) / 10.0;
		final double stepHeight = ((getHeight() - area.getHeight()) / lattice.getScale()) / 10.0;
		final double initWidth = getWidth() / lattice.getScale();
		final double initHeight = getHeight() / lattice.getScale();

		ActionListener animZoom = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double x = stepX * lattice.getScale();
				double y = stepY * lattice.getScale();
				double areaWidth = (initWidth - (animCounter + 1) * stepWidth) * lattice.getScale();
				double areaHeight = (initHeight - (animCounter + 1) * stepHeight) * lattice.getScale();
				Rectangle2D newArea = new Rectangle2D.Double(x, y, areaWidth, areaHeight);

				lattice.zoomInArea(new Rectangle2D.Double(0, 0, getSize().getWidth(), getSize().getHeight()), newArea);
				panel.repaint();

				animCounter++;
				if (animCounter >= 10)
					timer.stop();
			}
		};

		/* Attente de la fin d'exécution des autres animations */
		while (timer != null && timer.isRunning())
			;

		animCounter = 0;
		timer = new Timer(25, animZoom);
		timer.setInitialDelay(0);
		timer.start();
	}

	/**
	 * Permet d'afficher toutes les étiquettes de la zone choisie
	 * @param area Le Rectangle2D qui délimite la zone du treillis dans laquelle les étiquettes
	 *        doivent être affichées
	 */
	public void showAreaLabels(Rectangle2D area) {
		currentLattice.showAreaLabels(area);
	}

	/**
	 * Permet de cacher toutes les étiquettes de la zone choisie
	 * @param area Le Rectangle2D qui délimite la zone du treillis dans laquelle les étiquettes
	 *        doivent être cachées
	 */
	public void hideAreaLabels(Rectangle2D area) {
		currentLattice.hideAreaLabels(area);
	}

	/**
	 * Ouvre une fenêtre contenant ce panneau
	 */
	public JFrame openViewerFrame() {
		frame = new JFrame(GUIMessages.getString("GUI.latticeStructureViewer")); //$NON-NLS-1$
		//JFrame.setDefaultLookAndFeelDecorated(true);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		ScrollPane viewerScrollPane = new ScrollPane(ScrollPane.SCROLLBARS_NEVER);
		viewerScrollPane.setBounds(0, 0, 575, 600);
		viewerScrollPane.add(this);
		frame.getContentPane().add(viewerScrollPane, BorderLayout.CENTER);
		frame.getContentPane().add(buildControlBar(), BorderLayout.NORTH);
		frame.getContentPane().add(buildExecutionBar(), BorderLayout.SOUTH);

		frame.pack();
		frame.setVisible(true);

		return frame;
	}

	/* ======== MOUSELISTENER INTERFACE ======== */

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		if (selectedArea != null) {
			/* Affichage du popup menu de zoom/étiquettes */
			if (selectedArea.contains(e.getPoint()) && e.getClickCount() == 1) {
				labelsPopupMenu.show(this, e.getX(), e.getY());
			}

			/* Annulation du rectangle de sélection */
			else {
				selectedArea = null;
				repaint();
			}
		}

		else {
			GraphicalConcept clickedNode = currentLattice.mouseClicked(e.getX(), e.getY());

			/* Affichage des étiquettes */
			if (clickedNode != null && e.getButton() == MouseEvent.BUTTON3) {
				clickedNode.toggleLabel();
			}
			repaint();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		if (selectedArea == null) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				Object clickedObject = currentLattice.mousePressed(e.getX(), e.getY());

				/* Déplacement d'une étiquette */
				if (clickedObject != null && clickedObject instanceof ConceptLabel) {
					mousePos.setLocation(e.getX(), e.getY());
					labelMoving = (ConceptLabel) clickedObject;
				}
				/* Déplacement d'un noeud */
				else if (clickedObject != null && clickedObject instanceof GraphicalConcept) {
					mousePos.setLocation(e.getX(), e.getY());
					nodeMoving = (GraphicalConcept) clickedObject;

					/* Recherche des bornes supérieure et inférieure de déplacement */
					GraphicalLattice parentLattice = nodeMoving.getParentLattice();
					basePoint = new Point2D.Double(parentLattice.getRootPosition().getX(),
							parentLattice.getRootPosition().getY());
					upperLimit = nodeMoving.getLowestParentY() + (1.8 * parentLattice.getScale());
					lowerLimit = nodeMoving.getHighestChildY() - (1.8 * parentLattice.getScale());
				}
				/* Déplacement du treillis en entier */
				else {
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
					mousePos.setLocation(e.getX(), e.getY());
					dragLattice = true;

					/*
					 * Établissement de la structure actuelle pour la conserver pendant le
					 * déplacement
					 */
					double currentScale = currentLattice.getScale();
					Point2D refPoint = new Point2D.Double(currentLattice.getTopNode().getShape().getX(),
							currentLattice.getTopNode().getShape().getY());

					Vector<ConceptPosition> currentLatticePositions = new Vector<ConceptPosition>();
					Vector<GraphicalConcept> currentNodesList = currentLattice.getNodesList();
					for (int j = 0; j < currentNodesList.size(); j++) {
						GraphicalConcept currentNode = currentNodesList.elementAt(j);
						BasicSet intent = currentNode.getNestedConcept().getConcept().getIntent();
						double relX = (currentNode.getShape().getX() - refPoint.getX()) / currentScale;
						double relY = (currentNode.getShape().getY() - refPoint.getY()) / currentScale;

						ConceptPosition newPosition = new ConceptPosition(intent, relX, relY);
						currentLatticePositions.add(newPosition);
					}

					LatticeStructure newStructure = new LatticeStructure(currentLatticePositions);
					Vector<LatticeStructure> newStructuresList = new Vector<LatticeStructure>();
					newStructuresList.add(newStructure);
					currentLattice.setLatticeStructures(newStructuresList);
					currentLattice.resetLatticeImage(refPoint.getX(), refPoint.getY(), currentScale);
				}
			}
			/* Création d'une zone de sélection */
			else if (e.getButton() == MouseEvent.BUTTON3) {
				selectedArea = new Rectangle2D.Double(e.getX(), e.getY(), 0, 0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		/* Fin du déplacement de l'étiquette en déplacement */
		if (labelMoving != null) {
			mousePos.setLocation(0, 0);
			labelMoving = null;
		}

		/* Fin du déplacement du noeud en déplacement */
		else if (nodeMoving != null) {
			GraphicalLattice parentLattice = nodeMoving.getParentLattice();

			/* Repositionnement du noeud sur un niveau */
			double newY = nodeMoving.getShape().getY();
			int newRelY = (int) Math.round((newY - basePoint.getY()) / (parentLattice.getScale()/* *2 */));
			double correctY = newRelY * parentLattice.getScale()/* *2 */+ basePoint.getY();

			/* Repositionnement du noeud en x */
			double newX = nodeMoving.getShape().getX();
			int newRelX = (int) Math.round((newX - basePoint.getX()) / parentLattice.getScale());
			double correctX = newRelX * parentLattice.getScale() + basePoint.getX();

			nodeMoving.setPosition(correctX, correctY);
			repaint();

			mousePos.setLocation(0, 0);
			nodeMoving = null;
		}

		/* Annulation de la construction d'une zone de sélection si elle est trop petite */
		else if (selectedArea != null && selectedArea.getWidth() < 2 && selectedArea.getHeight() < 2) {
			selectedArea = null;
			repaint();
		}

		/* Fin du déplacement du treillis */
		else if (dragLattice) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			mousePos.setLocation(0, 0);
			dragLattice = false;
		}
	}

	/* ======== MOUSEMOTIONLISTENER INTERFACE ======== */

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		/* Déplacement d'une étiquette */
		if (labelMoving != null) {
			int decX = (e.getX() - mousePos.x);
			int decY = (e.getY() - mousePos.y);
			mousePos.x = e.getX();
			mousePos.y = e.getY();

			double labelX = labelMoving.getShape().getX();
			double labelY = labelMoving.getShape().getY();
			labelMoving.setPosition(labelX + decX, labelY + decY);
			repaint();
		}

		/* Déplacement d'un noeud */
		else if (nodeMoving != null) {
			/* Déplacement en x et en y */
			if (e.getY() < lowerLimit && e.getY() > upperLimit) {
				int decX = (e.getX() - mousePos.x);
				int decY = (e.getY() - mousePos.y);
				mousePos.x = e.getX();
				mousePos.y = e.getY();

				double nodeX = nodeMoving.getShape().getX();
				double nodeY = nodeMoving.getShape().getY();
				nodeMoving.setPosition(nodeX + decX, nodeY + decY);
				repaint();
			}

			/* Déplacement en x seulement */
			else {
				int decX = (e.getX() - mousePos.x);

				// En X seulement
				//int decY = (e.getY() - mousePos.y);
				mousePos.x = e.getX();
				mousePos.y = e.getY();

				double nodeX = nodeMoving.getShape().getX();
				double nodeY = nodeMoving.getShape().getY();

				nodeMoving.setPosition(nodeX + decX, nodeY);
				repaint();
			}
		}

		/* Construction de la zone de sélection */
		else if (selectedArea != null) {
			double newWidth = e.getX() - selectedArea.getX();
			double newHeight = e.getY() - selectedArea.getY();
			double rectX = selectedArea.getX();
			double rectY = selectedArea.getY();

			selectedArea.setRect(rectX, rectY, newWidth, newHeight);
			repaint();
		}

		/* Déplacement du treillis */
		else if (dragLattice) {
			int decX = (e.getX() - mousePos.x);
			int decY = (e.getY() - mousePos.y);
			mousePos.x = e.getX();
			mousePos.y = e.getY();

			Point2D rootPosition = currentLattice.getRootPosition();
			currentLattice.setRootPosition(rootPosition.getX() + decX, rootPosition.getY() + decY);

			repaint();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
	}

	/* ======== AFFICHAGE ======== */

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		/* Affichage du treillis et de ses étiquettes */
		currentLattice.paint(g2, new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
		currentLattice.paintLabels(g2, new Rectangle2D.Double(0, 0, getWidth(), getHeight()));

		/* Affichage de la zone de sélection */
		if (selectedArea != null) {
			g2.setPaint(Color.LIGHT_GRAY);
			g2.draw(selectedArea);
		}
	}

	/**
	 * Cette classe gère les événements liés au popup menu de la zone sélectionnée
	 */
	private class PopupListener implements ActionListener {
		/* ======== ACTIONLISTENER INTERFACE ======== */

		/*
		 * (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			/* Élément du menu pour le zoom dans la zone sélectionnée */
			if (e.getSource() == zoomAreaItem) {
				zoomInArea(selectedArea);
				selectedArea = null;
				repaint();
			}

			/* Élément du menu pour l'affichage des étiquettes dans la zone sélectionnée */
			else if (e.getSource() == showAreaLabelsItem) {
				showAreaLabels(selectedArea);
				selectedArea = null;
				repaint();
			}

			/* Élément du menu pour cacher les étiquettes dans la zone sélectionnée */
			else if (e.getSource() == hideAreaLabelsItem) {
				hideAreaLabels(selectedArea);
				selectedArea = null;
				repaint();
			}
		}
	}

	/**
	 * Cette classe gère les événements liés au panneau de contrôle
	 */
	private class ControlListener implements ActionListener {
		/* ======== ACTIONLISTENER INTERFACE ======== */

		/*
		 * (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			/* Bouton de navigation vers le treillis précédant */
			if (e.getSource() == previousBtn) {
				if (currentLatticeIdx > 0) {
					currentLatticeIdx--;
					currentLattice = latticesList.elementAt(currentLatticeIdx);
					repaint();

					/* Vérification de la disponibilité des templates de structure */
					ConceptLattice currentConceptLattice = currentLattice.getNestedLattice().getConceptLattice();
					currentLattice.setRootPosition(currentLattice.getRootPosition().getX(),
							currentLattice.getRootPosition().getY());

					templateTypeList.removeActionListener(listener);
					templateTypeList.removeAllItems();
					templateTypeList.addItem(GUIMessages.getString("GUI.noTemplate")); //$NON-NLS-1$
					templateTypeList.addItem(GUIMessages.getString("GUI.automatic")); //$NON-NLS-1$
					if (LatticeStructure.isCubicAvailable(currentConceptLattice))
						templateTypeList.addItem(GUIMessages.getString("GUI.cubicShape")); //$NON-NLS-1$
					if (LatticeStructure.isAdditiveAvailable(currentConceptLattice))
						templateTypeList.addItem(GUIMessages.getString("GUI.additiveLineDiagram")); //$NON-NLS-1$
					if (LatticeStructure.isIntentAvailable(currentConceptLattice))
						templateTypeList.addItem(GUIMessages.getString("GUI.sortedByIntentSize")); //$NON-NLS-1$
					if (LatticeStructure.isHighestAvailable(currentConceptLattice))
						templateTypeList.addItem(GUIMessages.getString("GUI.highestPossibleLevel")); //$NON-NLS-1$

					templateTypeList.setSelectedIndex(0);
					templateTypeList.addActionListener(listener);

					/* Vérification de l'existence des treillis précédant et suivant */
					if (currentLatticeIdx == 0)
						previousBtn.setEnabled(false);
					else
						previousBtn.setEnabled(true);

					if (currentLatticeIdx == latticesList.size() - 1)
						nextBtn.setEnabled(false);
					else
						nextBtn.setEnabled(true);
				}
			}

			/* Bouton de navigation vers le treillis suivant */
			else if (e.getSource() == nextBtn) {
				if (currentLatticeIdx < latticesList.size() - 1) {
					currentLatticeIdx++;
					currentLattice = latticesList.elementAt(currentLatticeIdx);
					repaint();

					/* Vérification de la disponibilité des templates de structure */
					ConceptLattice currentConceptLattice = currentLattice.getNestedLattice().getConceptLattice();
					currentLattice.setRootPosition(currentLattice.getRootPosition().getX(),
							currentLattice.getRootPosition().getY());

					templateTypeList.removeActionListener(listener);
					templateTypeList.removeAllItems();
					templateTypeList.addItem(GUIMessages.getString("GUI.noTemplate")); //$NON-NLS-1$
					templateTypeList.addItem(GUIMessages.getString("GUI.automatic")); //$NON-NLS-1$
					if (LatticeStructure.isCubicAvailable(currentConceptLattice))
						templateTypeList.addItem(GUIMessages.getString("GUI.cubicShape")); //$NON-NLS-1$
					//if(LatticeStructure.isAdditiveAvailable(currentConceptLattice))
					//  templateTypeList.addItem("Additive Line Diagram");
					if (LatticeStructure.isParentDrivenAvailable(currentConceptLattice))
						templateTypeList.addItem(GUIMessages.getString("GUI.parentPosition")); //$NON-NLS-1$
					if (LatticeStructure.isIntersectionAvailable(currentConceptLattice))
						templateTypeList.addItem(GUIMessages.getString("GUI.intersections")); //$NON-NLS-1$
					if (LatticeStructure.isIntentAvailable(currentConceptLattice))
						templateTypeList.addItem(GUIMessages.getString("GUI.sortedByIntentSize")); //$NON-NLS-1$
					if (LatticeStructure.isHighestAvailable(currentConceptLattice))
						templateTypeList.addItem(GUIMessages.getString("GUI.highestPossibleLevel")); //$NON-NLS-1$

					templateTypeList.setSelectedIndex(0);
					templateTypeList.addActionListener(listener);

					/* Vérification de l'existence des treillis précédant et suivant */
					if (currentLatticeIdx == latticesList.size() - 1)
						nextBtn.setEnabled(false);
					else
						nextBtn.setEnabled(true);

					if (currentLatticeIdx == 0)
						previousBtn.setEnabled(false);
					else
						previousBtn.setEnabled(true);
				}
			}

			/* Bouton de fin de modification des structures */
			else if (e.getSource() == finishBtn) {
				/*
				 * Fixe la structure actuelle de chacun des treillis et leur assignant une nouvelle
				 * structure
				 */
				Vector<LatticeStructure> newStructuresList = new Vector<LatticeStructure>();
				for (int i = 0; i < latticesList.size(); i++) {
					GraphicalLattice currentLattice = latticesList.elementAt(i);
					double currentScale = currentLattice.getScale();
					Point2D refPoint = new Point2D.Double(currentLattice.getTopNode().getShape().getX(),
							currentLattice.getTopNode().getShape().getY());

					Vector<ConceptPosition> currentLatticePositions = new Vector<ConceptPosition>();
					Vector<GraphicalConcept> currentNodesList = currentLattice.getNodesList();
					for (int j = 0; j < currentNodesList.size(); j++) {
						GraphicalConcept currentNode = currentNodesList.elementAt(j);
						BasicSet intent = currentNode.getNestedConcept().getConcept().getIntent();
						double relX = (currentNode.getShape().getX() - refPoint.getX()) / currentScale;
						double relY = (currentNode.getShape().getY() - refPoint.getY()) / currentScale;

						ConceptPosition newPosition = new ConceptPosition(intent, relX, relY);
						currentLatticePositions.add(newPosition);
					}

					LatticeStructure newStructure = new LatticeStructure(currentLatticePositions);
					newStructuresList.add(newStructure);
				}

				/* Les nouvelles structures sont communiquées aux treillis du viewer de base */
				baseViewer.setLatticeStructures(newStructuresList);
				baseViewer.lockHistory();
				baseViewer.zoomInArea(baseViewer.getBoundsForLattice(baseViewer.getRootLattice()));
				baseViewer.unlockHistory();
				frame.dispose();
				frame.setVisible(false);
				baseViewer.refreshMapViewer();
			}

			/* Bouton d'annulation de modification de structures */
			else if (e.getSource() == cancelBtn) {
				frame.dispose();
				frame.setVisible(false);
			}

			/* Bouton de grossissement du treillis affiché */
			else if (e.getSource() == zoomInBtn) {
				double currentScale = currentLattice.getScale();
				Point2D refPoint = new Point2D.Double(currentLattice.getTopNode().getShape().getX(),
						currentLattice.getTopNode().getShape().getY());

				/* Fixe la structure du treillis actuel pour la conserver après le grossissement */
				Vector<ConceptPosition> currentLatticePositions = new Vector<ConceptPosition>();
				Vector<GraphicalConcept> currentNodesList = currentLattice.getNodesList();
				for (int j = 0; j < currentNodesList.size(); j++) {
					GraphicalConcept currentNode = currentNodesList.elementAt(j);
					BasicSet intent = currentNode.getNestedConcept().getConcept().getIntent();
					double relX = (currentNode.getShape().getX() - refPoint.getX()) / currentScale;
					double relY = (currentNode.getShape().getY() - refPoint.getY()) / currentScale;

					ConceptPosition newPosition = new ConceptPosition(intent, relX, relY);
					currentLatticePositions.add(newPosition);
				}

				LatticeStructure newStructure = new LatticeStructure(currentLatticePositions);
				Vector<LatticeStructure> newStructuresList = new Vector<LatticeStructure>();
				newStructuresList.add(newStructure);
				currentLattice.setLatticeStructures(newStructuresList);
				currentLattice.resetLatticeImage(refPoint.getX(), refPoint.getY(), currentScale);

				/* Grossissement du treillis */
				//final Rectangle2D rect = new Rectangle2D.Double(0,0,getSize().getWidth(),getSize().getHeight());
				final JPanel panel = thisPanel;
				final GraphicalLattice lattice = currentLattice;

				ActionListener animZoom = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						lattice.zoomIn(new Rectangle2D.Double(0, 0, getSize().getWidth(), getSize().getHeight()), 1.1);
						panel.repaint();

						animCounter++;
						if (animCounter >= 10)
							timer.stop();
					}
				};

				/* Attente de la fin d'exécution des autres animations */
				while (timer != null && timer.isRunning())
					;

				animCounter = 0;
				timer = new Timer(25, animZoom);
				timer.setInitialDelay(0);
				timer.start();
			}

			/* Bouton de réduction du treillis affiché */
			else if (e.getSource() == zoomOutBtn) {
				double currentScale = currentLattice.getScale();
				Point2D refPoint = new Point2D.Double(currentLattice.getTopNode().getShape().getX(),
						currentLattice.getTopNode().getShape().getY());

				/* Fixe la structure du treillis actuel pour la conserver après le grossissement */
				Vector<ConceptPosition> currentLatticePositions = new Vector<ConceptPosition>();
				Vector<GraphicalConcept> currentNodesList = currentLattice.getNodesList();
				for (int j = 0; j < currentNodesList.size(); j++) {
					GraphicalConcept currentNode = currentNodesList.elementAt(j);
					BasicSet intent = currentNode.getNestedConcept().getConcept().getIntent();
					double relX = (currentNode.getShape().getX() - refPoint.getX()) / currentScale;
					double relY = (currentNode.getShape().getY() - refPoint.getY()) / currentScale;

					ConceptPosition newPosition = new ConceptPosition(intent, relX, relY);
					currentLatticePositions.add(newPosition);
				}

				LatticeStructure newStructure = new LatticeStructure(currentLatticePositions);
				Vector<LatticeStructure> newStructuresList = new Vector<LatticeStructure>();
				newStructuresList.add(newStructure);
				currentLattice.setLatticeStructures(newStructuresList);
				currentLattice.resetLatticeImage(refPoint.getX(), refPoint.getY(), currentScale);

				/* Réduction du treillis */
				//final Rectangle2D rect = new Rectangle2D.Double(0,0,getSize().getWidth(),getSize().getHeight());
				final JPanel panel = thisPanel;
				final GraphicalLattice lattice = currentLattice;

				ActionListener animZoom = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						lattice.zoomOut(new Rectangle2D.Double(0, 0, getSize().getWidth(), getSize().getHeight()), 1.1);
						panel.repaint();

						animCounter++;
						if (animCounter >= 10)
							timer.stop();
					}
				};

				/* Attente de la fin d'exécution des autres animations */
				while (timer != null && timer.isRunning())
					;

				animCounter = 0;
				timer = new Timer(25, animZoom);
				timer.setInitialDelay(0);
				timer.start();
			}

			/* Bouton de zoom dans le rectangle sélectionné */
			else if (e.getSource() == zoomAreaBtn) {
				if (selectedArea != null) {
					zoomInArea(selectedArea);
					selectedArea = null;
					repaint();
				} else
					DialogBox.showMessageInformation(frame, GUIMessages.getString("GUI.noSelectedArea"), GUIMessages.getString("GUI.noZoomInSelectedArea")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			/* Bouton pour voir le treillis entier */
			else if (e.getSource() == noZoomBtn) {
				GraphicalConcept node = currentLattice.getTopNode();
				double radius = currentLattice.getRadius() * currentLattice.getScale();
				double centerX = currentLattice.getCenter().getX() * currentLattice.getScale();
				double centerY = currentLattice.getCenter().getY() * currentLattice.getScale();
				Point2D rootPos = currentLattice.getRootPosition();
				double rectX = rootPos.getX() + centerX - radius - 5;
				double rectY = rootPos.getY() + centerY - radius - 5;
				double rectWidth = (radius * 2) + node.getShape().getWidth() + 10;
				double rectHeight = (radius * 2) + node.getShape().getHeight() + 10;
				Rectangle2D area = new Rectangle2D.Double(rectX, rectY, rectWidth, rectHeight);

				/* Grossissement du treillis dans la zone sélectionnée */
				//currentLattice.zoomInArea(new Rectangle2D.Double(0,0,getSize().getWidth(), getSize().getHeight()), area, thisPanel);
				/*
				 * Ajustement du rectangle de la nouvelle zone pour le rendre proportionnelle à
				 * l'espace d'affichage
				 */
				double scaleX = getWidth() / area.getWidth();
				double scaleY = getHeight() / area.getHeight();

				//double finalScale;
				if (scaleX < scaleY) {
					double ajustY = (getHeight() / scaleX) - area.getHeight();
					area.setRect(area.getX(), area.getY() - (ajustY / 2), area.getWidth(), area.getHeight() + ajustY);
				} else {
					double ajustX = (getWidth() / scaleY) - area.getWidth();
					area.setRect(area.getX() - (ajustX / 2), area.getY(), area.getWidth() + ajustX, area.getHeight());
				}

				final JPanel panel = thisPanel;
				final GraphicalLattice lattice = currentLattice;

				final double stepX = (area.getX() / lattice.getScale()) / 10.0;
				final double stepY = (area.getY() / lattice.getScale()) / 10.0;
				final double stepWidth = ((getWidth() - area.getWidth()) / lattice.getScale()) / 10.0;
				final double stepHeight = ((getHeight() - area.getHeight()) / lattice.getScale()) / 10.0;
				final double initWidth = getWidth() / lattice.getScale();
				final double initHeight = getHeight() / lattice.getScale();

				ActionListener animZoom = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						double x = stepX * lattice.getScale();
						double y = stepY * lattice.getScale();
						double areaWidth = (initWidth - (animCounter + 1) * stepWidth) * lattice.getScale();
						double areaHeight = (initHeight - (animCounter + 1) * stepHeight) * lattice.getScale();
						Rectangle2D newArea = new Rectangle2D.Double(x, y, areaWidth, areaHeight);

						lattice.zoomInArea(new Rectangle2D.Double(0, 0, getSize().getWidth(), getSize().getHeight()),
								newArea);
						panel.repaint();

						animCounter++;
						if (animCounter >= 10)
							timer.stop();
					}
				};

				/* Attente de la fin d'exécution des autres animations */
				while (timer != null && timer.isRunning())
					;

				animCounter = 0;
				timer = new Timer(25, animZoom);
				timer.setInitialDelay(0);
				timer.start();
			}

			/* Changement de type de valeur d'objets dans les étiquettes */
			else if (e.getSource() == objValueTypeList) {
				String type = (String) objValueTypeList.getSelectedItem();
				if (type.equals(GUIMessages.getString("GUI.dontShowObjects"))) { //$NON-NLS-1$
					for (int i = 0; i < latticesList.size(); i++)
						(latticesList.elementAt(i)).setObjLabelType(LMOptions.NO_LABEL);
				} else if (type.equals(GUIMessages.getString("GUI.list"))) { //$NON-NLS-1$
					for (int i = 0; i < latticesList.size(); i++)
						(latticesList.elementAt(i)).setObjLabelType(LMOptions.OBJECTS_ALL);
				} else if (type.equals(GUIMessages.getString("GUI.reducedLabelling"))) { //$NON-NLS-1$
					for (int i = 0; i < latticesList.size(); i++)
						(latticesList.elementAt(i)).setObjLabelType(LMOptions.OBJECTS_REDUCED);
				} else if (type.equals(GUIMessages.getString("GUI.count"))) { //$NON-NLS-1$
					for (int i = 0; i < latticesList.size(); i++)
						(latticesList.elementAt(i)).setObjLabelType(LMOptions.OBJECTS_NUMBER);
				} else if (type.equals(GUIMessages.getString("GUI.purcentOfObjects"))) { //$NON-NLS-1$
					for (int i = 0; i < latticesList.size(); i++)
						(latticesList.elementAt(i)).setObjLabelType(LMOptions.OBJECTS_PERC_CTX);
				}
				repaint();
			}

			/* Changement de type de valeur d'attributs dans les étiquettes */
			else if (e.getSource() == attValueTypeList) {
				String type = (String) attValueTypeList.getSelectedItem();
				if (type.equals(GUIMessages.getString("GUI.dontShowAttributes"))) { //$NON-NLS-1$
					for (int i = 0; i < latticesList.size(); i++)
						(latticesList.elementAt(i)).setAttLabelType(LMOptions.NO_LABEL);
				} else if (type.equals(GUIMessages.getString("GUI.list"))) { //$NON-NLS-1$
					for (int i = 0; i < latticesList.size(); i++)
						(latticesList.elementAt(i)).setAttLabelType(LMOptions.ATTRIBUTES_ALL);
				} else if (type.equals(GUIMessages.getString("GUI.reducedLabelling"))) { //$NON-NLS-1$
					for (int i = 0; i < latticesList.size(); i++)
						(latticesList.elementAt(i)).setAttLabelType(LMOptions.ATTRIBUTES_REDUCED);
				}
				repaint();
			}

			/* Bouton de template */
			else if (e.getSource() == templateTypeList) {
				String template = (String) templateTypeList.getSelectedItem();
				if (template.equals(GUIMessages.getString("GUI.automatic"))) { //$NON-NLS-1$
					double currentScale = currentLattice.getScale();
					Point2D refPoint = new Point2D.Double(currentLattice.getTopNode().getShape().getX(),
							currentLattice.getTopNode().getShape().getY());

					/* Assignation de la structure cubique au treillis affiché */
					ConceptLattice currentConceptLattice = currentLattice.getNestedLattice().getConceptLattice();
					NestedContext newContext = new NestedContext(currentConceptLattice.getContext());
					LatticeStructure newStructure = new LatticeStructure(currentConceptLattice, newContext,
							LatticeStructure.BEST);
					Vector<LatticeStructure> newStructuresList = new Vector<LatticeStructure>();
					newStructuresList.add(newStructure);
					currentLattice.setLatticeStructures(newStructuresList);
					currentLattice.resetLatticeImage(refPoint.getX(), refPoint.getY(), currentScale);
					repaint();
				}
				if (template.equals(GUIMessages.getString("GUI.cubicShape"))) { //$NON-NLS-1$
					double currentScale = currentLattice.getScale();
					Point2D refPoint = new Point2D.Double(currentLattice.getTopNode().getShape().getX(),
							currentLattice.getTopNode().getShape().getY());

					/* Assignation de la structure cubique au treillis affiché */
					ConceptLattice currentConceptLattice = currentLattice.getNestedLattice().getConceptLattice();
					NestedContext newContext = new NestedContext(currentConceptLattice.getContext());
					LatticeStructure newStructure = new LatticeStructure(currentConceptLattice, newContext,
							LatticeStructure.CUBIC);
					Vector<LatticeStructure> newStructuresList = new Vector<LatticeStructure>();
					newStructuresList.add(newStructure);
					currentLattice.setLatticeStructures(newStructuresList);
					currentLattice.resetLatticeImage(refPoint.getX(), refPoint.getY(), currentScale);
					repaint();
				}

				if (template.equals(GUIMessages.getString("GUI.parentPosition"))) { //$NON-NLS-1$
					double currentScale = currentLattice.getScale();
					Point2D refPoint = new Point2D.Double(currentLattice.getTopNode().getShape().getX(),
							currentLattice.getTopNode().getShape().getY());

					/* Assignation de la structure cubique au treillis affiché */
					ConceptLattice currentConceptLattice = currentLattice.getNestedLattice().getConceptLattice();
					NestedContext newContext = new NestedContext(currentConceptLattice.getContext());
					LatticeStructure newStructure = new LatticeStructure(currentConceptLattice, newContext,
							LatticeStructure.PARENTS);
					Vector<LatticeStructure> newStructuresList = new Vector<LatticeStructure>();
					newStructuresList.add(newStructure);
					currentLattice.setLatticeStructures(newStructuresList);
					currentLattice.resetLatticeImage(refPoint.getX(), refPoint.getY(), currentScale);
					repaint();
				}

				if (template.equals(GUIMessages.getString("GUI.intersections"))) { //$NON-NLS-1$
					double currentScale = currentLattice.getScale();
					Point2D refPoint = new Point2D.Double(currentLattice.getTopNode().getShape().getX(),
							currentLattice.getTopNode().getShape().getY());

					/* Assignation de la structure cubique au treillis affiché */
					ConceptLattice currentConceptLattice = currentLattice.getNestedLattice().getConceptLattice();
					NestedContext newContext = new NestedContext(currentConceptLattice.getContext());
					LatticeStructure newStructure = new LatticeStructure(currentConceptLattice, newContext,
							LatticeStructure.INTERSECTIONS);
					Vector<LatticeStructure> newStructuresList = new Vector<LatticeStructure>();
					newStructuresList.add(newStructure);
					currentLattice.setLatticeStructures(newStructuresList);
					currentLattice.resetLatticeImage(refPoint.getX(), refPoint.getY(), currentScale);
					repaint();
				}

				//if(template.equals("Additive Line Diagram")){
				//  double currentScale = currentLattice.getScale();
				//  Point2D refPoint = new Point2D.Double(currentLattice.getTopNode().getShape().getX(),
				//                                        currentLattice.getTopNode().getShape().getY());

				//  /*Assignation de la structure cubique au treillis affiché*/
				//  ConceptLattice currentConceptLattice = currentLattice.getNestedLattice().getConceptLattice();
				//  NestedContext newContext = new NestedContext(currentConceptLattice.getContext());
				//  LatticeStructure newStructure = new LatticeStructure(currentConceptLattice, newContext, LatticeStructure.ADDITIVE);
				//  Vector newStructuresList = new Vector();
				//  newStructuresList.add(newStructure);
				//  currentLattice.setLatticeStructures(newStructuresList);
				//  currentLattice.resetLatticeImage(refPoint.getX(), refPoint.getY(), currentScale);
				//  repaint();
				//}

				/* Bouton de template classé par cardinalité des intentions */
				else if (template.equals(GUIMessages.getString("GUI.sortedByIntentSize"))) { //$NON-NLS-1$
					double currentScale = currentLattice.getScale();
					Point2D refPoint = new Point2D.Double(currentLattice.getTopNode().getShape().getX(),
							currentLattice.getTopNode().getShape().getY());

					/* Assignation de la structure classée par cardinalité des intentions */
					ConceptLattice currentConceptLattice = currentLattice.getNestedLattice().getConceptLattice();
					NestedContext newContext = new NestedContext(currentConceptLattice.getContext());
					LatticeStructure newStructure = new LatticeStructure(currentConceptLattice, newContext,
							LatticeStructure.INTENT);
					Vector<LatticeStructure> newStructuresList = new Vector<LatticeStructure>();
					newStructuresList.add(newStructure);
					currentLattice.setLatticeStructures(newStructuresList);
					currentLattice.resetLatticeImage(refPoint.getX(), refPoint.getY(), currentScale);
					repaint();
				}

				/* Bouton de template du plus haut niveau */
				else if (template.equals(GUIMessages.getString("GUI.highestPossibleLevel"))) { //$NON-NLS-1$
					double currentScale = currentLattice.getScale();
					Point2D refPoint = new Point2D.Double(currentLattice.getTopNode().getShape().getX(),
							currentLattice.getTopNode().getShape().getY());

					/* Assignation de la structure du plus haut niveau possible */
					ConceptLattice currentConceptLattice = currentLattice.getNestedLattice().getConceptLattice();
					NestedContext newContext = new NestedContext(currentConceptLattice.getContext());
					LatticeStructure newStructure = new LatticeStructure(currentConceptLattice, newContext,
							LatticeStructure.HIGHEST);
					Vector<LatticeStructure> newStructuresList = new Vector<LatticeStructure>();
					newStructuresList.add(newStructure);
					currentLattice.setLatticeStructures(newStructuresList);
					currentLattice.resetLatticeImage(refPoint.getX(), refPoint.getY(), currentScale);
					repaint();
				}
			}
		}
	}

}
