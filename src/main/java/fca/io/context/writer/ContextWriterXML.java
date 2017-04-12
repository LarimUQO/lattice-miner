package fca.io.context.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import fca.core.context.Context;
import fca.exception.WriterException;
import fca.io.context.ContextWriter;

/**
 * Le standard ContextWriter pour l'ecriture de contexte dans un fichier XML
 * @author Ludovic Thomas
 * @version 1.0
 */
public abstract class ContextWriterXML extends ContextWriter {
	
	/**
	 * Le Document XML en cours d'ecriture
	 */
	protected Document doc;
	
	/**
	 * Constructeur de l'ecriture de contexte abstrait pour un fichier XML
	 * @param contextFile le fichier dans lequel ecrire
	 * @param context le contexte a ecrire
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	public ContextWriterXML(File contextFile, Context context) throws IOException, WriterException {
		super(contextFile, context);
		writeContext();
	}
	
	/*
	 * (non-Javadoc)
	 * @see fca.io.context.ContextWriter#writeContext()
	 */
	@Override
	protected void writeContext() throws IOException {
		
		// Créer le document de base
		doc = new Document();
		
		// L'element "Data"
		Element root = getRootElement();
		doc.addContent(root);
		
		//On utilise ici un affichage classic avec getPrettyFormat()
		XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
		
		// Sérialise le DOM depuis le noeud "doc"
		sortie.output(doc, new FileOutputStream(contextFile.getAbsolutePath()));
	}
	
	/**
	 * Recupère les objets du contexte et génère le noeud correspondant
	 */
	protected abstract Element getObjectsElement();
	
	/**
	 * Recupère les attributs du contexte et génère le noeud correspondant
	 */
	protected abstract Element getAttributesElement();
	
	/**
	 * Recupère les données du contexte et génère le noeud correspondant
	 */
	protected abstract Element getDataElement();
	
	/**
	 * Recupère tout le context et génère le noeud de la racine du fichier XML final
	 */
	protected abstract Element getRootElement();
	
}