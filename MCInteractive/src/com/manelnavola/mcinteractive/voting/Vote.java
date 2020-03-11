package com.manelnavola.mcinteractive.voting;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.manelnavola.mcinteractive.generic.ChatManager;
import com.manelnavola.mcinteractive.generic.PlayerData;
import com.manelnavola.mcinteractive.generic.PlayerManager;
import com.manelnavola.mcinteractive.utils.ActionBar;
import com.manelnavola.twitchbotx.TwitchUser;

public class Vote {
	
	public enum VoteType {
		PLAYER, CHANNEL, EVENT
	}
	
	private VoteType voteType;
	protected int duration; // Seconds
	protected long time;
	private String channel;
	private List<Player> playerList;
	private List<String> options;
	private Map<String, Integer> userChoice;
	private String[] procOptions;
	private int procOptionsSize, procOptionsIter = 0;
	private ChatColor[] chosenColors;
	
	private static final ChatColor[][] optionColors = new ChatColor[][] {
		{ChatColor.GREEN},
		{ChatColor.GREEN, ChatColor.DARK_AQUA},
		{ChatColor.LIGHT_PURPLE, ChatColor.GREEN, ChatColor.DARK_AQUA},
		{ChatColor.LIGHT_PURPLE, ChatColor.GREEN, ChatColor.DARK_AQUA, ChatColor.GOLD},
		{ChatColor.AQUA, ChatColor.LIGHT_PURPLE, ChatColor.GREEN, ChatColor.DARK_AQUA, ChatColor.GOLD},
		{ChatColor.AQUA, ChatColor.LIGHT_PURPLE, ChatColor.GREEN, ChatColor.RED, ChatColor.DARK_AQUA, ChatColor.GOLD}
	};
	
	public Vote(VoteType vt, List<Player> pl, String ch, int dur,
			String title, String subtitle, List<String> opt) {
		voteType = vt;
		playerList = pl;
		channel = ch;
		time = -4;
		duration = dur;
		options = opt;
		
		chosenColors = optionColors[options.size() - 1];
		userChoice = new HashMap<>();
		
		// Fill processed options array
		procOptions = new String[options.size()];
		for (int i = 0; i < options.size(); i++) { procOptions[i] = ""; }
		int totalLen = 0;
		int iter = 0;
		boolean first = true;
		for (int i = 0; i < options.size(); i++) {
			totalLen += options.get(i).length() + 3;
			if (totalLen >= 40 && !first) {
				totalLen = 0;
				procOptions[iter] = procOptions[iter].substring(0, procOptions[iter].length() - 2);
				iter++;
			}
			procOptions[iter] += chosenColors[i] + options.get(i) + ChatColor.WHITE + ", ";
			first = false;
		}
		procOptions[iter] = procOptions[iter].substring(0, procOptions[iter].length() - 2);
		procOptionsSize = iter;
		
		for (Player p : playerList) {
			p.sendTitle(title, subtitle, 10, 50, 10);
			ActionBar.sendHotBarMessage(p, "");
		}
	}

	public void process(TwitchUser tu, String ch, String message) {
		if (time < 0) return;
		if (!channel.equals(ch)) return;
		message = message.toLowerCase();
		if (options.contains(message)) {
			String msg;
			if (userChoice.containsKey(tu.getUUID())) {
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
			userChoice.put(tu.getUUID(), options.indexOf(message));
		}
	}
	
	public void updateTitle() {
		if (time%3 == 0) {
			procOptionsIter++;
			if (procOptionsIter > procOptionsSize) procOptionsIter = 0;
		}
		for (Player p : playerList) {
			p.sendTitle("", procOptions[procOptionsIter], 0, 40, 10);
		}
	}
	
	public void updateActionBar() {
		DecimalFormat df = new DecimalFormat("0.#");
		String send = ""; // 40 '|'
		double[] counts = new double[options.size()];
		if (userChoice.isEmpty()) {
			send = ChatColor.WHITE + "|||||||||||||||||||||||||||||||||||||||| ";
		} else {
			double[] perCounts = new double[options.size()];
			for (String tuId : userChoice.keySet()) {
				counts[userChoice.get(tuId)] += 1.0/userChoice.size();
				perCounts[userChoice.get(tuId)] += 1.0/userChoice.size();
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
		
		send = "Time left: " + secsToTime((int) (duration - time)) + " " + send;
		if (userChoice.size() == 1) {
			send += ChatColor.RESET + "(1 vote)";
		} else {
			send += ChatColor.RESET + "(" + userChoice.size() + " votes)";
		}
		
		for (Player p : playerList) {
			ActionBar.sendHotBarMessage(p, send);
		}
	}
	
	public boolean timeStep() {
		time++;
		if (time >= 0) {
			updateTitle();
			updateActionBar();
		}
		if (time >= duration) {
			finish();
			return true;
		}
		return false;
	}
	
	public String getChannel() { return channel; }
	public List<Player> getPlayerList() { return playerList; }

	public String finish() {
		// Countdown just ended
		time = -9999;
		int[] results = calculateWinner();
		String ts = "";
		if (results[1] == 1) {
			ts = ChatColor.WHITE + "The winner is " + chosenColors[results[0]] + options.get(results[0]) +
					ChatColor.WHITE + " with 1 vote!";
		} else {
			ts = ChatColor.WHITE + "The winner is " + chosenColors[results[0]] + options.get(results[0]) +
					ChatColor.WHITE + " with " + results[1] + " votes!";
		}
		for (Player p : playerList) {
			p.sendTitle("", ts, 10, 70, 10);
		}
		return options.get(results[0]);
	}
	
	private int[] calculateWinner() {
		int[] counts = new int[options.size()];
		for (String tuId : userChoice.keySet()) {
			counts[userChoice.get(tuId)] ++;
		}
		int max = -1;
		List<Integer> maxes = new ArrayList<>();
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] > max) {
				max = counts[i];
				maxes.clear();
				maxes.add(i);
			} else if (counts[i] == max) {
				maxes.add(i);
			}
		}
		if (maxes.size() > 1) {
			max = maxes.get((int) (Math.random()*maxes.size()));
		} else {
			max = maxes.get(0);
		}
		userChoice.clear();
		return new int[] {max, counts[max]};
	}
	
	private String secsToTime(int s) {
		if (s < 0) s = 0;
		int seconds = s%60;
		int totalMinutes = (int) Math.floor(s/60.0);
		int minutes = totalMinutes%60;
		int hours = (int) Math.floor(s/3600.0);
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
	
	private String ensureLen(int i) {
		String s = "" + i;
		if (s.length() < 2) {
			return "0" + s;
		} else {
			return s;
		}
	}

	public boolean isValidOption(String message) {
		message = message.toLowerCase();
		return options.contains(message);
	}

	public VoteType getVoteType() {
		return voteType;
	}
	
}
