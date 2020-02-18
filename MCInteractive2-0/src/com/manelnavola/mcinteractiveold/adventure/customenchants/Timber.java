package com.manelnavola.mcinteractiveold.adventure.customenchants;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractiveold.adventure.CustomItemInfo;
import com.manelnavola.mcinteractiveold.adventure.CustomItemManager;
import com.manelnavola.mcinteractiveold.adventure.CustomTrail;

public class Timber extends CustomEnchant {
	
	private static CustomTrail trail = new CustomTrail(Particle.FLAME, 1, 0);
	
	public Timber() {
		super(new CustomItemFlag[] {CustomItemFlag.BLOCK_BREAK, CustomItemFlag.PROJECTILE,
				CustomItemFlag.DISPENSES, CustomItemFlag.SHOOT_BOW},
				new CustomEnchantFlag[] {CustomEnchantFlag.AXE, CustomEnchantFlag.ARROW});
		setRarities(null,
				getEnchantedBook("Timber I", "25% chance to timber adjacent logs"),
				getEnchantedBook("Timber II", "50% chance to timber adjacent logs"),
				getEnchantedBook("Timber III", "100% chance to timber adjacent logs"));
	}
	
	@Override
	public void onBlockBreak(Player player, Block b, CustomItemInfo cii) {
		if (player.getGameMode() == GameMode.CREATIVE) return;
		if (failCalculateChance(cii.getTier())) return;
		timberBlock(b, 12, 12, true);
	}
	
	@Override
	public void onProjectileHit(Entity proj, Block b, Entity e, int tier) {
		BlockIterator iterator = new BlockIterator(proj.getWorld(),
				proj.getLocation().toVector(), proj.getVelocity().normalize(), 0.0D, 4);
		
		Block hitBlock = null;
		while (iterator.hasNext()) {
			hitBlock = iterator.next();
			 
			if (hitBlock.getType() != Material.AIR) {
				break;
			}
		}
		
		if (timberBlock(hitBlock, 3, 3, false)) {
			proj.getWorld().spawnParticle(Particle.FLAME,
					hitBlock.getLocation().add(0.5, 0.5, 0.5), 4, 0.5, 0.5, 0.5, 0.1);
		} else {
			proj.getWorld().spawnParticle(Particle.SMOKE_NORMAL,
					proj.getLocation(), 3, 0.2, 0, 0.2, 0.05);
		}
		proj.remove();
	}
	
	@Override
	public void onEntityShootBow(Player player, Entity proj, CustomItemInfo cii) {
		if (failCalculateChance(cii.getTier())) return;
		registerEntity(proj, cii.getTier());
		registerTrail(proj, trail);
	}
	
	@Override
	public void onBlockDispense(Dispenser d, Location l, Vector dir, CustomItemInfo cii) {
		Arrow a = l.getWorld().spawn(l, Arrow.class);
		a.setVelocity(dir);
		if (failCalculateChance(cii.getTier())) return;
		registerEntity(a, cii.getTier());
		registerTrail(a, trail);
	}
	
	private boolean timberBlock(Block b, int len, int max, boolean first) {
		if (len <= 0) return false;
		if (b != null && b.getType().name().contains("_LOG") && !b.getType().name().contains("STRIPPED")) {
			b.breakNaturally();
			if (!first) {
				float lenf = (float) len;
				b.getWorld().playSound(b.getLocation(), Sound.BLOCK_WOOD_BREAK, 1, 1 + (1 - (lenf/max)));
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(CustomItemManager.getPlugin(), new Runnable() {
				@Override
				public void run() {
					timberBlock(b.getRelative(BlockFace.UP), len - 1, max, false);
					timberBlock(b.getRelative(BlockFace.EAST), len - 1, max, false);
					timberBlock(b.getRelative(BlockFace.NORTH), len - 1, max, false);
					timberBlock(b.getRelative(BlockFace.WEST), len - 1, max, false);
					timberBlock(b.getRelative(BlockFace.SOUTH), len - 1, max, false);
					timberBlock(b.getRelative(BlockFace.DOWN), len/2, max, false);
				}
			}, 3L);
			return true;
		}
		return false;
	}
	
	private boolean failCalculateChance(int tier) {
		if (tier == 3) {
			return false;
		} else if (tier == 2) {
			return !quickChance(50);
		} else {
			return !quickChance(25);
		}
	}

}
