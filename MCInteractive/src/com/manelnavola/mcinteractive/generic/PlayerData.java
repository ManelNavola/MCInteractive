package com.manelnavola.mcinteractive.generic;

import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

public class PlayerData extends PlayerManager {
	
	private FileConfiguration fileConfig;
	private Map<String, Boolean> configMap;
	private int bits = 0;
	private String playerUUID;
	private Object bitsLock = new Object();
	
	public PlayerData(FileConfiguration fc, String uuid) {
		fileConfig = fc;
		playerUUID = uuid;
		
		configMap = ConfigManager.getDefaults();
		
		if (!fileConfig.contains(playerUUID)) {
			return;
		}
		
		for (String config : configMap.keySet()) {
			if (fileConfig.contains(playerUUID + "." + config)) {
				setConfig(config, fileConfig.getBoolean(playerUUID + "." + config));
			}
		}
		
		if (fileConfig.contains(playerUUID + ".bits")) {
			bits = fileConfig.getInt(playerUUID + ".bits");
		} else {
			bits = 0;
		}
	}
	
	public int getBits() {
		synchronized (bitsLock) {
			return bits;
		}
	}
	
	public void setBits(int bb) {
		synchronized (bitsLock) {
			bits = bb;
		}
	}
	
	public boolean getConfig(String config) {
		Boolean b = PlayerManager.getLock(config);
		if (b != null) {
			return b.booleanValue();
		}
		synchronized(configMap) {
			return configMap.get(config);
		}
	}
	
	public void setConfig(String config, boolean value) {
		synchronized(configMap) {
			configMap.put(config, value);
		}
	}
	
	public void save() {
		synchronized(fileConfig) {
			fileConfig.set(playerUUID, null);
			for (String config : configMap.keySet()) {
				fileConfig.set(playerUUID + "." + config, getConfig(config));
			}
			fileConfig.set(playerUUID + ".bits", bits);
		}
	}

	public void addBits(int n) {
		synchronized (bitsLock) {
			bits += n;
		}
	}
	
}
