package com.manelnavola.mcinteractive.core.managers;

import java.util.concurrent.ConcurrentHashMap;

import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.mcinteractive.core.wrappers.Wrapper;

/**
 * Singleton class for managing configuration files
 * Configuration has as keys lowercaseCamel and nested values are represented as parent.child
 * 
 * @author Manel Navola
 *
 */
public class ConfigurationManager extends Manager {
	
	public enum ConfigField {
		STREAM_FREEJOIN("streamFreeJoin");
		
		String configKey;
		
		ConfigField(String configKey) {
			this.configKey = configKey;
		}
		
		public <T extends Object> T getConfigValue(final Class<T> type) {
			return ConfigurationManager.getInstance().getConfiguration(configKey, type);
		}
	}
	
	private static ConfigurationManager INSTANCE;

	private ConfigurationManager() {
	}

	private ConcurrentHashMap<String, Object> configurationMap;

	/**
	 * Gets the singleton object
	 * 
	 * @return The singleton object
	 */
	public static ConfigurationManager getInstance() {
		if (INSTANCE == null)
			INSTANCE = new ConfigurationManager();
		return INSTANCE;
	}

	/**
	 * Gets a configuration object
	 * 
	 * @param <T>          The class type to retrieve
	 * @param configString The string key of the configuration
	 * @param type         The class type to retrieve
	 * @return An object of class T of the configuration or null if not found or
	 *         mismatched class
	 */
	private <T extends Object> T getConfiguration(final String configString, final Class<T> type) {
		Object obj = configurationMap.get(configString);
		if (obj == null) {
			return null;
		} else if (obj.getClass().isAssignableFrom(type)) {
			return type.cast(obj);
		} else {
			ChatUtils.broadcastOpError("Wrong configuration string class casting! (" + configString + ") expected "
					+ type.getCanonicalName() + " but got " + obj.getClass().getCanonicalName());
			return null;
		}
	}
	
	/**
	 * Gets a player configuration object
	 * 
	 * @param <T>          The class type to retrieve
	 * @param configString The string key of the configuration
	 * @param type         The class type to retrieve
	 * @return An object of class T of the configuration or null if not found or
	 *         mismatched class
	 */
	public <T extends Object> T getPlayerConfiguration(final String configString, final Class<T> type) {
		Object obj = configurationMap.get(configString);
		if (obj == null) {
			return null;
		} else if (obj.getClass().isAssignableFrom(type)) {
			return type.cast(obj);
		} else {
			ChatUtils.broadcastOpError("Wrong configuration string class casting! (" + configString + ") expected "
					+ type.getCanonicalName() + " but got " + obj.getClass().getCanonicalName());
			return null;
		}
	}

	@Override
	public void start() {
		configurationMap = Wrapper.getInstance().getServer().loadConfiguration();
	}

	@Override
	public void stop() {
		Wrapper.getInstance().getServer().saveConfiguration(configurationMap, true);
		configurationMap = null;
		INSTANCE = null;
	}

}
