package com.manelnavola.mcinteractive.core.wrappers;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Wrapper class that represents a server
 * @author Manel Navola
 *
 */
public abstract class WServer<T> {
	
	private T server;
	
	/**
	 * Wrapped server constructor
	 * @param apiObject The object to base the player on
	 */
	public WServer(T server) {
		this.server = server;
	}
	
	/**
	 * Gets the server instance
	 * @return The server instance
	 */
	public T getServer() {
		return server;
	}
	
	/**
	 * Gets all online players in the server
	 * @return A collection with all currently online players
	 */
	public abstract Collection<WPlayer<?>> getOnlinePlayers();
	
	/**
	 * Sends a message to the server's console
	 * @param message The message to send
	 */
	public abstract void sendConsoleMessage(String message);
	
	/**
	 * Runs code in the server thread
	 * @param runnable The runnable to run
	 */
	public abstract void runOnServer(Consumer<WServer<?>> consumer);
	
	/**
	 * Loads configuration files
	 * @return A map containing the loaded configuration
	 */
	public abstract ConcurrentHashMap<String, Object> loadConfiguration();
	
	/**
	 * Saves a configuration file
	 * @param configurationMap The map containing data to save the configuration
	 * @param b Whether the configuration should be unloaded
	 */
	public abstract void saveConfiguration(ConcurrentHashMap<String, Object> configurationMap, boolean unload);

	/**
	 * Loads a player configuration file
	 * @param id The player to load the configuration from
	 * @return A map containing the loaded configuration
	 */
	public abstract ConcurrentHashMap<String, Object> loadPlayerConfiguration(String id);
	
	/**
	 * Saves a player configuration file
	 * @param configurationMap The map containing data to save the configuration
	 */
	public abstract void savePlayerConfiguration(WPlayer<?> wp, ConcurrentHashMap<String, Object> configMap);

	/**
	 * Returns whether server logging is available
	 * @return True if server logging is enabled
	 */
	public abstract boolean isServerLoggingAvailable();
	
}
