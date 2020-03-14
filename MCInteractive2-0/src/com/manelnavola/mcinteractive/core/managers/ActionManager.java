package com.manelnavola.mcinteractive.core.managers;

import java.util.function.Consumer;

import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.mcinteractive.core.utils.ChatUtils.LogMessageType;
import com.manelnavola.mcinteractive.core.wrappers.WPlayer;
import com.manelnavola.mcinteractive.core.wrappers.WServer;
import com.manelnavola.mcinteractive.core.wrappers.Wrapper;

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
		ChatUtils.logOperators("Restarting connection...", LogMessageType.INFO);
		BotManager.getInstance().stop();
		BotManager.getInstance().stopAll();
		Wrapper.getInstance().runOnServer(new Consumer<WServer<?>>() {
			@Override
			public void accept(WServer<?> server) {
				for (WPlayer<?> wp : server.getOnlinePlayers()) {
					EventManager.getInstance().onPlayerJoin(wp);
				}
			}
		});
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