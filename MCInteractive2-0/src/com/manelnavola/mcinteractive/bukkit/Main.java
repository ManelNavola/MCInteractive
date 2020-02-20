package com.manelnavola.mcinteractive.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import com.manelnavola.mcinteractive.bukkit.wrapper.BukkitServer;
import com.manelnavola.mcinteractive.core.LifeCycle;
import com.manelnavola.mcinteractive.core.wrappers.Wrapper;

/**
 * Main starting point for Bukkit plugins
 * @author Manel Navola
 *
 */
public class Main extends JavaPlugin {
	
	@Override
	public void onEnable() {
		Wrapper.getInstance().setServer(new BukkitServer(this.getServer()));
		LifeCycle.getInstance().start();
	}
	
	@Override
	public void onDisable() {
		LifeCycle.getInstance().start();
	}
	
}
