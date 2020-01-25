package com.manelnavola.mcinteractive.adventure;

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

import com.manelnavola.mcinteractive.adventure.customitems.CustomItem.CustomItemTier;
import com.manelnavola.mcinteractive.generic.PlayerData;
import com.manelnavola.mcinteractive.generic.PlayerManager;
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
		e.setCancelled(true);
		if (e.getClick().equals(ClickType.LEFT)) {
			if (clickedItem != null && (!clickedItem.getType().equals(Material.AIR))) {
				if (clickedItem.getType().equals(Material.BARRIER)) {
					p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 2);
					p.closeInventory();
					return;
				} else {
					for (int i = 0; i < 4; i++) {
						if (clickedItem.getType() == CustomItemTier.getById(i).getDisplayMaterial()) {
							if (bits >= prices[i]) {
								List<Player> pl = new ArrayList<Player>();
								pl.add(p);
								pd.setBits(bits - prices[i]);
								RewardManager.process(pl, (i+1)*2 - 1, SubPlan.LEVEL_1, ChatColor.stripColor(TITLE));
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
							} else {
								p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 2);
							}
							break;
						}
					}
				}
			}
			open(p);
		}
	}

	public static String getTitle() {
		return TITLE;
	}
}
