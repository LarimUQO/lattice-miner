package fca.gui.context.table;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import fca.core.context.Context;
import fca.core.context.binary.BinaryContext;
import fca.gui.context.table.model.BinaryContextTableModel;
import fca.gui.context.table.model.ContextTableModel;
import fca.gui.util.DialogBox;
import fca.messages.GUIMessages;

public class BinaryContextTable extends ContextTable implements KeyListener {

	/**
	 * FIXME : ColumneHeaderListener (mousemotion) défaillant
	 */
	private static final long serialVersionUID = 1L;
	/* Gestion du langage */
	private static int NEW_ATT_NAME = 1;
	private static int NEW_OBJ_NAME = 2;
	private static int MODIFY = 3;

	private int draggedRow = -1;
	private int draggedCol = 1;
	private JPanel panel;

	/**
	 * Constructeur.
	 * @param bc Le {@link BinaryContext} de cette table
	 */
	public BinaryContextTable(BinaryContext bc) {
		super(new BinaryContextTableModel(bc));

		ColumnHeaderListener colListener = new ColumnHeaderListener();
		getTableHeader().addMouseListener(colListener);
		//getTableHeader().addMouseMotionListener(colListener);


		RowHeaderListener rowListener = new RowHeaderListener();
		getRowHeader().addMouseListener(rowListener);
		getRowHeader().addMouseMotionListener(rowListener);

		addKeyListener(this);
	}

	/**
	 * Constructeur.
	 * @param model Le {@link BinaryContextTableModel} de cette table
	 */
	public BinaryContextTable(BinaryContextTableModel model) {
		super(model);

		ColumnHeaderListener colListener = new ColumnHeaderListener();
		getTableHeader().addMouseListener(colListener);
		//getTableHeader().addMouseMotionListener(colListener);

		RowHeaderListener rowListener = new RowHeaderListener();
		getRowHeader().addMouseListener(rowListener);
		getRowHeader().addMouseMotionListener(rowListener);
	}

	@Override
	public void setModelFromContext(Context c) {
		setModel(new BinaryContextTableModel((BinaryContext) c));

		getTableHeader().repaint();

		ColumnHeaderListener colListener = new ColumnHeaderListener();
		getTableHeader().addMouseListener(colListener);
		//getTableHeader().addMouseMotionListener(colListener);

		getRowHeader().repaint();

		RowHeaderListener rowListener = new RowHeaderListener();
		getRowHeader().addMouseListener(rowListener);
		getRowHeader().addMouseMotionListener(rowListener);
	}

	/**
	 * Permet d'indiquer si le déplacement des rangées est permis.
	 * @param b Un boolean indiquant si le déplacement est permis.
	 */
	public void setMoveRowAllowed(boolean b) {
		((ContextTableModel) getModel()).setMoveRowAllowed(b);
	}

	/**
	 * Permet d'indiquer si le déplacement des colonnes est permis.
	 * @param b Un boolean indiquant si le déplacement est permis.
	 */
	public void setMoveColAllowed(boolean b) {
		((ContextTableModel) getModel()).setMoveColAllowed(b);
	}

	/**
	 * Permet de savoir si le déplacement des rangées est permis.
	 * @return Un boolean indiquant si le déplacement est permis.
	 */
	public boolean isMoveRowAllowed() {
		return ((ContextTableModel) getModel()).isMoveRowAllowed();
	}

	/**
	 * Permet de déplacer une rangee.
	 * @param startIdx Un int indiquant la rangee à deplacer
	 * @param endIdx Un int indiquant la destination de la rangee à deplacer
	 */
	public void moveRow(int startIdx, int endIdx) {
		if (((ContextTableModel) getModel()).hasMovedRow(startIdx, endIdx)) {
			setModelFromContext(((ContextTableModel) getModel()).getContext());
		}
	}

	/**
	 * Permet de déplacer une colonne.
	 * @param startIdx Un int indiquant la colonne à déplacer
	 * @param endIdx Un int indiquant la destination de la colonne à deplacer
	 */
	// FIXME
	public void moveCol(int startIdx, int endIdx) {
		if (((ContextTableModel) getModel()).hasMovedCol(startIdx, endIdx)) {
			//System.out.println("test");
			setModelFromContext(((ContextTableModel) getModel()).getContext());
		}
	}

