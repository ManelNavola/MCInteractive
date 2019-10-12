package com.manelnavola.mcinteractive.generic;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlayerManager {
	
	private static Plugin plugin;
	private static Map<String, PlayerData> playerDataMap = new HashMap<>();
	private static File playerSaveFile;
	private static FileConfiguration playerSave;
	private static String error = null;
	
	public static void init(Plugin plg) {
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
			playerDataMap.remove(uuid);
		}
	}
	
	public static void saveAll() {
		for (PlayerData pd : playerDataMap.values()) {
			pd.save();
		}
		
		try {
			playerSave.save(playerSaveFile);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not save players.yml!");
			plugin.getLogger().log(Level.SEVERE, "Restart the server and if the problem persists contact the developer with the following information:");
			plugin.getLogger().log(Level.SEVERE, e.toString());
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