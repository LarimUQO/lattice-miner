package fca.core.rule;

import fca.core.util.BasicSet;
import fca.messages.CoreMessages;

/**
 * Cette classe permet de decrire les caracteristiques des regles d'association. Celles-ci sont de
 * la forme: <code>antecedent --> consequence (support, confiance)</code>
 * @author Inconnu
 * @version 1.0
 */
public class Rule implements Cloneable {
	/**
	 * Antecedent de la regle
	 */
	private BasicSet antecedent;
	
	/**
	 * Consequence de la regle
	 */
	private BasicSet consequence;
	
	/**
	 * Support de la regle
	 */
	private double support;
	
	/**
	 * Confiance de la regle
	 */
	private double confidence;
	
	/**
	 * Mesure d'interet de la regle
	 *  A => B
	 *  li = conf / prob(B)
	 */
	private double lift;
	
	/**
	 * Construit une regle a partir de l'antecedent, la consequence, le support et la confiance
	 * @param ant l'antecedent de la regle
	 * @param cons la consequence de la regle
	 * @param supp le support de la regle
	 * @param conf la confiance de la regle
	 */
	public Rule(BasicSet ant, BasicSet cons, double supp, double conf, double li) {
		antecedent = ant;
		consequence = cons;
		support = supp;
		confidence = conf;
		lift = li;
	}
	
	/**
	 * Construit une regle a partir de l'antecedent et la consequence
	 * @param ant l'antecedent de la regle
	 * @param cons la consequence de la regle
	 */
	public Rule(BasicSet ant, BasicSet cons) {
		antecedent = ant;
		consequence = cons;
		support = 1.0;
		confidence = 1.0;
		lift = 1.0;
	}
	
	/**
	 * @return la confiance de la regle
	 */
	public double getConfidence() {
		return confidence;
	}
	
	/**
	 * Change la confiance de la regle
	 * @param conf la nouvelle confiance de la regle
	 */
	public void setConfidence(double conf) {
		confidence = conf;
	}
	
	/**
	 * @return le support de la regle
	 */
	public double getSupport() {
		return support;
	}
	
	/**
	 * Change le support de la regle
	 * @param supp le nouveau support de la regle
	 */
	public void setSupport(double supp) {
		support = supp;
	}
	
	/**
	 * @return l'antecedent de la regle
	 */
	public BasicSet getAntecedent() {
		return antecedent;
	}
	
	/**
	 * Change l'antecedent de la regle
	 * @param ant le nouvel antecedent de la regle
	 */
	public void setAntecedent(BasicSet ant) {
		antecedent = ant;
	}
	
	/**
	 * @return la consequence de la regle
	 */
	public BasicSet getConsequence() {
		return consequence;
	}
	
	/**
	 * Change la consequence de la regle
	 * @param cons la nouvelle consequence de la regle
	 */
	public void setConsequence(BasicSet cons) {
		consequence = cons;
	}
	
	/**
	 * @return la mesure de lift de la regle
	 */
	public double getLift(){
		return lift;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object ruleObj) {
		if (!(ruleObj instanceof Rule))
			return false;
		
		Rule rule = (Rule) ruleObj;
		return ((antecedent.equals(rule.getAntecedent())) && (consequence.equals(rule.getConsequence())));
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = ""; //$NON-NLS-1$
		str += antecedent;
		str += " => "; //$NON-NLS-1$
		str += consequence;
		str += "\t\t"; //$NON-NLS-1$
		
		// Enregistrement du support de la regle
		str += "("+CoreMessages.getString("Core.support")+" = "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String suppStr = Double.toString((((int) (support * 100.0))) / 100.0);
		str += suppStr;
		str += " ; "; //$NON-NLS-1$
		
		// Enregistrement de la confiance de la regle
		str += CoreMessages.getString("Core.confidence")+" = "; //$NON-NLS-1$ //$NON-NLS-2$
		String confStr = Double.toString((((int) (confidence * 100.0))) / 100.0);
		str += confStr;
		str += ")"; //$NON-NLS-1$
		
		return str;
	}
	
	@Override
	public Rule clone() {
		Rule out = new Rule(antecedent.clone(), consequence.clone(), support, confidence, lift);
		return out;
	}
}