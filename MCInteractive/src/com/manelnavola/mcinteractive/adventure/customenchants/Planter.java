package com.manelnavola.mcinteractive.adventure.customenchants;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractive.Main;
import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomItemManager;
import com.manelnavola.mcinteractive.adventure.CustomTrail;

public class Planter extends CustomEnchant {
	
	private static CustomTrail trail = new CustomTrail(Particle.VILLAGER_HAPPY, 1, 0);
	
	public Planter() {
		super(new CustomItemFlag[] {CustomItemFlag.RIGHT_CLICK, CustomItemFlag.DISPENSES,
				CustomItemFlag.SHOOT_BOW, CustomItemFlag.PROJECTILE},
				new CustomEnchantFlag[] {CustomEnchantFlag.HOE, CustomEnchantFlag.ARROW});
		setRarities(null, null,
				getEnchantedBook("Planter I", "20% chance to freeze an enemy"),
				null);
	}
	
	@Override
	public void onProjectileHit(Entity proj, Block b, Entity e, int tier) {
		BlockIterator iterator = new BlockIterator(proj.getWorld(),
				proj.getLocation().toVector(), proj.getVelocity().normalize(), 0.0D, 4);
		
		Block hitBlock = null;
		while (iterator.hasNext()) {
			hitBlock = iterator.next();
			 
			if (hitBlock.getType() != Material.AIR) {
				break;
			}
		}
		
		if (proj.hasMetadata("MCI/Planter")) {
			String md = proj.getMetadata("MCI/Planter").get(0).asString();
			Material td = Material.getMaterial(md);
			if (!tryAlreadyGrow(hitBlock, getFarmable(new ItemStack(td)))) {
				proj.removeMetadata("MCI/Planter", CustomItemManager.getPlugin());
				proj.getWorld().dropItemNaturally(proj.getLocation(), new ItemStack(td));
				proj.getWorld().spawnParticle(Particle.SMOKE_NORMAL,
						proj.getLocation(), 3, 0.2, 0, 0.2, 0.05);
			}
			proj.remove();
		}
	}
	
	@Override
	public void onEntityShootBow(Player player, Entity proj, CustomItemInfo cii) {
		ItemStack toPlant = getPlayerPlant(player);
		if (toPlant == null) return;
		registerEntity(proj, cii.getTier());
		proj.setMetadata("MCI/Planter", new FixedMetadataValue(CustomItemManager.getPlugin(),
				toPlant.getType().name()));
		registerTrail(proj, trail);
		
		if (toPlant.getAmount() == 1) {
			toPlant.setType(Material.AIR);
		} else {
			toPlant.setAmount(toPlant.getAmount() - 1);
		}
	}
	
	@Override
	public void onBlockDispense(Dispenser d, Location l, Vector dir, CustomItemInfo cii) {
		Arrow a = l.getWorld().spawn(l, Arrow.class);
		a.setVelocity(dir);
		
		ItemStack is = null;
		for (int i = 0; i < d.getInventory().getSize(); i++) {
			is = d.getInventory().getItem(i);
			if (getFarmable(is) != null) break;
			is = null;
		}
		
		if (is == null) return;
		registerEntity(a, cii.getTier());
		a.setMetadata("MCI/Planter", new FixedMetadataValue(CustomItemManager.getPlugin(),
				is.getType().name()));
		registerTrail(a, trail);
		
		if (is.getAmount() == 1) {
			is.setType(Material.AIR);
		} else {
			is.setAmount(is.getAmount() - 1);
		}
	}
	
	@Override
	public void onPlayerInteract(Player p, PlayerInteractEvent e, CustomItemInfo cii) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		ItemStack held = p.getInventory().getItemInMainHand();
		if (!held.hasItemMeta() || !(held.getItemMeta() instanceof Damageable)) return;
		ItemStack toPlant = getPlayerPlant(p);
		if (toPlant != null && tryGrow(e.getClickedBlock(), toPlant.getType())) {
			if (toPlant.getAmount() == 1) {
				toPlant.setType(Material.AIR);
			} else {
				toPlant.setAmount(toPlant.getAmount() - 1);
			}
		}
		Material m = e.getClickedBlock().getType();
		if (m == Material.DIRT || m == Material.GRASS_BLOCK || m == Material.GRASS_PATH) {
			Damageable dmg = (Damageable) held.getItemMeta();
			if (dmg.getDamage() + 1 >= held.getType().getMaxDurability()) {
				p.getInventory().setItemInMainHand(null);
				p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
			} else {
				dmg.setDamage(dmg.getDamage() + 1);
				held.setItemMeta((ItemMeta) dmg);
			}
		}
	}
	
	private boolean tryAlreadyGrow(Block b, Material toPlant) {
		if (b != null && b.getType() == Material.FARMLAND) {
			Block top = b.getRelative(BlockFace.UP);
			b.setType(Material.FARMLAND);
			
			if (top != null && top.getType() == Material.AIR) {
				if (toPlant != null) {
					top.getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
							top.getLocation().add(0.5, 0.1, 0.5),
							6, 0.5, 0.1, 0.5, 0);
					top.setType(toPlant);
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean tryGrow(Block b, Material toPlant) {
		if (b != null) {
			Material m = b.getType();
			if (m == Material.DIRT || m == Material.GRASS_BLOCK || m == Material.GRASS_PATH) {
				Block top = b.getRelative(BlockFace.UP);
				b.setType(Material.FARMLAND);
				
				if (top != null && top.getType() == Material.AIR) {
					if (toPlant != null) {
						top.getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
								top.getLocation().add(0.5, 0.1, 0.5),
								6, 0.5, 0.1, 0.5, 0);
						top.setType(getFarmable(new ItemStack(toPlant)));
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private ItemStack getPlayerPlant(Player p) {
		Material toPlant = null;
		ItemStack is = p.getInventory().getItemInOffHand();
		toPlant = getFarmable(is);
		if (toPlant == null) {
			for (int i = 0; i < p.getInventory().getSize(); i++) {
				is = p.getInventory().getItem(i);
				toPlant = getFarmable(is);
				if (toPlant != null) break;
			}
		}
		return is;
	}
	
	private Material getFarmable(ItemStack is) {
		if (is == null) return null;
		if (Main.isOn1_13()) {
			switch(is.getType()) {
			case POTATO:
				return Material.POTATOES;
			case CARROT:
				return Material.CARROTS;
			case PUMPKIN_SEEDS:
				return Material.PUMPKIN_STEM;
			case MELON_SEEDS:
				return Material.MELON_STEM;
			case WHEAT_SEEDS:
				return Material.WHEAT;
			case BEETROOT_SEEDS:
				return Material.BEETROOTS;
			default:
				return null;
			}
		} else {
			switch(is.getType()) {
			case POTATO:
				return Material.POTATOES;
			case CARROT:
				return Material.CARROTS;
			case SWEET_BERRIES:
				return Material.SWEET_BERRY_BUSH;
			case PUMPKIN_SEEDS:
				return Material.PUMPKIN_STEM;
			case MELON_SEEDS:
				return Material.MELON_STEM;
			case WHEAT_SEEDS:
				return Material.WHEAT;
			case BEETROOT_SEEDS:
				return Material.BEETROOTS;
			default:
				return null;
			}
		}
	}

}
