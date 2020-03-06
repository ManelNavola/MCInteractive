package com.manelnavola.mcinteractive.core.utils;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import com.manelnavola.mcinteractive.core.wrappers.WPlayer;
import com.manelnavola.mcinteractive.core.wrappers.WServer;
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
	private static final MessageColor SUCCESS_COLOR = MessageColor.GREEN;
	private static final MessageColor WARN_COLOR = MessageColor.YELLOW;
	
	/**
	 * Sends a message to a player or a console
	 * @param wPlayer The player to send the message to or null if the console is the recipient
	 * @param message The message to send
	 */
	public static void sendRaw(final WPlayer<?> wPlayer, final String message) {
		if (wPlayer == null) {
			Wrapper.getInstance().getServer().runOnServer(new Consumer<WServer<?>>() {
				@Override
				public void accept(WServer<?> server) {
					server.sendConsoleMessage(message);
				}
			});
		} else {
			Wrapper.getInstance().getServer().runOnServer(new Consumer<WServer<?>>() {
				@Override
				public void accept(WServer<?> server) {
					wPlayer.sendMessage(message);
				}
			});
		}
	}
	
	/**
	 * Sends a tagged message to a player or a console
	 * @param wPlayer The player to send the message to or null if the console is the recipient
	 * @param message The message to send
	 */
	public static void sendTagged(final WPlayer<?> wPlayer, final String message) {
		sendRaw(wPlayer, CHAT_TAG + message);
	}
	
	/**
	 * Sends an informative message to a player or a console
	 * @param wPlayer The player to send the message to or null if the console is the recipient
	 * @param message The message to send
	 */
	public static void sendInfo(WPlayer<?> wPlayer, String message) {
		sendTagged(wPlayer, INFO_COLOR + " " + message);
	}
	
	/**
	 * Sends an informative message to a player or a console
	 * @param wPlayer The player to send the message to or null if the console is the recipient
	 * @param message The message to send
	 */
	public static void sendSuccess(WPlayer<?> wPlayer, String message) {
		sendTagged(wPlayer, SUCCESS_COLOR + " " + message);
	}
	
	public static void sendWarn(WPlayer<?> wPlayer, String message) {
		sendTagged(wPlayer, WARN_COLOR + " " + message);
	}
	
	/**
	 * Sends an error message to a player or a console
	 * @param wPlayer The player to send the message to or null if the console is the recipient
	 * @param message The message to send
	 */
	public static void sendError(WPlayer<?> wPlayer, String message) {
		sendTagged(wPlayer, ERROR_COLOR + " " + message);
	}
	
	/**
	 * Broadcasts a message to a collection of players
	 * @param players The players to send the message to
	 * @param message The message to send
	 */
	public static void broadcast(final Collection<WPlayer<?>> players, final String message) {
		if (players == null || message == null) return;
		
		Wrapper.getInstance().getServer().runOnServer(new Consumer<WServer<?>>() {
			@Override
			public void accept(WServer<?> t) {
				for (WPlayer<?> wp : players) {
					wp.sendMessage(message);
				}
			}
		});
	}
	
	/**
	 * Broadcasts an error to a set of players
	 * @param message The error to broadcast
	 */
	public static void broadcastError(Set<WPlayer<?>> players, String message) {
		broadcast(players, ERROR_COLOR + CHAT_TAG + " " + message);
	}
	
	/**
	 * Broadcasts a message to all op players and the console
	 * @param message The message to broadcast
	 */
	public static void broadcastOp(final String message) {
		if (message == null) return;
		
		Wrapper.getInstance().getServer().runOnServer(new Consumer<WServer<?>>() {
			@Override
			public void accept(WServer<?> t) {
				for (WPlayer<?> wp : t.getOnlinePlayers()) {
					if (wp.isOp()) {
						wp.sendMessage(message);
					}
				}
				t.sendConsoleMessage(message);
			}
		});
	}
	
	/**
	 * Broadcasts an error to all op players and the console
	 * @param message The error to broadcast
	 */
	public static void broadcastOpError(String message) {
		broadcastOp(ERROR_COLOR + CHAT_TAG + " " + message);
	}
	
	/**
	 * Broadcasts info to all op players and the console
	 * @param message The info to broadcast
	 */
	public static void broadcastOpInfo(String message) {
		broadcastOp(INFO_COLOR + CHAT_TAG + " " + message);
	}
	
	/**
	 * Broadcasts a success message to all op players and the console
	 * @param message The success message to broadcast
	 */
	public static void broadcastOpSuccess(String message) {
		broadcastOp(SUCCESS_COLOR + CHAT_TAG + " " + message);
	}
	
	/**
	 * Enum for formatted chat color codes
	 * @author Manel Navola
	 *
	 */
	public enum MessageColor {
		RED('c'), GOLD('6'), GREEN('a'), YELLOW('4');
		
		private String toString;
		
		MessageColor(char colorChar) {
			this.toString = String.valueOf(new char[] {'\u00A7', colorChar});
		}
		
		public String toString() {
			return toString;
		}
	}
	
}
