package fca.core.context.triadic;

import java.util.List;

public class TriadicJson {
	String name;
	List<String> objects;
	List<String> attributes;
	List<String> conditions;
	String supportMin;
	List<List<List<String>>> relations;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getObjects() {
		return objects;
	}

	public void setObjects(List<String> objects) {
		this.objects = objects;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}

	public List<String> getConditions() {
		return conditions;
	}

	public void setConditions(List<String> conditions) {
		this.conditions = conditions;
	}

	public String getSupportMin() {
		return supportMin;
	}

	public void setSupportMin(String supportMin) {
		this.supportMin = supportMin;
	}

	public List<List<List<String>>> getRelations() {
		return relations;
	}

	public void setRelations(List<List<List<String>>> relations) {
		this.relations = relations;
	}

	@Override
	public String toString() {
		return "TriadicJson{" +
				"name='" + name + '\'' +
				", objects=" + objects +
				", attributes=" + attributes +
				", conditions=" + conditions +
				", supportMin='" + supportMin + '\'' +
				", relations=" + relations +
				'}';
	}
}
