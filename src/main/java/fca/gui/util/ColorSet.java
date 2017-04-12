package fca.gui.util;

import java.awt.Color;
import java.util.Vector;

import fca.gui.util.constant.LMColors.LatticeColor;

/**
 * Ensemble ordonné de couleurs
 * @author Geneviève Roberge
 * @author Ludovic Thomas
 * @version 1.0
 */
public class ColorSet {

	/**
	 * Position de la dernière couleur obtenue
	 */
	private int currentIndex;

	/**
	 * Constructeur d'nsemble ordonné de couleurs
	 */
	public ColorSet() {
		currentIndex = -1;
	}

	/**
	 * Permet de faire revenir l'ensemble de couleur une couleur avant la couleur courante
	 */
	public void backOneColor() {
		if (currentIndex > -1)
			currentIndex--;
	}

	/**
	 * Permet d'obtenir la prochaine couleur disponible dans l'ensemble
	 * @return La Color contenant la prochaine couleur disponible
	 */
	public Color getNextColor() {
		currentIndex++;
		return getColorAt(currentIndex);
	}

	/**
	 * Permet d'obtenir la prochaine couleur disponible dans l'ensemble sauf la couleur par défaut
	 * @param color la couleur actuelle
	 * @return La Color contenant la prochaine couleur disponible
	 */
	public static LatticeColor getNextColor(LatticeColor color) {
		Vector<LatticeColor> colorStrings = LatticeColor.getLatticeColors();
		int position = colorStrings.indexOf(color);

		if (position != -1) {
			int index = (position + 1) % (colorStrings.size());
			if (index == 0)
				index = 1; // Enleve la valeur par defaut
			return colorStrings.elementAt(index);
		}
		return LatticeColor.DEFAULT;
	}

	/**
	 * Permet de connaître la couleur à une position donnée de l'ensemble
	 * @param targetIndex Le int contenant la position pour laquelle la couleur est recherchée
	 * @return la couleur a la position recherchée
	 */
	public static Color getColorAt(int targetIndex) {
		Vector<Color> colors = LatticeColor.getColorsValues();
		int index = targetIndex % (colors.size());
		return colors.elementAt(index);
	}

	/**
	 * Permet de connaître la couleur (en caracteres) à une position donnée de l'ensemble
	 * @param targetIndex Le int contenant la position pour laquelle la couleur est recherchée
	 * @return La String contenant la couleur à la position demandée
	 */
	public static LatticeColor getColorStringAt(int targetIndex) {
		Vector<LatticeColor> colorStrings = LatticeColor.getLatticeColors();
		int index = targetIndex % (colorStrings.size());
		return colorStrings.elementAt(index);
	}
}