package com.manelnavola.mcinteractive.generic;

import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.manelnavola.mcinteractive.utils.Log;

public final class ConnectionManager {
	
	//private static List<Player> botPlayers;
	//private static Map<Player, String> playerConnections;
	private static TwitchBotMCI anonTwitchBotX;
	//private static Object safetyLock = new Object();
	
	/**
	 * Initializes the Manager values
	 */
	public static void init(Plugin plg) {
		try {
			anonTwitchBotX = new TwitchBotMCI();
		} catch (Exception e) {
			Log.error("Could not start bot! Restart the plugin and if the issue persists contact the developer");
			Log.error(e);
		}
		//botPlayers = new ArrayList<>();
		//playerConnections = new HashMap<>();
	}
	
	/**
	 * Disposes the current Manager
	 */
	public static void dispose() {
		// Close listen-only bot
		anonTwitchBotX.dispose();
	}
	
	public static TwitchBotMCI getAnonBot() {
		return anonTwitchBotX;
	}
	
	public static List<Player> getChannelPlayers(String ch) {
		return anonTwitchBotX.getChannelPlayers(ch);
	}
	
	public static void listen(Player p, String ch) {
		anonTwitchBotX.connect(p, ch);
		/*LoggingManager.l("Attempting to listen " + p.getName() + " to " + ch);
		
		// Leave if player is already connected
		String listeningChannel = getPlayerChannel(p);
		if (listeningChannel != null) {
			if (listeningChannel.equals(ch)) {
				MessageSender.warn(p, "You are already connected to " + ch + "!");
				return;
			} else {
				leave(p);
			}
		}
		listeningChannel = ch;
		
		synchronized(safetyLock) {
			// Create new player connection
			playerConnections.put(p, listeningChannel);
			
			// Connect player to channel
			botPlayers.add(p);
			anonTwitchBotX.connect(p, ch);
		}*/
	}
	
	public static void leave(Player p) {
		anonTwitchBotX.disconnect(p);
		//leave(p, true);
	}
	
	/*public static void leave(Player p, boolean logErr) {
		String listeningChannel = getPlayerChannel(p);
		if (listeningChannel == null) {
			MessageSender.err(p, "You are not connected to a channel!");
		} else {
			synchronized(safetyLock) {
				anonTwitchBotX.disconnect(p);
				playerConnections.remove(p);
				botPlayers.remove(p);
			}
		}
	}*/
	
	public static Set<String> getConnectedChannels() {
		return anonTwitchBotX.getConnectedChannels();
	}
	
	public static String getPlayerChannel(Player p) {
		return anonTwitchBotX.getPlayerChannel(p);
		/*synchronized(safetyLock) {
			return playerConnections.get(p);
		}*/
	}
	
	public static boolean isConnected(Player p) {
		return anonTwitchBotX.getPlayerChannel(p) != null;
		/*synchronized(safetyLock) {
			return playerConnections.containsKey(p);
		}*/
	}
	
}
