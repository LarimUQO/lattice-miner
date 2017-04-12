package fca.core.lattice;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import fca.core.context.binary.BinaryContext;
import fca.core.util.BasicSet;
import fca.messages.CoreMessages;

public class BordatAlgo {
	
	private BinaryContext context;
	private Vector<FormalConcept> initialConcepts;
	private Vector<FormalConcept> concepts;
	
	private int attCount;
	
	private int objCount;
	
	private Hashtable<String, Integer> objects;
	
	private FormalConcept topConcept;
	
	private FormalConcept bottomConcept;
	
	public BordatAlgo(BinaryContext binCtx) {
		context = binCtx;
		concepts = new Vector<FormalConcept>();
		initialConcepts = new Vector<FormalConcept>();
		
		attCount = context.getAttributeCount();
		objCount = context.getObjectCount();
		objects = new Hashtable<String, Integer>(objCount);
		
		findInitialConcepts();
		for (int i = 0; i < initialConcepts.size(); i++)
			objects.put(((initialConcepts.elementAt(i)).getExtent().first()), new Integer(i));
		
		findConcepts();
	}
	
	public void findInitialConcepts() {
		for (int i = 0; i < context.getObjects().size(); i++) {
			String currObj = context.getObjectAt(i);
			BasicSet objs = new BasicSet();
			objs.add(context.getObjects().elementAt(i));
			
			BasicSet atts = context.getAttributesFor(currObj);
			FormalConcept newConcept = new FormalConcept(objs, atts);
			
			initialConcepts.add(newConcept);
		}
	}
	
	public void findConcepts() {
		findBottomConcept();
		findTopConcept();
		
		//BasicSet intent = topConcept.getIntent();
		
		Hashtable<String, FormalConcept> conceptHash = new Hashtable<String, FormalConcept>();
		conceptHash.put(topConcept.getExtent().toString(), topConcept);
		
		LinkedList<FormalConcept> candidates = new LinkedList<FormalConcept>();
		candidates.addLast(topConcept);
		
		do {
			FormalConcept concept = candidates.getFirst();
			Vector<FormalConcept> lowerCover = getLowerCover(concept.getExtent(), concept.getIntent());
			
			for (int i = 0; i < lowerCover.size(); i++) {
				FormalConcept lowerConcept = lowerCover.elementAt(i);
				
				FormalConcept child = conceptHash.get(lowerConcept.getExtent().toString());
				if (child == null) {
					child = new FormalConcept(lowerConcept.getExtent(), lowerConcept.getIntent());
					conceptHash.put(child.getExtent().toString(), child);
					candidates.addLast(child);
				}
				
				if (!child.getIntent().equals(concept.getIntent())) {
					child.addParent(concept);
					concept.addChild(child);
				}
			}
			
			concepts.add(concept);
			candidates.removeFirst();
			
		} while (candidates.isEmpty() == false);
	}
	
	public void findTopConcept() {
		BasicSet extent = new BasicSet();
		BasicSet intent = new BasicSet();
		
		if (initialConcepts.size() > 0) {
			FormalConcept firstConcept = initialConcepts.elementAt(0);
			intent.addAll(firstConcept.getIntent());
		}
		
		for (int i = 1; i < initialConcepts.size(); i++) {
			FormalConcept currConcept = initialConcepts.elementAt(i);
			intent = currConcept.getIntent().intersection(intent);
		}
		
		extent.addAll(context.getObjects());
		
		topConcept = new FormalConcept(extent, intent);
	}
	
	public void findBottomConcept() {
		BasicSet extent = new BasicSet();
		BasicSet intent = new BasicSet();
		
		if (initialConcepts.size() > 0) {
			FormalConcept firstConcept = initialConcepts.elementAt(0);
			extent.addAll(firstConcept.getExtent());
		}
		
		for (int i = 1; i < initialConcepts.size(); i++) {
			FormalConcept currConcept = initialConcepts.elementAt(i);
			extent = currConcept.getExtent().intersection(extent);
		}
		
		intent.addAll(context.getAttributes());
		
		bottomConcept = new FormalConcept(extent, intent);
	}
	
	public Vector<FormalConcept> getLowerCover(BasicSet extent, BasicSet intent) {
		Vector<FormalConcept> lowerCover = new Vector<FormalConcept>();
		BasicSet allAtts = (BasicSet) intent.clone();
		
		FormalConcept firstObj;
		Iterator<String> it = extent.iterator();
		while ((firstObj = getFirstObject(allAtts, it)) != null) {
			BasicSet objIntent = firstObj.getIntent();
			BasicSet objExtent = firstObj.getExtent();
			
			while (it.hasNext()) {
				FormalConcept nextObj = (getNextObject(it));
				
				BasicSet tempIntent = nextObj.getIntent().intersection(objIntent);
				if (!(allAtts.containsAll(tempIntent))) {
					objExtent = objExtent.union(nextObj.getExtent());
					objIntent = objIntent.intersection(nextObj.getIntent());
				}
			}
			
			if (objIntent.intersection(allAtts).equals(intent))
				lowerCover.add(new FormalConcept(objExtent, objIntent));
			
			allAtts = allAtts.union(objIntent);
			it = extent.iterator();
		}
		
		//Add the bottom concept to the lower cover
		if ((lowerCover.size() == 0) && (intent.size() != attCount))
			lowerCover.add(bottomConcept);
		
		return lowerCover;
	}
	
	private FormalConcept getFirstObject(BasicSet intent, Iterator<String> it) {
		FormalConcept first = null;
		
		while (it.hasNext()) {
			String currObj = it.next();
			int numObj = (objects.get(currObj)).intValue();
			first = initialConcepts.elementAt(numObj);
			
			if (!intent.containsAll(first.getIntent()))
				break;
			
			first = null;
		}
		
		FormalConcept firstConcept = null;
		if (first != null)
			firstConcept = new FormalConcept((BasicSet) first.getExtent().clone(), (BasicSet) first.getIntent().clone());
		
		return firstConcept;
	}
	
	private FormalConcept getNextObject(Iterator<String> it) {
		FormalConcept nextConcept = null;
		
		if (it.hasNext()) {
			String nextObject = it.next();
			int numObj = (objects.get(nextObject)).intValue();
			
			FormalConcept concept = initialConcepts.elementAt(numObj);
			nextConcept = new FormalConcept((BasicSet) concept.getExtent().clone(),
					(BasicSet) concept.getIntent().clone());
		}
		
		return nextConcept;
	}
	
	public Vector<FormalConcept> getConcepts() {
		return concepts;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
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