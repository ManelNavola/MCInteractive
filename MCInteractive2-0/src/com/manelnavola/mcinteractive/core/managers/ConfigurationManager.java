package com.manelnavola.mcinteractive.core.managers;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.mcinteractive.core.utils.ChatUtils.LogMessageType;
import com.manelnavola.mcinteractive.core.wrappers.WPlayer;
import com.manelnavola.mcinteractive.core.wrappers.Wrapper;

/**
 * Singleton class for managing configuration files Configuration has as keys
 * lowercaseCamel and nested values are represented as parent.child
 * 
 * @author Manel Navola
 *
 */
public class ConfigurationManager extends Manager implements ManagerListener {

	/**
	 * Represents a configuration field
	 * 
	 * @author Manel Navola
	 *
	 */
	public enum ConfigField {
		STREAM_FREEJOIN("streamfreejoin", new Boolean(false)), STREAM_LIST("streamlist", new ArrayList<>());

		final String configKey;
		final Object def;

		/**
		 * Constructor
		 * 
		 * @param configKey The configuration key that represents this config
		 * @param def       The default value
		 */
		ConfigField(String configKey, Object def) {
			this.configKey = configKey;
			this.def = def;
		}

		/**
		 * Gets a configuration object
		 * 
		 * @param <T> The class type to retrieve
		 * @return An object of class T of the configuration or null if not found or
		 *         mismatched class
		 */
		public <T extends Object> T getConfigValue(final Class<T> type) {
			return ConfigurationManager.getInstance().getConfiguration(configKey, type, def);
		}

		/**
		 * Gets a configuration object as an arraylist
		 * 
		 * @param <T> The type of the arraylist elements
		 * @return An array with elements of class T of the configuration or null if not
		 *         found or mismatched class
		 */
		public <T extends Object> ArrayList<T> getConfigValueAsArrayList(final Class<T> type) {
			Object[] list = ConfigurationManager.getInstance().getConfiguration(configKey, Object[].class,
					new Object[] {});
			ArrayList<T> arrayList = new ArrayList<T>();
			for (Object obj : list) {
				if (obj.getClass().isAssignableFrom(type)) {
					arrayList.add(type.cast(obj));
				} else {
					return new ArrayList<T>();
				}
			}
			return arrayList;
		}

		/**
		 * Sets the configuration value to an object
		 * 
		 * @param obj The object to set the config value to
		 */
		public void setConfigValue(Object obj) {
			ConfigurationManager.getInstance().setConfiguration(configKey, obj);
		}
	}

	public enum PlayerConfigField {
		REGISTERED_STREAM("registeredstream", null), STREAM_BROADCAST("broadcaststream", new Boolean(false));

		final String configKey;
		final Object def;

		PlayerConfigField(String configKey, Object def) {
			this.configKey = configKey;
			this.def = def;
		}

		/**
		 * Gets a player configuration object
		 * 
		 * @param wp  The player to set the configuration to
		 * @param <T> The class type to retrieve
		 * @return An object of class T of the configuration or null if not found or
		 *         mismatched class
		 */
		public <T extends Object> T getConfigValue(final WPlayer<?> wp, final Class<T> type) {
			return ConfigurationManager.getInstance().getPlayerConfiguration(wp, configKey, type, def);
		}

		/**
		 * Sets the configuration value to an object
		 * 
		 * @param wp  The player to set the configuration value to
		 * @param obj The object to set the config value to
		 */
		public void setConfigValue(final WPlayer<?> wp, final Object obj) {
			ConfigurationManager.getInstance().setPlayerConfiguration(wp, configKey, obj);
		}
	}

	private static ConfigurationManager INSTANCE;

	private ConfigurationManager() {
	}

