package fca.core.lattice;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import fca.core.context.binary.BinaryContext;
import fca.core.lattice.operator.search.ApproximationSimple;
import fca.core.rule.Rule;
import fca.core.util.BasicSet;

/**
 * Classe Implication qui permet le calcul de NextClosure 
 * Permet egalement le calcul des pseudo intents.
 * Generation de la base de Guigues-Duquenne qui contient
 * une liste d implication de type P->P'' ou P est un pseudo intent 
 * et P'' son ferme prive de P.
 * @author Safwat Maya
 * @version 1.0
 */

public class Implication {

	private BinaryContext context;

	private Vector <Rule> Limps;

	public Implication(BinaryContext c,Vector <Rule> l){
		context=c;
		Limps=l;
	}


	public Vector<Rule> getImp(){
		return Limps;
	}

	public void setImp(Vector<Rule> l){
		Limps=l;
	}

	/**
	 * RepresentationBinaire represente un BasicSet sous la forme binaire.
	 * Pour chaque attribut present la valeur est mise a 1.
	 * @param A une map qui pour chaque attribut possede pour valeur 0 ou 1.
	 * @return A
	 */

	Map <String,Integer> RepresentationBinaire(BasicSet A){
		Map<String,Integer> bit=new TreeMap<String,Integer>();
		Vector<String> v=context.getAttributes();		
		BasicSet attributes=new BasicSet();
		attributes.addAll(v);

		for(String item: attributes )
		{
			bit.put(item,0);		
		}
		for(String item: attributes)
		{		
			for(String it: A){	
				if(it.equals(item)){
					bit.put(item,1);
				}	
			}
		}

		return bit;
	}

	/**
	 * LecticOrder ordre lexical
	 * @param A
	 * @param B
	 * @return true si A est inferieur a B.
	 */

