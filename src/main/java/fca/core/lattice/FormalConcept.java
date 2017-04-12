package fca.core.lattice;

import java.util.Vector;

import fca.core.util.BasicSet;

public class FormalConcept extends DataCel {
	
	private Vector<FormalConcept> children;
	private Vector<FormalConcept> parents;
	private Vector<BasicSet> generators;
	
	private boolean visited;
	
	public FormalConcept(BasicSet e, BasicSet i) {
		super(e, i);
		children = new Vector<FormalConcept>();
		parents = new Vector<FormalConcept>();
		generators = null;
		visited = false;
	}
	
	/**
	 * Return a clone of this by cloning only the extent and the intent
	 * @return the clone {@link FormalConcept} of this
	 */
	public FormalConcept cloneIntExt() {
		return new FormalConcept((BasicSet) getExtent().clone(), (BasicSet) getIntent().clone());
	}
	
	public void addParent(FormalConcept p) {
		parents.add(p);
	}
	
	public void addChild(FormalConcept c) {
		children.add(c);
	}
	
	public void setGenerators(Vector<BasicSet> gen) {
		generators = gen;
	}
	
	public Vector<FormalConcept> getParents() {
		return parents;
	}
	
	public Vector<FormalConcept> getChildren() {
		return children;
	}
	
	public Vector<BasicSet> getGenerators() {
		return generators;
	}
	
	public boolean equals(FormalConcept fc) {
		return (hasSameIntent(fc) && hasSameExtent(fc));
	}
	
	public boolean hasSameIntent(FormalConcept fc) {
		return fc.getIntent().equals(intent);
	}
	
	public boolean hasSameExtent(FormalConcept fc) {
		return fc.getExtent().equals(extent);
	}
	
	public boolean hasBeenVisited() {
		return visited;
	}
	
	/**
	 * @param v
	 */
	public void setVisited(boolean v) {
		visited = v;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = new String("FormalConcept" + " {" + getIntent() + "," + getExtent() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() {
		FormalConcept res = new FormalConcept((BasicSet) extent.clone(), (BasicSet) intent.clone());
		res.children = new Vector<FormalConcept>(children);
		res.parents = new Vector<FormalConcept>(parents);
		res.generators = new Vector<BasicSet>(generators);
		res.visited = visited;
		return res;
	}
	
}