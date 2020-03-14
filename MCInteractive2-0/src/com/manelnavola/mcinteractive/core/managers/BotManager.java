package com.manelnavola.mcinteractive.core.managers;

import com.manelnavola.mcinteractive.core.LifeCycle;
import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.mcinteractive.core.utils.ChatUtils.LogMessageType;
import com.manelnavola.twitchbotx.TwitchBotX;
import com.manelnavola.twitchbotx.TwitchBotXListenerAdapter;
import com.manelnavola.twitchbotx.events.TwitchMessageEvent;

/**
 * Singleton class for TwitchBotX management
 * 
 * @author Manel Navola
 *
 */
public class BotManager extends Manager {

	private static BotManager INSTANCE;

	private BotManager() {
	}

	private TwitchBotX twitchBotX;
	private boolean forcedDisconnect;

	/**
	 * Gets the singleton object
	 * 
	 * @return The singleton object
	 */
	public static BotManager getInstance() {
		if (INSTANCE == null)
			INSTANCE = new BotManager();
		return INSTANCE;
	}

	@Override
	public void start() {
		forcedDisconnect = false;
		if (twitchBotX == null) {
			twitchBotX = new TwitchBotX();
			twitchBotX.setListenerAdapter(new TwitchBotXAdapter());
		}
		ChatUtils.logOperators("Connecting to Twitch servers...", LogMessageType.INFO);
		twitchBotX.connectAsync();
	}

	@Override
	public void stop() {
		forcedDisconnect = true;
		if (twitchBotX != null) {
			twitchBotX.disconnect();
			twitchBotX = null;
		}
		INSTANCE = null;
	}
	
	public void stopWithError() {
		forcedDisconnect = false;
		if (twitchBotX != null) {
			twitchBotX.disconnect();
			twitchBotX = null;
		}
	}

	/**
	 * Tries reconnecting
	 */
	public void reconnect() {
		if (twitchBotX != null) {
			twitchBotX.connectAsync();
		}
	}

	/**
	 * Utility class for listening to TwitchBotX events
	 * 
	 * @author Manel Navola
	 *
	 */
	public class TwitchBotXAdapter extends TwitchBotXListenerAdapter {
		
		@Override
		public void onConnectSuccess() {
			try {
				startAll();
				ChatUtils.logOperators("Connected to Twitch servers!", LogMessageType.NICE);
			} catch (Exception e) {
				ChatUtils.logOperators("Internal error! MC Interactive has been disabled", LogMessageType.ERROR);
				ChatUtils.logOperators(e);
				stopAll();
			}
		}

		@Override
		public void onConnectFail(Exception e) {
			try {
				ChatUtils.logOperators("Error connecting to Twitch servers: " + e.getMessage(), LogMessageType.ERROR);

				stopAll();

				if (LifeCycle.getInstance().isCommandManagerEnabled()) {
					ChatUtils.logOperators("MC Interactive has been disabled, but you can try "
							+ "reconnecting to Twitch servers using /mci reload", LogMessageType.WARN);
				} else {
					ChatUtils.logOperators("MC Interactive has been disabled!", LogMessageType.ERROR);
				}
			} catch (Exception e2) {
				ChatUtils.logOperators("Internal error! MC Interactive has been disabled", LogMessageType.ERROR);
				ChatUtils.logOperators(e2);
				stopAll();
			}
		}

		@Override
		public void onDisconnect() {
			try {
				if (forcedDisconnect) {
					return;
				} else {
					ChatUtils.logOperators("MC Interactive has lost connection to Twitch servers!",
							LogMessageType.ERROR);
				}

				stopAll();

				if (forcedDisconnect) {
					forcedDisconnect = false;
				} else {
					if (LifeCycle.getInstance().isCommandManagerEnabled()) {
						ChatUtils.logOperators("MC Interactive has been disabled, but you can try "
								+ "reconnecting to Twitch servers using /mci reload", LogMessageType.WARN);
					} else {
						ChatUtils.logOperators("MC Interactive has been disabled!", LogMessageType.ERROR);
					}
				}
			} catch (Exception e) {
				ChatUtils.logOperators("Internal error! MC Interactive has been disabled", LogMessageType.ERROR);
				ChatUtils.logOperators(e);
				stopAll();
			}
		}

		@Override
		public void onTwitchMessage(TwitchMessageEvent twitchMessageEvent) {
			try {
				StreamManager.getInstance().onTwitchMessage(twitchMessageEvent.getChannelName(),
						twitchMessageEvent.getTwitchUser(), twitchMessageEvent.getMessage());
				ChatManager.getInstance().onTwitchMessage(twitchMessageEvent.getChannelName(),
						twitchMessageEvent.getTwitchUser(), twitchMessageEvent.getMessage());
			} catch (Exception e) {
				ChatUtils.logOperators("Internal error! MC Interactive has been disabled", LogMessageType.ERROR);
				ChatUtils.logOperators(e);
				stopAll();
			}
		}

	}
	
	private void startAll() {
		EventManager.getInstance().start();
		
		ConfigurationManager.getInstance().start();
		StreamManager.getInstance().start();
		ChatManager.getInstance().start();

		if (LifeCycle.getInstance().isCommandManagerEnabled()) {
			CommandManager.getInstance().start();
		}
		
		EventManager.getInstance().enable();
	}

	public void stopAll() {
		EventManager.getInstance().disable();
		
		CommandManager.getInstance().stop();
		StreamManager.getInstance().stop();
		ChatManager.getInstance().stop();
		ConfigurationManager.getInstance().stop();

		if (LifeCycle.getInstance().isCommandManagerEnabled()) {
			if (forcedDisconnect) {
				CommandManager.getInstance().stop();
			} else {
				CommandManager.getInstance().startReloadOnly();
			}
		}
		
		EventManager.getInstance().stop();
	}

	/**
	 * Makes the bot join a channel
	 * 
	 * @param channel The channel name
	 */
	public void joinChannel(String channelName) {
		twitchBotX.joinChannel(channelName);
	}

	/**
	 * Makes the bot part from a channel
	 * 
	 * @param channel The channel name
	 */
	public void leaveChannel(String channelName) {
		twitchBotX.partChannel(channelName);
	}

}
