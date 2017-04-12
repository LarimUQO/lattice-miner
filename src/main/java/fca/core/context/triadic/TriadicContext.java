package fca.core.context.triadic;

import fca.core.context.binary.BinaryContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class TriadicContext extends BinaryContext {


	public TriadicContext(String name, Vector<String> objects, Vector<String> attributes, Vector<Vector<String>> values) {
		super(name, objects, attributes, values);
	}

	public static TriadicContext getFlatBinaryContextFromTriadic(TriadicJson json) {

		final String SPLIT_CHAR = "-";

		Vector<String> binaryObjects = new Vector<>();
		Vector<String> binaryAttr = new Vector<>();
		Vector<Vector<String>> binaryValues = new Vector<>();

		List<String> jsonObjects = json.getObjects();
		List<String> jsonAttributes = json.getAttributes();
		List<String> jsonConditions = json.getConditions();
		List<List<List<String>>> jsonRelations = json.getRelations();

		/* We store the original attributes in a map so we can get the position of the value by using the map */
		Map<String, Integer> attributePosMap = new HashMap<>();
		for (int i = 0; i < jsonAttributes.size(); i++) {
			attributePosMap.put(jsonAttributes.get(i), Integer.valueOf(i));
		}

		for (int i = 0; i < jsonObjects.size(); i++) {
			binaryObjects.add(jsonObjects.get(i));
		}

		for (int i = 0; i < jsonAttributes.size(); i++) {
			for (int j = 0; j < jsonConditions.size(); j++) {
				binaryAttr.add(jsonAttributes.get(i) + SPLIT_CHAR + jsonConditions.get(j));
			}
		}

		for (int i = 0; i < binaryObjects.size(); i++) {
			Vector<String> currObj = new Vector<>();
			for (int j = 0; j < binaryAttr.size(); j++) {
				String attName = binaryAttr.get(j);
				String leftSide = attName.split(SPLIT_CHAR)[0];
				String rightSide = attName.split(SPLIT_CHAR)[1];
				List<String> relations = jsonRelations.get(i).get(attributePosMap.get(leftSide).intValue());
				boolean containsRel = relations.contains(rightSide);
				if (containsRel) {
					currObj.add(BinaryContext.TRUE);
				} else {
					currObj.add(BinaryContext.FALSE);
				}
			}
			binaryValues.add(currObj);
		}

		return new TriadicContext(json.getName(), binaryObjects, binaryAttr, binaryValues);
	}
}
