package fca.gui.context.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import fca.core.context.binary.BinaryContext;
import fca.core.util.BasicSet;
import fca.core.util.LogicalFormula;
import fca.exception.LatticeMinerException;
import fca.gui.context.ContextViewer;
import fca.gui.context.table.ContextTable;
import fca.gui.context.table.ContextTableScrollPane;
import fca.gui.context.table.model.ContextTableModel;
import fca.gui.util.DialogBox;
import fca.gui.util.constant.LMIcons;
import fca.messages.GUIMessages;

/**
 * Panneau affichant les objets d'un contexte et certains opérateurs logiques
 * pour creer une taxonomie definissant un nouvel objet
 * @author Linda Bogni
 * @version 1.0
 */
public class TaxonomyObjectPanel extends JFrame implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4494395998618201639L;

	BinaryContext context; //Le contexte a modifier
	
	ContextTable table; //La table contenant le contexte
	
	ContextTableScrollPane scrollPane; //Le scrollPane contenant la table
	
	ContextViewer viewer; //Le viewer qui affiche le scrollPane
	
	/* Composants graphiques */

	JTextField expressionField; //Zone de texte pour l'expression a creer
	
	JButton createBtn; //Bouton pour exécuter la projection
	
	JButton cancelBtn; //Bouton pour annuler la projection
	
	JPanel objectsPanel; //Panneau affichant la liste des objets
	
	Vector<JButton>objectButtons; //Boutons pour la sélection des objets
	
	Vector<String> objects; //Liste de tous les objets du treillis
	
	JPanel operatorsPanel; //Panneau affichant la liste des objets
	
	JButton andButton; //Bouton pour operateur AND
	
	JButton orButton; //Bouton pour operateur OR
	
	JButton percentageButton; //Bouton pour operateur PERCENTAGE
	
	JButton openButton; //Bouton pour parenthese ouvrante
	
	JButton closeButton; //Bouton pour parenthese fermante
	
	Color couleurDefaut; //Couleur par défaut des sélections
	
	/* Liste des attributs du contexte */

	BasicSet extent; //Intention contenant les objets du contexte
	
	Vector<String> oldObjects; //objets a retirer du contexte apres taxonomie	
	/**
	 * Constructeur
	 * @param ctsp Le ContextTableScrollPane pour lequel ce panneau affiche les attributs et objets
	 * @param cv Le ContextViewer dans lequel est affiché le treillis
	 */
	public TaxonomyObjectPanel(ContextTableScrollPane ctsp, ContextViewer cv){
		//setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setTitle(GUIMessages.getString("GUI.objectGeneralisation")); //$NON-NLS-1$
		
		scrollPane = ctsp;
		viewer = cv;
		
		table = scrollPane.getContextTable();
		context = (BinaryContext) ((ContextTableModel) table.getModel()).getContext();
		
		/*initialisation du vector contenant les objets a retirer du contexte*/ //-->LINDA
		oldObjects=new Vector<String>();
		
		/* Initialisation de l'intention choisie avec tous les objets du contexte */
		objects = context.getObjects();
		extent = new BasicSet();
		for (int i = 0; i < objects.size(); i++)
			extent.add(objects.elementAt(i));
		
		/* Construction du panneau de sélection des objets */
		objectsPanel = new JPanel();
		objectsPanel.setLayout(new GridBagLayout());
		
		TitledBorder objectsTitle = new TitledBorder(BorderFactory.createLineBorder(Color.BLACK),
				GUIMessages.getString("GUI.selectAnObject")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
		objectsTitle.setTitleColor(Color.BLACK);
		objectsPanel.setBorder(objectsTitle);
		
		GridBagConstraints gc = new GridBagConstraints();
		int gridy = -1;
		gc.insets = new Insets(2, 5, 2, 5);
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		
		/* Construction et ajout des boutons de sélection des objets */
		objectButtons = new Vector<JButton>();
		for (int i = 0; i < objects.size(); i++) {
			String fa = objects.elementAt(i);
			JButton newButton = new JButton(fa.toString());
			newButton.setPreferredSize(new Dimension(150, 20));
			newButton.addActionListener(this);
			objectButtons.add(newButton);
			gridy++;
			gc.gridy = gridy;
			objectsPanel.add(newButton, gc);
		}
		
		/* Construction du panneau de sélection des operateurs */
		operatorsPanel = new JPanel();
		operatorsPanel.setLayout(new GridBagLayout());
		
		TitledBorder operatorsTitle = new TitledBorder(BorderFactory.createLineBorder(Color.BLACK),
				GUIMessages.getString("GUI.selectAnOperator")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
		operatorsTitle.setTitleColor(Color.BLACK);
		operatorsPanel.setBorder(operatorsTitle);
		
		gc.insets = new Insets(2, 5, 2, 5);
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.NONE;
		
		/* Creation et ajout des boutons de selection des operateurs */
		andButton = new JButton("AND"); //$NON-NLS-1$
		andButton.setPreferredSize(new Dimension(70, 20));
		andButton.addActionListener(this);
		andButton.setToolTipText(GUIMessages.getString("GUI.operatorAND")); //$NON-NLS-1$
		gc.gridy = 0;
		gc.gridx = 0;
		gc.gridwidth = 1;
		operatorsPanel.add(andButton, gc);
		
		orButton = new JButton("OR"); //$NON-NLS-1$
		orButton.setPreferredSize(new Dimension(70, 20));
		orButton.addActionListener(this);
		orButton.setToolTipText(GUIMessages.getString("GUI.operatorOR")); //$NON-NLS-1$
		gc.gridy = 0;
		gc.gridx = 1;
		gc.gridwidth = 1;
		operatorsPanel.add(orButton, gc);
		
		percentageButton = new JButton("PERCENTAGE"); //$NON-NLS-1$
		percentageButton.setPreferredSize(new Dimension(150, 20));
		percentageButton.addActionListener(this);
		percentageButton.setToolTipText(GUIMessages.getString("GUI.operatorPERCENTAGE")); //$NON-NLS-1$
		gc.gridy = 1;
		gc.gridx = 0;
		gc.gridwidth = 2;
		operatorsPanel.add(percentageButton, gc);
		
		openButton = new JButton("("); //$NON-NLS-1$
		openButton.setPreferredSize(new Dimension(70, 20));
		openButton.addActionListener(this);
		openButton.setToolTipText(GUIMessages.getString("GUI.openParenthesis")); //$NON-NLS-1$
		gc.gridy = 2;
		gc.gridx = 0;
		gc.gridwidth = 1;
		operatorsPanel.add(openButton, gc);
		
		closeButton = new JButton(")"); //$NON-NLS-1$
		closeButton.setPreferredSize(new Dimension(70, 20));
		closeButton.addActionListener(this);
		closeButton.setToolTipText(GUIMessages.getString("GUI.closeParenthesis")); //$NON-NLS-1$
		gc.gridy = 2;
		gc.gridx = 1;
		gc.gridwidth = 1;
		operatorsPanel.add(closeButton, gc);
		
		/* Construction du panneau global */
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		createBtn = new JButton(LMIcons.getOK());
		createBtn.addActionListener(this);
		createBtn.setToolTipText(GUIMessages.getString("GUI.executeAndQuit")); //$NON-NLS-1$
		
		cancelBtn = new JButton(LMIcons.getCancel());
		cancelBtn.addActionListener(this);
		cancelBtn.setToolTipText(GUIMessages.getString("GUI.cancelAndQuit")); //$NON-NLS-1$
		
		expressionField = new JTextField();
		couleurDefaut = expressionField.getSelectionColor();
		
		/* Ajout des panneaux pour les objets et pour les operateurs */
		gc = new GridBagConstraints();
		gc.gridwidth = 1;
		gc.weighty = 0.0;
		gc.weightx = 1.0;
		gc.fill = GridBagConstraints.NONE;
		
		gc.insets = new Insets(5, 5, 5, 0);
		gc.gridy = 0;
		gc.gridx = 0;
		gc.anchor = GridBagConstraints.EAST;
		panel.add(createBtn, gc);
		
		gc.insets = new Insets(5, 0, 5, 5);
		gc.gridy = 0;
		gc.gridx = 1;
		gc.anchor = GridBagConstraints.WEST;
		panel.add(cancelBtn, gc);
		
		gc.insets = new Insets(5, 5, 5, 5);
		gc.gridy = 1;
		gc.gridx = 0;
		gc.gridwidth = 2;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(expressionField, gc);
		
		gc.insets = new Insets(2, 5, 2, 5);
		gc.gridy = 2;
		gc.gridx = 0;
		gc.gridwidth = 1;
		gc.weighty = 1.0;
		gc.anchor = GridBagConstraints.NORTH;
		gc.fill = GridBagConstraints.NONE;
		panel.add(objectsPanel, gc);
		
		gc.insets = new Insets(2, 5, 2, 5);
		gc.gridy = 2;
		gc.gridx = 1;
		gc.gridwidth = 1;
		gc.weighty = 1.0;
		gc.anchor = GridBagConstraints.NORTH;
		panel.add(operatorsPanel, gc);
		
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
		if(context.getTypeGen().equals("AND")){
			orButton.setEnabled(false);
			percentageButton.setEnabled(false);
		}else if(context.getTypeGen().equals("OR")){
			andButton.setEnabled(false);
			percentageButton.setEnabled(false);
		}if(context.getTypeGen().equals("PERCENTAGE")){
			andButton.setEnabled(false);
			orButton.setEnabled(false);
		}
		setVisible(true);
	}
	
	/* ======== ACTIONLISTENER INTERFACE ======== */
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		/* Bouton d'exécution de la projection */
		if (e.getSource() == createBtn) {
			String expression = expressionField.getText();
			LogicalFormula formula = new LogicalFormula(expression, extent);
			
			if (!formula.isValid()) {
				int errorPos = formula.getErrorPosition();
				int errorLength = formula.getErrorLength();
				
				expressionField.requestFocus();
				expressionField.setSelectionColor(Color.YELLOW);
				expressionField.select(errorPos, errorPos + errorLength);
				DialogBox.showMessageError(this, formula.getErrorMessage() + " ("+GUIMessages.getString("GUI.positionError")+" = " + errorPos + ")", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						GUIMessages.getString("GUI.formuleError")); //$NON-NLS-1$
			}

			else {
				try {//-----ICI
					context.createTaxonomyObject(formula, expression);
				} catch (LatticeMinerException error) {
				DialogBox.showMessageError(this, error);
				}
				
				table.setModelFromContext(context);
				scrollPane.setRowHeaderView(((ContextTableModel) table.getModel()).getRowHeader());
				viewer.repaint();
				
				setVisible(false);
				dispose();
			}
		}

		else if (e.getSource() == cancelBtn) {
			setVisible(false);
			dispose();
		}

		else if (e.getSource() == andButton) {
			context.setTypeGen("AND");
			orButton.setEnabled(false);
			percentageButton.setEnabled(false);
			if (expressionField.getCaretPosition() < expressionField.getText().length()) {
				String text1 = expressionField.getText().substring(0, expressionField.getCaretPosition());
				String text2 = expressionField.getText().substring(expressionField.getCaretPosition());
				expressionField.setText(text1 + " AND " + text2); //$NON-NLS-1$
			} else
				expressionField.setText(expressionField.getText() + " AND"); //$NON-NLS-1$
		}

		else if (e.getSource() == orButton) {
			context.setTypeGen("OR");
			andButton.setEnabled(false);
			percentageButton.setEnabled(false);
			if (expressionField.getCaretPosition() < expressionField.getText().length()) {
				String text1 = expressionField.getText().substring(0, expressionField.getCaretPosition());
				String text2 = expressionField.getText().substring(expressionField.getCaretPosition());
				expressionField.setText(text1 + " OR " + text2); //$NON-NLS-1$
			} else
				expressionField.setText(expressionField.getText() + " OR"); //$NON-NLS-1$
		}
		
		else if (e.getSource() == percentageButton) {
			context.setTypeGen("PERCENTAGE");
			andButton.setEnabled(false);
			orButton.setEnabled(false);
			if (expressionField.getCaretPosition() < expressionField.getText().length()) {
				String text1 = expressionField.getText().substring(0, expressionField.getCaretPosition());
				String text2 = expressionField.getText().substring(expressionField.getCaretPosition());
				expressionField.setText(text1 + " PERCENTAGE " + text2); //$NON-NLS-1$
			} else
				expressionField.setText(expressionField.getText() + " PERCENTAGE"); //$NON-NLS-1$
			}

		else if (e.getSource() == openButton) {
			if (expressionField.getCaretPosition() < expressionField.getText().length()) {
				String text1 = expressionField.getText().substring(0, expressionField.getCaretPosition());
				String text2 = expressionField.getText().substring(expressionField.getCaretPosition());
				expressionField.setText(text1 + " ( " + text2); //$NON-NLS-1$
			} else
				expressionField.setText(expressionField.getText() + " ("); //$NON-NLS-1$
		}

		else if (e.getSource() == closeButton) {
			if (expressionField.getCaretPosition() < expressionField.getText().length()) {
				String text1 = expressionField.getText().substring(0, expressionField.getCaretPosition());
				String text2 = expressionField.getText().substring(expressionField.getCaretPosition());
				expressionField.setText(text1 + " ) " + text2); //$NON-NLS-1$
			} else
				expressionField.setText(expressionField.getText() + " )"); //$NON-NLS-1$
		}

		else if (objectButtons.contains(e.getSource())) {
			JButton button = (JButton) e.getSource();
			oldObjects.add(button.getText());
			if (expressionField.getCaretPosition() < expressionField.getText().length()) {
				String text1 = expressionField.getText().substring(0, expressionField.getCaretPosition());
				String text2 = expressionField.getText().substring(expressionField.getCaretPosition());
				expressionField.setText(text1 + " [" + button.getText() + "] " + text2); //$NON-NLS-1$ //$NON-NLS-2$
			} else
				expressionField.setText(expressionField.getText() + " [" + button.getText() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			}
	}


}
