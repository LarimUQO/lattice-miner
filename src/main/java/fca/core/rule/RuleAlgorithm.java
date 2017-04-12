package fca.core.rule;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import fca.core.lattice.ConceptLattice;
import fca.core.util.BasicSet;
import fca.messages.CoreMessages;

/**
 * Classe abstraite d'algorithme recherche de regles.
 * @author Maya Safwat
 * @author Arnaud Renaud-Goud
 * @version 1.0
 */
public abstract class RuleAlgorithm {

	/**
	 * Le treillis associe
	 */
	protected ConceptLattice lattice;

	/**
	 * Confiance minimale
	 */
	protected double minConfidence;

	/**
	 * Support minimal
	 */
	protected double minSupport;

	/** Ensemble de regles produites */
	protected Vector<Rule> rules;


	/**
	 * Constructeur vide
	 * @author Maya Safwat
	 */
	public RuleAlgorithm(){
		rules = new Vector<Rule>();
	}


	/**
	 * Constructeur d'un algorithme de recherche de regle
	 * @param lat le treillis graphique ou l'on recherche les regles
	 * @param minSupp le support minimum
	 * @param minConf la confiance minimum
	 */
	public RuleAlgorithm(ConceptLattice lat, double minSupp, double minConf) {
		lattice = lat;
		minConfidence = minConf;
		minSupport = minSupp;
		rules = new Vector<Rule>();
	}

	
	
	/**
	 * @return la confiance minimale
	 */
	public double getMinimumConfidence() {
		return minConfidence;
	}

	/**
	 * Affectation de la confiance minimale
	 * @param minConf la nouvelle confiance minimale
	 */
	public void setMinimumConfidence(double minConf) {
		minConfidence = minConf;
	}

	/**
	 * @return le support minimal
	 */
	public double getMinimumSupport() {
		return minSupport;
	}

	/**
	 * Affectation du support minimal
	 * @param minSupp le nouveau support minimal
	 */
	public void setMinimumSupport(double minSupp) {
		minSupport = minSupp;
	}

	/**
	 * @return la base genre sous forme de Vecteur
	 */
	public Vector<Rule> getRules() {
		return rules;
	}

	public boolean setRules(Vector<Rule> rules) {
		this.rules = rules;
		return true;
	}

	/**
	 * @return le nombre de regles
	 */
	public int size() {
		return rules.size();
	}

	/**
	 * fermeture dun attribut dans un ensemble F (ensemble d implications)
	 * @param BasicSet un ensemble d attributs
	 *  //@param F un ensemble de regles
	 * @return ensemble ferme  de l attribut dans F. 
	 * 
	 */
	public BasicSet closure(BasicSet x/*,Vector<Rule> rules*/) {
		BasicSet update = new BasicSet();
		BasicSet newdep = new BasicSet();
		BasicSet add = new BasicSet();
		Map< String, Vector<Rule>> map = new HashMap<String, Vector<Rule>>();
		Map <Rule, Integer> count = new HashMap<Rule, Integer>();

		/* pour chaque regle on stocke le nombre d attributs dans la variable count*/
		for (int i = 0; i < rules.size(); i++) {
			Rule rule = rules.elementAt(i);
			count.put(rule, rule.getAntecedent().size());

			for (String item : rule.getAntecedent()) {
				/*si la map pour un item donne contient deja une regle alors 
				 * on ajoute la regle suivante en queue de liste */
				if(map.containsKey(item)){
					Vector <Rule> r=map.get(item);
					r.add(rule);
					map.put(item, r); // TODO a revoir (pourquoi?)
				}
				else {
					Vector<Rule> ru=new Vector<Rule> ();
					ru.add(rule);
					map.put(item, ru);
				}
			}
		}

		newdep = (BasicSet) x.clone();
		update = (BasicSet) x.clone();

		while(update.isEmpty()==false){
			//System.out.println("apres while upd  "+update);
			//String s=it.next();
			String s = "";
			s = update.first();
			//System.out.println("apres while s  "+s);

			update.remove(s);
			//System.out.println("apres while s  apres remove "+s);

			if(map.get(s) !=null){
				for(int k=0;k<map.get(s).size();k++){
					count.put(map.get(s).elementAt(k), (count.get(map.get(s).elementAt(k))-1));
					//System.out.println(map.get(s).elementAt(k));
					//System.out.println(count.get(map.get(s).elementAt(k)));
					if(count.get(map.get(s).elementAt(k))==0){
						add = map.get(s).elementAt(k).getConsequence().difference(newdep);
						//System.out.println("add "+add);
						newdep = newdep.union(add);
						//System.out.println("newdep "+newdep);

						update = update.union(add);
					}
				}					
			}
		}
		//System.out.println(newdep);
		return newdep;
	}
	
