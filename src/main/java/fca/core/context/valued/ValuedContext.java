package fca.core.context.valued;

import java.util.TreeSet;
import java.util.Vector;

import fca.core.context.Context;
import fca.core.context.binary.BinaryContext;
import fca.core.util.BasicSet;
import fca.exception.AlreadyExistsException;
import fca.exception.InvalidTypeException;
import fca.exception.LMLogger;
import fca.messages.CoreMessages;

public class ValuedContext extends Context {
	private Vector<Integer> numberAttributeValueFlags;
	
	public ValuedContext(String name) {
		super(name);
		numberAttributeValueFlags = new Vector<Integer>();
	}
	
	public ValuedContext(String name, int objCnt, int attCnt) {
		super(name, objCnt, attCnt);
		
		/* Initialement, chaque attribut n'a que des valeurs non numeriques ("") */
		numberAttributeValueFlags = new Vector<Integer>();
		for (int i = 0; i < attCnt; i++)
			numberAttributeValueFlags.add(new Integer(0));
	}
	
	@Override
	public void setValueAt(String val, int objIdx, int attIdx) throws InvalidTypeException {
		if (val == null)
			throw new InvalidTypeException(CoreMessages.getString("Core.valueOf") + " (" + objIdx + "," + attIdx + ") " + CoreMessages.getString("Core.isNull")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		else {
			String oldValue = getValueAt(objIdx, attIdx);
			int numCount = (numberAttributeValueFlags.elementAt(attIdx)).intValue();
			if (isInteger(oldValue) || isDouble(oldValue))
				numCount--;
			if (isInteger(val) || isDouble(val)) {
				numCount++;
			}
			numberAttributeValueFlags.set(attIdx, new Integer(numCount));
			
			(values.elementAt(objIdx)).set(attIdx, val);
			isModified = true;
		}
	}
	
	@Override
	public void setValueAt(String val, String obj, String att) throws InvalidTypeException {
		int objIdx = objects.indexOf(obj);
		int attIdx = attributes.indexOf(att);
		
		if (objIdx > -1 && attIdx > -1) {
			if (val == null)
				throw new InvalidTypeException(CoreMessages.getString("Core.valueOf") + " (" + obj + "," + att + ") " + CoreMessages.getString("Core.isNull")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			else {
				/*
				 * Ajustement de l'indicateur de valeur non numerique pour l'attribut de la relation
				 * ciblee
				 */
				String oldValue = getValueAt(objIdx, attIdx);
				int numCount = numberAttributeValueFlags.elementAt(attIdx).intValue();
				if (isInteger(oldValue) || isDouble(oldValue))
					numCount--;
				if (isInteger(val) || isDouble(val)) {
					numCount++;
				}
				numberAttributeValueFlags.set(attIdx, new Integer(numCount));
				
				(values.elementAt(objIdx)).set(attIdx, val);
				isModified = true;
			}
		} else
			throw new InvalidTypeException(CoreMessages.getString("Core.cellContext") + " (" + obj + "," + att + ") " + CoreMessages.getString("Core.isInvalid")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
	
	public void removeObject(int objIdx) {
		for (int j = 0; j < attributes.size(); j++) {
			/*
			 * Ajustement de l'indicateur de valeur non numerique pour l'attribut de la relation
			 * ciblee
			 */
			String oldValue = getValueAt(objIdx, j);
			int numCount = numberAttributeValueFlags.elementAt(j).intValue();
			if (isInteger(oldValue) || isDouble(oldValue))
				numCount--;
			numberAttributeValueFlags.set(j, new Integer(numCount));
		}
		
		if (objIdx >= 0) {
			objects.removeElementAt(objIdx);
			values.removeElementAt(objIdx);
			isModified = true;
		}
	}
	
	@Override
	public void removeObject(String obj) {
		int objIdx = objects.indexOf(obj);
		
		for (int j = 0; j < attributes.size(); j++) {
			/*
			 * Ajustement de l'indicateur de valeur non numerique pour l'attribut de la relation
			 * ciblee
			 */
			String oldValue = getValueAt(objIdx, j);
			int numCount = numberAttributeValueFlags.elementAt(j).intValue();
			if (isInteger(oldValue) || isDouble(oldValue))
				numCount--;
			numberAttributeValueFlags.set(j, new Integer(numCount));
		}
		
		if (objIdx >= 0) {
			objects.removeElementAt(objIdx);
			values.removeElementAt(objIdx);
			isModified = true;
		}
	}
	
	@Override
	public void addAttribute() {
		super.addAttribute();
		numberAttributeValueFlags.add(new Integer(0));
	}
	
	@Override
	public void addAttribute(String att) throws AlreadyExistsException {
		super.addAttribute(att);
		numberAttributeValueFlags.add(new Integer(0));
	}
	
	/**
	 * Supprime l'attribut a la position donnee
	 * @param attIdx Le int contenant la position de l'attribut a supprimer
	 */
	public void removeAttribute(int attIdx) {
		super.removeAttribute(getAttributeAt(attIdx));
		if (attIdx >= 0 && attIdx < numberAttributeValueFlags.size()) {
			numberAttributeValueFlags.removeElementAt(attIdx);
		}
	}
	
	/**
	 * Supprime l'attribut donne
	 * @param att La String contenant la nom de l'attribut a supprimer
	 */
	@Override
	public void removeAttribute(String att) {
		int attIdx = getAttributeIndex(att);
		super.removeAttribute(att);
		if (attIdx >= 0 && attIdx < numberAttributeValueFlags.size()) {
			numberAttributeValueFlags.removeElementAt(attIdx);
		}
	}
	
	/**
	 * Permet d'obtenir l'ensemble (sans repetition) des valeurs possedees par l'attibut a la
	 * position donnee
	 * @param attIdx Le int contenant la position de l'attribut recherche
	 * @return Le TreeSet contenant l'ensemble des valeurs pour l'attribut recherche
	 */
	public TreeSet<String> getValuesForAttribute(int attIdx) {
		TreeSet<String> attValues = new TreeSet<String>();
		for (int i = 0; i < objects.size(); i++)
			attValues.add(new String(((values.elementAt(i)).elementAt(attIdx))));
		return attValues;
	}
	
	/**
	 * Convertit ce contexte value en un contexte binaire
	 * @param model Le ValuedContextConversionModel qui contient les regles de conversion de ce
	 *        contexte
	 * @return Le BinaryContext cree suite a la conversion
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	// getNewAttributeValues non type
	public BinaryContext convertToBinaryContext(ValuedContextConversionModel model) {
		BinaryContext binCtx = new BinaryContext(getName() + CoreMessages.getString("Core.conv_"), 0, 0); //$NON-NLS-1$
		Vector<String> binObjects = new Vector<String>(objects);
		
		/* Ajout de tous les objets dans la relation binaire */
		for (int i = 0; i < binObjects.size(); i++) {
			try {
				binCtx.addObject(binObjects.elementAt(i));
			} catch (AlreadyExistsException e) {
				// No right to be there but already tested
				LMLogger.logSevere(e, false);
			}
		}
		
		/* Ajout des attributs et de leurs relations */
		for (int oldAttIdx = 0; oldAttIdx < attributes.size(); oldAttIdx++) {
			boolean[] remainingObjects = new boolean[objects.size()];
			for (int idx = 0; idx < objects.size(); idx++)
				remainingObjects[idx] = true;
			
			Vector<String> attributesNames = model.getNewAttributesNames(oldAttIdx);
			int attributeType = model.getNewAttributesType(oldAttIdx);
			
			for (int newAttIdx = 0; newAttIdx < attributesNames.size(); newAttIdx++) {
				/* Ajout de l'attribut */
				String attribute = new String(attributesNames.elementAt(newAttIdx));
				boolean usedAttribute = false;
				try {
					binCtx.addAttribute(attribute);
				} catch (AlreadyExistsException e) {
					// No right to be there but already tested
					LMLogger.logSevere(e, false);
					usedAttribute = true;
				}
				
				/* Ajustement du contexte */
				/* Traitement des attributs avec des valeurs spécifiques */
				if (attributeType == ValuedContextConversionModel.VALUES) {
					Vector attributeValues = model.getNewAttributeValues(oldAttIdx, newAttIdx);
					for (int objIdx = 0; objIdx < objects.size(); objIdx++) {
						if (attributeValues.contains(getValueAt(objIdx, oldAttIdx))) {
							String obj = binCtx.getObjectAt(objIdx);
							try {
								binCtx.setValueAt(BinaryContext.TRUE, obj, attribute);
								usedAttribute = true;
								remainingObjects[objIdx] = false;
							} catch (InvalidTypeException e) {
								// Never reach because the original BinaryContext is valid
								LMLogger.logSevere(e, false);
							}
						}
					}
				}

				/* Traitement des attributs avec un intervalle de valeurs */
				else if (attributeType == ValuedContextConversionModel.INTERVAL) {
					double startValue = 0;
					double endValue = -1;
					startValue = ((Double) model.getNewAttributeValues(oldAttIdx, newAttIdx).elementAt(0)).doubleValue();
					endValue = ((Double) model.getNewAttributeValues(oldAttIdx, newAttIdx).elementAt(1)).doubleValue();
					
					for (int objIdx = 0; objIdx < objects.size(); objIdx++) {
						if (getValueAt(objIdx, oldAttIdx) != null && !(getValueAt(objIdx, oldAttIdx).trim()).equals("")) { //$NON-NLS-1$
							double currentValue = Double.parseDouble(getValueAt(objIdx, oldAttIdx));
							if (currentValue >= startValue && currentValue < endValue) {
								String obj = binCtx.getObjectAt(objIdx);
								try {
									binCtx.setValueAt(BinaryContext.TRUE, obj, attribute);
									usedAttribute = true;
									remainingObjects[objIdx] = false;
								} catch (InvalidTypeException e) {
									// Never reach because the original BinaryContext is valid
									LMLogger.logSevere(e, false);
								}
							}
						}
					}
				}
				
				/* Si l'attribut n'est pas utilise, il est retire du contexte. */
				if (usedAttribute == false)
					binCtx.removeAttribute(attribute);
			}
			
			/* Traitement des valeurs non comprises dans un intervalle */
			if (attributeType == ValuedContextConversionModel.INTERVAL) {
				/* Ajout de l'attribut vide */
				String emptyAttribute = new String(getAttributeAt(oldAttIdx));
				boolean isCorrect = false;
				while (!isCorrect) {
					emptyAttribute = emptyAttribute + "_"; //$NON-NLS-1$
					try {
						binCtx.addAttribute(emptyAttribute);
						isCorrect = true;
					} catch (AlreadyExistsException aee) {
						isCorrect = false;
					}
				}
				
				boolean emptyUsed = false;
				for (int objIdx = 0; objIdx < objects.size(); objIdx++) {
					if (remainingObjects[objIdx] == true) {
						String obj = binCtx.getObjectAt(objIdx);
						try {
							binCtx.setValueAt(BinaryContext.TRUE, obj, emptyAttribute);
							emptyUsed = true;
						} catch (InvalidTypeException e) {
							// Never reach because the original BinaryContext is valid
							LMLogger.logSevere(e, false);
						}
					}
				}
				
				/* Si l'attribut n'est pas utilise, il est retire du contexte. */
				if (emptyUsed == false)
					binCtx.removeAttribute(emptyAttribute);
			}
		}
		return binCtx;
	}
	
	/**
	 * Convertit cette relation valee en plusieurs relations binaires
	 * @param model Le ValuedContextConversionModel qui contient les regles de conversion de ce
	 *        contexte
	 * @return Le Vector de BinaryContext cree suite a la conversion
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	// getNewAttributeValues non type
	public Vector<BinaryContext> convertToBinaryContexts(ValuedContextConversionModel model) {
		Vector<BinaryContext> contexts = new Vector<BinaryContext>();
		Vector<String> binObjects = new Vector<String>(objects);
		
		/* Creation des relations binaires */
		for (int oldAttIdx = 0; oldAttIdx < attributes.size(); oldAttIdx++) {
			BinaryContext binCtx = new BinaryContext(getName() + CoreMessages.getString("Core.conv_") + (oldAttIdx + 1), 0, 0); //$NON-NLS-1$
			contexts.add(binCtx);
			/* Ajout des objets */
			for (int j = 0; j < binObjects.size(); j++) {
				try {
					binCtx.addObject(new String(binObjects.elementAt(j)));
				} catch (AlreadyExistsException e) {
					// No right to be there but already tested
					LMLogger.logSevere(e, false);
				}
			}
			
			/* Ajout des attributs et de leurs relations */
			boolean[] remainingObjects = new boolean[objects.size()];
			for (int idx = 0; idx < objects.size(); idx++)
				remainingObjects[idx] = true;
			
			Vector attributesNames = model.getNewAttributesNames(oldAttIdx);
			int attributeType = model.getNewAttributesType(oldAttIdx);
			
			for (int newAttIdx = 0; newAttIdx < attributesNames.size(); newAttIdx++) {
				/* Ajout de l'attribut */
				boolean usedAttribute = false;
				String attribute = new String((String) attributesNames.elementAt(newAttIdx));
				try {
					binCtx.addAttribute(attribute);
				} catch (AlreadyExistsException e) {
					// No right to be there but already tested
					LMLogger.logSevere(e, false);
				}
				
				/* Ajustement du contexte */
				/* Traitement des attributs avec des valeurs spécifiques */
				if (attributeType == ValuedContextConversionModel.VALUES) {
					Vector attributeValues = model.getNewAttributeValues(oldAttIdx, newAttIdx);
					for (int objIdx = 0; objIdx < objects.size(); objIdx++) {
						if (attributeValues.contains(getValueAt(objIdx, oldAttIdx))) {
							String obj = binCtx.getObjectAt(objIdx);
							try {
								binCtx.setValueAt(BinaryContext.TRUE, obj, attribute);
								usedAttribute = true;
							} catch (InvalidTypeException e) {
								// Never reach because the original BinaryContext is valid
								LMLogger.logSevere(e, false);
							}
						}
					}
				}

				/* Traitement des attributs avec un intervalle de valeurs */
				else if (attributeType == ValuedContextConversionModel.INTERVAL) {
					double startValue = 0;
					double endValue = -1;
					startValue = ((Double) model.getNewAttributeValues(oldAttIdx, newAttIdx).elementAt(0)).doubleValue();
					endValue = ((Double) model.getNewAttributeValues(oldAttIdx, newAttIdx).elementAt(1)).doubleValue();
					
					for (int objIdx = 0; objIdx < objects.size(); objIdx++) {
						if (getValueAt(objIdx, oldAttIdx) != null && !(getValueAt(objIdx, oldAttIdx).trim()).equals("")) { //$NON-NLS-1$
							double currentValue = Double.parseDouble(getValueAt(objIdx, oldAttIdx));
							if (currentValue >= startValue && currentValue < endValue) {
								String obj = binCtx.getObjectAt(objIdx);
								try {
									binCtx.setValueAt(BinaryContext.TRUE, obj, attribute);
									remainingObjects[objIdx] = false;
									usedAttribute = true;
								} catch (InvalidTypeException e) {
									// Never reach because the original BinaryContext is valid
									LMLogger.logSevere(e, false);
								}
							}
						}
					}
				}
				
				/* Si l'attribut n'est pas utilise, il est retire du contexte. */
				if (usedAttribute == false)
					binCtx.removeAttribute(attribute);
			}
			
			/* Traitement des valeurs non comprises dans un intervalle */
			if (attributeType == ValuedContextConversionModel.INTERVAL) {
				/* Ajout de l'attribut vide */
				String emptyAttribute = new String(getAttributeAt(oldAttIdx));
				boolean isCorrect = false;
				while (!isCorrect) {
					emptyAttribute = emptyAttribute + "_"; //$NON-NLS-1$
					try {
						binCtx.addAttribute(emptyAttribute);
						isCorrect = true;
					} catch (AlreadyExistsException aee) {
						isCorrect = false;
					}
				}
				
				boolean emptyUsed = false;
				for (int objIdx = 0; objIdx < objects.size(); objIdx++) {
					if (remainingObjects[objIdx] == true) {
						String obj = binCtx.getObjectAt(objIdx);
						try {
							binCtx.setValueAt(BinaryContext.TRUE, obj, emptyAttribute);
							emptyUsed = true;
						} catch (InvalidTypeException e) {
							// Never reach because the original BinaryContext is valid
							LMLogger.logSevere(e, false);
						}
					}
				}
				
				/* Si l'attribut n'est pas utilise, il est retire du contexte. */
				if (emptyUsed == false)
					binCtx.removeAttribute(emptyAttribute);
			}
		}
		return contexts;
	}
	
	/**
	 * Permet de savoir si un attribut contient des valeurs qui sont toutes numeriques ou non
	 * @param att La String de l'attribut pour lequel on veut connaitre le type de valeurs
	 * @return Le boolean indiquant si les valeurs de l'attribut sont toutes numeriques
	 */
	public boolean isNumericAttribute(String att) {
		int attIdx = attributes.indexOf(att);
		if (attIdx >= numberAttributeValueFlags.size())
			return false;
		
		return isNumericAttribute(attIdx);
	}
	
	/**
	 * Permet de savoir si un attribut contient des valeurs qui sont toutes numeriques ou non
	 * @param attIdx Le int contenant la position de l'attribut pour lequel on veut connaitre le type de
	 *        valeurs
	 * @return Le boolean indiquant si les valeurs de l'attribut sont toutes numeriques
	 */
	public boolean isNumericAttribute(int attIdx) {
		if (attIdx >= numberAttributeValueFlags.size())
			return false;
		return (numberAttributeValueFlags.elementAt(attIdx).intValue() == objects.size());
	}
	
	/**
	 * Permet de savoir si la valeur contenue dans une chaine est un entier
	 * @param value La String contenant la valeur a verifier
	 * @return Le boolean indiquant si la valeur est un entier
	 */
	private boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Permet de savoir si la valeur contenue dans une chaine est un nombre a virgule
	 * @param value La String contenant la valeur a verifier
	 * @return Le boolean indiquant si la valeur est un nombre a virgule
	 */
	private boolean isDouble(String value) {
		try {
			Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public Object clone() {
		ValuedContext newValCtx = new ValuedContext(contextName);
		for (int i = 0; i < objects.size(); i++) {
			try {
				newValCtx.addObject(objects.elementAt(i));
			} catch (AlreadyExistsException e) {
				// Never reach because the original BinaryContext is valid
				LMLogger.logSevere(e, false);
			}
		}
		
		for (int i = 0; i < attributes.size(); i++) {
			try {
				newValCtx.addAttribute(attributes.elementAt(i));
			} catch (AlreadyExistsException e) {
				// Never reach because the original BinaryContext is valid
				LMLogger.logSevere(e, false);
			}
		}
		
		for (int i = 0; i < values.size(); i++) {
			Vector<String> currentValues = values.elementAt(i);
			for (int j = 0; j < currentValues.size(); j++) {
				String val = new String(currentValues.elementAt(j));
				try {
					newValCtx.setValueAt(val, i, j);
				} catch (InvalidTypeException e) {
					// Never reach because the original BinaryContext is valid
					LMLogger.logSevere(e, false);
				}
			}
		}
		
		return newValCtx;
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
		for (int i = 0; i < objects.size(); i++) {
			for (int j = 0; j < attributes.size(); j++) {
				String val = new String(getValueAt(i, j));
				str = str + val + " "; //$NON-NLS-1$
			}
			str = str + "\n"; //$NON-NLS-1$
		}
		
		return str;
	}

	@Override
	public void setFleche(String value, int objIdx, int attIdx) {
		// TODO Auto-generated method stub
		// TODO Utile?
	}
}