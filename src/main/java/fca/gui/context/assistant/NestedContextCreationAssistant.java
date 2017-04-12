package fca.gui.context.assistant;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import fca.core.context.binary.BinaryContext;
import fca.core.context.nested.NestedContext;
import fca.gui.context.ContextViewer;
import fca.gui.context.panel.LevelCreationPanel;
import fca.gui.util.DialogBox;
import fca.messages.GUIMessages;

/**
 * Assistant de creation d'un contexte imbrique
 * @author Geneviève Roberge
 * @version 1.0
 */
public class NestedContextCreationAssistant extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = -3305282518528726877L;

	private ContextViewer viewer;

	private JFrame thisFrame;

	private JPanel introPanel;
	//private JPanel lastPanel;

	private Vector<JPanel> levelPanelList;

	private Vector<BinaryContext> contextList;

	private int currentPanelIdx;

	private JPanel currentPanel;

	private JTextField contextNameFld;
	//private JTextField levelCountFld;

	private JButton nextBtn;

	//private JButton cancelBtn;

	/**
	 * Constructeur
	 */
	public NestedContextCreationAssistant(ContextViewer cv, Vector<BinaryContext> contexts) {
		setTitle(GUIMessages.getString("GUI.nestedContextCreationAssistant")); //$NON-NLS-1$
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);

		viewer = cv;
		contextList = contexts;

		thisFrame = this;
		levelPanelList = new Vector<JPanel>();
		introPanel = buildIntroPanel();
		currentPanel = introPanel;
		currentPanelIdx = -1;
		getContentPane().add(currentPanel);

		pack();
		setVisible(true);
	}

	private JPanel buildIntroPanel() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(400, 325));
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.setLayout(new GridBagLayout());

		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(20, 20, 20, 20);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 2;
		gc.weightx = 0.0;
		gc.weighty = 0.0;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(GUIMessages.getString("GUI.pleaseEnterFollowingInformation")+" :"), gc); //$NON-NLS-1$ //$NON-NLS-2$

		gc.insets = new Insets(20, 20, 2, 2);
		gc.gridx = 0;
		gc.gridy = 1;
		gc.gridwidth = 1;
		gc.weightx = 0.0;
		gc.weighty = 0.0;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(GUIMessages.getString("GUI.contextName")+" : "), gc); //$NON-NLS-1$ //$NON-NLS-2$

		contextNameFld = new JTextField(GUIMessages.getString("GUI.untitled"), 20); //$NON-NLS-1$
		gc.insets = new Insets(20, 2, 2, 20);
		gc.gridx = 1;
		gc.gridy = 1;
		gc.weightx = 1.0;
		gc.weighty = 0.0;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(contextNameFld, gc);

		nextBtn = new JButton(GUIMessages.getString("GUI.next")); //$NON-NLS-1$
		nextBtn.addActionListener(new AssistantListener());
		gc.insets = new Insets(20, 20, 20, 20);
		gc.gridx = 0;
		gc.gridy = 3;
		gc.gridwidth = 2;
		gc.weightx = 1.0;
		gc.weighty = 1.0;
		gc.anchor = GridBagConstraints.SOUTHEAST;
		gc.fill = GridBagConstraints.NONE;
		panel.add(nextBtn, gc);

		return panel;
	}

	private JPanel createNewPanel() {
		NextListener nextListener = new NextListener();
		PreviousListener prevListener = new PreviousListener();
		EndListener endListener = new EndListener();

		JPanel newPanel = new LevelCreationPanel(levelPanelList.size() + 1, nextListener, prevListener, endListener,
				contextList, viewer);
		levelPanelList.add(newPanel);

		return newPanel;
	}

	private void nextPanel() {
		getContentPane().remove(currentPanel);
		currentPanelIdx++;

		if (currentPanelIdx == 0 && currentPanelIdx >= levelPanelList.size()) {
			currentPanel = createNewPanel();
		} else if (currentPanelIdx >= levelPanelList.size()) {
			((LevelCreationPanel) currentPanel).changeNextButton();
			currentPanel = createNewPanel();
		} else {
			currentPanel = levelPanelList.elementAt(currentPanelIdx);
		}

		getContentPane().add(currentPanel);
		pack();
		setVisible(true);
		repaint();
	}

	private void previousPanel() {
		getContentPane().remove(currentPanel);
		currentPanelIdx--;

		if (currentPanelIdx >= 0)
			currentPanel = levelPanelList.elementAt(currentPanelIdx);
		else
			currentPanel = introPanel;

		getContentPane().add(currentPanel);
		pack();
		setVisible(true);
		repaint();
	}

	private NestedContext createNestedContext() {
		if (levelPanelList.size() > 0) {
			BinaryContext binCtx = ((LevelCreationPanel) levelPanelList.elementAt(0)).getContext();
			if (binCtx == null)
				return null;

			NestedContext context = new NestedContext(binCtx);
			for (int i = 1; i < levelPanelList.size() - 1; i++) {
				binCtx = ((LevelCreationPanel) levelPanelList.elementAt(i)).getContext();
				NestedContext currCtx = new NestedContext(binCtx);
				context.addNextContext(currCtx);
			}

			if (levelPanelList.size() > 1) {
				binCtx = ((LevelCreationPanel) levelPanelList.elementAt(levelPanelList.size() - 1)).getContext();
				if (binCtx != null) {
					NestedContext currCtx = new NestedContext(binCtx);
					context.addNextContext(currCtx);
				}
			}

			context.setNestedContextName(contextNameFld.getText());
			return context;
		}

		else {
			return null;
		}
	}

	private class AssistantListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == nextBtn) {
				nextPanel();
			}
		}
	}

	private class NextListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			nextPanel();
		}
	}

	private class PreviousListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			previousPanel();
		}
	}

	private class EndListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			NestedContext context = createNestedContext();
			if (context != null)
				viewer.addNestedContext(context);
			else
				DialogBox.showMessageError(thisFrame, GUIMessages.getString("GUI.errorWhileTryingToCreateNestedContext"), //$NON-NLS-1$
						GUIMessages.getString("GUI.noContextCreated")); //$NON-NLS-1$
			thisFrame.setVisible(false);
			thisFrame.dispose();
		}
	}
}
