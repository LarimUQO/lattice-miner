package fca.exception;

import fca.messages.ExceptionMessages;

/**
 * Exception lorsqu'une valeur a un mauvais type
 * @author Ludovic Thomas
 * @version 1.0
 */
public class InvalidTypeException extends LatticeMinerException {

	/**
	 *
	 */
	private static final long serialVersionUID = -3288628536610120078L;

	/**
	 * Constructeur
	 * @param moreInformation le message détaillé
	 */
	public InvalidTypeException(String moreInformation) {
		super(moreInformation);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.exception.LatticeMinerException#getMessageGeneral()
	 */
	@Override
	public String getMessageGeneral() {
		return ExceptionMessages.getString("InvalidTypeException"); //$NON-NLS-1$
	}
}
