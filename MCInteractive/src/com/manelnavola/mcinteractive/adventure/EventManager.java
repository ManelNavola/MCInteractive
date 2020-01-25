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
	
	public static final int VOTING_LENGTH_S = 10;
	public static final int EVENT_LENGTH_S = 30;
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
						CustomEvent ce = getRandomEvent();
						VoteManager.startEventVote(ch, ce);
					}
				}
			}
			
		}, 0L, 20L*2);
		
		events.add(new ChangeGravity());
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
	
	private static CustomEvent getRandomEvent() {
		return events.get((int) (Math.random()*events.size()));
	}
	
}
