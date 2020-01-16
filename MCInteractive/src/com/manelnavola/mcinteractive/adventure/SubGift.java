package com.manelnavola.mcinteractive.adventure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractive.adventure.customitems.CustomItem;

import net.md_5.bungee.api.ChatColor;

public class SubGift extends CustomItem {
	
	public static final String NAME = "Sub Gift";
	
	private final static int[][] CHANCES = {
		{ 2, 6, 27, 65 },
		{ 4, 27, 55, 14 },
		{ 10, 45, 30, 15 },
		{ 35, 30, 20, 15 }
	};

	private Sound[] sounds = new Sound[] { Sound.BLOCK_BARREL_OPEN, Sound.BLOCK_CHEST_OPEN,
			Sound.BLOCK_ENDER_CHEST_OPEN, Sound.BLOCK_SHULKER_BOX_OPEN };

	public SubGift() {
		super(new CustomItemFlag[] { CustomItemFlag.DISPENSES, CustomItemFlag.RIGHT_CLICK });
		ItemStack common = new CustomItemStackBuilder<>(Material.BARREL)
				.name(NAME)
				.lore("Right click to open the gift!").lore("Common - " + CHANCES[0][3] + "%")
				.lore("Uncommon - " + CHANCES[0][2] + "%").lore("Rare - " + CHANCES[0][1] + "%")
				.lore("Legendary - " + CHANCES[0][0] + "%").build();
		ItemStack uncommon = new CustomItemStackBuilder<>(common).material(Material.CHEST)
				.newLore("Right click to open the gift!").lore("Common - " + CHANCES[1][3] + "%")
				.lore("Uncommon - " + CHANCES[1][2] + "%").lore("Rare - " + CHANCES[1][1] + "%")
				.lore("Legendary - " + CHANCES[1][0] + "%").build();
		ItemStack rare = new CustomItemStackBuilder<>(common).material(Material.ENDER_CHEST)
				.newLore("Right click to open the gift!").lore("Common - " + CHANCES[2][3] + "%")
				.lore("Uncommon - " + CHANCES[2][2] + "%").lore("Rare - " + CHANCES[2][1] + "%")
				.lore("Legendary - " + CHANCES[2][0] + "%").build();
		ItemStack legendary = new CustomItemStackBuilder<>(common).material(Material.YELLOW_SHULKER_BOX)
				.newLore("Right click to open the gift!").lore("Common - " + CHANCES[3][3] + "%")
				.lore("Uncommon - " + CHANCES[3][2] + "%").lore("Rare - " + CHANCES[3][1] + "%")
				.lore("Legendary - " + CHANCES[3][0] + "%").build();
		setRarities(common, uncommon, rare, legendary);
	}

	private int getRandomTier(int giftTier) {
		int[] probs = CHANCES[giftTier];
		int randomNum = (int) Math.floor(Math.random() * 100);
		if (randomNum >= probs[3]) {
			if (randomNum >= probs[3] + probs[2]) {
				if (randomNum >= probs[3] + probs[2] + probs[1]) {
					return 3;
				} else {
					return 2;
				}
			} else {
				return 1;
			}
		} else {
			return 0;
		}
	}

	@Override
	public void onPlayerInteract(Player p, PlayerInteractEvent ev, CustomItemInfo cii) {
		int tier = cii.getTier();
		String gifterName = ChatColor.stripColor(cii.getItemStack().getItemMeta().getDisplayName()).split("'")[0];
		RewardManager.giftRandomCustomItem(p, gifterName, getRandomTier(tier));
		p.getWorld().playSound(p.getLocation(), sounds[tier], 1F, 1F);
	}

	@Override
	public void onBlockDispense(Dispenser d, Location l, Vector dir, CustomItemInfo cii) {
		int tier = cii.getTier();
		String gifterName = ChatColor.stripColor(cii.getItemStack().getItemMeta().getDisplayName()).split("'")[0];
		ItemStack is = RewardManager.giftRandomCustomItem(null, gifterName, getRandomTier(tier));
		Item itemEntity = l.getWorld().dropItemNaturally(l, is);
		itemEntity.setVelocity(dir.multiply(0.5F).add(new Vector(0, 0.05F, 0)));
		l.getWorld().playSound(l, sounds[tier], 1F, 1F);
	}

}
