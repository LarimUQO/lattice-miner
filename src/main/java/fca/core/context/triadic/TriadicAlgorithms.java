package fca.core.context.triadic;

import fca.core.rule.Rule;
import fca.core.rule.TRule;
import fca.gui.rule.RuleTableModel;
import fca.messages.GUIMessages;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

public class TriadicAlgorithms {

	private static String percentFormat(double d) {
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMinimumFractionDigits(1);
		return percentFormat.format(d);
	}

	//BCAAR
	public static String minnerBCAAR(Vector<Rule> r) {
		String result = GUIMessages.getString("GUI.triadicBCAAR") + "\n";

		Vector<TRule> curr = new Vector<TRule>();
		Vector<Rule> curr1 = new Vector<Rule>();
		String[] b = null;
		int h = 0;
		int h4 = 0;

		for (int i = 0; i < r.size(); i++) {

			b = tokenizer2(r.elementAt(i).getAntecedent().toString(), "\\-");
			if (b.length > 0) {
				// triMajMin(b,r.elementAt(i));

				int n = b.length;
				String Modus = "";
				// String Modus1 ="" ;
				String Attrib = "";
				// String Attrib1 ="" ;

				// System.out.print("Entrez une seule lettre A..Z ou a..z : ");
				for (int f = 0; f < n; f++) {
					String str = b[f];

					int d = str.length();
					for (int j = 0; j < d; j++) {
						char car;
						car = str.charAt(j);

						if ((car <= 'z') && (car >= 'a')) {

							Modus = Modus + Character.toString(car);

						} else if ((car <= 'Z') && (car >= 'A')) {

							Attrib = Attrib + Character.toString(car);

						} else if ((car <= '9') && (car >= '1')) {
							//Modus = Modus + Character.toString(car);
							Attrib = Attrib + Character.toString(car);
						}

					}

				}

				if ((RuleTableModel.removeDuplicates(Modus).length()
						* RuleTableModel.removeDuplicates(Attrib).length() == r.elementAt(i)
						.getAntecedent().size())
						) {
					curr1.add(r.elementAt(i));
					/*System.out
								.println(h + ": " + r.elementAt(i)
										+ " { AL (Attributes)= "
										+ removeDuplicates(Attrib)
										+ ", ML (Modus)= "
										+ removeDuplicates(Modus) + " }");
						h++; */
					Iterator<String> iterateur = r.elementAt(i)
							.getConsequence().iterator();

					String g = "";
					while (iterateur.hasNext()) {

						String[] c = tokenizer2(iterateur.next(), "\\-");

						if (c.length > 0) {

							String Modus1 = "";
							String Attrib1 = "";
							// System.out.print("Entrez une seule lettre A..Z ou a..z : ");
							for (int w = 0; w < c.length; w++) {
								String str = c[w];

								int o = str.length();
								for (int y = 0; y < o; y++) {
									char car;
									car = str.charAt(y);

									if ((car <= 'z') && (car >= 'a')) {

										Modus1 = Modus1
												+ Character.toString(car);

									} else if ((car <= 'Z') && (car >= 'A')) {

										Attrib1 = Attrib1
												+ Character.toString(car);

									} else if ((car <= '9') && (car >= '1')) {
										//	Modus1 = Modus1 + Character.toString(car);
										Attrib1 = Attrib1
												+ Character.toString(car);
									}

								}

							}

							HashSet<Character> h1 = new HashSet<Character>(), h2 = new HashSet<Character>();
							for (int i1 = 0; i1 < Modus1.length(); i1++) {
								h1.add(Modus1.charAt(i1));
							}
							for (int i1 = 0; i1 < Modus.length(); i1++) {
								h2.add(Modus.charAt(i1));
							}
							h1.retainAll(h2);
							String uk = h1.toString();
							String[] pu = tokenizer2(uk, "\\[]");
							if (pu.length > 0) {
								g = g + Arrays.asList(c).toString();
							}
							;

						}

					}

					if (g.length() > 0) {
						String Attri = "";
						String[] group = tokenizer2(g, "]");
						String tempo = Arrays.asList(group).toString();
						//System.out.println(tempo) ;
						for (int w1 = 0; w1 < group.length; w1++) {
							//	String str1 = group[w1];
							String str1 = tempo;
							//int o1 = str1.length();
							int o1 = tempo.length();
							for (int y1 = 0; y1 < o1; y1++) {
								char car1;
								car1 = str1.charAt(y1);

								if ((car1 <= 'Z') && (car1 >= 'A')) {

									if (RuleTableModel.regexOccur(str1, Character.toString(car1)) == RuleTableModel.removeDuplicates(Modus).length()) {
										Attri = Attri + Character.toString(car1);
									}

								} else if ((car1 <= '9') && (car1 >= '1')) {
									if (RuleTableModel.regexOccur(str1, Character.toString(car1)) == RuleTableModel.removeDuplicates(Modus).length()) {
										Attri = Attri + Character.toString(car1);
									}
								}

							}

						}
						if (Attri.length() > 0) {
							h4++;
							result += h4 + ": ( " + RuleTableModel.removeDuplicates(Attrib) + " -> " + RuleTableModel.removeDuplicates(Attri) + " ) " + RuleTableModel.removeDuplicates(Modus) + " [support = " + percentFormat(r.elementAt(i).getSupport()) + " confidence = " + percentFormat(r.elementAt(i).getConfidence()) + "]\n";
							TRule temp1 = new TRule(RuleTableModel.removeDuplicates(Attrib), RuleTableModel.removeDuplicates(Attri), RuleTableModel.removeDuplicates(Modus), r.elementAt(i).getSupport(), r.elementAt(i).getConfidence(), r.elementAt(i).getLift(), 1);
							curr.add(temp1);
						}
					}
				}
			}
		}
		return result;
	}

