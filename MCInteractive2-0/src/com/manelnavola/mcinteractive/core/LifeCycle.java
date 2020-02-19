package com.manelnavola.mcinteractive.core;

import com.manelnavola.mcinteractive.core.managers.BotManager;

/**
 * Singleton class for starting/stopping MCInteractive
 * @author Manel Navola
 *
 */
public class LifeCycle {
	
	private static LifeCycle INSTANCE;
	
	/**
	 * Gets the singleton object
	 * @return The singleton object
	 */
	public static LifeCycle getInstance() {
		if (INSTANCE == null) INSTANCE = new LifeCycle();
		return INSTANCE;
	}
	
	/**
	 * Starts MCInteractive
	 */
	public void start() {
		BotManager.getInstance().start();
	}
	
	/**
	 * Stops, saves and disposes MCInteractive
	 */
	public void end() {
		BotManager.getInstance().end();
	}
	
}
