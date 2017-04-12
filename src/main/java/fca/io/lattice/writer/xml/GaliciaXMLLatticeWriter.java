package fca.io.lattice.writer.xml;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.jdom2.Element;

import fca.core.lattice.ConceptLattice;
import fca.core.lattice.FormalConcept;
import fca.exception.WriterException;
import fca.io.lattice.writer.LatticeWriterXML;

/**
 * Le LatticeWriter pour l'ecriture de treillis au format Galicia XML
 * @author Ludovic Thomas
 * @version 1.0
 */
public class GaliciaXMLLatticeWriter extends LatticeWriterXML {

	/**
	 * Constructeur de l'ecriture de contexte pour Galicia en XML
	 * @param file le fichier dans lequel ecrire
	 * @param lattice le treillis a ecrire
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */
	public GaliciaXMLLatticeWriter(File file, ConceptLattice lattice) throws IOException, WriterException {
		super(file, lattice);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriterXML#getAttributesElement()
	 */
	@Override
	protected Element getAttributesElement() {

		Element attributes = new Element("ATTS"); //$NON-NLS-1$

		for (int i = 0; i < lattice.getContext().getAttributeCount(); i++) {

			// L'element "ATT"
			Element attribute = new Element("ATT"); //$NON-NLS-1$
			attribute.setAttribute("id", "" + i); //$NON-NLS-1$ //$NON-NLS-2$

			// La valeur est le nom
			attribute.setText(lattice.getContext().getAttributeAt(i));

			// L'ajout en tant que sous-element de "ATTS"
			attributes.addContent(attribute);
		}

		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriterXML#getObjectsElement()
	 */
	@Override
	protected Element getObjectsElement() {

		Element objects = new Element("OBJS"); //$NON-NLS-1$

		for (int i = 0; i < lattice.getContext().getObjectCount(); i++) {

			// L'element "OBJ"
			Element object = new Element("OBJ"); //$NON-NLS-1$
			object.setAttribute("id", "" + i); //$NON-NLS-1$ //$NON-NLS-2$

			// La valeur est le nom
			object.setText(lattice.getContext().getObjectAt(i));

			// L'ajout en tant que sous-element de "OBJS"
			objects.addContent(object);
		}

		return objects;
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriterXML#getDataElement()
	 */
	@Override
	protected Element getDataElement() {

		Element nods = new Element("NODS"); //$NON-NLS-1$

		// Recupère les concepts du treillis et les clone car on les manipulent (suppriment,...)
		Vector<FormalConcept> nodes = new Vector<FormalConcept>(lattice.getConcepts());
		Vector<FormalConcept> alreadyPresents = new Vector<FormalConcept>();
		int nodeNum = 0;

		// Tant que l'on a pas rajouté tous les concepts du treillis
		while (!nodes.isEmpty()) {

			// Récupère le premier concept et le supprime
			FormalConcept node = nodes.remove(0);

			// Si les parents de ce concept ne sont pas encore traités
			if (!alreadyPresents.containsAll(node.getParents())) {
				// Rajoute le concept à la fin du vecteur
				nodes.add(node);
			}
			// Sinon on le traite
			else {

				// On l'ajoute aux concepts traités
				alreadyPresents.add(node);

				Element nod = new Element("NOD"); //$NON-NLS-1$
				nod.setAttribute("id", "" + nodeNum); //$NON-NLS-1$ //$NON-NLS-2$
				nodeNum++;

				// Extension
				Element extension = new Element("EXT"); //$NON-NLS-1$

				for (String extString : node.getExtent()) {

					// L'element "OBJ"
					Element object = new Element("OBJ"); //$NON-NLS-1$
					object.setAttribute("id", "" + lattice.getContext().getObjectIndex(extString)); //$NON-NLS-1$ //$NON-NLS-2$

					// L'ajout en tant que sous-element de "EXT"
					extension.addContent(object);
				}

				nod.addContent(extension);

				// Intention
				Element intention = new Element("INT"); //$NON-NLS-1$

				for (String intString : node.getIntent()) {

					// L'element "ATT"
					Element attribute = new Element("ATT"); //$NON-NLS-1$
					attribute.setAttribute("id", "" + lattice.getContext().getAttributeIndex(intString)); //$NON-NLS-1$ //$NON-NLS-2$

					// L'ajout en tant que sous-element de "INT"
					intention.addContent(attribute);
				}

				nod.addContent(intention);

				// Sup_Nod
				Element supNod = new Element("SUP_NOD"); //$NON-NLS-1$

				for (FormalConcept nodeParent : node.getParents()) {

					// L'element "PARENT"
					Element parent = new Element("PARENT"); //$NON-NLS-1$
					int parentNum = alreadyPresents.indexOf(nodeParent);
					parent.setAttribute("id", "" + parentNum); //$NON-NLS-1$ //$NON-NLS-2$

					// L'ajout en tant que sous-element de "SUP_NOD"
					supNod.addContent(parent);
				}

				nod.addContent(supNod);

				nods.addContent(nod);
			}
		}

		return nods;
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriterXML#getRootElement()
	 */
	@Override
	protected Element getRootElement() {

		// Recupere le nom du fichier
		String fileNameWithExt = latticeFile.getName();
		int endIdx = fileNameWithExt.indexOf('.');
		String fileName = fileNameWithExt;
		if (endIdx != -1)
			fileName = fileNameWithExt.substring(0, endIdx);

		// L'element "LAT"
		Element lat = new Element("LAT"); //$NON-NLS-1$
		lat.setAttribute("Desc", lattice.getName() + " - " + fileName + ".slf" + " - #OfNodes = " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ lattice.getConcepts().size());
		lat.setAttribute("type", "ConceptLattice"); //$NON-NLS-1$ //$NON-NLS-2$

		// L'ajout de MINSUPP sous-element de "LAT"
		Element minsupp = new Element("MINSUPP"); //$NON-NLS-1$
		minsupp.setText("0.0"); //$NON-NLS-1$
		lat.addContent(minsupp);

		// L'ajout de OBJS sous-element de "LAT"
		lat.addContent(getObjectsElement());

		// L'ajout de ATTS sous-element de "LAT"
		lat.addContent(getAttributesElement());

		// L'ajout de NODS sous-element de "LAT"
		lat.addContent(getDataElement());

		return lat;
	}

}