	//BACAR
	public static String minnerBACAR(Vector<Rule> r) {
		String result = GUIMessages.getString("GUI.triadicBACAR") + "\n";
		Vector<TRule> curr1 = new Vector<TRule>();
		Vector<Rule> curr = new Vector<Rule>();

		String[] b = null;
		int h = 0;
		int h4 = 0;
		for (int i = 0; i < r.size(); i++) {

			b = tokenizer2(r.elementAt(i).getAntecedent().toString(), "\\-");
			if (b.length > 0) {
				// triMajMin(b,r.elementAt(i));

				int n = b.length;
				String Modus = "";
				// String Modus1 ="" ;
				String Attrib = "";
				// String Attrib1 ="" ;

				// System.out.print("Entrez une seule lettre A..Z ou a..z : ");
				for (int f = 0; f < n; f++) {
					String str = b[f];

					int d = str.length();
					for (int j = 0; j < d; j++) {
						char car;
						car = str.charAt(j);

						if ((car <= 'z') && (car >= 'a')) {

							Modus = Modus + Character.toString(car);

						} else if ((car <= 'Z') && (car >= 'A')) {

							Attrib = Attrib + Character.toString(car);

						} else if ((car <= '9') && (car >= '1')) {
							//	Modus = Modus + Character.toString(car);
							Attrib = Attrib + Character.toString(car);
						}

					}

				}

				if ((RuleTableModel.removeDuplicates(Modus).length()
						* RuleTableModel.removeDuplicates(Attrib).length() == r.elementAt(i)
						.getAntecedent().size())
						) {
					curr.add(r.elementAt(i));
					/*	System.out
								.println(h + ": " + r.elementAt(i)
										+ " { AL (Attributes)= "
										+ removeDuplicates(Attrib)
										+ ", ML (Modus)= "
										+ removeDuplicates(Modus) + " }");
						h++; */
					Iterator<String> iterateur = r.elementAt(i)
							.getConsequence().iterator();

					String g = "";
					while (iterateur.hasNext()) {

						String[] c = tokenizer2(iterateur.next(), "\\-");

						if (c.length > 0) {

							String Modus1 = "";
							String Attrib1 = "";
							// System.out.print("Entrez une seule lettre A..Z ou a..z : ");
							for (int w = 0; w < c.length; w++) {
								String str = c[w];

								int o = str.length();
								for (int y = 0; y < o; y++) {
									char car;
									car = str.charAt(y);

									if ((car <= 'z') && (car >= 'a')) {

										Modus1 = Modus1
												+ Character.toString(car);

									} else if ((car <= 'Z') && (car >= 'A')) {

										Attrib1 = Attrib1
												+ Character.toString(car);

									} else if ((car <= '9') && (car >= '1')) {
										//	Modus1 = Modus1 + Character.toString(car);
										Attrib1 = Attrib1
												+ Character.toString(car);

									}

								}

							}

							HashSet<Character> h1 = new HashSet<Character>(), h2 = new HashSet<Character>();
							for (int i1 = 0; i1 < Attrib1.length(); i1++) {
								h1.add(Attrib1.charAt(i1));
							}
							for (int i1 = 0; i1 < Attrib.length(); i1++) {
								h2.add(Attrib.charAt(i1));
							}
							h1.retainAll(h2);
							String uk = h1.toString();
							String[] pu = tokenizer2(uk, "\\[]");
							if (pu.length > 0) {
								g = g + Arrays.asList(c).toString();
							}
							;

						}

					}

					if (g.length() > 0) {
						String Attri = "";
						String[] group = tokenizer2(g, "]");
						String tempo = Arrays.asList(group).toString();
						//System.out.println(tempo) ;
						for (int w1 = 0; w1 < group.length; w1++) {
							//	String str1 = group[w1];
							String str1 = tempo;
							//int o1 = str1.length();
							int o1 = tempo.length();
							for (int y1 = 0; y1 < o1; y1++) {
								char car1;
								car1 = str1.charAt(y1);

								if ((car1 <= 'z') && (car1 >= 'a')) {

									if (RuleTableModel.regexOccur(str1, Character.toString(car1)) == RuleTableModel.removeDuplicates(Attrib).length()) {
										Attri = Attri + Character.toString(car1);
									}

								}
							}
						}

						if (Attri.length() > 0) {
							h4++;
							result += h4 + ": ( " + RuleTableModel.removeDuplicates(Modus) + " -> " + RuleTableModel.removeDuplicates(Attri) + " ) " + RuleTableModel.removeDuplicates(Attrib) + " [support = " + percentFormat(r.elementAt(i).getSupport()) + " confidence = " + percentFormat(r.elementAt(i).getConfidence()) + "]\n";
							TRule temp1 = new TRule(RuleTableModel.removeDuplicates(Modus), RuleTableModel.removeDuplicates(Attri), RuleTableModel.removeDuplicates(Attrib), r.elementAt(i).getSupport(), r.elementAt(i).getConfidence(), r.elementAt(i).getLift(), 2);
							curr1.add(temp1);
						}
					}
				}
			}
		}
		return result;
	}

	public static String[] tokenizer2(String s, String delimiteur) {
		return s.split(delimiteur);
	}
}
