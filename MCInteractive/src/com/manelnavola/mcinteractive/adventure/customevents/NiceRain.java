package com.manelnavola.mcinteractive.adventure.customevents;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

public class NiceRain extends CustomEvent {
	
	public NiceRain() {
		super("What should rain?", new String[] {"tnt", "treasure"}, 3);
	}
	
	@Override
	public void run(List<Player> playerList, String option) {
		switch(option) {
		case "tnt":
			runTaskTimer(new Runnable() {
				@Override
				public void run() {
					distributeDelayedTask(playerList, 20, new DistributedTaskRunnable() {
						@Override
						public void run(List<Player> pl) {
							for (Player p : pl) {
								TNTPrimed tp = p.getLocation().getWorld().spawnEntity(arg0, arg1);
							}
						}
					}, 10L);
				}
			}, 0L, 40L);
			break;
		case "treasure":
			setWalkspeed(playerList, 0.4F);
			break;
		}
	}
	
}
