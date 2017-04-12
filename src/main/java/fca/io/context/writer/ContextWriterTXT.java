package fca.io.context.writer;

import java.io.File;
import java.io.IOException;

import fca.core.context.Context;
import fca.core.context.Context.HeaderType;
import fca.exception.WriterException;
import fca.io.context.ContextWriter;

/**
 * Le standard ContextWriter pour l'ecriture de contexte dans un fichier TXT
 * @author Ludovic Thomas
 * @version 1.0
 */
public abstract class ContextWriterTXT extends ContextWriter {

	/**
	 * L'entête correspondant au type du contexte
	 */
	protected HeaderType headerType;

	/**
	 * Constructeur de l'ecriture de contexte abstrait pour un fichier TXT
	 * @param contextFile le fichier dans lequel ecrire
	 * @param context le contexte a ecrire
	 * @param headerType le type du contexte a placer en entête du fichier
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	public ContextWriterTXT(File contextFile, Context context, HeaderType headerType) throws IOException, WriterException {
		super(contextFile, context);
		this.headerType = headerType;
		writeContext();
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriter#writeContext()
	 */
	@Override
	protected void writeContext() throws IOException {

		writeHeader();

		writeObjects();
		writeAttributes();
		writeData();
	}

	/**
	 * Ecrit l'entête du fichier correspondant au type du contexte
	 * @throws IOException si l'ecriture echoue
	 */
	protected void writeHeader() throws IOException {
		write(headerType.getValue() + "\n"); //$NON-NLS-1$
		flush();
	}

	/**
	 * Ecrit les objets du contexte
	 * @throws IOException si l'ecriture echoue
	 */
	protected abstract void writeObjects() throws IOException;

	/**
	 * Ecrit les attributs du contexte
	 * @throws IOException si l'ecriture echoue
	 */
	protected abstract void writeAttributes() throws IOException;

	/**
	 * Ecrit les données du contexte pour extraire la relation entre attributs et objets
	 * @throws IOException si l'ecriture echoue
	 */
	protected abstract void writeData() throws IOException;

}