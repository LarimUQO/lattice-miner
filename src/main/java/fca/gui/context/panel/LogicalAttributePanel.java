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
 * Panneau affichant les attributs d'un contexte et certains opérateur pour creer une expression
 * logique definissant un nouvel attribut
 * @author Geneviève Roberge
 * @version 1.0
 */
public class LogicalAttributePanel extends JFrame implements ActionListener {
	
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
	
	JPanel attributesPanel; //Panneau affichant la liste des attributs
	Vector<JButton> attributeButtons; //Boutons pour la sélection des attributs
	Vector<String> attributes; //Liste de tous les attributs du treillis
	
	JPanel operatorsPanel; //Panneau affichant la liste des attributs
	
	JButton andButton; //Bouton pour operateur AND
	
	JButton orButton; //Bouton pour operateur OR
	
	JButton notButton; //Bouton pour operateur NOT
	
	JButton openButton; //Bouton pour parenthese ouvrante
	
	JButton closeButton; //Bouton pour parenthese fermante
	
	Color couleurDefaut; //Couleur par défaut des sélections
	
	/* Liste des attributs du contexte */

	BasicSet intent; //Intention contenant les attributs du contexte
	
	/**
	 * Constructeur
	 * @param ctsp Le ContextTableScrollPane pour lequel ce panneau affiche les attributs et objets
	 * @param cv Le ContextViewer dans lequel est affiché le treillis
	 */
	public LogicalAttributePanel(ContextTableScrollPane ctsp, ContextViewer cv) {
		//setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setTitle(GUIMessages.getString("GUI.attributeCreation")); //$NON-NLS-1$
		
		scrollPane = ctsp;
		viewer = cv;
		
		table = scrollPane.getContextTable();
		context = (BinaryContext) ((ContextTableModel) table.getModel()).getContext();
		
		/* Initialisation de l'intention choisie avec tous les attributs du contexte */
		attributes = context.getAttributes();
		intent = new BasicSet();
		for (int i = 0; i < attributes.size(); i++)
			intent.add(attributes.elementAt(i));
		
		/* Construction du panneau de sélection des attributs */
		attributesPanel = new JPanel();
		attributesPanel.setLayout(new GridBagLayout());
		
		TitledBorder attributesTitle = new TitledBorder(BorderFactory.createLineBorder(Color.BLACK),
				GUIMessages.getString("GUI.selectAnAttribute")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
		attributesTitle.setTitleColor(Color.BLACK);
		attributesPanel.setBorder(attributesTitle);
		
		GridBagConstraints gc = new GridBagConstraints();
		int gridy = -1;
		gc.insets = new Insets(2, 5, 2, 5);
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		
		/* Construction et ajout des boutons de sélection des attributs */
		attributeButtons = new Vector<JButton>();
		for (int i = 0; i < attributes.size(); i++) {
			String fa = attributes.elementAt(i);
			JButton newButton = new JButton(fa.toString());
			newButton.setPreferredSize(new Dimension(150, 20));
			newButton.addActionListener(this);
			attributeButtons.add(newButton);
			gridy++;
			gc.gridy = gridy;
			attributesPanel.add(newButton, gc);
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
		
		notButton = new JButton("NOT"); //$NON-NLS-1$
		notButton.setPreferredSize(new Dimension(70, 20));
		notButton.addActionListener(this);
		notButton.setToolTipText(GUIMessages.getString("GUI.operatorNOT")); //$NON-NLS-1$
		gc.gridy = 1;
		gc.gridx = 0;
		gc.gridwidth = 2;
		operatorsPanel.add(notButton, gc);
		
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
		
		/* Ajout des panneaux pour les attributs et pour les operateurs */
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
		panel.add(attributesPanel, gc);
		
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
			LogicalFormula formula = new LogicalFormula(expression, intent);
			
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
				try {
					context.createLogicalAttribute(formula, expression);
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
			if (expressionField.getCaretPosition() < expressionField.getText().length()) {
				String text1 = expressionField.getText().substring(0, expressionField.getCaretPosition());
				String text2 = expressionField.getText().substring(expressionField.getCaretPosition());
				expressionField.setText(text1 + " AND " + text2); //$NON-NLS-1$
			} else
				expressionField.setText(expressionField.getText() + " AND"); //$NON-NLS-1$
		}

		else if (e.getSource() == orButton) {
			if (expressionField.getCaretPosition() < expressionField.getText().length()) {
				String text1 = expressionField.getText().substring(0, expressionField.getCaretPosition());
				String text2 = expressionField.getText().substring(expressionField.getCaretPosition());
				expressionField.setText(text1 + " OR " + text2); //$NON-NLS-1$
			} else
				expressionField.setText(expressionField.getText() + " OR"); //$NON-NLS-1$
		}

		else if (e.getSource() == notButton) {
			if (expressionField.getCaretPosition() < expressionField.getText().length()) {
				String text1 = expressionField.getText().substring(0, expressionField.getCaretPosition());
				String text2 = expressionField.getText().substring(expressionField.getCaretPosition());
				expressionField.setText(text1 + " NOT " + text2); //$NON-NLS-1$
			} else
				expressionField.setText(expressionField.getText() + " NOT"); //$NON-NLS-1$
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

		else if (attributeButtons.contains(e.getSource())) {
			JButton button = (JButton) e.getSource();
			if (expressionField.getCaretPosition() < expressionField.getText().length()) {
				String text1 = expressionField.getText().substring(0, expressionField.getCaretPosition());
				String text2 = expressionField.getText().substring(expressionField.getCaretPosition());
				expressionField.setText(text1 + " [" + button.getText() + "] " + text2); //$NON-NLS-1$ //$NON-NLS-2$
			} else
				expressionField.setText(expressionField.getText() + " [" + button.getText() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}