package fca.gui.lattice.element;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.ImageIcon;

import fca.core.lattice.FormalConcept;
import fca.core.lattice.NestedConcept;
import fca.core.lattice.NestedLattice;
import fca.gui.util.constant.LMImages;
import fca.gui.util.constant.LMOptions;
import fca.gui.util.constant.LMColors.LatticeColor;
import fca.gui.util.constant.LMColors.SelectionColor;

/**
 * Définition d'un noeud graphique pour un treillis
 * @author Geneviève Roberge
 * @author Ludovic Thomas
 * @version 2.0
 */
public class GraphicalConcept extends GraphicalLatticeElement {

	/** Diamètre réel du concept graphique : Petit */
	public static final double SMALL_NODE_SIZE = 1.0;

	/** Diamètre réel du concept graphique : Large */
	public static final double LARGE_NODE_SIZE = 1.6;

	/** Diamètre réel du concept graphique : Point */
	public static final double DOT_NODE_SIZE = 0.2;

	protected NestedConcept nestedConcept; // Le FormalConcept représenté dans ce noeud
	/**
	 * inverse="nodesList:fca.lattice.graphical.GraphicalLattice"
	 */
	protected GraphicalLattice parentLattice; // Le graphe auquel appartient ce noeud

	protected GraphicalLattice internalLattice; // Le graphe qui est à l'intéreur de ce noeud

	protected Vector<Edge> childrenEdges; // La liste des arêtes vers les enfants de ce noeud

	protected Vector<Edge> parentEdges; // La liste des arêtes vers les parents de ce noeud

	protected Vector<LatticeStructure> subStructures; // La liste des structures pour les graphes internes

	protected double scale;

	protected Ellipse2D shape; // La forme de ce noeud

	protected double relX; // La position en X relative au sommet du treillis

	protected double relY; // La position en Y relative au sommet du treillis

	protected int sizeType; // Le type de grosseur du concept (petit ou grand)

	protected boolean isVisible; // Un indicateur disant si le noeud est affiché

	protected boolean isOutOfFocus; // Un indicateur disant si le noeud est flou

	/**
	 * inverse="parentConcept:fca.lattice.graphical.ConceptLabel"
	 */
	protected ConceptLabel label; // L'étiquette donnant l'information sur le noeud

	protected int childNavigationPos; // La position de l'enfant duquel on navige le filtre

	protected int intensity; // Intensité de la couleur associée à ce concept (échelle de 1 à 5)

	protected SelectionColor selectionColor; // Couleur de la selection

	protected boolean ownObject; // Indique si un nouvel objet apparait dans ce concept

	protected boolean sizeDependsOnOwnObjects; // Indique si la taille varie en fonction des objets

	// propres

	/**
	 * Constructeur
	 * @param nc Le NestedConcept représenté par ce noeud
	 * @param p Le GraphicalLattice auquel appartient le noeud
	 * @param s La liste des structures pour l'affichage des treillis internes
	 */
	public GraphicalConcept(NestedConcept nc, GraphicalLattice p, Vector<LatticeStructure> s) {
		nestedConcept = nc;
		parentLattice = p;
		subStructures = s;
		childrenEdges = new Vector<Edge>();
		parentEdges = new Vector<Edge>();
		internalLattice = null;
		isVisible = true;
		isSelected = false;
		isHighlighted = false;
		isOutOfFocus = false;
		scale = 0;
		setColorDefault();
		setSelectionColorDefault();
		if (parentLattice.getConceptSize() == LARGE_NODE_SIZE)
			sizeType = LMOptions.LARGE;
		else
			sizeType = LMOptions.SMALL;
		shape = new Ellipse2D.Double(0, 0, 1, 1);
		relX = 0;
		relY = 0;

		if (p != null)
			label = new ConceptLabel(this, p.getContextObjectCount(), p.getExtNodeObjectCount());
		else
			label = new ConceptLabel(this, nc.getExtent().size(), nc.getExtent().size());
		childNavigationPos = 0;

		ownObject = nestedConcept.getReducedExtent().size() > 0;
		// sizeDependsOnOwnObjects = false;

		calcIntensity();
		createInternalLattice();
	}

