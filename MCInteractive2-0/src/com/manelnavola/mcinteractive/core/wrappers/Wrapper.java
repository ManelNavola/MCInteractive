package com.manelnavola.mcinteractive.core.wrappers;

import java.util.Collection;

/**
 * Abstract class for implementation with different Minecraft APIs
 * @author Manel Navola
 *
 */
public abstract class Wrapper {
	
	/**
	 * Obtains a collection of currently online players
	 * @return A collection of online players
	 */
	public abstract Collection<WPlayer> getOnlinePlayers();
	
	/**
	 * Sends a message to a player
	 * @param wp The player to send the message to
	 * @param string The message
	 */
	public abstract void sendMessage(WPlayer wp, String message);
	
	/**
	 * Sends a message to the console
	 * @param message The message to send
	 */
	public abstract void sendConsoleMessage(String message);
	
}
