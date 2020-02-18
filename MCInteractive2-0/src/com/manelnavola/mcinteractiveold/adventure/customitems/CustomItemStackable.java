package com.manelnavola.mcinteractiveold.adventure.customitems;

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
		CustomItemTier cit = CustomItemTier.getById(rarity);
		im.setDisplayName(ChatColor.RESET + "" + cit.getColor() + im.getDisplayName()
			+ " [" + cit.getName() + "]");
		is.setItemMeta(im);
	}
	
}
