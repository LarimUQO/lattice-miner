package fca.core.lattice.operator.projection;

import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

import fca.core.lattice.ConceptLattice;
import fca.core.lattice.FormalConcept;
import fca.core.util.BasicSet;

/**
 * Classe contenant les opérations sur les attributs d'un treillis
 * @author Inconnu
 */
public class BasicAttributeProjection extends BasicProjection {

	/**
	 * Constructeur de projections simples sur les attributs d'un treillis
	 * @param data le treillis sur lequel on va faire les projections
	 */
	public BasicAttributeProjection(ConceptLattice data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.lattice.conceptual.operators.Operator#perform(java.lang.Object)
	 */
	@Override
	public ConceptLattice perform(BasicSet entry) {
		purifie(lowerNode);
		FormalConcept finalNode = performRec(lowerNode, entry);
		FormalConcept topNode = chercheSommet(finalNode);
		result = new ConceptLattice(topNode, "ProjectionOn" + entry.toString()); //$NON-NLS-1$
		return result;
	}

	/**
	 * Algorithme recursif permettant de construire le trellis. Cette algorithme parcours
	 * récursivement les noeuds et lors du backtrack, marque les noeuds indispensable et les
	 * ajoutent dans la map ensembleNoeuds. La variable pile permet de conserver les parents qui
	 * seront lies avec le fils
	 */
	private FormalConcept performRec(FormalConcept n, BasicSet intent) {

		// On creer un tableau contenant les nodes parents
		Stack<FormalConcept> pile = new Stack<FormalConcept>();

		// Permet l'elagage en fonction des inclusions d'intent
		Vector<BasicSet> intentVisites = new Vector<BasicSet>();

		BasicSet i1 = n.getIntent();
		i1 = i1.intersection(intent);

		Vector<FormalConcept> parents = n.getParents();
		Iterator<FormalConcept> itParents = parents.iterator();
		boolean elagage = false;

		// On trie les noeuds en creeant des objets Paires
		TreeSet<Paire> tri = new TreeSet<Paire>();

		while (itParents.hasNext()) {
			tri.add(new Paire(itParents.next(), intent));
		}

		Iterator<Paire> itPaire = tri.iterator();
		while (itPaire.hasNext()) {
			FormalConcept nodei2 = ((itPaire.next()).getNoeud());
			BasicSet i2 = nodei2.getIntent();
			i2 = i2.intersection(intent);

			// elagage, comparaison de l'inclusion
			Iterator<BasicSet> it2 = intentVisites.iterator();
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
			}// fin elagage //code pas beau...

			if (i2.equals(i1)) {
				FormalConcept nx;
				if (nodei2.hasBeenVisited())
					nx = nodes.get(nodei2);
				else
					nx = performRec(nodei2, intent);
				n.setVisited(true);
				nodes.put(n, nx);
				return nx;
			}
			if (nodei2.hasBeenVisited()) {
				pile.push(nodes.get(nodei2));
				intentVisites.add(i2);
			} else {
				pile.push(performRec(nodei2, intent));
				intentVisites.add(i2);
			}
		}

		FormalConcept nouveauNoeud = new FormalConcept((BasicSet) n.getExtent().clone(), i1);
		n.setVisited(true);
		nodes.put(n, nouveauNoeud);

		while (!pile.empty()) {
			FormalConcept noeud = pile.pop();
			nouveauNoeud.addParent(noeud);
			noeud.addChild(nouveauNoeud);
		}
		return nouveauNoeud;
	}

	/**
	 * Cette classe permet de ranger les differents élements dans un ordre precis : Les noeuds
	 * possédant les intent ayant le plus de relations avec les intents choisis sont placés devants
	 */
	private class Paire implements Comparable<Paire> {

		/** Contient le noeud */
		private FormalConcept noeud;

		/** Contient le nombre d'attributs interressants */
		private int nbIntentInt;

		/** Taille de l'intention avec laquelle comparer */
		private int taille;

		/**
		 * Constructeur d'une paire
		 * @param noeud le noeud
		 * @param intent son intention
		 */
		public Paire(FormalConcept noeud, BasicSet intent) {
			this.noeud = noeud;
			nbIntentInt = noeud.getIntent().intersection(intent).size();
			taille = noeud.getIntent().size();
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Paire b) {
			if (nbIntentInt == b.nbIntentInt) {
				if (taille < b.taille)
					return -1;
				else
					return 1;
			}

			if (nbIntentInt > b.nbIntentInt)
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
