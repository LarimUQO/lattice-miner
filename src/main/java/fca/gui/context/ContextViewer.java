package fca.gui.context;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import fca.LatticeMiner;
import fca.core.context.Context;
import fca.core.context.binary.BinaryContext;
import fca.core.context.nested.NestedContext;
import fca.core.context.nested.RankingAlgo;
import fca.core.context.nested.SupervisedMultivaluedRankingAlgo;
import fca.core.context.nested.UnsupervisedMultivaluedRankingAlgo;
import fca.core.context.triadic.Ganter;
import fca.core.context.triadic.TriadicAlgorithms;
import fca.core.context.triadic.TriadicContext;
import fca.core.context.valued.ValuedContext;
import fca.core.lattice.*;
import fca.core.rule.GenericBasisAlgorithm;
import fca.core.rule.InformativeBasisAlgorithm;
import fca.core.rule.Rule;
import fca.core.rule.RuleAlgorithm;
import fca.core.util.BasicSet;
import fca.exception.AlreadyExistsException;
import fca.exception.InvalidTypeException;
import fca.exception.ReaderException;
import fca.exception.WriterException;
import fca.gui.Viewer;
import fca.gui.context.assistant.LevelAdditionAssistant;
import fca.gui.context.assistant.NestedContextCreationAssistant;
import fca.gui.context.assistant.ValuedContextConversionAssistant;
import fca.gui.context.panel.LogicalAttributePanel;
import fca.gui.context.panel.MergeAttributesPanel;
import fca.gui.context.panel.RemoveAttributesPanel;
import fca.gui.context.panel.RemoveObjectsPanel;
import fca.gui.context.panel.TaxonomyAttributePanel;
import fca.gui.context.panel.TaxonomyObjectPanel;
import fca.gui.context.table.*;
import fca.gui.context.table.model.ContextTableModel;
import fca.gui.lattice.LatticeViewer;
import fca.gui.lattice.element.GraphicalLattice;
import fca.gui.lattice.element.LatticeStructure;
import fca.gui.rule.RuleViewer;
import fca.gui.util.DialogBox;
import fca.gui.util.ExampleFileFilter;
import fca.gui.util.constant.LMIcons;
import fca.gui.util.constant.LMPreferences;
import fca.io.context.ContextReaderJson.txt.GaliciaSLFBinaryContextReader;
import fca.io.context.ContextReaderJson.txt.LMBinaryContextReader;
import fca.io.context.ContextReaderJson.txt.LMNestedContextReader;
import fca.io.context.ContextReaderJson.txt.LMValuedContextReader;
import fca.io.context.ContextReaderJson.xml.CexBinaryContextReader;
import fca.io.context.ContextReaderJson.xml.GaliciaXMLBinaryContextReader;
import fca.io.context.writer.txt.GaliciaSLFBinaryContextWriter;
import fca.io.context.writer.txt.LMBinaryContextWriter;
import fca.io.context.writer.txt.LMNestedContextWriter;
import fca.io.context.writer.txt.LMValuedContextWriter;
import fca.io.context.writer.xml.CexBinaryContextWriter;
import fca.io.context.writer.xml.GaliciaXMLBinaryContextWriter;
import fca.io.taxonomy.writer.xml.LatticeXMLTaxonomyWriter;
import fca.messages.GUIMessages;

/**
 * Viewer de contextes
 * 
 * @author Genevieve Roberge
 * @author Ludovic Thomas
 * @author Aicha Bennis, Linda Bogni, Maya Safwat, Babacar SY
 * @author Nicolas Convers, Arnaud Renaud-Goud
 * @version 1.4
 */
public class ContextViewer extends Viewer {

	private static final long serialVersionUID = 6515899540960064345L;

	/** L'unique instance de ContextViewer */
	private static ContextViewer SINGLETON = null;

	private final JFrame frame;
	private JPanel panel;
	private JTabbedPane panelTab;

	private Vector<ContextTableScrollPane> contextPanes;

	private final Vector<String> contextFiles;

	private ButtonGroup contextGroup;

	private JMenuItem noContextItem;

	private JLabel currentContextName;

	private int currentContextIdx;

	private int untitledCount;

	private int binaryNb=0;// savoir combien de contextes binaires sont ouverts //-->LINDA
	private int valuedNb=0;// savoir combien de contextes valuees sont ouverts //-->LINDA
	private int nestedNb=0;// savoir combien de contextes imbriquees sont ouverts //-->LINDA
	private int triadicNb=0;

	private HashMap<String, LMButton> buttons;
	
	/* HashMap contenant les élements des menus */
	private HashMap<String,JMenuItem> file;
	private HashMap<String, JMenuItem> edit;
	private HashMap<String, JMenuItem> rules;
	private HashMap<String, JMenuItem> windowMenu;
	private HashMap<String, JMenuItem> lattice;
	private HashMap<String, JMenuItem> triadic;

	private Ganter ganterApp;

	private GridBagConstraints constraints;
	
	protected void about() {
		new AboutScreen();
	}
	
	protected void quit() {
		if (hasClosedAllContexts()) {
			frame.setVisible(false);
			frame.dispose();
			// FIXME : fermer les autres potentielles fenetres ouvertes
		}
	}
	
	protected ContextViewer() {
		frame = this;
		setTitle(GUIMessages.getString("GUI.latticeMiner")); //$NON-NLS-1$
		setDefaultLookAndFeelDecorated(false);


		// Pour le fonctionnement sous Mac OSX avec la barre de menu en haut...
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		// set the name of the application menu item
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Lattice Miner");
		// On cherche le style du systeme d'exploitation pour l'utiliser sur LM
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
				UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		setJMenuBar(buildMenuBar());
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(buildToolBar(), BorderLayout.NORTH);
		getContentPane().add(buildPanel(), BorderLayout.CENTER);
		setActiveActions();

		addWindowListener(this);
		contextFiles = new Vector<>();
		pack();
		setVisible(true);
	}

	/**
	 * @return l'unique instance du {@link ContextViewer}
	 */
	public static ContextViewer getContextViewer() {
		if (SINGLETON == null) {
			SINGLETON = new ContextViewer();
		}
		return SINGLETON;
	}

	protected JMenuBar buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		//FrameMenuListener menuListener = new FrameMenuListener();
		
		/* ==== FILE MENU ==== */
		file = new HashMap<>();
		file.put("Title", new JMenu(GUIMessages.getString("GUI.file"))); //$NON-NLS-1$
		file.get("Title").setMnemonic(KeyEvent.VK_F);


		file.put("NewContext", new JMenu(GUIMessages.getString("GUI.new"))); //$NON-NLS-1$
		file.get("NewContext").setMnemonic(KeyEvent.VK_N);
		file.get("Title").add(file.get("NewContext"));

		file.put("NewBinaryContext", new LMMenuItem(GUIMessages.getString("GUI.binaryContext"),this, "createBinaryContext")); //$NON-NLS-1$
		file.get("NewBinaryContext").setMnemonic(KeyEvent.VK_B);
		file.get("NewBinaryContext").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.get("NewContext").add(file.get("NewBinaryContext"));

		file.put("NewValuedContext", new LMMenuItem(GUIMessages.getString("GUI.valuedContext"),this, "createValuedContext")); //$NON-NLS-1$
		file.get("NewValuedContext").setMnemonic(KeyEvent.VK_V);
		file.get("NewValuedContext").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.get("NewValuedContext").setEnabled(false);
		file.get("NewContext").add(file.get("NewValuedContext"));

		file.put("NewNestedContext", new LMMenuItem(GUIMessages.getString("GUI.nestedContext"), this, "createNestedContext")); //$NON-NLS-1$
		file.get("NewNestedContext").setMnemonic(KeyEvent.VK_N);
		file.get("NewNestedContext").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.get("NewNestedContext").setEnabled(true);
		file.get("NewContext").add(file.get("NewNestedContext"));		
		file.get("Title").add(file.get("NewContext"));

