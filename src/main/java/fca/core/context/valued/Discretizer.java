package fca.core.context.valued;

import java.util.Vector;

/**
 * Classe permettant de trouver un discretisation associee a un ensemble de valeurs
 * @author Genevieve Roberge
 * @version 1.0
 */
public class Discretizer {
	private Vector<Double> values; //Ensemble ordonne des valeurs a discretiser
	private Vector<Integer> frequencies; //Frequence de chacune des valeurs a discretiser
	private Vector<Double> splitPoints; //Valeurs des points de coupure pour les intervalles
	private Vector<Double> intervals; //Ensemble ordonne des points de coupure
	
	public Discretizer(Vector<Double> val, Vector<Integer> freq) {
		values = val;
		frequencies = freq;
		intervals = null;
		discretize();
	}
	
	/**
	 * Execute la discretisation sur l'ensemble des valeurs entrees
	 */
	private void discretize() {
		splitPoints = new Vector<Double>();
		splitPoints.add(new Double((values.elementAt(0)).doubleValue()));
		splitPoints.add(new Double((values.elementAt(values.size() - 1)).doubleValue() + 1.0));
		
		/*
		 * Le premier intervalle debute au premier index des valeurs et se termine au dernier index
		 * des valeurs
		 */
		Interval firstInterval = new Interval(0, values.size());
		int totalFreq = firstInterval.getFrequency();
		
		/* Recherche du meilleur point de coupure dans le premier intervalle */
		HeapItem firstSplit = getBestSplit(firstInterval);
		if (firstSplit == null)
			return;
		
		/* Construction de la file de priorite pour le traitement des intervalles */
		MaxHeap heap = new MaxHeap();
		heap.add(firstSplit);
		
		/*
		 * Les coupures sont faites jusqu'a ce que le plus grand intervalle contienne 20% des objets
		 * ou jusqu'a ce que 15 intervalles aient ete construits
		 */
		int iteration = 1;
		while (iteration < 15 && heap.getMax() != null && heap.getMax().interval.getFrequency() > (0.2 * totalFreq)) {
			/* Retire l'element le plus prioritaire pour le traitement (meilleur point de coupure) */
			HeapItem nextSplit = heap.getMax();
			heap.removeMax();
			splitPoints.add(new Double((values.elementAt(nextSplit.splitIdx)).doubleValue()));
			
			/*
			 * Traitement de l'intervalle gauche nouvellement cree : ajout d'un point de coupure
			 * dans la file de priorite
			 */
			Interval leftInterval = new Interval(nextSplit.interval.getStartIdx(), nextSplit.splitIdx);
			HeapItem leftSplit = getBestSplit(leftInterval);
			if (leftSplit != null)
				heap.add(leftSplit);
			
			/*
			 * Traitement de l'intervalle gauche nouvellement cree : ajout d'un point de coupure
			 * dans la file de priorite
			 */
			Interval rightInterval = new Interval(nextSplit.splitIdx, nextSplit.interval.getEndIdx());
			HeapItem rightSplit = getBestSplit(rightInterval);
			if (rightSplit != null)
				heap.add(rightSplit);
			
			iteration++;
		}
	}
	
	/**
	 * Recherche le meilleur point de coupure dans l'intervalle donne
	 * @param interval L'Interval a traiter
	 * @return Un HeapItem contenant l'intervalle, son meilleur point de coupure et le score associe
	 *         a ce point de coupure
	 */
	private HeapItem getBestSplit(Interval interval) {
		int start = interval.getStartIdx();
		int end = interval.getEndIdx();
		
		int bestSplit = -1;
		double bestScore = -100000.0;
		for (int i = start + 1; i < end; i++) {
			double score = getScore(interval, i);
			if (score > bestScore) {
				bestSplit = i;
				bestScore = score;
			}
		}
		
		/* Un element est retourne seulement si un point de coupure a ete trouve */
		if (bestScore > -100000.0)
			return new HeapItem(interval, bestScore, bestSplit);
		return null;
	}
	
	/**
	 * Calcule le score associe a un point de coupure dans un intervalle
	 * @param interval L'Interval auquel appartient le point de coupure
	 * @param split L'entier contenant l'index du point de coupure (dans l'ensemble des valeurs)
	 * @return Un double contenant le score associe au point de coupure donne
	 */
	private double getScore(Interval interval, int split) {
		int start = interval.getStartIdx();
		int end = interval.getEndIdx();
		//int totalFreq = interval.getFrequency();
		
		int leftFreq = 0;
		for (int i = start; i < split; i++)
			leftFreq += (frequencies.elementAt(i)).intValue();
		
		int rightFreq = 0;
		for (int i = split; i < end; i++)
			rightFreq += (frequencies.elementAt(i)).intValue();
		
		return (10 * (leftFreq + rightFreq) - Math.abs(leftFreq - rightFreq));
	}
	
