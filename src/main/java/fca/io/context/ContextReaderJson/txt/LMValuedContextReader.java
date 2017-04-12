package fca.io.context.ContextReaderJson.txt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

import fca.core.context.Context;
import fca.core.context.valued.ValuedContext;
import fca.exception.AlreadyExistsException;
import fca.exception.ReaderException;
import fca.exception.InvalidTypeException;
import fca.io.context.ContextReaderJson.ContextReaderTXT;
import fca.messages.IOMessages;

/**
 * Le LatticeReader pour la lecture de contexte valué au format Lattice Miner
 * @author Ludovic Thomas
 * @version 1.0
 */
public class LMValuedContextReader extends ContextReaderTXT {
	
	/**
	 * Constructeur du lecteur de contexte valué de Lattice Miner
	 * @param file le fichier a lire
	 * @throws FileNotFoundException si le fichier ne peut être trouvé
	 * @throws ReaderException si une erreur de lecture arrive
	 */
	public LMValuedContextReader(File file) throws FileNotFoundException, ReaderException {
		super(file, Context.HeaderType.LM_VALUED);
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReader#initContext()
	 */
	@Override
	protected void initContext() {
		context = new ValuedContext(""); //$NON-NLS-1$
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readObjects(fca.context.Context)
	 */
	@Override
	protected void readObjects() throws AlreadyExistsException {
		String objects = reader.readLine();
		StringTokenizer tok = new StringTokenizer(objects, "|"); //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			context.addObject(tok.nextToken().trim());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readAttributes(fca.context.Context)
	 */
	@Override
	protected void readAttributes() throws AlreadyExistsException {
		String attributes = reader.readLine();
		StringTokenizer tok = new StringTokenizer(attributes, "|"); //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			context.addAttribute(tok.nextToken().trim());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readData(fca.context.Context)
	 */
	@Override
	protected void readData() throws InvalidTypeException, ReaderException {
		for (int i = 0; i < context.getObjectCount(); i++) {
			String values = reader.readLine();
			StringTokenizer tok = new StringTokenizer(values, "|"); //$NON-NLS-1$
			if (tok.countTokens() != context.getAttributeCount()) {
				throw new ReaderException(IOMessages.getString("IO.ReaderException")); //$NON-NLS-1$
			}
			
			int j = 0;
			while (tok.hasMoreTokens()) {
				String value = tok.nextToken().trim();
				if (value.length() > 0) {
					context.setValueAt(value, i, j);
				}
				j++;
			}
		}
	}
	
}