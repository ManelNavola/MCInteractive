package com.manelnavola.mcinteractive.core.managers;

import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.twitchbotx.TwitchBotX;
import com.manelnavola.twitchbotx.TwitchBotXListenerAdapter;
import com.manelnavola.twitchbotx.events.TwitchMessageEvent;

/**
 * Singleton class for TwitchBotX management
 * @author Manel Navola
 *
 */
public class BotManager {
	
	private static BotManager INSTANCE;
	
	private TwitchBotX twitchBotX;
	
	/**
	 * Gets the singleton object
	 * @return The singleton object
	 */
	public static BotManager getInstance() {
		if (INSTANCE == null) INSTANCE = new BotManager();
		return INSTANCE;
	}
	
	/**
	 * Starts the bot
	 */
	public void start() {
		twitchBotX = new TwitchBotX();
		twitchBotX.setListenerAdapter(new TwitchBotXAdapter());
		twitchBotX.connectAsync();
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
	 * Ends the bot
	 */
	public void end() {
		if (twitchBotX != null) {
			twitchBotX.disconnect();
			twitchBotX = null;
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
			ConnectionManager.getInstance().enable();
			ChatManager.getInstance().enable();
		}
		
		@Override
		public void onConnectFail(Exception e) {
			ChatUtils.broadcastError("Error connecting the bot: " + e.getMessage());
			ChatUtils.broadcastError("The bot has been disabled, but you can retry connecting using /mci reload");
		}
		
		@Override
		public void onDisconnect() {
			ConnectionManager.getInstance().disable();
			ChatManager.getInstance().disable();
			
			ChatUtils.broadcastError("The bot lost connection to Twitch servers!");
			ChatUtils.broadcastError("The bot has been disabled, but you can retry connecting using /mci reload");
		}
		
		@Override
		public void onTwitchMessage(TwitchMessageEvent twitchMessageEvent) {
			ChatManager.getInstance().onTwitchMessage(twitchMessageEvent.getChannelName(),
					twitchMessageEvent.getTwitchUser(),
					twitchMessageEvent.getMessage());
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
