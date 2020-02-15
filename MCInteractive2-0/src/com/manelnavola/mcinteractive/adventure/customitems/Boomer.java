package com.manelnavola.mcinteractive.adventure.customitems;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomItemStackBuilder;
import com.manelnavola.mcinteractive.adventure.CustomTrail;

public class Boomer extends CustomItemStackable {
	
	private static CustomTrail trail = new CustomTrail(Particle.SMOKE_NORMAL, 1, 0.33);
	
	public Boomer() {
		super(new CustomItemFlag[] {CustomItemFlag.DISPENSES, CustomItemFlag.RIGHT_CLICK});
		ItemStack rare = new CustomItemStackBuilder<>(Material.TNT)
				.name("Boomer")
				.amount(10)
				.lore("Right click to throw ignited tnt!")
				.stackable()
				.addEnchantEffect()
				.build();
		setRarities(null, null, rare, null);
	}
	
	@Override
	public void onPlayerInteract(Player p, PlayerInteractEvent ev, CustomItemInfo cii) {
		p.getWorld().playSound(p.getLocation(), Sound.ENTITY_SNOW_GOLEM_SHOOT, 1F, 1F);
		TNTPrimed tp = p.getWorld().spawn(p.getLocation(), TNTPrimed.class);
		tp.setVelocity(p.getLocation().getDirection().multiply(0.7F).add(new Vector(0, 0.2F, 0)));
		registerTrail(tp, trail);
	}
	
	@Override
	public void onBlockDispense(Dispenser d, Location l, Vector dir, CustomItemInfo cii) {
		TNTPrimed tp = l.getWorld().spawn(l, TNTPrimed.class);
		tp.setVelocity(dir.multiply(0.7F).add(new Vector(0, 0.2F, 0)));
		registerTrail(tp, trail);
	}
	
}
