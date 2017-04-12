package fca.gui.lattice.tool;

import java.util.Stack;
import java.util.Vector;

import fca.core.context.Context;
import fca.core.lattice.DataCel;
import fca.core.lattice.NestedConcept;
import fca.core.lattice.NestedLattice;
import fca.core.lattice.operator.search.SearchApproximateExtentNested;
import fca.core.lattice.operator.search.SearchApproximateIntentNested;
import fca.core.util.BasicSet;
import fca.exception.LatticeMinerException;
import fca.gui.lattice.LatticePanel;
import fca.gui.lattice.element.GraphicalConcept;
import fca.gui.lattice.element.GraphicalLattice;
import fca.gui.lattice.element.LatticeStructure;
import fca.gui.util.DialogBox;
import fca.messages.GUIMessages;

/**
 * Panneau de recherche pour les treillis imbriqués
 * @author Ludovic Thomas
 * @version 1.0
 */
public class SearchNested extends Search {

	/**
	 *
	 */
	private static final long serialVersionUID = -884650316657528607L;

	/**
	 * Constructeur
	 * @param l Le {@link GraphicalLattice} pour lequel ce panneau affiche les attributs et objets
	 * @param lp Le {@link LatticePanel} dans lequel est affiché le treillis
	 */
	public SearchNested(GraphicalLattice l, LatticePanel lp) {
		super(l, lp);
		// Treillis imbriqué ne possède que une recherche exacte
		exactMatchOnly.setVisible(false);
		exactMatchOnly.setSelected(true);
		openResultButton.setVisible(false);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelSearch#approximateMatchAction()
	 */
	@Override
	public void approximateMatchAction() throws LatticeMinerException {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalConcept result = (GraphicalConcept) approximateMatch();
			showAndShakeResult(result);
			activateOpenResultButton(result);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelSearch#exactMatchAction()
	 */
	@Override
	public void exactMatchAction() {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalConcept result = exactMatch();
			showAndShakeResult(result);
			activateOpenResultButton(result);
		}
	}

	/**
	 * Active ou non le bouton "OpenResult" pour ouvrir le resultat dans une nouvelle frame
	 * @param result le resultat {@link GraphicalConcept} a afficher
	 */
	private void activateOpenResultButton(GraphicalConcept result) {
		if (result != null && result.getInternalLattice() != null) {

			// Recupere le GraphicalLattice du concept resultat
			GraphicalLattice resultLattice = result.getInternalLattice();

			// Recupere les latticesStructures
			Vector<LatticeStructure> latticesStructures = new Vector<LatticeStructure>();
			latticesStructures.add(resultLattice.getLatticeStructure());
			latticesStructures.addAll(resultLattice.getInternalLatticeStructures());

			// Recupere le Nested Lattice interne du concept resultat
			NestedLattice nestedLattice = resultLattice.getNestedLattice();

			// Creer un nouveau GraphicalLattice associé au nestedLattice
			GraphicalLattice graphLattice = new GraphicalLattice(nestedLattice, null, latticesStructures);
			resultOperation = graphLattice;

			openResultButton.setEnabled(true);
		} else {
			openResultButton.setEnabled(false);
			resultOperation = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelSearch#searchApproximateNodeWithExtent(fca.general.BasicSet)
	 */
	@Override
	protected GraphicalConcept searchApproximateNodeWithExtent(BasicSet extent) {
		SearchApproximateExtentNested search = new SearchApproximateExtentNested(lattice.getNestedLattice());
		Stack<NestedConcept> formalResult = search.perform(extent);

		GraphicalConcept result = null;

		// On parcours la pile pour traverser les treillis imbriqués jusqu'au dernier
		if (!formalResult.isEmpty()) {
			GraphicalLattice latticeRec = lattice;
			for (NestedConcept node : formalResult) {
				result = latticeRec.getGraphicalConcept(node);
				latticeRec = result.getInternalLattice();
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelSearch#searchApproximateNodeWithIntent(fca.general.BasicSet)
	 */
	@Override
	protected GraphicalConcept searchApproximateNodeWithIntent(BasicSet intent) {
		SearchApproximateIntentNested search = new SearchApproximateIntentNested(lattice.getNestedLattice());
		Stack<NestedConcept> formalResult = search.perform(intent);

		GraphicalConcept result = null;

		// On parcours la pile pour traverser les treillis imbriqués jusqu'au dernier
		if (!formalResult.isEmpty()) {
			GraphicalLattice latticeRec = lattice;
			for (NestedConcept node : formalResult) {
				result = latticeRec.getGraphicalConcept(node);
				latticeRec = result.getInternalLattice();
			}
		}

		return result;
	}

	@Override
	protected DataCel Alpha1(DataCel cel, Context ctx) {
		// TODO Auto-generated method stub
		return null;
	}

}