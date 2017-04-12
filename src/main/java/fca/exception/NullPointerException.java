package fca.exception;

import fca.messages.ExceptionMessages;

/**
 * Exception lorsqu'un objet est <code>null</code>
 * @author Ludovic Thomas
 * @version 1.0
 */
public class NullPointerException extends LatticeMinerException {

	/**
	 *
	 */
	private static final long serialVersionUID = 7196105264470969379L;

	/**
	 * Constructeur
	 * @param moreInformation le message détaillé
	 */
	public NullPointerException(String moreInformation) {
		super(moreInformation);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.exception.LatticeMinerException#getMessageGeneral()
	 */
	@Override
	public String getMessageGeneral() {
		return ExceptionMessages.getString("NullPointerException"); //$NON-NLS-1$
	}
}
