package com.manelnavola.mcinteractive.adventure.customitems;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractive.adventure.CustomItemInfo;

public class Freezer extends CustomEnchant {

	public Freezer() {
		super(new CustomItemFlag[] {CustomItemFlag.ENTITY_HIT, CustomItemFlag.PROJECTILE, CustomItemFlag.DISPENSES},
				new CustomEnchantFlag[] {CustomEnchantFlag.SWORD, CustomEnchantFlag.ARROW});
		setRarities(null,
				getEnchantedBook("Freezer I", "20% chance to slow down enemy"),
				getEnchantedBook("Freezer II", "20% chance to freeze an enemy"),
				getEnchantedBook("Freezer III", "20% chance to deeply freeze an enemy"));
	}
	
	@Override
	public void onProjectileHit(Entity proj, Block b, Entity e, int tier) {
		tryFreeze(e, tier);
	}
	
	@Override
	public void onBlockDispense(Location l, Vector dir, CustomItemInfo cii) {
		Arrow a = l.getWorld().spawn(l, Arrow.class);
		a.setVelocity(dir);
		registerEntity(a, cii.getTier());
	}
	
	@Override
	public void onEntityDamageByEntity(Player playerDamager, Entity e, CustomItemInfo cii) {
		tryFreeze(e, cii.getTier());
	}
	
	private void tryFreeze(Entity e, int tier) {
		if (!quickChance(5)) return;
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
