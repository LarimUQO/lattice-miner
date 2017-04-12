package fca.io.context.ContextReaderJson.json;

import fca.exception.ReaderException;
import fca.io.context.ContextReaderJson.ContextReaderJson;

import java.io.File;
import java.io.FileNotFoundException;

public class TriadicJsonContext extends ContextReaderJson {

	/**
	 * Constructeur du lecteur de contexte abstrait
	 *
	 * @param contextFile le fichier a lire
	 * @throws FileNotFoundException si le fichier ne peut etre trouve
	 * @throws ReaderException       si une erreur de lecture arrive
	 */
	public TriadicJsonContext(File contextFile) throws FileNotFoundException, ReaderException {
		super(contextFile);
	}
}
