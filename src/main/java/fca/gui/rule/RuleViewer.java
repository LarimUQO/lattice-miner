package fca.gui.rule;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import fca.core.rule.Rule;
import fca.gui.lattice.LatticePanel;
import fca.gui.util.DialogBox;
import fca.gui.util.XML_Filter;
import fca.io.rules.writer.RulesWriter;
import fca.messages.GUIMessages;

public class RuleViewer extends JFrame {
	private static final long serialVersionUID = 1600664354577165948L;
	private LatticePanel latticePanel;
	private String contextName;
	private JPanel rulePanel;
	private JButton xmlButton = null;
	private JScrollPane ruleScrollPane;
	private RuleTable ruleTable;
	private double minSupport;
	private double minConfidence;
	private Vector<Rule> rules;

	public RuleViewer(Vector<Rule> ruleSet, String name, double minSupp,
			double minConf, LatticePanel latPanel) {
		setTitle(GUIMessages.getString("GUI.associationRuleViewer"));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		rules = ruleSet;
		contextName = name;
		minSupport = minSupp;
		minConfidence = minConf;

		latticePanel = latPanel;
		rulePanel = buildPanel();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(rulePanel, BorderLayout.CENTER);

		pack();
		setVisible(true);
	}

	private JPanel buildPanel() {
		final JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(625, 350));
		panel.setLayout(new GridBagLayout());

		GridBagConstraints gc = new GridBagConstraints();

		JLabel nameLabel = new JLabel(
				GUIMessages.getString("GUI.context") + " : " + contextName); //$NON-NLS-1$ //$NON-NLS-2$
		gc.insets = new Insets(20, 20, 5, 20);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 0.0;
		gc.weighty = 0.0;
		gc.gridwidth = 2;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		panel.add(nameLabel, gc);

		JLabel suppLabel = new JLabel(
				GUIMessages.getString("GUI.minSupport") + " : " + (minSupport * 100.0) + "%"); //$NON-NLS-1$ //$NON-NLS-2$
		gc.insets = new Insets(5, 20, 2, 2);
		gc.gridx = 0;
		gc.gridy = 1;
		gc.weightx = 0.0;
		gc.weighty = 0.0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		panel.add(suppLabel, gc);

		JLabel confLabel = new JLabel(
				GUIMessages.getString("GUI.minConfidence") + " : " + (minConfidence * 100.0) + "%"); //$NON-NLS-1$ //$NON-NLS-2$
		gc.insets = new Insets(2, 20, 5, 2);
		gc.gridx = 0;
		gc.gridy = 2;
		gc.weightx = 0.0;
		gc.weighty = 0.0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		panel.add(confLabel, gc);

		final JLabel ruleCountLabel = new JLabel(GUIMessages
				.getString("GUI.ruleCount") + " : " + rules.size()); //$NON-NLS-1$ //$NON-NLS-2$
		gc.insets = new Insets(5, 20, 10, 2);
		gc.gridx = 0;
		gc.gridy = 3;
		gc.weightx = 0.0;
		gc.weighty = 0.0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		panel.add(ruleCountLabel, gc);

		
		ruleTable = new RuleTable(rules, latticePanel);
		ruleScrollPane = new JScrollPane(ruleTable,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		ruleScrollPane.setBorder(BorderFactory.createEmptyBorder());
		gc.insets = new Insets(10, 20, 5, 20);
		gc.gridx = 0;
		gc.gridy = 5;
		gc.weightx = 1.0;
		gc.weighty = 1.0;
		gc.gridwidth = 2;
		gc.anchor = GridBagConstraints.NORTHWEST;
		gc.fill = GridBagConstraints.BOTH;
		panel.add(ruleScrollPane, gc);

		xmlButton = new JButton();
		xmlButton.setText(GUIMessages.getString("GUI.xmlexport"));
		gc.insets = new Insets(5, 20, 20, 2);
		gc.gridx = 0;
		gc.gridy = 6;
		gc.weightx = 1.0;
		gc.weighty = 0.0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.SOUTHWEST;
		gc.fill = GridBagConstraints.NONE;
		xmlButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileFilter(new XML_Filter(""));
				chooser.setDialogTitle(GUIMessages
						.getString("GUI.xmlsavedialog"));

				if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					String fileName = chooser.getSelectedFile()
							.getAbsolutePath();
					if (!fileName.substring(fileName.length() - 4,
							fileName.length()).equals(".xml")) {
						fileName += ".xml";
					}

					try {
						File sortie = new File(fileName);
						if (sortie.exists()) {
							int overwrite = DialogBox.showDialogWarning(panel, GUIMessages
									.getString("GUI.doYouWantToOverwriteFile"), GUIMessages
									.getString("GUI.selectedFileAlreadyExist"));

							if (overwrite == DialogBox.NO) {
								DialogBox.showMessageInformation(panel, GUIMessages
										.getString("GUI.contextHasNotBeenSaved"), GUIMessages
										.getString("GUI.notSaved"));
							} else if (overwrite == DialogBox.YES) {
								new RulesWriter(sortie,rules,minSupport,
											minConfidence, contextName, panel);
							}
						} else {
							new RulesWriter(sortie,rules,minSupport,
									minConfidence, contextName, panel);
						}
					} catch (NoClassDefFoundError e1) {
						JOptionPane.showMessageDialog(panel, GUIMessages
								.getString("GUI.jdomLibraryMissing"),
								GUIMessages.getString("GUI.notSaved"),
								JOptionPane.ERROR_MESSAGE);
					} catch (IOException e1) {
						System.err.println("I/O Error");
					}
				}
			}
		});
		panel.add(xmlButton, gc);

		return panel;
	}

}
