package fca.io.context.writer.txt;

import java.io.File;
import java.io.IOException;

import fca.core.context.Context;
import fca.core.context.binary.BinaryContext;
import fca.exception.WriterException;
import fca.io.context.writer.ContextWriterTXT;

/**
 * Le LatticeWriter pour l'ecriture de contexte binaire au format Galicia SLF
 * @author Ludovic Thomas
 * @version 1.0
 */
public class GaliciaSLFBinaryContextWriter extends ContextWriterTXT {
	
	/**
	 * Constructeur de l'ecriture de contexte binaire de Galicia en SLF
	 * @param file le fichier dans lequel ecrire
	 * @param binCtx le contexte binaire a ecrire
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	public GaliciaSLFBinaryContextWriter(File file, BinaryContext binCtx) throws IOException, WriterException {
		super(file, binCtx, Context.HeaderType.SLF_BINARY);
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriterTXT#writeHeader()
	 */
	@Override
	protected void writeHeader() throws IOException {
		super.writeHeader();
		write(context.getObjectCount() + "\n"); //$NON-NLS-1$
		flush();
		write(context.getAttributeCount() + "\n"); //$NON-NLS-1$
		flush();
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriter#writeObjects()
	 */
	@Override
	protected void writeObjects() throws IOException {
		write("[Objects]\n"); //$NON-NLS-1$
		flush();
		for (int i = 0; i < context.getObjectCount(); i++) {
			write(context.getObjectAt(i) + "\n"); //$NON-NLS-1$
			flush();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriter#writeAttributes()
	 */
	@Override
	protected void writeAttributes() throws IOException {
		write("[Attributes]\n"); //$NON-NLS-1$
		flush();
		for (int i = 0; i < context.getAttributeCount(); i++) {
			write(context.getAttributeAt(i) + "\n"); //$NON-NLS-1$
			flush();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriter#writeData()
	 */
	@Override
	protected void writeData() throws IOException {
		write("[relation]\n"); //$NON-NLS-1$
		flush();
		for (int i = 0; i < context.getObjectCount(); i++) {
			for (int j = 0; j < context.getAttributeCount(); j++) {
				if (context.getValueAt(i, j) == BinaryContext.TRUE)
					write("1 "); //$NON-NLS-1$
				else
					write("0 "); //$NON-NLS-1$
				flush();
			}
			write("\n"); //$NON-NLS-1$
			flush();
		}
	}
	
}