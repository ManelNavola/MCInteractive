package com.manelnavola.mcinteractiveold.adventure.customitems;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import com.manelnavola.mcinteractiveold.adventure.CustomItemInfo;
import com.manelnavola.mcinteractiveold.adventure.CustomItemStackBuilder;
import com.manelnavola.mcinteractiveold.adventure.CustomTrail;

@SuppressWarnings("deprecation")
public class Rock extends CustomItemStackable {
	
	private static CustomTrail trail = new CustomTrail(Particle.SMOKE_NORMAL, 1, 0.1);
	
	public Rock() {
		super(new CustomItemFlag[] {CustomItemFlag.DISPENSES, CustomItemFlag.RIGHT_CLICK});
		ItemStack common = new CustomItemStackBuilder<>(Material.STONE)
				.name("Rock")
				.amount(10)
				.lore("Right click to throw!")
				.stackable()
				.addEnchantEffect()
				.build();
		setRarities(common, null, null, null);
	}
	
	@Override
	public void onPlayerInteract(Player p, PlayerInteractEvent ev, CustomItemInfo cii) {
		p.getWorld().playSound(p.getLocation(), Sound.ENTITY_SNOW_GOLEM_SHOOT, 1F, 1F);
		FallingBlock fb = p.getWorld().spawnFallingBlock(p.getLocation(), new MaterialData(Material.STONE));
		fb.setVelocity(p.getLocation().getDirection().multiply(0.7F).add(new Vector(0, 0.2F, 0)));
		registerTrail(fb, trail);
	}
	
	@Override
	public void onBlockDispense(Dispenser d, Location l, Vector dir, CustomItemInfo cii) {
		FallingBlock fb = l.getWorld().spawnFallingBlock(l, new MaterialData(Material.STONE));
		fb.setVelocity(dir.multiply(0.7F).add(new Vector(0, 0.2F, 0)));
		registerTrail(fb, trail);
	}
	
}
