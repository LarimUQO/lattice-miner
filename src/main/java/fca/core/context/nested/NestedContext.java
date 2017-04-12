package fca.core.context.nested;

import java.awt.Color;
import java.util.Vector;

import fca.core.context.binary.BinaryContext;
import fca.core.context.valued.ValuedContext;
import fca.core.lattice.ConceptLattice;
import fca.core.lattice.FormalConcept;
import fca.core.lattice.NestedLattice;
import fca.core.util.BasicSet;
import fca.exception.AlreadyExistsException;
import fca.exception.InvalidTypeException;
import fca.exception.LMLogger;
import fca.messages.CoreMessages;

/**
 * Context binaire pouvant contenir un autre context binaire
 * @author Geneviève Roberge
 * @version 1.0
 */
public class NestedContext extends BinaryContext {
	
	protected Color contextColor; //Couleur associée à cette relation (utile pour l'affichage)
	
	protected NestedContext nextContext; //Contexte contenue dans cet contexte binaire
	
	protected String nestedContextName; //Nom de la structure imbriquée de relations
	
	protected NestedLattice nestedLattice; //Treillis graphique associé
	
	/**
	 * Constructeur
	 * @param binCtx Le BinaryContext représenté par ce NestedContext
	 */
	public NestedContext(BinaryContext binCtx) {
		/*
		 * Construction du contexte avec les mêmes objets, attributs et relations que le contexte
		 * binaire de base
		 */
		super(binCtx.getName());
		Vector<String> objects = binCtx.getObjects();
		for (int i = 0; i < objects.size(); i++) {
			try {
				String obj = objects.elementAt(i);
				addObject(obj);
			} catch (AlreadyExistsException e) {
				// No right to be there but already tested
				LMLogger.logSevere(e, false);
			}
		}
		
		Vector<String> attributes = binCtx.getAttributes();
		for (int i = 0; i < attributes.size(); i++) {
			try {
				String att = attributes.elementAt(i);
				addAttribute(att);
			} catch (AlreadyExistsException e) {
				// No right to be there but already tested
				LMLogger.logSevere(e, false);
			}
		}
		
		for (int i = 0; i < binCtx.getObjectCount(); i++) {
			for (int j = 0; j < binCtx.getAttributeCount(); j++) {
				String value = binCtx.getValueAt(i, j);
				if (value.equals(BinaryContext.TRUE)) {
					try {
						setValueAt(value, i, j);
					} catch (InvalidTypeException e) {
						// Nothing to do, the value as already been tested and is valid
						System.err.println("Invalid Type error :"+e);
					}
				}
			}
		}
		
		nestedContextName = contextName;
		nextContext = null;
		contextColor = Color.WHITE;
		nestedLattice = null;
	}
	
	/**
	 * Assigne une couleur à la relation
	 * @param c Le Color contenant la couleur assignée
	 */
	public void setColor(Color c) {
		contextColor = c;
	}
	
	/**
	 * Permet de connaître la couleur associée à la relation
	 * @return Le Color contenant la couleur associée à la relation
	 */
	public Color getColor() {
		return contextColor;
	}
	
	/**
	 * Assigne un treillis graphique à la relation
	 * @param nl le {@link NestedLattice} à associer avec la relation
	 */
	public void setNestedLattice(NestedLattice nl) {
		nestedLattice = nl;
	}
	
	/**
	 * Permet de connaître le treillis graphique assicié à la relation
	 * @return Le NestedLattice associée à la relation
	 */
	public NestedLattice getNestedLattice() {
		return nestedLattice;
	}
	
	/**
	 * Permet de changer la position de 2 objets dans la liste des objets et celle des relations
	 * @param idx1 Le int contenant la position d'un premier objet qui doit être changé de position
	 * @param idx2 Le int contenant la position d'un deuxième objet qui doit être changé de position
	 */
	public void swapObjects(int idx1, int idx2) {
		String obj1 = objects.elementAt(idx1);
		String obj2 = objects.elementAt(idx2);
		Vector<String> values1 = values.elementAt(idx1);
		Vector<String> values2 = values.elementAt(idx2);
		
		objects.set(idx1, obj2);
		values.set(idx1, values2);
		
		objects.set(idx2, obj1);
		values.set(idx2, values1);
	}
	
