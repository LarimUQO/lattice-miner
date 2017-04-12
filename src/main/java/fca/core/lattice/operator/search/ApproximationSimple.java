package fca.core.lattice.operator.search;

import java.util.Iterator;

import fca.core.context.Context;
import fca.core.context.binary.BinaryContext;
import fca.core.lattice.ConceptLattice;
import fca.core.lattice.DataCel;
import fca.core.lattice.FormalConcept;
import fca.core.lattice.operator.Operator;
import fca.core.util.BasicSet;
import fca.core.util.Couple;
import fca.core.util.Triple;




/**
 * Opération d'approximation simple sur un treillis plat </br> L'idée d'approximation d'un couple
 * (X,Y), non nécessairement complet, au sein d'un treillis vient initialement de Professeur Mohamed
 * Quafafou de l'université d'Aix-Marseille et a été reformulée par Rokia Missaoui de l'université
 * du Québec en Outaouais.
 * @author Ludovic Thomas, Bennis Aicha
 * @version 1.4
 */
public class ApproximationSimple extends Operator<ConceptLattice, DataCel, Triple<ConceptLattice, ConceptLattice, ConceptLattice>> {

	/**
	 * Constructeur d'une opération d'approximation simple sur un treillis plat
	 * @param data le treillis dont sur lequel on doit faire l'approximation
	 */
	public ApproximationSimple(ConceptLattice data) {
		super(data, "ApproximationSimple"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see fca.lattice.conceptual.operations.Operator#perform(java.lang.Object)
	 */
	@Override
	public Triple<ConceptLattice, ConceptLattice, ConceptLattice> perform(DataCel entry) {
		ConceptLattice latticeL1 = approximationL1Rec(entry, data.getTopConcept());
		Couple<ConceptLattice, ConceptLattice> latticeL2andUI = approximationL2etUIRec(entry, data.getBottomConcept(),
				latticeL1);
		return new Triple<ConceptLattice, ConceptLattice, ConceptLattice>(latticeL1, latticeL2andUI.getFirst(),
				latticeL2andUI.getSecond());
	}

	/**
	 * Realise l'approximation par rapport à l'extension afin de récupérer le treillis L1. C'est une
	 * recherche recursive dont le premier appel est fait pour le supremum
	 * @param c la cellule à approximer
	 * @param fc le concept formel courant dans la recherche recursive
	 * @return le treillis L1
	 */
	private ConceptLattice approximationL1Rec(DataCel c, FormalConcept fc) {
		FormalConcept topL1 = fc.cloneIntExt();
		BasicSet extentC = c.getExtent();
		for (FormalConcept children : fc.getChildren()) {
			BasicSet extentChildren = children.getExtent();
			if ((extentC.size() <= extentChildren.size()) && (extentChildren.isIncluding(extentC))) {
				ConceptLattice lattL1rec = approximationL1Rec(c, children);
				topL1.addChild(lattL1rec.getTopConcept());
			}
		}
		return new ConceptLattice(topL1, "L1"); //$NON-NLS-1$
	}

	/**
	 * Realise l'approximation par rapport à l'intention et l'intersection avec L1 afin de récupérer
	 * le treillis L2 et UI. C'est une recherche recursive dont le premier appel est fait pour
	 * l'infimum
	 * @param c la cellule à approximer
	 * @param fc le concept formel courant dans la recherche recursive
	 * @param latticeL1 le treillis L1 utilisé pour le calcul de l'intersection
	 * @return le treillis L2 et UI
	 */
	private Couple<ConceptLattice, ConceptLattice> approximationL2etUIRec(DataCel c, FormalConcept fc,
			ConceptLattice latticeL1) {

		FormalConcept topL2 = fc.cloneIntExt();
		FormalConcept topUI = null;
		if (latticeL1.getConceptWithIntent(fc.getIntent()) != null) {
			topUI = fc.cloneIntExt();
		}
		BasicSet intentC = c.getIntent();
		for (FormalConcept parent : fc.getParents()) {
			BasicSet intentParent = parent.getIntent();
			if ((intentC.size() <= intentParent.size()) && (intentParent.isIncluding(intentC))) {
				Couple<ConceptLattice, ConceptLattice> latticeL2UIrec = approximationL2etUIRec(c, parent, latticeL1);
				topL2.addParent(latticeL2UIrec.getFirst().getBottomConcept());

				// Construction de UI
				ConceptLattice latticeUIrec = latticeL2UIrec.getSecond();
				if ((topUI != null) && (latticeUIrec != null)
						&& (latticeL1.getConceptWithIntent(parent.getIntent()) != null)) {
					if (latticeUIrec.getBottomConcept().getIntent().isIncluding(topUI.getIntent())) {
						topUI = latticeUIrec.getBottomConcept();
					} else {
						topUI.addParent(latticeUIrec.getBottomConcept());
					}
				} else if ((topUI != null) && (latticeUIrec != null)) {
					if (latticeUIrec.getBottomConcept().getIntent().isIncluding(topUI.getIntent())) {
						topUI = latticeUIrec.getBottomConcept();
					}
				} else if (latticeUIrec != null) {
					topUI = latticeUIrec.getBottomConcept();
				}
			}
		}

		ConceptLattice latticeL2 = new ConceptLattice(topL2, "L2", true); //$NON-NLS-1$
		ConceptLattice latticeUI = null;
		if (topUI != null) {
			latticeUI = new ConceptLattice(topUI, "UI", true); //$NON-NLS-1$
		}
		return new Couple<ConceptLattice, ConceptLattice>(latticeL2, latticeUI);
	}



	/**
	 * Calcul des attributs d'un objet donne
	 * @param o  objet dont on veut calculer les attibuts
	 * @param c le contexte dans lequel on fait la recherche
	 * @return liste des attributs
	 */

	public BasicSet CalculAttributes(String o, Context c) {
		BasicSet att=c.getAttributesFor(o);
		return att;
	}
	/**
	 * Calcul des objets d'un attribut donne
	 * @param a attribut dont on veut calculer les objets
	 * @param c le contexte dans lequel on fait la recherche
	 * @return liste des objets
	 */

	public BasicSet CalculObjets(String a, Context c) {
		BasicSet obj=c.getObjectsFor(a);
		return obj;
	}


	/**
	 * Calcul d'attributs d'une liste d'objets
	 * @param extentc objets dont on veut calculer les attributs
	 * @param cxt contexte dans lequel on fait la recherche
	 * @return res BasicSet 
	 */ 
	public BasicSet CalculXPrime(BasicSet extentc, Context cxt) {
		BasicSet exttemp=(BasicSet)extentc.clone();
		int nbobjets=exttemp.size();
		int i;
		BasicSet[] v=new BasicSet[nbobjets];
		String a=exttemp.first();
		v[0]=CalculAttributes(a, cxt);
		exttemp.remove(a);
		BasicSet res=(BasicSet) v[0].clone();

		for(i=1; i<nbobjets; i++){
			a=exttemp.first();
			v[i]=CalculAttributes(a, cxt);
			res=res.intersection(v[i]);
			exttemp.remove(a);
		}
		return res;
	}


	/**
	 * Calcul d'objets d'une liste d'attributs
	 * @param intentionc attributs dont on veut calculer les objets
	 * @param cxt contexte dans lequel on fait la recherche
	 * @return res BasicSet
	 */	
	public BasicSet CalculYPrime(BasicSet intentionc, Context cxt) {
		BasicSet inttemp=(BasicSet)intentionc.clone();
		int nbobjets= inttemp.size();
		int i;
		BasicSet[] v=new BasicSet[nbobjets];
		String a= inttemp.first();
		v[0]=CalculObjets(a, cxt);
		inttemp.remove(a);
		BasicSet res=(BasicSet) v[0].clone();


		for(i=1; i<nbobjets; i++){
			a= inttemp.first();
			v[i]=CalculObjets(a, cxt);
			res=res.intersection(v[i]);
			inttemp.remove(a);
		}
		return res;
	}



	/**
	 * Calcul du Xseconde
	 * @param extension objets dont on veut calculer les attributs
	 * @param cxt contexte dans lequel on fait la recherche
	 * @return Xseconde BasicSet
	 */

	public BasicSet CalculXseconde(BasicSet extension, Context cxt) {
		BasicSet Xprime=CalculXPrime(extension,cxt);
		BasicSet Xseconde=CalculYPrime(Xprime,cxt);
		return Xseconde;

	}
	/**
	 * Partie pour Implication
	 */

	/**
	 * Calcul du Yseconde
	 * @param intention attributs dont on veut calculer les objets
	 * @param cxt contexte dans lequel on fait la recherche
	 * @return Yseconde BasicSet
	 */
	public BasicSet CalculYseconde(BasicSet intention, Context cxt) {

		BasicSet Yseconde=new BasicSet();
		BasicSet vide=new BasicSet();
		BasicSet Yprime=CalculYPrime(intention,cxt);

		if(Yprime.equals(vide)){
			BasicSet y=new BasicSet();
			y.addAll(cxt.getAttributes());
			//Yseconde=CalculYsecondeNull(intention,cxt);
			//if(Yseconde.equals(vide))
			Yseconde=y;
		}
		else{
			Yseconde=CalculXPrime(Yprime,cxt);
		}

		return Yseconde;

	}

	/**
	 * XprimeNull calcul de Xprime pour le cas ou X est null	
	 * @param extenc
	 * @param cxt
	 * @return le ferme de X null
	 * @author Safwat
	 */

	public BasicSet CalculXprimeNull(BasicSet extenc, Context cxt) {

		//int nbobjets= inttemp.size();
		BasicSet v=new BasicSet();
		BasicSet v2=new BasicSet();
		BasicSet v3=new BasicSet();

		for(int i=0;i<cxt.getAttributeCount();i++){
			v.add(cxt.getAttributeAt(i));

		}
		for(String item: v){

			Iterator<String> it=v.iterator(); // Ajout de <String>
			while(it.hasNext()){
				String it2=(String) it.next();
				if(!item.equals(it2)){

					BasicSet b2=new BasicSet();
					BasicSet b3=new BasicSet();

					b3=this.CalculObjets(item,cxt).intersection(CalculObjets(it2,cxt));

					if(!b3.equals(b2)){
						v2.add(item);

					}

					v3=v.difference(v2);
				}
			}
		}

		return v3;
	}

	/**
	 * 
	 * @param intentionc
	 * @param cxt
	 * @return
	 * @author Safwat
	 */


	public BasicSet CalculYprimeNull(BasicSet intentionc, Context cxt) {

		BasicSet v=new BasicSet();
		BasicSet v2=new BasicSet();
		BasicSet v3=new BasicSet();

		for(int i=0;i<cxt.getObjectCount();i++){
			v.add(cxt.getObjectAt(i));

		}
		for(String item: v){

			Iterator<String> it=v.iterator(); // Ajout de <String>
			while(it.hasNext()){
				String it2=/*(String) */it.next(); // Suppression du cast inutile avec l'ajout prededent

				if(!item.equals(it2)){

					BasicSet b2=new BasicSet();
					BasicSet b3=new BasicSet();

					b3=CalculAttributes(item,cxt).intersection(CalculAttributes(it2,cxt));

					if(!b3.equals(b2)){
						//System.out.println("itemmmmmm222 :"+item);
						v2.add(item);
					}

					v3=v.difference(v2);
				}
			}
		}

		return v3;
	}

	/**
	 * CalculYsecondeNull
	 * @param BasicSet intention
	 * @param BasicSet
	 * @return un BasicSet
	 * @author Safwat
	 */

	public BasicSet CalculYsecondeNull(BasicSet intention, Context cxt) {
		BasicSet Yprime=CalculYprimeNull(intention,cxt);

		BasicSet Yseconde=CalculXprimeNull(Yprime,cxt);
		return Yseconde;

	}



	/**
	 * Calcul du Alpha1
	 * @param cel cellule pour laquelle on calcule le Alpha1
	 * @param cxt contexte courant
	 * @return celres Datacel cellule representant le alpha1
	 */
	public DataCel Alpha1(DataCel cel, Context cxt) {
		//extension de la cellule
		BasicSet ext=cel.getExtent();

		//intention de la cellule
		BasicSet intention=cel.getIntent();
		BasicSet XSeconde=CalculXseconde(ext,cxt);
		BasicSet YPrime=CalculYPrime(intention,cxt);

		//cas ou l'intersection de XSeconde et Yprime est vide 
		if(XSeconde.intersection(YPrime).isEmpty()==true){
			return null;
		}else{
			BasicSet intersection2=XSeconde.intersection(YPrime);	
			BasicSet extension2=(BasicSet)intersection2.clone();
			BasicSet intersectionprime=CalculXPrime(intersection2,cxt);
			DataCel celres= new DataCel(extension2,intersectionprime);

			return celres;
		}

	}	

	/**
	 * Calcul du Alpha2
	 * @param cel cellule pour laquelle on calcule le Alpha2
	 * @param cxt contexte courant
	 * @return celres Datacel cellule representant le alpha2
	 */
	public DataCel Alpha2(DataCel cel, Context cxt) {
		//extension de la cellule
		BasicSet ext=cel.getExtent();

		//intention de la cellule
		BasicSet intention=cel.getIntent();

		BasicSet XSeconde=CalculXseconde(ext,cxt);
		BasicSet YPrime=CalculYPrime(intention,cxt);

		//dans le cas ou la difference entre XSeconde et YPrime est vide 
		if(XSeconde.difference(YPrime).isEmpty()==true)
		{
			return null;
		}else{
			BasicSet dif=XSeconde.difference(YPrime);	
			BasicSet difprime=CalculXPrime(dif,cxt);
			BasicSet dif3=(BasicSet)difprime.clone();
			BasicSet difSeconde=CalculYPrime(difprime,cxt);
			DataCel celres= new DataCel(difSeconde,dif3);

			return celres;
		}
	}

	/**
	 * Calcul du Alpha3
	 * @param cel cellule pour laquelle on calcule le Alpha3
	 * @param cxt contexte courant
	 * @return celres Datacel cellule representant le alpha3
	 */
	public DataCel Alpha3(DataCel cel, Context cxt) {
		//extension de la cellule
		BasicSet ext=cel.getExtent();
		//intention de la cellule
		BasicSet intention=cel.getIntent();

		BasicSet XSeconde=CalculXseconde(ext,cxt);
		BasicSet YPrime=CalculYPrime(intention,cxt);

		//dans le cas ou la difference entre YPrime et XSeconde est vide 
		if(YPrime.difference(XSeconde).isEmpty()==true){
			return null;
		}else{
			BasicSet dif=YPrime.difference(XSeconde);	
			BasicSet difprime=CalculXPrime(dif,cxt);
			BasicSet dif3=(BasicSet)difprime.clone();
			BasicSet difSeconde=CalculYPrime(difprime,cxt);
			DataCel celres= new DataCel(difSeconde,dif3);

			return celres;
		}
	}	

	/**
	 * Calcul du Beta1
	 * @param cel cellule pour laquelle on calcule le Beta1
	 * @param cxt contexte courant
	 * @return celres Datacel cellule representant le Beta1
	 */
	public DataCel Beta1(DataCel cel, Context cxt) {
		//extension de la cellule
		BasicSet ext=cel.getExtent();
		//intention de la cellule
		BasicSet intention=cel.getIntent();
		//calcul de xprime et yseconde
		BasicSet XPrime=CalculXPrime(ext,cxt);
		BasicSet YSeconde=CalculYseconde(intention,cxt);
		//cas ou l'intersection est vide
		if(YSeconde.intersection(XPrime).isEmpty()==true){
			return null;
		}else{
			BasicSet intersection2=YSeconde.intersection(XPrime);	
			BasicSet intention2=(BasicSet)intersection2.clone();
			BasicSet intersectionprime=CalculYPrime(intersection2,cxt);
			DataCel celres= new DataCel(intersectionprime,intention2);
			return celres;
		}

	}	

	/**
	 * Calcul du Beta2
	 * @param cel cellule pour laquelle on calcule le Beta2
	 * @param cxt contexte courant
	 * @return celres Datacel cellule representant le Beta2
	 */
	public DataCel Beta2(DataCel cel, Context cxt) {
		//extension de la cellule
		BasicSet ext=cel.getExtent();
		//intention de la cellule
		BasicSet intention=cel.getIntent();
		BasicSet XPrime=CalculXPrime(ext,cxt);
		BasicSet YSeconde=CalculYseconde(intention,cxt);

		//cas ou la difference est nulle
		if(YSeconde.difference(XPrime).isEmpty()==true){
			return null;
		}else{
			BasicSet dif=YSeconde.difference(XPrime);	
			BasicSet difprime=CalculYPrime(dif,cxt);
			BasicSet dif3=(BasicSet)difprime.clone();
			BasicSet difSeconde=CalculXPrime(difprime,cxt);
			DataCel celres= new DataCel(dif3,difSeconde);
			return celres;
		}
	}

	/**
	 * Calcul du Beta3
	 * @param cel cellule pour laquelle on calcule le Beta3
	 * @param cxt contexte courant
	 * @return celres Datacel cellule representant le Beta3
	 */
	public DataCel Beta3(DataCel cel, Context cxt) {
		//extension de la cellule
		BasicSet ext=cel.getExtent();
		//intention de la cellule
		BasicSet intention=cel.getIntent();
		BasicSet XPrime=CalculXPrime(ext,cxt);
		BasicSet YSeconde=CalculYseconde(intention,cxt);

		//cas ou la difference est nulle
		if(XPrime.difference(YSeconde).isEmpty()==true){
			return null;
		}else{
			BasicSet dif=XPrime.difference(YSeconde);	
			BasicSet difprime=CalculYPrime(dif,cxt);
			BasicSet dif3=(BasicSet)difprime.clone();
			BasicSet difSeconde=CalculXPrime(difprime,cxt);
			DataCel celres= new DataCel(dif3,difSeconde);
			return celres;
		}
	}






	/**
	 * La methode CelluleType permet d'envoyer le type de la cellule
	 * @param ctx : le contexte courant
	 * @param cel : la cellule dont on veut connaitre le type
	 */

	public int CelluleType(Context ctx,DataCel cel){
		//int type=0;
		ConceptLattice c= new ConceptLattice((BinaryContext) ctx);
		BasicSet extent=cel.getExtent();
		BasicSet intent=cel.getIntent();
		ApproximationSimple a=new ApproximationSimple (c);

		if(a.CalculXPrime(extent,ctx).size()==0|| a.CalculYPrime(intent,ctx).size()==0 ||a.CalculXseconde(extent, ctx).size()==0 || a.CalculYseconde(intent, ctx).size()==0){
			return 7;
		} else 	if(a.CalculXPrime(extent,ctx).equals(intent) && a.CalculYPrime(intent,ctx).equals(extent)){
			//System.out.println("Concept");
			return 1;
		} else if(a.CalculXseconde(extent, ctx).equals(a.CalculYPrime(intent, ctx))){
			//System.out.println("Protoconcept");
			return 2;
		} else if(a.CalculXPrime(extent, ctx).equals(intent) || a.CalculYPrime(intent, ctx).equals(extent)){
			//System.out.println("semi-concept");
			return 3;
		} else if(a.CalculYPrime(intent, ctx).isIncluding(extent)){
			//System.out.println("Préconcept");
			return 4;
		} else if(a.CalculXseconde(extent, ctx).isIncluding(a.CalculYPrime(intent, ctx)) || a.CalculXseconde(extent, ctx).equals(a.CalculYPrime(intent, ctx))){
			//System.out.println("n-concept");
			return 5;
		} else {
			// System.out.println("Not found");
			return 6;
		}
	}
}









