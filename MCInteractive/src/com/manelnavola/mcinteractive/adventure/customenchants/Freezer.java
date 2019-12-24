package com.manelnavola.mcinteractive.adventure.customenchants;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomTrail;

public class Freezer extends CustomEnchant {
	
	private static CustomTrail trail = new CustomTrail(Particle.WATER_DROP, 1, 0);
	
	public Freezer() {
		super(new CustomItemFlag[] {CustomItemFlag.ENTITY_HIT, CustomItemFlag.PROJECTILE,
				CustomItemFlag.DISPENSES, CustomItemFlag.SHOOT_BOW},
				new CustomEnchantFlag[] {CustomEnchantFlag.SWORD, CustomEnchantFlag.ARROW});
		setRarities(null,
				getEnchantedBook("Freezer I", "20% chance to slow down enemy"),
				getEnchantedBook("Freezer II", "20% chance to freeze an enemy"),
				getEnchantedBook("Freezer III", "20% chance to deeply freeze an enemy"));
	}
	
	private boolean checkChance() {
		return !quickChance(20);
	}
	
	@Override
	public void onProjectileHit(Entity proj, Block b, Entity e, int tier) {
		freeze(e, tier);
	}
	
	@Override
	public void onBlockDispense(Location l, Vector dir, CustomItemInfo cii) {
		Arrow a = l.getWorld().spawn(l, Arrow.class);
		a.setVelocity(dir);
		if (checkChance()) return;
		registerEntity(a, cii.getTier());
		registerTrail(a, trail);
	}
	
	@Override
	public void onEntityDamageByEntity(Player playerDamager, Entity e, CustomItemInfo cii) {
		if (checkChance()) return;
		freeze(e, cii.getTier());
	}
	
	@Override
	public void onEntityShootBow(Player player, Entity proj, CustomItemInfo cii) {
		if (checkChance()) return;
		registerEntity(proj, cii.getTier());
		registerTrail(proj, trail);
	}
	
	private void freeze(Entity e, int tier) {
		if (e instanceof LivingEntity) {
			LivingEntity le = (LivingEntity) e;
			le.getLocation().getWorld().spawnParticle(Particle.WATER_DROP,
					le.getLocation().add(0, le.getHeight()/2.0, 0), 10, 0.5, 0.5, 0.5);
			le.getLocation().getWorld().playSound(le.getLocation(),
					Sound.BLOCK_GLASS_BREAK, 1, 0.8F);
			switch(tier) {
			case 1:
				le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2*20, 3, false));
				break;
			case 2:
				le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2*20, 10, false));
				break;
			case 3:
				le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5*20, 10, false));
				break;
			}
		}
	}

}
