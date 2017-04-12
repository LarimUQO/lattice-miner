package fca.gui.lattice.element;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import fca.core.context.binary.BinaryContext;
import fca.core.lattice.ConceptLattice;
import fca.core.lattice.FormalConcept;
import fca.core.util.BasicSet;

/**
 * Définition d'une structure représentant la position relative d'un noeud dans un graphe par
 * rapport au sommet du graphe
 * @author Geneviève Roberge
 * @version 1.0
 */
@SuppressWarnings("unchecked") //$NON-NLS-1$
// Impossible de typer "positionsByLevel"
public class LatticeStructure {

	/* Constantes pour la forme désirée de structure de graphe */

	/** Structure de forme cubique */
	public static final int CUBIC = 1;

	/** Structure avec un niveau par cardinalité d'intention */
	public static final int INTENT = 2;

	/** Structure avec les noeuds placés sur le plus haut niveau possible, en fonction des parents */
	public static final int HIGHEST = 3;

	/** Structure jugée la meilleure selon les bases connues */
	public static final int BEST = 4;

	/** Structure avec position des concepts gérée par les attributs */
	public static final int ADDITIVE = 5;

	/** Structure avec position des concepts dirigée par la position des parents */
	public static final int PARENTS = 6;

	/** Structure permettant de réduire les intersections */
	public static final int INTERSECTIONS = 7;

	/* La structure et sa relation */

	private ConceptLattice lattice; //Contient le treillis pour lequel la structure est créée

	private BinaryContext context; //Contient la relation pour laquelle la structure est créée

	/* Variables utilitaires pour la construction de la structure */

	private Hashtable levelByNode; //Contient le niveau de chaque noeud visité avec son id comme clé

	private Vector nodesByLevel; //Contient la liste des vecteurs de noeuds pour chaque niveau

	private Vector positionsByLevel;//Contient la liste des vecteurs de positions pour chaque niveau

	private Vector endNodes; //Contient les noeuds aux extémités de chaque niveau

	private int type; //Contient le type de structure désirée pour le treillis

	/* Variables résultant d'une structure complétée */
	private Vector<ConceptPosition> conceptPositions; //Position de chacun des noeuds du graphe (Point2D)

	private Point2D center; //Position du centre du graphe

	private double radius; //Longueur du rayon du graphe

	private int bestCrossingCount;

	private Vector bestOrderByLevel; //Contient la liste des vecteurs de noeuds pour chaque niveau

	private int solutionCount;
	private static int MAX_SOLUTIONS = 500;

	/**
	 * Constructeur
	 * @param cl Le ConceptLattice pour lequel la structure sera construite
	 * @param nc Le NestedContext qui correspond à la structure
	 * @param t Le type de structure désirée (CUBIC, INTENT, HIGHEST)
	 */
	public LatticeStructure(ConceptLattice cl, BinaryContext nc, int t) {
		this.conceptPositions = new Vector<ConceptPosition>();
		this.center = new Point2D.Double(0, 0);
		this.radius = 0.0;
		this.levelByNode = null;
		this.lattice = cl;
		this.context = nc;
		this.type = t;

		calcLatticeStructure();
	}

	/**
	 * Constructeur
	 * @param positions Le Vector contenant les ConceptPosition déjà calculés pour chacun des noeuds
	 */
	public LatticeStructure(Vector<ConceptPosition> positions) {
		/* Construction du vecteur contenant les positions des noeuds de la structure */
		conceptPositions = new Vector<ConceptPosition>();
		for (int i = 0; i < positions.size(); i++)
			conceptPositions.add((ConceptPosition) (positions.elementAt(i)).clone());

		/* Classement des positions par niveau, selon la position en y */
		positionsByLevel = new Vector();
		for (int i = 0; i < conceptPositions.size(); i++) {
			ConceptPosition currentPosition = conceptPositions.elementAt(i);
			int level = (int) (Math.round(currentPosition.getRelY()) / 2);
			while (positionsByLevel.size() < level + 1)
				positionsByLevel.add(new Vector());

			Vector levelPositions = (Vector) positionsByLevel.elementAt(level);
			levelPositions.add(currentPosition);
		}

		/* Recherche des positions aux extrémités de chaque niveau */
		endNodes = new Vector();
		for (int i = 0; i < positionsByLevel.size(); i++) {
			Vector currentLevel = (Vector) positionsByLevel.elementAt(i);
			double minX = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			ConceptPosition minPosition = null;
			ConceptPosition maxPosition = null;

			/* Recherche des positions aux extrémités pour un niveau */
			for (int j = 0; j < currentLevel.size(); j++) {
				ConceptPosition currentPosition = (ConceptPosition) currentLevel.elementAt(j);
				/* Position la plus à gauche */
				if (currentPosition.getRelX() < minX) {
					minX = currentPosition.getRelX();
					minPosition = currentPosition;
				}

				/* Position la plus à droite */
				if (currentPosition.getRelX() > maxX) {
					maxX = currentPosition.getRelX();
					maxPosition = currentPosition;
				}
			}

			/* Ajout des extrémités non nulles dans le vecteur des extrémités */
			if (minPosition != null)
				endNodes.add(minPosition);

			if (minX != maxX && maxPosition != null)
				endNodes.add(maxPosition);
		}

		/* Recherche du point central du rectangle minimal contenant le treillis */
		double maxX = 0;
		double minX = 0;
		for (int i = 0; i < endNodes.size(); i++) {
			ConceptPosition currentPosition = (ConceptPosition) endNodes.elementAt(i);
			double currentX = currentPosition.getRelX();
			if (currentX < minX)
				minX = currentX;
			if (currentX > maxX)
				maxX = currentX;
		}
		double centerX = (minX + maxX) / 2;
		center = new Point2D.Double(centerX, positionsByLevel.size() - 1);

		/*
		 * Recherche du rayon du cercle contenant le treillis (distance avec le point le plus
		 * éloigné du centre)
		 */
		radius = 0;
		for (int i = 0; i < endNodes.size(); i++) {
			ConceptPosition currentPosition = (ConceptPosition) endNodes.elementAt(i);
			double distX = Math.abs((center.getX() - currentPosition.getRelX()));
			double distY = Math.abs((center.getY() - currentPosition.getRelY()));
			double currentRadius = Math.sqrt((distX * distX) + (distY * distY));
			if (currentRadius > radius)
				radius = currentRadius;
		}
	}

	/**
	 * Permet d'obtenir la liste de positions de la structure
	 * @return Le Vector contenant la liste des positions
	 */
	public Vector<ConceptPosition> getConceptPositions() {
		return conceptPositions;
	}

	/**
	 * Permet de donner la position d'un noeud en particulier
	 * @param intent Le BasicSet contenant l'intetion du noeud recherché
	 * @return Le ConceptPosition contenant la position du noeud
	 */
	public ConceptPosition getConceptPosition(BasicSet intent) {
		for (int i = 0; i < conceptPositions.size(); i++) {
			ConceptPosition currentPosition = conceptPositions.elementAt(i);
			if (currentPosition.getIntent().equals(intent))
				return currentPosition;
		}
		return null;
	}

	/**
	 * Permet de donner le point central à la structure
	 * @return Le Point2D contenant le point central
	 */
	public Point2D getCenter() {
		return center;
	}

	/**
	 * Permet de donner le rayon du graphe
	 * @return Le double contenant le rayon
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Permet de déterminer si la structure cubique est intéressante pour le treillis actuel
	 * @return Le boolean indiquant si la structure cubique est intéressante pour le treillis actuel
	 */
	private boolean isCubicGoodChoice() {
		if (!isCubicAvailable() || nodesByLevel.size() < 4)
			return false;

		/*
		 * Le nombre de noeuds manquants doit être inférieur au tiers des noeuds normalement
		 * présents dans une structure cubique
		 */
		int completeSize = (int) Math.pow(2, ((Vector) nodesByLevel.elementAt(1)).size());
		int totalSize = 2;
		//int pascalRowIdx = nodesByLevel.size() - 1;
		for (int i = 1; i < nodesByLevel.size() - 1; i++) {
			/* Lignes commentées : Assurait que chaque ligne contient le 2/3 des noeuds attendus */
			//int pascalNum = factorial(pascalRowIdx) / (factorial(i)*factorial(pascalRowIdx-i));
			//if( ((Vector)nodesByLevel.elementAt(i)).size() >= Math.ceil((double)pascalNum*2.0/3.0) )
			//  return false;
			totalSize += ((Vector) nodesByLevel.elementAt(i)).size();
		}

		if (totalSize < Math.ceil(completeSize * 2.0 / 3.0))
			return false;

		return true;
	}

	/**
	 * Permet de déterminer si la structure cubique est disponible pour le treillis actuel
	 * @return Le boolean indiquant si la structure cubique est disponible pour le treillis actuel
	 */
	private boolean isCubicAvailable() {
		if (nodesByLevel == null || nodesByLevel.size() < 2)
			return false;

		/* Recherche des attributs se trouvant dans les enfants de la source du treillis */
		BasicSet firstLevelAttributes = new BasicSet();
		Vector firstLevelNodes = (Vector) nodesByLevel.elementAt(1);
		for (int i = 0; i < firstLevelNodes.size(); i++) {
			BasicSet currentIntent = (BasicSet) firstLevelNodes.elementAt(i);
			firstLevelAttributes.addAll(currentIntent);
		}

		/* Recherche de tous les attributs se trouvant dans le treillis */
		BasicSet sinkIntent = (BasicSet) ((Vector) nodesByLevel.lastElement()).elementAt(0);

		/*
		 * Si tous les attributs se retouvent dans le niveau sous la source, la structure cubique
		 * est possible
		 */
		if (sinkIntent.equals(firstLevelAttributes))
			return true;

		return false;
	}

	private boolean isAdditiveAvailable() {
		if (nodesByLevel == null || nodesByLevel.size() < 2)
			return false;

		Vector attributes = context.getAttributes();
		/*
		 * Positionnement des niveaux internes : si 2 concepts ont la même position, la structure
		 * additive n'est pas possible
		 */
		for (int i = 1; i < nodesByLevel.size() - 1; i++) {
			Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(i);
			Vector currentLevelPositions = new Vector();
			for (int j = 0; j < currentLevelNodes.size(); j++) {
				BasicSet currentIntent = (BasicSet) currentLevelNodes.elementAt(j);

				/* Les noeuds sont placés à la position attribuée à leur intention */
				double posX = findAdditiveIntentPositionX(attributes, currentIntent);
				for (int k = 0; k < currentLevelPositions.size(); k++) {
					double currPos = ((Double) currentLevelPositions.elementAt(k)).doubleValue();
					if (posX == currPos)
						return false;
				}
				currentLevelPositions.add(new Double(posX));
			}
		}

		return true;
	}

