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

import com.manelnavola.mcinteractive.adventure.CustomItemManager;
import com.manelnavola.mcinteractive.adventure.RewardManager;
import com.manelnavola.mcinteractive.utils.ItemStackBuilder;
import com.manelnavola.twitchbotx.events.TwitchSubscriptionEvent.SubPlan;

public class BitsGUI {
	
	private static final String TITLE = ChatColor.BLUE + "Bits Shop";
	private static final int[] prices = new int[] {500, 1350, 2200, 3000};
	
	public static void open(Player p) {
		Inventory inv = Bukkit.createInventory(null, 27, TITLE);
		PlayerData pd = PlayerManager.getPlayerData(p);
		int bits = pd.getBits();
		inv.setItem(0, new ItemStackBuilder<>(Material.PRISMARINE_SHARD)
				.name(ChatColor.AQUA + "" + pd.getBits() + ChatColor.WHITE + " Bits")
				.lore(ChatColor.GRAY + "Use bits to purchase gifts")
				.lore(ChatColor.GRAY + "from the bits shop!")
				.addEnchantEffect()
				.build());
		
		inv.setItem(inv.getSize() - 1,
				new ItemStackBuilder<>(Material.BARRIER).name(ChatColor.RED + "Close GUI").build());
		
		ItemStack c = CustomItemManager.getSubGift().getRarity(0);
		ItemStack u = CustomItemManager.getSubGift().getRarity(1);
		ItemStack r = CustomItemManager.getSubGift().getRarity(2);
		ItemStack l = CustomItemManager.getSubGift().getRarity(3);
		
		inv.setItem(18, new ItemStackBuilder<>(c.getType())
				.name(ChatColor.GREEN + "Common Gift")
				.lore(ChatColor.GRAY + "You need " + prices[0] + " bits to purchase!")
				.build());
		inv.setItem(19, new ItemStackBuilder<>(u.getType())
				.name(ChatColor.AQUA + "Uncommon Gift")
				.lore(ChatColor.GRAY + "You need " + prices[1] + " bits to purchase!")
				.build());
		inv.setItem(20, new ItemStackBuilder<>(r.getType())
				.name(ChatColor.LIGHT_PURPLE + "Rare Gift")
				.lore(ChatColor.GRAY + "You need " + prices[2] + " bits to purchase!")
				.build());
		inv.setItem(21, new ItemStackBuilder<>(l.getType())
				.name(ChatColor.YELLOW + "Unique Gift")
				.lore(ChatColor.GRAY + "You need " + prices[3] + " bits to purchase!")
				.build());
		
		if (bits >= prices[0]) {
			inv.setItem(18, new ItemStackBuilder<>(c.getType())
					.name(ChatColor.GREEN + "Common Gift")
					.lore(ChatColor.GRAY + "Purchase for " + prices[0] + " bits")
					.build());
			if (bits >= prices[1]) {
				inv.setItem(19, new ItemStackBuilder<>(u.getType())
						.name(ChatColor.AQUA + "Uncommon Gift")
						.lore(ChatColor.GRAY + "Purchase for " + prices[1] + " bits")
						.build());
				if (bits >= prices[2]) {
					inv.setItem(20, new ItemStackBuilder<>(r.getType())
							.name(ChatColor.LIGHT_PURPLE + "Rare Gift")
							.lore(ChatColor.GRAY + "Purchase for " + prices[2] + " bits")
							.build());
					if (bits >= prices[3]) {
						inv.setItem(21, new ItemStackBuilder<>(l.getType())
								.name(ChatColor.YELLOW + "Unique Gift")
								.lore(ChatColor.GRAY + "Purchase for " + prices[3] + " bits")
								.build());
					}
				}
			}
		}
		
		p.openInventory(inv);
	}
	
	public static void click(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		PlayerData pd = PlayerManager.getPlayerData(p);
		int bits = pd.getBits();
		ItemStack clickedItem = e.getCurrentItem();
		
		if (e.getClick().equals(ClickType.LEFT)) {
			if (clickedItem != null && (!clickedItem.getType().equals(Material.AIR))) {
				if (clickedItem.getType().equals(Material.BARRIER)) {
					p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 2);
					p.closeInventory();
					return;
				} else {
					if (clickedItem.getType() == CustomItemManager.getSubGift().getRarity(0).getType()) {
						if (bits >= prices[0]) {
							List<Player> pl = new ArrayList<Player>();
							pl.add(p);
							pd.setBits(bits - prices[0]);
							RewardManager.process(pl, 1, SubPlan.LEVEL_1, ChatColor.stripColor(TITLE));
							p.closeInventory();
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
							return;
						} else {
							p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 2);
						}
					} else if (clickedItem.getType() == CustomItemManager.getSubGift().getRarity(1).getType()) {
						if (bits >= prices[1]) {
							List<Player> pl = new ArrayList<Player>();
							pl.add(p);
							pd.setBits(bits - prices[1]);
							RewardManager.process(pl, 3, SubPlan.LEVEL_1, ChatColor.stripColor(TITLE));
							p.closeInventory();
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
							return;
						} else {
							p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 2);
						}
					} else if (clickedItem.getType() == CustomItemManager.getSubGift().getRarity(2).getType()) {
						if (bits >= prices[2]) {
							List<Player> pl = new ArrayList<Player>();
							pl.add(p);
							pd.setBits(bits - prices[3]);
							RewardManager.process(pl, 4, SubPlan.LEVEL_1, ChatColor.stripColor(TITLE));
							p.closeInventory();
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
							return;
						} else {
							p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 2);
						}
					} else if (clickedItem.getType() == CustomItemManager.getSubGift().getRarity(3).getType()) {
						if (bits >= prices[3]) {
							List<Player> pl = new ArrayList<Player>();
							pl.add(p);
							RewardManager.process(pl, 7, SubPlan.LEVEL_1, ChatColor.stripColor(TITLE));
							p.closeInventory();
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
							return;
						} else {
							p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 2);
						}
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

	public static String getTitle() {
		return TITLE;
	}
}
