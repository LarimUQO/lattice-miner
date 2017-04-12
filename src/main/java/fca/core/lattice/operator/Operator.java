package fca.core.lattice.operator;

/**
 * Cette classe abstraite {@link Operator} permet d'appeler et de créer rapidement une opération
 * grâce aux possibilités des templates et eux typages des méthodes qui s'en suivent. Un simple
 * appel à la méthode <code>perform(E)<code> permet de réaliser l'opération.
 * @param <D> type des données en entrées (à la création de l'opérateur)
 * @param <E> type du paramètre de la requête
 * @param <R> type du résultat de la requête
 * @author Ludovic Thomas
 * @version 1.0
 */
public abstract class Operator<D, E, R> {

	/**
	 * Données utiles pour l'opération
	 */
	protected D data;

	/**
	 * Résultat de l'opération
	 */
	protected R result;

	/**
	 * Nom de l'opération
	 */
	private String operatorName;

	/**
	 * Version de l'opération
	 */
	private double operatorVersion;

	/** Version par défaut d'une opération */
	private final static double INIT_VERSION = 1.0;

	/**
	 * Constructeur d'une opération
	 * @param data les données de bases de l'opération
	 * @param operatorName le nom de l'opération
	 */
	public Operator(D data, String operatorName) {
		this.operatorName = operatorName;
		this.operatorVersion = INIT_VERSION;
		this.data = data;
		this.result = null;
	}

	/**
	 * Exécute l'algorithme pour une requête spécifiaue via le paramètre
	 * @param entry la donnée de la requête
	 * @return la résultat de la requête
	 */
	public abstract R perform(E entry);

	/**
	 * @return le nom de l'opération
	 */
	public String getOperatorName() {
		return operatorName;
	}

	/**
	 * @return la version de l'opération
	 */
	public double getOperatorVersion() {
		return operatorVersion;
	}

}
