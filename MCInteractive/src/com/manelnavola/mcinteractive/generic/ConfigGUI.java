package com.manelnavola.mcinteractive.generic;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.manelnavola.mcinteractive.utils.ItemStackBuilder;

public class ConfigGUI {
	
	private static final String TITLE = ChatColor.AQUA + "MCInteraftive Config";
	
	public static String getTitle() {
		return TITLE;
	}
	
	public static void open(Player p) {
		Inventory inv = Bukkit.createInventory(null, 27, TITLE);
		PlayerData pd = PlayerManager.getPlayerData(p);
		Map<String, List<Config>> cp = ConfigManager.getCommandPairs();
        int column = 0;
		int row = 0;
		for (String key : cp.keySet()) {
			column = 0;
			List<Config> cl = cp.get(key);
			for (Config c : cl) {
				int slot = row*9 + column;
				if (pd.getConfig(c.getID())) {
					inv.setItem(slot, new ItemStackBuilder<>(c.getIcon())
							.name(ChatColor.GREEN + c.getName())
							.lore(ChatColor.WHITE + c.getDescription())
							.lore(ChatColor.AQUA + "Left-click " + ChatColor.WHITE + "to disable!")
							.addEnchantEffect()
							.build());
				} else {
					inv.setItem(slot, new ItemStackBuilder<>(c.getIcon())
							.name(ChatColor.RED + c.getName())
							.lore(ChatColor.WHITE + c.getDescription())
							.lore(ChatColor.AQUA + "Left-click " + ChatColor.WHITE + "to enable!")
							.build());
				}
				column++;
			}
			row++;
		}
		inv.setItem(inv.getSize() - 1, new ItemStackBuilder<>(Material.BARRIER).name(ChatColor.RED + "Close GUI").build());
        p.openInventory(inv);
        PlayerManager.updateInventory(p);
	}

	public static void click(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		PlayerData pd = PlayerManager.getPlayerData(p);
		ItemStack clickedItem = e.getCurrentItem();
		
		if (e.getClick().equals(ClickType.LEFT)) {
			if (clickedItem != null && (!clickedItem.getType().equals(Material.AIR))) {
				if (clickedItem.getType().equals(Material.BARRIER)) {
					p.closeInventory();
					return;
				} else {
					if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
						boolean newValue = clickedItem.getItemMeta().getEnchants().isEmpty();
						if (newValue) {
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
						} else {
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
						}
						pd.setConfig(ConfigManager.getIDbyName(ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName())),
								newValue);
					}
				}
			}
			open(p);
			e.setCancelled(true);
		} else {
			e.setCancelled(true);
			return;
		}
	}
	
}
