package fca.core.lattice.operator.projection;

import java.util.Vector;

import fca.core.lattice.ConceptLattice;
import fca.core.lattice.FormalConcept;
import fca.core.lattice.operator.Operator;
import fca.core.util.BasicSet;

/**
 * Classe contenant les opération pour la projection optimisée sur un treillis
 * @author Geneviève Roberge
 * @author Ludovic Thomas (reprise de la structure via {@link Operator})
 */
public abstract class ImprovedProjection extends Operator<ConceptLattice, BasicSet, ConceptLattice> {
	
	protected Vector<FormalConcept> projectionConcepts;
	
	/**
	 * Constructeur d'une projection optimisée
	 * @param data le treillis dans un veut faire une projection
	 */
	public ImprovedProjection(ConceptLattice data) {
		super(data, "ImprovedProjection"); //$NON-NLS-1$
		projectionConcepts = new Vector<FormalConcept>();
	}
	
	protected FormalConcept getConcept(BasicSet extent, BasicSet intent) {
		for (int i = 0; i < projectionConcepts.size(); i++) {
			FormalConcept currConcept = projectionConcepts.elementAt(i);
			if (currConcept.getExtent().equals(extent) && currConcept.getIntent().equals(intent))
				return currConcept;
		}
		return null;
	}
	
	protected Vector<FormalConcept> sortConcepts(Vector<FormalConcept> concepts) {
		/* Bubble sort pour placer les intentions en ordre décroissant de taille */
		for (int i = 0; i < concepts.size() - 1; i++) {
			FormalConcept c1 = concepts.elementAt(i);
			for (int j = i + 1; j < concepts.size(); j++) {
				FormalConcept c2 = concepts.elementAt(j);
				if (c2.getIntent().size() > c1.getIntent().size()) {
					concepts.setElementAt(c2, i);
					concepts.setElementAt(c2, j);
				}
			}
		}
		
		return concepts;
	}
	
	protected Vector<FormalConcept> selectConceptsByIntent(Vector<FormalConcept> concepts) {
		Vector<FormalConcept> selectedConcepts = new Vector<FormalConcept>();
		/* Sélectionne les concepts dont l'intent n'est pas un sous-ensemble d'un autre */
		for (int i = 0; i < concepts.size(); i++) {
			boolean select = true;
			FormalConcept c1 = concepts.elementAt(i);
			for (int j = 0; j < concepts.size(); j++) {
				FormalConcept c2 = concepts.elementAt(j);
				if (i != j && c1.getIntent().size() < c2.getIntent().size()
						&& c2.getIntent().containsAll(c1.getIntent()))
					select = false;
				else if (i > j && c1.getIntent().size() == c2.getIntent().size()
						&& c1.getIntent().equals(c2.getIntent()))
					select = false;
			}
			
			if (select)
				selectedConcepts.add(c1);
		}
		
		return selectedConcepts;
	}
	
	protected Vector<FormalConcept> selectConceptsByExtent(Vector<FormalConcept> concepts) {
		Vector<FormalConcept> selectedConcepts = new Vector<FormalConcept>();
		
		/* Sélectionne les concepts dont l'intent n'est pas un sous-ensemble d'un autre */
		for (int i = 0; i < concepts.size(); i++) {
			boolean select = true;
			FormalConcept c1 = concepts.elementAt(i);
			
			for (int j = 0; j < concepts.size(); i++) {
				FormalConcept c2 = concepts.elementAt(j);
				if (i != j && c1.getExtent().size() <= c2.getExtent().size()
						&& c2.getExtent().containsAll(c1.getExtent()))
					select = false;
				else if (i > j && c1.getExtent().size() == c2.getExtent().size()
						&& c1.getExtent().equals(c2.getExtent()))
					select = false;
			}
			
			if (select)
				selectedConcepts.add(c1);
		}
		
		return selectedConcepts;
	}
	
	protected FormalConcept findBottomConcept() {
		if (projectionConcepts.size() < 1)
			return null;
		
		FormalConcept currentConcept = projectionConcepts.elementAt(0);
		while (currentConcept.getChildren().size() > 0)
			currentConcept = currentConcept.getChildren().elementAt(0);
		
		return currentConcept;
	}
	
	protected FormalConcept findTopConcept() {
		if (projectionConcepts.size() < 1)
			return null;
		
		FormalConcept currentConcept = projectionConcepts.elementAt(0);
		while (currentConcept.getParents().size() > 0)
			currentConcept = currentConcept.getParents().elementAt(0);
		
		return currentConcept;
	}
	
}