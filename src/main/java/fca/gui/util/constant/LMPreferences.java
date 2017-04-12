package fca.gui.util.constant;

import java.util.prefs.Preferences;

import fca.LatticeMiner;

/**
 * Contient toutes les constantes relatives aux préférences du programme Lattice Minner
 * @author Ludovic Thomas
 * @version 1.0
 */
public final class LMPreferences {

	/** Noeud des préférences contenant les fichiers recents */
	public static final String RECENTS = "recents"; //$NON-NLS-1$

	/** Nombre de fichiers recents */
	public static final String NB_RECENTS = "nbRecents"; //$NON-NLS-1$

	/** Dernier repertoire utilisé dans un Open/Save FileChooser */
	public static final String LAST_DIRECTORY = "lastDirectory"; //$NON-NLS-1$

	/** Option : Type de la selection simple */
	public static final String SINGLE_SEL_TYPE = "singleSelType"; //$NON-NLS-1$

	/** Option : Type de la selection multiple */
	public static final String MULT_SEL_TYPE = "multSelType"; //$NON-NLS-1$

	/** Option : Type du contrastes pour la selection */
	public static final String SEL_CONTRAST_TYPE = "selectionContrastType"; //$NON-NLS-1$

	/** Option : Cacher l'infimum s'il est vide ou pas */
	public static final String HIDE_OUT_OF_FOCUS = "hideLabelForOutOfFocusConcept"; //$NON-NLS-1$

	/** Option : Type de label pour les attributs */
	public static final String ATT_LABEL_TYPE = "attLabelType"; //$NON-NLS-1$

	/** Option : Type de label pour les objets */
	public static final String OBJ_LABEL_TYPE = "objLabelType"; //$NON-NLS-1$

	/** Option : Type de label pour les règles */
	public static final String RULES_LABEL_TYPE = "rulesLabelType"; //$NON-NLS-1$

	/** Option : Activer les animations ou pas */
	public static final String ANIMATE_ZOOM = "animateZoom"; //$NON-NLS-1$

	/** Option : Type choisit pour attirer l'attention lors d'une action */
	public static final String FEATURE_TYPE = "attentionFeatureType"; //$NON-NLS-1$

	/** Option : Changer l'intensité des concepts avec le nombre d'objets ou pas */
	public static final String CHANGE_COLOR_INTENSITY = "changeColorIntensity"; //$NON-NLS-1$

	/** Option : Type choisit pour la taille des concepts */
	public static final String CONCEPT_SIZE_TYPE = "conceptSizeType"; //$NON-NLS-1$

	/** Option : Montrer tous les labels des concepts ou pas */
	public static final String SHOW_ALL_CONCEPTS = "showAllConcepts"; //$NON-NLS-1$

	/** Option : Montrer la mini carte du treillis ou pas */
	public static final String SHOW_LATTICE_MAP = "showLatticeMap"; //$NON-NLS-1$

	/**
	 * Le noeud de base de stockage des preferences de Lattice Miner
	 */
	private static final Preferences PREFERENCES_STORE = Preferences.userNodeForPackage(LatticeMiner.class);

	/**
	 * Permet de récupérer les préférences de Lattice Miner
	 * @return l'objet {@link Preferences} des préférences de Lattice Miner
	 */
	public static final Preferences getPreferences() {
		return PREFERENCES_STORE;
	}

	/**
	 * Permet de récupérer le dernier emplacement utilisé pour sauvegarder/ouvrir les fichiers
	 * @return la sauvegarde du dernier emplacement utilisé ou <code>null</code> s'il n'y a pas de
	 *         sauvegarde précédente
	 */
	public static final String getLastDirectory() {
		String lastPath = getPreferences().get(LAST_DIRECTORY, ""); //$NON-NLS-1$
		if (lastPath != "") //$NON-NLS-1$
			return lastPath;
		else
			return null;
	}

	/**
	 * Permet de sauvegarder le nouvel emplacement utilisé pour sauvegarder/ouvrir les fichiers
	 * @param directory nouvel emplacement utilisé
	 */
	public static final void setLastDirectory(String directory) {
		getPreferences().put(LAST_DIRECTORY, directory);
	}

}