	boolean LecticOrder(Map<String,Integer> A, Map<String,Integer> B ){
		Vector<String> v=context.getAttributes();

		BasicSet attributes=new BasicSet();
		attributes.addAll(v);

		Iterator<String> it=attributes.iterator();

		if(B.size()==0)
			return false;

		if(A.size()==0)
			return true;

		while(it.hasNext()){

			String item=it.next();

			if(A.get(item)!=B.get(item)){
				if(A.get(item)==0){
					//System.out.println("bit1 plus petit"+A);
					return true;
				}
				else	if(B.get(item)==0){
					//System.out.println(B);
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * FirstClosure()
	 * @return un BasicSet qui est le ferme de l ensemble vide
	 * qui est tout simplement le Xprime de tous les objets.
	 */
	public BasicSet FirstClosure(){
		BasicSet v=new BasicSet();
		BasicSet res=new BasicSet();
		ConceptLattice cp=new ConceptLattice(context);
		ApproximationSimple ap=new ApproximationSimple(cp);	
		Vector<String> b=context.getObjects();
		v.addAll(b);
		res=ap.CalculXPrime(v, context);

		return res;
	}


	/**
	 * NextClosure calcule le prochain ferme de A
	 * @param M ensemble des attributs du Contexte
	 * @param A BasicSet
	 * @return A BasicSet contenant le prochain ferme.
	 */

	BasicSet NextClosure(BasicSet M,BasicSet A){
		ConceptLattice cp=new ConceptLattice(context);
		ApproximationSimple ap=new ApproximationSimple(cp);
		Map <String,Integer> repbin=this.RepresentationBinaire(M);
		Vector<BasicSet> fermes=new Vector<BasicSet>();
		Set<String> key=repbin.keySet();
		Object[] items= key.toArray();

		/* on recupere la position i qui correspond a la derniere position de M*/
		int i=M.size()-1;
		boolean success=false;
		BasicSet plein=new BasicSet();
		Vector <String> vv=context.getAttributes();
		plein.addAll(vv);


		while(success==false ){		

			String item=items[i].toString();

			if(!A.contains(item)){	

				/* Ensemble contenant item associe a i*/
				BasicSet I=new BasicSet();
				I.add(item);

				/*A union I*/	
				A=A.union(I);
				BasicSet union=(BasicSet) A.clone();
				//System.out.println(" union  "+union);

				//fermes.add(union);

				fermes.add(union);
				//System.out.println("ll11 "+fermes);

				/* Calcul du ferme de A*/
				BasicSet b=ap.CalculYseconde(A,context);

				//BasicSet b=this.LpClosure(A);
				//System.out.println("b procchain "+b);

				//System.out.println("b sec "+b);
				BasicSet diff=new BasicSet();
				diff=b.difference(A);
				Map <String,Integer> repB=this.RepresentationBinaire(diff);
				BasicSet test=new BasicSet();
				Map <String,Integer> testt=RepresentationBinaire(test);
				testt.put(item, 1);

				/* on teste si l ensemble B\A est plus petit que l ensemble contenant i
				 * Si B\A est plus petit alors on affecte B à A.
				 **/

				if(item.equals(b.first())||LecticOrder(repB,testt)){
					//System.out.println("A succes=true "+ A);
					A=b;
					success=true;	  

				}else{
					A.remove(item);
				}	
			}
			else{
				A.remove(item);
			}			
			i--;
		}		
		return A;
	}


	/**
	 * AllClosure calcule tous les fermes d un ensemble d attributs.
	 * @param 
	 * @return un vecteur contenant tous les fermes.
	 */

	public Vector<BasicSet> AllClosure(){

		Vector<BasicSet> fermes=new Vector<BasicSet>();
		boolean success=false;
		int i=0;
		//BasicSet b=new BasicSet();
		fermes.add(FirstClosure());
		BasicSet attributs=new BasicSet();
		attributs.addAll(context.getAttributes());
		while(success==false){
			//System.out.println("fermeeees iiii "+fermes.get(i));
			BasicSet clone=(BasicSet) fermes.get(i).clone();
			fermes.add(NextClosure(attributs,clone));
			//System.out.println("fermeeees "+fermes);	
			if(fermes.lastElement().size()==context.getAttributeCount()){
				success=true;
				//System.out.println("iiiii "+i);
				break;
			}
			i++;
		}
		return fermes;
	}


	/**
	 * Lp Closure Calcul du ferme de x
	 * permet d obtenir les pseudo intents par la suite.
	 * @param x
	 * @return le ferme de x
	 */
	@SuppressWarnings("unchecked")
	public BasicSet LpClosure(BasicSet x){

		int used_imps=0;
		BasicSet old_closure = null;
		BasicSet new_closure;
		BasicSet T;
		int usable_imps;
		int use_now_imps;
		Vector<String> att=context.getAttributes();
		BasicSet M = new BasicSet();
		M.addAll(att);


		int [] avoid=new int[M.size()];
		if(getImp().isEmpty())
			return x;
		for(int i=0;i<M.size();i++){
			//int a=(int)Math.pow(6,4);
			int entier =( 1 << getImp().size())-1;

			avoid[i]= entier;
			//System.out.println("L size "+Limps.size()+" entier "+entier+" i "+i+ " avoid  "+avoid[i]);
			for(int k=0;k<getImp().size();k++){
				Vector<Rule> rrr=(Vector<Rule>) getImp().clone();

				Rule r=rrr.get(k);

				Object [] tt= M.toArray();
				String ite=(String) tt[i];
				BasicSet xx=new BasicSet();
				xx.add(ite);

				BasicSet ant= (BasicSet) r.getAntecedent().clone();

				if(ant.contains(ite)){

					int bin=1 << k;
					avoid[i] &= ~bin;		

				}
			}
		}

		used_imps=0;
		old_closure=new BasicSet();
		old_closure.add("-1");
		new_closure=x;
		int inter;
		int union;

		while(!new_closure.equals(old_closure)){
			old_closure=(BasicSet) new_closure.clone();

			T=M.difference(new_closure);
			int ent=1 << (getImp().size());
			inter=ent-1;
			union =0;

			for(int ii=0; ii<M.size(); ii++){

				Object[] tt=M.toArray();
				String ite=(String) tt[ii];
				if(T.contains(ite))
					inter &= avoid[ii];
				if(new_closure.contains(ite))
					union |= avoid[ii];
			}

			usable_imps = inter & union;

			use_now_imps = usable_imps & (~used_imps);
			used_imps = usable_imps;

			for(int ii=0;ii<getImp().size();ii++){

				int enti=use_now_imps & (1 << ii);

				//System.out.println("enti "+enti+"basicset "+old_closure);
				if( enti!= 0){
					Vector<Rule> rrr=(Vector<Rule>) getImp().clone();
					Rule r=rrr.get(ii);
					new_closure=new_closure.union(r.getConsequence());

				}
			}

		}

		return new_closure;

	}


	/**
	 * NextLClosure peremet de calculer le prochain ferme 
	 * ou prochain pseudo intent
	 * @param M un basicSet qui represente les attributs du context
	 * @param A un basicSet
	 * @return un BasicSet prochain closed ou pseudo closed
	 */

	BasicSet NextLClosure(BasicSet M,BasicSet A){
		//ConceptLattice cp=new ConceptLattice(context);
		//ApproximationSimple ap=new ApproximationSimple(cp);
		Map <String,Integer> repbin=this.RepresentationBinaire(M);
		Vector<BasicSet> fermes=new Vector<BasicSet>();
		Set<String> key=repbin.keySet();
		Object[] items= key.toArray();

		/* on recupere la position i qui correspond a la derniere position de M*/
		int i=M.size()-1;
		boolean success=false;
		BasicSet plein=new BasicSet();
		Vector <String> vv=context.getAttributes();
		plein.addAll(vv);


		while(success==false ){		

			String item=items[i].toString();

			if(!A.contains(item)){	

				/* Ensemble contenant item associe a i*/
				BasicSet I=new BasicSet();
				I.add(item);

				/*A union I*/	
				A=A.union(I);
				BasicSet union=(BasicSet) A.clone();
				//System.out.println(" union  "+union);

				//fermes.add(union);

				fermes.add(union);


				/* Calcul du ferme de A*/


				BasicSet b=this.LpClosure(A);
				//System.out.println("b procchain "+b);

				BasicSet diff=new BasicSet();
				diff=b.difference(A);
				Map <String,Integer> repB=this.RepresentationBinaire(diff);
				BasicSet test=new BasicSet();
				Map <String,Integer> testt=RepresentationBinaire(test);
				testt.put(item, 1);

				/* on teste si l ensemble B\A est plus petit que l ensemble contenant i
				 * Si B\A est plus petit alors on affecte B à A.
				 **/

				if(item.equals(b.first())||LecticOrder(repB,testt)){
					//System.out.println("A succes=true "+ A);
					A=b;
					success=true;	  

				}else{
					A.remove(item);
				}	
			}
			else{
				A.remove(item);
			}			
			i--;
		}		
		return A;
	}

	/**
	 * StemBase genere la Base de GuigueS-Duquenne
	 * @return un Vecteur contenant la liste d implications.
	 */

	public Vector<Rule> StemBase(){
		//Vector <Rule> rule=new Vector <Rule>();
		ConceptLattice cp=new ConceptLattice(context);
		ApproximationSimple ap=new ApproximationSimple(cp);

		BasicSet A=new BasicSet();
		BasicSet cnt=new BasicSet();
		BasicSet b=new BasicSet();
		cnt.addAll(context.getAttributes());
		while(!A.equals(cnt)){

			if(A.isEmpty()){
				b =ap.CalculYsecondeNull(A, context);
			}else{
				b=ap.CalculYseconde(A, context);
			}
			if(!A.equals(b)){
				BasicSet diff=b.difference(A);
				BasicSet ant=(BasicSet) A.clone();
				double lift = 1;
				double confidence = 1;
				Rule r=new Rule(ant,diff,context.support(ant.clone().union(diff)), confidence, lift );
				//int i=0;
				getImp().add( r);				
			}
			A=this.NextLClosure(cnt, A);
		}

		return getImp();

	}
}
