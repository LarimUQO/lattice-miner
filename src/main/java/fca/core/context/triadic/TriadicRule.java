package fca.core.context.triadic;

import fca.core.util.BasicSet;

import java.text.NumberFormat;

public class TriadicRule {
	BasicSet antecedant;
	BasicSet consequence;
	String condition;
	double support;
	boolean cai;

	public TriadicRule(BasicSet antecedant, BasicSet consequence, String condition, double support) {
		this.antecedant = antecedant;
		this.consequence = consequence;
		this.condition = condition;
		this.support = support;
		this.cai = false;
	}

	public TriadicRule(BasicSet antecedant, BasicSet consequence, String condition, double support, boolean cai) {
		this.antecedant = antecedant;
		this.consequence = consequence;
		this.condition = condition;
		this.support = support;
		this.cai = cai;
	}

	public boolean isCai() {
		return cai;
	}

	public void setConsequence(BasicSet consequence) {
		this.consequence = consequence;
	}

	public void setSupport(double support) {
		this.support = support;
	}

	public BasicSet getAntecedant() {
		return antecedant;
	}

	public BasicSet getConsequence() {
		return consequence;
	}

	public String getCondition() {
		return condition;
	}

	public double getSupport() {
		return support;
	}

	public boolean areSameRulesButDifferentSupport(TriadicRule r) {
		if (this.antecedant.equals(r.antecedant) && this.consequence.equals(r.consequence) &&
				this.condition.equals((r.condition)))
			return true;
		return false;
	}

	public boolean areSameRulesButDifferentCondition(TriadicRule r) {
		if (this.antecedant.equals(r.antecedant) && this.consequence.equals(r.consequence) && !this.condition.equals(r.condition))
			return true;
		return false;
	}

	public double lowestSupport(TriadicRule b1) {
		return b1.support < this.support ? b1.support : this.support;
	}

	private static String removeDuplicates(String s) {
		StringBuilder noDupes = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			String si = s.substring(i, i + 1);
			if (noDupes.indexOf(si) == -1) {
				noDupes.append(si);
			}
		}
		return noDupes.toString();
	}

	@Override
	public String toString() {
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMinimumFractionDigits(1);
		if (cai) {
			return antecedant + "-(" + removeDuplicates(condition) + ")>" + consequence;
		} else {
			return "(" + antecedant + " -> " + consequence + ")" + removeDuplicates(condition);
		}

	}
}
