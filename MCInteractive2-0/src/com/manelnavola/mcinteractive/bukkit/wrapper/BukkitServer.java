package com.manelnavola.mcinteractive.bukkit.wrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.manelnavola.mcinteractive.core.wrappers.WPlayer;
import com.manelnavola.mcinteractive.core.wrappers.WServer;

/**
 * Wrapper implementation using Bukkit API
 * @author Manel Navola
 *
 */
public class BukkitServer extends WServer<Server> {
	
	private Plugin plugin;
	
	/**
	 * Wrapper constructor
	 * @param plugin The Bukkit plugin
	 */
	public BukkitServer(Plugin plugin) {
		super(plugin.getServer());
		this.plugin = plugin;
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

	@Override
	public void runOnServer(Consumer<WServer<?>> consumer) {
		new ServerThreadRunnable(this, consumer).runTask(plugin);
	}
	
	private class ServerThreadRunnable extends BukkitRunnable {
		
		private BukkitServer bukkitServer;
		private Consumer<WServer<?>> consumer;
		
		public ServerThreadRunnable(BukkitServer bukkitServer, Consumer<WServer<?>> consumer) {
			this.bukkitServer = bukkitServer;
			this.consumer = consumer;
		}

		@Override
		public void run() {
			consumer.accept(bukkitServer);
		}
		
	}

	@Override
	public ConcurrentHashMap<String, Object> loadConfiguration() {
		ConcurrentHashMap<String, Object> chm = new ConcurrentHashMap<>();
		FileConfiguration fc = plugin.getConfig();
		Set<String> keys = fc.getKeys(true);
		for (String key : keys) {
			chm.put(key, fc.get(key));
		}
		return chm;
	}

	@Override
	public void saveConfiguration(Map<String, Object> configurationMap, boolean unload) {
		FileConfiguration fc = plugin.getConfig();
		for (String key : fc.getKeys(false)) {
			fc.set(key, null);
		}
		for (Entry<String, Object> entry : configurationMap.entrySet()) {
			fc.set(entry.getKey(), entry.getValue());
		}
	}

}
