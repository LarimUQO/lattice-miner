package fca.core.rule;

import java.util.Vector;

import fca.core.lattice.ConceptLattice;
import fca.core.lattice.FormalConcept;
import fca.core.util.BasicSet;

/**
 * Cette classe permet de generer la base generique Informative
 * @author Arnaud Renaud-Goud
 * @version 1.0
 */
public class InformativeBasisAlgorithm extends RuleAlgorithm {	
	/**
	 * Constructeur pour generer la base generique Informative
	 * @param lat le treillis graphique ou l'on recherche les regles
	 * @param minSupp le support minimum
	 * @param minConf la confiance minimum
	 */
	public InformativeBasisAlgorithm(ConceptLattice lat, double minSupp, double minConf) {
		super(lat, minSupp, minConf);
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
	 * Calcule un support d'elements mixtes en utilisant les supports des elements positif
	 * Supp(T) = Somme(X allant de PT(T) à T ) de  (-1)^NNT(X) * supp(P(X))
	 * @param bs Ensemble sur lequel calculer le support mixte
	 * @return le support mixte
	 */
	public double suppMixte(BasicSet bs) {
		return BasicSet.suppMixte(bs, lattice.getContext());
	}

	/**
	 * Calcule les regles mixtes a partir de regles positives strictes
	 * @return Les regles mixtes
	 */
	@SuppressWarnings("unchecked") 
	public Vector<Rule> MixedRules() {
		Vector<Rule> F = (Vector<Rule>) rules.clone(); 
		for(Rule FD : rules) {
			if(FD.getSupport()==0) {
				for(String a : FD.getAntecedent()) {
					BasicSet ant = (BasicSet) FD.getAntecedent().clone();
					ant.remove(a);
					BasicSet cons = new BasicSet();
					cons.add(BasicSet.negation(a));
					double supp = this.suppMixte(cons.union(ant));
					int conf = 1;
					int li = 0; // FIXME
					F.add(new Rule(ant, cons, supp, conf, li));	

				}
			} else if(FD.getConsequence().size()!=0) {
				for(String a : FD.getConsequence()) {
					BasicSet ant = (BasicSet) FD.getAntecedent().clone();
					ant.add(BasicSet.negation(a));
					BasicSet cons = new BasicSet(); 
					cons.add("MM\'"); // cons add all
					double supp = 0;
					int conf = 1;
					int li = 0; // FIXME
					F.add(new Rule(ant, cons, supp, conf, li));
				}				
			}
		}
		return F;
	}
	/**
	 * Generation de l'ensemble des regles generees a partir du concept rentre en parametre
	 * @param node noeud de base
	 * @param objCount nombre d'objets
	 */
	private void processNode(FormalConcept node, float objCount) { //generationRegleNoeud
		float antSupport = (node.getExtent().size()) / objCount;

		// Parcours des generateurs du concept courant
		Vector<BasicSet> generators = node.getGenerators();

		for (int i = 0; i < generators.size(); i++) {
			// Selection du generateur courant
			BasicSet currGen = generators.elementAt(i);
			Vector<FormalConcept> children = node.getChildren();

			if ((children.size() != 0) && (node.getIntent().size() != 0)) {
				// Parcours des concepts enfants du concept courant
				for (int j = 0; j < children.size(); j++) {
					FormalConcept child = children.elementAt(j);
					float consSupport = (child.getExtent().size()) / objCount;

					BasicSet potentialCons = getReducedConsequence(child.getIntent(), node.getIntent());
					double ruleConf = consSupport / antSupport;

					// Si la confiance est >= a la confiance minimale, la regle est conservee
					if ((potentialCons.size() != 0) && (ruleConf >= minConfidence) && (consSupport >= minSupport)) {
						Rule newRule = new Rule(currGen, potentialCons, consSupport, ruleConf,1.0);
						rules.add(newRule);
					}
				}
			}

			double ruleSupport = ((double) node.getExtent().size()) / objCount;

			BasicSet potentialCons = getReducedConsequence(node.getIntent(), currGen);
			if ((potentialCons.size() != 0) && (ruleSupport >= minSupport)) {
				Rule newRule = new Rule(currGen, potentialCons, ruleSupport, 1.0,1.0);
				rules.add(newRule);
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
