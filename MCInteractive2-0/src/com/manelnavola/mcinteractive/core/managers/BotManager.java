package com.manelnavola.mcinteractive.core.managers;

import com.manelnavola.mcinteractive.core.LifeCycle;
import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.twitchbotx.TwitchBotX;
import com.manelnavola.twitchbotx.TwitchBotXListenerAdapter;
import com.manelnavola.twitchbotx.events.TwitchMessageEvent;

/**
 * Singleton class for TwitchBotX management
 * @author Manel Navola
 *
 */
public class BotManager extends Manager {
	
	private static BotManager INSTANCE;
	private BotManager() {}
	
	private TwitchBotX twitchBotX;
	private boolean forcedDisconnect;
	
	/**
	 * Gets the singleton object
	 * @return The singleton object
	 */
	public static BotManager getInstance() {
		if (INSTANCE == null) INSTANCE = new BotManager();
		return INSTANCE;
	}
	
	@Override
	public void start() {
		forcedDisconnect = false;
		if (twitchBotX == null) {
			twitchBotX = new TwitchBotX();
			twitchBotX.setListenerAdapter(new TwitchBotXAdapter());
		}
		ChatUtils.broadcastOpInfo("Connecting to Twitch servers...");
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
	 * @author Manel Navola
	 *
	 */
	public class TwitchBotXAdapter extends TwitchBotXListenerAdapter {
		
		@Override
		public void onConnectSuccess() {
			try {
				startAll();
				ChatUtils.broadcastOpSuccess("Connected to Twitch servers!");
			} catch (Exception e) {
				e.printStackTrace();
				ChatUtils.broadcastOpError("Internal error! MC Interactive has been disabled");
				stopAll();
			}
		}
		
		@Override
		public void onConnectFail(Exception e) {
			try {
				ChatUtils.broadcastOpError("Error connecting to Twitch servers: " + e.getMessage());
				
				stopAll();
				
				if (LifeCycle.getInstance().isCommandManagerEnabled()) {
					ChatUtils.broadcastOpError("MC Interactive has been disabled, but you can try "
							+ "reconnecting to Twitch servers using /mci reload");
				} else {
					ChatUtils.broadcastOpError("MC Interactive has been disabled!");
				}
			} catch (Exception e2) {
				ChatUtils.broadcastOpError("Internal error! MC Interactive has been disabled");
				stopAll();
				e2.printStackTrace();
			}
		}
		
		@Override
		public void onDisconnect() {
			try {
				if (!forcedDisconnect) {
					ChatUtils.broadcastOpError("MC Interactive has lost connection to Twitch servers!");
				}
				
				stopAll();
				
				if (forcedDisconnect) {
					forcedDisconnect = false;
				} else {
					if (LifeCycle.getInstance().isCommandManagerEnabled()) {
						ChatUtils.broadcastOpError("MC Interactive has been disabled, but you can try " 
								+ "reconnecting to Twitch servers using /mci reload");
					} else {
						ChatUtils.broadcastOpError("MC Interactive has been disabled!");
					}
				}
			} catch (Exception e) {
				ChatUtils.broadcastOpError("Internal error! MC Interactive has been disabled");
				stopAll();
				e.printStackTrace();
			}
		}
		
		@Override
		public void onTwitchMessage(TwitchMessageEvent twitchMessageEvent) {
			try {
				ChatManager.getInstance().onTwitchMessage(twitchMessageEvent.getChannelName(),
						twitchMessageEvent.getTwitchUser(),
						twitchMessageEvent.getMessage());
			} catch (Exception e) {
				ChatUtils.broadcastOpError("Internal error! MC Interactive has been disabled");
				stopAll();
				e.printStackTrace();
			}
		}
		
		private void startAll() {
			StreamManager.getInstance().start();
			ChatManager.getInstance().start();
			
			if (LifeCycle.getInstance().isCommandManagerEnabled()) {
				CommandManager.getInstance().start();
			}
		}
		
		private void stopAll() {
			CommandManager.getInstance().stop();
			StreamManager.getInstance().stop();
			ChatManager.getInstance().stop();
			
			if (LifeCycle.getInstance().isCommandManagerEnabled()) {
				if (forcedDisconnect) {
					CommandManager.getInstance().stop();
				} else {
					CommandManager.getInstance().startReloadOnly();
				}
			}
		}
		
	}
	
	/**
	 * Makes the bot join a channel
	 * @param channel The channel name
	 */
	public void joinChannel(String channelName) {
		twitchBotX.joinChannel(channelName);
	}
	
	/**
	 * Makes the bot part from a channel
	 * @param channel The channel name
	 */
	public void leaveChannel(String channelName) {
		twitchBotX.partChannel(channelName);
	}
	
}
