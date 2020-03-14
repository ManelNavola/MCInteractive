package com.manelnavola.mcinteractive.core.managers;

import org.apache.commons.lang.NullArgumentException;

/**
 * Abstract class to represent a Manager
 * @author Manel Navola
 *
 */
public abstract class Manager {
	
	private boolean enabled;
	
	/**
	 * Initializes the manager
	 */
	public abstract void start();
	
	/**
	 * Stops the manager
	 */
	public abstract void stop();
	
	/**
	 * Sets whether the manager is enabled or not
	 * @param b True if the manager is enabled
	 */
	public void setEnabled(boolean b) {
		enabled = b;
	}
	
	/**
	 * Returns whether the manager is enabled or not 
	 * @return True if the manager is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Throws an exception if the manager is not enabled
	 * @throws Exception The exception to throw
	 */
	public void requireEnabled() {
		if (!enabled) {
			throw new NullArgumentException("Manager should be enabled! ignore this -> ");
		}
	}
	
}
