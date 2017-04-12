package fca.core.context;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import fca.core.util.BasicSet;
import fca.exception.AlreadyExistsException;
import fca.exception.InvalidTypeException;
import fca.gui.util.DialogBox;
import fca.messages.CoreMessages;
import fca.messages.GUIMessages;

/**
 * Classe abstraite representant un contexte general
 * @author Genevieve Roberge
 * @author Ludovic Thomas
 * @author Yoann Montouchet
 * @author Aicha Bennis, Linda Bogni, Maya Safwat, Babacar SY
 * @author Nicolas Convers, Arnaud Renaud-Goud
 * @version 1.5
 */
public abstract class Context {
	/** Nom du contexte */
	protected String contextName;

	/** Objets du contexte */
	protected Vector<String> objects;

	/** Attributs du contexte */
	protected Vector<String> attributes;

	/** Valeurs du contexte */
	protected Vector<Vector<String>> values;

	/**
	 * Fichier associe au contexte. <code>null</code> si pas encore enregistre.
	 */
	protected File contextFile;

	/**
	 * Specifie si le contexte a ete modifie ou non, en vue d'etre sauvegarde.
	 */
	protected boolean isModified;

	/**
	 * Valeurs taxonomie du contexte(attributs).
	 */
	// Contient les donnees de construction de la taxomie du contexte.
	protected Vector<String> donneesTaxonomieAtt;

	/**
	 * Valeurs taxonomie du contexte(objetss).
	 */
	// Contient les donnees de construction de la taxomie du contexte.
	protected Vector<String> donneesTaxonomieObj;

	/** Pourcentage de la taxonomie. */
	protected double percentage;
	
	/**
	 * Type de la taxonomie (sur les attributs ou sur les objets).
	 */
	protected String typeTax="rien";

	/** Structure de la taxonomie utilisee. */
	protected Vector<DefaultMutableTreeNode> taxStruct;

	// TODO Clarifier commentaire.
	/**
	 * Type de la generalisation (AND, ET , POURCENTAGE
	 */
	protected String typeGen="";

	/**
	 * Type enumere des headers de context pour la sauvegarde.
	 */
	// FIXME Rajouter des Header
	public static enum HeaderType {
		/** L'entete du contexte binaire pour Lattice Miner */
		LM_BINARY("LM_BINARY_CONTEXT"), //$NON-NLS-1$
	
		/** L'entete du contexte imbrique pour Lattice Miner */
		LM_NESTED("LM_NESTED_CONTEXT"), //$NON-NLS-1$
	
		/** L'entete du contexte value pour Lattice Miner */
		LM_VALUED("LM_VALUED_CONTEXT"), //$NON-NLS-1$
	
		/** L'entete du contexte binaire pour Galicia (Format SLF) */
		SLF_BINARY("[Lattice]"), //$NON-NLS-1$
		
		/** L'entete du contexte binaire pour Microsoft Excel (Format XLS) */
		XLS_BINARY("XLS_BINARY_CONTEXT"); //$NON-NLS-1$
	
		private String value;
	
		/**
		 * Constructeur prive du type enum {@link HeaderType}.
		 * @param value la valeur de l'entete
		 */
		HeaderType(String value) {
			this.value = value;
		}

