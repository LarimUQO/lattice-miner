package fca.gui.lattice;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fca.core.context.binary.BinaryContext;
import fca.core.lattice.ConceptLattice;
import fca.core.lattice.NestedLattice;
import fca.core.rule.InformativeBasisAlgorithm;
import fca.core.rule.Rule;
import fca.core.rule.RuleAlgorithm;
import fca.exception.AlreadyExistsException;
import fca.exception.InvalidTypeException;
import fca.exception.LatticeMinerException;
import fca.exception.WriterException;
import fca.gui.Viewer;
import fca.gui.lattice.element.GraphicalConcept;
import fca.gui.lattice.element.GraphicalLattice;
import fca.gui.lattice.element.LatticeStructure;
import fca.gui.lattice.tool.Projection;
import fca.gui.lattice.tool.Search;
import fca.gui.lattice.tool.SearchNested;
import fca.gui.lattice.tool.SearchSimple;
import fca.gui.lattice.tool.Tree;
import fca.gui.rule.RuleViewer;
import fca.gui.util.ColorSet;
import fca.gui.util.DialogBox;
import fca.gui.util.ExampleFileFilter;
import fca.gui.util.ScreenImage;
import fca.gui.util.constant.LMHistory;
import fca.gui.util.constant.LMIcons;
import fca.gui.util.constant.LMOptions;
import fca.gui.util.constant.LMPreferences;
import fca.io.lattice.writer.xml.GaliciaXMLLatticeWriter;
import fca.messages.GUIMessages;

/**
 * Fenetre affichant un treillis (imbrique ou non) avec sa structure arborescente et son panneau de
 * controle
 * @author Genevieve Roberge
 * @version 1.0
 */
public class LatticeViewer extends Viewer {

	/**
	 *
	 */
	private static final long serialVersionUID = 6042867291900333577L;

	LatticeStructureFrame structureFrame;

	LatticePanel viewer; //Le panneau charge d'afficher le treillis imbrique

	JButton nextBtn;
	JButton previousBtn;
	JButton zoomInBtn;
	JButton zoomOutBtn;
	JButton zoomAreaBtn;
	JButton noZoomBtn;
	JButton modifyBtn;
	//	JButton projectionBtn;
	//	JButton searchBtn;
	JButton duplicateBtn;
	JButton ruleBtn;
	JButton captureBtn;

	Tree treePanel; //Le panneau charge d'afficher la structure arborescente du treillis

	Projection projectionPanel;

	Search searchPanel;

	JTabbedPane toolPane; //TabbedPane contenant tous les outils externes pour le treillis

	GraphicalLattice glCurrent;

	JLabel textSizeLabel;

	JSlider textSize; //Slider permettant de changer a la volee la taille du texte des labels

	JLabel textSizeValue;


	private FrameMenu frameMenu = null;

	/**
	 * Constructeur
	 * @param gl Le NestedGraphLattice qui doit etre affiche
	 */
	public LatticeViewer(GraphicalLattice gl) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		glCurrent = gl;
		structureFrame = null;
		setTitle(GUIMessages.getString("GUI.lattice")+" : " + gl.getName()); //$NON-NLS-1$ //$NON-NLS-2$

		frameMenu = new FrameMenu();
		setJMenuBar(frameMenu.getMenuBar());
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(buildToolBar(), BorderLayout.NORTH);
		getContentPane().add(buildPanel(gl), BorderLayout.CENTER);

		// Par defaut c'est le User Options qui est selectionne et on bloque l'historique pour ne pas voir l'operation
		viewer.lockHistory();
		frameMenu.setPresetOptions(LMOptions.USER);
		viewer.unlockHistory();

