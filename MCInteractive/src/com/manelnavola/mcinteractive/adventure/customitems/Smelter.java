package com.manelnavola.mcinteractive.adventure.customitems;

import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.utils.ItemStackBuilder;

public class Smelter extends CustomEnchant {

	public Smelter() {
		super(new CustomItemFlag[] {CustomItemFlag.BLOCK_BREAK},
				new CustomEnchantFlag[] {CustomEnchantFlag.PICKAXE});
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
		if (block.getType().name().contains("ORE")) {
			Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
			if (drops.isEmpty()) return;
			ItemStack firstDrop = drops.iterator().next();
			if (!firstDrop.getType().name().contains("ORE")) return;
			e.setDropItems(false);
			Location end = block.getLocation().add(0.5, 0.5, 0.5);
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
			block.getWorld().playSound(end, Sound.ENTITY_BLAZE_BURN, 1, 1.2F);
			block.getWorld().spawnParticle(Particle.FLAME, end, 10, 0.5, 0.5, 0.5, 0.2);
			block.getWorld().dropItemNaturally(end,
					new ItemStackBuilder<>(toDrop).amount(drops.size()).build());
		}
	}

}
