package fca.gui.util.constant;

import javax.swing.ImageIcon;

/**
 * Contient toutes les constantes relatives aux icones du programme Lattice Minner
 * @author Ludovic Thomas
 * @version 1.0
 */
public final class LMIcons {

	/* CONSTANTES GENERALES */

	/** Chemin des icones */
	private static final String PATH_ICONS = "/" + "icon" + "/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/** Extension des icones */
	private static final String EXTENSION_ICONS = ".png"; //$NON-NLS-1$

	/** Le singleton représentant {@link LMIcons} */
	private static LMIcons SINGLETON = null;

	/**
	 * Constructeur privé de l'instance {@link LMIcons}
	 */
	private LMIcons() {
		SINGLETON = this;
	}

	/**
	 * Permet de recupérer, ou de créer s'il n'existe pas, une unique instance de {@link LMIcons}
	 * @return l'instance unique de {@link LMIcons}
	 */
	public static LMIcons getLMIcons() {
		if (SINGLETON == null) {
			SINGLETON = new LMIcons();
		}
		return SINGLETON;
	}

	/* METHODES D'ICONES DE NOEUDS */

	/**
	 * @return L'ImageIcon d'ajout d'attribut
	 */
	public static ImageIcon getAddAttribute() {
		return getImagePathExtension("add_attribute"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon d'ajout d'objet
	 */
	public static ImageIcon getAddObject() {
		return getImagePathExtension("add_object"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de supression d'attribut
	 */
	public static ImageIcon getDelAttribute() {
		return getImagePathExtension("del_attribute"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de supression d'objet
	 */
	public static ImageIcon getDelObject() {
		return getImagePathExtension("del_object"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon d'annulation
	 */
	public static ImageIcon getCancel() {
		return getImagePathExtension("cancel"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de fermeture d'un contexte
	 */
	public static ImageIcon getCloseContext() {
		return getImagePathExtension("close_ctx"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de duplication de fenetre
	 */
	public static ImageIcon getDuplicate() {
		return getImagePathExtension("duplicate"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de recherche
	 */
	public static ImageIcon getFind() {
		return getImagePathExtension("find"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de zoom ajuster a la selection
	 */
	public static ImageIcon getFitZoomToSelection() {
		return getImagePathExtension("fitSelection"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon d'ouverture de dossier
	 */
	public static ImageIcon getOpenFolder() {
		return getImagePathExtension("folder_open"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de modification de la structure
	 */
	public static ImageIcon getModifyTools() {
		return getImagePathExtension("modifyStructure"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de creation de context binaire
	 */
	public static ImageIcon getNewBinContext() {
		return getImagePathExtension("new_bin_ctx"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de retour au zoom de ratio 1:1
	 */
	public static ImageIcon getNoZoom() {
		return getImagePathExtension("noZoom"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de OK
	 */
	public static ImageIcon getOK() {
		return getImagePathExtension("ok"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de precedent
	 */
	public static ImageIcon getPrevious() {
		return getImagePathExtension("previous"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de suivant
	 */
	public static ImageIcon getNext() {
		return getImagePathExtension("next"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de capture d'écran
	 */
	public static ImageIcon getPrintScreen() {
		return getImagePathExtension("printscreen"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de redo
	 */
	public static ImageIcon getRedo() {
		return getImagePathExtension("redo"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de sauvegarder tout
	 */
	public static ImageIcon getSave() {
		return getImagePathExtension("save_all"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de treillis
	 */
	public static ImageIcon getShowLattice() {
		return getImagePathExtension("show_lattice"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon petite des règles
	 */
	public static ImageIcon getShowRulesLittle() {
		return getImagePathExtension("show_rules_little"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon grande des règles
	 */
	public static ImageIcon getShowRulesBig() {
		return getImagePathExtension("show_rules_big"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de tri vers le bas
	 */
	public static ImageIcon getSortDown() {
		return getImagePathExtension("sortdown"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de tri vers le haut
	 */
	public static ImageIcon getSortUp() {
		return getImagePathExtension("sortup"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de undo
	 */
	public static ImageIcon getUndo() {
		return getImagePathExtension("undo"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de zoom en avant
	 */
	public static ImageIcon getZoomIn() {
		return getImagePathExtension("zoomIn"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon de zoom en arrière
	 */
	public static ImageIcon getZoomOut() {
		return getImagePathExtension("zoomOut"); //$NON-NLS-1$
	}

	/**
	 *
	 * @return L'imageIcon de regles reduites
	 */
	public static ImageIcon getShowRulesLittle2() {
		return getImagePathExtension("showRules2"); //$NON-NLS-1$
	}


	public static ImageIcon getFleche() {
		return getImagePathExtension("fl"); //$NON-NLS-1$
	}

	/**
	 * @return L'ImageIcon grande de l'apposition
	 */
	public static ImageIcon getAppositionLittle() {
		return getImagePathExtension("apposition_little1");
	}

	/**
	 * @return L'ImageIcon grande de la subposition
	 */
	public static ImageIcon getSubpositionLittle() {
		return getImagePathExtension("subposition_little");
	}

	/**
	 * Permet d'obtenir l'icone avec le path complet et l'extension usuelle
	 * @param content la chaine de caractere representant le path d'une icone
	 * @return L'ImageIcon associée à l'icone
	 */
	private static ImageIcon getImagePathExtension(String content) {
		String path = PATH_ICONS + content + EXTENSION_ICONS;
		return new ImageIcon(SINGLETON.getClass().getResource(path));
	}




}
