package fca.io.context.ContextReaderJson.txt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;
import java.util.Vector;

import fca.core.context.Context;
import fca.core.context.binary.BinaryContext;
import fca.core.context.nested.NestedContext;
import fca.exception.AlreadyExistsException;
import fca.exception.ReaderException;
import fca.exception.InvalidTypeException;
import fca.io.context.ContextReaderJson.ContextReaderTXT;
import fca.messages.IOMessages;

/**
 * Le LatticeReader pour la lecture de contexte imbriqué au format Lattice Miner
 * @author Ludovic Thomas
 * @version 1.0
 */
public class LMNestedContextReader extends ContextReaderTXT {
	
	/** La liste des objets du contexte */
	private Vector<String> objectList;
	
	/**
	 * Constructeur du lecteur de contexte imbriqué de Lattice Miner
	 * @param file le fichier a lire
	 * @throws FileNotFoundException si le fichier ne peut être trouvé
	 * @throws ReaderException si une erreur de lecture arrive
	 */
	public LMNestedContextReader(File file) throws FileNotFoundException, ReaderException {
		super(file, Context.HeaderType.LM_NESTED);
		
		// Rechange le nom du contexte car Nested
		String fileName = contextFile.getName();
		int endIdx = fileName.indexOf('.');
		String contextName = fileName.substring(0, endIdx);
		((NestedContext) context).setNestedContextName(contextName);
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReader#initContext()
	 */
	@Override
	protected void initContext() {
		context = null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readObjects(fca.context.Context)
	 */
	@Override
	protected void readObjects() {
		String objects = reader.readLine();
		objectList = new Vector<String>();
		StringTokenizer tok = new StringTokenizer(objects, "|"); //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			objectList.add(tok.nextToken().trim());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readAttributes(fca.context.Context)
	 */
	@Override
	protected void readAttributes() {
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readData(fca.context.Context)
	 */
	@Override
	protected void readData() throws ReaderException, AlreadyExistsException, InvalidTypeException {
		int level = 0;
		String attributes;
		while ((attributes = reader.readLine()) != null && (attributes.trim().length() > 0)) {
			level++;
			
			BinaryContext binCtx = new BinaryContext("Level " + level); //$NON-NLS-1$
			for (int i = 0; i < objectList.size(); i++) {
				binCtx.addObject(objectList.elementAt(i));
			}
			
			StringTokenizer tok = new StringTokenizer(attributes, "|"); //$NON-NLS-1$
			while (tok.hasMoreTokens()) {
				binCtx.addAttribute(tok.nextToken().trim());
			}
			
			for (int i = 0; i < binCtx.getObjectCount(); i++) {
				String values = reader.readLine();
				
				tok = new StringTokenizer(values);
				if (tok.countTokens() != binCtx.getAttributeCount()) {
					throw new ReaderException(IOMessages.getString("IO.ReaderException")); //$NON-NLS-1$
				}
				
				int j = 0;
				while (tok.hasMoreTokens()) {
					String value = tok.nextToken();
					if (value.equals("1")) { //$NON-NLS-1$
						binCtx.setValueAt(BinaryContext.TRUE, i, j);
					}
					j++;
				}
			}
			
			if (context == null)
				context = new NestedContext(binCtx);
			else
				((NestedContext) context).addNextContext(new NestedContext(binCtx));
		}
	}
}