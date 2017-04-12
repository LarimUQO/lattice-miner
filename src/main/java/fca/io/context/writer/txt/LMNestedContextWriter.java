package fca.io.context.writer.txt;

import java.io.File;
import java.io.IOException;

import fca.core.context.Context;
import fca.core.context.binary.BinaryContext;
import fca.core.context.nested.NestedContext;
import fca.exception.WriterException;
import fca.io.context.writer.ContextWriterTXT;

/**
 * Le LatticeWriter pour l'ecriture de contexte imbriqué au format Lattice Miner
 * @author Ludovic Thomas
 * @version 1.0
 */
public class LMNestedContextWriter extends ContextWriterTXT {
	
	/**
	 * Constructeur de l'ecriture de contexte imbriqué de Lattice Miner
	 * @param file le fichier dans lequel ecrire
	 * @param nesCtx le contexte binaire a ecrire
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	public LMNestedContextWriter(File file, NestedContext nesCtx) throws IOException, WriterException {
		super(file, nesCtx, Context.HeaderType.LM_NESTED);
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriter#writeObjects()
	 */
	@Override
	protected void writeObjects() throws IOException {
		for (int i = 0; i < context.getObjectCount(); i++) {
			write("| " + context.getObjectAt(i) + " "); //$NON-NLS-1$ //$NON-NLS-2$
			flush();
		}
		write("\n"); //$NON-NLS-1$
		flush();
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriter#writeAttributes()
	 */
	@Override
	protected void writeAttributes() {
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriter#writeData()
	 */
	@Override
	protected void writeData() throws IOException {
		NestedContext currentContext = (NestedContext) context;
		while (currentContext != null) {
			for (int i = 0; i < currentContext.getAttributeCount(); i++) {
				write("| " + currentContext.getAttributeAt(i) + " "); //$NON-NLS-1$ //$NON-NLS-2$
				flush();
			}
			write("\n"); //$NON-NLS-1$
			flush();
			
			for (int i = 0; i < currentContext.getObjectCount(); i++) {
				for (int j = 0; j < currentContext.getAttributeCount(); j++) {
					if (currentContext.getValueAt(i, j) == BinaryContext.TRUE)
						write("1 "); //$NON-NLS-1$
					else
						write("0 "); //$NON-NLS-1$
					flush();
				}
				write("\n"); //$NON-NLS-1$
				flush();
			}
			
			currentContext = currentContext.getNextContext();
		}
	}
	
}