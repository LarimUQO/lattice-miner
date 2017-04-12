package fca.core.context.binary;

import java.util.Iterator;
import java.util.Vector;

import fca.core.util.BasicSet;

public class Decomposition {
	private static int TRY_COUNT = 49;

	private BinaryContext context;

	private boolean[][] attributeRelations;

	private int preferredClassCount;
	private Vector<String> freeAttributes;

	private Vector<BasicSet>[] classesAtt;

	private Vector<BasicSet>[] classesObj;

	private int[] nbClasses;

	private int[][] classesWeight;

	private int bestDecomposition;

	private double lastAvgLinkCount1;

	private double lastAvgLinkCount2;

	private double lastAvgLinkCount3;

	private double lastAvgLinkCount4;

	private double lastAvgLinkCount5;

	private double currentAvgLinkCount;

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	// tableau de vecteur non typable
	public Decomposition(BinaryContext ctx) {
		context = ctx;
		preferredClassCount = 0;

		int tabSize = 2;
		if (TRY_COUNT == 1)
			tabSize = 1;

		classesAtt = new Vector[tabSize];
		classesObj = new Vector[tabSize];
		nbClasses = new int[tabSize];
		classesWeight = new int[tabSize][ctx.getAttributeCount()];

		attributeRelations = new boolean[ctx.getAttributeCount()][ctx.getAttributeCount()];
		fillAttributeRelations();

		decompose(0);
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	// tableau de vecteur non typable
	public Decomposition(BinaryContext ctx, int classCnt) {
		context = ctx;
		preferredClassCount = classCnt;

		classesAtt = new Vector[TRY_COUNT];
		classesObj = new Vector[TRY_COUNT];
		nbClasses = new int[TRY_COUNT];
		classesWeight = new int[TRY_COUNT][ctx.getAttributeCount()];

		attributeRelations = new boolean[ctx.getAttributeCount()][ctx.getAttributeCount()];
		fillAttributeRelations();

		decompose(0);
	}

	private void fillAttributeRelations() {
		/* Initialisation : un attribut est en relation avec lui-meme seulement */
		for (int i = 0; i < context.getAttributeCount(); i++) {
			for (int j = 0; j < context.getAttributeCount(); j++) {
				if (i == j)
					attributeRelations[i][j] = true;
				else
					attributeRelations[i][j] = false;
			}
		}

		/* Si 2 attributs sont possedes par le meme objet, ils sont en relation */
		for (int i = 0; i < context.getObjectCount(); i++) {
			for (int j = 0; j < context.getAttributeCount(); j++) {
				String value1 = context.getValueAt(i, j);

				/* Recherche d'un attribut possede par l'objet */
				if (value1.equals(BinaryContext.TRUE)) {
					for (int k = j + 1; k < context.getAttributeCount(); k++) {
						String value2 = context.getValueAt(i, k);

						/* Recherche des attributs suivants possedes par l'objet */
						if (value2.equals(BinaryContext.TRUE)) {
							attributeRelations[j][k] = true;
							attributeRelations[k][j] = true;
						}
					}
				}
			}
		}
	}

	private void decompose(int ctxNum) {
		if (preferredClassCount == 0) {
			Vector<String> objects = context.getObjects();
			double totalRelCount = 0;
			for (int i = 0; i < objects.size(); i++)
				totalRelCount += context.getAttributesFor(objects.elementAt(i)).size();

			double nbObjects = objects.size();
			nbClasses[ctxNum] = (int) Math.round(totalRelCount / nbObjects);
			preferredClassCount = nbClasses[ctxNum];
		} else {
			nbClasses[ctxNum] = preferredClassCount;
		}

		initClasses(ctxNum);
		buildClasses(ctxNum);

		//for(int i=1; i<TRY_COUNT; i++){
		lastAvgLinkCount1 = context.getAttributeCount();
		lastAvgLinkCount2 = context.getAttributeCount();
		lastAvgLinkCount3 = context.getAttributeCount();
		lastAvgLinkCount4 = context.getAttributeCount();
		lastAvgLinkCount5 = context.getAttributeCount();
		currentAvgLinkCount = context.getAttributeCount();

		do {
			rebuildClasses(ctxNum);
		} while (currentAvgLinkCount < lastAvgLinkCount1 || currentAvgLinkCount < lastAvgLinkCount2
				|| currentAvgLinkCount < lastAvgLinkCount3 || currentAvgLinkCount < lastAvgLinkCount4
				|| currentAvgLinkCount < lastAvgLinkCount5);

		rebuildClasses(ctxNum);
	}

	private void sortAttributesByWeight(Vector<String> attributes) {
		Vector<Integer> weights = new Vector<Integer>();
		for (int i = 0; i < attributes.size(); i++) {
			String att = attributes.elementAt(i);
			weights.add(new Integer(context.getObjectsFor(att).size()));
		}

		quickSortStrings(attributes, weights, 0, attributes.size() - 1);
	}

	private void quickSortStrings(Vector<String> strings, Vector<Integer> weights, int left, int right) {
		int lHold = left;
		int rHold = right;
		int pivot = (weights.elementAt(left)).intValue();
		String pivotString = strings.elementAt(left);

		while (left < right) {
			while (((weights.elementAt(right)).intValue() <= pivot) && (left < right))
				right--;

			if (left != right) {
				int rightValue = (weights.elementAt(right)).intValue();
				String rightString = strings.elementAt(right);
				weights.setElementAt(new Integer(rightValue), left);
				strings.setElementAt(new String(rightString), left);
				left++;
			}

			while (((weights.elementAt(left)).intValue() >= pivot) && (left < right))
				left++;

			if (left != right) {
				int leftValue = (weights.elementAt(left)).intValue();
				String leftString = strings.elementAt(left);
				weights.setElementAt(new Integer(leftValue), right);
				strings.setElementAt(new String(leftString), right);
				right--;
			}
		}

		weights.setElementAt(new Integer(pivot), left);
		strings.setElementAt(new String(pivotString), left);
		int pivotIdx = left;
		left = lHold;
		right = rHold;

		if (left < pivotIdx)
			quickSortStrings(strings, weights, left, pivotIdx - 1);
		if (right > pivotIdx)
			quickSortStrings(strings, weights, pivotIdx + 1, right);
	}

	private void initClasses(int ctxNum) { //, Vector representants){
		freeAttributes = new Vector<String>();
		freeAttributes.addAll(context.getAttributes());

		classesAtt[ctxNum] = new Vector<BasicSet>();
		classesObj[ctxNum] = new Vector<BasicSet>();

		for (int i = 0; i < nbClasses[ctxNum]; i++) {
			classesAtt[ctxNum].add(new BasicSet());
			classesObj[ctxNum].add(new BasicSet());
			classesWeight[ctxNum][i] = 0;
		}

		sortAttributesByWeight(freeAttributes);
	}

	private void sortClassesByWeight(int ctxNum) {
		Vector<BasicSet> classes = classesAtt[ctxNum];
		Vector<BasicSet> coveredObjects = classesObj[ctxNum];

		quickSortClasses(classes, coveredObjects, 0, classes.size() - 1);
	}

	private void quickSortClasses(Vector<BasicSet> classes, Vector<BasicSet> objects, int left, int right) {
		int lHold = left;
		int rHold = right;
		BasicSet pivot = objects.elementAt(left);
		BasicSet pivotClass = classes.elementAt(left);

		while (left < right) {
			while (((objects.elementAt(right)).size() <= pivot.size()) && (left < right))
				right--;

			if (left != right) {
				BasicSet rightObjects = objects.elementAt(right);
				BasicSet rightClass = classes.elementAt(right);
				objects.setElementAt(rightObjects, left);
				classes.setElementAt(rightClass, left);
				left++;
			}

			while (((objects.elementAt(left)).size() >= pivot.size()) && (left < right))
				left++;

			if (left != right) {
				BasicSet leftObjects = objects.elementAt(left);
				BasicSet leftClass = classes.elementAt(left);
				objects.setElementAt(leftObjects, right);
				classes.setElementAt(leftClass, right);
				right--;
			}
		}

		objects.setElementAt(pivot, left);
		classes.setElementAt(pivotClass, left);
		int pivotIdx = left;
		left = lHold;
		right = rHold;

		if (left < pivotIdx)
			quickSortClasses(classes, objects, left, pivotIdx - 1);
		if (right > pivotIdx)
			quickSortClasses(classes, objects, pivotIdx + 1, right);
	}

	private void buildClasses(int ctxNum) {
		for (int i = 0; i < freeAttributes.size(); i++) {
			String att = freeAttributes.elementAt(i);
			BasicSet objSet = context.getObjectsFor(att);

			sortClassesByWeight(ctxNum);

			boolean found = false;
			int bestClass = classesAtt[ctxNum].size();
			int bestScore = Integer.MAX_VALUE;
			for (int j = 0; j < classesAtt[ctxNum].size() && !found; j++) {
				int score = getScore(att, objSet, ctxNum, j);

				if (score == 0) {
					addAttributeToClass(ctxNum, att, objSet, j);
					found = true;
				} else if (score < bestScore) {
					bestScore = score;
					bestClass = j;
				} else if (score == bestScore
						&& (classesAtt[ctxNum].elementAt(j)).size() < (classesAtt[ctxNum].elementAt(bestClass)).size()) {
					bestScore = score;
					bestClass = j;
				}
			}

			if (!found)
				addAttributeToClass(ctxNum, att, objSet, bestClass);
		}
	}

	private void rebuildClasses(int ctxNum) {
		String[] attributes = new String[context.getAttributeCount()];
		int[] relationCount = new int[context.getAttributeCount()];

		/* Recherche du nombre moyen de relations par attribut */
		int linkedAttributeCount = 0;
		for (int i = 0; i < classesAtt[ctxNum].size(); i++) {
			BasicSet attributeSet = classesAtt[ctxNum].elementAt(i);
			Iterator<String> extAttIt = attributeSet.iterator();

			while (extAttIt.hasNext()) {
				String extAttribute = extAttIt.next();
				int extIdx = context.getAttributeIndex(extAttribute);
				int localLinkCount = 0;
				Iterator<String> intAttIt = attributeSet.iterator();

				while (intAttIt.hasNext()) {
					String intAttribute = intAttIt.next();
					int intIdx = context.getAttributeIndex(intAttribute);
					if (attributeRelations[extIdx][intIdx] == true)
						localLinkCount++;
				}

				linkedAttributeCount += localLinkCount;
				attributes[extIdx] = extAttribute;
				relationCount[extIdx] = localLinkCount;
			}
		}

		lastAvgLinkCount1 = lastAvgLinkCount2;
		lastAvgLinkCount2 = lastAvgLinkCount3;
		lastAvgLinkCount3 = lastAvgLinkCount4;
		lastAvgLinkCount4 = lastAvgLinkCount5;
		lastAvgLinkCount5 = currentAvgLinkCount;
		currentAvgLinkCount = (double) linkedAttributeCount / (double) context.getAttributeCount();

		/* Retrait des attributs avec un nombre de relations dépassant la moyenne */
		freeAttributes = new Vector<String>();
		for (int i = 0; i < classesAtt[ctxNum].size(); i++) {
			BasicSet newAttributeSet = new BasicSet();
			BasicSet newObjectSet = new BasicSet();

			BasicSet attributeSet = classesAtt[ctxNum].elementAt(i);
			Iterator<String> attIt = attributeSet.iterator();

			while (attIt.hasNext()) {
				String attribute = attIt.next();
				int attIdx = context.getAttributeIndex(attribute);
				if (relationCount[attIdx] <= currentAvgLinkCount) {
					newAttributeSet.add(attribute);
					newObjectSet.addAll(context.getObjectsFor(attribute));
				} else
					freeAttributes.add(attribute);
			}

			classesAtt[ctxNum].setElementAt(newAttributeSet, i);
			classesObj[ctxNum].setElementAt(newObjectSet, i);
		}

		buildClasses(ctxNum);
	}

	private int getScore(String att, BasicSet objSet, int ctxNum, int classIdx) {
		BasicSet classObj = classesObj[ctxNum].elementAt(classIdx);
		BasicSet inter = objSet.intersection(classObj);

		if (inter.size() == 0)
			return 0;

		int relationCount = 1;
		int attIdx = context.getAttributeIndex(att);
		BasicSet classAtt = classesAtt[ctxNum].elementAt(classIdx);
		Iterator<String> attIt = classAtt.iterator();

		while (attIt.hasNext()) {
			String currAtt = attIt.next();
			int currIdx = context.getAttributeIndex(currAtt);
			if (attributeRelations[attIdx][currIdx] == true)
				relationCount++;
		}

		return (int) Math.pow(5 * relationCount, 3) * inter.size();
	}

	private void addAttributeToClass(int ctxNum, String att, BasicSet objSet, int classIdx) {
		BasicSet thisClass = classesAtt[ctxNum].elementAt(classIdx);
		thisClass.add(att);

		BasicSet thisClassObjects = classesObj[ctxNum].elementAt(classIdx);
		thisClassObjects.addAll(objSet);
		classesWeight[ctxNum][classIdx] = thisClassObjects.size();
	}

	@SuppressWarnings("unused") //$NON-NLS-1$
	private void findBestDecomposition() {
		int bestTotalWeight = 0;

		for (int i = 0; i < 2; i++) {
			int totalWeight = 0;
			for (int j = 0; j < nbClasses[i]; j++)
				totalWeight += classesWeight[i][j];

			if (totalWeight > bestTotalWeight) {
				bestTotalWeight = totalWeight;
				bestDecomposition = i;
			}
		}
	}

	public Vector<BasicSet> getClasses() {
		Vector<BasicSet> classes = new Vector<BasicSet>();

		for (int i = 0; i < classesAtt[bestDecomposition].size(); i++)
			classes.add(classesAtt[bestDecomposition].elementAt(i));

		return classes;
	}
}