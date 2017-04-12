package fca.core.lattice;

import java.util.Vector;

import fca.core.context.binary.BinaryContext;

class IncrementalAlgoProxy {
	//private BinaryContext context;
	
	private String[] contextObjects;
	
	private String[] contextAttributes;
	
	private boolean[][] contextMatrix;
	private Vector<FormalConcept> concepts;
	
	public IncrementalAlgoProxy(BinaryContext context) {
		System.loadLibrary("fca_core_lattice_IncrementalAlgoProxy"); //$NON-NLS-1$
		
		contextObjects = new String[context.getObjectCount()];
		for (int i = 0; i < context.getObjectCount(); i++)
			contextObjects[i] = context.getObjectAt(i);
		
		contextAttributes = new String[context.getAttributeCount()];
		for (int i = 0; i < context.getAttributeCount(); i++)
			contextAttributes[i] = context.getAttributeAt(i);
		
		contextMatrix = new boolean[context.getObjectCount()][context.getAttributeCount()];
		for (int i = 0; i < context.getObjectCount(); i++)
			for (int j = 0; j < context.getAttributeCount(); j++)
				contextMatrix[i][j] = (context.getValueAt(i, j) == BinaryContext.TRUE);
		
		concepts = new Vector<FormalConcept>();
		constructLattice();
		contextObjects = null;
		contextAttributes = null;
		contextMatrix = null;
	}
	
	public native void constructLattice();
	
	/**
	 * @return les objets du contexte
	 */
	public String[] getContextObjects() {
		return contextObjects;
	}
	
	/**
	 * @return les attributs du contexte
	 */
	public String[] getContextAttributes() {
		return contextAttributes;
	}
	
	/**
	 * @return les données du contexte
	 */
	public boolean[][] booleanGetContextMatrix() {
		return contextMatrix;
	}
	
	public void setLink(int childPos, int parentPos) {
		FormalConcept child = concepts.elementAt(childPos);
		FormalConcept parent = concepts.elementAt(parentPos);
		child.addParent(parent);
		parent.addChild(child);
	}
	
	public Vector<FormalConcept> getConcepts() {
		return concepts;
	}
	
	public int getConceptCount() {
		return concepts.size();
	}
	
}