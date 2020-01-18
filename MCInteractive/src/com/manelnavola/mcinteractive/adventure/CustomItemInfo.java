package com.manelnavola.mcinteractive.adventure;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.manelnavola.mcinteractive.adventure.customenchants.CustomEnchant;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem.CustomItemTier;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem.CustomItemType;
import com.manelnavola.mcinteractive.utils.Log;

public class CustomItemInfo {
	
	private boolean valid;
	private CustomItem customItem;
	private ItemStack customItemStack;
	private String className;
	private int tier;
	private int uses;
	private boolean singleUse = false;
	private boolean enchant = false;
	
	public CustomItemInfo(ItemStack item) {
		valid = false;
		if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;
		// Dealing with an item w/ meta
		ItemMeta im = item.getItemMeta();
		if (!im.hasLore()) return;
		// and lore
		List<String> lore = im.getLore();
		String lastLoreRaw = lore.get(lore.size() - 1);
		String lastLore = ChatColor.stripColor(lastLoreRaw);
		if (lastLore.equals(CustomItemType.SINGLE_USE.getName())
				|| lastLore.endsWith("uses")
				|| lastLore.equals(CustomItemType.ENCHANTMENT.getName())) {
			// It is a custom item
			String displayName = ChatColor.stripColor(im.getDisplayName());
			String itemName, tierText;
			try {
				tierText = displayName.substring(displayName.lastIndexOf('[') + 1, displayName.lastIndexOf(']'));
				if (displayName.contains("'s")) {
					itemName = displayName.substring(displayName.indexOf(' ') + 1, displayName.lastIndexOf('[') - 1);
				} else {
					itemName = displayName.substring(0, displayName.lastIndexOf('[') - 1);
				}
			} catch (Exception e) {
				valid = false;
				return;
			}
			int l_tier = 0;
			if (!tierText.equals(CustomItemType.STACKABLE.getName())) {
				CustomItemTier cit = CustomItemTier.find(tierText);
				if (cit != null) l_tier = cit.getValue();
			}
			if (itemName.equals(SubGift.NAME)) {
				customItem = CustomItemManager.getSubGift();
			} else {
				customItem = CustomItemManager.getCustomItemByName(itemName);
				if (customItem == null) return;
			}
			
			valid = true;
			className = customItem.getClass().getName();
			customItemStack = item;
			
			if (lastLore.equals(CustomItemType.SINGLE_USE.getName())) {
				singleUse = true;
				uses = 1;
			} else if (lastLore.equals(CustomItemType.ENCHANTMENT.getName())) {
				enchant = true;
				uses = 0;
			} else {
				uses = Integer.parseInt(lastLore.split("/")[0]);
			}
			
			tier = l_tier;
		} else {
			// Check hidden enchantment
			if (lastLoreRaw.startsWith(CustomEnchant.CUSTOM_PREFIX)) {
				customItem = CustomItemManager.getCustomItemByName(
						lastLore.substring(lastLore.indexOf(' ') + 1)
						);
				if (customItem == null) return;
				valid = true;
				enchant = false;
				className = customItem.getClass().getName();
				customItemStack = item;
				uses = 0;
				tier = 0;
				
				String strippedLastLore = lastLore.substring(lastLore.indexOf(' ') + 1);
				for (int i = 0; i < 4; i++) {
					if (customItem.getRarity(i) != null &&
							ChatColor.stripColor(customItem.getRarity(i).getItemMeta().getDisplayName())
							.equals(strippedLastLore)) {
						tier = i; break;
					}
				}
			}
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
	
	public boolean isEnchant() { return enchant; }
	public boolean isSingleUse() { return singleUse; }
	public String getClassName() { return className; }
	public int getTier() { return tier; }
	public int getUses() { return uses; }
	public CustomItem getCustomItem() { return customItem; }
	public CustomEnchant getCustomEnchant() {
		if (enchant) {
			return (CustomEnchant) customItem;
		}
		return null;
	}
	public ItemStack getItemStack() { return customItemStack; }
	
}
