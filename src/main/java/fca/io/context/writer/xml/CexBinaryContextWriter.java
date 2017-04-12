package fca.io.context.writer.xml;

import java.io.File;
import java.io.IOException;

import org.jdom2.Element;

import fca.core.context.binary.BinaryContext;
import fca.core.util.BasicSet;
import fca.exception.WriterException;
import fca.io.context.writer.ContextWriterXML;

/**
 * Le LatticeWriter pour l'ecriture de contexte binaire au format Concept Explorer
 * @author Ludovic Thomas
 * @version 1.0
 */
public class CexBinaryContextWriter extends ContextWriterXML {
	
	/**
	 * Constructeur de l'ecriture de contexte binaire de Concept Explorer
	 * @param file le fichier dans lequel ecrire
	 * @param binCtx le contexte binaire a ecrire
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	public CexBinaryContextWriter(File file, BinaryContext binCtx) throws IOException, WriterException {
		super(file, binCtx);
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriterXML#getAttributesElement()
	 */
	@Override
	protected Element getAttributesElement() {
		
		Element attributes = new Element("Attributes"); //$NON-NLS-1$
		
		for (int i = 0; i < context.getAttributeCount(); i++) {
			
			// L'element "Attribute"
			Element attribute = new Element("Attribute"); //$NON-NLS-1$
			attribute.setAttribute("Identifier", "" + i); //$NON-NLS-1$ //$NON-NLS-2$
			
			// Le sous-element "Name"
			Element name = new Element("Name"); //$NON-NLS-1$
			name.setText(context.getAttributeAt(i));
			attribute.addContent(name);
			
			// L'ajout en tant que sous-element de "Attributes"
			attributes.addContent(attribute);
		}
		
		return attributes;
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriterXML#getObjectsElement()
	 */
	@Override
	protected Element getObjectsElement() {
		
		Element objects = new Element("Objects"); //$NON-NLS-1$
		
		for (int i = 0; i < context.getObjectCount(); i++) {
			
			// L'element "Object"
			Element object = new Element("Object"); //$NON-NLS-1$
			
			// Le sous-element "Name"
			Element name = new Element("Name"); //$NON-NLS-1$
			name.setText(context.getObjectAt(i));
			object.addContent(name);
			
			// Le sous-element "Intent"
			Element intent = new Element("Intent"); //$NON-NLS-1$
			BasicSet attObject = context.getAttributesFor(i);
			
			for (Object attName : attObject) {
				
				// Le sous-sous-element "HasAttribute"
				Element attribute = new Element("HasAttribute"); //$NON-NLS-1$
				int attIndex = context.getAttributeIndex((String) attName);
				attribute.setAttribute("AttributeIdentifier", "" + attIndex); //$NON-NLS-1$ //$NON-NLS-2$
				
				// L'ajout en tant que sous-element de "Intent"
				intent.addContent(attribute);
			}
			object.addContent(intent);
			
			// L'ajout en tant que sous-element de "Objects"
			objects.addContent(object);
		}
		
		return objects;
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriterXML#getDataElement()
	 */
	@Override
	protected Element getDataElement() {
		
		// Le sous-element "Contexts"
		Element contexts = new Element("Contexts"); //$NON-NLS-1$
		
		// Le sous-sous-element "Context"
		Element context = new Element("Context"); //$NON-NLS-1$
		context.setAttribute("Identifier", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		context.setAttribute("Type", "Binary"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// L'ajout des Attributs sous-element de "Context"
		context.addContent(getAttributesElement());
		
		// L'ajout des Objets sous-element de "Context"
		context.addContent(getObjectsElement());
		
		// L'ajout du Context sous-element de "Contexts"
		contexts.addContent(context);
		
		return contexts;
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriterXML#getRootElement()
	 */
	@Override
	protected Element getRootElement() {
		// L'element "ConceptualSystem"
		Element conceptualSystem = new Element("ConceptualSystem"); //$NON-NLS-1$
		
		// Le sous-element "Version"
		Element version = new Element("Version"); //$NON-NLS-1$
		version.setAttribute("MajorNumber", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		version.setAttribute("MinorNumber", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		conceptualSystem.addContent(version);
		
		// L'ajout de Contexts sous-element de "ConceptualSystem"
		conceptualSystem.addContent(getDataElement());
		
		return conceptualSystem;
	}
	
}