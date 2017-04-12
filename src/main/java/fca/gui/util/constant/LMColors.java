package fca.gui.util.constant;

import java.awt.Color;
import java.util.Vector;

import fca.messages.GUIMessages;

/**
 * Contient toutes les constantes relatives aux couleurs du programme Lattice Minner
 * @author Ludovic Thomas
 * @version 1.0
 */
public final class LMColors {

	/**
	 * Type énuméré de selection de couleur de treillis
	 */
	public static enum LatticeColor {
		/** La couleur par défaut d'un treillis : Bleu */
		DEFAULT(GUIMessages.getString("GUI.blue"), new Color(153, 203, 252)), //$NON-NLS-1$

		/** Couleur jaune */
		YELLOW(GUIMessages.getString("GUI.yellow"), new Color(255, 255, 133)), //$NON-NLS-1$

		/** Couleur rose */
		PINK(GUIMessages.getString("GUI.pink"), new Color(240, 160, 255)), //$NON-NLS-1$

		/** Couleur verte */
		GREEN(GUIMessages.getString("GUI.green"), new Color(106, 254, 104)), //$NON-NLS-1$

		/** Couleur orange */
		ORANGE(GUIMessages.getString("GUI.orange"), new Color(255, 178, 122)); //$NON-NLS-1$

		/** Chaine de caractère représentant la couleur */
		private String stringColor;

		/** Objet {@link Color} représentant la couleur */
		private Color color;

		/** Le vecteur de toutes les LatticeColor en type {@link Color} */
		private static Vector<Color> ALL_COLORS = null;

		/** Le vecteur de toutes les LatticeColor */
		private static Vector<LatticeColor> ALL_LATTICE_COLORS = null;

		/**
		 * Constructeur privé du type enum {@link LatticeColor}
		 * @param stringColor Chaine de caractère représentant la couleur
		 * @param color Objet {@link Color} représentant la couleur
		 */
		LatticeColor(String stringColor, Color color) {
			this.stringColor = stringColor;
			this.color = color;
		}

		/**
		 */
		/**
		 * @return l'objet {@link Color} représentant la couleur
		 */
		public Color getColor() {
			return color;
		}

		/**
		 * @return la couleur de treillis par défaut
		 */
		public LatticeColor getDefault() {
			return LatticeColor.DEFAULT;
		}

		/**
		 * @return le vecteur de toutes les LatticeColor
		 */
		public static Vector<LatticeColor> getLatticeColors() {
			if (ALL_LATTICE_COLORS == null) {
				LatticeColor[] colors = LatticeColor.values();
				ALL_LATTICE_COLORS = new Vector<LatticeColor>();
				for (LatticeColor color : colors) {
					ALL_LATTICE_COLORS.add(color);
				}
			}
			return ALL_LATTICE_COLORS;
		}

		/**
		 * @return le vecteur de toutes les LatticeColor de type {@link Color}
		 */
		public static Vector<Color> getColorsValues() {
			if (ALL_COLORS == null) {
				LatticeColor[] colors = LatticeColor.values();
				ALL_COLORS = new Vector<Color>();
				for (LatticeColor color : colors) {
					ALL_COLORS.add(color.getColor());
				}
			}
			return ALL_COLORS;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return stringColor;
		}
	};

	/**
	 * Type énuméré de selection de couleur de selection
	 */
	public static enum SelectionColor {

		/** La couleur par défaut d'une selection : Noir */
		DEFAULT(GUIMessages.getString("GUI.black")), //$NON-NLS-1$

		/** Couleur verte */
		GREEN(GUIMessages.getString("GUI.green")), //$NON-NLS-1$

		/** Couleur rouge */
		RED(GUIMessages.getString("GUI.red")); //$NON-NLS-1$

		/** Chaine de caractère représentant la couleur */
		private String value;

		/**
		 * Constructeur privé du type enum {@link SelectionColor}
		 * @param color Chaine de caractère représentant la couleur
		 */
		SelectionColor(String color) {
			value = color;
		}

		/**
		 */
		/**
		 * @return Chaine de caractère représentant la couleur
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @return la couleur de selection par défaut
		 */
		public SelectionColor getDefault() {
			return SelectionColor.DEFAULT;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return value;
		}
	};

}