	/**
	 * Permet de replacer les object d'une autre relation ayant les mêmes objects que celle-ci dans
	 * le même ordre que les objets de cette relation
	 * @param newCtx Le NestedContext à trier
	 */
	public void sortObjects(NestedContext newCtx) {
		Vector<String> ownObjects = getObjects();
		Vector<String> newObjects = newCtx.getObjects();
		
		for (int i = 0; i < ownObjects.size() && i < newObjects.size(); i++) {
			String ownObj = ownObjects.elementAt(i);
			String newObj = newObjects.elementAt(i);
			if (!(ownObj.equals(newObj))) {
				int idx = newCtx.getObjectIndex(ownObj);
				newCtx.swapObjects(i, idx);
			}
		}
	}
	
	/**
	 * Permet d'ajouter un contexte à la fin de la liste des contextes imbriqués
	 * @param nextCtx Le NestedContext à ajouter
	 */
	public void addNextContext(NestedContext nextCtx) {
		/* Le treillis graphique n'est plus correct */
		nestedLattice = null;
		
		/* Comparaison des objets de la nouvelle relation avec les autres */
		/*
		 * Une liste de tous les objets de la relation courante est initialement construite et on
		 * lui enlèvera chacun des objets qu'elle partage avec la relation à ajouter pour connaître
		 * les objets qui doivent être ajoutés à la nouvelle relation. En même temps, les objets de
		 * la relation courante non présents dans la nouvelle relation seront ajoutés à la nouvelle
		 * relation.
		 */
		Vector<String> existingObjects = new Vector<String>(getObjects());
		Vector<String> newObjects = new Vector<String>(nextCtx.getObjects());
		for (int i = 0; i < newObjects.size(); i++) {
			String obj = newObjects.elementAt(i);
			if (existingObjects.contains(obj))
				existingObjects.remove(obj);
			else {
				try {
					addObject(obj);
				} catch (AlreadyExistsException e) {
					// Never reach because the original BinaryContext is valid
					LMLogger.logSevere(e, false);
				}
			}
		}
		
		/*
		 * Les objets de la nouvelle relation non présents dans la relation courante sont ajoutés
		 * dans la relation courante
		 */
		for (int i = 0; i < existingObjects.size(); i++)
			try {
				nextCtx.addObject(existingObjects.elementAt(i));
			} catch (AlreadyExistsException e) {
				// Never reach because the original BinaryContext is valid
				LMLogger.logSevere(e, false);
			}
		
		/*
		 * Tri de la nouvelle relation pour que ses objets soient dans le même ordre que ceux de la
		 * relation courante
		 */
		sortObjects(nextCtx);
		
		/* Ajout de la nouvelle relation */
		if (nextContext != null)
			nextContext.addNextContext(nextCtx);
		else
			nextContext = nextCtx;
	}
	
	/**
	 * Permet de connaître le contexte qui suit ce contexte
	 * @return Le NestedContext qui suit ce context
	 */
	public NestedContext getNextContext() {
		return nextContext;
	}
	
	/**
	 * Permet de supprimer le dernier contexte de la liste de contextes
	 * @return Le boolean indiquant si un contexte a pu être retirée à partir de ce treillis
	 */
	public boolean hasRemovedLastContext() {
		nestedLattice = null;
		
		/* Aucun contexte n'a été enlevé à partir de ce contexte */
		if (nextContext == null) {
			return false;
		}
		/*
		 * Le contexte "nextContext" est le dernier dans la séquence : c'est lui qui doit être
		 * enlevé
		 */
		else if (nextContext.getNextContext() == null) {
			nextContext = null;
			return true;
		}
		/* Un contexte a été enlevé plus loin dans la séquence de contextes */
		else {
			nextContext.hasRemovedLastContext();
			return true;
		}
	}
	
	/**
	 * Permet de nommer cette liste de context
	 * @param name La String contenant le nom à donner à la liste de contextes
	 */
	public void setNestedContextName(String name) {
		nestedContextName = name;
		if (nextContext != null)
			nextContext.setNestedContextName(name);
	}
	
	/**
	 * Permet de connaître le nom de cette liste de contextes
	 * @return La String contenant le nom de la liste de contextes
	 */
	public String getNestedContextName() {
		return nestedContextName;
	}
	
