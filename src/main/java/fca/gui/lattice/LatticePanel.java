package fca.gui.lattice;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import fca.core.rule.Rule;
import fca.core.util.BasicSet;
import fca.exception.LMLogger;
import fca.exception.NullPointerException;
import fca.gui.lattice.element.ConceptLabel;
import fca.gui.lattice.element.Edge;
import fca.gui.lattice.element.GraphicalConcept;
import fca.gui.lattice.element.GraphicalLattice;
import fca.gui.lattice.element.GraphicalLatticeElement;
import fca.gui.lattice.element.LatticeStructure;
import fca.gui.util.ColorSet;
import fca.gui.util.DialogBox;
import fca.gui.util.constant.LMHistory;
import fca.gui.util.constant.LMOptions;
import fca.gui.util.constant.LMPreferences;
import fca.gui.util.constant.LMColors.LatticeColor;
import fca.messages.GUIMessages;

/**
 * Panneau de visualisation pour les treillis
 * @author Geneviève Roberge
 * @version 1.0
 */
public class LatticePanel extends JPanel implements MouseListener, MouseMotionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1739614498053498759L;


	/* Variables pour les déplacements */

	private Point mousePos; //Position du clic souris

	private boolean dragLattice; //Indique si le treillis est en déplacement
	/**
	 * inverse="baseViewer:fca.gui.lattice.LatticeLittleMap"
	 */
	private LatticeLittleMap mapViewer;

	private LatticeViewer frame;

	/* Variables pour l'historique */
	/**
	 * elementType="fca.gui.lattice.LatticePanel$HistoryItem"
	 */
	private Vector<HistoryItem> history; //Historique des zoom et déplacements

	private int lockHistoryCount;

	private int historyPosition;

	private HistoryItem lastHistory;

	private boolean moveHistoryStored;

	private boolean zoomInSelectionAllowed; //Indique s'il est permis de grossir le treillis du noeud sélectionné

	/* Variables pour les actions */

	private GraphicalLattice rootLattice; //Le treilli le plus externe
	//private int mouseDragAction;             //L'action associée au glissement de la souris

	private int objLabelType; //Le type d'affichage des objets dans les étiquettes

	private int attLabelType; //Le type d'affichage des attributs dans les étiquettes

	private int rulesLabelType; //Le type d'affichage des règles dans les étiquettes
	
	// TODO implementer le support des generateurs dans l'historique
	private int geneLabelType; //Le type d'affichage des generateurs dans les étiquettes

	private int singleSelType; //Le type de sélection simple

	private int multSelType; //Le type de sélection multiple

	private int selectionContrastType; //Type de contraste associé aux sélections

	private int attentionFeatureType; //Type de fonctionalité pour attirer l'attention (mouvement, clignotement)

	private boolean isBottomHidden; //Indique si les noeud vides du bas sont affichés ou non

	private boolean hideLabelForOutOfFocusConcept;

	private boolean showGlobalBottom; //Indique si le noeud du bas global (avec sa ligne) doit être affiché ou non

	private Ellipse2D globalBottomNode; //Le noeud représentant tous les noeuds du bas cachés
	private Vector<GraphicalConcept> selectedNodes; //La liste des noeuds qui sont sélectionnés

	private boolean isCTRLPressed; //Indicateur de sélection multiple

	private Rectangle2D selectedArea; //Le rectangle de sélection du panneau

	private ConceptLabel labelMoving; //Étiquette en déplacement;

	private boolean animateZoom; //Indicateur pour animer ou non les zoom

	/* Variable pour la fenêtre de navigation dans les enfants d'un noeud */

	//	private JInternalFrame iframe; //Fenêtre interne contenant les boutons de contrôle
	//	private JButton previous; //Bouton de navigation vers l'enfant précédant
	//	/**
	//	 */
	//	private JButton next; //Bouton de navigation vers l'enfant suivant
	/* Variables pour le popup menu de zoom/étiquettes */

	private JPopupMenu zoomPopupMenu; //Menu popup pour la zone sélectionnée

	private JMenuItem zoomAreaItem; //Bouton de zoom dans la zone sélectionnée

	private JMenuItem showAreaLabelsItem; //Bouton pour montrer les étiquettes dans la zone sélectionnée

	private JMenuItem hideAreaLabelsItem; //Bouton pour cacher les étiquettes dans la zone sélectionnée

	/* Variables pour les animations */
	private Animation animation; //Thread en charge des animations

	private LatticePanel panel;

	/* Variables pour le visualisateur de noeud */
	//NodeViewer nodeViewer;
	/**
	 * Contructeur
	 * @param gl Le GraphicalLattice à afficher
	 * @param v Le LatticeViewer associé au treillis affiché
	 */
	public LatticePanel(GraphicalLattice gl, LatticeViewer v) {
		setBackground(Color.WHITE);

		rootLattice = gl;
		frame = v;
		panel = this;

		objLabelType = LMOptions.OBJECTS_PERC_NODE;
		attLabelType = LMOptions.ATTRIBUTES_REDUCED;
		rulesLabelType = LMOptions.NO_LABEL;
		singleSelType = LMOptions.FILTER_IDEAL;
		multSelType = LMOptions.COMMON_FILTER_IDEAL;
		selectionContrastType = LMOptions.BLUR;
		attentionFeatureType = LMOptions.SHAKE;

		zoomInSelectionAllowed = true;
		isCTRLPressed = false;
		isBottomHidden = false;
		showGlobalBottom = false;

		selectedNodes = new Vector<GraphicalConcept>();
		globalBottomNode = new Ellipse2D.Double(0, 0, 1, 1);

		mousePos = new Point(0, 0);
		dragLattice = false;
		history = new Vector<HistoryItem>();
		lockHistoryCount = 0;
		historyPosition = -1;
		moveHistoryStored = true;

		animateZoom = true;
		animation = new Animation();
		animation.start();

		addMouseListener(this);
		addMouseMotionListener(this);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new CtrlKeyDetector());

		/* Crée la fenêtre de navigation entre les enfants directs d'un noeud */
		// Plus utilisé actuellement
		//		iframe = new JInternalFrame("Navigate through filters", false, false, false, false);
		//		JPanel ipanel = new JPanel();
		//		ipanel.setBackground(Color.BLACK);
		//		
		//		previous = new JButton("Previous");
		//		previous.setPreferredSize(new Dimension(100, 25));
		//		previous.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				if (selectedNodes.size() > 0) {
		//					(selectedNodes.elementAt(0)).showPreviousChild();
		//					repaint();
		//				}
		//			}
		//		});
		//		next = new JButton("Next");
		//		next.setPreferredSize(new Dimension(100, 25));
		//		next.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				if (selectedNodes.size() > 0) {
		//					(selectedNodes.elementAt(0)).showNextChild();
		//					repaint();
		//				}
		//			}
		//		});
		//		
		//		ipanel.add(previous);
		//		ipanel.add(next);
		//		
		//		iframe.getContentPane().add(ipanel);
		//		iframe.setVisible(true);
		//		add(iframe);
		/* Crée le popup menu pour le zoom d'un rectangle choisi */
		zoomPopupMenu = new JPopupMenu();
		zoomAreaItem = new JMenuItem(GUIMessages.getString("GUI.zoomInSelectedArea")); //$NON-NLS-1$
		zoomPopupMenu.add(zoomAreaItem);
		zoomAreaItem.addActionListener(new PopupListener());
		showAreaLabelsItem = new JMenuItem(GUIMessages.getString("GUI.showLabelsInSelectedArea")); //$NON-NLS-1$
		zoomPopupMenu.add(showAreaLabelsItem);
		showAreaLabelsItem.addActionListener(new PopupListener());
		hideAreaLabelsItem = new JMenuItem(GUIMessages.getString("GUI.hideLabelsInSelectedArea")); //$NON-NLS-1$
		zoomPopupMenu.add(hideAreaLabelsItem);
		hideAreaLabelsItem.addActionListener(new PopupListener());
		zoomPopupMenu.setLightWeightPopupEnabled(false);

		mapViewer = new LatticeLittleMap(this);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
		add(mapViewer, c);
	}

	/**
	 * Permet d'afficher un nouveau treillis dans le viewer
	 * @param gl Le GraphicalLattice qui doit maintenant être affiché
	 */
	public void changeDisplayedLattice(GraphicalLattice gl, int history) {
		if ((history != LMHistory.LATTICE_PROJECTION) && (history != LMHistory.APPROXIMATION))
			return;

		addHistoryItem(history);
		lockHistory();

		boolean changeIntensity = rootLattice.isChangeIntensity();
		int conceptSizeType;
		if (rootLattice.getConceptSize() == GraphicalConcept.LARGE_NODE_SIZE)
			conceptSizeType = LMOptions.LARGE;
		else
			conceptSizeType = LMOptions.SMALL;

		rootLattice = gl;
		selectedNodes = new Vector<GraphicalConcept>();
		isCTRLPressed = false;

		mousePos = new Point(0, 0);
		dragLattice = false;
		//		iframe.setVisible(false);

		/* L'information dans les étiquettes garde le type précédent */
		setObjLabelType(objLabelType);
		setAttLabelType(attLabelType);
		rootLattice.setConceptSizeType(conceptSizeType);
		rootLattice.setSelectionContrastType(selectionContrastType);
		rootLattice.changeIntensity(changeIntensity);

		// Changement des données dans la vue arborescente
		frame.getTreePanel().setNewLattice(rootLattice);
		frame.getTreePanel().repaint();

		// Changement des données dans la vue "Search/Approximate"
		frame.getSearchPanel().setNewLattice(rootLattice);
		frame.getSearchPanel().repaint();

		// Changement du treillis dans la carte globale
		mapViewer.setNewLattice(rootLattice);
		mapViewer.repaint();

		unlockHistory();
		repaint();
	}

	public void setLatticeStructures(Vector<LatticeStructure> newStructures) {
		addHistoryItem(LMHistory.STRUCTURE);
		lockHistory();

		rootLattice.setLatticeStructures(newStructures);

		mapViewer.setNewLattice(rootLattice);
		mapViewer.repaint();

		unlockHistory();
	}

	public void refreshMapViewer() {
		mapViewer.refreshMap();
		mapViewer.repaint();
	}

	/**
	 * Permet d'obtenir le rectangle contenant la partie visible du treillis
	 * @return Le double représentant le facteur de grossissement
	 */
	public Rectangle2D getVisibleArea() {
		double rootScale = rootLattice.getScale();
		double rootRadius = rootLattice.getRadius();
		GraphicalConcept topNode = rootLattice.getTopNode();

		double rootX = topNode.getShape().getX();
		double rootY = topNode.getShape().getY();

		double mapX = (rootRadius - rootLattice.getCenter().getX()) * rootScale;
		double mapY = (0.2 + rootRadius - rootLattice.getCenter().getY()) * rootScale;

		double decX = (mapX - rootX) / rootScale;
		double decY = (mapY - rootY) / rootScale;
		double width = panel.getWidth() / rootScale;
		double height = panel.getHeight() / rootScale;

		return new Rectangle2D.Double(decX, decY, width, height);
	}

	//	
	//	/**
	//	 * Permet d'ouvrir la fenête de contrôle pour naviguer parmi les parents des enfants
	//	 */
	//	public void openNavigationFrame() {
	//		iframe.setVisible(true);
	//	}
	//	
	//	/**
	//	 * Permet de fermer la fenête de contrôle pour naviguer parmi les parents des enfants
	//	 */
	//	public void closeNavigationFrame() {
	//		iframe.setVisible(false);
	//	}

	/**
	 * Permet d'augmenter le facteur de grossissement du treillis
	 */
	public void zoomIn() {
		addHistoryItem(LMHistory.SCALE);
		lockHistory();
		if (animateZoom)
			animation.add(LMOptions.ZOOM, 1.2);
		else {
			rootLattice.zoomIn(panel.getBounds(), 1.2);
			repaint();
		}
		unlockHistory();
	}

	/**
	 * Permet de réduire le facteur de grossissement du treillis
	 */
	public void zoomOut() {
		addHistoryItem(LMHistory.SCALE);
		lockHistory();
		if (animateZoom)
			animation.add(LMOptions.ZOOM, (1.0 / 1.2));
		else {
			rootLattice.zoomOut(panel.getBounds(), 1.2);
			repaint();
		}
		unlockHistory();
	}

	/**
	 * Permet d'ajuster le facteur de grossissement du treillis à la zone choisie
	 */
	public void zoomInArea(Rectangle2D area) {
		addHistoryItem(LMHistory.SCALE_AND_MOVE);
		lockHistory();

		area = ajustAreaForPanel(area);
		double finalRatio = panel.getWidth() / area.getWidth();
		double deplX;
		double deplY;
		/* Zoom out */
		if (finalRatio <= 1.0 / 1.07) {
			deplX = (panel.getWidth() / 2.0 - (area.getX() + area.getWidth() / 2.0)) * finalRatio;
			deplY = (panel.getHeight() / 2.0 - (area.getY() + area.getHeight() / 2.0)) * finalRatio;
		}
		/* Zoom in */
		else {
			deplX = panel.getWidth() / 2.0 - (area.getX() + (area.getWidth() / 2.0));
			deplY = panel.getHeight() / 2.0 - (area.getY() + (area.getHeight() / 2.0));
		}

		/* Zoom out et déplacement */
		if (animateZoom) {
			if (finalRatio <= 1.0 / 1.07) {
				animation.add(LMOptions.ZOOM, finalRatio);
				animation.add(LMOptions.MOVE, deplX, deplY);
			}

			/* Déplacement et zoom in */
			else {
				animation.add(LMOptions.MOVE, deplX, deplY);
				animation.add(LMOptions.ZOOM, finalRatio);
			}
		}

		else {
			if (finalRatio <= 1.0 / 1.07) {
				rootLattice.zoomIn(panel.getBounds(), finalRatio);
				rootLattice.setRootPosition(rootLattice.getRootPosition().getX() + deplX,
						rootLattice.getRootPosition().getY() + deplY);
				panel.repaint();
			} else {
				rootLattice.setRootPosition(rootLattice.getRootPosition().getX() + deplX,
						rootLattice.getRootPosition().getY() + deplY);
				rootLattice.zoomIn(panel.getBounds(), finalRatio);
				panel.repaint();
			}
		}

		unlockHistory();
	}

	/**
	 * Permet d'ajuster le facteur de grossissement du treillis à la zone du treillis parent du
	 * noeud sélectionné (sélection simple seulement)
	 */
	public void zoomInSelectionArea() {
		/* Disponible seulement pour les sélections simples */
		if (selectedNodes.size() == 1 && zoomInSelectionAllowed) {
			/* Recherche du rectangle englobant le treillis du noeud sélectionné */
			GraphicalConcept node = selectedNodes.elementAt(0);
			GraphicalLattice parentLattice = node.getParentLattice();
			animation.add(LMOptions.ZOOM, parentLattice);
		}
	}

	public Rectangle2D getBoundsForLattice(GraphicalLattice targetLattice) {
		double targetScale = targetLattice.getScale();

		Point2D targetPos = targetLattice.getRootPosition();
		double targetX = targetPos.getX() + (targetLattice.getCenter().getX() - targetLattice.getRadius())
		* targetScale - 5.0;
		double targetY = targetPos.getY() + (targetLattice.getCenter().getY() - targetLattice.getRadius())
		* targetScale - 5.0;

		double targetRadius = targetLattice.getRadius() * targetScale;
		double targetWidth = (targetRadius * 2.0) + targetLattice.getConceptSize() * targetScale + 5.0;
		double targetHeight = (targetRadius * 2.0) + targetLattice.getTopNode().getShape().getHeight() + 5.0;
		Rectangle2D targetArea = new Rectangle2D.Double(targetX, targetY, targetWidth, targetHeight);

		return targetArea;
	}

	private Rectangle2D ajustAreaForPanel(Rectangle2D area) {
		/*
		 * Ajustement du rectangle de la nouvelle zone pour le rendre proportionnelle à l'espace
		 * d'affichage
		 */
		double ratioX = panel.getWidth() / area.getWidth();
		double ratioY = panel.getHeight() / area.getHeight();

		double finalRatio;
		if (ratioX < ratioY) {
			finalRatio = ratioX;
			double ajustY = (panel.getHeight() / finalRatio) - area.getHeight();
			area.setRect(area.getX(), area.getY() - (ajustY / 2.0), area.getWidth(), area.getHeight() + ajustY);
		} else {
			finalRatio = ratioY;
			double ajustX = (panel.getWidth() / finalRatio) - area.getWidth();
			area.setRect(area.getX() - (ajustX / 2.0), area.getY(), area.getWidth() + ajustX, area.getHeight());
		}

		return area;
	}

	/**
	 * Permet d'afficher toutes les étiquettes du treillis
	 */
	public void showAllLabels() {
		rootLattice.showAllLabels();
		repaint();
	}

	/**
	 * Permet de cacher toutes les étiquettes du treillis
	 */
	public void hideAllLabels() {
		rootLattice.hideAllLabels();
		repaint();
	}

	/**
	 * Permet d'afficher toutes les étiquettes de la zone choisie
	 */
	public void showAreaLabels(Rectangle2D area) {
		rootLattice.showAreaLabels(area);
		repaint();
	}

	/**
	 * Permet de cacher toutes les étiquettes de la zone choisie
	 */
	public void hideAreaLabels(Rectangle2D area) {
		rootLattice.hideAreaLabels(area);
		repaint();
	}

	/**
	 * Permet de cacher les étiquettes des concepts flous
	 */
	public void hideLabelForOutOfFocusConcept(boolean hide) {
		hideLabelForOutOfFocusConcept = hide;
		rootLattice.hideLabelForOutOfFocusConcept(hide);
		repaint();
	}

	/**
	 * Permet d'ajuster le type de valeur d'objet affiché dans les étiquettes<br>
	 * <br>
	 * NONE - Ne pas afficher<br>
	 * NBR_OBJ - Le nombre absolu d'objects<br>
	 * PERC_CTX_OBJ - Le pourcentage des objets du contexte global<br>
	 * PERC_NODE_OBJ - Le pourcentage des objets contenus dans le noeud externe
	 * @param type Le int contenant le nouveau type de valeur
	 */
	public void setObjLabelType(int type) {
		addHistoryItem(LMHistory.OBJECT_LABELS);
		objLabelType = type;
		rootLattice.setObjLabelType(type);
		repaint();
	}

	/**
	 * Permet d'ajuster le type de valeur d'attributs affiché dans les étiquettes<br>
	 * <br>
	 * NONE - Ne pas afficher<br>
	 * ALL_ATTRIBUTES - La liste complète des attributs du noeud REDUCED_LABEL - La liste des
	 * attributs propres au noeud
	 * @param type Le int contenant le nouveau type de valeur
	 */
	public void setAttLabelType(int type) {
		addHistoryItem(LMHistory.ATTRIBUTE_LABELS);
		attLabelType = type;
		rootLattice.setAttLabelType(type);
		repaint();
	}

	/**
	 * Permet d'ajuster le type de règles affichées dans les étiquettes<br>
	 * <br>
	 * NONE - Ne pas afficher<br>
	 * SHOW - Afficher la liste des règles du noeud
	 * @param type Le int contenant le nouveau type de valeur
	 */
	public void setRulesLabelType(int type) {
		addHistoryItem(LMHistory.RULE_LABELS);
		rulesLabelType = type;
		rootLattice.setRulesLabelType(type);
		repaint();
	}

	/**
	 * Permet d'ajuter le type de sélection simple
	 * @param type Le int contenant le type de sélection
	 */
	public void setSingleSelType(int type) {
		addHistoryItem(LMHistory.SINGLE_SELECTION);
		singleSelType = type;
		doSelections();
	}

	/**
	 * Permet d'ajuster le type de sélection multiple
	 * @param type Le int contenant le type de sélection
	 */
	public void setMultSelType(int type) {
		addHistoryItem(LMHistory.MULTIPLE_SELECTION);
		multSelType = type;
		doSelections();
	}

	/**
	 * Permet d'ajuster le type de contraste associé aux sélections (flou, changement de taille)
	 * @param type Le int contenant le type de contraste
	 */
	public void setSelectionContrastType(int type) {
		if (type != LMOptions.BLUR && type != LMOptions.FISHEYE)
			return;
		addHistoryItem(LMHistory.CONTRAST);
		selectionContrastType = type;

		/* Ajuste la taille des concepts */
		rootLattice.setSelectionContrastType(type);
		rootLattice.setRootPosition(rootLattice.getRootPosition().getX(), rootLattice.getRootPosition().getY());
		if (rootLattice.isEditable()) {
			doSelections();
		}
		repaint();
	}

	/**
	 * Permet d'ajuster le type de fonctionnalité pour attirer l'attention (mouvement, clignotement)
	 * @param type Le int contenant le type de contraste
	 */
	public void setAttentionFeatureType(int type) {
		if (type != LMOptions.SHAKE && type != LMOptions.BLINK && type != LMOptions.NONE)
			return;
		addHistoryItem(LMHistory.ATTENTION);
		attentionFeatureType = type;
	}

	
	/**
	 * Permet d'ajuster l'affichage des générateurs dans les étiquettes
	 * NONE - Ne pas afficher
	 * SHOW - Afficher la liste des générateurs du noeud
	 * @param type Le int contenant le nouveau type de valeur
	 */
	public void setGeneLabelType(int type) {
		addHistoryItem(LMHistory.GENE_LABELS);
		geneLabelType = type;
		rootLattice.setGeneLabelType(type);
		repaint();
	}
	
	/**
	 * Permet de propager la modification de la taille du texte
	 * des labels au treillis
	 * @param newSize la nouvelle taille du texte
	 */
	public void setLabelSize(int newSize){
		rootLattice.fontSizeChanged(newSize);
	}
	
	
	/**
	 * Permet d'obtenir le type de sélection simple
	 * @return Le int contenant le type de sélection
	 */
	public int getSingleSelType() {
		return singleSelType;
	}

	/**
	 * Permet d'obtenir le type de sélection multiple
	 * @return Le int contenant le type de sélection
	 */
	public int getMultSelType() {
		return multSelType;
	}

	/**
	 * Permet d'obtenir le type de contraste associé aux sélections (flou, changement de taille)
	 * @return Le int contenant le type de contraste
	 */
	public int getSelectionContrastType() {
		return selectionContrastType;
	}

	/**
	 * Permet d'obtenir le type de fonctionnalité pour attirer l'attention (mouvement, clignotement)
	 * @return Le int contenant le type de fonctionnalité
	 */
	public int getAttentionFeatureType() {
		return attentionFeatureType;
	}

	/**
	 * @param animate
	 */
	public void setAnimateZoom(boolean animate) {
		addHistoryItem(LMHistory.ANIMATION);
		animateZoom = animate;
	}

	public void setChangeColorIntensity(boolean b) {
		addHistoryItem(LMHistory.COLOR);
		rootLattice.changeIntensity(b);
		repaint();
	}

	public boolean isChangeColorIntensity() {
		return rootLattice.isChangeIntensity();
	}

	public void setConceptSizeType(int type) {
		addHistoryItem(LMHistory.SIZE);
		lockHistory();
		boolean animate = animateZoom;
		animateZoom = false;
		rootLattice.setConceptSizeType(type);
		//zoomInArea(getBoundsForLattice(rootLattice));
		animateZoom = animate;
		repaint();
		unlockHistory();
	}

	public int getConceptSizeType() {
		return rootLattice.getConceptSizeType();
	}

	public void changeOptions(int type) {
		// ATTENTION : A mettre en lien avec LatticeViewer.changeMenuItem(hist,value)
		// pour selectionner les menus. Ici seules les actions sont réalisées
		addHistoryItem(LMHistory.CHANGE_OPTIONS);
		lockHistory();
		if (type == LMOptions.LIGHT) {
			//----------------------------------------------
			setSingleSelType(LMOptions.FILTER_IDEAL);
			setMultSelType(LMOptions.COMMON_FILTER_IDEAL);
			setSelectionContrastType(LMOptions.BLUR);
			//----------------------------------------------
			hideLabelForOutOfFocusConcept(true);
			setAttLabelType(LMOptions.ATTRIBUTES_REDUCED);
			setObjLabelType(LMOptions.OBJECTS_PERC_NODE);
			setRulesLabelType(LMOptions.RULES_SHOW);
			//----------------------------------------------
			setAnimateZoom(false);
			setAttentionFeatureType(LMOptions.BLINK);
			//----------------------------------------------
			setChangeColorIntensity(false);
			setConceptSizeType(LMOptions.LARGE);
			//----------------------------------------------
			showAllConcepts();
			showLatticeMap(false);
			//----------------------------------------------

		} else if (type == LMOptions.HEAVY) {
			//----------------------------------------------
			setSingleSelType(LMOptions.FILTER_IDEAL);
			setMultSelType(LMOptions.COMMON_FILTER_IDEAL);
			setSelectionContrastType(LMOptions.FISHEYE);
			//----------------------------------------------
			hideLabelForOutOfFocusConcept(false);
			setAttLabelType(LMOptions.ATTRIBUTES_ALL);
			setObjLabelType(LMOptions.OBJECTS_ALL);
			setRulesLabelType(LMOptions.RULES_SHOW);
			//----------------------------------------------
			setAnimateZoom(true);
			setAttentionFeatureType(LMOptions.SHAKE);
			//----------------------------------------------
			setChangeColorIntensity(true);
			setConceptSizeType(LMOptions.LARGE);
			//----------------------------------------------
			showAllConcepts();
			showLatticeMap(true);
			//----------------------------------------------

		} else if (type == LMOptions.USER) {

			// Recupère les préférences de Lattice Miner
			Preferences preferences = LMPreferences.getPreferences();

			//----------------------------------------------
			setSingleSelType(preferences.getInt(LMPreferences.SINGLE_SEL_TYPE, LMOptions.FILTER_IDEAL));
			setMultSelType(preferences.getInt(LMPreferences.MULT_SEL_TYPE, LMOptions.COMMON_FILTER_IDEAL));
			setSelectionContrastType(preferences.getInt(LMPreferences.SEL_CONTRAST_TYPE, LMOptions.FISHEYE));
			//----------------------------------------------
			hideLabelForOutOfFocusConcept(preferences.getBoolean(LMPreferences.HIDE_OUT_OF_FOCUS, false));
			setAttLabelType(preferences.getInt(LMPreferences.ATT_LABEL_TYPE, LMOptions.ATTRIBUTES_REDUCED)); // ATTRIBUTES_ALL));
			setObjLabelType(preferences.getInt(LMPreferences.OBJ_LABEL_TYPE, LMOptions.OBJECTS_REDUCED)); // OBJECTS_ALL));
			setRulesLabelType(preferences.getInt(LMPreferences.RULES_LABEL_TYPE, LMOptions.RULES_SHOW));
			//----------------------------------------------
			setAnimateZoom(preferences.getBoolean(LMPreferences.ANIMATE_ZOOM, true));
			setAttentionFeatureType(preferences.getInt(LMPreferences.FEATURE_TYPE, LMOptions.SHAKE));
			//----------------------------------------------
			setChangeColorIntensity(preferences.getBoolean(LMPreferences.CHANGE_COLOR_INTENSITY, true));
			setConceptSizeType(preferences.getInt(LMPreferences.CONCEPT_SIZE_TYPE, LMOptions.LARGE));
			//----------------------------------------------
			showAllConcepts(); // Infimum jamais caché par défaut
			showLatticeMap(preferences.getBoolean(LMPreferences.SHOW_LATTICE_MAP, true));
			//----------------------------------------------

		}
		unlockHistory();
	}

	/**
	 * Cache les noeuds du bas pour chaque treillis dont le noeud du bas du treillis le plus bas du
	 * même niveau a une extention vide.
	 * @return vrai si le noeud est vraiment masquéaux sinon
	 */
	public boolean hasHideEmptyBottomConcepts() {
		addHistoryItem(LMHistory.GLOBAL_BOTTOM);
		isBottomHidden = true;
		if (rootLattice.hasHideEmptyBottomConcepts())
			showGlobalBottom = true;
		else {
			DialogBox.showMessageInformation(this, GUIMessages.getString("GUI.nothingHasBeenHidden"), GUIMessages.getString("GUI.noEmptyBottomConcept")); //$NON-NLS-1$ //$NON-NLS-2$
			showGlobalBottom = false;
		}
		repaint();
		return showGlobalBottom;
	}

	/**
	 * Cache/Affiche la petite carte qui représente le treillis en miniature
	 * @param show vrai s'il faut montrer la carte, faux sinon
	 */
	public void showLatticeMap(boolean show) {
		mapViewer.setVisible(show);
		repaint();
	}

	/**
	 * Permet de savoir si la petite carte est visible ou non
	 * @return vrai si la map est visible
	 */
	public boolean isShowLatticeMap() {
		return mapViewer.isVisible();
	}

	/**
	 * Affiche tous les noeuds de chacun des treillis initialement affichés.
	 */
	public void showAllConcepts() {
		addHistoryItem(LMHistory.GLOBAL_BOTTOM);
		isBottomHidden = false;
		showGlobalBottom = false;
		rootLattice.showAllConcepts();
		repaint();
	}

	/**
	 * Permet d'animer le noeud choisi d'un léger mouvement de gauche à droite.
	 * @param node Le GraphicalConcept à sélectionner
	 */
	public void selectAndShakeNode(GraphicalConcept node) {
		/* Grossissement du treillis contenant le noeud sélectionné */
		if (node == null || !zoomInSelectionAllowed)
			return;

		if (!rootLattice.isEditable())
			return;

		addHistoryItem(LMHistory.SELECT_NODE);
		lockHistory();

		/* Ajustement des sélections */
		rootLattice.clearLattice();
		selectedNodes.removeAllElements();
		//rootLattice.setInFocus();
		doSelections();
		repaint();

		/* Recherche du rectangle englobant le treillis du noeud sélectionné */
		GraphicalLattice parentLattice = node.getParentLattice();
		Rectangle2D area = ajustAreaForPanel(getBoundsForLattice(parentLattice));

		double finalRatio = panel.getWidth() / area.getWidth();
		double deplX;
		double deplY;
		/* Zoom out */
		if (finalRatio <= 1.0 / 1.07) {
			deplX = (panel.getWidth() / 2.0 - (area.getX() + area.getWidth() / 2.0)) * finalRatio;
			deplY = (panel.getHeight() / 2.0 - (area.getY() + area.getHeight() / 2.0)) * finalRatio;
		}
		/* Zoom in */
		else {
			deplX = panel.getWidth() / 2.0 - (area.getX() + (area.getWidth() / 2.0));
			deplY = panel.getHeight() / 2.0 - (area.getY() + (area.getHeight() / 2.0));
		}

		Vector<GraphicalLattice> parentList = new Vector<GraphicalLattice>();
		parentLattice = node.getParentLattice();
		parentList.add(parentLattice);
		while (parentLattice.getExternalGraphNode() != null) {
			parentLattice = parentLattice.getExternalGraphNode().getParentLattice();
			parentList.add(parentLattice);
		}

		/*
		 * Si le déplacement à faire est suffisamment grand, on retourne au treillis global et on
		 * zoom à l'endroit voulu
		 */
		if (animateZoom && (Math.abs(deplX) > 2.5 * panel.getWidth() || Math.abs(deplY) > 2.5 * panel.getHeight())) {
			/* Recherche du rectangle englobant le treillis racine */
			Rectangle2D rootArea = getBoundsForLattice(rootLattice);
			zoomInArea(rootArea);

			/* Zoom dans les treillis externes un à un, mis à part le treillis racine */
			for (int i = parentList.size() - 2; i >= 0; i--) {
				parentLattice = parentList.elementAt(i);
				animation.add(LMOptions.ZOOM, parentLattice);
			}

			if (attentionFeatureType == LMOptions.SHAKE)
				animation.add(LMOptions.SHAKE, node, 2);
			else
				animation.add(LMOptions.BLINK, node, 4);
		}

		/* Si le déplacement à faire est petit, on ne retourne pas au treillis global */
		else {
			/* Zoom out et déplacement */
			if (finalRatio <= 1.0 / 1.07) {
				if (animateZoom) {
					animation.add(LMOptions.ZOOM, finalRatio);
					animation.add(LMOptions.MOVE, deplX, deplY);
				}

				else {
					rootLattice.zoomOut(panel.getBounds(), 1.0 / finalRatio);
					rootLattice.setRootPosition(rootLattice.getRootPosition().getX() + deplX,
							rootLattice.getRootPosition().getY() + deplY);
				}

			}
			/* Déplacement et zoom in */
			else {
				for (int i = parentList.size() - 1; i >= 0; i--) {
					parentLattice = parentList.elementAt(i);
					Rectangle2D parentArea = ajustAreaForPanel(getBoundsForLattice(parentLattice));

					if (parentArea.getWidth() > panel.getWidth())
						continue;

					if (animateZoom)
						animation.add(LMOptions.ZOOM, parentLattice);
					else
						zoomInArea(getBoundsForLattice(parentLattice));
				}
			}

			selectedNodes.add(node);
			if (attentionFeatureType != LMOptions.NONE) {
				if (attentionFeatureType == LMOptions.SHAKE)
					animation.add(LMOptions.SHAKE, node, 2);
				else
					animation.add(LMOptions.BLINK, node, 4);
			}

			else {
				doSelections();
			}

			repaint();
		}

		unlockHistory();
	}

	/**
	 * Permet de sélectionner un noeud choisi
	 * @param node Le GraphicalConcept à sélectionner
	 */
	public void selectNode(GraphicalConcept node) {
		addHistoryItem(LMHistory.SELECT_NODE);
		lockHistory();

		/* Sélection simple => désélection des autres noeuds sélectionnés */
		if (!isCTRLPressed)
			selectedNodes.removeAllElements();

		/* Grossissement du treillis contenant le noeud sélectionné */
		if (node != null && zoomInSelectionAllowed) {
			selectedNodes.add(node);
			zoomInSelectionArea();
		}

		/* Affiche effectivement les sélections */
		doSelections();
		unlockHistory();
	}

	/**
	 * Permet de savoir quels noeuds sont eélectionnes
	 * @return Le Vector contenant les GraphicalConcept selectionnes
	 */
	public Vector<GraphicalConcept> getSelectedNodes() {
		return selectedNodes;
	}

	/**
	 * Permet d'indiquer quels noeuds doivent etre sélectionnes
	 * @param selNodes Le Vector contenant les GraphicalConcept a selectionner
	 */
	public void setSelectedNodes(Vector<GraphicalConcept> selNodes) {
		addHistoryItem(LMHistory.SELECTION);
		selectedNodes = selNodes;
		doSelections();
	}

	/**
	 * Permet de mettre les noeuds choisis en évidence
	 */
	public void showOnlySelectedNodes() {
		rootLattice.setOutOfFocus(true);

		for (int i = 0; i < selectedNodes.size(); i++) {
			GraphicalConcept selectedNode = selectedNodes.elementAt(i);
			selectedNode.setOutOfFocus(false);
		}

		repaint();
	}

	public void showRule(Rule rule) throws NullPointerException {

		GraphicalConcept node1 = null; // = rootLattice.findNodeWithIntent(rule.getConsequence());
		BasicSet ruleIntent = rule.getConsequence().union(rule.getAntecedent());
		GraphicalConcept node2 = frame.getSearchPanel().searchExacteNodeWithIntent(ruleIntent);

		if (node2 != null) {
			zoomInArea(getBoundsForLattice(node2.getParentLattice()));
			rootLattice.setOutOfFocus(true);

			for (int i = 0; i < node2.getParents().size(); i++) {
				Object element = node2.getParents().elementAt(i);
				if (element instanceof GraphicalConcept) {
					GraphicalConcept parent = (GraphicalConcept) element;
					if (parent.getConcept().getIntent().containsAll(rule.getConsequence())
							|| parent.getConcept().getIntent().containsAll(rule.getAntecedent())) {
						node1 = parent;
						break;
					}
				}
			}
		}

		if (node1 == null && node2 == null)
			throw new NullPointerException(GUIMessages.getString("GUI.rulesCannotBeDisplayed")); //$NON-NLS-1$

		if (node1 != null) {
			selectedNodes.add(node1);
			node1.setOutOfFocus(false);
			node1.setHighlighted(true);
		}

		if (node2 != null) {
			selectedNodes.add(node2);
			node2.setOutOfFocus(false);
			node2.setHighlighted(true);
		}

		repaint();
	}

	/**
	 * Permet de faire les bonnes sélections selon le contexte du viewer
	 */
	public void doSelections() {
		doSelections(false, LatticeColor.DEFAULT);
	}

	/**
	 * Permet de faire les bonnes sélections selon le contexte du viewer Ne fonctionne avec true que
	 * si le treillis n'est pas imbriqué (nested)
	 * @param changeColor vrai s'il faut changer les couleurs des noeuds, faut sinon
	 */
	public void doSelections(boolean changeColor, LatticeColor colorToChangedTo) {

		boolean reallyChangeColor = changeColor;

		// On ne modifie pas les couleurs si le treillis est imbriqué
		if (rootLattice.isNested())
			reallyChangeColor = false;

		/* Aucune sélection : retour à l'état normal du treillis */
		if (selectedNodes.size() == 0) {
			rootLattice.setOutOfFocus(false);
			rootLattice.setNormalSize();
			rootLattice.refreshConceptsShape();
		}

		/* Sélection simple */
		if (selectedNodes.size() == 1) {

			// Set everything out of focus
			rootLattice.setOutOfFocus(true);
			rootLattice.setNormalSize();
			rootLattice.refreshConceptsShape();

			GraphicalConcept selectedNode = selectedNodes.elementAt(0);

			Vector<GraphicalLatticeElement> shownItems = new Vector<GraphicalLatticeElement>();
			if (singleSelType == LMOptions.FILTER) {
				shownItems = selectedNode.getFilter();
			} else if (singleSelType == LMOptions.IDEAL) {
				shownItems = selectedNode.getIdeal();
			} else if (singleSelType == LMOptions.FILTER_IDEAL) {
				shownItems = selectedNode.getFilter();
				shownItems.addAll(selectedNode.getIdeal());
			} else if (singleSelType == LMOptions.PARENTS) {
				shownItems = selectedNode.getParents();
			} else if (singleSelType == LMOptions.CHILDREN) {
				shownItems = selectedNode.getChildren();
			} else if (singleSelType == LMOptions.PARENTS_CHILDREN) {
				shownItems = selectedNode.getParents();
				shownItems.addAll(selectedNode.getChildren());
			} else if (singleSelType == LMOptions.CHILDREN_PARENTS) {
				if (selectedNode.getChildren().size() > 0) {
					shownItems.add(selectedNode.getChildren().elementAt(0));
					shownItems.addAll(((GraphicalConcept) selectedNode.getChildren().elementAt(0)).getParents());
				}
			}

			// Mets les parents directs "in focus"
			GraphicalConcept parentConcept = selectedNode.getParentLattice().getExternalGraphNode();
			while (parentConcept != null) {
				parentConcept.setOutOfFocus(false);
				if (reallyChangeColor)
					parentConcept.setColor(colorToChangedTo);
				if (selectionContrastType == LMOptions.FISHEYE)
					parentConcept.setSizeType(LMOptions.LARGE);
				parentConcept = parentConcept.getParentLattice().getExternalGraphNode();
			}

			// Remets le parent direct "out of focus"
			selectedNode.getParentLattice().setOutOfFocus(true);

			// Selectionne le noeud
			selectedNode.setSelected(true);
			selectedNode.setOutOfFocus(false);

			if (reallyChangeColor)
				selectedNode.setColor(colorToChangedTo);
			if (selectionContrastType == LMOptions.FISHEYE)
				selectedNode.setSizeType(LMOptions.LARGE);

			for (int i = 0; i < shownItems.size(); i++) {
				if (shownItems.elementAt(i) instanceof GraphicalConcept) {
					GraphicalConcept concept = (GraphicalConcept) shownItems.elementAt(i);
					concept.setOutOfFocus(false);
					if (reallyChangeColor)
						concept.setColor(colorToChangedTo);
					else
						concept.setColorDefault();
					concept.setSelectionColorDefault();
					concept.setHighlighted(true);
					if (selectionContrastType == LMOptions.FISHEYE)
						concept.setSizeType(LMOptions.LARGE);
				} else {
					Edge edge = (Edge) shownItems.elementAt(i);
					edge.setSelected(true);
					edge.setColor(colorToChangedTo);
				}
			}
		}

		/* Sélection multiple */
		else if (selectedNodes.size() > 1) {
			GraphicalLattice lattice = null;

			// Mets tout "out of focus"
			lattice = getSelectionsLattice();
			rootLattice.setOutOfFocus(true);

			Vector<GraphicalLatticeElement> shownConcepts = new Vector<GraphicalLatticeElement>();
			if (lattice != null) {
				if (multSelType == LMOptions.COMMON_FILTER) {
					shownConcepts = lattice.getCommonFilter(selectedNodes);
				} else if (multSelType == LMOptions.COMMON_IDEAL) {
					shownConcepts = lattice.getCommonIdeal(selectedNodes);
				} else if (multSelType == LMOptions.COMMON_FILTER_IDEAL) {
					shownConcepts = lattice.getCommonFilter(selectedNodes);
					shownConcepts.addAll(lattice.getCommonIdeal(selectedNodes));
				} else if (multSelType == LMOptions.SUB_LATTICE) {
					GraphicalConcept first = selectedNodes.elementAt(0);
					GraphicalConcept second = selectedNodes.elementAt(1);

					selectedNodes.removeAllElements();
					selectedNodes.add(first);
					selectedNodes.add(second);
					shownConcepts = lattice.getSubLattice(selectedNodes);
				}
			}

			for (int i = 0; i < selectedNodes.size(); i++) {
				GraphicalConcept o = selectedNodes.elementAt(i);
				o.setSelected(true);
				o.setOutOfFocus(false);
				if (reallyChangeColor)
					o.setColor(colorToChangedTo);
				if (selectionContrastType == LMOptions.FISHEYE)
					o.setSizeType(LMOptions.LARGE);
			}

			for (int i = 0; i < shownConcepts.size(); i++) {
				Object o = shownConcepts.elementAt(i);
				if (o instanceof GraphicalConcept) {
					((GraphicalConcept) o).setOutOfFocus(false);
					((GraphicalConcept) o).setHighlighted(true);
					if (reallyChangeColor)
						((GraphicalConcept) o).setColor(colorToChangedTo);
					if (selectionContrastType == LMOptions.FISHEYE)
						((GraphicalConcept) o).setSizeType(LMOptions.LARGE);
				} else {
					((Edge) o).setHighlighted(true);
					if (reallyChangeColor)
						((Edge) o).setColor(colorToChangedTo);
				}
			}
		}

		repaint();
	}

	/**
	 * Permet d'obtenir le treillis le plus externe
	 * @return Le GraphicalLattice le plus externe
	 */
	public GraphicalLattice getRootLattice() {
		return rootLattice;
	}

	/**
	 * Permet d'obtenir le treillis dans lequel les noeuds ont été sélectionnés, si ce treillis est
	 * unique.
	 * @return Le GraphicalLattice contenant les noeuds sélectionnés
	 */
	private GraphicalLattice getSelectionsLattice() {
		GraphicalLattice lattice = null;

		for (int i = 0; i < selectedNodes.size(); i++) {
			/* Le treillis à retourner est initialement celui du 1ier noeud */
			if (i == 0)
				lattice = (selectedNodes.elementAt(i)).getParentLattice();
			/* Si un seul treillis différent est trouvé, la recherche est arrêtée */
			else if (!lattice.equals((selectedNodes.elementAt(i)).getParentLattice())) {
				lattice = null;
				break;
			}
		}

		return lattice;
	}

	/**
	 * @return la zone de selection
	 */
	public Rectangle2D getSelectedArea() {
		return selectedArea;
	}

	public void zoomInSelectedArea() {
		zoomInArea(selectedArea);
		selectedArea = null;
		repaint();
	}

	public void lockHistory() {
		lockHistoryCount++;
	}

	public void unlockHistory() {
		lockHistoryCount--;
	}

	public void addHistoryItem(int type) {
		if (lockHistoryCount != 0)
			return;

		if (history.size() == 20)
			history.removeElementAt(0);

		/* Supprime l'historique venant après le nouvel élément ajouté */
		for (int i = history.size() - 1; i > historyPosition; i--)
			history.removeElementAt(i);

		history.add(new HistoryItem(type));
		historyPosition = history.size() - 1;

		frame.setBackMessage(type);
		frame.setForwardMessage(LMOptions.NONE);

	}

	public int backHistory() {
		if (historyPosition <= -1) {
			frame.setBackMessage(LMOptions.NONE);
			return historyPosition;
		}

		if (historyPosition == history.size() - 1)
			lastHistory = new HistoryItem(LMHistory.ALL);

		HistoryItem historyItem = history.elementAt(historyPosition);
		historyPosition--;

		if (historyPosition > -1) {
			HistoryItem nextItem = history.elementAt(historyPosition);
			frame.setBackMessage(nextItem.itemType);
		} else
			frame.setBackMessage(LMOptions.NONE);

		if (historyPosition + 1 >= history.size()) {
			//HistoryItem nextHistory = lastHistory;
			frame.setForwardMessage(LMOptions.NONE);
		} else {
			HistoryItem nextHistory = history.elementAt(historyPosition + 1);
			frame.setForwardMessage(nextHistory.itemType);
		}

		restoreHistoryItem(historyItem, historyItem.itemType);
		return historyPosition;
	}

	public int forwardHistory() {
		if (historyPosition >= history.size() - 1) {
			frame.setForwardMessage(LMOptions.NONE);
			return historyPosition;
		}

		historyPosition++;
		HistoryItem currentHistory = history.elementAt(historyPosition);

		HistoryItem nextHistory;
		if (historyPosition + 1 >= history.size()) {
			nextHistory = lastHistory;
			frame.setForwardMessage(LMOptions.NONE);
		} else {
			nextHistory = history.elementAt(historyPosition + 1);
			frame.setForwardMessage(nextHistory.itemType);
		}

		if (historyPosition > -1) {
			HistoryItem nextItem = history.elementAt(historyPosition);
			frame.setBackMessage(nextItem.itemType);
		} else
			frame.setBackMessage(LMOptions.NONE);

		restoreHistoryItem(nextHistory, currentHistory.itemType);
		return historyPosition;
	}

	public void restoreHistoryItem(HistoryItem item, int type) {
		lockHistory();
		switch (type) {
		case LMHistory.LATTICE_PROJECTION:
			changeDisplayedLattice(item.lattice, LMHistory.LATTICE_PROJECTION);
			break;
		case LMHistory.MOVE_LATTICE:
			rootLattice.setRootPosition(item.rootPosition.getX(), item.rootPosition.getY());
			break;
		case LMHistory.SCALE:
			rootLattice.setScale(item.scale);
			rootLattice.setRootPosition(item.rootPosition.getX(), item.rootPosition.getY());
			break;
		case LMHistory.SCALE_AND_MOVE:
			rootLattice.setScale(item.scale);
			rootLattice.setRootPosition(item.rootPosition.getX(), item.rootPosition.getY());
			break;
		case LMHistory.SELECTION:
			setSelectedNodes(item.selectedNodes);
			break;
		case LMHistory.RULES:
			rootLattice.getNestedLattice().setRules(item.rules);
			setRulesLabelType(item.rulesLabelType);
			rootLattice.setRootPosition(item.rootPosition.getX(), item.rootPosition.getY());
			break;
		case LMHistory.GENE_LABELS:
			setGeneLabelType(item.geneLabelType);
			frame.changeMenuItem(LMHistory.GENE_LABELS, item.geneLabelType);
			break;
		case LMHistory.ATTRIBUTE_LABELS:
			setAttLabelType(item.attLabelType);
			frame.changeMenuItem(LMHistory.ATTRIBUTE_LABELS, item.attLabelType);
			break;
		case LMHistory.OBJECT_LABELS:
			setObjLabelType(item.objLabelType);
			frame.changeMenuItem(LMHistory.OBJECT_LABELS, item.objLabelType);
			break;
		case LMHistory.RULE_LABELS:
			setRulesLabelType(item.rulesLabelType);
			frame.changeMenuItem(LMHistory.RULE_LABELS, item.rulesLabelType);
			break;
		case LMHistory.ATTENTION:
			setAttentionFeatureType(item.attentionFeatureType);
			frame.changeMenuItem(LMHistory.ATTENTION, item.attentionFeatureType);
			break;
		case LMHistory.CONTRAST:
			setSelectionContrastType(item.selectionContrastType);
			frame.changeMenuItem(LMHistory.CONTRAST, item.selectionContrastType);
			break;
		case LMHistory.SINGLE_SELECTION:
			setSingleSelType(item.singleSelType);
			frame.changeMenuItem(LMHistory.SINGLE_SELECTION, item.singleSelType);
			break;
		case LMHistory.MULTIPLE_SELECTION:
			setMultSelType(item.multSelType);
			frame.changeMenuItem(LMHistory.MULTIPLE_SELECTION, item.multSelType);
			break;
		case LMHistory.GLOBAL_BOTTOM:
			if (item.isBottomHidden) {
				hasHideEmptyBottomConcepts();
				frame.changeMenuItem(LMHistory.GLOBAL_BOTTOM, LMOptions.NONE);
			} else {
				showAllConcepts();
				frame.changeMenuItem(LMHistory.GLOBAL_BOTTOM, LMOptions.ANIMATION_OK);
			}
			break;
		case LMHistory.ANIMATION:
			setAnimateZoom(item.animateZoom);
			if (item.animateZoom)
				frame.changeMenuItem(LMHistory.ANIMATION, LMOptions.ANIMATION_OK);
			else
				frame.changeMenuItem(LMHistory.ANIMATION, LMOptions.NONE);
			break;
		case LMHistory.STRUCTURE:
			rootLattice.setLatticeStructures(item.latticesStructure);
			rootLattice.setRootPosition(item.rootPosition.getX(), item.rootPosition.getY());
			refreshMapViewer();
			break;
		case LMHistory.COLOR:
			setChangeColorIntensity(item.changeIntensity);
			if (item.changeIntensity)
				frame.changeMenuItem(LMHistory.COLOR, LMOptions.CHANGE);
			else
				frame.changeMenuItem(LMHistory.COLOR, LMOptions.NONE);
			break;
		case LMHistory.SIZE:
			setConceptSizeType(item.sizeType);
			frame.changeMenuItem(LMHistory.SIZE, item.sizeType);

			rootLattice.setScale(item.scale);
			rootLattice.setRootPosition(item.rootPosition.getX(), item.rootPosition.getY());
			break;
		case LMHistory.SELECT_NODE:
			zoomInSelectionAllowed = false;
			rootLattice.clearLattice();
			selectedNodes.removeAllElements();

			if (item.selectedNodes.size() == 1)
				frame.getTreePanel().selectPathNode(item.selectedNodes.elementAt(0));
			else
				frame.getTreePanel().selectPathNode(null);

			selectedNodes.addAll(item.selectedNodes);
			doSelections();
			rootLattice.setScale(item.scale);
			rootLattice.setRootPosition(item.rootPosition.getX(), item.rootPosition.getY());
			zoomInSelectionAllowed = true;
			break;
		case LMHistory.DESELECT_NODE:
			rootLattice.clearLattice();
			selectedNodes.removeAllElements();
			selectedNodes.addAll(item.selectedNodes);
			doSelections();
			rootLattice.setScale(item.scale);
			rootLattice.setRootPosition(item.rootPosition.getX(), item.rootPosition.getY());
			break;
		case LMHistory.CHANGE_OPTIONS:
			/* Taille */
			setConceptSizeType(item.sizeType);
			frame.changeMenuItem(LMHistory.SIZE, item.sizeType);

			/* Couleur */
			setChangeColorIntensity(item.changeIntensity);
			if (item.changeIntensity)
				frame.changeMenuItem(LMHistory.COLOR, LMOptions.CHANGE);
			else
				frame.changeMenuItem(LMHistory.COLOR, LMOptions.NONE);

			/* Étiquettes */
			setAttLabelType(item.attLabelType);
			setObjLabelType(item.objLabelType);
			setRulesLabelType(item.rulesLabelType);
			frame.changeMenuItem(LMHistory.ATTRIBUTE_LABELS, item.attLabelType);
			frame.changeMenuItem(LMHistory.OBJECT_LABELS, item.objLabelType);
			frame.changeMenuItem(LMHistory.RULE_LABELS, item.rulesLabelType);

			/* Attribut pour attirer l'attention */
			setAttentionFeatureType(item.attentionFeatureType);
			frame.changeMenuItem(LMHistory.ATTENTION, item.attentionFeatureType);

			/* Liens entre les concepts */
			setSelectionContrastType(item.selectionContrastType);
			frame.changeMenuItem(LMHistory.CONTRAST, item.selectionContrastType);

			/* Sélections simple et multiple */
			setSingleSelType(item.singleSelType);
			setMultSelType(item.multSelType);
			frame.changeMenuItem(LMHistory.SINGLE_SELECTION, item.singleSelType);
			frame.changeMenuItem(LMHistory.MULTIPLE_SELECTION, item.multSelType);

			/* Animations */
			setAnimateZoom(item.animateZoom);
			if (item.animateZoom)
				frame.changeMenuItem(LMHistory.ANIMATION, LMOptions.ANIMATION_OK);
			else
				frame.changeMenuItem(LMHistory.ANIMATION, LMOptions.NONE);

			break;
		case LMHistory.APPROXIMATION:
			item.lattice.setEditable(false);
			changeDisplayedLattice(item.lattice, LMHistory.APPROXIMATION);
			//			lockHistory();
			//			getFrame().getTreePanel().selectPathNode(item.lattice.getTopNode());
			//			unlockHistory();
			item.lattice.setEditable(true);
			break;
		default:
			break;
		}
		repaint();
		unlockHistory();
	}

	/* ======== MOUSELISTENER INTERFACE ======== */

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		/*
		 * Le zoom dans le treillis du noeud sélectionné est désactivé lors des sélections faites à
		 * l'aide de la souris
		 */
		zoomInSelectionAllowed = false;

		/* Traitement de la zone de sélection */
		if (selectedArea != null) {
			/* Affichage du popup menu pour la zone sélectionnée */
			if (selectedArea.contains(e.getPoint()) && e.getClickCount() == 1)
				zoomPopupMenu.show(this, e.getX(), e.getY());

			/* Annulation du rectangle de sélection */
			else {
				selectedArea = null;
				repaint();
			}
		}

		/* Recherche d'un noeud ciblé */
		else {
			GraphicalConcept clickedNode = rootLattice.mouseClicked(e.getX(), e.getY());

			boolean changeColor = false;
			LatticeColor colorToChangedTo = LatticeColor.DEFAULT;

			if (clickedNode != null && e.getButton() == MouseEvent.BUTTON2) {
				changeColor = true;
				colorToChangedTo = ColorSet.getNextColor(clickedNode.getColor());
			}

			/* Bouton de droite => Affichage des étiquettes */
			else if (clickedNode != null && e.getButton() == MouseEvent.BUTTON3)
				clickedNode.toggleLabel();

			/* Sélection simple */
			else if (!isCTRLPressed) {
				/* Double-clic => grossissement du noeud sélectionné */
				if (clickedNode != null && e.getClickCount() == 2) {
					history.removeElementAt(history.size() - 1); //Retire l'événement du clic simple sur le noeud
					rootLattice.clearLattice();
					selectedNodes.removeAllElements();
					frame.getTreePanel().selectPathNode(null);

					/*
					 * Détermine la zone d'agrandissement : si un treillis est à l'intérieur du
					 * concept, c'est le treillis interne qu'on veut voir en entier, sinon, c'est le
					 * concept lui-même
					 */
					Rectangle2D area;
					if (clickedNode.getInternalLattice() != null) {
						GraphicalLattice il = clickedNode.getInternalLattice();
						area = getBoundsForLattice(il);//new Rectangle2D.Double(posX, posY, size, size);
					} else
						area = clickedNode.getShape().getBounds2D();

					zoomInArea(area);
				}

				/*
				 * Clic simple d'un noeud final non sélectionné => sélection simple du noeud et
				 * sélection dans l'arbre
				 */
				else if (clickedNode != null && clickedNode.isSelected() == false
						&& clickedNode.getNestedConcept().isFinalConcept()) {
					addHistoryItem(LMHistory.SELECT_NODE);
					rootLattice.clearLattice();
					selectedNodes.removeAllElements();
					lockHistory();
					frame.getTreePanel().selectPathNode(clickedNode);
					unlockHistory();
					selectedNodes.add(clickedNode);

					/* GESTION DU NODE VIEWER */
					//ConceptLattice cl = clickedNode.getParentLattice().getNestedLattice().getConceptLattice();
					//FormalConcept node = clickedNode.getNestedConcept().getConcept();
					//Color nodeColor = clickedNode.getParentLattice().getLatticeColor();
					//BasicSet extIntent;
					//BasicSet extExtent;
					//if(clickedNode.getNestedConcept().getExternalConcept() != null){
					//  extIntent = clickedNode.getNestedConcept().getExternalConcept().getIntent();
					//  extExtent = clickedNode.getNestedConcept().getExternalConcept().getExtent();
					//}
					//else{
					//  extIntent = rootLattice.getNestedLattice().getTopNestedConcept().getIntent();
					//  extExtent = rootLattice.getNestedLattice().getTopNestedConcept().getExtent();
					//}
					//if(nodeViewer == null)
					//  nodeViewer = new NodeViewer(cl, node, extIntent, extExtent, nodeColor);
					//else
					//  nodeViewer.changeDisplayedNode(cl, node, extIntent, extExtent, nodeColor);
				}

				/*
				 * Clic simple d'un noeud non final => désélection des noeuds et déselection de
				 * l'arbre
				 */
				else if (clickedNode != null && clickedNode.isSelected() == false) {
					if (selectedNodes.size() > 0)
						addHistoryItem(LMHistory.DESELECT_NODE);

					rootLattice.clearLattice();
					selectedNodes.removeAllElements();
					lockHistory();
					frame.getTreePanel().selectPathNode(null);
					unlockHistory();
				}

				/*
				 * Aucun noeud ou clic simple d'un noeud final et sélectioné => désélection des
				 * noeuds et desélection dans l'arbre
				 */
				else {
					if (selectedNodes.size() > 0)
						addHistoryItem(LMHistory.DESELECT_NODE);

					rootLattice.clearLattice();
					selectedNodes.removeAllElements();
					lockHistory();
					frame.getTreePanel().selectPathNode(null);
					unlockHistory();

					/* Ajuste la taille des concepts */
					if (selectionContrastType == LMOptions.FISHEYE)
						rootLattice.setRootPosition(rootLattice.getRootPosition().getX(),
								rootLattice.getRootPosition().getY());
				}
			}

			/* Sélection multiple */
			else {
				if (clickedNode != null) {
					changeColor = true;
					/*
					 * Sélection multiple d'un noeud final non sélectionné => ajout de la sélection
					 * du noeud et sélection dans l'arbre
					 */
					if (!selectedNodes.contains(clickedNode) && clickedNode.getNestedConcept().isFinalConcept()) {
						if (!selectedNodes.isEmpty())
							colorToChangedTo = (selectedNodes.get(0)).getColor();
						addHistoryItem(LMHistory.SELECT_NODE);
						rootLattice.clearLattice();
						frame.getTreePanel().selectPathNode(clickedNode);
						selectedNodes.add(clickedNode);

						if (selectedNodes.size() == 2) {
							rootLattice.setOutOfFocus(false);
							rootLattice.refreshConceptsShape();
						}
					}

					/* Sélection multiple d'un noeud non final non sélectionné => sélection du noeud */
					else if (!selectedNodes.contains(clickedNode)) {
						if (!selectedNodes.isEmpty())
							colorToChangedTo = (selectedNodes.get(0)).getColor();
						addHistoryItem(LMHistory.SELECT_NODE);
						rootLattice.clearLattice();
						selectedNodes.add(clickedNode);
					}

					/* Sélection multiple d'un noeud sélectionné => désélection du noeud */
					else {
						colorToChangedTo = clickedNode.getColor();
						addHistoryItem(LMHistory.DESELECT_NODE);
						rootLattice.clearLattice();
						selectedNodes.removeElement(clickedNode);
					}
				}
			}

			/* Affiche effectivement les sélections */
			doSelections(changeColor, colorToChangedTo);
			repaint();
		}

		/*
		 * Suite au traitement du clic de la souris, le grossissement du treillis du noeud
		 * sélectionné est maintenant permis à nouveau
		 */
		zoomInSelectionAllowed = true;
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
		/* Traitement de la zone de sélection */
		if (selectedArea == null) {
			Object clickedObject = rootLattice.mousePressed(e.getX(), e.getY());

			/* Bouton pesé sur une étiquette à déplacer */
			if (clickedObject != null && clickedObject instanceof ConceptLabel) {
				mousePos.setLocation(e.getX(), e.getY());
				labelMoving = (ConceptLabel) clickedObject;
			}

			/* Bouton de gauche pesé, mais pas sur une étiquette => déplacement du treillis */
			else if (e.getButton() != MouseEvent.BUTTON3) {
				moveHistoryStored = false;
				setCursor(new Cursor(Cursor.MOVE_CURSOR));
				mousePos.setLocation(e.getX(), e.getY());
				dragLattice = true;
			}

			/* Bouton de droite pesé, mais pas sur une étiquette => cération de la zone de sélection */
			else {
				selectedArea = new Rectangle2D.Double(e.getX(), e.getY(), 0, 0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		/* Fin du déplacement d'une étiquette */
		if (labelMoving != null) {
			mousePos.setLocation(0, 0);
			labelMoving = null;
		}

		/* Fin du déplacement du treillis */
		else if (dragLattice) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			mousePos.setLocation(0, 0);
			dragLattice = false;
		}

		/* Fin de la création d'une zone de sélection trop petite */
		else if (selectedArea != null && selectedArea.getWidth() < 2 && selectedArea.getHeight() < 2) {
			selectedArea = null;
			repaint();
		}
	}

	/* ======== MOUSEMOTIONLISTENER INTERFACE ======== */

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		/* Déplacement d'une étiquette */
		if (labelMoving != null) {
			/* Calcul du déplacement à effectuer */
			int decX = (e.getX() - mousePos.x);
			int decY = (e.getY() - mousePos.y);
			mousePos.x = e.getX();
			mousePos.y = e.getY();

			/* Déplacement effectif de l'étiquette */
			double labelX = labelMoving.getShape().getX();
			double labelY = labelMoving.getShape().getY();
			labelMoving.setPosition(labelX + decX, labelY + decY);
			repaint();
		}

		/* Déplacement du treillis en entier */
		else if (dragLattice) {
			if (!moveHistoryStored) {
				addHistoryItem(LMOptions.MOVE);
				moveHistoryStored = true;
			}

			/* Calcul du déplacement à effectuer */
			int decX = (e.getX() - mousePos.x);
			int decY = (e.getY() - mousePos.y);
			mousePos.x = e.getX();
			mousePos.y = e.getY();

			/* Déplacement effectif du treillis */
			Point2D rootPosition = rootLattice.getRootPosition();
			rootLattice.setRootPosition(rootPosition.getX() + decX, rootPosition.getY() + decY);

			repaint();
		}

		/* Modification du format de la zone de sélection */
		else if (selectedArea != null) {
			/* Calcul des nouvelles dimension de la zone de sélection */
			double newWidth = e.getX() - selectedArea.getX();
			double newHeight = e.getY() - selectedArea.getY();
			double rectX = selectedArea.getX();
			double rectY = selectedArea.getY();

			/* Modification effective du format de la zone de sélection */
			selectedArea.setRect(rectX, rectY, newWidth, newHeight);
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
		rootLattice.paint(g2, g2.getClipBounds());
		rootLattice.paintLabels(g2, g2.getClipBounds());

		/* Affichage de la zone de sélection */
		if (selectedArea != null) {
			g2.setPaint(Color.LIGHT_GRAY);
			g2.draw(selectedArea);
		}

		/* Affichage du noeud inférieur global, avec sa ligne */
		if (showGlobalBottom) {
			/* Données sur le treillis de premier niveau */
			Point2D rootPosition = rootLattice.getRootPosition();
			Point2D rootCenter = rootLattice.getCenter();
			double rootScale = rootLattice.getScale();
			double rootRadius = rootLattice.getRadius();

			/* Calcul du format et de la position du noeud inférieur global */
			double nodeRadius = 0.1 * rootScale;
			double startX = rootPosition.getX() + (rootCenter.getX() - rootRadius) * rootScale;
			double endX = rootPosition.getX() + (rootCenter.getX() + rootRadius + 1.8) * rootScale;
			double middleX = (endX + startX) / 2;
			double bottomY = rootPosition.getY() + (rootCenter.getY() + 1.8 + rootRadius) * rootScale
			+ (nodeRadius / 2);

			/* Affichage effectif du noeud inférieur global */
			g2.setPaint(Color.BLACK);

			Line2D globalBottomLine = new Line2D.Double(startX, bottomY + nodeRadius, endX, bottomY + nodeRadius);
			g2.draw(globalBottomLine);

			globalBottomNode.setFrame(middleX - nodeRadius, bottomY, nodeRadius * 2, nodeRadius * 2);
			g2.fill(globalBottomNode);
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
					//					if (multSelType == NONE)
					//						showOnlySelectedNodes();
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
	 * Gère les événements dans le menu popup de la zone sélectionnée
	 */
	private class PopupListener implements ActionListener {
		/* ======== ACTIONLISTENER INTERFACE ======== */

		/*
		 * (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			/* Menu de grossissement de la zone sélectionnée */
			if (e.getSource() == zoomAreaItem) {
				zoomInArea(selectedArea);
				selectedArea = null;
				repaint();
			}

			/* Menu d'affichage des étiquettes dans la zone sélectionnée */
			else if (e.getSource() == showAreaLabelsItem) {
				showAreaLabels(selectedArea);
				selectedArea = null;
				repaint();
			}

			/* Menu pour cacher les étiquettes dans la zone sélectionnée */
			else if (e.getSource() == hideAreaLabelsItem) {
				hideAreaLabels(selectedArea);
				selectedArea = null;
				repaint();
			}
		}
	}

	/**
	 * @author Geneviève Roberge
	 * @version 1.0
	 */
	private class Animation extends Thread {
		/* Variables pour le zoom */
		private double finalRatio;
		private double stepRatio;
		private double lastRatio;
		private int zoomCount;

		/* Variables pour les déplacements */
		private double deplX;
		private double stepX;
		private double lastStepX;
		private double deplY;
		private double stepY;
		private double lastStepY;
		private int moveCount;

		/* Variables pour le mouvement d'un concept */
		private GraphicalConcept concept;
		private GraphicalLattice lattice;
		private int shakeCount;
		private int blinkCount;

		private Vector<AnimationItem> animationQueue;
		private int animCounter;
		private int animationType;

		public Animation() {
			animationQueue = new Vector<AnimationItem>();
		}

		@SuppressWarnings("deprecation") //$NON-NLS-1$
		public void add(int type, GraphicalLattice lattice) {
			animationQueue.add(new AnimationItem(LMOptions.ZOOM, lattice));
			try {
				resume();
			} catch (SecurityException e) {
				// If there, there is no need to interrupt the user : it's for the developper
				LMLogger.logSevere(e, false);
			}
		}

		@SuppressWarnings("deprecation") //$NON-NLS-1$
		public void add(int type, double ratio) {
			animationQueue.add(new AnimationItem(LMOptions.ZOOM, ratio));
			try {
				resume();
			} catch (SecurityException e) {
				// If there, there is no need to interrupt the user : it's for the developper
				LMLogger.logSevere(e, false);
			}
		}

		@SuppressWarnings("deprecation") //$NON-NLS-1$
		public void add(int type, double x, double y) {
			animationQueue.add(new AnimationItem(LMOptions.MOVE, x, y));
			try {
				resume();
			} catch (SecurityException e) {
				// If there, there is no need to interrupt the user : it's for the developper
				LMLogger.logSevere(e, false);
			}
		}

		@SuppressWarnings("deprecation") //$NON-NLS-1$
		public void add(int type, GraphicalConcept cpt, int count) {
			if (type == LMOptions.SHAKE)
				animationQueue.add(new AnimationItem(LMOptions.SHAKE, cpt, count));
			else if (type == LMOptions.BLINK)
				animationQueue.add(new AnimationItem(LMOptions.BLINK, cpt, count));

			try {
				resume();
			} catch (SecurityException e) {
				// If there, there is no need to interrupt the user : it's for the developper
				LMLogger.logSevere(e, false);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@SuppressWarnings("deprecation") //$NON-NLS-1$
		@Override
		public void run() {
			if (animationQueue.size() == 0)
				try {
					suspend();
				} catch (SecurityException e) {
					// If there, there is no need to interrupt the user : it's for the developper
					LMLogger.logSevere(e, false);
				}

				while (animationQueue.size() > 0) {
					AnimationItem animItem = animationQueue.elementAt(0);
					animationQueue.removeElementAt(0);
					animationType = animItem.animationType;
					animCounter = 0;

					if (animationType == LMOptions.ZOOM && animItem.lattice == null) {
						finalRatio = animItem.zoomRatio;

						if (finalRatio >= 1.07) { //Zoom in
							stepRatio = 1.07;
							zoomCount = (int) Math.floor(Math.log(finalRatio) / Math.log(stepRatio));
							lastRatio = finalRatio / Math.pow(stepRatio, zoomCount);
						} else if (finalRatio <= 1.0 / 1.07) { //Zoom out
							stepRatio = 1.07;
							zoomCount = (int) Math.floor(Math.log(finalRatio) / Math.log(1.0 / stepRatio));
							lastRatio = 1.0 / (finalRatio / Math.pow(1.0 / stepRatio, zoomCount));
						} else {
							stepRatio = 1.0;
							zoomCount = 0;
							lastRatio = finalRatio >= 1 ? finalRatio : 1.0 / finalRatio;
						}

						doZoom();
					}

					else if (animationType == LMOptions.ZOOM) {
						lattice = animItem.lattice;
						Rectangle2D zoomArea = ajustAreaForPanel(getBoundsForLattice(lattice));

						/* Gestion du zoom */
						finalRatio = panel.getWidth() / zoomArea.getWidth();
						if (finalRatio >= 1.07) { //Zoom in
							stepRatio = 1.07;
							zoomCount = (int) Math.floor(Math.log(finalRatio) / Math.log(stepRatio));
							lastRatio = finalRatio / Math.pow(stepRatio, zoomCount);
							deplX = panel.getWidth() / 2.0 - (zoomArea.getX() + (zoomArea.getWidth() / 2.0));
							deplY = panel.getHeight() / 2.0 - (zoomArea.getY() + (zoomArea.getHeight() / 2.0));
						} else if (finalRatio <= 1.0 / 1.07) { //Zoom out
							stepRatio = 1.07;
							zoomCount = (int) Math.floor(Math.log(finalRatio) / Math.log(1.0 / stepRatio));
							lastRatio = 1.0 / (finalRatio / Math.pow(1.0 / stepRatio, zoomCount));
							deplX = (panel.getWidth() / 2.0 - (zoomArea.getX() + (zoomArea.getWidth() / 2.0))) * finalRatio;
							deplY = (panel.getHeight() / 2.0 - (zoomArea.getY() + (zoomArea.getHeight() / 2.0)))
							* finalRatio;
						} else {
							stepRatio = 1.0;
							zoomCount = 0;
							lastRatio = finalRatio >= 1 ? finalRatio : 1.0 / finalRatio;
							deplX = panel.getWidth() / 2.0 - (zoomArea.getX() + (zoomArea.getWidth() / 2.0));
							deplY = panel.getHeight() / 2.0 - (zoomArea.getY() + (zoomArea.getHeight() / 2.0));
						}

						/* Gestion du déplacement */
						if (Math.abs(deplX) > Math.abs(deplY)) {
							stepX = deplX > 0.0 ? 25.0 : -25.0;
							moveCount = (int) Math.floor(deplX / stepX);
							stepY = moveCount == 0.0 ? 0.0 : deplY / moveCount;
						} else {
							stepY = deplY > 0.0 ? 25.0 : -25.0;
							moveCount = (int) Math.floor(deplY / stepY);
							stepX = moveCount == 0.0 ? 0 : deplX / moveCount;
						}

						lastStepX = deplX - (moveCount * stepX);
						lastStepY = deplY - (moveCount * stepY);

						if (finalRatio <= 1.0 / 1.07) { //Zoom out
							doZoom();
							animCounter = 0;
							doMove();
						} else { //Zoom in
							doMove();
							animCounter = 0;
							doZoom();
						}
					}

					else if (animationType == LMOptions.MOVE) {
						deplX = animItem.deplX;
						deplY = animItem.deplY;
						if (Math.abs(deplX) > Math.abs(deplY)) {
							stepX = deplX > 0.0 ? 25.0 : -25.0;
							moveCount = (int) Math.floor(deplX / stepX);
							stepY = moveCount == 0.0 ? 0.0 : deplY / moveCount;
						} else {
							stepY = deplY > 0.0 ? 25.0 : -25.0;
							moveCount = (int) Math.floor(deplY / stepY);
							stepX = moveCount == 0.0 ? 0 : deplX / moveCount;
						}

						lastStepX = deplX - (moveCount * stepX);
						lastStepY = deplY - (moveCount * stepY);

						doMove();
					}

					else if (animationType == LMOptions.SHAKE) {
						concept = animItem.concept;
						shakeCount = animItem.moveCount;

						doShake();
					}

					else if (animationType == LMOptions.BLINK) {
						concept = animItem.concept;
						blinkCount = animItem.moveCount;

						doBlink();
					}

					if (animationQueue.size() == 0)
						try {
							suspend();
						} catch (SecurityException e) {
							// If there, there is no need to interrupt the user : it's for the developper
							LMLogger.logSevere(e, false);
						}
				}

				return;
		}

		private void doZoom() {
			/* Zoom in */
			if (finalRatio >= 1) {
				while (animCounter < zoomCount) {
					rootLattice.zoomIn(panel.getBounds(), stepRatio);
					animCounter++;

					panel.repaint();
					try {
						Thread.sleep(15);
					} catch (InterruptedException e) {
						// If there, there is no need to interrupt the user : it's for the developper
						LMLogger.logSevere(e, false);
					}
				}

				rootLattice.zoomIn(panel.getBounds(), lastRatio);
				panel.repaint();

				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					// If there, there is no need to interrupt the user : it's for the developper
					LMLogger.logSevere(e, false);
				}

				return;
			}

			/* Zoom out */
			else {
				while (animCounter < zoomCount) {
					rootLattice.zoomOut(panel.getBounds(), stepRatio);
					animCounter++;

					panel.repaint();
					try {
						Thread.sleep(15);
					} catch (InterruptedException e) {
						// If there, there is no need to interrupt the user : it's for the developper
						LMLogger.logSevere(e, false);
					}
				}

				rootLattice.zoomOut(panel.getBounds(), lastRatio);
				panel.repaint();

				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					// If there, there is no need to interrupt the user : it's for the developper
					LMLogger.logSevere(e, false);
				}

				return;
			}
		}

		private void doMove() {
			while (animCounter < moveCount) {
				rootLattice.setRootPosition(rootLattice.getRootPosition().getX() + stepX,
						rootLattice.getRootPosition().getY() + stepY);
				animCounter++;

				panel.repaint();
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					// If there, there is no need to interrupt the user : it's for the developper
					LMLogger.logSevere(e, false);
				}
			}

			rootLattice.setRootPosition(rootLattice.getRootPosition().getX() + lastStepX,
					rootLattice.getRootPosition().getY() + lastStepY);
			panel.repaint();

			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				// If there, there is no need to interrupt the user : it's for the developper
				LMLogger.logSevere(e, false);
			}

			return;
		}

		private void doShake() {
			if (shakeCount == 0)
				return;

			/* Premier déplacement à gauche */
			concept.setSelected(true);
			concept.moveLeft(5);
			panel.repaint();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// If there, there is no need to interrupt the user : it's for the developper
				LMLogger.logSevere(e, false);
			}

			while (animCounter < shakeCount * 2) {
				/* Déplacement à gauche */
				if (animCounter % 2 == 0)
					concept.moveLeft(10);

				/* Déplacement à droite */
				else
					concept.moveRight(10);

				panel.repaint();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// If there, there is no need to interrupt the user : it's for the developper
					LMLogger.logSevere(e, false);
				}
				animCounter++;
			}

			/* Dernier déplacement à droite */
			concept.moveRight(5);
			concept.setSelected(false);
			doSelections();

			panel.repaint();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// If there, there is no need to interrupt the user : it's for the developper
				LMLogger.logSevere(e, false);
			}
			return;
		}

		private void doBlink() {
			if (blinkCount == 0)
				return;

			/* Premier clignotement */
			concept.setSelected(true);
			panel.repaint();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// If there, there is no need to interrupt the user : it's for the developper
				LMLogger.logSevere(e, false);
			}

			while (animCounter < blinkCount * 2) {
				/* Effacement du cercle de sélection */
				if (animCounter % 2 == 0)
					concept.setSelected(false);

				/* Apparition du cercle de sélection */
				else
					concept.setSelected(true);

				panel.repaint();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// If there, there is no need to interrupt the user : it's for the developper
					LMLogger.logSevere(e, false);
				}
				animCounter++;
			}

			/* Dernier clignotement */
			concept.setSelected(false);
			doSelections();
			//panel.repaint();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// If there, there is no need to interrupt the user : it's for the developper
				LMLogger.logSevere(e, false);
			}
			return;
		}
	}

	/**
	 * @author Geneviève Roberge
	 * @version 1.0
	 */
	public class AnimationItem {

		private int animationType;
		private int moveCount;
		private double zoomRatio;
		private double deplX;
		private double deplY;
		private GraphicalLattice lattice;
		private GraphicalConcept concept;

		public AnimationItem(int type, GraphicalLattice lat) {
			animationType = type;
			zoomRatio = 1;
			deplX = 0;
			deplY = 0;
			moveCount = 0;
			concept = null;
			lattice = lat;
		}

		public AnimationItem(int type, double ratio) {
			animationType = type;
			zoomRatio = ratio;
			deplX = 0;
			deplY = 0;
			moveCount = 0;
			concept = null;
			lattice = null;
		}

		public AnimationItem(int type, double x, double y) {
			animationType = type;
			zoomRatio = 1;
			deplX = x;
			deplY = y;
			moveCount = 0;
			concept = null;
			lattice = null;
		}

		public AnimationItem(int type, GraphicalConcept cpt, int count) {
			animationType = type;
			zoomRatio = 1;
			deplX = 0;
			deplY = 0;
			moveCount = count;
			concept = cpt;
			lattice = null;
		}
	}

	/**
	 * @author Geneviève Roberge
	 * @version 1.0
	 */
	private class HistoryItem {
		private int itemType;
		private GraphicalLattice lattice;
		private Point2D rootPosition;
		private double scale;
		private Vector<GraphicalConcept> selectedNodes;
		private Vector<Rule> rules;
		private int attLabelType;
		private int objLabelType;
		private int rulesLabelType;
		private int attentionFeatureType;
		private int selectionContrastType;
		private int singleSelType; //Le type de sélection simple
		private int multSelType; //Le type de sélection multiple
		private boolean isBottomHidden; //Indique si les noeud vides du bas sont affichés ou non
		private boolean animateZoom;
		private Vector<LatticeStructure> latticesStructure;
		private boolean changeIntensity;
		private int sizeType;
		private int geneLabelType;

		public HistoryItem(int type) {
			itemType = type;
			lattice = panel.rootLattice;
			rootPosition = new Point2D.Double(panel.rootLattice.getRootPosition().getX(),
					panel.rootLattice.getRootPosition().getY());
			scale = panel.rootLattice.getScale();
			selectedNodes = new Vector<GraphicalConcept>();
			selectedNodes.addAll(panel.getSelectedNodes());
			rules = new Vector<Rule>();
			rules.addAll(panel.rootLattice.getNestedLattice().getRules());
			attLabelType = panel.attLabelType;
			objLabelType = panel.objLabelType;
			rulesLabelType = panel.rulesLabelType;
			attentionFeatureType = panel.attentionFeatureType;
			selectionContrastType = panel.selectionContrastType;
			singleSelType = panel.singleSelType;
			multSelType = panel.multSelType;
			isBottomHidden = panel.isBottomHidden;
			animateZoom = panel.animateZoom;
			latticesStructure = new Vector<LatticeStructure>();
			latticesStructure.add(rootLattice.getLatticeStructure());
			latticesStructure.addAll(rootLattice.getInternalLatticeStructures());
			changeIntensity = panel.rootLattice.isChangeIntensity();
			sizeType = rootLattice.getTopNode().getSizeType();
			geneLabelType = panel.geneLabelType;
		}
	}

	/**
	 * @return the frame
	 */
	public LatticeViewer getFrame() {
		return frame;
	}

	/**
	 * @return the hideLabelForOutOfFocusConcept
	 */
	public boolean isHideLabelForOutOfFocusConcept() {
		return hideLabelForOutOfFocusConcept;
	}

	/**
	 * @return the objLabelType
	 */
	public int getObjLabelType() {
		return objLabelType;
	}

	/**
	 * @return the attLabelType
	 */
	public int getAttLabelType() {
		return attLabelType;
	}

	/**
	 * @return the rulesLabelType
	 */
	public int getRulesLabelType() {
		return rulesLabelType;
	}

	/**
	 * @return the animateZoom
	 */
	public boolean isAnimateZoom() {
		return animateZoom;
	}

	/**
	 * @return the isBottomHidden
	 */
	public boolean isBottomHidden() {
		return isBottomHidden;
	}
	
	/**
	 * @return Le type de generateur que l'on veut afficher
	 */
	public int getGeneLabelType(){
		return geneLabelType;
	}
}
