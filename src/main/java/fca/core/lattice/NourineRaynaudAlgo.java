package fca.core.lattice;

import java.util.Iterator;
import java.util.Vector;

import fca.core.context.binary.BinaryContext;
import fca.core.util.BasicSet;
import fca.messages.CoreMessages;

public class NourineRaynaudAlgo {
	
	private BinaryContext context;
	private Vector<BasicSet> basis;
	private Vector<BasicSet> family;
	private Vector<BasicSet> yFamily;
	private Vector<FormalConcept> concepts;
	
	public NourineRaynaudAlgo(BinaryContext binCtx) {
		context = binCtx;
		computeBasis();
		generateFamily();
		buildLattice();
	}
	
	private void computeBasis() {
		basis = new Vector<BasicSet>();
		
		Vector<String> attributes = context.getAttributes();
		for (int i = 0; i < attributes.size(); i++) {
			BasicSet extent = context.getObjectsFor(attributes.elementAt(i));
			basis.add(extent);
		}
	}
	
	private void generateFamily() {
		BasicSet allObjects = new BasicSet();
		allObjects.addAll(context.getObjects());
		BasicSet emptySet = new BasicSet();
		
		family = new Vector<BasicSet>();
		yFamily = new Vector<BasicSet>();
		
		family.add(allObjects);
		yFamily.add(emptySet);
		
		/* Ajustement du supremum */
		for (int i = 0; i < basis.size(); i++) {
			BasicSet basisItem = basis.elementAt(i);
			if (basisItem.equals(allObjects)) {
				BasicSet attribute = new BasicSet();
				attribute.add(context.getAttributeAt(i));
				BasicSet newIntent = (yFamily.elementAt(0)).union(attribute);
				yFamily.setElementAt(newIntent, 0);
			}
		}
		
		for (int i = 0; i < basis.size(); i++) {
			BasicSet basisItem = basis.elementAt(i);
			for (int j = 0; j < family.size(); j++) {
				BasicSet familyItem = family.elementAt(j);
				BasicSet newItem = familyItem.intersection(basisItem);
				
				if (!isInFamily(newItem, family)) {
					family.add(newItem);
					yFamily.add(closedItemSet(newItem));
				} else {
					BasicSet attribute = new BasicSet();
					attribute.add(context.getAttributeAt(i));
					int itemIdx = family.indexOf(newItem);
					
					BasicSet newIntent = (yFamily.elementAt(itemIdx)).union(attribute);
					yFamily.setElementAt(newIntent, itemIdx);
				}
			}
		}
	}
	
	private void buildLattice() {
		concepts = new Vector<FormalConcept>();
		for (int i = 0; i < family.size(); i++) {
			BasicSet extent = family.elementAt(i);
			BasicSet intent = yFamily.elementAt(i);
			FormalConcept concept = new FormalConcept(extent, intent);
			concepts.add(concept);
		}
		
		Vector<Integer> count = new Vector<Integer>();
		for (int i = 0; i < family.size(); i++)
			count.add(new Integer(0));
		
		for (int i = 0; i < family.size(); i++) {
			BasicSet familyItem = family.elementAt(i);
			for (int j = 0; j < basis.size(); j++) {
				BasicSet basisItem = basis.elementAt(j);
				BasicSet modItem = new BasicSet();
				modItem.addAll(basisItem);
				modItem.removeAll(yFamily.elementAt(i));
				
				BasicSet newItem = familyItem.intersection(modItem);
				int idx = family.indexOf(newItem);
				int cntValue = (count.elementAt(idx)).intValue() + 1;
				count.setElementAt(new Integer(cntValue), idx);
				
				int newSize = (yFamily.elementAt(idx)).size();
				int size = (yFamily.elementAt(i)).size();
				if (newSize == (size + cntValue))
					linkConcepts(concepts.elementAt(i), concepts.elementAt(idx));
			}
			
			count = new Vector<Integer>();
			for (int c = 0; c < family.size(); c++)
				count.add(new Integer(0));
		}
	}
	
	private BasicSet closedItemSet(BasicSet objects) {
		if (objects.size() == 0) {
			BasicSet allAttributes = new BasicSet();
			allAttributes.addAll(context.getAttributes());
			return allAttributes;
		}
		
		BasicSet attributes = new BasicSet();
		Iterator<String> it = objects.iterator();
		if (it.hasNext()) {
			String firstObj = it.next();
			attributes.addAll(context.getAttributesFor(firstObj));
		}
		
		while (it.hasNext()) {
			String obj = it.next();
			attributes = attributes.intersection(context.getAttributesFor(obj));
		}
		
		return attributes;
	}
	
	private boolean isInFamily(BasicSet item, Vector<BasicSet> family) {
		for (int i = 0; i < family.size(); i++) {
			BasicSet familyItem = family.elementAt(i);
			if (familyItem.equals(item))
				return true;
		}
		return false;
	}
	
	private void linkConcepts(FormalConcept parent, FormalConcept child) {
		parent.addChild(child);
		child.addParent(parent);
	}
	
	public Vector<FormalConcept> getConcepts() {
		return concepts;
	}
	
	@Override
	public String toString() {
		String result = "\n"; //$NON-NLS-1$
		result += "**********************************************************\n"; //$NON-NLS-1$
		result += CoreMessages.getString("Core.nbConceptsFound") + " : " + concepts.size() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i = 0; i < concepts.size(); i++) {
			FormalConcept concept = concepts.elementAt(i);
			result += "   " + CoreMessages.getString("Core.concept") + " #" + (i + 1) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			result += "   " + CoreMessages.getString("Core.intent") + " : " + concept.getIntent().toString() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			result += "   " + CoreMessages.getString("Core.extent") + " : " + concept.getExtent().toString() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			
			result += "   " + CoreMessages.getString("Core.children") + " :"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Vector<FormalConcept> children = concept.getChildren();
			for (int c = 0; c < children.size(); c++) {
				result += " " + (children.elementAt(c)).getIntent().toString(); //$NON-NLS-1$
			}
			result += "\n"; //$NON-NLS-1$
			
			result += "   " + CoreMessages.getString("Core.parents") + " :"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Vector<FormalConcept> parents = concept.getParents();
			for (int p = 0; p < parents.size(); p++) {
				result += " " + (parents.elementAt(p)).getIntent().toString(); //$NON-NLS-1$
			}
			result += "\n"; //$NON-NLS-1$
		}
		result += "\n**********************************************************\n"; //$NON-NLS-1$
		return result;
	}
}