package com.manelnavola.mcinteractive.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import com.manelnavola.mcinteractive.bukkit.wrapper.BukkitWrapper;
import com.manelnavola.mcinteractive.core.LifeCycle;
import com.manelnavola.mcinteractive.core.wrappers.WUtils;

/**
 * Main starting point for Bukkit plugins
 * @author Manel Navola
 *
 */
public class Main extends JavaPlugin {
	
	@Override
	public void onEnable() {
		WUtils.setWrapper(new BukkitWrapper(this));
		LifeCycle.getInstance().start();
	}
	
	@Override
	public void onDisable() {
		LifeCycle.getInstance().start();
	}
	
}