	/**
	 * Permet de déterminer si la structure cubique est disponible pour ce treillis
	 * @param cl Le ConceptLattice pour lequel on veut connaître la disponibilité de la structure
	 * @return Le boolean indiquant si la structure cubique est disponible pour le treillis donné
	 */
	public static boolean isCubicAvailable(ConceptLattice cl) {
		ConceptLattice lattice = cl;

		/* Assignation d'un niveau à chaque concept */
		Hashtable levelByNode = new Hashtable(lattice.size());
		FormalConcept topConcept = lattice.getTopConcept();
		LatticeStructure.setHighestLevels(topConcept, 0, levelByNode);

		/*
		 * Création d'un vecteur de vecteurs contenant la liste des noeuds pour chaque niveau et
		 * d'un vecteur de vecteurs devant contenir la liste des positions pour chaque niveau
		 */
		Vector nodesByLevel = new Vector();
		Enumeration nodeIntents = levelByNode.keys();
		while (nodeIntents.hasMoreElements()) {
			BasicSet currentIntent = (BasicSet) nodeIntents.nextElement();
			Integer currentLevel = (Integer) levelByNode.get(currentIntent);
			while (nodesByLevel.size() <= currentLevel.intValue()) {
				nodesByLevel.add(new Vector());
			}
			Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(currentLevel.intValue());
			currentLevelNodes.add(currentIntent);
		}

		/* Suppression des niveaux vides */
		for (int i = nodesByLevel.size() - 1; i >= 0; i--) {
			if (((Vector) nodesByLevel.elementAt(i)).size() == 0) {
				nodesByLevel.removeElementAt(i);
			}
		}

		/* Application de l'algorithme de positionnement approprié au type de treillis */
		if (nodesByLevel.size() < 4) {
			/* Une structure avec moins de 4 niveaux ne peut générer une structure cubique */
			return false;
		} else {
			/* Recherche des attributs se trouvant dans les enfants de la source du treillis */
			BasicSet firstLevelAttributes = new BasicSet();
			Vector firstLevelNodes = (Vector) nodesByLevel.elementAt(1);
			for (int i = 0; i < firstLevelNodes.size(); i++) {
				BasicSet currentIntent = (BasicSet) firstLevelNodes.elementAt(i);
				firstLevelAttributes.addAll(currentIntent);
			}

			/* Recherche de tous les attributs se trouvant dans le treillis */
			BasicSet sinkIntent = (BasicSet) ((Vector) nodesByLevel.lastElement()).elementAt(0);

			/*
			 * La structure cubique doit nécéssairement retrouver tous les attributs du treillis
			 * dans les enfants de la source
			 */
			if (sinkIntent.equals(firstLevelAttributes)) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Permet de déterminer si la structure dirigée par la position des parents est disponible pour
	 * ce treillis
	 * @param cl Le ConceptLattice pour lequel on veut connaître la disponibilité de la structure
	 * @return Le boolean indiquant si la structure cubique est disponible pour le treillis donné
	 */
	public static boolean isParentDrivenAvailable(ConceptLattice cl) {
		//return true;
		ConceptLattice lattice = cl;

		/* Assignation d'un niveau à chaque concept */
		Hashtable levelByNode = new Hashtable(lattice.size());
		FormalConcept topConcept = lattice.getTopConcept();
		LatticeStructure.setHighestLevels(topConcept, 0, levelByNode);

		/*
		 * Création d'un vecteur de vecteurs contenant la liste des noeuds pour chaque niveau et
		 * d'un vecteur de vecteurs devant contenir la liste des positions pour chaque niveau
		 */
		Vector nodesByLevel = new Vector();
		Enumeration nodeIntents = levelByNode.keys();
		while (nodeIntents.hasMoreElements()) {
			BasicSet currentIntent = (BasicSet) nodeIntents.nextElement();
			Integer currentLevel = (Integer) levelByNode.get(currentIntent);
			while (nodesByLevel.size() <= currentLevel.intValue()) {
				nodesByLevel.add(new Vector());
			}
			Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(currentLevel.intValue());
			currentLevelNodes.add(currentIntent);
		}

		/* Suppression des niveaux vides */
		for (int i = nodesByLevel.size() - 1; i >= 0; i--) {
			if (((Vector) nodesByLevel.elementAt(i)).size() == 0) {
				nodesByLevel.removeElementAt(i);
			}
		}

		if (nodesByLevel.size() < 2)
			return false;

		boolean available = (((Vector) nodesByLevel.elementAt(1)).size() <= 7);
		return available;
	}

	/**
	 * Permet de déterminer si la structure réduisant les intersections est disponible pour ce
	 * treillis
	 * @param cl Le ConceptLattice pour lequel on veut connaître la disponibilité de la structure
	 * @return Le boolean indiquant si la structure cubique est disponible pour le treillis donné
	 */
	public static boolean isIntersectionAvailable(ConceptLattice cl) {
		ConceptLattice lattice = cl;

		/* Assignation d'un niveau à chaque concept */
		Hashtable levelByNode = new Hashtable(lattice.size());
		FormalConcept topConcept = lattice.getTopConcept();
		LatticeStructure.setHighestLevels(topConcept, 0, levelByNode);

		/*
		 * Création d'un vecteur de vecteurs contenant la liste des noeuds pour chaque niveau et
		 * d'un vecteur de vecteurs devant contenir la liste des positions pour chaque niveau
		 */
		Vector nodesByLevel = new Vector();
		Enumeration nodeIntents = levelByNode.keys();
		while (nodeIntents.hasMoreElements()) {
			BasicSet currentIntent = (BasicSet) nodeIntents.nextElement();
			Integer currentLevel = (Integer) levelByNode.get(currentIntent);
			while (nodesByLevel.size() <= currentLevel.intValue()) {
				nodesByLevel.add(new Vector());
			}
			Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(currentLevel.intValue());
			currentLevelNodes.add(currentIntent);
		}

		/* Suppression des niveaux vides */
		for (int i = nodesByLevel.size() - 1; i >= 0; i--) {
			if (((Vector) nodesByLevel.elementAt(i)).size() == 0) {
				nodesByLevel.removeElementAt(i);
			}
		}

		boolean doMinCrossing = true;
		for (int i = 0; i < nodesByLevel.size(); i++) {
			if (((Vector) nodesByLevel.elementAt(i)).size() > 7) {
				doMinCrossing = false;
				break;
			}
		}

		return doMinCrossing;
	}

	/**
	 * Permet de déterminer si la structure dirigée par la position des parents est disponible pour
	 * ce treillis
	 * @return Le boolean indiquant si la structure cubique est disponible pour le treillis donné
	 */
	public boolean isParentDrivenAvailable() {
		return true;
		//if(nodesByLevel.size() < 2)
		//  return false;

		//boolean available = ( ((Vector)nodesByLevel.elementAt(1)).size() <= 7 );
		//return available;
	}

	/**
	 * Permet de déterminer si la structure réduisant les intersections est disponible pour ce
	 * treillis
	 * @return Le boolean indiquant si la structure cubique est disponible pour le treillis donné
	 */
	public boolean isIntersectionAvailable() {
		boolean doMinCrossing = true;
		for (int i = 0; i < nodesByLevel.size(); i++) {
			if (((Vector) nodesByLevel.elementAt(i)).size() > 7) {
				doMinCrossing = false;
				break;
			}
		}

		return doMinCrossing;
	}

	/**
	 * Permet de déterminer si la structure additive est disponible pour ce treillis
	 * @param cl Le ConceptLattice pour lequel on veut connaître la disponibilité de la structure
	 * @return Le boolean indiquant si la structure cubique est disponible pour le treillis donné
	 */
	public static boolean isAdditiveAvailable(ConceptLattice cl) {
		ConceptLattice lattice = cl;

		/* Assignation d'un niveau à chaque concept */
		Hashtable levelByNode = new Hashtable(lattice.size());
		FormalConcept topConcept = lattice.getTopConcept();
		LatticeStructure.setHighestLevels(topConcept, 0, levelByNode);

		/*
		 * Création d'un vecteur de vecteurs contenant la liste des noeuds pour chaque niveau et
		 * d'un vecteur de vecteurs devant contenir la liste des positions pour chaque niveau
		 */
		Vector nodesByLevel = new Vector();
		Enumeration nodeIntents = levelByNode.keys();
		while (nodeIntents.hasMoreElements()) {
			BasicSet currentIntent = (BasicSet) nodeIntents.nextElement();
			Integer currentLevel = (Integer) levelByNode.get(currentIntent);
			while (nodesByLevel.size() <= currentLevel.intValue()) {
				nodesByLevel.add(new Vector());
			}
			Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(currentLevel.intValue());
			currentLevelNodes.add(currentIntent);
		}

		/* Suppression des niveaux vides */
		for (int i = nodesByLevel.size() - 1; i >= 0; i--) {
			if (((Vector) nodesByLevel.elementAt(i)).size() == 0) {
				nodesByLevel.removeElementAt(i);
			}
		}

		/* Application de l'algorithme de positionnement approprié au type de treillis */
		if (nodesByLevel.size() == 3) {
			/*
			 * Une structure à 3 niveaux implique que les noeuds sous la source n'ont pas d'autre
			 * enfant que le sink, donc elle ne peut générer une structure cubique
			 */
			return false;
		} else {
			/* Recherche des attributs se trouvant dans les enfants de la source du treillis */
			Vector attributes = lattice.getContext().getAttributes();

			/*
			 * Positionnement des niveaux internes : si 2 concepts ont la même position, la
			 * structure additive n'est pas possible
			 */
			for (int i = 1; i < nodesByLevel.size() - 1; i++) {
				Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(i);
				Vector currentLevelPositions = new Vector();
				for (int j = 0; j < currentLevelNodes.size(); j++) {
					BasicSet currentIntent = (BasicSet) currentLevelNodes.elementAt(j);

					/* Les noeuds sont placés à la position attribuée à leur intention */
					double posX = findAdditiveIntentPositionX(attributes, currentIntent);
					for (int k = 0; k < currentLevelPositions.size(); k++) {
						double currPos = ((Double) currentLevelPositions.elementAt(k)).doubleValue();
						if (posX == currPos)
							return false;
					}
					currentLevelPositions.add(new Double(posX));
				}
			}

			return true;
		}
	}

	/**
	 * Permet de déterminer si la structure triée par cardinalité des intentions est disponible pour
	 * ce treillis
	 * @param cl Le ConceptLattice pour lequel on veut connaître la disponibilité de la structure
	 * @return Le boolean indiquant si la structure triée par cardinalité des niveaux est disponible
	 *         pour le treillis donné
	 */
	public static boolean isIntentAvailable(ConceptLattice cl) {
		/* Cette structure n'a aucune contrainte imposée jusqu'à maintenant */
		return true;
	}

	/**
	 * Permet de déterminer si la structure du plus haut niveau est disponible pour ce treillis
	 * @param cl Le ConceptLattice pour lequel on veut connaître la disponibilité de la structure
	 * @return Le boolean indiquant si la structure du plus haut niveau est disponible pour le
	 *         treillis donné
	 */
	public static boolean isHighestAvailable(ConceptLattice cl) {
		/* Cette structure n'a aucune contrainte imposée jusqu'à maintenant */
		return true;
	}

	/**
	 * Permet d'assigner un niveau à chaque noeud du treillis en se basant sur le niveau des
	 * parents. (Méthode statique)
	 * @param node Le FormalConcept contenant le noeud auquel une position doit être assignée
	 * @param level Le int contenant le niveau suggéré pour le noeud
	 * @param levelByNode La Hashtable contenant le niveau assigné à chaque noeud visité
	 */
	public static void setHighestLevels(FormalConcept node, int level, Hashtable levelByNode) {
		/* Le noeud est visité pour la première fois : on le place au niveau suggéré */
		if (!levelByNode.containsKey(node.getIntent())) {
			levelByNode.put(node.getIntent(), new Integer(level));
			Vector children = node.getChildren();

			/* Les enfants du noeud doivent être positionnés en fonction du niveau de leur parent */
			for (int i = 0; i < children.size(); i++)
				LatticeStructure.setHighestLevels((FormalConcept) children.elementAt(i), level + 1, levelByNode);
		}
		/*
		 * Le noeud a déjà été visité : on le place au le plus bas entre son niveau actuel et le
		 * niveau suggéré
		 */
		else {
			Integer currentLevel = (Integer) levelByNode.get(node.getIntent());
			if (level > currentLevel.intValue()) {
				levelByNode.put(node.getIntent(), new Integer(level));

				/*
				 * Les enfants du noeud doivent être positionnés en fonction du niveau de leur
				 * parent
				 */
				Vector children = node.getChildren();
				for (int i = 0; i < children.size(); i++)
					LatticeStructure.setHighestLevels((FormalConcept) children.elementAt(i), level + 1, levelByNode);
			}
		}
	}

	/**
	 * Permet d'assigner un niveau à chaque noeud du treillis en se basant sur le niveau des
	 * parents. (Méthode non-statique)
	 * @param node Le ConceptNodeImp contenant le noeud auquel une position doit être assignée
	 * @param level Le int contenant le niveau suggéré pour le noeud
	 */
	private void setHighestLevels(FormalConcept node, int level) {
		/* Le noeud est visité pour la première fois : on le place au niveau suggéré */
		if (!levelByNode.containsKey(node.getIntent())) {
			levelByNode.put(node.getIntent(), new Integer(level));

			/* Les enfants du noeud doivent être positionnés en fonction du niveau de leur parent */
			Vector children = node.getChildren();
			for (int i = 0; i < children.size(); i++)
				setHighestLevels((FormalConcept) children.elementAt(i), level + 1);
		}
		/*
		 * Le noeud a déjà été visité : on le place au le plus bas entre son niveau actuel et le
		 * niveau suggéré
		 */
		else {
			Integer currentLevel = (Integer) levelByNode.get(node.getIntent());
			if (level > currentLevel.intValue()) {
				levelByNode.put(node.getIntent(), new Integer(level));

				/*
				 * Les enfants du noeud doivent être positionnés en fonction du niveau de leur
				 * parent
				 */
				Vector children = node.getChildren();
				for (int i = 0; i < children.size(); i++)
					setHighestLevels((FormalConcept) children.elementAt(i), level + 1);
			}
		}
	}

	/**
	 * Permet d'assigner un niveau à chaque noeud du treillis en se basant sur la cardinalité de
	 * leur intention
	 * @param node Le ConceptNodeImp contenant le noeud auquel une position doit être assignée
	 */
	private void setIntentLevels(FormalConcept node) {
		/* Le noeud est visité pour la première fois : on l'assigne à un niveau */
		if (!levelByNode.containsKey(node.getIntent())) {
			int level = node.getIntent().size();
			levelByNode.put(node.getIntent(), new Integer(level));

			/* Les enfants du noeud sont assignés au niveau de leur cardinalité */
			Vector children = node.getChildren();
			for (int i = 0; i < children.size(); i++)
				setIntentLevels((FormalConcept) children.elementAt(i));
		}
	}

	/**
	 * Permet d'initialiser les variables qui contiendront la structure du treilis
	 */
	private void resetStructure() {
		endNodes = new Vector();
		conceptPositions = new Vector();

		for (int i = 0; i < positionsByLevel.size(); i++)
			positionsByLevel.setElementAt(new Vector(), i);
	}

	/**
	 * Permet de calculer les coordonnées des concepts du treillis ainsi que le centre du graphe et
	 * son rayon
	 */
	private void calcLatticeStructure() {
		//try{
		/* Assignation d'un niveau à chaque concept, selon le type de structure désirée */
		levelByNode = new Hashtable(lattice.size());
		FormalConcept topConcept = lattice.getTopConcept();
		if (type == INTENT)
			setIntentLevels(topConcept);
		else
			setHighestLevels(topConcept, 0);

		/*
		 * Création d'un vecteur de vecteurs contenant la liste des noeuds pour chaque niveau et
		 * d'un vecteur de vecteurs devant contenir la liste des positions pour chaque niveau
		 */
		nodesByLevel = new Vector();
		positionsByLevel = new Vector();
		Enumeration nodeIntents = levelByNode.keys();
		while (nodeIntents.hasMoreElements()) {
			BasicSet currentIntent = (BasicSet) nodeIntents.nextElement();
			Integer currentLevel = (Integer) levelByNode.get(currentIntent);
			while (nodesByLevel.size() <= currentLevel.intValue()) {
				nodesByLevel.add(new Vector());
				positionsByLevel.add(new Vector());
			}
			Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(currentLevel.intValue());
			currentLevelNodes.add(currentIntent);
		}

		/* Suppression des niveaux vides */
		for (int i = nodesByLevel.size() - 1; i >= 0; i--) {
			if (((Vector) nodesByLevel.elementAt(i)).size() == 0) {
				nodesByLevel.removeElementAt(i);
				positionsByLevel.removeElementAt(i);
			}
		}

		/*
		 * Application de l'algorithme de positionnement approprié au type de treillis et au type de
		 * structure désirée
		 */
		if (type == HIGHEST || type == INTENT) {
			/*
			 * Les noeuds seront positionnés un à la suite de l'autre (dans leur ordre de création
			 * dans le treillis, et selon le niveau où ils ont préalablement été placés dans cette
			 * fonction
			 */
			//calcSimpleStructure();
			boolean calcOthers = true;
			if (isIntersectionAvailable()) {
				int oldMax = MAX_SOLUTIONS;
				MAX_SOLUTIONS = 100;
				if (calcTwoLayersMinCrossing() == 0)
					calcOthers = false;
				MAX_SOLUTIONS = oldMax;

				if (calcOthers)
					calcSimpleStructure();
			}

			else
				calcSimpleStructure();
		} else if (type == CUBIC && isCubicAvailable()) {
			calcCubicStructure();
		} else if (type == ADDITIVE && isAdditiveAvailable()) {
			calcAdditiveStructure();
		} else if (type == PARENTS && isParentDrivenAvailable()) {
			calcParentDrivenStructure();
		} else if (type == INTERSECTIONS && isIntersectionAvailable()) {
			calcTwoLayersMinCrossing();
		} else if (type == BEST) {
			/* Dans le cas où il y a 3 niveaux */
			if (nodesByLevel.size() == 3)
				calc3LevelsStructure();
			/* Dans le cas où la structure cubique est intéressante */
			else if (isCubicGoodChoice())
				calcCubicStructure();
			/* Dans les autres cas où il y a 4 niveaux */
			else if (nodesByLevel.size() == 4)
				calc4LevelsStructure();
			/* Dans les autres cas où il y a plus que 4 niveaux */
			else {
				boolean calcOthers = true;
				if (isIntersectionAvailable()) {
					int oldMax = MAX_SOLUTIONS;
					MAX_SOLUTIONS = 100;
					if (calcTwoLayersMinCrossing() == 0)
						calcOthers = false;
					MAX_SOLUTIONS = oldMax;
				}

				if (calcOthers && isParentDrivenAvailable())
					calcParentDrivenStructure();
				else if (calcOthers)
					calcSimpleStructure();
			}
		} else {
			calcSimpleStructure();
		}

		/* Recherche du point central dans le rectangle minimal qui contient le treillis */
		double maxX = Double.MIN_VALUE;
		double minX = Double.MAX_VALUE;
		for (int i = 0; i < endNodes.size(); i++) {
			ConceptPosition currentPosition = (ConceptPosition) endNodes.elementAt(i);
			double currentX = currentPosition.getRelX();
			if (currentX < minX)
				minX = currentX;
			if (currentX > maxX)
				maxX = currentX;
		}
		double centerX = (minX + maxX) / 2.0;

		double minY = ((ConceptPosition) ((Vector) positionsByLevel.elementAt(0)).elementAt(0)).getRelY();
		double maxY = ((ConceptPosition) ((Vector) positionsByLevel.elementAt(positionsByLevel.size() - 1)).elementAt(0)).getRelY();
		double centerY = (minY + maxY) / 2.0;

		center = new Point2D.Double(centerX, centerY);

		/*
		 * Recherche du rayon du cercle contenant le treillis, donc de la distance avec le noeud le
		 * plus éloigné du centre
		 */
		radius = 0;
		for (int i = 0; i < endNodes.size(); i++) {
			ConceptPosition currentPosition = (ConceptPosition) endNodes.elementAt(i);
			double distX = Math.abs((center.getX() - currentPosition.getRelX()));
			double distY = Math.abs((center.getY() - currentPosition.getRelY()));
			double currentRadius = Math.sqrt((distX * distX) + (distY * distY));
			if (currentRadius > radius)
				radius = currentRadius;
		}
	}

	/**
	 * Permet de calculer les coordonnées des concepts du treillis en les positionnant selon l'ordre
	 * spécifié
	 */
	private void calcPositionsStructure(Vector nodePositions) {
		if (nodePositions != null) {
			endNodes = new Vector();
			resetStructure();

			/* Positionnement de la source */
			ConceptPosition sourcePosition = (ConceptPosition) ((Vector) nodePositions.elementAt(0)).elementAt(0);
			conceptPositions.add(sourcePosition);
			((Vector) positionsByLevel.elementAt(0)).add(sourcePosition);
			endNodes.add(sourcePosition);

			/* Positionnement du sink */
			ConceptPosition sinkPosition = (ConceptPosition) ((Vector) nodePositions.elementAt(nodePositions.size() - 1)).elementAt(0);
			conceptPositions.add(sinkPosition);
			((Vector) positionsByLevel.elementAt(positionsByLevel.size() - 1)).add(sinkPosition);
			endNodes.add(sinkPosition);

			/* Création du ConceptPosition pour chaque autre noeud */
			for (int i = 1; i < nodePositions.size() - 1; i++) {
				Vector currentNodePositions = (Vector) nodePositions.elementAt(i);
				Vector currentLevelPositions = (Vector) positionsByLevel.elementAt(i);
				Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(i);

				double minX = Integer.MAX_VALUE;
				double maxX = Integer.MIN_VALUE;
				int minIdx = -1;
				int maxIdx = -1;

				for (int j = 0; j < currentNodePositions.size(); j++) {
					ConceptPosition position = (ConceptPosition) currentNodePositions.elementAt(j);
					conceptPositions.add(position);
					currentLevelPositions.add(position);
					currentLevelNodes.setElementAt(position.getIntent(), j);

					if (position.getRelX() < minX) {
						minX = position.getRelX();
						minIdx = j;
					}
					if (position.getRelX() > maxX) {
						maxX = position.getRelX();
						maxIdx = j;
					}
				}

				/* Ajout des noeuds aux extrémités dans la liste des noeuds extrèmes */
				if (currentLevelPositions.size() > 0) {
					endNodes.add(currentLevelPositions.elementAt(minIdx));
					endNodes.add(currentLevelPositions.elementAt(maxIdx));
				}
			}
		}
	}

	/**
	 * Permet de calculer les coordonnées des concepts du treillis en les positionnant selon l'ordre
	 * spécifié
	 */
	private void calcOrderStructure(Vector nodesOrder) {
		if (nodesOrder != null) {
			endNodes = new Vector();
			resetStructure();

			int yFactor = 1;
			for (int i = 0; i < nodesByLevel.size(); i++) {
				Vector level = (Vector) nodesByLevel.elementAt(i);
				if (level.size() > 20 && yFactor < 3)
					yFactor = 3;
				else if (level.size() > 12 && yFactor < 2)
					yFactor = 2;
			}

			/* Positionnement de la source */
			BasicSet sourceIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(0)).elementAt(0);
			ConceptPosition sourcePosition = new ConceptPosition(sourceIntent, 0, 0);
			conceptPositions.add(sourcePosition);
			((Vector) positionsByLevel.elementAt(0)).add(sourcePosition);
			endNodes.add(sourcePosition);

			/* Positionnement du sink */
			BasicSet sinkIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(nodesByLevel.size() - 1)).elementAt(0);
			ConceptPosition sinkPosition = new ConceptPosition(sinkIntent, 0, 3 * (nodesByLevel.size() - 1) * yFactor);
			conceptPositions.add(sinkPosition);
			((Vector) positionsByLevel.elementAt(positionsByLevel.size() - 1)).add(sinkPosition);
			endNodes.add(sinkPosition);

			/* Création du ConceptPosition pour chaque autre noeud */
			for (int i = 1; i < nodesOrder.size() - 1; i++) {
				Vector currentLevelNodes = (Vector) nodesOrder.elementAt(i);
				Vector currentLevelPositions = (Vector) positionsByLevel.elementAt(i);
				for (int j = 0; j < currentLevelNodes.size(); j++) {
					BasicSet currentIntent = (BasicSet) currentLevelNodes.elementAt(j);
					ConceptPosition position = new ConceptPosition(currentIntent, j * 4 - 2
							* (currentLevelNodes.size() - 1), i * 3 * yFactor);
					conceptPositions.add(position);
					currentLevelPositions.add(position);
				}

				/* Ajout des noeuds aux extrémités dans la liste des noeuds extrèmes */
				if (currentLevelPositions.size() > 0) {
					endNodes.add(currentLevelPositions.firstElement());
					endNodes.add(currentLevelPositions.lastElement());
				}
			}
		}
	}

