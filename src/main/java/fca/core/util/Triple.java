package fca.core.util;

/**
 * Represente un triplet de donnees
 * @param <T1> type du premier element du triplet
 * @param <T2> type du deuxieme element du triplet
 * @param <T3> type du troisieeme element du triplet
 * @author Ludovic Thomas
 * @version 1.0
 */
public class Triple<T1, T2, T3> extends Couple<T1, T2> {
	
	/**
	 * Le troisieme element du triplet elementType="fca.lattice.graphical.GraphicalConcept"
	 */
	protected T3 third;
	
	/**
	 * Construit un triplet
	 * @param first le premier element du triplet
	 * @param second le deuxieme element du triplet
	 * @param third le troisieme element du triplet
	 */
	public Triple(T1 first, T2 second, T3 third) {
		super(first, second);
		this.third = third;
	}
	
	/**
	 * @return le troisieme element du triplet
	 */
	public T3 getThird() {
		return third;
	}
	
	/**
	 * Change le troisieme element du triplet
	 * @param third le nouveau troisieme element du triplet
	 */
	public void setThird(T3 third) {
		this.third = third;
	}
	
}