	/**
	 * Permet d'obtenir le NestedConcept auquel est rattaché ce noeud
	 * @return Le NestedConcept correspondant au concept du noeud
	 */
	public NestedConcept getNestedConcept() {
		return nestedConcept;
	}

	/**
	 * Permet d'obtenir le FormalConcept auquel est rattaché ce noeud
	 * @return Le FormalConcept correspondant au concept du noeud
	 */
	public FormalConcept getConcept() {
		return nestedConcept.getConcept();
	}

	/**
	 * Permet d'obtenir la liste des arêtes de ce noeud menant à ses enfants
	 */
	public Vector<Edge> getChildrenEdges() {
		return childrenEdges;
	}

	/**
	 * Permet d'obtenir la liste des arêtes de ce noeud en provenance de ses parents
	 */
	public Vector<Edge> getParentEdges() {
		return parentEdges;
	}

	/**
	 * Permet d'obtenir l'élément graphique qui forme ce noeud
	 * @return La Ellipse2D correspondant à la forme du noeud
	 */
	public Ellipse2D getShape() {
		return shape;
	}

	/*
	 * (non-Javadoc)
	 * @see fca.lattice.graphical.GraphicalLatticeElement#getColorDefault()
	 */
	@Override
	public LatticeColor getColorDefault() {
		if (internalLattice == null)
			return LatticeColor.DEFAULT;
		else
			return internalLattice.getLatticeColor();
	}

	/**
	 * @return the conceptColor
	 */
	public SelectionColor getSelectionColor() {
		return selectionColor;
	}

	/**
	 * @param selectionColor the conceptColor to set
	 */
	public void setSelectionColor(SelectionColor selectionColor) {
		this.selectionColor = selectionColor;
	}

	/**
	 * Set the conceptColor to the default one
	 */
	public void setSelectionColorDefault() {
		if (internalLattice == null)
			this.selectionColor = SelectionColor.DEFAULT;
		else if (internalLattice.getLatticeColor() == LatticeColor.PINK
				|| internalLattice.getLatticeColor() == LatticeColor.ORANGE)
			this.selectionColor = SelectionColor.GREEN;
		else
			this.selectionColor = SelectionColor.RED;
	}

	/**
	 * Permet d'obtenir le nombre d'objets contenus dans ce noeud
	 * @return Le int contenant le nombre d'objets
	 */
	public int getObjectCount() {
		return nestedConcept.getExtent().size();
	}

	/**
	 * Permet d'obtenir l'étiquette du noeud
	 * @return Le NodeLabel correspondant à l'étiquette du noeud
	 */
	public ConceptLabel getLabel() {
		return label;
	}

	/**
	 * Permet d'afficher ou cacher l'étiquette du noeud
	 */
	public void toggleLabel() {
		label.setVisible(!label.isVisible());
	}

	/**
	 * Permet de savoir si le noeud est affiché
	 * @return Le boolean indiquant si le noeud est affiché
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Permet de savoir si le noeud est flou
	 * @return Le boolean indiquant si le noeud est flou
	 */
	public boolean isOutOfFocus() {
		return isOutOfFocus;
	}

	/**
	 * Permet d'obtenir la position relative du noeud, telle que spécifiée dans sa structure (ne
	 * tient pas compte de l'échelle)
	 * @return Le Point2D où est situé le noeud par rapport à la racine
	 */
	public Point2D getRelPosition() {
		return new Point2D.Double(relX, relY);
	}

	/**
	 * Permet d'obtenir l'échelle pour l'affichage du noeud
	 * @return Le double contenant l'échelle
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Permet d'obtenir le type de taille du concept (petit, grand, varie)
	 * @return Le int contenant le type de taille
	 */
	public int getSizeType() {
		return sizeType;
	}

	/**
	 * Permet d'obtenir le graphe interne du noeud
	 * @return Le GraphicalLattice à l'intérieur du noeud
	 */
	public GraphicalLattice getInternalLattice() {
		return internalLattice;
	}

	/**
	 * Permet d'obtenir le graphe auquel appartient ce noeud
	 * @return Le GraphicalLattice auquel appartient ce noeud
	 */
	public GraphicalLattice getParentLattice() {
		return parentLattice;
	}

	/**
	 * Permet d'ajouter une arête vers un noeud enfant
	 * @param e la LatticeEdge à ajouter
	 */
	public void addChildEdge(Edge e) {
		if (childrenEdges.indexOf(e) == -1)
			childrenEdges.add(e);
	}

