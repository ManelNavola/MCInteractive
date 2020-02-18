package com.manelnavola.mcinteractive.managers;

import org.bukkit.plugin.Plugin;

/**
 * Abstract definition of a Manager class
 * @author Manel Navola
 *
 */
public abstract class Manager {
	
	private Plugin plugin;
	
	/**
	 * Initialization of this manager
	 * @param plugin The plugin reference to store
	 */
	public void init(Plugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Disposal method for saving or cleaning up
	 */
	public void dispose() {
		this.plugin = null;
	}
	
	/**
	 * Gets the assigned plugin to the Manager
	 * @return The plugin
	 */
	public Plugin getPlugin() {
		return this.plugin;
	}
	
	
	
}
