package fca.gui.context.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import fca.core.context.Context;
import fca.core.util.BasicSet;
import fca.gui.context.ContextViewer;
import fca.gui.context.table.ContextTable;
import fca.gui.context.table.ContextTableScrollPane;
import fca.gui.context.table.model.ContextTableModel;
import fca.gui.util.constant.LMIcons;
import fca.messages.GUIMessages;

/**
 * Panneau affichant les attributs d'un contexte pour en permettre la suppression Outaouais
 * @author Geneviève Roberge
 * @version 1.0
 */
public class RemoveAttributesPanel extends JFrame implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8938327909098586988L;

	Context context; //Le contexte a modifier
	
	ContextTable table; //La table contenant le contexte
	
	ContextTableScrollPane scrollPane; //Le scrollPane contenant la table
	
	ContextViewer viewer; //Le viewer qui affiche le scrollPane
	
	/* Composants graphiques */

	JButton removeBtn; //Bouton pour exécuter la projection
	
	JButton cancelBtn; //Bouton pour annuler la projection
	
	JPanel attributesPanel; //Panneau affichant la liste des attributs
	Vector<JCheckBox> attributeCheckBoxes; //Boîtes de sélection/désélection des attributs
	Vector<String> attributes; //Liste de tous les attributs du treillis
	
	BasicSet intent; //Intention contenant les attributs choisis
	
	/**
	 * Constructeur
	 * @param ctsp Le ContextTableScrollPane pour lequel ce panneau affiche les attributs et objets
	 * @param cv Le ContextViewer dans lequel est affiché le treillis
	 */
	public RemoveAttributesPanel(ContextTableScrollPane ctsp, ContextViewer cv) {
		//setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setTitle(GUIMessages.getString("GUI.removeAttributes")); //$NON-NLS-1$
		
		scrollPane = ctsp;
		viewer = cv;
		
		table = scrollPane.getContextTable();
		context = ((ContextTableModel) table.getModel()).getContext();
		
		/* Initialisation de l'intention choisie avec tous les attributs du contexte */
		Vector<String> tempAttributes = context.getAttributes();
		intent = new BasicSet();
		for (int i = 0; i < tempAttributes.size(); i++)
			intent.add(tempAttributes.elementAt(i));
		
		/* Construction du panneau de sélection des attributs */
		attributesPanel = new JPanel();
		attributesPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gc = new GridBagConstraints();
		int gridy = -1;
		gc.insets = new Insets(2, 5, 2, 5);
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		
		/* Construction et ajout des booîtes de sélection des attributs */
		attributeCheckBoxes = new Vector<JCheckBox>();
		attributes = new Vector<String>();
		Iterator<String> attIt = intent.iterator();
		while (attIt.hasNext()) {
			String fa = attIt.next();
			JCheckBox newCheckBox = new JCheckBox(fa.toString());
			newCheckBox.setSelected(false);
			newCheckBox.setPreferredSize(new Dimension(180, 20));
			newCheckBox.addActionListener(this);
			attributeCheckBoxes.add(newCheckBox);
			gridy++;
			gc.gridy = gridy;
			attributesPanel.add(newCheckBox, gc);
			attributes.add(fa);
		}
		
		/* Construction du panneau global */
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		TitledBorder panelTitle = new TitledBorder(BorderFactory.createLineBorder(Color.BLACK),
				GUIMessages.getString("GUI.selectAttributesToRemove")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
		panelTitle.setTitleColor(Color.BLACK);
		panel.setBorder(panelTitle);
		
		removeBtn = new JButton(LMIcons.getOK());
		removeBtn.addActionListener(this);
		removeBtn.setToolTipText(GUIMessages.getString("GUI.executeAndQuit")); //$NON-NLS-1$
		
		cancelBtn = new JButton(LMIcons.getCancel());
		cancelBtn.addActionListener(this);
		cancelBtn.setToolTipText(GUIMessages.getString("GUI.cancelAndQuit")); //$NON-NLS-1$
		
		/*
		 * Ajout des panneaux pour les attributs et pour les objets au même endroit : un seul des
		 * deux sera visible à la fois
		 */
		gc = new GridBagConstraints();
		gc.gridwidth = 1;
		gc.weighty = 0.0;
		gc.weightx = 1.0;
		gc.fill = GridBagConstraints.NONE;
		
		gc.insets = new Insets(5, 5, 5, 0);
		gc.gridy = 0;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.EAST;
		panel.add(removeBtn, gc);
		
		gc.insets = new Insets(5, 0, 5, 5);
		gc.gridy = 0;
		gc.gridx = 1;
		gc.anchor = GridBagConstraints.WEST;
		panel.add(cancelBtn, gc);
		
		gc.insets = new Insets(2, 5, 2, 5);
		gc.gridy = 1;
		gc.gridx = 0;
		gc.gridwidth = 2;
		gc.weighty = 1.0;
		gc.anchor = GridBagConstraints.NORTH;
		panel.add(attributesPanel, gc);
		
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBounds(0, 0, 200, 500);
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		pack();
		setVisible(false);
	}
	
	/**
	 * Ouvre ce panneau
	 */
	public void open() {
		setVisible(true);
	}
	
	/* ======== ACTIONLISTENER INTERFACE ======== */
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		/* Bouton d'exécution de la projection */
		if (e.getSource() == removeBtn) {
			intent = new BasicSet();
			for (int i = 0; i < attributeCheckBoxes.size(); i++) {
				JCheckBox checkBox = attributeCheckBoxes.elementAt(i);
				if (checkBox.isSelected()) {
					context.removeAttribute(checkBox.getText());
				}
			}
			
			table.setModelFromContext(context);
			scrollPane.setRowHeaderView(((ContextTableModel) table.getModel()).getRowHeader());
			viewer.repaint();
			
			setVisible(false);
			dispose();
		}
		
		if (e.getSource() == cancelBtn) {
			setVisible(false);
			dispose();
		}
	}
}