package com.manelnavola.mcinteractive.core.managers;

import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.twitchbotx.domain.TwitchUser;

/**
 * Singleton class for managing twitch messages
 * @author Manel Navola
 *
 */
public class ChatManager extends Manager {
	
	private static ChatManager INSTANCE;
	private ChatManager() {}
	
	private boolean enabled;
	
	/**
	 * Gets the singleton object
	 * @return The singleton object
	 */
	public static ChatManager getInstance() {
		if (INSTANCE == null) INSTANCE = new ChatManager();
		return INSTANCE;
	}
	
	@Override
	public void start() {
		enabled = true;
	}
	
	@Override
	public void stop() {
		enabled = false;
		INSTANCE = null;
	}

	/**
	 * Processes a Twitch message
	 * @param twitchUser The Twitch User who issued the message
	 * @param message The message issued
	 */
	public void onTwitchMessage(String channelName, TwitchUser twitchUser, String message) {
		if (!enabled) return;
		
		// TODO parse twitch message
		ChatUtils.broadcast(ConnectionManager.getInstance().getChannelPlayers(channelName), message);
	}
	
}
