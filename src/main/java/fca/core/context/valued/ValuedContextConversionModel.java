package fca.core.context.valued;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import fca.messages.CoreMessages;

/**
 * Classe donnant le modele a utiliser pour convertir une relation valuee en une relation binaire
 * @author Genevieve Roberge
 * @version 1.0
 */
@SuppressWarnings("unchecked") //$NON-NLS-1$
// Impossible de typer "newAttributesValues"
public class ValuedContextConversionModel {
	/* Constantes pour les codes d'erreur resultat de l'ajout d'une separation d'un attribut */
	public static final int OK = 0; //Aucune erreur generee
	public static final int EXISTS = 1; //La separation existe deja
	public static final int WRONG_TYPE = 2; //L'attribut est separe d'une autre façon (intervalle/valeur)

	/* Constantes pour les types de separation d'attributs */
	public static final int EMPTY = 0; //Aucun type de separation defini
	public static final int VALUES = 1; //Separation par valeurs precises
	public static final int INTERVAL = 2; //Separation par intervalle de valeurs

	/* Variables de la classe */

	private ValuedContext valuedContext; //Contexte value concerne par ce modele

	private Vector<Vector<String>> attributesSplit; //Noms des nouveaux attributs rattaches a l'attibut value d'origine

	private Vector<Vector> newAttributesValues; //Valeurs incluses dans les nouveaux attributs (une liste pour les separations par valeurs precises; les bornes de l'intervalle pour les separations par intervalle)
	private Vector<Integer> splitType; //Separation de l'attribut par valeur ou par intervalle

	/**
	 * Constructeur
	 * @param valCtx Le ValuedContext pour lequel est construit ce modele
	 */
	public ValuedContextConversionModel(ValuedContext valCtx) {
		valuedContext = valCtx;
		attributesSplit = new Vector<Vector<String>>();
		splitType = new Vector<Integer>();
		newAttributesValues = new Vector<Vector>();

		for (int i = 0; i < valuedContext.getAttributeCount(); i++) {
			attributesSplit.add(new Vector<String>());
			splitType.add(new Integer(EMPTY));
			newAttributesValues.add(new Vector());
		}
	}

	/**
	 * Permet de connaitre le nombre de separation faites pour un attribut donne
	 * @param idx Le int contenant l'index de l'attribut recherche
	 * @return Le int contenant le nombre de separations de l'attribut (-1 si l'index idx ne
	 *         correspond a aucun attribut)
	 */
	public int getNewAttributeCount(int idx) {
		if (idx < attributesSplit.size())
			return attributesSplit.elementAt(idx).size();
		else
			return -1;
	}

	/**
	 * Permet de connaitre le nombre d'objets contenus dans le contexte de base
	 * @return Le int contenant le nombre d'objets
	 */
	public int getObjectCount() {
		return valuedContext.getObjectCount();
	}

	/**
	 * Permet d'ajouter une nouvelle separation par valeurs pour un attribut donne
	 * @param oldAttIdx Le int contenant l'index de l'attribut a separer
	 * @param newAttributeName La String contenant le nom du nouvel attribut
	 * @param attributeValues Le Vector contenant les valeurs du contexte de base a inclure dans le
	 *        nouvel attribut
	 * @return Le int contenant le code d'erreur genere par l'ajout de la separation (OK,
	 *         WRONG_TYPE, EXISTS)
	 */
	public int addAttribute(int oldAttIdx, String newAttributeName, Vector<String> attributeValues) {
		/* Une separation par intervalle a deja ete faite pour cet attribut */
		if ((splitType.elementAt(oldAttIdx)).intValue() == INTERVAL)
			return WRONG_TYPE;

		/* Validation de l'unicite du nom du nouvel attribut */
		for (int i = 0; i < (attributesSplit.elementAt(oldAttIdx)).size(); i++) {
			/* Un attribut de meme nom existe deja dans les attributs produits par la conversion */
			if (((attributesSplit.elementAt(oldAttIdx)).elementAt(i)).equals(newAttributeName))
				return EXISTS;
		}

		/* Ajout du nouvel attribut au modele de conversion */
		(attributesSplit.elementAt(oldAttIdx)).add(new String(newAttributeName));
		newAttributesValues.elementAt(oldAttIdx).add(attributeValues);
		splitType.set(oldAttIdx, new Integer(VALUES));

		return OK;
	}

