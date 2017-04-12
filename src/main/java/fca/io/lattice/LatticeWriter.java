package fca.io.lattice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import fca.core.lattice.ConceptLattice;
import fca.exception.WriterException;

/**
 * Le standard LatticeWriter pour l'ecriture de treillis dans un fichier XML
 * @author Ludovic Thomas
 * @version 1.0
 */
public abstract class LatticeWriter extends FileWriter {
	
	/**
	 * Le treillis qui doit etre ecrit
	 */
	protected ConceptLattice lattice;
	
	/**
	 * Le fichier dans lequel ecrire
	 */
	protected File latticeFile;
	
	/**
	 * Constructeur de l'ecriture de treillis abstrait
	 * @param contextFile le fichier dans lequel ecrire
	 * @param lattice le treillis a ecrire
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	public LatticeWriter(File contextFile, ConceptLattice lattice) throws IOException, WriterException {
		super(contextFile);
		this.lattice = lattice;
		this.latticeFile = contextFile;
	}
	
	/**
	 * Ecrit le treillis dans le fichier
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	protected abstract void writeLattice() throws IOException, WriterException;
	
}