	/**
	 * Trie un tableau de doubles (quick sort)
	 * @param tab Le Vector de doubles a trier
	 * @param left L'entier contenant l'index gauche ou debute le tri
	 * @param right L'entier contenant l'index droit ou se termine le tri
	 */
	private void sort(Vector<Double> tab, int left, int right) {
		if (right > left) {
			double val = (tab.elementAt(right)).doubleValue();
			int pivot = left - 1;
			
			for (int i = left; i < right; i++) {
				if ((tab.elementAt(i)).doubleValue() <= val) {
					pivot++;
					Double temp = new Double((tab.elementAt(i)).doubleValue());
					tab.setElementAt(new Double((tab.elementAt(pivot)).doubleValue()), i);
					tab.setElementAt(temp, pivot);
				}
			}
			
			pivot++;
			Double temp = new Double((tab.elementAt(right)).doubleValue());
			tab.setElementAt(new Double((tab.elementAt(pivot)).doubleValue()), right);
			tab.setElementAt(temp, pivot);
			
			sort(tab, left, pivot - 1); /* Tri rapide à gauche */
			sort(tab, pivot + 1, right); /* Tri rapide à droite */
		}
	}
	
	/**
	 * Retourne les intervalles calcules par la discretisation
	 * @return Un Vector de Double contenant la suite ordonnee des points de coupure
	 */
	public Vector<Double> getIntervals() {
		if (intervals == null) {
			intervals = new Vector<Double>();
			intervals.addAll(splitPoints);
			sort(intervals, 0, intervals.size() - 1);
		}
		
		return intervals;
	}
	
	/**
	 * @author Geneviève Roberge
	 * @version 1.0
	 */
	private class Interval {
		private int startIdx;
		private int endIdx;
		private int frequency;
		private double width;
		
		public Interval(int start, int end) {
			startIdx = start;
			endIdx = end;
			frequency = 0;
			for (int i = startIdx; i < endIdx; i++)
				frequency += (frequencies.elementAt(i)).intValue();
			width = (values.elementAt(endIdx - 1)).doubleValue() - (values.elementAt(startIdx)).doubleValue() + 1;
		}
		
		/**
		 * @return le debut
		 */
		public int getStartIdx() {
			return startIdx;
		}
		
		/**
		 * @return la fin
		 */
		public int getEndIdx() {
			return endIdx;
		}
		
		/**
		 * @return la frequence
		 */
		public int getFrequency() {
			return frequency;
		}
		
		/**
		 * @return la largeur
		 */
		public double getWidth() {
			return width;
		}
	}
	
	/**
	 * @author Geneviève Roberge
	 * @version 1.0
	 */
	private class HeapItem {
		
		private Interval interval;
		private double score;
		private int splitIdx;
		
		public HeapItem(Interval inter, double sc, int split) {
			interval = inter;
			score = sc;
			splitIdx = split;
		}
	}
	
	private class MaxHeap {
		private Vector<HeapItem> items;
		
		public MaxHeap() {
			items = new Vector<HeapItem>();
		}
		
		public void add(HeapItem item) {
			items.add(item);
			int idx = items.size() - 1;
			while (getParent(idx) > -1 && getValueAt(getParent(idx)) < getValueAt(idx)) {
				int parentIdx = getParent(idx);
				HeapItem temp = items.elementAt(idx);
				items.setElementAt(items.elementAt(parentIdx), idx);
				items.setElementAt(temp, parentIdx);
				idx = parentIdx;
			}
		}
		
		public HeapItem getMax() {
			if (items.size() > 0)
				return items.elementAt(0);
			return null;
		}
		
		public void removeMax() {
			if (items.size() > 0) {
				items.setElementAt(items.elementAt(items.size() - 1), 0);
				items.removeElementAt(items.size() - 1);
				setItemAt(0);
			}
		}
		
		private void setItemAt(int idx) {
			if (items.size() > idx) {
				int max = idx;
				if (items.size() > getLeft(idx) && getValueAt(getLeft(idx)) > getValueAt(idx))
					max = getLeft(idx);
				if (items.size() > getRight(idx) && getValueAt(getRight(idx)) > getValueAt(idx))
					max = getRight(idx);
				
				if (max != idx) {
					HeapItem temp = items.elementAt(idx);
					items.setElementAt(items.elementAt(max), idx);
					items.setElementAt(temp, max);
					setItemAt(max);
				}
			}
		}
		
		private double getValueAt(int idx) {
			if (items.size() > idx)
				return (items.elementAt(idx)).score;
			return Double.MIN_VALUE;
		}
		
		private int getLeft(int idx) {
			return ((idx + 1) * 2) - 1;
		}
		
		private int getRight(int idx) {
			return ((idx + 1) * 2);
		}
		
		private int getParent(int idx) {
			return (((idx + 1) / 2)) - 1;
		}
	}
}