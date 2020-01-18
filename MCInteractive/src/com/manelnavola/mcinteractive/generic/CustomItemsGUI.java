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

import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomItemManager;
import com.manelnavola.mcinteractive.adventure.RewardManager;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem;
import com.manelnavola.mcinteractive.utils.ItemStackBuilder;
import com.manelnavola.mcinteractive.utils.Log;

public class CustomItemsGUI {
	
	private static final String TITLE = ChatColor.RED + "Adventure Items - ";
	private static Sound[] sounds = new Sound[] { Sound.BLOCK_BARREL_OPEN, Sound.BLOCK_CHEST_OPEN,
			Sound.BLOCK_ENDER_CHEST_OPEN, Sound.BLOCK_SHULKER_BOX_OPEN };
	
	public static void open(Player p, int actual) {
		Inventory inv = null;
		switch(actual) {
		case 0:
			inv = Bukkit.createInventory(null, 54, TITLE + ChatColor.GREEN + "[Common]");
			break;
		case 1:
			inv = Bukkit.createInventory(null, 54, TITLE + ChatColor.AQUA + "[Uncommon]");
			break;
		case 2:
			inv = Bukkit.createInventory(null, 54, TITLE + ChatColor.LIGHT_PURPLE + "[Rare]");
			break;
		case 3:
			inv = Bukkit.createInventory(null, 54, TITLE + ChatColor.YELLOW + "[Legendary]");
			break;
		}
		
		List<CustomItem> cil = CustomItemManager.getCustomItemTiers(actual);
		for (CustomItem ci : cil) {
			inv.addItem(ci.getRarity(actual));
		}
		
		ItemStack c = CustomItemManager.getSubGift().getRarity(0);
		ItemStack u = CustomItemManager.getSubGift().getRarity(1);
		ItemStack r = CustomItemManager.getSubGift().getRarity(2);
		ItemStack l = CustomItemManager.getSubGift().getRarity(3);
		
		inv.setItem(45, new ItemStackBuilder<>(c.getType())
				.name(ChatColor.GREEN + "View Common items")
				.build());
		inv.setItem(46, new ItemStackBuilder<>(u.getType())
				.name(ChatColor.AQUA + "View Uncommon items")
				.build());
		inv.setItem(47, new ItemStackBuilder<>(r.getType())
				.name(ChatColor.LIGHT_PURPLE + "View Rare items")
				.build());
		inv.setItem(48, new ItemStackBuilder<>(l.getType())
				.name(ChatColor.YELLOW + "View Legendary items")
				.build());
		
		p.openInventory(inv);
	}
	
	public static void click(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		PlayerData pd = PlayerManager.getPlayerData(p);
		int bits = pd.getBits();
		ItemStack clickedItem = e.getCurrentItem();
		
		if (e.getClick().equals(ClickType.LEFT)) {
			e.setCancelled(true);
			if (clickedItem != null && (!clickedItem.getType().equals(Material.AIR))) {
				if (clickedItem.getType().equals(Material.BARRIER)) {
					p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 2);
					p.closeInventory();
					return;
				} else {
					if (clickedItem.getType() == CustomItemManager.getSubGift().getRarity(0).getType()) {
						open(p, 0);
						p.getWorld().playSound(p.getLocation(), sounds[0], 1F, 1F);
					} else if (clickedItem.getType() == CustomItemManager.getSubGift().getRarity(1).getType()) {
						open(p, 1);
						p.getWorld().playSound(p.getLocation(), sounds[1], 1F, 1F);
					} else if (clickedItem.getType() == CustomItemManager.getSubGift().getRarity(2).getType()) {
						open(p, 2);
						p.getWorld().playSound(p.getLocation(), sounds[2], 1F, 1F);
					} else if (clickedItem.getType() == CustomItemManager.getSubGift().getRarity(3).getType()) {
						open(p, 3);
						p.getWorld().playSound(p.getLocation(), sounds[3], 1F, 1F);
					} else {
						Log.info(ChatColor.RESET + "" + ChatColor.ITALIC
										+ "Server's " + clickedItem.getItemMeta().getDisplayName() + " "
										+ p.getOpenInventory().getTitle().substring(TITLE.length()));
						CustomItemInfo cii = new CustomItemInfo(new ItemStackBuilder<>(clickedItem)
								.name(ChatColor.RESET + "" + ChatColor.ITALIC
										+ "Server's " + clickedItem.getItemMeta().getDisplayName() + " "
										+ p.getOpenInventory().getTitle().substring(TITLE.length()))
								.build());
						if (cii.isValid()) {
							RewardManager.giftCustomItem(p, cii.getCustomItem(), cii.getTier(), "Server");
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
						}
					}
				}
			}
		} else {
			e.setCancelled(true);
			return;
		}
	}

	public static String getTitle() {
		return TITLE;
	}
	
}