	/**
	 * Permet de connaître le nombre de contextes imbriqués à partir de ce contexte
	 * @return Le int contenant le nombre de contextes imbriqués
	 */
	public int getLevelCount() {
		int nbLevels = 1;
		NestedContext currentContext = getNextContext();
		while (currentContext != null) {
			currentContext = currentContext.getNextContext();
			nbLevels++;
		}
		
		return nbLevels;
	}
	
	/**
	 * Permet de changer la position d'un contexte dans la liste des contextes
	 * @param fromIndex Le int contenant la position du contexte à déplacer
	 * @param toIndex Le int contenant la nouvelle position du contexte déplacé
	 */
	public void moveLevel(int fromIndex, int toIndex) {
		/* Le treillis graphique n'est plus correct */
		nestedLattice = null;
		
		/* Aucun déplacement à effectuer */
		if (fromIndex == toIndex)
			return;
		
		/* Création d'une liste de contextes positionnés dans le même ordre la liste courante */
		Vector<NestedContext> levelsList = new Vector<NestedContext>();
		levelsList.add((NestedContext) this.clone());
		
		NestedContext currentContext = this;
		while ((currentContext = currentContext.getNextContext()) != null) {
			levelsList.add((NestedContext) currentContext.clone());
		}
		
		/* Suppression de toutes les relations suivant cette relation */
		while (hasRemovedLastContext())
			;
		
		/* Déplacement de la relation à déplacer, dans la liste créée */
		NestedContext fromLevel = levelsList.elementAt(fromIndex);
		levelsList.removeElementAt(fromIndex);
		levelsList.insertElementAt(fromLevel, toIndex);
		
		/*
		 * Si le contexte courant n'est pas touché par le déplacement, ajout des contextes suivant
		 * ce contexte, dans l'ordre de la liste créée
		 */
		if (fromIndex > 0 && toIndex > 0) {
			for (int i = 1; i < levelsList.size(); i++)
				addNextContext(new NestedContext(levelsList.elementAt(i)));
		}
		/*
		 * Si le contexte courant est touché par le déplacement, modification du contexte binaire
		 * représenté dans le contexte courant et ensuite ajout des contextes suivant ce contexte,
		 * dans l'ordre de la liste créée
		 */
		else {
			setBinaryContext(levelsList.elementAt(0));
			
			for (int i = 1; i < levelsList.size(); i++)
				addNextContext(new NestedContext(levelsList.elementAt(i)));
		}
	}
	
