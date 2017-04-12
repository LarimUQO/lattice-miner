package fca.core.context.triadic;

import fca.core.context.binary.BinaryContext;
import fca.core.util.BasicSet;

import java.util.*;

public class Bucket {
	String label;
	List<TriadicRule> triadicRules;
	boolean decomposed = false;

	public Bucket(String label) {
		this.label = label;
		this.triadicRules = new ArrayList<>();
	}

	public String getLabel() {
		return label;
	}

	public List<TriadicRule> getTriadicRules() {
		return triadicRules;
	}

	public void decomposeRuleConsequence() {
		List<TriadicRule> newRules = new ArrayList<>();
		for (TriadicRule rule : this.triadicRules) {
			if (rule.getConsequence().size() > 1) {
				for (String s : rule.getConsequence()) {
					BasicSet csq = new BasicSet();
					csq.add(s);
					TriadicRule newRule = new TriadicRule(rule.getAntecedant(), csq, rule.getCondition(), rule.getSupport());
					newRules.add(newRule);
				}
			} else {
				newRules.add(rule);
			}
		}
		this.decomposed = true;
		this.triadicRules = newRules;
	}

	// TODO: Recalculer le support quand on regroupe les conditions.
	// SI le support de l'un des deux est zero, pas de recalul ce sera 0.
	public void toCAIs(Map<String, BinaryContext> contextsWithCondition) {
		// We will put in these subuckets all rules with the same consequence
		Map<String, List<TriadicRule>> subBuckets = new HashMap<String, List<TriadicRule>>();

		List<TriadicRule> newRules = new ArrayList<>();

		for (TriadicRule triadicRule : triadicRules) {
			String key = triadicRule.getConsequence().last();
			populateSubBucketOnKey(subBuckets, triadicRule, key);
		}

		// On a des buckets avec des regles ayant les memes consequences, pour le moment chaque regle n'a qu'une seule
		// condition. On va maintenant regrouper les regles a l'interieur meme de ses sous buckets.
		for (String s : subBuckets.keySet()) {
			List<TriadicRule> rules = subBuckets.get(s);
			if (rules.size() > 1) {
				String conditions = "";
				double support = rules.get(0).getSupport();
				for (TriadicRule rule : rules) {
					conditions += rule.getCondition();
					if (rule.getSupport() < support) {
						support = rule.getSupport();
					}
				}
				TriadicRule newRule = new TriadicRule(rules.get(0).getAntecedant(),
						rules.get(0).getConsequence(), conditions, support, true);
				newRules.add(newRule);
			} else {
				TriadicRule newRule = rules.get(0);
				newRule.cai = true;
				newRules.add(newRule);
			}
		}

		triadicRules = newRules;
	}


	private double recalculateSupport(BinaryContext context, BasicSet antecedent, BasicSet consequence) {
		BasicSet basicSet = new BasicSet();
		basicSet.addAll(antecedent);
		basicSet.addAll(consequence);
		return context.support(basicSet);
	}

	private void populateSubBucketOnKey(Map<String, List<TriadicRule>> subBuckets, TriadicRule triadicRule, String key) {
		if (subBuckets.containsKey(key)) {
			List<TriadicRule> rulesForKey = subBuckets.get(key);
			rulesForKey.add(triadicRule);
		} else {
			List<TriadicRule> rules = new ArrayList<>();
			rules.add(triadicRule);
			subBuckets.put(key, rules);
		}
	}

	public void removeDuplicateRulesFromBucket() {
		// {K}-(ae)>{P} [Support = 20.0%]
		// {K}-(ae)>{R} [Support = 20.0%]
		// {K}-(e)>{S} [Support = 20.0%]
		// {K}-(e)>{N} [Support = 20.0%]
		// We want to remove these kind of duplicates

		// key is the condition.
		Map<String, List<TriadicRule>> subBuckets = new HashMap<>();
		List<TriadicRule> newRules = new ArrayList<>();

		for (TriadicRule rule : triadicRules) {
			String keyCondition = rule.getCondition();
			populateSubBucketOnKey(subBuckets, rule, keyCondition);
		}

		for (String s : subBuckets.keySet()) {
			List<TriadicRule> rules = subBuckets.get(s);
			if (rules.size() > 1) {
				List<TriadicRule> rulesInSubBucket = subBuckets.get(s);
				BasicSet newConsequences = new BasicSet();
				double support = rules.get(0).getSupport();
				for (TriadicRule triadicRule : rulesInSubBucket) {
					newConsequences.addAll(triadicRule.getConsequence());
					if (triadicRule.getSupport() < support) {
						support = triadicRule.getSupport();
					}
				}
				TriadicRule triadicRule = new TriadicRule(rules.get(0).getAntecedant(), newConsequences, s, support, true);
				newRules.add(triadicRule);
			} else {
				TriadicRule newRule = rules.get(0);
				newRule.cai = true;
				newRules.add(newRule);
			}
		}
		triadicRules = newRules;
	}

	@Override
	public String toString() {
		String str = "";
		for (TriadicRule triadicRule : triadicRules) {
			str += triadicRule + "\n";
		}
		return str;
	}


}
