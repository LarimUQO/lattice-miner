package fca.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

public abstract class Viewer extends JFrame implements WindowListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8793366771598613772L;

	public void windowActivated(WindowEvent e) {
	}
	
	public void windowClosed(WindowEvent e) {
	}
	
	public void windowClosing(WindowEvent e) {
		if (e.getSource() instanceof JFrame)
			((JFrame) e.getSource()).dispose();
	}
	
	public void windowDeactivated(WindowEvent e) {
	}
	
	public void windowDeiconified(WindowEvent e) {
	}
	
	public void windowIconified(WindowEvent e) {
	}
	
	public void windowOpened(WindowEvent e) {
	}
}