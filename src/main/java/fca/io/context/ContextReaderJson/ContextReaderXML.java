package fca.io.context.ContextReaderJson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import fca.exception.AlreadyExistsException;
import fca.exception.InvalidTypeException;
import fca.exception.ReaderException;
import fca.io.context.ContextReader;
import fca.messages.IOMessages;

/**
 * Le standard ContextReader pour la lecture de contexte depuis un fichier XML
 * @author Ludovic Thomas
 * @version 1.0
 */
public abstract class ContextReaderXML extends ContextReader {

	/**
	 * Le document en cours de lecture
	 */
	protected org.jdom2.Document doc;

	/**
	 * La racine du document
	 */
	protected Element racine;

	/**
	 * Constructeur du lecteur de contexte abstrait pour un format XML
	 * @param contextFile le fichier a lire
	 * @throws FileNotFoundException si le fichier ne peut être trouvé
	 * @throws ReaderException si une erreur de lecture arrive
	 */
	public ContextReaderXML(File contextFile) throws FileNotFoundException, ReaderException {
		super(contextFile);
		readContext();
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReader#launchContextCreation()
	 */
	@Override
	protected void launchContextCreation() throws ReaderException {

		try {
			// Instance du parseur SAXBuilder
			SAXBuilder sxb = new SAXBuilder();

			// Créer un nouveau document JDOM avec en argumant le fichier
			doc = sxb.build(contextFile);

		} catch (JDOMException e) {
			throw new ReaderException(IOMessages.getString("IO.ParserException")); //$NON-NLS-1$
		} catch (IOException e) {
			throw new ReaderException(IOMessages.getString("IO.ReaderException")); //$NON-NLS-1$
		}

		// Initialise la racine avec l'element racine du document.
		racine = doc.getRootElement();

		// Créer le contexte par lecture du document via la racine
		createContext();
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReader#createContext()
	 */
	@Override
	protected void createContext() throws ReaderException {

		initContext();

		try {
			// Read objects and attributes
			readObjectsElement();
			readAttributesElement();

			// Read data
			readDataElement();

		} catch (AlreadyExistsException e) {
			throw new ReaderException(IOMessages.getString("IO.AlreadyExistException")); //$NON-NLS-1$
		} catch (InvalidTypeException e) {
			throw new ReaderException(IOMessages.getString("IO.InvalidTypeException")); //$NON-NLS-1$
		} catch (ReaderException e) {
			throw new ReaderException(IOMessages.getString("IO.ReaderException")); //$NON-NLS-1$
		}
	}

	/**
	 * Lit les objets du contexte
	 * @throws AlreadyExistsException si une objet est en double
	 * @throws XPathExpressionException si une erreur de parsing survient a la lecture
	 */
	protected abstract void readObjectsElement() throws AlreadyExistsException;

	/**
	 * Lit les attributs du contexte
	 * @throws AlreadyExistsException si une attribut est en double
	 * @throws XPathExpressionException si une erreur de parsing survient a la lecture
	 */
	protected abstract void readAttributesElement() throws AlreadyExistsException;

	/**
	 * Lit les données du contexte pour extraire la relation entre attributs et objets
	 * @throws InvalidTypeException si un mauvais type est lu dans le fichier
	 * @throws ReaderException si une erreur de lecture arrive
	 * @throws XPathExpressionException si une erreur de parsing survient a la lecture
	 */
	protected abstract void readDataElement() throws InvalidTypeException, ReaderException;

}