		/**
		 * @return l'entete correspondante
		 */
		public String getValue() {
			return value;
		}
	
		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return value;
		}
	}
	
	public JPanel panel;
	
	
	/**
	 * Constructeur d'un contexte.
	 * @param name le nom du contexte
	 */
	public Context(String name) {
		if (name != null && name.length() > 0)
			contextName = name;
		else
			contextName = CoreMessages.getString("Core.untitled"); //$NON-NLS-1$
	
		objects = new Vector<String>();
		attributes = new Vector<String>();
		values = new Vector<Vector<String>>();
		contextFile = null;
		isModified = true;
		donneesTaxonomieAtt = new Vector<String>();
		donneesTaxonomieObj = new Vector<String>();
		setTypeTax( typeTax);
		taxStruct=new Vector<DefaultMutableTreeNode>(); // Permet de garder la structure de la taxonomie utilisee.
	}

	/**
	 * Constructeur d'un contexte.
	 * @param name le nom du contexte
	 * @param objNb le nombre d'objet
	 * @param attNb le nombre d'attribut
	 */
	public Context(String name, int objNb, int attNb) {
		if (name != null && name.length() > 0)
			contextName = name;
		else
			contextName = CoreMessages.getString("Core.untitled"); //$NON-NLS-1$
	
		objects = new Vector<String>();
		for (int i = 0; i < objNb; i++)
			objects.add(Integer.toString(i+1));
	
		attributes = new Vector<String>();
		for (int i = 0; i < attNb; i++) {
			String attName = ""; //$NON-NLS-1$
			String attLetter = String.valueOf((char) ('a' + (i % 26)));
	
			int loopCount = (int) Math.floor(i / 26.0);
			for (int j = 0; j <= loopCount; j++)
				attName = attName + attLetter;
	
			attributes.add(attName);
		}
	
		values = new Vector<Vector<String>>();
		for (int i = 0; i < objNb; i++) {
			Vector<String> currValues = new Vector<String>();
			for (int j = 0; j < attNb; j++)
				currValues.add(new String("")); //$NON-NLS-1$
			values.add(currValues);
		}

		isModified = true;
		donneesTaxonomieAtt = new Vector<String>();
		donneesTaxonomieObj = new Vector<String>();
	}

	public Context(String name, Vector<String> objects, Vector<String> attributes, Vector<Vector<String>> values) {
		this.contextName = name;
		this.objects = objects;
		this.attributes = attributes;
		this.values = values;
		this.isModified = true;
		this.donneesTaxonomieAtt = new Vector<String>();
		this.donneesTaxonomieObj = new Vector<String>();
	}
	
	/**
	 * Constructeur d'un contexte par recopie
	 * @param source Contexte existant
	 */
	@SuppressWarnings("unchecked")
	public Context(Context source) {
		this.contextName = source.contextName;
		this.contextFile = source.contextFile;
		this.isModified = source.isModified;
		this.percentage = source.percentage;
		this.typeTax = source.typeTax;
		this.typeGen = source.typeGen;
		this.panel = source.panel;
		this.objects = (Vector<String>) source.objects.clone();
		this.attributes = (Vector<String>) source.attributes.clone();
		this.donneesTaxonomieAtt = (Vector<String>) source.donneesTaxonomieAtt.clone();
		this.donneesTaxonomieObj = (Vector<String>) source.donneesTaxonomieObj.clone();
		this.taxStruct = (Vector<DefaultMutableTreeNode>) source.taxStruct.clone();

		this.values = new Vector<Vector<String>>();
		for(Vector<String> temp : source.values) {
			this.values.add((Vector<String>)temp.clone());
		}
	}

	public String getTypeGen() {
		return typeGen;
	}

	public void setTypeGen(String typeGen) {
		this.typeGen = typeGen;
	}

	public String getTypeTax() {
		return typeTax;
	}

	public void setTypeTax(String typeTax) {
		this.typeTax = typeTax;
	}

	public Vector<DefaultMutableTreeNode> getTaxStruct() {
		return taxStruct;
	}

	public void setTaxStruct(Vector<DefaultMutableTreeNode> taxS) {
		for(int i=0;i<taxS.size();i++){
			this.taxStruct.add(taxS.elementAt(i));
		}
	}

	/**
	 * Recuperation du nom du contexte
	 * @return le nom du contexte
	 */
	public String getName() {
		return contextName;
	}

	/**
	 * Enregistrement du nom du contexte
	 * @param n le nouveau nom
	 */
	public void setName(String n) {
		contextName = n;
		isModified = true;
	}

	/**
	 * Retourne la valeur d'un objet pour un attribut particulier dans
	 * le contexte donne.
	 * @param objIdx Index de l'objet.
	 * @param attIdx Index de l'attribut.
	 * @return Valeur correspondante.
	 */
	public String getValueAt(int objIdx, int attIdx) {
		return (values.elementAt(objIdx)).elementAt(attIdx);
	}

	public boolean isModified() {
		return isModified;
	}

	public void setModified(boolean m) {
		isModified = m;
	}

	/**
	 * @return le fichier associe au context. <code>null</code> s'il n'y en a pas
	 */
	public File getContextFile() {
		return contextFile;
	}

	/**
	 * Fixe le fichier associe au contexte.
	 * @param fileName Nom du fichier.
	 */
	public void setContextFile(File fileName) {
		contextFile = fileName;
	}

	/**
	 * Recuperation des objets du contexte.
	 * @return Liste des objets du contexte.
	 */
	public Vector<String> getObjects() {
		return objects;
	}

	/**
	 * Recuperation du nombre d'objet
	 * @return le nombre d'objet
	 */
	public int getObjectCount() {
		return objects.size();
	}

	
	/**
	 * Ajout d'un objet
	 */
	public void addObject() {
		int objNb = 1;
		String obj = CoreMessages.getString("Core.new_") + objNb; //$NON-NLS-1$
	
		while (objects.contains(obj)) {
			objNb++;
			obj = CoreMessages.getString("Core.new_") + objNb; //$NON-NLS-1$
		}
	
		objects.add(obj);
		Vector<String> currValues = new Vector<String>();
		for (int i = 0; i < attributes.size(); i++)
			currValues.add(new String("")); //$NON-NLS-1$
		values.add(currValues);
	
		isModified = true;
	}

	/**
	 * Ajout d'un objet.
	 * @param obj L'objet qu'on ajoute.
	 * @throws AlreadyExistsException
	 */
	public void addObject(String obj) throws AlreadyExistsException {
		if (!objects.contains(obj)) {
			objects.add(obj);
			Vector<String> currValues = new Vector<String>();
			for (int i = 0; i < attributes.size(); i++)
				currValues.add(new String("")); //$NON-NLS-1$
			values.add(currValues);
			isModified = true;
		} else
			throw new AlreadyExistsException(CoreMessages.getString("Core.object") + " " + obj +
					CoreMessages.getString("Core.alreadyPresent")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Ajout des copies d'un objet.
	 * @param origName Le nom de l'objet a copier.
	 * @param names La base des objets dass laquelle il y a les copies.
	 * @throws AlreadyExistsException
	 */
	public void addObjectCopies(String origName, Vector<String> names) throws AlreadyExistsException {
		int objIdx = objects.indexOf(origName);
	
		if (objIdx >= 0) {
			for (int i = 0; i < names.size(); i++) {
				String name = names.elementAt(i);
				if (!objects.contains(name)) {
					objects.add(name);
					Vector<String> currValues = new Vector<String>();
					currValues.addAll(values.elementAt(objIdx));
					values.add(currValues);
					isModified = true;
				} else
					throw new AlreadyExistsException(CoreMessages.getString("Core.object") + " " + name + CoreMessages.getString("Core.alreadyPresent")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
	}

	/**
	 * Suppression d'un objet
	 * @param obj l'objet a supprimer
	 */
	public void removeObject(String obj) {
		int objIdx = objects.indexOf(obj);
	
		if (objIdx >= 0) {
			objects.removeElementAt(objIdx);
			values.removeElementAt(objIdx);
			isModified = true;
		}
	}

	/**
	 * Verifie si l'objet a ete deplace
	 * @param startIdx indice de depart
	 * @param endIdx indice d'arrivee
	 * @return true si l'objet a ete deplace, false sinon
	 */
	public boolean hasMovedObject(int startIdx, int endIdx) {
		if (startIdx >= objects.size() || startIdx < 0 || endIdx >= objects.size() || endIdx < 0 || startIdx == endIdx)
			return false;
	
		Vector<String> tempValues = values.elementAt(startIdx);
		String objectName = objects.elementAt(startIdx);
		objects.remove(startIdx);
		values.remove(startIdx);
		objects.insertElementAt(objectName, endIdx);
		values.insertElementAt(tempValues, endIdx);
		return true;
	}
	
	/**
	 * Verifie si l'attribut a ete deplace
	 * @param startIdx indice de depart
	 * @param endIdx indice d'arrivee
	 * @return true si l'objet a ete deplace, false sinon
	 */
	public boolean hasMovedAttributes(int startIdx, int endIdx) {
		if (startIdx >= attributes.size() || startIdx < 0 || endIdx >= attributes.size() || endIdx < 0 || startIdx == endIdx)
			return false;
	
		/*Vector<String> tempValues = values.elementAt(startIdx);
		String objectName = attributes.elementAt(startIdx);
		attributes.remove(startIdx);
		values.remove(startIdx);
		attributes.insertElementAt(objectName, endIdx);
		values.insertElementAt(tempValues, endIdx);*/
		return true;
	}
	
	public String getObjectAt(int idx) {
		return objects.elementAt(idx);
	}

	/**
	 * Enregistrement d'un objet
	 * @param obj l'objet a enregistrer
	 * @param idx l'indice ou enregistrer l'objet
	 * @throws AlreadyExistsException
	 */
	public void setObjectAt(String obj, int idx) throws AlreadyExistsException {
		if (!objects.contains(obj)) {
			objects.set(idx, obj);
			isModified = true;
		} else
			throw new AlreadyExistsException(CoreMessages.getString("Core.alreadyObjectNamed") + " \"" + obj + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public int getObjectIndex(String obj) {
		return objects.indexOf(obj);
	}

	public BasicSet getObjectsFor(String att) {
		BasicSet objSet = new BasicSet();
	
		int attIdx = attributes.indexOf(att);
		if (attIdx > -1) {
			for (int i = 0; i < values.size(); i++) {
				Vector<String> objValues = values.elementAt(i);
				if (!(objValues.elementAt(attIdx)).equals("")) //$NON-NLS-1$
					objSet.add(objects.elementAt(i));
			}
		}
		return objSet;
	}

	/**
	 * Fonction pour calculer le prime des objets avec relation de fleches.
	 * @param att
	 * @return
	 **/
	public BasicSet getObjectsArrow(String att) {
		BasicSet objSet = new BasicSet();
	
		int attIdx = attributes.indexOf(att);
		if (attIdx > -1) {
			for (int i = 0; i < values.size(); i++) {
				Vector<String> objValues = values.elementAt(i);
				if ((objValues.elementAt(attIdx)).equals("arrowdown") || (objValues.elementAt(attIdx)).equals("doblearrow")) //$NON-NLS-1$
					objSet.add(objects.elementAt(i));
			}
		}
		return objSet;
	}

	/**
	 * Determine si les contextes ont les memes objets (nombre, type).
	 * @param comp Contexte a comparer.
	 * @return Vrai si les objets sont les memes, faux sinon.
	 */
	public boolean compareObjects(Context comp) {
		boolean trouve = false;
		
		Vector<String> vcomp = new Vector<String>();
		
		vcomp = (Vector<String>) comp.getObjects();
		
		if(vcomp.size() != this.objects.size()) {
			return false;
		}
		
		for(int i=0; i< this.objects.size(); i++) {
			trouve = false;
			
			for(int j=0; j<vcomp.size(); j++) {
				
				// Si les deux chaines correspondent, on met la variable a true et on interrompt la boucle.
				if(vcomp.elementAt(j).equals(objects.elementAt(i))) {
					trouve = true;
					vcomp.remove(j);
					break;
				}
			}
			
			// La chaine n'existe pas dans vcomp.
			if (trouve == false) {
				return trouve;
			}
		}
		
		return true;
	}
	
	/**
	 * Assemble deux contextes ayant les memes objets.
	 * @param context Contexte a assembler, <code>null</code> si les objets sont differents.
	 * @return Nouveau contexte assemble.
	 * @throws InvalidTypeException
	 * @throws CloneNotSupportedException
	 * @throws AlreadyExistsException
	 * @author Linda Bongi
	 * @author Nicolas Convers, Arnaud Renaud-Goud
	 */
	public Context apposition (Context context) throws InvalidTypeException, CloneNotSupportedException, AlreadyExistsException {
		if(!this.compareObjects(context)) {
			return null;
		}
		
		Context newContext =(Context) context.clone();
		int k=0;
		int thisAttNb = this.getAttributeCount();
		int attNb = context.getAttributeCount() + thisAttNb;
		
		for(int i= newContext.getAttributeCount(); i<attNb; i++){
			newContext.addAttribute();
			newContext.setAttributeAt(this.getAttributeAt(k), i);
			
			for(int j=0; j<newContext.getObjectCount(); j++){
				for(int l=0; l<thisAttNb; l++){
					if(newContext.getObjectAt(j).equals(this.getObjectAt(l))){
						newContext.setValueAt(this.getValueAt(l, k), j, i);
						break;
					}
				}
			}
			
			k++;
		}
		
		isModified = true; // TODO Interet? Cet objet n'est pas modifie.
		return newContext;
	}

	public Vector<String> getAttributes() {
		return attributes;
	}

	/**
	 * Recuperation du nombre d'attribut
	 * @return le nombre d'attribut
	 */
	public int getAttributeCount() {
		return attributes.size();
	}

	/**
	 * Ajout d'un attribut
	 */
	public void addAttribute() {
		int attNb = 0;
		String att = CoreMessages.getString("Core.new_"); //$NON-NLS-1$
		String attLetter = String.valueOf((char) ('a' + (attNb % 26)));

		int loopCount = (int) Math.floor(attNb / 26.0);
		for (int j = 0; j <= loopCount; j++)
			att = att + attLetter;

		while (attributes.contains(att)) {
			attNb++;
			att = CoreMessages.getString("Core.new_"); //$NON-NLS-1$
			attLetter = String.valueOf((char) ('a' + (attNb % 26)));

			loopCount = (int) Math.floor(attNb / 26.0);
			for (int j = 0; j <= loopCount; j++)
				att = att + attLetter;
		}

		attributes.add(att);
		for (int i = 0; i < objects.size(); i++) {
			Vector<String> currValues = values.elementAt(i);
			currValues.add(new String("")); //$NON-NLS-1$
		}

		isModified = true;
	}

	/**
	 * Ajout d'un attribut
	 * @param att l'attribut a ajouter
	 * @throws AlreadyExistsException
	 */
	public void addAttribute(String att) throws AlreadyExistsException {
		if (!attributes.contains(att)) {
			attributes.add(att);
			for (int i = 0; i < objects.size(); i++) {
				Vector<String> currValues = values.elementAt(i);
				currValues.add(new String("")); //$NON-NLS-1$
			}
			isModified = true;
		} else
			throw new AlreadyExistsException(CoreMessages.getString("Core.attribute") + //$NON-NLS-1$
					" " + att + CoreMessages.getString("Core.alreadyPresent")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Suppression d'un attribut
	 * @param att l'attribut a supprimer
	 */
	public void removeAttribute(String att) {
		int attIdx = attributes.indexOf(att);

		if (attIdx >= 0) {
			attributes.removeElementAt(attIdx);

			for (int i = 0; i < values.size(); i++) {
				Vector<String> currValues = values.elementAt(i);
				currValues.removeElementAt(attIdx);
			}
			isModified = true;
		}
	}


	/**
	 * Fusion d'attributs
	 * @param attSet l'ensemble d'attribut
	 * @param newName le nom de l'attribut a ajouter
	 * @throws AlreadyExistsException
	 * @throws InvalidTypeException
	 */
	public void mergeAttributes(BasicSet attSet, String newName) throws AlreadyExistsException, InvalidTypeException {
		if (attSet.size() < 2)
			return;
		if (getAttributeIndex(newName) >= 0) {
			throw new AlreadyExistsException(CoreMessages.getString("Core.alreadyAttributeNamed") + " \"" + newName + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		String currAtt = CoreMessages.getString("Core.noValue"); //$NON-NLS-1$
		String currentValue = CoreMessages.getString("Core.noValue"); //$NON-NLS-1$
		String newValue = CoreMessages.getString("Core.noValue"); //$NON-NLS-1$

		int currAttIdx;
		int newAttIdx;
		int objIdx;

		Iterator<String> objIt = attSet.iterator();
		Iterator<String> attIt = attSet.iterator();

		addAttribute(newName);
		newAttIdx = getAttributeIndex(newName);

		while (attIt.hasNext()) {
			currAtt = attIt.next();
			currAttIdx = getAttributeIndex(currAtt);
			BasicSet objSet = getObjectsFor(currAtt);

			objIt = objSet.iterator();
			while (objIt.hasNext()) {
				objIdx = getObjectIndex(objIt.next());
				currentValue = getValueAt(objIdx, currAttIdx);
				newValue = getValueAt(objIdx, newAttIdx);

				if (newValue.equals("")) { //$NON-NLS-1$
					newValue = currentValue;
					setValueAt(newValue, objIdx, newAttIdx);
				}

				else if (!newValue.equals(currentValue)) {
					newValue = newValue + "&" + currentValue; //$NON-NLS-1$
					setValueAt(newValue, objIdx, newAttIdx);
				}
			}
		}
	}

	/**
	 * Enregistrement d'un attribut
	 * @param att l'attribut a enregistrer
	 * @param idx l'indice ou enregistrer l'attribut
	 * @throws AlreadyExistsException
	 */
	public void setAttributeAt(String att, int idx) throws AlreadyExistsException {
		if (!attributes.contains(att)) {
			attributes.set(idx, att);
			isModified = true;
		} else
			throw new AlreadyExistsException(CoreMessages.getString("Core.alreadyAttributeNamed") + att + "\""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getAttributeAt(int idx) {
		return attributes.elementAt(idx);
	}

	public int getAttributeIndex(String att) {
		return attributes.indexOf(att);
	}

	public BasicSet getAttributesFor(int objIdx) {
		BasicSet attSet = new BasicSet();
		if (objIdx > -1 && objIdx < objects.size()) {
			Vector<String> objValues = values.elementAt(objIdx);
			for (int i = 0; i < objValues.size(); i++) {
				if (!(objValues.elementAt(i).equals(""))) //$NON-NLS-1$
					attSet.add(attributes.elementAt(i));
			}
		}
		return attSet;
	}

	public BasicSet getAttributesFor(String obj) {
		return getAttributesFor(objects.indexOf(obj));
	}

	// TODO Verifier le commentaire.
	/**
	 * Fonction pour calculer le prime des attributs avec relation
	 * de fleches.
	 * @param objIdx
	 * @return
	 ***/
	public BasicSet getAttributesArrow(int objIdx) {
		BasicSet attSet = new BasicSet();
		if (objIdx > -1 && objIdx < objects.size()) {
			Vector<String> objValues = values.elementAt(objIdx);
			for (int i = 0; i < objValues.size(); i++) {
				if ((objValues.elementAt(i)).equals("arrowup") || (objValues.elementAt(i)).equals("doblearrow")) //$NON-NLS-1$
					attSet.add(attributes.elementAt(i));
			}
		}
		return attSet;
	}

	public BasicSet getAttributesArrow(String obj) {
		return getAttributesArrow(objects.indexOf(obj));
	}

	public BasicSet compareAttributes(String att1, String att2) {
		int idx1 = getAttributeIndex(att1);
		int idx2 = getAttributeIndex(att2);
	
		if (idx1 < 0 || idx2 < 0)
			return null;
	
		BasicSet differentObjects = new BasicSet();
	
		for (int i = 0; i < values.size(); i++) {
			Vector<String> objValues = values.elementAt(i);
			if (!(objValues.elementAt(idx1)).equals(objValues.elementAt(idx2)))
				differentObjects.add(new String(objects.elementAt(i)));
		}
	
		return differentObjects;
	}

	/**
	 * Regarde si les contextes ont les meme attributs (nombre, type).
	 * @param comp Contexte a comparer.
	 * @return Vrai si les contextes ont les memes attributs, faux sinon.
	 */
	public boolean compareAttributes(Context comp) {
		boolean trouve = false;
		
		Vector<String> vcomp = new Vector<String>();
		vcomp = (Vector<String>) comp.getAttributes();
	
		if( vcomp.size() != this.objects.size()) {
			return false;
		}
	
		for(int i=0; i<this.objects.size(); i++) {
			trouve = false;
			
			for(int j=0; j<vcomp.size(); j++) {
				if(objects.elementAt(i).equals(vcomp.elementAt(j))) {
					// Si les deux chaines correspondent, on met la variable a true et on interrompt la boucle.	
					trouve = true;
					vcomp.remove(j);
					break;
				}
			}
			
			// La chaine n'existe pas dans vcomp.
			if (trouve == false) {
				return trouve;
			}
		}
		
		return true;
	}
	
	/**
	 * Assemble deux contextes ayant les memes attributs.
	 * @param context Contexte a assembler, <code>null</code> si les attributs sont differents.
	 * @return Assemblage des deux contextes.
	 * @throws InvalidTypeException
	 * @throws CloneNotSupportedException
	 * @throws AlreadyExistsException
	 * @author Linda Bongi
	 * @author Nicolas Convers, Arnaud Renaud-Goud
	 */
	public Context subposition(Context context) throws InvalidTypeException, CloneNotSupportedException, AlreadyExistsException {
		if(!this.compareAttributes(context)){
			//System.out.println("Les contextes n'ont pas les meme attributs");
			return null;
		}
	
		Context newContext = (Context) context.clone();
		int thisObjNb = this.getObjectCount();
		int objNb=newContext.getObjectCount()+thisObjNb;
		int k=0;
		
		for(int i=context.getObjectCount(); i<objNb; i++){
			newContext.addObject();
			newContext.setObjectAt(this.getObjectAt(k), i);
			
			for(int j=0; j<thisObjNb; j++){
				for(int l=0; l<this.getAttributeCount(); l++){
					if(newContext.getAttributeAt(j).equals(this.getAttributeAt(l))){
						newContext.setValueAt(this.getValueAt(k, l), i, j);
						break;
					}
				}
			}
			
			k++;
		}
		
		isModified = true; // TODO Interet? Cet objet est non modifie.
		return newContext;
	}

	public abstract void setValueAt(String value, int objIdx, int attIdx) throws InvalidTypeException;
	public abstract void setValueAt(String value, String obj, String att) throws InvalidTypeException;

	public abstract void setFleche(String value, int objIdx, int attIdx);

	/**
	 * Recuperation des donnees de la taxonomie attributs
	 * @author Linda Bogni
	 * @return les donneesTaxonomie
	 */
	public Vector<String> getDonneesTaxonomieAtt() {
		return donneesTaxonomieAtt;
	}

	/**
	 * Retourne taille des donnees de la taxonomie attributs
	 * @author Linda Bogni
	 */
	public int getDonneesTaxonomieAttCount() {
		return donneesTaxonomieAtt.size();
	}

	/**
	 * Retourne un element des donnees de la taxnomie attributs
	 * @author Linda Bogni
	 */
	public String getDonneesTaxonomieAttElement(int i) {
		return donneesTaxonomieAtt.elementAt(i);
	}

	/**
	 * Recuperation des donnees de la taxonomie objets
	 * @author Linda Bogni
	 * @return les donneesTaxonomie
	 */
	public Vector<String> getDonneesTaxonomieObj() {
		return donneesTaxonomieObj;
	}

	/**
	 * Retourne taille des donnees de la taxonomie objets
	 * @author Linda Bogni
	 */
	public int getDonneesTaxonomieObjCount() {
		return donneesTaxonomieObj.size();
	}

	/**
	 * Retourne un element des donnees de la taxnomie objets
	 * @author Linda Bogni
	 */
	public String getDonneesTaxonomieObjElement(int i) {
		return donneesTaxonomieObj.elementAt(i);
	}

	/**
	 * Choix du percentage
	 * @author Linda Bogni
	 */
	public void percentageChoice() {
		String percentageS = DialogBox.showInputQuestion(panel,GUIMessages.getString("GUI.enterValueOfPercentage") + " (%)", //$NON-NLS-1$ //$NON-NLS-2$
				GUIMessages.getString("GUI.taxonomy")); //$NON-NLS-1$
		if (percentageS == null){
			percentage=0;
			return;
		}
		percentage = -1;
		while (percentage < 0 || percentage > 100) {
			try {
				percentage=Double.parseDouble(percentageS);
			} catch (NumberFormatException ex) {
				DialogBox.showMessageWarning(panel, GUIMessages.getString("GUI.valueMustBeBetween0And100"), //$NON-NLS-1$
						GUIMessages.getString("GUI.taxonomy")); //$NON-NLS-1$
				percentage = -1;
				percentageS = DialogBox.showInputQuestion(panel,GUIMessages.getString("GUI.enterValueOfPercentage") + " (%)", //$NON-NLS-1$ //$NON-NLS-2$
						GUIMessages.getString("GUI.taxonomy"));  //$NON-NLS-1$
				if (percentageS == null)
					return;
			}
		}
	}

	public void setObjects(Vector<String> objects) {
		this.objects = objects;
	}

	public void setAttributes(Vector<String> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Modifier un element du vecteur attribut taxonnomie
	 * @auuthor Linda Bogni
	 * @param i
	 * @param value
	 */
	public void setDonneesTaxonomieAttElement(int i, String value) {
		donneesTaxonomieAtt.add(i, value);	
	}
	
	/**
	 * Modifier un element du vecteur objet taxonnomie
	 * @author Linda Bogni
	 * @param i
	 * @param value
	 */
	public void setDonneesTaxonomieObjElement(int i, String value) {
		donneesTaxonomieObj.add(i, value);	
	}
}