package fca.core.lattice;

import java.util.Vector;

import fca.core.context.binary.BinaryContext;

class IncrementalAlgo {
	
	private BinaryContext context;
	
	private Vector<FormalConcept> concepts;
	
	public IncrementalAlgo(BinaryContext ctx) {
		context = ctx;
		callConstructLattice();
	}
	
	private void callConstructLattice() {
		IncrementalAlgoProxy proxy = new IncrementalAlgoProxy(context);
		concepts = proxy.getConcepts();
	}
	
	public Vector<FormalConcept> getConcepts() {
		return concepts;
	}
}