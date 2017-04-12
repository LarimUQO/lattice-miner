package fca.exception;

import fca.messages.ExceptionMessages;

/**
 * Exception lorsqu'une ecriture de fichier echoue
 * @author Ludovic Thomas
 * @version 1.0
 */
public class WriterException extends LatticeMinerException {

	/**
	 *
	 */
	private static final long serialVersionUID = -1818221078995429511L;

	/**
	 * Constructeur
	 * @param moreInformation le message détaillé
	 */
	public WriterException(String moreInformation) {
		super(moreInformation);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.exception.LatticeMinerException#getMessageGeneral()
	 */
	@Override
	public String getMessageGeneral() {
		return ExceptionMessages.getString("WriterException"); //$NON-NLS-1$
	}
}