	private ConcurrentHashMap<String, Object> configurationMap;
	private ConcurrentHashMap<WPlayer<?>, ConcurrentHashMap<String, Object>> playerConfigurations;

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
	private <T extends Object> T getConfiguration(String configString, Class<T> type, Object def) {
		requireEnabled();

		Object obj = configurationMap.get(configString);
		if (obj == null) {
			if (def.getClass().isAssignableFrom(type)) {
				return type.cast(def);
			} else {
				ChatUtils.logOperators(
						"Wrong default config string class casting! (" + configString + ") expected "
								+ type.getCanonicalName() + " but got " + def.getClass().getCanonicalName(),
						LogMessageType.ERROR);
				return null;
			}
		} else if (obj.getClass().isAssignableFrom(type)) {
			return type.cast(obj);
		} else {
			ChatUtils.logOperators(
					"Wrong configuration string class casting! (" + configString + ") expected "
							+ type.getCanonicalName() + " but got " + obj.getClass().getCanonicalName(),
					LogMessageType.ERROR);
			return null;
		}
	}

	/**
	 * Sets a configuration field
	 * 
	 * @param configString The string key of the configuration
	 * @param obj          The object to save
	 */
	private void setConfiguration(final String configString, final Object obj) {
		requireEnabled();

		configurationMap.put(configString, obj);
	}

	/**
	 * Gets a player configuration object
	 * 
	 * @param wp           The player to set the configuration to
	 * @param <T>          The class type to retrieve
	 * @param configString The string key of the configuration
	 * @param type         The class type to retrieve
	 * @return An object of class T of the configuration or null if not found or
	 *         mismatched class
	 */
	private <T extends Object> T getPlayerConfiguration(WPlayer<?> wp, String configString, Class<T> type, Object def) {
		requireEnabled();

		ConcurrentHashMap<String, Object> playerConfig = playerConfigurations.get(wp);
		Object obj = null;
		if (playerConfig != null) {
			obj = playerConfig.get(configString);
		}

		if (obj == null) {
			System.out.println("Trying with default...");
			if (def == null) {
				return null;
			}
			if (def.getClass().isAssignableFrom(type)) {
				return type.cast(def);
			} else {
				ChatUtils.logOperators(
						"Wrong default config string class casting! (" + configString + ") expected "
								+ type.getCanonicalName() + " but got " + def.getClass().getCanonicalName(),
						LogMessageType.ERROR);
				return null;
			}
		} else if (obj.getClass().isAssignableFrom(type)) {
			return type.cast(obj);
		} else {
			ChatUtils.logOperators(
					"Wrong configuration string class casting! (" + configString + ") expected "
							+ type.getCanonicalName() + " but got " + obj.getClass().getCanonicalName(),
					LogMessageType.ERROR);
			return null;
		}
	}

	/**
	 * Sets a player configuration field
	 * 
	 * @param wp           The player to set the configuration to
	 * @param configString The string key of the configuration
	 * @param obj          The object to save
	 */
	private void setPlayerConfiguration(final WPlayer<?> wp, final String configString, final Object obj) {
		requireEnabled();

		ConcurrentHashMap<String, Object> playerConfig = playerConfigurations.get(wp);
		if (playerConfig != null) {
			if (obj == null) {
				playerConfig.remove(configString);
			} else {
				System.out.println("Saved " + configString + " with " + obj);
				playerConfig.put(configString, obj);
			}
		}
	}

	@Override
	public void onPlayerJoin(WPlayer<?> wp) {
		requireEnabled();

		ConcurrentHashMap<String, Object> configMap = Wrapper.getInstance().getServer()
				.loadPlayerConfiguration(wp.getUUID());
		playerConfigurations.put(wp, configMap);
	}

	@Override
	public void onPlayerLeave(WPlayer<?> wp) {
		requireEnabled();

		ConcurrentHashMap<String, Object> configMap = playerConfigurations.get(wp);
		if (configMap != null) {
			Wrapper.getInstance().getServer().savePlayerConfiguration(wp, configMap);
			playerConfigurations.remove(wp);
		}
	}

	@Override
	public void start() {
		configurationMap = Wrapper.getInstance().getServer().loadConfiguration();
		playerConfigurations = new ConcurrentHashMap<>();
		EventManager.getInstance().addListener(this);
		setEnabled(true);
	}

	@Override
	public void stop() {
		setEnabled(false);
		Wrapper.getInstance().getServer().saveConfiguration(configurationMap, true);
		EventManager.getInstance().removeListener(this);
		configurationMap = null;
		playerConfigurations = null;
		INSTANCE = null;
	}

}
