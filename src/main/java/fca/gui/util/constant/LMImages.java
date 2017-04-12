package fca.gui.util.constant;

import javax.swing.ImageIcon;

import fca.gui.util.constant.LMColors.LatticeColor;
import fca.gui.util.constant.LMColors.SelectionColor;

/**
 * Contient toutes les constantes relatives aux images du programme Lattice Minner
 * @author Ludovic Thomas
 * @version 1.0
 */
public final class LMImages {

	/* CONSTANTES GENERALES */

	/** Chemin des images */
	private static final String PATH_IMAGES = "/" + "images" + "/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/** Chemin des images de selection */
	private static final String PATH_SELECTION = "Selection" + "/"; //$NON-NLS-1$ //$NON-NLS-2$

	/** Extension des images */
	private static final String EXTENSION_IMAGES = ".png"; //$NON-NLS-1$

	/* VALEURS PAR DEFAUT */

	/** Nom du noeud */
	private static final String NODE = "Node_"; //$NON-NLS-1$

	/** Noeud inactif */
	private static final String INACTIVE_NODE = "GrayNode"; //$NON-NLS-1$

	/** Intensité par défaut */
	private static final int DEFAULT_INTENSITY = 3;

	/** Le singleton représentant {@link LMImages} */
	private static LMImages SINGLETON = null;

	/* ATTRIBUTS D'ICONES DE NOEUDS */

	// A propos des icones
	private static final String FUZZY_CLOSED = "FuzzyClosed"; //$NON-NLS-1$
	private static final String FUZZY_OPENED = "FuzzyOpened"; //$NON-NLS-1$
	private static final String CLEAR_CLOSED = "ClearClosed"; //$NON-NLS-1$
	private static final String CLEAR_OPENED = "FuzzyOpened"; // ClearOpened not used //$NON-NLS-1$

	/* ATTRIBUTS DE SELECTION */

	/** Pour une selection classique */
	private static final String SELECTION = "Selection"; //$NON-NLS-1$

	/** Pour une selection classique ouverte */
	private static final String OPEN_SELECTION = "OpenSelection"; //$NON-NLS-1$

	/**
	 * Constructeur privé de l'instance {@link LMImages}
	 */
	private LMImages() {
		SINGLETON = this;
	}

	/**
	 * Permet de recupérer, ou de créer s'il n'existe pas, une unique instance de {@link LMImages}
	 * @return l'instance unique de {@link LMImages}
	 */
	public static LMImages getLMImages() {
		if (SINGLETON == null) {
			SINGLETON = new LMImages();
		}
		return SINGLETON;
	}

	/* METHODES D'ICONES DE NOEUDS */

	/**
	 * Permet d'obtenir l'image claire et ouverte des noeuds de ce treillis
	 * @param color la couleur du noeud
	 * @param changeIntensity le booléen indiquant si l'intensié doit être prise en compte
	 * @param intensity l'intensité du noeud
	 * @return L'ImageIcon associée au noeuds du treillis
	 */
	public static ImageIcon getClearOpenedIcon(LatticeColor color, boolean changeIntensity, int intensity) {
		// ATTENTION : intensity in clearOpened not used if FuzzyOpened used for CLEAR_OPENED constant
		//		if (changeIntensity)
		//			return getImagePathExtension(color, CLEAR_OPENED + color + NODE + intensity);
		//		else
		return getImagePathExtension(color, CLEAR_OPENED + color + NODE + DEFAULT_INTENSITY);
	}

	/**
	 * Permet d'obtenir l'image claire et fermee des noeuds de ce treillis
	 * @param color la couleur du noeud
	 * @param changeIntensity le booléen indiquant si l'intensié doit être prise en compte
	 * @param intensity l'intensité du noeud
	 * @return L'ImageIcon associée au noeuds du treillis
	 */
	public static ImageIcon getClearClosedIcon(LatticeColor color, boolean changeIntensity, int intensity) {
		if (changeIntensity)
			return getImagePathExtension(color, CLEAR_CLOSED + color + NODE + intensity);
		else
			return getImagePathExtension(color, CLEAR_CLOSED + color + NODE + DEFAULT_INTENSITY);
	}

