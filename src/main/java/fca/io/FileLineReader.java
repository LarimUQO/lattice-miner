package fca.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Lecteur de ligne pour un fichier
 * @author Geneviève Roberge
 * @version 1.0
 */
public class FileLineReader extends FileReader {

	/**
	 * Constructeur de lecteur de ligne
	 * @param file le fichier depuis lequel on doit lire
	 * @throws FileNotFoundException si le fichier n'est pas trouvé
	 */
	public FileLineReader(File file) throws FileNotFoundException {
		super(file);
	}

	/**
	 * Lit une ligne à la fois
	 * @return la ligne courante lue dans le fichier
	 */
	public String readLine() {
		try {
			int ch = -1;
			StringBuffer sb = new StringBuffer();
			while (true) {
				ch = read();
				if ((ch < 0) || (ch == '\n'))
					break;
				sb.append((char) ch);
			}
			return sb.toString();
		} catch (IOException e) {
			return (null);
		}
	}
}