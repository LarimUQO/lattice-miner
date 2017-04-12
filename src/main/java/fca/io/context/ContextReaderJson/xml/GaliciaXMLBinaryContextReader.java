package fca.io.context.ContextReaderJson.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.jdom2.Element;

import fca.core.context.binary.BinaryContext;
import fca.exception.AlreadyExistsException;
import fca.exception.InvalidTypeException;
import fca.exception.ReaderException;
import fca.io.context.ContextReaderJson.ContextReaderXML;
import fca.messages.IOMessages;

/**
 * Le LatticeReader pour la lecture de contexte binaire au format Galicia XML
 * @author Ludovic Thomas
 * @version 1.0
 */
public class GaliciaXMLBinaryContextReader extends ContextReaderXML {

	/** L'attribut type de l'element BIN doit être BinaryRelation */
	private static final String TYPE_BIN = "BinaryRelation"; //$NON-NLS-1$

	/**
	 * Constructeur du lecteur de contexte binaire de Galicia en XML
	 * @param file le fichier a lire
	 * @throws FileNotFoundException si le fichier ne peut être trouvé
	 * @throws ReaderException si une erreur de lecture arrive
	 */
	public GaliciaXMLBinaryContextReader(File file) throws FileNotFoundException, ReaderException {
		super(file);

		String type = racine.getAttributeValue("type"); //$NON-NLS-1$
		String name = racine.getAttributeValue("name"); //$NON-NLS-1$
		int objectsCount = Integer.parseInt(racine.getAttributeValue("nbObj")); //$NON-NLS-1$
		int attributesCount = Integer.parseInt(racine.getAttributeValue("nbAtt")); //$NON-NLS-1$

		if (!type.equals(TYPE_BIN) || objectsCount != context.getObjectCount()
				|| attributesCount != context.getAttributeCount())
			throw new ReaderException(IOMessages.getString("IO.WrongVerificationNumber")); //$NON-NLS-1$

		// Recupere le nom du champ pour le nom du contexte
		int endIdx = name.indexOf('.');
		String contextName = name;
		if (endIdx != -1)
			contextName = name.substring(0, endIdx);
		context.setName(contextName);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReader#initContext()
	 */
	@Override
	protected void initContext() {
		context = new BinaryContext(""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readObjects()
	 */
	@SuppressWarnings({ "unchecked", "cast" }) //$NON-NLS-1$ //$NON-NLS-2$
	@Override
	protected void readObjectsElement() throws AlreadyExistsException {
		List<Element> objects = (List<Element>)racine.getChild("OBJS").getChildren("OBJ"); //$NON-NLS-1$ //$NON-NLS-2$
		for (Element object : objects) {
			String objectName = object.getText();
			context.addObject(objectName);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readAttributes()
	 */
	@SuppressWarnings({ "unchecked", "cast" }) //$NON-NLS-1$ //$NON-NLS-2$
	@Override
	protected void readAttributesElement() throws AlreadyExistsException {
		List<Element> attributes = (List<Element>)racine.getChild("ATTS").getChildren("ATT"); //$NON-NLS-1$ //$NON-NLS-2$
		for (Element attribute : attributes) {
			String attributeName = attribute.getText();
			context.addAttribute(attributeName);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readData()
	 */
	@SuppressWarnings({ "unchecked", "cast" }) //$NON-NLS-1$ //$NON-NLS-2$
	@Override
	protected void readDataElement() throws InvalidTypeException {
		List<Element> relations = (List<Element>)racine.getChild("RELS").getChildren("REL"); //$NON-NLS-1$ //$NON-NLS-2$
		for (Element relation : relations) {
			int objectNumber = Integer.parseInt(relation.getAttributeValue("idObj")); //$NON-NLS-1$
			int attributeNumber = Integer.parseInt(relation.getAttributeValue("idAtt")); //$NON-NLS-1$
			context.setValueAt(BinaryContext.TRUE, objectNumber, attributeNumber);
		}
	}
}