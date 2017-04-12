package fca.core.lattice.operator.search;

import java.util.Stack;

import fca.core.lattice.NestedConcept;
import fca.core.lattice.NestedLattice;
import fca.core.lattice.operator.Operator;
import fca.core.util.BasicSet;

/**
 * Opérateur de recherche approximative pour l'extension pour un treillis imbriqué
 * @author Ludovic Thomas
 * @version 1.0
 */
public class SearchApproximateExtentNested extends Operator<NestedLattice, BasicSet, Stack<NestedConcept>> {

	/**
	 * Constructeur de l'opérateur de recherche approximative pour l'extension pour un treillis
	 * imbriqué
	 * @param data le treillis imbriqué où l'on doit faire la recherche
	 */
	public SearchApproximateExtentNested(NestedLattice data) {
		super(data, "SearchApproximateExtentNested"); //$NON-NLS-1$
		result = new Stack<NestedConcept>();
	}

	/*
	 * (non-Javadoc)
	 * @see fca.lattice.conceptual.operators.Operator#perform(java.lang.Object)
	 */
	@Override
	public Stack<NestedConcept> perform(BasicSet extent) {

		// Recupere les objets du supremum qui sont tous ceux du lattice
		BasicSet latticeObjects = data.getTopNestedConcept().getExtent();
		BasicSet extentIntent = extent.intersection(latticeObjects);

		// Il y a forcement TOUS les objets dans le treillis actuel sinon c'est une erreur
		NestedConcept conceptInter = performRec(extentIntent, data.getTopNestedConcept());
		if (conceptInter != null && conceptInter.getExtent().equals(extent)) {
			result.push(conceptInter);
		} else if (conceptInter != null && conceptInter.getInternalNestedLattice() != null) {
			result.push(conceptInter);
			SearchApproximateExtentNested searchConceptInter = new SearchApproximateExtentNested(
					conceptInter.getInternalNestedLattice());
			result.addAll(searchConceptInter.perform(extentIntent));
		}

		if (!result.lastElement().isFinalConcept())
			return new Stack<NestedConcept>();

		return result;
	}

	/**
	 * Recherche recursive pour une extension donnée et un concept
	 * @param extent l'extension recherchée
	 * @param fc le concept courant dans la recherche recursive
	 * @return le concept trouvé
	 */
	private NestedConcept performRec(BasicSet extent, NestedConcept fc) {
		NestedConcept result = fc;
		for (NestedConcept children : fc.getChildren()) {
			BasicSet extentChildren = children.getExtent();
			if ((extent.size() <= extentChildren.size()) && (extentChildren.isIncluding(extent))) {
				result = performRec(extent, children);
			}
		}
		return result;
	}

}
