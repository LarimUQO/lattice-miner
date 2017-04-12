package fca.io.taxonomy.writer.xml;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.jdom2.Element;
import java.text.Collator;
import java.util.Locale;

import fca.core.context.binary.BinaryContext;
import fca.exception.WriterException;
import fca.io.taxonomy.writer.TaxonomyWriterXML;

/**
 * La taxonomyWriter pour l'ecriture de taxonomie au format Lattice miner XML
 * @author Linda Bogni
 * @version 1.0
 */

public class LatticeXMLTaxonomyWriter extends TaxonomyWriterXML {
	/**
	 * Constructeur de l'ecriture de taxonomie de Lattice miner en XML
	 * @param file le fichier dans lequel ecrire
	 * @param binCtx le context binaire associe a la taxonomie a ecrire
	 * @throws IOException si le fichier ne peut être trouvé ou est corrompu
	 * @throws WriterException si une erreur d'ecriture arrive
	 */

	public LatticeXMLTaxonomyWriter(File file, BinaryContext binCtx) throws IOException, WriterException {
		super(file, binCtx);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.TaxonomyWriterXML#getAttributesElement()
	 */
	@Override
	protected Vector<Element> getRootAttributesElement() {

		Vector<Element> EltsExistants=new Vector<Element>();
		Vector<Element> TabEltsFinale=new Vector<Element>();

		// Instantiation d'un collator français
		Collator compareOperator = Collator.getInstance (Locale.FRENCH);
		// Comparaison strict, faire la difference entre les minuscules et les majuscules
		compareOperator.setStrength (Collator.TERTIARY);

		//System.out.println("taille exis0: "+EltsExistants.size());
		//System.out.println("taille tab0 : "+EltsExistants.size());

		int k;
		int trouve;
		for (int i = 0; i < context.getDonneesTaxonomieAttCount(); i++) {
			Element attribute = new Element("CONCEPT");

			//Nom et Expression du noeud
			String[] expression=new String[3];
			expression=context.getDonneesTaxonomieAttElement(i).split(" : ");
			//nom du noeud
			attribute.setAttribute("name", "" +expression[0]);

			Vector<String> elts=new Vector<String>();//contient les attributs(termes) du noeud
			if(expression[1].length()>4){
				int pos=1;
				//On traite l'expression de l'attribut généralisé
				while (expression[1].charAt(pos) == ' ')
					pos++;
				String deb=expression[1].substring(pos, pos+10);
				if(deb.compareToIgnoreCase("PERCENTAGE")==0){
					//System.out.println("per");
					pos+=11;
					//System.out.println("expression[1].length(): "+expression[1].length());
					while (pos < expression[1].length()-1){
						while (expression[1].charAt(pos) == ' '|| expression[1].substring(pos, pos+1).compareToIgnoreCase("]")==0
								|| expression[1].substring(pos, pos+1).compareToIgnoreCase("[")==0)
							pos++;
						elts.add(expression[1].substring(pos, pos+1));
						pos++;
					}
				}else{
					String op=expression[1].substring(pos+4, pos+6);//op=OR
					String expr=expression[1].substring(1);//expression sans le 1er espace au debut de la chaine
					String[] ensElts=new String[expr.length()];
					ensElts=expr.split(" OR ");
					if(op.equals("OR"))	{
						//System.out.println("or");
						for(int l=0;l<ensElts.length;l++){
							if(ensElts[l]==null) break;//sil ya plus dattributs afin deviter de parcourir tout le le reste du tableau vide
							elts.add(ensElts[l].substring(1, ensElts[l].length()-1));
						}

					}else{//AND
						//System.out.println("and");
						op=expression[1].substring(pos+4, pos+7);//op=AND
						ensElts=expr.split(" AND ");
						for(int l=0;l<ensElts.length;l++){
							if(ensElts[l]==null) break;//sil ya plus dattributs afin deviter de parcourir tout le le reste du tableau vide
							elts.add(ensElts[l].substring(1, ensElts[l].length()-1));
						}
					}
				}
			}else{//par ex, A : [a]
				elts.add(expression[1].substring(2, expression[1].length()-1));
			}
			//nb d'enfts du noeud
			attribute.setAttribute("childNb", "" +elts.size());
			for(int j=0;j<elts.size();j++){

				//System.out.println("enftsTax : "+elts.elementAt(j));
				//System.out.println("je construis le noeud");

				trouve = 0;
				for(k=0;k < EltsExistants.size();k++){
					if(elts.elementAt(j).equals(EltsExistants.elementAt(k).getAttributeValue("name"))){
						trouve=1;
						//System.out.println("je construis le noeud existant");
						//Si le nom de l'enfant  est connu,
						//On récupere l'enfant dans EltsExistants et on l'ajoute a attribute
						attribute.addContent((Element)EltsExistants.elementAt(k).clone());

						//Puis on met a jour TabEltsFinale
						for(int l=0; l < TabEltsFinale.size(); l++){
							if(elts.elementAt(j).compareTo(TabEltsFinale.elementAt(l).getAttributeValue("name"))==0){
								TabEltsFinale.removeElementAt(l);
							}
						}
						break;
					}
				}

				//Si l'élément n'est pas connu, on construit une nouvelle feuille
				if(trouve == 0 && k >= EltsExistants.size()){
					//System.out.println("je construis les noeuds");
					// L'element "CONCEPT" du noued principal
					Element att = new Element("CONCEPT");
					//att.setName(elts.elementAt(j));
					att.setAttribute("name", "" +elts.elementAt(j));
					// La valeur est 0 car ils ont pas d'enfts
					att.setText("0");
					//Ajout de l'enfant
					attribute.addContent(att);
					//break;
				}
				//System.out.println("fini le XML");
			}
			//On met a jour les tables
			EltsExistants.addElement(attribute);
			TabEltsFinale.addElement(attribute);
		}
		return TabEltsFinale;
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.ContextWriterXML#getObjectsElement()
	 */
	@Override
	protected Vector<Element> getRootObjectsElement() {

		Vector<Element> EltsExistants=new Vector<Element>();
		Vector<Element> TabEltsFinale=new Vector<Element>();

		// Instantiation d'un collator français
		Collator compareOperator = Collator.getInstance (Locale.FRENCH);
		// Comparaison strict, faire la difference entre les minuscules et les majuscules
		compareOperator.setStrength (Collator.TERTIARY);

		int k;
		int trouve;
		for (int i = 0; i < context.getDonneesTaxonomieObjCount(); i++) {
			Element attribute = new Element("CONCEPT");

			//Nom et Expression du noeud
			String[] expression=new String[3];
			expression=context.getDonneesTaxonomieObjElement(i).split(" : ");

			//nom du noeud
			attribute.setAttribute("name", "" +expression[0]);

			Vector<String> elts=new Vector<String>();//contient les attributs(termes) du noeud
			int pos=1;
			//On traite l'expression de l'attribut généralisé
			while (expression[1].charAt(pos) == ' ')
				pos++;
			String deb=expression[1].substring(pos, pos+10);
			if(deb.compareToIgnoreCase("PERCENTAGE")==0){
				//System.out.println("per");
				pos+=11;
				//System.out.println("expression[1].length(): "+expression[1].length());
				while (pos < expression[1].length()-1){
					while (expression[1].charAt(pos) == ' '|| expression[1].substring(pos, pos+1).compareToIgnoreCase("]")==0
							|| expression[1].substring(pos, pos+1).compareToIgnoreCase("[")==0)
						pos++;
					elts.add(expression[1].substring(pos, pos+1));
					pos++;
				}
			}else{
				String op=expression[1].substring(pos+4, pos+6);//op=OR
				String expr=expression[1].substring(1);//expression sans le 1er espace au debut de la chaine
				String[] ensElts=new String[expr.length()];
				ensElts=expr.split(" OR ");
				if(op.equals("OR"))	{
					//System.out.println("or");
					for(int l=0;l<ensElts.length;l++){
						if(ensElts[l]==null) break;//sil ya plus dattributs afin deviter de parcourir tout le le reste du tableau vide
						elts.add(ensElts[l].substring(1, ensElts[l].length()-1));
						//System.out.println("obj : "+ensElts[l].substring(1, ensElts[l].length()-1));
					}

				}else{//AND
					//System.out.println("and");
					op=expression[1].substring(pos+4, pos+7);//op=AND
					ensElts=expr.split(" AND ");
					for(int l=0;l<ensElts.length;l++){
						if(ensElts[l]==null) break;//sil ya plus dattributs afin deviter de parcourir tout le le reste du tableau vide
						elts.add(ensElts[l].substring(1, ensElts[l].length()-1));
					}
				}
			}

			//nb d'enfts du noeud
			attribute.setAttribute("childNb", "" +elts.size());
			for(int j=0;j<elts.size();j++){

				trouve = 0;
				for(k=0;k < EltsExistants.size();k++){
					if(elts.elementAt(j).equals(EltsExistants.elementAt(k).getAttributeValue("name"))){
						trouve=1;
						//System.out.println("je construis le noeud existant");
						//Si le nom de l'enfant  est connu,
						//On récupere l'enfant dans EltsExistants et on l'ajoute a attribute
						attribute.addContent((Element)EltsExistants.elementAt(k).clone());

						//Puis on met a jour TabEltsFinale
						for(int l=0; l < TabEltsFinale.size(); l++){
							if(elts.elementAt(j).compareTo(TabEltsFinale.elementAt(l).getAttributeValue("name"))==0){
								TabEltsFinale.removeElementAt(l);
							}
						}
						break;
					}
				}

				//Si l'élément n'est pas connu, on construit une nouvelle feuille
				if(trouve == 0 && k >= EltsExistants.size()){
					//System.out.println("je construis les noeuds");
					// L'element "CONCEPT" du noued principal
					Element att = new Element("CONCEPT");
					//att.setName(elts.elementAt(j));
					att.setAttribute("name", "" +elts.elementAt(j));
					// La valeur est 0 car ils ont pas d'enfts
					att.setText("0");
					//Ajout de l'enfant
					attribute.addContent(att);
					//break;
				}
				//System.out.println("fini le XML");
			}
			//On met a jour les tables
			EltsExistants.addElement(attribute);
			TabEltsFinale.addElement(attribute);
		}
		return TabEltsFinale;
	}

	/*
	 * (non-Javadoc)
	 * @see fca.io.TaxonomyWriterXML#getRootElement()
	 */
	@Override
	protected Element getRootElement() {

		//System.out.println("Jecris le XML");
		// Recupere le nom du fichier
		String fileNameWithExt = context.getName();
		int endIdx = fileNameWithExt.indexOf('.');
		String fileName = fileNameWithExt;
		if (endIdx != -1)
			fileName = fileNameWithExt.substring(0, endIdx);

		// L'element "TAX"
		Element tax = new Element("TAX"); //$NON-NLS-1$
		tax.setAttribute("name", fileName); //$NON-NLS-1$ //$NON-NLS-2$
		tax.setAttribute("type", " Context Generalization "); //$NON-NLS-1$ //$NON-NLS-2$

		//Mettre les donnees
		if(context.getTypeTax().equals("attributes")){
			for(int i=0; i< getRootAttributesElement().size();i++){
				tax.addContent((Element)getRootAttributesElement().elementAt(i).clone());
			}
		}else if(context.getTypeTax().equals("objects")){
			for(int i=0; i< getRootObjectsElement().size();i++){
				tax.addContent((Element)getRootObjectsElement().elementAt(i).clone());
			}
		}else{
			tax=null;
		}

		return tax;
	}

}