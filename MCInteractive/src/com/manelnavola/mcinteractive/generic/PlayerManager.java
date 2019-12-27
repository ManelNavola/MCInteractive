package com.manelnavola.mcinteractive.generic;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.manelnavola.mcinteractive.utils.Log;

public class PlayerManager {
	
	private static Plugin plugin;
	private static Map<String, PlayerData> playerDataMap = new HashMap<>();
	private static File playerSaveFile;
	private static FileConfiguration playerSave;
	private static String error = null;
	private static FileConfiguration config;
	private static Map<String, Boolean> locks;
	
	public static void init(Plugin plg) {
		saveAll();
		
		plugin = plg;
		playerSave = new YamlConfiguration();
		try {
			playerSaveFile = new File(plugin.getDataFolder(), "players.yml");
			if (!playerSaveFile.exists()) {
				playerSaveFile.getParentFile().mkdirs();
			}
			playerSave.load(playerSaveFile);
		} catch (IOException | InvalidConfigurationException e) {
			error = e.toString();
		}
		
		plugin.saveDefaultConfig();
		config = plugin.getConfig();
		
		locks = new HashMap<>();
		ConfigurationSection cs = config.getConfigurationSection("locks");
		if (cs != null) {
			for (String value : cs.getKeys(false)) {
				locks.put(value, cs.getBoolean(value));
			}
		}
	}
	
	public static void setGlobalConfig(String configID, Boolean b) {
		if (b != null) {
			locks.put(configID, b);
		} else {
			locks.remove(configID);
		}
	}
	
	public static Boolean getLock(String config) {
		return locks.get(config);
	}
	
	public static void playerJoin(Player p) {
		String uuid = p.getUniqueId().toString();
		playerDataMap.put(uuid, new PlayerData(playerSave, uuid));
	}
	
	public static PlayerData getPlayerData(Player p) {
		String uuid = p.getUniqueId().toString();
		if (playerDataMap.containsKey(uuid)) {
			return playerDataMap.get(uuid);
		} else {
			return null;
		}
	}

	public static void playerQuit(Player p) {
		String uuid = p.getUniqueId().toString();
		if (playerDataMap.containsKey(uuid)) {
			playerDataMap.get(uuid).save();
			playerDataMap.remove(uuid);
		}
	}
	
	public static void saveAll() {
		if (playerDataMap != null) {
			for (PlayerData pd : playerDataMap.values()) {
				pd.save();
			}
		}
		
		if (config != null) {
			ConfigurationSection cs = config.getConfigurationSection("locks");
			if (cs != null) {
				for (String value : cs.getKeys(false)) {
					Log.info("Set " + value + " to null");
					cs.set(value, null);
				}
				for (String value : locks.keySet()) {
					Log.info("Set " + value + " to " + locks.get(value));
					cs.set(value, locks.get(value));
				}
			}
		}
		
		if (playerSave != null && playerSaveFile != null) {
			try {
				playerSave.save(playerSaveFile);
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "Could not save players.yml!");
				plugin.getLogger().log(Level.SEVERE, "Restart the server and if the problem persists contact the developer with the following information:");
				plugin.getLogger().log(Level.SEVERE, e.toString());
			}
		}
	}
	
	public static String getError() {
		return error;
	}
	
	public static void dispose() {
		saveAll();
	}

	public static boolean isValid() {
		return (getError() == null) || (getError().isEmpty());
	}

	public static void updateInventory(Player p) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				p.updateInventory();
			}
		}, 01L);
	}
	
}