	@Override
	public boolean isRowSelected(int rowIdx) {
		boolean superSelected = super.isRowSelected(rowIdx);

		return superSelected || draggedRow == rowIdx;
	}

	@Override
	public boolean isColumnSelected(int colIdx) {
		boolean superSelected = super.isColumnSelected(colIdx);

		return superSelected || draggedCol == colIdx;
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		int col = columnAtPoint(e.getPoint());
		int row = rowAtPoint(e.getPoint());

		//***********************************************************
		//			La déclaration pour le menu contextuel
		//***********************************************************
		JPopupMenu jpm = new JPopupMenu();
		//JMenu Copy = new JMenu("Copy");
		//JMenu Paste = new JMenu("Paste");
		JMenuItem copy= new JMenuItem("Copy");
		//JMenuItem paste = new JMenuItem("Paste");

		if (row > -1 && col > -1 && e.getClickCount() == 2) {
			Context ctx=((ContextTableModel) getModel()).getContext();

			if (getValueAt(row, convertColumnIndexToModel(col)).equals("X")){ //$NON-NLS-1$
				getModel().setValueAt("", row, convertColumnIndexToModel(col)); //$NON-NLS-1$
				//-->LINDA
				//mettre a jour si existe attributs ou objtes créés par taxonomie

				if(!ctx.getTypeTax().equals("rien")){
					update();
				}
			}else{
				getModel().setValueAt("X", row, convertColumnIndexToModel(col)); //$NON-NLS-1$
				//-->LINDA
				//mettre a jour si existe attributs ou objtes créés par taxonomie
				if(!ctx.getTypeTax().equals("rien")){
					update();
				}
			}

			repaint();
		}
		//
		else{
			if(e.getButton() == MouseEvent.BUTTON3){
				//System.out.println("clik droit");
				jpm.add(copy);
				//jpm.add(paste);
				//La méthode qui va afficher le menu
				jpm.show(getTableHeader(), e.getX(), e.getY());

			}
		}
	}

	/*private class ColumnHeaderListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			int col = columnAtPoint(e.getPoint());
			if (col > -1 && e.getClickCount() == 2) {
				String newName = DialogBox.showInputQuestion(thisTable, getBinaryTableText(NEW_ATT_NAME),
						getBinaryTableText(MODIFY));

				if (newName != null)
					thisTable.setColumnName(col, newName);
			}
		}*/

	private class ColumnHeaderListener implements MouseListener, MouseMotionListener {
		private static final int INVALID_ATT_NAME = 0;
		private boolean drag = false;
		private int dragStartIdx = -1;

