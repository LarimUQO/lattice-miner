
package fca.core.rule;

import fca.core.util.BasicSet;
import fca.messages.CoreMessages;

/**
 * Cette classe permet de decrire les caracteristiques des regles d'association.
 * Celles-ci sont de la forme:
 * <code>(antecedent --> consequence) condition (support, confiance, type)</code>
 *
 * @author Inconnu
 * @version 1.0
 */
@SuppressWarnings("unused")
public class TRule {

	/**
	 * antecedent de la regle
	 */
	private String antecedent;

	/**
	 * consequence de la regle
	 */
	private String consequence;

	/**
	 * Condition de la regle
	 */
	private String condition;

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
	 */
	private double lift;
	/**
	 * Type de la regle
	 */
	private int type;
	/**
	 * Construit une regle a partir de l'antecedent, la consequence, le support
	 * et la confiance
	 *
	 * @param ant
	 *            l'antecedent de la regle
	 * @param cons
	 *            la consequence de la regle
	 * @param supp
	 *            le support de la regle
	 * @param conf
	 *            la confiance de la regle
	 */
	public TRule(String ant, String cons,String cond,  double supp, double conf, double li, int ty) {
		antecedent = ant;
		consequence = cons;
		condition = cond;
		support = supp;
		confidence = conf;
		lift = li;
		type = ty;
	}

	/**
	 * Construit une regle a partir de l'antecedent et la consequence
	 *
	 * @param ant
	 *            l'antecedent de la regle
	 * @param cons
	 *            la consequence de la regle
	 */
	public TRule(String ant, String cons,String cond) {
		antecedent = ant;
		consequence = cons;
		condition = cond;
		support = 1.0;
		confidence = 1.0;
		lift = 1.0;
		type= 0;
	}

	/**
	 * @return la confiance de la regle
	 */
	public double getConfidence() {
		return confidence;
	}

	/**
	 * Change la confiance de la regle
	 *
	 * @param conf
	 *            la nouvelle confiance de la regle
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
	 *
	 * @param supp
	 *            le nouveau support de la regle
	 */
	public void setSupport(double supp) {
		support = supp;
	}

	/**
	 * @return l'antecedent de la regle
	 */
	public String getAntecedent() {
		return antecedent;
	}

	/**
	 * Change l'antecedent de la regle
	 *
	 * @param ant
	 *            le nouvel antecedent de la regle
	 */
	public void setAntecedent(String ant) {
		antecedent = ant;
	}

	/**
	 * @return la consequence de la regle
	 */
	public String getConsequence() {
		return consequence;
	}

	/**
	 * Change la consequence de la regle
	 *
	 * @param cons
	 *            la nouvelle consequence de la regle
	 */
	public void setConsequence(String cons) {
		consequence = cons;
	}
	/**
	 * @return la condition de la regle
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Change la consequence de la regle
	 *
	 * @param cons
	 *            la nouvelle consequence de la regle
	 */
	public void setCondition(String cond) {
		consequence = cond;
	}
	/**
	 * @return la mesure de lift de la regle
	 */
	public double getLift() {
		return lift;
	}
	/**
	 * @return la mesure de lift de la regle
	 */
	public double getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object ruleObj) {
		if (!(ruleObj instanceof TRule))
			return false;

		TRule rule = (TRule) ruleObj;
		return ((antecedent.equals(rule.getAntecedent())) && (consequence.equals(rule.getConsequence())) && (condition.equals(rule.getCondition())));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = "( "; //$NON-NLS-1$
		str += antecedent;
		str += " -> "; //$NON-NLS-1$
		str += consequence;
		str += ")";
		str += condition;
		str += "\t"; //$NON-NLS-1$

		// Enregistrement du support de la regle
		str += "(" + CoreMessages.getString("Core.support") + " = "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String suppStr = Double.toString(((int) (support * 100.0)));
		str += suppStr;
		str += "%";
		str += " ; "; //$NON-NLS-1$

		// Enregistrement de la confiance de la regle
		str += CoreMessages.getString("Core.confidence") + " = "; //$NON-NLS-1$ //$NON-NLS-2$
		String confStr = Double
				.toString(((int) (confidence * 100.0)));
		str += confStr;
		str += "%";
		//	str += " ; "; //$NON-NLS-1$
		//	str += type;
		str += ")";
		str += "\n";
		return str;

	}
	public String toString2() {
		String str = "( "; //$NON-NLS-1$
		str += antecedent;
		str += " -> "; //$NON-NLS-1$
		str += consequence;
		str += ")";
		str += condition;
		str += "\t"; //$NON-NLS-1$

		// Enregistrement du support de la regle
		str += "(" + CoreMessages.getString("Core.support") + " = "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String suppStr = Double.toString(((int) (support * 100.0)));
		str += suppStr;
		str += " ; "; //$NON-NLS-1$

		// Enregistrement de la confiance de la regle
		str += CoreMessages.getString("Core.confidence") + " = "; //$NON-NLS-1$ //$NON-NLS-2$
		String confStr = Double
				.toString(((int) (confidence * 100.0)));
		str += confStr;
		str += " ; "; //$NON-NLS-1$
		str += type;
		str += ")";
		str += "\n";
		return str;
	}

}
