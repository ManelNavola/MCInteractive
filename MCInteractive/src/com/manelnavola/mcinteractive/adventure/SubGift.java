package com.manelnavola.mcinteractive.adventure;

import org.bukkit.Location;
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
		{ 2, 6, 25, 67 },
		{ 4, 25, 55, 16 },
		{ 10, 45, 30, 15 },
		{ 35, 30, 20, 15 }
	};

	private Sound[] sounds = new Sound[] { Sound.BLOCK_BARREL_OPEN, Sound.BLOCK_CHEST_OPEN,
			Sound.BLOCK_ENDER_CHEST_OPEN, Sound.BLOCK_SHULKER_BOX_OPEN };

	public SubGift() {
		super(new CustomItemFlag[] { CustomItemFlag.DISPENSES, CustomItemFlag.RIGHT_CLICK });
		ItemStack[] rarities = new ItemStack[4];
		CustomItemTier cit;
		for (int i = 0; i < 4; i++) {
			cit = CustomItemTier.getById(i);
			rarities[i] = new CustomItemStackBuilder<>(cit.getDisplayMaterial())
					.name(NAME)
					.lore("Right click to open the gift!")
					.lore("Common - " + CHANCES[i][3] + "%")
					.lore("Uncommon - " + CHANCES[i][2] + "%")
					.lore("Rare - " + CHANCES[i][1] + "%")
					.lore("Legendary - " + CHANCES[i][0] + "%")
					.build();
		}
		setRarities(rarities[0], rarities[1], rarities[2], rarities[3]);
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
