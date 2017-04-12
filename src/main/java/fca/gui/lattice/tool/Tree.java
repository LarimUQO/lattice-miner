package fca.gui.lattice.tool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import fca.gui.lattice.LatticePanel;
import fca.gui.lattice.element.GraphicalConcept;
import fca.gui.lattice.element.GraphicalLattice;
import fca.messages.GUIMessages;

/**
 * Panneau affichant la vue arborescente d'un treillis : chaque concept du treillis y est montré,
 * placé sous les noeuds qui le contiennent, dans l'ordre d'imbrication, s'il y a lieu
 * @author Geneviève Roberge
 * @version 1.0
 */
public class Tree extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 8106841785269391252L;

	private GraphicalLattice lattice; //Treillis pour lequel l'arbre est construit

	private DefaultTreeModel treeModel; //Modèle conceptuel de l'arbre

	private JTree tree; //Arbre affiché
	/**
	 * inverse="this$0:fca.gui.lattice.PanelTree$TreeGraphNode"
	 */
	private TreePath path; //Chemin pour la racine du modèle

	private GraphicalConcept selectedConcept; //Concept graphique lié au noeud de l'arbre sélectionné

	private boolean isGlobalLatticeSelected; //Indique si le noeud racine est sélectionné

	/**
	 * Le viewer qui affiche le treillis
	 */
	LatticePanel viewer;

	/**
	 * Constructeur
	 * @param gl Le NestedGraphLattice à représenter dans l'arbre
	 */
	public Tree(GraphicalLattice gl, LatticePanel lp) {
		lattice = gl;
		viewer = lp;
		treeModel = null;
		tree = null;
		path = null;
		selectedConcept = null;
		isGlobalLatticeSelected = false;

		createTreeModel();
		createTree();

		setBackground(Color.WHITE);
		setLayout(new BorderLayout(0, 0));
		add(tree, BorderLayout.WEST);
	}

	/**
	 * Permet d'obtenir l'arbre affiché dans ce panneau
	 * @return Le JTree affiché dans ce panneau
	 */
	public JTree getTree() {
		return tree;
	}

	/**
	 * Change l'arbre pour représenter un nouveau treillis
	 * @param gl Le GraphicalLattice qui doit maintenant être réprésenté dans l'arbre
	 */
	public void setNewLattice(GraphicalLattice gl) {
		lattice = gl;
		path = null;
		selectedConcept = null;
		isGlobalLatticeSelected = false;

		treeModel = null;
		createTreeModel();
		tree.setModel(treeModel);
		tree.validate();
	}

	/**
	 * Permet d'obtenir le concept grpahique représenté par le noeud sélectionné de l'arbre
	 * @return Le NestedGraphNode derrière le noeud sélectionné de l'arbre
	 */
	public GraphicalConcept getSelectedNode() {
		return selectedConcept;
	}

	/**
	 * Indique si la racine de l'arbre est sélectionnée ou non La racine de l'arbre ne contient
	 * aucun concept graphique, il est donc utile de savoir si le noeud sélectionné de l'arbre est
	 * la racine ou non avant de demander d'obtenir le concept graphique derrière le noeud
	 * sélectionné de l'arbre.
	 * @return Le boolean indiquand si la racine de l'arbre est sélectionnée
	 */
	public boolean isGlobalLatticeSelected() {
		return isGlobalLatticeSelected;
	}

	/**
	 * Sélectionne le noeud de l'arbre qui correspond au concept graphique donné
	 * @param concept Le GraphicalConcept contenant le concept graphique choisi
	 */
	public void selectPathNode(GraphicalConcept concept) {
		selectedConcept = null;
		if (concept != null) {
			Object[] nodes = path.getPath();
			int level = -1;
			DefaultMutableTreeNode conceptNode = null;
			/* Recherche du noeud correspondant au concept graphique choisi */
			for (int i = 0; i < nodes.length; i++) {
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) nodes[i];
				TreeGraphNode userNode = (TreeGraphNode) treeNode.getUserObject();
				if (userNode.getGraphicalConcept() != null && userNode.getGraphicalConcept().equals(concept)) {
					level = treeNode.getLevel();
					conceptNode = treeNode;
					break;
				}
			}
			/* Construction du chemin jusqu'au noeud ciblé de l'arbre */
			DefaultMutableTreeNode[] selectionPath = new DefaultMutableTreeNode[level + 1];
			DefaultMutableTreeNode currentNode = conceptNode;
			for (int i = level; i >= 0; i--) {
				selectionPath[i] = currentNode;
				currentNode = (DefaultMutableTreeNode) currentNode.getParent();
			}

			/* Ouverture de l'arbre pour voir le noeud ciblé et sélection de ce noeud */
			TreePath selectionTreePath = new TreePath(selectionPath);
			tree.makeVisible(selectionTreePath);
			int selectionRow = tree.getRowForPath(selectionTreePath);
			tree.setSelectionInterval(selectionRow, selectionRow);
		}

		/* Aucun concept graphique reçu => Le noeud racine est sélectionné */
		else {
			tree.setSelectionInterval(0, 0);
		}
	}

	/**
	 * Crée le modèle d'arbre qui permettra de construire et gérer l'arbre, à partir des variables
	 * de ce panneau
	 */
	private void createTreeModel() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new TreeGraphNode(null));
		path = new TreePath(top);
		createTreeModelNodes(lattice, top);
		treeModel = new DefaultTreeModel(top);
	}

	/**
	 * Crée les noeuds du modèle de l'arbre pour un treillis donné et à partir d'un noeud d'arbre
	 * donné
	 * @param currentLattice Le NestedGraphLattice pour lequel les noeuds sont construits
	 * @param parent Le DefaultMutableTreeNode à sous lequel les nouveaux noeuds seront créés
	 */
	private void createTreeModelNodes(GraphicalLattice currentLattice, DefaultMutableTreeNode parent) {
		/*
		 * Recherche dans les concepts du treillis pour construire les noeuds des concepts finaux
		 * seulement
		 */
		Vector<GraphicalConcept> latticeNodes = currentLattice.getNodesList();
		for (int i = 0; i < latticeNodes.size(); i++) {
			GraphicalConcept currentConcept = latticeNodes.elementAt(i);
			if (currentConcept.getNestedConcept().isFinalConcept()) {
				DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(new TreeGraphNode(currentConcept));
				parent.add(currentNode);
				path = path.pathByAddingChild(currentNode);

				/*
				 * Construction des noeuds des concepts finaux du treillis interne sous le noeud
				 * courant
				 */
				GraphicalLattice internalLattice = currentConcept.getInternalLattice();
				if (internalLattice != null)
					createTreeModelNodes(internalLattice, currentNode);
			}
		}
	}

	/**
	 * Construction de l'arbre à partir du modèle déjà créé appartenant à ce panneau
	 */
	private void createTree() {
		tree = new JTree(treeModel);
		tree.setShowsRootHandles(true);
		tree.setEditable(false);
		tree.setSelectionPath(path);
		tree.setExpandsSelectedPaths(true);
		tree.addTreeSelectionListener(new LatticeTreeListener());
	}

	/**
	 * Objet qui sera placé derrière un noeud de l'arbre et qui contient le concept graphique
	 * associé à chaque noeud ainsi que sa représentation sous forme de chaîne de caractère (pour
	 * l'affichage dans l'arbre)
	 */
	private class TreeGraphNode {
		GraphicalConcept concept; //Concept graphique associé au noeud
		String stringValue; //Valeur à afficher dans l'arbre

		/**
		 * Constructeur
		 * @param gc Le GraphicalConcept qui sera représenté par le noeud
		 */
		public TreeGraphNode(GraphicalConcept gc) {
			concept = gc;
			/* Un concept graphique non null prendra l'intention du concept comme valeur à afficher */
			if (gc != null && gc.getNestedConcept().getConcept().getIntent() != null)
				stringValue = concept.getNestedConcept().getConcept().getIntent().toString();

			/* Un concept graphique null prendra une chaîne fixe comme valeur à afficher */
			else
				stringValue = GUIMessages.getString("GUI.globalLattice"); //$NON-NLS-1$
		}

		/**
		 * Permet d'obtenir le concept graphique associé à ce noeud
		 * @return Le GraphicalConcept associé au noeud
		 */
		public GraphicalConcept getGraphicalConcept() {
			return concept;
		}

		/**
		 * Permet d'obtenir la représentation de ce noeud sous forme de chaîne de caractères
		 * @return La String contenant la représentation du noeud sous forme de chaîne de caractères
		 */
		@Override
		public String toString() {
			return stringValue;
		}
	}

	/**
	 * Classe chargée de traiter les événements de l'arbre
	 */
	private class LatticeTreeListener implements TreeSelectionListener {
		/* ======== TREESELECTIONLISTENER INTERFACE ======== */

		/* (non-Javadoc)
		 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
		 */
		public void valueChanged(TreeSelectionEvent e) {
			TreePath currentPath = e.getPath();
			if (lattice.isEditable())
				lattice.setOutOfFocus(true);
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) currentPath.getLastPathComponent();
			TreeGraphNode selectedGraphConcept = (TreeGraphNode) selectedNode.getUserObject();

			/* Sélection d'un noeud autre que le noeud racine */
			if (selectedGraphConcept.getGraphicalConcept() != null) {
				selectedConcept = selectedGraphConcept.getGraphicalConcept();
				isGlobalLatticeSelected = false;
			}

			/* Selection du noeud racine */
			else {
				selectedConcept = null;
				isGlobalLatticeSelected = true;
			}
		}
	}

}