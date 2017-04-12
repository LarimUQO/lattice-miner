/*
 ***********************************************************************
 * Copyright (C) 2004 The Galicia Team 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA; or visit the following url:
 * http://www.gnu.org/copyleft/lesser.html
 *
 ***********************************************************************
 */

package fca.gui.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 *
 * <p>Titre : Lattice</p>
 * <p>Description : Classe Absraite pour définir des filtres de fichiers</p>
 * <p>Copyright : Copyright (c) 2002</p>
 * <p>Société : Université de Montréal</p>
 * @author Alexandre Frantz et Pascal Camarda
 * @version 1.0
 */
public abstract class AbstractFilter extends FileFilter
{

	public abstract String getDescription();

	/**
	 * Accept all directories and IBM format.
	 * @param f l'objet File que l'on veut accepter
	 * @return un booleen
	 */
	public boolean accept(File f)
	{
		if(f!=null){
			if(f.isDirectory() || f.getName().endsWith(getFileExtension())) return true;
		}
		return false;
	}

	/**
	 * Obtient l'extension d'un fichier
	 * @return une String contenant l'extension
	 */
	public abstract String getFileExtension();

}