	/**
	 * Permet d'ajouter une nouvelle separation par intervalle pour un attribut donne
	 * @param oldAttIdx Le int contenant l'index de l'attribut a separer
	 * @param newAttributeName La String contenant le nom du nouvel attribut
	 * @param startValue Le double contenant la valeur de depart de l'intervalle (inclusif)
	 * @param endValue Le double contenant la valeur de fin de l'intervalle (exclusif)
	 * @return Le int contenant le code d'erreur genere par l'ajout de la separation (OK,
	 *         WRONG_TYPE, EXISTS)
	 */
	public int addAttribute(int oldAttIdx, String newAttributeName, double startValue, double endValue) {
		/* Une separation par intervalle a deja ete faite pour cet attribut */
		if ((splitType.elementAt(oldAttIdx)).intValue() == VALUES)
			return WRONG_TYPE;

		/* Validation de l'unicite du nom du nouvel attribut */
		for (int i = 0; i < attributesSplit.elementAt(oldAttIdx).size(); i++) {
			if ((attributesSplit.elementAt(oldAttIdx).elementAt(i)).equals(newAttributeName))
				return EXISTS;
		}

		/* Ajout du nouvel attribut au modele de conversion */
		(attributesSplit.elementAt(oldAttIdx)).add(new String(newAttributeName));
		Vector<Double> interval = new Vector<Double>();
		interval.add(new Double(startValue));
		interval.add(new Double(endValue));
		newAttributesValues.elementAt(oldAttIdx).add(interval);
		splitType.set(oldAttIdx, new Integer(INTERVAL));

		return OK;
	}

	/**
	 * Permet de supprimer une separation dans un attribut donne
	 * @param oldAttIdx Le int contenant l'index de l'attribut du contexte de base
	 * @param newAttIdx Le int contenant l'index de la separation de l'attribut du contexte de base
	 */
	public void removeAttribute(int oldAttIdx, int newAttIdx) {
		(attributesSplit.elementAt(oldAttIdx)).removeElementAt(newAttIdx);
		newAttributesValues.elementAt(oldAttIdx).removeElementAt(newAttIdx);

		/*
		 * S'il ne reste plus de separation pour l'attribut donne, son type de separation redevient
		 * libre
		 */
		if ((attributesSplit.elementAt(oldAttIdx)).size() == 0)
			splitType.set(oldAttIdx, new Integer(EMPTY));
	}

	/**
	 * Permet de connaitre les valeurs contenues dans une separation d'un attribut du contexte de
	 * base
	 * @param oldIdx Le int contenant l'index de l'attribut de la relation de base
	 * @param newIdx Le int contenant l'index de la separation de l'attribut du contexte de base
	 */
	public Vector getNewAttributeValues(int oldIdx, int newIdx) {
		return (Vector) newAttributesValues.elementAt(oldIdx).elementAt(newIdx);
	}

	/**
	 * Permet de connaitre le type de separation d'un attribut donne
	 * @param idx Le int contenant l'index de l'attribut de la relation de base
	 * @return Le int contenant la valeur associee au type de separation (EMPTY, VALUES, INTERVAL)
	 */
	public int getNewAttributesType(int idx) {
		return (splitType.elementAt(idx)).intValue();
	}

	/**
	 * Permet de connaitre les nom des attributs separant un attribut du contexte de base
	 * @param idx Le int contenant l'index de l'attribut du contexte de base
	 * @return Le Vector contenant les noms des nouveaux attributs crees pour l'attribut du contexte
	 *         de base
	 */
	public Vector<String> getNewAttributesNames(int idx) {
		return attributesSplit.elementAt(idx);
	}

