package com.manelnavola.mcinteractive.adventure;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import com.manelnavola.mcinteractive.adventure.customenchants.CustomEnchant;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem;
import com.manelnavola.mcinteractive.generic.PlayerData;
import com.manelnavola.mcinteractive.generic.PlayerManager;
import com.manelnavola.mcinteractive.utils.ItemStackBuilder;
import com.manelnavola.mcinteractive.utils.MessageSender;
import com.manelnavola.twitchbotx.events.TwitchSubscriptionEvent.SubPlan;

public class RewardManager {
	
	// Calculated from http://www.naughtynathan.co.uk/minecraft/prices.htm#ores
	// 750 - 1500
	private static Reward[] legendaryRewards = new Reward[] {
		// One item
		new EnchantReward(Enchantment.DIG_SPEED, 4),
		new EnchantReward(Enchantment.DURABILITY, 3),
		new EnchantReward(Enchantment.LOOT_BONUS_BLOCKS, 1),
		new EnchantReward(Enchantment.DAMAGE_ALL, 3),
		new EnchantReward(Enchantment.DAMAGE_UNDEAD, 2),
		new EnchantReward(Enchantment.DAMAGE_UNDEAD, 3),
		new EnchantReward(Enchantment.DAMAGE_ARTHROPODS, 2),
		new EnchantReward(Enchantment.DAMAGE_ARTHROPODS, 3),
		new EnchantReward(Enchantment.KNOCKBACK, 2),
		new EnchantReward(Enchantment.ARROW_DAMAGE, 4),
		new EnchantReward(Enchantment.ARROW_FIRE, 1),
		new EnchantReward(Enchantment.PROTECTION_FIRE, 2),
		new EnchantReward(Enchantment.PROTECTION_PROJECTILE, 3),
		new EnchantReward(Enchantment.PROTECTION_PROJECTILE, 4),
		new EnchantReward(Enchantment.PROTECTION_FALL, 4),
		new EnchantReward(Enchantment.OXYGEN, 1),
		new EnchantReward(Enchantment.OXYGEN, 2),
		new Reward(Material.DIAMOND_LEGGINGS), new Reward(Material.DIAMOND_CHESTPLATE),
		new Reward(Material.DIAMOND_HELMET), new Reward(Material.CHAINMAIL_CHESTPLATE),
		new Reward(Material.EMERALD_BLOCK), new Reward(Material.DIAMOND_BLOCK),
		// Multiple items
		new Reward(6, 11, Material.IRON_BLOCK), new Reward(3, 4, Material.GOLD_BLOCK),
		new Reward(5, 9, Material.DIAMOND), new Reward(8, 15, Material.EMERALD),
		new Reward(25, 40, Material.QUARTZ_BLOCK), new Reward(2, 3, Material.GOLDEN_APPLE),
		new Reward(4, 7, Material.GHAST_TEAR)
	};
	// 250 - 750
	private static Reward[] rareRewards = new Reward[] {
		// One item
		new EnchantReward(Enchantment.PROTECTION_FALL, 2),
		new EnchantReward(Enchantment.PROTECTION_FALL, 3),
		new EnchantReward(Enchantment.PROTECTION_PROJECTILE, 2),
		new EnchantReward(Enchantment.PROTECTION_EXPLOSIONS, 1),
		new EnchantReward(Enchantment.PROTECTION_FIRE, 1),
		new EnchantReward(Enchantment.PROTECTION_ENVIRONMENTAL, 2),
		new EnchantReward(Enchantment.PROTECTION_ENVIRONMENTAL, 3),
		new EnchantReward(Enchantment.ARROW_KNOCKBACK, 1),
		new EnchantReward(Enchantment.ARROW_DAMAGE, 3),
		new EnchantReward(Enchantment.KNOCKBACK, 1),
		new EnchantReward(Enchantment.DAMAGE_ARTHROPODS, 1),
		new EnchantReward(Enchantment.DAMAGE_ALL, 2),
		new EnchantReward(Enchantment.DURABILITY, 2),
		new EnchantReward(Enchantment.DIG_SPEED, 3),
		new Reward(Material.CHAINMAIL_BOOTS), new Reward(Material.CHAINMAIL_LEGGINGS),
		new Reward(Material.CHAINMAIL_HELMET), new Reward(Material.GOLDEN_CHESTPLATE),
		new Reward(Material.GOLDEN_LEGGINGS), new Reward(Material.DIAMOND_SWORD),
		new Reward(Material.DIAMOND_PICKAXE), new Reward(Material.DIAMOND_AXE),
		new Reward(Material.DIAMOND_HOE), new Reward(Material.SADDLE),
		new Reward(Material.ENCHANTING_TABLE), new Reward(Material.ANVIL),
		new Reward(Material.GOLDEN_APPLE), new Reward(Material.GOLD_BLOCK),
		new Reward(Material.EMERALD_BLOCK),
		// Multiple items
		new Reward(40, 50, Material.IRON_INGOT), new Reward(10, 20, Material.GOLD_INGOT),
		new Reward(2, 5, Material.DIAMOND), new Reward(4, 8, Material.EMERALD),
		new Reward(2, 4, Material.GHAST_TEAR), new Reward(5, 10, Material.BOOKSHELF),
		new Reward(10, 20, Material.POWERED_RAIL),
	};
	// 100 - 250
	private static Reward[] uncommonRewards = new Reward[] {
		// One item
		new EnchantReward(Enchantment.DIG_SPEED, 1),
		new EnchantReward(Enchantment.DIG_SPEED, 2),
		new EnchantReward(Enchantment.DURABILITY, 1),
		new EnchantReward(Enchantment.DAMAGE_UNDEAD, 1),
		new EnchantReward(Enchantment.ARROW_DAMAGE, 2),
		new EnchantReward(Enchantment.PROTECTION_PROJECTILE, 1),
		new EnchantReward(Enchantment.PROTECTION_FALL, 1),
		
		new Reward(Material.DIAMOND_BOOTS), new Reward(Material.GOLDEN_HOE),
		new Reward(Material.GOLDEN_AXE), new Reward(Material.GOLDEN_PICKAXE),
		new Reward(Material.GOLDEN_HELMET), new Reward(Material.GOLDEN_BOOTS),
		new Reward(Material.IRON_CHESTPLATE), new Reward(Material.IRON_LEGGINGS),
		new Reward(Material.LEATHER_HELMET), new Reward(Material.LEATHER_CHESTPLATE),
		new Reward(Material.LEATHER_LEGGINGS),
		
		new Reward(Material.CLOCK), new Reward(Material.SADDLE),
		new Reward(Material.LAVA_BUCKET), new Reward(Material.CAULDRON),
		new Reward(Material.JUKEBOX), new Reward(Material.HOPPER),
		new Reward(Material.ENDER_CHEST), new Reward(Material.GHAST_TEAR),
		new Reward(Material.EMERALD), new Reward(Material.DIAMOND),
		new Reward(Material.IRON_BLOCK),
		// Multiple items
		new Reward(20, 30, Material.IRON_INGOT), new Reward(5, 10, Material.GOLD_INGOT),
		new Reward(2, 6, Material.BOOKSHELF),
	};
	// 0 - 100
	private static Reward[] commonRewards = new Reward[] {
		new Reward(15, 30, Material.ACACIA_LOG), new Reward(15, 30, Material.BIRCH_LOG),
		new Reward(15, 30, Material.JUNGLE_LOG), new Reward(15, 30, Material.DARK_OAK_LOG),
		new Reward(15, 30, Material.SPRUCE_LOG), new Reward(15, 30, Material.OAK_LOG),
		new Reward(10, 20, Material.IRON_INGOT), new Reward(8, 16, Material.COAL_BLOCK),
		new Reward(8, 16, Material.OBSIDIAN), new Reward(16, 24, Material.BLUE_WOOL),
		new Reward(16, 24, Material.RED_WOOL), new Reward(8, 16, Material.SOUL_SAND),
		
		new Reward(58, 64, Material.STONE), new Reward(32, 64, Material.REDSTONE),
		new Reward(32, 64, Material.GLOWSTONE), new Reward(8, 24, Material.QUARTZ),
		new Reward(18, 36, Material.LAPIS_LAZULI), new Reward(58, 64, Material.SAND),
		new Reward(32, 64, Material.STICK), new Reward(58, 64, Material.TORCH),
		new Reward(32, 64, Material.BRICKS), new Reward(10, 20, Material.APPLE),
		new Reward(10, 20, Material.COOKED_SALMON), new Reward(8, 16, Material.MELON),
		new Reward(48, 64, Material.ARROW), new Reward(4, 8, Material.ENDER_PEARL),
		new Reward(8, 16, Material.SLIME_BALL),
	};
	
