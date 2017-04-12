package fca.core.lattice.operator.projection;

import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

import fca.core.lattice.ConceptLattice;
import fca.core.lattice.FormalConcept;
import fca.core.util.BasicSet;

/**
 * Classe contenant les opérations sur les objets d'un treillis
 * @author Inconnu
 */
public class BasicObjectProjection extends BasicProjection {

	/**
	 * Constructeur de projections simples sur les objets d'un treillis
	 * @param data le treillis sur lequel on va faire les projections
	 */
	public BasicObjectProjection(ConceptLattice data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.lattice.conceptual.operators.Operator#perform(java.lang.Object)
	 */
	@Override
	public ConceptLattice perform(BasicSet entry) {
		purifie(lowerNode);
		FormalConcept topNode = performRec(upperNode, entry);
		result = new ConceptLattice(topNode, "ProjectionOn" + entry.toString()); //$NON-NLS-1$
		return result;
	}

	/**
	 * Algorithme recursif permettant de construire le trellis Cette algorithme parcours
	 * récursivement les noeuds et lors du backtrack, marque les noeuds indispensable et les
	 * ajoutent dans la map ensembleNoeuds. La variable pile permet de conserver les parents qui
	 * seront lies avec le fils
	 */
	private FormalConcept performRec(FormalConcept n, BasicSet extent) {
		//on creer un tableau contenant les nodes parents
		Stack<FormalConcept> pile = new Stack<FormalConcept>();
		//permet l'elagage en fonction des inclusions d'intent
		Vector<BasicSet> extentVisites = new Vector<BasicSet>();

		BasicSet i1 = n.getExtent();
		i1 = i1.intersection(extent);

		Vector<FormalConcept> enfants = n.getChildren();
		Iterator<FormalConcept> itEnfants = enfants.iterator();
		boolean elagage = false;

		//on trie les noeuds en creeant des objets Paires
		TreeSet<Paire> tri = new TreeSet<Paire>();
		while (itEnfants.hasNext()) {
			tri.add(new Paire(itEnfants.next(), extent));
		}

		Iterator<Paire> itPaire = tri.iterator();
		while (itPaire.hasNext()) {

			FormalConcept nodei2 = ((itPaire.next()).getNoeud());
			BasicSet i2 = nodei2.getExtent();
			i2 = i2.intersection(extent);

			//elagage
			Iterator<BasicSet> it2 = extentVisites.iterator();
			while (it2.hasNext()) {
				BasicSet i = it2.next();
				if (i.union(i2).size() <= i.size()) {
					elagage = true;
					break;
				}
			}
			if (elagage == true) {
				elagage = false;
				continue;
			}

			if (i2.equals(i1)) {
				FormalConcept nx;
				if (nodei2.hasBeenVisited())
					nx = nodes.get(nodei2);
				else
					nx = performRec(nodei2, extent);
				n.setVisited(true);
				nodes.put(n, nx);
				return nx;
			}

			if (nodei2.hasBeenVisited()) {
				pile.push(nodes.get(nodei2));
				extentVisites.add(i2);
			} else {
				pile.push(performRec(nodei2, extent));
				extentVisites.add(i2);
			}
		}

		FormalConcept nouveauNoeud = new FormalConcept(i1, (BasicSet) n.getIntent().clone());
		n.setVisited(true);
		nodes.put(n, nouveauNoeud);

		while (!pile.empty()) {
			FormalConcept noeud = pile.pop();
			nouveauNoeud.addChild(noeud);
			noeud.addParent(nouveauNoeud);
		}
		return nouveauNoeud;
	}

	/**
	 * Cette classe permet de ranger les differents élements dans un ordre precis. Les noeuds
	 * possédant les extents ayant le plus de relations avec les extents choisis sont placés devants
	 */
	private class Paire implements Comparable<Paire> {

		/** Contient le noeud */
		private FormalConcept noeud;

		/** Contient le nombre d'attributs interressants */
		private int nbExtentInt;

		/**
		 * Constructeur d'une paire
		 * @param noeud le noeud
		 * @param extent son extension
		 */
		public Paire(FormalConcept noeud, BasicSet extent) {
			this.noeud = noeud;
			nbExtentInt = noeud.getExtent().intersection(extent).size();
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Paire b) {
			if (nbExtentInt > b.nbExtentInt)
				return -1;
			else
				return 1;
		}

		/**
		 */
		/**
		 * @return le noeud de la paire
		 */
		public FormalConcept getNoeud() {
			return noeud;
		}
	}

}