	/**
	 * Permet d'ajouter une arête arrivant d'un noeud parent
	 * @param e la LatticeEdge à ajouter
	 */
	public void addParentEdge(Edge e) {
		if (parentEdges.indexOf(e) == -1)
			parentEdges.add(e);
	}

	/**
	 * Permet d'afficher / cacher un noeud
	 * @param v Le boolean indiquant si le noeud doit être affiché
	 */
	public void setVisible(boolean v) {
		isVisible = v;
	}

	/**
	 * Permet de mettre / enlever le flou sur un noeud
	 * @param s Le boolean indiquant si le noeud doit être flou
	 */
	public void setOutOfFocus(boolean s) {
		isOutOfFocus = s;

		if (internalLattice != null)
			internalLattice.setOutOfFocus(s);
	}

	/**
	 * Permet d'ajuster la taille du concept (petit ou grand)
	 * @param type Le int contenant le type de taille.
	 */
	public void setSizeType(int type) {
		if (sizeType == type)
			return;
		if (type != LMOptions.SMALL && type != LMOptions.LARGE && type != LMOptions.VARY)
			return;

		double scale = parentLattice.getScale();
		double dotSize = DOT_NODE_SIZE * scale;
		double smallSize = SMALL_NODE_SIZE * scale;
		double largeSize = LARGE_NODE_SIZE * scale;

		if (type == LMOptions.SMALL && sizeType == LMOptions.LARGE) {
			sizeType = LMOptions.SMALL;
			double x = shape.getX() + ((largeSize - smallSize) / 2.0);
			double y = shape.getY() + ((largeSize - smallSize) / 2.0);
			shape.setFrame(x, y, smallSize, smallSize);
		}

		else if (type == LMOptions.SMALL && sizeType == LMOptions.VARY) {
			sizeType = LMOptions.SMALL;
			if (ownObject) {
				double x = shape.getX() + ((largeSize - smallSize) / 2.0);
				double y = shape.getY() + ((largeSize - smallSize) / 2.0);
				shape.setFrame(x, y, smallSize, smallSize);
			} else {
				double x = shape.getX() - ((smallSize - dotSize) / 2.0);
				double y = shape.getY() - ((smallSize - dotSize) / 2.0);
				shape.setFrame(x, y, smallSize, smallSize);
			}
		}

		else if (type == LMOptions.LARGE && sizeType == LMOptions.SMALL) {
			sizeType = LMOptions.LARGE;
			double x = shape.getX() - ((largeSize - smallSize) / 2.0);
			double y = shape.getY() - ((largeSize - smallSize) / 2.0);
			shape.setFrame(x, y, largeSize, largeSize);
		}

		else if (type == LMOptions.LARGE && sizeType == LMOptions.VARY) {
			sizeType = LMOptions.LARGE;
			if (!ownObject) {
				double x = shape.getX() - ((largeSize - dotSize) / 2.0);
				double y = shape.getY() - ((largeSize - dotSize) / 2.0);
				shape.setFrame(x, y, largeSize, largeSize);
			}
		}

		else if (type == LMOptions.VARY && sizeType == LMOptions.LARGE) {
			sizeType = LMOptions.VARY;
			if (!ownObject) {
				double x = shape.getX() + ((largeSize - dotSize) / 2.0);
				double y = shape.getY() + ((largeSize - dotSize) / 2.0);
				shape.setFrame(x, y, dotSize, dotSize);
			}
		}

		else if (type == LMOptions.VARY && sizeType == LMOptions.SMALL) {
			sizeType = LMOptions.VARY;
			if (ownObject) {
				double x = shape.getX() - ((largeSize - smallSize) / 2.0);
				double y = shape.getY() - ((largeSize - smallSize) / 2.0);
				shape.setFrame(x, y, largeSize, largeSize);
			} else {
				double x = shape.getX() + ((smallSize - dotSize) / 2.0);
				double y = shape.getY() + ((smallSize - dotSize) / 2.0);
				shape.setFrame(x, y, dotSize, dotSize);
			}
		}

		if (internalLattice != null)
			internalLattice.refreshConceptsShape();

		label.refreshShape();
	}