	/**
	 * Contraire de fermeture dun attribut dans un ensemble F (ensemble d implications)
	 * @param BasicSet un ensemble d attributs
	 * @return ensemble ferme  de l attribut dans F. 
	 * FIXME a corrige (resultats faux)
	 */
	public BasicSet antiClosure(BasicSet x) {
		BasicSet update = new BasicSet();
		BasicSet newdep = new BasicSet();
		BasicSet add = new BasicSet();
		Map< String, Vector<Rule>> map = new HashMap<String, Vector<Rule>>();
		Map <Rule, Integer> count = new HashMap<Rule, Integer>();

		/* pour chaque regle on stocke le nombre d attributs dans la variable count*/
		for (int i = 0; i < rules.size(); i++) {
			Rule rule = rules.elementAt(i);
			count.put(rule, rule.getConsequence().size());
			
			for (String item : rule.getConsequence()) {
				/*si la map pour un item donne contient deja une regle alors 
				 * on ajoute la regle suivante en queue de liste */
				if(map.containsKey(item)){
					Vector <Rule> r=map.get(item);
					r.add(rule);
					map.put(item, r); // TODO a revoir (pourquoi?)
				}
				else {
					Vector<Rule> ru=new Vector<Rule> ();
					ru.add(rule);
					map.put(item, ru);
				}
			}
		}
		//System.out.println(map);
		newdep = (BasicSet) x.clone();
		update = (BasicSet) x.clone();

		while(update.isEmpty()==false){
			//System.out.println("apres while upd  "+update);
			//String s=it.next();
			String s = "";
			s = update.first();
			update.remove(s);
			//System.out.println("apres while s  apres remove "+s);
			
			if(map.get(s) !=null){
				for(int k=0;k<map.get(s).size();k++){
					/* decrementation */
					count.put(map.get(s).elementAt(k), (count.get(map.get(s).elementAt(k))-1));
					if(count.get(map.get(s).elementAt(k))==0){ // FIXME
						//System.out.println(k);
						add = map.get(s).elementAt(k).getAntecedent().difference(newdep);
						newdep = newdep.union(add);
						//System.out.println("newdep "+newdep);

						update = update.union(add);
					}
				}					
			}
		}
		return newdep;
	}

	/**
	 * Methode Member qui teste si une regle est presente 
	 * dans la fermeture de F qui est donne en argument.
	 * @param F un ensemble de regles.
	 * @param x une dependance fonctionnelle.
	 * @return boolean.
	 */
	public boolean Member(/*Vector<Rule> v,*/Rule r) {
		boolean flag = false;
		BasicSet cons = r.getConsequence();
		BasicSet ant = r.getAntecedent();

		//if( closure(ant,v).isIncluding(cons))
		if( this.closure(ant).isIncluding(cons))
			flag=true;

		return flag;
	}


