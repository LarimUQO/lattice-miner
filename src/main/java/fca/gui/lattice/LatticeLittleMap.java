package fca.gui.lattice;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import fca.gui.lattice.element.Edge;
import fca.gui.lattice.element.GraphicalConcept;
import fca.gui.lattice.element.GraphicalLattice;

/**
 * Fenêtre affichant le treillis en entier pour indiquer ou se trouve la zone visible
 * @author Geneviève Roberge
 * @version 1.0
 */
public class LatticeLittleMap extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2149229368907711626L;

	private LatticePanel baseViewer; //Le panneau chargé d'afficher le treillis imbriqué
	
	private GraphicalLattice rootLattice;
	
	private Rectangle2D visibleArea;
	
	private Point2D lastPos;
	
	private boolean drag;
	
	private int panelSize = 100;
	
	private Vector<Vector<Ellipse2D>> mapNodesByLevel;
	
	private Vector<Vector<Line2D>> mapEdgesByLevel;
	
	private double scale;
	
	private double rootRatio;
	
	/**
	 * Constructeur
	 * @param lp Le LatticePanel qui doit être affiché
	 */
	public LatticeLittleMap(LatticePanel lp) {
		setPreferredSize(new Dimension(panelSize, panelSize));
		setBackground(Color.LIGHT_GRAY);
		setBorder(new LineBorder(Color.LIGHT_GRAY));
		baseViewer = lp;
		rootLattice = baseViewer.getRootLattice();
		
		visibleArea = new Rectangle2D.Double(0, 0, 1, 1);
		lastPos = new Point2D.Double(0, 0);
		drag = false;
		
		buildMap();
		
		addMouseListener(new PanelMouseListener());
		addMouseMotionListener(new PanelMouseMotionListener());
	}
	
	public void setNewLattice(GraphicalLattice lattice) {
		rootLattice = lattice;
		buildMap();
	}
	
	public void refreshMap() {
		buildMap();
	}
	
	private void buildMap() {
		scale = (panelSize) / (rootLattice.getRadius() * 2.0 + rootLattice.getConceptSize());
		visibleArea = baseViewer.getVisibleArea();
		
		rootRatio = scale / rootLattice.getScale();
		mapNodesByLevel = new Vector<Vector<Ellipse2D>>();
		mapEdgesByLevel = new Vector<Vector<Line2D>>();
		
		Vector<GraphicalConcept> rootNodesList = rootLattice.getNodesList();
		for (int i = 0; i < rootNodesList.size(); i++) {
			GraphicalConcept concept = rootNodesList.elementAt(i);
			addEdges(concept, 0, null);
			addNodes(concept, 0, null);
		}
	}
	
	private void addEdges(GraphicalConcept concept, int level, Ellipse2D externalShape) {
		while (mapEdgesByLevel.size() <= level)
			mapEdgesByLevel.add(new Vector<Line2D>());
		
		double parentRadius = concept.getParentLattice().getRadius();
		Point2D parentCenter = concept.getParentLattice().getCenter();
		double parentConceptSize = rootLattice.getConceptSize();
		
		double startX = 0.0;
		double startY = 0.0;
		double parentScale = 1.0;
		/*
		 * Mêmes formules que dans le LatticeViewer pour assurer la même image Prendre les
		 * coordonnées des concepts ne fonctionne pas dû aux temps de calcul
		 */
		if (externalShape != null) {
			parentScale = (externalShape.getWidth() / 2.0) / (parentRadius + 1.5) * 0.8;
			startX = externalShape.getX() + (externalShape.getWidth() / 4.0) - (parentCenter.getX() * parentScale)
					- (parentConceptSize / 2.0 * parentScale) /* + 1.0 */;
			startY = externalShape.getY() + (externalShape.getHeight() / 2.0) - (parentCenter.getY() * parentScale)
					- (parentConceptSize / 2.0 * parentScale) + 1.0 /*- (1.0*parentScale)*/;
		} else {
			parentScale = panelSize / (parentRadius * 2.0 + parentConceptSize);
			startX = 0;
			startY = 0;
		}
		
		Vector<Line2D> mapEdges = mapEdgesByLevel.elementAt(level);
		Vector<Edge> edges = concept.getChildrenEdges();
		for (int i = 0; i < edges.size(); i++) {
			Edge currentEdge = edges.elementAt(i);
			double sx = parentRadius - parentCenter.getX() + currentEdge.getSource().getRelPosition().getX()
					+ parentConceptSize / 2.0;
			double sy = parentRadius - parentCenter.getY() + currentEdge.getSource().getRelPosition().getY()
					+ parentConceptSize / 2.0;
			double dx = parentRadius - parentCenter.getX() + currentEdge.getDestination().getRelPosition().getX()
					+ parentConceptSize / 2.0;
			double dy = parentRadius - parentCenter.getY() + currentEdge.getDestination().getRelPosition().getY()
					+ parentConceptSize / 2.0;
			
			double map_sx = startX + sx * parentScale;
			double map_sy = startY + sy * parentScale;
			double map_dx = startX + dx * parentScale;
			double map_dy = startY + dy * parentScale;
			
			Line2D mapEdge = new Line2D.Double(map_sx, map_sy, map_dx, map_dy);
			mapEdges.add(mapEdge);
		}
	}
	
	private void addNodes(GraphicalConcept concept, int level, Ellipse2D externalShape) {
		while (mapNodesByLevel.size() <= level)
			mapNodesByLevel.add(new Vector<Ellipse2D>());
		
		double parentRadius = concept.getParentLattice().getRadius();
		Point2D parentCenter = concept.getParentLattice().getCenter();
		double parentConceptSize = rootLattice.getConceptSize();
		
		double startX = 0.0;
		double startY = 0.0;
		double parentScale = 1.0;
		/*
		 * Mêmes formules que dans le LatticeViewer pour assurer la même image Prendre les
		 * coordonnées des concepts ne fonctionne pas dû aux temps de calcul
		 */
		if (externalShape != null) {
			parentScale = (externalShape.getWidth() / 2.0) / (parentRadius + 1.5) * 0.8;
			startX = externalShape.getX() + (externalShape.getWidth() / 4.0) - (parentCenter.getX() * parentScale)
					- (parentConceptSize / 2.0 * parentScale) /* + 1.0 */;
			startY = externalShape.getY() + (externalShape.getHeight() / 2.0) - (parentCenter.getY() * parentScale)
					- (parentConceptSize / 2.0 * parentScale) + 1.0 /*- (1.0*parentScale)*/;
		} else {
			parentScale = panelSize / (parentRadius * 2.0 + parentConceptSize);
			startX = 0;
			startY = 0;
		}
		
		Vector<Ellipse2D> mapNodes = mapNodesByLevel.elementAt(level);
		
		double x = (parentRadius - parentCenter.getX() + concept.getRelPosition().getX());
		double y = (parentRadius - parentCenter.getY() + concept.getRelPosition().getY());
		double r = parentConceptSize;
		
		double map_x = startX + x * parentScale;
		double map_y = startY + y * parentScale;
		double map_r = r * parentScale;
		
		Ellipse2D mapNode = new Ellipse2D.Double(map_x, map_y, map_r, map_r);
		mapNodes.add(mapNode);
		
		if (map_r > 1 && concept.getInternalLattice() != null) {
			Vector<GraphicalConcept> internalNodes = concept.getInternalLattice().getNodesList();
			for (int i = 0; i < internalNodes.size(); i++) {
				GraphicalConcept c = internalNodes.elementAt(i);
				addEdges(c, level + 1, mapNode);
				addNodes(c, level + 1, mapNode);
			}
		}
	}
	
	/* ======== AFFICHAGE ======== */
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(1));
		
		/* Affichage de la zone visible */
		Rectangle2D relArea = baseViewer.getVisibleArea();
		if (relArea != null) {
			double x = (relArea.getX()) * scale;
			double y = (relArea.getY()) * scale;
			double width = relArea.getWidth() * scale;
			double height = relArea.getHeight() * scale;
			
			visibleArea.setRect(x, y, width, height);
			g2.setPaint(Color.WHITE);
			g2.fill(visibleArea);
		}
		
		/* Affichage du treillis */
		for (int i = 0; i < mapNodesByLevel.size(); i++) {
			Vector<Ellipse2D> nodes = mapNodesByLevel.elementAt(i);
			Vector<Line2D> edges = mapEdgesByLevel.elementAt(i);
			
			g2.setPaint(Color.BLACK);
			for (int j = 0; j < edges.size(); j++)
				g2.draw(edges.elementAt(j));
			
			g2.setPaint(Color.WHITE);
			for (int j = 0; j < nodes.size(); j++)
				g2.fill(nodes.elementAt(j));
			
			g2.setPaint(Color.BLACK);
			for (int j = 0; j < nodes.size(); j++)
				g2.draw(nodes.elementAt(j));
		}
		
		if (relArea != null) {
			g2.setPaint(Color.RED);
			g2.draw(visibleArea);
		}
	}
	
	private class PanelMouseListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
		}
		
		public void mouseEntered(MouseEvent e) {
		}
		
		public void mouseExited(MouseEvent e) {
		}
		
		public void mousePressed(MouseEvent e) {
			if (visibleArea.contains(e.getPoint())) {
				lastPos.setLocation(e.getX(), e.getY());
				drag = true;
			} else
				drag = false;
		}
		
		public void mouseReleased(MouseEvent e) {
			drag = false;
		}
	}
	
	private class PanelMouseMotionListener implements MouseMotionListener {
		public void mouseMoved(MouseEvent e) {
		}
		
		public void mouseDragged(MouseEvent e) {
			if (drag) {
				/* Calcul du déplacement à effectuer */
				double decX = (e.getX() - lastPos.getX()) / rootRatio;
				double decY = (e.getY() - lastPos.getY()) / rootRatio;
				lastPos.setLocation(e.getX(), e.getY());
				
				/* Déplacement effectif du treillis */
				Point2D rootPosition = rootLattice.getRootPosition();
				rootLattice.setRootPosition(rootPosition.getX() - decX, rootPosition.getY() - decY);
				baseViewer.repaint();
			}
		}
	}
	
}
