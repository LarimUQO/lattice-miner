package fca.gui.lattice.tool;

import java.util.Vector;

import fca.core.context.Context;
import fca.core.context.binary.*;
import fca.core.lattice.ConceptLattice;
import fca.core.lattice.DataCel;
import fca.core.lattice.FormalConcept;
import fca.core.lattice.NestedLattice;
import fca.core.lattice.operator.search.ApproximationSimple;
import fca.core.lattice.operator.search.SearchApproximateExtentSimple;
import fca.core.lattice.operator.search.SearchApproximateIntentSimple;
import fca.core.util.BasicSet;
import fca.core.util.Triple;
import fca.exception.LatticeMinerException;
import fca.gui.lattice.LatticePanel;
import fca.gui.lattice.element.GraphicalConcept;
import fca.gui.lattice.element.GraphicalLattice;
import fca.gui.lattice.element.GraphicalLatticeElement;
import fca.gui.lattice.element.LatticeStructure;
import fca.gui.util.DialogBox;
import fca.gui.util.constant.LMHistory;
import fca.gui.util.constant.LMColors.LatticeColor;
import fca.messages.GUIMessages;

/**
 * Panneau de recherche pour les treillis simples
 * @author Ludovic Thomas, Bennis Aicha
 * @version 1.0
 */
