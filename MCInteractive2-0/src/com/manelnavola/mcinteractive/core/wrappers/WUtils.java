package com.manelnavola.mcinteractive.core.wrappers;

/**
 * Utility class to encapsulate the wrapper
 * @author Manel
 *
 */
public class WUtils {
	
	private static Wrapper wrapper;
	
	/**
	 * Sets the wrapper implementation to use
	 * @param w The wrapper implementation
	 */
	public static void setWrapper(Wrapper w) {
		wrapper = w;
	}
	
	/**
	 * Gets the wrapper implementation to use
	 * @return The wrapper implementation
	 */
	public static Wrapper get() {
		return wrapper;
	}
	
}