		addWindowListener(this);
		addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent e) {
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentResized(ComponentEvent e) {
				viewer.lockHistory();
				showEntireLattice();
				viewer.unlockHistory();
			}

			public void componentShown(ComponentEvent e) {
			}
		});
	}

	private JToolBar buildToolBar() {
		JToolBar toolBar = new JToolBar(GUIMessages.getString("GUI.quickTools")); //$NON-NLS-1$
		ToolBarListener listener = new ToolBarListener();
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

		previousBtn = new JButton(LMIcons.getUndo());
		previousBtn.addActionListener(listener);
		previousBtn.setToolTipText(GUIMessages.getString("GUI.back")); //$NON-NLS-1$
		previousBtn.setEnabled(false);
		previousBtn.setMnemonic(KeyEvent.VK_Z);
		toolBar.add(previousBtn);

		nextBtn = new JButton(LMIcons.getRedo());
		nextBtn.addActionListener(listener);
		nextBtn.setToolTipText(GUIMessages.getString("GUI.forward")); //$NON-NLS-1$
		nextBtn.setEnabled(false);
		nextBtn.setMnemonic(KeyEvent.VK_Y);
		toolBar.add(nextBtn);

		toolBar.addSeparator();

		zoomInBtn = new JButton(LMIcons.getZoomIn());
		zoomInBtn.addActionListener(listener);
		zoomInBtn.setToolTipText(GUIMessages.getString("GUI.zoomIn")); //$NON-NLS-1$
		zoomInBtn.setMnemonic(KeyEvent.VK_ADD);
		toolBar.add(zoomInBtn);

		zoomOutBtn = new JButton(LMIcons.getZoomOut());
		zoomOutBtn.addActionListener(listener);
		zoomOutBtn.setToolTipText(GUIMessages.getString("GUI.zoomOut")); //$NON-NLS-1$
		zoomOutBtn.setMnemonic(KeyEvent.VK_SUBTRACT);
		toolBar.add(zoomOutBtn);

		zoomAreaBtn = new JButton(LMIcons.getFitZoomToSelection());
		zoomAreaBtn.addActionListener(listener);
		zoomAreaBtn.setToolTipText(GUIMessages.getString("GUI.zoomInSelectedArea")); //$NON-NLS-1$
		zoomAreaBtn.setMnemonic(KeyEvent.VK_S);
		toolBar.add(zoomAreaBtn);

		noZoomBtn = new JButton(LMIcons.getNoZoom());
		noZoomBtn.addActionListener(listener);
		noZoomBtn.setToolTipText(GUIMessages.getString("GUI.showAllEntireLattice")); //$NON-NLS-1$
		noZoomBtn.setMnemonic(KeyEvent.VK_A);
		toolBar.add(noZoomBtn);

		toolBar.addSeparator();

		modifyBtn = new JButton(LMIcons.getModifyTools());
		modifyBtn.addActionListener(listener);
		modifyBtn.setToolTipText(GUIMessages.getString("GUI.modifyLatticeStructure")); //$NON-NLS-1$
		modifyBtn.setMnemonic(KeyEvent.VK_M);
		toolBar.add(modifyBtn);

		ruleBtn = new JButton(LMIcons.getShowRulesBig());
		ruleBtn.addActionListener(listener);
		ruleBtn.setToolTipText(GUIMessages.getString("GUI.showRules")); //$NON-NLS-1$
		ruleBtn.setMnemonic(KeyEvent.VK_R);
		toolBar.add(ruleBtn);

		toolBar.addSeparator();

		captureBtn = new JButton(LMIcons.getPrintScreen());
		captureBtn.addActionListener(listener);
		captureBtn.setToolTipText(GUIMessages.getString("GUI.exportLatticeAsImage")); //$NON-NLS-1$
		captureBtn.setMnemonic(KeyEvent.VK_E);
		toolBar.add(captureBtn);

		toolBar.addSeparator();

		textSizeLabel = new JLabel();
		textSizeLabel.setText("Label Size");
		toolBar.add(textSizeLabel);


		textSize = new JSlider(6,36,12);
		textSize.addChangeListener(new SliderListener());
		toolBar.add(textSize);

		textSizeValue = new JLabel();
		textSizeValue.setText(""+textSize.getValue());
		toolBar.add(textSizeValue);

		toolBar.add(Box.createHorizontalGlue());
		toolBar.addSeparator();

		duplicateBtn = new JButton(LMIcons.getDuplicate());
		duplicateBtn.addActionListener(listener);
		duplicateBtn.setToolTipText(GUIMessages.getString("GUI.duplicate")); //$NON-NLS-1$
		duplicateBtn.setMnemonic(KeyEvent.VK_D);
		toolBar.add(duplicateBtn);

		return toolBar;
	}

	private JPanel buildPanel(GraphicalLattice gl) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel currentLatticeName = new JLabel(GUIMessages.getString("GUI.lattice")+" : " + gl.getName()); //$NON-NLS-1$ //$NON-NLS-2$

		/* Creation du panneau affichant le treillis imbrique */
		viewer = new LatticePanel(gl, this);
		ScrollPane viewerScrollPane = new ScrollPane(ScrollPane.SCROLLBARS_NEVER);
		viewerScrollPane.add(viewer);
		viewerScrollPane.setMinimumSize(new Dimension(0, 50));
		viewerScrollPane.setPreferredSize(new Dimension(575, 575));

		/* Création du panneau pour la structure arborescente du treillis imbrique */
		treePanel = new Tree(gl, viewer);
		ScrollPane treeScrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		treeScrollPane.setBackground(Color.WHITE);
		treeScrollPane.add(treePanel);
		treeScrollPane.setMinimumSize(new Dimension(150, 50));

		/* Le panneau est sensible aux evenements dans la structure arborescente */
		treePanel.getTree().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				/*
				 * Changement de selection dans l'arbre : Le noeud du treillis graphique
				 * correspondant doit etre selectionne et visible, donc le zoom est ajuste pour que
				 * le treillis du noeud soit au centre de l'ecran
				 */
				if (e.getPropertyName().equals("leadSelectionPath")) { //$NON-NLS-1$
					GraphicalConcept selectedNode = treePanel.getSelectedNode();
					/* Selection du noeud racine de l'arbre */
					if (!treePanel.isGlobalLatticeSelected()) {
						viewer.selectAndShakeNode(selectedNode);
						viewer.repaint();
					}

				}

				/*
				 * Changement de modele pour l'arbre (exemple : projection) : Comme la projection
				 * modifie aussi le noeud selectionne, il est important d'ajuster le zoom pour
				 * permettre de voir le treillis en entier
				 */
				else if (e.getPropertyName().equals("model")) { //$NON-NLS-1$
					viewer.lockHistory();
					viewer.zoomInArea(viewer.getBoundsForLattice(viewer.getRootLattice()));
					if (viewer.getRootLattice().isEditable())
						viewer.getRootLattice().clearLattice();
					viewer.repaint();
					viewer.unlockHistory();
				}
			}
		});

		projectionPanel = new Projection(viewer.getRootLattice(), viewer);
		ScrollPane projectionScrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		projectionScrollPane.setBackground(Color.WHITE);
		projectionScrollPane.add(projectionPanel);
		projectionScrollPane.setMinimumSize(new Dimension(0, 50));

		toolPane = new JTabbedPane();
		toolPane.addTab(GUIMessages.getString("GUI.treeViewPanel"), treeScrollPane); //$NON-NLS-1$
		toolPane.addTab(GUIMessages.getString("GUI.projectSelectPanel"), projectionScrollPane); //$NON-NLS-1$

		// "Search/Approximate" differents si imbrique ou non
		if (!glCurrent.isNested())
			searchPanel = new SearchSimple(viewer.getRootLattice(), viewer);
		else
			searchPanel = new SearchNested(viewer.getRootLattice(), viewer);

		ScrollPane searchScrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		searchScrollPane.setBackground(Color.WHITE);
		searchScrollPane.add(searchPanel);
		searchScrollPane.setMinimumSize(new Dimension(0, 50));
		toolPane.addTab(GUIMessages.getString("GUI.searchApproximatePanel"), searchScrollPane); //$NON-NLS-1$

		/*
		 * Les panneaux contenant le treillis et son arbre d'imbrication partageront un même espace
		 * de visualisation pour permettre la modification de leur largeur
		 */
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, toolPane, viewerScrollPane);//treeScrollPane, viewerScrollPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		splitPane.setDividerLocation(210);

		/* Ajout des panneau dans la fenetre */
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(0, 0, 0, 0);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.NONE;
		panel.add(currentLatticeName, constraints);

		constraints.insets = new Insets(0, 0, 0, 0);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.BOTH;
		panel.add(splitPane, constraints);

		return panel;
	}

	/**
	 * Construit une fenetre qui permettra de modifier la structure des treillis
	 */
	private void openStructureViewer() throws AlreadyExistsException, InvalidTypeException {
		/* Construction d'un vecteur contenant chacun des treillis du treillis imbriques */
		Vector<ConceptLattice> conceptLattices = new Vector<ConceptLattice>();
		conceptLattices.add(viewer.getRootLattice().getNestedLattice().getConceptLattice());
		conceptLattices.addAll(viewer.getRootLattice().getNestedLattice().getInternalLattices());

		/* Construction d'un vecteur contenant les structures de chacun des treillis imbriques */
		Vector<LatticeStructure> structures = new Vector<LatticeStructure>();
		structures.add(viewer.getRootLattice().getLatticeStructure());
		structures.addAll(viewer.getRootLattice().getInternalLatticeStructures());

		/* Construction d'un vecteur contenant chacun des treillis graphiques, sans imbrication */
		int conceptSizeType;
		if (viewer.getRootLattice().getConceptSize() == GraphicalConcept.LARGE_NODE_SIZE)
			conceptSizeType = LMOptions.LARGE;
		else
			conceptSizeType = LMOptions.SMALL;

		Vector<GraphicalLattice> latticesList = new Vector<GraphicalLattice>();
		for (int i = 0; i < conceptLattices.size(); i++) {
			ConceptLattice currentConceptLattice = conceptLattices.elementAt(i);
			LatticeStructure currentStructure = structures.elementAt(i);

			Vector<LatticeStructure> currentStructuresList = new Vector<LatticeStructure>();
			currentStructuresList.add(new LatticeStructure(currentStructure.getConceptPositions()));

			Vector<ConceptLattice> internalLattices = new Vector<ConceptLattice>();
			internalLattices.add(currentConceptLattice);

			NestedLattice currentNestedLattice = new NestedLattice(null, internalLattices, null, GUIMessages.getString("GUI.structure")); //$NON-NLS-1$
			GraphicalLattice currentGraphLattice = new GraphicalLattice(currentNestedLattice, null,
					currentStructuresList);
			currentGraphLattice.setLatticeColor(ColorSet.getColorAt(i), ColorSet.getColorStringAt(i));
			currentGraphLattice.setConceptSizeType(conceptSizeType);
			latticesList.add(currentGraphLattice);
		}

		/* Ouverture du panneau de modification des structres */
		structureFrame = new LatticeStructureFrame(viewer, latticesList);
	}

	public LatticePanel getLatticePanel() {
		return viewer;
	}

	private void backHistory() {
		viewer.backHistory();
	}

	private void forwardHistory() {
		viewer.forwardHistory();
	}

	private void zoomIn() {
		viewer.zoomIn();
		repaint();
	}

	private void zoomOut() {
		viewer.zoomOut();
		repaint();
	}

	private void zoomInSelectedArea() {
		if (viewer.getSelectedArea() != null)
			viewer.zoomInSelectedArea();
		else
			DialogBox.showMessageInformation(viewer, GUIMessages.getString("GUI.noSelectedArea"), GUIMessages.getString("GUI.noZoomInSelectedArea")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void showEntireLattice() {
		viewer.addHistoryItem(LMHistory.SCALE_AND_MOVE);
		viewer.lockHistory();
		viewer.zoomInArea(viewer.getBoundsForLattice(viewer.getRootLattice()));
		viewer.unlockHistory();
	}

	private void showAllLabels() {
		viewer.showAllLabels();
	}

	private void hideAllLabels() {
		viewer.hideAllLabels();
	}

	private void showAreaLabels() {
		if (viewer.getSelectedArea() != null)
			viewer.showAreaLabels(viewer.getSelectedArea());
		else
			DialogBox.showMessageInformation(viewer, GUIMessages.getString("GUI.noSelectedArea"), GUIMessages.getString("GUI.noLabelInSelectedArea")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void hideAreaLabels() {
		if (viewer.getSelectedArea() != null)
			viewer.hideAreaLabels(viewer.getSelectedArea());
		else
			DialogBox.showMessageInformation(viewer, GUIMessages.getString("GUI.noSelectedArea"), GUIMessages.getString("GUI.noLabelInSelectedArea")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void duplicateViewer() {
		LatticeViewer duplicateViewer = new LatticeViewer((GraphicalLattice) viewer.getRootLattice().clone());
		LatticePanel duplicatePanel = duplicateViewer.getLatticePanel();
		duplicatePanel.getRootLattice().setScale(viewer.getRootLattice().getScale());
		duplicatePanel.getRootLattice().setRootPosition(viewer.getRootLattice().getRootPosition().getX(),
				viewer.getRootLattice().getRootPosition().getY());

		WindowListener[] listeners = getWindowListeners();
		for (int i = 0; i < listeners.length; i++)
			duplicateViewer.addWindowListener(listeners[i]);

		duplicateViewer.pack();
		duplicateViewer.setLocation((int) getLocation().getX() + 25, (int) getLocation().getY() + 25);
		duplicateViewer.setVisible(true);
	}

	/**
	 * Duplique la fenetre pour {@link GraphicalLattice} donne
	 * @param gl le GraphicalLattice a afficher dans la nouvelle fenetre
	 */
	public void duplicateViewer(GraphicalLattice gl) {
		LatticeViewer duplicateViewer = new LatticeViewer(gl);

		WindowListener[] listeners = getWindowListeners();
		for (int i = 0; i < listeners.length; i++)
			duplicateViewer.addWindowListener(listeners[i]);

		duplicateViewer.pack();
		duplicateViewer.setLocation((int) getLocation().getX() + 25, (int) getLocation().getY() + 25);
		duplicateViewer.setVisible(true);
	}

	private void showRulePanel() {
		ConceptLattice lattice;
		if (viewer.getRootLattice().getNestedLattice().getInternalLattices().size() == 1)
			lattice = viewer.getRootLattice().getNestedLattice().getConceptLattice();
		else {
			BinaryContext context = viewer.getRootLattice().getNestedLattice().getGlobalContext();
			lattice = new ConceptLattice(context);
		}

		String suppStr = (String) DialogBox.showInputQuestion(this, GUIMessages.getString("GUI.enterMinimumSupport")+" (%)", //$NON-NLS-1$ //$NON-NLS-2$
				GUIMessages.getString("GUI.minimumSupportForRules"), null, "50"); //$NON-NLS-1$ //$NON-NLS-2$
		if (suppStr == null)
			return;

		double minSupp = -1;
		while (minSupp < 0 || minSupp > 100) {
			try {
				minSupp = Double.parseDouble(suppStr);
			} catch (NumberFormatException ex) {
				DialogBox.showMessageWarning(this, GUIMessages.getString("GUI.valueMustBeBetween0And100"), //$NON-NLS-1$
						GUIMessages.getString("GUI.wrongSupportValue")); //$NON-NLS-1$
				minSupp = -1;
				suppStr = (String) DialogBox.showInputQuestion(this, GUIMessages.getString("GUI.enterMinimumSupport")+" (%)", //$NON-NLS-1$ //$NON-NLS-2$
						GUIMessages.getString("GUI.minimumSupportForRules"), null, "50"); //$NON-NLS-1$ //$NON-NLS-2$
				if (suppStr == null)
					return;
			}
		}

		String confStr = (String) DialogBox.showInputQuestion(this, GUIMessages.getString("GUI.enterMinimumConfidence")+" (%)", //$NON-NLS-1$ //$NON-NLS-2$
				GUIMessages.getString("GUI.minimumConfidenceForRules"), null, "50"); //$NON-NLS-1$ //$NON-NLS-2$
		if (confStr == null)
			return;

		double minConf = -1;
		while (minConf < 0 || minConf > 100) {
			try {
				minConf = Double.parseDouble(confStr);
			} catch (NumberFormatException ex) {
				DialogBox.showMessageWarning(this, GUIMessages.getString("GUI.valueMustBeBetween0And100"), //$NON-NLS-1$
						GUIMessages.getString("GUI.wrongConfidenceValue")); //$NON-NLS-1$
				minConf = -1;
				confStr = (String) DialogBox.showInputQuestion(this, GUIMessages.getString("GUI.enterMinimumConfidence")+" (%)", //$NON-NLS-1$ //$NON-NLS-2$
						GUIMessages.getString("GUI.minimumConfidenceForRules"), null, "50"); //$NON-NLS-1$ //$NON-NLS-2$
				if (confStr == null)
					return;
			}
		}

		// TODO A nettoyer!
//		RuleAlgorithm algo =  new GenericBasisAlgorithm(lattice, minSupp / 100.0);
		RuleAlgorithm algo = new InformativeBasisAlgorithm(lattice, minSupp / 100.0, minConf / 100.0);

		Vector<Rule> rules = algo.getRules();
//		if (minConf != 100.0) {
//			algo = new InformativeBasisAlgorithm(lattice, minSupp / 100.0, minConf / 100.0);
//			rules.addAll(algo.getRules());
//		}

		viewer.getRootLattice().getNestedLattice().setRules(algo.getRules());
		viewer.repaint();
		new RuleViewer(rules, viewer.getRootLattice().getNestedLattice().getName(),
				algo.getMinimumSupport(), algo.getMinimumConfidence(), viewer);
	}

	/**
	 * Ouvre le panneau de demande de nom pour le fichier image a enregistrer puis sauvegarde de
	 * l'image sous le nom de fichier specifie
	 */
	private void showCapturePanel() {

		// Creation du panneau de choix de fichier
		JFileChooser fileChooser = new JFileChooser(LMPreferences.getLastDirectory());

		// Propriétés du fileChooser
		fileChooser.setApproveButtonText(GUIMessages.getString("GUI.save")); //$NON-NLS-1$
		fileChooser.setDialogTitle(GUIMessages.getString("GUI.saveCurrentLatticeAsImage")); //$NON-NLS-1$
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Met un nom d'image par défaut
		fileChooser.setSelectedFile(new File(glCurrent.getName() + GUIMessages.getString("GUI.latticeDefaultImageName"))); //$NON-NLS-1$

		// Gere les extensions compatibles (bmp, jpg, png)
		ExampleFileFilter filterBMP = new ExampleFileFilter("bmp", GUIMessages.getString("GUI.bmpFormat")); //$NON-NLS-1$ //$NON-NLS-2$
		fileChooser.addChoosableFileFilter(filterBMP);
		ExampleFileFilter filterJPG = new ExampleFileFilter("jpg", GUIMessages.getString("GUI.jpgFormat")); //$NON-NLS-1$ //$NON-NLS-2$
		fileChooser.addChoosableFileFilter(filterJPG);
		ExampleFileFilter filterPNG = new ExampleFileFilter("png", GUIMessages.getString("GUI.pngFormat")); //$NON-NLS-1$ //$NON-NLS-2$
		fileChooser.addChoosableFileFilter(filterPNG);

		// La boite de dialogue
		int returnVal = fileChooser.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File fileName = fileChooser.getSelectedFile();

			// Recupere les extensions du filtre
			ExampleFileFilter currentFilter = (ExampleFileFilter) fileChooser.getFileFilter();
			ArrayList<String> extensions = currentFilter.getExtensionsList();

			// Recupere l'extension du fichier
			String oldFileType = ExampleFileFilter.getExtension(fileName);

			// Compare l'extension du fichier du fichier avec celle du filtre
			if (extensions != null && !extensions.contains(oldFileType)) {
				String newFileType = extensions.get(0);

				// Creer le nouveau fichier avec la bonne extension
				String oldFileName = fileName.getAbsolutePath();
				int posOldExt = oldFileName.lastIndexOf("."); //$NON-NLS-1$

				String newFileName = oldFileName + "." + newFileType; //$NON-NLS-1$
				if (posOldExt != -1)
					newFileName = newFileName.substring(0, posOldExt) + "." + newFileType; //$NON-NLS-1$

				fileName = new File(newFileName);
			}

			// Recupere la confirmation du nom de fichier
			if (fileName.exists()) {
				int overwrite = DialogBox.showDialogWarning(this, GUIMessages.getString("GUI.doYouWantToOverwriteFile"), //$NON-NLS-1$
						GUIMessages.getString("GUI.selectedFileAlreadyExist")); //$NON-NLS-1$

				if (overwrite == DialogBox.NO) {
					DialogBox.showMessageInformation(this, GUIMessages.getString("GUI.imageNotSaved"), GUIMessages.getString("GUI.notSaved")); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
			}

			// Ecrit le fichier image
			try {
				ScreenImage.createImage(viewer, fileName.getAbsolutePath());
				DialogBox.showMessageInformation(this, GUIMessages.getString("GUI.imageSuccessfullySaved"), GUIMessages.getString("GUI.saveSuccess")); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException ioe) {
				DialogBox.showMessageError(this, GUIMessages.getString("GUI.imageCouldnotBeSaved"), GUIMessages.getString("GUI.errorWithFile")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Sauvergarde le path utilise
			LMPreferences.setLastDirectory(fileChooser.getCurrentDirectory().getAbsolutePath());
		}
	}

	/**
	 * Ouvre une boite de dialogue pour sauvegarder le treillis
	 */
	private void saveCurrentLatticeAs() {

		// Fonctionne seulement sur un treillis non imbriqué
		if (!glCurrent.isNested()) {

			// Recupere le treillis non imbrique
			ConceptLattice lattice = glCurrent.getNestedLattice().getConceptLattice();

			JFileChooser fileChooser = new JFileChooser(LMPreferences.getLastDirectory());

			// Proprietes du fileChooser
			fileChooser.setApproveButtonText(GUIMessages.getString("GUI.saveAs")); //$NON-NLS-1$
			fileChooser.setDialogTitle(GUIMessages.getString("GUI.saveCurrentLattice")); //$NON-NLS-1$
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			// Gere les extensions compatibles (lmv, lmn, lmb)
			// et met un nom de fichier par defaut
			ExampleFileFilter filterGaliciaXml = new ExampleFileFilter("lat.xml", GUIMessages.getString("GUI.galiciaXMLLatticeFormat")); //$NON-NLS-1$ //$NON-NLS-2$
			fileChooser.addChoosableFileFilter(filterGaliciaXml);

			// La boite de dialogue
			int returnVal = fileChooser.showSaveDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File fileName = fileChooser.getSelectedFile();

				// Recupere les extensions du filtre
				ExampleFileFilter currentFilter = (ExampleFileFilter) fileChooser.getFileFilter();
				ArrayList<String> extensions = currentFilter.getExtensionsList();

				// Recupere l'extension du fichier
				String oldFileType = ExampleFileFilter.getExtension(fileName);
				String newFileType = oldFileType;

				// Compare l'extension du fichier du fichier avec celle du filtre
				if (extensions != null && !extensions.contains(oldFileType)) {
					newFileType = extensions.get(0);

					// Creer le nouveau fichier avec la bonne extension
					String oldFileName = fileName.getAbsolutePath();
					int posOldExt = oldFileName.lastIndexOf("."); //$NON-NLS-1$

					String newFileName = oldFileName + "." + newFileType; //$NON-NLS-1$
					if (posOldExt != -1)
						newFileName = newFileName.substring(0, posOldExt) + "." + newFileType; //$NON-NLS-1$

					fileName = new File(newFileName);
				}

				if (fileName.exists()) {
					int overwrite = DialogBox.showDialogWarning(this, GUIMessages.getString("GUI.doYouWantToOverwriteFile"), //$NON-NLS-1$
							GUIMessages.getString("GUI.selectedFileAlreadyExist")); //$NON-NLS-1$

					if (overwrite == DialogBox.NO) {
						DialogBox.showMessageInformation(this, GUIMessages.getString("GUI.latticeNotSaved"), GUIMessages.getString("GUI.notSaved")); //$NON-NLS-1$ //$NON-NLS-2$
						return;
					}
				}

				// Sauvegarde le context
				try {
					// Recupere l'extension du fichier
					String fileType = ExampleFileFilter.getExtension(fileName);

					if (fileType.equals("lat.xml")) { //$NON-NLS-1$
						new GaliciaXMLLatticeWriter(fileName, lattice);
					}

					else {
						DialogBox.showMessageError(this, GUIMessages.getString("GUI.latticeExtensionNotKnown"), //$NON-NLS-1$
								GUIMessages.getString("GUI.wrongLatticeFormat")); //$NON-NLS-1$
						return;
					}
					DialogBox.showMessageInformation(this, GUIMessages.getString("GUI.latticeSuccessfullySaved"), GUIMessages.getString("GUI.saveSuccess")); //$NON-NLS-1$ //$NON-NLS-2$

				} catch (IOException ioe) {
					DialogBox.showMessageError(this, GUIMessages.getString("GUI.latticeCouldnotBeSaved"), GUIMessages.getString("GUI.errorWithFile")); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (WriterException e) {
					DialogBox.showMessageError(this, e);
				}

				// Sauvergarde le path utilise
				LMPreferences.setLastDirectory(fileChooser.getCurrentDirectory().getAbsolutePath());
			}
		}
	}

	private boolean hasSetRulesInLabels() {
		viewer.addHistoryItem(LMHistory.RULES);
		RuleAlgorithm algo = calcRules();

		if (algo != null) {
			viewer.getRootLattice().getNestedLattice().setRules(algo.getRules());
			return true;
		}

		return false;
	}

	private RuleAlgorithm calcRules() {
		ConceptLattice lattice;
		if (viewer.getRootLattice().getNestedLattice().getInternalLattices().size() == 1)
			lattice = viewer.getRootLattice().getNestedLattice().getConceptLattice();
		else {
			BinaryContext context = viewer.getRootLattice().getNestedLattice().getGlobalContext();
			lattice = new ConceptLattice(context);
		}

		String suppStr = (String) DialogBox.showInputQuestion(this, GUIMessages.getString("GUI.enterMinimumSupport")+" (%)", //$NON-NLS-1$ //$NON-NLS-2$
				GUIMessages.getString("GUI.minimumSupportForRules"), null, "50"); //$NON-NLS-1$ //$NON-NLS-2$
		if (suppStr == null)
			return null;

		double minSupp = -1;
		while (minSupp < 0 || minSupp > 100) {
			try {
				minSupp = Double.parseDouble(suppStr);
			} catch (NumberFormatException ex) {
				DialogBox.showMessageWarning(this, GUIMessages.getString("GUI.valueMustBeBetween0And100"), //$NON-NLS-1$
						GUIMessages.getString("GUI.wrongSupportValue")); //$NON-NLS-1$
				minSupp = -1;
				suppStr = (String) DialogBox.showInputQuestion(this, GUIMessages.getString("GUI.enterMinimumSupport")+" (%)", //$NON-NLS-1$ //$NON-NLS-2$
						GUIMessages.getString("GUI.minimumSupportForRules"), null, "50"); //$NON-NLS-1$ //$NON-NLS-2$
				if (suppStr == null)
					return null;
			}
		}

		String confStr = (String) DialogBox.showInputQuestion(this, GUIMessages.getString("GUI.enterMinimumConfidence")+" (%)", //$NON-NLS-1$ //$NON-NLS-2$
				GUIMessages.getString("GUI.minimumConfidenceForRules"), null, "50"); //$NON-NLS-1$ //$NON-NLS-2$
		if (confStr == null)
			return null;

		double minConf = -1;
		while (minConf < 0 || minConf > 100) {
			try {
				minConf = Double.parseDouble(confStr);
			} catch (NumberFormatException ex) {
				DialogBox.showMessageWarning(this, GUIMessages.getString("GUI.valueMustBeBetween0And100"), //$NON-NLS-1$
						GUIMessages.getString("GUI.wrongConfidenceValue")); //$NON-NLS-1$
				minConf = -1;
				confStr = (String) DialogBox.showInputQuestion(this, GUIMessages.getString("GUI.enterMinimumConfidence")+" (%)", //$NON-NLS-1$ //$NON-NLS-2$
						GUIMessages.getString("GUI.minimumConfidenceForRules"), null, "50"); //$NON-NLS-1$ //$NON-NLS-2$
				if (confStr == null)
					return null;
			}
		}

		RuleAlgorithm algo = null;
//		if (minConf == 100.0){
//			algo = new GenericBasisAlgorithm(lattice, minSupp / 100.0);
//			algo = new InformativeBasisAlgorithm(lattice, minSupp / 100.0, minConf / 100.0);
//		}
//		else{
		algo = new InformativeBasisAlgorithm(lattice, minSupp / 100.0, minConf / 100.0);
//		}

		return algo;
	}

	public void setBackMessage(int action) {
		if (action == LMOptions.NONE) {
			previousBtn.setEnabled(false);
			frameMenu.setUndoOperation(false, null);
		} else {
			String msg = getHistoryMessage(action);
			previousBtn.setEnabled(true);
			previousBtn.setToolTipText(GUIMessages.getString("GUI.undo")+" (" + msg + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			frameMenu.setUndoOperation(true, msg);
		}
	}

	public void setForwardMessage(int action) {
		if (action == LMOptions.NONE) {
			nextBtn.setEnabled(false);
			frameMenu.setRedoOperation(false, null);
		} else {
			String msg = getHistoryMessage(action);
			nextBtn.setEnabled(true);
			nextBtn.setToolTipText(GUIMessages.getString("GUI.redo")+" (" + msg + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			frameMenu.setRedoOperation(true, msg);
		}
	}

	private String getHistoryMessage(int type) {
		switch (type) {
			case LMHistory.ALL:
				return GUIMessages.getString("GUI.historyAll"); //$NON-NLS-1$
			case LMHistory.LATTICE_PROJECTION:
				return GUIMessages.getString("GUI.historyLatticeprojection"); //$NON-NLS-1$
			case LMHistory.MOVE_LATTICE:
				return GUIMessages.getString("GUI.historyMoveLattice"); //$NON-NLS-1$
			case LMHistory.SCALE:
				return GUIMessages.getString("GUI.historyChangeScale"); //$NON-NLS-1$
			case LMHistory.SCALE_AND_MOVE:
				return GUIMessages.getString("GUI.historyChangeScaleAndMove"); //$NON-NLS-1$
			case LMHistory.SELECTION:
				return GUIMessages.getString("GUI.historySelectConcepts"); //$NON-NLS-1$
			case LMHistory.RULES:
				return GUIMessages.getString("GUI.historyChangeRules"); //$NON-NLS-1$
			case LMHistory.ATTRIBUTE_LABELS:
				return GUIMessages.getString("GUI.historyAttributeLabels"); //$NON-NLS-1$
			case LMHistory.OBJECT_LABELS:
				return GUIMessages.getString("GUI.historyObjectLabels"); //$NON-NLS-1$
			case LMHistory.RULE_LABELS:
				return GUIMessages.getString("GUI.historyRuleLabels"); //$NON-NLS-1$
			case LMHistory.ATTENTION:
				return GUIMessages.getString("GUI.historyShakeOrBlink"); //$NON-NLS-1$
			case LMHistory.CONTRAST:
				return GUIMessages.getString("GUI.historyBlurOrReduce"); //$NON-NLS-1$
			case LMHistory.SINGLE_SELECTION:
				return GUIMessages.getString("GUI.historySingleSelection"); //$NON-NLS-1$
			case LMHistory.MULTIPLE_SELECTION:
				return GUIMessages.getString("GUI.historyMultipleSelection"); //$NON-NLS-1$
			case LMHistory.GLOBAL_BOTTOM:
				return GUIMessages.getString("GUI.historyShowBottom"); //$NON-NLS-1$
			case LMHistory.ANIMATION:
				return GUIMessages.getString("GUI.historyShowAnimation"); //$NON-NLS-1$
			case LMHistory.STRUCTURE:
				return GUIMessages.getString("GUI.historyLatticeStructure"); //$NON-NLS-1$
			case LMHistory.COLOR:
				return GUIMessages.getString("GUI.historyColorIntensity"); //$NON-NLS-1$
			case LMHistory.SIZE:
				return GUIMessages.getString("GUI.historyConceptSize"); //$NON-NLS-1$
			case LMHistory.SELECT_NODE:
				return GUIMessages.getString("GUI.historySelectConcept"); //$NON-NLS-1$
			case LMHistory.DESELECT_NODE:
				return GUIMessages.getString("GUI.historyDeselectConcept"); //$NON-NLS-1$
			case LMHistory.CHANGE_OPTIONS:
				return GUIMessages.getString("GUI.historyPresetOptions"); //$NON-NLS-1$
			case LMHistory.APPROXIMATION:
				return GUIMessages.getString("GUI.historyLatticeApproximation"); //$NON-NLS-1$
			default:
				return GUIMessages.getString("GUI.historyUnknow"); //$NON-NLS-1$
		}
	}

	/**
	 * Sauvegarde les options courantes comme celles de l'utilisateur
	 */
	private void saveCurrentOptions() {
		// Recupere les preferences de Lattice Miner
		Preferences preferences = LMPreferences.getPreferences();

		// Sauvegarde les options courantes
		//----------------------------------------------
		preferences.putInt(LMPreferences.SINGLE_SEL_TYPE, viewer.getSingleSelType());
		preferences.putInt(LMPreferences.MULT_SEL_TYPE, viewer.getMultSelType());
		preferences.putInt(LMPreferences.SEL_CONTRAST_TYPE, viewer.getSelectionContrastType());
		//----------------------------------------------
		preferences.putBoolean(LMPreferences.HIDE_OUT_OF_FOCUS, viewer.isHideLabelForOutOfFocusConcept());
		preferences.putInt(LMPreferences.ATT_LABEL_TYPE, viewer.getAttLabelType());
		preferences.putInt(LMPreferences.OBJ_LABEL_TYPE, viewer.getObjLabelType());
		preferences.putInt(LMPreferences.RULES_LABEL_TYPE, viewer.getRulesLabelType());
		//----------------------------------------------
		preferences.putBoolean(LMPreferences.ANIMATE_ZOOM, viewer.isAnimateZoom());
		preferences.putInt(LMPreferences.FEATURE_TYPE, viewer.getAttentionFeatureType());
		//----------------------------------------------
		preferences.putBoolean(LMPreferences.CHANGE_COLOR_INTENSITY, viewer.isChangeColorIntensity());
		preferences.putInt(LMPreferences.CONCEPT_SIZE_TYPE, viewer.getConceptSizeType());
		//----------------------------------------------
		preferences.putBoolean(LMPreferences.SHOW_ALL_CONCEPTS, !viewer.isBottomHidden());
		preferences.putBoolean(LMPreferences.SHOW_LATTICE_MAP, viewer.isShowLatticeMap());

		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			DialogBox.showMessageError(this, GUIMessages.getString("GUI.problemDuringBackupOptions"), GUIMessages.getString("GUI.savingProblem")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void changeMenuItem(int item, int value) {
		frameMenu.selectMenuItem(item, value);
	}

	/**
	 * Classe gerant le menu du {@link LatticeViewer}
	 * @author Ludovic Thomas
	 * @version 1.0
	 */
	private class FrameMenu {

		boolean emptyBottomsHidden;

		/* Boutons du menu */
		JMenuItem saveLattice;
		JMenuItem exportLattice;
		JMenuItem quitFrame;
		ButtonGroup optionsGroup;
		JMenuItem lightOptions;
		JMenuItem heavyOptions;
		JMenuItem userOptions;
		JMenuItem userSaveOptions;

		JRadioButtonMenuItem noSingleSel;
		JRadioButtonMenuItem singleSelFilter;
		JRadioButtonMenuItem singleSelIdeal;
		JRadioButtonMenuItem singleSelFilterIdeal;
		JRadioButtonMenuItem singleSelParents;
		JRadioButtonMenuItem singleSelChildren;
		JRadioButtonMenuItem singleSelParentsChildren;
		JRadioButtonMenuItem singleSelChildParents;
		JRadioButtonMenuItem noMultipleSel;
		JRadioButtonMenuItem multipleSelFilter;
		JRadioButtonMenuItem multipleSelIdeal;
		JRadioButtonMenuItem multipleSelFilterIdeal;
		JRadioButtonMenuItem multipleSelSubLattice;
		JRadioButtonMenuItem blurItem;
		JRadioButtonMenuItem fisheyeItem;
		JCheckBoxMenuItem hideOutFocusLabelItem;
		JRadioButtonMenuItem noAttributesItem;
		JRadioButtonMenuItem attReducedItem;
		JRadioButtonMenuItem attListItem;
		JRadioButtonMenuItem noObjectsItem;
		JRadioButtonMenuItem objReducedItem;
		JRadioButtonMenuItem objListItem;
		JRadioButtonMenuItem objCountItem;
		JRadioButtonMenuItem objPercAllItem;
		JRadioButtonMenuItem objPercNodeItem;
		JRadioButtonMenuItem noRulesItem;
		JRadioButtonMenuItem showRulesItem;

		JMenuItem setRulesItem;
		JCheckBoxMenuItem zoomAnimItem;
		JRadioButtonMenuItem shakeAnimationItem;
		JRadioButtonMenuItem blinkAnimationItem;
		JRadioButtonMenuItem noSearchAnimationItem;
		JRadioButtonMenuItem differentIntensityItem;
		JRadioButtonMenuItem sameIntensityItem;
		JRadioButtonMenuItem largeSizeItem;
		JRadioButtonMenuItem smallSizeItem;
		JRadioButtonMenuItem varySizeItem;

		JMenuItem undoOperation;
		JMenuItem redoOperation;
		JMenuItem zoomInItem;
		JMenuItem zoomOutItem;
		JMenuItem zoomSelectItem;
		JMenuItem zoomLatticeItem;
		JMenuItem showAllLabelsItem;
		JMenuItem hideAreaLabelsItem;
		JMenuItem showAreaLabelsItem;
		JMenuItem hideAllLabelsItem;
		JMenuItem duplicateItem;
		JMenuItem showBottomItem;
		JMenuItem showLatticeMapItem;
		JMenuItem latticeStructureItem;
		JMenuItem rulesItem;

		JMenuBar menuBar;
		JMenu fileMenu;
		JMenu editMenu;
		JMenu optionsMenu;
		JMenu toolsMenu;

		JRadioButtonMenuItem noGeneItem;
		JRadioButtonMenuItem showGeneItem;

		/**
		 * Construit un {@link FrameMenu} avec les options utilisateurs par défaut
		 */
		public FrameMenu() {
			buildMenuBar();

			// Desactive varySizeItem si pas de treillis imbrique
			if (glCurrent.getNestedLattice().getInternalLattices().size() > 0)
				varySizeItem.setEnabled(false);
		}

		/**
		 * @return la menuBar
		 */
		public JMenuBar getMenuBar() {
			return menuBar;
		}

		/**
		 * Construit la menuBar
		 */
		private void buildMenuBar() {
			JPopupMenu.setDefaultLightWeightPopupEnabled(false);
			FrameMenuListener listener = new FrameMenuListener();

			menuBar = new JMenuBar();
			fileMenu = new JMenu(GUIMessages.getString("GUI.file")); //$NON-NLS-1$
			fileMenu.setMnemonic(KeyEvent.VK_F);

			editMenu = new JMenu(GUIMessages.getString("GUI.edit")); //$NON-NLS-1$
			editMenu.setMnemonic(KeyEvent.VK_E);

			optionsMenu = new JMenu(GUIMessages.getString("GUI.options")); //$NON-NLS-1$
			optionsMenu.setMnemonic(KeyEvent.VK_P);

			toolsMenu = new JMenu(GUIMessages.getString("GUI.tools")); //$NON-NLS-1$
			toolsMenu.setMnemonic(KeyEvent.VK_T);
			//FrameMenuListener menuListener = new FrameMenuListener();

			/* ==== FILE MENU ==== */
			duplicateItem = new JMenuItem(GUIMessages.getString("GUI.duplicateWindow")); //$NON-NLS-1$
			duplicateItem.addActionListener(listener);
			duplicateItem.setMnemonic(KeyEvent.VK_D);
			duplicateItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			fileMenu.add(duplicateItem);

			fileMenu.addSeparator();

			// On ne peut sauvegarder qu'un treillis non imbrique
			if (!glCurrent.isNested()) {
				saveLattice = new JMenuItem(GUIMessages.getString("GUI.saveAs")); //$NON-NLS-1$
				saveLattice.addActionListener(listener);
				saveLattice.setMnemonic(KeyEvent.VK_S);
				saveLattice.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
				fileMenu.add(saveLattice);
			}

			exportLattice = new JMenuItem(GUIMessages.getString("GUI.exportAsImage")); //$NON-NLS-1$
			exportLattice.addActionListener(listener);
			exportLattice.setMnemonic(KeyEvent.VK_E);
			exportLattice.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			fileMenu.add(exportLattice);

			fileMenu.addSeparator();

			quitFrame = new JMenuItem(GUIMessages.getString("GUI.close")); //$NON-NLS-1$
			quitFrame.addActionListener(listener);
			quitFrame.setMnemonic(KeyEvent.VK_W);
			quitFrame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			fileMenu.add(quitFrame);

			/* ==== EDIT MENU ==== */

			undoOperation = new JMenuItem(GUIMessages.getString("GUI.undo")); //$NON-NLS-1$
			undoOperation.addActionListener(listener);
			undoOperation.setMnemonic(KeyEvent.VK_U);
			undoOperation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			undoOperation.setEnabled(false);
			editMenu.add(undoOperation);

			redoOperation = new JMenuItem(GUIMessages.getString("GUI.redo")); //$NON-NLS-1$
			redoOperation.addActionListener(listener);
			redoOperation.setMnemonic(KeyEvent.VK_R);
			redoOperation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			redoOperation.setEnabled(false);
			editMenu.add(redoOperation);

			editMenu.addSeparator();

			JMenu zoomMenu = new JMenu(GUIMessages.getString("GUI.zoom")); //$NON-NLS-1$
			zoomMenu.setMnemonic(KeyEvent.VK_Z);

			zoomInItem = new JMenuItem(GUIMessages.getString("GUI.zoomIn")); //$NON-NLS-1$
			zoomInItem.addActionListener(listener);
			zoomInItem.setMnemonic(KeyEvent.VK_I);
			zoomInItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			zoomMenu.add(zoomInItem);

			zoomOutItem = new JMenuItem(GUIMessages.getString("GUI.zoomOut")); //$NON-NLS-1$
			zoomOutItem.addActionListener(listener);
			zoomOutItem.setMnemonic(KeyEvent.VK_O);
			zoomOutItem.setDisplayedMnemonicIndex(5);
			zoomOutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			zoomMenu.add(zoomOutItem);

			zoomMenu.addSeparator();

			zoomSelectItem = new JMenuItem(GUIMessages.getString("GUI.zoomInSelectedArea")); //$NON-NLS-1$
			zoomSelectItem.addActionListener(listener);
			zoomSelectItem.setMnemonic(KeyEvent.VK_S);
			zoomSelectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
					InputEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			zoomMenu.add(zoomSelectItem);

			zoomLatticeItem = new JMenuItem(GUIMessages.getString("GUI.showAllEntireLattice")); //$NON-NLS-1$
			zoomLatticeItem.addActionListener(listener);
			zoomLatticeItem.setMnemonic(KeyEvent.VK_A);
			zoomLatticeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
					InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			zoomMenu.add(zoomLatticeItem);

			editMenu.add(zoomMenu);

			JMenu showLabelsMenu = new JMenu(GUIMessages.getString("GUI.labels")); //$NON-NLS-1$
			showLabelsMenu.setMnemonic(KeyEvent.VK_L);

			showAllLabelsItem = new JMenuItem(GUIMessages.getString("GUI.showAllLabels")); //$NON-NLS-1$
			showAllLabelsItem.addActionListener(listener);
			showLabelsMenu.add(showAllLabelsItem);
			hideAllLabelsItem = new JMenuItem(GUIMessages.getString("GUI.hideAllLabels")); //$NON-NLS-1$
			hideAllLabelsItem.addActionListener(listener);
			showLabelsMenu.add(hideAllLabelsItem);
			showLabelsMenu.addSeparator();
			showAreaLabelsItem = new JMenuItem(GUIMessages.getString("GUI.showLabelsInSelectedArea")); //$NON-NLS-1$
			showAreaLabelsItem.addActionListener(listener);
			showLabelsMenu.add(showAreaLabelsItem);
			hideAreaLabelsItem = new JMenuItem(GUIMessages.getString("GUI.hideLabelsInSelectedArea")); //$NON-NLS-1$
			hideAreaLabelsItem.addActionListener(listener);
			showLabelsMenu.add(hideAreaLabelsItem);

			editMenu.add(showLabelsMenu);

			/* ==== PRESETS MENU ==== */
			JMenu presetsMenu = new JMenu(GUIMessages.getString("GUI.presetOptions")); //$NON-NLS-1$
			presetsMenu.setMnemonic(KeyEvent.VK_P);

			optionsGroup = new ButtonGroup();

			lightOptions = new JRadioButtonMenuItem(GUIMessages.getString("GUI.lightOptions")); //$NON-NLS-1$
			lightOptions.addActionListener(listener);
			lightOptions.setMnemonic(KeyEvent.VK_1);
			//lightOptions.setAccelerator(KeyStroke.getKeyStroke("ctrl NUMPAD1")); //$NON-NLS-1$
			lightOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			optionsGroup.add(lightOptions);
			presetsMenu.add(lightOptions);

			heavyOptions = new JRadioButtonMenuItem(GUIMessages.getString("GUI.heavyOptions")); //$NON-NLS-1$
			heavyOptions.addActionListener(listener);
			heavyOptions.setMnemonic(KeyEvent.VK_2);
			//heavyOptions.setAccelerator(KeyStroke.getKeyStroke("ctrl NUMPAD2")); //$NON-NLS-1$
			heavyOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			optionsGroup.add(heavyOptions);
			presetsMenu.add(heavyOptions);

			userOptions = new JRadioButtonMenuItem(GUIMessages.getString("GUI.userOptions")); //$NON-NLS-1$
			userOptions.addActionListener(listener);
			userOptions.setMnemonic(KeyEvent.VK_3);
			//userOptions.setAccelerator(KeyStroke.getKeyStroke("ctrl NUMPAD3")); //$NON-NLS-1$
			userOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			optionsGroup.add(userOptions);
			presetsMenu.add(userOptions);

			presetsMenu.addSeparator();

			userSaveOptions = new JMenuItem(GUIMessages.getString("GUI.saveCurrentOptionsAsUsers")); //$NON-NLS-1$
			userSaveOptions.addActionListener(listener);
			presetsMenu.add(userSaveOptions);

			optionsMenu.add(presetsMenu);

			optionsMenu.addSeparator();

			/* ==== SELECTIONS MENU ==== */
			JMenu selectionsMenu = new JMenu(GUIMessages.getString("GUI.selections")); //$NON-NLS-1$
			selectionsMenu.setMnemonic(KeyEvent.VK_S);

			JMenu singleSelMenu = new JMenu(GUIMessages.getString("GUI.singleConcept")); //$NON-NLS-1$
			ButtonGroup singleSelGroup = new ButtonGroup();
			noSingleSel = new JRadioButtonMenuItem(GUIMessages.getString("GUI.conceptOnly")); //$NON-NLS-1$
			noSingleSel.addActionListener(listener);
			singleSelGroup.add(noSingleSel);
			singleSelMenu.add(noSingleSel);
			singleSelFilter = new JRadioButtonMenuItem(GUIMessages.getString("GUI.showFilter")); //$NON-NLS-1$
			singleSelFilter.addActionListener(listener);
			singleSelGroup.add(singleSelFilter);
			singleSelMenu.add(singleSelFilter);
			singleSelIdeal = new JRadioButtonMenuItem(GUIMessages.getString("GUI.showIdeal")); //$NON-NLS-1$
			singleSelIdeal.addActionListener(listener);
			singleSelGroup.add(singleSelIdeal);
			singleSelMenu.add(singleSelIdeal);
			singleSelFilterIdeal = new JRadioButtonMenuItem(GUIMessages.getString("GUI.showFilterAndIdeal")); //$NON-NLS-1$
			singleSelFilterIdeal.addActionListener(listener);
			singleSelGroup.add(singleSelFilterIdeal);
			singleSelMenu.add(singleSelFilterIdeal);
			singleSelParents = new JRadioButtonMenuItem(GUIMessages.getString("GUI.showParents")); //$NON-NLS-1$
			singleSelParents.addActionListener(listener);
			singleSelGroup.add(singleSelParents);
			singleSelMenu.add(singleSelParents);
			singleSelChildren = new JRadioButtonMenuItem(GUIMessages.getString("GUI.showChildren")); //$NON-NLS-1$
			singleSelChildren.addActionListener(listener);
			singleSelGroup.add(singleSelChildren);
			singleSelMenu.add(singleSelChildren);
			singleSelParentsChildren = new JRadioButtonMenuItem(GUIMessages.getString("GUI.showParentsAndChildren")); //$NON-NLS-1$
			singleSelParentsChildren.addActionListener(listener);
			singleSelGroup.add(singleSelParentsChildren);
			singleSelMenu.add(singleSelParentsChildren);
			//singleSelChildParents = new JRadioButtonMenuItem("Show children's parents");
			//singleSelChildParents.addActionListener(listener);
			//singleSelGroup.add(singleSelChildParents);
			//singleSelMenu.add(singleSelChildParents);
			selectionsMenu.add(singleSelMenu);

			JMenu multipleSelMenu = new JMenu(GUIMessages.getString("GUI.multipleConcepts")); //$NON-NLS-1$
			ButtonGroup multipleSelGroup = new ButtonGroup();
			noMultipleSel = new JRadioButtonMenuItem(GUIMessages.getString("GUI.conceptsOnly")); //$NON-NLS-1$
			noMultipleSel.addActionListener(listener);
			multipleSelGroup.add(noMultipleSel);
			multipleSelMenu.add(noMultipleSel);
			multipleSelFilter = new JRadioButtonMenuItem(GUIMessages.getString("GUI.showCommonFilter")); //$NON-NLS-1$
			multipleSelFilter.addActionListener(listener);
			multipleSelGroup.add(multipleSelFilter);
			multipleSelMenu.add(multipleSelFilter);
			multipleSelIdeal = new JRadioButtonMenuItem(GUIMessages.getString("GUI.showCommonIdeal")); //$NON-NLS-1$
			multipleSelIdeal.addActionListener(listener);
			multipleSelGroup.add(multipleSelIdeal);
			multipleSelMenu.add(multipleSelIdeal);
			multipleSelFilterIdeal = new JRadioButtonMenuItem(GUIMessages.getString("GUI.showCommonFilterAndIdeal")); //$NON-NLS-1$
			multipleSelFilterIdeal.addActionListener(listener);
			multipleSelGroup.add(multipleSelFilterIdeal);
			multipleSelMenu.add(multipleSelFilterIdeal);
			multipleSelSubLattice = new JRadioButtonMenuItem(GUIMessages.getString("GUI.showSubLattice")); //$NON-NLS-1$
			multipleSelSubLattice.addActionListener(listener);
			multipleSelGroup.add(multipleSelSubLattice);
			multipleSelMenu.add(multipleSelSubLattice);
			selectionsMenu.add(multipleSelMenu);
			selectionsMenu.addSeparator();

			ButtonGroup selectionGroup = new ButtonGroup();
			blurItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.blurUnrelatedConcepts")); //$NON-NLS-1$
			blurItem.addActionListener(listener);
			selectionGroup.add(blurItem);
			selectionsMenu.add(blurItem);
			fisheyeItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.reduceUnrelatedConcepts")); //$NON-NLS-1$
			fisheyeItem.addActionListener(listener);
			selectionGroup.add(fisheyeItem);
			selectionsMenu.add(fisheyeItem);

			optionsMenu.add(selectionsMenu);

			/* ==== LABELS MENU ==== */
			JMenu labelsMenu = new JMenu(GUIMessages.getString("GUI.labelInformation")); //$NON-NLS-1$
			labelsMenu.setMnemonic(KeyEvent.VK_L);

			hideOutFocusLabelItem = new JCheckBoxMenuItem(GUIMessages.getString("GUI.hideLabelForBluredConcept")); //$NON-NLS-1$
			hideOutFocusLabelItem.addActionListener(listener);
			hideOutFocusLabelItem.setMnemonic(KeyEvent.VK_H);
			hideOutFocusLabelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
					InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			labelsMenu.add(hideOutFocusLabelItem);
			labelsMenu.addSeparator();
			JMenu attributesMenu = new JMenu(GUIMessages.getString("GUI.attributes")); //$NON-NLS-1$
			ButtonGroup attLabelsGroup = new ButtonGroup();
			noAttributesItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.dontShowAttributes")); //$NON-NLS-1$
			noAttributesItem.addActionListener(listener);
			attLabelsGroup.add(noAttributesItem);
			attributesMenu.add(noAttributesItem);
			attReducedItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.reducedLabelling")); //$NON-NLS-1$
			attReducedItem.addActionListener(listener);
			attLabelsGroup.add(attReducedItem);
			attributesMenu.add(attReducedItem);
			attListItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.completeList")); //$NON-NLS-1$
			attListItem.addActionListener(listener);
			attLabelsGroup.add(attListItem);

			attributesMenu.add(attListItem);
			labelsMenu.add(attributesMenu);

			JMenu objectsMenu = new JMenu(GUIMessages.getString("GUI.objects")); //$NON-NLS-1$
			ButtonGroup objLabelsGroup = new ButtonGroup();
			noObjectsItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.dontShowObjects")); //$NON-NLS-1$
			noObjectsItem.addActionListener(listener);
			objLabelsGroup.add(noObjectsItem);
			objectsMenu.add(noObjectsItem);
			objReducedItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.reducedLabelling")); //$NON-NLS-1$
			objReducedItem.addActionListener(listener);
			objLabelsGroup.add(objReducedItem);
			objectsMenu.add(objReducedItem);
			objListItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.completeList")); //$NON-NLS-1$
			objListItem.addActionListener(listener);
			objLabelsGroup.add(objListItem);
			objectsMenu.add(objListItem);
			objCountItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.count")); //$NON-NLS-1$
			objCountItem.addActionListener(listener);
			objLabelsGroup.add(objCountItem);
			objectsMenu.add(objCountItem);
			objPercAllItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.purcentOfAllObjects")); //$NON-NLS-1$
			objPercAllItem.addActionListener(listener);
			objLabelsGroup.add(objPercAllItem);
			objectsMenu.add(objPercAllItem);
			objPercNodeItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.purcentOfExternalObjects")); //$NON-NLS-1$
			objPercNodeItem.addActionListener(listener);
			objLabelsGroup.add(objPercNodeItem);
			objectsMenu.add(objPercNodeItem);
			labelsMenu.add(objectsMenu);

			JMenu geneMenu = new JMenu(GUIMessages.getString("GUI.generators")); //$NON-NLS-1$
			ButtonGroup geneLabelsGroup = new ButtonGroup();
			noGeneItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.dontShowGene")); //$NON-NLS-1$
			noGeneItem.setSelected(true);
			noGeneItem.addActionListener(listener);
			geneLabelsGroup.add(noGeneItem);
			geneMenu.add(noGeneItem);
			showGeneItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.showGene")); //$NON-NLS-1$
			showGeneItem.addActionListener(listener);
			geneLabelsGroup.add(showGeneItem);
			geneMenu.add(showGeneItem);
			labelsMenu.add(geneMenu);



			JMenu rulesMenu = new JMenu(GUIMessages.getString("GUI.rules")); //$NON-NLS-1$
			setRulesItem = new JMenuItem(GUIMessages.getString("GUI.setRules")); //$NON-NLS-1$
			setRulesItem.addActionListener(listener);
			rulesMenu.add(setRulesItem);
			rulesMenu.addSeparator();
			ButtonGroup rulesLabelsGroup = new ButtonGroup();
			noRulesItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.dontShowRules")); //$NON-NLS-1$
			noRulesItem.addActionListener(listener);
			noRulesItem.setEnabled(false);
			rulesLabelsGroup.add(noRulesItem);
			rulesMenu.add(noRulesItem);
			showRulesItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.showRules")); //$NON-NLS-1$
			showRulesItem.addActionListener(listener);
			showRulesItem.setEnabled(false);
			rulesLabelsGroup.add(showRulesItem);
			rulesMenu.add(showRulesItem);
			labelsMenu.add(rulesMenu);

			optionsMenu.add(labelsMenu);

			/* ==== ANIMATIONS MENU ==== */
			JMenu animationMenu = new JMenu(GUIMessages.getString("GUI.animations")); //$NON-NLS-1$
			animationMenu.setMnemonic(KeyEvent.VK_A);

			zoomAnimItem = new JCheckBoxMenuItem(GUIMessages.getString("GUI.enableZoomAnimation")); //$NON-NLS-1$
			zoomAnimItem.addActionListener(listener);
			zoomAnimItem.setMnemonic(KeyEvent.VK_A);
			zoomAnimItem.setDisplayedMnemonicIndex(12);
			animationMenu.add(zoomAnimItem);
			animationMenu.addSeparator();
			ButtonGroup searchGroup = new ButtonGroup();
			shakeAnimationItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.shakeFoundConcept")); //$NON-NLS-1$
			shakeAnimationItem.addActionListener(listener);
			searchGroup.add(shakeAnimationItem);
			animationMenu.add(shakeAnimationItem);
			blinkAnimationItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.blinkFoundConcept")); //$NON-NLS-1$
			blinkAnimationItem.addActionListener(listener);
			searchGroup.add(blinkAnimationItem);
			animationMenu.add(blinkAnimationItem);
			noSearchAnimationItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.noAnimationForFoundConcept")); //$NON-NLS-1$
			noSearchAnimationItem.addActionListener(listener);
			searchGroup.add(noSearchAnimationItem);
			animationMenu.add(noSearchAnimationItem);

			optionsMenu.add(animationMenu);

			optionsMenu.addSeparator();

			/* ==== CONCEPT INTENSITY MENU ==== */
			JMenu conceptIntensityMenu = new JMenu(GUIMessages.getString("GUI.conceptColor")); //$NON-NLS-1$
			conceptIntensityMenu.setMnemonic(KeyEvent.VK_C);

			ButtonGroup intensityGroup = new ButtonGroup();
			differentIntensityItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.intensityDependsOnObjectCount")); //$NON-NLS-1$
			differentIntensityItem.addActionListener(listener);
			intensityGroup.add(differentIntensityItem);
			conceptIntensityMenu.add(differentIntensityItem);
			sameIntensityItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.sameIntensityForAllConcepts")); //$NON-NLS-1$
			sameIntensityItem.addActionListener(listener);
			intensityGroup.add(sameIntensityItem);
			conceptIntensityMenu.add(sameIntensityItem);

			optionsMenu.add(conceptIntensityMenu);

			/* === CONCEPT SIZE MENU === */
			JMenu conceptSizeMenu = new JMenu(GUIMessages.getString("GUI.conceptSize")); //$NON-NLS-1$
			conceptSizeMenu.setMnemonic(KeyEvent.VK_C);

			ButtonGroup sizeGroup = new ButtonGroup();
			largeSizeItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.sameForAllConceptsLarge")); //$NON-NLS-1$
			largeSizeItem.addActionListener(listener);
			sizeGroup.add(largeSizeItem);
			conceptSizeMenu.add(largeSizeItem);
			smallSizeItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.sameForAllConceptsSmall")); //$NON-NLS-1$
			smallSizeItem.addActionListener(listener);
			sizeGroup.add(smallSizeItem);
			conceptSizeMenu.add(smallSizeItem);
			varySizeItem = new JRadioButtonMenuItem(GUIMessages.getString("GUI.dependsOnOwnedObjects")); //$NON-NLS-1$
			varySizeItem.addActionListener(listener);
			sizeGroup.add(varySizeItem);
			conceptSizeMenu.add(varySizeItem);

			optionsMenu.add(conceptSizeMenu);

			optionsMenu.addSeparator();

			/* === HIDE/SHOW INFIMUM/MAP MENU === */

			showBottomItem = new JMenuItem(GUIMessages.getString("GUI.hideEmptyInfimum")); //$NON-NLS-1$
			showBottomItem.addActionListener(listener);
			showBottomItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			optionsMenu.add(showBottomItem);

			showLatticeMapItem = new JMenuItem(GUIMessages.getString("GUI.hideLatticeMap")); //$NON-NLS-1$
			showLatticeMapItem.addActionListener(listener);
			showLatticeMapItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			optionsMenu.add(showLatticeMapItem);

			/* ==== TOOLS MENU ==== */

			//projectionItem = new JMenuItem("Projection");
			//projectionItem.addActionListener(listener);
			//toolsMenu.add(projectionItem);
			//searchItem = new JMenuItem("Search");
			//searchItem.addActionListener(listener);
			//toolsMenu.add(searchItem);
			latticeStructureItem = new JMenuItem(GUIMessages.getString("GUI.modifyLatticeStructure")); //$NON-NLS-1$
			latticeStructureItem.addActionListener(listener);
			latticeStructureItem.setMnemonic(KeyEvent.VK_M);
			latticeStructureItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			toolsMenu.add(latticeStructureItem);

			rulesItem = new JMenuItem(GUIMessages.getString("GUI.showRules")); //$NON-NLS-1$
			rulesItem.addActionListener(listener);
			rulesItem.setMnemonic(KeyEvent.VK_R);
			rulesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			toolsMenu.add(rulesItem);

			menuBar.add(fileMenu);
			menuBar.add(editMenu);
			menuBar.add(optionsMenu);
			menuBar.add(toolsMenu);
		}

		/**
		 * Selectionne les bons menus selon certaines actions
		 * @param item le type d'item a selectionner via LMHistory
		 * @param value la valeur de l'item
		 */
		protected void selectMenuItem(int item, int value) {
			switch (item) {
				case LMHistory.ATTRIBUTE_LABELS:
					switch (value) {
						case LMOptions.NO_LABEL:
							noAttributesItem.setSelected(true);
							break;
						case LMOptions.ATTRIBUTES_ALL:
							attListItem.setSelected(true);
							break;
						case LMOptions.ATTRIBUTES_REDUCED:
						default:
							attReducedItem.setSelected(true);
							break;
					}
					break;
				case LMHistory.OBJECT_LABELS:
					switch (value) {
						case LMOptions.NO_LABEL:
							noObjectsItem.setSelected(true);
							break;
						case LMOptions.OBJECTS_REDUCED:
							objReducedItem.setSelected(true);
							break;
						case LMOptions.OBJECTS_ALL:
							objListItem.setSelected(true);
							break;
						case LMOptions.OBJECTS_NUMBER:
							objCountItem.setSelected(true);
							break;
						case LMOptions.OBJECTS_PERC_CTX:
							objPercAllItem.setSelected(true);
							break;
						case LMOptions.OBJECTS_PERC_NODE:
						default:
							objPercNodeItem.setSelected(true);
							break;
					}
					break;
				case LMHistory.RULE_LABELS:
					switch (value) {
						case LMOptions.NO_LABEL:
							noRulesItem.setSelected(true);
							break;
						case LMOptions.RULES_SHOW:
						default:
							showRulesItem.setSelected(true);
							break;
					}
					break;
				case LMHistory.GENE_LABELS:
					switch (value) {
						case LMOptions.NO_LABEL:
							noGeneItem.setSelected(true);
							break;
						case LMOptions.GENE_SHOW:
						default:
							showGeneItem.setSelected(true);
							break;
					}
					break;
				case LMHistory.ATTENTION:
					switch (value) {
						case LMOptions.NONE:
							noSearchAnimationItem.setSelected(true);
							break;
						case LMOptions.SHAKE:
							shakeAnimationItem.setSelected(true);
							break;
						case LMOptions.BLINK:
						default:
							blinkAnimationItem.setSelected(true);
							break;
					}
					break;
				case LMHistory.CONTRAST:
					switch (value) {
						case LMOptions.BLUR:
							blurItem.setSelected(true);
							break;
						case LMOptions.FISHEYE:
						default:
							fisheyeItem.setSelected(true);
							break;
					}
					break;
				case LMHistory.SINGLE_SELECTION:
					switch (value) {
						case LMOptions.NONE:
							noSingleSel.setSelected(true);
							break;
						case LMOptions.FILTER:
							singleSelFilter.setSelected(true);
							break;
						case LMOptions.IDEAL:
							singleSelIdeal.setSelected(true);
							break;
						case LMOptions.FILTER_IDEAL:
							singleSelFilterIdeal.setSelected(true);
							break;
						case LMOptions.PARENTS:
							singleSelParents.setSelected(true);
							break;
						case LMOptions.CHILDREN:
							singleSelChildren.setSelected(true);
							break;
						case LMOptions.PARENTS_CHILDREN:
							singleSelParentsChildren.setSelected(true);
							break;
						case LMOptions.CHILDREN_PARENTS:
						default:
							singleSelChildParents.setSelected(true);
							break;
					}
					break;
				case LMHistory.MULTIPLE_SELECTION:
					switch (value) {
						case LMOptions.NONE:
							noMultipleSel.setSelected(true);
							break;
						case LMOptions.COMMON_FILTER:
							multipleSelFilter.setSelected(true);
							break;
						case LMOptions.COMMON_IDEAL:
							multipleSelIdeal.setSelected(true);
							break;
						case LMOptions.COMMON_FILTER_IDEAL:
							multipleSelFilterIdeal.setSelected(true);
							break;
						case LMOptions.SUB_LATTICE:
						default:
							multipleSelSubLattice.setSelected(true);
							break;
					}
					break;
				case LMHistory.GLOBAL_BOTTOM:
					switch (value) {
						case LMOptions.NONE:
							showBottomItem.setSelected(false);
							break;
						case LMOptions.SHOW:
						default:
							showBottomItem.setSelected(true);
							break;
					}
					break;
				case LMHistory.ANIMATION:
					switch (value) {
						case LMOptions.NONE:
							zoomAnimItem.setSelected(false);
							break;
						case LMOptions.ANIMATION_OK:
						default:
							zoomAnimItem.setSelected(true);
							break;
					}
					break;
				case LMHistory.COLOR:
					switch (value) {
						case LMOptions.CHANGE:
							differentIntensityItem.setSelected(true);
							break;
						case LMOptions.SAME:
						default:
							sameIntensityItem.setSelected(true);
							break;
					}
					break;
				case LMHistory.SIZE:
					switch (value) {
						case LMOptions.LARGE:
							largeSizeItem.setSelected(true);
							break;
						case LMOptions.SMALL:
							smallSizeItem.setSelected(true);
							break;
						case LMOptions.VARY:
						default:
							varySizeItem.setSelected(true);
							break;
					}
					break;
				case LMHistory.CHANGE_OPTIONS:
					// ATTENTION : A mettre en lien avec LatticePanel.changeOptions(type)
					// pour effectuer les actions. Ici seul le menu est modifié
					switch (value) {
						case LMOptions.LIGHT:
							//----------------------------------------------
							singleSelFilterIdeal.setSelected(true);
							multipleSelFilterIdeal.setSelected(true);
							blurItem.setSelected(true);
							//----------------------------------------------
							hideOutFocusLabelItem.setSelected(true);
							attReducedItem.setSelected(true);
							objPercNodeItem.setSelected(true);
							setRulesHidden(true);
							//----------------------------------------------
							zoomAnimItem.setSelected(false);
							blinkAnimationItem.setSelected(true);
							//----------------------------------------------
							sameIntensityItem.setSelected(true);
							largeSizeItem.setSelected(true);
							//----------------------------------------------
							setEmptyBottomHidden(false);
							setLatticeMapHidden(true);
							//----------------------------------------------
							break;
						case LMOptions.HEAVY:
							//----------------------------------------------
							singleSelFilterIdeal.setSelected(true);
							multipleSelFilterIdeal.setSelected(true);
							fisheyeItem.setSelected(true);
							//----------------------------------------------
							hideOutFocusLabelItem.setSelected(false);
							attListItem.setSelected(true);
							objListItem.setSelected(true);
							setRulesHidden(false);
							showGeneItem.setSelected(true);
							//----------------------------------------------
							zoomAnimItem.setSelected(true);
							shakeAnimationItem.setSelected(true);
							//----------------------------------------------
							differentIntensityItem.setSelected(true);
							largeSizeItem.setSelected(true);
							//----------------------------------------------
							setEmptyBottomHidden(false);
							setLatticeMapHidden(false);
							//----------------------------------------------
							break;
						case LMOptions.USER:
						default:
							// Recupère les préférences de Lattice Miner
							Preferences preferences = LMPreferences.getPreferences();

							//----------------------------------------------
							changeMenuItem(LMHistory.SINGLE_SELECTION, preferences.getInt(LMPreferences.SINGLE_SEL_TYPE,
									LMOptions.FILTER_IDEAL));
							changeMenuItem(LMHistory.MULTIPLE_SELECTION, preferences.getInt(LMPreferences.MULT_SEL_TYPE,
									LMOptions.COMMON_FILTER_IDEAL));
							changeMenuItem(LMHistory.CONTRAST, preferences.getInt(LMPreferences.SEL_CONTRAST_TYPE,
									LMOptions.FISHEYE));
							//----------------------------------------------
							hideOutFocusLabelItem.setSelected(preferences.getBoolean(LMPreferences.HIDE_OUT_OF_FOCUS, false));
							changeMenuItem(LMHistory.ATTRIBUTE_LABELS, preferences.getInt(LMPreferences.ATT_LABEL_TYPE,
									LMOptions.ATTRIBUTES_REDUCED)); //.ATTRIBUTES_ALL));
							changeMenuItem(LMHistory.OBJECT_LABELS, preferences.getInt(LMPreferences.OBJ_LABEL_TYPE,
									LMOptions.OBJECTS_REDUCED)); //OBJECTS_ALL));
							changeMenuItem(LMHistory.RULE_LABELS, preferences.getInt(LMPreferences.RULES_LABEL_TYPE,
									LMOptions.RULES_SHOW));
							boolean hideRules = preferences.getInt(LMPreferences.RULES_LABEL_TYPE, LMOptions.RULES_SHOW) == LMOptions.NO_LABEL;
							setRulesHidden(hideRules);
							//----------------------------------------------
							zoomAnimItem.setSelected(preferences.getBoolean(LMPreferences.ANIMATE_ZOOM, true));
							changeMenuItem(LMHistory.ATTENTION, preferences.getInt(LMPreferences.FEATURE_TYPE, LMOptions.SHAKE));
							//----------------------------------------------
							if (preferences.getBoolean(LMPreferences.CHANGE_COLOR_INTENSITY, true)) {
								changeMenuItem(LMHistory.COLOR, LMOptions.CHANGE);
							} else {
								changeMenuItem(LMHistory.COLOR, LMOptions.SAME);
							}
							changeMenuItem(LMHistory.SIZE, preferences.getInt(LMPreferences.CONCEPT_SIZE_TYPE, LMOptions.LARGE));
							//----------------------------------------------
							setEmptyBottomHidden(false); // Infimum jamais caché par défaut
							setLatticeMapHidden(!preferences.getBoolean(LMPreferences.SHOW_LATTICE_MAP, true));
							//----------------------------------------------
							break;
					}
					break;
				default:
					break;
			}
		}

		/**
		 * Change le message pour le menu "Undo"
		 * @param enabled vrai s'il faut activer le menu "Undo", faux sinon
		 * @param message le message a mettre si le "Undo" est possible
		 */
		protected void setUndoOperation(boolean enabled, String message) {
			undoOperation.setEnabled(enabled);
			if (enabled && message != null && !message.equals("")) //$NON-NLS-1$
				undoOperation.setText(GUIMessages.getString("GUI.undo")+" (" + message + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		/**
		 * Change le message pour le menu "Redo"
		 * @param enabled vrai s'il faut activer le menu "Redo", faux sinon
		 * @param message le message a mettre si le "Redo" est possible
		 */
		protected void setRedoOperation(boolean enabled, String message) {
			redoOperation.setEnabled(enabled);
			if (enabled && message != null && !message.equals("")) //$NON-NLS-1$
				redoOperation.setText(GUIMessages.getString("GUI.redo")+" (" + message + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		/**
		 * Change le choix d'affichage de l'infimum s'il est vide et previens le treillis
		 * @param hide vrai s'il faut masquer l'infimum s'il est vide, faux sinon
		 */
		protected void setEmptyBottomHidden(boolean hide) {
			if (hide) {
				if (viewer.hasHideEmptyBottomConcepts()) {
					emptyBottomsHidden = true;
					showBottomItem.setText(GUIMessages.getString("GUI.showEmptyInfimum")); //$NON-NLS-1$
				}
			} else {
				emptyBottomsHidden = false;
				showBottomItem.setText(GUIMessages.getString("GUI.hideEmptyInfimum")); //$NON-NLS-1$
				viewer.showAllConcepts();
			}
		}

		/**
		 * Change le choix d'affichage de la petite carte du treillis et previens le treillis
		 * @param hide vrai s'il faut masquer la petite carte, faux sinon
		 */
		protected void setLatticeMapHidden(boolean hide) {
			if (hide) {
				showLatticeMapItem.setSelected(true);
				showLatticeMapItem.setText(GUIMessages.getString("GUI.showLatticeMap")); //$NON-NLS-1$
				viewer.showLatticeMap(false);
			} else {
				showLatticeMapItem.setSelected(false);
				showLatticeMapItem.setText(GUIMessages.getString("GUI.hideLatticeMap")); //$NON-NLS-1$
				viewer.showLatticeMap(true);
			}
		}

		/**
		 * Change le choix d'affichage des règles et previens le treillis
		 * @param hide vrai s'il faut masquer les règles, faux sinon
		 */
		protected void setRulesHidden(boolean hide) {
			if (hide) {
				viewer.lockHistory();
				noRulesItem.setSelected(true);
				viewer.setRulesLabelType(LMOptions.NO_LABEL);
				viewer.unlockHistory();
			} else {
				viewer.lockHistory();
				showRulesItem.setSelected(true);
				viewer.setRulesLabelType(LMOptions.RULES_SHOW);
				viewer.unlockHistory();
			}
		}

		/**
		 * Change le preset option et previens le treillis
		 * @param optionsType le type de preset
		 */
		protected void setPresetOptions(int optionsType) {

			if (optionsType != LMOptions.LIGHT && optionsType != LMOptions.HEAVY && optionsType != LMOptions.USER)
				return;

			switch (optionsType) {
				case LMOptions.LIGHT:
					lightOptions.setSelected(true);
					break;
				case LMOptions.HEAVY:
					heavyOptions.setSelected(true);
					break;
				case LMOptions.USER:
				default:
					userOptions.setSelected(true);
					break;
			}

			selectMenuItem(LMHistory.CHANGE_OPTIONS, optionsType);
			viewer.changeOptions(optionsType);
		}

		/**
		 * Listener du menu
		 * @author Geneviève Roberge
		 * @version 1.0
		 */
		private class FrameMenuListener implements ActionListener {
			public void actionPerformed(ActionEvent ae) {
				try {
					if (ae.getSource() == lightOptions) {
						setPresetOptions(LMOptions.LIGHT);
					} else if (ae.getSource() == heavyOptions) {
						setPresetOptions(LMOptions.HEAVY);
					} else if (ae.getSource() == userOptions) {
						setPresetOptions(LMOptions.USER);
					} else if (ae.getSource() == userSaveOptions) {
						saveCurrentOptions();
					} else if (ae.getSource() == noSingleSel) {
						viewer.setSingleSelType(LMOptions.NONE);
					} else if (ae.getSource() == singleSelFilter) {
						viewer.setSingleSelType(LMOptions.FILTER);
					} else if (ae.getSource() == singleSelIdeal) {
						viewer.setSingleSelType(LMOptions.IDEAL);
					} else if (ae.getSource() == singleSelFilterIdeal) {
						viewer.setSingleSelType(LMOptions.FILTER_IDEAL);
					} else if (ae.getSource() == singleSelParents) {
						viewer.setSingleSelType(LMOptions.PARENTS);
					} else if (ae.getSource() == singleSelChildren) {
						viewer.setSingleSelType(LMOptions.CHILDREN);
					} else if (ae.getSource() == singleSelParentsChildren) {
						viewer.setSingleSelType(LMOptions.PARENTS_CHILDREN);
					} else if (ae.getSource() == singleSelChildParents) {
						viewer.setSingleSelType(LMOptions.CHILDREN_PARENTS);
					} else if (ae.getSource() == noMultipleSel) {
						viewer.setMultSelType(LMOptions.NONE);
					} else if (ae.getSource() == multipleSelFilter) {
						viewer.setMultSelType(LMOptions.COMMON_FILTER);
					} else if (ae.getSource() == multipleSelIdeal) {
						viewer.setMultSelType(LMOptions.COMMON_IDEAL);
					} else if (ae.getSource() == multipleSelFilterIdeal) {
						viewer.setMultSelType(LMOptions.COMMON_FILTER_IDEAL);
					} else if (ae.getSource() == multipleSelSubLattice) {
						viewer.setMultSelType(LMOptions.SUB_LATTICE);
					} else if (ae.getSource() == blurItem) {
						viewer.setSelectionContrastType(LMOptions.BLUR);
					} else if (ae.getSource() == fisheyeItem) {
						viewer.setSelectionContrastType(LMOptions.FISHEYE);
					} else if (ae.getSource() == hideOutFocusLabelItem) {
						viewer.hideLabelForOutOfFocusConcept(hideOutFocusLabelItem.isSelected());
					} else if (ae.getSource() == noAttributesItem) {
						viewer.setAttLabelType(LMOptions.NO_LABEL);
					} else if (ae.getSource() == attReducedItem) {
						viewer.setAttLabelType(LMOptions.ATTRIBUTES_REDUCED);
					} else if (ae.getSource() == attListItem) {
						viewer.setAttLabelType(LMOptions.ATTRIBUTES_ALL);
					} else if (ae.getSource() == noObjectsItem) {
						viewer.setObjLabelType(LMOptions.NO_LABEL);
					} else if (ae.getSource() == objReducedItem) {
						viewer.setObjLabelType(LMOptions.OBJECTS_REDUCED);
					} else if (ae.getSource() == objListItem) {
						viewer.setObjLabelType(LMOptions.OBJECTS_ALL);
					} else if (ae.getSource() == objCountItem) {
						viewer.setObjLabelType(LMOptions.OBJECTS_NUMBER);
					} else if (ae.getSource() == objPercAllItem) {
						viewer.setObjLabelType(LMOptions.OBJECTS_PERC_CTX);
					} else if (ae.getSource() == objPercNodeItem) {
						viewer.setObjLabelType(LMOptions.OBJECTS_PERC_NODE);
					} else if (ae.getSource() == setRulesItem) {
						hasSetRulesInLabels();
						noRulesItem.setEnabled(true);
						showRulesItem.setEnabled(true);
						setRulesHidden(setRulesItem.isSelected());
					} else if (ae.getSource() == noRulesItem) {
						viewer.setRulesLabelType(LMOptions.NO_LABEL);
					} else if (ae.getSource() == showRulesItem) {
						viewer.setRulesLabelType(LMOptions.RULES_SHOW);
					} else if (ae.getSource() == noGeneItem) {
						viewer.setGeneLabelType(LMOptions.NO_LABEL);
					} else if (ae.getSource() == showGeneItem) {
						viewer.setGeneLabelType(LMOptions.GENE_SHOW);
					} else if (ae.getSource() == zoomAnimItem) {
						viewer.setAnimateZoom(zoomAnimItem.isSelected());
					} else if (ae.getSource() == shakeAnimationItem) {
						viewer.setAttentionFeatureType(LMOptions.SHAKE);
					} else if (ae.getSource() == blinkAnimationItem) {
						viewer.setAttentionFeatureType(LMOptions.BLINK);
					} else if (ae.getSource() == noSearchAnimationItem) {
						viewer.setAttentionFeatureType(LMOptions.NONE);
					} else if (ae.getSource() == differentIntensityItem) {
						viewer.setChangeColorIntensity(true);
					} else if (ae.getSource() == sameIntensityItem) {
						viewer.setChangeColorIntensity(false);
					} else if (ae.getSource() == largeSizeItem) {
						viewer.setConceptSizeType(LMOptions.LARGE);
					} else if (ae.getSource() == smallSizeItem) {
						viewer.setConceptSizeType(LMOptions.SMALL);
					} else if (ae.getSource() == varySizeItem) {
						viewer.setConceptSizeType(LMOptions.VARY);
					} else if (ae.getSource() == undoOperation) {
						backHistory();
					} else if (ae.getSource() == redoOperation) {
						forwardHistory();
					} else if (ae.getSource() == zoomInItem) {
						zoomIn();
					} else if (ae.getSource() == zoomOutItem) {
						zoomOut();
					} else if (ae.getSource() == zoomSelectItem) {
						zoomInSelectedArea();
					} else if (ae.getSource() == zoomLatticeItem) {
						showEntireLattice();
					} else if (ae.getSource() == showAllLabelsItem) {
						showAllLabels();
					} else if (ae.getSource() == hideAllLabelsItem) {
						hideAllLabels();
					} else if (ae.getSource() == showAreaLabelsItem) {
						showAreaLabels();
					} else if (ae.getSource() == hideAreaLabelsItem) {
						hideAreaLabels();
					} else if (ae.getSource() == duplicateItem) {
						duplicateViewer();
					} else if (ae.getSource() == showBottomItem) {
						setEmptyBottomHidden(!emptyBottomsHidden);
					} else if (ae.getSource() == showLatticeMapItem) {
						setLatticeMapHidden(!showLatticeMapItem.isSelected());
					} else if (ae.getSource() == latticeStructureItem) {
						openStructureViewer();
					} else if (ae.getSource() == rulesItem) {
						showRulePanel();
					} else if (ae.getSource() == exportLattice) {
						showCapturePanel();
					} else if (ae.getSource() == quitFrame) {
						setVisible(false);
						dispose();
					} else if (ae.getSource() == saveLattice) {
						saveCurrentLatticeAs();
					}

				} catch (LatticeMinerException error) {
					DialogBox.showMessageError(viewer, error);
				}
			}

		}

	}

	private class ToolBarListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			try {
				if (ae.getSource() == previousBtn) {
					backHistory();
				} else if (ae.getSource() == nextBtn) {
					forwardHistory();
				} else if (ae.getSource() == zoomInBtn) {
					zoomIn();
				} else if (ae.getSource() == zoomOutBtn) {
					zoomOut();
				} else if (ae.getSource() == zoomAreaBtn) {
					zoomInSelectedArea();
				} else if (ae.getSource() == noZoomBtn) {
					showEntireLattice();
				} else if (ae.getSource() == modifyBtn) {
					openStructureViewer();
				} else if (ae.getSource() == ruleBtn) {
					showRulePanel();
				} else if (ae.getSource() == captureBtn) {
					showCapturePanel();
				} else if (ae.getSource() == duplicateBtn) {
					duplicateViewer();
				}
			} catch (LatticeMinerException error) {
				DialogBox.showMessageError(viewer, error);
			}
		}
	}


	public class SliderListener implements ChangeListener{
		public void stateChanged(ChangeEvent ce){
			int value = textSize.getValue();
			viewer.setLabelSize(value);
			textSizeValue.setText(Integer.toString(value));
			viewer.repaint();
		}
	}

	/**
	 * @return the treePanel
	 */
	public Tree getTreePanel() {
		return treePanel;
	}

	/**
	 * @return the projectionPanel
	 */
	public Projection getProjectionPanel() {
		return projectionPanel;
	}

	/**
	 * @return the searchPanel
	 */
	public Search getSearchPanel() {
		return searchPanel;
	}

}