	/**
	 * Permet d'ajuster la position relative d'un noeud
	 * @param x Le double contenant la nouvelle position relative en X du noeud
	 * @param y Le double contenant la nouvelle position relative en Y du noeud
	 */
	public void setRelPosition(double x, double y) {
		relX = x;
		relY = y;
	}

	/**
	 * Permet d'ajuster la position réelle d'un noeud
	 * @param x Le double contenant la nouvelle position réelle en X du noeud
	 * @param y Le double contenant la nouvelle position réelle en Y du noeud
	 */
	public void setPosition(double x, double y) {
		double scale = parentLattice.getScale();
		double parentSize = parentLattice.getConceptSize();
		double smallSize = SMALL_NODE_SIZE;
		double largeSize = LARGE_NODE_SIZE;
		double dotSize = DOT_NODE_SIZE;

		if (sizeType == LMOptions.SMALL && parentSize == smallSize) {
			shape.setFrame(x, y, smallSize * scale, smallSize * scale);
		} else if (sizeType == LMOptions.SMALL) {
			double new_x = x - (largeSize - smallSize) * scale / 2.0;
			double new_y = y - (largeSize - smallSize) * scale / 2.0;
			shape.setFrame(new_x, new_y, smallSize * scale, smallSize * scale);
		} else if (sizeType == LMOptions.LARGE && parentSize == largeSize) {
			shape.setFrame(x, y, largeSize * scale, largeSize * scale);
		} else if (sizeType == LMOptions.LARGE) {
			double new_x = x + (largeSize - smallSize) * scale / 2.0;
			double new_y = y + (largeSize - smallSize) * scale / 2.0;
			shape.setFrame(new_x, new_y, largeSize * scale, largeSize * scale);
		} else if (sizeType == LMOptions.VARY && ownObject) {
			shape.setFrame(x, y, largeSize * scale, largeSize * scale);
		} else if (sizeType == LMOptions.VARY) {
			shape.setFrame(x, y, dotSize * scale, dotSize * scale);
		}

		label.refreshShape();
	}

	/**
	 * Permet de rafraîchir la forme de ce noeud, c'est à dire de lui donner la bonne échelle et la
	 * bonne position réelle qui correspond à sa position relative à la racine du treillis
	 */
	public void refreshShape() {
		double scale = parentLattice.getScale();
		double parentSize = parentLattice.getConceptSize();
		double smallSize = SMALL_NODE_SIZE;
		double largeSize = LARGE_NODE_SIZE;
		double dotSize = DOT_NODE_SIZE;

		double x = parentLattice.getRootPosition().getX() + relX * scale;
		double y = parentLattice.getRootPosition().getY() + relY * scale;

		if (sizeType == LMOptions.SMALL && parentSize == smallSize) {
			shape.setFrame(x, y, smallSize * scale, smallSize * scale);
		} else if (sizeType == LMOptions.SMALL) {
			double new_x = x + (largeSize - smallSize) * scale / 2.0;
			double new_y = y + (largeSize - smallSize) * scale / 2.0;
			shape.setFrame(new_x, new_y, smallSize * scale, smallSize * scale);
		} else if (sizeType == LMOptions.LARGE && parentSize == largeSize) {
			shape.setFrame(x, y, largeSize * scale, largeSize * scale);
		} else if (sizeType == LMOptions.LARGE) {
			double new_x = x - (largeSize - smallSize) * scale / 2.0;
			double new_y = y - (largeSize - smallSize) * scale / 2.0;
			shape.setFrame(new_x, new_y, largeSize * scale, largeSize * scale);
		} else if (sizeType == LMOptions.VARY && ownObject) {
			double new_x = x - (largeSize - dotSize) * scale / 2.0;
			double new_y = y - (largeSize - dotSize) * scale / 2.0;
			shape.setFrame(new_x, new_y, largeSize * scale, largeSize * scale);
		} else if (sizeType == LMOptions.VARY) {
			shape.setFrame(x, y, dotSize * scale, dotSize * scale);
		}

		label.refreshShape();

		/* Les noeuds du treillis interne doivent aussi être ajustés */
		if (internalLattice != null)
			internalLattice.refreshConceptsShape();
	}

