package com.manelnavola.mcinteractive.adventure.customitems;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomItemStackBuilder;
import com.manelnavola.mcinteractive.adventure.CustomTrail;

public class FireWand extends CustomItem {
	
	private static CustomTrail trail = new CustomTrail(Particle.FLAME, 1, 0.2);
	
	public FireWand() {
		super(new CustomItemFlag[] {CustomItemFlag.DISPENSES, CustomItemFlag.RIGHT_CLICK});
		ItemStack uncommon = new CustomItemStackBuilder<>(Material.STICK)
				.name("Fire wand")
				.lore("Right click to shoot a weak fireball")
				.uses(5)
				.addEnchantEffect()
				.build();
		ItemStack rare = new CustomItemStackBuilder<>(uncommon)
				.newLore("Richt click to shoot a fireball!")
				.build();
		ItemStack legendary = new CustomItemStackBuilder<>(uncommon)
				.newLore("Richt click to shoot a powerful fireball!")
				.build();
		setRarities(null, uncommon, rare, legendary);
	}
	
	private void fixFireball(Fireball f, int tier) {
		registerTrail(f, trail);
		if (tier == 0) {
			f.setIsIncendiary(false);
			f.setYield(2F);
		} else {
			if (tier > 1) {
				f.setDirection(f.getDirection().multiply(1.5F));
				f.setYield(2.8F);
			}
			if (tier == 3) {
				f.setDirection(f.getDirection().multiply(2F));
				f.setYield(3.5F);
			}
		}
	}
	
	@Override
	public void onPlayerInteract(Player p, CustomItemInfo cii) {
		p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1F, 1F);
		Fireball f = p.launchProjectile(Fireball.class);
		f.setShooter(p);
		fixFireball(f, cii.getTier());
	}
	
	@Override
	public void onBlockDispense(Location l, Vector dir, CustomItemInfo cii) {
		l.setDirection(dir);
		Fireball f = l.getWorld().spawn(l, Fireball.class);
		f.setVelocity(dir);
		fixFireball(f, cii.getTier());
	}
	
}
