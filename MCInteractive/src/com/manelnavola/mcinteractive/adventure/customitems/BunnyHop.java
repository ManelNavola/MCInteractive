package com.manelnavola.mcinteractive.adventure.customitems;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomItemStackBuilder;

public class BunnyHop extends CustomItem {

	public BunnyHop() {
		super(new CustomItemFlag[] {CustomItemFlag.DISPENSES, CustomItemFlag.RIGHT_CLICK});
		ItemStack uncommon = new CustomItemStackBuilder<>(Material.RABBIT_FOOT)
				.name("Bunny hop")
				.lore("Right click to fling yourself")
				.uses(8)
				.addEnchantEffect()
				.build();
		ItemStack common = new CustomItemStackBuilder<>(uncommon)
				.newLore("Right click to boost lightly")
				.uses(4)
				.build();
		setRarities(common, uncommon, null, null);
	}
	
	@Override
	public void onPlayerInteract(Player p, PlayerInteractEvent ev, CustomItemInfo cii) {
		if (cii.getTier() == 0) {
			p.setVelocity(p.getLocation().getDirection().multiply(0.5F).add(new Vector(-0.03F, 0.1F, -0.03F)));
		} else {
			p.setVelocity(p.getLocation().getDirection().add(new Vector(-0.03F, 0.1F, -0.03F)));
		}
		Location l = p.getLocation();
		p.getWorld().playSound(l, Sound.ENTITY_SNOW_GOLEM_SHOOT, 1F, 1F);
		l.getWorld().spawnParticle(Particle.CLOUD, l.getX(), l.getY(), l.getZ(), 5, 0.5, 0.5, 0.5, 0.5);
	}
	
	@Override
	public void onBlockDispense(Dispenser d, Location l, Vector dir, CustomItemInfo cii) {
		Entity[] entities = l.getChunk().getEntities();
		l.getWorld().spawnParticle(Particle.CLOUD, l.getX(), l.getY(), l.getZ(), 5, 0.5, 0.5, 0.5, 0.5);
		for (Entity e : entities) {
			if (e instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) e;
				if (le.getLocation().distance(l) < 1.1F) {
					if (cii.getTier() == 0) {
						le.setVelocity(le.getVelocity().multiply(0.5F).add(dir.add(new Vector(-0.03F, 0.1F, -0.03F))));
					} else {
						le.setVelocity(le.getVelocity().add(dir.add(new Vector(-0.03F, 0.1F, -0.03F))));
					}
				}
			}
		}
	}
	
}