	/**
	 * Permet de calculer les coordonnées des concepts du treillis en les positionnant selon les
	 * heuristiques de base retenues
	 */
	private void calcSimpleStructure() {

		resetStructure();
		if (nodesByLevel != null) {
			endNodes = new Vector();

			int yFactor = 1;
			for (int i = 0; i < nodesByLevel.size(); i++) {
				Vector level = (Vector) nodesByLevel.elementAt(i);
				if (level.size() > 20 && yFactor < 3)
					yFactor = 3;
				else if (level.size() > 12 && yFactor < 2)
					yFactor = 2;
			}

			/* Positionnement de la source */
			BasicSet sourceIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(0)).elementAt(0);
			ConceptPosition sourcePosition = new ConceptPosition(sourceIntent, 0, 0);
			conceptPositions.add(sourcePosition);
			((Vector) positionsByLevel.elementAt(0)).add(sourcePosition);
			endNodes.add(sourcePosition);

			/* Si le treillis a un seul concept, il n'y a plus de positions à calculer */
			if (nodesByLevel.size() < 2)
				return;

			/* Positionnement du sink */
			BasicSet sinkIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(nodesByLevel.size() - 1)).elementAt(0);
			ConceptPosition sinkPosition = new ConceptPosition(sinkIntent, 0, 3 * (nodesByLevel.size() - 1) * yFactor);
			conceptPositions.add(sinkPosition);
			((Vector) positionsByLevel.elementAt(positionsByLevel.size() - 1)).add(sinkPosition);
			endNodes.add(sinkPosition);

			/*
			 * Les noeuds directements liés à la source et au sink sont retirés pour être placés à
			 * droite
			 */
			Vector directNodes = new Vector();
			Vector parentNodes = new Vector();
			if (nodesByLevel.size() > 1) {
				Vector secondLevelNodes = (Vector) nodesByLevel.elementAt(1);
				if (secondLevelNodes != null) {
					for (int i = 0; i < secondLevelNodes.size(); i++) {
						BasicSet currentIntent = (BasicSet) secondLevelNodes.elementAt(i);
						FormalConcept currentConcept = lattice.getConceptWithIntent(currentIntent);

						/* Un enfant sans enfant => directement lié au sink */
						if (currentConcept.getChildren().size() == 1
								&& (currentConcept.getChildren().elementAt(0)).getChildren().size() == 0)
							directNodes.add(currentIntent);
						else
							parentNodes.add(currentIntent);
					}
				}
			}

			/* Recherche du nombre max d'éléments pour un même niveau */
			int maxLevelSize = parentNodes.size();
			for (int i = 1; i < nodesByLevel.size(); i++) {
				Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(i);
				if (currentLevelNodes.size() > maxLevelSize)
					maxLevelSize = currentLevelNodes.size();
			}

			/* Milieu pour les éléments non directement liés à la source et au sink */
			int middleX = 0 - 2 * directNodes.size();

			/* Début pour les élements directement liés à la source et au sink */
			int directInitX = middleX + 2 * parentNodes.size() + 2;

			/* Creation du ConceptPosition pour les noeuds directs */
			Vector secondLevelPositions = null;
			ConceptPosition directPosition = null;
			if (positionsByLevel.size() > 1) {
				secondLevelPositions = (Vector) positionsByLevel.elementAt(1);
				for (int i = 0; i < directNodes.size(); i++) {
					BasicSet directIntent = (BasicSet) directNodes.elementAt(i);
					directPosition = new ConceptPosition(directIntent, directInitX + 4 * i, 3 * yFactor);
					conceptPositions.add(directPosition);
					secondLevelPositions.add(directPosition);
				}
				if (directPosition != null)
					endNodes.add(directPosition);
			}

			/* Creation du Concept Position pour les autres noeud sous la source */
			ConceptPosition parentPosition = null;
			for (int i = 0; i < parentNodes.size(); i++) {
				BasicSet parentIntent = (BasicSet) parentNodes.elementAt(i);
				parentPosition = new ConceptPosition(parentIntent, middleX + i * 4 - 2 * parentNodes.size() + 2,
						3 * yFactor);
				conceptPositions.add(parentPosition);
				secondLevelPositions.add(parentPosition);

				if (i == 0)
					endNodes.add(parentPosition);
			}
			if (directPosition == null && parentPosition != null)
				endNodes.add(parentPosition);

