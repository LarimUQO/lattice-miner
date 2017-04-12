package fca.exception;

/**
 * Represente le minimum d'une Exception pour Lattice Miner a savoir un message général et un
 * detaillé
 * @author Ludovic Thomas
 * @version 1.0
 */
public abstract class LatticeMinerException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -6989918176523155011L;
	/**
	 * Precisions sur le message
	 */
	private String moreInformation;

	/**
	 * Constructeur
	 * @param moreInformation le message détaillé
	 */
	public LatticeMinerException(String moreInformation) {
		this.moreInformation = moreInformation;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return getMessageGeneral() + "\n" + moreInformation; //$NON-NLS-1$
	}

	/**
	 * @return the general message
	 */
	public abstract String getMessageGeneral();
}
