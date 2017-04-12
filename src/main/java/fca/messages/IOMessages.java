package fca.messages;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Ludovic Thomas
 * @version 1.0
 */
public class IOMessages {
	private static final String BUNDLE_NAME = "fca.messages.IOMessages"; //$NON-NLS-1$
	
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	private IOMessages() {
	}
	
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
