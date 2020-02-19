package com.manelnavola.mcinteractive.bukkit.wrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.manelnavola.mcinteractive.core.wrappers.WPlayer;
import com.manelnavola.mcinteractive.core.wrappers.Wrapper;

/**
 * Wrapper implementation using Bukkit API
 * @author Manel Navola
 *
 */
public class BukkitWrapper extends Wrapper {
	
	private Plugin plugin;
	
	/**
	 * Wrapper constructor
	 * @param plugin The Bukkit plugin
	 */
	public BukkitWrapper(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public final Collection<WPlayer> getOnlinePlayers() {
		List<WPlayer> players = new ArrayList<>();
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			players.add(Conversion.convert(p));
		}
		return players;
	}

	@Override
	public void sendMessage(WPlayer wp, String message) {
		((Player) wp.getAPIObject()).sendMessage(message);
	}

	@Override
	public void sendConsoleMessage(String message) {
		plugin.getServer().getConsoleSender().sendMessage(message);
	}

}
