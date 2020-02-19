package com.manelnavola.mcinteractive.core.wrappers;

/**
 * Wrapper class that represents a player
 * @author Manel Navola
 *
 */
public class WPlayer {
	
	private Object APIObject;
	private boolean op;
	
	/**
	 * Wrapped player constructor
	 * @param op Whether the player has operator status
	 */
	public WPlayer(Object APIObject, boolean op) {
		this.APIObject = APIObject;
		this.op = op;
	}
	
	/**
	 * Gets the player's API Object
	 * @return The API Object of the player
	 */
	public Object getAPIObject() {
		return APIObject;
	}
	
	/**
	 * Gets whether the player has operator status
	 * @return True if the player has operator status
	 */
	public boolean isOp() {
		return op;
	}
	
}
