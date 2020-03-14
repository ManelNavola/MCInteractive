package com.manelnavola.mcinteractive.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import com.manelnavola.mcinteractive.core.wrappers.WPlayer;
import com.manelnavola.mcinteractive.core.wrappers.WServer;
import com.manelnavola.mcinteractive.core.wrappers.Wrapper;

/**
 * Utils class
 * 
 * @author Manel Navola
 *
 */
public class ChatUtils {

	private static final String CHAT_TAG = "[MCI]";

	/**
	 * Sends a message to a player or a console
	 * 
	 * @param wPlayer The player to send the message to or null if the console is
	 *                the recipient
	 * @param message The message to send
	 */
	public static void sendRaw(WPlayer<?> wPlayer, String message) {
		Objects.requireNonNull(message);

		if (wPlayer == null) {
			if (Wrapper.getInstance().getServer().isServerLoggingAvailable()) {
				// Send to server console
				Wrapper.getInstance().runOnServer(new Consumer<WServer<?>>() {
					@Override
					public void accept(WServer<?> server) {
						server.sendConsoleMessage(message);
					}
				});
			} else {
				// Send directly to console
				printToSystem(message, LogMessageType.INFO);
			}
		} else {
			// Send to player
			Wrapper.getInstance().runOnServer(new Consumer<WServer<?>>() {
				@Override
				public void accept(WServer<?> server) {
					wPlayer.sendMessage(message);
				}
			});
		}
	}

	/**
	 * Sends a tagged message to a player or a console
	 * 
	 * @param wPlayer The player to send the message to or null if the console is
	 *                the recipient
	 * @param message The message to send
	 */
	public static void send(WPlayer<?> wPlayer, String message, LogMessageType type) {
		Objects.requireNonNull(message);
		Objects.requireNonNull(type);

		sendRaw(wPlayer, type.getColor() + CHAT_TAG + " " + message);
	}
	
	/**
	 * Broadcasts a raw message to a collection of players
	 * 
	 * @param wPlayers The players to send the message to
	 * @param message  The message to send
	 */
	public static void broadcastRaw(Collection<WPlayer<?>> wPlayers, String message) {
		Objects.requireNonNull(wPlayers);
		Objects.requireNonNull(message);
		
		WPlayer<?>[] wPlayersArray = wPlayers.toArray(new WPlayer<?>[wPlayers.size()]);
		String sendMessage = new String(message);
		Wrapper.getInstance().runOnServer(new Consumer<WServer<?>>() {
			@Override
			public void accept(WServer<?> t) {
				for (WPlayer<?> wp : wPlayersArray) {
					wp.sendMessage(sendMessage);
				}
			}
		});
	}

	/**
	 * Broadcasts a message to a collection of players
	 * 
	 * @param wPlayers The players to send the message to
	 * @param message  The message to send
	 */
	public static void broadcast(Collection<WPlayer<?>> wPlayers, String message, LogMessageType type) {
		Objects.requireNonNull(wPlayers);
		Objects.requireNonNull(message);
		Objects.requireNonNull(type);

		WPlayer<?>[] wPlayersArray = wPlayers.toArray(new WPlayer<?>[wPlayers.size()]);
		String sendMessage = type.getColor() + CHAT_TAG + " " + message;
		Wrapper.getInstance().runOnServer(new Consumer<WServer<?>>() {
			@Override
			public void accept(WServer<?> t) {
				for (WPlayer<?> wp : wPlayersArray) {
					wp.sendMessage(sendMessage);
				}
			}
		});
	}

	/**
	 * Broadcasts a message to all op players and the console
	 * 
	 * @param message The message to broadcast
	 */
	public static void logOperators(String message, LogMessageType type) {
		Objects.requireNonNull(message);
		Objects.requireNonNull(type);

		String sendMessage = type.getColor() + CHAT_TAG + " " + message;
		if (Wrapper.getInstance().getServer().isServerLoggingAvailable()) {
			Wrapper.getInstance().runOnServer(new Consumer<WServer<?>>() {
				@Override
				public void accept(WServer<?> t) {
					for (WPlayer<?> wp : t.getOnlinePlayers()) {
						if (wp.isOp()) {
							wp.sendMessage(sendMessage);
						}
					}
					t.sendConsoleMessage(sendMessage);
				}
			});
		} else {
			printToSystem(sendMessage, type);
		}
	}

	/**
	 * Broadcasts an exception to all op players and the console
	 * 
	 * @param e The exception to broadcast
	 */
	public static void logOperators(Exception e) {
		Objects.requireNonNull(e);

		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		printWriter.flush();

		String sendMessage = LogMessageType.ERROR.getColor() + CHAT_TAG + " " + writer.toString();
		Wrapper.getInstance().runOnServer(new Consumer<WServer<?>>() {
			@Override
			public void accept(WServer<?> t) {
				for (WPlayer<?> wp : t.getOnlinePlayers()) {
					if (wp.isOp()) {
						wp.sendMessage(sendMessage);
					}
				}
			}
		});

		e.printStackTrace();
	}

	/**
	 * Prints a message to the standard output
	 * 
	 * @param message The message
	 * @param type    The message severity
	 */
	private static void printToSystem(String message, LogMessageType type) {
		String strippedMessage = MessageColor.stripColors(message);
		switch (type) {
		case INFO:
			System.out.println("[MCI Info] " + strippedMessage);
			break;
		case NICE:
			System.out.println("[MCI Success] " + strippedMessage);
			break;
		case WARN:
			System.out.println("[MCI Warn] " + strippedMessage);
			break;
		case ERROR:
			System.err.println("[MCI Error] " + strippedMessage);
			break;
		}
	}

	/**
	 * Enum for formatted chat color codes
	 * 
	 * @author Manel Navola
	 *
	 */
	public enum MessageColor {
		BLACK('0'), DARK_BLUE('1'), DARK_GREEN('2'), DARK_AQUA('3'), DARK_RED('4'), DARK_PURPLE('5'), GOLD('6'),
		GRAY('7'), DARK_GRAY('8'), BLUE('9'), GREEN('a'), AQUA('b'), RED('c'), LIGHT_PURPLE('d'), YELLOW('e'),
		WHITE('f'), OBFUSCATED('k'), BOLD('l'), STRIKETHROUGH('m'), UNDERLINE('n'), ITALIC('o'), RESET('r'),;

		private String toString;

		/**
		 * Main constructor
		 * 
		 * @param colorChar The char that references the color
		 */
		MessageColor(char colorChar) {
			this.toString = String.valueOf(new char[] { '§', colorChar });
		}

		/**
		 * Removes all colors from a string
		 * 
		 * @param message The string to remove the colors from
		 * @return String without colors
		 */
		public static String stripColors(String message) {
			Objects.requireNonNull(message);

			return message.replaceAll("§[A-z0-9]", "");
		}

		public String toString() {
			return toString;
		}
	}

	/**
	 * Enum for message logging types
	 * 
	 * @author Manel Navola
	 *
	 */
	public enum LogMessageType {
		INFO(MessageColor.GOLD), NICE(MessageColor.GREEN), WARN(MessageColor.YELLOW), ERROR(MessageColor.RED);

		private MessageColor color;

		LogMessageType(MessageColor color) {
			this.color = color;
		}

		/**
		 * Gets the color of the message log
		 * 
		 * @return
		 */
		public MessageColor getColor() {
			return color;
		}
	}

}
