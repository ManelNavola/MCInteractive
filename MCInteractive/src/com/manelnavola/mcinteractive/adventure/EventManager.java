package com.manelnavola.mcinteractive.adventure;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.manelnavola.mcinteractive.Main;
import com.manelnavola.mcinteractive.adventure.customevents.CustomEvent;
import com.manelnavola.mcinteractive.adventure.customevents.VoteRunnable;
import com.manelnavola.mcinteractive.generic.ConnectionManager;
import com.manelnavola.mcinteractive.voting.VoteManager;

public class EventManager {
	
	public static final int VOTING_LENGTH_S = 5;
	public static final int EVENT_LENGTH_S = 10;
	private static List<CustomEvent> events;
	private static BukkitTask bt;
	private static List<BukkitTask> btl;
	private static Plugin plugin;
	
	public static void init(Plugin plg) {
		plugin = plg;
		events = new ArrayList<>();
		btl = new ArrayList<>();
		
		bt = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {

			@Override
			public void run() {
				for (String ch : ConnectionManager.getAnonConnectedChannels()) {
					if (Math.random() < 0.3) {
						CustomEvent ce = getRandomEvent();
						VoteManager.startEventVote(ch, ce.getDescription(), ce.getRunnable(), ce.getOptions());
					}
				}
			}
			
		}, 0L, 20L*2);
		
		// Player movement
		events.add(new CustomEvent(
				"How should the player move?",
				new VoteRunnable() {
					@Override
					public void run(List<Player> playerList, String option) {
						for (Player p : playerList) {
							switch(option) {
							case "jump":
								p.setMetadata("MCI_WALKSPEED", new FixedMetadataValue(plugin, true));
								p.setWalkSpeed(0F);
								p.removePotionEffect(PotionEffectType.JUMP);
								p.removePotionEffect(PotionEffectType.SLOW_FALLING);
								p.setMetadata("MCI_POTIONEFFECT", new FixedMetadataValue(plugin, "JUMP,SLOW_FALLING"));
								p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, EVENT_LENGTH_S*20, 1, true));
								p.addPotionEffect(
										new PotionEffect(PotionEffectType.SLOW_FALLING, EVENT_LENGTH_S*20, 0, true));
								break;
							case "fast":
								p.setMetadata("MCI_WALKSPEED", new FixedMetadataValue(plugin, true));
								p.setWalkSpeed(0.4F);
								break;
							case "auto":
								p.setMetadata("MCI_WALKSPEED", new FixedMetadataValue(plugin, true));
								p.setWalkSpeed(0F);
								p.removePotionEffect(PotionEffectType.JUMP);
								p.setMetadata("MCI_POTIONEFFECT", new FixedMetadataValue(plugin, "JUMP"));
								p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, EVENT_LENGTH_S*20, 128, true));
								BukkitTask bt = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
									@Override
									public void run() {
										for (Player p : playerList) {
											if (p.getWalkSpeed() == 0.0F) {
												Block b = p.getLocation().add(0, -2, 0).getBlock();
												if (b == null || b.getType() == Material.AIR) continue;
												p.setVelocity(p.getLocation().getDirection());
											}
										}
									}
								}, 0L, 10L);
								BukkitTask bt2 = Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
									@Override
									public void run() {
										if (bt != null && !bt.isCancelled()) {
											bt.cancel();
										}
									}
								}, EVENT_LENGTH_S*20L);
								addTasks(bt, bt2);
								break;
							}
						}
						clearLater(playerList);
					}
				}, new String[] {"jump", "fast", "auto"}, 3));
	}
	
	private static void addTasks(BukkitTask... btt) {
		btl.removeIf(bt -> bt.isCancelled());
		for (BukkitTask bt : btt) {
			btl.add(bt);
		}
	}

	private static void clearLater(List<Player> playerList) {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				for (Player p : playerList) {
					Main.clearEventEffects(p);
				}
			}
		}, EVENT_LENGTH_S*20L);
	}
	
	public static void dispose() {
		for (BukkitTask bt2 : btl) {
			bt2.cancel();
		}
		bt.cancel();
	}
	
	private static CustomEvent getRandomEvent() {
		return events.get((int) (Math.random()*events.size()));
	}
	
}
