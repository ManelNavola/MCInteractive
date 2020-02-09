package com.manelnavola.mcinteractive.adventure;

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

import com.manelnavola.mcinteractive.adventure.customenchants.CustomEnchant;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem.CustomItemTier;
import com.manelnavola.mcinteractive.utils.ItemStackBuilder;

public class CustomItemsGUI {
	
	private static final String TITLE = ChatColor.RED + "Custom Items " + ChatColor.WHITE + "- ";
	private static Sound[] sounds = new Sound[] { Sound.BLOCK_BARREL_OPEN, Sound.BLOCK_CHEST_OPEN,
			Sound.BLOCK_ENDER_CHEST_OPEN, Sound.BLOCK_SHULKER_BOX_OPEN };
	
	public static void open(Player p, int actual) {
		Inventory inv = null;
		CustomItemTier cit = CustomItemTier.getById(actual);
		inv = Bukkit.createInventory(null, 54, TITLE + cit.getColor() +
				"[" + cit.getName() + "]");
		
		List<CustomItem> cil = CustomItemManager.getCustomItemTiers(actual);
		for (CustomItem ci : cil) {
			inv.addItem(ci.getRarity(actual));
		}
		
		for (int i = 0; i < 4; i++) {
			cit = CustomItemTier.getById(i);
			inv.setItem(45 + i, new ItemStackBuilder<>(cit.getDisplayMaterial())
					.name(cit.getColor() + "View " + cit.getName() + " items")
					.build());
		}
		
		p.openInventory(inv);
	}
	
	public static void click(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		ItemStack clickedItem = e.getCurrentItem();
		
		if (e.getRawSlot() > 53) return;
		
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
						CustomItemInfo cii = new CustomItemInfo(new ItemStackBuilder<>(clickedItem)
								.name(ChatColor.RESET + "" + ChatColor.ITALIC
										+ "Server's " + clickedItem.getItemMeta().getDisplayName() + " "
										+ p.getOpenInventory().getTitle().substring(TITLE.length()))
								.build());
						if (cii.isValid() && cii.isEnchant() &&
								e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
							ItemStack cursor = e.getCursor();
							CustomEnchant ce = cii.getCustomEnchant();
							if (ce.isCompatible(cursor.getType())) {
								if (!cursor.hasItemMeta() || !cursor.getItemMeta().hasEnchants()) {
									p.setItemOnCursor(ce.enchant(cursor, cii.getTier(), "Server"));
									p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
									return;
								}
							}
							p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 2);
						} else {
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