			/* Création du ConceptPosition pour chaque autre noeud */
			for (int i = 2; i < nodesByLevel.size() - 1; i++) {
				Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(i);
				Vector currentLevelPositions = (Vector) positionsByLevel.elementAt(i);
				for (int j = 0; j < currentLevelNodes.size(); j++) {
					BasicSet currentIntent = (BasicSet) currentLevelNodes.elementAt(j);
					ConceptPosition position = new ConceptPosition(currentIntent, middleX + j * 4 - 2
							* currentLevelNodes.size() + 2, i * 3 * yFactor);
					conceptPositions.add(position);
					currentLevelPositions.add(position);
				}

				/* Ajout des noeuds aux extrémités dans la liste des noeuds extrèmes */
				if (currentLevelPositions.size() > 0) {
					endNodes.add(currentLevelPositions.firstElement());
					endNodes.add(currentLevelPositions.lastElement());
				}
			}
		}
	}

	/**
	 * Permet de calculer les coordonnées des concepts d'un treillis à 3 niveau en plaçant les
	 * concepts qui ne sont ni la source ni le sink sur une même ligne, en considérant l'ordre
	 * d'entrée des attributs dans l'éditeur.
	 */
	private void calc3LevelsStructure() {
		/* Création du ConceptPosition pour chaque noeud */
		endNodes = new Vector();

		/* Positionnement de la source */
		BasicSet sourceIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(0)).elementAt(0);
		ConceptPosition sourcePosition = new ConceptPosition(sourceIntent, 0, 0);
		conceptPositions.add(sourcePosition);
		((Vector) positionsByLevel.elementAt(0)).add(sourcePosition);
		endNodes.add(sourcePosition);

		/* Positionnement du sink */
		BasicSet sinkIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(2)).elementAt(0);
		ConceptPosition sinkPosition = new ConceptPosition(sinkIntent, 0, 6);
		conceptPositions.add(sinkPosition);
		((Vector) positionsByLevel.elementAt(2)).add(sinkPosition);
		endNodes.add(sinkPosition);

		/* Positionnement du niveau du milieu */
		Vector middleLevelNodes = (Vector) nodesByLevel.elementAt(1);
		Vector middleLevelPositions = (Vector) positionsByLevel.elementAt(1);
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		ConceptPosition minPos = null;
		ConceptPosition maxPos = null;

		/*
		 * Si les concepts du milieu contiennent un seul attributs, ils seront placés dans l'ordre
		 * d'apparition des attributs dans le contexte
		 */
		boolean simpleAttributes = true;
		String[] attributesOrderTab = new String[context.getAttributeCount()];
		for (int i = 0; i < context.getAttributeCount(); i++)
			attributesOrderTab[i] = ""; //$NON-NLS-1$

		for (int i = 0; i < middleLevelNodes.size(); i++) {
			BasicSet currentIntent = (BasicSet) middleLevelNodes.elementAt(i);
			Iterator attIt = currentIntent.iterator();
			String att = (String) attIt.next();
			if (attIt.hasNext()) {
				simpleAttributes = false;
				break;
			}
			if (context.getObjectsFor(att).size() > 0)
				attributesOrderTab[context.getAttributeIndex(att)] = att;
		}

		/*
		 * L'ordre des attributs ne doit pas inclure les attributs qui ne sont possédés par aucun
		 * objet
		 */
		Vector attributesOrder = new Vector();
		if (simpleAttributes) {
			for (int i = 0; i < context.getAttributeCount(); i++) {
				if (!attributesOrderTab[i].equals("")) //$NON-NLS-1$
					attributesOrder.add(new String(attributesOrderTab[i]));
			}
		}

		for (int i = 0; i < middleLevelNodes.size(); i++) {
			BasicSet currentIntent = (BasicSet) middleLevelNodes.elementAt(i);

			int coordX;
			if (simpleAttributes) {
				/*
				 * Les noeuds sont placés dans le même ordre que les attributs dans l'éditeur de
				 * contextes
				 */
				String att = currentIntent.iterator().next();
				coordX = (attributesOrder.indexOf(att) * 2) - middleLevelNodes.size() + 1;
			} else {
				/*
				 * Les noeuds sont placés dans le même ordre qu'ils apparaissent dans le treillis
				 * conceptuel
				 */
				coordX = (i * 2) - middleLevelNodes.size() + 1;
			}

			ConceptPosition position = new ConceptPosition(currentIntent, coordX, 3);
			conceptPositions.add(position);
			middleLevelPositions.add(position);

			if (coordX < minX) {
				minX = coordX;
				minPos = position;
			}

			if (coordX > maxX) {
				maxX = coordX;
				maxPos = position;
			}
		}

		/* Ajout des noeuds aux extrémités dans la liste des noeuds extrèmes */
		if (minPos != null)
			endNodes.add(minPos);
		if (maxPos != null)
			endNodes.add(maxPos);
	}

	/**
	 * Permet de calculer les coordonnées des concepts d'un treillis à 3 niveau en plaçant les
	 * concepts qui ne sont ni la source ni le sink sur une même ligne, en considérant l'ordre
	 * d'entrée des attributs dans l'éditeur
	 */
	private void calc4LevelsStructure() {
		/* Création du ConceptPosition pour chaque noeud */
		endNodes = new Vector();

		/* Positionnement de la source */
		BasicSet sourceIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(0)).elementAt(0);
		ConceptPosition sourcePosition = new ConceptPosition(sourceIntent, 0, 0);
		conceptPositions.add(sourcePosition);
		((Vector) positionsByLevel.elementAt(0)).add(sourcePosition);
		endNodes.add(sourcePosition);

		/* Positionnement du sink */
		BasicSet sinkIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(3)).elementAt(0);
		ConceptPosition sinkPosition = new ConceptPosition(sinkIntent, 0, 9);
		conceptPositions.add(sinkPosition);
		((Vector) positionsByLevel.elementAt(3)).add(sinkPosition);
		endNodes.add(sinkPosition);

		/*
		 * Niveau 2 : Distinction entre les concepts directement liés au sink et les concepts avec
		 * des enfants
		 */
		Vector secondLevelNodes = (Vector) nodesByLevel.elementAt(1);
		Vector directNodes = new Vector();
		Vector parentNodes = new Vector();
		for (int i = 0; i < secondLevelNodes.size(); i++) {
			BasicSet currentIntent = (BasicSet) secondLevelNodes.elementAt(i);
			FormalConcept currentConcept = lattice.getConceptWithIntent(currentIntent);

			/* Un enfant sans enfant => directement lié au sink */
			if (currentConcept.getChildren().size() == 1
					&& (currentConcept.getChildren().elementAt(0)).getChildren().size() == 0)
				directNodes.add(currentIntent);
			else
				parentNodes.add(currentIntent);
		}

		/* Niveau 3 */
		Vector thirdLevelNodes = (Vector) nodesByLevel.elementAt(2);

		/* HEURISTIQUES RETENUES */
		/* 1 seul enfant : 1 possibilité */
		if (thirdLevelNodes.size() == 1) {
			/* Positionnement de l'enfant */
			ConceptPosition position = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(0), 0, 6);
			conceptPositions.add(position);
			((Vector) positionsByLevel.elementAt(2)).add(position);
			endNodes.add(position);

			/* Positionnement des parents du noeud de niveau 3 */
			ConceptPosition parentPosition = null;
			int initX = 1 - parentNodes.size();
			for (int i = 0; i < parentNodes.size(); i++) {
				parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(i), initX + 2 * i, 3);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);

				if (directNodes.size() == 0 && i == 0)
					endNodes.add(parentPosition);
			}
			if (directNodes.size() == 0 && parentPosition != null)
				endNodes.add(parentPosition);

			/* Positionnement d'une moitié des noeuds directs à gauche */
			ConceptPosition directPosition = null;
			initX = initX - (directNodes.size() / 2) * 2;
			for (int i = 0; i < directNodes.size() / 2; i++) {
				directPosition = new ConceptPosition((BasicSet) directNodes.elementAt(i), initX + 2 * i, 3);
				conceptPositions.add(directPosition);
				((Vector) positionsByLevel.elementAt(1)).add(directPosition);

				if (i == 0)
					endNodes.add(directPosition);
			}

			/* Positionnement de l'autre moitié des noeuds directs à droite */
			initX = parentNodes.size() + 1;
			for (int i = directNodes.size() / 2; i < directNodes.size(); i++) {
				directPosition = new ConceptPosition((BasicSet) directNodes.elementAt(i), initX + 2
						* ((i - directNodes.size() / 2)), 3);
				conceptPositions.add(directPosition);
				((Vector) positionsByLevel.elementAt(1)).add(directPosition);
			}
			if (directPosition != null)
				endNodes.add(directPosition);
		}

		/* 2 enfants et 2 parents : 3 possibilités */
		else if (thirdLevelNodes.size() == 2 && parentNodes.size() == 2) {
			FormalConcept child1 = lattice.getConceptWithIntent((BasicSet) thirdLevelNodes.elementAt(0));
			//FormalConcept child2 = lattice.getConceptWithIntent((BasicSet) thirdLevelNodes.elementAt(1));

			FormalConcept parent1 = lattice.getConceptWithIntent((BasicSet) parentNodes.elementAt(0));
			FormalConcept parent2 = lattice.getConceptWithIntent((BasicSet) parentNodes.elementAt(1));

			/* Chaque parent a un seul enfant distinct (qui n'est pas le sink) */
			if (parent1.getChildren().size() == 1 && parent2.getChildren().size() == 1) {
				/* Positionnement des couples parent-enfant à chaque extrémité */
				int initX = -1 * (directNodes.size() + 1);
				ConceptPosition parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(0), initX, 2);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);
				endNodes.add(parentPosition);

				ConceptPosition childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(0), initX, 6);
				conceptPositions.add(childPosition);
				((Vector) positionsByLevel.elementAt(2)).add(childPosition);
				endNodes.add(childPosition);

				initX = directNodes.size() + 1;
				parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(1), initX, 2);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);
				endNodes.add(parentPosition);

				childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(1), initX, 6);
				conceptPositions.add(childPosition);
				((Vector) positionsByLevel.elementAt(2)).add(childPosition);
				endNodes.add(childPosition);

				/* Positionnement des noeuds directs au milieu */
				initX = 1 - directNodes.size();
				for (int i = 0; i < directNodes.size(); i++) {
					ConceptPosition directPosition = new ConceptPosition((BasicSet) directNodes.elementAt(i), initX + 2
							* i, 4);
					conceptPositions.add(directPosition);
					((Vector) positionsByLevel.elementAt(2)).add(directPosition);
				}

				/* Ajustement de la position du sink */
				sinkPosition.setRelY(8);
			}

			/* Le 1ier parent a un seul enfant et l'autre en a 2. */
			else if (parent1.getChildren().size() == 1 && parent2.getChildren().size() == 2) {
				/* Positionnement des parents */
				int initX = 1 - secondLevelNodes.size();
				ConceptPosition parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(1), initX, 3);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);
				endNodes.add(parentPosition);

				parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(0), initX + 2, 3);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);

				/* L'enfant avec 1 seul parent est placé le plus à gauche */
				if (child1.getParents().size() == 1) {
					ConceptPosition childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(0), initX,
							6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);

					childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(1), initX + 2, 6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);
				}

				else {
					ConceptPosition childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(1), initX,
							6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);

					childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(0), initX + 2, 6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
				}

				/* Positionnement des noeuds directs à droite */
				ConceptPosition directPosition = null;
				initX = initX + 4;
				for (int i = 0; i < directNodes.size(); i++) {
					directPosition = new ConceptPosition((BasicSet) directNodes.elementAt(i), initX + 2 * i, 3);
					conceptPositions.add(directPosition);
					((Vector) positionsByLevel.elementAt(1)).add(directPosition);
				}
				if (directPosition != null)
					endNodes.add(directPosition);
			}

			/* Le 2e parent a un seul enfant et l'autre en a 2 */
			else if (parent2.getChildren().size() == 1 && parent1.getChildren().size() == 2) {
				/* Positionnement des parents */
				int initX = 1 - secondLevelNodes.size();
				ConceptPosition parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(0), initX, 3);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);
				endNodes.add(parentPosition);

				parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(1), initX + 2, 3);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);

				/* L'enfant avec 1 seul parent est placé le plus à gauche */
				if (child1.getParents().size() == 1) {
					ConceptPosition childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(0), initX,
							6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);

					childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(1), initX + 2, 6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);
				}

				else {
					ConceptPosition childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(1), initX,
							6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);

					childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(0), initX + 2, 6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);
				}

				/* Positionnement des noeuds directs à droite */
				ConceptPosition directPosition = null;
				initX = initX + 4;
				for (int i = 0; i < directNodes.size(); i++) {
					directPosition = new ConceptPosition((BasicSet) directNodes.elementAt(i), initX + 2 * i, 3);
					conceptPositions.add(directPosition);
					((Vector) positionsByLevel.elementAt(1)).add(directPosition);
				}
				if (directPosition != null)
					endNodes.add(directPosition);
			}

			/* S'il arrivait un cas non prévu... */
			else {
				conceptPositions = new Vector();
				calcTwoLayersMinCrossing(); //calcSimpleStructure();
			}
		}

		/* 2 enfants et 3 parents : 3 possibilités */
		else if (thirdLevelNodes.size() == 2 && parentNodes.size() == 3) {
			FormalConcept child1 = lattice.getConceptWithIntent((BasicSet) thirdLevelNodes.elementAt(0));
			FormalConcept child2 = lattice.getConceptWithIntent((BasicSet) thirdLevelNodes.elementAt(1));

			FormalConcept parent1 = lattice.getConceptWithIntent((BasicSet) parentNodes.elementAt(0));
			FormalConcept parent2 = lattice.getConceptWithIntent((BasicSet) parentNodes.elementAt(1));
			FormalConcept parent3 = lattice.getConceptWithIntent((BasicSet) parentNodes.elementAt(2));

			/* 2 parents ont 1 enfant et 1 parent a 2 enfants */
			if (child1.getParents().size() == 2 && child2.getParents().size() == 2) {
				int idxCenter = -1;
				int idxLeft = -1;
				int idxRight = -1;
				FormalConcept leftParent = null;

				if (parent1.getChildren().size() == 2 && parent2.getChildren().size() == 1
						&& parent3.getChildren().size() == 1) {
					idxCenter = 0;
					idxLeft = 1;
					idxRight = 2;
					leftParent = parent2;
				} else if (parent1.getChildren().size() == 1 && parent2.getChildren().size() == 2
						&& parent3.getChildren().size() == 1) {
					idxCenter = 1;
					idxLeft = 0;
					idxRight = 2;
					leftParent = parent1;
				} else {
					idxCenter = 2;
					idxLeft = 0;
					idxRight = 1;
					leftParent = parent1;
				}

				/* Le parent avec 2 enfants va au centre et les autres de chaque côté */
				ConceptPosition parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(idxCenter), 0, 3);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);

				parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(idxLeft), -2, 3);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);
				if (directNodes.size() / 2 < 1)
					endNodes.add(parentPosition);

				parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(idxRight), 2, 3);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);
				if (directNodes.size() < 1)
					endNodes.add(parentPosition);

				/* Les enfants sont placés à la position milieu de leurs parents */
				if (child1.getIntent().containsAll(leftParent.getIntent())) {
					ConceptPosition childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(0), -1, 6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);

					childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(1), 1, 6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);
				}

				else {
					ConceptPosition childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(1), -1, 6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);

					childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(0), 1, 6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);
				}

				/* Positionnement d'une moitié des noeuds directs à gauche */
				ConceptPosition directPosition = null;
				int initX = -2 - (directNodes.size() / 2) * 2;
				for (int i = 0; i < directNodes.size() / 2; i++) {
					directPosition = new ConceptPosition((BasicSet) directNodes.elementAt(i), initX + 2 * i, 3);
					conceptPositions.add(directPosition);
					((Vector) positionsByLevel.elementAt(1)).add(directPosition);

					if (i == 0)
						endNodes.add(directPosition);
				}

				/* Positionnement de l'autre moitié des noeuds directs à droite */
				directPosition = null;
				initX = 4;
				for (int i = directNodes.size() / 2; i < directNodes.size(); i++) {
					directPosition = new ConceptPosition((BasicSet) directNodes.elementAt(i), initX + 2
							* ((i - directNodes.size() / 2)), 3);
					conceptPositions.add(directPosition);
					((Vector) positionsByLevel.elementAt(1)).add(directPosition);
				}
				if (directPosition != null)
					endNodes.add(directPosition);
			}

			/* Un enfant a 2 parents et l'autre a 1 parent */
			else if ((child1.getParents().size() == 2 && child2.getParents().size() == 1)
					|| (child2.getParents().size() == 2 && child1.getParents().size() == 1)) {
				FormalConcept leftChild = null;
				if (child1.getParents().size() == 2)
					leftChild = child1;
				else
					leftChild = child2;

				FormalConcept leftParent = null;
				int centerIdx = -1;
				int leftIdx = -1;
				int rightIdx = -1;
				if (leftChild.getIntent().containsAll(parent1.getIntent())) {
					leftParent = parent1;
					leftIdx = 0;
					if (leftChild.getIntent().containsAll(parent2.getIntent())) {
						centerIdx = 1;
						rightIdx = 2;
					} else {
						centerIdx = 2;
						rightIdx = 1;
					}
				} else if (leftChild.getIntent().containsAll(parent2.getIntent())) {
					leftParent = parent2;
					leftIdx = 1;
					if (leftChild.getIntent().containsAll(parent1.getIntent())) {
						centerIdx = 0;
						rightIdx = 2;
					} else {
						centerIdx = 2;
						rightIdx = 0;
					}
				}

				int initX = 1 - (directNodes.size() + parentNodes.size());
				/* L'enfant avec 2 parents va à gauche avec ses parents au dessus */
				ConceptPosition parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(leftIdx), initX,
						3);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);
				endNodes.add(parentPosition);

				parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(centerIdx), initX + 2, 3);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);

				parentPosition = new ConceptPosition((BasicSet) parentNodes.elementAt(rightIdx), initX + 4, 3);
				conceptPositions.add(parentPosition);
				((Vector) positionsByLevel.elementAt(1)).add(parentPosition);
				if (directNodes.size() < 1)
					endNodes.add(parentPosition);

				/* Les enfants sont placés à la position milieu de leurs parents */
				if (child1.getIntent().containsAll(leftParent.getIntent())) {
					ConceptPosition childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(0),
							initX + 1, 6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);

					childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(1), initX + 4, 6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);
				}

				else {
					ConceptPosition childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(1),
							initX + 1, 6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);

					childPosition = new ConceptPosition((BasicSet) thirdLevelNodes.elementAt(0), initX + 4, 6);
					conceptPositions.add(childPosition);
					((Vector) positionsByLevel.elementAt(2)).add(childPosition);
					endNodes.add(childPosition);
				}

				/* Positionnement des noeuds directs à droite */
				ConceptPosition directPosition = null;
				initX = initX + 6;
				for (int i = 0; i < directNodes.size(); i++) {
					directPosition = new ConceptPosition((BasicSet) directNodes.elementAt(i), initX + 2 * i, 3);
					conceptPositions.add(directPosition);
					((Vector) positionsByLevel.elementAt(1)).add(directPosition);
				}
				if (directPosition != null)
					endNodes.add(directPosition);
			}

			/* S'il arrivait un cas non prévu... */
			else {
				conceptPositions = new Vector();
				int oldMax = MAX_SOLUTIONS;
				MAX_SOLUTIONS = 100;

				if (isIntersectionAvailable() && calcTwoLayersMinCrossing() == 0) {
					MAX_SOLUTIONS = oldMax;
					return;
				}

				MAX_SOLUTIONS = oldMax;
				if (isParentDrivenAvailable())
					calcParentDrivenStructure();
				else
					calcSimpleStructure();
			}
		}

		/* Pour tous les autres cas, la structure simple est utilisée */
		else {
			conceptPositions = new Vector();
			int oldMax = MAX_SOLUTIONS;
			MAX_SOLUTIONS = 100;

			if (isIntersectionAvailable() && calcTwoLayersMinCrossing() == 0) {
				MAX_SOLUTIONS = oldMax;
				return;
			}

			MAX_SOLUTIONS = oldMax;
			if (isParentDrivenAvailable())
				calcParentDrivenStructure();
			else
				calcSimpleStructure();
		}
	}

	/**
	 * Permet de calculer les coordonnées des concepts d'un treillis pour lui donner une structure
	 * cubique
	 */
	private void calcCubicStructure() {
		/* Obtention des positions de chacune des intentions du premier niveau */
		Vector firstLevelIntents = sortConceptIntents((Vector) nodesByLevel.elementAt(1));
		Vector deltaX = calcFirstLevelDelta(firstLevelIntents.size());
		int levelCount = nodesByLevel.size();

		/* Création d'un vecteur pour contenir les noeuds aux extrémités de chaque niveau */
		endNodes = new Vector();

		/* Positionnement de la source */
		BasicSet sourceIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(0)).elementAt(0);
		ConceptPosition sourcePosition = new ConceptPosition(sourceIntent, 0, 0);
		conceptPositions.add(sourcePosition);
		((Vector) positionsByLevel.elementAt(0)).add(sourcePosition);
		endNodes.add(sourcePosition);

		/* Positionnement des niveaux internes */
		for (int i = 1; i < nodesByLevel.size() - 1; i++) {
			Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(i);
			Vector currentLevelPositions = (Vector) positionsByLevel.elementAt(i);
			double minX = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			ConceptPosition minPos = null;
			ConceptPosition maxPos = null;
			for (int j = 0; j < currentLevelNodes.size(); j++) {
				BasicSet currentIntent = (BasicSet) currentLevelNodes.elementAt(j);

				/* Les noeuds sont placés à la position attribuée à leur intention */
				int posX = findCubicIntentPositionX(firstLevelIntents, deltaX, currentIntent);
				int coordX = posX * 3 - (i * 3);

				ConceptPosition position = new ConceptPosition(currentIntent, coordX, (i * 3));
				conceptPositions.add(position);
				currentLevelPositions.add(position);

				if (coordX < minX) {
					minX = coordX;
					minPos = position;
				}

				if (coordX > maxX) {
					maxX = coordX;
					maxPos = position;
				}
			}

			/* Ajout des noeuds aux extrémités dans la liste des noeuds extrèmes */
			if (minPos != null)
				endNodes.add(minPos);
			if (maxPos != null)
				endNodes.add(maxPos);
		}

		/* Positionnement du sink */
		BasicSet sinkIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(levelCount - 1)).elementAt(0);
		double sinkCoordX;
		if (nodesByLevel.size() == ((Vector) nodesByLevel.elementAt(1)).size() + 1) {
			/* Cube à la structure complète, donc ayant tous ses niveaux */
			int posX = findCubicIntentPositionX(firstLevelIntents, deltaX, sinkIntent);
			sinkCoordX = posX * 3 - ((levelCount - 1) * 3);
		} else
			/*
			 * Cube auquel il manque des niveaux : le sink est positionné en X au centre du dernier
			 * niveau
			 */
			sinkCoordX = (((ConceptPosition) endNodes.elementAt(endNodes.size() - 1)).getRelX() + ((ConceptPosition) endNodes.elementAt(endNodes.size() - 2)).getRelX()) / 2;

		ConceptPosition sinkPosition = new ConceptPosition(sinkIntent, sinkCoordX, (levelCount - 1) * 3);
		conceptPositions.add(sinkPosition);
		((Vector) positionsByLevel.elementAt(levelCount - 1)).add(sinkPosition);
		endNodes.add(sinkPosition);
	}

	private void calcAdditiveStructure() {
		/* Création du ConceptPosition pour chaque noeud */
		endNodes = new Vector();

		/* Positionnement de la source */
		BasicSet sourceIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(0)).elementAt(0);
		ConceptPosition sourcePosition = new ConceptPosition(sourceIntent, 0, 0);
		conceptPositions.add(sourcePosition);
		((Vector) positionsByLevel.elementAt(0)).add(sourcePosition);
		endNodes.add(sourcePosition);

		/* Positionnement du sink */
		BasicSet sinkIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(nodesByLevel.size() - 1)).elementAt(0);
		ConceptPosition sinkPosition = new ConceptPosition(sinkIntent, 0, 3 * (nodesByLevel.size() - 1));
		conceptPositions.add(sinkPosition);
		((Vector) positionsByLevel.elementAt(positionsByLevel.size() - 1)).add(sinkPosition);
		endNodes.add(sinkPosition);

		/*
		 * Attribution d'une position à chaque attribut (position = ordre dans la liste des
		 * attributs)
		 */
		Vector attributes = context.getAttributes();
		Vector permutations = findPermutations(attributes);
		int bestCrossingCount = Integer.MAX_VALUE - 1;
		//int bestIdx = -1;

		bestOrderByLevel = new Vector();
		for (int i = 0; i < nodesByLevel.size(); i++) {
			Vector currentLevel = (Vector) nodesByLevel.elementAt(i);
			Vector currentNewLevel = new Vector();
			for (int j = 0; j < currentLevel.size(); j++)
				currentNewLevel.add(currentLevel.elementAt(j));
			bestOrderByLevel.add(currentNewLevel);
		}

		for (int p = 0; p < permutations.size(); p++) {
			resetStructure();
			Vector attributeOrder = (Vector) permutations.elementAt(p);

			/* Positionnement des niveaux internes */
			for (int i = 1; i < nodesByLevel.size() - 1; i++) {
				Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(i);
				Vector currentLevelPositions = (Vector) positionsByLevel.elementAt(i);
				currentLevelPositions.removeAllElements();

				//ConceptPosition minPos = null;
				//ConceptPosition maxPos = null;
				for (int j = 0; j < currentLevelNodes.size(); j++) {
					BasicSet currentIntent = (BasicSet) currentLevelNodes.elementAt(j);

					/* Les noeuds sont placés à la position attribuée à leur intention */
					double posX = findAdditiveIntentPositionX(attributeOrder, currentIntent);
					double coordX = posX * 2;

					ConceptPosition position = new ConceptPosition(currentIntent, coordX, (i * 3));
					conceptPositions.add(position);
					currentLevelPositions.add(position);
				}
			}

			int interCount = getCrossingCount();
			if (interCount < bestCrossingCount) {
				bestCrossingCount = interCount;
				copyNodesOrder(nodesByLevel, bestOrderByLevel);
			}
		}

		copyNodesOrder(bestOrderByLevel, nodesByLevel);
		((Vector) positionsByLevel.elementAt(0)).removeAllElements();
		((Vector) positionsByLevel.elementAt(0)).add(sourcePosition);
		((Vector) positionsByLevel.elementAt(positionsByLevel.size() - 1)).removeAllElements();
		((Vector) positionsByLevel.elementAt(positionsByLevel.size() - 1)).add(sinkPosition);

		for (int i = 0; i < nodesByLevel.size(); i++) {
			Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(i);
			Vector currentLevelPositions = (Vector) positionsByLevel.elementAt(i);
			double minX = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			ConceptPosition minPos = null;
			ConceptPosition maxPos = null;
			for (int j = 0; j < currentLevelNodes.size(); j++) {
				ConceptPosition position = (ConceptPosition) currentLevelPositions.elementAt(j);
				double coordX = position.getRelX();

				if (coordX < minX) {
					minX = coordX;
					minPos = position;
				}

				if (coordX > maxX) {
					maxX = coordX;
					maxPos = position;
				}
			}

			/* Ajout des noeuds aux extrémités dans la liste des noeuds extrèmes */
			if (minPos != null)
				endNodes.add(minPos);
			if (maxPos != null)
				endNodes.add(maxPos);
		}
	}

	private void calcParentDrivenStructure() {
		if (nodesByLevel == null || nodesByLevel.size() == 0)
			return;

		if (nodesByLevel.size() < 2) {
			calcSimpleStructure();
			return;
		}

		/* Vérifie que le nombre de permutations à faire au premier niveau sera raisonnable */
		if (((Vector) nodesByLevel.elementAt(1)).size() > 7) {
			calcSimpleStructure();
			return;
		}

		int yFactor = 1;
		for (int i = 0; i < nodesByLevel.size(); i++) {
			Vector level = (Vector) nodesByLevel.elementAt(i);
			if (level.size() > 20 && yFactor < 3)
				yFactor = 3;
			else if (level.size() > 12 && yFactor < 2)
				yFactor = 2;
		}

		Vector nodesPosition = new Vector();
		for (int i = 0; i < nodesByLevel.size(); i++)
			nodesPosition.add(new Vector());

		Vector bestPositionsByLevel = new Vector();
		for (int i = 0; i < nodesByLevel.size(); i++) {
			Vector currentLevel = (Vector) nodesByLevel.elementAt(i);
			Vector currentNewLevel = new Vector();
			for (int j = 0; j < currentLevel.size(); j++)
				currentNewLevel.add(new ConceptPosition(null, 0, 0));
			bestPositionsByLevel.add(currentNewLevel);
		}

		bestCrossingCount = Integer.MAX_VALUE - 1;

		BasicSet topIntent = (BasicSet) ((Vector) nodesByLevel.elementAt(0)).elementAt(0);
		ConceptPosition topPosition = new ConceptPosition(topIntent, 0, 0);
		Vector topLevel = new Vector();
		topLevel.add(topPosition);
		nodesPosition.setElementAt(topLevel, 0);

		Vector topPermutations = findPermutations((Vector) nodesByLevel.elementAt(1));
		for (int i = 0; i < topPermutations.size() && i < MAX_SOLUTIONS; i++) {
			nodesByLevel.setElementAt(topPermutations.elementAt(i), 1);
			Vector currentLevelNodes = (Vector) nodesByLevel.elementAt(1);

			Vector currentLevelPositions = new Vector();
			for (int j = 0; j < currentLevelNodes.size(); j++) {
				ConceptPosition position = new ConceptPosition((BasicSet) currentLevelNodes.elementAt(j), j * 4 - 2
						* (currentLevelNodes.size() - 1), 3 * yFactor);
				currentLevelPositions.add(position);
			}
			nodesPosition.setElementAt(currentLevelPositions, 1);

			for (int j = 2; j < nodesByLevel.size(); j++) {
				Vector nextLevel = (Vector) nodesByLevel.elementAt(j);
				currentLevelPositions = findNextLevelPositions(currentLevelPositions, nextLevel, j, yFactor);
				nodesPosition.setElementAt(currentLevelPositions, j);
			}

			calcPositionsStructure(nodesPosition);
			int interCount = getCrossingCount();

			if (interCount < bestCrossingCount) {
				bestCrossingCount = interCount;
				copyNodesPosition(nodesPosition, bestPositionsByLevel);

				if (interCount == 0)
					break;
			}
		}

		calcPositionsStructure(bestPositionsByLevel);
		copyNodesPosition(bestPositionsByLevel, nodesPosition);
	}

	private Vector findNextLevelPositions(Vector parentLevelPositions, Vector childrenLevel, int level, int yFactor) {
		Vector childrenPositions = new Vector();
		int totalMinX = Integer.MAX_VALUE;
		int totalMaxX = Integer.MIN_VALUE;

		for (int i = 0; i < childrenLevel.size(); i++) {
			BasicSet childIntent = (BasicSet) childrenLevel.elementAt(i);
			int minX = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;

			for (int j = 0; j < parentLevelPositions.size(); j++) {
				ConceptPosition parentPosition = (ConceptPosition) parentLevelPositions.elementAt(j);
				BasicSet parentIntent = parentPosition.getIntent();
				if (childIntent.containsAll(parentIntent)) {
					if (parentPosition.getRelX() < minX)
						minX = (int) parentPosition.getRelX();
					if (parentPosition.getRelX() > maxX)
						maxX = (int) parentPosition.getRelX();
				}
			}

			/*
			 * Pour assurer la symétrie, les arrondissement sont fait différemment pour les positifs
			 * et les négatifs
			 */
			int childX;
			if (maxX + minX < 0)
				childX = (int) Math.floor((maxX + minX) / 2.0);
			else
				childX = (int) Math.ceil((maxX + minX) / 2.0);

			ConceptPosition childPosition = new ConceptPosition(childIntent, childX, level * 3 * yFactor);
			childrenPositions.add(childPosition);

			if (childX < totalMinX)
				totalMinX = childX;
			if (childX > totalMaxX)
				totalMaxX = childX;
		}

		/* L'espace de coordonnées est agrandi pour s'assurer que chaque concept aura une position */
		totalMinX -= 4 * childrenLevel.size();
		totalMaxX += 4 * childrenLevel.size();
		int coordSize = totalMaxX - totalMinX + 1 + childrenLevel.size() * 8;

		boolean[] positionTaken = new boolean[coordSize];
		for (int i = 0; i < coordSize; i++)
			positionTaken[i] = false;

		for (int i = 0; i < childrenPositions.size(); i++) {
			ConceptPosition childPosition = (ConceptPosition) childrenPositions.elementAt(i);
			int idx = (int) childPosition.getRelX() - totalMinX;
			int newIdx = idx;
			while (positionTaken[newIdx] || (newIdx > 0 && positionTaken[newIdx - 1])
					|| (newIdx < coordSize - 1 && positionTaken[newIdx + 1])) {

				if (totalMinX + idx > 0) {
					/* Si la position a été avancée à la dernière itération, on la recule d'autant */
					if (newIdx > idx) {
						if (idx - (newIdx - idx) > 0)
							newIdx = idx - (newIdx - idx);
						else
							newIdx++;
					}

					/* Si la position a été reculée à la dernière itération, on l'avance d'autant +1 */
					else {
						if (idx + (idx - newIdx) + 1 < coordSize)
							newIdx = idx + (idx - newIdx) + 1;
						else
							newIdx--;
					}
				}

				else {
					/* Si la position a été reculée à la dernière itération, on l'avance d'autant */
					if (newIdx < idx) {
						if (idx + (idx - newIdx) < coordSize)
							newIdx = idx + (idx - newIdx);
						else
							newIdx--;
					}

					/*
					 * Si la position a été avancée à la dernière itération, on la recule d'autant -
					 * 1
					 */
					else {
						if (idx - (newIdx - idx) - 1 > 0)
							newIdx = idx - (newIdx - idx) - 1;
						else
							newIdx++;
					}
				}
			}

			childPosition.setRelX(totalMinX + newIdx);
			positionTaken[newIdx] = true;
		}

		/*
		 * Espacement des enfants d'un niveau plus grand que le niveau parent pour permettre aux
		 * niveaux de prendre plus d'ampleur et de ne pas se limiter à la position des extrémités du
		 * niveau supérieur
		 */
		Vector newChildrenPositions;
		if (parentLevelPositions.size() < childrenPositions.size()) {
			newChildrenPositions = new Vector();
			int totalInc = (childrenPositions.size() - parentLevelPositions.size()) * 2;
			int stepInc = totalInc / (int) Math.floor(childrenPositions.size() / 2);
			int currentInc = totalInc;

			while (childrenPositions.size() > 1) {
				totalMinX = Integer.MAX_VALUE;
				totalMaxX = Integer.MIN_VALUE;
				int minIdx = -1;
				int maxIdx = -1;
				for (int i = 0; i < childrenPositions.size(); i++) {
					ConceptPosition childPosition = (ConceptPosition) childrenPositions.elementAt(i);
					if (childPosition.getRelX() < totalMinX) {
						totalMinX = (int) childPosition.getRelX();
						minIdx = i;
					}
					if (childPosition.getRelX() > totalMaxX) {
						totalMaxX = (int) childPosition.getRelX();
						maxIdx = i;
					}
				}

				ConceptPosition firstChild = (ConceptPosition) childrenPositions.elementAt(minIdx);
				//positionTaken[(int)firstChild.getRelX()] = false;
				//positionTaken[(int)firstChild.getRelX()-(int)currentInc] = true;
				firstChild.setRelX(firstChild.getRelX() - currentInc);

				ConceptPosition lastChild = (ConceptPosition) childrenPositions.elementAt(maxIdx);
				//positionTaken[(int)lastChild.getRelX()] = false;
				//positionTaken[(int)lastChild.getRelX()+(int)currentInc] = true;
				lastChild.setRelX(lastChild.getRelX() + currentInc);

				currentInc = currentInc - stepInc;
				newChildrenPositions.add(firstChild);
				newChildrenPositions.add(lastChild);
				childrenPositions.remove(firstChild);
				childrenPositions.remove(lastChild);
			}

			newChildrenPositions.addAll(childrenPositions);
			childrenPositions = newChildrenPositions;
		}

		//     for(int i=0; i<childrenPositions.size(); i++){
		//       ConceptPosition currChild = (ConceptPosition)childrenPositions.elementAt(i);
		//       int posX = (int)currChild.getRelX();
		//
		//       int leftX = posX-1;
		//       while(leftX > 0 && !positionTaken[leftX])
		//         leftX--;
		//       int rightX = posX+1;
		//       while(rightX < coordSize && !positionTaken[rightX])
		//         rightX++;
		//
		//       /*Premier concept à gauche*/
		//       if(leftX == -1){
		//         if(rightX < posX + 4){
		//           currChild.setRelPosition(rightX - 4);
		//           positionTaken[posX] = false;
		//           if(rightX - 4 >= 0)
		//             positionTaken[rightX - 4] = true;
		//           else
		//             positionTaken[0] = true;
		//         }
		//       }
		//     }

		/* Ajustement des noeuds aux extrémités */
		if (childrenPositions.size() > 1) {
			newChildrenPositions = new Vector();
			int leftEndIdx = -1;
			int rightEndIdx = -1;
			int beforeLeftIdx = -1;
			int beforeRightIdx = -1;
			boolean endFound = false;
			boolean beforeEndFound = true;

			while (childrenPositions.size() > 1) {
				totalMinX = Integer.MAX_VALUE;
				totalMaxX = Integer.MIN_VALUE;
				int minIdx = -1;
				int maxIdx = -1;
				for (int i = 0; i < childrenPositions.size(); i++) {
					ConceptPosition childPosition = (ConceptPosition) childrenPositions.elementAt(i);
					if (childPosition.getRelX() < totalMinX) {
						totalMinX = (int) childPosition.getRelX();
						minIdx = i;
					}
					if (childPosition.getRelX() > totalMaxX) {
						totalMaxX = (int) childPosition.getRelX();
						maxIdx = i;
					}
				}

				ConceptPosition firstChild = (ConceptPosition) childrenPositions.elementAt(minIdx);
				ConceptPosition lastChild = (ConceptPosition) childrenPositions.elementAt(maxIdx);

				if (!beforeEndFound) {
					beforeLeftIdx = newChildrenPositions.size();
					beforeRightIdx = newChildrenPositions.size() + 1;
					beforeEndFound = true;
				}

				else if (!endFound) {
					leftEndIdx = newChildrenPositions.size();
					rightEndIdx = newChildrenPositions.size() + 1;
					endFound = true;
					beforeEndFound = false;
				}

				else
					break;

				newChildrenPositions.add(firstChild);
				newChildrenPositions.add(lastChild);
				childrenPositions.remove(firstChild);
				childrenPositions.remove(lastChild);
			}

			/*
			 * Modification de la position des noeuds limites pour leur permettre de ne pas être
			 * trop près de leur voisin
			 */
			if (leftEndIdx > -1 && beforeLeftIdx > -1) {
				ConceptPosition leftEnd = (ConceptPosition) newChildrenPositions.elementAt(leftEndIdx);
				ConceptPosition beforeLeft = (ConceptPosition) newChildrenPositions.elementAt(beforeLeftIdx);

				if (leftEnd.getRelX() > beforeLeft.getRelX() - 4.0) {
					leftEnd.setRelX(beforeLeft.getRelX() - 4.0);
				}

				ConceptPosition rightEnd = (ConceptPosition) newChildrenPositions.elementAt(rightEndIdx);
				ConceptPosition beforeRight = (ConceptPosition) newChildrenPositions.elementAt(beforeRightIdx);

				if (rightEnd.getRelX() < beforeRight.getRelX() + 4.0) {
					rightEnd.setRelX(beforeRight.getRelX() + 4.0);
				}
			}

			else if (leftEndIdx > -1) {
				ConceptPosition leftEnd = (ConceptPosition) newChildrenPositions.elementAt(leftEndIdx);
				ConceptPosition rightEnd = (ConceptPosition) newChildrenPositions.elementAt(rightEndIdx);

				if (childrenPositions.size() == 0 && leftEnd.getRelX() > rightEnd.getRelX() - 4.0) {
					leftEnd.setRelX(rightEnd.getRelX() - 2.0);
					rightEnd.setRelX(leftEnd.getRelX() + 4.0);
				}

				else if (childrenPositions.size() > 0) {
					ConceptPosition middleChild = (ConceptPosition) childrenPositions.elementAt(0);

					if (leftEnd.getRelX() > middleChild.getRelX() - 4.0) {
						leftEnd.setRelX(middleChild.getRelX() - 4.0);
					}

					if (rightEnd.getRelX() < middleChild.getRelX() + 4.0) {
						rightEnd.setRelX(middleChild.getRelX() + 4.0);
					}
				}
			}

			newChildrenPositions.addAll(childrenPositions);
			childrenPositions = newChildrenPositions;
		}

		return childrenPositions;
	}

	private int calcTwoLayersMinCrossing() {
		if (nodesByLevel == null || nodesByLevel.size() == 0)
			return Integer.MAX_VALUE;

		/* Vérifie que le nombre de permutations à faire sera raisonnable */
		boolean doMinCrossing = true;
		for (int i = 0; i < nodesByLevel.size(); i++) {
			if (((Vector) nodesByLevel.elementAt(i)).size() > 7) {
				doMinCrossing = false;
				break;
			}
		}

		if (nodesByLevel.size() < 4)
			doMinCrossing = false;

		if (!doMinCrossing) {
			calcSimpleStructure();
			return Integer.MAX_VALUE;
		}

		Vector topLevel = (Vector) nodesByLevel.elementAt(0);
		Vector bottomLevel = (Vector) nodesByLevel.elementAt(nodesByLevel.size() - 1);

		Vector nodesOrder = new Vector();
		for (int i = 0; i < nodesByLevel.size(); i++)
			nodesOrder.add(new Vector());

		bestOrderByLevel = new Vector();
		for (int i = 0; i < nodesByLevel.size(); i++) {
			Vector currentLevel = (Vector) nodesByLevel.elementAt(i);
			Vector currentNewLevel = new Vector();
			for (int j = 0; j < currentLevel.size(); j++)
				currentNewLevel.add(currentLevel.elementAt(j));
			bestOrderByLevel.add(currentNewLevel);
		}

		bestCrossingCount = Integer.MAX_VALUE - 1;

		int maxPermutations = (int) Math.floor((double) MAX_SOLUTIONS / (double) (nodesByLevel.size() - 2));

		Vector firstLevel = (Vector) nodesByLevel.elementAt(1);
		Vector topPermutations = findPermutations(firstLevel);

		//solutionCount = 0;
		for (int i = 0; i < topPermutations.size() && i < maxPermutations; i++) {
			nodesOrder.setElementAt(topPermutations.elementAt(i), 1);
			Vector currPermutation = (Vector) topPermutations.elementAt(i);

			Vector currentLevel = currPermutation;
			for (int j = 2; j < nodesByLevel.size() - 1; j++) {
				Vector nextLevel = (Vector) nodesByLevel.elementAt(j);
				nodesOrder.setElementAt(findNextLevelOrder(currentLevel, nextLevel, maxPermutations), j);
			}
			nodesOrder.setElementAt(topLevel, 0);
			nodesOrder.setElementAt(bottomLevel, nodesOrder.size() - 1);

			resetStructure();

			calcOrderStructure(nodesOrder);

			int interCount = getCrossingCount();

			if (interCount < bestCrossingCount) {
				bestCrossingCount = interCount;
				copyNodesOrder(nodesOrder, bestOrderByLevel);

				if (interCount == 0)
					break;
			}
		}

		/*
		 * Vector lastLevel = (Vector)nodesByLevel.elementAt(nodesByLevel.size()-2); Vector
		 * bottomPermutations = findPermutations(lastLevel); i=0; i<bottomPermutations.size();
		 * i++){ nodesOrder.setElementAt((Vector)bottomPermutations.elementAt(i), 1); Vector
		 * currPermutation = (Vector)bottomPermutations.elementAt(i); Vector currentLevel =
		 * currPermutation; for(int j=nodesByLevel.size()-2; j>0; j--){ Vector previousLevel =
		 * (Vector)nodesByLevel.elementAt(j);
		 * nodesOrder.setElementAt(findPreviousLevelOrder(currentLevel, previousLevel), j); }
		 * nodesOrder.setElementAt(topLevel, 0); nodesOrder.setElementAt(bottomLevel,
		 * nodesOrder.size()-1); resetStructure(); calcOrderStructure(nodesOrder); int interCount =
		 * getCrossingCount(); bestCrossingCount){ bestCrossingCount = interCount;
		 * copyNodesOrder(nodesOrder, bestOrderByLevel); } }
		 */

		resetStructure();
		copyNodesOrder(bestOrderByLevel, nodesByLevel);
		calcOrderStructure(bestOrderByLevel);

		return bestCrossingCount;
	}

	private Vector findNextLevelOrder(Vector fixedLevel, Vector nextLevel, int maxPermutations) {
		Vector permutations = findPermutations(nextLevel);

		int bestInterCount = Integer.MAX_VALUE;
		int bestIdx = -1;
		for (int i = 0; i < permutations.size() && i < maxPermutations /*
																		 * && solutionCount <
																		 * MAX_SOLUTIONS
																		 */; i++) {
			Vector currentOrder = (Vector) permutations.elementAt(i);

			int interCount = getIntersectionCount(fixedLevel, currentOrder);

			if (interCount < bestInterCount) {
				bestInterCount = interCount;
				bestIdx = i;
			}
		}
		return (Vector) permutations.elementAt(bestIdx);
	}

	@SuppressWarnings("unused") //$NON-NLS-1$
	private Vector findPreviousLevelOrder(Vector fixedLevel, Vector previousLevel) {
		Vector permutations = findPermutations(previousLevel);

		int bestInterCount = Integer.MAX_VALUE;
		int bestIdx = -1;
		for (int i = 0; i < permutations.size(); i++) {
			Vector currentOrder = (Vector) permutations.elementAt(i);
			int interCount = getIntersectionCount(currentOrder, fixedLevel);

			if (interCount < bestInterCount) {
				bestInterCount = interCount;
				bestIdx = i;
			}
		}
		return (Vector) permutations.elementAt(bestIdx);
	}

	private int getIntersectionCount(Vector parentLevel, Vector childrenLevel) {
		Vector edges = new Vector();
		for (int i = 0; i < parentLevel.size(); i++) {
			BasicSet parentIntent = (BasicSet) parentLevel.elementAt(i);
			for (int j = 0; j < childrenLevel.size(); j++) {
				BasicSet childIntent = (BasicSet) childrenLevel.elementAt(j);
				if (childIntent.containsAll(parentIntent)) {
					double p_x = (i * 4 - 2 * (parentLevel.size() - 1));
					double p_y = 0.0;
					double c_x = (j * 4 - 2 * (parentLevel.size() - 1));
					double c_y = 3.0;
					edges.add(new Line2D.Double(p_x, p_y, c_x, c_y));
				}
			}
		}

		int interCount = getCrossingCount(edges);
		return interCount;
	}

	@SuppressWarnings("unused") //$NON-NLS-1$
	private void findBestCrossingOrder() {
		if (nodesByLevel == null || nodesByLevel.size() == 0)
			return;

		int firstLevelSize = ((Vector) nodesByLevel.elementAt(0)).size();
		boolean[] usedNodes = new boolean[firstLevelSize];
		for (int i = 0; i < firstLevelSize; i++)
			usedNodes[i] = false;

		Vector nodesOrder = new Vector();
		for (int i = 0; i < nodesByLevel.size(); i++)
			nodesOrder.add(new Vector());

		bestOrderByLevel = new Vector();
		for (int i = 0; i < nodesByLevel.size(); i++) {
			Vector currentLevel = (Vector) nodesByLevel.elementAt(i);
			Vector currentNewLevel = new Vector();
			for (int j = 0; j < currentLevel.size(); j++)
				currentNewLevel.add(currentLevel.elementAt(j));
			bestOrderByLevel.add(currentNewLevel);
		}

		bestCrossingCount = Integer.MAX_VALUE - 1;
		solutionCount = 0;
		reorderNodes(nodesOrder, usedNodes, 0);

		copyNodesOrder(bestOrderByLevel, nodesByLevel);
	}

	private int reorderNodes(Vector nodesOrderByLevel, boolean[] usedNodes, int levelIdx) {
		if (bestCrossingCount == 0)
			return bestCrossingCount;
		if (solutionCount > MAX_SOLUTIONS)
			return bestCrossingCount;

		if (levelIdx >= nodesByLevel.size()) {
			solutionCount++;
			calcOrderStructure(nodesOrderByLevel);
			int interCount = getCrossingCount();

			if (interCount < bestCrossingCount) {
				bestCrossingCount = interCount;
				copyNodesOrder(nodesOrderByLevel, bestOrderByLevel);
			}

			resetStructure();
			return interCount;
		}

		int interCount = Integer.MAX_VALUE - 1;
		int localBestCount = Integer.MAX_VALUE - 1;

		Vector currentLevel = (Vector) nodesByLevel.elementAt(levelIdx);
		Vector currentOrder = (Vector) nodesOrderByLevel.elementAt(levelIdx);

		for (int i = 0; i < currentLevel.size(); i++) {
			if (solutionCount > MAX_SOLUTIONS)
				return bestCrossingCount;
			if (usedNodes[i] == false) {
				usedNodes[i] = true;
				int addIdx = currentOrder.size();
				currentOrder.add(currentLevel.elementAt(i));

				/* Traitement du niveau suivant */
				if (currentOrder.size() == currentLevel.size() && levelIdx < nodesByLevel.size() - 1) {
					int nextLevelSize = ((Vector) nodesByLevel.elementAt(levelIdx + 1)).size();
					boolean[] newUsedNodes = new boolean[nextLevelSize];
					for (int j = 0; j < nextLevelSize; j++)
						newUsedNodes[j] = false;

					interCount = reorderNodes(nodesOrderByLevel, newUsedNodes, levelIdx + 1);
				}

				/* Traitement du prochain concept de ce niveau */
				else if (currentOrder.size() < currentLevel.size())
					interCount = reorderNodes(nodesOrderByLevel, usedNodes, levelIdx);

				/* Fin de l'ordonnancement */
				else
					interCount = reorderNodes(nodesOrderByLevel, usedNodes, levelIdx + 1);

				if (interCount < localBestCount)
					localBestCount = interCount;

				usedNodes[i] = false;
				currentOrder.removeElementAt(addIdx);
			}
		}

		return localBestCount;
	}

	private void copyNodesOrder(Vector srcOrderByLevel, Vector destOrderByLevel) {
		for (int i = 0; i < srcOrderByLevel.size(); i++) {
			Vector currentLevel = (Vector) destOrderByLevel.elementAt(i);
			Vector currentOrder = (Vector) srcOrderByLevel.elementAt(i);
			currentLevel.removeAllElements();
			for (int j = 0; j < currentOrder.size(); j++) {
				BasicSet currentIntent = (BasicSet) currentOrder.elementAt(j);
				currentLevel.add(currentIntent);
			}
		}
	}

	private void copyNodesPosition(Vector srcPositionsByLevel, Vector destPositionsByLevel) {
		for (int i = 0; i < srcPositionsByLevel.size(); i++) {
			Vector currentLevel = (Vector) destPositionsByLevel.elementAt(i);
			Vector currentPositions = (Vector) srcPositionsByLevel.elementAt(i);
			currentLevel.removeAllElements();
			for (int j = 0; j < currentPositions.size(); j++) {
				ConceptPosition currentPosition = (ConceptPosition) currentPositions.elementAt(j);
				currentLevel.add(currentPosition);
			}
		}
	}

	private Vector getChildrenPositions(ConceptPosition position, int level) {
		FormalConcept parent = lattice.getConceptWithIntent(position.getIntent());
		Vector childrenConcepts = parent.getChildren();

		Vector childrenPositions = new Vector();
		for (int i = 0; i < childrenConcepts.size(); i++) {
			FormalConcept child = (FormalConcept) childrenConcepts.elementAt(i);

			int childLevel = -1;
			for (int j = level + 1; j < positionsByLevel.size() && childLevel < 0; j++) {
				Vector levelPositions = (Vector) positionsByLevel.elementAt(j);
				for (int k = 0; k < levelPositions.size() && childLevel < 0; k++) {
					if (child.getIntent().equals(((ConceptPosition) levelPositions.elementAt(k)).getIntent())) {
						childrenPositions.add(levelPositions.elementAt(k));
						childLevel = j;
					}
				}
			}
		}

		return childrenPositions;
	}

	private int findVerticalPosition(Vector verticalOrder, Line2D seg) {
		for (int i = 0; i < verticalOrder.size(); i++) {
			EventPoint event = (EventPoint) verticalOrder.elementAt(i);
			if (event.edge == seg)
				return i;
		}

		return -1;
	}

	private int insertVerticalPoint(Vector verticalOrder, EventPoint event) {
		for (int i = 0; i < verticalOrder.size(); i++) {
			EventPoint currEvent = (EventPoint) verticalOrder.elementAt(i);

			/* Traitement des segments verticaux */
			if (currEvent.edge.getX1() == currEvent.edge.getX2()) {
				if (event.y >= currEvent.edge.getY1() && event.y >= currEvent.edge.getY2()) {
					verticalOrder.insertElementAt(event, i);
					return i;
				}
				continue;
			}

			double m = (currEvent.edge.getY2() - currEvent.edge.getY1())
					/ (currEvent.edge.getX2() - currEvent.edge.getX1());
			double b = currEvent.y - m * currEvent.x;
			double y = Math.round((m * event.x + b) * 1000000.0) / 1000000.0;

			if (y < event.y) {
				verticalOrder.insertElementAt(event, i);
				return i;
			}

			/* Ajout d'un segment à la même position en x que l'ajout d'un autre segment */
			if (y == event.y && currEvent.type == 1) {
				double currEndX = currEvent.edge.getX2();
				double currEndY = currEvent.edge.getY2();
				if (currEndX == currEvent.x && currEndY == currEvent.x) {
					currEndX = currEvent.edge.getX1();
					currEndY = currEvent.edge.getY1();
				}

				double eventEndX = event.edge.getX2();
				double eventEndY = event.edge.getY2();
				if (eventEndX == event.x && eventEndX == event.y) {
					eventEndX = event.edge.getX1();
					eventEndY = event.edge.getY1();
				}

				double crossProduct = (eventEndX - event.x) * (currEndY - event.y) - (currEndX - event.x)
						* (eventEndY - event.y);

				if (crossProduct <= 0) {
					verticalOrder.insertElementAt(event, i);
					return i;
				}
			}
		}

		verticalOrder.add(event);
		return verticalOrder.size() - 1;
	}

	private int getIntersectionIdx(Vector eventList, EventPoint event) {
		Line2D edge1 = event.edge;
		Line2D edge2 = event.c_edge;

		for (int i = 0; i < eventList.size(); i++) {
			EventPoint currentEvent = (EventPoint) eventList.elementAt(i);
			Line2D currEdge1 = currentEvent.edge;
			Line2D currEdge2 = currentEvent.c_edge;

			if (currentEvent.type == 3 && currentEvent.x == event.x && currentEvent.y == event.y) {
				if (edge1.getX1() == currEdge1.getX1() && edge1.getY1() == currEdge1.getY1()
						&& edge1.getX2() == currEdge1.getX2() && edge1.getY2() == currEdge1.getY2()
						&& edge2.getX1() == currEdge2.getX1() && edge2.getY1() == currEdge2.getY1()
						&& edge2.getX2() == currEdge2.getX2() && edge2.getY2() == currEdge2.getY2())
					return i;

				if (edge1.getX1() == currEdge2.getX1() && edge1.getY1() == currEdge2.getY1()
						&& edge1.getX2() == currEdge2.getX2() && edge1.getY2() == currEdge2.getY2()
						&& edge2.getX1() == currEdge1.getX1() && edge2.getY1() == currEdge1.getY1()
						&& edge2.getX2() == currEdge1.getX2() && edge2.getY2() == currEdge1.getY2())
					return i;
			}
		}
		return -1;
	}

	private int getEdgeIdx(Vector eventList, EventPoint event) {
		Line2D edge = event.edge;

		for (int i = 0; i < eventList.size(); i++) {
			EventPoint currentEvent = (EventPoint) eventList.elementAt(i);
			Line2D currentEdge = currentEvent.edge;

			if (edge.getX1() == currentEdge.getX1() && edge.getY1() == currentEdge.getY1()
					&& edge.getX2() == currentEdge.getX2() && edge.getY2() == currentEdge.getY2())
				return i;
		}

		return -1;
	}

	private int insertEvent(Vector eventPoints, EventPoint event) {
		if (event.type == 3) {
			int interIdx = getIntersectionIdx(eventPoints, event);
			if (interIdx > -1)
				return interIdx;
		}

		for (int i = 1; i < eventPoints.size(); i++) {
			EventPoint currEvent = (EventPoint) eventPoints.elementAt(i);
			if ((event.x < currEvent.x) || (event.x == currEvent.x && event.type < currEvent.type)) {
				eventPoints.insertElementAt(event, i);
				return i;
			}
		}

		eventPoints.add(event);
		return eventPoints.size() - 1;
	}

	private EventPoint checkIntersection(Vector verticalOrder, int idx1, int idx2, double init_x) {
		if (idx1 < 0 || idx1 >= verticalOrder.size() || idx2 < 0 || idx2 >= verticalOrder.size())
			return null;

		Line2D edge1 = ((EventPoint) verticalOrder.elementAt(idx1)).edge;
		Line2D edge2 = ((EventPoint) verticalOrder.elementAt(idx2)).edge;

		/* Pas d'intersection entre un segment et lui-même */
		if (edge1.getX1() == edge2.getX1() && edge1.getY1() == edge2.getY1() && edge1.getX2() == edge2.getX2()
				&& edge1.getY2() == edge2.getY2())
			return null;

		/* Recherche du point d'intersection */
		//EventPoint newEvent = null;
		double inter_x = 0;
		double inter_y = 0;

		/* Pas de segment vertical */
		if (edge1.getX2() != edge1.getX1() && edge2.getX2() != edge2.getX1()) {
			double m1 = (edge1.getY2() - edge1.getY1()) / (edge1.getX2() - edge1.getX1());
			double b1 = edge1.getY1() - m1 * edge1.getX1();

			double m2 = (edge2.getY2() - edge2.getY1()) / (edge2.getX2() - edge2.getX1());
			double b2 = edge2.getY1() - m2 * edge2.getX1();

			/* Pentes différentes */
			if (m1 != m2) {
				inter_x = (b2 - b1) / (m1 - m2);
				inter_y = m1 * inter_x + b1;
			}

			/* Segements colinéaires */
			else {
				double y1_1 = edge1.getY1() <= edge1.getY2() ? edge1.getY1() : edge1.getY2();
				double y1_2 = edge1.getY1() > edge1.getY2() ? edge1.getY1() : edge1.getY2();
				double y2_1 = edge2.getY1() <= edge2.getY2() ? edge2.getY1() : edge2.getY2();
				double y2_2 = edge2.getY1() > edge2.getY2() ? edge2.getY1() : edge2.getY2();

				/* edge2 est entièrementsur le segment edge1 */
				if ((y2_1 >= y1_1 && y2_1 <= y1_2) && (y2_2 >= y1_1 && y2_2 <= y1_2)) {
					inter_x = edge2.getX1();
					inter_y = edge2.getY1();
				}

				/* edge1 est entièrementsur le segment edge2 */
				if ((y1_1 >= y2_1 && y1_1 <= y2_2) && (y1_2 >= y2_1 && y1_2 <= y2_2)) {
					inter_x = edge1.getX1();
					inter_y = edge1.getY1();
				}

				/* edge2 est partiellement sur le segment edge1 */
				else if ((y2_1 >= y1_1 && y2_1 <= y1_2)) {
					inter_x = edge2.getY1() <= edge2.getY2() ? edge2.getX1() : edge2.getX2();
					inter_y = y2_1;
				}

				/* edge1 est partiellement sur le segment edge2 */
				else if ((y1_1 >= y2_1 && y1_1 <= y2_2)) {
					inter_x = edge1.getY1() <= edge1.getY2() ? edge1.getX1() : edge1.getX2();
					inter_y = y1_1;
				}
			}
		}

		/* edge1 est vertical mais pas edge2 */
		else if (edge1.getX2() == edge1.getX1() && edge2.getX2() != edge2.getX1()) {
			double m2 = (edge2.getY2() - edge2.getY1()) / (edge2.getX2() - edge2.getX1());
			double b2 = edge2.getY1() - m2 * edge2.getX1();

			inter_x = edge1.getX1();
			inter_y = m2 * inter_x + b2;
		}

		/* edge2 est vertical mais pas edge1 */
		else if (edge1.getX2() != edge1.getX1() && edge2.getX2() == edge2.getX1()) {
			double m1 = (edge1.getY2() - edge1.getY1()) / (edge1.getX2() - edge1.getX1());
			double b1 = edge1.getY1() - m1 * edge1.getX1();

			inter_x = edge2.getX1();
			inter_y = m1 * inter_x + b1;
		}

		/* edge1 et edge2 sont verticaux */
		else {
			/* Valeur de x différente : pas d'intersection */
			if (edge1.getX1() != edge2.getX1())
				return null;

			/* Segments colinéaires */
			else {
				double y1_1 = edge1.getY1() <= edge1.getY2() ? edge1.getY1() : edge1.getY2();
				double y1_2 = edge1.getY1() > edge1.getY2() ? edge1.getY1() : edge1.getY2();
				double y2_1 = edge2.getY1() <= edge2.getY2() ? edge2.getY1() : edge2.getY2();
				double y2_2 = edge2.getY1() > edge2.getY2() ? edge2.getY1() : edge2.getY2();

				inter_x = edge1.getX1();

				/* edge2 est entièrementsur le segment edge1 */
				if ((y2_1 >= y1_1 && y2_1 <= y1_2) && (y2_2 >= y1_1 && y2_2 <= y1_2))
					inter_y = y2_1;

				/* edge1 est entièrementsur le segment edge2 */
				if ((y1_1 >= y2_1 && y1_1 <= y2_2) && (y1_2 >= y2_1 && y1_2 <= y2_2))
					inter_y = y1_1;

				/* edge2 est partiellement sur le segment edge1 */
				else if ((y2_1 >= y1_1 && y2_1 <= y1_2))
					inter_y = y2_1;

				/* edge1 est partiellement sur le segment edge2 */
				else if ((y1_1 >= y2_1 && y1_1 <= y2_2))
					inter_y = y1_1;
			}
		}

		inter_x = Math.round(inter_x * 1000000.0) / 1000000.0;
		inter_y = Math.round(inter_y * 1000000.0) / 1000000.0;

		if (isOnBothEndPoints(inter_x, inter_y, edge1, edge2)) {
			return null;
		}

		/*
		 * L'intersection se trouve sur le segment de edge1 et sur le segment de edge2 et apres
		 * init_x
		 */
		if (isOnEdge(inter_x, inter_y, edge1) && isOnEdge(inter_x, inter_y, edge2) && inter_x >= init_x) {
			int weight = 1;
			if (isOnEdge(edge1.getX1(), edge1.getY1(), edge2) || isOnEdge(edge1.getX2(), edge1.getY2(), edge2)
					|| isOnEdge(edge2.getX1(), edge2.getY1(), edge1) || isOnEdge(edge2.getX2(), edge2.getY2(), edge1)) {
				weight += 10000;
			}

			//if(edge1.getX1()==edge1.getX2() && !(edge2.getX1()==edge2.getX2()))
			//  weight /= 2;

			//if(edge2.getX1()==edge2.getX2() && !(edge1.getX1()==edge1.getX2()))
			//  weight /= 2;

			///newEvent =
			return new EventPoint(edge1, edge2, inter_x, inter_y, weight);
		}

		return null; //newEvent;
	}

	private boolean isOnEdge(double x, double y, Line2D edge) {
		x = Math.round(x * 1000000.0) / 1000000.0;
		y = Math.round(y * 1000000.0) / 1000000.0;

		if (edge.getX1() == edge.getX2()) {
			if (x != edge.getX1())
				return false;

			if (!((y >= edge.getY1() && y <= edge.getY2()) || (y >= edge.getY2() && y <= edge.getY1())))
				return false;

			return true;
		}

		//if( !( (x >= edge.getX1() && x <= edge.getX2()) || (x >= edge.getX2() && x <= edge.getX1()) ) )
		//  return false;

		//if( !( (y >= edge.getY1() && y <= edge.getY2()) || (y >= edge.getY2() && y <= edge.getY1()) ) )
		//  return false;

		double m = (edge.getY2() - edge.getY1()) / (edge.getX2() - edge.getX1());
		double b = edge.getY1() - m * edge.getX1();

		double inter_y = Math.round((m * x + b) * 1000000.0) / 1000000.0;

		if (y >= inter_y - 0.00001 && y <= inter_y + 0.00001) {
			if (!((x >= edge.getX1() && x <= edge.getX2()) || (x >= edge.getX2() && x <= edge.getX1())))
				return false;

			if (!((y >= edge.getY1() && y <= edge.getY2()) || (y >= edge.getY2() && y <= edge.getY1())))
				return false;

			return true;
		}

		return false;
	}

	private boolean isOnBothEndPoints(double x, double y, Line2D edge1, Line2D edge2) {
		if (x == edge1.getX1() && y == edge1.getY1()) {
			if (x == edge2.getX1() && y == edge2.getY1())
				return true;
			else if (x == edge2.getX2() && y == edge2.getY2())
				return true;
			return false;
		}

		else if (x == edge1.getX2() && y == edge1.getY2()) {
			if (x == edge2.getX1() && y == edge2.getY1())
				return true;
			else if (x == edge2.getX2() && y == edge2.getY2())
				return true;
			return false;
		}

		return false;
	}

	private int getCrossingCount(Vector edges) {
		int interCount = 0;

		Vector eventPoints = new Vector();
		for (int i = 0; i < edges.size(); i++) {
			Line2D currEdge = (Line2D) edges.elementAt(i);
			eventPoints.add(new EventPoint(currEdge, 1)); //Start point
			eventPoints.add(new EventPoint(currEdge, 2)); //End point
		}
		sortEventPoints(eventPoints, 0, eventPoints.size() - 1);

		Vector verticalOrder = new Vector();

		while (eventPoints.size() > 0) {
			double x = ((EventPoint) eventPoints.elementAt(0)).x;

			while (eventPoints.size() > 0 && ((EventPoint) eventPoints.elementAt(0)).x == x) {
				EventPoint currentEvent = (EventPoint) eventPoints.elementAt(0);

				/* Événement : retait d'un segment */
				if (currentEvent.type == 2) {
					int suppIdx = getEdgeIdx(verticalOrder, currentEvent);

					if (suppIdx > -1) {
						verticalOrder.removeElementAt(suppIdx);
						EventPoint intersection = checkIntersection(verticalOrder, suppIdx - 1, suppIdx, x);
						if (intersection != null && getIntersectionIdx(eventPoints, intersection) == -1) {
							insertEvent(eventPoints, intersection);
							interCount += intersection.weight; //++;
						}
					}
				}

				/* Événement : inversion des positions de 2 segments qui s'intersectent */
				else if (currentEvent.type == 3) {
					int idx1 = findVerticalPosition(verticalOrder, currentEvent.edge);
					int idx2 = findVerticalPosition(verticalOrder, currentEvent.c_edge);

					if (idx1 > -1 && idx2 > -1 && idx1 < verticalOrder.size() && idx2 < verticalOrder.size()) {
						EventPoint event1 = (EventPoint) verticalOrder.elementAt(idx1);
						EventPoint event2 = (EventPoint) verticalOrder.elementAt(idx2);
						verticalOrder.setElementAt(event1, idx2);
						verticalOrder.setElementAt(event2, idx1);

						EventPoint intersection;
						if (idx1 < idx2)
							intersection = checkIntersection(verticalOrder, idx1 - 1, idx1, x + 0.00001);
						else
							intersection = checkIntersection(verticalOrder, idx1, idx1 + 1, x + 0.00001);

						if (intersection != null && getIntersectionIdx(eventPoints, intersection) == -1) {
							insertEvent(eventPoints, intersection);
							interCount += intersection.weight; //++;
						}

						if (idx1 < idx2)
							intersection = checkIntersection(verticalOrder, idx2, idx2 + 1, x + 0.00001);
						else
							intersection = checkIntersection(verticalOrder, idx2 - 1, idx2, x + 0.00001);

						if (intersection != null && getIntersectionIdx(eventPoints, intersection) == -1) {
							insertEvent(eventPoints, intersection);
							interCount += intersection.weight; //++;
						}
					}
				}

				/* Événement : ajout d'un segment */
				else if (currentEvent.type == 1) {
					int insertIdx = insertVerticalPoint(verticalOrder, currentEvent);

					EventPoint intersection = checkIntersection(verticalOrder, insertIdx - 1, insertIdx, x);
					if (intersection != null && getIntersectionIdx(eventPoints, intersection) == -1) {
						insertEvent(eventPoints, intersection);
						interCount += intersection.weight; //++;
					}

					intersection = checkIntersection(verticalOrder, insertIdx, insertIdx + 1, x);
					if (intersection != null && getIntersectionIdx(eventPoints, intersection) == -1) {
						insertEvent(eventPoints, intersection);
						interCount += intersection.weight; //++;
					}
				}

				eventPoints.removeElementAt(0);
			}
		}

		return interCount;
	}

	private int getCrossingCount() {
		Vector edges = new Vector();

		double min_x = 1000.0;
		double max_x = -1000.0;
		for (int i = 0; i < positionsByLevel.size() - 1; i++) {
			Vector currLevel = (Vector) positionsByLevel.elementAt(i);
			for (int j = 0; j < currLevel.size(); j++) {
				ConceptPosition currPos = (ConceptPosition) currLevel.elementAt(j);
				Vector childrenPositions = getChildrenPositions(currPos, i);
				for (int k = 0; k < childrenPositions.size(); k++) {
					ConceptPosition childPos = (ConceptPosition) childrenPositions.elementAt(k);
					Line2D newEdge;
					if (currPos.getRelX() <= childPos.getRelX())
						newEdge = new Line2D.Double(currPos.getRelX(), currPos.getRelY(), childPos.getRelX(),
								childPos.getRelY());
					else
						newEdge = new Line2D.Double(childPos.getRelX(), childPos.getRelY(), currPos.getRelX(),
								currPos.getRelY());

					if (newEdge.getX1() < min_x)
						min_x = newEdge.getX1();
					if (newEdge.getX1() > max_x)
						max_x = newEdge.getX1();
					if (newEdge.getX2() < min_x)
						min_x = newEdge.getX2();
					if (newEdge.getX2() > max_x)
						max_x = newEdge.getX2();

					edges.add(newEdge);
				}
			}
		}

		return getCrossingCount(edges);
	}

	private void sortEventPoints(Vector eventPoints, int start, int end) {
		for (int i = start; i < end; i++) {
			for (int j = i + 1; j <= end; j++) {
				EventPoint event1 = (EventPoint) eventPoints.elementAt(i);
				EventPoint event2 = (EventPoint) eventPoints.elementAt(j);
				if (event2.x < event1.x || (event2.x == event1.x && event2.y < event1.y)) {
					eventPoints.setElementAt(event2, i);
					eventPoints.setElementAt(event1, j);
				}
			}
		}
		//if(start < end){
		//  EventPoint pivotEvent = (EventPoint)eventPoints.elementAt(end);
		//  int idx = start-1;

		//  for(int i=start; i<end; i++){
		//    EventPoint currEvent = (EventPoint)eventPoints.elementAt(i);
		//    if( (currEvent.x < pivotEvent.x) || (currEvent.x == pivotEvent.x && currEvent.type <= pivotEvent.type) ){
		//      idx++;
		//      eventPoints.setElementAt((EventPoint)eventPoints.elementAt(idx), i);
		//      eventPoints.setElementAt(currEvent, idx);
		//    }
		//  }

		//  idx++;
		//  eventPoints.setElementAt((EventPoint)eventPoints.elementAt(idx), end);
		//  eventPoints.setElementAt(pivotEvent, idx);

		//  sortEventPoints(eventPoints, start, idx-1);
		//  sortEventPoints(eventPoints, idx+1, end);
		//}
	}

	/**
	 * Trie les intentions d'un vecteur donné en ordre alphabétique
	 * @param conceptIntentList Un Vector contenant la liste des identifiants des concepts à trier
	 * @return Le Vector contenant la liste des intentions triées
	 */
	private Vector sortConceptIntents(Vector conceptIntentList) {
		/* Recherche de l'ordre des intentions des noeuds identifiés dans le vecteur en entrée */
		Vector sortedLevelIntents = new Vector();
		for (int i = 0; i < conceptIntentList.size(); i++) {
			BasicSet nodeIntent = (BasicSet) conceptIntentList.elementAt(i);

			/* Tri par insertion */
			boolean found = false;
			for (int j = 0; (j < sortedLevelIntents.size() && !found); j++) {
				BasicSet currentIntent = (BasicSet) sortedLevelIntents.elementAt(j);
				if (compareIntents(nodeIntent, currentIntent) < 0) {
					found = true;
					sortedLevelIntents.insertElementAt(nodeIntent, j);
				}
			}

			/* Ajout à la fin si la toutes les intentions déjà triées sont "plus petites" */
			if (!found)
				sortedLevelIntents.add(nodeIntent);
		}

		return sortedLevelIntents;
	}

	/**
	 * Compare deux intentions en fonction de l'ordre alphabétique des attributs qu'il contiennent
	 * @param intent1 Un BasicSet contenant une intension à comparer
	 * @param intent2 Un BasicSet contenant une intension à comparer
	 * @return Le int contenant le résultat de la comparasion, soit 0 pour deux intentions
	 *         identiques, une valeur négative si intent1 < intent2 et une valeur positive si
	 *         intent1 > intent2.
	 */
	private int compareIntents(BasicSet intent1, BasicSet intent2) {
		Iterator it1 = intent1.iterator();
		Iterator it2 = intent2.iterator();

		while (it1.hasNext() && it2.hasNext()) {
			String att1 = (String) it1.next();
			String att2 = (String) it2.next();

			int compare = att1.compareTo(att2);
			if (compare < 0)
				return -1;

			if (compare > 0)
				return 1;
		}

		/* Une chaîne préfixée est "plus grande" que son préfixe */
		if (it1.hasNext() && !it2.hasNext())
			return 1;

		/* Un préfixe est "plus petit" que la chaîne préfixée */
		if (it2.hasNext() && !it1.hasNext())
			return -1;

		return 0;
	}

	/**
	 * Crée un vecteur qui détermine le déplacement à effectuer à la position naturelle des concepts
	 * du premier niveau après la source du treillis. La position naturelle est simplement la
	 * position dans le vecteur trié des concepts. Toutefois, pour afficher correctement le treillis
	 * dans sa forme cubique, les concepts du premier niveau doivent être positionnés à des endroits
	 * précis de l'axe des x pour permettre d'afficher le(s) cube(s) des positions précédentes et de
	 * la position courante.
	 * @param conceptCount Un int contenant le nombre de concepts du premier niveau après la source
	 *        du treillis
	 * @return Le Vector contenant la liste des déplacements à effectuer aux concepts, selon l'ordre
	 *         des concepts du premier niveau après la source du treillis
	 */
	private Vector calcFirstLevelDelta(int conceptCount) {
		Vector delta = new Vector();

		/*
		 * Les 3 premiers concepts font partie du premier cube, donc ils ne subissent aucun
		 * déplacement
		 */
		delta.add(new Integer(0));
		delta.add(new Integer(0));
		delta.add(new Integer(0));

		/*
		 * Les concepts situés après les 3 premiers doivent tenir compte de l'espace occupé par les
		 * cubes qui leur appartiennent et qui ceux qui les précèdent
		 */
		int totalDelta = 0;
		for (int i = 3; i < conceptCount; i++) {
			delta.add(new Integer(totalDelta + 1));
			totalDelta += ((i - 2) * 3 - 1);
		}

		return delta;
	}

	/**
	 * Assigne une position en x pour une intention donnée en fonction des positions assignées aux
	 * concepts du premier niveau après la source du treillis
	 * @param firstLevelIntents Un Vector contenant la liste ordonnée des intentions du premier
	 *        niveau après la source du treillis
	 * @param deltaX Un Vector contenant la liste des déplacement à affecter à la position naturelle
	 *        des concepts du premier niveau après la source du treillis
	 * @param intent Un Intent contenant l'intention du concept pour lequel la position est
	 *        recherchée
	 * @return Le int contenant la position assignée à l'intention donnée
	 */
	private int findCubicIntentPositionX(Vector firstLevelIntents, Vector deltaX, BasicSet intent) {
		int position = 0;
		for (int i = 0; i < firstLevelIntents.size(); i++) {
			BasicSet currentIntent = (BasicSet) firstLevelIntents.elementAt(i);
			if (intent.containsAll(currentIntent)) {
				position += i;
				position += ((Integer) deltaX.elementAt(i)).intValue();
			}
		}

		return position;
	}

	/**
	 * Assigne une position en x pour une intention donnée en fonction des positions assignées aux
	 * attributs du contexte
	 * @param attributes Un Vector contenant la liste ordonnée des attributs du contexte
	 * @param intent Un Intent contenant l'intention du concept pour lequel la position est
	 *        recherchée
	 * @return Le double contenant la position assignée à l'intention donnée
	 */
	private static double findAdditiveIntentPositionX(Vector attributes, BasicSet intent) {
		double position = 0;
		double startX = -(attributes.size() - 1.0) / 2.0;

		for (int i = 0; i < attributes.size(); i++) {
			String att = (String) attributes.elementAt(i);
			if (intent.contains(att))
				position += (startX + i);
		}

		return position;
	}

	@SuppressWarnings("unused") //$NON-NLS-1$
	private int factorial(int n) {
		int res = 1;
		for (int i = 1; i <= n; i++)
			res *= i;
		return res;
	}

	public Vector findPermutations(Vector elements) {
		boolean[] isInPermutation = new boolean[elements.size()];
		for (int i = 0; i < elements.size(); i++)
			isInPermutation[i] = false;
		Object[] permutation = new Object[elements.size()];
		for (int i = 0; i < elements.size(); i++)
			permutation[i] = ""; //$NON-NLS-1$
		Vector permutations = permutation(elements, isInPermutation, permutation, 0);
		return permutations;
	}

	public Vector permutation(Vector elements, boolean[] isInPermutation, Object[] permutation, int pos) {
		Vector allPermutations = new Vector();

		if (pos == elements.size()) {
			Vector newPermutation = new Vector();
			for (int i = 0; i < elements.size(); i++)
				newPermutation.add(permutation[i]);
			allPermutations.add(newPermutation);
		}

		else {
			for (int i = 0; i < elements.size(); i++) {
				if (!isInPermutation[i]) {
					isInPermutation[i] = true;
					permutation[pos] = elements.elementAt(i);
					allPermutations.addAll(permutation(elements, isInPermutation, permutation, pos + 1));
					isInPermutation[i] = false;
				}
			}
		}

		return allPermutations;
	}

	@Override
	public String toString() {
		String str = ""; //$NON-NLS-1$

		for (int i = 0; i < conceptPositions.size(); i++) {
			ConceptPosition pos = conceptPositions.elementAt(i);
			str = str + pos.getIntent().toString() + " (" + pos.getRelX() + ", " + pos.getRelY() + ")\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return str;
	}

	private class EventPoint {

		private int type;
		private Line2D edge;
		private Line2D c_edge;
		private double x;
		private double y;
		private int weight;

		public EventPoint(Line2D e, int t) {
			edge = e;
			type = t;

			double x1 = edge.getX1();
			double y1 = edge.getY1();
			double x2 = edge.getX2();
			double y2 = edge.getY2();

			/* Start point */
			if (type == 1) {
				if (x1 < x2) {
					x = x1;
					y = y1;
				} else if (x2 < x1) {
					x = x2;
					y = y2;
				} else if (y1 < y2) {
					x = x1;
					y = y1;
				} else {
					x = x2;
					y = y2;
				}
			}

			/* End point */
			else if (type == 2) {
				if (x1 > x2) {
					x = x1;
					y = y1;
				} else if (x2 > x1) {
					x = x2;
					y = y2;
				} else if (y1 > y2) {
					x = x1;
					y = y1;
				} else {
					x = x2;
					y = y2;
				}
			}
		}

		public EventPoint(Line2D e1, Line2D e2, double inter_x, double inter_y, int w) {
			edge = e1;
			c_edge = e2;
			type = 3; /* Intersection */
			weight = w;
			x = inter_x;
			y = inter_y;
		}
	}

}