	/**
	 * Permet d'obtenir l'image floue et fermee des noeuds de ce treillis
	 * @param color la couleur du noeud
	 * @return L'ImageIcon associée au noeuds du treillis
	 */
	public static ImageIcon getFuzzyOpenedIcon(LatticeColor color) {
		// Don't used the color
		return getImagePathExtension(color, FUZZY_OPENED + color + NODE + DEFAULT_INTENSITY);
	}

	/**
	 * Permet d'obtenir l'image floue et fermee des noeuds de ce treillis
	 * @param color la couleur du noeud
	 * @return L'ImageIcon associée au noeuds du treillis
	 */
	public static ImageIcon getFuzzyClosedIcon(LatticeColor color) {
		return getImagePathExtension(color, FUZZY_CLOSED + color + NODE + DEFAULT_INTENSITY);
	}

	/**
	 * Permet d'obtenir l'image floue et inactive des noeuds de ce treillis
	 * @return L'ImageIcon associée au noeuds du treillis
	 */
	public static ImageIcon getFuzzyInactiveIcon() {
		return getImagePathExtension(FUZZY_CLOSED + INACTIVE_NODE);
	}

	/**
	 * Permet d'obtenir l'image claire et inactive des noeuds de ce treillis
	 * @return L'ImageIcon associée au noeuds du treillis
	 */
	public static ImageIcon getClearInactiveIcon() {
		return getImagePathExtension(CLEAR_CLOSED + INACTIVE_NODE);
	}

	/* METHODES DE SELECTION */

	/**
	 * Permet d'obtenir l'image de surbrillance pour un concept fermé
	 * @param color la couleur du noeud
	 * @return L'ImageIcon associée à la surbrillance
	 */
	public static ImageIcon getClosedHighlightIcon(SelectionColor color) {
		return getImagePathExtension(PATH_SELECTION + color + SELECTION);
	}

	/**
	 * Permet d'obtenir l'image de selection pour un concept fermé
	 * @param color la couleur du noeud
	 * @return L'ImageIcon associée à la sélection
	 */
	public static ImageIcon getOpenedHighlightIcon(SelectionColor color) {
		return getImagePathExtension(PATH_SELECTION + color + OPEN_SELECTION);
	}

	/**
	 * Permet d'obtenir l'image de sélection pour un concept ouvert
	 * @param color la couleur du noeud
	 * @return L'ImageIcon associée à la sélection
	 */
	public static ImageIcon getOpenedSelectionIcon(SelectionColor color) {
		return getImagePathExtension(PATH_SELECTION + color + OPEN_SELECTION);
	}

	/**
	 * Permet d'obtenir l'image de sélection pour un concept fermé
	 * @param color la couleur du noeud
	 * @return L'ImageIcon associée à la sélection
	 */
	public static ImageIcon getClosedSelectionIcon(SelectionColor color) {
		return getImagePathExtension(PATH_SELECTION + color + SELECTION);
	}

	/* METHODES PRIVEES */

	/**
	 * Permet d'obtenir l'image avec le path complet et l'extension usuelle
	 * @param content la chaine de caractere representant un path d'une image sans couleur
	 * @return L'ImageIcon associée à l'image
	 */
	private static ImageIcon getImagePathExtension(String content) {
		String path = PATH_IMAGES + content + EXTENSION_IMAGES;
		return new ImageIcon(SINGLETON.getClass().getResource(path));
	}

	/**
	 * Permet d'obtenir l'image avec le path complet et l'extension usuelle
	 * @param la couleur du noeud a prendre en compte comme dossier
	 * @param content la chaine de caractere representant un path d'une image sans couleur
	 * @return L'ImageIcon associée à l'image
	 */
	private static ImageIcon getImagePathExtension(LatticeColor color, String content) {
		String path = PATH_IMAGES + color + "/" + content + EXTENSION_IMAGES; //$NON-NLS-1$
		return new ImageIcon(SINGLETON.getClass().getResource(path));
	}

}
