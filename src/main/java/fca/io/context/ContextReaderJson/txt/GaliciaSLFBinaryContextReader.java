package fca.io.context.ContextReaderJson.txt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

import fca.core.context.Context;
import fca.core.context.binary.BinaryContext;
import fca.exception.AlreadyExistsException;
import fca.exception.ReaderException;
import fca.exception.InvalidTypeException;
import fca.io.context.ContextReaderJson.ContextReaderTXT;
import fca.messages.IOMessages;

/**
 * Le LatticeReader pour la lecture de contexte binaire au format Galicia SLF
 * @author Ludovic Thomas
 * @version 1.0
 */
public class GaliciaSLFBinaryContextReader extends ContextReaderTXT {

	/**
	 * Le nombre d'objets du contexte (utilisé pour une vérification finale)
	 */
	protected int nbObjectsVerif;

	/**
	 * Le nombre d'attributs du contexte (utilisé pour une vérification finale)
	 */
	protected int nbAttributesVerif;

	/**
	 * Constructeur du lecteur de contexte binaire de Galicia en SLF
	 * @param file le fichier a lire
	 * @throws FileNotFoundException si le fichier ne peut être trouvé
	 * @throws ReaderException si une erreur de lecture arrive
	 */
	public GaliciaSLFBinaryContextReader(File file) throws FileNotFoundException, ReaderException {
		super(file, Context.HeaderType.SLF_BINARY);

		if (nbObjectsVerif != context.getObjectCount() || nbAttributesVerif != context.getAttributeCount())
			throw new ReaderException(IOMessages.getString("IO.WrongVerificationNumber")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReader#initContext()
	 */
	@Override
	protected void initContext() {
		context = new BinaryContext(""); //$NON-NLS-1$

		// Recupere les premieres lignes pour verifier a la fin nombre d'objets et d'attributs
		nbObjectsVerif = Integer.parseInt(reader.readLine());
		nbAttributesVerif = Integer.parseInt(reader.readLine());
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readObjects()
	 */
	@Override
	protected void readObjects() throws AlreadyExistsException {
		String objects = reader.readLine();
		if (objects.equals("[Objects]")) { //$NON-NLS-1$
			for (int i = 0; i < nbObjectsVerif; i++) {
				String object = reader.readLine();
				context.addObject(object);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readAttributes()
	 */
	@Override
	protected void readAttributes() throws AlreadyExistsException {
		String attributes = reader.readLine();
		if (attributes.equals("[Attributes]")) { //$NON-NLS-1$
			for (int i = 0; i < nbAttributesVerif; i++) {
				String attribute = reader.readLine();
				context.addAttribute(attribute);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextReaderTXT#readData()
	 */
	@Override
	protected void readData() throws ReaderException, InvalidTypeException {
		String relation = reader.readLine();
		if (relation.equals("[relation]")) { //$NON-NLS-1$
			for (int i = 0; i < context.getObjectCount(); i++) {
				String values = reader.readLine();

				StringTokenizer tok = new StringTokenizer(values);
				if (tok.countTokens() != context.getAttributeCount()) {
					throw new ReaderException(IOMessages.getString("IO.ReaderException")); //$NON-NLS-1$
				}

				int j = 0;
				while (tok.hasMoreTokens()) {
					String value = tok.nextToken();
					if (value.equals("1")) { //$NON-NLS-1$
						context.setValueAt(BinaryContext.TRUE, i, j);
					}
					j++;
				}
			}
		}
	}
}