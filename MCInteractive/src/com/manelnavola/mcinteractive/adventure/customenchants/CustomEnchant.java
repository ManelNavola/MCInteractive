package com.manelnavola.mcinteractive.adventure.customenchants;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.manelnavola.mcinteractive.adventure.CustomItemStackBuilder;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem;

public class CustomEnchant extends CustomItem {
	
	public enum CustomEnchantFlag {
	    SWORD(0), ARROW(1), PICKAXE(2), HOE(3);

	    private final int value;
	    private CustomEnchantFlag(int value) {
	        this.value = value;
	    }
	}

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
		return new CustomItemStackBuilder<>(Material.ENCHANTED_BOOK)
				.name(name)
				.lore(description)
				.uses(0)
				.addEnchantEffect()
				.build();
	}
	
	public boolean isCompatible(Material m) {
		if (m.name().contains("SWORD") && compatibleEnchantsFlags[CustomEnchantFlag.SWORD.value])
			return true;
		if (m.name().contains("PICKAXE") && compatibleEnchantsFlags[CustomEnchantFlag.PICKAXE.value])
			return true;
		if (m == Material.ARROW && compatibleEnchantsFlags[CustomEnchantFlag.ARROW.value])
			return true;
		if (m.name().contains("HOE") && compatibleEnchantsFlags[CustomEnchantFlag.HOE.value])
			return true;
		return false;
	}
	
}
