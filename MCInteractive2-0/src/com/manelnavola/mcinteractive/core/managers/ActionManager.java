package com.manelnavola.mcinteractive.core.managers;

import com.manelnavola.mcinteractive.core.utils.ChatUtils;

/**
 * Singleton class for managing various actions
 * 
 * @author Manel Navola
 *
 */
public class ActionManager extends Manager {

	private static ActionManager INSTANCE;

	private ActionManager() {
	}

	/**
	 * Gets the singleton object
	 * 
	 * @return The singleton object
	 */
	public static ActionManager getInstance() {
		if (INSTANCE == null)
			INSTANCE = new ActionManager();
		return INSTANCE;
	}

	/**
	 * Reloads the bot manager
	 */
	public void reloadBot() {
		ChatUtils.broadcastOpInfo("Restarting connection...");
		BotManager.getInstance().stop();
		BotManager.getInstance().start();
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {
		INSTANCE = null;
	}

}