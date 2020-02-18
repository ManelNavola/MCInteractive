package com.manelnavola.mcinteractiveold.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackBuilder<T extends ItemStackBuilder<T>> {
	
	protected String name = null;
	protected int amount = 1;
	protected Material material = Material.BARRIER;
	protected List<String> lore = new ArrayList<>();
	protected List<ItemFlag> itemFlags = new ArrayList<ItemFlag>();
	protected Map<Enchantment, Integer> enchants = new HashMap<>();
	protected int uses = 1;
	
	public ItemStackBuilder(Material m) {
		material = m;
	}
	
	public ItemStackBuilder(ItemStack is) {
		ItemMeta im = is.getItemMeta();
		name = im.getDisplayName();
		material = is.getType();
		if (im.getLore() != null) {
			lore = im.getLore();
		}
		amount = is.getAmount();
		for (Entry<Enchantment, Integer> kp : im.getEnchants().entrySet()) {
			enchants.put(kp.getKey(), kp.getValue());
		}
		for (ItemFlag flag : im.getItemFlags()) {
			itemFlags.add(flag);
		}
		is.setItemMeta(im);
	}
	
	public T material(Material m) {
		material = m;
		return self();
	}
	
	public T amount(int a) {
		amount = a;
		return self();
	}
	
	public T name(String s) {
		name = s;
		return self();
	}
	
	public T newLore(String s) {
		lore.clear();
		return lore(s);
	}
	
	public T lore(String s) {
		lore.add(s);
		return self();
	}
	
	public T lore(List<String> sl) {
		for (String s : sl) {
			lore.add(s);
		}
		return self();
	}
	
	public T enchant(Enchantment ench, int lvl) {
		enchants.put(ench, lvl);
		return self();
	}
	
	public T addFlag(ItemFlag flag) {
		itemFlags.add(flag);
		return self();
	}
	
	public T addEnchantEffect() {
		if (material == Material.ARROW) {
			enchants.put(Enchantment.DURABILITY, 1);
		} else {
			enchants.put(Enchantment.ARROW_INFINITE, 1);
		}
		itemFlags.add(ItemFlag.HIDE_ENCHANTS);
		return self();
	}
	
	public ItemStack build() {
		ItemStack is = new ItemStack(material);
		is.setAmount(amount);
		
		ItemMeta im = is.getItemMeta();
		
		if (name != null) {
			im.setDisplayName(name);
		}
		if (!lore.isEmpty()) {
			im.setLore(lore);
		}
		for(Entry<Enchantment, Integer> p : enchants.entrySet()) {
			im.addEnchant(p.getKey(), p.getValue(), true);
		}
		for (ItemFlag flag : itemFlags) {
			im.addItemFlags(flag);
		}
		
		is.setItemMeta(im);
		
		return is;
	}
	
	@SuppressWarnings("unchecked")
	protected final T self() {
		return (T) this;
	}

	public T setLore(List<String> lore2) {
		lore = lore2;
		return self();
	}
	
}
