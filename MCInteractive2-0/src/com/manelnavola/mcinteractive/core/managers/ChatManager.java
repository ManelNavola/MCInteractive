package com.manelnavola.mcinteractive.core.managers;

import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.twitchbotx.domain.TwitchUser;

/**
 * Singleton class for managing twitch messages
 * @author Manel Navola
 *
 */
public class ChatManager {
	
	private static ChatManager INSTANCE;
	
	private boolean enabled = false;
	
	/**
	 * Gets the singleton object
	 * @return The singleton object
	 */
	public static ChatManager getInstance() {
		if (INSTANCE == null) INSTANCE = new ChatManager();
		return INSTANCE;
	}
	
	/**
	 * Enables the manager
	 */
	public void enable() {
		enabled = true;
	}
	
	/**
	 * Disables the manager
	 */
	public void disable() {
		enabled = false;
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
