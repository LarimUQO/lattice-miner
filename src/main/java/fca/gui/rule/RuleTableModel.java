package fca.gui.rule;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

import fca.core.rule.Rule;
import fca.gui.util.constant.LMIcons;
import fca.messages.GUIMessages;

public class RuleTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3694667801785011862L;

	private Vector<Rule> rules;

	private int sortIndex;

	private boolean naturalOrder;

	public RuleTableModel(Vector<Rule> r) {
		super();
		rules = r;
		sortIndex = -1;
		naturalOrder = true;
	}

	public Object getValueAt(int rowIdx, int colIdx) {
		Rule currRule = rules.elementAt(rowIdx);

		String value = ""; //$NON-NLS-1$
		switch (colIdx) {
		case 0:
			value = "" + (rowIdx + 1) + "."; //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case 1:
			value = currRule.getAntecedent().toString();
			break;
		case 2:
			value = "=>"; //$NON-NLS-1$
			break;
		case 3:
			value = currRule.getConsequence().toString();
			break;
		case 4:
			value = "" + (((int) (currRule.getSupport() * 10000.0)) / 100.0) + "%"; //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case 5:
			value = "" + (((int) (currRule.getConfidence() * 10000.0)) / 100.0) + "%"; //$NON-NLS-1$ //$NON-NLS-2$
			break;
		default:
			value = ""; //$NON-NLS-1$
		break;
		}

		return value;
	}

	@Override
	public boolean isCellEditable(int rowIdx, int colIdx) {
		return false;
	}

	public int getRowCount() {
		return rules.size();
	}

	public int getColumnCount() {
		return 6;
	}

	@Override
	public String getColumnName(int colIdx) {
		String value = ""; //$NON-NLS-1$

		switch (colIdx) {
		case 0:
			value = "#"; //$NON-NLS-1$
			break;
		case 1:
			value = GUIMessages.getString("GUI.antecedent"); //$NON-NLS-1$
			break;
		case 2:
			value = "=>"; //$NON-NLS-1$
			break;
		case 3:
			value = GUIMessages.getString("GUI.consequence"); //$NON-NLS-1$
			break;
		case 4:
			value = GUIMessages.getString("GUI.support"); //$NON-NLS-1$
			break;
		case 5:
			value = GUIMessages.getString("GUI.confidence"); //$NON-NLS-1$
			break;
		case 6:
			value = "test"; //$NON-NLS-1$
			break;
		default:
			value = ""; //$NON-NLS-1$
			break;
		}

		return value;
	}

	public void sortRulesOnColumn(int colIdx) {
		if (colIdx == sortIndex) {
			naturalOrder = !naturalOrder;
		} else {
			sortIndex = colIdx;
			naturalOrder = true;
		}

		Collections.sort(rules, new RuleComparator(sortIndex, naturalOrder));
	}

	public Rule getRuleAt(int index) {
		if (index >= rules.size())
			return null;

		return rules.elementAt(index);
	}

	public Icon getSortingIcon() {
		if ((sortIndex == 1 || sortIndex == 3) && naturalOrder)
			return LMIcons.getSortDown();

		else if ((sortIndex == 4 || sortIndex == 5) && !naturalOrder)
			return LMIcons.getSortDown();

		else
			return LMIcons.getSortUp();
	}

	public static final int regexOccur(String text, String regex) {
		Matcher matcher = Pattern.compile(regex).matcher(text);
		int occur = 0;
		while(matcher.find()) {
			occur ++;
		}
		return occur;
	}

	public static String removeDuplicates(String s) {
		StringBuilder noDupes = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			String si = s.substring(i, i + 1);
			if (noDupes.indexOf(si) == -1) {
				noDupes.append(si);
			}
		}
		return noDupes.toString();
	}

	/**
	 * @author Geneviève Roberge
	 * @version 1.0
	 */
	private class RuleComparator implements Comparator<Rule> {
		private int sortIndex;
		private boolean naturalOrder;

		public RuleComparator(int idx, boolean order) {
			sortIndex = idx;
			naturalOrder = order;
		}

		public int compare(Rule obj1, Rule obj2) {

			int result;
			Rule rule1 = obj1;
			Rule rule2 = obj2;

			/* Antecendant */
			if (sortIndex == 1)
				result = rule1.getAntecedent().toString().compareTo(rule2.getAntecedent().toString());
			else if (sortIndex == 3)
				result = rule1.getConsequence().toString().compareTo(rule2.getConsequence().toString());
			else if (sortIndex == 4)
				result = rule1.getSupport() < rule2.getSupport() ? -1 : (rule1.getSupport() > rule2.getSupport() ? 1
						: 0);
			else if (sortIndex == 5)
				result = rule1.getConfidence() < rule2.getConfidence() ? -1
						: (rule1.getConfidence() > rule2.getConfidence() ? 1 : 0);
			else
				result = 0;

			if (!naturalOrder)
				result = -result;
			return result;
		}

		/**
		 * @return l'index courant de tri
		 */
		public int getSortIndex() {
			return sortIndex;
		}

		/**
		 * @return vrai si l'ordre est naturel, faux sinon
		 */
		public boolean isNaturalOrder() {
			return naturalOrder;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof RuleComparator))
				return false;

			RuleComparator comp = (RuleComparator) obj;
			if ((comp.getSortIndex() == sortIndex) && (comp.isNaturalOrder() == naturalOrder))
				return true;

			return false;
		}
	}
}