package fca.core.context.binary;

import java.util.Iterator;
import java.util.Vector;

import fca.core.context.Context;
import fca.core.context.nested.NestedContext;
import fca.core.lattice.ConceptLattice;
import fca.core.util.BasicSet;
import fca.core.util.LogicalFormula;
import fca.exception.AlreadyExistsException;
import fca.exception.InvalidTypeException;
import fca.exception.LMLogger;
import fca.gui.util.DialogBox;
import fca.messages.CoreMessages;
import fca.messages.GUIMessages;

/**
 * Viewer de contextes
 *
 * @author Genevieve Roberge
 * @author Ludovic Thomas
 * @author Linda Bogni
 * @author Sy Babacar
 * @author Nicolas Convers, Arnaud Renaud-Goud
 * @version 1.4
 */

public class BinaryContext extends Context {
	public static final String TRUE = "true"; //$NON-NLS-1$
	public static final String FALSE = ""; //$NON-NLS-1$

	/*** constantes pour les fleches ***/
	public static final String ARROWUP = "arrowup"; //$NON-NLS-1$
	public static final String ARROWDOWN = "arrowdown"; //$NON-NLS-1$
	public static final String DOUBLEARROW = "doblearrow"; //$NON-NLS-1$


	protected ConceptLattice conceptLattice;

	protected int countTRUE;

	public BinaryContext(BinaryContext bc) {
		super(bc);
		this.conceptLattice = bc.conceptLattice;
		this.countTRUE = bc.countTRUE;
	}

	public BinaryContext(String name) {
		super(name);
		conceptLattice = null;
		countTRUE = 0;
	}

	public BinaryContext(String name, int objNb, int attNb) {
		super(name, objNb, attNb);
		conceptLattice = null;
	}

	public BinaryContext(String name, Vector<String> objects, Vector<String> attributes, Vector<Vector<String>> values) {
		super(name, objects, attributes, values);
		conceptLattice = null;
	}

	@Override
	public void setValueAt(String value, int objIdx, int attIdx) throws InvalidTypeException {
		if (!value.equals(TRUE) && !value.equals(FALSE) && !value.equals(ARROWDOWN) && !value.equals(ARROWUP) && !value.equals(DOUBLEARROW))
			throw new InvalidTypeException(CoreMessages.getString("Core.valueNotTrueNotFalse")); //$NON-NLS-1$
		else {
			if (!getValueAt(objIdx, attIdx).equals(TRUE) && value.equals(TRUE))
				countTRUE++;
			else if (getValueAt(objIdx, attIdx).equals(TRUE) && !value.equals(TRUE))
				countTRUE--;

			/***     Condition pour placer les fleches dans le contexte   ***/
			if (!getValueAt(objIdx, attIdx).equals(TRUE) && value.equals(ARROWDOWN))
				countTRUE++;
			else if (!getValueAt(objIdx, attIdx).equals(TRUE) && value.equals(ARROWUP))
				countTRUE++;
			else if (!getValueAt(objIdx, attIdx).equals(TRUE) && value.equals(DOUBLEARROW))
				countTRUE++;
			else if (getValueAt(objIdx, attIdx).equals(TRUE) && !value.equals(TRUE))
				countTRUE--;

			(values.elementAt(objIdx)).set(attIdx, value);
			isModified = true;
		}
	}


	/**
	 * Calcule le support d'un ensemble par rapport au Contexte courant
	 * @param bs L'ensemble sur lequel on veux calculer le support
	 * @return le support
	 */
	public double support(BasicSet bs) {
		BasicSet out = new BasicSet();
		out.addAll(objects);
		for(String s : bs) {
			out = out.intersection(this.getObjectsFor(s));
		}
		return (double)out.size()/(double)this.getObjectCount();
	}

	/*** Fonction qui permet de placer des fleches dans le contexte ***/
	public void setFleche(String value, int objIdx, int attIdx){

		if (!getValueAt(objIdx, attIdx).equals(TRUE) && value.equals(ARROWDOWN))
			countTRUE++;
		else if (!getValueAt(objIdx, attIdx).equals(TRUE) && value.equals(ARROWUP))
			countTRUE++;
		else if (!getValueAt(objIdx, attIdx).equals(TRUE) && value.equals(DOUBLEARROW))
			countTRUE++;
		else if (getValueAt(objIdx, attIdx).equals(TRUE) && !value.equals(TRUE))
			countTRUE--;

		(values.elementAt(objIdx)).set(attIdx, value);
		isModified = true;
	}


