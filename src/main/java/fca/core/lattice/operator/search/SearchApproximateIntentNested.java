package fca.core.lattice.operator.search;

import java.util.Stack;

import fca.core.lattice.NestedConcept;
import fca.core.lattice.NestedLattice;
import fca.core.lattice.operator.Operator;
import fca.core.util.BasicSet;

/**
 * Opérateur de recherche approximative pour l'intention pour un treillis imbriqué
 * @author Ludovic Thomas
 * @version 1.0
 */
public class SearchApproximateIntentNested extends Operator<NestedLattice, BasicSet, Stack<NestedConcept>> {
	
	/**
	 * Constructeur de l'opérateur de recherche approximative pour l'intention pour un treillis
	 * imbriqué
	 * @param data le treillis imbriqué où l'on doit faire la recherche
	 */
	public SearchApproximateIntentNested(NestedLattice data) {
		super(data, "SearchApproximateIntentNested"); //$NON-NLS-1$
		result = new Stack<NestedConcept>();
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.lattice.conceptual.operators.Operator#perform(java.lang.Object)
	 */
	@Override
	public Stack<NestedConcept> perform(BasicSet intent) {
		
		// Recupere les attributs de l'infimum qui sont tous ceux du lattice
		BasicSet latticeAttributes = data.getBottomNestedConcept().getIntent();
		BasicSet interIntent = intent.intersection(latticeAttributes);
		
		// S'il n'y a pas d'intersection, seul le suprémum est à considérer
		if (interIntent.size() == 0) {
			// Supremum a regarder que si son intention n'est pas vide et qu'il contient un treillis
			NestedConcept supremum = data.getTopNestedConcept();
			if (supremum.getConcept().getIntent().size() == 0 && supremum.getInternalNestedLattice() != null) {
				result.push(supremum);
				SearchApproximateIntentNested searchSupremum = new SearchApproximateIntentNested(
						supremum.getInternalNestedLattice());
				result.addAll(searchSupremum.perform(intent));
			}
		}
		// Tous les attributs sont dans l'intersection, recherche dans le treillis actuel
		else if (interIntent.size() == intent.size()) {
			result.push(performRec(intent, data.getBottomNestedConcept()));
		}
		// Certains attributs ne sont pas dans l'intersecton, recherche dans les niveaux internes
		else if (interIntent.size() < intent.size()) {
			NestedConcept conceptInter = performRec(interIntent, data.getBottomNestedConcept());
			// ConceptInter a regarder que si le concept existe et qu'il contient un treillis
			if (conceptInter != null && conceptInter.getInternalNestedLattice() != null) {
				result.push(conceptInter);
				SearchApproximateIntentNested searchConceptInter = new SearchApproximateIntentNested(
						conceptInter.getInternalNestedLattice());
				result.addAll(searchConceptInter.perform(intent.difference(interIntent)));
			}
		}
		
		if (!result.lastElement().isFinalConcept())
			return new Stack<NestedConcept>();
		
		return result;
	}
	
	/**
	 * Recherche recursive pour une extension donnée et un concept
	 * @param intent l'intention recherchée
	 * @param fc le concept courant dans la recherche recursive
	 * @return le concept trouvé
	 */
	private NestedConcept performRec(BasicSet intent, NestedConcept fc) {
		NestedConcept result = fc;
		for (NestedConcept parent : fc.getParents()) {
			BasicSet intentParent = parent.getIntent();
			if ((intent.size() <= intentParent.size()) && (intentParent.isIncluding(intent))) {
				result = performRec(intent, parent);
			}
		}
		return result;
	}
	
}
