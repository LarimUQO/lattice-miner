package fca.gui.lattice.element;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import fca.core.lattice.ConceptLattice;
import fca.core.lattice.DataCel;
import fca.core.lattice.FormalConcept;
import fca.core.lattice.NestedConcept;
import fca.core.lattice.NestedLattice;
import fca.core.util.BasicSet;
import fca.exception.AlreadyExistsException;
import fca.exception.InvalidTypeException;
import fca.gui.util.ColorSet;
import fca.gui.util.constant.LMOptions;
import fca.gui.util.constant.LMColors.LatticeColor;
import fca.messages.GUIMessages;

/**
 * Définition d'un treillis graphique
 * @author Geneviève Roberge
 * @version 1.0
 */
public class GraphicalLattice {

	/** Taille du diamètre des concepts du treillis */
	private static double CONCEPT_SIZE;

	/** Numéro de taille des concepts du treillis */
	private static int CONCEPT_SIZE_TYPE;

	/* Variables de la classe */

	private NestedLattice nestedConceptLattice; // Le treillis conceptuel imbriqué représenté dans
	// ce graphe

	private GraphicalConcept externalGraphNode; // Le noeud graphique dans lequel se trouve ce
	// graphe

	private LatticeStructure graphStructure; // La structure visuelle associée à ce graphe
	private Vector<LatticeStructure> subStructures; // La liste des structures des treillis internes
	/**
	 * inverse="internalLattice:fca.lattice.graphical.GraphicalConcept"
	 */
	private GraphicalConcept topNode; // Le noeud graphique à la source de ce treillis
	/**
	 * inverse="parentLattice:fca.lattice.graphical.GraphicalConcept"
	 */
	private Vector<GraphicalConcept> nodesList; // La liste des noeuds graphiques appartenant à ce treillis

	private boolean isVisible; // Indique si le treillis est affiché ou non

	private double scale; // L'échelle à laquelle est affichée ce treillis

	private Point2D latticeCenter; // Le point correspondant au centre du treillis non à l'échelle
	// et par rapport au sommet

	private double latticeRadius; // Le rayon du treillis non à l'échelle

	private Color latticeColor; // La couleur associée à ce treillis

	private LatticeColor latticeColorEnum; // La couleur associée à ce treillis (en chaine de
	// caracteres)

	private boolean isBottomHidden; // Indique si le noeud du bas doit être affiché même s'il n'a
	// aucun objet
	// private Timer timer; //Timer pour les animations
	// private int animCounter;

	private boolean changeIntensity;

	/**
	 * Si le treillis est imbrique (nested) ou non
	 */
	private boolean isNested;

	private boolean isEditable;

	private int selectionContrastType;

	/**
	 * Constructeur pour treillis imbriqué
	 * @param l La NestedConceptLattice représentée par ce graphe
	 * @param n Le GraphicalConcept dans lequel se trouve ce graphe
	 * @param s La liste des structures utiles pour l'affichage
	 */
	public GraphicalLattice(NestedLattice l, GraphicalConcept n, Vector<LatticeStructure> s) {

		isEditable = true;
		isNested = !l.getInternalLattices().isEmpty();

		nestedConceptLattice = l;
		externalGraphNode = n;
		graphStructure = s.elementAt(0);
		subStructures = new Vector<LatticeStructure>(s);
		subStructures.removeElementAt(0);
		nodesList = new Vector<GraphicalConcept>();
		topNode = null;
		isVisible = false;
		isBottomHidden = false;
		changeIntensity = true;
		scale = 0;
		CONCEPT_SIZE = GraphicalConcept.LARGE_NODE_SIZE;
		selectionContrastType = LMOptions.BLUR;

		latticeColor = ColorSet.getColorAt(l.getNestedLevel());
		latticeColorEnum = ColorSet.getColorStringAt(l.getNestedLevel());
		latticeRadius = graphStructure.getRadius();
		latticeCenter = graphStructure.getCenter();

		createGraphicalConcepts();

		/* Les 2 premiers treillis sont initiallement affichés */
		if (nestedConceptLattice.getNestedLevel() < 2)
			isVisible = true;

		/* Positionnement du graphe le plus externe */
		if (externalGraphNode == null) {
			/* Valeurs trouvées à la main par essai-erreur */
			scale = 475.0 / (latticeRadius * 2.0 + CONCEPT_SIZE);
			double rootX = (latticeRadius - latticeCenter.getX()) * scale + 5.0;
			double rootY = (latticeRadius - latticeCenter.getY()) * scale + 50.0;
			setRootPosition(rootX, rootY);
		}
	}

	/**
	 * Constructeur pour un treillis plat
	 * @param l La ConceptLattice représentée par ce graphe
	 * @param s La LatticeStructure utile pour l'affichage
	 */
	public GraphicalLattice(ConceptLattice l, LatticeStructure s) throws AlreadyExistsException, InvalidTypeException {
		isEditable = true;
		isNested = false;

		Vector<ConceptLattice> il = new Vector<ConceptLattice>();
		il.add(l);
		nestedConceptLattice = new NestedLattice(null, il, null, l.getName());

		externalGraphNode = null;
		graphStructure = s;
		subStructures = new Vector<LatticeStructure>();
		nodesList = new Vector<GraphicalConcept>();
		topNode = null;
		isVisible = false;
		isBottomHidden = false;
		changeIntensity = true;
		scale = 0;
		CONCEPT_SIZE = GraphicalConcept.LARGE_NODE_SIZE;
		selectionContrastType = LMOptions.BLUR;

		latticeColor = ColorSet.getColorAt(nestedConceptLattice.getNestedLevel());
		latticeColorEnum = ColorSet.getColorStringAt(nestedConceptLattice.getNestedLevel());
		latticeRadius = graphStructure.getRadius();
		latticeCenter = graphStructure.getCenter();

		createGraphicalConcepts();

		isVisible = true;

		/* Valeurs trouvées à la main par essai-erreur */
		scale = 475.0 / (latticeRadius * 2.0 + CONCEPT_SIZE);
		double rootX = (latticeRadius - latticeCenter.getX()) * scale + 5.0;
		double rootY = (latticeRadius - latticeCenter.getY()) * scale + 50.0;
		setRootPosition(rootX, rootY);
	}

	/**
	 * Permet d'obtenir le nom du treillis
	 * @return La String contenant le nom du treillis
	 */
	public String getName() {
		return nestedConceptLattice.getName();
	}

