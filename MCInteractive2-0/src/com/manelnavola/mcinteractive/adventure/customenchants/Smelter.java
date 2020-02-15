package com.manelnavola.mcinteractive.adventure.customenchants;

import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomTrail;
import com.manelnavola.mcinteractive.utils.ItemStackBuilder;

public class Smelter extends CustomEnchant {
	
	private static CustomTrail trail = new CustomTrail(Particle.FLAME, 1, 0);
	
	public Smelter() {
		super(new CustomItemFlag[] {CustomItemFlag.BLOCK_BREAK, CustomItemFlag.PROJECTILE,
				CustomItemFlag.DISPENSES, CustomItemFlag.SHOOT_BOW},
				new CustomEnchantFlag[] {CustomEnchantFlag.PICKAXE, CustomEnchantFlag.ARROW});
		setRarities(null, null,
				getEnchantedBook("Smelter I", "25% chance to automatically smelt ores"),
				getEnchantedBook("Smelter II", "50% chance to automatically smelt ores"));
	}
	
	@Override
	public void onBlockBreak(Player player, Block b, CustomItemInfo cii) {
		if (player.getGameMode() == GameMode.CREATIVE) return;
		if (failCalculateChance(cii.getTier())) return;
		
		smeltBlock(b, player.getInventory().getItemInMainHand());
	}
	
	private boolean failCalculateChance(int tier) {
		if (tier == 2) {
			return !quickChance(50);
		} else {
			return !quickChance(25);
		}
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
		
		if (smeltBlock(hitBlock, new ItemStack(Material.DIAMOND_PICKAXE))) {
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
	
	private boolean smeltBlock(Block b, ItemStack mineWith) {
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
