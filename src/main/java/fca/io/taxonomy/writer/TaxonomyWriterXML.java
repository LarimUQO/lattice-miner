package fca.io.taxonomy.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import fca.core.context.Context;
import fca.exception.WriterException;
import fca.io.taxonomy.TaxonomyWriter;

/**
 * Le standard TaxonomyWriter pour l'ecriture de taxonomie dans un fichier XML
 * @author Linda Bogni
 * @version 1.0
 */
public abstract class TaxonomyWriterXML extends TaxonomyWriter{

	/**
	 * Le Document XML en cours d'ecriture
	 */
	protected Document doc;
	private File sortie;
	
	/**
	 * Constructeur de l'ecriture de taxonomie abstraite pour un fichier XML
	 * @param taxonomyFile le fichier dans lequel ecrire
	 * @param taxonomy la taxonomie a ecrire
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	public TaxonomyWriterXML(File taxonomyFile, Context context) throws IOException, WriterException {
		super(taxonomyFile, context);
		sortie=taxonomyFile;
		writeTaxonomy();
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.taxonomy.TaxonomyWriter#writeTaxonomy()
	 */
	@Override
	protected void writeTaxonomy() throws IOException {
		
		// Créer le document de base
		doc = new Document();
		Element root = getRootElement();;
		// L'element "Data"
		if(root==null)
			return;

		doc.addContent(root);
		
		FileOutputStream fileOut = new FileOutputStream(sortie.getAbsolutePath());
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		out.output(doc, fileOut);
		fileOut.close();

		
		/*FileOutputStream fileOut = new FileOutputStream(taxonomyFile.getAbsolutePath());
		//On utilise ici un affichage classic avec getPrettyFormat()
		XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
		
		// Sérialise le DOM depuis le noeud "doc"
		sortie.output(doc, fileOut);
		fileOut.close();*/
		
	}
	
	/**
	 * Recupère les objets de la taxonomie et génère le noeud correspondant
	 */
	protected abstract Vector<Element> getRootObjectsElement();
	
	/**
	 * Recupère les attributs de la taxonomie et génère le noeud correspondant
	 */
	protected abstract Vector<Element> getRootAttributesElement();
	
	/**
	 * Recupère toute la txonomie et génère le noeud de la racine du fichier XML final
	 */
	protected abstract Element getRootElement();
	
}
