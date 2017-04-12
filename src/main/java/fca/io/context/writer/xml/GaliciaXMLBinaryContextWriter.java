package fca.io.context.writer.xml;

import java.io.File;
import java.io.IOException;

import org.jdom2.Element;

import fca.core.context.binary.BinaryContext;
import fca.core.util.BasicSet;
import fca.exception.WriterException;
import fca.io.context.writer.ContextWriterXML;

/**
 * Le LatticeWriter pour l'ecriture de contexte binaire au format Galicia XML
 * @author Ludovic Thomas
 * @version 1.0
 */
public class GaliciaXMLBinaryContextWriter extends ContextWriterXML {

	/**
	 * Constructeur de l'ecriture de contexte binaire de Galicia en XML
	 * @param file le fichier dans lequel ecrire
	 * @param binCtx le contexte binaire a ecrire
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	public GaliciaXMLBinaryContextWriter(File file, BinaryContext binCtx) throws IOException, WriterException {
		super(file, binCtx);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriterXML#getAttributesElement()
	 */
	@Override
	protected Element getAttributesElement() {

		Element attributes = new Element("ATTS"); //$NON-NLS-1$

		for (int i = 0; i < context.getAttributeCount(); i++) {

			// L'element "ATT"
			Element attribute = new Element("ATT"); //$NON-NLS-1$
			attribute.setAttribute("id", "" + i); //$NON-NLS-1$ //$NON-NLS-2$

			// La valeur est le nom
			attribute.setText(context.getAttributeAt(i));

			// L'ajout en tant que sous-element de "ATTS"
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

		Element objects = new Element("OBJS"); //$NON-NLS-1$

		for (int i = 0; i < context.getObjectCount(); i++) {

			// L'element "OBJ"
			Element object = new Element("OBJ"); //$NON-NLS-1$
			object.setAttribute("id", "" + i); //$NON-NLS-1$ //$NON-NLS-2$

			// La valeur est le nom
			object.setText(context.getObjectAt(i));

			// L'ajout en tant que sous-element de "OBJS"
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

		Element relations = new Element("RELS"); //$NON-NLS-1$

		for (int i = 0; i < context.getObjectCount(); i++) {

			BasicSet attObject = context.getAttributesFor(i);
			for (String attName : attObject) {

				int attIndex = context.getAttributeIndex(attName);

				// L'element "REL"
				Element relation = new Element("REL"); //$NON-NLS-1$
				relation.setAttribute("idObj", "" + i); //$NON-NLS-1$ //$NON-NLS-2$
				relation.setAttribute("idAtt", "" + attIndex); //$NON-NLS-1$ //$NON-NLS-2$

				// L'ajout en tant que sous-element de "RELS"
				relations.addContent(relation);
			}
		}

		return relations;
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriterXML#getRootElement()
	 */
	@Override
	protected Element getRootElement() {

		// Recupere le nom du fichier
		String fileNameWithExt = contextFile.getName();
		int endIdx = fileNameWithExt.indexOf('.');
		String fileName = fileNameWithExt;
		if (endIdx != -1)
			fileName = fileNameWithExt.substring(0, endIdx);

		// L'element "BIN"
		Element bin = new Element("BIN"); //$NON-NLS-1$
		bin.setAttribute("name", fileName + ".slf"); //$NON-NLS-1$ //$NON-NLS-2$
		bin.setAttribute("nbObj", "" + context.getObjectCount()); //$NON-NLS-1$ //$NON-NLS-2$
		bin.setAttribute("nbAtt", "" + context.getAttributeCount()); //$NON-NLS-1$ //$NON-NLS-2$
		bin.setAttribute("type", "BinaryRelation"); //$NON-NLS-1$ //$NON-NLS-2$

		// L'ajout de OBJS sous-element de "BIN"
		bin.addContent(getObjectsElement());

		// L'ajout de ATTS sous-element de "BIN"
		bin.addContent(getAttributesElement());

		// L'ajout de RELS sous-element de "BIN"
		bin.addContent(getDataElement());

		return bin;
	}

}