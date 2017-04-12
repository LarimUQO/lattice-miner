package fca.core.lattice;

/**
 * Cette classe permet de construire l'ensemble des generateurs associes a un noeud.
 */

import java.util.Iterator;
import java.util.Vector;

import fca.core.util.BasicSet;

public class JenAlgorithm {
	
	private int generatorCount; // nombre de generateurs (total pour l'ensemble du treillis)
	
	private BasicSet difference;
	
	private ConceptLattice lattice;
	
	public JenAlgorithm(ConceptLattice lat) {
		lattice = lat;
		difference = new BasicSet();
		generatorCount = 0;
		calcGenerators();
	}
	
	// Calcul les faces d'un noeud
	private Vector<BasicSet> getFace(FormalConcept concept) {
		Vector<BasicSet> faces = new Vector<BasicSet>();
		Vector<FormalConcept> parents = concept.getParents();
		for (int i = 0; i < parents.size(); i++) {
			FormalConcept currParent = parents.elementAt(i);
			difference = concept.getIntent().difference(currParent.getIntent());
			faces.add(difference);
		}
		return faces;
	}
	
	// Retourne les differents attributs d'un intent
	public Vector<BasicSet> getItemsetItems(BasicSet itemset) {
		Iterator<String> it = itemset.iterator();
		Vector<BasicSet> result = new Vector<BasicSet>();
		while (it.hasNext()) {
			String currItem = it.next();
			BasicSet temp = new BasicSet();
			temp.add(currItem);
			result.add(temp);
		}
		return result;
	}
	
	// Calcul les generateurs d'un noeud
	private Vector<BasicSet> getModifiedGenerators(BasicSet face, Vector<BasicSet> generators,
			Vector<BasicSet> minBlockers, Vector<BasicSet> blockers) { //modificationGenerateurs

		// Parcours de l'ensemble des generateurs
		for (int i = 0; i < generators.size(); i++) {
			BasicSet currGen = generators.elementAt(i);

			// Intersection entre la face courante et le générateur courant
			BasicSet intersection = currGen.intersection(face);
			if (intersection.size() == 0) {
				Vector<BasicSet> newGenerators = getGenerators(face, currGen);
				blockers.addAll(newGenerators);
			} else
				minBlockers.add(currGen);
		}
		
		if (blockers.size() == 0)
			return minBlockers;
		
		else if (minBlockers.size() == 0)
			return blockers;
		
		else {
			Vector<BasicSet> result = getAlreadyDefinedGenerators(blockers, minBlockers);
			blockers.removeAll(result);
			minBlockers.addAll(blockers);
			return minBlockers;
		}
	}
	
	// Effectue l'union du generateur courant avec chacun des attributs du generateur courant
	private Vector<BasicSet> getGenerators(BasicSet face, BasicSet generator) { //parcoursItemsFace
		// Parcours de l'ensemble des faces
		Vector<BasicSet> result = new Vector<BasicSet>();
		Iterator<String> faceIt = face.iterator();
		

		while (faceIt.hasNext()) {
			String faceItem = faceIt.next();
			BasicSet faceItemset = new BasicSet();
			faceItemset.add(faceItem);
			BasicSet blocker = generator.union(faceItemset);
			result.add(blocker);
		}
		return result;
	}
	
	// Calcul des generateurs pour l'ensemble des noeuds
	public Vector<BasicSet> calcNodeGenerators(FormalConcept concept) { //calculGenerateursNoeud
		Vector<BasicSet> generators = new Vector<BasicSet>();
		
		if (concept.getIntent().size() != 0) {
			Vector<BasicSet> faces = getFace(concept);
			if (faces.size() != 0) {
				BasicSet firstFace = faces.elementAt(0);
				generators = getItemsetItems(firstFace);
				faces.removeElementAt(0);
				
				//Vector temp = new Vector();
				if (faces.size() != 0) {
					for (int i = 0; i < faces.size(); i++) {
						BasicSet currFace = faces.elementAt(i);
						
						Vector<BasicSet> minBlockers = new Vector<BasicSet>();
						Vector<BasicSet> blockers = new Vector<BasicSet>();
						Vector<BasicSet> res = getModifiedGenerators(currFace, generators, minBlockers, blockers);
						generators = res;
					}
				}
			}

			else {
				// Les generateurs du noeud courant correspondent a l'ensemble des items de l'intent du noeud courant
				BasicSet intent = concept.getIntent();
				for (String intentIt : intent) {
					BasicSet item = new BasicSet();
					item.add(intentIt);
					generators.add(item);
				}
			}
		}
		//System.out.println("Concept : "+concept+" generateurs : "+generators);
		concept.setGenerators(generators);

		return generators;
	}
	
	// Calcul des generateurs pour l'ensemble des noeuds
	private void calcGenerators() {
		if (lattice.areGeneratorsCalculated())
			return;
		
		Vector<FormalConcept> concepts = lattice.getConcepts();
		for (int i = 0; i < concepts.size(); i++) {
			FormalConcept currNode = concepts.elementAt(i);
			calcNodeGenerators(currNode);
			generatorCount += currNode.getGenerators().size();
		}
		lattice.setGeneratorsCalculated(true);
	}
	
	private Vector<BasicSet> getAlreadyDefinedGenerators(Vector<BasicSet> definedGenerators,
			Vector<BasicSet> newGenerators) { //estDejaGenerateur
		Vector<BasicSet> result = new Vector<BasicSet>();
		
		for (int i = 0; i < definedGenerators.size(); i++) {
			BasicSet currGen = definedGenerators.elementAt(i);
			boolean found = false;
			for (int j = 0; j < newGenerators.size() && !found; j++) {
				BasicSet gen = newGenerators.elementAt(j);
				if (currGen.containsAll(gen)) {
					result.add(currGen);
					found = true;
				}
			}
		}
		
		return result;
	}
	
}
