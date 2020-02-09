package com.manelnavola.mcinteractive.adventure.customitems;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomItemManager;
import com.manelnavola.mcinteractive.adventure.CustomTrail;

public abstract class CustomItem {
	
	public enum CustomItemType {
		STACKABLE("Stackable"), ENCHANTMENT("Enchantment"), SINGLE_USE("Single use");
		
		private final String name;
		private CustomItemType(String n) {
			name = n;
		}
		
		public String getName() {
			return name;
		}
	}
	
	public enum CustomItemTier {
		COMMON("Common", 0, ChatColor.GREEN, Material.CRAFTING_TABLE),
		UNCOMMON("Uncommon", 1, ChatColor.AQUA, Material.CHEST),
		RARE("Rare", 2, ChatColor.LIGHT_PURPLE, Material.ENDER_CHEST),
		LEGENDARY("Legendary", 3, ChatColor.YELLOW, Material.YELLOW_SHULKER_BOX);
		
		private final int value;
		private final String name;
		private final ChatColor color;
		private final Material displayMaterial;
		
		private CustomItemTier(String n, int v, ChatColor cc, Material dm) {
			name = n; value = v; color = cc; displayMaterial = dm;
		}
		
		public int getValue() {
			return value;
		}
		
		public String getName() {
			return name;
		}
		
		public ChatColor getColor() {
			return color;
		}
		
		public Material getDisplayMaterial() {
			return displayMaterial;
		}
		
		public static CustomItemTier getById(int id) {
			for (CustomItemTier cit : CustomItemTier.values()) {
				if (cit.getValue() == id) {
					return cit;
				}
			}
			return null;
		}
		
		public static CustomItemTier find(String tf) {
			tf = tf.toUpperCase();
			for (CustomItemTier cit : CustomItemTier.values()) {
				if (cit.getName().toUpperCase().equals(tf)) {
					return cit;
				}
			}
			return null;
		}
	}
	
	public enum CustomItemFlag {
	    PROJECTILE(0), RIGHT_CLICK(1), ENTITY_HIT(2), DISPENSES(3), BURNS(4),
	    BLOCK_BREAK(5), SHOOT_BOW(6);

	    private final int value;
	    private CustomItemFlag(int value) {
	        this.value = value;
	    }
	    
	    public int getValue() {
			return value;
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
	
	public void onPlayerInteract(Player p, PlayerInteractEvent e, CustomItemInfo cii) {}
	
	public void onProjectileHit(Entity proj, Block block, Entity entity, int tier) {}
	public void onEntityDamageByEntity(Player playerDamager, Entity e, CustomItemInfo cii) {}
	public void onBlockDispense(Dispenser d, Location l, Vector dir, CustomItemInfo cii) {}
	public void onBurn(Furnace f, FurnaceBurnEvent e, CustomItemInfo cii) {}
	public void onEntityMount(Player p, Entity mount, int parseInt) {}
	public void onBlockBreak(Player player, Block block, CustomItemInfo cii) {}
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
		e.setMetadata("MCI", new FixedMetadataValue(CustomItemManager.getPlugin(),
				this.getClass().getName() + "/" + tier));
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
		CustomItemTier cit = CustomItemTier.getById(rarity);
		im.setDisplayName(ChatColor.RESET + "" + ChatColor.ITALIC + gifterNickname + "'s "
				+ ChatColor.RESET + "" + cit.getColor() + im.getDisplayName()
				+ " [" + cit.getName() + "]");
		is.setItemMeta(im);
	}
	
	public String getCustomName(String gifterNickname, int tier) {
		return null;
	}
	
}
