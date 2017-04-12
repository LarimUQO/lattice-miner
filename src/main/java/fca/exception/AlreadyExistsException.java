package fca.exception;

import fca.messages.ExceptionMessages;

/**
 * Exception lorsqu'un objet existe deja lors d'un ajout
 * @author Ludovic Thomas
 * @version 1.0
 */
public class AlreadyExistsException extends LatticeMinerException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 114944911579099502L;

	/**
	 * Constructeur
	 * @param moreInformation le message détaillé
	 */
	public AlreadyExistsException(String moreInformation) {
		super(moreInformation);
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.exception.LatticeMinerException#getMessageGeneral()
	 */
	@Override
	public String getMessageGeneral() {
		return ExceptionMessages.getString("AlreadyExistsException"); //$NON-NLS-1$
	}
}
