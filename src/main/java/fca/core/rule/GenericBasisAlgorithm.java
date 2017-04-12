package fca.core.rule;

import java.util.Vector;

import fca.core.lattice.ConceptLattice;
import fca.core.lattice.FormalConcept;
import fca.core.util.BasicSet;

/**
 * Cette classe permet de generer la base generique de Pasquier (regles exactes) a partir des
 * concepts frequents et de ses generateurs associes. Les generateurs ont ete prealablement
 * construits par l'algorithme JEN. Regle : generateur --> ConceptFrequent \ generateur confiance =
 * 1.0 support = support(ConceptFrequent)
 * @author Inconnu
 * @version 1.0
 */
public class GenericBasisAlgorithm extends RuleAlgorithm {	
	/**
	 * Constructeur pour generer la base generique de Pasquier
	 * @param lat le treillis graphique ou l'on recherche les regles
	 * @param minSupp le support minimum
	 */
	public GenericBasisAlgorithm(ConceptLattice lat, double minSupp) {
		super(lat, minSupp, 1.0);
		lattice.findGenerators();
		run();
	}
	
	/**
	 * Permet d'obtenir la consequence reduite de la regle
	 * @param consequence la consequence de la regle
	 * @param antecedent l'antecedent de la regle
	 * @return la consequence reduite de la regle
	 */
	private BasicSet getReducedConsequence(BasicSet consequence, BasicSet antecedent) {
		BasicSet reducedConsequence = new BasicSet();
		
		/* Parcours de tous les items de la consequence non reduite de la regle */
		for (String item : consequence) {
			/* Si l'item n'est pas inclus dans l'antecedent, il fait partie de la consequence
			 * reduite
			 */
			if (!antecedent.contains(item))
				reducedConsequence.add(item);
		}
		
		return reducedConsequence;
	}
	
	/**
	 * Generation de l'ensemble des regles generees a partir du concept rentre en parametre
	 * @param node noeud de base
	 * @param objCount nombre d'objets
	 */
	private void processNode(FormalConcept node, float objCount) {
		if (node.getIntent().size() > 1) {
			// Parcours des generateurs du concept courant
			Vector<BasicSet> generators = node.getGenerators();
			
			for (int i = 0; i < generators.size(); i++) {
				BasicSet currGen = generators.elementAt(i);
				double ruleSupport = ((double) node.getExtent().size()) / objCount;
				
				BasicSet potentialCons = getReducedConsequence(node.getIntent(), currGen);
				
				if ((potentialCons.size() != 0) && (ruleSupport >= minSupport)) {
					Rule newRule = new Rule(currGen, potentialCons, ruleSupport, 1.0,1.0);
					rules.add(newRule);
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.rule.RuleAlgorithm#run()
	 */
	@Override
	public void run() {
		int objCount = lattice.getTopConcept().getExtent().size();
		
		// Parcours de l'ensemble des concepts courants
		Vector<FormalConcept> concepts = lattice.getConcepts();
		for (int i = 0; i < concepts.size(); i++) {
			FormalConcept currConcept = concepts.elementAt(i);
			processNode(currConcept, objCount);
		}
	}
}
