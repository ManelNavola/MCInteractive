package com.manelnavola.mcinteractiveold.adventure.customevents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class FavoriteMob extends CustomEvent {
	
	private static final Random r = new Random();
	
	private static final long spawnDelay = 100L;
	private static final float spawnChance = .333333334f;
	
	private static final int maxSpawnRadius = 8;
	private static final int minSpawnRadius = 6;
	
	private EntityType et;
	
	public FavoriteMob() {
		super("What's your favourite mob?", new String[] {"spider", "skeleton", "zombie", "slime"}, 3);
	}
	
	public EntityType getEntityType() {
		return et;
	}
	
	@Override
	public void run(List<Player> playerList, String option) {
		final FavoriteMob fm = this;
		switch(option) {
		case "spider":
			et = EntityType.SPIDER;
			break;
		case "skeleton":
			et = EntityType.SKELETON;
			break;
		case "zombie":
			et = EntityType.ZOMBIE;
			break;
		case "slime":
			et = EntityType.SLIME;
			break;
		}
		runTaskTimer(new Runnable() {
			@Override
			public void run() {
				List<Player> chosen = new ArrayList<>();
				for (final Player p : playerList) {
					if (Math.random() < spawnChance) {
						chosen.add(p);
					}
				}
				
				fm.distributeDelayedTask(chosen, 20, new DistributedTaskRunnable() {
					@Override
					public void run(List<Player> players) {
						for (Player p : players) {
							spawnNearPlayer(p, fm.getEntityType());
						}
					}
				}, 10L);
			}
		}, spawnDelay, spawnDelay);
	}

	private static boolean trySpawn(Set<Vector> vs, int x, int y, int z, Location l) {
		l.add(x, y, z);
		Block other = l.getBlock();
		if (other == null)
			return false;
		if (Math.abs(x) <= minSpawnRadius && Math.abs(y) <= minSpawnRadius && Math.abs(z) <= minSpawnRadius)
			return false;
		if (other.getType() == Material.AIR || other.getType() == Material.CAVE_AIR) {
			other = other.getRelative(BlockFace.UP);
			if (other != null && (other.getType() == Material.AIR || other.getType() == Material.CAVE_AIR)) {
				Block co = other.getRelative(BlockFace.DOWN);
				int times = maxSpawnRadius;
				while (co != null && co.getY() != 0
						&& (co.getType() == Material.AIR || co.getType() == Material.CAVE_AIR)
						&& times >= -maxSpawnRadius/2) {
					times--;
					other = co;
					co = other.getRelative(BlockFace.DOWN);
					y--;
				}
				
				if (co == null || co.getType() == Material.AIR || co.getType() == Material.CAVE_AIR) {
					return false;
				} else {
					if (Math.abs(x) <= minSpawnRadius && Math.abs(y) <= minSpawnRadius && Math.abs(z) <= minSpawnRadius)
						return false;
					vs.add(new Vector(x, y, z));
					return true;
				}
			}
		}
		return false;
	}
	
	private static void spawnNearPlayer(Player p, EntityType et) {
		Set<Vector> vs = new HashSet<>();
		Location l = p.getLocation().getBlock().getLocation();
		int x = -maxSpawnRadius;
		int y = 0;
		int z = -maxSpawnRadius;
		int inARow = 0;
		while (y <= maxSpawnRadius) {
			// Check up
			if (trySpawn(vs, x, y, z, l.clone())) {
				if (inARow > 2) {
					z += inARow;
				}
				x += r.nextInt(maxSpawnRadius*2);
				inARow++;
			} else if (trySpawn(vs, x, -y, z, l.clone())) {
				if (inARow > 2) {
					z += inARow;
				}
				x += r.nextInt(maxSpawnRadius*2);
				inARow++;
			} else {
				inARow = 0;
			}
			
			if (vs.size() > 12) {
				break;
			}
			
			x += 2;
			if (x > maxSpawnRadius) {
				x = -maxSpawnRadius;
				z++;
			}
			if (z > maxSpawnRadius) {
				z = -maxSpawnRadius;
				y += 2;
			}
		}
		
		if (vs.isEmpty()) {
			return;
		}
		
		if (vs.size() > 6)
			checkSpawn(l, et, vs, p);
		
		x = -maxSpawnRadius + 1;
		y = -maxSpawnRadius + 1;
		z = -maxSpawnRadius;
		while (y <= maxSpawnRadius) {
			// Check up
			if (trySpawn(vs, x, y, z, l.clone())) {
				if (inARow > 2) {
					z += inARow;
				}
				x += r.nextInt(maxSpawnRadius*2);
				inARow++;
			} else if (trySpawn(vs, x, -y, z, l.clone())) {
				if (inARow > 2) {
					z += inARow;
				}
				x += r.nextInt(maxSpawnRadius*2);
				inARow++;
			} else {
				inARow = 0;
			}
			
			if (vs.size() > 12) {
				break;
			}
			
			x += 2;
			if (x > maxSpawnRadius) {
				x = -maxSpawnRadius + 1;
				z++;
			}
			if (z > maxSpawnRadius) {
				z = -maxSpawnRadius;
				y += 2;
			}
		}
		
		if (!vs.isEmpty())
			checkSpawn(l, et, vs, p);
	}

	private static void checkSpawn(Location l, EntityType et, Set<Vector> vs, Player p) {
		int size = vs.size();
		int item = r.nextInt(size);
		int i = 0;
		for(Vector v : vs) {
		    if (i == item) {
		    	l.add(v).add(0.5, 1, 0.5);
		    	Collection<Entity> ce = l.getWorld().getNearbyEntities(l, 4.0, 4.0, 4.0);
		    	for (Entity en : ce) {
		    		if (en.getType() == EntityType.VILLAGER) return;
		    	}
		    	l.getWorld().spawnParticle(Particle.SPELL_WITCH, l.getX(), l.getY(), l.getZ(), 5, 0.5, 0.5, 0.5, 0.5);
		    	Mob mo = (Mob) l.getWorld().spawnEntity(l, et);
		    	if (mo.getType() == EntityType.ZOMBIE || mo.getType() == EntityType.SKELETON) {
		    		if (l.getWorld().getTime() < 12210 || l.getWorld().getTime() > 23460) {
		    			mo.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
			    	}
		    	}
		    	if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE)
		    		mo.setTarget(p);
		    }
		    i++;
		}
	}

	@Override
	public CustomEvent clone() {
		return new FavoriteMob();
	}
	
}
