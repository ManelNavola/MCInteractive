package com.manelnavola.mcinteractive.generic;

import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

public class PlayerData extends PlayerManager {
	
	private FileConfiguration fileConfig;
	private Map<String, Boolean> configMap;
	private int bits = 0;
	private String playerUUID;
	
	public PlayerData(FileConfiguration fc, String uuid) {
		fileConfig = fc;
		playerUUID = uuid;
		
		configMap = ConfigManager.getDefaults();
		
		if (!fileConfig.contains(playerUUID)) return;
		
		for (String config : configMap.keySet()) {
			if (fileConfig.contains(playerUUID + "." + config)) {
				setConfig(config, fileConfig.getBoolean(playerUUID + "." + config));
				break;
			}
		}
		bits = fileConfig.getInt(playerUUID + ".bits");
	}
	
	public int getBits() {
		return bits;
	}
	
	public void setBits(int bb) {
		bits = bb;
	}
	
	public boolean getConfig(String config) {
		Boolean b = PlayerManager.getLock(config);
		if (b != null) {
			return b.booleanValue();
		}
		return configMap.get(config);
	}
	
	public void setConfig(String config, boolean value) { configMap.put(config, value); }
	
	public void save() {
		fileConfig.set(playerUUID, null);
		for (String config : configMap.keySet()) {
			fileConfig.set(playerUUID + "." + config, getConfig(config));
		}
		fileConfig.set(playerUUID + ".bits", bits);
	}
	
}
