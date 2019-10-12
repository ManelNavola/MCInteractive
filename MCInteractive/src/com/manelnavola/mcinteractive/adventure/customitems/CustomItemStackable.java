package com.manelnavola.mcinteractive.adventure.customitems;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomItemStackable extends CustomItem {
	
	public CustomItemStackable(CustomItemFlag[] customItemFlags) {
		super(customItemFlags);
	}

	@Override
	public ItemStack getRarity(int tier) {
		if (rarities[tier] == null) return null;
		ItemStack is = rarities[tier].clone();
		return is;
	}
	
	public void fixDisplayName(ItemStack is, String gifterNickname, int rarity) {
		ItemMeta im = is.getItemMeta();
		switch(rarity) {
		case 0:
			im.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + im.getDisplayName() + " [Common]");
			break;
		case 1:
			im.setDisplayName(ChatColor.RESET + "" + ChatColor.AQUA + im.getDisplayName() + " [Uncommon]");
			break;
		case 2:
			im.setDisplayName(ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + im.getDisplayName() + " [Rare]");
			break;
		case 3:
			im.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + im.getDisplayName() + " [Legendary]");
			break;
		}
		is.setItemMeta(im);
	}
	
}
