package com.manelnavola.mcinteractive.core.utils;

import java.util.Collection;

import com.manelnavola.mcinteractive.core.wrappers.WPlayer;
import com.manelnavola.mcinteractive.core.wrappers.WUtils;

/**
 * Utils class
 * @author Manel Navola
 *
 */
public class ChatUtils {
	
	private static final String CHAT_TAG = "[MCI]";
	
	/**
	 * Broadcasts a message to a collection of players
	 * @param players The players to send the message to
	 * @param message The message to send
	 */
	public static void broadcast(Collection<WPlayer> players, String message) {
		if (players == null || message == null) return;
		
		for (WPlayer wp : players) {
			WUtils.get().sendMessage(wp, message);
		}
	}
	
	/**
	 * Broadcasts an error to all op players and the console
	 * @param message The error to broadcast
	 */
	public static void broadcastError(String message) {
		if (message == null) return;
		
		String error = MessageColor.RED + CHAT_TAG + " " + message;
		for (WPlayer wp : WUtils.get().getOnlinePlayers()) {
			if (wp.isOp()) {
				WUtils.get().sendMessage(wp, error);
			}
		}
		WUtils.get().sendConsoleMessage(error);
	}
	
	public enum MessageColor {
		RED('c');
		
		private String toString;
		
		MessageColor(char colorChar) {
			this.toString = String.valueOf(new char[] {'\u00A7', colorChar});
		}
		
		public String toString() {
			return toString;
		}
	}
	
}
