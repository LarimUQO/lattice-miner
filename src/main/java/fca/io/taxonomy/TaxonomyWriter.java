package fca.io.taxonomy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.tree.TreeNode;

import fca.core.context.Context;
import fca.exception.WriterException;

/**
 * Le standard TaxonomyWriter pour l'ecriture de taxonomie
 * @author Linda Bogni
 * @version 1.0
 */
public abstract class TaxonomyWriter extends FileWriter{

	/**
	 * La taxonomie qui doit être ecrite
	 */
	protected Context context;

	/**
	 * Le fichier dans lequel ecrire
	 */
	protected File taxonomyFile;

	/**
	 * L'arbre a construire
	 */
	protected TreeNode mTreeNode;

	/**
	 * Constructeur de l'ecriture de contexte abstrait
	 * @param taxonomyFile le fichier dans lequel ecrire
	 * @param context le contexte lie a la taxonomie a ecrire
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	public TaxonomyWriter(File taxonomyFile, Context c) throws IOException, WriterException {
		super(taxonomyFile);
		this.context = c;
		this.taxonomyFile = taxonomyFile;

	}

	/**
	 * Ecrit la taxonomie dans le fichier
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	protected abstract void writeTaxonomy() throws IOException, WriterException;

}
