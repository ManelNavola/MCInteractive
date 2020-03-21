package com.manelnavola.mcinteractive.generic;

import java.util.ArrayList;
import java.util.List;

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
	
	private static final String TITLE = ChatColor.BLUE + "MCInteractive Config";
	private static final String GLOBAL_TITLE = ChatColor.RED + "MCInteractive Global Config";
	
	public static String getTitle() {
		return TITLE;
	}
	
	public static String getGlobalTitle() {
		return GLOBAL_TITLE;
	}
	
	public static void open(Player p) {
		Inventory inv = Bukkit.createInventory(null, 27, TITLE);
		PlayerData pd = PlayerManager.getPlayerData(p);
		List<ConfigContainer> configContainers = ConfigManager.getConfigContainers();
        int column = 0;
		int row = 0;
		for (ConfigContainer cc : configContainers) {
			column = 0;
			for (Config c : cc.getConfigs()) {
				int slot = row*9 + column;
				Boolean b = PlayerManager.getLock(c.getID());
				if (c.globalConfig) {
					continue;
				}
				Config pre = c.getPrequisite();
				if (pre != null) {
					if (!pd.getConfig(pre.getID())) {
						continue;
					}
				}
				List<String> desc = new ArrayList<>();
				for (String s : c.getDescription()) {
					desc.add(ChatColor.WHITE + s);
				}
				if (b == null) {
					if (pd.getConfig(c.getID())) {
						inv.setItem(slot, new ItemStackBuilder<>(c.getIcon())
								.name(ChatColor.GREEN + c.getName())
								.lore(desc)
								.lore(ChatColor.AQUA + "Left-click " + ChatColor.WHITE + "to disable!")
								.addEnchantEffect()
								.build());
					} else {
						inv.setItem(slot, new ItemStackBuilder<>(c.getIcon())
								.name(ChatColor.RED + c.getName())
								.lore(desc)
								.lore(ChatColor.AQUA + "Left-click " + ChatColor.WHITE + "to enable!")
								.build());
					}
				} else {
					if (b.booleanValue()) {
						inv.setItem(slot, new ItemStackBuilder<>(c.getIcon())
								.name(ChatColor.GRAY + c.getName())
								.lore(desc)
								.lore(ChatColor.GOLD + "Globally" + ChatColor.GREEN + " enabled")
								.addEnchantEffect()
								.build());
					} else {
						inv.setItem(slot, new ItemStackBuilder<>(c.getIcon())
								.name(ChatColor.GRAY + c.getName())
								.lore(desc)
								.lore(ChatColor.GOLD + "Globally" + ChatColor.RED + " disabled")
								.build());
					}
				}
				column++;
			}
			row++;
		}
		inv.setItem(inv.getSize() - 1, new ItemStackBuilder<>(Material.BARRIER).name(ChatColor.RED + "Close GUI").build());
        p.openInventory(inv);
        PlayerManager.updateInventory(p);
	}
	
	public static void openGlobal(Player p) {
		Inventory inv = Bukkit.createInventory(null, 27, GLOBAL_TITLE);
		List<ConfigContainer> configContainers = ConfigManager.getConfigContainers();
        int column = 0;
		int row = 0;
		for (ConfigContainer cc : configContainers) {
			column = 0;
			for (Config c : cc.getConfigs()) {
				int slot = row*9 + column;
				Boolean b = PlayerManager.getLock(c.getID());
				Config pre = c.getPrequisite();
				if (pre != null) {
					Boolean b2 = PlayerManager.getLock(pre.getID());
					if (b2 != null && !b2.booleanValue()) {
						continue;
					}
				}
				List<String> desc = new ArrayList<>();
				for (String s : c.getDescription()) {
					desc.add(ChatColor.WHITE + s);
				}
				if (b == null) {
					// Unlocked
					inv.setItem(slot, new ItemStackBuilder<>(c.getIcon())
						.name(ChatColor.GOLD + c.getName())
						.lore(desc)
						.lore(ChatColor.GRAY + "Currently " + ChatColor.GOLD + "unlocked")
						.lore(ChatColor.AQUA + "Left-click " + ChatColor.WHITE
								+ "to lock on " + ChatColor.GREEN + "enable")
						.lore(ChatColor.AQUA + "Right-click " + ChatColor.WHITE
								+ "to lock on " + ChatColor.RED + "disable")
						.build());
				} else {
					if (b.booleanValue()) {
						// Locked on enable
						inv.setItem(slot, new ItemStackBuilder<>(c.getIcon())
							.name(ChatColor.GREEN + c.getName())
							.lore(desc)
							.lore(ChatColor.GRAY + "Currently locked on " + ChatColor.GREEN + "enable")
							.lore(ChatColor.AQUA + "Left-click " + ChatColor.WHITE
									+ "to lock on " + ChatColor.RED + "disable")
							.lore(ChatColor.AQUA + "Right-click " + ChatColor.WHITE
									+ "to " + ChatColor.GOLD + "unlock")
							.addEnchantEffect()
							.build());
					} else {
						// Locked on disable
						inv.setItem(slot, new ItemStackBuilder<>(c.getIcon())
							.name(ChatColor.RED + c.getName())
							.lore(desc)
							.lore(ChatColor.GRAY + "Currently locked on " + ChatColor.RED + "disable")
							.lore(ChatColor.AQUA + "Left-click " + ChatColor.WHITE
									+ "to lock on " + ChatColor.GREEN + "enable")
							.lore(ChatColor.AQUA + "Right-click " + ChatColor.WHITE
									+ "to " + ChatColor.GOLD + "unlock")
							.addEnchantEffect()
							.build());
					}
				}
				column++;
			}
			row++;
		}
		inv.setItem(inv.getSize() - 1, new ItemStackBuilder<>(Material.BARRIER).name(ChatColor.RED + "Close GUI").build());
        p.openInventory(inv);
        PlayerManager.updateInventory(p);
	}
	
	public static void clickGlobal(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		ItemStack clickedItem = e.getCurrentItem();
		
		if (e.getClick().equals(ClickType.LEFT) || e.getClick().equals(ClickType.RIGHT)) {
			if (clickedItem != null && (!clickedItem.getType().equals(Material.AIR))) {
				if (clickedItem.getType().equals(Material.BARRIER)) {
					p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 2);
					p.closeInventory();
					return;
				} else {
					if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
						String configID = ConfigManager.getIDbyName(
								ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
						Boolean b = PlayerManager.getLock(configID);
						if (b == null) {
							p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0.8F);
							if (e.getClick().equals(ClickType.LEFT)) {
								// Lock on enable
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
								PlayerManager.setLock(configID, true);
							} else {
								// Lock on disable
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
								PlayerManager.setLock(configID, false);
							}
						} else {
							if (b.booleanValue()) {
								if (e.getClick().equals(ClickType.LEFT)) {
									// Lock on disable
									p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
									PlayerManager.setLock(configID, false);
								} else {
									// Unlock
									p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1, 1.2F);
									PlayerManager.setLock(configID, null);
								}
							} else {
								if (e.getClick().equals(ClickType.LEFT)) {
									// Lock on enable
									p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
									PlayerManager.setLock(configID, true);
								} else {
									// Unlock
									p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 1.2F);
									PlayerManager.setLock(configID, null);
								}
							}
						}
					}
				}
			}
			e.setCancelled(true);
			openGlobal(p);
		} else {
			e.setCancelled(true);
			return;
		}
	}

	public static void click(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		PlayerData pd = PlayerManager.getPlayerData(p);
		ItemStack clickedItem = e.getCurrentItem();
		
		if (e.getClick().equals(ClickType.LEFT)) {
			if (clickedItem != null && (!clickedItem.getType().equals(Material.AIR))) {
				if (clickedItem.getType().equals(Material.BARRIER)) {
					p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 2);
					p.closeInventory();
					return;
				} else {
					if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
						boolean newValue = clickedItem.getItemMeta().getEnchants().isEmpty();
						String configID = ConfigManager.getIDbyName(
								ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
						Boolean b = PlayerManager.getLock(configID);
						if (b != null) {
							e.setCancelled(true);
							p.playSound(p.getLocation(), Sound.BLOCK_STONE_STEP, 1.5F, 1);
							return;
						}
						if (newValue) {
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
						} else {
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
						}
						pd.setConfig(configID, newValue);
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