	public static void process(List<Player> channelPlayers, int months, SubPlan subPlan, String gifterNickname) {
		int calcMoneyTimesFive = months;
		if (subPlan == SubPlan.LEVEL_2) {
			calcMoneyTimesFive = months*2;
		} else if (subPlan == SubPlan.LEVEL_3) {
			calcMoneyTimesFive = months*5;
		}
		
		int tier = 0;
		if (calcMoneyTimesFive >= 6) {
			tier = 3;
		} else if (calcMoneyTimesFive >= 4) {
			tier = 2;
		} else if (calcMoneyTimesFive >= 2) {
			tier = 1;
		}
		
		gifterNickname = gifterNickname.replace(' ', '_');
		
		for (Player p : channelPlayers) {
			PlayerData pd = PlayerManager.getPlayerData(p);
			if (pd.getConfig("rewards")) {
				if (pd.getConfig("specialitems")) {
					giftCustomItem(p, new SubGift(), tier, gifterNickname);
				} else {
					giftRandomItem(p, tier, gifterNickname);
				}
			}
		}
	}
	
	private static void giftRandomItem(Player p, int tier, String gifterNickname) {
		ItemStack reward = null;
		switch(tier) {
		case 0:
			reward = commonRewards[(int) (Math.random() * commonRewards.length)].get();
			break;
		case 1:
			reward = uncommonRewards[(int) (Math.random() * uncommonRewards.length)].get();
			break;
		case 2:
			reward = rareRewards[(int) (Math.random() * rareRewards.length)].get();
			break;
		case 3:
			reward = legendaryRewards[(int) (Math.random() * legendaryRewards.length)].get();
			break;
		}
		char[] formatMaterial = reward.getType().name().toCharArray();
		String materialName = "";
		boolean alreadyUppercase = false;
		for (int i = 0; i < formatMaterial.length; i++) {
			if (formatMaterial[i] == '_') {
				materialName += " ";
				alreadyUppercase = false;
			} else {
				if (alreadyUppercase) {
					materialName += Character.toLowerCase(formatMaterial[i]);
				} else {
					alreadyUppercase = true;
					materialName += formatMaterial[i];
				}
			}
		}
		MessageSender.info(p, ChatColor.ITALIC + gifterNickname + ChatColor.RESET + "" +
			ChatColor.GOLD + " has gifted you x" + reward.getAmount() + " " + materialName);
		if (p.getInventory().firstEmpty() == -1) {
			// Full
			p.getWorld().dropItemNaturally(p.getLocation(), reward);
			MessageSender.info(p, "Inventory full! Sub reward dropped on ground");
		} else {
			p.getInventory().addItem(reward);
			PlayerManager.updateInventory(p);
		}
	}