	/**
	 * Redundancy teste si dans un ensemble de regles il y a redondance ou pas.
	 * @param rules un vecteur de regles
	 * @return <code>true</code> s'il y a redondance, <code>false</code> sinon.
	 */
	public boolean Redundancy(/*Vector<Rule> rules*/) {
		 // boolean v=false;
		//Vector<Rule> clo = (Vector<Rule>) rules.clone();		
		int taille = this.rules.size();
		//int taille=clo.size();	

		//while (clo.size()>0 && taille>0) {	
		while (taille>0) {	   
			/* on recupere le premier element */
			//Rule rule=clo.firstElement();
			Rule rule = this.rules.firstElement();
			//clo.remove(rule);
			this.rules.remove(rules);
			taille--;

			//if(Member(clo, rule)) {
			if(this.Member(rule)) {
				this.rules.add(rule);
				return true;
				//v = true;
			}
			else {
				//	clo.add(rule);  
				this.rules.add(rule);
			}
		}
		return false;
		//return v;
	}



	/**
	 * NonRedundancy permet d'eliminer les dependances fonctionnelles
	 * deductibles des autres FDs.
	 * @param rules un vecteur de regles.
	 * @return Le vecteur de regles auquel on a enleve les redondances.
	 */
	public Vector<Rule> NonRedundancy(/*Vector<Rule> rules*/){
		//Vector<Rule> clo = (Vector<Rule>) rules.clone();
		//int taille = clo.size();	
		int taille = this.rules.size();

		while(/* clo.size()>0 && */taille>0 ) {	   
			/* on recupere le premier element */
			//Rule rule = clo.firstElement();
			Rule rule = this.rules.firstElement();
			//clo.remove(rule);
			this.rules.remove(rule);
			taille--;

			//if( !Member(clo,rule)) {
			if(!this.Member(rule)) {
				//clo.add(rule);
				this.rules.add(rule);
			}	
		}
		return this.rules;
		//return clo;
	}

	// FIXME Passage par reference! Impact a verifier.
	/**
	 * LeftRed methode qui permet de reduire les FDs a gauche
	 * @param rules un vecteur de regles
	 * @return le vecteur de regles avec la partie gauche de la regle reduite.
	 */
	public RuleAlgorithm leftRed() {

		for(int i=0; i<this.rules.size(); i++) {
			for(String item : this.rules.get(i).getAntecedent()) {
				BasicSet x = this.rules.get(i).getAntecedent().clone();
				x.remove(item);
				Rule r = new Rule(x, this.rules.get(i).getConsequence());
				if(this.Member(r)) {
					this.rules.elementAt(i).setAntecedent(x);
				}
			}
		}
		return this;

	}

	// FIXME Passage par reference! Impact a verifier.
	/**
	 * RightRed methode qui permet de reduire les FDs a droite
	 * @param rules un vecteur de regles
	 * @return le vecteur de regles avec la partie droite de la regle reduite.
	 */
	public /*Vector<Rule>*/ RuleAlgorithm rightRed(/*Vector<Rule> rules*/){
		//		Vector<Rule> rulClone = (Vector<Rule>) rules.clone();
		//		int taille=rules.size();	
		//
		//		if ((rules==null) || (rules.size()==0))
		//			return rules;
		//
		//		while ( taille>0 ) {
		//			/* on recupere le premier element */
		//			Rule rule = rulClone.firstElement();
		//			BasicSet consequence = rule.getConsequence();
		//
		//			for (String item : consequence) {	  
		//				//BasicSet consequence=(BasicSet) rule.getConsequence().clone();
		//				BasicSet consClone = (BasicSet) consequence.clone();	
		//				consClone.remove(item);
		//				Rule r = new Rule(rule.getAntecedent(), consClone);
		//
		//				BasicSet bs=new BasicSet();
		//				bs.add(item);
		//				Rule a = new Rule(rule.getAntecedent(), bs);
		//
		//				rulClone.remove(rule);
		//				rulClone.add(r);
		//
		//				if(Member(rulClone, a)){	    	
		//					rule.setConsequence(consClone);
		//					consequence=consClone;
		//				}
		//
		//				rulClone.add(rule);	
		//				rulClone.remove(r);
		//			}
		//
		//			taille--; 	
		//		}
		//
		//		return rulClone;
		if(this.rules == null || this.rules.size() == 0) {
			return this;
		}
		for(int taille = this.rules.size(); taille>0; taille--) {
			//while(taille > 0) {
			Rule rule = this.rules.firstElement();
			BasicSet consequence = rule.getConsequence();

			for(String item : consequence) {
				BasicSet consClone = (BasicSet) consequence.clone();	
				consClone.remove(item);
				Rule r = new Rule(rule.getAntecedent(), consClone);

				BasicSet bs = new BasicSet();
				bs.add(item);
				Rule a = new Rule(rule.getAntecedent(), bs);

				this.rules.remove(rule);
				this.rules.add(r);

				if(this.Member(a)) {
					rule.setConsequence(consClone);
					consequence = consClone;
				}

				this.rules.add(rule);
				this.rules.remove(r);

			}
			//taille--;
		}
		return this;
	}


