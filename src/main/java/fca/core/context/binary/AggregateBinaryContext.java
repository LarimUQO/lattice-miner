package fca.core.context.binary;
import fca.exception.AlreadyExistsException;

import java.util.Vector;

public class AggregateBinaryContext extends BinaryContext {
	private Vector<Integer> objectsFrequencies;
	
	public AggregateBinaryContext(String name) {
		super(name);
		this.objectsFrequencies = new Vector<Integer>();
	}
	
	public AggregateBinaryContext(String name, int objNb, int attNb) {
		super(name, objNb, attNb);
		this.objectsFrequencies = new Vector<Integer>();
	}
	
	public void addObject() {
		super.addObject();
		objectsFrequencies.add(0);		
	}
	
	public void addObject(String obj) throws AlreadyExistsException {
		super.addObject(obj);
		objectsFrequencies.add(0);	
	}
	
	public void addObjectsCopies(String origName, Vector<String> names) throws AlreadyExistsException {
		super.addObjectCopies(origName, names);
		for (int i = 0; i < names.size(); i++)
			objectsFrequencies.add(0);
	}
	
	public void setFrequencyAt(int value, int objIdx) {
		objectsFrequencies.set(objIdx, value);
	}
	
	public int getFrequencyAt(int objIdx) {
		return objectsFrequencies.get(objIdx);
	}
	
	public Vector<Integer> getFrequencies() {
		return objectsFrequencies;
	}
	
	public int getRelativeFrequencyAt(int objIdx) {
		int total = 0;
		for (int i = 0; i < objectsFrequencies.size(); i++) {
			total += objectsFrequencies.get(i);
		}
		return objectsFrequencies.get(objIdx) / total;
	}	
	
}