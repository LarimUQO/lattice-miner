package fca.io.context.ContextReaderJson;

import fca.exception.ReaderException;
import fca.io.context.ContextReader;

import java.io.File;
import java.io.FileNotFoundException;

public abstract class ContextReaderJson extends ContextReader{

	/**
	 * Constructeur du lecteur de contexte abstrait
	 *
	 * @param contextFile le fichier a lire
	 * @throws FileNotFoundException si le fichier ne peut etre trouve
	 * @throws ReaderException       si une erreur de lecture arrive
	 */
	public ContextReaderJson(File contextFile) throws FileNotFoundException, ReaderException {
		super(contextFile);
		readContext();
	}

	@Override
	protected void launchContextCreation() throws ReaderException {

	}

	@Override
	protected void createContext() throws ReaderException {

	}

	@Override
	protected void initContext() {

	}
}