	/**
	 * Complete le modele conversion : assure que chaque valeur d'attribut du contexte de base est
	 * incluse dans un nouvel attribut (cree les attributs necessaires, soit un attribut pour chaque
	 * valeur differente d'un attribut non separable en intervalles ou un maximum de 5 intervalles
	 * de valeurs pour un attribut separable en intervalles) et cree un intervalle general pour
	 * chaque attribut separe en intervalles pour recueillir les valeurs qui se seraient orphelines.
	 */
	public void completeConversionModel() {
		for (int i = 0; i < attributesSplit.size(); i++) {
			//Vector split = (Vector)attributesSplit.elementAt(i);
			// int nbSplits = split.size();

			/*
			 * Completion du modele de conversion d'un attribut separe en intervalles : ajout d'un
			 * intervalle general
			 */
			if ((splitType.elementAt(i)).intValue() == INTERVAL)
				addAttribute(i, valuedContext.getAttributeAt(i).toString() + CoreMessages.getString("Core.others_"), Integer.MIN_VALUE, //$NON-NLS-1$
						Integer.MAX_VALUE);

			/* Completion du modele de conversion d'un attribut separe en valeurs discretes */
			else if ((splitType.elementAt(i)).intValue() == VALUES) {
				/*
				 * Construction d'un vecteur contenant toutes valeurs incluses dans les separations
				 * de l'attribut
				 */
				Vector<String> newAttValues = new Vector<String>();
				for (int valueIdx = 0; valueIdx < newAttributesValues.elementAt(i).size(); valueIdx++)
					newAttValues.addAll((Vector<String>) newAttributesValues.elementAt(i).elementAt(valueIdx));

				/*
				 * Verification de l'existence d'une separation comprennant chacune des valeurs de
				 * l'attribut
				 */
				TreeSet valueTree = valuedContext.getValuesForAttribute(i);
				Iterator values = valueTree.iterator();
				while (values.hasNext()) {
					String currentValue = (String) values.next();

					/*
					 * Creation des separations contenant les valeurs d'attribut qui ne sont
					 * incluses dans aucune separation
					 */
					if (!newAttValues.contains(currentValue)) {
						Vector<String> valuesList = new Vector<String>();
						valuesList.add(currentValue);
						addAttribute(i, valuedContext.getAttributeAt(i) + "_" + currentValue, valuesList); //$NON-NLS-1$
					}
				}
			}

			/* Completion du modele de conversion d'un attribut qui n'a pas ete separe */
			else if ((splitType.elementAt(i)).intValue() == EMPTY) {
				TreeSet valueTree = valuedContext.getValuesForAttribute(i);

				/*
				 * Un attribut ne contenant que des valeurs numeriques - en nombre suffisant - est
				 * separe en intervalles
				 */
				if (valuedContext.isNumericAttribute(i) && valueTree.size() > 5) {

					/* Construction du vecteur de valeurs numeriques */
					Vector<Double> values = new Vector<Double>();
					Iterator valIt = valueTree.iterator();
					while (valIt.hasNext()) {
						String nextVal = (String) valIt.next();
						if (nextVal != null && !(nextVal.trim()).equals("")) //$NON-NLS-1$
							values.add(Double.parseDouble(nextVal));
					}
					sort(values, 0, values.size() - 1);

					/* Construction du vecteur des frequences */
					Vector<Integer> frequencies = new Vector<Integer>();
					for (int j = 0; j < values.size(); j++) {
						double val = (values.elementAt(j)).doubleValue();
						int freq = 0;
						for (int k = 0; k < valuedContext.getObjectCount(); k++) {
							if (valuedContext.getValueAt(k, i) != null
									&& !(valuedContext.getValueAt(k, i).trim()).equals("")) { //$NON-NLS-1$
								double objVal = Double.parseDouble(valuedContext.getValueAt(k, i));
								if (val == objVal)
									freq++;
							}
						}
						frequencies.add(new Integer(freq));
					}

					/* Construction des intervalles */
					Discretizer discretizer = new Discretizer(values, frequencies);
					Vector intervals = discretizer.getIntervals();
					for (int j = 0; j < intervals.size() - 1; j++) {
						/* Ajustement de la precision a 4 chiffres */
						double min = Math.round(((Double) intervals.elementAt(j)).doubleValue() * 10000.0) / 10000.0;
						double max = Math.round(((Double) intervals.elementAt(j + 1)).doubleValue() * 10000.0) / 10000.0;

						/* Permet d'ecrire des nombres entiers lorsque cela s'applique */
						if (Math.floor(min) == Math.ceil(min) && Math.floor(max) == Math.ceil(max))
							addAttribute(i, valuedContext.getAttributeAt(i) + "_" + (int) min + CoreMessages.getString("Core.to") + (int) max, min, //$NON-NLS-1$ //$NON-NLS-2$
									max);
						else if (Math.floor(max) == Math.ceil(max))
							addAttribute(i, valuedContext.getAttributeAt(i) + "_" + min + CoreMessages.getString("Core.to") + (int) max, min, max); //$NON-NLS-1$ //$NON-NLS-2$
						else if (Math.floor(min) == Math.ceil(min))
							addAttribute(i, valuedContext.getAttributeAt(i) + "_" + (int) min + CoreMessages.getString("Core.to") + max, min, max); //$NON-NLS-1$ //$NON-NLS-2$
						else
							addAttribute(i, valuedContext.getAttributeAt(i) + "_" + min + CoreMessages.getString("Core.to") + max, min, max); //$NON-NLS-1$ //$NON-NLS-2$
					}
					//addAttribute(i,valuedContext.getAttributeAt(i).toString()+"_",Integer.MIN_VALUE, Integer.MAX_VALUE);
				}

				/*
				 * Un attribut qui ne contient pas que des valeurs numerique est separe par valeurs,
				 * a raison d'un nouvel attribut pour chaque valeur differente
				 */
				else {
					/* Creation d'une separation pour chaque valeur distincte de l'attribut */
					Iterator values = valueTree.iterator();
					while (values.hasNext()) {
						String currentValue = (String) values.next();
						Vector<String> valuesList = new Vector<String>();
						valuesList.add(currentValue);
						addAttribute(i, valuedContext.getAttributeAt(i) + "_" + currentValue, valuesList); //$NON-NLS-1$
						splitType.set(i, new Integer(VALUES));
					}
				}
			}
		}
	}

