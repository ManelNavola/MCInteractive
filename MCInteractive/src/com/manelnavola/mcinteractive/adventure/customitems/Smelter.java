package com.manelnavola.mcinteractive.adventure.customitems;

import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.utils.ItemStackBuilder;
import com.manelnavola.mcinteractive.utils.Log;

public class Smelter extends CustomEnchant {

	public Smelter() {
		super(new CustomItemFlag[] {CustomItemFlag.BLOCK_BREAK, CustomItemFlag.PROJECTILE, CustomItemFlag.DISPENSES},
				new CustomEnchantFlag[] {CustomEnchantFlag.PICKAXE, CustomEnchantFlag.ARROW});
		setRarities(null, null,
				getEnchantedBook("Smelter I", "25% chance to automatically smelt ores"),
				getEnchantedBook("Smelter II", "50% chance to automatically smelt ores"));
	}
	
	@Override
	public void onBlockBreak(Player player, BlockBreakEvent e, CustomItemInfo cii) {
		Block block = e.getBlock();
		if (player.getGameMode() == GameMode.CREATIVE) return;
		if (cii.getTier() == 2) {
			if (!quickChance(4)) return;
		} else {
			if (!quickChance(2)) return;
		}
		smeltBlock(block, cii.getTier(), player.getInventory().getItemInMainHand());
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

		
		if (tier == 2) {
			if (!quickChance(6)) return;
		} else {
			if (!quickChance(3)) return;
		}
		if (smeltBlock(hitBlock, tier, new ItemStack(Material.DIAMOND_PICKAXE))) {
			proj.remove();
		}
	}
	
	@Override
	public void onBlockDispense(Location l, Vector dir, CustomItemInfo cii) {
		Arrow a = l.getWorld().spawn(l, Arrow.class);
		a.setVelocity(dir);
		registerEntity(a, cii.getTier());
	}
	
	private boolean smeltBlock(Block b, int tier, ItemStack mineWith) {
		if (b.getType().name().contains("ORE")) {
			Collection<ItemStack> drops = b.getDrops(mineWith);
			if (drops.isEmpty()) return false;
			ItemStack firstDrop = drops.iterator().next();
			if (!firstDrop.getType().name().contains("ORE")) return false;
			b.setType(Material.AIR);
			Location end = b.getLocation().add(0.5, 0.5, 0.5);
			Material toDrop = Material.REDSTONE;
			switch(firstDrop.getType()) {
			case COAL_ORE:
				toDrop = Material.COAL;
				break;
			case DIAMOND_ORE:
				toDrop = Material.DIAMOND;
				break;
			case EMERALD_ORE:
				toDrop = Material.EMERALD;
				break;
			case GOLD_ORE:
				toDrop = Material.GOLD_INGOT;
				break;
			case IRON_ORE:
				toDrop = Material.IRON_INGOT;
				break;
			case LAPIS_ORE:
				toDrop = Material.LAPIS_LAZULI;
				break;
			default:
				break;
			}
			b.getWorld().playSound(end, Sound.ENTITY_BLAZE_BURN, 1, 1.2F);
			b.getWorld().spawnParticle(Particle.FLAME, end, 10, 0.5, 0.5, 0.5, 0.2);
			b.getWorld().dropItemNaturally(end,
					new ItemStackBuilder<>(toDrop).amount(drops.size()).build());
			return true;
		}
		return false;
	}

}
