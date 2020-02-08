package com.manelnavola.mcinteractive.adventure.customevents;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.metadata.FixedMetadataValue;

import com.manelnavola.mcinteractive.adventure.EventManager;

public class Falling extends CustomEvent {
	
	private static final long SPAWN_DELAY = 10L;
	private static final double NEAR_DISTANCE = 2.0;
	
	private Material toDrop;
	private List<Entity> pendingEntities;
	
	public Falling() {
		super("What's falling from the sky?", new String[] {"sand", "anvil", "tnt", "stone"}, 3);
		pendingEntities = new ArrayList<>();
	}
	
	public Material getDrop() {
		return toDrop;
	}
	
	@Override
	public void run(List<Player> playerList, String option) {
		final Falling fall = this;
		long delay = SPAWN_DELAY;
		toDrop = Material.SAND;
		switch(option) {
		case "stone":
			toDrop = Material.STONE;
			break;
		case "tnt":
			toDrop = Material.TNT;
			delay *= 5;
			break;
		case "anvil":
			toDrop = Material.ANVIL;
			delay *= 2;
			break;
		}
		
		runTaskTimer(new Runnable() {
			int t = 0;
			@Override
			public void run() {
				if (t < 2) {
					fall.distributeDelayedTask(playerList, 20, new DistributedTaskRunnable() {
						@Override
						public void run(List<Player> players) {
							for (Player p : players) {
								drop(p, fall, 2.0f);
							}
						}
					}, 5L);
					t++;
				} else {
					fall.distributeDelayedTask(playerList, 20, new DistributedTaskRunnable() {
						@Override
						public void run(List<Player> players) {
							for (Player p : players) {
								drop(p, fall, 1.0f);
							}
						}
					}, 5L);
				}
			}
		}, delay, delay);
	}
	
	@SuppressWarnings("deprecation")
	private static void drop(Player p, Falling fall, float div) {
		Material toDrop = fall.getDrop();
		Entity dr;
		FallingBlock fb;
		if (toDrop == Material.TNT) {
			Location top = getNearTopLocation(p);
			TNTPrimed pt = (TNTPrimed) p.getWorld().spawnEntity(top, EntityType.PRIMED_TNT);
			pt.setYield(0F);
			pt.setFuseTicks(55);
			pt.setMetadata("MCI_FallEventDamageTnt", new FixedMetadataValue(EventManager.getPlugin(),
					new float[] {4, 8/div}));
			p.playSound(p.getLocation().add(0, 8, 0), Sound.ENTITY_TNT_PRIMED, 1F, 1F);
			p.getWorld().playSound(top.add(0, -10, 0), Sound.ENTITY_TNT_PRIMED, 1F, 1F);
			dr = pt;
		} else {
			fb = p.getWorld().spawnFallingBlock(getNearTopLocation(p), toDrop, (byte) 0);
			fb.setDropItem(false);
			dr = fb;
		}
		
		if (toDrop == Material.ANVIL) {
			dr.setCustomName("anvil");
			dr.setMetadata("MCI_FallEventDamage", new FixedMetadataValue(EventManager.getPlugin(),
					new float[] {1.1F, 6/div}));
		} else if (toDrop == Material.STONE) {
			dr.setCustomName("stone");
			dr.setMetadata("MCI_FallEventDamage", new FixedMetadataValue(EventManager.getPlugin(),
					new float[] {1.1F, 4/div}));
		} else if (toDrop == Material.SAND) {
			dr.setCustomName("sand");
			dr.setMetadata("MCI_FallEventDamage", new FixedMetadataValue(EventManager.getPlugin(),
					new float[] {1.1F, 2/div}));
		}
		synchronized (fall.pendingEntities) {
			fall.pendingEntities.add(dr);
		}
	}
	
	private static Location getNearTopLocation(Player p) {
		Location l = p.getLocation();
		l.add(Math.random()*NEAR_DISTANCE*2 - NEAR_DISTANCE,
				0,
				Math.random()*NEAR_DISTANCE*2 - NEAR_DISTANCE);
		l.setY(Math.max(l.getWorld().getHighestBlockYAt(l), l.getY()) + 20);
		return l;
	}

	@Override
	public CustomEvent clone() {
		return new Falling();
	}
	
	@Override
	public void dispose(List<Player> playerList) {
		super.dispose(playerList);
		synchronized (pendingEntities) {
			for (Entity e : pendingEntities) {
				e.remove();
			}
		}
	}
	
}