	/**
	 * Permet de changer le contexte binaire représenté dans ce contexte
	 * @param binCtx Le BinaryContext à insérer dans ce contexte
	 */
	public void setBinaryContext(BinaryContext binCtx) {
		/* Réinitialisation de toutes les variables du contexte */
		objects = new Vector<String>();
		attributes = new Vector<String>();
		values = new Vector<Vector<String>>();
		conceptLattice = null;
		nextContext = null;
		nestedLattice = null;
		
		/* Reconstruction de la relation binaire */
		if (getObjects().size() == 0 && getAttributes().size() == 0 && nextContext == null) {
			Vector<String> objList = binCtx.getObjects();
			for (int i = 0; i < objList.size(); i++) {
				try {
					addObject(objList.elementAt(i));
				} catch (AlreadyExistsException e) {
					// Never reach because the original BinaryContext is valid
					LMLogger.logSevere(e, false);
				}
			}
			
			Vector<String> attList = binCtx.getAttributes();
			for (int i = 0; i < attList.size(); i++) {
				try {
					addAttribute(attList.elementAt(i));
				} catch (AlreadyExistsException e) {
					// Never reach because the original BinaryContext is valid
					LMLogger.logSevere(e, false);
				}
			}
			
			for (int i = 0; i < binCtx.getObjectCount(); i++) {
				for (int j = 0; j < binCtx.getAttributeCount(); j++) {
					String value = binCtx.getValueAt(i, j);
					if (value.equals(BinaryContext.TRUE)) {
						try {
							setValueAt(value, i, j);
						} catch (InvalidTypeException e) {
							// Never reach because the original BinaryContext is valid
							LMLogger.logSevere(e, false);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Crée un seul contexte binaire contenant tous les contextes binaires de la suite de contextes
	 * @return Le BinaryContext contenant tous les contextes binaires de la suite de contextes
	 */
	public BinaryContext mergeContextList() throws AlreadyExistsException, InvalidTypeException {
		BinaryContext binCtx = (BinaryContext) super.clone();
		
		/* Ajout de chacun des contextes de la liste de contextes dans le nouveau contexte binaire */
		NestedContext currentContext = this;
		while ((currentContext = currentContext.getNextContext()) != null) {
			BinaryContext nextCtx = (BinaryContext) ((BinaryContext) currentContext).clone();
			binCtx.addBinaryContext(nextCtx, false);
		}
		
		binCtx.setName(contextName);
		return binCtx;
	}
	
	public BinaryContext getFirstLevelBinaryContext() {
		BinaryContext binCtx = (BinaryContext) super.clone();
		return binCtx;
	}
	
	/**
	 * Crée un seul contexte binaire contenant tous les contextes binaires de la suite de contextes
	 * @return Le BinaryContext contenant tous les contextes binaires de la suite de contextes
	 */
	public BinaryContext convertToBinaryContext() throws AlreadyExistsException, InvalidTypeException {
		BinaryContext binCtx = (BinaryContext) super.clone();
		
		/* Ajout de chacun des contextes de la liste de contextes dans le nouveau contexte binaire */
		NestedContext currentContext = this;
		while ((currentContext = currentContext.getNextContext()) != null) {
			BinaryContext nextCtx = (BinaryContext) ((BinaryContext) currentContext).clone();
			binCtx.addBinaryContext(nextCtx, false);
		}
		
		binCtx.setName(contextName);
		return binCtx;
	}
	
	/**
	 * Crée un liste de contextes binaires contenant chacun des contextes binaires de la suite de
	 * contextes
	 * @return Le Vector contenant la liste des contextes binaires de la suite de contextes
	 */
	public Vector<BinaryContext> convertToBinaryContextList() {
		BinaryContext binCtx = (BinaryContext) super.clone();
		Vector<BinaryContext> contextList = new Vector<BinaryContext>();
		contextList.add(binCtx);
		
		/*
		 * Ajout de chacun des contextes de la contextes imbrique dans la liste de contextes
		 * binaires
		 */
		NestedContext currentContext = this;
		while ((currentContext = currentContext.getNextContext()) != null) {
			BinaryContext nextCtx = currentContext.getFirstLevelBinaryContext();
			contextList.add(nextCtx);
		}
		
		return contextList;
	}
	
	public ValuedContext getLatticeBasedValuedContext() throws AlreadyExistsException, InvalidTypeException {
		ValuedContext latticeContext = new ValuedContext(CoreMessages.getString("Core.levelBasedContext")); //$NON-NLS-1$
		BinaryContext binCtx = convertToBinaryContext();
		ConceptLattice binLattice = new ConceptLattice(binCtx);
		Vector<FormalConcept> conceptList = binLattice.getConcepts();
		Vector<BasicSet> extents = new Vector<BasicSet>();
		
		for (int i = 0; i < conceptList.size(); i++) {
			FormalConcept concept = conceptList.elementAt(i);
			try {
				latticeContext.addObject(concept.getExtent().toString());
				extents.add(concept.getExtent());
			} catch (AlreadyExistsException e) {
				// Never reach because the original BinaryContext is valid
				LMLogger.logSevere(e, false);
			}
		}
		
		Vector<BinaryContext> contextList = convertToBinaryContextList();
		for (int i = 0; i < contextList.size(); i++) {
			ConceptLattice currentLattice = new ConceptLattice(contextList.elementAt(i));
			String attName = CoreMessages.getString("Core.level") + (i + 1); //$NON-NLS-1$
			
			try {
				latticeContext.addAttribute(attName);
			} catch (AlreadyExistsException e) {
				// Never reach because the original BinaryContext is valid
				LMLogger.logSevere(e, false);
			}
			
			Vector<FormalConcept> concepts = currentLattice.getConcepts();
			for (int j = 0; j < extents.size(); j++) {
				BasicSet extent = extents.elementAt(j);
				int conceptIdx = -1;
				
				for (int k = 0; k < concepts.size(); k++) {
					FormalConcept concept = concepts.elementAt(k);
					BasicSet conceptExtent = concept.getExtent();
					if (conceptExtent.containsAll(extent)) {
						if (conceptIdx < 0)
							conceptIdx = k;
						else if (conceptExtent.size() < (concepts.elementAt(conceptIdx)).getExtent().size())
							conceptIdx = k;
					}
				}
				
				String objName = extent.toString();
				try {
					latticeContext.setValueAt("" + conceptIdx, objName, attName); //$NON-NLS-1$
				} catch (InvalidTypeException e) {
					// Never reach because the original BinaryContext is valid
					LMLogger.logSevere(e, false);
				}
				
			}
		}
		
		int objNum = 1;
		for (int i = 0; i < extents.size(); i++) {
			BasicSet extent = extents.elementAt(i);
			String objName = extent.toString();
			Vector<String> names = new Vector<String>();
			for (int j = 0; j < extent.size(); j++) {
				names.add(new String("" + objNum)); //$NON-NLS-1$
				objNum++;
			}
			
			try {
				latticeContext.addObjectCopies(objName, names);
			} catch (AlreadyExistsException e) {
				// Never reach because the original BinaryContext is valid
				LMLogger.logSevere(e, false);
			}
			latticeContext.removeObject(objName);
		}
		
		return latticeContext;
	}
	
	public ValuedContext getNumericValuedContext() {
		ValuedContext valCtx = new ValuedContext(CoreMessages.getString("Core.valued_") + contextName); //$NON-NLS-1$
		Vector<BinaryContext> binCtxList = convertToBinaryContextList();
		
		for (int i = 0; i < getObjectCount(); i++) {
			String objName = getObjectAt(i);
			try {
				valCtx.addObject(objName);
			} catch (AlreadyExistsException e) {
				// Never reach because the original BinaryContext is valid
				LMLogger.logSevere(e, false);
			}
		}
		
		for (int i = 0; i < binCtxList.size(); i++) {
			BinaryContext binCtx = binCtxList.elementAt(i);
			String attName = binCtx.getName() + CoreMessages.getString("Core.level_") + (i + 1); //$NON-NLS-1$
			try {
				valCtx.addAttribute(attName);
			} catch (AlreadyExistsException e) {
				// Never reach because the original BinaryContext is valid
				LMLogger.logSevere(e, false);
			}
			
			Vector<BasicSet> values = new Vector<BasicSet>();
			for (int j = 0; j < getObjectCount(); j++) {
				String objName = getObjectAt(j);
				BasicSet attList = binCtx.getAttributesFor(objName);
				int valueIdx = values.indexOf(attList);
				
				if (valueIdx < 0) {
					values.add(attList);
					valueIdx = values.size() - 1;
				}
				
				try {
					valCtx.setValueAt(new String("" + valueIdx), objName, attName); //$NON-NLS-1$
				} catch (InvalidTypeException e) {
					// Never reach because the original BinaryContext is valid
					LMLogger.logSevere(e, false);
				}
			}
		}
		
		return valCtx;
	}
	
	/**
	 * Crée une copie profonde de ce contexte
	 * @return L'Object contenant la copie du contexte
	 */
	@Override
	public Object clone() {
		BinaryContext binCtx = (BinaryContext) super.clone();
		
		/* Création d'une nouvelle NestedRelation avec les même valeurs que la relation courante */
		NestedContext subCtx = new NestedContext(binCtx);
		subCtx.setName(binCtx.getName());
		subCtx.setNestedContextName(nestedContextName);
		subCtx.setNestedLattice(nestedLattice);
		if (nextContext != null)
			subCtx.addNextContext((NestedContext) getNextContext().clone());
		
		return subCtx;
	}
	
	/**
	 * Permet d'obtenir la chaîne de caractères représentant cette relation
	 * @return La String contenant le nom de la suite de contextes
	 */
	@Override
	public String toString() {
		String str = "\n" + CoreMessages.getString("Core.contextName") + " : " + contextName + "\n" + CoreMessages.getString("Core.levelCount") + " : " + getLevelCount() + "\n" + CoreMessages.getString("Core.objectCount") + " : " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				+ getObjectCount() + "\n" + CoreMessages.getString("Core.attributeCount") + " : " + getAttributeCount() + "\n\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		return str;
	}
	
}
