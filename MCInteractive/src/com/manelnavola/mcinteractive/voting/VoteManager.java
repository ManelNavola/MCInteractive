package com.manelnavola.mcinteractive.voting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.manelnavola.mcinteractive.adventure.EventManager;
import com.manelnavola.mcinteractive.adventure.customevents.VoteRunnable;
import com.manelnavola.mcinteractive.generic.ConnectionManager;
import com.manelnavola.mcinteractive.generic.PlayerConnection;
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
					List<Vote> toRemove = new ArrayList<>();
					for (List<Vote> vl : channelVotes.values()) {
						for (Vote v : vl) {
							if (v.timeStep()) {
								toRemove.add(v);
							}
						}
					}
					for (Vote v : toRemove) {
						removeVote(v);
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
	
	public static void process(TwitchUser tu, String channel, String message) {
		for (Vote v : channelVotes.get(channel)) {
			v.process(tu, channel, message);
		}
	}
	
	// EVENT VOTES
	// Start
	public static void startEventVote(String channel, String desc, VoteRunnable run, List<String> options) {
		List<Player> pl = new ArrayList<>();
		for (Player p : ConnectionManager.getChannelPlayers(channel)) {
			Vote v = playerVotes.get(p);
			if (v == null && !EventManager.contains(p)) {
				pl.add(p);
			}
		}
		if (pl.isEmpty()) return;
		Vote v = new Vote(VoteType.EVENT, pl, channel, EventManager.VOTING_LENGTH_S,
				ChatColor.ITALIC + "" + ChatColor.AQUA + "Event Vote",
				ChatColor.GREEN + desc,
				options, run);
		addVote(v);
	}
	
	// CHANNEL VOTES
	// Start
	public static void startChannelVote(CommandSender cs, String channel, int duration,
			List<String> options, boolean force) {
		List<Vote> vl = channelVotes.get(channel);
		if (vl != null) {
			for (Vote v : vl) {
				if (v.getVoteType() == VoteType.CHANNEL) {
					MessageSender.err(cs, "There's already a channel vote running!");
					return;
				}
			}
		}
		List<Player> pl = new ArrayList<>();
		int st = 0;
		for (Player p : ConnectionManager.getChannelPlayers(channel)) {
			Vote v = playerVotes.get(p);
			if (v == null) {
				pl.add(p);
				st++;
			} else {
				if (force) {
					MessageSender.warn(p, "Your vote has been cancelled due to a channel vote!");
					removeVote(v);
					pl.add(p);
					st++;
				}
			}
		}
		if (st == 0) {
			MessageSender.err(cs, "There are no available players in this channel!");
			return;
		}
		Vote v = new Vote(VoteType.CHANNEL, pl, channel, duration,
				ChatColor.ITALIC + "" + ChatColor.LIGHT_PURPLE + "Channel Vote",
				ChatColor.GREEN + "Vote in twitch chat!",
				options);
		addVote(v);
		
		if (st == 1) {
			MessageSender.nice(cs, "Started channel vote on " + channel + " for 1 player!");
		} else {
			MessageSender.nice(cs, "Started channel vote on " + channel + " for " + st + " players!");
		}
	}
	public static void startChannelVote(Player p, int duration, List<String> options, boolean force) {
		PlayerConnection pc = ConnectionManager.getPlayerConnection(p);
		if (pc != null) {
			String ch = pc.getChannel();
			List<Vote> vl = channelVotes.get(ch);
			if (vl != null) {
				for (Vote v : vl) {
					if (v.getVoteType() == VoteType.CHANNEL) {
						MessageSender.err(p, "There's already a channel vote running!");
						return;
					}
				}
			}
			List<Player> pl = new ArrayList<>();
			int st = 0;
			for (Player pp : ConnectionManager.getChannelPlayers(ch)) {
				Vote v = playerVotes.get(pp);
				if (v == null) {
					pl.add(pp);
					st++;
				} else {
					if (force) {
						MessageSender.warn(p, "Your vote has been cancelled due to a channel vote!");
						removeVote(v);
						pl.add(pp);
						st++;
					}
				}
			}
			if (st == 0) {
				MessageSender.err(p, "There are no available players in this channel!");
				return;
			}
			Vote v = new Vote(VoteType.CHANNEL, pl, ch, duration,
					ChatColor.ITALIC + "" + ChatColor.LIGHT_PURPLE + "Channel Vote",
					ChatColor.GREEN + "Vote in twitch chat!",
					options);
			addVote(v);
			if (st == 1) {
				MessageSender.nice(p, "Started channel vote for 1 player!");
			} else {
				MessageSender.nice(p, "Started channel vote for " + st + " players!");
			}
		} else {
			MessageSender.err(p, "You are not connected to a channel!");
		}
	}
	// End
	public static void endChannelVote(CommandSender cs, String ch) {
		List<Vote> vl = channelVotes.get(ch);
		if (vl != null) {
			for (Vote v : vl) {
				if (v.getVoteType() == VoteType.CHANNEL) {
					v.finish();
					removeVote(v);
					MessageSender.nice(cs, ch + "'s vote ended!");
					return;
				}
			}
		}
		MessageSender.err(cs, ch + " has no currently running channel vote!");
	}
	public static void endChannelVote(Player p) {
		PlayerConnection pc = ConnectionManager.getPlayerConnection(p);
		if (pc != null) {
			endChannelVote(p, pc.getChannel());
		} else {
			MessageSender.err(p, "You are not connected to a channel!");
		}
	}
	// Cancel
	public static void cancelChannelVote(CommandSender cs, String ch) {
		List<Vote> vl = channelVotes.get(ch);
		if (vl != null) {
			for (Vote v : vl) {
				if (v.getVoteType() == VoteType.CHANNEL) {
					removeVote(v);
					MessageSender.nice(cs, ch + "'s vote cancelled!");
					return;
				}
			}
		}
		MessageSender.err(cs, ch + " has no currently running channel vote!");
	}
	public static void cancelChannelVote(Player p) {
		PlayerConnection pc = ConnectionManager.getPlayerConnection(p);
		if (pc != null) {
			cancelChannelVote(p, pc.getChannel());
		} else {
			MessageSender.err(p, "You are not connected to a channel!");
		}
	}
	
	// PLAYER VOTES
	// Start
	public static void startPlayerVote(Player p, int duration, List<String> options) {
		if (isActive(p)) {
			MessageSender.err(p, "A vote is already operative!");
		} else {
			if (ConnectionManager.getPlayerConnection(p) == null) {
				MessageSender.err(p, "You must be connected to a channel!");
				return;
			}
			List<Player> pl = new ArrayList<Player>();
			pl.add(p);
			addVote(new Vote(VoteType.PLAYER, pl, ConnectionManager.getPlayerConnection(p).getChannel(), duration,
					ChatColor.ITALIC + "" + ChatColor.LIGHT_PURPLE + "Vote",
					ChatColor.GREEN + "in twitch chat!", options));
			MessageSender.nice(p, "Vote started!");
		}
	}
	// Cancel
	public static void cancelPlayerVote(Player p) {
		Vote v = playerVotes.get(p);
		if (v != null) {
			removeVote(v);
			MessageSender.nice(p, "Vote cancelled!");
		} else {
			MessageSender.err(p, "There is no currently running vote!");
		}
	}
	// End
	public static void endPlayerVote(Player p) {
		Vote v = playerVotes.get(p);
		if (v != null) {
			v.finish();
			removeVote(v);
			MessageSender.nice(p, "Ended vote!");
		} else {
			MessageSender.err(p, "There is no currently running vote!");
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
