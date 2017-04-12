package fca.gui.util.constant;

/**
 * Contient toutes les constantes relatives aux options du programme Lattice Minner
 * @author Ludovic Thomas
 * @version 1.0
 */
public final class LMOptions {

	/* Options générale a deux choix : oui ou non */

	/** Rien n'est en effet */
	public static final int NONE = 0;

	/** Afficher */
	public static final int SHOW = 1;

	/* Options sur les sélections simples */

	/** Le filtre du noeud est en surbrillance */
	public static final int FILTER = 1;

	/** L'idéal du noeud est en surbrillance */
	public static final int IDEAL = 2;

	/** Le filtre et l'idéal du noeud sont en surbrillance */
	public static final int FILTER_IDEAL = 3;

	/** Les parents directs du noeud sont en surbrillance */
	public static final int PARENTS = 4;

	/** Les enfants directs du noeud sont en surbrillance */
	public static final int CHILDREN = 5;

	/** Les parents et enfants directs du noeud sont en surbrillance */
	public static final int PARENTS_CHILDREN = 6;

	/** La navigation dans les enfants directs du noeud sera possible */
	public static final int CHILDREN_PARENTS = 7;

	/* Options sur les sélections multiples */

	/** Les noeuds commun des filtres sont en surbrillance */
	public static final int COMMON_FILTER = 1;

	/** Les noeuds commun des idéaux sont en surbrillance */
	public static final int COMMON_IDEAL = 2;

	/** Les noeuds commun des filtres et idéaux sont en surbrillance */
	public static final int COMMON_FILTER_IDEAL = 3;

	/** Le treillis ayant la source et le sink séctionnés est en surbrillance */
	public static final int SUB_LATTICE = 4;

	/* Options sur le contraste associé aux sélections */

	/** Les noeuds non sélectionnés sont mis en flou */
	public static final int BLUR = 1;

	/** Les noeuds non sélectionnés sont plus petits */
	public static final int FISHEYE = 2;

	/* Options sur l'activation des animations */

	/** Animation souhaitée */
	public static final int ANIMATION_OK = 1;

	/* Options sur les animations */

	/** Animation de type zoom du treillis */
	public static final int ZOOM = 1;

	/** Animation de type déplacement du treillis */
	public static final int MOVE = 2;

	/** Animation de type movement d'un concept */
	public static final int SHAKE = 3;

	/** Animation de type clignotement */
	public static final int BLINK = 4;

	/* Options sur les changements d'intensité des couleurs */

	/** Changement d'intensité des couleurs : changer selon l'importance */
	public static final int CHANGE = 1;

	/** Changement d'intensité des couleurs : identique pour tous */
	public static final int SAME = 2;

	/* Options "preset" des options */

	/** Option "preset" : légère en performance */
	public static final int LIGHT = 1;

	/** Option "preset" : forte en performance */
	public static final int HEAVY = 2;

	/** Option "preset" : utilisateur */
	public static final int USER = 3;

	/* Options sur les tailles des concepts */

	/** Numéro de taille du concept graphique : Petit */
	public static final int SMALL = 1;

	/** Numéro de taille du concept graphique : Large */
	public static final int LARGE = 2;

	/** Numéro de taille du concept graphique : Depend de ses caracteristiques */
	public static final int VARY = 3;

	/* Options sur les labels des concepts */

	/** Ne pas afficher */
	public static final int NO_LABEL = 0;

	/** Liste complète des attributs */
	public static final int ATTRIBUTES_ALL = 1;

	/** La liste réduite des attributs */
	public static final int ATTRIBUTES_REDUCED = 2;

	/** Liste des objets */
	public static final int OBJECTS_ALL = 1;

	/** Liste réduite des objets */
	public static final int OBJECTS_REDUCED = 2;

	/** Nombre absolu d'objets */
	public static final int OBJECTS_NUMBER = 3;

	/** Pourcentage des objets du contexte */
	public static final int OBJECTS_PERC_CTX = 4;

	/** Pourcentage des objets du noeud externe */
	public static final int OBJECTS_PERC_NODE = 5;

	/** Afficher les règles */
	public static final int RULES_SHOW = 1;

	/** Afficher les generateurs */
	public static final int GENE_SHOW = 1;
}