		file.put("OpenContext", new LMMenuItem(GUIMessages.getString("GUI.open"), this, "openContext")); //$NON-NLS-1$
		file.get("OpenContext").setMnemonic(KeyEvent.VK_O);
		file.get("OpenContext").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())); //$NON-NLS-1$
		file.get("OpenContext").setEnabled(true);
		file.get("Title").add(file.get("OpenContext"));

		file.put("OpenRecentContext", new JMenu(GUIMessages.getString("GUI.openRecent"))); //$NON-NLS-1$
		file.get("OpenRecentContext").setEnabled(false);
		file.get("Title").add(file.get("OpenRecentContext"));

		if(setRecentFileMenuItem() != 0) {
			file.get("OpenRecentContext").setEnabled(true);
		}

		((JMenu)file.get("Title")).addSeparator();

		file.put("SaveContext", new LMMenuItem(GUIMessages.getString("GUI.save"), this, "hasSaveCurrentContext")); //$NON-NLS-1$
		file.get("SaveContext").setMnemonic(KeyEvent.VK_S);
		file.get("SaveContext").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.get("SaveContext").setEnabled(false);
		file.get("Title").add(file.get("SaveContext"));

		file.put("SaveAsContext", new LMMenuItem(GUIMessages.getString("GUI.saveAs"), this, "hasSaveCurrentContextAs")); //$NON-NLS-1$
		file.get("SaveAsContext").setEnabled(false);
		file.get("Title").add(file.get("SaveAsContext"));

		file.put("SaveAllContext", new LMMenuItem(GUIMessages.getString("GUI.saveAll"), this, "saveAllContexts")); //$NON-NLS-1$
		file.get("SaveAllContext").setMnemonic(KeyEvent.VK_S);
		file.get("SaveAllContext").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.get("SaveAllContext").setEnabled(false);
		file.get("Title").add(file.get("SaveAllContext"));

		file.put("ViewTXT", new LMMenuItem(GUIMessages.getString("GUI.contexttxt"), this, "TXTContext")); //$NON-NLS-1$
		file.get("ViewTXT").setMnemonic(KeyEvent.VK_O);
		file.get("ViewTXT").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.get("ViewTXT").setEnabled(false);
		file.get("Title").add(file.get("ViewTXT"));

		((JMenu)file.get("Title")).addSeparator();

		file.put("CloseContext", new LMMenuItem(GUIMessages.getString("GUI.close"), this)); //$NON-NLS-1$
		file.get("CloseContext").setMnemonic(KeyEvent.VK_C);
		file.get("CloseContext").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.get("CloseContext").setEnabled(false);
		file.get("Title").add(file.get("CloseContext"));

		file.put("CloseAllContexts", new LMMenuItem(GUIMessages.getString("GUI.closeAll"), this, "hasClosedAllContexts")); //$NON-NLS-1$
		file.get("CloseAllContexts").setMnemonic(KeyEvent.VK_C);
		file.get("CloseAllContexts").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.get("CloseAllContexts").setEnabled(false);
		file.get("Title").add(file.get("CloseAllContexts"));

		((JMenu)file.get("Title")).addSeparator();

		file.put("QuitViewer", new LMMenuItem(GUIMessages.getString("GUI.quit"),this, "quit")); //$NON-NLS-1$
		file.get("QuitViewer").setMnemonic(KeyEvent.VK_Q);
		file.get("QuitViewer").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.get("Title").add(file.get("QuitViewer"));

		menuBar.add(file.get("Title"));




		/* ==== EDIT MENU ==== */
		edit = new HashMap<>();
		edit.put("Title", new JMenu(GUIMessages.getString("GUI.edit"))); //$NON-NLS-1$
		edit.get("Title").setMnemonic(KeyEvent.VK_E);

		edit.put("AddEmptyLevel", new LMMenuItem(GUIMessages
				.getString("GUI.addEmptylevel"), this, "addEmptyLevelToCurrentContext")); //$NON-NLS-1$
		edit.get("Title").add(edit.get("AddEmptyLevel"));

		edit.put("AddContextLevel", new LMMenuItem(GUIMessages
				.getString("GUI.addContextLevel"), this, "addContextLevelToCurrentContext" )); //$NON-NLS-1$
		edit.get("Title").add(edit.get("AddContextLevel"));

		edit.put("RemoveLevel", new LMMenuItem(GUIMessages
				.getString("GUI.removeLastLevel"), this, "removeLastLevelFromCurrentContext")); //$NON-NLS-1$
		edit.get("Title").add(edit.get("RemoveLevel"));

		edit.put("OrderLevels", new LMMenuItem(GUIMessages.getString("GUI.orderLevels"), this, "orderCurrentContextLevels")); //$NON-NLS-1$
		edit.get("Title").add(edit.get("OrderLevels"));
		((JMenu) edit.get("Title")).addSeparator();

		edit.put("AddObject", new LMMenuItem(GUIMessages.getString("GUI.addObject"), this, "addObjectToCurrentContext")); //$NON-NLS-1$
		edit.get("AddObject").setMnemonic(KeyEvent.VK_O);
		edit.get("AddObject").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		edit.get("Title").add(edit.get("AddObject"));

		edit.put("AddAttribute", new LMMenuItem(GUIMessages.getString("GUI.addAttribute"), this, "addAttributeToCurrentContext")); //$NON-NLS-1$
		edit.get("AddAttribute").setMnemonic(KeyEvent.VK_A);
		edit.get("AddAttribute").setDisplayedMnemonicIndex(4);
		edit.get("AddAttribute").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		edit.get("Title").add(edit.get("AddAttribute"));
		((JMenu) edit.get("Title")).addSeparator();


		edit.put("Clarify", new JMenu(GUIMessages.getString("GUI.clarify"))); //$NON-NLS-1$
		edit.get("Clarify").setMnemonic(KeyEvent.VK_C);

		edit.put("ClarifyObject", new LMMenuItem(GUIMessages
				.getString("GUI.clarifyObject"), this, "clarifyObjectOfCurrentContext")); //$NON-NLS-1$
		edit.get("ClarifyObject").setMnemonic(KeyEvent.VK_O);
		edit.get("ClarifyObject").setEnabled(false);
		edit.get("Clarify").add(edit.get("ClarifyObject"));

		edit.put("ClarifyAttribute", new LMMenuItem(GUIMessages
				.getString("GUI.clarifyAttribute"), this, "clarifyAttributeOfCurrentContext" )); //$NON-NLS-1$
		edit.get("ClarifyAttribute").setMnemonic(KeyEvent.VK_A);
		edit.get("ClarifyAttribute").setEnabled(false);
		edit.get("Clarify").add(edit.get("ClarifyAttribute"));

		edit.put("ClarifyAll",new LMMenuItem(GUIMessages
				.getString("GUI.clarifyAll"), this, "clarifyCurrentContext")); //$NON-NLS-1$
		edit.get("ClarifyAll").setMnemonic(KeyEvent.VK_C);
		edit.get("ClarifyAll").setEnabled(false);
		edit.get("Clarify").add(edit.get("ClarifyAll"));		

		edit.get("Title").add(edit.get("Clarify"));

		edit.put("Reduce", new JMenu(GUIMessages.getString("GUI.reduce"))); //$NON-NLS-1$
		edit.get("Reduce").setMnemonic(KeyEvent.VK_R);

		edit.put("ReduceObject", new LMMenuItem(GUIMessages
				.getString("GUI.reduceObject"), this, "reduceObjectToCurrentContexte")); //$NON-NLS-1$
		edit.get("ReduceObject").setMnemonic(KeyEvent.VK_O);
		edit.get("ReduceObject").setEnabled(false);
		edit.get("Reduce").add(edit.get("ReduceObject"));

		edit.put("ReduceAttribute", new LMMenuItem(GUIMessages
				.getString("GUI.reduceAttribute"), this, "reduceAttributeToCurrentContexte")); //$NON-NLS-1$
		edit.get("ReduceAttribute").setMnemonic(KeyEvent.VK_A);
		edit.get("ReduceAttribute").setEnabled(false);
		edit.get("Reduce").add(edit.get("ReduceAttribute"));

		edit.put("ReduceContext", new LMMenuItem(GUIMessages
				.getString("GUI.reduceContext"), this, "reduceCurrentContexte")); //$NON-NLS-1$
		edit.get("ReduceContext").setMnemonic(KeyEvent.VK_C);
		edit.get("ReduceContext").setEnabled(false);
		edit.get("Reduce").add(edit.get("ReduceContext"));

		edit.get("Title").add(edit.get("Reduce"));


		edit.put("Complementary", new JMenu(GUIMessages.getString("GUI.complementary"))); //$NON-NLS-1$
		edit.get("Complementary").setMnemonic(KeyEvent.VK_C);

		edit.put("ComplementaryContext", new LMMenuItem(GUIMessages
				.getString("GUI.complementaryContext"), this, "addComplementaryToCurrentContext")); //$NON-NLS-1$
		edit.get("ComplementaryContext").setMnemonic(KeyEvent.VK_N);
		edit.get("ComplementaryContext").setEnabled(false);
		edit.get("Complementary").add(edit.get("ComplementaryContext"));

		edit.put("GlobalContext", new LMMenuItem(GUIMessages
				.getString("GUI.globalContext"), this, "ShowComplementaryToCurrentContext")); //$NON-NLS-1$
		edit.get("GlobalContext").setMnemonic(KeyEvent.VK_G);
		edit.get("GlobalContext").setEnabled(false);
		edit.get("Complementary").add(edit.get("GlobalContext"));

		edit.get("Title").add(edit.get("Complementary"));


		edit.put("ShowArrowRelation",  new JMenu(GUIMessages.getString("GUI.showArrowRelation"))); //$NON-NLS-1$
		edit.get("ShowArrowRelation").setMnemonic(KeyEvent.VK_C);

		edit.put("ShowArrowContext", new LMMenuItem(GUIMessages
				.getString("GUI.showArrowContext"), this, "showArrowContexte")); //$NON-NLS-1$
		edit.get("ShowArrowContext").setMnemonic(KeyEvent.VK_N);
		edit.get("ShowArrowContext").setEnabled(false);
		edit.get("ShowArrowRelation").add(edit.get("ShowArrowContext"));

		edit.put("HideArrowContext", new LMMenuItem(GUIMessages
				.getString("GUI.hideArrowContext"), this, "hideArrowContexte")); //$NON-NLS-1$
		edit.get("HideArrowContext").setMnemonic(KeyEvent.VK_G);
		edit.get("HideArrowContext").setEnabled(false);
		edit.get("ShowArrowRelation").add(edit.get("HideArrowContext"));

		edit.get("Title").add(edit.get("ShowArrowRelation"));

		edit.put("TransitiveClosureObject", new LMMenuItem(GUIMessages
				.getString("GUI.transitiveClosureObject"), this, "ShowTransitiveClosureObj")); //$NON-NLS-1$
		edit.get("TransitiveClosureObject").setMnemonic(KeyEvent.VK_N);
		edit.get("TransitiveClosureObject").setEnabled(false);

		edit.put("TransitiveClosureAtt", new LMMenuItem(GUIMessages
				.getString("GUI.transitiveClosureAtt"), this, "ShowTransitiveClosureAtt")); //$NON-NLS-1$
		edit.get("TransitiveClosureAtt").setMnemonic(KeyEvent.VK_G);
		edit.get("TransitiveClosureAtt").setEnabled(false);


		/*calculateTransitiveClosure = new JMenuItem(GUIMessages.getString("GUI.calculateTransitiveClosure")); //$NON-NLS-1$
		calculateTransitiveClosure.addActionListener(menuListener);
		calculateTransitiveClosure.setMnemonic(KeyEvent.VK_R);
		//showArrowContext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
				InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));; //$NON-NLS-1$
		editMenu.add(calculateTransitiveClosure);*/

		((JMenu) edit.get("Title")).addSeparator();


		edit.put("RemoveObject",new LMMenuItem(GUIMessages.getString("GUI.removeObjects"), this, "removeObjectFromCurrentContext")); //$NON-NLS-1$
		edit.get("RemoveObject").setMnemonic(KeyEvent.VK_O);
		edit.get("RemoveObject").setDisplayedMnemonicIndex(7);
		edit.get("Title").add(edit.get("RemoveObject"));

		edit.put("RemoveAttribute", new LMMenuItem(GUIMessages
				.getString("GUI.removeAttributes"), this, "removeAttributeFromCurrentContext")); //$NON-NLS-1$
		edit.get("RemoveAttribute").setMnemonic(KeyEvent.VK_A);
		edit.get("Title").add(edit.get("RemoveAttribute"));

		edit.put("MergeAttributes", new LMMenuItem(GUIMessages
				.getString("GUI.mergeAttributes"), this, "mergeAttributesInCurrentContext")); //$NON-NLS-1$
		edit.get("MergeAttributes").setMnemonic(KeyEvent.VK_M);
		edit.get("Title").add(edit.get("MergeAttributes"));

		edit.put("LogicalAttribute", new LMMenuItem(GUIMessages
				.getString("GUI.addAttributeComposition"), this, "createLogicalAttributeInCurrentContext")); //$NON-NLS-1$
		edit.get("Title").add(edit.get("LogicalAttribute"));

		/*edit.put("TaxonomyAttribute", new JMenuItem(GUIMessages
				.getString("GUI.createAttributeTaxonomy")));
		edit.get("TaxonomyAttribute").addActionListener(menuListener);
		edit.get("Title").add(edit.get("TaxonomyAttribute"));

		edit.put("TaxonomyObject", new JMenuItem(GUIMessages
				.getString("GUI.createObjectTaxonomy")));
		edit.get("TaxonomyObject").addActionListener(menuListener);
		edit.get("Title").add(edit.get("TaxonomyObject"));

		edit.put("SaveTaxonomy", new JMenuItem(GUIMessages 
				.getString("GUI.saveTaxonomy")));
		edit.get("SaveTaxonomy").addActionListener(menuListener);
		edit.get("Title").add(edit.get("SaveTaxonomy"));*/

		edit.put("CompareAttributes", new LMMenuItem(GUIMessages
				.getString("GUI.compareAttributes"), this, "compareAttributesInCurrentContext")); //$NON-NLS-1$
		edit.get("CompareAttributes").setMnemonic(KeyEvent.VK_C);
		edit.get("Title").add(edit.get("CompareAttributes"));


		((JMenu) edit.get("Title")).addSeparator();


		edit.put("CreateClusters",new LMMenuItem(GUIMessages
				.getString("GUI.sortObjectInClusters"), this , "createObjectClustersInCurrentContext")); //$NON-NLS-1$
		edit.get("CompareAttributes").setMnemonic(KeyEvent.VK_S);
		edit.get("Title").add(edit.get("CompareAttributes"));

		((JMenu) edit.get("Title")).addSeparator();

		edit.put("ConvertToBinary",new LMMenuItem(GUIMessages
				.getString("GUI.convertToBinaryContext"), this, "convertCurrentContextToBinaryContext")); //$NON-NLS-1$
		edit.get("Title").add(edit.get("ConvertToBinary"));

		edit.put("ConvertToNested",new LMMenuItem(GUIMessages
				.getString("GUI.convertToNestedContext"), this, "convertCurrentContextToNestedContext")); //$NON-NLS-1$
		edit.get("Title").add(edit.get("ConvertToNested"));

		menuBar.add(edit.get("Title"));

		/* ==== LATTICE MENU ==== */
		lattice = new HashMap<>();
		lattice.put("Title", new JMenu(GUIMessages.getString("GUI.lattice"))); //$NON-NLS-1$
		lattice.get("Title").setMnemonic(KeyEvent.VK_T);

		lattice.put("ShowLatticeMenu", new LMMenuItem(GUIMessages
				.getString("GUI.showLattice"), this, "showCurrentLattice")); //$NON-NLS-1$
		lattice.get("ShowLatticeMenu").setMnemonic(KeyEvent.VK_L);
		lattice.get("ShowLatticeMenu").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		lattice.get("Title").add(lattice.get("ShowLatticeMenu"));

		menuBar.add(lattice.get("Title"));

		/* ==== RULES MENU ==== */
		rules = new HashMap<>();
		rules.put("Title", new JMenu(GUIMessages.getString("GUI.rules"))); //$NON-NLS-1$
		rules.get("Title").setMnemonic(KeyEvent.VK_R);

		rules.put("GenericBase", new LMMenuItem(GUIMessages
				.getString("GUI.genericBase"), this, "showCurrentRules")); //$NON-NLS-1$
		rules.get("GenericBase").setMnemonic(KeyEvent.VK_O);
		rules.get("GenericBase").setEnabled(false);
		rules.get("Title").add(rules.get("GenericBase"));
		
		rules.put("StemBaseMenu", new LMMenuItem(GUIMessages.getString("GUI.StemBase"), this, "showCurrentRulesStemBase")); //$NON-NLS-1$
		rules.get("StemBaseMenu").setMnemonic(KeyEvent.VK_R);
		rules.get("StemBaseMenu") .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		rules.get("StemBaseMenu") .setEnabled(false);
		rules.get("Title").add(rules.get("StemBaseMenu") );

		rules.put("ShowNegationImplications", new LMMenuItem(GUIMessages.getString("GUI.negationImplications"), this, "showNegationImplications"));
		rules.get("ShowNegationImplications").setEnabled(true);
		rules.get("Title").add(rules.get("ShowNegationImplications"));

		menuBar.add(rules.get("Title"));

		/* ==== TRIADIC MENU ==== */
		triadic = new HashMap<>();
		triadic.put("Title", new JMenu(GUIMessages.getString("GUI.triadic")));

		triadic.put("OpenTriadicContext", new LMMenuItem(GUIMessages.getString("GUI.openTriadic"), this, "openTriadicContext"));
		triadic.get("OpenTriadicContext").setEnabled(true);
		triadic.get("Title").add(triadic.get("OpenTriadicContext"));

		triadic.put("Implications", new LMMenuItem(GUIMessages.getString("GUI.triadicImplications"), this, "triadicImplications"));
		triadic.get("Implications").setEnabled(false);
		triadic.get("Title").add(triadic.get("Implications"));


		triadic.put("BACAR", new LMMenuItem(GUIMessages.getString("GUI.triadicBACAR"), this, "triadicBACAR"));
		triadic.get("BACAR").setEnabled(false);
		triadic.get("Title").add(triadic.get("BACAR"));

		triadic.put("BCAAR", new LMMenuItem(GUIMessages.getString("GUI.triadicBCAAR"), this, "triadicBCAAR"));
		triadic.get("BCAAR").setEnabled(false);
		triadic.get("Title").add(triadic.get("BCAAR"));

		menuBar.add(triadic.get("Title"));
		/* ==== WINDOW MENU ==== */
		windowMenu = new HashMap<>();

		//JMenu window = new JMenu(GUIMessages.getString("GUI.window")); //$NON-NLS-1$
		windowMenu.put("Title", new JMenu(GUIMessages.getString("GUI.window"))); //$NON-NLS-1$
		windowMenu.get("Title").setMnemonic(KeyEvent.VK_W);
		menuBar.add(windowMenu.get("Title"));

		/* ==== ABOUT MENU ==== */
		HashMap<String, JMenuItem> help = new HashMap<>();

		help.put("Title", new JMenu(GUIMessages.getString("GUI.about"))); //$NON-NLS-1$
		help.get("Title").setMnemonic(KeyEvent.VK_A);
		menuBar.add(help.get("Title"));


		help.put("About", new LMMenuItem(GUIMessages.getString("GUI.aboutMore"), this, "about"));
		help.get("About").setMnemonic(KeyEvent.VK_A);
		help.get("Title").add(help.get("About"));



		return menuBar;
	}

	/**
	 * Creer les {@link MenuItem} relatifs aux fichiers recements utilises
	 * 
	 * @return le nombre de {@link MenuItem} reellement pris en compte
	 */
	protected int setRecentFileMenuItem() {
		int nbRecentInMenu = 0;

		// Recupere les preferences de Lattice Miner
		Preferences preferences = LMPreferences.getPreferences();

		// Nom du noeud des preferences des fichiers recents
		int nbRecents = preferences.getInt(LMPreferences.NB_RECENTS, -1);

		if (nbRecents != -1) {

			int numNode = nbRecents;

			// Nom du noeud des preferences du "ieme" fichier recent
			String irecentNode = LMPreferences.RECENTS + "/" + numNode; //$NON-NLS-1$
			String filename = preferences.get(irecentNode, ""); //$NON-NLS-1$

			// Charge les 5 derniers fichiers ouverts (s'ils existent)
			while (!filename.equals("") && nbRecentInMenu < 5) { //$NON-NLS-1$

				// Rajoute le MenuItem
				JMenuItem recentMenuItem = new JMenuItem(filename);
				recentMenuItem.addActionListener(new RecentMenuListener(
						filename));
				file.get("OpenRecentContext").add(recentMenuItem);
				//openRecentContext.add(recentMenuItem);
				nbRecentInMenu++;

				// Decremente numNode
				numNode = numNode - 1;
				if (numNode == -1)
					numNode = 9;

				// Charge le fichier suivant
				irecentNode = LMPreferences.RECENTS + "/" + numNode; //$NON-NLS-1$
				filename = preferences.get(irecentNode, ""); //$NON-NLS-1$
			}

		}

		return nbRecentInMenu;
	}

	protected JPanel buildPanel() {
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(675, 400));
		panel.setLayout(new GridBagLayout());

		constraints = new GridBagConstraints();
		constraints.insets = new Insets(0, 0, 0, 0);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.NONE;

		panelTab = new JTabbedPane();
		panelTab.addChangeListener(e -> {
			int activeIndex = panelTab.getSelectedIndex();
			if (activeIndex != -1)
				selectContextAt(activeIndex);
		});

		currentContextName = new JLabel(GUIMessages
				.getString("GUI.noContextLoaded")); //$NON-NLS-1$
		panel.add(currentContextName, constraints);

		constraints.gridy = 1;
		constraints.weighty = 1.0;
		constraints.weightx = 1.0;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;

		contextGroup = new ButtonGroup();
		contextPanes = new Vector<>();
		currentContextIdx = -1;
		untitledCount = 0;

		noContextItem = new JMenuItem(GUIMessages
				.getString("GUI.noContextLoaded")); //$NON-NLS-1$
		noContextItem.setEnabled(false);

		windowMenu.get("Title").add(noContextItem);

		return panel;
	}

	protected JToolBar buildToolBar() {
		JToolBar toolBar = new JToolBar(GUIMessages.getString("GUI.quickTools")); //$NON-NLS-1$
		//ToolBarListener listener = new ToolBarListener();
		buttons = new HashMap<>();

		buttons.put("SaveBtn", new LMButton(LMIcons.getSave(), this, "openContext"));
		buttons.get("SaveBtn").setToolTipText(GUIMessages.getString("GUI.save")); //$NON-NLS-1$
		buttons.get("SaveBtn").setMnemonic(KeyEvent.VK_S);
		toolBar.add(buttons.get("SaveBtn"));

		buttons.put("OpenBtn", new LMButton(LMIcons.getOpenFolder(), this, "openContext"));
		buttons.get("OpenBtn").setToolTipText(GUIMessages.getString("GUI.openContext")); //$NON-NLS-1$
		buttons.get("OpenBtn").setMnemonic(KeyEvent.VK_O);
		toolBar.add(buttons.get("OpenBtn"));

		buttons.put("NewBinCtxBtn", new LMButton(LMIcons.getNewBinContext(), this ,"createBinaryContext"));
		buttons.get("NewBinCtxBtn").setToolTipText(GUIMessages
				.getString("GUI.newBinaryContext")); //$NON-NLS-1$
		buttons.get("NewBinCtxBtn").setMnemonic(KeyEvent.VK_B);
		toolBar.add(buttons.get("NewBinCtxBtn"));

		buttons.put("RemoveCtxBtn", new LMButton(LMIcons.getCloseContext(), this));
		buttons.get("RemoveCtxBtn").setToolTipText(GUIMessages
				.getString("GUI.closeCurrentContext")); //$NON-NLS-1$
		buttons.get("RemoveCtxBtn").setMnemonic(KeyEvent.VK_C);
		toolBar.add(buttons.get("RemoveCtxBtn"));

		buttons.put("NewObjectBtn", new LMButton(LMIcons.getAddObject(), this ,"addObjectToCurrentContext"));
		buttons.get("NewObjectBtn").setToolTipText(GUIMessages.getString("GUI.addObject")); //$NON-NLS-1$
		toolBar.add(buttons.get("NewObjectBtn"));

		buttons.put("NewAttributeBtn", new LMButton(LMIcons.getAddAttribute(), this, "addAttributeToCurrentContext"));
		buttons.get("NewAttributeBtn").setToolTipText(GUIMessages
				.getString("GUI.addAttribute")); //$NON-NLS-1$
		toolBar.add(buttons.get("NewAttributeBtn"));

		buttons.put("DelObjectBtn", new LMButton(LMIcons.getDelObject(), this, "removeObjectFromCurrentContext"));
		buttons.get("DelObjectBtn").setToolTipText(GUIMessages.getString("GUI.removeObject")); //$NON-NLS-1$
		toolBar.add(buttons.get("DelObjectBtn"));

		buttons.put("DelAttributeBtn", new LMButton(LMIcons.getDelAttribute(), this ,"removeAttributeFromCurrentContext"));
		buttons.get("DelAttributeBtn").setToolTipText(GUIMessages
				.getString("GUI.removeAttribute")); //$NON-NLS-1$
		toolBar.add(buttons.get("DelAttributeBtn"));


		/*clarifyContextBtn = new JButton(LMIcons.getShowRulesLittle());
		clarifyContextBtn.addActionListener(listener);
		clarifyContextBtn.setToolTipText(GUIMessages.getString("GUI.clarifyContextBtn")); //$NON-NLS-1$
		toolBar.add(clarifyContextBtn);*/

		/*reduceContextBtn = new JButton(LMIcons.getShowRulesLittle());
		reduceContextBtn.addActionListener(listener);
		reduceContextBtn.setToolTipText(GUIMessages.getString("GUI.reduceContext")); //$NON-NLS-1$
		toolBar.add(reduceContextBtn);*/

		/*complemContextBtn = new JButton(LMIcons.getShowRulesLittle());
		complemContextBtn.addActionListener(listener);
		complemContextBtn.setToolTipText(GUIMessages.getString("GUI.addComplemContext")); //$NON-NLS-1$
		toolBar.add(complemContextBtn);*/

		buttons.put("ShowLatBtn", new LMButton(LMIcons.getShowLattice(), this ,"showCurrentLattice"));
		buttons.get("ShowLatBtn").setToolTipText(GUIMessages.getString("GUI.showLattice")); //$NON-NLS-1$
		buttons.get("ShowLatBtn").setMnemonic(KeyEvent.VK_L);
		toolBar.add(buttons.get("ShowLatBtn"));

		buttons.put("ShowRulesBtn", new LMButton(LMIcons.getShowRulesLittle(), this, "showCurrentRules"));
		buttons.get("ShowRulesBtn").setToolTipText(GUIMessages.getString("GUI.showRules")); //$NON-NLS-1$
		buttons.get("ShowRulesBtn").setMnemonic(KeyEvent.VK_R);
		toolBar.add(buttons.get("ShowRulesBtn"));


		buttons.put("ShowRulesRed", new LMButton(LMIcons.getShowRulesLittle2(), this ,"showCurrentReducedRules"));
		buttons.get("ShowRulesRed").setToolTipText(GUIMessages.getString("GUI.showRules")); //$NON-NLS-1$
		buttons.get("ShowRulesRed").setMnemonic(KeyEvent.VK_R);
		toolBar.add(buttons.get("ShowRulesRed"));

		buttons.put("AppositionBtn", new LMButton(LMIcons.getAppositionLittle(), this, "apposition"));
		buttons.get("AppositionBtn").setToolTipText(GUIMessages.getString("GUI.apposition"));//-->LINDA
		buttons.get("AppositionBtn").setMnemonic(KeyEvent.VK_R);//-->LINDA
		toolBar.add(buttons.get("AppositionBtn"));//-->LINDA

		buttons.put("SubpositionBtn", new LMButton(LMIcons.getSubpositionLittle(), this ,"subposition"));
		buttons.get("SubpositionBtn").setToolTipText(GUIMessages.getString("GUI.subposition")); //-->LINDA
		buttons.get("SubpositionBtn").setMnemonic(KeyEvent.VK_R);//-->LINDA
		toolBar.add(buttons.get("SubpositionBtn"));//-->LINDA


		return toolBar;
	}

	protected void setActiveActions() {

		/*Elements du menu "Context"  et bouton en rapport */
		if(nestedNb > 1 || binaryNb > 1 || valuedNb > 1){

			buttons.get("AppositionBtn").setEnabled(true);
			buttons.get("SubpositionBtn").setEnabled(true);
		} else {
			buttons.get("AppositionBtn").setEnabled(false);
			buttons.get("SubpositionBtn").setEnabled(false);
		}

		if (ganterApp != null) {
			triadic.get("Implications").setEnabled(true);
			triadic.get("BACAR").setEnabled(true);
			triadic.get("BCAAR").setEnabled(true);
		}


		if (contextPanes.size() == 0) {
			/* Boutons de la toolbar */
			buttons.get("SaveBtn").setEnabled(false);
			buttons.get("RemoveCtxBtn").setEnabled(false);
			buttons.get("NewAttributeBtn").setEnabled(false);
			buttons.get("NewObjectBtn").setEnabled(false);
			buttons.get("DelAttributeBtn").setEnabled(false);
			buttons.get("DelObjectBtn").setEnabled(false);
			buttons.get("ShowLatBtn").setEnabled(false);
			buttons.get("ShowRulesBtn").setEnabled(false);
			buttons.get("ShowRulesRed").setEnabled(false);

			/* Elements du menu "File" */
			file.get("ViewTXT").setEnabled(false);
			file.get("SaveContext").setEnabled(false);
			file.get("SaveAsContext").setEnabled(false);
			file.get("SaveAllContext").setEnabled(false);
			file.get("CloseContext").setEnabled(false);
			file.get("CloseAllContexts").setEnabled(false);

			//reduceContextBtn.setEnabled(false);
			//complemContextBtn.setEnabled(false);
			//clarifyContextBtn.setEnabled(false);

			/* Elements du menu "Edit" */
			edit.get("Title").setEnabled(false);

			//clarifyContext.setEnabled(false);
			//reduceContext.setEnabled(false);
			//addComplemContext.setEnabled(false);
			//showArrowContext.setEnabled(false);

			edit.get("CreateClusters").setEnabled(false);
			edit.get("ConvertToBinary").setEnabled(false);
			edit.get("ConvertToNested").setEnabled(false);

			/* Elements du menu "Lattice" */
			lattice.get("Title").setEnabled(false);

			/* Elements du menu "Rules" */
			rules.get("Title").setEnabled(false);

			return;
		}
		/* Menu file */
		file.get("SaveContext").setEnabled(false);
		file.get("SaveContext").setEnabled(true);
		file.get("SaveAsContext").setEnabled(true);
		file.get("SaveAllContext").setEnabled(true);
		file.get("CloseContext").setEnabled(true);
		file.get("CloseAllContexts").setEnabled(true);
		file.get("ViewTXT").setEnabled(true);


		edit.get("Title").setEnabled(true);


		/* toolbar */
		buttons.get("SaveBtn").setEnabled(true);
		buttons.get("RemoveCtxBtn").setEnabled(true);

		rules.get("StemBaseMenu").setEnabled(true);

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();




		if (currentContext instanceof NestedContext) {
			/* Boutons de la toolbar */
			buttons.get("NewAttributeBtn").setEnabled(false);
			buttons.get("NewObjectBtn").setEnabled(false);
			buttons.get("DelAttributeBtn").setEnabled(false);
			buttons.get("DelObjectBtn").setEnabled(false);
			buttons.get("ShowLatBtn").setEnabled(true);
			buttons.get("ShowRulesBtn").setEnabled(true);
			buttons.get("ShowRulesRed").setEnabled(true);

			/*clarifyContextBtn.setEnabled(true);
			reduceContextBtn.setEnabled(true);
			complemContextBtn.setEnabled(true);
			/***                         ***/

			/* Elements du menu "Edit" */
			edit.get("AddEmptyLevel").setEnabled(true);
			edit.get("AddContextLevel").setEnabled(true);
			edit.get("RemoveLevel").setEnabled(true);
			edit.get("OrderLevels").setEnabled(true);
			edit.get("AddObject").setEnabled(false);
			edit.get("AddAttribute").setEnabled(false);
			edit.get("MergeAttributes").setEnabled(false);
			edit.get("LogicalAttribute").setEnabled(false);
			edit.get("CompareAttributes").setEnabled(false);
			edit.get("RemoveObject").setEnabled(false);
			edit.get("RemoveAttribute").setEnabled(false);
			edit.get("CreateClusters").setEnabled(false);
			edit.get("ConvertToBinary").setEnabled(true);
			edit.get("ConvertToNested").setEnabled(false);
			edit.get("TaxonomyAttribute").setEnabled(false); //-->LINDA
			edit.get("TaxonomyObject").setEnabled(false); //-->LINDA
			edit.get("SaveTaxonomy").setEnabled(false);//-->LINDA

			//clarifyContext.setEnabled(true);
			edit.get("ClarifyObject").setEnabled(true);
			edit.get("ClarifyAttribute").setEnabled(true);
			edit.get("ClarifyAll").setEnabled(true);

			edit.get("ReduceObject").setEnabled(true);
			edit.get("ReduceAttribute").setEnabled(true);
			edit.get("ReduceContext").setEnabled(true);

			edit.get("ComplementaryContext").setEnabled(true);
			edit.get("GlobalContext").setEnabled(true);

			edit.get("ShowArrowContext").setEnabled(true);
			edit.get("ShowArrowRelation").setEnabled(true);
			edit.get("HideArrowContext").setEnabled(true);
			//showArrowContext.setEnabled(true);
			edit.get("TransitiveClosureObject").setEnabled(true);
			edit.get("TransitiveClosureAtt").setEnabled(true);

			/* Elements du menu "Lattice" */
			lattice.get("Title").setEnabled(true);

			/* Elements du menu "Rules" */
			rules.get("Title").setEnabled(true);
			rules.get("StemBaseMenu").setEnabled(true);

		}

		else if (currentContext instanceof BinaryContext) {
			/* Boutons de la toolbar */
			buttons.get("NewAttributeBtn").setEnabled(true);
			buttons.get("NewObjectBtn").setEnabled(true);
			buttons.get("DelAttributeBtn").setEnabled(true);
			buttons.get("DelObjectBtn").setEnabled(true);
			buttons.get("ShowLatBtn").setEnabled(true);
			buttons.get("ShowRulesBtn").setEnabled(true);
			buttons.get("ShowRulesRed").setEnabled(true);


			/* Elements du menu "Edit" */
			edit.get("AddEmptyLevel").setEnabled(false);
			edit.get("AddContextLevel").setEnabled(false);
			edit.get("RemoveLevel").setEnabled(false);
			edit.get("OrderLevels").setEnabled(false);
			edit.get("AddObject").setEnabled(true);
			edit.get("AddAttribute").setEnabled(true);
			edit.get("MergeAttributes").setEnabled(true);
			edit.get("LogicalAttribute").setEnabled(true);
			edit.get("RemoveObject").setEnabled(true);
			edit.get("RemoveAttribute").setEnabled(true);
			edit.get("CreateClusters").setEnabled(true);
			edit.get("CompareAttributes").setEnabled(true);
			edit.get("ConvertToBinary").setEnabled(false);
			edit.get("ConvertToNested").setEnabled(true);

			//clarifyContext.setEnabled(true);
			edit.get("ClarifyObject").setEnabled(true);
			edit.get("ClarifyAttribute").setEnabled(true);
			edit.get("ClarifyAll").setEnabled(true);

			edit.get("ReduceObject").setEnabled(true);
			edit.get("ReduceAttribute").setEnabled(true);
			edit.get("ReduceContext").setEnabled(true);

			edit.get("ComplementaryContext").setEnabled(true);
			edit.get("GlobalContext").setEnabled(true);

			edit.get("ShowArrowContext").setEnabled(true);
			edit.get("ShowArrowRelation").setEnabled(true);
			edit.get("HideArrowContext").setEnabled(true);
			edit.get("TransitiveClosureObject").setEnabled(true);
			edit.get("TransitiveClosureAtt").setEnabled(true);

			/* Elements du menu "Lattice" */
			lattice.get("Title").setEnabled(true);

			/* Elements du menu "Rules" */
			rules.get("StemBaseMenu").setEnabled(true);
			rules.get("Title").setEnabled(true);
			rules.get("GenericBase").setEnabled(true);
		}

		else if (currentContext instanceof ValuedContext) {
			/* Boutons de la toolbar */
			buttons.get("NewAttributeBtn").setEnabled(true);
			buttons.get("NewObjectBtn").setEnabled(true);
			buttons.get("DelAttributeBtn").setEnabled(true);
			buttons.get("DelObjectBtn").setEnabled(true);
			buttons.get("ShowLatBtn").setEnabled(false);
			buttons.get("ShowRulesBtn").setEnabled(false);
			buttons.get("ShowRulesRed").setEnabled(false);


			/* Elements du menu "Edit" */
			edit.get("AddEmptyLevel").setEnabled(false);
			edit.get("AddContextLevel").setEnabled(false);
			edit.get("RemoveLevel").setEnabled(false);
			edit.get("OrderLevels").setEnabled(false);
			edit.get("AddObject").setEnabled(true);
			edit.get("AddAttribute").setEnabled(true);
			edit.get("MergeAttributes").setEnabled(true);
			edit.get("LogicalAttribute").setEnabled(false);
			edit.get("CompareAttributes").setEnabled(false);
			edit.get("RemoveObject").setEnabled(true);
			edit.get("RemoveAttribute").setEnabled(true);
			edit.get("CreateClusters").setEnabled(false);
			edit.get("ConvertToBinary").setEnabled(true);
//			edit.get("ConvertToNested").setEnabled(false);
//			edit.get("TaxonomyAttribute").setEnabled(false); //-->LINDA
//			edit.get("TaxonomyObject").setEnabled(false); //-->LINDA
//			edit.get("SaveTaxonomy").setEnabled(false);//-->LINDA

			/* Elements du menu "Lattice" */
			//showLatticeMenu.setEnabled(false);
			lattice.get("Title").setEnabled(false); // FIXME ?? (false)

			/* Elements du menu "Rules" */
			rules.get("Title").setEnabled(false);
		}

		else {
			/* Boutons de la toolbar */
			buttons.get("SaveBtn").setEnabled(false);
			buttons.get("OpenBtn").setEnabled(false);
			buttons.get("NewBinCtxBtn").setEnabled(false);
			buttons.get("RemoveCtxBtn").setEnabled(false);
			buttons.get("NewAttributeBtn").setEnabled(false);
			buttons.get("NewObjectBtn").setEnabled(false);
			buttons.get("DelAttributeBtn").setEnabled(false);
			buttons.get("DelObjectBtn").setEnabled(false);
			buttons.get("ShowLatBtn").setEnabled(false);
			buttons.get("ShowRulesBtn").setEnabled(false);

			/* Elements du menu "Edit" */
			edit.get("AddEmptyLevel").setEnabled(false);
			edit.get("AddContextLevel").setEnabled(false);
			edit.get("RemoveLevel").setEnabled(false);
			edit.get("OrderLevels").setEnabled(false);
			edit.get("AddObject").setEnabled(false);
			edit.get("AddAttribute").setEnabled(false);
			edit.get("MergeAttributes").setEnabled(false);
			edit.get("LogicalAttribute").setEnabled(false);
			edit.get("CompareAttributes").setEnabled(false);
			edit.get("RemoveObject").setEnabled(false);
			edit.get("RemoveAttribute").setEnabled(false);
			edit.get("CreateClusters").setEnabled(false);
			edit.get("ConvertToBinary").setEnabled(false);
			edit.get("ConvertToNested").setEnabled(false);
//
//			edit.get("TaxonomyAttribute").setEnabled(false); //-->LINDA
//			edit.get("TaxonomyObject").setEnabled(false); //-->LINDA
//			edit.get("SaveTaxonomy").setEnabled(false);//-->LINDA

			/* Elements du menu "Lattice" */
			lattice.get("ShowLatticeMenu").setEnabled(false);

			/* Elements du menu "Rules" */
			rules.get("Title").setEnabled(false);
		}
	}

	/**
	 * @return le panneau actuellement selectionne
	 */
	public ContextTableScrollPane getSelectedPane() {
		if (contextPanes.size() == 0)
			return null;

		else
			return contextPanes.elementAt(currentContextIdx);
	}

	/**
	 * Permet de selectionner un contexte en particulier
	 * 
	 * @param idx
	 *            l'index du {@link ContextTableScrollPane} a mettre en avant
	 */
	protected void selectContextAt(int idx) {
		if (idx >= contextPanes.size())
			return;

		// Recupere le panneau souhaite
		currentContextIdx = idx;
		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);

		// Recherche le nom du context
		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();
		if (currentContext instanceof NestedContext)
			currentContextName
			.setText(GUIMessages.getString("GUI.context") + " : " + ((NestedContext) currentContext).getNestedContextName()); //$NON-NLS-1$ //$NON-NLS-2$
		else
			currentContextName
			.setText(GUIMessages.getString("GUI.context") + " : " + currentContext.getName()); //$NON-NLS-1$ //$NON-NLS-2$

		// Rajoute si necesaire le panelTab
		if (!panel.isAncestorOf(panelTab)) {
			panel.add(panelTab, constraints);
		}

		// Rajoute l'onglet s'il n'existe pas
		int tabIndex = panelTab.indexOfComponent(selectedPane);
		if (tabIndex == -1) {
			panelTab.addTab(currentContextName.getText(), selectedPane);
			tabIndex = panelTab.indexOfComponent(selectedPane);
		}
		panelTab.setSelectedIndex(tabIndex);

		((JRadioButtonMenuItem) ((JMenu)windowMenu.get("Title")).getMenuComponent(currentContextIdx))
		.setSelected(true);

		setActiveActions();
		frame.repaint();
	}

	/**
	 * Permet d'ajouter un contexte binaire au {@link ContextViewer}
	 * 
	 * @param binCtx
	 *            le contexte binaire a ajouter
	 */
	public void addBinaryContext(BinaryContext binCtx) {

		BinaryContextTable newTable = new BinaryContextTable(binCtx);
		ContextTableScrollPane newScrollPane = new ContextTableScrollPane(
				newTable);
		contextPanes.add(newScrollPane);

		// MenuItem pour le contexte courant
		JRadioButtonMenuItem contextBtn;
		contextBtn = new JRadioButtonMenuItem(binCtx.getName());
		contextBtn.addActionListener(new ViewTableListener(newScrollPane));
		contextGroup.add(contextBtn);
		windowMenu.get("Title").add(contextBtn);

		// Supprime le MenuItem "No Context" s'il existe
		if (((JMenu)windowMenu.get("Title")).isMenuComponent(noContextItem))
			windowMenu.get("Title").remove(noContextItem);
		contextBtn.setSelected(true);

		// Rajoute le raccourci "Ctrl + Num" au context
		int num = ((JMenu)windowMenu.get("Title")).getMenuComponentCount();
		//System.out.println(num);
		//contextBtn.setAccelerator(KeyStroke.getKeyStroke("ctrl NUMPAD" + num)); //$NON-NLS-1$
		contextBtn.setAccelerator(KeyStroke.getKeyStroke(num + KeyEvent.VK_0,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		// Selectionne effectivement le contexte
		selectContextAt(contextPanes.size() - 1);

		binaryNb++;//-->LINDA
	}

	protected void createBinaryContext() {
		String newName = DialogBox
		.showInputQuestion(
				panel,
				GUIMessages.getString("GUI.enterNewContextName"), GUIMessages.getString("GUI.createNewBinaryContext")); //$NON-NLS-1$ //$NON-NLS-2$
		  

		if (newName != null && newName.length() == 0) {
			untitledCount++;
			newName = GUIMessages.getString("GUI.untitled") + "_" + untitledCount; //$NON-NLS-1$ //$NON-NLS-2$
		}

		int objNb = -1;
		while (objNb == -1 && newName != null) {
			try {
				String str = DialogBox
				.showInputQuestion(
						panel,
						GUIMessages
						.getString("GUI.enterNumberOfObjects"), GUIMessages.getString("GUI.createNewBinaryContext")); //$NON-NLS-1$ //$NON-NLS-2$
				if (str != null)
					objNb = Integer.parseInt(str);
				else
					objNb = -2;
			} catch (NumberFormatException nfe) {
				objNb = -1;
			}
		}

		int attNb = -1;
		while (attNb == -1 && objNb != -2 && newName != null) {
			try {
				String str = DialogBox
				.showInputQuestion(
						panel,
						GUIMessages
						.getString("GUI.enterNumberOfAttributes"), GUIMessages.getString("GUI.createNewBinaryContext")); //$NON-NLS-1$ //$NON-NLS-2$
				if (str != null)
					attNb = Integer.parseInt(str);
				else
					attNb = -2;
			} catch (NumberFormatException nfe) {
				attNb = -1;
			}
		}

		if (attNb >= 0) {
			// if(contextPanes.size() == 0)
			// windowMenu.removeAll();

			BinaryContext newBinCtx = new BinaryContext(newName, objNb, attNb);
			addBinaryContext(newBinCtx);
		}

		else {
			DialogBox
			.showMessageInformation(
					panel,
					GUIMessages
					.getString("GUI.noContextHasBeenCreated"), GUIMessages.getString("GUI.noContextCreated")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Permet d'ajouter un contexte value au {@link ContextViewer}
	 * 
	 * @param valCtx
	 *            le contexte binaire a ajouter
	 */
	protected void addValuedContext(ValuedContext valCtx) {

		ValuedContextTable newTable = new ValuedContextTable(valCtx);
		ContextTableScrollPane newScrollPane = new ContextTableScrollPane(
				newTable);
		contextPanes.add(newScrollPane);

		JRadioButtonMenuItem contextBtn;
		contextBtn = new JRadioButtonMenuItem(valCtx.getName());
		contextBtn.addActionListener(new ViewTableListener(newScrollPane));
		contextGroup.add(contextBtn);
		windowMenu.get("Title").add(contextBtn);
		if (((JMenu) windowMenu.get("Title")).isMenuComponent(noContextItem))
			windowMenu.get("Title").remove(noContextItem);
		contextBtn.setSelected(true);

		int num = ((JMenu) windowMenu.get("Title")).getMenuComponentCount();
		//contextBtn.setAccelerator(KeyStroke.getKeyStroke("ctrl NUMPAD" + num)); //$NON-NLS-1$
		contextBtn.setAccelerator(KeyStroke.getKeyStroke(num + KeyEvent.VK_B,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		selectContextAt(contextPanes.size() - 1);

		valuedNb++;//-->LINDA
	}

	protected void createValuedContext() {
		String newName = DialogBox
		.showInputQuestion(
				panel,
				GUIMessages.getString("GUI.enterNewContextName"), GUIMessages.getString("GUI.createNewValuedContext")); //$NON-NLS-1$ //$NON-NLS-2$

		if (newName != null && newName.length() == 0) {
			untitledCount++;
			newName = GUIMessages.getString("GUI.untitled") + "_" + untitledCount; //$NON-NLS-1$ //$NON-NLS-2$
		}

		int objNb = -1;
		while (objNb == -1 && newName != null) {
			try {
				String str = DialogBox
				.showInputQuestion(
						panel,
						GUIMessages
						.getString("GUI.enterNumberOfObjects"), GUIMessages.getString("GUI.createNewValuedContext")); //$NON-NLS-1$ //$NON-NLS-2$
				if (str != null)
					objNb = Integer.parseInt(str);
				else
					objNb = -2;
			} catch (NumberFormatException nfe) {
				objNb = -1;
			}
		}

		int attNb = -1;
		while (attNb == -1 && objNb != -2 && newName != null) {
			try {
				String str = DialogBox
				.showInputQuestion(
						panel,
						GUIMessages
						.getString("GUI.enterNumberOfAttributes"), GUIMessages.getString("GUI.createNewValuedContext")); //$NON-NLS-1$ //$NON-NLS-2$
				if (str != null)
					attNb = Integer.parseInt(str);
				else
					attNb = -2;
			} catch (NumberFormatException nfe) {
				attNb = -1;
			}
		}

		if (attNb >= 0) {
			// if(contextPanes.size() == 0)
			// windowMenu.removeAll();

			ValuedContext newValCtx = new ValuedContext(newName, objNb, attNb);
			addValuedContext(newValCtx);
		}

		else {
			DialogBox
			.showMessageInformation(
					panel,
					GUIMessages
					.getString("GUI.noContextHasBeenCreated"), GUIMessages.getString("GUI.noContextCreated")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}



	protected boolean TXTContext() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		StringBuilder fichierContenu= new StringBuilder();
		JTextArea champTexte = new JTextArea();
		if (contextPanes.size() == 0)
			return false;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();

		try	{
			File fichier=currentContext.getContextFile();
			String chemin=fichier.getPath();
			RandomAccessFile raf = new RandomAccessFile(chemin, "r");
			fichierContenu.append(raf.readLine()).append("\n");
			if(fichierContenu.toString().startsWith("<?xml")) {
				fichierContenu = new StringBuilder(Context.HeaderType.SLF_BINARY + "\n");
			} else if (!fichierContenu.toString().startsWith("LM")) {
				fichierContenu = new StringBuilder(Context.HeaderType.XLS_BINARY + "\n");
			}
		} catch (Exception e) {
			if(currentContext instanceof BinaryContext) {
				fichierContenu.append(Context.HeaderType.LM_BINARY + "\n");
			} else if (currentContext instanceof ValuedContext) {
				fichierContenu.append(Context.HeaderType.LM_VALUED + "\n");
			} else {
				fichierContenu.append("UNKNOW_FORMAT \n");
			}
		}

		for (int i = 0; i < currentContext.getObjectCount(); i++) {
			fichierContenu.append("| ").append(currentContext.getObjectAt(i)).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		fichierContenu.append("\n");
		for (int i = 0; i < currentContext.getAttributeCount(); i++) {
			fichierContenu.append("| ").append(currentContext.getAttributeAt(i)).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		fichierContenu.append("\n");
		for (int i = 0; i < currentContext.getObjectCount(); i++) {
			for (int j = 0; j < currentContext.getAttributeCount(); j++) {
				if (Objects.equals(currentContext.getValueAt(i, j), BinaryContext.TRUE))
					fichierContenu.append("1 "); //$NON-NLS-1$
				else
					fichierContenu.append("0 "); //$NON-NLS-1$
			}
			fichierContenu.append("\n");
		}
		champTexte.setText(fichierContenu.toString());

		JFrame fenetre = new JFrame(); 
		JTextArea tf=new JTextArea();
		fenetre.setTitle("Contexte en Format texte");
		//Definit une taille pour celle-ci ; ici, 400 px de large et 500 px de haut
		fenetre.setSize(400, 500);
		//Nous allons maintenant dire a notre objet de se positionner au centre
		fenetre.setLocationRelativeTo(null);
		//Terminer le processus lorsqu'on clique sur "Fermer"
		fenetre.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		fenetre.setVisible(true);

		String temp = fichierContenu.toString();
		// Copy the txt export in the clipboard
		clipboard.setContents(new StringSelection(temp), null);

		tf.setText(temp);
		tf.setEditable(false);
		fenetre.add(tf);

		return true;
	}

	/**
	 * Permet d'ajouter un contexte imbrique au {@link ContextViewer}
	 * 
	 * @param nestedCtx
	 *            le contexte binaire a ajouter
	 */
	public void addNestedContext(NestedContext nestedCtx) {

		NestedContextTable newTable = new NestedContextTable(nestedCtx);
		ContextTableScrollPane newScrollPane = new ContextTableScrollPane(
				newTable);
		contextPanes.add(newScrollPane);

		JRadioButtonMenuItem contextBtn;
		contextBtn = new JRadioButtonMenuItem(nestedCtx.getNestedContextName());
		contextBtn.addActionListener(new ViewTableListener(newScrollPane));
		contextGroup.add(contextBtn);
		if (((JMenu) windowMenu.get("Title")).isMenuComponent(noContextItem))
			windowMenu.get("Title").remove(noContextItem);
		windowMenu.get("Title").add(contextBtn);

		int num = ((JMenu) windowMenu.get("Title")).getMenuComponentCount();
		//contextBtn.setAccelerator(KeyStroke.getKeyStroke("ctrl NUMPAD" + num)); //$NON-NLS-1$
		contextBtn.setAccelerator(KeyStroke.getKeyStroke(num + KeyEvent.VK_0,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		contextBtn.setSelected(true);
		selectContextAt(contextPanes.size() - 1);

		nestedNb++;//-->LINDA
	}

	protected void createNestedContext() {
		Vector<BinaryContext> contexts = new Vector<>();
		for (int i = 0; i < contextPanes.size(); i++) {
			ContextTableScrollPane scrollPane = contextPanes.elementAt(i);
			Context ctx = ((ContextTableModel) scrollPane.getContextTable()
					.getModel()).getContext();
			if ((ctx instanceof BinaryContext)
					&& !(ctx instanceof NestedContext))
				contexts.add((BinaryContext) ctx);
		}

		new NestedContextCreationAssistant(this, contexts);
	}

	/**
	 * Genere une fenetre pour pouvoir ouvrir un contexte existant en fichier et
	 * verifie les types et genere le contexte en fonction du type de fichier
	 * via les readers
	 */
	protected void openContext() {
		JFileChooser fileChooser = new JFileChooser(LMPreferences
				.getLastDirectory());

		// Proprietes du fileChooser
		fileChooser.setApproveButtonText(GUIMessages
				.getString("GUI.openButton")); //$NON-NLS-1$
		fileChooser.setDialogTitle(GUIMessages.getString("GUI.openAContext")); //$NON-NLS-1$
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Gere les extensions compatibles (cex, lmv, lmn, lmb)
		ExampleFileFilter filterCex = new ExampleFileFilter(
				"cex", GUIMessages.getString("GUI.conceptExplorerBinaryFormat")); //$NON-NLS-1$ //$NON-NLS-2$
		fileChooser.addChoosableFileFilter(filterCex);
		ExampleFileFilter filterXLS = new ExampleFileFilter(new String[] {
				"xls", "xlsx" } , //$NON-NLS-1$ //$NON-NLS-2$
				GUIMessages.getString("GUI.excelXLSBinaryFormat")); //$NON-NLS-1$
		fileChooser.addChoosableFileFilter(filterXLS);
		ExampleFileFilter filterGaliciaBinSLF = new ExampleFileFilter(
				"slf", GUIMessages.getString("GUI.galiciaSLFBinaryFormat")); //$NON-NLS-1$ //$NON-NLS-2$
		fileChooser.addChoosableFileFilter(filterGaliciaBinSLF);
		ExampleFileFilter filterGaliciaBin = new ExampleFileFilter(
				"bin.xml", GUIMessages.getString("GUI.galiciaXMLBinaryFormat")); //$NON-NLS-1$ //$NON-NLS-2$
		fileChooser.addChoosableFileFilter(filterGaliciaBin);
		ExampleFileFilter filterValued = new ExampleFileFilter(
				"lmv", GUIMessages.getString("GUI.LatticeMinerValuedFormat")); //$NON-NLS-1$ //$NON-NLS-2$
		fileChooser.addChoosableFileFilter(filterValued);
		ExampleFileFilter filterNested = new ExampleFileFilter(
				"lmn", GUIMessages.getString("GUI.LatticeMinerNestedFormat")); //$NON-NLS-1$ //$NON-NLS-2$
		fileChooser.addChoosableFileFilter(filterNested);
		ExampleFileFilter filterBinary = new ExampleFileFilter(
				"lmb", GUIMessages.getString("GUI.LatticeMinerBinaryFormat")); //$NON-NLS-1$ //$NON-NLS-2$
		fileChooser.addChoosableFileFilter(filterBinary);
		ExampleFileFilter filterLM = new ExampleFileFilter(new String[] {
				"lmb", "lmn", "lmv" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				GUIMessages.getString("GUI.LatticeMinerFormats")); //$NON-NLS-1$
		fileChooser.addChoosableFileFilter(filterLM);



		// La boite de dialogue
		int returnVal = fileChooser.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File contextFile = fileChooser.getSelectedFile();
			openContextFile(contextFile);

			// Sauvegarde le path utilise
			LMPreferences.setLastDirectory(fileChooser.getCurrentDirectory()
					.getAbsolutePath());
		}
	}

	protected void openContextFile(File contextFile) {

		String contextFileAbsolutePath = contextFile.getAbsolutePath();

		// Verifie que le fichier existe
		if (!contextFile.exists()) {
			DialogBox
			.showMessageError(
					this,
					GUIMessages.getString("GUI.fileDoesntExist"), GUIMessages.getString("GUI.errorWhileReadingFile")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// Verifie que le fichier n'est pas deja ouvert
		else if (contextFiles.contains(contextFileAbsolutePath)) {
			DialogBox.showMessageInformation(this, GUIMessages
					.getString("GUI.correspondingTabSelected"), //$NON-NLS-1$
					GUIMessages.getString("GUI.contextAlreadyOpen")); //$NON-NLS-1$
			for (ContextTableScrollPane openPane : contextPanes) {
				String paneFile = ((ContextTableModel) openPane
						.getContextTable().getModel()).getContext()
						.getContextFile().getAbsolutePath();
				if (paneFile.equals(contextFileAbsolutePath))
					selectContextAt(contextPanes.indexOf(openPane));
			}
			return;
		}

		try {

			// Recupere l'extension du fichier
			String fileType = ExampleFileFilter.getExtension(contextFile);

			switch (fileType) {
				case "lmb": { //$NON-NLS-1$
					LMBinaryContextReader contextReader = new LMBinaryContextReader(
							contextFile);
					BinaryContext binCtx = (BinaryContext) contextReader
							.getContext();
					addBinaryContext(binCtx);
					binCtx.setModified(false);
					binaryNb++;//-->LINDA
					break;
				}
				case "cex": { //$NON-NLS-1$
					CexBinaryContextReader contextReader = new CexBinaryContextReader(
							contextFile);
					BinaryContext binCtx = (BinaryContext) contextReader
							.getContext();
					addBinaryContext(binCtx);
					binCtx.setModified(false);
					binaryNb++;//-->LINDA
					break;
				}
				case "bin.xml": { //$NON-NLS-1$
					GaliciaXMLBinaryContextReader contextReader = new GaliciaXMLBinaryContextReader(
							contextFile);
					BinaryContext binCtx = (BinaryContext) contextReader
							.getContext();
					addBinaryContext(binCtx);
					binCtx.setModified(false);

					binaryNb++;//-->LINDA
					break;
				}
				case "slf": { //$NON-NLS-1$
					GaliciaSLFBinaryContextReader contextReader = new GaliciaSLFBinaryContextReader(
							contextFile);
					BinaryContext binCtx = (BinaryContext) contextReader
							.getContext();
					addBinaryContext(binCtx);
					binCtx.setModified(false);

					binaryNb++;//-->LINDA
					break;
				}
				case "lmn": { //$NON-NLS-1$
					LMNestedContextReader contextReader = new LMNestedContextReader(
							contextFile);
					NestedContext nesCtx = (NestedContext) contextReader
							.getContext();
					addNestedContext(nesCtx);
					nesCtx.setModified(false);

					nestedNb++;//-->LINDA
					break;
				}
				case "lmv": { //$NON-NLS-1$
					LMValuedContextReader contextReader = new LMValuedContextReader(
							contextFile);
					ValuedContext valCtx = (ValuedContext) contextReader
							.getContext();
					addValuedContext(valCtx);
					valCtx.setModified(false);
					break;
				}
				default:
					DialogBox.showMessageError(this, GUIMessages
									.getString("GUI.fileDoesntContainAKnownContextFormat"), //$NON-NLS-1$
							GUIMessages.getString("GUI.wrongContextFormat")); //$NON-NLS-1$

					return;
			}

			contextFiles.add(contextFileAbsolutePath);
			// Rajoute le fichier a la liste des fichiers recemments ouverts
			addRecentFilePreferences(contextFileAbsolutePath);
			// Refresh the menu
			setActiveActions();
			frame.repaint();
		} catch (FileNotFoundException e) {
			DialogBox
			.showMessageError(
					this,
					GUIMessages.getString("GUI.fileCannotBeFound"), GUIMessages.getString("GUI.errorWithFile")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (ReaderException e) {
			DialogBox.showMessageError(this, e);
		}
	}

	protected boolean openTriadicContext() {
		JFileChooser fileChooser = new JFileChooser(LMPreferences.getLastDirectory());

		fileChooser.setApproveButtonText(GUIMessages
				.getString("GUI.openButton"));
		fileChooser.setDialogTitle(GUIMessages.getString("GUI.openATriadicContext"));
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Compatible extension is JSON
		ExampleFileFilter filterJson = new ExampleFileFilter(
				"json", GUIMessages.getString("GUI.triadicJsonFormat"));
		fileChooser.addChoosableFileFilter(filterJson);

		// La boite de dialogue
		int returnVal = fileChooser.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File triadicContextFile = fileChooser.getSelectedFile();
			openTriadicContextFile(triadicContextFile);

			// Sauvegarde le path utilise
			LMPreferences.setLastDirectory(fileChooser.getCurrentDirectory()
					.getAbsolutePath());
			return true;
		}
		return false;
	}

	protected void openTriadicContextFile(File contextFile) {
		String contextFileAbsolutePath = contextFile.getAbsolutePath();

		// Check if file exists
		if (!contextFile.exists()) {
			DialogBox
					.showMessageError(
							this,
							GUIMessages.getString("GUI.fileDoesntExist"), GUIMessages.getString("GUI.errorWhileReadingFile")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		String fileType = ExampleFileFilter.getExtension(contextFile);

		if (fileType.equals("json")) {
			ganterApp = new Ganter(contextFileAbsolutePath);
			BinaryContext binCtx = TriadicContext.getFlatBinaryContextFromTriadic(ganterApp.getTriadicJsonObject());
			addBinaryContext(binCtx);
			binCtx.setModified(false);
		}

		else {
			DialogBox.showMessageError(this, GUIMessages
							.getString("GUI.fileDoesntContainAKnownContextFormat"), //$NON-NLS-1$
					GUIMessages.getString("GUI.wrongContextFormat")); //$NON-NLS-1$
		}
		contextFiles.add(contextFileAbsolutePath);
		triadicNb++;

		setActiveActions();

		frame.repaint();
	}

	protected void triadicImplications() {
		String caiRules = ganterApp.showRules();
		simpleRulesViewer(caiRules, GUIMessages.getString("GUI.triadicImplications"));
	}

	private void simpleRulesViewer(String rules, String title) {
		JFrame jFrame = new JFrame();

		JTextArea jTextArea = new JTextArea();
		jTextArea.setText(rules);
		jTextArea.setEditable(false);

		JScrollPane scrolll = new JScrollPane(jTextArea);

		jFrame.setTitle(title);
		jFrame.setSize(400, 500);
		jFrame.setLocationRelativeTo(null);
		jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jFrame.setVisible(true);
		jFrame.add(scrolll);
		jFrame.setVisible(true);
	}

	protected void triadicBACAR() {
		// TODO: BACAR
		ContextTableScrollPane selectedPane = contextPanes
				.elementAt(currentContextIdx);

		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		BinaryContext binCtx = (BinaryContext) currentContext;

		ConceptLattice lattice = binCtx.getConceptLattice();
		if (lattice == null)
			lattice = new ConceptLattice(binCtx);

		double minSupp = askMinSupport();
		double minConf = askMinConfidence();

		RuleAlgorithm algo = new InformativeBasisAlgorithm(lattice,minSupp / 100.0, minConf / 100.0);

		Vector<Rule> rules = algo.getRules();
		String bacarRules = TriadicAlgorithms.minnerBACAR(rules);
		simpleRulesViewer(bacarRules, GUIMessages.getString("GUI.triadicBACAR"));
	}

	protected void triadicBCAAR() {
		// TODO: BCAAR

		ContextTableScrollPane selectedPane = contextPanes
				.elementAt(currentContextIdx);

		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		BinaryContext binCtx = (BinaryContext) currentContext;

		ConceptLattice lattice = binCtx.getConceptLattice();
		if (lattice == null)
			lattice = new ConceptLattice(binCtx);

		double minSupp = askMinSupport();
		double minConf = askMinConfidence();

		RuleAlgorithm algo = new InformativeBasisAlgorithm(lattice,minSupp / 100.0, minConf / 100.0);
		Vector<Rule> rules = algo.getRules();
		String bcaarRules = TriadicAlgorithms.minnerBCAAR(rules);
		simpleRulesViewer(bcaarRules, GUIMessages.getString("GUI.triadicBCAAR"));

	}

	protected boolean hasSaveCurrentContext() {
		if (contextPanes.size() == 0)
			return false;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		// Sauvegarde pour la premiere fois
		if (currentContext.getContextFile() == null) {
			return hasSaveCurrentContextAs();
		}
		// Sauvegarde sur un fichier deja existant
		else {

			File fileName = currentContext.getContextFile();

			// Sauvegarde le context
			saveDependingOnFileType(fileName, currentContext);

			return true;
		}
	}

	protected boolean hasSaveCurrentContextAs() {
		if (contextPanes.size() == 0)
			return false;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		JFileChooser fileChooser = new JFileChooser(LMPreferences
				.getLastDirectory());

		// Proprietes du fileChooser
		fileChooser.setApproveButtonText(GUIMessages.getString("GUI.save")); //$NON-NLS-1$
		fileChooser.setDialogTitle(GUIMessages
				.getString("GUI.saveCurrentContext")); //$NON-NLS-1$
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Gere les extensions compatibles (lmv, lmn, lmb)
		// et met un nom de fichier par defaut
		if (currentContext instanceof ValuedContext) {
			ExampleFileFilter filterValued = new ExampleFileFilter(
					"lmv", GUIMessages.getString("GUI.LatticeMinerValuedFormat")); //$NON-NLS-1$ //$NON-NLS-2$
			fileChooser.addChoosableFileFilter(filterValued);
			fileChooser
			.setSelectedFile(new File(
					currentContext.getName()
					+ GUIMessages
					.getString("GUI.LatticeMinerValuedFormatDefaultName"))); //$NON-NLS-1$
		} else if (currentContext instanceof NestedContext) {
			ExampleFileFilter filterNested = new ExampleFileFilter(
					"lmn", GUIMessages.getString("GUI.LatticeMinerNestedFormat")); //$NON-NLS-1$ //$NON-NLS-2$
			fileChooser.addChoosableFileFilter(filterNested);
			fileChooser
			.setSelectedFile(new File(
					((NestedContext) currentContext)
					.getNestedContextName()
					+ GUIMessages
					.getString("GUI.LatticeMinerNestedFormatDefaultName"))); //$NON-NLS-1$
		}  else {
			ExampleFileFilter filterGaliciaBinSLF = new ExampleFileFilter(
					"slf", GUIMessages.getString("GUI.galiciaSLFBinaryFormat")); //$NON-NLS-1$ //$NON-NLS-2$
			fileChooser.addChoosableFileFilter(filterGaliciaBinSLF);
			ExampleFileFilter filterXLS = new ExampleFileFilter(new String[] {
					"xls" , "xlsx" }, GUIMessages.getString("GUI.excelXLSBinaryFormat")); //$NON-NLS-1$ //$NON-NLS-2$
			fileChooser.addChoosableFileFilter(filterXLS);
			ExampleFileFilter filterGaliciaBin = new ExampleFileFilter(
					"bin.xml", GUIMessages.getString("GUI.galiciaXMLBinaryFormat")); //$NON-NLS-1$ //$NON-NLS-2$
			fileChooser.addChoosableFileFilter(filterGaliciaBin);
			ExampleFileFilter filterCex = new ExampleFileFilter(
					"cex", GUIMessages.getString("GUI.conceptExplorerBinaryFormat")); //$NON-NLS-1$ //$NON-NLS-2$
			fileChooser.addChoosableFileFilter(filterCex);
			ExampleFileFilter filterBinary = new ExampleFileFilter(
					"lmb", GUIMessages.getString("GUI.LatticeMinerBinaryFormat")); //$NON-NLS-1$ //$NON-NLS-2$
			fileChooser.addChoosableFileFilter(filterBinary);
			fileChooser
			.setSelectedFile(new File(
					currentContext.getName()
					+ GUIMessages
					.getString("GUI.LatticeMinerBinaryFormatDefaultName"))); //$NON-NLS-1$
		}

		// La boite de dialogue
		int returnVal = fileChooser.showSaveDialog(panel);

		if (returnVal == JFileChooser.APPROVE_OPTION) {

			// Ferme le contexte precedent et ouvre le nouveau
			boolean hasBeenClosed = false;
			if (currentContext.getContextFile() != null) {
				hasBeenClosed = hasClosedCurrentContext(false);
			}

			File fileName = fileChooser.getSelectedFile();

			// Recupere les extensions du filtre
			ExampleFileFilter currentFilter = (ExampleFileFilter) fileChooser
			.getFileFilter();
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
					newFileName = newFileName.substring(0, posOldExt)
					+ "." + newFileType; //$NON-NLS-1$

				fileName = new File(newFileName);
			}

			if (fileName.exists()) {
				int overwrite = DialogBox.showDialogWarning(panel, GUIMessages
						.getString("GUI.doYouWantToOverwriteFile"), //$NON-NLS-1$
						GUIMessages.getString("GUI.selectedFileAlreadyExist")); //$NON-NLS-1$

				if (overwrite == DialogBox.NO) {
					DialogBox
					.showMessageInformation(
							panel,
							GUIMessages
							.getString("GUI.contextHasNotBeenSaved"), GUIMessages.getString("GUI.notSaved")); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
			}

			// Sauvegarde le context
			saveDependingOnFileType(fileName, currentContext);

			// Sauvergarde le path utilise
			LMPreferences.setLastDirectory(fileChooser.getCurrentDirectory()
					.getAbsolutePath());

			// Si l'ancien contexte a ete ferme on ouvre le nouveau
			if (hasBeenClosed) {
				openContextFile(fileName);
			}

			return true;
		} else {
			return false;
		}

	}

	/**
	 * Permet de sauvegarder un contexte dans un fichier selon le type du
	 * fichier
	 * 
	 * @param fileName
	 *            le fichier ou l'on souhaite sauvegarder
	 * @param currentContext
	 *            le contexte que l'on souhaite sauvegarder
	 */
	protected void saveDependingOnFileType(File fileName, Context currentContext) {

		try {
			// Recupere l'extension du fichier
			String fileType = ExampleFileFilter.getExtension(fileName);

			switch (fileType) {
				case "lmn":  //$NON-NLS-1$
					new LMNestedContextWriter(fileName,
							(NestedContext) currentContext);
					break;
				case "lmb":  //$NON-NLS-1$
					new LMBinaryContextWriter(fileName,
							(BinaryContext) currentContext);
					break;
				case "lmv":  //$NON-NLS-1$
					new LMValuedContextWriter(fileName,
							(ValuedContext) currentContext);
					break;
				case "cex":  //$NON-NLS-1$
					new CexBinaryContextWriter(fileName,
							(BinaryContext) currentContext);
					break;
				case "bin.xml":  //$NON-NLS-1$
					new GaliciaXMLBinaryContextWriter(fileName,
							(BinaryContext) currentContext);
					break;
				case "slf":  //$NON-NLS-1$
					new GaliciaSLFBinaryContextWriter(fileName,
							(BinaryContext) currentContext);
					break;
				default:
					DialogBox.showMessageError(panel, GUIMessages
									.getString("GUI.fileDoesntContainAKnownContextFormat"), //$NON-NLS-1$
							GUIMessages.getString("GUI.wrongContextFormat")); //$NON-NLS-1$

					return;
			}
			DialogBox
			.showMessageInformation(
					panel,
					GUIMessages
					.getString("GUI.contextHasBeenSuccessfullySaved"), GUIMessages.getString("GUI.saveSuccess")); //$NON-NLS-1$ //$NON-NLS-2$

		} catch (IOException ioe) {
			DialogBox
			.showMessageError(
					panel,
					GUIMessages.getString("GUI.contextCouldnotBeSaved"), GUIMessages.getString("GUI.errorWithFile")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (WriterException e) {
			DialogBox.showMessageError(panel, e);
		}
	}

	protected void saveAllContexts() {
		for (int i = 0; i < contextPanes.size(); i++) {
			selectContextAt(i);
			hasSaveCurrentContext();
		}
	}

	protected boolean hasClosedCurrentContext(Boolean askForConfirmation) {
		if (contextPanes.size() == 0)
			return true;
		// ATTENTION : Le TabPanel mets a jour le currentContextIdx en
		// selectionnant un nouveau,
		// donc il faut sauvegarder l'index du tab que l'on souhaite supprimer
		int contextIdxToClose = currentContextIdx;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(contextIdxToClose);
		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		if (currentContext instanceof BinaryContext){//-->LINDA
			binaryNb--;
		}else if(currentContext instanceof ValuedContext){//-->LINDA
			valuedNb--;
		}else {//-->LINDA
			nestedNb--;
		}

		if (askForConfirmation) {
			if (currentContext.getContextFile() == null) {
				int answer = DialogBox
				.showDialogWarningCancel(
						panel,
						GUIMessages
						.getString("GUI.doYouWantToSaveNewContext"), //$NON-NLS-1$
						GUIMessages.getString("GUI.context") + " : " + currentContext.getName()); //$NON-NLS-1$ //$NON-NLS-2$

				if (answer == DialogBox.YES) {
					if (!hasSaveCurrentContextAs()) {
						return false;
					}
				} else if (answer == DialogBox.CANCEL) {
					return false;
				}
			}

			else if (currentContext.isModified()) {
				int answer = DialogBox
				.showDialogWarningCancel(
						panel,
						GUIMessages
						.getString("GUI.doYouWantToSaveModifications"), //$NON-NLS-1$
						GUIMessages.getString("GUI.context") + " : " + currentContext.getName()); //$NON-NLS-1$ //$NON-NLS-2$

				if (answer == DialogBox.YES) {
					if (!hasSaveCurrentContext()) {
						return false;
					}
				} else if (answer == DialogBox.CANCEL) {
					return false;
				}
			}
		}

		// Supprime l'onglet
		panelTab.removeTabAt(panelTab.indexOfComponent(selectedPane));

		// Mets a jour par rapport au fichier du contexte
		if (currentContext.getContextFile() != null) {
			String fileAbsolutePath = currentContext.getContextFile()
			.getAbsolutePath();

			// Enleve le fichier des fichiers ouverts
			contextFiles.remove(fileAbsolutePath);

		}

		// Mets a jour la numerotation et les onglets
		if (contextPanes.size() == 1) {
			contextPanes = new Vector<>();
			currentContextIdx = -1;

			panel.remove(panelTab);

			contextGroup = new ButtonGroup();
			windowMenu.get("Title").removeAll();
			windowMenu.get("Title").add(noContextItem);

			currentContextName.setText(GUIMessages
					.getString("GUI.noContextLoaded")); //$NON-NLS-1$
			setActiveActions();
			frame.repaint();
		} else {
			contextPanes.removeElementAt(contextIdxToClose);

			JRadioButtonMenuItem currentCtxBtn = (JRadioButtonMenuItem) ((JMenu) windowMenu.get("Title"))
			.getMenuComponent(contextIdxToClose);
			windowMenu.get("Title").remove(currentCtxBtn);
			contextGroup.remove(currentCtxBtn);

			// Selectionne le contexte precedent
			int newIdx = contextIdxToClose - 1 > 0 ? contextIdxToClose - 1 : 0;
			selectContextAt(newIdx);

			// Replace les Window pour avoir les raccourcis claviers valides
			for (int i = 0; i < ((JMenu) windowMenu.get("Title")).getMenuComponentCount(); i++) {
				JRadioButtonMenuItem courant = (JRadioButtonMenuItem) ((JMenu) windowMenu.get("Title"))
				.getMenuComponent(i);
				//courant.setAccelerator(KeyStroke.getKeyStroke("ctrl NUMPAD" + (i + 1))); //$NON-NLS-1$
				courant.setAccelerator(KeyStroke.getKeyStroke((i+1) + KeyEvent.VK_0,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			}
		}

		// Met a jour le menu 
		setActiveActions();
		frame.repaint();
		return true;
	}

	/**
	 * Rajoute un fichier recemment ouvert a le liste dans les preferences
	 * 
	 * @param fileAbsolutePath
	 *            le path absolu du fichier
	 */
	protected void addRecentFilePreferences(String fileAbsolutePath) {
		// Recupere les preferences de Lattice Miner
		Preferences preferences = LMPreferences.getPreferences();

		// Avant de continuer on doit verfier si le fichier n'est pas deja dans la liste:
		JMenu jMenu = (JMenu) file.get("OpenRecentContext");
		for (int i = 0; i < jMenu.getItemCount(); i++) {
			JMenuItem item = jMenu.getItem(i);
			if (item.getText().equalsIgnoreCase(fileAbsolutePath)) {
				return;
			}
		}

		// Verifie que le fichier en cours de fermeture n'est pas deja le
		// premier de le liste
		//JMenuItem firstItem = openRecentContext.getItem(0);
		JMenuItem firstItem = jMenu.getItem(0);
		if (firstItem != null && firstItem.getText().equals(fileAbsolutePath)) {
			return;
		}

		// Nombre de fichiers recents et rajoute un pour celui-ci
		int nbRecents = preferences.getInt(LMPreferences.NB_RECENTS, 0);
		nbRecents = (nbRecents + 1) % 10;

		String currentRecent = LMPreferences.RECENTS + "/" + nbRecents; //$NON-NLS-1$
		preferences.put(currentRecent, fileAbsolutePath);

		// Rajoute le MenuItem
		JMenuItem recentMenuItem = new JMenuItem(fileAbsolutePath);
		recentMenuItem.addActionListener(new RecentMenuListener(
				fileAbsolutePath));

		file.get("OpenRecentContext").add(recentMenuItem, 0);
		//openRecentContext.add(recentMenuItem, 0);

		// Si encore aucun Recent File on active le menu
		//		if (!openRecentContext.isEnabled())
		//			openRecentContext.setEnabled(true);
		if(!file.get("OpenRecentContext").isEnabled()) {
			file.get("OpenRecentContext").setEnabled(true);
		}

		// Si plus de cinq element dans le menu, on supprime le dernier
		//		if (openRecentContext.getMenuComponentCount() > 5)
		//			openRecentContext.remove(5);
		if(((JMenu) file.get("OpenRecentContext")).getMenuComponentCount() > 5) {
			file.get("OpenRecentContext").remove(5);
		}

		preferences.putInt(LMPreferences.NB_RECENTS, nbRecents);
	}

	protected boolean hasClosedAllContexts() {
		boolean allClosed = true;
		while (allClosed && (contextPanes.size() != 0)) {
			allClosed = hasClosedCurrentContext(true);
		}
		return allClosed;
	}

	protected void addContextLevelToCurrentContext() {
		if (currentContextIdx < 0)
			return;

		Vector<BinaryContext> contexts = new Vector<>();
		for (int i = 0; i < contextPanes.size(); i++) {
			ContextTableScrollPane scrollPane = contextPanes.elementAt(i);
			Context ctx = ((ContextTableModel) scrollPane.getContextTable()
					.getModel()).getContext();
			if ((ctx instanceof BinaryContext)
					&& !(ctx instanceof NestedContext))
				contexts.add((BinaryContext) ctx);
		}

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		NestedContextTable currentTable = (NestedContextTable) selectedPane
		.getContextTable();
		int level = currentTable.getModel()
		.getColumnCount();

		new LevelAdditionAssistant(level + 1, this, contexts);
	}

	protected void addEmptyLevelToCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		NestedContextTable currentTable = (NestedContextTable) selectedPane
		.getContextTable();
		currentTable.addLevel();
		currentTable.validate();
	}

	protected void removeLastLevelFromCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		NestedContextTable currentTable = (NestedContextTable) selectedPane
		.getContextTable();
		currentTable.removeLevel();
		currentTable.validate();
	}

	protected void orderCurrentContextLevels() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		NestedContextTable currentTable = (NestedContextTable) selectedPane
		.getContextTable();
		Context ctx = ((ContextTableModel) currentTable.getModel())
		.getContext();

		if (!(ctx instanceof NestedContext))
			return;
		NestedContext nesCtx = (NestedContext) ctx;

		String[] possibleValues = {
				GUIMessages.getString("GUI.noClassificationLevel"), GUIMessages.getString("GUI.classificationlevelAtFirstLevel"), //$NON-NLS-1$ //$NON-NLS-2$
				GUIMessages.getString("GUI.classificationlevelAtLastlevel") }; //$NON-NLS-1$
		String selectedValue = (String) DialogBox
		.showInputQuestion(
				panel,
				GUIMessages
				.getString("GUI.whatKindOfOrdeningWouldYouLike"), //$NON-NLS-1$
				GUIMessages.getString("GUI.kindOfOrdening"), possibleValues, possibleValues[0]); //$NON-NLS-1$

		RankingAlgo algo = null;
		boolean classificationAtLast = false;
		if (selectedValue.equals(GUIMessages
				.getString("GUI.noClassificationLevel"))) { //$NON-NLS-1$
			algo = new UnsupervisedMultivaluedRankingAlgo(nesCtx);
		}

		else if (selectedValue.equals(GUIMessages
				.getString("GUI.classificationlevelAtFirstLevel"))) { //$NON-NLS-1$
			Vector<BinaryContext> contextList = nesCtx
			.convertToBinaryContextList();

			String[] levelValues = new String[contextList.size()];
			for (int i = 0; i < contextList.size(); i++)
				levelValues[i] = (contextList.elementAt(i)).getName();

			String selectedLevel = (String) DialogBox
			.showInputQuestion(
					panel,
					GUIMessages
					.getString("GUI.whichLevelShouldBeTheClassificationLevel"), GUIMessages.getString("GUI.classificationLevel"), levelValues, //$NON-NLS-1$ //$NON-NLS-2$
					levelValues[0]);

			int levelIdx = -1;
			for (int i = 0; i < contextList.size(); i++) {
				String ctxName = (contextList.elementAt(i)).getName();
				if (selectedLevel.equals(ctxName)) {
					levelIdx = i;
					break;
				}
			}

			algo = new SupervisedMultivaluedRankingAlgo(nesCtx, levelIdx);
		}

		else if (selectedValue.equals(GUIMessages
				.getString("GUI.classificationlevelAtLastlevel"))) { //$NON-NLS-1$
			Vector<BinaryContext> contextList = nesCtx
			.convertToBinaryContextList();

			String[] levelValues = new String[contextList.size()];
			for (int i = 0; i < contextList.size(); i++)
				levelValues[i] = (contextList.elementAt(i)).getName();

			String selectedLevel = (String) DialogBox
			.showInputQuestion(
					panel,
					GUIMessages
					.getString("GUI.whichLevelShouldBeTheClassificationLevel"), GUIMessages.getString("GUI.classificationLevel"), levelValues, //$NON-NLS-1$ //$NON-NLS-2$
					levelValues[0]);

			int levelIdx = -1;
			for (int i = 0; i < contextList.size(); i++) {
				String ctxName = (contextList.elementAt(i)).getName();
				if (selectedLevel.equals(ctxName)) {
					levelIdx = i;
					break;
				}
			}

			algo = new SupervisedMultivaluedRankingAlgo(nesCtx, levelIdx);
			classificationAtLast = true;
		}

		if (algo != null) {
			String[] ordering = algo.getOrdering();
			NestedContext currentContext = nesCtx;

			/* Construit le vecteur donnant l'ordre actuel des contextes */
			Vector<String> currentOrder = new Vector<>();
			// int idx = 0;
			do {
				currentOrder.add(currentContext.getFirstLevelBinaryContext()
						.getName());
				currentContext = currentContext.getNextContext();
			} while (currentContext != null);

			/* Reordonne les contextes */
			for (int i = 0; i < ordering.length; i++) {
				String currentName = ordering[i];

				/*
				 * Recherche de la position actuelle du prochain contexte a
				 * placer
				 */
				int currentIdx = -1;
				for (int j = 0; j < currentOrder.size(); j++) {
					String ctxName = currentOrder.elementAt(j);
					if (currentName.equals(ctxName)) {
						currentIdx = j;
						break;
					}
				}

				/* Place le contexte a sa position finale */
				currentOrder.removeElementAt(currentIdx);
				currentOrder.insertElementAt(currentName, i);
				nesCtx.moveLevel(currentIdx, i);
			}

			if (classificationAtLast)
				nesCtx.moveLevel(0, currentOrder.size() - 1);

			currentTable.setModelFromContext(ctx);
			currentTable.validate();
		}
	}

	protected void addObjectToCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();
		currentContext.addObject();
		currentTable.setModelFromContext(currentContext);
		selectedPane.setRowHeaderView(((ContextTableModel) currentTable
				.getModel()).getRowHeader());
	}

	protected void addAttributeToCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();
		currentContext.addAttribute();
		currentTable.setModelFromContext(currentContext);
	}

	protected void removeObjectFromCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		RemoveObjectsPanel remPanel = new RemoveObjectsPanel(selectedPane, this);
		remPanel.open();
	}

	protected void removeAttributeFromCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		RemoveAttributesPanel remPanel = new RemoveAttributesPanel(
				selectedPane, this);
		remPanel.open();
	}

	protected void mergeAttributesInCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		MergeAttributesPanel mergePanel = new MergeAttributesPanel(
				selectedPane, this);
		mergePanel.open();
	}

	protected void createLogicalAttributeInCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		LogicalAttributePanel logicalPanel = new LogicalAttributePanel(
				selectedPane, this);
		logicalPanel.open();
	}

	protected void createObjectClustersInCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		BinaryContext currentContext = (BinaryContext) ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		currentContext.sortObjectsInClusters();
		currentTable.setModelFromContext(currentContext);
	}

	protected void convertCurrentContextToBinaryContext()
	throws AlreadyExistsException, InvalidTypeException {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		// ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		if (currentContext instanceof NestedContext) {
			String[] possibleValues = {
					GUIMessages.getString("GUI.oneBinaryContextForAllLevels"), GUIMessages.getString("GUI.ABinaryContextForEachLevel") }; //$NON-NLS-1$ //$NON-NLS-2$
			String selectedValue = (String) DialogBox
			.showInputQuestion(
					panel,
					GUIMessages
					.getString("GUI.howManyContextsShouldBeCreated"), //$NON-NLS-1$
					GUIMessages.getString("GUI.numberOfConcepts"), possibleValues, possibleValues[0]); //$NON-NLS-1$

			/* Creation d'un seul contexte binaire */
			if (selectedValue.equals(GUIMessages
					.getString("GUI.oneBinaryContextForAllLevels"))) { //$NON-NLS-1$
				BinaryContext binCtx = ((NestedContext) currentContext)
				.convertToBinaryContext();
				binCtx.setName(((NestedContext) currentContext)
						.getNestedContextName()
						+ GUIMessages.getString("GUI._conv")); //$NON-NLS-1$
				addBinaryContext(binCtx);
			}

			/*
			 * Creation d'un contexte binaire pour chacun des niveaux de la
			 * relation imbriquee
			 */
			else {
				Vector<BinaryContext> contextList = ((NestedContext) currentContext)
				.convertToBinaryContextList();
				for (int i = 0; i < contextList.size(); i++) {
					BinaryContext binCtx = contextList.elementAt(i);
					binCtx.setName(((NestedContext) currentContext)
							.getNestedContextName()
							+ GUIMessages.getString("GUI._level") + (i + 1)); //$NON-NLS-1$
					addBinaryContext(binCtx);
				}
			}
		}

		else if (currentContext instanceof BinaryContext) {
			DialogBox
			.showMessageInformation(
					panel,
					GUIMessages
					.getString("GUI.contextIsAlreadyABinaryContext"), GUIMessages.getString("GUI.noConversionNeeded")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		else if (currentContext instanceof ValuedContext) {
			new ValuedContextConversionAssistant(
					(ValuedContext) currentContext, this);
		}
	}

	protected void convertCurrentContextToNestedContext()
	throws AlreadyExistsException, InvalidTypeException {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		// ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		if (currentContext instanceof BinaryContext) {
			/*
			 * Creation d'un seul contexte à niveaux avec nombre de niveaux non
			 * specifie
			 */
			NestedContext nestedCtx = ((BinaryContext) currentContext)
			.convertToNestedContext();
			nestedCtx.setNestedContextName(currentContext
					.getName()
					+ GUIMessages.getString("GUI._nested")); //$NON-NLS-1$
			addNestedContext(nestedCtx);
		}

		else {
			DialogBox
			.showMessageWarning(
					panel,
					GUIMessages
					.getString("GUI.OnlyABinaryContextCanBeConvertedToANestedContext"), //$NON-NLS-1$
					GUIMessages.getString("GUI.NoConversionAvailable")); //$NON-NLS-1$
		}
	}

	protected void compareAttributesInCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		// ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		if (currentContext instanceof BinaryContext) {
			String[] possibleValues = new String[currentContext
			                                     .getAttributeCount()];
			for (int i = 0; i < currentContext.getAttributeCount(); i++)
				possibleValues[i] = currentContext.getAttributeAt(i);

			String firstAttribute = (String) DialogBox
			.showInputQuestion(
					panel,
					GUIMessages.getString("GUI.selectFirstAttribute"), //$NON-NLS-1$
					GUIMessages.getString("GUI.firstAttribute"), possibleValues, possibleValues[0]); //$NON-NLS-1$
			if (firstAttribute == null)
				return;

			String secondAttribute = (String) DialogBox
			.showInputQuestion(
					panel,
					GUIMessages.getString("GUI.selectSecondAttribute"), //$NON-NLS-1$
					GUIMessages.getString("GUI.secondAttribute"), possibleValues, possibleValues[0]); //$NON-NLS-1$
			if (secondAttribute == null)
				return;

			BasicSet differentObjects = currentContext.compareAttributes(
					firstAttribute, secondAttribute);
			if (differentObjects == null) {
				DialogBox.showMessageInformation(panel, GUIMessages
						.getString("GUI.atLeastOneChosenAttributeDoesntExist"), //$NON-NLS-1$
						GUIMessages.getString("GUI.resultOfComparaison")); //$NON-NLS-1$
			} else {
				DialogBox.showMessageInformation(panel, differentObjects
						.toString(), GUIMessages
						.getString("GUI.resultOfComparaison")); //$NON-NLS-1$
			}
		}

		else {
			DialogBox
			.showMessageWarning(
					panel,
					GUIMessages
					.getString("GUI.attributeComparaisonIsAvailableOnlyForBinaryAttributes"), //$NON-NLS-1$
					GUIMessages.getString("GUI.noComparaisonAvailable")); //$NON-NLS-1$
		}
	}

	protected void showCurrentLattice() throws AlreadyExistsException,
	InvalidTypeException {
		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);

		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		if (currentContext instanceof NestedContext) {
			NestedContext context = (NestedContext) currentContext;

			Vector<ConceptLattice> lattices = new Vector<>();
			Vector<LatticeStructure> structures = new Vector<>();
			int levelIdx = 0;
			while (context != null) {
				levelIdx++;

				ConceptLattice lattice = new ConceptLattice(context);

				lattices.add(lattice);

				LatticeStructure structure = new LatticeStructure(lattice,
						context, LatticeStructure.BEST);
				structures.add(structure);

				context = context.getNextContext();
			}

			NestedLattice nestedLattice = new NestedLattice(null, lattices,
					null, ((NestedContext) currentContext)
					.getNestedContextName());
			GraphicalLattice graphLattice = new GraphicalLattice(nestedLattice,
					null, structures);
			LatticeViewer latticeViewer = new LatticeViewer(graphLattice);

			latticeViewer.addWindowListener(this);
			latticeViewer.pack();
			latticeViewer.setVisible(true);

		}

		else if (currentContext instanceof BinaryContext) {
			ConceptLattice lattice = new ConceptLattice(
					(BinaryContext) currentContext);

			LatticeStructure struct = new LatticeStructure(lattice,
					(BinaryContext) currentContext, LatticeStructure.BEST);
			GraphicalLattice graphLattice = new GraphicalLattice(lattice,
					struct);

			LatticeViewer latticeViewer = new LatticeViewer(graphLattice);
			latticeViewer.addWindowListener(this);
			latticeViewer.pack();
			latticeViewer.setVisible(true);
		}
	}

	/**
	 * Calcule et affiche les éléments qui peuvent engendrer l'attibut spécifié
	 * @throws InvalidTypeException
	 * @throws AlreadyExistsException
	 * FIXME non fonctionnel
	 */
	protected void showAntiClosure() throws AlreadyExistsException, InvalidTypeException {
		BinaryContext binCtx = null;
		Context currentContext = ((ContextTableModel) contextPanes.elementAt(currentContextIdx)
				.getContextTable().getModel()).getContext();
		if (currentContext instanceof NestedContext)
			binCtx = ((NestedContext) currentContext).convertToBinaryContext();
		else
			binCtx = (BinaryContext) currentContext;


		ConceptLattice lattice = binCtx.getConceptLattice();
		if (lattice == null)
			lattice = new ConceptLattice(binCtx);

		// TODO Passer les chaines dans le fichier properties
		String element = DialogBox.showInputQuestion(this, GUIMessages.getString("GUI.attribute"),"? -> x");

			StringTokenizer tokenizer = new StringTokenizer(element, ",");
			BasicSet entree = new BasicSet();

			while ( tokenizer.hasMoreTokens() ) {
				entree.add(tokenizer.nextToken());
			}

		RuleAlgorithm algo = new InformativeBasisAlgorithm(lattice,
				0 / 100.0, 100 / 100.0);

		Vector<Rule> rules = new Vector<>();
		rules.add(new Rule(algo.antiClosure(entree), entree));
		new RuleViewer(rules , binCtx.getName(), 100 / 100.0,
				0 / 100.0, null);
	}

	/**
	 * Calcule et affiche la "closure" d'un attribut
	 * @throws InvalidTypeException
	 * @throws AlreadyExistsException
	 */
	protected void showClosure() throws AlreadyExistsException, InvalidTypeException {
		BinaryContext binCtx = null;
		// TODO Passer les chaines dans le fichier properties
		Context currentContext = ((ContextTableModel) contextPanes.elementAt(currentContextIdx)
				.getContextTable().getModel()).getContext();
		if (currentContext instanceof NestedContext)
			binCtx = ((NestedContext) currentContext).convertToBinaryContext();
		else
			binCtx = (BinaryContext) currentContext;


		ConceptLattice lattice = binCtx.getConceptLattice();
		if (lattice == null)
			lattice = new ConceptLattice(binCtx);
		
		String element = DialogBox.showInputQuestion(this, GUIMessages.getString("GUI.attribute"),
			GUIMessages.getString("GUI.closure"));

		StringTokenizer tokenizer = new StringTokenizer(element, ",");
		BasicSet entree = new BasicSet();

		while ( tokenizer.hasMoreTokens() ) {
			entree.add(tokenizer.nextToken());
		}
		RuleAlgorithm algo = new InformativeBasisAlgorithm(lattice,
				0 / 100.0, 100 / 100.0);

		Vector<Rule> rules = new Vector<>();
		rules.add(new Rule(entree, algo.closure(entree)));
		new RuleViewer(rules , binCtx.getName(), 100 / 100.0,
				0 / 100.0, null); // FIXME Pb
	}
	/**
	 * Affiche les regles associes au contexte
	 * @throws AlreadyExistsException
	 * @throws InvalidTypeException
	 */
	protected void showCurrentRules() throws AlreadyExistsException,
	InvalidTypeException {
		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		// ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		if (!(currentContext instanceof BinaryContext)) {
			DialogBox
			.showMessageWarning(
					panel,
					GUIMessages
					.getString("GUI.ruleViewerIsAvailableOnlyForBinaryAndNested"), //$NON-NLS-1$
					GUIMessages
					.getString("GUI.ruleViewerIsNotAvailable")); //$NON-NLS-1$
			return;
		}

		BinaryContext binCtx = null;
		// String contextName = null;
		if (currentContext instanceof NestedContext)
			binCtx = ((NestedContext) currentContext).convertToBinaryContext();
		else
			binCtx = (BinaryContext) currentContext;

		ConceptLattice lattice = binCtx.getConceptLattice();
		if (lattice == null)
			lattice = new ConceptLattice(binCtx);

		double minSupp = askMinSupport();
		double minConf = askMinConfidence();


		RuleAlgorithm algo = new InformativeBasisAlgorithm(lattice,
				minSupp / 100.0, minConf / 100.0);
		Vector<Rule> rules = algo.getRules();

		new RuleViewer(rules, binCtx.getName(), minSupp / 100.0,
				minConf / 100.0, null);
	}

	private double askMinSupport() {
		double minSupp;
		String suppStr;
		do {
			suppStr = (String) DialogBox.showInputQuestion(this, GUIMessages.getString("GUI.enterMinimumSupport")+" (%)", //$NON-NLS-1$ //$NON-NLS-2$
					GUIMessages.getString("GUI.minimumSupportForRules"), null, "50"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				minSupp = Double.parseDouble(suppStr);
			} catch (NumberFormatException ex) {
				DialogBox.showMessageWarning(panel, GUIMessages.getString("GUI.valueMustBeBetween0And100"), //$NON-NLS-1$
						GUIMessages.getString("GUI.wrongSupportValue")); //$NON-NLS-1$
				minSupp = -1;
			}
		} while(minSupp < 0 || minSupp > 100);
		return minSupp;
	}

	private double askMinConfidence() {
		double minConf;
		String confStr;
		do {
			confStr = (String) DialogBox.showInputQuestion(this, GUIMessages.getString("GUI.enterMinimumConfidence")+" (%)", //$NON-NLS-1$ //$NON-NLS-2$
					GUIMessages.getString("GUI.minimumConfidenceForRules"), null, "50"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				minConf = Double.parseDouble(confStr);
			} catch (NumberFormatException ex) {
				DialogBox.showMessageWarning(panel, GUIMessages.getString("GUI.valueMustBeBetween0And100"), //$NON-NLS-1$
						GUIMessages.getString("GUI.wrongConfidenceValue")); //$NON-NLS-1$
				minConf = -1;

			}
		} while(minConf < 0 || minConf > 100);
		return minConf;
	}


	protected void showCurrentRulesNegatives() throws AlreadyExistsException,
	InvalidTypeException {
		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		// ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		if (!(currentContext instanceof BinaryContext)) {
			DialogBox
			.showMessageWarning(
					panel,
					GUIMessages
					.getString("GUI.ruleViewerIsAvailableOnlyForBinaryAndNested"), //$NON-NLS-1$
					GUIMessages
					.getString("GUI.ruleViewerIsNotAvailable")); //$NON-NLS-1$
			return;
		}

		BinaryContext binCtx = null;
		//BinaryContext binCtx2 = (BinaryContext) currentContext.clone();

		// String contextName = null;
		if (currentContext instanceof NestedContext)
			binCtx = ((NestedContext) currentContext).convertToBinaryContext();
		else
			//binCtx = (BinaryContext) currentContext.complementaryContext((BinaryContext) currentContext);
			binCtx = (BinaryContext) currentContext;
		BinaryContext binCtx2 = (BinaryContext) binCtx.clone();
		BinaryContext complem = binCtx2.complementaryContext();

		ConceptLattice lattice = complem.getConceptLattice();
		if (lattice == null)
			lattice = new ConceptLattice(complem);

		double minSupp = askMinSupport();
		double minConf = askMinConfidence();


		RuleAlgorithm algo = new InformativeBasisAlgorithm(lattice,
				minSupp / 100.0, minConf / 100.0);
		Vector<Rule> rules = algo.getRules();

		new RuleViewer(rules, complem.getName(), minSupp / 100.0,
				minConf / 100.0, null);
	}

	protected void showCurrentRulesMixtes() throws AlreadyExistsException,
	InvalidTypeException {
		//System.out.println("show Rules *************");
		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		// ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane
				.getContextTable().getModel()).getContext();

		if (!(currentContext instanceof BinaryContext)) {
			DialogBox
			.showMessageWarning(
					panel,
					GUIMessages
					.getString("GUI.ruleViewerIsAvailableOnlyForBinaryAndNested"), //$NON-NLS-1$
					GUIMessages
					.getString("GUI.ruleViewerIsNotAvailable")); //$NON-NLS-1$
			return;
		}

		BinaryContext binCtx = null;
		// String contextName = null;
		if (currentContext instanceof NestedContext)
			binCtx = ((NestedContext) currentContext).convertToBinaryContext();
		else
			//binCtx = (BinaryContext) currentContext.addComplementaryContext((BinaryContext) currentContext);
			binCtx = (BinaryContext) currentContext;
		BinaryContext binCtx2 = (BinaryContext) binCtx.clone();
		BinaryContext global = binCtx2.addComplementaryContext();

		ConceptLattice lattice = global.getConceptLattice();
		if (lattice == null)
			lattice = new ConceptLattice(global);

		double minSupp = askMinSupport();
		double minConf = askMinConfidence();

		RuleAlgorithm algo = new InformativeBasisAlgorithm(lattice,
				minSupp / 100.0, minConf / 100.0);
		Vector<Rule> rules = algo.getRules();

		new RuleViewer(rules, global.getName(), minSupp / 100.0,
				minConf / 100.0, null);
		global = (BinaryContext) currentContext;
	}


	/**
	 *Methode qui permet de visualiser les regles reduites
	 * @author Maya Safwat
	 */
	protected void showCurrentReducedRules() throws AlreadyExistsException, InvalidTypeException {
		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		//ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();

		if (!(currentContext instanceof BinaryContext)) {
			DialogBox.showMessageWarning(panel, GUIMessages.getString("GUI.ruleViewerIsAvailableOnlyForBinaryAndNested"), //$NON-NLS-1$
					GUIMessages.getString("GUI.ruleViewerIsNotAvailable")); //$NON-NLS-1$
			return;
		}

		BinaryContext binCtx = null;
		//String contextName = null;
		if (currentContext instanceof NestedContext)
			binCtx = ((NestedContext) currentContext).convertToBinaryContext();
		else
			binCtx = (BinaryContext) currentContext;

		ConceptLattice lattice = binCtx.getConceptLattice();
		if (lattice == null)
			lattice = new ConceptLattice(binCtx);


		double minSupp = askMinSupport();
		double minConf = 100;


		RuleAlgorithm algo =  new GenericBasisAlgorithm(lattice, minSupp / 100.0);
		Vector<Rule> r = algo.Cover2();

		// Le support est incorect, recalcul
		Vector<Rule> fixedRules = new Vector<>();
		for (Rule rule : r) {
			BasicSet basicSet = new BasicSet();
			basicSet.addAll(rule.getAntecedent());
			basicSet.addAll(rule.getConsequence());
			double newSupport = binCtx.support(basicSet);

			rule.setSupport(newSupport);
			fixedRules.add(rule);
		}

		new RuleViewer(fixedRules, binCtx.getName(), minSupp / 100.0, minConf / 100.0, null);
	}

	protected Vector<Rule> mixedRulesFromKey(Vector<BasicSet> keys) {
		Vector<Rule> mixedRules = new Vector<>();
		for (BasicSet key : keys) {
			Rule rule = new Rule(key, key);
			mixedRules.add(rule);
		}
		return mixedRules;
	}


	/**
	 * Affichage des implications avec negation dans la console (pour le moment)
	 * Kevin Emamirad
	 *
	 */
	protected void showNegationImplications() {
		// Recuper le tab (pane) courant
		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);

		// Recuperer le contexte K courant
		BinaryContext currentContext = (BinaryContext)((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();

		// Calculer le complementaire du contexte. K~ (complementaryContext)
		BinaryContext binaryContext = (BinaryContext)currentContext.clone();
		binaryContext.addComplementaryContext();

		// Generate Lattice for this context
		ConceptLattice conceptLattice = new ConceptLattice(binaryContext);
		Vector<BasicSet> gen = getGenerateursInfimum(conceptLattice);
		Vector<BasicSet> elementsCandidates = getCandidates(conceptLattice, gen);

		Vector<Rule> rules = new Vector<>();
		for (BasicSet elementsCandidate : elementsCandidates) {
			rules.addAll(computeImplicationsWithNegation(binaryContext, elementsCandidate));
		}

		new RuleViewer(rules, binaryContext.getName(), 0.0, 1.0, null);
	}

	/**
	 * En partant des règles candidats filtrés, nous cherchons à générer les
	 * implications avec négation qui en découlent.
	 * @param binaryContext
	 * @param elementsCandidate
	 */
	protected Vector<Rule> computeImplicationsWithNegation(BinaryContext binaryContext, BasicSet elementsCandidate) {
		Vector<Rule> newRules = new Vector<>();
		for (String el : elementsCandidate) {
			BasicSet ant = elementsCandidate.clone();
			ant.remove(el);
			BasicSet cons = new BasicSet();
			cons.add(BasicSet.negation(el));
			Rule newRule = new Rule(ant, cons);

			// On doit calculer le support de cette nouvelle regle.
			BasicSet allElements = new BasicSet();
			allElements.addAll(ant);
			allElements.addAll(cons);
			// On peut aussi utiliser la formule du calcul du support sans avoir à refaire l'apposition.
			double support = binaryContext.support(allElements);
			newRule.setSupport(support);
			newRules.add(newRule);
		}
		return newRules;

	}

	public double supportMixte(BasicSet bs, BinaryContext positiveContext) {

		BasicSet bsPos = new BasicSet();
		BasicSet bsNeg = new BasicSet();
		for (String s : bs) {
			if (!BasicSet.isNegative(s)) {
				bsPos.add(s);
			}
			if (BasicSet.isNegative(s)) {
				bsNeg.add(s);
			}
		}

		int n = bsNeg.size();

		double support = 0.0;

		for (int i = 0; i <= n; i++) {
			double pow = Math.pow(-1, i);

			Vector<BasicSet> allPositivesCombinations = getAllPosCombinations(bsPos, bsNeg, i);
			for(BasicSet posCombination : allPositivesCombinations) {
				support = support + (pow * supportPos(posCombination, positiveContext));
			}
		}

		return support;
	}

	protected Vector<BasicSet> getAllPosCombinations(BasicSet initialBsPos, BasicSet bsNeg, int numberOfNegElToAdd) {
		Vector<BasicSet> allPosCombinations = new Vector<>();

		// Nous creeons un nouveau BasicSet de tous les elements negatifs transformes en positifs.
		BasicSet posElFromBsNeg = bsNeg.P();

		Vector<BasicSet> permutations = getAllPermutations(posElFromBsNeg, new Vector<>(), numberOfNegElToAdd);

		for(BasicSet permutation : permutations) {
			BasicSet perm = permutation.clone();
			perm.addAll(initialBsPos);
			allPosCombinations.add(perm);
		}

		return allPosCombinations;
	}

	protected double supportPos(BasicSet bs, BinaryContext ctx) {
		if (bs.isEmpty()) {
			return 1.0;
		}
		double support = 0.0;

		// get the total of objects
		double objectsSize = (double)(ctx.getObjects()).size();


		return support;
	}

	/**
	 * Just use a simple permutation algorithm
	 * See http://www.geeksforgeeks.org/print-all-possible-combinations-of-r-elements-in-a-given-array-of-size-n/
	 * @param bs
	 * @param permutations
	 * @param r
	 * @return
	 */
	protected Vector<BasicSet> getAllPermutations(BasicSet bs, Vector<BasicSet> permutations, int r) {
		Vector<BasicSet> all = new Vector<>();
		if (r == 0) {
			return all;
		} else {
			Iterator<String> it = bs.iterator();
			BasicSet bsCandidates = new BasicSet();
			while (it.hasNext()) {
				String el = it.next();
				bsCandidates.add(el);
			}
			permutations.add(bsCandidates);
			// TODO: a finir;
		}
		return all;

	}

	/**
	 * Méthode permettant de récupérer la liste des attributs candidats à la génération des implications avec négation.
	 * Parmi les règles associées à notre treillis, nous filtrons les règles qui ont un support de 0 % et qui ne sont pas
	 * des contradictions où les antécédents correspondent à des générateurs de l'infimum du treillis.
	 *
	 * Kevin Emamirad
	 *
	 * @param lattice Treillis
	 * @param generateurs Générateurs de l'infimum du treillis.
	 * @return Ensemble d'éléments candidats à la génération des implications avec négation.
	 */
	protected Vector<BasicSet> getCandidates(ConceptLattice lattice, Vector<BasicSet> generateurs) {
		// On genère les règles avec l'algorithme Informative Basis pour le lattice, avec un support de 0% et une confience
		// de 100 %.
		RuleAlgorithm ruleAlgorithm = new InformativeBasisAlgorithm(lattice,
				0.0 , 1.0);

		// Comme on veut uniquement les éléments candidats (la partie gauche de la règle), nous les enregistrons dans une
		// collection de BasicSet.
		Vector<BasicSet> elementsCandidats = new Vector<>();

		for (Rule rule : ruleAlgorithm.getRules()) {
			if (rule.getSupport() == 0.0 && !BasicSet.isContradictionBasicSet(rule.getAntecedent()) &&
					generateurs.contains(rule.getAntecedent())) {
				elementsCandidats.add(rule.getAntecedent());
			}
		}

		return elementsCandidats;
	}

	/**
	 * Méthode permettant de récupérer les générateurs de l'infimum.
	 * @param conceptLattice
	 * @return
	 */
	protected Vector<BasicSet> getGenerateursInfimum(ConceptLattice conceptLattice) {
		// On recupère tout les concepts.
		Vector<FormalConcept> concepts = conceptLattice.getConcepts();

		// On recherche les generateurs de l'infimum
		Vector<BasicSet> gen = new Vector<>();

		for (FormalConcept concept : concepts) {
			if (concept.getExtent().size() == 0) {
				gen = new JenAlgorithm(conceptLattice).calcNodeGenerators(concept);
			}
		}
		return gen;
	}

	/**
	 * Affichage de la Stem Base (Base Guigues-*Duquenne)
	 */
	protected void showCurrentRulesStemBase() throws AlreadyExistsException, InvalidTypeException {
		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		//ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();

		if (!(currentContext instanceof BinaryContext)) {
			DialogBox.showMessageWarning(panel, GUIMessages.getString("GUI.ruleViewerIsAvailableOnlyForBinaryAndNested"), //$NON-NLS-1$
					GUIMessages.getString("GUI.ruleViewerIsNotAvailable")); //$NON-NLS-1$
			return;
		}

		BinaryContext binCtx = null;
		//String contextName = null;
		if (currentContext instanceof NestedContext)
			binCtx = ((NestedContext) currentContext).convertToBinaryContext();
		else
			binCtx = (BinaryContext) currentContext;

		ConceptLattice lattice = binCtx.getConceptLattice();
		if (lattice == null)
			lattice = new ConceptLattice(binCtx);

		double minSupp = askMinSupport();
		double minConf = 100;


		RuleAlgorithm algo =  new GenericBasisAlgorithm(lattice, minSupp / 100.0);
		Vector<Rule> rules = new Vector<>();

		Implication a=new Implication(binCtx,rules);

		Vector <Rule> r=a.StemBase();
		//Vector<Rule> l=algo.leftRed(r);
		algo.setRules(r);
		Vector<Rule> l=algo.leftRed(/*r*/).getRules();
		new RuleViewer(l, binCtx.getName(), minSupp / 100.0, minConf / 100.0, null);

	}

	/*** Contexte a objets clarifies ***/
	protected void clarifyObjectOfCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();
		((BinaryContext) currentContext).clarifyObject();
		currentTable.setModelFromContext(currentContext);

	}

	/*** Contexte a attributs clarifies ***/
	protected void clarifyAttributeOfCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();
		((BinaryContext) currentContext).clarifyAttribute();
		currentTable.setModelFromContext(currentContext);

	}

	/*** Contexte clarifies ***/
	protected void clarifyCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();
		((BinaryContext) currentContext).clarifyContext();
		currentTable.setModelFromContext(currentContext);

	}


	/*** Ajout du contexte complementaire (contexte globale) ***/
	// FIXME : erreur lors de l'enregistement
	protected void addComplementaryToCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();
		((BinaryContext) currentContext).addComplementaryContext();
		currentTable.setModelFromContext(currentContext);

	}


	/*** contexte complementaire ***/
	protected void ShowComplementaryToCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();
		/*currentContext.complementaryContext();
		currentTable.setModelFromContext(currentContext);*/


		BinaryContext complement = new BinaryContext((BinaryContext) currentContext);
		complement.complementaryContext();
		complement.setName(currentContext.getName() + " '");



		addBinaryContext(complement);
		currentTable.setModelFromContext(currentContext);
	}

	/*** Reduction des objets contexte ***/
	protected void reduceObjectToCurrentContexte() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();
		((BinaryContext) currentContext).reduceObject().clarifyObject(); // TODO A confirmer.
		currentTable.setModelFromContext(currentContext);

	}

	/*** Reduction des attributs contexte ***/
	protected void reduceAttributeToCurrentContexte() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();
		((BinaryContext) currentContext).reduceAttribut().clarifyAttribute(); // TODO A confirmer.
		currentTable.setModelFromContext(currentContext);

	}

	/*** Reduction du contexte ***/
	protected void reduceCurrentContexte() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();
		((BinaryContext) currentContext).reduceContext();
		currentTable.setModelFromContext(currentContext);
		currentTable.revalidate();
	}

	/*** Show Arrow Context :fonction qui permet d'afficher le contexte avec les fleches ***/
	protected void showArrowContexte() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();
		Context c = ((BinaryContext) currentContext).setArrow();
		currentTable.setModelFromContext(c);
		

		selectedPane.repaint();
		currentTable.repaint();

	}

	/*** Hide Arrow Context ***/
	protected void hideArrowContexte() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();
		Context c = ((BinaryContext) currentContext).hideArrowContext();
		currentTable.setModelFromContext(c);
		

		
		selectedPane.repaint();
		currentTable.repaint();

	}

	/*protected void transitiveClosureFromCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
				.elementAt(currentContextIdx);
		TransitiveClosurePanel transPanel = new TransitiveClosurePanel(selectedPane);
		transPanel.open();

	}*/

	/*** Afficher le contexte de la Fermeture transitve des objets ***/
	protected void ShowTransitiveClosureObj() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();
		((BinaryContext) currentContext).transitives();
		//currentContext.setName("see");
		currentTable.setModelFromContext(currentContext);

	}

	/*** Afficher le contexte de la Fermeture transitve des attributs ***/
	protected void ShowTransitiveClosureAtt() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		ContextTable currentTable = selectedPane.getContextTable();

		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();
		((BinaryContext) currentContext).transitivesAtt();
		//currentContext.setName("see");
		currentTable.setModelFromContext(currentContext);

	}

	/**
	 * appposition
	 * Implemente l'action apposition de 2 contextes sur un clic
	 * @author Linda Bogni
	 */
	protected void apposition() throws AlreadyExistsException, InvalidTypeException, CloneNotSupportedException {

		Context c1=null;
		Context c2=null;
		String newName1="";
		String newName2="";

		//recuperer les contextes ouvertes
		Vector<ContextTableScrollPane> selectedPane = new Vector<>();
		for(int i=0;i<contextPanes.size();i++)
			selectedPane.add(contextPanes.elementAt(i));

		//Les stocker ds un tableau afin d'utiliser la boite dialog
		String[] choices1=new String[selectedPane.size()];
		for(int i=0;i<selectedPane.size();i++)
			choices1[i]=((ContextTableModel)selectedPane.elementAt(i).getContextTable().getModel()).getContext().getName();
		//System.out.println(" taille1 selectedPane : "+selectedPane.size());

		//1er context
		newName1=(String) DialogBox.showInputQuestion(panel, GUIMessages.getString("GUI.chooseFirstContext"), GUIMessages.getString("GUI.appositionOfContexts"), choices1, choices1[0]);
		if(newName1==null){
			//si aucun element choisi, arreter l'apposition
			return;
		}
		for(int i=0;i<selectedPane.size();i++){
			if(Objects.equals(newName1, choices1[i])){
				//recuperer le contexte choisi
				c1 = ((ContextTableModel) selectedPane.elementAt(i).getContextTable().getModel()).getContext();
				selectedPane.remove(i);//retirer le 1er context choisi
			}
		}

		//System.out.println(" taille2 selectedPane : "+selectedPane.size());

		//stocker le vecteur modifié ds un 2e tableau
		String[] choices2=new String[selectedPane.size()];
		for(int i=0;i<selectedPane.size();i++)
			choices2[i]=((ContextTableModel)selectedPane.elementAt(i).getContextTable().getModel()).getContext().getName();
		newName2=(String) DialogBox.showInputQuestion(panel, GUIMessages.getString("GUI.chooseSecondContext"), GUIMessages.getString("GUI.appositionOfContexts"), choices2, choices2[0]);
		if(newName2==null){
			//si aucun element choisi, arreter l'apposition
			return;
		}
		for(int i=0;i<selectedPane.size();i++){
			if(Objects.equals(newName2, choices2[i])){
				//recuperer le contexte choisi
				c2 = ((ContextTableModel) selectedPane.elementAt(i).getContextTable().getModel()).getContext();
			}
		}

		//Apposition
		//System.out.println(" c1 : "+newName1);
		//System.out.println(" c2 : "+newName2);
		Context c= c1 != null ? c1.apposition(c2) : null;
		if(c!=null){
			String newName = DialogBox.showInputQuestion(panel, GUIMessages.getString("GUI.enterGlobalContextName"), GUIMessages.getString("GUI.appositionOfContexts"));
			if(newName==null){//appuie sur annuler
				return;
			}
			c.setName(newName);
			//Construire le contexte resultat
			if (c instanceof BinaryContext){
				addBinaryContext((BinaryContext) c);
			}else if (c instanceof ValuedContext) {
				addValuedContext((ValuedContext) c);
			}
		}else{
			//Si le contexte est null, appuie sur annuler
			DialogBox.showMessageError(this, GUIMessages.getString("GUI.contextsDontHaveSameObjects"), GUIMessages.getString("GUI.errorObjectsContexts"));
		}
	}


	/**
	 * subposition
	 * Implemente l'action subposition de 2 contextes sur un clic
	 * @author Linda Bogni
	 */
	protected void subposition() throws AlreadyExistsException, InvalidTypeException, CloneNotSupportedException {

		Context c1=null;
		Context c2=null;
		String newName1="";
		String newName2="";

		Vector<ContextTableScrollPane> selectedPane = new Vector<>();
		for(int i=0;i<contextPanes.size();i++)
			selectedPane.add(contextPanes.elementAt(i));

		String[] choices1=new String[selectedPane.size()];
		for(int i=0;i<selectedPane.size();i++)
			choices1[i]=((ContextTableModel)selectedPane.elementAt(i).getContextTable().getModel()).getContext().getName();
		//System.out.println(" taille1 selectedPane : "+selectedPane.size());

		//1er context
		newName1=(String) DialogBox.showInputQuestion(panel, GUIMessages.getString("GUI.chooseFirstContext"), GUIMessages.getString("GUI.subpositionOfContexts"), choices1, choices1[0]);
		if(newName1==null){
			return;
		}
		for(int i=0;i<selectedPane.size();i++){
			if(Objects.equals(newName1, choices1[i])){
				c1 = ((ContextTableModel) selectedPane.elementAt(i).getContextTable().getModel()).getContext();
				selectedPane.remove(i);
			}
		}

		//System.out.println(" taille2 selectedPane : "+selectedPane.size());

		String[] choices2=new String[selectedPane.size()];
		for(int i=0;i<selectedPane.size();i++)
			choices2[i]=((ContextTableModel)selectedPane.elementAt(i).getContextTable().getModel()).getContext().getName();
		newName2=(String) DialogBox.showInputQuestion(panel, GUIMessages.getString("GUI.chooseSecondContext"), GUIMessages.getString("GUI.subpositionOfContexts"), choices2, choices2[0]);
		if(newName2==null){
			return;
		}
		for(int i=0;i<selectedPane.size();i++){
			if(Objects.equals(newName2, choices2[i])){
				c2 = ((ContextTableModel) selectedPane.elementAt(i).getContextTable().getModel()).getContext();
			}
		}

		//subposition
		//System.out.println(" c1 : "+newName1);
		//System.out.println(" c2 : "+newName2);
		Context c= c1 != null ? c1.subposition(c2) : null;
		if(c!=null){
			String newName = DialogBox.showInputQuestion(panel, GUIMessages.getString("GUI.enterGlobalContextName"), GUIMessages.getString("GUI.subpositionOfContexts"));
			if(newName==null){//appuie sur annuler 
				return;
			}
			c.setName(newName);
			//Construire le contexte resultat
			if (c instanceof BinaryContext){
				addBinaryContext((BinaryContext) c);
			}else if (c instanceof ValuedContext) {
				addValuedContext((ValuedContext) c);
			}
		}else{
			DialogBox.showMessageError(this, GUIMessages.getString("GUI.contextsDontHaveSameAttributes"), GUIMessages.getString("GUI.errorAttributesContexts"));
		}
	}


	/**
	 * createTaxonomyAttributeInCurrentContext
	 * Implemente l'action createTaxonomyAttribute
	 * @author Linda Bogni
	 */
	protected void createTaxonomyAttributeInCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);

		TaxonomyAttributePanel taxonomyPanel = new TaxonomyAttributePanel(
				selectedPane, this);
		taxonomyPanel.open();
	}

	/**
	 * createTaxonomyObjectInCurrentContext
	 * Implemente l'action createTaxonomyAttribute
	 * @author Linda Bogni
	 */
	protected void createTaxonomyObjectInCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes
		.elementAt(currentContextIdx);
		TaxonomyObjectPanel taxonomyPanel = new TaxonomyObjectPanel(
				selectedPane, this);
		taxonomyPanel.open();
	}
	/**
	 * createTaxonomyInCurrentContext
	 * Implemente l'action createTaxonomy afin de creer et sauver la taxonomie 
	 * @author Linda Bogni
	 */
	protected void saveGeneralisationInCurrentContext() {
		if (currentContextIdx < 0)
			return;

		ContextTableScrollPane selectedPane = contextPanes.elementAt(currentContextIdx);
		//recupere le contexte courant
		Context currentContext = ((ContextTableModel) selectedPane.getContextTable().getModel()).getContext();

		//Si pas de taxonomie construite
		if(currentContext.getDonneesTaxonomieObj()==null && currentContext.getDonneesTaxonomieAtt()==null){
			return;
		}
		//Choisir de faire la taxonomie des attributs ou celle des objects
		String typeTax = DialogBox.showInputQuestion(panel,GUIMessages.getString("GUI.enterTypeGeneralisation"),
				GUIMessages.getString("GUI.Generalisation")); 
		currentContext.setTypeTax(typeTax);
		//System.out.println("Donnees objets : "+currentContext.getDonneesTaxonomieObjElement(0));
		if(typeTax==null)
			return;
		if(!typeTax.equals("objects") && !typeTax.equals("attributes")){
			DialogBox.showMessageInformation(this, GUIMessages.getString("GUI.choiceAttributesOrObjects"), GUIMessages.getString("GUI.Generalisation")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		//Si pas de taxonomie sur les objets construite
		if(typeTax.equals("objects") && currentContext.getDonneesTaxonomieObj().isEmpty()){
			DialogBox.showMessageInformation(this, GUIMessages.getString("GUI.GeneralisationNotExistedObj"), GUIMessages.getString("GUI.saveGeneralisation")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		//Si pas de taxonomie sur les attributs construite
		if(typeTax.equals("attributes") && currentContext.getDonneesTaxonomieAtt().isEmpty()){
			DialogBox.showMessageInformation(this, GUIMessages.getString("GUI.GeneralisationNotExistedAtt"), GUIMessages.getString("GUI.saveGeneralisation")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		JFileChooser fileChooser = new JFileChooser(LMPreferences.getLastDirectory());

		// Proprietes du fileChooser
		fileChooser.setApproveButtonText(GUIMessages.getString("GUI.saveAs")); //$NON-NLS-1$
		fileChooser.setDialogTitle(GUIMessages.getString("GUI.saveCurrentGeneralisation")); //$NON-NLS-1$
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Gere les extensions compatibles (lmv, lmn, lmb)
		// et met un nom de fichier par defaut
		ExampleFileFilter filterLatticeXml = new ExampleFileFilter("tax.xml", GUIMessages.getString("GUI.LatticeXMLGeneralisationFormat")); //$NON-NLS-1$ //$NON-NLS-2$
		fileChooser.addChoosableFileFilter(filterLatticeXml);
		fileChooser.setSelectedFile(new File("" + GUIMessages.getString("GUI.LatticeMinerBinaryFormatDefaultName")));


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

				String newFileName = oldFileName +"_Gen_"+typeTax + "." + newFileType; //$NON-NLS-1$
				if (posOldExt != -1)
					newFileName = newFileName.substring(0, posOldExt) + "." + newFileType; //$NON-NLS-1$

				fileName = new File(newFileName);
			}

			if (fileName.exists()) {
				int overwrite = DialogBox.showDialogWarning(this, GUIMessages.getString("GUI.doYouWantToOverwriteFile"), //$NON-NLS-1$
						GUIMessages.getString("GUI.selectedFileAlreadyExist")); //$NON-NLS-1$

				if (overwrite == DialogBox.NO) {
					DialogBox.showMessageInformation(this, GUIMessages.getString("GUI.GeneralisationNotSaved"), GUIMessages.getString("GUI.notSaved")); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
			}

			// Sauvegarde la taxonomie
			try {
				// Recupere l'extension du fichier
				String fileType = ExampleFileFilter.getExtension(fileName);

				if (fileType.equals("tax.xml")) {
					new LatticeXMLTaxonomyWriter(fileName, (BinaryContext) currentContext);
				}

				else {
					DialogBox.showMessageError(this, GUIMessages.getString("GUI.GeneralisationExtensionNotKnown"), //$NON-NLS-1$
							GUIMessages.getString("GUI.wrongGeneralisationFormat")); //$NON-NLS-1$
					return;
				}
				DialogBox.showMessageInformation(this, GUIMessages.getString("GUI.GeneralisationSuccessfullySaved"), GUIMessages.getString("GUI.saveSuccess")); //$NON-NLS-1$ //$NON-NLS-2$

			} catch (IOException ioe) {
				DialogBox.showMessageError(this, GUIMessages.getString("GUI.GeneralisationCouldnotBeSaved"), GUIMessages.getString("GUI.errorWithFile")); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (WriterException e) {
				DialogBox.showMessageError(this, e);
			}

			// Sauvergarde le path utilise
			LMPreferences.setLastDirectory(fileChooser.getCurrentDirectory().getAbsolutePath());

		}
	}



	/* Redefinition adaptee ala fenetre racine */
	@Override
	public void windowClosing(WindowEvent e) {
		if (e.getSource() == this) {
			if (hasClosedAllContexts())
				System.exit(0);
		} else if (e.getSource() instanceof JFrame) {
			((JFrame) e.getSource()).dispose();
		}
	}

	/**
	 * Implemente l'action a executer lors d'un clic sur un {@link MenuItem} du
	 * menu "Window", a savoir faire basculer de context dans le
	 * {@link ContextViewer}
	 */
	protected class ViewTableListener implements ActionListener {
		protected final ContextTableScrollPane table;

		/**
		 * Constructeur de l'observateur sur un {@link MenuItem} du menu
		 * "Window"
		 * 
		 * @param ctsp
		 *            le panneau qu'il faudra ouvrir lors du clic
		 */
		public ViewTableListener(ContextTableScrollPane ctsp) {
			table = ctsp;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public void actionPerformed(ActionEvent ae) {
			int idx = contextPanes.indexOf(table);
			selectContextAt(idx);
		}
	}

	/**
	 * Implemente l'action a executer lors d'un clic sur un {@link MenuItem} du
	 * menu "OpenRecent", a savoir ouvrir un nouveau context avec le fichier
	 * souhaite
	 * 
	 * @author Ludovic Thomas
	 */
	protected class RecentMenuListener implements ActionListener {
		private final String fileAbsolutePath;

		/**
		 * Constructeur de l'observateur sur un {@link MenuItem} du menu
		 * "OpenRecent"
		 * 
		 * @param fileAbsolutePath
		 *            le path du fichier qu'il faudra ouvrir lors du clic
		 */
		public RecentMenuListener(String fileAbsolutePath) {
			this.fileAbsolutePath = fileAbsolutePath;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public void actionPerformed(ActionEvent e) {
			openContextFile(new File(fileAbsolutePath));
		}

	}
	public class LMMenuItem extends JMenuItem implements ActionListener {
		private static final long serialVersionUID = 1L;
		private final ContextViewer parent;
		private Method method=null;
		private boolean arg;
		public LMMenuItem(String text,ContextViewer parent, String action) {
			super(text);
			this.parent=parent;
			try {
				method = parent.getClass().getDeclaredMethod(action);
				addActionListener(this);
			} catch (SecurityException | NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		protected LMMenuItem(String text, ContextViewer parent) {
			super(text);
			this.parent = parent;
			try {
				method = parent.getClass().getDeclaredMethod("hasClosedCurrentContext", Boolean.class);
				addActionListener(this);
			} catch (SecurityException | NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.arg = true;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				if (this.arg == false) {
					method.invoke(parent);
				} else {
					method.invoke(parent, this.arg);
				}
			} catch (IllegalArgumentException e) {
				try {
					method.invoke(parent);
					addActionListener(this);
				} catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (IllegalAccessException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public class LMButton extends JButton implements ActionListener{
		private static final long serialVersionUID = 1L;
		private final ContextViewer parent;
		private Method method=null;
		private boolean arg;
		public LMButton(ImageIcon save,ContextViewer parent, String action) {
			super(save);
			this.parent=parent;
			try {
				method = parent.getClass().getDeclaredMethod(action);
				addActionListener(this);
			} catch (SecurityException | NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		protected LMButton(ImageIcon save, ContextViewer parent) {
			super(save);
			this.parent = parent;
			try {
				method = parent.getClass().getDeclaredMethod("hasClosedCurrentContext", Boolean.class);
				addActionListener(this);
			} catch (SecurityException | NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.arg = true;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				method.invoke(parent, arg);
			} catch (IllegalArgumentException e) {
				try {
					method.invoke(parent);
				} catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (IllegalAccessException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Implemente la fenetre d'information de Lattice Miner
	 */
	public class AboutScreen {

		/**
		 * Constructeur de la fenetre d'information de Lattice Miner
		 */
		public AboutScreen() {

			String title = GUIMessages.getString("GUI.aboutLatticeMiner"); //$NON-NLS-1$

			String about = GUIMessages.getString("GUI.latticeMinerPlatform") + "\n" + GUIMessages.getString("GUI.releaseLatticeMiner") + " : " + LatticeMiner.LATTICE_MINER_VERSION + " \n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			+ "\n" //$NON-NLS-1$
			+ GUIMessages.getString("GUI.copyrightLatticeMiner") + " \n" //$NON-NLS-1$ //$NON-NLS-2$
			+ GUIMessages.getString("GUI.companyLatticeMiner") + ": " + GUIMessages.getString("GUI.companyLatticeMiner1") + "\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			+ GUIMessages.getString("GUI.companyLatticeMiner2") + "\n" //$NON-NLS-1$ //$NON-NLS-2$
			+ GUIMessages.getString("GUI.companyLatticeMiner3") + "\n" + "\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			+ GUIMessages.getString("GUI.contributorsLatticeMiner") + ": " + GUIMessages.getString("GUI.contributorsLatticeMinerValue") + "\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			+ GUIMessages
			.getString("GUI.principalnitiatorLatticeMiner") + ": " + GUIMessages.getString("GUI.principalnitiatorLatticeMinerValue"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			DialogBox.showMessageInformation(new JFrame(title), about, title);
		}
	}

}