package fca.gui.lattice.element;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import fca.gui.util.constant.LMColors.LatticeColor;

/**
 * Définition d'une arête graphique pour un treillis
 * @author Geneviève Roberge
 * @author Ludovic Thomas
 * @version 2.0
 */
public class Edge extends GraphicalLatticeElement {
	
	/** Arête sélectionnée (finale ou non) */
	private static final Color EDGE_SELECTED = Color.BLACK;
	
	/** Coutour normal d'un noeud non-final */
	private static final Color EDGE_OUT_FOCUS = Color.LIGHT_GRAY;
	
	/**
	 * Le noeud d'où part l'arête inverse="childrenEdges:fca.lattice.graphical.GraphicalConcept"
	 */
	private GraphicalConcept sourceNode;
	
	/**
	 * Le noeud où se termine l'arête inverse="parentEdges:fca.lattice.graphical.GraphicalConcept"
	 */
	private GraphicalConcept destNode;
	
	/**
	 * Constructeur d'une arête graphique
	 * @param s Le ConceptNode d'où part cette arête
	 * @param d Le ConceptNode où arrive cette arête
	 */
	public Edge(GraphicalConcept s, GraphicalConcept d) {
		super();
		sourceNode = s;
		destNode = d;
	}
	
	/**
	 * Permet d'obtenir le noeud d'où part l'arête
	 * @return Le ConceptNode d'où part l'arête
	 */
	public GraphicalConcept getSource() {
		return sourceNode;
	}
	
	/**
	 * Permet d'obtenir le noeud où arrive l'arête
	 * @return Le ConceptNode où arrive l'arête
	 */
	public GraphicalConcept getDestination() {
		return destNode;
	}
	
	/**
	 * Permet d'afficher une arête
	 * @param g2 Le composant Graphics2D qui doit afficher l'arête
	 */
	public void paint(Graphics2D g2) {
		/* Les deux extrimités d'une arête doivent être affichées pour afficher l'arête */
		if (sourceNode.isVisible() && destNode.isVisible()) {
			double sX = sourceNode.getShape().getX() + (sourceNode.getShape().getWidth()) / 2;
			double sY = sourceNode.getShape().getY() + (sourceNode.getShape().getHeight()) / 2;
			double dX = destNode.getShape().getX() + (destNode.getShape().getWidth()) / 2;
			double dY = destNode.getShape().getY() + (destNode.getShape().getHeight()) / 2;
			Line2D edge = new Line2D.Double(sX, sY, dX, dY);
			
			if (sourceNode.isOutOfFocus() || destNode.isOutOfFocus()) {
				g2.setStroke(new BasicStroke(1));
				g2.setPaint(EDGE_OUT_FOCUS);
			}

			else if (sourceNode.getNestedConcept().isFinalConcept() && destNode.getNestedConcept().isFinalConcept()) {
				Color edgeColorDarker = color.getColor().darker();
				if (isSelected || isHighlighted) {
					g2.setPaint(edgeColorDarker);
					// g2.setPaint(GraphicalLattice.EDGE_SELECTED_OUT);
					g2.setStroke(new BasicStroke(2));
				} else {
					g2.setPaint(edgeColorDarker);
					// g2.setPaint(GraphicalLattice.FINAL_OUT);
					g2.setStroke(new BasicStroke(1));
				}
			}

			else {
				if (isSelected || isHighlighted) {
					g2.setPaint(EDGE_SELECTED);
					g2.setStroke(new BasicStroke(2));
				} else {
					g2.setPaint(EDGE_OUT_FOCUS);
					g2.setStroke(new BasicStroke(1));
				}
			}
			g2.draw(edge);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.lattice.graphical.GraphicalLatticeElement#getColorDefault()
	 */
	@Override
	public LatticeColor getColorDefault() {
		if (sourceNode == null)
			return LatticeColor.DEFAULT;
		else
			return sourceNode.getParentLattice().getLatticeColor();
	}
}