	/**
	 * Permet de faire bouger légèrement le concept à gauche, sans déplacer son étiquette.
	 */
	public void moveLeft(double ratio) {
		double x = shape.getX();
		double y = shape.getY();
		double size = shape.getWidth();
		shape.setFrame(x - ratio, y, size, size);
	}

	/**
	 * Permet de faire bouger légèrement le concept à droite, sans déplacer son étiquette.
	 */
	public void moveRight(double ratio) {
		// double decX = ratio * parentLattice.getScale();
		double x = shape.getX();
		double y = shape.getY();
		double size = shape.getWidth();
		// shape.setFrame(x+decX,y,size,size);
		shape.setFrame(x + ratio, y, size, size);
	}

	/**
	 * Permet de calculer la valeur entre 1 et 5 de l'intensité de la couleur associée à ce concept.
	 * 1 - Entre 0% et 20% des objets 2 - Entre 21% et 40% des objets 3 - Entre 41% et 60% des
	 * objets 4 - Entre 61% et 80% des objets 5 - Entre 81 et 100% des objets
	 */
	private void calcIntensity() {
		int latticeObjectCount = parentLattice.getExtNodeObjectCount();
		int conceptObjectCount = nestedConcept.getExtent().size();

		double perc = (double) conceptObjectCount / (double) latticeObjectCount;

		if (perc <= 0.1)
			intensity = 1;
		else if (perc <= 0.2)
			intensity = 2;
		else if (perc <= 0.4)
			intensity = 3;
		else if (perc <= 0.65)
			intensity = 4;
		else
			intensity = 5;
	}

	/**
	 * Permet de créer le graphe interne du noeud
	 */
	public void createInternalLattice() {
		NestedLattice internalNL = nestedConcept.getInternalNestedLattice();
		if (internalNL != null) {
			internalLattice = new GraphicalLattice(internalNL, this, subStructures);
			// Take care of the lattice color to colore the concept and its selection
			setColorDefault();
			setSelectionColorDefault();
		} else
			internalLattice = null;
	}

	/**
	 * Permet d'obtenir les parents et arrêtes de ce noeud
	 * @return Le Vector qui contient les parents
	 */
	public Vector<GraphicalLatticeElement> getParents() {
		Vector<GraphicalLatticeElement> parents = new Vector<GraphicalLatticeElement>();
		if (parentEdges != null)
			for (int i = 0; i < parentEdges.size(); i++) {
				Edge edge = parentEdges.elementAt(i);
				parents.add(edge);
				parents.add(edge.getSource());
			}

		return parents;
	}

	/**
	 * Permet d'obtenir les enfants de ce noeud
	 * @return Le Vector qui contient les enfants
	 */
	public Vector<GraphicalLatticeElement> getChildren() {
		Vector<GraphicalLatticeElement> children = new Vector<GraphicalLatticeElement>();
		if (childrenEdges != null)
			for (int i = 0; i < childrenEdges.size(); i++) {
				Edge edge = childrenEdges.elementAt(i);
				children.add(edge);
				children.add(edge.getDestination());
			}

		return children;
	}

	/**
	 * Permet d'obtenir le filtre de ce noeud
	 * @return Le Vector qui contient les arêtes du filtre
	 */
	public Vector<GraphicalLatticeElement> getFilter() {
		Vector<GraphicalLatticeElement> filter = new Vector<GraphicalLatticeElement>();
		if (parentEdges != null)
			for (int i = 0; i < parentEdges.size(); i++) {
				/* Les noeuds et les arêtes sont retournés dans un même vecteur */
				Edge edge = parentEdges.elementAt(i);
				filter.add(edge);
				filter.add(edge.getSource());
				filter.addAll(edge.getSource().getFilter());
			}
		return filter;
	}

	/**
	 * Permet d'obtenir l'idéal du noeud
	 * @return Le Vector qui contient l'idéal
	 */
	public Vector<GraphicalLatticeElement> getIdeal() {
		Vector<GraphicalLatticeElement> ideal = new Vector<GraphicalLatticeElement>();
		if (childrenEdges != null)
			for (int i = 0; i < childrenEdges.size(); i++) {
				/* Les noeuds et les arêtes sont retournés dans un même vecteur */
				Edge edge = childrenEdges.elementAt(i);
				ideal.add(edge);
				ideal.add(edge.getDestination());
				ideal.addAll(edge.getDestination().getIdeal());
			}
		return ideal;
	}

