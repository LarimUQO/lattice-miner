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

/**
 * Le LatticeReader pour la lecture de contexte binaire au format Concept Explorer
 * @author Ludovic Thomas
 * @version 1.0
 */
public class CexBinaryContextReader extends ContextReaderXML {
	
	/**
	 * Constructeur du lecteur de contexte binaire de Concept Explorer
	 * @param file le fichier a lire
	 * @throws FileNotFoundException si le fichier ne peut être trouvé
	 * @throws ReaderException si une erreur de lecture arrive
	 */
	public CexBinaryContextReader(File file) throws FileNotFoundException, ReaderException {
		super(file);
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReader#initContext()
	 */
	@Override
	protected void initContext() {
		context = new BinaryContext(""); //$NON-NLS-1$
		racine = (Element)racine.getChild("Contexts").getChildren("Context").get(0); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readObjects()
	 */
	@SuppressWarnings({ "unchecked", "cast" }) //$NON-NLS-1$ //$NON-NLS-2$
	@Override
	protected void readObjectsElement() throws AlreadyExistsException {
		List<Element> objects = (List<Element>)racine.getChild("Objects").getChildren("Object"); //$NON-NLS-1$ //$NON-NLS-2$
		for (Element object : objects) {
			String objectName = object.getChildText("Name"); //$NON-NLS-1$
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
		List<Element> attributes = (List<Element>)racine.getChild("Attributes").getChildren("Attribute"); //$NON-NLS-1$ //$NON-NLS-2$
		for (Element attribute : attributes) {
			String attributeName = attribute.getChildText("Name"); //$NON-NLS-1$
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
		List<Element> objects = (List<Element>)racine.getChild("Objects").getChildren("Object"); //$NON-NLS-1$ //$NON-NLS-2$
		for (Element object : objects) {
			List<Element> intents = (List<Element>)object.getChild("Intent").getChildren("HasAttribute"); //$NON-NLS-1$ //$NON-NLS-2$
			for (Element intent : intents) {
				int attributeNumber = Integer.parseInt(intent.getAttributeValue("AttributeIdentifier")); //$NON-NLS-1$
				context.setValueAt(BinaryContext.TRUE, objects.indexOf(object), attributeNumber);
			}
		}
	}
}