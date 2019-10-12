package com.manelnavola.mcinteractive.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.manelnavola.mcinteractive.generic.ConnectionManager;
import com.manelnavola.mcinteractive.utils.MessageSender;
import com.manelnavola.twitchbotx.TwitchUser;

public class VoteManager {
	
	private static Plugin plugin;
	private static Map<Player, Vote> playerVotes = new HashMap<>();
	
	public static void init(Plugin plg) {
		plugin = plg;
	}

	public static void createPlayerVote(Player p, String desc, float duration, List<String> options) {
		if (playerVotes.containsKey(p)) {
			MessageSender.error(p, "A vote is already operative!");
		} else {
			List<Player> pl = new ArrayList<>();
			pl.add(p);
			playerVotes.put(p, new Vote(plugin, ConnectionManager.getPlayerConnection(p).getChannel(),
					desc, duration, options, pl));
		}
	}

	public static void cancelVote(Player p) {
		if (playerVotes.containsKey(p)) {
			playerVotes.get(p).dispose();
		} else {
			MessageSender.error(p, "There is no currently running vote!");
		}
	}

	public static void endVote(Player p) {
		if (playerVotes.containsKey(p)) {
			playerVotes.get(p).finish();
		} else {
			MessageSender.error(p, "There is no currently running vote!");
		}
	}
	
	public static void remove(Player p) {
		if (playerVotes.containsKey(p)) {
			playerVotes.get(p).dispose();
		}
	}
	
	public static boolean isActive(Player p) {
		if (playerVotes.containsKey(p)) {
			Vote v = playerVotes.get(p);
			if (v.isDisposed()) {
				playerVotes.remove(p);
				return false;
			} else {
				return v.isVotingAllowed();
			}
		}
		return false;
	}
	
	public static void dispose() {
		for (Vote v : playerVotes.values()) {
			v.dispose();
		}
		playerVotes.clear();
	}

	public static void process(TwitchUser tu, String channel, String message) {
		for (Vote v : playerVotes.values()) {
			v.process(tu, channel, message);
		}
	}

	public static Vote getVote(Player p) {
		if (playerVotes.containsKey(p)) {
			return playerVotes.get(p);
		} else {
			return null;
		}
	}
	
}
