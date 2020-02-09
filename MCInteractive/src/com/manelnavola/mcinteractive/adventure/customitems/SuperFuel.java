package com.manelnavola.mcinteractive.adventure.customitems;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;

import com.manelnavola.mcinteractive.Main;
import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomItemStackBuilder;

public class SuperFuel extends CustomItemStackable {
	
	public SuperFuel() {
		super(new CustomItemFlag[] {CustomItemFlag.BURNS});
		ItemStack common = new CustomItemStackBuilder<>(Material.CHARCOAL)
				.name("Super Fuel")
				.amount(10)
				.lore("Instantly smelt or cook!")
				.stackable()
				.addEnchantEffect()
				.build();
		setRarities(common, null, null, null);
	}
	
	@Override
	public void onBurn(Furnace f, FurnaceBurnEvent e, CustomItemInfo cii) {
		if (Main.isOn1_13()) {
			f.setCookTime((short) 196);
			e.setBurnTime((short) 4);
			ItemStack is = f.getInventory().getFuel();
			if (is.getAmount() == 1) {
				f.getInventory().setFuel(null);
			} else {
				is.setAmount(is.getAmount() - 1);
				Bukkit.getScheduler().runTaskLater(Main.plugin, new Runnable() {
					@Override
					public void run() {
						f.getInventory().setFuel(is);
					}
				}, 1L);
			}
			f.update();
		} else {
			if (f.getCookTimeTotal() == 200) {
				f.setCookTimeTotal((short) 4);
				e.setBurnTime((short) 4);
				ItemStack is = f.getInventory().getFuel();
				if (is.getAmount() == 1) {
					f.getInventory().setFuel(null);
				} else {
					is.setAmount(is.getAmount() - 1);
					Bukkit.getScheduler().runTaskLater(Main.plugin, new Runnable() {
						@Override
						public void run() {
							f.getInventory().setFuel(is);
						}
					}, 1L);
				}
				f.update();
			}
		}
	}
	
}