public class SearchSimple extends Search {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur
	 * @param l Le {@link GraphicalLattice} pour lequel ce panneau affiche les attributs et objets
	 * @param lp Le {@link LatticePanel} dans lequel est affiché le treillis
	 */
	public SearchSimple(GraphicalLattice l, LatticePanel lp) {
		super(l, lp);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelSearch#exactMatchAction()
	 */
	@Override
	public void exactMatchAction() {
		super.exactMatchAction();
		openResultButton.setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelAttObjSearch#approximateMatch()
	 */
	@Override
	public GraphicalLattice approximateMatch() throws LatticeMinerException {

		GraphicalLattice res = (GraphicalLattice) lattice.clone();

		ApproximationSimple approximHaute = new ApproximationSimple(res.getNestedLattice().getConceptLattice());
		DataCel dataCel = new DataCel(getSelectedExtent(), getSelectedIntent());
		Triple<ConceptLattice, ConceptLattice, ConceptLattice> latticeL1L2UI = approximHaute.perform(dataCel);

		// Reinitialise le treillis sans aucune selection
		res.setOutOfFocus(true);

		// Affiche UI et UExt, UInt s'il y a une intersection
		if (latticeL1L2UI.getThird() != null) {
			GraphicalConcept conceptUExt = res.getNestedNodeByIntent(latticeL1L2UI.getThird().getBottomConcept().getIntent());
			Vector<GraphicalLatticeElement> filterUExt = conceptUExt.getFilter();
			GraphicalConcept conceptUInt = res.getNestedNodeByIntent(latticeL1L2UI.getThird().getTopConcept().getIntent());
			Vector<GraphicalLatticeElement> idealUInt = conceptUInt.getIdeal();

			// Affiche UI
			Vector<GraphicalLatticeElement> intersection = idealUInt;
			intersection.retainAll(filterUExt);
			intersection.add(conceptUExt);
			intersection.add(conceptUInt);

			res.showSubLattice(intersection, LatticeColor.ORANGE);

			// Affiche UExt et UInt
			conceptUExt.setColor(LatticeColor.PINK);
			conceptUInt.setColor(LatticeColor.PINK);

			// Construction du nouveau treillis graphique résultant de l'approximation
			ConceptLattice latticeUI = new ConceptLattice(latticeL1L2UI.getThird().getContext());
			LatticeStructure structUI = new LatticeStructure(latticeUI, latticeUI.getContext(), LatticeStructure.BEST);
			resultOperation = new GraphicalLattice(latticeUI, structUI);

			openResultButton.setEnabled(true);
		} else {
			openResultButton.setEnabled(false);
			resultOperation = null;
			res = null;
		}

		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelAttObjSearch#approximateMatchAction()
	 */
	@Override
	public void approximateMatchAction() throws LatticeMinerException {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalLattice displayedLattice = approximateMatch();

			if (displayedLattice != null) {
				displayedLattice.setEditable(false);
				viewer.changeDisplayedLattice(displayedLattice, LMHistory.APPROXIMATION);

				viewer.lockHistory();
				viewer.getFrame().getTreePanel().selectPathNode(displayedLattice.getTopNode());
				viewer.unlockHistory();

				displayedLattice.setEditable(true);

			} else {
				viewer.setSelectedNodes(new Vector<GraphicalConcept>());
				viewer.getRootLattice().setOutOfFocus(true);
				viewer.repaint();
				DialogBox.showMessageInformation(viewer, NO_RESULT, GUIMessages.getString("GUI.resultForSearch")); //$NON-NLS-1$
			}
		}
	}



	/**
	 *
	 * @return
	 * @throws LatticeMinerException
	 */
	public GraphicalLattice typeNConcept() throws LatticeMinerException {


		GraphicalLattice res = (GraphicalLattice) lattice.clone();
		NestedLattice res2=res.getNestedLattice();
		BinaryContext ctx=res2.getGlobalContext();
		ApproximationSimple ap = new ApproximationSimple(res.getNestedLattice().getConceptLattice());

		BasicSet xpr=ap.CalculXPrime(getSelectedExtent(), ctx);
		BasicSet xsec=ap.CalculXseconde(getSelectedExtent(), ctx);
		BasicSet ypr=ap.CalculYPrime(getSelectedIntent(), ctx);
		BasicSet ysec=ap.CalculYseconde(getSelectedIntent(), ctx);
		DataCel celres = new DataCel(xsec, xpr);
		DataCel celres2 = new DataCel(ypr, ysec);
		GraphicalConcept conceptres = res.getNestedNodeByCellule(celres);
		GraphicalConcept conceptres2 = res.getNestedNodeByCellule(celres2);

		conceptres.setColor(LatticeColor.GREEN);
		conceptres2.setColor(LatticeColor.GREEN);


		return res;

	}




	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelAttObjSearch#typeAction()
	 */
	public void typeNConceptAction() throws LatticeMinerException {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalLattice displayedLattice = typeNConcept();

			if (displayedLattice != null) {
				displayedLattice.setEditable(false);
				viewer.changeDisplayedLattice(displayedLattice, LMHistory.APPROXIMATION);

				viewer.lockHistory();
				viewer.getFrame().getTreePanel().selectPathNode(displayedLattice.getTopNode());
				viewer.unlockHistory();

				displayedLattice.setEditable(true);

			} else {
				viewer.setSelectedNodes(new Vector<GraphicalConcept>());
				viewer.getRootLattice().setOutOfFocus(true);
				viewer.repaint();
				DialogBox.showMessageInformation(viewer, NO_RESULT, GUIMessages.getString("GUI.resultForSearch")); //$NON-NLS-1$
			}
		}
	}



	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelAttObjSearch#Coupledecomposition2()
	 */

	public GraphicalLattice Coupledecomposition2() throws LatticeMinerException {

		GraphicalLattice res = (GraphicalLattice) lattice.clone();
		NestedLattice res2=res.getNestedLattice();
		BinaryContext ctx=res2.getGlobalContext();
		ApproximationSimple approximAlpha = new ApproximationSimple(res.getNestedLattice().getConceptLattice());
		DataCel dataCel = new DataCel(getSelectedExtent(), getSelectedIntent());

		// Reinitialise le treillis sans aucune selection
		res.setOutOfFocus(true);

		//affichage de la cellule
		GraphicalConcept concept = res.getNestedNodeByCellule(dataCel);


		//Cas ou la cellule appartient au treillis
		if(res.verifyCellule(dataCel)==true){
			concept.setColor(LatticeColor.YELLOW);
		}

		//cas ou la cellule n'appartient pas au treillis
		else{
			//cas ou l'extension de alpha1 erst nulle
			if(approximAlpha.Alpha1(dataCel,ctx)==null || approximAlpha.Alpha2(dataCel,ctx)==null || approximAlpha.Alpha3(dataCel,ctx)==null){
				if(approximAlpha.Alpha1(dataCel,ctx)==null && approximAlpha.Alpha2(dataCel,ctx)!=null && approximAlpha.Alpha3(dataCel,ctx)!=null)
				{
					DataCel cel2 = approximAlpha.Alpha2(dataCel,ctx);
					DataCel cel3 = approximAlpha.Alpha3(dataCel,ctx);
					//DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
					// Affiche alpha 2,3
					GraphicalConcept conceptalpha2 = res.getNestedNodeByCellule(cel2);
					GraphicalConcept conceptalpha3 = res.getNestedNodeByCellule(cel3);

					conceptalpha2.setOutOfFocus(false);
					conceptalpha3.setOutOfFocus(false);
					conceptalpha2.setColor(LatticeColor.PINK);
					conceptalpha3.setColor(LatticeColor.ORANGE);
				}else{
					if(approximAlpha.Alpha2(dataCel,ctx)==null && approximAlpha.Alpha1(dataCel,ctx)!=null && approximAlpha.Alpha3(dataCel,ctx)!=null)
					{
						DataCel cel = approximAlpha.Alpha1(dataCel,ctx);
						DataCel cel3 = approximAlpha.Alpha3(dataCel,ctx);
						// Affiche alpha1,2,3
						GraphicalConcept conceptalpha1 = res.getNestedNodeByCellule(cel);
						GraphicalConcept conceptalpha3 = res.getNestedNodeByCellule(cel3);
						// Affiche alpha1
						conceptalpha1.setHighlighted(true);
						conceptalpha1.setOutOfFocus(false);
						conceptalpha3.setOutOfFocus(false);
						conceptalpha1.setColor(LatticeColor.YELLOW);
						conceptalpha3.setColor(LatticeColor.ORANGE);
					}else{
						if(approximAlpha.Alpha3(dataCel,ctx)==null && approximAlpha.Alpha1(dataCel,ctx)!=null && approximAlpha.Alpha2(dataCel,ctx)!=null){
							DataCel cel = approximAlpha.Alpha1(dataCel,ctx);
							DataCel cel2 = approximAlpha.Alpha2(dataCel,ctx);

							// Affiche alpha1,2,3
							GraphicalConcept conceptalpha1 = res.getNestedNodeByCellule(cel);



							GraphicalConcept conceptalpha2 = res.getNestedNodeByCellule(cel2);

							// Affiche alpha1
							conceptalpha1.setHighlighted(true);
							conceptalpha1.setOutOfFocus(false);
							conceptalpha2.setOutOfFocus(false);

							conceptalpha1.setColor(LatticeColor.YELLOW);
							conceptalpha2.setColor(LatticeColor.PINK);

						}else{
							if(approximAlpha.Alpha1(dataCel,ctx)==null && approximAlpha.Alpha2(dataCel,ctx)==null )
							{

								DataCel cel3 = approximAlpha.Alpha3(dataCel,ctx);
								// Affiche alpha1,2,3

								GraphicalConcept conceptalpha3 = res.getNestedNodeByCellule(cel3);
								// Affiche alpha1

								conceptalpha3.setOutOfFocus(false);
								conceptalpha3.setColor(LatticeColor.ORANGE);
							}else{
								if(approximAlpha.Alpha1(dataCel,ctx)==null && approximAlpha.Alpha3(dataCel,ctx)==null){

									DataCel cel2 = approximAlpha.Alpha2(dataCel,ctx);

									// Affiche alpha1,2,3

									GraphicalConcept conceptalpha2 = res.getNestedNodeByCellule(cel2);

									// Affiche alpha1

									conceptalpha2.setOutOfFocus(false);

									conceptalpha2.setColor(LatticeColor.PINK);

								}else{
									if(approximAlpha.Alpha2(dataCel,ctx)==null && approximAlpha.Alpha3(dataCel,ctx)==null){
										DataCel cel = approximAlpha.Alpha1(dataCel,ctx);

										// Affiche alpha1,2,3
										GraphicalConcept conceptalpha1 = res.getNestedNodeByCellule(cel);

										// Affiche alpha1
										conceptalpha1.setHighlighted(true);
										conceptalpha1.setOutOfFocus(false);
										conceptalpha1.setColor(LatticeColor.YELLOW);

									}
								}
							}
						}
					}
				}
			}else{
				DataCel cel = approximAlpha.Alpha1(dataCel,ctx);
				DataCel cel2 = approximAlpha.Alpha2(dataCel,ctx);
				DataCel cel3 = approximAlpha.Alpha3(dataCel,ctx);
				// Affiche alpha1,2,3
				GraphicalConcept conceptalpha1 = res.getNestedNodeByCellule(cel);
				GraphicalConcept conceptalpha2 = res.getNestedNodeByCellule(cel2);
				GraphicalConcept conceptalpha3 = res.getNestedNodeByCellule(cel3);
				// Affiche alpha1
				conceptalpha1.setHighlighted(true);
				conceptalpha1.setOutOfFocus(false);
				conceptalpha2.setOutOfFocus(false);
				conceptalpha3.setOutOfFocus(false);
				conceptalpha1.setColor(LatticeColor.YELLOW);
				conceptalpha2.setColor(LatticeColor.PINK);
				conceptalpha3.setColor(LatticeColor.ORANGE);
			}
		}


		//cas ou l'extension de beta1 est nulle
		if(approximAlpha.Beta1(dataCel,ctx)==null || approximAlpha.Beta2(dataCel,ctx)==null || approximAlpha.Beta3(dataCel,ctx)==null){
			if(approximAlpha.Beta1(dataCel,ctx)==null && approximAlpha.Beta2(dataCel,ctx)!=null && approximAlpha.Beta3(dataCel,ctx)!=null)
			{
				DataCel cel2 = approximAlpha.Beta2(dataCel,ctx);
				DataCel cel3 = approximAlpha.Beta3(dataCel,ctx);
				DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
				// Affiche beta 2,3
				GraphicalConcept conceptbeta2 = res.getNestedNodeByCellule(cel2);
				GraphicalConcept conceptbeta3 = res.getNestedNodeByCellule(cel3);

				conceptbeta2.setOutOfFocus(false);
				conceptbeta3.setOutOfFocus(false);
				conceptbeta2.setColor(LatticeColor.PINK);
				conceptbeta3.setColor(LatticeColor.ORANGE);
			}else{
				if(approximAlpha.Beta2(dataCel,ctx)==null && approximAlpha.Beta1(dataCel,ctx)!=null && approximAlpha.Beta3(dataCel,ctx)!=null)
				{
					DataCel cel = approximAlpha.Beta1(dataCel,ctx);
					DataCel cel3 = approximAlpha.Beta3(dataCel,ctx);
					// Affiche beta1,2,3
					GraphicalConcept conceptbeta1 = res.getNestedNodeByCellule(cel);
					GraphicalConcept conceptbeta3 = res.getNestedNodeByCellule(cel3);
					// Affiche beta1
					conceptbeta1.setHighlighted(true);
					conceptbeta1.setOutOfFocus(false);
					conceptbeta3.setOutOfFocus(false);
					conceptbeta1.setColor(LatticeColor.GREEN);
					conceptbeta3.setColor(LatticeColor.ORANGE);
				}else{
					if(approximAlpha.Beta3(dataCel,ctx)==null && approximAlpha.Beta1(dataCel,ctx)!=null && approximAlpha.Beta2(dataCel,ctx)!=null){
						DataCel cel = approximAlpha.Beta1(dataCel,ctx);
						DataCel cel2 = approximAlpha.Beta2(dataCel,ctx);

						// Affiche beta1,2,3
						GraphicalConcept conceptbeta1 = res.getNestedNodeByCellule(cel);
						GraphicalConcept conceptbeta2 = res.getNestedNodeByCellule(cel2);

						// Affiche beta1
						conceptbeta1.setHighlighted(true);
						conceptbeta1.setOutOfFocus(false);
						conceptbeta2.setOutOfFocus(false);

						conceptbeta1.setColor(LatticeColor.GREEN);
						conceptbeta2.setColor(LatticeColor.PINK);

					}else{
						if(approximAlpha.Beta1(dataCel,ctx)==null && approximAlpha.Beta2(dataCel,ctx)==null )
						{

							DataCel cel3 = approximAlpha.Beta3(dataCel,ctx);
							// Affiche beta 3

							GraphicalConcept conceptbeta3 = res.getNestedNodeByCellule(cel3);
							// Affiche beta3

							conceptbeta3.setOutOfFocus(false);
							conceptbeta3.setColor(LatticeColor.ORANGE);
						}else{
							if(approximAlpha.Beta1(dataCel,ctx)==null && approximAlpha.Beta3(dataCel,ctx)==null){

								DataCel cel2 = approximAlpha.Beta2(dataCel,ctx);

								// Affiche beta 2

								GraphicalConcept conceptbeta2 = res.getNestedNodeByCellule(cel2);

								// Affiche beta2

								conceptbeta2.setOutOfFocus(false);

								conceptbeta2.setColor(LatticeColor.PINK);

							}else{
								if(approximAlpha.Beta2(dataCel,ctx)==null && approximAlpha.Beta3(dataCel,ctx)==null){
									DataCel cel = approximAlpha.Beta1(dataCel,ctx);

									// Affiche beta 1
									GraphicalConcept conceptbeta1 = res.getNestedNodeByCellule(cel);

									// Affiche beta1
									conceptbeta1.setHighlighted(true);
									conceptbeta1.setOutOfFocus(false);
									conceptbeta1.setColor(LatticeColor.GREEN);

								}
							}
						}
					}
				}
			}
		}else{
			DataCel cel = approximAlpha.Beta1(dataCel,ctx);
			DataCel cel2 = approximAlpha.Beta2(dataCel,ctx);
			DataCel cel3 = approximAlpha.Beta3(dataCel,ctx);
			// Affiche beta1,2,3
			GraphicalConcept conceptbeta1 = res.getNestedNodeByCellule(cel);
			GraphicalConcept conceptbeta2 = res.getNestedNodeByCellule(cel2);
			GraphicalConcept conceptbeta3 = res.getNestedNodeByCellule(cel3);

			conceptbeta1.setHighlighted(true);
			conceptbeta1.setOutOfFocus(false);
			conceptbeta2.setOutOfFocus(false);
			conceptbeta3.setOutOfFocus(false);
			conceptbeta1.setColor(LatticeColor.GREEN);
			conceptbeta2.setColor(LatticeColor.PINK);
			conceptbeta3.setColor(LatticeColor.ORANGE);
		}

		openResultButton.setEnabled(true);


		return res;
	}


/*
 * (non-Javadoc)
 * @see fca.gui.lattice.PanelAttObjSearch#CoupledecompositionAction()
 */

	public void Coupledecomposition2Action() throws LatticeMinerException {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalLattice displayedLattice = Coupledecomposition2();

			if (displayedLattice != null) {
				displayedLattice.setEditable(false);
				viewer.changeDisplayedLattice(displayedLattice, LMHistory.APPROXIMATION);

				viewer.lockHistory();
				viewer.getFrame().getTreePanel().selectPathNode(displayedLattice.getTopNode());
				viewer.unlockHistory();

				displayedLattice.setEditable(true);

			} else {
				viewer.setSelectedNodes(new Vector<GraphicalConcept>());
				viewer.getRootLattice().setOutOfFocus(true);
				viewer.repaint();
				DialogBox.showMessageInformation(viewer, NO_RESULT, GUIMessages.getString("GUI.resultForSearch")); //$NON-NLS-1$
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelAttObjSearch#CoupledecompositionAction()
	 */
	@Override
	public void CoupledecompositionAction() throws LatticeMinerException {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalLattice displayedLattice = Coupledecomposition();

			if (displayedLattice != null) {
				displayedLattice.setEditable(false);
				viewer.changeDisplayedLattice(displayedLattice, LMHistory.APPROXIMATION);

				viewer.lockHistory();
				viewer.getFrame().getTreePanel().selectPathNode(displayedLattice.getTopNode());
				viewer.unlockHistory();

				displayedLattice.setEditable(true);

			} else {
				viewer.setSelectedNodes(new Vector<GraphicalConcept>());
				viewer.getRootLattice().setOutOfFocus(true);
				viewer.repaint();
				DialogBox.showMessageInformation(viewer, NO_RESULT, GUIMessages.getString("GUI.resultForSearch")); //$NON-NLS-1$
			}
		}
	}





/*
 * (non-Javadoc)
 * @see fca.gui.lattice.PanelAttObjSearch#Coupledecomposition()
 */

	public GraphicalLattice Coupledecomposition() throws LatticeMinerException {

		GraphicalLattice res = (GraphicalLattice) lattice.clone();
		NestedLattice res2=res.getNestedLattice();
		BinaryContext ctx=res2.getGlobalContext();
		ApproximationSimple approximAlpha = new ApproximationSimple(res.getNestedLattice().getConceptLattice());
		DataCel dataCel = new DataCel(getSelectedExtent(), getSelectedIntent());

		// Reinitialise le treillis sans aucune selection
		res.setOutOfFocus(true);

		//affichage de la cellule
		GraphicalConcept concept = res.getNestedNodeByCellule(dataCel);


		//Cas ou la cellule appartient au treillis
		if(res.verifyCellule(dataCel)==true){
			concept.setColor(LatticeColor.YELLOW);
		}

		//cas ou la cellule n'appartient pas au treillis
		else{
			//cas ou l'extension de alpha1 erst nulle
			if(approximAlpha.Alpha1(dataCel,ctx)==null || approximAlpha.Alpha2(dataCel,ctx)==null || approximAlpha.Alpha3(dataCel,ctx)==null){
				if(approximAlpha.Alpha1(dataCel,ctx)==null && approximAlpha.Alpha2(dataCel,ctx)!=null && approximAlpha.Alpha3(dataCel,ctx)!=null)
				{
					DataCel cel2 = approximAlpha.Alpha2(dataCel,ctx);
					DataCel cel3 = approximAlpha.Alpha3(dataCel,ctx);
					//DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
					// Affiche alpha 2,3
					GraphicalConcept conceptalpha2 = res.getNestedNodeByCellule(cel2);
					GraphicalConcept conceptalpha3 = res.getNestedNodeByCellule(cel3);

					conceptalpha2.setOutOfFocus(false);
					conceptalpha3.setOutOfFocus(false);
					conceptalpha2.setColor(LatticeColor.PINK);
					conceptalpha3.setColor(LatticeColor.ORANGE);

				}else{
					if(approximAlpha.Alpha2(dataCel,ctx)==null && approximAlpha.Alpha1(dataCel,ctx)!=null && approximAlpha.Alpha3(dataCel,ctx)!=null)
					{
						DataCel cel = approximAlpha.Alpha1(dataCel,ctx);
						DataCel cel3 = approximAlpha.Alpha3(dataCel,ctx);
						// Affiche alpha1,2,3
						GraphicalConcept conceptalpha1 = res.getNestedNodeByCellule(cel);
						GraphicalConcept conceptalpha3 = res.getNestedNodeByCellule(cel3);
						// Affiche alpha1
						conceptalpha1.setHighlighted(true);
						conceptalpha1.setOutOfFocus(false);
						conceptalpha3.setOutOfFocus(false);
						conceptalpha1.setColor(LatticeColor.YELLOW);
						conceptalpha3.setColor(LatticeColor.ORANGE);
					}else{
						if(approximAlpha.Alpha3(dataCel,ctx)==null && approximAlpha.Alpha1(dataCel,ctx)!=null && approximAlpha.Alpha2(dataCel,ctx)!=null){
							DataCel cel = approximAlpha.Alpha1(dataCel,ctx);
							DataCel cel2 = approximAlpha.Alpha2(dataCel,ctx);

							// Affiche alpha1,2,3
							GraphicalConcept conceptalpha1 = res.getNestedNodeByCellule(cel);



							GraphicalConcept conceptalpha2 = res.getNestedNodeByCellule(cel2);

							// Affiche alpha1
							conceptalpha1.setHighlighted(true);
							conceptalpha1.setOutOfFocus(false);
							conceptalpha2.setOutOfFocus(false);

							conceptalpha1.setColor(LatticeColor.YELLOW);
							conceptalpha2.setColor(LatticeColor.PINK);

						}else{
							if(approximAlpha.Alpha1(dataCel,ctx)==null && approximAlpha.Alpha2(dataCel,ctx)==null )
							{

								DataCel cel3 = approximAlpha.Alpha3(dataCel,ctx);
								// Affiche alpha1,2,3

								GraphicalConcept conceptalpha3 = res.getNestedNodeByCellule(cel3);
								// Affiche alpha1

								conceptalpha3.setOutOfFocus(false);
								conceptalpha3.setColor(LatticeColor.ORANGE);
							}else{
								if(approximAlpha.Alpha1(dataCel,ctx)==null && approximAlpha.Alpha3(dataCel,ctx)==null){

									DataCel cel2 = approximAlpha.Alpha2(dataCel,ctx);

									// Affiche alpha1,2,3

									GraphicalConcept conceptalpha2 = res.getNestedNodeByCellule(cel2);

									// Affiche alpha1

									conceptalpha2.setOutOfFocus(false);

									conceptalpha2.setColor(LatticeColor.PINK);

								}else{
									if(approximAlpha.Alpha2(dataCel,ctx)==null && approximAlpha.Alpha3(dataCel,ctx)==null){
										DataCel cel = approximAlpha.Alpha1(dataCel,ctx);

										// Affiche alpha1,2,3
										GraphicalConcept conceptalpha1 = res.getNestedNodeByCellule(cel);

										// Affiche alpha1
										conceptalpha1.setHighlighted(true);
										conceptalpha1.setOutOfFocus(false);
										conceptalpha1.setColor(LatticeColor.YELLOW);

									}
								}
							}
						}
					}
				}
			}else{
				DataCel cel = approximAlpha.Alpha1(dataCel,ctx);
				DataCel cel2 = approximAlpha.Alpha2(dataCel,ctx);
				DataCel cel3 = approximAlpha.Alpha3(dataCel,ctx);
				// Affiche alpha1,2,3
				GraphicalConcept conceptalpha1 = res.getNestedNodeByCellule(cel);
				GraphicalConcept conceptalpha2 = res.getNestedNodeByCellule(cel2);
				GraphicalConcept conceptalpha3 = res.getNestedNodeByCellule(cel3);
				// Affiche alpha1
				conceptalpha1.setHighlighted(true);
				conceptalpha1.setOutOfFocus(false);
				conceptalpha2.setOutOfFocus(false);
				conceptalpha3.setOutOfFocus(false);
				conceptalpha1.setColor(LatticeColor.YELLOW);
				conceptalpha2.setColor(LatticeColor.PINK);
				conceptalpha3.setColor(LatticeColor.ORANGE);
			}
		}

		openResultButton.setEnabled(true);


		return res;
	}








/*
 * (non-Javadoc)
 * @see fca.gui.lattice.PanelAttObjSearch#Beta()
 */

	public GraphicalLattice Beta() throws LatticeMinerException {

		GraphicalLattice res = (GraphicalLattice) lattice.clone();
		NestedLattice res2=res.getNestedLattice();
		BinaryContext ctx=res2.getGlobalContext();
		ApproximationSimple approximBeta = new ApproximationSimple(res.getNestedLattice().getConceptLattice());
		DataCel dataCel = new DataCel(getSelectedExtent(), getSelectedIntent());

		// Reinitialise le treillis sans aucune selection
		res.setOutOfFocus(true);

		//affichage de la cellule
		GraphicalConcept concept = res.getNestedNodeByCellule(dataCel);


		//Cas ou la cellule appartient au treillis
		if(res.verifyCellule(dataCel)==true){
			concept.setColor(LatticeColor.YELLOW);
		}

		//cas ou la cellule n'appartient pas au treillis
		else{
			//cas ou l'extension de beta1 erst nulle
			if(approximBeta.Beta1(dataCel,ctx)==null || approximBeta.Beta2(dataCel,ctx)==null || approximBeta.Beta3(dataCel,ctx)==null){
				if(approximBeta.Beta1(dataCel,ctx)==null && approximBeta.Beta2(dataCel,ctx)!=null && approximBeta.Beta3(dataCel,ctx)!=null)
				{
					DataCel cel2 = approximBeta.Beta2(dataCel,ctx);
					DataCel cel3 = approximBeta.Beta3(dataCel,ctx);
					DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
					// Affiche beta 2,3
					GraphicalConcept conceptbeta2 = res.getNestedNodeByCellule(cel2);
					GraphicalConcept conceptbeta3 = res.getNestedNodeByCellule(cel3);

					conceptbeta2.setOutOfFocus(false);
					conceptbeta3.setOutOfFocus(false);
					conceptbeta2.setColor(LatticeColor.PINK);
					conceptbeta3.setColor(LatticeColor.ORANGE);
				}else{
					if(approximBeta.Beta2(dataCel,ctx)==null && approximBeta.Beta1(dataCel,ctx)!=null && approximBeta.Beta3(dataCel,ctx)!=null)
					{
						DataCel cel = approximBeta.Beta1(dataCel,ctx);
						DataCel cel3 = approximBeta.Beta3(dataCel,ctx);
						// Affiche beta1,2,3
						GraphicalConcept conceptbeta1 = res.getNestedNodeByCellule(cel);
						GraphicalConcept conceptbeta3 = res.getNestedNodeByCellule(cel3);
						// Affiche beta1
						conceptbeta1.setHighlighted(true);
						conceptbeta1.setOutOfFocus(false);
						conceptbeta3.setOutOfFocus(false);
						conceptbeta1.setColor(LatticeColor.GREEN);
						conceptbeta3.setColor(LatticeColor.ORANGE);
					}else{
						if(approximBeta.Beta3(dataCel,ctx)==null && approximBeta.Beta1(dataCel,ctx)!=null && approximBeta.Beta2(dataCel,ctx)!=null){
							DataCel cel = approximBeta.Beta1(dataCel,ctx);
							DataCel cel2 = approximBeta.Beta2(dataCel,ctx);

							// Affiche beta1,2,3
							GraphicalConcept conceptbeta1 = res.getNestedNodeByCellule(cel);
							GraphicalConcept conceptbeta2 = res.getNestedNodeByCellule(cel2);

							// Affiche beta1
							conceptbeta1.setHighlighted(true);
							conceptbeta1.setOutOfFocus(false);
							conceptbeta2.setOutOfFocus(false);

							conceptbeta1.setColor(LatticeColor.GREEN);
							conceptbeta2.setColor(LatticeColor.PINK);

						}else{
							if(approximBeta.Beta1(dataCel,ctx)==null && approximBeta.Beta2(dataCel,ctx)==null )
							{

								DataCel cel3 = approximBeta.Beta3(dataCel,ctx);
								// Affiche beta 3

								GraphicalConcept conceptbeta3 = res.getNestedNodeByCellule(cel3);
								// Affiche beta3

								conceptbeta3.setOutOfFocus(false);
								conceptbeta3.setColor(LatticeColor.ORANGE);
							}else{
								if(approximBeta.Beta1(dataCel,ctx)==null && approximBeta.Beta3(dataCel,ctx)==null){

									DataCel cel2 = approximBeta.Beta2(dataCel,ctx);

									// Affiche beta 2

									GraphicalConcept conceptbeta2 = res.getNestedNodeByCellule(cel2);

									// Affiche beta2

									conceptbeta2.setOutOfFocus(false);

									conceptbeta2.setColor(LatticeColor.PINK);

								}else{
									if(approximBeta.Beta2(dataCel,ctx)==null && approximBeta.Beta3(dataCel,ctx)==null){
										DataCel cel = approximBeta.Beta1(dataCel,ctx);

										// Affiche beta 1
										GraphicalConcept conceptbeta1 = res.getNestedNodeByCellule(cel);

										// Affiche beta1
										conceptbeta1.setHighlighted(true);
										conceptbeta1.setOutOfFocus(false);
										conceptbeta1.setColor(LatticeColor.GREEN);

									}
								}
							}
						}
					}
				}
			}else{
				DataCel cel = approximBeta.Beta1(dataCel,ctx);
				DataCel cel2 = approximBeta.Beta2(dataCel,ctx);
				DataCel cel3 = approximBeta.Beta3(dataCel,ctx);
				// Affiche beta1,2,3
				GraphicalConcept conceptbeta1 = res.getNestedNodeByCellule(cel);
				GraphicalConcept conceptbeta2 = res.getNestedNodeByCellule(cel2);
				GraphicalConcept conceptbeta3 = res.getNestedNodeByCellule(cel3);

				conceptbeta1.setHighlighted(true);
				conceptbeta1.setOutOfFocus(false);
				conceptbeta2.setOutOfFocus(false);
				conceptbeta3.setOutOfFocus(false);
				conceptbeta1.setColor(LatticeColor.GREEN);
				conceptbeta2.setColor(LatticeColor.PINK);
				conceptbeta3.setColor(LatticeColor.ORANGE);
			}
		}

		openResultButton.setEnabled(true);


		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelAttObjSearch#BetaAction()
	 */
	public void BetaAction() throws LatticeMinerException {
		if (!isSearchOnObjects() && !isSearchOnAttributes()) {
			DialogBox.showMessageInformation(viewer, NO_SEARCH, GUIMessages.getString("GUI.noSearch")); //$NON-NLS-1$
		} else {
			GraphicalLattice displayedLattice = Beta();

			if (displayedLattice != null) {
				displayedLattice.setEditable(false);
				viewer.changeDisplayedLattice(displayedLattice, LMHistory.APPROXIMATION);

				viewer.lockHistory();
				viewer.getFrame().getTreePanel().selectPathNode(displayedLattice.getTopNode());
				viewer.unlockHistory();

				displayedLattice.setEditable(true);

			} else {
				viewer.setSelectedNodes(new Vector<GraphicalConcept>());
				viewer.getRootLattice().setOutOfFocus(true);
				viewer.repaint();
				DialogBox.showMessageInformation(viewer, NO_RESULT, GUIMessages.getString("GUI.resultForSearch")); //$NON-NLS-1$
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelSearch#searchApproximateNodeWithExtent(fca.general.BasicSet)
	 */
	@Override
	protected GraphicalConcept searchApproximateNodeWithExtent(BasicSet extent) {
		SearchApproximateExtentSimple search = new SearchApproximateExtentSimple(
				lattice.getNestedLattice().getConceptLattice());
		FormalConcept formalResult = search.perform(extent);
		return lattice.getGraphicalConcept(formalResult);
	}

	/*
	 * (non-Javadoc)
	 * @see fca.gui.lattice.PanelSearch#searchApproximateNodeWithIntent(fca.general.BasicSet)
	 */
	@Override
	protected GraphicalConcept searchApproximateNodeWithIntent(BasicSet intent) {
		SearchApproximateIntentSimple search = new SearchApproximateIntentSimple(
				lattice.getNestedLattice().getConceptLattice());
		FormalConcept formalResult = search.perform(intent);
		return lattice.getGraphicalConcept(formalResult);
	}

	@Override
	protected DataCel Alpha1(DataCel cel, Context ctx) {
		// TODO Auto-generated method stub
		return null;
	}

}