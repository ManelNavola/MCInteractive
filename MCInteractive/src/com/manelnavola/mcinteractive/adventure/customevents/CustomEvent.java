package com.manelnavola.mcinteractive.adventure.customevents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.manelnavola.mcinteractive.Main;
import com.manelnavola.mcinteractive.adventure.EventManager;

public abstract class CustomEvent {
	
	protected Plugin plugin;
	private String description;
	private List<String> options;
	private List<BukkitTask> tasks;
	private int optionAmount = 2;
	
	public CustomEvent(String d, String[] op, int amount) {
		plugin = EventManager.getPlugin();
		description = d;
		options = new ArrayList<>();
		tasks = new ArrayList<>();
		for (String s : op) {
			options.add(s);
		}
		optionAmount = amount;
	}

	public String getDescription() { return description; }
	public List<String> getOptions() {
		List<String> newOptions = new ArrayList<>(options);
		Collections.shuffle(newOptions);
		newOptions = newOptions.subList(0, optionAmount);
		return newOptions;
	}
	
	public void clearLater(List<Player> playerList) {
		EventManager.clearLater(playerList);
	}
	
	public void setWalkspeed(List<Player> pl, float spd) {
		for (Player p : pl) {
			p.setMetadata("MCI_WALKSPEED", new FixedMetadataValue(plugin, true));
			p.setWalkSpeed(spd);
		}
	}
	
	public void setPotionEffects(List<Player> playerList, PotionEffectType[] potionEffectTypes, int[] amplifiers) {
		String ta = "";
		for (int i = 0; i < potionEffectTypes.length; i++) {
			for (Player p : playerList) {
				p.removePotionEffect(potionEffectTypes[i]);
				p.addPotionEffect(new PotionEffect(potionEffectTypes[i], EventManager.EVENT_LENGTH_S*20,
						amplifiers[i], true));
				ta += potionEffectTypes[i].getName() + ",";
			}
		}
		ta = ta.substring(0, ta.length() - 1);
		for (Player p : playerList) {
			p.setMetadata("MCI_POTIONEFFECT",
					new FixedMetadataValue(plugin, ta));
		}
	}
	
	public void setPotionEffects(List<Player> playerList, PotionEffectType potionEffectType, int amplifier) {
		setPotionEffects(playerList, new PotionEffectType[] {potionEffectType}, new int[] {amplifier});
	}
	
	public void runTaskTimer(Runnable runnable, long delay, long period) {
		tasks.add(Bukkit.getScheduler().runTaskTimer(EventManager.getPlugin(), runnable, delay, period));
	}
	
	public void runTaskLater(Runnable runnable, long period) {
		tasks.add(Bukkit.getScheduler().runTaskLater(EventManager.getPlugin(), runnable, period));
	}
	
	public void distributeDelayedTask(List<Player> playerList, int batchSize, DistributedTaskRunnable dtr, long period) {
		List<Player> push = new ArrayList<>();
		if (playerList.size() > 0) push.add(playerList.get(0));
		for (int i = 1; i < playerList.size(); i++) {
			push.add(playerList.get(0));
			if (i%batchSize == 0) {
				runTaskLater(new Runnable() {
					@Override
					public void run() {
						dtr.run(new ArrayList<>(push));
					}
				}, period*(i/batchSize));
				push.clear();
			}
		}
		dtr.run(new ArrayList<>(push));
	}
	
	public abstract void run(List<Player> playerList, String option);
	
	public void dispose(List<Player> playerList) {
		for (BukkitTask bt : tasks) {
			if (!bt.isCancelled()) bt.cancel();
		}
		for (Player p : playerList) {
			Main.clearEventEffects(p);
		}
	}

}

interface DistributedTaskRunnable {
	
	public void run(List<Player> players);
	
}