	//	/**
	//	 * Permet de sélectionner le filtre du noeud
	//	 */
	//	public void showFilter() {
	//		if (parentEdges != null)
	//			for (int i = 0; i < parentEdges.size(); i++) {
	//				Edge edge = parentEdges.elementAt(i);
	//				edge.setHighlighted(true);
	//				edge.getSource().setOutOfFocus(false);
	//				edge.getSource().showFilter();
	//				edge.getSource().setHighlighted(true);
	//
	//				GraphicalLattice internLat = edge.getSource().getInternalLattice();
	//				if (internLat != null)
	//					internLat.setOutOfFocus(false);
	//			}
	//	}
	//
	//	/**
	//	 * Permet de sélectionner l'idéal du noeud
	//	 */
	//	public void showIdeal() {
	//		if (childrenEdges != null)
	//			for (int i = 0; i < childrenEdges.size(); i++) {
	//				Edge edge = childrenEdges.elementAt(i);
	//				edge.setHighlighted(true);
	//				edge.getDestination().setOutOfFocus(false);
	//				edge.getDestination().showIdeal();
	//				edge.getDestination().setHighlighted(true);
	//
	//				GraphicalLattice internLat = edge.getSource().getInternalLattice();
	//				if (internLat != null)
	//					internLat.setOutOfFocus(false);
	//			}
	//	}
	//
	//	/**
	//	 * Permet de sélectionner les parents du noeud et les arêtes correspondantes
	//	 */
	//	public void showParents() {
	//		if (parentEdges != null)
	//			for (int i = 0; i < parentEdges.size(); i++) {
	//				Edge edge = parentEdges.elementAt(i);
	//				edge.setHighlighted(true);
	//				edge.getSource().setOutOfFocus(false);
	//				edge.getSource().setHighlighted(true);
	//
	//				GraphicalLattice internLat = edge.getSource().getInternalLattice();
	//				if (internLat != null)
	//					internLat.setOutOfFocus(false);
	//			}
	//	}
	//
	//	/**
	//	 * Permet de sélectionner les enfants du noeud et les arêtes correspondantes
	//	 */
	//	public void showChildren() {
	//		if (childrenEdges != null)
	//			for (int i = 0; i < childrenEdges.size(); i++) {
	//				Edge edge = childrenEdges.elementAt(i);
	//				edge.setHighlighted(true);
	//				edge.getDestination().setOutOfFocus(false);
	//				edge.getDestination().setHighlighted(true);
	//
	//				GraphicalLattice internLat = edge.getSource().getInternalLattice();
	//				if (internLat != null)
	//					internLat.setOutOfFocus(false);
	//			}
	//	}
	//
	//	/**
	//	 * Permet de sélectionner le premier enfant du noeud (et l'arête correspondante) ainsi que
	//	 * chacun des parents de cet enfant (avec les arêtes correspondantes)
	//	 */
	//	public void showFirstChild() {
	//		if (childrenEdges != null) {
	//			Edge edge = childrenEdges.elementAt(0);
	//			edge.setHighlighted(true);
	//			edge.getDestination().setOutOfFocus(false);
	//			edge.getDestination().setHighlighted(true);
	//			edge.getDestination().showParents();
	//
	//			GraphicalLattice internLat = edge.getSource().getInternalLattice();
	//			if (internLat != null)
	//				internLat.setOutOfFocus(false);
	//		}
	//	}
	//
	//	/**
	//	 * Permet de sélectionner le prochain enfant du noeud (et l'arête correspondante) ainsi que
	//	 * chacun des parents de cet enfant (avec les arêtes correspondantes)
	//	 */
	//	public void showNextChild() {
	//		if (childrenEdges != null && childrenEdges.size() > childNavigationPos + 1) {
	//			Edge edge = childrenEdges.elementAt(childNavigationPos);
	//			edge.getDestination().setOutOfFocus(true);
	//			edge.getDestination().setColorDefault();
	//			edge.getDestination().setHighlighted(true);
	//			edge.getDestination().setParentsOutOfFocus();
	//			childNavigationPos++;
	//			edge = childrenEdges.elementAt(childNavigationPos);
	//			edge.getDestination().setOutOfFocus(false);
	//			edge.getDestination().showParents();
	//
	//			GraphicalLattice internLat = edge.getSource().getInternalLattice();
	//			if (internLat != null)
	//				internLat.setOutOfFocus(false);
	//		}
	//	}
	//
	//	/**
	//	 * Permet de sélectionner l'enfant précédent du noeud (et l'arête correspondante) ainsi que
	//	 * chacun des parents de cet enfant (avec les arêtes correspondantes)
	//	 */
	//	public void showPreviousChild() {
	//		if (childrenEdges != null && childrenEdges.size() > 0 && childNavigationPos > 0) {
	//			Edge edge = childrenEdges.elementAt(childNavigationPos);
	//			edge.getDestination().setOutOfFocus(true);
	//			edge.getDestination().setColorDefault();
	//			edge.getDestination().setHighlighted(true);
	//			edge.getDestination().setParentsOutOfFocus();
	//			childNavigationPos--;
	//			edge = childrenEdges.elementAt(childNavigationPos);
	//			edge.getDestination().setOutOfFocus(false);
	//			edge.getDestination().showParents();
	//
	//			GraphicalLattice internLat = edge.getSource().getInternalLattice();
	//			if (internLat != null)
	//				internLat.setOutOfFocus(false);
	//		}
	//	}
	//
	//	/**
	//	 * Permet d'enlever le focus aux parents du noeud
	//	 */
	//	public void setParentsOutOfFocus() {
	//		if (parentEdges != null)
	//			for (int i = 0; i < parentEdges.size(); i++) {
	//				Edge edge = parentEdges.elementAt(i);
	//				edge.setHighlighted(false);
	//				edge.setSelected(false);
	//				edge.getSource().setOutOfFocus(true);
	//				edge.getSource().setColorDefault();
	//				edge.getSource().setSelectionColorDefault();
	//
	//				GraphicalLattice internLat = edge.getSource().getInternalLattice();
	//				if (internLat != null)
	//					internLat.setOutOfFocus(true);
	//			}
	//	}

