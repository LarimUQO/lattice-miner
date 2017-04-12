package fca;

import java.util.Vector;


import fca.core.rule.Rule;
import fca.core.util.BasicSet;

/**
 * Classe secondaire pour lancer Lattice Miner en test
 *
 * @author Genevieve Roberge
 * @version 1.0
 */
public class TestMain {

	/**
	 * Methode pour tester Lattice Miner
	 *
	 * @param args
	 *            arguments de tests de Lattice Miner
	 */
	public boolean  test(Vector<Rule> F) {
		for(Rule FD : F) {
			if(FD.getSupport()==0) {
				for(String a : FD.getAntecedent()) {
					BasicSet ant = FD.getAntecedent().clone();
					ant.remove(a);
					BasicSet cons = new BasicSet();
					cons.add(BasicSet.negation(a));
					F.add(new Rule(ant, cons));
				}
			} else if(FD.getConsequence().size()==1) {
				BasicSet ant = FD.getAntecedent().clone();
				ant.add(FD.getConsequence().first());
				BasicSet cons = new BasicSet(); // cons add all
				int conf = 0;
				int li = 0;
				F.add(new Rule(ant, cons, 0, conf, li));
			}
		}
		// TODO faire aussi l'inverse
		// si ab -> c [supp != 0]  ===>  abc' -> M [supp 0]

		return true;
	}

	public static void main(String[] args) {
		BasicSet validElements = new BasicSet();
		validElements.add("bleu"); //$NON-NLS-1$
		//validElements.add("rouge"); //$NON-NLS-1$
		validElements.add("vert"); //$NON-NLS-1$
		validElements.add("pomme"); //$NON-NLS-1$
		//validElements.add("poire"); //$NON-NLS-1$


		Vector<Rule> regles = new Vector<Rule>();
		BasicSet ant = new BasicSet();
		BasicSet cons = new BasicSet();
		ant.add("bleu");
		cons.add("rouge");
		regles.add(new Rule(ant, cons));
		ant = new BasicSet();
		cons = new BasicSet();
		ant.add("bleu");
		cons.add("pomme");
		regles.add(new Rule(ant, cons));
		ant = new BasicSet();
		cons = new BasicSet();
		ant.add("rouge");
		cons.add("vert");
		regles.add(new Rule(ant, cons));
		ant = new BasicSet();
		cons = new BasicSet();
		ant.add("vert");
		cons.add("bleu");
		regles.add(new Rule(ant, cons));
		TestMain toto = new TestMain();

	}

}