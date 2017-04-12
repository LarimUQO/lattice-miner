package fca.core.context.nested;

import java.util.Vector;

import fca.core.context.binary.BinaryContext;

/**
 * Algorithme d'ordonnancement des niveaux d'un NestedContext
 * @author Geneviève Roberge
 * @version 1.0
 */
public abstract class RankingAlgo {

	protected NestedContext context;
	protected Vector<BinaryContext> binCtxList;

	protected double[][] similarityMatrix;

	protected String[] levels;

	protected double[] averages;

	protected double[] standardDeviations;

	protected double[] correlations;

	public RankingAlgo(NestedContext ctx) {
		context = ctx;
		binCtxList = context.convertToBinaryContextList();
	}

	protected abstract void orderLevels();

	protected abstract void buildMultivaluedSimilarityMatrix();

	protected abstract void calcStatistics();

	protected abstract void orderLevelsByStandardDevation();

	protected abstract void orderLevelsByCorrelations();

	protected abstract void orderLevelsByHybridProcedure();

	public String[] getOrdering() {
		return levels;
	}
}
