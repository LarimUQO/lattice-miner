package fca.core.context.triadic;

import com.google.gson.Gson;
import fca.core.context.binary.BinaryContext;
import fca.core.lattice.ConceptLattice;
import fca.core.rule.InformativeBasisAlgorithm;
import fca.core.rule.Rule;
import fca.core.rule.RuleAlgorithm;
import fca.core.util.BasicSet;
import fca.exception.InvalidTypeException;

import java.io.*;
import java.util.*;

public class Ganter {

	private final TriadicJson triadicJsonObject;
	private final List<Bucket> buckets = new ArrayList<>();

	public Ganter(String contextFilePath) {
		String json = readJsonFile(contextFilePath);
		this.triadicJsonObject = createTriadicFromJson(json);
	}

	public TriadicJson getTriadicJsonObject() {
		return triadicJsonObject;
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Please pass the context Json path.");
			System.exit(0);
		}
		Ganter app = new Ganter(args[0]);
		System.out.println(app.showRules());
	}

	public String showRules() {
		StringBuilder rules = new StringBuilder();
		// Contexts with Conditions.
		rules.append("== Biedermann's implications  ==\n"); // Diadic decomposition
		// key is the condition, value is the binary context for that condition.
		Map<String, BinaryContext> contextsWithCondition = generateDiadicsFromTriadics();

		for (Map.Entry<String, BinaryContext> binaryContextEntry : contextsWithCondition.entrySet()) {
			BinaryContext bc = binaryContextEntry.getValue();
			String condition = binaryContextEntry.getKey();

			List<TriadicRule> biRules = getBinaryRulesAndGenerateBuckets(condition, bc);
			for (TriadicRule biRule : biRules) {
				String label = getBucketLabel(biRule.getAntecedant());
				Bucket bucket = findBucketByLabel(label);
				List<TriadicRule> bucketTriadicRules = null;
				if (bucket != null) {
					bucketTriadicRules = bucket.getTriadicRules();
				}
				if (bucketTriadicRules != null) {
					bucketTriadicRules.add(biRule);
				}
				rules.append(biRule);
				rules.append("\n");
			}
		}


		// Buckets
		for (Bucket bucket : buckets) {
			rules.append(bucket);
		}

		rules.append("\n==== CAIs ======\n");
		for (Bucket bucket : buckets) {
			bucket.decomposeRuleConsequence();
			bucket.toCAIs(contextsWithCondition);
			bucket.removeDuplicateRulesFromBucket();
			rules.append(bucket);
		}
		return rules.toString();
	}

	private String readJsonFile(String filePath) {
		StringBuilder json = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				json.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	private TriadicJson createTriadicFromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, TriadicJson.class);
	}

	private Map<String, BinaryContext> generateDiadicsFromTriadics() {
		Map<String, BinaryContext> contexts = new HashMap<>();
		String triName = triadicJsonObject.getName();
		List<String> objects = triadicJsonObject.getObjects();
		List<String> attributes = triadicJsonObject.getAttributes();
		List<List<List<String>>> relations = triadicJsonObject.getRelations();
		int objectsCount = objects.size();
		int attrCount = attributes.size();
		for (String condition : triadicJsonObject.getConditions()) {
			BinaryContext bc = new BinaryContext(triName + "_" + condition, objectsCount, attrCount);
			bc.setAttributes(new Vector<>(attributes));
			bc.setObjects(new Vector<>(objects));
			for (int i = 0; i < objectsCount; i++) {
				List<List<String>> object = relations.get(i);
				for (int j = 0; j < attrCount; j++) {
					List<String> conditions = object.get(j);
					for (String s : conditions) {
						if (condition.equals(s)) {
							try {
								bc.setValueAt(BinaryContext.TRUE, i, j);
							} catch (InvalidTypeException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			contexts.put(condition, bc);
		}
		return contexts;
	}

	private List<TriadicRule> getBinaryRulesAndGenerateBuckets(String condition, BinaryContext bc) {
		ConceptLattice lattice = new ConceptLattice(bc);
		RuleAlgorithm ruleAlgorithm = new InformativeBasisAlgorithm(lattice, 0.0, 1.0);
		Vector<Rule> r = ruleAlgorithm.getRules();

		// wrong support
		List<TriadicRule> biRules = new ArrayList<>();
		for (Rule rule : r) {
			// BasicSet to calculate support.
			BasicSet basicSet = new BasicSet();
			BasicSet antecedent = rule.getAntecedent();
			BasicSet consequence = rule.getConsequence();
			basicSet.addAll(antecedent);
			basicSet.addAll(consequence);
			double support = bc.support(basicSet);

			// TriadicRule
			TriadicRule br = new TriadicRule(antecedent, consequence, condition, support);
			biRules.add(br);

			// The bucket Label is all antecedents without any space.
			String bucketLabel = getBucketLabel(antecedent);
			if (!findBucket(bucketLabel)) {
				buckets.add(new Bucket(bucketLabel));
			}
		}
		return biRules;
	}

	private String getBucketLabel(BasicSet antecedent) {
		// We add the rule to the bucket
		StringBuilder bucketLabel = new StringBuilder();
		for (String s : antecedent) {
			bucketLabel.append(s);
		}
		return bucketLabel.toString();
	}

	private boolean findBucket(String label) {
		// If bucket exists we return it
		for (Bucket bucket : buckets) {
			if (bucket.getLabel().equalsIgnoreCase(label)) {
				return true;
			}
		}
		// If it doesn't exists we return false;
		return false;
	}

	private Bucket findBucketByLabel(String label) {
		for (Bucket bucket : buckets) {
			if (bucket.getLabel().equalsIgnoreCase(label)) {
				return bucket;
			}
		}
		return null;
	}

}
