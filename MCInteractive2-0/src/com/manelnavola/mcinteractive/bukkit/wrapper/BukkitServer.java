package com.manelnavola.mcinteractive.bukkit.wrapper;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.manelnavola.mcinteractive.core.wrappers.WPlayer;
import com.manelnavola.mcinteractive.core.wrappers.WServer;

/**
 * Wrapper implementation using Bukkit API
 * @author Manel Navola
 *
 */
public class BukkitServer extends WServer<Server> {
	
	/**
	 * Wrapper constructor
	 * @param plugin The Bukkit plugin
	 */
	public BukkitServer(Server bukkitServer) {
		super(bukkitServer);
	}

	@Override
	public Collection<WPlayer<?>> getOnlinePlayers() {
		Collection<WPlayer<?>> players = new ArrayList<>();
		for (Player p : getServer().getOnlinePlayers()) {
			players.add(new BukkitPlayer(p));
		}
		return players;
	}

	@Override
	public void sendConsoleMessage(String message) {
		getServer().getConsoleSender().sendMessage(message);
	}

}
