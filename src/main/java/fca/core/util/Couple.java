package fca.core.util;

/**
 * Represente un doublet de donnees
 * @param <T1> type du premier element du doublet
 * @param <T2> type du deuxieme element du doublet
 * @author Ludovic Thomas
 * @version 1.0
 */
public class Couple<T1, T2> {
	
	/**
	 * Le premier element du doublet
	 */
	protected T1 first;
	
	/**
	 * Le deuxieme element du doublet
	 */
	protected T2 second;
	
	/**
	 * Construit un doublet
	 * @param first le premier element du doublet
	 * @param second le deuxieme element du doublet
	 */
	public Couple(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}
	
	/**
	 * @return le premier element du doublet
	 */
	public T1 getFirst() {
		return first;
	}
	
	/**
	 * Change le premier element du doublet
	 * @param first le nouveau premier element du doublet
	 */
	public void setFirst(T1 first) {
		this.first = first;
	}
	
	/**
	 * @return le deuxieme element du doublet
	 */
	public T2 getSecond() {
		return second;
	}
	
	/**
	 * Change le deuxieme element du doublet
	 * @param second le nouveau deuxieme element du doublet
	 */
	public void setSecond(T2 second) {
		this.second = second;
	}
	
}
