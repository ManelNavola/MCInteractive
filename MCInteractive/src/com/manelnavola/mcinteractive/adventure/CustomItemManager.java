package com.manelnavola.mcinteractive.adventure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractive.adventure.customenchants.*;
import com.manelnavola.mcinteractive.adventure.customitems.*;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem.CustomItemFlag;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem.CustomItemType;
import com.manelnavola.mcinteractive.generic.PlayerManager;
import com.manelnavola.mcinteractive.utils.ItemStackBuilder;
import com.manelnavola.mcinteractive.utils.MessageSender;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class CustomItemManager {
	
	private static Plugin plugin;
	private static BukkitTask customTrails;
	private static Map<Entity, CustomTrail> customTrailMap;
	private static List<Entity> customTrailMaptoRemove = new ArrayList<Entity>();
	
	private static List<CustomItem> customItemList;
	private static CustomItem subGift;
	private static Map<String, CustomItem> customItemDisplayNameMap;
	private static Map<String, CustomItem> projectiles;
	private static Map<String, CustomItem> entityHits;
	private static Map<String, CustomItem> blockDispenses;
	private static Map<String, CustomItem> burns;
	private static Map<String, CustomItem> breaks;
	private static Map<String, CustomItem> rightClicks;
	private static Map<String, CustomItem> shoots;
	//private static Map<String, CustomItem> mounts;
	
	private static Map<Integer, List<CustomItem>> customItemTierList;
	
	public static void init(Plugin plg) {
		plugin = plg;
		customItemList = new ArrayList<>();
		projectiles = new HashMap<>();
		entityHits = new HashMap<>();
		blockDispenses = new HashMap<>();
		burns = new HashMap<>();
		breaks = new HashMap<>();
		rightClicks = new HashMap<>();
		customItemTierList = new HashMap<>();
		customItemDisplayNameMap = new HashMap<>();
		customTrailMap = new HashMap<>();
		shoots = new HashMap<>();
		
		for (int i = 0; i < 4; i++) {
			customItemTierList.put(i, new ArrayList<>());
		}
		
		register(new Rock());
		register(new FireWand());
		register(new Eggscelent());
		register(new BunnyHop());
		register(new SuperFuel());
		register(new Freezer());
		register(new Smelter());
		register(new Planter());
		register(new Boomer());
		register(new Timber());
		
		subGift = new SubGift();
		setFlags(subGift);
		
		customTrails = Bukkit.getScheduler().runTaskTimer(plg, new Runnable() {
			@Override
			public void run() {
				customTrailMaptoRemove.clear();
				for (Map.Entry<Entity, CustomTrail> en : customTrailMap.entrySet()) {
					Entity e = en.getKey();
					if (e.isDead() || en.getValue().timeDue()) {
						customTrailMaptoRemove.add(e);
					} else {
						en.getValue().showParticleEffect(e.getLocation());
					}
				}
				for (Entity e : customTrailMaptoRemove) {
					customTrailMap.remove(e);
				}
			}
		}, 0L, 2L);
	}
	
	private static void setFlags(CustomItem ci) {
		if (ci.hasFlag(CustomItemFlag.RIGHT_CLICK)) {
			rightClicks.put(ci.getClass().getName(), ci);
		}
		if (ci.hasFlag(CustomItemFlag.PROJECTILE)) {
			projectiles.put(ci.getClass().getName(), ci);
		}
		if (ci.hasFlag(CustomItemFlag.ENTITY_HIT)) {
			entityHits.put(ci.getClass().getName(), ci);
		}
		if (ci.hasFlag(CustomItemFlag.DISPENSES)) {
			blockDispenses.put(ci.getClass().getName(), ci);
		}
		if (ci.hasFlag(CustomItemFlag.BURNS)) {
			burns.put(ci.getClass().getName(), ci);
		}
		if (ci.hasFlag(CustomItemFlag.BLOCK_BREAK)) {
			breaks.put(ci.getClass().getName(), ci);
		}
		if (ci.hasFlag(CustomItemFlag.SHOOT_BOW)) {
			shoots.put(ci.getClass().getName(), ci);
		}
	}
	
	public static void registerTrail(Entity e, CustomTrail ct) {
		customTrailMap.put(e, ct);
	}
	
	public static CustomItem getCustomItemByName(String s) {
		return customItemDisplayNameMap.get(s);
	}
	
	private static void register(CustomItem ci) {
		customItemList.add(ci);
		for (int i = 0; i < 4; i++) {
			if (ci.getRarity(i) != null) {
				String dn = ci.getRarity(i).getItemMeta().getDisplayName();
				if (!customItemDisplayNameMap.containsKey(dn)) {
					customItemDisplayNameMap.put(dn, ci);
				}
				customItemTierList.get(i).add(ci);
			}
		}
		setFlags(ci);
	}
	
	public static CustomItem getSubGift() {
		return subGift;
	}
	
	public static List<CustomItem> getCustomItemTiers(int t) {
		return customItemTierList.get(t);
	}

	public static void onPlayerInteract(CustomItemInfo cii, Player p, PlayerInteractEvent e) {
		if (rightClicks.containsKey(cii.getClassName())) {
			if (cii.isValid()) {
				Boolean lock = PlayerManager.getLock("specialitems");
				if (lock != null && !lock.booleanValue()) {
					MessageSender.err(p, "Cannot use item: the server has disabled custom items!");
					return;
				} else if (!PlayerManager.getPlayerData(p).getConfig("specialitems")) {
					MessageSender.err(p, "Cannot use item: you have disabled custom items!");
					return;
				}
				if (cii.getClassName().equals(SubGift.class.getName())) {
					CustomItem ci = getSubGift();
					p.getInventory().remove(cii.getItemStack());
					ci.onPlayerInteract(p, e, cii);
				} else {
					registerUse(cii, p.getInventory(), p);
					cii.getCustomItem().onPlayerInteract(p, e, cii);
				}
				e.setCancelled(true);
			}
		}
	}
	
	public static ItemStack registerUse(CustomItemInfo cii, Inventory inv, Player p) {
		ItemStack item = cii.getItemStack();
		ItemMeta im = item.getItemMeta();
		List<String> lore = im.getLore();
		String lastLore = ChatColor.stripColor(lore.get(lore.size() - 1));
		int amount = 1;
		int maxUses = 1;
		if (cii.isEnchant()) {
			return item;
		} else if (cii.getCustomItem() instanceof CustomEnchant) {
			if (item.getAmount() == 1) {
				return null;
			} else {
				item.setAmount(item.getAmount() - 1);
				return item;
			}
		}
		if (!lastLore.equals(CustomItemType.SINGLE_USE.getName())) {
			maxUses = Integer.parseInt(lastLore.split("/")[1].split(" ")[0]);
			amount = Integer.parseInt(lastLore.split("/")[0]);
		}
		if (amount == 1) {
			// Delete
			if (item.getAmount() > 1) {
				item.setAmount(item.getAmount() - 1);
				if (!lastLore.equals(CustomItemType.SINGLE_USE.getName())) {
					lore.remove(lore.size() - 1);
					lore.add(ChatColor.GOLD + "" + maxUses + "/" + maxUses + " uses");
					im.setLore(lore);
					item.setItemMeta(im);
				}
			} else {
				inv.remove(item);
				item = null;
			}
		} else {
			// Fix amount
			amount--;
			lore.remove(lore.size() - 1);
			lore.add(ChatColor.GOLD + "" + amount + "/" + maxUses + " uses");
			im.setLore(lore);
			item.setItemMeta(im);
		}
		if (!cii.isSingleUse() && p != null) {
			if (item == null) {
				p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1.1F);
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("").create());
			} else {
				if (amount == 1) {
					p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
							new ComponentBuilder(ChatColor.GRAY + "" + amount + " use remaining").create());
				} else {
					p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
							new ComponentBuilder(ChatColor.GRAY + "" + amount + " uses remaining").create());
				}
			}
		}
		return item;
	}

	public static void onProjectileHit(MetadataValue mv, Entity proj, Block block, Entity entity) {
		String s = mv.asString();
		CustomItem ci = projectiles.get(s.split("/")[0]);
		if (ci != null) {
			Boolean lock = PlayerManager.getLock("specialitems");
			if (lock != null && !lock.booleanValue()) {
				customTrailMap.remove(proj);
				return;
			}
			
			customTrailMap.remove(proj);
			ci.onProjectileHit(proj, block, entity, Integer.parseInt(s.split("/")[1]));
		}
	}

	public static Plugin getPlugin() {
		return plugin;
	}

	public static void onEntityDamageByEntity(CustomItemInfo cii, Player p, Entity e) {
		if (entityHits.containsKey(cii.getClassName())) {
			Boolean lock = PlayerManager.getLock("specialitems");
			if (lock != null && !lock.booleanValue()) {
				return;
			}
			cii.getCustomItem().onEntityDamageByEntity(p, e, cii);
			registerUse(cii, p.getInventory(), p);
		}
	}
	
	private static Vector getVectorFromFace(BlockFace bf) {
		switch(bf) {
		case UP:
			return new Vector(0, 1, 0);
		case DOWN:
			return new Vector(0, -1, 0);
		case EAST:
			return new Vector(1, 0, 0);
		case NORTH:
			return new Vector(0, 0, -1);
		case SOUTH:
			return new Vector(0, 0, 1);
		case WEST:
			return new Vector(-1, 0, 0);
		default:
			return new Vector();
		}
	}

	public static void onBlockDispense(CustomItemInfo cii, BlockDispenseEvent e) {
		if (blockDispenses.containsKey(cii.getClassName())) {
			Boolean lock = PlayerManager.getLock("specialitems");
			if (lock != null && !lock.booleanValue()) {
				return;
			}
			e.setCancelled(true);
			
			Dispenser d = (Dispenser) e.getBlock().getState();
			
			Location bl = d.getLocation().add(new Vector(0.5F, 0.5F, 0.5F));
			Directional dir = (Directional) d.getBlockData();
			Vector vDir = getVectorFromFace(dir.getFacing());
			cii.getCustomItem().onBlockDispense(d, bl.add(vDir), vDir, cii);
			
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					Inventory di = d.getInventory();
					int size = 0;
					int[] found = new int[di.getSize()];
					for (int i = 0; i < di.getSize(); i++) {
						CustomItemInfo cmp = new CustomItemInfo(di.getItem(i));
						if (cmp.isValid()) {
							if (cmp.equals(cii)) {
								found[size] = i;
								size++;
							}
						}
					}
					
					if (size == 0) return;
					
					int slot = found[(int) Math.floor(Math.random()*size)];
					CustomItemInfo slotCii = new CustomItemInfo(di.getItem(slot));
					ItemStack tr = registerUse(slotCii, di, null);
					d.getInventory().setItem(slot, tr);
				}
			}, 1L);
		}
	}

	public static void onFurnaceBurn(CustomItemInfo cii, Furnace f, FurnaceBurnEvent e) {
		if (burns.containsKey(cii.getClassName())) {
			Boolean lock = PlayerManager.getLock("specialitems");
			if (lock != null && !lock.booleanValue()) {
				return;
			}
			f.setCookTimeTotal((short) 200);
			f.update();
			cii.getCustomItem().onBurn(f, e, cii);
		}
	}
	
	public static void onBlockBreak(CustomItemInfo cii, Player player, BlockBreakEvent e) {
		if (breaks.containsKey(cii.getClassName())) {
			Boolean lock = PlayerManager.getLock("specialitems");
			if (lock != null && !lock.booleanValue()) {
				return;
			}
			cii.getCustomItem().onBlockBreak(player, e.getBlock(), cii);
		}
	}

	public static List<CustomItem> getCustomItems() {
		return customItemList;
	}

	public static void checkCustomEnchant(PrepareAnvilEvent e) {
		AnvilInventory anvilInv = (AnvilInventory) e.getInventory();
		ItemStack[] itemsInAnvil = anvilInv.getContents();
		ItemStack toEnchant = itemsInAnvil[0];
		ItemStack result = e.getResult();
		
		if (new CustomItemInfo(toEnchant).isValid()) {
			e.getResult().setType(Material.AIR);
			e.setResult(null);
			Bukkit.getScheduler().runTaskLater(getPlugin(), new Runnable() {
				@Override
				public void run() {
					if (e.getResult() != null) {
						e.getResult().setType(Material.AIR);
					}
					e.setResult(null);
				}
			}, 1L);
			return;
		}
		
		if (toEnchant != null && toEnchant.getType() != Material.AIR && toEnchant.getEnchantments().isEmpty()) {
			if (result == null || result.getType() == Material.AIR) {
				Boolean lock = PlayerManager.getLock("specialitems");
				if (lock != null && !lock.booleanValue()) {
					return;
				}
				CustomItemInfo cii = new CustomItemInfo(itemsInAnvil[1]);
				if (cii.isValid() && cii.isEnchant()) {
					if (cii.getCustomEnchant().isCompatible(toEnchant.getType())) {
						String newEnchantLore = cii.getItemStack().getItemMeta().getDisplayName();
						newEnchantLore = CustomEnchant.CUSTOM_PREFIX + newEnchantLore;
						anvilInv.setRepairCost(5 * cii.getTier() + 5);
						Bukkit.getScheduler().runTaskLater(getPlugin(), new Runnable() {
							@Override
							public void run() {
								anvilInv.setRepairCost(5 * cii.getTier() + 5);
							}
						}, 1L);
						newEnchantLore = newEnchantLore.substring(0, newEnchantLore.lastIndexOf('[') - 1);
						e.setResult(new ItemStackBuilder<>(toEnchant)
								.newLore(newEnchantLore)
								.addEnchantEffect()
								.build());
					}
				}
			}
		}
	}

	public static void onEntityShootBow(Player player, Entity proj, CustomItemInfo cii) {
		if (shoots.containsKey(cii.getClassName())) {
			Boolean lock = PlayerManager.getLock("specialitems");
			if (lock != null && !lock.booleanValue()) {
				return;
			}
			cii.getCustomItem().onEntityShootBow(player, proj, cii);
		}
	}

	public static void dispose() {
		if (customTrails != null) customTrails.cancel();
	}
}
