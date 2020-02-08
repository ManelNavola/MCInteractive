package com.manelnavola.mcinteractive.adventure;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import com.manelnavola.mcinteractive.Main;
import com.manelnavola.mcinteractive.adventure.customevents.*;
import com.manelnavola.mcinteractive.generic.ConnectionManager;
import com.manelnavola.mcinteractive.voting.VoteManager;

public class EventManager {
	
	public static final int VOTING_LENGTH_S = 5;
	public static final int EVENT_LENGTH_S = 90;
	private static List<CustomEvent> events;
	private static BukkitTask bt;
	private static Plugin plugin;
	
	public static void init(Plugin plg) {
		plugin = plg;
		events = new ArrayList<>();
		
		bt = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {

			@Override
			public void run() {
				for (String ch : ConnectionManager.getAnonConnectedChannels()) {
					if (Math.random() < 0.3) {
						startRandomEvent(ch);
					}
				}
			}
			
		}, 0L, 20*20);
		
		events.add(new Falling());
	}
	
	public static int startRandomEvent(String ch) {
		CustomEvent ce = getNewRandomEvent();
		return VoteManager.startEventVote(ch, ce);
	}

	public static void clearLater(List<Player> playerList) {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				for (Player p : playerList) {
					Main.clearEventEffects(p);
				}
			}
		}, EVENT_LENGTH_S*20L);
	}
	
	public static Plugin getPlugin() {
		return plugin;
	}
	
	public static void dispose() {
		bt.cancel();
	}
	
	private static CustomEvent getNewRandomEvent() {
		return events.get((int) (Math.random()*events.size())).clone();
	}
	
}