	/**
	 * Permet d'obtenir le noeud au sommet du treillis
	 * @return Le GraphicalConcept au sommet du treillis
	 */
	public GraphicalConcept getTopNode() {
		return topNode;
	}

	/**
	 * Permet d'obtenir la liste des noeuds du treillis
	 * @return Le Vector contenant les noeuds du treillis
	 */
	public Vector<GraphicalConcept> getNodesList() {
		return nodesList;
	}

	/**
	 * Permet d'obtenir la couleur associée à ce treillis graphique
	 * @return La Color associée au treillis
	 */
	public Color getColor() {
		return latticeColor;
	}

	/**
	 * @return the latticeColorEnum
	 */
	public LatticeColor getLatticeColor() {
		return latticeColorEnum;
	}

	/**
	 * Permet d'ajuster la couleur associée à ce treillis graphique
	 * @param c la {@link Color} associée au treillis
	 * @param ce la {@link LatticeColor} associée au treillis
	 */
	public void setLatticeColor(Color c, LatticeColor ce) {
		latticeColor = c;
		latticeColorEnum = ce;
	}

	/**
	 * Permet de savoir si le graphe est affiché ou non
	 * @return Le boolean indiquant si le graphe est affiché
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Permet d'indiquer à un graphe s'il doit s'afficher
	 * @param s Le boolean indiquant si le graphe doit s'afficher
	 */
	public void setVisible(boolean s) {
		isVisible = s;
	}

	/**
	 * @return the isNested
	 */
	public boolean isNested() {
		return isNested;
	}

	/**
	 * @return the noUnselected
	 */
	public boolean isEditable() {
		return isEditable;
	}

	/**
	 * @param editable the editable to set
	 */
	public void setEditable(boolean editable) {
		this.isEditable = editable;
	}

	/**
	 * @param selectionContrastType the selectionContrastType to set
	 */
	public void setSelectionContrastType(int selectionContrastType) {
		this.selectionContrastType = selectionContrastType;
	}

	/**
	 * @return the selectionContrastType
	 */
	public int getSelectionContrastType() {
		return selectionContrastType;
	}

	/**
	 * Permet d'indiquer à un graphe s'il doit s'afficher
	 * @param outOfFocus Le boolean indiquant si le graphe doit s'afficher
	 */
	public void setOutOfFocus(boolean outOfFocus) {
		// Mets hors focus (deselectionne, couleurs défauts,...)
		if (outOfFocus) {
			for (GraphicalConcept node : nodesList) {
				node.deselectEdges();
				node.setOutOfFocus(true);
				node.setSelected(false);
				node.setHighlighted(false);
				node.setColorDefault();
				node.setSelectionColorDefault();
				if (selectionContrastType == LMOptions.FISHEYE)
					node.setSizeType(LMOptions.SMALL);
			}
		}
		// Mets en focus
		else {
			for (GraphicalConcept node : nodesList)
				node.setOutOfFocus(false);
		}
	}

	/**
	 * Permet de remettre correct un graphe, sans le passer "out of focus"
	 */
	public void clearLattice() {
		for (GraphicalConcept node : nodesList) {
			node.deselectEdges();
			node.setSelected(false);
			node.setHighlighted(false);
			node.setColorDefault();
			node.setSelectionColorDefault();
		}
	}

	/**
	 * Permet d'obtenir le point central du treillis, non à l'échelle et par rapport au noeud racine
	 * @return Le Point2D représentant le centre du treillis
	 */
	public Point2D getCenter() {
		return latticeCenter;
	}

	/**
	 * Permet d'obtenir le rayon du cercle englobant le treillis non à l'échelle
	 * @return Le double représentant le rayon du cercle englobant le treillis
	 */
	public double getRadius() {
		return latticeRadius;
	}

	/**
	 * Permet d'obtenir le facteur de grossissement associé au graphe
	 * @return Le double représentant le facteur de grossissement
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Permet d'ajuster le facteur de grossissement associé au graphe Le graphe n'est pas rafraichi
	 * suite à cette opération.
	 * @param s Le double représentant le facteur de grossissement
	 */
	public void setScale(double s) {
		scale = s;
	}

	/**
	 * Permet d'obtenir le format des concepts
	 * @return Le double représentant le facteur de grossissement
	 */
	public double getConceptSize() {
		return CONCEPT_SIZE;
	}

	/**
	 * Permet d'obtenir le format des concepts
	 * @return Le double représentant le facteur de grossissement
	 */
	public int getConceptSizeType() {
		return CONCEPT_SIZE_TYPE;
	}

	/**
	 * Permet d'ajuster le format des concepts
	 * @param type L'entier représentant le facteur de grossissement
	 */
	public void setConceptSizeType(int type) {
		if (type != LMOptions.SMALL && type != LMOptions.LARGE && type != LMOptions.VARY)
			return;

		if (type == LMOptions.SMALL) {
			CONCEPT_SIZE = GraphicalConcept.SMALL_NODE_SIZE;
			setSmallSize();
		} else if (type == LMOptions.LARGE) {
			CONCEPT_SIZE = GraphicalConcept.LARGE_NODE_SIZE;
			setLargeSize();
		} else if (type == LMOptions.VARY) {
			CONCEPT_SIZE = GraphicalConcept.LARGE_NODE_SIZE;
			setVarySize();
		}

		CONCEPT_SIZE_TYPE = type;

		refreshConceptsShape();
	}

	/**
	 * Permet d'obtenir le nombre d'objets contenus dans le contexte global
	 * @return Le int contenant le nombre d'objets
	 */
	public int getContextObjectCount() {
		return nestedConceptLattice.getGlobalContext().getObjectCount();
	}

	/**
	 * Permet d'obtenir le nombre d'objets contenus dans le noeud externe
	 * @return Le int contenant le nombre d'objets
	 */
	public int getExtNodeObjectCount() {
		if (nestedConceptLattice.getExternalNestedConcept() != null)
			return nestedConceptLattice.getExternalNestedConcept().getExtent().size();
		/*
		 * S'il n'y a pas de noeud externe, les objets externes sont tous les objets du contexte du
		 * treillis
		 */
		else
			return nestedConceptLattice.getGlobalContext().getObjectCount();
	}

	/**
	 * Permet d'obtenir le noeud graphique externe de ce treillis graphique
	 * @return Le GraphicalConcept externe de ce treillis
	 */
	public GraphicalConcept getExternalGraphNode() {
		return externalGraphNode;
	}

