package com.manelnavola.mcinteractive.chat;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.manelnavola.mcinteractive.generic.PlayerData;
import com.manelnavola.mcinteractive.generic.PlayerManager;
import com.manelnavola.twitchbotx.TwitchUser;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Vote {
	
	private static final int[] basicDuration = new int[] {8, 70, 8};
	private static final ChatColor[][] optionColors = new ChatColor[][] {
		{ChatColor.GREEN},
		{ChatColor.GREEN, ChatColor.DARK_AQUA},
		{ChatColor.LIGHT_PURPLE, ChatColor.GREEN, ChatColor.DARK_AQUA},
		{ChatColor.LIGHT_PURPLE, ChatColor.GREEN, ChatColor.DARK_AQUA, ChatColor.GOLD},
		{ChatColor.AQUA, ChatColor.LIGHT_PURPLE, ChatColor.GREEN, ChatColor.DARK_AQUA, ChatColor.GOLD},
		{ChatColor.AQUA, ChatColor.LIGHT_PURPLE, ChatColor.GREEN, ChatColor.RED, ChatColor.DARK_AQUA, ChatColor.GOLD}
	};
	
	private Plugin plugin;
	private String channelName;
	private long voteTime;
	private long voteTimeEnd;
	private List<String> options;
	private String[] procOptions;
	private int procOptionsSize = 0;
	private List<Player> playerList;
	private boolean disposed = false;
	
	private boolean allowAnswers = false;
	
	private int guiTicks;
	private int procOptionsIter;
	private int delayedTaskID;
	private int repeatedTaskID;
	private int removedTaskID;
	private ChatColor[] chosenColors;
	private int winnerData[];
	private Map<TwitchUser, Integer> userChoice = new HashMap<>();
	
	public Vote(Plugin plg, String ch, float vt, List<String> opt, List<Player> pl) {
		plugin = plg;
		channelName = ch;
		voteTime = (long) (vt*1000.0);
		options = opt;
		playerList = pl;
		
		// Broadcast voting notice
		sendTitle(ChatColor.ITALIC + "" + ChatColor.LIGHT_PURPLE + "Vote", ChatColor.GREEN + "in Twitch chat!",
				basicDuration[0], basicDuration[1], basicDuration[2]);
		
		delayedTaskID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				voteTimeEnd = System.currentTimeMillis() + voteTime;
				procOptions = new String[options.size()];
				for (int i = 0; i < options.size(); i++) { procOptions[i] = ""; }
				chosenColors = optionColors[options.size() - 1];
				
				// Fill processed options array
				int totalLen = 0;
				int iter = 0;
				boolean first = true;
				for (int i = 0; i < options.size(); i++) {
					totalLen += options.get(i).length() + 3;
					if (totalLen >= 40 && (!first)) {
						totalLen = 0;
						procOptions[iter] = procOptions[iter].substring(0, procOptions[iter].length() - 3);
						iter++;
					}
					procOptions[iter] += chosenColors[i] + options.get(i) + ChatColor.WHITE + " | ";
					first = false;
				}
				procOptions[iter] = procOptions[iter].substring(0, procOptions[iter].length() - 3);
				procOptionsSize = iter;
				
				guiTicks = 0;
				procOptionsIter = 0;
				winnerData = new int[2];
				
				repeatedTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
					
					private boolean ended = false;
					private boolean firstTime = true;
					
					@Override
					public void run() {
						if (firstTime) {
							allowAnswers = true;
							firstTime = false;
						}
						if (voteTimeEnd > System.currentTimeMillis()) {
							// Looping
							guiTicks++;
							updateTitle();
							updateActionBar();
						} else if (!ended) {
							// Countdown just ended
							ended = true;
							allowAnswers = false;
							calculateWinner();
							sendTitle("", ChatColor.WHITE + "The winner is " + chosenColors[winnerData[0]] + options.get(winnerData[0]) +
									ChatColor.WHITE + " with " + winnerData[1] + " votes!",
									basicDuration[0], basicDuration[1], basicDuration[2]);
							removedTaskID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

								@Override
								public void run() {
									dispose();
								}
								
							}, basicDuration[0] + basicDuration[1] + basicDuration[2]);
						}
					}
					
				}, 0L, 1L);
			}
			
		}, basicDuration[0] + basicDuration[1] + basicDuration[2]);
	}
	
	private void sendTitle(String t, String s, int a, int b, int c) {
		for (Player p : playerList) {
			p.sendTitle(t, s, a, b, c);
		}
	}
	
	private void sendActionBar(String txt) {
		for (Player p : playerList) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(txt).create());
		}
	}
	
	public void updateTitle() {
		if (guiTicks%40 == 0) {
			procOptionsIter++;
			if (procOptionsIter > procOptionsSize) procOptionsIter = 0;
		}
		sendTitle("", procOptions[procOptionsIter], 0, basicDuration[1], basicDuration[2]);
	}
	
	public void updateActionBar() {
		DecimalFormat df = new DecimalFormat("0.#");
		String send = ""; // 40 '|'
		double[] counts = new double[options.size()];
		if (userChoice.isEmpty()) {
			send = ChatColor.WHITE + "|||||||||||||||||||||||||||||||||||||||| ";
		} else {
			double[] perCounts = new double[options.size()];
			for (TwitchUser tu : userChoice.keySet()) {
				counts[userChoice.get(tu)] += 1.0/userChoice.size();
				perCounts[userChoice.get(tu)] += 1.0/userChoice.size();
			}
			for (int i = 1; i < perCounts.length; i++) {
				perCounts[i] += perCounts[i-1];
			}
			send = "";
			for (int i = 0; i < 40; i++) {
				double rate = i/40.0;
				int chosen = 0;
				for (int e = 0; e < perCounts.length; e++) {
					if (rate < perCounts[e]) {
						chosen = e;
						break;
					}
				}
				send += chosenColors[chosen] + "|";
			}
			
			int maxCount = 0;
			for (int i = 0; i < counts.length; i++) {
				counts[i] = (int) Math.floor(counts[i]*1000);
				maxCount += counts[i];
			}
			int max = 0;
			if (max < 1000) {
				for (int i = 1; i < counts.length; i++) {
					if (counts[max] < counts[i]) max = i;
				}
			}
			counts[max] += (1000 - maxCount);
		}
		
		send += " ";
		for (int i = 0; i < counts.length; i++) {
			send += chosenColors[i] + "" + df.format(counts[i]/10.0) + "% ";
		}
		
		send = "Time left: " + millisToTime(voteTimeEnd - System.currentTimeMillis()) + " " + send;
		send += ChatColor.RESET + "(" + userChoice.size() + " votes)";
		sendActionBar(send);
	}
	
	public void calculateWinner() {
		int[] counts = new int[options.size()];
		for (TwitchUser tu : userChoice.keySet()) {
			counts[userChoice.get(tu)] ++;
		}
		int max = 0;
		for (int i = 1; i < counts.length; i++) {
			if (counts[i] > counts[max]) {
				max = i;
			}
			if (counts[i] == counts[max] && Math.random() < 0.5) {
				max = i;
			}
		}
		userChoice.clear();
		winnerData[0] = max;
		winnerData[1] = counts[max];
	}
	
	public void extend(float time) {
		voteTimeEnd += time*1000.0;
	}
	
	public void finish() {
		voteTimeEnd = 0;
	}
	
	public void dispose() {
		Bukkit.getScheduler().cancelTask(delayedTaskID);
		Bukkit.getScheduler().cancelTask(repeatedTaskID);
		Bukkit.getScheduler().cancelTask(removedTaskID);
		sendTitle("", "", 0, 0, 0);
		sendActionBar("");
		disposed = true;
	}

	public void process(TwitchUser tu, String channel, String message) {
		if (!allowAnswers) return;
		
		if (!channelName.equals(channel)) return;
		
		message = message.toLowerCase();
		if (options.contains(message)) {
			String msg;
			if (userChoice.containsKey(tu)) {
				msg = "changed vote to " + message;
			} else {
				msg = "voted " + message;
			}
			for (Player p : playerList) {
				PlayerData pd = PlayerManager.getPlayerData(p);
				if (pd.getConfig("showvotes")) {
					String nickname = ChatManager.parseUsername(pd, tu, false);
					String pm = ChatManager.parseMessage(pd, tu, msg);
					p.sendMessage(nickname + " " + pm);
				}
			}
			userChoice.put(tu, options.indexOf(message));
		}
	}
	
	public boolean isDisposed() {
		return disposed;
	}

	public String getOptionChatColor(String message) {
		for (int i=0; i < options.size(); i++) {
			if (options.get(i).equals(message.toLowerCase())) {
				return "" + chosenColors[i];
			}
		}
		return null;
	}
	
	private String ensureLen(int i) {
		String s = "" + i;
		if (s.length() < 2) {
			return "0" + s;
		} else {
			return s;
		}
	}
	
	private String millisToTime(long ms) {
		int totalSeconds = (int) Math.floor(ms/1000.0);
		int seconds = totalSeconds%60;
		int totalMinutes = (int) Math.floor(totalSeconds/60.0);
		int minutes = totalMinutes%60;
		int hours = (int) Math.floor(totalSeconds/3600.0);
		if (hours == 0) {
			if (minutes == 0) {
				return "" + ensureLen(seconds);
			} else {
				return ensureLen(minutes) + ":" + ensureLen(seconds);
			}
		} else {
			return ensureLen(hours) + ":" + ensureLen(minutes) + ":" + ensureLen(seconds);
		}
	}

	public boolean isVotingAllowed() {
		return allowAnswers;
	}

	public String getTwitchUserVote(TwitchUser tu) {
		if (userChoice.containsKey(tu)) {
			return options.get(userChoice.get(tu));
		} else {
			return null;
		}
	}
	
}
