package fca.core.context.nested;

import fca.core.context.binary.BinaryContext;
import fca.core.util.BasicSet;

/**
 * Algorithme d'ordonnancement des niveaux d'un NestedContext, en fonction d'un niveau de
 * classification donné
 * @author Geneviève Roberge
 * @version 1.0
 */
public class SupervisedMultivaluedRankingAlgo extends RankingAlgo {
	
	private int classifIdx;
	
	public SupervisedMultivaluedRankingAlgo(NestedContext ctx, int lev) {
		super(ctx);
		classifIdx = lev;
		orderLevels();
	}
	
	@Override
	protected void orderLevels() {
		buildMultivaluedSimilarityMatrix();
		calcStatistics();
		
		//orderLevelsByStandardDevation();
		orderLevelsByCorrelations();
		//orderLevelsByHybridProcedure();
	}
	
	@Override
	protected void buildMultivaluedSimilarityMatrix() {
		int objCount = context.getObjectCount();
		int levelCount = context.getLevelCount();
		similarityMatrix = new double[objCount * objCount][levelCount];
		averages = new double[levelCount];
		for (int i = 0; i < levelCount; i++)
			averages[i] = 0;
		
		for (int i = 0; i < objCount; i++) {
			for (int j = 0; j < objCount; j++) {
				for (int k = 0; k < levelCount; k++) {
					BinaryContext currentLevel = binCtxList.elementAt(k);
					BasicSet attSet1 = currentLevel.getAttributesFor(i);
					BasicSet attSet2 = currentLevel.getAttributesFor(j);
					BasicSet inter = attSet1.intersection(attSet2);
					double similarity = (double) inter.size() / (double) attSet1.size();
					similarityMatrix[i * objCount + j][k] = similarity;
					averages[k] += similarity; //initialement, la somme est stockée dans averages.
				}
			}
		}
		for (int i = 0; i < levelCount; i++) {
			averages[i] = averages[i] / (objCount * objCount);
		}
	}
	
	@Override
	protected void calcStatistics() {
		int objCount = context.getObjectCount();
		int levelCount = context.getLevelCount();
		standardDeviations = new double[levelCount];
		correlations = new double[levelCount];
		
		for (int i = 0; i < levelCount; i++) {
			double deviationSum = 0.0;
			double covarianceSum = 0.0;
			for (int j = 0; j < objCount * objCount; j++) {
				double deviation = (similarityMatrix[j][i] - averages[i]) * (similarityMatrix[j][i] - averages[i]);
				deviationSum += deviation;
				double covariance = (similarityMatrix[j][i] - averages[i])
						* (similarityMatrix[j][classifIdx] - averages[classifIdx]);
				covarianceSum += covariance;
			}
			standardDeviations[i] = Math.sqrt(deviationSum / (objCount * objCount));
			correlations[i] = (covarianceSum / (objCount * objCount)) / standardDeviations[i];
		}
		
		levels = new String[levelCount];
		for (int i = 0; i < levelCount; i++) {
			levels[i] = (binCtxList.elementAt(i)).getName();
			correlations[i] = correlations[i] / correlations[classifIdx];
		}
	}
	
	@Override
	protected void orderLevelsByStandardDevation() {
		int levelCount = binCtxList.size();
		
		//Bubble sort
		for (int i = 0; i < levelCount - 1; i++) {
			for (int j = i + 1; j < levelCount; j++) {
				if (standardDeviations[i] < standardDeviations[j]) {
					String tempLevel = levels[i];
					double tempDeviation = standardDeviations[i];
					double tempCorrelation = correlations[i];
					
					levels[i] = levels[j];
					standardDeviations[i] = standardDeviations[j];
					correlations[i] = correlations[j];
					
					levels[j] = tempLevel;
					standardDeviations[j] = tempDeviation;
					correlations[j] = tempCorrelation;
				}
			}
		}
	}
	
	@Override
	protected void orderLevelsByCorrelations() {
		int levelCount = binCtxList.size();
		
		//Bubble sort
		for (int i = 0; i < levelCount - 1; i++) {
			for (int j = i + 1; j < levelCount; j++) {
				if (correlations[i] < correlations[j]) {
					String tempLevel = levels[i];
					double tempDeviation = standardDeviations[i];
					double tempCorrelation = correlations[i];
					
					levels[i] = levels[j];
					standardDeviations[i] = standardDeviations[j];
					correlations[i] = correlations[j];
					
					levels[j] = tempLevel;
					standardDeviations[j] = tempDeviation;
					correlations[j] = tempCorrelation;
				}
			}
		}
	}
	
	@Override
	protected void orderLevelsByHybridProcedure() {
		int levelCount = binCtxList.size();
		double deviationAvg = 0.0;
		double correlationAvg = 0.0;
		
		for (int i = 0; i < levelCount; i++) {
			deviationAvg += standardDeviations[i];
			correlationAvg += correlations[i];
		}
		
		deviationAvg = deviationAvg / levelCount;
		correlationAvg = correlationAvg / levelCount;
		
		//Bubble sort pour les déviations standards
		for (int i = 0; i < levelCount - 1; i++) {
			for (int j = i + 1; j < levelCount; j++) {
				if (standardDeviations[i] < standardDeviations[j]) {
					String tempLevel = levels[i];
					double tempDeviation = standardDeviations[i];
					double tempCorrelation = correlations[i];
					
					levels[i] = levels[j];
					standardDeviations[i] = standardDeviations[j];
					correlations[i] = correlations[j];
					
					levels[j] = tempLevel;
					standardDeviations[j] = tempDeviation;
					correlations[j] = tempCorrelation;
				}
			}
		}
		
		int startIdx = -1;
		for (int i = 0; i < levelCount - 1; i++) {
			if (standardDeviations[i] < deviationAvg) {
				startIdx = i;
				break;
			}
		}
		
		/* Bubble sort des correlations à partir de startIdx */
		for (int i = startIdx; i < levelCount - 1; i++) {
			for (int j = i + 1; j < levelCount; j++) {
				if (correlations[i] < correlations[j]) {
					String tempLevel = levels[i];
					double tempCorrelation = correlations[i];
					double tempDeviation = standardDeviations[i];
					
					levels[i] = levels[j];
					correlations[i] = correlations[j];
					standardDeviations[i] = standardDeviations[j];
					
					levels[j] = tempLevel;
					correlations[j] = tempCorrelation;
					standardDeviations[j] = tempDeviation;
				}
			}
		}
	}
}
