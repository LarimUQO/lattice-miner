package fca.gui.util;

import java.awt.Component;
import javax.swing.JOptionPane;

import fca.exception.LMLogger;
import fca.exception.LatticeMinerException;

/**
 * Classe regroupant des methodes d'affichages de boites de dialogue. Utilise la classe
 * {@link JOptionPane}, mais permet d'avoir un affichage plus clair dans le code Lattice Miner et
 * d'eviter les erreurs grece e des noms de methodes plus claires
 * @author Ludovic Thomas
 * @version 1.0
 */
public final class DialogBox {

	/** Resultat retourni lors du clic sur OK d'une {@link DialogBox} */
	public static final int OK = JOptionPane.OK_OPTION;

	/** Resultat retourni lors du clic sur NO d'une {@link DialogBox} */
	public static final int NO = JOptionPane.NO_OPTION;

	/** Resultat retourni lors du clic sur YES d'une {@link DialogBox} */
	public static final int YES = JOptionPane.YES_OPTION;

	/** Resultat retourni lors du clic sur CANCEL d'une {@link DialogBox} */
	public static final int CANCEL = JOptionPane.CANCEL_OPTION;

	/**
	 * Affiche une boite de dialogue pour un composant donne, avec une icone d'information. Le titre
	 * et le message sont a specifier
	 * @param parentComponent le composant parent de la fenetre
	 * @param message le message de la boite de dialogue
	 * @param title le titre de la boite de dialogue
	 */
	public static void showMessageInformation(Component parentComponent, String message, String title) {
		JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Affiche une boite de dialogue pour un composant donne, avec une icone d'information. Le titre
	 * et le message sont recuperes via l'exception
	 * @param parentComponent le composant parent de la fenetre
	 * @param exception une exception de Lattice Miner dont on extrait l'inofrmation (message et
	 *        titre)
	 */
	public static void showMessageInformation(Component parentComponent, LatticeMinerException exception) {
		JOptionPane.showMessageDialog(parentComponent, exception.getMessage(), exception.getMessageGeneral(),
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Affiche une boite de dialogue pour un composant donne, avec une icone de warning. Le titre et
	 * le message sont a specifier
	 * @param parentComponent le composant parent de la fenetre
	 * @param message le message de la boite de dialogue
	 * @param title le titre de la boite de dialogue
	 */
	public static void showMessageWarning(Component parentComponent, String message, String title) {
		JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Affiche une boite de dialogue pour un composant donne, avec une icone de warning. Le titre et
	 * le message sont recuperes via l'exception
	 * @param parentComponent le composant parent de la fenetre
	 * @param exception une exception de Lattice Miner dont on extrait l'inofrmation (message et
	 *        titre)
	 */
	public static void showMessageWarning(Component parentComponent, LatticeMinerException exception) {
		JOptionPane.showMessageDialog(parentComponent, exception.getMessage(), exception.getMessageGeneral(),
				JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Affiche une boite de dialogue pour un composant donne, avec une icone d'erreur. Le titre et
	 * le message sont a specifier
	 * @param parentComponent le composant parent de la fenetre
	 * @param message le message de la boite de dialogue
	 * @param title le titre de la boite de dialogue
	 */
	public static void showMessageError(Component parentComponent, String message, String title) {
		JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
		LMLogger.logSevere(title + "\n" + message, false); //$NON-NLS-1$
	}

	/**
	 * Affiche une boite de dialogue pour un composant donne, avec une icone d'erreur. Le titre et
	 * le message sont recuperes via l'exception
	 * @param parentComponent le composant parent de la fenetre
	 * @param exception une exception de Lattice Miner dont on extrait l'inofrmation (message et
	 *        titre)
	 */
	public static void showMessageError(Component parentComponent, LatticeMinerException exception) {
		JOptionPane.showMessageDialog(parentComponent, exception.getMessage(), exception.getMessageGeneral(),
				JOptionPane.ERROR_MESSAGE);
		LMLogger.logSevere(exception, false);
	}

	/**
	 * Affiche une boite d'interrogation ou l'utilisateur doit entrer de l'informnation. Ceci
	 * correspond a un composant donne et possede une icone de question. Le titre et le message sont
	 * a specifier
	 * @param parentComponent le composant parent de la fenetre
	 * @param message le message de la boite de dialogue
	 * @param title le titre de la boite de dialogue
	 * @return la valeur entree par l'utilisateur
	 */
	public static String showInputQuestion(Component parentComponent, String message, String title) {
		return JOptionPane.showInputDialog(parentComponent, message, title, JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * Affiche une boite d'interrogation ou l'utilisateur doit entrer des informnations. Ceci
	 * correspond a un composant donne et possede une icone de question. Le titre et les messages sont
	 * a specifier
	 * @param parentComponent le composant parent de la fenetre
	 * @param messages le message de la boite de dialogue sous la forme d'une liste question:valeur de retour
	 * @param title le titre de la boite de dialogue
	 * @return les valeurs entree par l'utilisateur sous la forme d'une liste question:reponse
	 */
//	public static TreeMap<String, String> showMultipleInputQuestion(Component parentComponent, TreeMap<String, String> messages, String title) {
//		TreeMap<String, String> list = new TreeMap<String, String>();
//
//		/*Viewer window  = new Viewer(title);
//		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		window.setSize(new java.awt.Dimension(500, 500));
//		window.setPreferredSize(new java.awt.Dimension(500, 500));
//		window.setLocationRelativeTo(null);
//
//        window.setLayout(new BoxLayout(window.getContentPane(), BoxLayout.PAGE_AXIS));*/
//
//
//		for(Entry<String, String> entry : messages.entrySet()) {
//			String cle = entry.getKey();
//			String valeur = entry.getValue();
//			String str = "";
//
//			/*JPanel pane = new JPanel();
//			pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));
//
//			window.add(pane);
//			JLabel label = new JLabel(cle);
//			pane.add(label);
//			JTextPane text = new JTextPane();
//			pane.add(text);*/
//
//			if(valeur.equals("String")) {
//				str = JOptionPane.showInputDialog(parentComponent,cle,title,JOptionPane.QUESTION_MESSAGE);
//				if(str==null) {
//					str = "";
//				}
//			}
//			if(valeur.equals("Integer")) {
//				int Nb = -1; //-1;
//				while (Nb <= 0) {
//					try {
//						str = JOptionPane.showInputDialog(parentComponent,cle,title,JOptionPane.QUESTION_MESSAGE);
//						if (str != null) {
//							Nb = Integer.parseInt(str);
//						}
//					} catch (NumberFormatException nfe) {
//						Nb = -1;
//					}
//				}
//			}
//			list.put(cle, str);
//		}/*
//		JButton ok = new JButton(LMIcons.getOK());
//		window.add(ok);
//
//		window.pack();		
//		window.setVisible(true);*/
//		return list;
//	}


	/**
	 * Affiche une boite d'interrogation o`l'utilisateur doit entrer de l'informnation. Ceci
	 * correspond a un composant donne et possede une icone de question. Le titre et le message sont
	 * a specifier, ainsi que les valeurs selectionnables et la valeur par defaut selectionnee
	 * @param parentComponent le composant parent de la fenetre
	 * @param message le message de la boite de dialogue
	 * @param title le titre de la boite de dialogue
	 * @param selectionValues les valeurs selectionnables par l'utilisateur
	 * @param initialSelectionValue la valeur par defaut selectionnee
	 * @return la valeur choisie par l'utilisateur
	 */
	public static Object showInputQuestion(Component parentComponent, String message, String title,
			Object[] selectionValues, Object initialSelectionValue) {
		return JOptionPane.showInputDialog(parentComponent, message, title, JOptionPane.QUESTION_MESSAGE, null,
				selectionValues, initialSelectionValue);
	}

	/**
	 * Affiche une boite de dialogue posant une question a l'utilisateur qui peut repondre par YES
	 * ou NO, ceci pour un composant donne. Le titre et le message sont a specifier
	 * @param parentComponent le composant parent de la fenetre
	 * @param message le message de la boite de dialogue
	 * @param title le titre de la boite de dialogue
	 * @return le choix fait par l'utilisateur : YES ou NO
	 */
	public static int showDialogQuestion(Component parentComponent, String message, String title) {
		return JOptionPane.showConfirmDialog(parentComponent, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * Affiche une boite de dialogue posant une question a l'utilisateur qui peut repondre par YES,
	 * NO ou CANCEL, ceci pour un composant donne. Le titre et le message sont a specifier
	 * @param parentComponent le composant parent de la fenetre
	 * @param message le message de la boite de dialogue
	 * @param title le titre de la boite de dialogue
	 * @return le choix fait par l'utilisateur : YES, NO ou CANCEL
	 */
	public static int showDialogQuestionCancel(Component parentComponent, String message, String title) {
		return JOptionPane.showConfirmDialog(parentComponent, message, title, JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * Affiche une boite de dialogue posant une question mais avec une icone de warning pour attirer
	 * l'attention de l'utilisateur qui peut repondre par YES ou NO, ceci pour un composant donne.
	 * Le titre et le message sont a specifier
	 * @param parentComponent le composant parent de la fenetre
	 * @param message le message de la boite de dialogue
	 * @param title le titre de la boite de dialogue
	 * @return le choix fait par l'utilisateur : YES ou NO
	 */
	public static int showDialogWarning(Component parentComponent, String message, String title) {
		return JOptionPane.showConfirmDialog(parentComponent, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Affiche une boite de dialogue posant une question mais avec une icone de warning pour attirer
	 * l'attention de l'utilisateur qui peut repondre par YES, NO ou CANCEL, ceci pour un composant
	 * donne. Le titre et le message sont a specifier
	 * @param parentComponent le composant parent de la fenetre
	 * @param message le message de la boite de dialogue
	 * @param title le titre de la boite de dialogue
	 * @return le choix fait par l'utilisateur : YES, NO ou CANCEL
	 */
	public static int showDialogWarningCancel(Component parentComponent, String message, String title) {
		return JOptionPane.showConfirmDialog(parentComponent, message, title, JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Affiche une boite de dialogue posant une question et permettant de choisir parmis une liste de reponses
	 * @param parentComponent le composant parent de la fenetre
	 * @param message le message de la boite de dialogue
	 * @param title le titre de la boite de dialogue
	 * @param options la liste sous forme de chaine de caracteres des propositions de reponses
	 * @return la reponse choisie
	 */
	public static String showDropDown(Component parentComponent, String message, String title, String[] options) {		
		return (String) JOptionPane.showInputDialog(parentComponent, message, title, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
	}

}
