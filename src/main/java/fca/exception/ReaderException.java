package fca.exception;

import fca.messages.ExceptionMessages;

/**
 * Exception lorsqu'une lecture de context echoue
 * @author Ludovic Thomas
 * @version 1.0
 */
public class ReaderException extends LatticeMinerException {

	/**
	 *
	 */
	private static final long serialVersionUID = 995421837111300068L;

	/**
	 * Constructeur
	 * @param moreInformation le message détaillé
	 */
	public ReaderException(String moreInformation) {
		super(moreInformation);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.exception.LatticeMinerException#getMessageGeneral()
	 */
	@Override
	public String getMessageGeneral() {
		return ExceptionMessages.getString("ReaderException"); //$NON-NLS-1$
	}
}
