package fca.gui.context.table;

import java.awt.event.KeyListener;
import java.util.Vector;

import fca.core.context.binary.AggregateBinaryContext;
import fca.gui.context.table.model.BinaryContextTableModel;

public class AggregatedBinaryContextTable extends BinaryContextTable implements KeyListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4020810606592481524L;
	private Vector<Integer> frequencies;
	
	/**
	 * Constructeur.
	 * @param abc Le {@link AggregateBinaryContext} de cette table
	 */
	public AggregatedBinaryContextTable(AggregateBinaryContext abc) {
		super(new BinaryContextTableModel(abc));
		frequencies = abc.getFrequencies();
	}
	
	public int getFrequencyAt(int objIdx) {
		return frequencies.get(objIdx);
	}
	
	public Vector<Integer> getFrequencies() {
		return frequencies;
	}
}