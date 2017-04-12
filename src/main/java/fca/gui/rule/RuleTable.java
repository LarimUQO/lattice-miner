package fca.gui.rule;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import fca.core.rule.Rule;
import fca.exception.NullPointerException;
import fca.gui.lattice.LatticePanel;
import fca.gui.util.DialogBox;

public class RuleTable extends JTable implements MouseListener {

	private static final long serialVersionUID = -1821418322663586686L;

	private static Color YELLOW = new Color(255, 249, 218);

	JTable thisTable;

	LatticePanel latticePanel;

	public RuleTable(Vector<Rule> rules, LatticePanel latPanel) {
		super(new RuleTableModel(rules));
		latticePanel = latPanel;

		setRuleTable(rules);

	}

	public void setRuleTable(Vector<Rule> rules){
		setModel(new RuleTableModel(rules));


		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(true);
		setShowVerticalLines(false);
		setShowHorizontalLines(true);
		setIntercellSpacing(new Dimension(0, 0));
		getTableHeader().setReorderingAllowed(false);
		addMouseListener(this);

		setForeground(Color.BLUE);
		setBackground(YELLOW);

		TableColumn column = getColumnModel().getColumn(0);
		column.setHeaderRenderer(createHeaderCellRenderer());
		column.setPreferredWidth(55);
		column.setMinWidth(55);
		column.setMaxWidth(55);
		column.setResizable(false);

		column = getColumnModel().getColumn(1);
		column.setHeaderRenderer(createHeaderCellRenderer());
		column.setPreferredWidth(160);
		column.setResizable(true);

		column = getColumnModel().getColumn(2);
		column.setHeaderRenderer(createHeaderCellRenderer());
		column.setPreferredWidth(55);
		column.setMinWidth(55);
		column.setMaxWidth(55);
		column.setResizable(false);

		column = getColumnModel().getColumn(3);
		column.setHeaderRenderer(createHeaderCellRenderer());
		column.setPreferredWidth(160);
		column.setResizable(true);

		column = getColumnModel().getColumn(4);
		column.setHeaderRenderer(createHeaderCellRenderer());
		column.setPreferredWidth(84);
		column.setMinWidth(84);
		column.setMaxWidth(84);
		column.setResizable(false);

		column = getColumnModel().getColumn(5);
		column.setHeaderRenderer(createHeaderCellRenderer());
		column.setPreferredWidth(84);
		column.setMinWidth(84);
		column.setMaxWidth(84);
		column.setResizable(false);

		getTableHeader().addMouseListener(new ColumnHeaderListener());

		thisTable = this;
	}

	public TableCellRenderer createHeaderCellRenderer() {
		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
			/**
			 *
			 */
			private static final long serialVersionUID = 5827838429173805173L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				if (table != null) {
					JTableHeader header = table.getTableHeader();
					if (header != null) {
						setForeground(header.getForeground());
						setBackground(header.getBackground());
						setFont(header.getFont());
					}
				}

				String text = value == null ? "" : value.toString(); //$NON-NLS-1$
				setText(text);
				setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY));
				return this;
			}
		};

		headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		return headerRenderer;
	}

	@Override
	public boolean isRowSelected(int rowIdx) {
		boolean superSelected = super.isRowSelected(rowIdx);
		return superSelected;
	}

	/* ======== MOUSELISTENER INTERFACE ======== */
	public void mouseClicked(MouseEvent e) {
		int col = columnAtPoint(e.getPoint());
		int row = rowAtPoint(e.getPoint());

		if (row > -1 && col > -1) {
			if (latticePanel != null) {
				/* Show rule in lattice */
				Rule currRule = ((RuleTableModel) getModel()).getRuleAt(row);
				try {
					latticePanel.showRule(currRule);
				} catch (NullPointerException error) {
					DialogBox.showMessageError(this, error);
				}
				latticePanel.repaint();
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	private class ColumnHeaderListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			int colIdx = columnAtPoint(e.getPoint());

			if (colIdx > -1 && colIdx < 6) {
				RuleTableModel tableModel = (RuleTableModel) getModel();
				TableColumnModel columnModel = thisTable.getColumnModel();
				tableModel.sortRulesOnColumn(colIdx);

				for (int i = 0; i < tableModel.getColumnCount(); i++) {
					TableColumn column = columnModel.getColumn(i);
					int index = column.getModelIndex();
					JLabel renderer = (JLabel) column.getHeaderRenderer();

					if (index == colIdx)
						renderer.setIcon(tableModel.getSortingIcon());
					else
						renderer.setIcon(null);
				}

				thisTable.tableChanged(new TableModelEvent(tableModel));
				thisTable.repaint();
			}
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}

}