package fca;

import fca.exception.LMLogger;
import fca.gui.context.ContextViewer;
import fca.gui.util.constant.LMIcons;
import fca.gui.util.constant.LMImages;
import fca.messages.MainMessages;

/**
 * Classe principale pour lancer Lattice Miner</br> Ce prototype a été élaboré
 * dans le cadre du mémoire de maîtrise de Geneviève Roberge (UQO) sous la
 * direction des professeurs Rokia Missaoui et Jurek Czyzowicz.
 * 
 * @author Geneviève Roberge
 * @author Ludovic Thomas
 * @author Yoann Montouchet
 * @author Aicha Bennis, Linda Bogni, Maya Safwat, Babacar SY
 * @version 1.4
 */
public class LatticeMiner {

	/** Numéro de la version actuelle de Lattice Miner */
	public static final double LATTICE_MINER_VERSION = 1.4;

	/**
	 * Methode principale permettant de lancer Lattice Miner
	 * 
	 * @param args
	 *            aucun argument n'est nécessaire pour lancer Lattice Miner
	 */
	public static void main(String[] args) {

		// Aucun argument n'est nécessaire pour lancer Lattice Miner
		if (args.length != 0) {

			System.exit(-1);
		}
		// Initalise la classe du logger
		LMLogger.getLMLogger();

		// Initialise la classe des images et des icônes
		LMImages.getLMImages();
		LMIcons.getLMIcons();

		// try {
		// Initialise et créer la fenetre des contextes
		ContextViewer.getContextViewer();
		// } catch (Exception e) {
		// LMLogger.logSevere(e, true);
		// }
	}
}