	/**
	 * Permet d'obtenir le treillis imbriqué conceptuel représenté dans ce treillis graphique
	 * @return Le NestedLattice propre à ce treillis
	 */
	public NestedLattice getNestedLattice() {
		return nestedConceptLattice;
	}

	/**
	 * Permet d'obtenir la structure graphique de ce treillis
	 * @return La GraphStructure propre à ce treillis
	 */
	public LatticeStructure getLatticeStructure() {
		return graphStructure;
	}

	/**
	 * Permet d'obtenir la liste des structures graphiques des treillis imbriqués dans celui-ci
	 * @return Le Vector contenant la liste des GraphStructure des treillis internes
	 */
	public Vector<LatticeStructure> getInternalLatticeStructures() {
		return subStructures;
	}

	/**
	 * Permet de modifier la structure de chacun des treillis
	 * @param structures Le Vector contenant toutes les structures de ce treillis et ses treillis
	 *        internes
	 */
	public void setLatticeStructures(Vector<LatticeStructure> structures) {
		graphStructure = structures.elementAt(0);
		subStructures = new Vector<LatticeStructure>(structures);
		subStructures.removeElementAt(0);

		/* Ajustement du centre et du rayon */
		latticeRadius = graphStructure.getRadius();
		latticeCenter = graphStructure.getCenter();

		/*
		 * Assigne la position à chacun des noeuds selon la position déterminée dans le
		 * GraphStructure
		 */
		Vector<ConceptPosition> positions = graphStructure.getConceptPositions();
		for (int i = 0; i < positions.size(); i++) {
			ConceptPosition pos = positions.elementAt(i);
			GraphicalConcept node = getNestedNodeByIntent(pos.getIntent());
			node.setRelPosition(pos.getRelX(), pos.getRelY());

			/* Propagation dans les treillis internes */
			if (node.getInternalLattice() != null)
				node.getInternalLattice().setLatticeStructures(subStructures);
		}
		/* Réajustement de tous les noeud du treillis */
		refreshConceptsShape();
	}

	/**
	 * Permet d'obtenir la position réelle du noeud racine du treillis
	 * @return Le Point2D représentant la position du noeud racine
	 */
	public Point2D getRootPosition() {
		/* La position est copiée pour empêcher qu'elle soit modifiée */
		double x = topNode.getShape().getX();
		double y = topNode.getShape().getY();
		return new Point2D.Double(x, y);
	}

