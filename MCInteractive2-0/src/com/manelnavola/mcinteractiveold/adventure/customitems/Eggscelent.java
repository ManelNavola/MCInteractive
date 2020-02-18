package com.manelnavola.mcinteractiveold.adventure.customitems;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractiveold.adventure.CustomItemInfo;
import com.manelnavola.mcinteractiveold.adventure.CustomItemStackBuilder;
import com.manelnavola.mcinteractiveold.adventure.CustomTrail;

public class Eggscelent extends CustomItem {
	
	private int[] spawns = new int[] {2, 4, 8};
	private EntityType[] passive = new EntityType[] {EntityType.CHICKEN,
			EntityType.COW,
			EntityType.MUSHROOM_COW,
			EntityType.PIG,
			EntityType.RABBIT};
	private static CustomTrail trail = new CustomTrail(Particle.SPELL_WITCH, 1, 0);
	
	public Eggscelent() {
		super(new CustomItemFlag[] {CustomItemFlag.PROJECTILE, CustomItemFlag.DISPENSES, CustomItemFlag.RIGHT_CLICK});
		ItemStack uncommon = new CustomItemStackBuilder<>(Material.EGG)
				.name("Eggscelent")
				.lore("Throw to spawn 4 random farm animals!")
				.addEnchantEffect()
				.build();
		ItemStack common = new CustomItemStackBuilder<>(uncommon)
				.newLore("Throw to spawn 2 random farm animals!")
				.build();
		ItemStack rare = new CustomItemStackBuilder<>(uncommon)
				.newLore("Throw to spawn 8 random farm animals!")
				.build();
		setRarities(common, uncommon, rare, null);
	}
	
	@Override
	public void onPlayerInteract(Player p, PlayerInteractEvent ev, CustomItemInfo cii) {
		p.getWorld().playSound(p.getLocation(), Sound.ENTITY_SNOW_GOLEM_SHOOT, 1F, 1F);
		Egg e = p.launchProjectile(Egg.class);
		registerEntity(e, cii.getTier());
		registerTrail(e, trail);
	}
	
	@Override
	public void onProjectileHit(Entity proj, Block b, Entity e, int tier) {
		Location l = proj.getLocation();
		int amount = spawns[tier];
		double angle = Math.random()*2*Math.PI;
		for (int i = 0; i < amount; i++) {
			Entity ent = l.getWorld().spawnEntity(l, passive[(int)(Math.random()*passive.length)]);
			ent.setVelocity(new Vector(Math.cos(angle)/4F, 0.1, Math.sin(angle)/4F));
			angle += (2*Math.PI)/amount;
		}
		l.getWorld().playSound(l, Sound.BLOCK_BEACON_POWER_SELECT, 1F, 2F);
	}
	
	@Override
	public void onBlockDispense(Dispenser d, Location l, Vector dir, CustomItemInfo cii) {
		Egg e = l.getWorld().spawn(l, Egg.class);
		e.setVelocity(dir);
		registerEntity(e, cii.getTier());
		registerTrail(e, trail);
	}
	
}
