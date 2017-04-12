package fca.core.util;

import fca.core.context.binary.BinaryContext;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * Ensemble de donnees (attributs ou objets)
 * @author Genevieve Roberge
 * @author Arnaud Renaud-Goud
 * @version 1.0
 */
public class BasicSet extends TreeSet<String> {

	private static final long serialVersionUID = -206703893373734732L;
	/**
	 * Symbol suffixe de negation d'un element
	 */
	public static final String symbNeg = "\'";

	/**
	 * Methode statique faisant la negation d'un element
	 * @param element L'element a traiter
	 * @return sa negation
	 */
	public static String negation(String element) {
		if(element.endsWith(symbNeg)) {
			return element.subSequence(0, element.length()-symbNeg.length()).toString();
		} else {
			return element+symbNeg;
		}
	}
	/**
	 * Methode statique testant si l'element est negatif
	 * @param element L'element a analyser
	 * @return vrai si l'element est negatif, faux sinon
	 */
	public static boolean isNegative(String element) {
		if(element.endsWith(symbNeg)) {
			return true;
		}
		return false;
	}

	/**
	 * Cree un nouveau ensemble contenant les elements mis en negatif
	 * @return le basicset des elements negatifs
	 */
	public BasicSet negation() {
		BasicSet negSet = new BasicSet();
		for(String a : this) {
			negSet.add(BasicSet.negation(a));
		}
		return negSet;
	}

	/**
	 * Realise l'union de deux BasicSet
	 * @param set l'ensemble avec lequel faire l'union
	 * @return l'union des deux BasicSet
	 */
	public BasicSet union(BasicSet set) {
		BasicSet unionSet = (BasicSet) this.clone();
		unionSet.addAll(set);
		return unionSet;
	}

	/**
	 * Realise l'intersection de deux BasicSet
	 * @param set l'ensemble avec lequel faire l'intersection
	 * @return l'intersection des deux BasicSet
	 */
	public BasicSet intersection(BasicSet set) {
		BasicSet intersectionSet = (BasicSet) this.clone();
		intersectionSet.retainAll(set);
		return intersectionSet;
	}

	/**
	 * Realise la difference de deux BasicSet
	 * @param set l'ensemble avec lequel faire la difference
	 * @return la difference des deux BasicSet
	 */
	public BasicSet difference(BasicSet set) {
		BasicSet differenceSet = (BasicSet) this.clone();
		differenceSet.removeAll(set);
		return differenceSet;
	}

	/**
	 * Verifie que le BasicSet contient (inclus) le BasicSet demande
	 * @param set le BasicSet dont on verifie l'inclusion
	 * @return vrai si le BasicSet parametre est inclu
	 */
	public boolean isIncluding(BasicSet set) {
		return this.containsAll(set);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object setObj) {
		if (!(setObj instanceof BasicSet))
			return false;

		BasicSet set = (BasicSet) setObj;
		return (this.containsAll(set) && set.containsAll(this));
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.TreeSet#clone()
	 */
	@Override
	public BasicSet clone() {
		BasicSet newBasicSet = new BasicSet();
		newBasicSet.addAll(this);
		return newBasicSet;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	@Override
	public String toString() {
		String str = "{"; //$NON-NLS-1$

		Iterator<String> it = iterator();
		if (it.hasNext())
			str = str + it.next();

		while (it.hasNext())
			str = str + ", " + it.next(); //$NON-NLS-1$

		str = str + "}"; //$NON-NLS-1$
		return str;
	}

	/**
	 * Renvoie un ensemble contenant uniquement les elements positifs
	 * @return L'ensemble prive des elements negatifs
	 */
	public BasicSet PT() {
		BasicSet out = new BasicSet();
		for(String i : this) {
			if(!BasicSet.isNegative(i)) {
				out.add(i);
			}
		}
		return out;
	}
	/**
	 * Calcule le nombre d'elements negatifs contenu dans l'ensemble
	 * @return le nombre d'elements negatifs
	 */
	public int NNT() {
		int out = 0;
		for(String i : this) {
			if(BasicSet.isNegative(i)) {
				out++;
			}
		}
		return out;
	}
	/**
	 * Renvoie un Ensemble compose des elements positifs et des elements qui sont negatifs
	 * dans l'ensemble de depart ramene en positif ( A B' C D' -> A B C D )
	 * @return l'ensemble des elements ramenes positifs
	 */
	public BasicSet P() {
		BasicSet out = new BasicSet();
		for(String i : this) {
			if(BasicSet.isNegative(i)) {
				out.add(BasicSet.negation(i));
			} else {
				out.add(i);
			}
		}
		return out;
	}


	/**
	 * Calcule un support d'elements mixtes en utilisant les supports des elements positif
	 * Supp(T) = Somme(X allant de PT(T) a T ) de  (-1)^NNT(X) * supp(P(X))
	 * @param bs Ensemble sur lequel calculer le support mixte
	 * @param ctx Contexte
	 * @return
	 */
	public static double suppMixte(BasicSet bs, BinaryContext ctx) {
		double supp = 0;

		// X les elements postifs
		BasicSet X = bs.PT();

		// T les elements negatifs
		BasicSet T = bs.clone();
		T.removeAll(bs.PT());

		boolean fin = false;
		while(!fin) {
			if(X.NNT()%2!=1) {
				supp+=ctx.support(X.P());
			} else {
				supp-=ctx.support(X.P());
			}
			String ajout = T.pollFirst();
			if(ajout!=null) {
				X.add(ajout);
			} else {
				fin=true;
			}
		}
		return supp;
	}

	public static double calcSupportMixte(BasicSet bs, BinaryContext context) {
		double support = 0.0;
		BasicSet bsPositifs = bs.PT();
		BasicSet bsNegatifs = bs.clone();
		bsNegatifs.removeAll(bsPositifs);

		// We iterate from 0 to n, where n is the number of negatives
		int n = bsNegatifs.size();

		for (int i = 0; i <= n; i++) {
			double pow = Math.pow(-1.0,i);
			// On ajoute i elements negatifs dans la liste des positifs.
			if (i == 0) {
				if (bsPositifs.size() == 0) {
					support = support + pow;
				} else {
					support = support + pow * context.support(bsPositifs);
				}
			} else {

				BasicSet lesPositifs = bsPositifs.clone();
				Iterator<String> it = bsNegatifs.iterator();
				if (i == 1) {
					while (it.hasNext()) {
						String next = it.next();
						lesPositifs.add(BasicSet.negation(next));
						support = support + pow * context.support(lesPositifs);
						lesPositifs.remove(BasicSet.negation(next));
					}
				}

			}

		}

		return support;
	}

	/**
	 * On detecte les couples du type {a, a'}, {b, b'}, {c, c'}
	 * @param b
	 * @return true s'il s'agit d'une contradiction.
	 */
	public static boolean isContradictionBasicSet(BasicSet b) {
		if (b.size() == 2) {
			Iterator<String> itr = b.iterator();
			String first = "";
			while (itr.hasNext()) {
				if (first.isEmpty()) {
					first = itr.next();
				} else {
					String second = itr.next();
					if (first.equals(second + BasicSet.symbNeg) || second.equals(first + BasicSet.symbNeg)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}