	/**
	 * Permet de positionner le noeud racine
	 * @param x Le double contenant la nouvelle position en X de la racine
	 * @param y Le double contenant la nouvelle position en Y de la racine
	 */
	public void setRootPosition(double x, double y) {
		topNode.setPosition(x, y);
		/* Les autres noeuds sont repositionnés pour suivre le déplacement de la racine */
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			node.refreshShape();
		}
	}

	/**
	 * Permet de modifier le type d'affichage des étiquettes pour les objets
	 * @param type Le int contenant le type de valeur à afficher
	 */
	public void setObjLabelType(int type) {
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			node.getLabel().setObjLabelType(type);

			/* Propagation du changement dans les treillis internes */
			if (node.getInternalLattice() != null)
				node.getInternalLattice().setObjLabelType(type);
		}
	}

	/**
	 * Permet de modifier le type d'affichage des étiquettes pour les attributs
	 * @param type Le int contenant le type de valeur à afficher
	 */
	public void setAttLabelType(int type) {
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			node.getLabel().setAttLabelType(type);

			/* Propagation du changement dans les treillis internes */
			if (node.getInternalLattice() != null)
				node.getInternalLattice().setAttLabelType(type);
		}
	}

	/**
	 * Permet de modifier le type d'affichage des ?iquettes pour les r?les
	 * @param type Le int contenant le type de valeur ?afficher
	 */
	public void setRulesLabelType(int type) {
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			node.getLabel().setRulesLabelType(type);

			/* Propagation du changement dans les treillis internes */
			if (node.getInternalLattice() != null)
				node.getInternalLattice().setRulesLabelType(type);
		}
	}

	public void changeIntensity(boolean change) {
		if (change != changeIntensity) {
			changeIntensity = change;

			for (GraphicalConcept node : nodesList) {
				if (node.getInternalLattice() != null)
					node.getInternalLattice().changeIntensity(change);
			}
		}
	}

	/**
	 * @return vrai si l'intensité doit varier pour les concepts, faux sinon
	 */
	public boolean isChangeIntensity() {
		return changeIntensity;
	}

	/**
	 * Permet d'obtenir le noeud graphique qui représente le concept possédant une intention spécifiée
	 * @param intent l'intention recherchée
	 * @return le NestedConcept qui contient le Concept ayant l'identifiant donné
	 */
	public GraphicalConcept getNestedNodeByIntent(BasicSet intent) {
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			if (node.getNestedConcept().getConcept().getIntent().equals(intent))
				return node;
		}
		return null;
	}

	/**
	 * Permet d'obtenir le noeud graphique qui représente le noeud formel
	 * @param concept Le {@link NestedConcept} dont on veut le noeud graphique
	 * @return Le {@link GraphicalConcept} qui represente le {@link NestedConcept} associé
	 */
	public GraphicalConcept getGraphicalConcept(NestedConcept concept) {
		if (concept == null)
			return null;

		for (GraphicalConcept node : nodesList) {
			if (node.getNestedConcept() == concept)
				return node;
		}
		return null;
	}

	/**
	 * Permet d'obtenir le noeud graphique qui représente le noeud formel
	 * @param concept Le {@link FormalConcept} dont on veut le noeud graphique
	 * @return Le {@link GraphicalConcept} qui represente le {@link FormalConcept} associé
	 */
	public GraphicalConcept getGraphicalConcept(FormalConcept concept) {
		if (concept == null)
			return null;

		for (GraphicalConcept node : nodesList) {
			if (node.getNestedConcept().getConcept() == concept)
				return node;
		}
		return null;
	}

	/**
	 * Permet d'obtenir le noeud graphique qui représente le concept possédant une extension spécifiée
	 * @param extent l'extension recherchée
	 * @return Le NestedConcept qui contient le Concept ayant l'identifiant donné
	 */
	public GraphicalConcept getNestedNodeByExtent(BasicSet extent) {
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			if (node.getNestedConcept().getConcept().getExtent().equals(extent))
				return node;
		}
		return null;
	}

	/**
	 * Permet de créer les noeuds graphiques de ce treillis
	 */
	private void createGraphicalConcepts() {
		BasicSet topIntent = nestedConceptLattice.getTopNestedConcept().getConcept().getIntent();

		/*
		 * Assigne la position à chacun des noeuds selon la position déterminée dans le
		 * GraphStructure
		 */
		Vector<ConceptPosition> positions = graphStructure.getConceptPositions();
		for (int i = 0; i < positions.size(); i++) {
			ConceptPosition pos = positions.elementAt(i);
			NestedConcept nc = nestedConceptLattice.getNestedConceptWithIntent(pos.getIntent());
			GraphicalConcept node = new GraphicalConcept(nc, this, subStructures);
			node.setRelPosition(pos.getRelX(), pos.getRelY());
			nodesList.add(node);

			/* Trouve le noeud sommet */
			if (pos.getIntent().equals(topIntent))
				topNode = node;
		}

		/* Construit les arêtes entre les noeuds */
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			NestedConcept nc = node.getNestedConcept();
			Vector<NestedConcept> children = nc.getChildren();

			if (children != null)
				for (NestedConcept child : children) {
					GraphicalConcept dest = getGraphicalConcept(child);
					Edge edge = new Edge(node, dest);
					node.addChildEdge(edge);
					dest.addParentEdge(edge);
				}
		}
	}

	/**
	 * Permet d'augmenter le facteur de grossissement associé au graphe
	 * @param area Le Rectangle2D délimitant l'espace dans lequel est affiché le treillis
	 */
	public void zoomIn(Rectangle2D area, double ratio) {
		/* Centre de la zone d'affichage */
		double centerX = area.getWidth() / 2.0;
		double centerY = area.getHeight() / 2.0;

		double deltaX = centerX * ratio - centerX;
		double deltaY = centerY * ratio - centerY;

		/* Ajustement de l'échelle */
		scale *= ratio;

		/* Récupération de la position actuelle de la racine du treillis */
		double currentRootX = getRootPosition().getX();
		double currentRootY = getRootPosition().getY();

		/* Nouvelle position du treillis */
		double rootX = (currentRootX * ratio) - deltaX;
		double rootY = (currentRootY * ratio) - deltaY;

		setRootPosition(rootX, rootY);
	}

	/**
	 * Permet de réduire le facteur de grossissement associé au graphe
	 * @param area Le Rectangle2D délimitant l'espace dans lequel est affiché le treillis
	 */
	public void zoomOut(Rectangle2D area, double ratio) {
		/* Centre de la zone d'affichage */
		double centerX = area.getWidth() / 2.0;
		double centerY = area.getHeight() / 2.0;

		/* Déplacement du centre d'affichage actuel au centre du nouvel affichage */
		double deltaX = centerX - centerX / ratio;
		double deltaY = centerY - centerY / ratio;

		/* Ajustement de l'échelle */
		scale /= ratio;

		/* Récupération de la position actuelle de la racine du treillis */
		double currentRootX = getRootPosition().getX();
		double currentRootY = getRootPosition().getY();

		/* Nouvelle position du treillis */
		double rootX = (currentRootX / ratio) + deltaX;
		double rootY = (currentRootY / ratio) + deltaY;

		setRootPosition(rootX, rootY);
	}

	/**
	 * Permet d'ajuster le facteur de grossissement du treillis à la zone choisie
	 * @param oldArea Le Rectangle2D délimitant l'espace dans lequel est affiché le treillis
	 * @param newArea Le Rectangle2D délimitant la partie du treillis qui doit être vue
	 */
	public void zoomInArea(Rectangle2D oldArea, Rectangle2D newArea) {
		/* Position actuelle de la racine du treillis */
		double currentRootX = getRootPosition().getX();
		double currentRootY = getRootPosition().getY();

		/*
		 * Recherche du facteur de grossissement et ajustement du rectangle de la nouvelle zone pour
		 * le rendre prop. à l'espace d'affichage
		 */
		double scaleX = oldArea.getWidth() / newArea.getWidth();
		double scaleY = oldArea.getHeight() / newArea.getHeight();

		double finalScale;
		if (scaleX < scaleY) {
			finalScale = scaleX;
			double ajustY = (oldArea.getHeight() / scaleX) - newArea.getHeight();
			newArea.setRect(newArea.getX(), newArea.getY() - (ajustY / 2), newArea.getWidth(), newArea.getHeight()
					+ ajustY);
		} else {
			finalScale = scaleY;
			double ajustX = (oldArea.getWidth() / scaleY) - newArea.getWidth();
			newArea.setRect(newArea.getX() - (ajustX / 2), newArea.getY(), newArea.getWidth() + ajustX,
					newArea.getHeight());
		}

		/* Déplacement du treillis pour placer la nouvelles zone d'affichage en (0,0) */
		double newRootX = currentRootX - newArea.getX();
		double newRootY = currentRootY - newArea.getY();

		/* Ajustement de l'échelle */
		scale *= finalScale;

		/* Nouvelle position du treillis */
		double rootX = (newRootX * finalScale);
		double rootY = (newRootY * finalScale);

		setRootPosition(rootX, rootY);
	}

	/**
	 * Permet d'afficher le graphe imbriqué au niveau donné
	 * @param level Le int contenant le niveau du treillis à afficher
	 * @return Le boolean indiquant si un nouveau treillis à été affiché
	 */
	public boolean hasShowLevel(int level) {
		/* Si ce treillis est au niveau recherché, il est rendu visible */
		if (nestedConceptLattice.getNestedLevel() == level) {
			isVisible = true;
			return true;
		}

		/*
		 * Si ce treillis n'est pas au niveau recherché, on cherche le niveau dans les treillis
		 * imbriqués de chacun de ses enfants
		 */
		else {
			boolean changed = false;
			for (int i = 0; i < nodesList.size(); i++) {
				GraphicalConcept node = nodesList.elementAt(i);
				if (node.getInternalLattice() != null && node.getInternalLattice().hasShowLevel(level))
					changed = true;
			}
			return changed;
		}
	}

	/**
	 * Permet de cacher le treillis au niveau donné
	 * @param level Le int contenant le niveau du treillis à cacher
	 * @return Le boolean indiquant si un treillis à été caché
	 */
	public boolean hasHideLevel(int level) {
		/* Si ce treillis est au niveau recherché, il est rendu non-visible */
		if (nestedConceptLattice.getNestedLevel() == level) {
			isVisible = false;
			return true;
		}

		/*
		 * Si ce treillis n'est pas au niveau recherché, on cherche le niveau dans les treillis
		 * imbriqués de chacun de ses enfants
		 */
		else {
			boolean changed = false;
			for (int i = 0; i < nodesList.size(); i++) {
				GraphicalConcept node = nodesList.elementAt(i);
				if (node.getInternalLattice() != null && node.getInternalLattice().hasHideLevel(level))
					changed = true;
			}
			return changed;
		}
	}

	/**
	 * Permet d'afficher toutes les étiquettes du treillis
	 */
	public void showAllLabels() {
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			if (node.getNestedConcept().isFinalConcept()) {
				node.getLabel().setVisible(true);

				/* Propagation du changement dans les treillis internes */
				if (node.getInternalLattice() != null)
					node.getInternalLattice().showAllLabels();
			} else
				node.getLabel().setVisible(false);
		}
	}

	/**
	 * Permet de cacher toutes les étiquettes du treillis
	 */
	public void hideAllLabels() {
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			node.getLabel().setVisible(false);

			/* Propagation du changement dans les treillis internes */
			if (node.getInternalLattice() != null)
				node.getInternalLattice().hideAllLabels();
		}
	}

	/**
	 * Permet d'afficher toutes les étiquettes de la zone choisie
	 * @param area Le Rectangle2D délimitant la zone dans laquelle les étiquettes doivent être
	 *        affichées
	 */
	public void showAreaLabels(Rectangle2D area) {
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			if (node.getShape().intersects(area.getX(), area.getY(), area.getWidth(), area.getHeight())) {
				if (node.getNestedConcept().isFinalConcept()) {
					node.getLabel().setVisible(true);

					/* Propagation du changement dans les treillis internes */
					if (node.getInternalLattice() != null)
						node.getInternalLattice().showAreaLabels(area);
				} else
					node.getLabel().setVisible(false);
			}
		}
	}

	/**
	 * Permet de cacher toutes les étiquettes de la zone choisie
	 * @param area Le Rectangle2D délimitant la zone dans laquelle les étiquettes doivent être
	 *        affichées
	 */
	public void hideAreaLabels(Rectangle2D area) {
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			if (node.getNestedConcept().isFinalConcept()
					&& node.getShape().intersects(area.getX(), area.getY(), area.getWidth(), area.getHeight())) {
				node.getLabel().setVisible(false);

				/* Propagation du changement dans les treillis internes */
				if (node.getInternalLattice() != null)
					node.getInternalLattice().hideAreaLabels(area);
			}
		}
	}

	/**
	 * Permet de cacher les étiquettes des concepts flous
	 * @param hide Le boolean indiquant si les étiquettes doivent être cachées
	 */
	public void hideLabelForOutOfFocusConcept(boolean hide) {
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			node.getLabel().hideLabelWhenConceptIsOutOfFocus(hide);
		}
	}

	/**
	 * Permet de mettre à jour la forme des noeuds du treillis
	 */
	public void refreshConceptsShape() {
		double rootX;
		double rootY;

		/* Si ce treillis est le premier, son grossissement est ajusté de manière arbitraire */
		if (externalGraphNode == null) {
			// scale = 285.0 / (latticeRadius+1.0);
			// if(scale > 10)
			// isVisible = true;
			// else
			// isVisible = false;

			// rootX = getRootPosition().getX() * scale;
			// rootY = getRootPosition().getY() * scale;;
		}

		/*
		 * Si ce treillis n'est pas le premier, son grossissement est ajusté pour qu'il soit contenu
		 * entièrement à l'intérieur de son noeud externe
		 */
		else {
			Ellipse2D node = externalGraphNode.getShape();
			scale = (node.getWidth() / 2.0) / (latticeRadius + 1.5) * 0.8;
			if (scale > 10)
				isVisible = true;
			else
				isVisible = false;

			rootX = node.getX() + (node.getWidth() / 2) - (latticeCenter.getX() * scale) - (CONCEPT_SIZE / 2 * scale)
					+ 1.0;
			rootY = node.getY() + (node.getHeight() / 2) - (latticeCenter.getY() * scale) - (CONCEPT_SIZE / 2 * scale)
					+ 1.0 - (1 * scale);
			setRootPosition(rootX, rootY);
		}

		// setRootPosition(rootX, rootY);
	}

	/**
	 * Permet de retourner l'idéal commun d'une liste de noeuds
	 * @param nodes Le Vector contenant la liste des noeuds pour lesquels le filtre commun est
	 *        cherché
	 * @return Le Vector contenant l'idéal
	 */
	public Vector<GraphicalLatticeElement> getCommonFilter(Vector<GraphicalConcept> nodes) {
		Vector<GraphicalLatticeElement> filter = new Vector<GraphicalLatticeElement>();

		for (int i = 0; i < nodes.size(); i++) {
			GraphicalConcept currNode = nodes.elementAt(i);

			/* Initialisation du vecteur avec les arêtes du 1ier noeud */
			if (filter.size() == 0) {
				filter.addAll(currNode.getFilter());
			}
			/* Interesection avec les arêtes des autres noeuds */
			else {
				filter.retainAll(currNode.getFilter());
			}
		}

		return filter;
	}

	/**
	 * Permet de retourner l'idéal commun d'une liste de noeuds
	 * @param nodes Le Vector contenant la liste des noeuds pour lesquels l'idéal commun est cherché
	 * @return Le Vector contenant l'idéal
	 */
	public Vector<GraphicalLatticeElement> getCommonIdeal(Vector<GraphicalConcept> nodes) {
		Vector<GraphicalLatticeElement> ideal = new Vector<GraphicalLatticeElement>();

		for (int i = 0; i < nodes.size(); i++) {
			GraphicalConcept currNode = nodes.elementAt(i);

			/* Initialisation du vecteur avec les arêtes du 1ier noeud */
			if (ideal.size() == 0) {
				ideal.addAll(currNode.getIdeal());
			}
			/* Intersection avec les arêtes des autres noeuds */
			else {
				ideal.retainAll(currNode.getIdeal());
			}
		}

		return ideal;
	}

	/**
	 * Permet de retourner le treillis dont la source et le sink on été sélectionnés
	 * @param nodes Le Vector contenant le noeud source (position 0) et le noeud sink (position 1)
	 * @return Le Vector contenant les noeuds du sous-treillis
	 */
	public Vector<GraphicalLatticeElement> getSubLattice(Vector<GraphicalConcept> nodes) {
		Vector<GraphicalLatticeElement> subLattice = new Vector<GraphicalLatticeElement>();

		/* Il doit y avoir un minimum de 2 noeuds sélectionnés pour avoir une source et un sink */
		if (nodes.size() >= 2) {
			/* Seulement les 2 premiers noeuds sélectionnés comptent */
			GraphicalConcept source = nodes.elementAt(0);
			GraphicalConcept sink = nodes.elementAt(1);

			/* Le noeud source ne doit pas être plus bas que le noeud sink */
			if (source.getRelPosition().getY() > sink.getRelPosition().getY()) {
				source = nodes.elementAt(1);
				sink = nodes.elementAt(0);
			}

			/* Intersection entre l'ideal de la source et le filtre du sink */
			Vector<GraphicalLatticeElement> sourceIdeal = source.getIdeal();
			Vector<GraphicalLatticeElement> sinkFilter = sink.getFilter();
			subLattice.addAll(sourceIdeal);
			subLattice.retainAll(sinkFilter);
		}

		return subLattice;
	}

	/**
	 * Permet de sélectionner l'idéal commun d'une liste de noeuds
	 * @param nodes Le Vector contenant la liste des noeuds pour lesquels le filtre commun est
	 *        cherché
	 */
	public void showCommonFilter(Vector<GraphicalConcept> nodes) {
		Vector<GraphicalLatticeElement> selectedFilter = null;

		for (int i = 0; i < nodesList.size(); i++) {
			(nodesList.elementAt(i)).setSelected(false);
			(nodesList.elementAt(i)).setOutOfFocus(true);
			(nodesList.elementAt(i)).setColorDefault();
			(nodesList.elementAt(i)).setSelectionColorDefault();
		}

		for (int i = 0; i < nodes.size(); i++) {
			GraphicalConcept currNode = nodes.elementAt(i);
			currNode.setSelected(true);
			currNode.setOutOfFocus(false);

			/* Initialisation du vecteur avec les arêtes du 1ier noeud */
			if (selectedFilter == null) {
				selectedFilter = new Vector<GraphicalLatticeElement>();
				selectedFilter.addAll(currNode.getFilter());
			}
			/* Interesection avec les arêtes des autres noeuds */
			else {
				selectedFilter.retainAll(currNode.getFilter());
			}
		}

		/* Mise en subrillance des noeuds et arêtes trouvés */
		for (int i = 0; i < selectedFilter.size(); i++) {
			if (selectedFilter.elementAt(i) instanceof GraphicalConcept)
				((GraphicalConcept) selectedFilter.elementAt(i)).setOutOfFocus(false);
			else
				((Edge) selectedFilter.elementAt(i)).setHighlighted(true);
		}
	}

	/**
	 * Permet de sélectionner l'idéal commun d'une liste de noeuds
	 * @param nodes Le Vector contenant la liste des noeuds pour lesquels l'idéal commun est cherché
	 */
	public void showCommonIdeal(Vector<GraphicalConcept> nodes) {
		Vector<GraphicalLatticeElement> selectedIdeal = null;

		for (int i = 0; i < nodesList.size(); i++) {
			(nodesList.elementAt(i)).setSelected(false);
			(nodesList.elementAt(i)).setOutOfFocus(true);
			(nodesList.elementAt(i)).setColorDefault();
			(nodesList.elementAt(i)).setSelectionColorDefault();
		}

		for (int i = 0; i < nodes.size(); i++) {
			GraphicalConcept currNode = nodes.elementAt(i);
			currNode.setSelected(true);
			currNode.setOutOfFocus(false);

			/* Initialisation du vecteur avec les arêtes du 1ier noeud */
			if (selectedIdeal == null) {
				selectedIdeal = new Vector<GraphicalLatticeElement>();
				selectedIdeal.addAll(currNode.getIdeal());
			}
			/* Intersection avec les arêtes des autres noeuds */
			else {
				selectedIdeal.retainAll(currNode.getIdeal());
			}
		}

		/* Mise en subrillance des noeuds et arêtes trouvés */
		for (int i = 0; i < selectedIdeal.size(); i++) {
			if (selectedIdeal.elementAt(i) instanceof GraphicalConcept)
				((GraphicalConcept) selectedIdeal.elementAt(i)).setOutOfFocus(false);
			else
				((Edge) selectedIdeal.elementAt(i)).setHighlighted(true);
		}
	}

	/**
	 * Permet de sélectionner le treillis dont le top et le bottom on été sélectionné Mais ne
	 * déselectionne pas le reste de la figure --> Permet plusieurs sélections
	 */
	public void showSubLattice(GraphicalConcept top, GraphicalConcept bottom, LatticeColor colorLattice) {

		// ATTENTION, pas de réinitialisation du reste du treillis fait volontairement

		// Mise en évidence du noeud top
		top.setColor(colorLattice);
		top.setSelected(true);
		top.setOutOfFocus(false);

		// Mise en évidence du noeud bottom
		bottom.setColor(colorLattice);
		bottom.setSelected(true);
		bottom.setOutOfFocus(false);

		// Intersection entre l'ideal de le top et le filtre du bottom
		Vector<GraphicalLatticeElement> topIdeal = top.getIdeal();
		Vector<GraphicalLatticeElement> bottomFilter = bottom.getFilter();
		Vector<GraphicalLatticeElement> intersection = topIdeal;
		intersection.retainAll(bottomFilter);

		// Mise en subrillance des noeuds et arrêtes de l'intersection
		for (Object o : intersection) {
			if (o instanceof GraphicalConcept) {
				((GraphicalConcept) o).setColor(colorLattice);
				((GraphicalConcept) o).setOutOfFocus(false);
				((GraphicalConcept) o).setSelected(true);
			} else {
				((Edge) o).setColor(colorLattice);
				((Edge) o).setHighlighted(true);
			}
		}
	}

	/**
	 * Permet de sélectionner le treillis dont la source et le sink on été sélectionné Mais ne
	 * déselectionne pas le reste de la figure --> Permet plusieurs sélections
	 */
	public void showSubLattice(Vector<GraphicalLatticeElement> nodesEdges, LatticeColor colorLattice) {

		// ATTENTION, pas de réinitialisation du reste du treillis fait volontairement

		// Mise en subrillance des noeuds et arrêtes de l'intersection
		for (Object o : nodesEdges) {
			if (o instanceof GraphicalConcept) {
				((GraphicalConcept) o).setColor(colorLattice);
				((GraphicalConcept) o).setOutOfFocus(false);
				((GraphicalConcept) o).setSelected(true);
			} else if (o instanceof Edge) {
				((Edge) o).setColor(colorLattice);
				((Edge) o).setHighlighted(true);
			}
		}
	}

	/**
	 * Cache les noeuds du bas pour ce treillis (et récursivement pour ses treillis internes) si le
	 * noeud du bas du treillis le plus bas du même niveau a une extention vide.
	 * @return Un boolean indiquant si un noeud a été caché
	 */
	public boolean hasHideEmptyBottomConcepts() {
		boolean bottomRemoved = false;

		/* L'opération est exécutée seulement si le noeud du bas n'est pas déjà caché */
		if (!isBottomHidden) {
			isBottomHidden = true;

			/*
			 * Recherche récursivement (en entrant toujours dans les noeuds les plus bas des
			 * treillis uniquement) les noeuds du bas qui n'ont aucun objet
			 */
			int level = 0;
			GraphicalLattice currentLattice = this;
			while (currentLattice != null) {
				BasicSet bottomIntent = currentLattice.getNestedLattice().getBottomNestedConcept().getConcept().getIntent();
				GraphicalConcept bottomNode = currentLattice.getNestedNodeByIntent(bottomIntent);

				if (bottomNode == null) {
					return false;
				}

				/* Cache effectivement le noeud du bas s'il ne contient aucun objet */
				if (bottomNode.getNestedConcept().getExtent().size() == 0) {
					hideBottomConceptsOnLevel(level);
					bottomRemoved = true;
				}

				/* Le prochain treillis est celui imbriqué dans le noeud du bas du treillis courant */
				currentLattice = bottomNode.getInternalLattice();
				level++;
			}
		}

		return bottomRemoved;
	}

	/**
	 * Cache les noeuds du bas pour le treillis imbriqué au niveau donné à partir de ce niveau-ci.
	 * @param level Le nombre de niveaux plus bas où les concepts du bas doivent être retirés
	 */
	public void hideBottomConceptsOnLevel(int level) {
		isBottomHidden = true;

		/* Le niveau recherché n'est pas encore atteint */
		if (level > 0) {
			/* Le niveau est recherché dans chacun des enfants du treillis courant */
			for (int i = 0; i < nodesList.size(); i++) {
				GraphicalConcept node = nodesList.elementAt(i);
				if (node.getInternalLattice() != null)
					node.getInternalLattice().hideBottomConceptsOnLevel(level - 1);
			}
		}

		/* Ce treillis est au niveau recherché : son noeud du bas est caché */
		else {
			BasicSet bottomIntent = nestedConceptLattice.getBottomNestedConcept().getConcept().getIntent();
			GraphicalConcept bottomNode = getNestedNodeByIntent(bottomIntent);
			bottomNode.setVisible(false);
		}
	}

	/**
	 * Affiche les noeuds du bas pour ce treillis (et récursivement pour ses treillis internes) même
	 * si le noeud du bas du treillis le plus bas du même niveau a une extention vide.
	 */
	public void showAllConcepts() {
		isBottomHidden = false;

		/* Tous les noeuds sont rendus visibles */
		for (GraphicalConcept node : nodesList) {
			node.setVisible(true);

			/* Propagation du changement dans les treillis internes des noeuds */
			if (node.getInternalLattice() != null)
				node.getInternalLattice().showAllConcepts();
		}
	}

	public void setNormalSize() {
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept concept = nodesList.elementAt(i);
			if (CONCEPT_SIZE == GraphicalConcept.LARGE_NODE_SIZE && concept.getSizeType() != LMOptions.VARY)
				concept.setSizeType(LMOptions.LARGE);
			else if (CONCEPT_SIZE == GraphicalConcept.SMALL_NODE_SIZE && concept.getSizeType() != LMOptions.VARY)
				concept.setSizeType(LMOptions.SMALL);
			else
				concept.setSizeType(LMOptions.VARY);

			if (concept.getInternalLattice() != null)
				concept.getInternalLattice().setNormalSize();
		}
	}

	public void setSmallSize() {
		for (int i = 0; i < nodesList.size(); i++)
			(nodesList.elementAt(i)).setSizeType(LMOptions.SMALL);
	}

	public void setLargeSize() {
		for (int i = 0; i < nodesList.size(); i++)
			(nodesList.elementAt(i)).setSizeType(LMOptions.LARGE);
	}

	public void setVarySize() {
		for (int i = 0; i < nodesList.size(); i++)
			(nodesList.elementAt(i)).setSizeType(LMOptions.VARY);
	}

	/**
	 * Rafraîchie le treillis en le positionnant à l'endroit spécifié, avec l'échelle voulue
	 * @param rootPosX Le double contenant la position en x de la racine du treillis
	 * @param rootPosY Le double contenant la position en y de la racine du treillis
	 * @param newScale Le double contenant l'échelle voulue
	 */
	public void resetLatticeImage(double rootPosX, double rootPosY, double newScale) {
		scale = newScale;
		topNode.setPosition(rootPosX, rootPosY);
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			node.refreshShape();
		}
	}

	/**
	 * Permet d'indiquer au treillis qu'un clic a eu lieu
	 * @param x La position du clic en X
	 * @param y La position du clic en Y
	 * @return Le GraphicalConcept qui a été touché
	 */
	public GraphicalConcept mouseClicked(double x, double y) {
		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept selectedNode = null;
			GraphicalConcept node = nodesList.elementAt(i);

			/*
			 * Pour être clické, un noeud doit d'abord être visible, et aucun noeud qu'il contient
			 * ne doit avoir été touché par le click
			 */
			if (node.getShape().contains(x, y)) {
				/*
				 * Après ce bloc conditionnel, selectedNode contient soit un noeud interne, soit le
				 * noeud courant
				 */
				if (node.getInternalLattice() == null || !node.getInternalLattice().isVisible()
						|| (selectedNode = node.getInternalLattice().mouseClicked(x, y)) == null)
					selectedNode = node;

				return selectedNode;
			}
		}

		/* Aucun noeud de ce treillis ne contient les coordonnées du click */
		return null;
	}

	/**
	 * Permet d'indiquer au treillis qu'un bouton de la souris a été pesé
	 * @param x La position du clic en X
	 * @param y La position du clic en Y
	 * @return L'Object qui a été touché
	 */
	public Object mousePressed(double x, double y) {
		/* Recherche dans les étiquettes en premier */
		for (int i = 0; i < nodesList.size(); i++) {
			Object internalObject = null;
			GraphicalConcept node = nodesList.elementAt(i);
			GraphicalLattice nodeInternalLattice = node.getInternalLattice();
			ConceptLabel nodeLabel = node.getLabel();


			if (nodeInternalLattice != null && nodeInternalLattice.isVisible())
				internalObject = nodeInternalLattice.mousePressed(x, y);

			/* Les étiquettes internes ont priorité */
			if (internalObject != null && internalObject instanceof ConceptLabel)
				return internalObject;

			else if ((nodeLabel.isVisible() || nodeLabel.isHighlighted()) && nodeLabel.getShape().contains(x, y))
				return nodeLabel;
		}

		/* Ensuite recherche dans les noeuds */
		for (int i = 0; i < nodesList.size(); i++) {
			Object internalObject = null;
			GraphicalConcept node = nodesList.elementAt(i);
			GraphicalLattice nodeInternalLattice = node.getInternalLattice();

			if (nodeInternalLattice != null && nodeInternalLattice.isVisible())
				internalObject = nodeInternalLattice.mousePressed(x, y);

			/* Les noeuds internes ont priorité */
			if (internalObject != null && internalObject instanceof GraphicalConcept)
				return internalObject;

			else if (node.getShape().contains(x, y))
				return node;
		}

		/* Aucune étiquette et aucun noeud ne contient la position de la souris */
		return null;
	}

	/**
	 * Permet d'afficher les étiquettes d'un treillis
	 * @param g Le composant Graphics2D qui doit afficher les étiquettes
	 */
	public void paintLabels(Graphics g, Rectangle2D r) {
		Graphics2D g2 = (Graphics2D) g;
		if (isVisible && nodesList != null) {
			for (int i = 0; i < nodesList.size(); i++)
				(nodesList.elementAt(i)).paintLabels(g2, r);
		}
	}

	/**
	 * Permet d'afficher un treillis
	 * @param g Le composant Graphics2D qui doit afficher le graphe
	 * @param r Le rectangle a mettre a jour
	 */
	public void paint(Graphics g, Rectangle2D r) {
		Graphics2D g2 = (Graphics2D) g;
		if (isVisible && nodesList != null) {
			for (int i = 0; i < nodesList.size(); i++)
				(nodesList.elementAt(i)).paintEdges(g2, r);
			for (int i = 0; i < nodesList.size(); i++)
				(nodesList.elementAt(i)).paintNode(g2, r);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = GUIMessages.getString("GUI.latticeName")+" : " + getName() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str = str + GUIMessages.getString("GUI.nodeCount")+" : " + nodesList.size() + " "+GUIMessages.getString("GUI.level")+" : " + nestedConceptLattice.getNestedLevel() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		str = str + "***** "+GUIMessages.getString("GUI.positions")+" *****\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str = str + graphStructure.toString();

		str = str + "\n"+GUIMessages.getString("GUI.internalLattice")+" :\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (topNode.getInternalLattice() != null)
			str = str + topNode.getInternalLattice().toString();
		else
			str = str + GUIMessages.getString("GUI.noInternalLattice")+"\n"; //$NON-NLS-1$ //$NON-NLS-2$

		return str;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		Vector<LatticeStructure> structuresList = new Vector<LatticeStructure>();
		structuresList.add(graphStructure);
		structuresList.addAll(subStructures);

		GraphicalLattice res = new GraphicalLattice(nestedConceptLattice, externalGraphNode, structuresList);
		res.selectionContrastType = this.selectionContrastType;

		return res;
	}


	/**
	 * Fonction appelee lors de l'utilisation du slider
	 * @param size La nouvelle taille du texte des etiquettes
	 */
	public void fontSizeChanged(int size){
		changingFontSize(size,nodesList);
	}


	public void changingFontSize(int size, Vector<GraphicalConcept> nodesL){
		for (int i = 0; i < nodesL.size(); i++) {
			GraphicalConcept node = nodesL.elementAt(i);
			ConceptLabel nodeLabel = node.getLabel();
			nodeLabel.setFontSize(size);
			if(node.getInternalLattice()!=null){
				changingFontSize(size,nodesL.elementAt(i).getInternalLattice().getNodesList());
			}
		}
	}

	/**
	 * Permet de modifier le type d'affichage des étiquettes pour les objets
	 * @param type Le int contenant le type de valeur à afficher
	 */
	public void setGeneLabelType(int type) {
		nestedConceptLattice.getConceptLattice().findGenerators();

		for (int i = 0; i < nodesList.size(); i++) {
			GraphicalConcept node = nodesList.elementAt(i);
			node.getLabel().setGeneLabelType(type);

			/* Propagation du changement dans les treillis internes */
			if (node.getInternalLattice() != null)
				node.getInternalLattice().setGeneLabelType(type);
		}
	}

	/**
	 * Permet d'obtenir le noeud graphique qui représente le concept de la cellule spécifiée
	 * @param LA cellule a chercher
	 * @return le NestedConcept qui contient le Concept ayant l'identifiant donné
	 */
	public GraphicalConcept getNestedNodeByCellule(DataCel cel) {
		BasicSet intent=cel.getIntent();
		BasicSet ext=cel.getExtent();
		for (int i = 0; i < nodesList.size(); i++) {
			for (int j = 0; j < nodesList.size(); j++){
				GraphicalConcept node = nodesList.elementAt(i);
				node = nodesList.elementAt(j);
				if ( (node.getNestedConcept().getConcept().getIntent().equals(intent)) && (node.getNestedConcept().getConcept().getExtent().equals(ext))   )
					return node;
			}
		}
		return null;
	}

	/**
	 * Permet de verifier si une cellule appartient au contexte globale
	 * @param La cellule a chercher
	 * @return boolean
	 */
	public boolean verifyCellule(DataCel cel) {
		BasicSet intent=cel.getIntent();
		BasicSet ext=cel.getExtent();
		for (int i = 0; i < nodesList.size(); i++) {
			for (int j = 0; j < nodesList.size(); j++){
				GraphicalConcept node = nodesList.elementAt(i);
				node = nodesList.elementAt(j);

				if ( (node.getNestedConcept().getConcept().getIntent().equals(intent)) && (node.getNestedConcept().getConcept().getExtent().equals(ext))   )
					return true;
			}
		}
		return false;
	}



}