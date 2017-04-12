package fca.io.context.ContextReaderJson;

import java.io.File;
import java.io.FileNotFoundException;

import fca.core.context.Context.HeaderType;
import fca.exception.AlreadyExistsException;
import fca.exception.ReaderException;
import fca.exception.InvalidTypeException;
import fca.io.FileLineReader;
import fca.io.context.ContextReader;
import fca.messages.IOMessages;

/**
 * Le standard ContextReader pour la lecture de contexte depuis un fichier TXT
 * @author Ludovic Thomas
 * @version 1.0
 */
public abstract class ContextReaderTXT extends ContextReader {
	
	/**
	 * Le lecteur de ligne en format TXT
	 */
	protected FileLineReader reader;
	
	/**
	 * L'entête correspondant au type du contexte
	 */
	protected HeaderType headerType;
	
	/**
	 * Constructeur du lecteur de contexte abstrait pour un format TXT
	 * @param contextFile le fichier a lire
	 * @param headerType l'entête que le fichier doit avoir pour être du bon type de contexte
	 * @throws FileNotFoundException si le fichier ne peut être trouvé
	 * @throws ReaderException si une erreur de lecture arrive
	 */
	public ContextReaderTXT(File contextFile, HeaderType headerType) throws FileNotFoundException, ReaderException {
		super(contextFile);
		this.headerType = headerType;
		this.reader = new FileLineReader(contextFile);
		readContext();
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.context.ContextReader#launchContextCreation()
	 */
	@Override
	protected void launchContextCreation() throws ReaderException {
		String fileType = reader.readLine();
		if (fileType.equals(headerType.getValue())) {
			createContext();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.context.ContextReader#createContext()
	 */
	@Override
	protected void createContext() throws ReaderException {
		
		initContext();
		
		try {
			// Read objects and attributes
			readObjects();
			readAttributes();
			
			// Read data
			readData();
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
	 */
	protected abstract void readObjects() throws AlreadyExistsException;
	
	/**
	 * Lit les attributs du contexte
	 * @throws AlreadyExistsException si une attribut est en double
	 */
	protected abstract void readAttributes() throws AlreadyExistsException;
	
	/**
	 * Lit les données du contexte pour extraire la relation entre attributs et objets
	 * @throws AlreadyExistsException si une relation est en double
	 * @throws InvalidTypeException si un mauvais type est lu dans le fichier
	 * @throws ContextReaderException si une erreur de lecture arrive
	 */
	protected abstract void readData() throws AlreadyExistsException, InvalidTypeException, ReaderException;
	
}