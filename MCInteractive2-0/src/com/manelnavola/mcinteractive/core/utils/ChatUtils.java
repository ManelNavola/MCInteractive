package com.manelnavola.mcinteractive.core.utils;

import java.util.Collection;

import com.manelnavola.mcinteractive.core.wrappers.WPlayer;
import com.manelnavola.mcinteractive.core.wrappers.Wrapper;

/**
 * Utils class
 * @author Manel Navola
 *
 */
public class ChatUtils {
	
	private static final String CHAT_TAG = "[MCI]";
	private static final MessageColor ERROR_COLOR = MessageColor.RED;
	private static final MessageColor INFO_COLOR = MessageColor.GOLD;
	
	/**
	 * Sends a tagged message to a player or a console
	 * @param wPlayer The player to send the message to or null if the console is the recipient
	 * @param message The message to send
	 */
	public static void sendTagged(WPlayer<?> wPlayer, String message) {
		if (wPlayer == null) {
			Wrapper.getInstance().getServer().sendConsoleMessage(message);
		} else {
			wPlayer.sendMessage(CHAT_TAG + message);
		}
	}
	
	/**
	 * Sends an informative message to a player or a console
	 * @param wPlayer The player to send the message to or null if the console is the recipient
	 * @param message The message to send
	 */
	public static void sendInfo(WPlayer<?> wPlayer, String message) {
		sendTagged(wPlayer, INFO_COLOR + message);
	}
	
	/**
	 * Sends an error message to a player or a console
	 * @param wPlayer The player to send the message to or null if the console is the recipient
	 * @param message The message to send
	 */
	public static void sendError(WPlayer<?> wPlayer, String message) {
		sendTagged(wPlayer, ERROR_COLOR + message);
	}
	
	/**
	 * Broadcasts a message to a collection of players
	 * @param players The players to send the message to
	 * @param message The message to send
	 */
	public static void broadcast(Collection<WPlayer<?>> players, String message) {
		if (players == null || message == null) return;
		
		for (WPlayer<?> wp : players) {
			wp.sendMessage(message);
		}
	}
	
	/**
	 * Broadcasts an error to all op players and the console
	 * @param message The error to broadcast
	 */
	public static void broadcastError(String message) {
		if (message == null) return;
		
		String error = MessageColor.RED + CHAT_TAG + " " + message;
		for (WPlayer<?> wp : Wrapper.getInstance().getServer().getOnlinePlayers()) {
			if (wp.isOp()) {
				wp.sendMessage(error);
			}
		}
		Wrapper.getInstance().getServer().sendConsoleMessage(error);
	}
	
	/**
	 * Enum for formatted chat color codes
	 * @author Manel Navola
	 *
	 */
	public enum MessageColor {
		RED('c'), GOLD('6');
		
		private String toString;
		
		MessageColor(char colorChar) {
			this.toString = String.valueOf(new char[] {'\u00A7', colorChar});
		}
		
		public String toString() {
			return toString;
		}
	}
	
}
