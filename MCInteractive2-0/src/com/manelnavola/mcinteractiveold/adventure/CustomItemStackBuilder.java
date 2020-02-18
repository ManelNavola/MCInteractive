package com.manelnavola.mcinteractiveold.adventure;

import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.manelnavola.mcinteractiveold.adventure.customitems.CustomItem.CustomItemType;
import com.manelnavola.mcinteractiveold.utils.ItemStackBuilder;

public class CustomItemStackBuilder<T extends CustomItemStackBuilder<T>> extends ItemStackBuilder<T>  {
	
	private int uses = 1;
	
	public CustomItemStackBuilder(ItemStack is) {
		super(is);
		String lastLine = lore.get(lore.size() - 1);
		String leftText = ChatColor.stripColor(lastLine).split("/")[0];
		if (leftText.equals(CustomItemType.SINGLE_USE.getName())) {
			uses = 1;
		} else if (leftText.equals(CustomItemType.ENCHANTMENT.getName())) {
			uses = 0;
		} else {
			uses = Integer.parseInt(leftText);
		}
		lore.remove(lore.size() - 1);
	}
	
	public CustomItemStackBuilder(Material m) {
		super(m);
	}

	public T uses(int n) {
		uses = n;
		return self();
	}
	
	public T stackable() {
		return this.lore(ChatColor.GRAY + CustomItemType.STACKABLE.getName());
	}
	
	@Override
	public ItemStack build() {
		ItemStack is = new ItemStack(material);
		is.setAmount(amount);
		
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		if (uses > 1) {
			lore.add(ChatColor.GOLD + "" + uses + "/" + uses + " uses");
		} else if (uses == 1) {
			lore.add(ChatColor.GOLD + CustomItemType.SINGLE_USE.getName());
		} else if (uses == 0) {
			lore.add(ChatColor.GOLD + CustomItemType.ENCHANTMENT.getName());
		}
		im.setLore(lore);
		for(Entry<Enchantment, Integer> p : enchants.entrySet()) {
			im.addEnchant(p.getKey(), p.getValue(), true);
		}
		for (ItemFlag flag : itemFlags) {
			im.addItemFlags(flag);
		}
		
		is.setItemMeta(im);
		
		return is;
	}

}
