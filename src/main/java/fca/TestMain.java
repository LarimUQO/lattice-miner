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

	
	
	
	public boolean  test(/*BasicSet X,*/Vector<Rule> F) {
		/*BasicSet Olddep = new BasicSet();
		BasicSet Newdep = (BasicSet) X.clone();
		while(!Newdep.equals(Olddep)) {
			Olddep = Newdep;
			for(Rule FD : F) {
				BasicSet W = FD.getAntecedent();
				BasicSet Z = FD.getConsequence();
				if(FD.getSupport()!=0) {
					if(Newdep.isIncluding(W)) {
						Newdep = Newdep.union(Z);
					}
				} 
			}
		}*/

		for(Rule FD : F) {
			if(FD.getSupport()==0) {
				for(String a : FD.getAntecedent()) {
					BasicSet ant = (BasicSet) FD.getAntecedent().clone();
					ant.remove(a);
					BasicSet cons = new BasicSet();
					cons.add(BasicSet.negation(a));
					F.add(new Rule(ant, cons));			
				}
			} else if(FD.getConsequence().size()==1) {
				BasicSet ant = (BasicSet) FD.getAntecedent().clone();
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

		//toto.test(validElements, regles, null, null);
		/*while(temp != "" ) {
			Row row = sheet.getRow(i); // ligne 2
			while(temp != "" ) {
				System.out.print("Ligne "+ i+" | Colonne : "+j+ " | Valeur :");
				Cell cell = row.getCell(j); // Col 1
				try {
					temp = cell.getStringCellValue();
				} catch (java.lang.IllegalStateException e) {
					temp = Double.toString(cell.getNumericCellValue());
				} catch (java.lang.NullPointerException e) {
					temp = ""; // On presume a 0 si pas de valeur
				}
				System.out.println(temp + "\t");
				j++;
			}
			System.out.println("\n");
			i++;
		}*/




		/*String formulaString = "([allo] AND [salut]) OR NOT ([patate] AND [banane] AND [pomme verte]) OR [bleu]"; //$NON-NLS-1$
		BasicSet validElements = new BasicSet();
		validElements.add("bleu"); //$NON-NLS-1$
		validElements.add("rouge"); //$NON-NLS-1$
		validElements.add("vert"); //$NON-NLS-1$
		validElements.add("pomme verte"); //$NON-NLS-1$
		validElements.add("pomme"); //$NON-NLS-1$
		validElements.add("poire"); //$NON-NLS-1$
		validElements.add("banane"); //$NON-NLS-1$
		validElements.add("carotte"); //$NON-NLS-1$
		validElements.add("patate"); //$NON-NLS-1$
		validElements.add("celeri vert"); //$NON-NLS-1$
		validElements.add("salut"); //$NON-NLS-1$
		validElements.add("bonjour"); //$NON-NLS-1$
		validElements.add("allo"); //$NON-NLS-1$

		LogicalFormula formula = new LogicalFormula(formulaString,
				validElements);
		System.out.println("Valid = " + formula.isValid()); //$NON-NLS-1$

		if (!formula.isValid()) {
			int pos = formula.getErrorPosition();
			int length = formula.getErrorLength();

			System.out.println("Error : " + formula.getErrorMessage()); //$NON-NLS-1$
			System.out.println("Error position : " + pos); //$NON-NLS-1$
			System.out
					.println("Error string : " + formulaString.substring(pos, pos + length)); //$NON-NLS-1$
		}

		else {
			BasicSet attSet = new BasicSet();
			attSet.add("rouge"); //$NON-NLS-1$
			attSet.add("vert"); //$NON-NLS-1$
			attSet.add("patate"); //$NON-NLS-1$
			attSet.add("pomme verte"); //$NON-NLS-1$
			attSet.add("banane"); //$NON-NLS-1$
			attSet.add("salut"); //$NON-NLS-1$
			attSet.add("bonjour"); //$NON-NLS-1$
			attSet.add("carotte"); //$NON-NLS-1$

			System.out
					.println("Accept " + attSet.toString() + " = " + formula.accept(attSet)); //$NON-NLS-1$ //$NON-NLS-2$
		}

		BinaryContext cnt = new BinaryContext("b",8,8);


		try {
			cnt.setValueAt("true", 0, 1);
			//cnt.setValueAt("true", 0, 3);
			cnt.setValueAt("true", 0, 6);
			cnt.setValueAt("true", 1, 0);
			cnt.setValueAt("true", 1, 1);
			cnt.setValueAt("true", 1, 3);
			cnt.setValueAt("true", 1, 4);
			cnt.setValueAt("true", 1, 5);
			cnt.setValueAt("true", 1, 6);
			cnt.setValueAt("true", 2, 0);
			cnt.setValueAt("true", 2, 1);
			cnt.setValueAt("true", 2, 3);
			cnt.setValueAt("true", 2, 4);
			cnt.setValueAt("true", 2, 6);
			cnt.setValueAt("true", 3, 0);
			//cnt.setValueAt("true", 3, 1);
			cnt.setValueAt("true", 3, 2);
			cnt.setValueAt("true", 3, 3);
			cnt.setValueAt("true", 3, 4);
			cnt.setValueAt("true", 3, 6);
			cnt.setValueAt("true", 4, 0);
			cnt.setValueAt("true", 4, 1);
			cnt.setValueAt("true", 4, 3);
			cnt.setValueAt("true", 4, 6);
			cnt.setValueAt("true", 5, 2);
			cnt.setValueAt("true", 5, 3);
			cnt.setValueAt("true", 5, 5);
			cnt.setValueAt("true", 5, 6);
			cnt.setValueAt("true", 5, 7);
			cnt.setValueAt("true", 6, 3);
			cnt.setValueAt("true", 6, 6);
			cnt.setValueAt("true", 7, 3);
			cnt.setValueAt("true", 7, 5);
			cnt.setValueAt("true", 7, 6);

		} catch (InvalidTypeException e) {
			e.printStackTrace();
		}*/


		//System.out.println(cnt);

		/*cnt.setLowArrow(cnt);
	cnt.setHighArrow(cnt);
	System.out.println(cnt.setLowArrow(cnt));
	System.out.println(cnt.setHighArrow(cnt));
	System.out.println(cnt);
	//cnt.setHighArrow(cnt);
	//cnt.reduceObject(cnt);
	//cnt.reduceContext(cnt);
	//System.out.println(cnt);*/

		//BinaryContext bc = cnt.setArrow(cnt);
		//cnt.reduceContext(cnt);
		//cnt.reduceObject(cnt);
		//System.out.println(cnt);
		//System.out.println(bc);
		//cnt.reduceAttribut(cnt);
		//System.out.println(cnt);
		//System.out.println(bc);
		//cnt.addComplementaryContext(cnt);
		//cnt.clarifyObject(cnt);
		//cnt.clarifyAttribute(cnt);
		//System.out.println(cnt);
		//cnt.complementaryContext(cnt);
		//cnt.transitive(cnt);
		//cnt.setArrow(cnt);
		//System.out.println(cnt);


		/* cnt.transitiveClosureAtt("a"); */


		//cnt.transitivesAtt(cnt);
		//cnt.test("[aabbabacda'a'b'b'v'c']");

	}

}