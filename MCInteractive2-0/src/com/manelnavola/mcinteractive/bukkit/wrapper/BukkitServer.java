package com.manelnavola.mcinteractive.bukkit.wrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.mcinteractive.core.utils.ChatUtils.LogMessageType;
import com.manelnavola.mcinteractive.core.wrappers.WPlayer;
import com.manelnavola.mcinteractive.core.wrappers.WServer;

/**
 * Wrapper implementation using Bukkit API
 * @author Manel Navola
 *
 */
public class BukkitServer extends WServer<Server> {
	
	private Plugin plugin;
	private Lock saveLock = new ReentrantLock();
	private File playerSaveFile;
	private FileConfiguration playerSave;
	
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
		if (plugin.isEnabled()) {
			new ServerThreadRunnable(this, consumer).runTask(plugin);
		}
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
		saveLock.lock();
		try {
			FileConfiguration fc = plugin.getConfig();
			synchronized(fc) {
				Set<String> keys = fc.getKeys(true);
				for (String key : keys) {
					chm.put(key, fc.get(key));
				}
			}
		} finally {
			saveLock.unlock();
		}
		return chm;
	}

	@Override
	public void saveConfiguration(ConcurrentHashMap<String, Object> configurationMap, boolean unload) {
		saveLock.lock();
		try {
			FileConfiguration fc = plugin.getConfig();
			for (String key : fc.getKeys(false)) {
				fc.set(key, null);
			}
			configurationMap.forEach(new BiConsumer<String, Object>() {
				@Override
				public void accept(String key, Object value) {
					fc.set(key, value);
				}
			});
			plugin.saveConfig();
			try {
				if (playerSave != null) {
					playerSave.save(playerSaveFile);
				}
			} catch (IOException e) {
				ChatUtils.logOperators("Could not save players.yml!", LogMessageType.ERROR);
				ChatUtils.logOperators(e);
			}
		} finally {
			saveLock.unlock();
		}
	}

	@Override
	public ConcurrentHashMap<String, Object> loadPlayerConfiguration(String id) {
		ConcurrentHashMap<String, Object> chm = new ConcurrentHashMap<>();
		saveLock.lock();
		try {
			if (playerSaveFile == null) {
				try {
					playerSaveFile = new File(plugin.getDataFolder(), "players.yml");
					playerSave = new YamlConfiguration();
					if (playerSaveFile.exists()) {
						playerSave.load(playerSaveFile);
					} else {
						playerSaveFile.getParentFile().mkdirs();
					}
				} catch (Exception e) {
					ChatUtils.logOperators("Could not load/create players.yml!", LogMessageType.ERROR);
					ChatUtils.logOperators(e);
				}
			}
			
			if (playerSave != null) {
				ConfigurationSection cs = playerSave.getConfigurationSection(id);
				if (cs != null) {
					Set<String> keys = cs.getKeys(true);
					for (String key : keys) {
						chm.put(key, cs.get(key));
					}
				}
			}
		} finally {
			saveLock.unlock();
		}
		
		return chm;
	}

	@Override
	public void savePlayerConfiguration(WPlayer<?> wp, ConcurrentHashMap<String, Object> configurationMap) {
		saveLock.lock();
		try {
			if (playerSave != null) {
				ConfigurationSection cs = playerSave.getConfigurationSection(wp.getUUID());
				if (cs != null) {
					for (String key : cs.getKeys(false)) {
						cs.set(key, null);
					}
				}
				configurationMap.forEach(new BiConsumer<String, Object>() {
					@Override
					public void accept(String key, Object value) {
						playerSave.set(wp.getUUID() + "." + key, value);
					}
				});
			}
		} finally {
			saveLock.unlock();
		}
	}

	@Override
	public boolean isServerLoggingAvailable() {
		return plugin.isEnabled();
	}

}
