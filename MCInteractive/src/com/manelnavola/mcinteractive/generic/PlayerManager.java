package com.manelnavola.mcinteractive.generic;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import com.manelnavola.mcinteractive.command.commandobjects.CommandTime;
import com.manelnavola.mcinteractive.utils.Log;

public class PlayerManager {
	
	private static Plugin plugin;
	private static Map<String, PlayerData> playerDataMap = new HashMap<>();
	private static File playerSaveFile;
	private static FileConfiguration playerSave;
	private static String error = null;
	private static FileConfiguration config;
	private static BukkitTask saveTimer;
	private static boolean eventForce = false;
	
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
		
		plugin.saveDefaultConfig();
		config = plugin.getConfig();
		
		String ef = PlayerManager.getConfig().getString("eventforce");
		if (ef != null) {
			if (ef.toLowerCase().equals("true")) {
				eventForce = true;
			} else if (!ef.toLowerCase().equals("false")) {
				Log.error("Invalid eventforce configuration value '" + ef + "'!");
				Log.warn("Defaulting to false");
			}
		}
		
		int autoSaveTime = 60*10;
		String autoSave = config.getString("autosave");
		if (autoSave != null) {
			if (!autoSave.toLowerCase().equals("disable")) {
				String err = new CommandTime(1, Integer.MAX_VALUE).pass(autoSave);
				if (err != null) {
					Log.error("Error setting auto save time from config: " + err);
					Log.warn("Defaulting to 10 minutes!");
				} else {
					autoSaveTime = CommandTime.textToTime(autoSave);
				}
				saveTimer = Bukkit.getScheduler().runTaskTimer(plg, new Runnable() {
					@Override
					public void run() {
						saveAll();
					}
				}, autoSaveTime*20L, autoSaveTime*20L);
			}
		} else {
			saveTimer = Bukkit.getScheduler().runTaskTimer(plg, new Runnable() {
				@Override
				public void run() {
					saveAll();
				}
			}, autoSaveTime*20L, autoSaveTime*20L);
		}
	}
	
	public static FileConfiguration getConfig() {
		return config;
	}
	
	public static void setLock(String configID, Boolean b) {
		if (b != null) {
			config.set("locks." + configID, b);
		} else {
			config.set("locks." + configID, null);
		}
	}
	
	public static Boolean getLock(String configID) {
		String ccc = "locks." + configID;
		if (config.contains(ccc, true)) {
			return new Boolean(config.getBoolean(ccc));
		}
		return null;
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
	
	public static synchronized void saveAll() {
		for (String uuid : playerDataMap.keySet()) {
			playerDataMap.get(uuid).save();
		}
		if (config.isConfigurationSection("locks")) {
			Set<String> ss = config.getConfigurationSection("locks").getKeys(false);
			if (ss.isEmpty()) {
				config.set("locks", null);
			}
		}
		plugin.saveConfig();
		
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
		if (saveTimer != null) {
			saveTimer.cancel();
		}
		saveAll();
	}

	public static boolean isValid() {
		return (getError() == null) || (getError().isEmpty());
	}

	public static void updateInventory(Player p) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				p.updateInventory();
			}
		}, 01L);
	}

	public static boolean getEventForce() {
		return eventForce;
	}
	
}