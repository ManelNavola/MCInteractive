package com.manelnavola.mcinteractive.core;

import com.manelnavola.mcinteractive.core.managers.*;

/**
 * Singleton class for starting/stopping MCInteractive
 * @author Manel Navola
 *
 */
public class LifeCycle extends Manager {
	
	private static LifeCycle INSTANCE;
	
	private boolean commandManagerEnabled = false;
	
	/**
	 * Gets the singleton object
	 * @return The singleton object
	 */
	public static LifeCycle getInstance() {
		if (INSTANCE == null) INSTANCE = new LifeCycle();
		return INSTANCE;
	}
	
	@Override
	public void start() {
		BotManager.getInstance().start();
	}
	
	@Override
	public void stop() {
		ActionManager.getInstance().stop();
		BotManager.getInstance().stop();
		ChatManager.getInstance().stop();
		CommandManager.getInstance().stop();
		ConnectionManager.getInstance().stop();
	}
	
	/**
	 * Enables the command manager
	 */
	public void enableCommandManager() {
		commandManagerEnabled = true;
	}
	
	/**
	 * Gets whether the command manager is enabled or not
	 * @return True if the command manager is enabled
	 */
	public boolean isCommandManagerEnabled() {
		return commandManagerEnabled;
	}
	
}
