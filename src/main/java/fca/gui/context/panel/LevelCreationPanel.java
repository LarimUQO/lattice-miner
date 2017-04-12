package fca.gui.context.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import fca.core.context.binary.BinaryContext;
import fca.exception.ReaderException;
import fca.gui.context.ContextViewer;
import fca.gui.util.ColorSet;
import fca.gui.util.DialogBox;
import fca.gui.util.ExampleFileFilter;
import fca.gui.util.constant.LMPreferences;
import fca.io.context.ContextReaderJson.txt.LMBinaryContextReader;
import fca.messages.GUIMessages;

/**
 * Assistant de creation d'un niveau de contexte imbrique
 * @author Geneviève Roberge
 * @version 1.0
 */
public class LevelCreationPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8202347190674458146L;

	private ContextViewer viewer;
	
	private BinaryContext context;
	
	private JPanel thisPanel;
	
	private int level;
	
	private ActionListener nextListener;
	
	private ActionListener prevListener;
	
	private ActionListener endListener;
	
	//private JFileChooser contextFileChooser;
	
	private JButton fileChooserBtn;
	
	private JScrollPane listScrollPane;
	
	private JList contextList;
	
	private JLabel levelContextLabel;
	private Vector<BinaryContext> contexts;
	private Vector<String> contextNames;
	
	private JRadioButton loadedCtxBtn;
	
	private JRadioButton ctxFileBtn;
	
	private JButton nextBtn;
	
	public LevelCreationPanel(int lev, ActionListener next, ActionListener previous, ActionListener end,
			Vector<BinaryContext> loadedContexts, ContextViewer cv) {
		//setPreferredSize(new Dimension(450, 335));
		setBorder(BorderFactory.createEtchedBorder());
		setLayout(new GridBagLayout());
		
		thisPanel = this;
		level = lev;
		nextListener = next;
		prevListener = previous;
		endListener = end;
		contexts = loadedContexts;
		viewer = cv;
		contextNames = new Vector<String>();
		for (int i = 0; i < contexts.size(); i++)
			contextNames.add((contexts.elementAt(i)).getName());
		
		PanelListener panelListener = new PanelListener();
		PanelMouseListener mouseListener = new PanelMouseListener();
		
		JPanel labelPanel = new JPanel();
		labelPanel.setPreferredSize(new Dimension(400, 30));
		labelPanel.setBorder(BorderFactory.createEtchedBorder());
		Color panelColor = ColorSet.getColorAt(level - 1);
		labelPanel.setBackground(panelColor);
		
		JLabel levelLabel = new JLabel(GUIMessages.getString("GUI.contextInLevel") + " " + level); //$NON-NLS-1$ //$NON-NLS-2$
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(2, 2, 2, 2);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.NONE;
		labelPanel.add(levelLabel, gc);
		
		gc.insets = new Insets(20, 20, 20, 20);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 4;
		gc.weightx = 0.0;
		gc.weighty = 0.0;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.HORIZONTAL;
		add(labelPanel, gc);
		
		loadedCtxBtn = new JRadioButton(GUIMessages.getString("GUI.loadedContext"), true); //$NON-NLS-1$
		loadedCtxBtn.addActionListener(panelListener);
		ctxFileBtn = new JRadioButton(GUIMessages.getString("GUI.contextFile"), false); //$NON-NLS-1$
		ctxFileBtn.addActionListener(panelListener);
		
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(loadedCtxBtn);
		radioGroup.add(ctxFileBtn);
		
		gc.insets = new Insets(5, 20, 2, 2);
		gc.gridx = 0;
		gc.gridy = 1;
		gc.gridwidth = 2;
		gc.weightx = 0.0;
		gc.weighty = 0.0;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		add(ctxFileBtn, gc);
		
		/* contextFileChooser = new JFileChooser(); */
		fileChooserBtn = new JButton(GUIMessages.getString("GUI.chooseFile")); //$NON-NLS-1$
		fileChooserBtn.addActionListener(panelListener);
		fileChooserBtn.setEnabled(false);
		gc.insets = new Insets(2, 20, 5, 20);
		gc.gridx = 2;
		gc.gridy = 1;
		gc.weightx = 0.0;
		gc.weighty = 0.0;
		gc.gridwidth = 2;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		add(fileChooserBtn, gc);
		
		gc.insets = new Insets(2, 20, 5, 2);
		gc.gridx = 0;
		gc.gridy = 2;
		gc.gridwidth = 4;
		gc.weightx = 0.0;
		gc.weighty = 1.0;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		add(loadedCtxBtn, gc);
		
		if (contextNames.isEmpty()) {
			loadedCtxBtn.setSelected(false);
			ctxFileBtn.setSelected(true);
			fileChooserBtn.setEnabled(true);
		} else {
			loadedCtxBtn.setSelected(true);
			ctxFileBtn.setSelected(false);
			fileChooserBtn.setEnabled(false);
		}
		
		contextList = new JList<>(contextNames);
		contextList.setEnabled(true);
		contextList.addMouseListener(mouseListener);
		
		listScrollPane = new JScrollPane(contextList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		listScrollPane.setPreferredSize(new Dimension(400, 60));
		
		gc.insets = new Insets(5, 20, 5, 20);
		gc.gridx = 0;
		gc.gridy = 3;
		gc.weightx = 1.0;
		gc.weighty = 0.0;
		gc.gridwidth = 4;
		gc.anchor = GridBagConstraints.NORTH;
		gc.fill = GridBagConstraints.HORIZONTAL;
		add(listScrollPane, gc);
		
		levelContextLabel = new JLabel(GUIMessages.getString("GUI.context")+" : ( "+GUIMessages.getString("GUI.none")+" )"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		gc.insets = new Insets(10, 20, 10, 2);
		gc.gridx = 0;
		gc.gridy = 4;
		gc.weightx = 0.0;
		gc.weighty = 1.0;
		gc.gridwidth = 4;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		add(levelContextLabel, gc);
		
		JButton prevBtn = new JButton(GUIMessages.getString("GUI.back")); //$NON-NLS-1$
		prevBtn.addActionListener(prevListener);
		gc.insets = new Insets(5, 20, 20, 5);
		gc.gridx = 0;
		gc.gridy = 5;
		gc.weightx = 1.0;
		gc.weighty = 1.0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.SOUTHWEST;
		gc.fill = GridBagConstraints.NONE;
		add(prevBtn, gc);
		
		nextBtn = new JButton(GUIMessages.getString("GUI.createNewLevel")); //$NON-NLS-1$
		nextBtn.setEnabled(false);
		nextBtn.addActionListener(nextListener);
		gc.insets = new Insets(5, 5, 20, 5);
		gc.gridx = 1;
		gc.gridy = 5;
		gc.weightx = 1.0;
		gc.weighty = 1.0;
		gc.gridwidth = 2;
		gc.anchor = GridBagConstraints.SOUTH;
		gc.fill = GridBagConstraints.NONE;
		add(nextBtn, gc);
		
		JButton finishBtn = new JButton(GUIMessages.getString("GUI.finish")); //$NON-NLS-1$
		finishBtn.addActionListener(endListener);
		gc.insets = new Insets(5, 5, 20, 20);
		gc.gridx = 3;
		gc.gridy = 5;
		gc.weightx = 1.0;
		gc.weighty = 1.0;
		gc.gridwidth = 1;
		gc.anchor = GridBagConstraints.SOUTHEAST;
		gc.fill = GridBagConstraints.NONE;
		add(finishBtn, gc);
	}
	
	public void changeNextButton() {
		nextBtn.setText(GUIMessages.getString("GUI.next")); //$NON-NLS-1$
	}
	
	/**
	 * @return le contexte associé
	 */
	public BinaryContext getContext() {
		return context;
	}
	
	/**
	 * Genere une fenetre pour pouvoir ouvrir un contexte existant en fichier et verifie les types
	 * et genere le contexte en fonction du type de fichier via les readers
	 */
	private void openContext() {
		JFileChooser fileChooser = new JFileChooser(LMPreferences.getLastDirectory());
		
		// Propriétés du fileChooser
		fileChooser.setApproveButtonText(GUIMessages.getString("GUI.open")); //$NON-NLS-1$
		fileChooser.setDialogTitle(GUIMessages.getString("GUI.openAContext")); //$NON-NLS-1$
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		// Gere la seule extension acceptée : lmb
		ExampleFileFilter filterBinary = new ExampleFileFilter("lmb", GUIMessages.getString("GUI.latticeMinerBinaryContext")); //$NON-NLS-1$ //$NON-NLS-2$
		fileChooser.addChoosableFileFilter(filterBinary);
		
		// La boite de dialogue
		int returnVal = fileChooser.showOpenDialog(this);
		
		// Sauvergarde le path utilisé
		LMPreferences.setLastDirectory(fileChooser.getCurrentDirectory().getAbsolutePath());
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File contextFile = fileChooser.getSelectedFile();
			
			if (!contextFile.exists()) {
				DialogBox.showMessageError(this, GUIMessages.getString("GUI.FiledoesntExist"), GUIMessages.getString("GUI.errorWithFile")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			
			try {
				LMBinaryContextReader contextReader = new LMBinaryContextReader(contextFile);
				BinaryContext binCtx = (BinaryContext) contextReader.getContext();
				context = binCtx;
				levelContextLabel.setText(GUIMessages.getString("GUI.context")+" : " + contextFile.getName()); //$NON-NLS-1$ //$NON-NLS-2$
				nextBtn.setEnabled(true);
			} catch (FileNotFoundException e) {
				DialogBox.showMessageError(this, GUIMessages.getString("GUI.fileCannotBeFound"), GUIMessages.getString("GUI.errorWithFile")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			} catch (ReaderException e) {
				DialogBox.showMessageError(this, e);
				return;
			}
		}
	}
	
	private class PanelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == loadedCtxBtn && loadedCtxBtn.isSelected()) {
				fileChooserBtn.setEnabled(false);
				contextList.setEnabled(true);
				thisPanel.repaint();
			}

			else if (e.getSource() == ctxFileBtn && ctxFileBtn.isSelected()) {
				contextList.setEnabled(false);
				fileChooserBtn.setEnabled(true);
				thisPanel.repaint();
			}

			else if (e.getSource() == fileChooserBtn) {
				openContext();
			}
		}
	}
	
	private class PanelMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			int index = contextList.locationToIndex(e.getPoint());
			if (contextList.getCellBounds(index, index) != null) {
				if (contextList.getCellBounds(index, index).contains(e.getPoint()) && contextList.isEnabled()) {
					context = contexts.elementAt(index);
					levelContextLabel.setText(GUIMessages.getString("GUI.context")+" : " + context.getName()); //$NON-NLS-1$ //$NON-NLS-2$
					nextBtn.setEnabled(true);
				}
			}
		}
	}
}