	/**
	 * Permet d'enlever la subrillance de toutes les arêtes (et les noeuds associés) du noeud
	 */
	public void deselectEdges() {
		if (childrenEdges != null)
			for (int i = 0; i < childrenEdges.size(); i++) {
				Edge edge = childrenEdges.elementAt(i);
				edge.setSelected(false);
				edge.setHighlighted(false);
				edge.setColorDefault();
			}

		if (parentEdges != null)
			for (int i = 0; i < parentEdges.size(); i++) {
				Edge edge = parentEdges.elementAt(i);
				edge.setSelected(false);
				edge.setHighlighted(false);
				edge.setColorDefault();
			}
	}

	/**
	 * Permet d'obtenir la position en y du parent le plus bas
	 * @return Le double contenant la position en y du parent le plus bas
	 */
	public double getLowestParentY() {
		/* Aucun parent => aucune contrainte en y vers le haut */
		if (parentEdges == null)
			return Double.MIN_VALUE;

		double maxY = Double.MIN_VALUE;
		for (int i = 0; i < parentEdges.size(); i++) {
			/*
			 * Recherche de l'extrémité la plus haute de l'arête (la plus basse étant le noeud
			 * courant)
			 */
			Edge currentEdge = parentEdges.elementAt(i);
			double y1 = currentEdge.getSource().getShape().getY();
			double y2 = currentEdge.getDestination().getShape().getY();
			double y = (y1 < y2) ? y1 : y2;

			/* Recherche de l'extrémité la plus basse parmi toutes les extrémités hautes */
			if (y > maxY)
				maxY = y;
		}

		return maxY;
	}

	/**
	 * Permet d'obtenir la position en y de l'enfant le plus haut
	 */
	public double getHighestChildY() {
		/* Aucun enfant => aucune contrainte en y vers le bas */
		if (childrenEdges == null)
			return Double.MAX_VALUE;

		double minY = Double.MAX_VALUE;
		for (int i = 0; i < childrenEdges.size(); i++) {
			/*
			 * Recherche de l'extrémité la plus basse de l'arête (la plus haute étant le noeud
			 * courant)
			 */
			Edge currentEdge = childrenEdges.elementAt(i);
			double y1 = currentEdge.getSource().getShape().getY();
			double y2 = currentEdge.getDestination().getShape().getY();
			double y = (y1 > y2) ? y1 : y2;

			/* Recherche de l'extrémité la plus haute parmi toutes les extrémités basses */
			if (y < minY)
				minY = y;
		}

		return minY;
	}

