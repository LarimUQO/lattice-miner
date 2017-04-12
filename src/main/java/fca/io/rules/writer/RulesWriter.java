/**
 * 
 */
package fca.io.rules.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JPanel;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import fca.core.rule.Rule;
import fca.gui.util.DialogBox;
import fca.messages.GUIMessages;

/**
 * Cette classe permet d'exporter la base des regles au format XML proprement en
 * utilisant l'api jdom
 * 
 * @author Yoann Montouchet
 */
public class RulesWriter extends FileWriter {
	static Element root = new Element("rules_base");
	static org.jdom2.Document document = new Document(root);

	private double minSupport;

	private double minConfidence;

	private String context_name;

	private static JPanel panel;
	
	private File sortie;
	
	private Vector<Rule> rules;

	/**
	 * Constructeur de l'exportateur XML
	 * 
	 * @param minSup
	 *            Le support minimal
	 * @param minConf
	 *            La confiance minimale
	 * @param contextName
	 *            Le nom du context
	 * @throws IOException 
	 */
	public RulesWriter(File file, Vector<Rule> rules, double minSup, double minConf, String contextName, JPanel panel) throws IOException {
		super(file);
		sortie = file;
		this.rules = rules;
		minSupport = minSup;
		minConfidence = minConf;
		context_name = contextName;
		RulesWriter.panel = panel;
		build();
	}

	/**
	 * Fonction qui genere le document XML
	 * 
	 * @param rules
	 *            l'ensemble des regles que l'on veut enregistrer
	 * @param filename
	 *            le nom du fichier XML de sortie
	 */
	public void build() {
		Element specs = new Element("specs");
		root.addContent(specs);

		Element contextName = new Element("context_name");
		contextName.setText(context_name);
		specs.addContent(contextName);

		Element minimal_support = new Element("minimal_support");
		minimal_support.setText("" + minSupport);
		specs.addContent(minimal_support);

		Element minimal_confidence = new Element("minimal_confidence");
		minimal_confidence.setText("" + minConfidence);
		specs.addContent(minimal_confidence);

		Element rules_base = new Element("rules");
		root.addContent(rules_base);

		Element rules_number = new Element("rules_number");
		rules_number.setText("" + rules.size());
		rules_base.addContent(rules_number);

		for (Rule rule : rules) {
			Element rules_element = new Element("rule");

			Element premise = new Element("premise");
			premise.setText("" + rule.getAntecedent());
			rules_element.addContent(premise);

			Element consequence = new Element("consequence");
			consequence.setText("" + rule.getConsequence());
			rules_element.addContent(consequence);

			Element support = new Element("support");
			support
					.setText(Double.toString(((double) ((int) (rule
							.getSupport() * 100.0))) / 100.0));
			rules_element.addContent(support);

			Element confidence = new Element("confidence");
			confidence.setText(Double.toString(((double) ((int) (rule
					.getConfidence() * 100.0))) / 100.0));
			rules_element.addContent(confidence);

			rules_base.addContent(rules_element);
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(sortie.getAbsolutePath());
			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
			out.output(document, fileOut);
			fileOut.close();
			DialogBox.showMessageInformation(panel, GUIMessages
					.getString("GUI.rulesHasBeenSuccessfullyExported"),
					GUIMessages.getString("GUI.saveSuccess"));
		} catch (java.io.IOException e) {
			System.err.println("IO Error : " + e);
		}
		
		// On efface proprement tous les elements enregistres dans root, dans le cas
		// ou on veut enregistrer un second fichier XML (sinon il garde en memoire 
		// la precedente sauvegarde et l'inclue lors des sauvegardes suivantes
		if (root.getContentSize() != 0) {
			for (int i = root.getContentSize() - 1; i >= 0; i--) {
				root.removeContent(i);
			}
		}
	}
}
