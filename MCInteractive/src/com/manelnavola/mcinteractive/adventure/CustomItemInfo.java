package com.manelnavola.mcinteractive.adventure;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.manelnavola.mcinteractive.adventure.customitems.CustomItem;

public class CustomItemInfo {
	
	private boolean valid;
	private CustomItem customItem;
	private ItemStack customItemStack;
	private String className;
	private int tier;
	private int uses;
	private boolean singleUse = false;
	
	public CustomItemInfo(ItemStack item) {
		valid = false;
		if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;
		// Dealing with an item w/ meta
		ItemMeta im = item.getItemMeta();
		if (!im.hasLore()) return;
		// and lore
		List<String> lore = im.getLore();
		String lastLore = ChatColor.stripColor(lore.get(lore.size() - 1));
		if (lastLore.equals("Single use") || lastLore.endsWith("uses")) {
			// It is a custom item
			String displayName = ChatColor.stripColor(im.getDisplayName());
			String itemName = displayName.substring(displayName.indexOf(' ') + 1, displayName.indexOf('[') - 1);
			String tierText = displayName.substring(displayName.indexOf('[') + 1, displayName.indexOf(']'));
			if (!displayName.contains("'s")) {
				itemName = displayName.substring(0, displayName.indexOf('[') - 1);
			}
			int l_tier = 0;
			switch(tierText) {
			case "Uncommon":
				l_tier = 1;
				break;
			case "Rare":
				l_tier = 2;
				break;
			case "Legendary":
				l_tier = 3;
				break;
			case "Stackable":
				l_tier = 0;
				break;
			}
			if (itemName.equals("sub gift")) {
				customItem = CustomItemManager.getSubGift();
			} else {
				customItem = CustomItemManager.getCustomItemByName(itemName);
				if (customItem == null) return;
			}
			
			valid = true;
			className = customItem.getClass().getName();
			customItemStack = item;
			
			if (!lastLore.equals("Single use")) {
				uses = Integer.parseInt(lastLore.split("/")[0]);
			} else {
				singleUse = true;
				uses = 1;
			}
			tier = l_tier;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof CustomItemInfo) {
			CustomItemInfo other = (CustomItemInfo) o;
			if (other.isValid()) {
				if (other.getClassName().equals(getClassName()) &&
						other.getTier() == getTier() &&
						other.getUses() == getUses()) {
					return true;
				}
			}
		}
		return false;
 	}
	
	public boolean isValid() { return valid; }
	
	public boolean isSingleUse() { return singleUse; }
	public String getClassName() { return className; }
	public int getTier() { return tier; }
	public int getUses() { return uses; }
	public CustomItem getCustomItem() { return customItem; }
	public ItemStack getItemStack() { return customItemStack; }
	
}