		@Override
		public void mouseClicked(MouseEvent e) {
			int col = columnAtPoint(e.getPoint());
			Context c=((ContextTableModel) getModel()).getContext();
			boolean exist=false;
			if (col > -1){
				if(e.getClickCount() == 2) {
					//Recupere l'ancien nom de colonne
					String former = thisTable.getColumnName(col);
					// Demande le nouveau nom
					String newName = (String) DialogBox.showInputQuestion(thisTable, getBinaryTableText(NEW_ATT_NAME),
							getBinaryTableText(MODIFY), null, former);
					//si l'objet existe ds le contexte, sortir.-->LINDA
					for(int i=0;i<c.getObjectCount();i++){
						if(c.getAttributes().contains(newName)){
							DialogBox.showMessageWarning(panel, getContextTableText(INVALID_ATT_NAME), GUIMessages.getString("GUI.invalidAttributeName")); //$NON-NLS-1$
							break;
						}
					}
					//System.out.println("exist0 : "+exist);
					if (newName != null && exist==false){
						int trouve=0;//attribut simple, non utilisé pr la generalisation
						//System.out.println("exist1 : "+exist);
						String[] expression=new String[3];
						for(int i=0; i<c.getDonneesTaxonomieAttCount(); i++){
							expression=c.getDonneesTaxonomieAttElement(i).split(" : ");
							//System.out.println("changer tax ");
							//regarder si un attribut généralisé qui change
							if(thisTable.getColumnName(convertColumnIndexToModel(col)).equals(expression[0])){
								if(c.getTypeGen().equals("AND")||c.getTypeGen().equals("OR")){
									String newName1=newName+ " : "+expression[1];
									//System.out.println("newName1 : "+newName1);
									c.getDonneesTaxonomieAtt().removeElementAt(i);
									c.setDonneesTaxonomieAttElement(i,newName1);

								}else if(c.getTypeGen().equals("PERCENTAGE")){
									String newName1=newName.toUpperCase()+ " : "+expression[1]+" : "+expression[2];
									//System.out.println("newName1 : "+newName1);
									c.getDonneesTaxonomieAtt().removeElementAt(i);
									c.setDonneesTaxonomieAttElement(i,newName1);
								}
								thisTable.setColumnName(col, newName);
								break;
							}else{//regarder si un attribut spécialisé qui change


								Vector<String> elts=new Vector<String>();//contient les attributs(termes) du noeud
								int pos=1;
								//On traite l'expression de l'attribut généralisé
								while (expression[1].charAt(pos) == ' ')
									pos++;
								String deb=expression[1].substring(pos, pos+10);
								//System.out.println(" deb : "+deb);
								if(deb.compareToIgnoreCase("PERCENTAGE")==0){
									//System.out.println("per");
									pos+=11;
									//System.out.println("expression[1].length(): "+expression[1].length());
									while (pos < expression[1].length()-1){
										while (expression[1].charAt(pos) == ' '|| expression[1].substring(pos, pos+1).compareToIgnoreCase("]")==0
												|| expression[1].substring(pos, pos+1).compareToIgnoreCase("[")==0)
											pos++;
										elts.add(expression[1].substring(pos, pos+1));
										pos++;
									}
									String expr1=" PERCENTAGE ";//nvl expression[1]
									for(int j=0;j<elts.size();j++){
										if(thisTable.getColumnName(convertColumnIndexToModel(col)).equals(elts.elementAt(j))){
											elts.removeElementAt(j);
											elts.add(j, newName);
											trouve=1;
										}
										expr1+=" ["+elts.elementAt(j)+"]";
									}
									if(trouve==1){
										String newName1=expression[0]+" : "+expr1+" : "+expression[2];
										//System.out.println("newName1 : "+newName1);
										//System.out.println("tax : "+c.getDonneesTaxonomieAttElement(i));
										c.getDonneesTaxonomieAtt().removeElementAt(i);
										c.setDonneesTaxonomieAttElement(i,newName1);
									}

								}else{

									String op=expression[1].substring(pos+4, pos+6);//op=OR
									String op1=expression[1].substring(pos+4, pos+7);//op=OR
									String expr=expression[1].substring(1);//expression sans le 1er espace au debut de la chaine
									String[] ensElts=new String[expr.length()];
									//System.out.println("op0 : "+op);
									ensElts=expr.split(" OR ");
									if(op.equals("OR"))	{
										//	System.out.println("or");
										for(int l=0;l<ensElts.length;l++){
											if(ensElts[l]==null) break;//sil ya plus dattributs afin deviter de parcourir tout le le reste du tableau vide
											elts.add(ensElts[l].substring(1, ensElts[l].length()-1));
										}
										String expr1 = "";
										for(int j=0;j<elts.size();j++){
											//System.out.println("cherche : "+elts.elementAt(j));
											//System.out.println("cherch : "+thisTable.getColumnName(convertColumnIndexToModel(col)));
											if(thisTable.getColumnName(convertColumnIndexToModel(col)).equals(elts.elementAt(j))){
												//System.out.println("chui la : "+elts.elementAt(j));
												elts.removeElementAt(j);
												elts.add(j, newName);
												trouve=1;
											}
											if(j==elts.size()-1){
												expr1+="["+elts.elementAt(j)+"]";
												break;
											}else{
												expr1+="["+elts.elementAt(j)+"] OR ";
											}
										}
										if(trouve==1){
											//System.out.println("expr1OR : "+expr1);
											String newName1=expression[0]+" :  "+expr1;
											c.getDonneesTaxonomieAtt().removeElementAt(i);
											c.setDonneesTaxonomieAttElement(i,newName1);
										}

									}else if(op1.equals("AND")){
										//System.out.println("and");
										op=expression[1].substring(pos+4, pos+7);//op=AND
										ensElts=expr.split(" AND ");
										for(int l=0;l<ensElts.length;l++){
											if(ensElts[l]==null) break;//sil ya plus dattributs afin deviter de parcourir tout le le reste du tableau vide
											elts.add(ensElts[l].substring(1, ensElts[l].length()-1));
										}
										String expr1 = "";
										for(int j=0;j<elts.size();j++){
											//	System.out.println("cherche : "+elts.elementAt(j));
											//System.out.println("cherch : "+thisTable.getColumnName(convertColumnIndexToModel(col)));
											if(thisTable.getColumnName(convertColumnIndexToModel(col)).equals(elts.elementAt(j))){
												elts.removeElementAt(j);
												elts.add(j, newName);
												trouve=1;
											}
											if(j==elts.size()-1){
												expr1+="["+elts.elementAt(j)+"]";
												break;
											}else{
												expr1+="["+elts.elementAt(j)+"] AND ";
											}
										}
										if(trouve==1){
											//System.out.println("expr1AND : "+expr1);
											//System.out.println("attNew : "+newName);
											String newName1=expression[0]+" :  "+expr1;
											//System.out.println("newName1 : "+newName1);
											//System.out.println("attChanG : "+c.getDonneesTaxonomieAttElement(i));
											c.getDonneesTaxonomieAtt().removeElementAt(i);
											c.setDonneesTaxonomieAttElement(i,newName1);
										}

									}
								}
								//System.out.println("maj");
								thisTable.setColumnName(col, newName);
								break;

							}
						}
						if(trouve==0){
							thisTable.setColumnName(col, newName);
							return;
						}
					}
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			setColumnSelectionAllowed(true);
			drag = true;
			dragStartIdx = columnAtPoint(e.getPoint());
			draggedCol = dragStartIdx;

			if (dragStartIdx < 0)
				drag = false;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			setColumnSelectionAllowed(false);
			drag = false;
			dragStartIdx = -1;
			draggedCol = -1;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int endIdx = columnAtPoint(e.getPoint());
			if (drag && dragStartIdx != endIdx) {
				moveCol(dragStartIdx, endIdx);
				dragStartIdx = endIdx;
				draggedCol = dragStartIdx;
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	}

	/*	private class RowHeaderListener implements MouseListener, MouseMotionListener {
		private boolean drag = false;
		private int dragStartIdx = -1;

		public void mouseClicked(MouseEvent e) {
			int row = rowAtPoint(e.getPoint());
			if (row > -1 && e.getClickCount() == 2) {
				String newName = DialogBox.showInputQuestion(thisTable, getBinaryTableText(NEW_OBJ_NAME),
						getBinaryTableText(MODIFY));

				if (newName != null)
					thisTable.setRowName(row, newName);
			}
		}*/

	private class RowHeaderListener implements MouseListener, MouseMotionListener {
		private static final int INVALID_OBJ_NAME = 0;
		private boolean drag = false;
		private int dragStartIdx = -1;

		@Override
		public void mouseClicked(MouseEvent e) {
			int row = rowAtPoint(e.getPoint());
			Context c=((ContextTableModel) getModel()).getContext();
			int exist=0;
			if (row > -1 ){
				if(e.getClickCount() == 2) {
					// Recupere le nom de ligne courant
					String former = ((ContextTableModel) getModel()).getRowName(row);
					// Demande le nouveau nom
					String newName = (String) DialogBox.showInputQuestion(thisTable, getBinaryTableText(NEW_OBJ_NAME),
							getBinaryTableText(MODIFY), null, former);
					//si l'objet existe ds le contexte, sortir.-->LINDA
					for(int i=0;i<c.getAttributeCount();i++){
						if(c.getObjects().contains(newName)){
							DialogBox.showMessageWarning(panel, getContextTableText(INVALID_OBJ_NAME), GUIMessages.getString("GUI.invalidObjectName")); //$NON-NLS-1$
							exist=1;
							break;
						}
					}
					//System.out.println("exist0O : "+exist);
					if (newName != null && exist==0){
						//changer le nom dans la taxonomie -->LINDA
						String[] expression=new String[3];
						int trouve =0;//objet simple, non utilisé pr la generalisation
						for(int i=0; i<c.getDonneesTaxonomieObjCount();i++){
							//System.out.println("changer tax ");
							expression=c.getDonneesTaxonomieObjElement(i).split(" : ");
							if(c.getObjectAt(row).equals(expression[0])){
								if(c.getTypeGen().equals("AND")||c.getTypeGen().equals("OR")){
									String newName1=newName.toUpperCase()+ " : "+expression[1];
									//System.out.println("newName1ANDOR : "+newName1);
									c.getDonneesTaxonomieObj().removeElementAt(i);
									c.setDonneesTaxonomieObjElement(i,newName1);

								}else if(c.getTypeGen().equals("PERCENTAGE")){
									String newName1=newName.toUpperCase()+" : "+expression[1]+" : "+expression[2];
									//System.out.println("newName1P : "+newName1);
									c.getDonneesTaxonomieObj().removeElementAt(i);
									c.setDonneesTaxonomieObjElement(i,newName1);

								}
								thisTable.setRowName(row, newName);
								break;
							}else{
								Vector<String> elts=new Vector<String>();//contient les attributs(termes) du noeud
								int pos=1;
								//On traite l'expression de l'attribut généralisé
								while (expression[1].charAt(pos) == ' ')
									pos++;
								String deb=expression[1].substring(pos, pos+10);
								if(deb.compareToIgnoreCase("PERCENTAGE")==0){
									//System.out.println("per");
									pos+=11;
									//System.out.println("expression[1].length(): "+expression[1].length());
									while (pos < expression[1].length()-1){
										while (expression[1].charAt(pos) == ' '|| expression[1].substring(pos, pos+1).compareToIgnoreCase("]")==0
												|| expression[1].substring(pos, pos+1).compareToIgnoreCase("[")==0)
											pos++;
										elts.add(expression[1].substring(pos, pos+1));
										pos++;
									}
									String expr1=" PERCENTAGE";//nvl expression[1]
									for(int j=0;j<elts.size();j++){
										if(c.getObjectAt(row).equals(elts.elementAt(j))){
											elts.removeElementAt(j);
											elts.add(j, newName);
											trouve=1;
										}
										expr1+=" ["+elts.elementAt(j)+"]";
									}
									String newName1=expression[0]+" : "+expr1+" : "+expression[2];
									c.getDonneesTaxonomieObj().removeElementAt(i);
									c.setDonneesTaxonomieObjElement(i,newName1);
									thisTable.setRowName(row, newName);
									break;
								}else{
									String op=expression[1].substring(pos+4, pos+6);//op=OR
									String op1=expression[1].substring(pos+4, pos+7);//op=AND
									String expr=expression[1].substring(1);//expression sans le 1er espace au debut de la chaine
									String[] ensElts=new String[expr.length()];
									ensElts=expr.split(" OR ");
									if(op.equals("OR"))	{
										//System.out.println("or");
										for(int l=0;l<ensElts.length;l++){
											if(ensElts[l]==null) break;//sil ya plus dattributs afin deviter de parcourir tout le le reste du tableau vide
											elts.add(ensElts[l].substring(1, ensElts[l].length()-1));
										}
										String expr1 ="";
										//System.out.println("expr1OR0 : "+expr1);
										for(int j=0;j<elts.size();j++){
											if(c.getObjectAt(row).equals(elts.elementAt(j))){
												elts.removeElementAt(j);
												elts.add(j, newName);
												trouve=1;
											}
											if(j==elts.size()-1){
												expr1+="["+elts.elementAt(j)+"]";
												break;
											}else{
												expr1+="["+elts.elementAt(j)+"] OR ";
											}
										}
										if(trouve==1){
											//System.out.println("expr1OR : "+expr1);
											String newName1=expression[0]+" :  "+expr1;
											c.getDonneesTaxonomieObj().removeElementAt(i);
											c.setDonneesTaxonomieObjElement(i,newName1);
										}

									}else if(op1.equals("AND")){
										//System.out.println("and");
										ensElts=expr.split(" AND ");
										for(int l=0;l<ensElts.length;l++){
											if(ensElts[l]==null) break;//sil ya plus dattributs afin deviter de parcourir tout le le reste du tableau vide
											elts.add(ensElts[l].substring(1, ensElts[l].length()-1));
										}
										String expr1 = "";
										for(int j=0;j<elts.size();j++){
											if(c.getObjectAt(row).equals(elts.elementAt(j))){
												elts.removeElementAt(j);
												elts.add(j, newName);
												trouve=1;
											}
											if(j==elts.size()-1){
												expr1+="["+elts.elementAt(j)+"]";
												break;
											}else{
												expr1+="["+elts.elementAt(j)+"] AND ";
											}
										}
										if(trouve==1){
											//System.out.println("expr1AND : "+expr1);
											String newName1=expression[0]+" :  "+expr1;
											c.getDonneesTaxonomieObj().removeElementAt(i);
											c.setDonneesTaxonomieObjElement(i,newName1);
										}
									}
								}
								thisTable.setRowName(row, newName);
								break;
							}
						}
						if(trouve==0){
							thisTable.setRowName(row, newName);
							return;
						}

					}
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			setRowSelectionAllowed(true);
			drag = true;
			dragStartIdx = rowAtPoint(e.getPoint());
			draggedRow = dragStartIdx;

			if (dragStartIdx < 0)
				drag = false;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			setRowSelectionAllowed(false);
			drag = false;
			dragStartIdx = -1;
			draggedRow = -1;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int endIdx = rowAtPoint(e.getPoint());
			if (drag && dragStartIdx != endIdx) {
				moveRow(dragStartIdx, endIdx);
				dragStartIdx = endIdx;
				draggedRow = dragStartIdx;
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	}

	private String getBinaryTableText(int textId) {
		if (textId == NEW_ATT_NAME) {
			return GUIMessages.getString("GUI.enterNewAttributeName")+" : "; //$NON-NLS-1$ //$NON-NLS-2$
		}

		else if (textId == NEW_OBJ_NAME) {
			return GUIMessages.getString("GUI.enterNewObjectName")+" : "; //$NON-NLS-1$ //$NON-NLS-2$
		}

		else if (textId == MODIFY) {
			return GUIMessages.getString("GUI.modify"); //$NON-NLS-1$
		} else
			return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		if ((e.getKeyCode() == KeyEvent.VK_ENTER) || (e.getKeyCode() == KeyEvent.VK_SPACE)) {
			int col = getSelectedColumn();
			int row = getSelectedRow();
			Context ctx=((ContextTableModel) getModel()).getContext();
			if (getValueAt(row, convertColumnIndexToModel(col)).equals("X")) {//$NON-NLS-1$
				getModel().setValueAt("", row, convertColumnIndexToModel(col)); //$NON-NLS-1$
				//-->LINDA
				//mettre a jour si existe attributs ou objtes créés par taxonomie
				if(!ctx.getTypeTax().equals("rien")){
					update();//si y'a pas de type de taxonomie
				}
			}else{
				getModel().setValueAt("X", row, convertColumnIndexToModel(col)); //$NON-NLS-1$
				//-->LINDA
				//mettre a jour si existe attributs ou objtes créés par taxonomie
				if(!ctx.getTypeTax().equals("rien")){//si y'a pas de type de taxonomie
					update();
				}
			}
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Met a jour les attributs ou objets généralisés
	 * @author Linda Bogni
	 */
	public void update(){
		Context ctx=((ContextTableModel) getModel()).getContext();//recuperer le context courant
		if(!ctx.getDonneesTaxonomieAtt().isEmpty()){
			for(int i=0;i<ctx.getDonneesTaxonomieAttCount();i++){
				//récupérer l'attribut généralisé et son expression
				String[] expression=new String[3];
				expression=ctx.getDonneesTaxonomieAttElement(i).split(" : ");
				Vector<String> elts=new Vector<String>();//contient les elements attributs de l'attribut principal
				if(expression[1].length()>4){
					int pos=1;
					//On traite l'expression de l'attribut généralisé
					while (expression[1].charAt(pos) == ' ')
						pos++;
					String deb=expression[1].substring(pos, pos+10);
					//System.out.println("per maj : "+deb);
					if(deb.compareToIgnoreCase("PERCENTAGE")==0){
						String percentage=expression[2];
						pos+=11;
						//System.out.println("expression[1].length(): "+expression[1].length());
						while (pos < expression[1].length()-1){
							while (expression[1].charAt(pos) == ' '|| expression[1].substring(pos, pos+1).compareToIgnoreCase("]")==0
									|| expression[1].substring(pos, pos+1).compareToIgnoreCase("[")==0)
								pos++;
							elts.add(expression[1].substring(pos, pos+1));
							pos++;
						}
						for(int k=0;k<ctx.getObjectCount();k++){
							double res=0.0;
							for(int j=0;j<elts.size();j++){
								if(ctx.getValueAt(k, ctx.getAttributeIndex(elts.elementAt(j))).equals("")){
									res+=0;
								}else{
									res+=1;
								}
							}
							res*=100;
							if((res/elts.size())>=Double.parseDouble(percentage)){
								getModel().setValueAt("X", k, ctx.getAttributeIndex(expression[0]));
							}else{
								getModel().setValueAt("", k, ctx.getAttributeIndex(expression[0]));
							}
						}
					}else{
						String op=expression[1].substring(pos+4, pos+6);//op=OR
						String op1=expression[1].substring(pos+4, pos+7);//op=AND
						String expr=expression[1].substring(1);//expression sans le 1er espace au debut de la chaine
						String[] ensElts=new String[expr.length()];
						ensElts=expr.split(" OR ");
						if(op.equals("OR"))	{
							for(int l=0;l<ensElts.length;l++){
								if(ensElts[l]==null) break;//sil ya plus dattributs afin deviter de parcourir tout le le reste du tableau vide
								elts.add(ensElts[l].substring(1, ensElts[l].length()-1));
							}

							for(int k=0;k<ctx.getObjectCount();k++){
								for(int j=0;j<elts.size();j++){
									//System.out.println("indexAtt : "+ctx.getAttributeIndex(elts.elementAt(j)));
									if(getModel().getValueAt(k, ctx.getAttributeIndex(elts.elementAt(j))).equals("X")){
										//Si on trouve une croix, on met une croix au resultat et on sort de la boucle
										getModel().setValueAt("X", k, ctx.getAttributeIndex(expression[0]));
										break;
									}
									getModel().setValueAt("", k, ctx.getAttributeIndex(expression[0]));
								}
							}
						}else if(op1.equals("AND")){//AND
							op=expression[1].substring(pos+4, pos+7);//op=AND
							ensElts=expr.split(" AND ");
							for(int l=0;l<ensElts.length;l++){
								if(ensElts[l]==null) break;//sil ya plus dattributs afin deviter de parcourir tout le le reste du tableau vide
								elts.add(ensElts[l].substring(1, ensElts[l].length()-1));
							}
							for(int k=0;k<ctx.getObjectCount();k++){
								for(int j=0;j<elts.size();j++){
									//System.out.println("change0 : "+elts.elementAt(j));
									//System.out.println("change1 : "+ctx.getAttributeIndex(elts.elementAt(j)));
									if(getModel().getValueAt(k, ctx.getAttributeIndex(elts.elementAt(j))).equals("")){
										//Si on trouve un vide, on met un vide au resultat et on sort de la boucle
										getModel().setValueAt("", k, ctx.getAttributeIndex(expression[0]));
										break;
									}
									//System.out.println("epr[0] : "+expression[0]);
									getModel().setValueAt("X", k, ctx.getAttributeIndex(expression[0]));
								}

							}
						}
					}
				}else{
					elts.add(expression[1].substring(2, expression[1].length()-1));
					for(int k=0;k<ctx.getObjectCount();k++){
						for(int j=0;j<elts.size();j++){
							//System.out.println("change0 : "+elts.elementAt(j));
							//System.out.println("change1 : "+ctx.getAttributeIndex(elts.elementAt(j)));
							if(getModel().getValueAt(k, ctx.getAttributeIndex(elts.elementAt(j))).equals("")){
								//Si on trouve un vide, on met un vide au resultat et on sort de la boucle
								getModel().setValueAt("", k, ctx.getAttributeIndex(expression[0]));
								break;
							}
							//System.out.println("epr[0] : "+expression[0]);
							getModel().setValueAt("X", k, ctx.getAttributeIndex(expression[0]));
						}

					}
				}
			}
		}
		if(!ctx.getDonneesTaxonomieObj().isEmpty()){
			for(int i=0;i<ctx.getDonneesTaxonomieObjCount();i++){
				//récupérer l'objet généralisé et son expression
				String[] expression=new String[3];
				expression=ctx.getDonneesTaxonomieObjElement(i).split(" : ");

				Vector<String> elts=new Vector<String>();
				if(expression[1].length()>4){
					int pos=1;
					//On traite l'expression de l'attribut généralisé
					while (expression[1].charAt(pos) == ' ')
						pos++;
					String deb=expression[1].substring(pos, pos+10);
					if(deb.compareToIgnoreCase("PERCENTAGE")==0){
						String percentage=expression[2];
						pos+=11;
						//System.out.println("expression[1].length(): "+expression[1].length());
						while (pos < expression[1].length()-1){
							while (expression[1].charAt(pos) == ' '|| expression[1].substring(pos, pos+1).compareToIgnoreCase("]")==0
									|| expression[1].substring(pos, pos+1).compareToIgnoreCase("[")==0)
								pos++;
							elts.add(expression[1].substring(pos, pos+1));
							pos++;
						}
						for(int k=0;k<ctx.getAttributeCount();k++){
							double res=0.0;
							for(int j=0;j<elts.size();j++){
								if(ctx.getValueAt(ctx.getObjectIndex(elts.elementAt(j)),k).equals("")){
									res+=0;
								}else{
									res+=1;
								}
							}
							res*=100;
							if((res/elts.size())>=Double.parseDouble(percentage)){
								getModel().setValueAt("X", ctx.getObjectIndex(expression[0]), k);
							}else{
								getModel().setValueAt("", ctx.getObjectIndex(expression[0]), k);
							}
						}
					}else{
						String op=expression[1].substring(pos+4, pos+6);//op=OR
						String expr=expression[1].substring(1);//expression sans le 1er espace au debut de la chaine
						String[] ensElts=new String[expr.length()];
						ensElts=expr.split(" OR ");
						if(op.equals("OR"))	{
							for(int l=0;l<ensElts.length;l++){
								if(ensElts[l]==null) break;//sil ya plus dattributs afin deviter de parcourir tout le le reste du tableau vide
								elts.add(ensElts[l].substring(1, ensElts[l].length()-1));
							}

							for(int k=0;k<ctx.getAttributeCount();k++){
								for(int j=0;j<elts.size();j++){
									if(getModel().getValueAt(ctx.getObjectIndex(elts.elementAt(j)), k).equals("X")){
										getModel().setValueAt("X", ctx.getObjectIndex(expression[0]), k);
										break;
									}
									getModel().setValueAt("", ctx.getObjectIndex(expression[0]), k);
								}
							}
						}else{
							op=expression[1].substring(pos+4, pos+7);//op=AND
							ensElts=expr.split(" AND ");
							for(int l=0;l<ensElts.length;l++){
								if(ensElts[l]==null) break;//sil ya plus dattributs afin deviter de parcourir tout le le reste du tableau vide
								elts.add(ensElts[l].substring(1, ensElts[l].length()-1));
							}
							for(int k=0;k<ctx.getAttributeCount();k++){
								for(int j=0;j<elts.size();j++){
									if(getModel().getValueAt(ctx.getObjectIndex(elts.elementAt(j)), k).equals("")){
										getModel().setValueAt("", ctx.getObjectIndex(expression[0]), k);
										break;
									}
									//System.out.println("epr[0] : "+expression[0]);
									getModel().setValueAt("X", ctx.getObjectIndex(expression[0]), k);
								}

							}
						}
					}
				}else{
					elts.add(expression[1].substring(2, expression[1].length()-1));
					for(int k=0;k<ctx.getAttributeCount();k++){
						for(int j=0;j<elts.size();j++){
							if(getModel().getValueAt(ctx.getObjectIndex(elts.elementAt(j)), k).equals("")){
								getModel().setValueAt("", ctx.getObjectIndex(expression[0]), k);
								break;
							}
							//System.out.println("epr[0] : "+expression[0]);
							getModel().setValueAt("X", ctx.getObjectIndex(expression[0]), k);
						}

					}
				}
			}
		}
	}

}


