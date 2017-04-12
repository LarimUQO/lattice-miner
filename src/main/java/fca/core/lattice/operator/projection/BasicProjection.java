package fca.core.lattice.operator.projection;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import fca.core.lattice.ConceptLattice;
import fca.core.lattice.FormalConcept;
import fca.core.lattice.operator.Operator;
import fca.core.util.BasicSet;

/**
 * Classe contenant les opérations sur les projections de treillis. Voir les classes
 * AttributeProjection et ObjectProjection pour plus de détails C'est une implémentation basique
 * (non optimisée) de projection/selection
 * @author Inconnu
 * @author Ludovic Thomas (reprise de la structure via {@link Operator})
 */

public abstract class BasicProjection extends Operator<ConceptLattice, BasicSet, ConceptLattice> {
	
	protected FormalConcept upperNode;
	
	protected FormalConcept lowerNode;
	/**
	 * fca.lattice.conceptual.FormalConcept"
	 */
	protected Hashtable<FormalConcept, FormalConcept> nodes;
	
	/**
	 * Constructeur d'une projection basique
	 * @param data le treillis dans un veut faire une projection
	 */
	public BasicProjection(ConceptLattice data) {
		super(data, "BasicProjection"); //$NON-NLS-1$
		
		upperNode = data.getTopConcept();
		lowerNode = data.getBottomConcept();
		nodes = new Hashtable<FormalConcept, FormalConcept>();
	}
	
	/**
	 * Permet de recuperer le sommet d'un treillis en le parcourant par le bas
	 */
	protected static FormalConcept chercheSommet(FormalConcept n) {
		Vector<FormalConcept> parents = n.getParents();
		Iterator<FormalConcept> it = parents.iterator();
		if (it.hasNext()) {
			return chercheSommet(it.next());
		}
		return n;
	}
	
	/**
	 * Permet de mettre a <code>false</code> les attributs Visited. La fonction parcours le
	 * treillis du bas vers le haut. Pour un soucis d'efficacité, on ne parcours que les noeuds qui
	 * sont marqués
	 */
	protected static void purifie(FormalConcept n) {
		if (n.hasBeenVisited()) {
			Vector<FormalConcept> parents = n.getParents();
			Iterator<FormalConcept> it = parents.iterator();
			while (it.hasNext()) {
				purifie(it.next());
			}
			n.setVisited(false);
		}
	}
	
}
