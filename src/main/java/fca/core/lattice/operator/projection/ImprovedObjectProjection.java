package fca.core.lattice.operator.projection;

import java.util.Vector;

import fca.core.lattice.ConceptLattice;
import fca.core.lattice.FormalConcept;
import fca.core.util.BasicSet;

/**
 * Opération de projection sur les objets d'un treillis
 * @author Geneviève Roberge
 * @author Ludovic Thomas
 */
public class ImprovedObjectProjection extends ImprovedProjection {

	/**
	 * Constructeur d'une projection optimisée sur les objets
	 * @param data le treillis dans un veut faire une projection
	 */
	public ImprovedObjectProjection(ConceptLattice data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.lattice.conceptual.operators.Operator#perform(java.lang.Object)
	 */
	@Override
	public ConceptLattice perform(BasicSet entry) {
		// Initialisation des concepts : aucun n'a encore été visité
		for (FormalConcept concept : data.getConcepts())
			concept.setVisited(false);

		// Execute la selection sur les objets
		performRec(data.getTopConcept(), entry);
		result = new ConceptLattice(findTopConcept(), "ProjectionOn" + entry.toString()); //$NON-NLS-1$

		return result;
	}

	private FormalConcept performRec(FormalConcept concept, BasicSet extent) {

		if (concept.hasBeenVisited())
			return getConcept(concept.getExtent().intersection(extent), concept.getIntent());

		BasicSet conceptExtent = concept.getExtent().intersection(extent);
		FormalConcept newConcept = new FormalConcept(conceptExtent, concept.getIntent());

		Vector<FormalConcept> children = sortConcepts(concept.getChildren());
		children = selectConceptsByExtent(children);

		for (int i = 0; i < children.size(); i++) {
			FormalConcept child = children.elementAt(i);
			BasicSet childExtent = child.getExtent().intersection(extent);

			if (conceptExtent.equals(childExtent))
				return performRec(child, extent);

			FormalConcept newChild = performRec(child, extent);
			child.setVisited(true);

			newConcept.addChild(newChild);
			newChild.addParent(newConcept);
		}

		projectionConcepts.add(newConcept);
		return newConcept;
	}

}