	/**
	 * Permet de connaitre le nombre de chiffre apres la virgule que contient un nombre
	 * @param number Le double contenant le nombre pour lequel la precision est cherchee
	 * @return Le int contenant le nombre de chiffres apres la virgule
	 */
	@SuppressWarnings("unused") //$NON-NLS-1$
	private int calcPrecision(double number) {
		int precision = 1;

		double testNb = (Math.floor(number * precision)) / precision;
		while (testNb != number) {
			precision *= 10;
			testNb = (Math.floor(number * precision)) / precision;
		}

		return precision;
	}

	private void sort(Vector<Double> tab, int left, int right) {
		if (right > left) {
			double val = (tab.elementAt(right)).doubleValue();
			int pivot = left - 1;

			for (int i = left; i < right; i++) {
				if ((tab.elementAt(i)).doubleValue() <= val) {
					pivot++;
					Double temp = new Double((tab.elementAt(i)).doubleValue());
					tab.setElementAt(new Double((tab.elementAt(pivot)).doubleValue()), i);
					tab.setElementAt(temp, pivot);
				}
			}

			pivot++;
			Double temp = new Double((tab.elementAt(right)).doubleValue());
			tab.setElementAt(new Double((tab.elementAt(pivot)).doubleValue()), right);
			tab.setElementAt(temp, pivot);

			sort(tab, left, pivot - 1); /* Tri rapide a gauche */
			sort(tab, pivot + 1, right); /* Tri rapide a droite */
		}
	}
}
