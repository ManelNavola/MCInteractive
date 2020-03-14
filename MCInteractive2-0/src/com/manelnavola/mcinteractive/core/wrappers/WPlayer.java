package com.manelnavola.mcinteractive.core.wrappers;

/**
 * Wrapper class that represents a player
 * @author Manel Navola
 *
 */
public abstract class WPlayer<T> {
	
	private T player;
	
	/**
	 * Wrapped player constructor
	 * @param apiObject The object to base the player on
	 */
	public WPlayer(T player) {
		this.player = player;
	}
	
	/**
	 * Gets the player instance
	 * @return The player instance
	 */
	public T getPlayer() {
		return player;
	}
	
	/**
	 * Gets whether the player has operator status
	 * @return True if the player has operator status
	 */
	public abstract boolean isOp();
	
	/**
	 * Gets whether a player has a permission
	 * @param permission The permission to check
	 * @return True if the player has that permission
	 */
	public abstract boolean checkPermission(String permission);
	
	/**
	 * Sends a message to the player
	 * @param message The message to send
	 */
	public abstract void sendMessage(String message);

	/**
	 * Gets the unique ID for this player in string form
	 * @return
	 */
	public abstract String getUUID();
	
	/**
	 * Sends a title+subtitle message
	 * @param title The title of the message
	 * @param subtitle The subtitle of the message
	 */
	public abstract void sendTitle(String title, String subtitle);
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WPlayer<?>) {
			return ((WPlayer<?>)obj).getUUID().equals(this.getUUID());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return getUUID().hashCode();
	}
	
}
