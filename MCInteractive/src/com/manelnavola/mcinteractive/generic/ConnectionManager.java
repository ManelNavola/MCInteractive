package com.manelnavola.mcinteractive.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.manelnavola.mcinteractive.utils.Log;
import com.manelnavola.mcinteractive.utils.MessageSender;

public final class ConnectionManager {
	
	private static Map<TwitchBotMCI, List<Player>> botToPlayers;
	private static Map<Player, PlayerConnection> playerConnections;
	private static TwitchBotMCI anonTwitchBotX;
	
	/**
	 * Initializes the Manager values
	 */
	public static void init(Plugin plg) {
		anonTwitchBotX = new TwitchBotMCI(plg);
		boolean connected = anonTwitchBotX.waitForStartup();
		if (!connected) {
			// Try one more time...
			Log.warn("Could not connect anon bot to Twitch servers, retrying...");
			anonTwitchBotX = new TwitchBotMCI(plg);
			connected = anonTwitchBotX.waitForStartup();
			if (!connected) {
				Log.error("Could not connect anon bot to Twitch servers!");
			}
		}
		botToPlayers = new HashMap<>();
		playerConnections = new HashMap<>();
		botToPlayers.put(anonTwitchBotX, new ArrayList<>());
	}
	
	/**
	 * Disposes the current Manager
	 */
	public static void dispose() {
		// Close listen-only bot
		anonTwitchBotX.dispose();
	}
	
	public static void listen(Player p, String ch) {
		// Leave if player is already connected
		PlayerConnection pbc = getPlayerConnection(p);
		if (pbc != null) {
			if (pbc.getChannel().equals(ch)) {
				MessageSender.warn(p, "You are already connected to " + ch + "!");
				return;
			} else {
				leave(p);
			}
		}
		
		// Create new player connection
		pbc = new PlayerConnection(anonTwitchBotX, ch);
		playerConnections.put(p, pbc);
		
		// Connect anonymous TwitchBotX if it's not connected already
		// if (!anonTwitchBotX.isConnectedTo(ch)) anonTwitchBotX.joinChannel(ch);
		
		// Connect player to channel
		botToPlayers.get(anonTwitchBotX).add(p);
		anonTwitchBotX.connect(p, ch);
	}
	
	public static void leave(Player p) {
		leave(p, true);
	}
	
	public static void leave(Player p, boolean logErr) {
		// Leave if player is already connected
		PlayerConnection pc = getPlayerConnection(p);
		if (pc != null) {
			TwitchBotMCI tbmci = pc.getTwitchBotMCI();
			tbmci.disconnect(p);
			playerConnections.remove(p);
			botToPlayers.get(tbmci).remove(p);
			if (botToPlayers.get(tbmci).isEmpty() && tbmci != anonTwitchBotX) {
				tbmci.dispose();
				botToPlayers.remove(tbmci);
			}
		}
	}
	
	public static PlayerConnection getPlayerConnection(Player p) {
		return playerConnections.get(p);
	}
	
	public static boolean isConnected(Player p) {
		return playerConnections.containsKey(p);
	}
	
}
