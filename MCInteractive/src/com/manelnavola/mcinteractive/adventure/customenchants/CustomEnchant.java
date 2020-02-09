package com.manelnavola.mcinteractive.adventure.customenchants;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.manelnavola.mcinteractive.adventure.CustomItemStackBuilder;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem;
import com.manelnavola.mcinteractive.utils.ItemStackBuilder;

public class CustomEnchant extends CustomItem {
	
	public enum CustomEnchantFlag {
	    SWORD(0), ARROW(1), PICKAXE(2), HOE(3), AXE(4);

	    private final int value;
	    private CustomEnchantFlag(int value) {
	        this.value = value;
	    }
	}
	
	private static final String[] USED_ON = new String[] {
			"Sword", "Arrow", "Pickaxe", "Hoe", "Axe"};
	public static final String CUSTOM_PREFIX = ChatColor.BLACK + "" + ChatColor.RESET + "" + ChatColor.GRAY;
	
	private boolean[] compatibleEnchantsFlags = new boolean[8];
	
	public CustomEnchant(CustomItemFlag[] l_flags, CustomEnchantFlag[] mats) {
		super(l_flags);
		for (CustomEnchantFlag f : mats) {
			compatibleEnchantsFlags[f.value] = true;
		}
	}
	
	protected boolean quickChance(double d) {
		return Math.random() < (d/100.0);
	}
	
	protected ItemStack getEnchantedBook(String name, String description) {
		String usedOn = ChatColor.GRAY + "Can be used on: ";
		for (int i = 0; i < CustomEnchantFlag.values().length; i++) {
			if (compatibleEnchantsFlags[i]) {
				usedOn += USED_ON[i] + ", ";
			}
		}
		return new CustomItemStackBuilder<>(Material.ENCHANTED_BOOK)
				.name(name)
				.lore(description)
				.lore(usedOn.substring(0, usedOn.length() - 2))
				.uses(0)
				.addEnchantEffect()
				.build();
	}
	
	public boolean isCompatible(Material m) {
		if (m == Material.ARROW && compatibleEnchantsFlags[CustomEnchantFlag.ARROW.value])
			return true;
		if (m.name().contains("_SWORD") && compatibleEnchantsFlags[CustomEnchantFlag.SWORD.value])
			return true;
		if (m.name().contains("_PICKAXE") && compatibleEnchantsFlags[CustomEnchantFlag.PICKAXE.value])
			return true;
		if (m.name().contains("_HOE") && compatibleEnchantsFlags[CustomEnchantFlag.HOE.value])
			return true;
		if (m.name().contains("_AXE") && compatibleEnchantsFlags[CustomEnchantFlag.AXE.value])
			return true;
		return false;
	}
	
	public ItemStack enchant(ItemStack te, int tier, String gifter) {
		if (isCompatible(te.getType())) {
			String newEnchantLore = getRarity(tier).getItemMeta().getDisplayName();
			newEnchantLore = CustomEnchant.CUSTOM_PREFIX + ChatColor.WHITE + "" + ChatColor.ITALIC + gifter + "'s "
					+ CustomItemTier.getById(tier).getColor() + newEnchantLore;
			return new ItemStackBuilder<>(te)
					.newLore(newEnchantLore)
					.addEnchantEffect()
					.build();
		}
		return null;
	}
	
}
