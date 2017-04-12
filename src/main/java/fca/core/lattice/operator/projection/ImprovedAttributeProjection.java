package fca.core.lattice.operator.projection;

import java.util.Vector;

import fca.core.lattice.ConceptLattice;
import fca.core.lattice.FormalConcept;
import fca.core.util.BasicSet;

/**
 * Opération de projection sur les attributs d'un treillis
 * @author Geneviève Roberge
 * @author Ludovic Thomas
 */
public class ImprovedAttributeProjection extends ImprovedProjection {

	/**
	 * Constructeur d'une projection optimisée sur les attributs
	 * @param data le treillis dans un veut faire une projection
	 */
	public ImprovedAttributeProjection(ConceptLattice data) {
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

		// Execute la projection sur les attributs
		performRec(data.getBottomConcept(), entry);
		result = new ConceptLattice(findTopConcept(), "ProjectionOn" + entry.toString()); //$NON-NLS-1$

		return result;
	}

	private FormalConcept performRec(FormalConcept concept, BasicSet intent) {

		if (concept.hasBeenVisited())
			return getConcept(concept.getExtent(), concept.getIntent().intersection(intent));

		BasicSet conceptIntent = concept.getIntent().intersection(intent);
		FormalConcept newConcept = new FormalConcept(concept.getExtent(), conceptIntent);

		Vector<FormalConcept> parents = sortConcepts(concept.getParents());
		parents = selectConceptsByIntent(parents);

		Vector<FormalConcept> successors = new Vector<FormalConcept>();

		for (int i = 0; i < parents.size(); i++) {
			FormalConcept parent = parents.elementAt(i);
			BasicSet parentIntent = parent.getIntent().intersection(intent);

			if (conceptIntent.equals(parentIntent))
				return performRec(parent, intent);

			FormalConcept newParent = performRec(parent, intent);
			successors.add(newParent);
			parent.setVisited(true);
		}

		for (int i = 0; i < successors.size(); i++) {
			FormalConcept parent = successors.elementAt(i);
			newConcept.addParent(parent);
			parent.addChild(newConcept);
		}

		projectionConcepts.add(newConcept);
		return newConcept;
	}

}