package com.manelnavola.mcinteractive.adventure.customitems;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomItemManager;
import com.manelnavola.mcinteractive.adventure.CustomTrail;

public abstract class CustomItem {
	
	public enum CustomItemFlag {
	    PROJECTILE(0), RIGHT_CLICK(1), ENTITY_HIT(2), DISPENSES(3), BURNS(4),
	    ENTITY_MOUNT(5), BLOCK_BREAK(6), SHOOT_BOW(7);

	    private final int value;
	    private CustomItemFlag(int value) {
	        this.value = value;
	    }
	}
	
	protected ItemStack[] rarities = new ItemStack[4];
	private boolean[] flags = new boolean[16];
	
	public CustomItem(CustomItemFlag[] l_flags) {
		for (CustomItemFlag f : l_flags) {
			flags[f.value] = true;
		}
	}
	public CustomItem() {}
	
	public void onPlayerInteract(Player p, CustomItemInfo cii) {}
	
	public void onProjectileHit(Entity proj, Block block, Entity entity, int tier) {}
	public void onEntityDamageByEntity(Player playerDamager, Entity e, CustomItemInfo cii) {}
	public void onBlockDispense(Location l, Vector dir, CustomItemInfo cii) {}
	public void onBurn(Furnace f, FurnaceBurnEvent e, CustomItemInfo cii) {}
	public void onEntityMount(Player p, Entity mount, int parseInt) {}
	public void onBlockBreak(Player player, BlockBreakEvent e, CustomItemInfo cii) {}
	public void onEntityShootBow(Player player, Entity projectile, CustomItemInfo cii) {}
	
	public boolean hasFlag(CustomItemFlag f) {
		return flags[f.value];
	}
	
	public void setRarities(ItemStack t1, ItemStack t2, ItemStack t3, ItemStack t4) {
		rarities[0] = t1;
		rarities[1] = t2;
		rarities[2] = t3;
		rarities[3] = t4;
	}
	
	public void registerEntity(Entity e, int tier) {
		e.setMetadata("MCI", new FixedMetadataValue(CustomItemManager.getPlugin(), this.getClass().getName() + "/" + tier));
	}
	
	public void registerTrail(Entity e, CustomTrail ct) {
		CustomItemManager.registerTrail(e, ct);
	}
	
	public ItemStack getRarity(int tier) {
		if (rarities[tier] == null) return null;
		ItemStack is = rarities[tier].clone();
		ItemMeta im = is.getItemMeta();
		List<String> lore = im.getLore();
		lore.set(0, lore.get(0) + randomChatColorSequence());
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}
	
	private String randomChatColorSequence() {
		ChatColor[] colors = ChatColor.values();
		String tr = "";
		for (int i = 0; i < 10; i++) {
			int chosen = (int)(Math.random()*colors.length);
			tr += colors[chosen];
		}
		return tr;
	}
	
	public void fixDisplayName(ItemStack is, String gifterNickname, int rarity) {
		ItemMeta im = is.getItemMeta();
		switch(rarity) {
		case 0:
			im.setDisplayName(ChatColor.RESET + "" + ChatColor.ITALIC + gifterNickname + "'s "
					+ ChatColor.RESET + "" + ChatColor.GREEN + im.getDisplayName() + " [Common]");
			break;
		case 1:
			im.setDisplayName(ChatColor.RESET + "" + ChatColor.ITALIC + gifterNickname + "'s "
					+ ChatColor.RESET + "" + ChatColor.AQUA + im.getDisplayName() + " [Uncommon]");
			break;
		case 2:
			im.setDisplayName(ChatColor.RESET + "" + ChatColor.ITALIC + gifterNickname + "'s "
					+ ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + im.getDisplayName() + " [Rare]");
			break;
		case 3:
			im.setDisplayName(ChatColor.RESET + "" + ChatColor.ITALIC + gifterNickname + "'s "
					+  ChatColor.RESET + "" + ChatColor.YELLOW + im.getDisplayName() + " [Legendary]");
			break;
		}
		is.setItemMeta(im);
	}
	
	public String getCustomName(String gifterNickname, int tier) {
		return null;
	}
	
}