	public static ItemStack giftRandomCustomItem(Player p, String gifterNickname, int t) {
		List<CustomItem> cil = CustomItemManager.getCustomItemTiers(t);
		if (cil.isEmpty()) {
			p.playSound(p.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 1, 1);
			p.getLocation().getWorld().spawnParticle(Particle.SMOKE_NORMAL, 
					p.getLocation().add(0, 1, 0), 5, 0, 0, 0, 0.2);
			MessageSender.info(p, ChatColor.GRAY + "Sorry, nothing?");
			return null;
		}
		int rand = (int)(Math.random()*cil.size());
		CustomItem ci = cil.get(rand);
		if (ci instanceof CustomEnchant) {
			rand = (int)(Math.random()*cil.size());
			ci = cil.get(rand);
		}
		if (p != null) {
			giftCustomItem(p, ci, t, gifterNickname);
			return null;
		} else {
			ItemStack is = ci.getRarity(t);
			ci.fixDisplayName(is, gifterNickname, t);
			return is;
		}
	}
	
	public static void giftCustomItem(Player p, CustomItem ci, int tier, String gifterNickname) {
		ItemStack is = ci.getRarity(tier);
		ci.fixDisplayName(is, gifterNickname, tier);
		if (ci.getClass() != SubGift.class) {
			String cn = ci.getCustomName(gifterNickname, tier);
			if (cn != null) {
				MessageSender.info(p, "You found " + cn);
			} else {
				MessageSender.info(p, "You found " + is.getItemMeta().getDisplayName());
			}
		}
		if (p.getInventory().firstEmpty() == -1) {
			// Full
			p.getWorld().dropItemNaturally(p.getLocation(), is);
			MessageSender.info(p, "Inventory full! Sub reward dropped on ground");
		} else {
			p.getInventory().addItem(is);
			PlayerManager.updateInventory(p);
		}
	}

	public static void processBits(List<Player> pl, int bits, String sourceName) {
		for (Player p : pl) {
			PlayerData pd = PlayerManager.getPlayerData(p);
			if (pd != null) {
				pd.setBits(pd.getBits() + bits);
				if (p.getOpenInventory().getTitle().equals(BitsGUI.getTitle())) {
					BitsGUI.open(p);
				}
			}
		}
	}
	
}

class Reward {
	
	private int minAmount, range;
	private Material material;
	
	public Reward(int min, int max, Material m) {
		minAmount = min;
		range = max - min + 1;
		material = m;
	}
	
	public Reward(Material m) {
		this(1, 1, m);
	}
	
	public ItemStack get() {
		return new ItemStackBuilder<>(material)
			.amount(minAmount + (int) (Math.random() * range))
			.build();
	}
	
}

class EnchantReward extends Reward {
	
	private Enchantment enchantment;
	private int enchLevel;
	
	public EnchantReward(Enchantment en, int level) {
		super(1, 1, Material.ENCHANTED_BOOK);
		enchantment = en;
		enchLevel = level;
	}
	
	@Override
	public ItemStack get() {
		ItemStack is = super.get();
		EnchantmentStorageMeta esm = (EnchantmentStorageMeta) is.getItemMeta();
		esm.addStoredEnchant(enchantment, enchLevel, true);
		is.setItemMeta(esm);
		return is;
	}
	
}