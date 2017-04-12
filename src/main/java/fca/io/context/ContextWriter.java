package fca.io.context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import fca.core.context.Context;
import fca.exception.WriterException;

/**
 * Le standard ContextWriter pour l'ecriture de contexte
 * @author Ludovic Thomas
 * @version 1.0
 */
public abstract class ContextWriter extends FileWriter {
	
	/**
	 * Le contexte qui doit être ecrit
	 */
	protected Context context;
	
	/**
	 * Le fichier dans lequel ecrire
	 */
	protected File contextFile;
	
	/**
	 * Constructeur de l'ecriture de contexte abstrait
	 * @param contextFile le fichier dans lequel ecrire
	 * @param context le contexte a ecrire
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	public ContextWriter(File contextFile, Context context) throws IOException, WriterException {
		super(contextFile);
		this.context = context;
		this.contextFile = contextFile;
		
		context.setContextFile(contextFile);
		context.setModified(false);
	}
	
	/**
	 * Ecrit le contexte dans le fichier
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	protected abstract void writeContext() throws IOException, WriterException;
	
}