	@Override
	public void setValueAt(String value, String obj, String att) throws InvalidTypeException {
		int objIdx = objects.indexOf(obj);
		int attIdx = attributes.indexOf(att);

		if (objIdx > -1 && attIdx > -1) {
			if (!value.equals(TRUE) && !value.equals(FALSE))
				throw new InvalidTypeException(CoreMessages.getString("Core.valueNotTrueNotFalse")); //$NON-NLS-1$
			else {

				if (!getValueAt(objIdx, attIdx).equals(TRUE) && value.equals(TRUE))
					countTRUE++;
				else if (getValueAt(objIdx, attIdx).equals(TRUE) && !value.equals(TRUE))
					countTRUE--;

				(values.elementAt(objIdx)).set(attIdx, value);
				isModified = true;
			}
		} else
			throw new InvalidTypeException(CoreMessages.getString("Core.invalidContextCell") + " (" + CoreMessages.getString("Core.object") + " :" + obj + ", " + CoreMessages.getString("Core.attribute") + " :" + att + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	}

	public void createLogicalAttribute(LogicalFormula formula, String newName) throws AlreadyExistsException, InvalidTypeException {
		if (formula.getElementCount() < 2)
			return;
		if (getAttributeIndex(newName) >= 0) {
			throw new AlreadyExistsException(CoreMessages.getString("Core.alreadyAttributeNamed") + " \"" + newName + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		addAttribute(newName);
		for (int i = 0; i < objects.size(); i++) {
			String obj = objects.elementAt(i);
			if (formula.accept(getAttributesFor(obj))) {
				setValueAt(TRUE, obj, newName);
				countTRUE++;
			}
		}
	}

	public void sortObjectsInClusters() {
		createClusters(0, objects.size() - 1, 0);
	}

	private void createClusters(int firstObjIdx, int lastObjIdx, int attIdx) {
		if (attIdx >= attributes.size())
			return;
		if (firstObjIdx >= lastObjIdx)
			return;

		int lastTrue = firstObjIdx - 1;
		for (int i = firstObjIdx; i <= lastObjIdx; i++)
			if (getValueAt(i, attIdx).equals(TRUE)) {
				lastTrue++;
				if (lastTrue != i) {
					hasMovedObject(i, lastTrue);
				}
			}

		createClusters(firstObjIdx, lastTrue, attIdx + 1);
		createClusters(lastTrue + 1, lastObjIdx, attIdx + 1);
	}

	public void addBinaryContext(BinaryContext bc, boolean overwrite) throws AlreadyExistsException, InvalidTypeException {
		/* Ajout des nouveaux objest */
		Vector<String> newObjects = bc.getObjects();
		for (int i = 0; i < newObjects.size(); i++) {
			String objName = newObjects.elementAt(i);
			try {
				addObject(objName);
			} catch (AlreadyExistsException e) {
				// Nothing to do there, already tested
				LMLogger.logWarning(e, false);
			}
		}
		isModified = true;

		/* Ajout des nouveaux attributs */
		Vector<String> newAttributes = bc.getAttributes();
		for (int i = 0; i < newAttributes.size(); i++) {
			String attName = newAttributes.elementAt(i);

			if (!overwrite) {
				int tryCount = 0;
				int idx = getAttributeIndex(attName);
				while (idx > -1) {
					tryCount++;
					idx = getAttributeIndex(attName + "_" + tryCount); //$NON-NLS-1$
				}

				if (tryCount > 0) {
					attName = attName + "_" + tryCount; //$NON-NLS-1$
					newAttributes.setElementAt(attName, i);
				}
			}

			addAttribute(attName);
		}

		/* Ajout des valeurs */
		for (int i = 0; i < newObjects.size(); i++) {
			String objName = newObjects.elementAt(i);
			for (int j = 0; j < newAttributes.size(); j++) {
				String attName = newAttributes.elementAt(j);
				String value = bc.getValueAt(i, j);
				if (value.equals(TRUE)) {
					int objIdx = getObjectIndex(objName);
					int attIdx = getAttributeIndex(attName);
					String oldValue = getValueAt(objIdx, attIdx);
					setValueAt(TRUE, objIdx, attIdx);
					if (!oldValue.equals(TRUE) && value.equals(TRUE))
						countTRUE++;
					else if (oldValue.equals(TRUE) && !value.equals(TRUE))
						countTRUE--;
				}
			}
		}
	}

	public NestedContext convertToNestedContext() throws AlreadyExistsException, InvalidTypeException {
		Decomposition dec = new Decomposition(this);
		Vector<BasicSet> classes = dec.getClasses();
		dec = null;

		NestedContext nestedCtx = null;
		for (int i = 0; i < classes.size(); i++) {
			BinaryContext ctx = new BinaryContext(CoreMessages.getString("Core.level") + " " + (i + 1)); //$NON-NLS-1$ //$NON-NLS-2$

			for (int j = 0; j < objects.size(); j++)
				ctx.addObject(getObjectAt(j));

			Iterator<String> attIt = (classes.elementAt(i)).iterator();
			while (attIt.hasNext()) {
				String att = attIt.next();
				ctx.addAttribute(att);
				Iterator<String> objIt = getObjectsFor(att).iterator();
				while (objIt.hasNext()) {
					ctx.setValueAt(TRUE, objIt.next(), att);
				}
			}

			if (nestedCtx == null)
				nestedCtx = new NestedContext(ctx);
			else
				nestedCtx.addNextContext(new NestedContext(ctx));
		}

		return nestedCtx;
	}

	/**
	 * @param cl
	 */
	public void setConceptLattice(ConceptLattice cl) {
		conceptLattice = cl;
	}

	/**
	 * @return le {@link ConceptLattice} du contexte binaire
	 */
	public ConceptLattice getConceptLattice() {
		return conceptLattice;
	}

	public double getDensity() {
		return ((double) countTRUE / (objects.size() * attributes.size()));
	}

	@Override
	public Object clone() {
		BinaryContext newBinCtx = new BinaryContext(contextName);
		for (int i = 0; i < objects.size(); i++) {
			try {
				newBinCtx.addObject(objects.elementAt(i));
			} catch (AlreadyExistsException aee) {
				// Never reach because the original BinaryContext is valid
				LMLogger.logSevere(aee, false);
			}
		}

		for (int i = 0; i < attributes.size(); i++) {
			try {
				newBinCtx.addAttribute(attributes.elementAt(i));
			} catch (AlreadyExistsException aee) {
				// Never reach because the original BinaryContext is valid
				LMLogger.logSevere(aee, false);
			}
		}

		for (int i = 0; i < values.size(); i++) {
			Vector<String> currentValues = values.elementAt(i);
			for (int j = 0; j < currentValues.size(); j++) {
				String val = currentValues.elementAt(j);
				if (val.equals(BinaryContext.TRUE)) {
					try {
						newBinCtx.setValueAt(BinaryContext.TRUE, i, j);
					} catch (InvalidTypeException ite) {
						// Never reach because the original BinaryContext is valid
						LMLogger.logSevere(ite, false);
					}
				}
			}
		}

		return newBinCtx;
	}

	@Override
	public String toString() {
		BasicSet objSet = new BasicSet();
		objSet.addAll(objects);
		BasicSet attSet = new BasicSet();
		attSet.addAll(attributes);

		String str = CoreMessages.getString("Core.contextName") + " : " + contextName + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str = str + CoreMessages.getString("Core.objects") + " : " + objSet.toString() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str = str + CoreMessages.getString("Core.attributes") + " : " + attSet.toString() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str = getBinaryContextAsString(str);

		return str;
	}

	private String getBinaryContextAsString(String str) {
		for (int i = 0; i < objects.size(); i++) {
			for (int j = 0; j < attributes.size(); j++) {
				if (getValueAt(i, j).equals(BinaryContext.TRUE))
					str = str + "X   "; //$NON-NLS-1$

				/*** Modification apporté a la fonction pour placer les fléches dans le contexte***/
				else if (getValueAt(i, j).equals(BinaryContext.ARROWDOWN))
					str = str + "<-  "; //$NON-NLS-1$
				else if (getValueAt(i, j).equals(BinaryContext.ARROWUP))
					str = str + "->  "; //$NON-NLS-1$
				else if (getValueAt(i, j).equals(BinaryContext.DOUBLEARROW))
					str = str + "<-> "; //$NON-NLS-1$
				/*** fin modif ***/

				else
					str = str + "0   "; //$NON-NLS-1$
			}
			str = str + "\n"; //$NON-NLS-1$
		}
		return str;
	}


	/***
	 * Fonction qui permet de placer les fleches basses
	 * aux objets candidats a la reduction
	 * @return contexte dans lequel on a place les fleches
	 ***/
	public BinaryContext setLowArrow() {

		//contexte dans lequel on place les fleches
		BinaryContext bc = new BinaryContext(this);

		for(int i=0; i<bc.getObjectCount(); i++) {

			String g = bc.getObjectAt(i);
			BasicSet g_prime = bc.CalculAttributes((g));

			for(int j=0; j<bc.getAttributeCount(); j++) {
				String m = bc.getAttributeAt(j);
				int test = -1;
				if(!bc.getValueAt(i, j).equals(TRUE)){
					// all right
					for(int k=0; k<bc.getObjectCount(); k++) {
						String h = bc.getObjectAt(k);
						// Calcul du h'
						BasicSet h_prime = this.CalculAttributes(h);
						if(m != h && !h_prime.equals(g_prime) && !bc.getValueAt(k, j).equals(TRUE)) {
							if(!(g_prime.contains(m))) {

								if(!h_prime.contains(m) && h_prime.containsAll(g_prime)) {
									test++;
								}
							}
						}
					}
					if(test < 0){
						bc.setFleche(ARROWDOWN, i, j);
					}
				}
			}
		}
		return bc;
	}


	/***
	 * Fonction qui permet de placer des fleches hautes
	 * aux attributs candidats a la reduction
	 * @return contexte dans lequel on a place les fleches
	 ***/
	public BinaryContext setHighArrow() {
		BinaryContext bc = new BinaryContext(this);
		for(int i=0; i<bc.getAttributeCount(); i++) {

			String m = getAttributeAt(i);
			//Calcul du m'
			BasicSet m_prime = this.CalculObjets((m));

			for(int j=0; j<bc.getObjectCount(); j++) {
				int test = -1;
				if(!getValueAt(j, i).equals(TRUE)){

					for(int k=0; k<bc.getAttributeCount(); k++) {
						String n = bc.getAttributeAt(k);
						// Calcul du n'
						BasicSet n_prime = this.CalculObjets(n);
						if(m != n && !n_prime.equals(m_prime) && !bc.getValueAt(j, k).equals(TRUE)) {

							if(!(m_prime.contains(m))) {
								if(!n_prime.contains(m) && n_prime.containsAll(m_prime)) {
									test++;
								}
							}
						}
					}
					if(test < 0){
						bc.setFleche( ARROWUP, j, i);
					}
				}
			}
		}
		return bc;
	}


	/***
	 * Fonction qui permet de placer tous les fleches
	 *  dans le contexte.
	 * @return contexte dans lequel on a place les fleches
	 ***/
	public BinaryContext setArrow() {

		//Contexte avec des fleches basses
		BinaryContext bc1 = this.setLowArrow();
		//Contexte avec des fleches hautes
		BinaryContext bc2 = this.setHighArrow();

		int objIndex = bc1.getObjectCount();
		int attIndex = bc1.getAttributeCount();

		//bc contexte dans lequel on a place les fleches
		BinaryContext bc = new BinaryContext(this);

		for(int i=0; i<bc1.getObjectCount(); i++) {
			for(int j=0; j<bc1.getAttributeCount(); j++) {
				String value1 = bc1.getValueAt(i, j);
				String value2 = bc2.getValueAt(i, j);
				if(bc1.getValueAt(i, j).equals(ARROWDOWN) && bc2.getValueAt(i, j).equals(ARROWUP)
						|| bc1.getValueAt(i, j).equals(ARROWUP) && bc2.getValueAt(i, j).equals(ARROWDOWN)) {
					bc.setFleche(DOUBLEARROW, i, j);
				}
				else if(bc1.getValueAt(i, j).equals(FALSE) || bc2.getValueAt(i, j).equals(FALSE)) {
					if(bc2.getValueAt(i, j).equals(ARROWUP)) {
						try {
							bc.setValueAt(ARROWUP, i, j);
						} catch (InvalidTypeException e) {
							e.printStackTrace();
						}
					}
					else if(bc2.getValueAt(i, j).equals(ARROWDOWN)) {
						try {
							bc.setValueAt(ARROWDOWN, i, j);
						} catch (InvalidTypeException e) {
							e.printStackTrace();
						}
					}
					else if(bc1.getValueAt(i, j).equals(ARROWUP)) {
						try {
							bc.setValueAt(ARROWUP, i, j);
						} catch (InvalidTypeException e) {
							e.printStackTrace();
						}
					}
					else if(bc1.getValueAt(i, j).equals(ARROWDOWN)) {
						try {
							bc.setValueAt(ARROWDOWN, i, j);
						} catch (InvalidTypeException e) {
							e.printStackTrace();
						}
					}
				}
				else
				{
					try {
						bc.setValueAt(value1, i, j);
					} catch (InvalidTypeException e) {
					}
					try {
						bc.setValueAt(value2, i, j);
					} catch (InvalidTypeException e) {
					}
				}
			}
		}

		return bc;
	}


	/***
	 * Fonction qui permet de cacher le context avec arrow relation
	 *
	 * @return contexte initiale sans affichage des fleches
	 ***/
	public BinaryContext hideArrowContext() {
		BinaryContext bc = new BinaryContext(this.getName(), this.getObjectCount(), this.getAttributeCount());
		for(int i=0; i<bc.getObjectCount(); i++){
			for(int j=0; j<bc.getAttributeCount(); j++){
				//System.out.println(bcArrow.getValueAt(i, j));
				if(this.getValueAt(i, j).equals(TRUE)){
					try {
						bc.setValueAt(TRUE, i, j);
					} catch (InvalidTypeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					try {
						bc.setValueAt(FALSE, i, j);
					} catch (InvalidTypeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return bc;
	}

	/***
	 * Fonction qui permet de de reduire le contexte.
	 * @return contexte reduit.
	 ***/
	public BinaryContext reduceContext() {

		//reduire les attributs
		this.reduceAttribut();
		//reduire les objets
		this.reduceObject();
		//clarifier le contexte
		//this.clarifyContext();

		return this;
	}


	/***
	 * Calcul des attributs d'un objet donne
	 * @param o objet dont on veut calculer les attibuts
	 * @return liste des attributs
	 */
	public BasicSet CalculAttributes(String o) {
		BasicSet attr=this.getAttributesFor(o);
		return attr;
	}

	/***
	 * Calcul des objets d'un attribut donne
	 * @param a attribut dont on veut calculer les objets
	 * @return liste des objets
	 */
	public BasicSet CalculObjets(String a) {
		BasicSet obj=this.getObjectsFor(a);
		//System.out.println(obj);
		return obj;
	}

	/***
	 * Calcul des objets d'un attribut donne
	 * @param a attribut dont on veut calculer les objets
	 * @return liste des objets
	 ***/
	public BasicSet CalculObjetsArow(String a) {
		BasicSet obj=this.getObjectsArrow(a);
		//System.out.println(obj);
		return obj;
	}

	/***
	 * Calcul des objets d'un attribut donne
	 * @param o attribut dont on veut calculer les objets
	 * @return liste des objets
	 ***/
	public BasicSet CalculAttributesArrow(String o) {
		BasicSet attr=this.getAttributesArrow(o);
		//System.out.println(attr);
		return attr;
	}

	/***
	 * Fonction qui permet de de reduire les objets.
	 * les objets a reduire par des fleches.
	 ***/
	public BinaryContext reduceObject() {
		this.clarifyObject();

		BinaryContext contextArrow = this.setArrow();
		Vector<String> tab = new Vector<String>(); //vecteur pour stocker les objets a supprimer

		for(int i=0; i<this.getObjectCount(); i++) {
			boolean arrowdown=false;
			boolean un = true;

			String obj = this.getObjectAt(i);
			for(int j=0; j<this.getAttributeCount(); j++) {
				if(!contextArrow.getValueAt(i, j).equals(TRUE) ){
					un = false;
				}

				if(!contextArrow.getValueAt(i, j).equals(TRUE) || !contextArrow.getValueAt(i, j).equals(FALSE) || !contextArrow.getValueAt(i, j).equals(ARROWUP)) {
					if((contextArrow.getValueAt(i, j).equals(ARROWDOWN))) {
						arrowdown=true;
					}
					else if(contextArrow.getValueAt(i, j).equals(DOUBLEARROW)) {
						arrowdown=true;
					}
				}
			}
			if(!arrowdown && !un){
				tab.add(obj);
			}
			else
			{
				//System.out.println("Impossible to reduce object "+obj);
			}
		}
		//System.out.println(tab);
		for(int j=0; j<tab.size(); j++){
			if(contextArrow.getObjects().contains(tab.elementAt(j))){
				this.removeObject(tab.elementAt(j));
			}
		}
		return this;
	}

	/***
	 * Fonction qui permet de de reduire les attributs.
	 * les attributs a reduire par des fleches.
	 ***/
	public BinaryContext reduceAttribut() {
		this.clarifyAttribute();

		BinaryContext contextArrow = this.setArrow();
		Vector<String> tab = new Vector<String>(); //vecteur pour stocker les attributs a supprimer

		for(int i=0; i<this.getAttributeCount(); i++) {
			boolean arrowUp = false;
			boolean un = true;
			String att = this.getAttributeAt(i);

			for(int j=0; j<this.getObjectCount(); j++) {
				if(!contextArrow.getValueAt(j, i).equals(TRUE) ){
					un = false;
				}

				if(!contextArrow.getValueAt(j, i).equals(TRUE) || !contextArrow.getValueAt(j, i).equals(FALSE) || !contextArrow.getValueAt(j, i).equals(ARROWDOWN)) {
					if((contextArrow.getValueAt(j, i).equals(ARROWUP))) {
						arrowUp = true;
					}
					else if(contextArrow.getValueAt(j, i).equals(DOUBLEARROW)) {
						arrowUp = true;
					}
				}
			}
			if(!arrowUp && !un) {
				//supprimer l'attribut
				tab.add(att);
			}
			else {
			}
		}

		for(int j=0; j<tab.size(); j++){
			if(contextArrow.getAttributes().contains(tab.elementAt(j))) {
				this.removeAttribute(tab.elementAt(j));
			}
		}

		return this;
	}

	/*** Clarification du contexte ***/

	/***
	 * Fonction qui permet de clarifier les objets.
	 * @return le context binaire avec clarification des objets
	 ***/
	public BinaryContext clarifyObject () {
		for (int i =this.getObjectCount()-1; i>=0; i--) {
			String g = this.getObjectAt(i);
			BasicSet g_prime = this.CalculAttributes(g);
			//Integer value = victor.get(i);

			for(int j = i-1; j>=0 ;j--) {
				String h = this.getObjectAt(j);
				BasicSet h_prime = this.CalculAttributes(h);
				if(g_prime.equals(h_prime)){
					//System.out.println("Objet a supprimer: "+h);
					//tab.add(h);
					this.removeObject(h);
					i--;
				}
			}

		}


		/*int objSize = this.getObjectCount();

		for(int i=0; i<objSize; i++) {
			String g = this.getObjectAt(i);
			BasicSet g_prime = this.CalculAttributes(g);

			for(int j=0; j<objSize; j++) {
				if(i != j){
					String h = this.getObjectAt(j);
					BasicSet h_prime = this.CalculAttributes(h);

					if(g_prime.equals(h_prime)){
						//System.out.println("Objet a supprimer: "+h);
						//tab.add(h);
						this.removeObject(h);
						objSize--;
					}
				}
			}
		}*/

		return this;//bc;
	}

	/***
	 * Fonction qui permet de clarifier les attributs.
	 * @return le context binaire avec clarification des attributs
	 ***/
	public BinaryContext clarifyAttribute () {
		for (int i = this.getAttributeCount()-1; i>=0; i--) {
			String g = this.getAttributeAt(i);
			BasicSet g_prime = this.CalculObjets(g);

			for(int j = i-1; j>=0 ;j--) {
				String h = this.getAttributeAt(j);
				BasicSet h_prime = this.CalculObjets(h);
				if(g_prime.equals(h_prime)){
					//System.out.println("Objet a supprimer: "+h);
					//tab.add(h);
					this.removeAttribute(h);
					i--;
				}
			}

		}




		/*int attSize = this.getAttributeCount();

		for(int i=0; i<attSize; i++){
			String g = this.getAttributeAt(i);
			//System.out.println("Pour g -> "+g);
			BasicSet g_prime = this.CalculObjets(g);
			//System.out.println("g_prime = "+g_prime);

			for(int j=0; j<attSize; j++){
				if(i != j){
					String h = this.getAttributeAt(j);
					BasicSet h_prime = this.CalculObjets(h);

					if(g_prime.equals(h_prime)){
						//System.out.println("attribut a supprimer: "+h);
						//tab.add(h);
						this.removeAttribute(h);
						attSize--;
					}
				}
			}
		}*/

		return this;
	}

	/***
	 * Fonction qui permet de clarifier les attributs.
	 * @return le context binaire avec clarification des attributs
	 ***/
	public BinaryContext clarifyContext () {
		this.clarifyAttribute();
		this.clarifyObject();
		return this;
	}


	/*** Contexte complementaire  ***/

	/***
	 * Fonction qui permet de faire l'apposition entre le contexte
	 * courant et son complementaire.
	 * @return le context binaire avec son complement
	 ***/
	public BinaryContext addComplementaryContext() {
		int attSize = this.getAttributeCount();
		int objSize = this.getObjectCount();

		int newAttSize = attSize;

		for (int i=0; i<attSize; i++) {
			this.addAttribute();
			try {
				this.setAttributeAt(getAttributeAt(i)+"'", newAttSize);
			} catch (AlreadyExistsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for (int j=0; j<objSize; j++) {
				if(this.getValueAt(j, i).equals(TRUE)) {
					try {
						this.setValueAt("",j, newAttSize);
					} catch (InvalidTypeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					try {
						this.setValueAt("true",j, newAttSize);
					} catch (InvalidTypeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//System.out.println(newAttSize);
			newAttSize++;
		}
		return this;
	}

	/***
	 * Fonction qui permet de recuperer le contexte complementaire
	 * @return le context complementaire
	 ***/
	public BinaryContext complementaryContext() {
		int attSize = this.getAttributeCount();

		BinaryContext complem = this.addComplementaryContext();
		Vector<String> tab = new Vector<String>();

		for(int i=0; i<attSize; i++){
			String att = this.getAttributeAt(i);
			tab.add(att);
		}
		//System.out.println("attribut : "+tab);

		for(int i=0; i<this.getAttributeCount(); i++){
			if(complem.getAttributes().contains(tab.elementAt(i))){
				complem.removeAttribute(tab.elementAt(i));
			}
		}
		return complem;
	}

	public BasicSet calculobj(String att){
		BinaryContext arrowContext = this.setArrow();
		BasicSet bs = arrowContext.CalculObjetsArow(att);
		//BasicSet bs = this.CalculObjetsArow(att);
		return bs;
	}

	public BasicSet calculatt(String obj){
		BinaryContext arrowContext = this.setArrow();
		BasicSet bs = arrowContext.CalculAttributesArrow(obj);
		//BasicSet bs = this.CalculAttributesArrow(obj);
		return bs;
	}

	public BinaryContext transitives() {
		BasicSet bs = null;
		//String name = bc.getName();
		BinaryContext bc3 = (BinaryContext) this.clone();

		for(int m=0; m<this.getObjectCount(); m++){
			for(int n=0; n<this.getAttributeCount(); n++){
				try {
					this.setValueAt("true", m, n);
				} catch (InvalidTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		for(int i=0; i<this.getObjectCount(); i++){
			bs = bc3.transitiveClosure(bc3.getObjectAt(i));
			//System.out.println("    La fermeture transitive de "+bc3.getObjectAt(i)+" est: "+bs);

			for(String att: bs){
				try {
					this.setValueAt("", this.getObjectAt(i), att);
				} catch (InvalidTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//bc.setName(name+"_Transitive");
		return this;
	}

	public BinaryContext transitivesAtt() {
		BasicSet bs = null;
		//String name = bc.getName();
		BinaryContext bc3 = (BinaryContext) this.clone();

		for(int m=0; m<this.getObjectCount(); m++){
			for(int n=0; n<this.getAttributeCount(); n++){
				try {
					this.setValueAt("true", m, n);
				} catch (InvalidTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		for(int i=0; i<this.getAttributeCount(); i++){
			bs = bc3.transitiveClosureAtt(bc3.getAttributeAt(i));
			//System.out.println("    La fermeture transitive de "+bc3.getAttributeAt(i)+" est: "+bs);

			for(String obj: bs){
				try {
					this.setValueAt("", obj, this.getAttributeAt(i));
				} catch (InvalidTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		//bc.setName(name+"_Transitive");
		return this;
	}

	/***
	 * Fonction qui permet de calculer la fermeture transitive.
	 * @param obj dont on veut calculer la fermeture transitve
	 * @return
	 ***/
	public BasicSet transitiveClosure(String obj) {

		BinaryContext arrowContext = this.setArrow();
		BasicSet tab1 = new BasicSet();
		BasicSet tab2 = new BasicSet();
		BasicSet tab4 = new BasicSet();
		BasicSet tab5 = new BasicSet();
		BasicSet tab6 = new BasicSet();

		BasicSet g_prime = arrowContext.CalculAttributesArrow(obj);

		tab1.addAll(g_prime);
		tab4.addAll(g_prime);

		do{
			tab6 = tab6.intersection(tab1);
			tab1.removeAll(tab1);

			tab1.addAll(tab4);
			for(String gprime: tab1){

				BasicSet m_prime = arrowContext.CalculObjetsArow(gprime);
				//System.out.println(gprime+"'"+" = "+m_prime);
				tab2.addAll(m_prime);
				if(tab2.contains(obj)){
					tab2.remove(obj);
					tab5.add(gprime);
				}
				tab4.remove(gprime);
			}
			BasicSet tab3 = new BasicSet();
			for(String hprime: tab2){
				BasicSet h_prime = arrowContext.CalculAttributesArrow(hprime);
				for(String h: h_prime){
					tab3.addAll(h_prime);
					if(tab1.contains(h)){
						tab3.remove(h);
						tab6.remove(h);
					}
					else
					{
						tab5.add(h);
						tab6.add(h);
					}
				}
			}

			tab1.addAll(tab5);
			tab4.addAll(tab5);

		}while(tab6.size()!= 0 );

		return tab4;
	}

	/***
	 * Fonction qui permet de calculer la fermeture transitive.
	 * @param att dont on veut calculer la fermeture transitve
	 * @return
	 ***/
	public BasicSet transitiveClosureAtt(String att) {

		BinaryContext arrowContext = this.setArrow();
		BasicSet tab1 = new BasicSet();
		BasicSet tab2 = new BasicSet();
		BasicSet tab4 = new BasicSet();
		BasicSet tab5 = new BasicSet();
		BasicSet tab6 = new BasicSet();

		BasicSet g_prime = arrowContext.CalculObjetsArow(att);

		tab1.addAll(g_prime);
		tab4.addAll(g_prime);

		do{
			tab6 = tab6.intersection(tab1);
			tab1.removeAll(tab1);

			tab1.addAll(tab4);
			for(String gprime: tab1){

				BasicSet m_prime = arrowContext.CalculAttributesArrow(gprime);
				//System.out.println(gprime+"'"+" = "+m_prime);
				tab2.addAll(m_prime);
				if(tab2.contains(att)){
					tab2.remove(att);
					tab5.add(gprime);
				}
				tab4.remove(gprime);
			}
			BasicSet tab3 = new BasicSet();
			for(String hprime: tab2){
				BasicSet h_prime = arrowContext.CalculObjetsArow(hprime);
				for(String h: h_prime){
					tab3.addAll(h_prime);
					if(tab1.contains(h)){
						tab3.remove(h);
						tab6.remove(h);
					}
					else
					{
						tab5.add(h);
						tab6.add(h);
					}
				}
			}

			tab1.addAll(tab5);
			tab4.addAll(tab5);

		}while(tab6.size()!= 0 );

		return tab4;
	}

	/**
	 * createTaxonomyAttribute
	 * @author Linda Bogni
	 * @param formula
	 * @param nameAtt
	 * @throws AlreadyExistsException
	 * @throws InvalidTypeException
	 */
	public void createTaxonomyAttribute(LogicalFormula formula, String nameAtt) throws AlreadyExistsException, InvalidTypeException {
		if (formula.getElementCount() < 1){//-->LINDA
			return;
		}
		this.setTypeTax("attributes");
		String newNameAtt = DialogBox.showInputQuestion(panel,GUIMessages.getString("GUI.enterNewAttributeName"),
				GUIMessages.getString("GUI.GeneralisationAttributes"));
		if(newNameAtt==null) return;
		if(formula.getFormula().contains("PERCENTAGE")){
			percentageChoice();
			if(percentage==0) return;
			formula.setPercentage(percentage);
			this.getDonneesTaxonomieAtt().add(newNameAtt.toUpperCase()+" : "+formula.getFormula()+" : "+String.valueOf(formula.getPercentage()));//Sauver les donnees pr la taxonomie
		}else{
			this.getDonneesTaxonomieAtt().add(newNameAtt.toUpperCase()+" : "+formula.getFormula());
		}
		if (getAttributeIndex(newNameAtt.toUpperCase()) >= 0) {
			throw new AlreadyExistsException(CoreMessages.getString("Core.alreadyAttributeNamed") + " \"" + newNameAtt + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}else{
			addAttribute(newNameAtt.toUpperCase());
			if (isModified){
				for (int i = 0; i < objects.size(); i++) {
					String obj = objects.elementAt(i);
					if (formula.accept(getAttributesFor(obj))) {
						setValueAt(TRUE, obj, newNameAtt.toUpperCase());
						countTRUE++;
					}
				}
			}
		}
	}

	/**
	 * createTaxonomyObject
	 * @author Linda Bogni
	 * @param formula
	 * @param nameObj
	 * @throws AlreadyExistsException
	 * @throws InvalidTypeException
	 */
	public void createTaxonomyObject(LogicalFormula formula, String nameObj) throws AlreadyExistsException, InvalidTypeException {
		if (formula.getElementCount() < 1){//-->LINDA
			return;
		}
		this.setTypeTax("objects");
		String newNameObj = DialogBox.showInputQuestion(panel,GUIMessages.getString("GUI.enterNewObjectName"),
				GUIMessages.getString("GUI.GeneralisationObjects"));
		if(newNameObj==null) return;
		if(formula.getFormula().contains("PERCENTAGE")){
			percentageChoice();
			if(percentage==0) return;
			formula.setPercentage(percentage);
			this.getDonneesTaxonomieObj().add(newNameObj.toUpperCase()+" : "+formula.getFormula()+" : "+String.valueOf(formula.getPercentage()));//Sauver les donnees pr la taxonomie
		}else{
			this.getDonneesTaxonomieObj().add(newNameObj.toUpperCase()+" : "+formula.getFormula());
		}
		if (getObjectIndex(newNameObj.toUpperCase()) >= 0) {
			throw new AlreadyExistsException(CoreMessages.getString("Core.alreadyObjectNamed") + " \"" + newNameObj + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}else{
			addObject(newNameObj.toUpperCase());
			if (isModified){
				for (int i = 0; i < attributes.size(); i++) {
					String att = attributes.elementAt(i);
					if (formula.accept(getObjectsFor(att))) {
						setValueAt(TRUE, newNameObj.toUpperCase(), att);
						countTRUE++;
					}
				}
			}
		}

	}
}