	/**
	 * Reduce reduit la base de regles a droite et a gauche.
	 * @param rules Vecteur de regles
	 * @return Base de regle reduite a droite et a gauche.
	 */
	public Vector<Rule> Reduce(/*Vector<Rule> rules*/) {
		/*Vector<Rule> redBase = rightRed(leftRed(rules));*/
		Vector<Rule> redBase = this.leftRed().rightRed().rules;

		for( int i=0; i<redBase.size(); i++) {
			if( redBase.get(i).getConsequence() == null) {
				redBase.remove( redBase.get(i));
			}	
		}

		return redBase;		
	}

	// FIXME Passage par reference! Impact a verifier.
	/**
	 * Couverture minimale 1 retourne plus une base reduite que couverture minimale
	 * On utilisera plutot la Cover2
	 */

	public Vector<Rule> Cover(/*Vector<Rule> rules*/) {
		//Vector <Rule> g=f;

		if ((rules==null) || (rules.size()==0))
			return rules;
		for(int i=0; i<rules.size(); i++) {
			rules.elementAt(i).setAntecedent( rules.elementAt(i).getAntecedent());
			//rules.elementAt(i).setConsequence( closure(rules.elementAt(i).getAntecedent(), rules));
			rules.elementAt(i).setConsequence( this.closure(rules.elementAt(i).getAntecedent()));
		}
		//if(Redundancy(rules)) {
		//rules = NonRedundancy(rules);
		//}
		if(this.Redundancy()) {
			this.NonRedundancy();
		}
		return rules;
	}

	// FIXME Passage par reference! Impact a verifier.
	/**
	 * Calcule une base non redondante.
	 * @param rules Vecteur de regles.
	 * @return Vecteur de regles resultant du calcul de la couverture minimale.
	 */
	public Vector<Rule> Cover2(/*Vector<Rule> rules*/){
		//Vector <Rule> g=f;
		/*
		if ((rules==null) || (rules.size()==0))
			return rules;

		rules = Reduce(rules);
		if(Redundancy(rules)) {
			rules = NonRedundancy(rules);
		}

		return rules;*/

		
		if(this.rules==null || this.rules.size() == 0) {
			return this.rules;
		}
		this.rules = this.Reduce();
		if(this.Redundancy()) {
			this.NonRedundancy();
		}
		return this.rules;

	}

	/**
	 * Generation de l'ensemble des regles de la base generique
	 */
	public abstract void run();

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "*********** "+CoreMessages.getString("Core.rules")+" ***********\n\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		for (int i = 0; i < rules.size(); i++) {
			Rule rule = rules.elementAt(i);
			result += rule.toString();
			result += "\n"; //$NON-NLS-1$
		}

		result += "\n*****************************"; //$NON-NLS-1$
		return result;
	}
}