	/**
	 * Permet d'afficher l'étiquette du noeud
	 * @param g2 Le composant Graphics2D dans lequel doit s'afficher l'étiquette
	 */
	public void paintLabels(Graphics2D g2, Rectangle2D r) {
		/* Seuls les noeuds finaux ont une étiquette qu'il est possible de voir */
		if ((label.isVisible() || label.isHighlighted()) && isVisible) {
			label.paintLabel(g2);
		}

		if (isVisible && internalLattice != null
				&& r.intersects(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight()))
			internalLattice.paintLabels(g2, r);
	}

	/**
	 * Permet d'afficher les arêtes du noeud
	 * @param g2 Le composant Graphics2D dans lequel doivent s'afficher les arêtes
	 * @param r the rectangle view (not used anymore)
	 */
	public void paintEdges(Graphics2D g2, Rectangle2D r) {
		/* Affichage des arêtes en direction des enfants */
		if (childrenEdges != null)
			for (int i = 0; i < childrenEdges.size(); i++)
				(childrenEdges.elementAt(i)).paint(g2);
	}

	/**
	 * Permet d'afficher le noeud
	 * @param g2 Le composant Graphics2D dans lequel doit s'afficher le noeud
	 */
	public void paintNode(Graphics2D g2, Rectangle2D r) {
		/* Affichage du noeud */
		if (isVisible && r.intersects(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight())) {
			/*
			 * Les couleurs des différents états sont différentes pour les noeuds finaux et non
			 * finaux
			 */
			/* Coloriage de l'intérieur du noeud */
			ImageIcon icon;
			if (nestedConcept.isFinalConcept()) {
				if (isOutOfFocus) {
					if (internalLattice == null || !internalLattice.isVisible())
						icon = LMImages.getFuzzyClosedIcon(color);
					else
						icon = LMImages.getFuzzyOpenedIcon(color);
				}

				else {
					if (internalLattice == null && parentLattice != null)
						icon = LMImages.getClearClosedIcon(color, parentLattice.isChangeIntensity(), intensity);
					else if (!internalLattice.isVisible())
						icon = LMImages.getClearClosedIcon(color, internalLattice.isChangeIntensity(), intensity);
					else
						icon = LMImages.getClearOpenedIcon(color, internalLattice.isChangeIntensity(), intensity);
				}
			}

			else {
				if (isOutOfFocus)
					icon = LMImages.getFuzzyInactiveIcon();
				else
					icon = LMImages.getClearInactiveIcon();
			}

			Image image = icon.getImage();
			scale = shape.getWidth() / (image.getWidth(null) * 0.75);
			g2.drawImage(image, new AffineTransform(scale, 0, 0, scale, shape.getX() - (58 * scale), shape.getY()
					- (58 * scale)), null);

			if (isSelected()) {
				ImageIcon selectionIcon;
				if (internalLattice == null || !internalLattice.isVisible())
					selectionIcon = LMImages.getClosedSelectionIcon(selectionColor);
				else
					selectionIcon = LMImages.getOpenedSelectionIcon(selectionColor);

				g2.drawImage(selectionIcon.getImage(), new AffineTransform(scale, 0, 0, scale, shape.getX()
						- (58 * scale), shape.getY() - (58 * scale)), null);
			}

			if (isHighlighted()) {
				ImageIcon highlightIcon;
				if (internalLattice == null || !internalLattice.isVisible())
					highlightIcon = LMImages.getClosedHighlightIcon(selectionColor);
				else
					highlightIcon = LMImages.getOpenedHighlightIcon(selectionColor);

				g2.drawImage(highlightIcon.getImage(), new AffineTransform(scale, 0, 0, scale, shape.getX()
						- (58 * scale), shape.getY() - (58 * scale)), null);
			}

			/* Affichage du treillis interne */
			if (nestedConcept.isFinalConcept() && internalLattice != null)
				internalLattice.paint(g2, r);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return nestedConcept.toString();
	}
}