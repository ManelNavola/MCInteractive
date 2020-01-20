package com.manelnavola.mcinteractive.voting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.manelnavola.mcinteractive.generic.ConnectionManager;
import com.manelnavola.mcinteractive.utils.Log;
import com.manelnavola.mcinteractive.utils.MessageSender;
import com.manelnavola.mcinteractive.voting.Vote.VoteType;
import com.manelnavola.twitchbotx.TwitchUser;

import net.md_5.bungee.api.ChatColor;

public class VoteManager {
	
	private static Plugin plugin;
	private static Map<String, List<Vote>> channelVotes;
	private static Map<Player, Vote> playerVotes;
	private static int voteLoop;
	private static Lock voteLock = new ReentrantLock();
	
	public static void init(Plugin plg) {
		plugin = plg;
		playerVotes = new HashMap<>();
		channelVotes = new HashMap<>();
		
		voteLoop = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				voteLock.lock();
				
				try {
					for (List<Vote> vl : channelVotes.values()) {
						for (Vote v : vl) {
							if (v.timeStep()) {
								removeVote(v);
							}
						}
					}
				} finally {
					voteLock.unlock();
				}
			}
		}, 0L, 20L);
	}
	
	private static void addVote( Vote v) {
		voteLock.lock();
		
		try {
			for (Player p : v.getPlayerList()) {
				playerVotes.put(p, v);
			}
			List<Vote> vl = channelVotes.get(v.getChannel());
			if (vl == null) {
				vl = new ArrayList<>();
				vl.add(v);
				channelVotes.put(v.getChannel(), vl);
			} else {
				vl.add(v);
			}
		} finally {
			voteLock.unlock();
		}
	}
	
	private static void removeVote(Vote v) {
		voteLock.lock();
		
		try {
			for (Player p : v.getPlayerList()) {
				playerVotes.remove(p);
			}
			List<Vote> vl = channelVotes.get(v.getChannel());
			if (vl != null) vl.remove(v);
			if (vl.isEmpty()) {
				channelVotes.remove(v.getChannel());
			}
		} finally {
			voteLock.unlock();
		}
	}
	
	public static void createChannelVote(String channel, int duration, String title, String subtitle, List<String> options) {
		List<Player> pl = new ArrayList<>();
		for (Player p : ConnectionManager.getChannelPlayers(channel)) {
			if (!isActive(p)) pl.add(p);
		}
		Vote v = new Vote(VoteType.CHANNEL, pl, channel, duration, title, subtitle, options);
		addVote(v);
	}
	
	public static void createPlayerVote(Player p, int duration, List<String> options) {
		if (isActive(p)) {
			MessageSender.error(p, "A vote is already operative!");
		} else {
			if (ConnectionManager.getPlayerConnection(p) == null) {
				MessageSender.error(p, "You must be connected to a channel!");
				return;
			}
			List<Player> pl = new ArrayList<Player>();
			pl.add(p);
			Vote v = new Vote(VoteType.PLAYER, pl, ConnectionManager.getPlayerConnection(p).getChannel(), duration,
					ChatColor.ITALIC + "" + ChatColor.LIGHT_PURPLE + "Vote",
					ChatColor.GREEN + "in twitch chat!", options);
			addVote(v);
		}
	}
	
	public static void process(TwitchUser tu, String channel, String message) {
		for (Vote v : channelVotes.get(channel)) {
			v.process(tu, channel, message);
		}
	}
	
	public static void cancelVote(Player p, VoteType vt) {
		if (isActive(p)) {
			// TODO cannot cancel events
			Vote v = playerVotes.get(p);
			if (v.getVoteType().getValue() > vt.getValue()) {
				// DO STUFF OR MAYBE NO VALUE AA
			} else {
				removeVote(v);
			}
		} else {
			MessageSender.error(p, "There is no currently running vote!");
		}
	}

	public static void endVote(Player p, VoteType vt) {
		if (isActive(p)) {
			playerVotes.get(p).finish();
			Vote v = playerVotes.get(p);
			removeVote(v);
		} else {
			MessageSender.error(p, "There is no currently running vote!");
		}
	}
	
	public static boolean isActive(Player p) {
		return playerVotes.containsKey(p);
	}
	
	public static void dispose() {
		Bukkit.getScheduler().cancelTask(voteLoop);
	}

	public static Vote getVote(Player p) {
		return playerVotes.get(p);
	}
	
}
