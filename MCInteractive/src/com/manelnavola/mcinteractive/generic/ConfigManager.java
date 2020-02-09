package com.manelnavola.mcinteractive.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

public class ConfigManager {
	
	private static Map<String, String> nameToID;
	private static List<ConfigContainer> configContainers;
	
	public static void init() {
		configContainers = new ArrayList<>();
		nameToID = new HashMap<>();
		List<Config> list;
		
		// Basic
		list = new ArrayList<Config>();
		register(new Config("Show chat",
				"Displays Twitch chat in Minecraft chat",
				"showchat",
				true, Material.FILLED_MAP), list);
		register(new Config("Highlight messages",
				"Highlights sub/mod Twitch messages",
				"highlight",
				true, Material.DIAMOND), list);
		register(new Config("Show votes",
				"Displays votes in Minecraft chat",
				"showvotes",
				true, Material.PAPER), list);
		register(new Config("Show notices",
				new String[] {"Displays important Twitch events", "as Minecraft titles"},
				"noticetitle",
				true, Material.OAK_SIGN), list);
		configContainers.add(new ConfigContainer("Chat", list));
		
		// Adventure
		list = new ArrayList<Config>();
		register(new Config("Twitch Rewards",
				new String[] {"Enables rewards from", "subscriptions and cheers"},
				"rewards",
				false, Material.BARREL), list);
		register(new Config("Custom items",
				"Enables using and obtaining custom items",
				"customitems",
				false, Material.FLINT_AND_STEEL), list);
		register(new Config("Custom voting events",
				"Enables random vote-based events",
				"eventsvote",
				false, Material.CHORUS_FRUIT), list);
		register(new Config("Bits",
				new String[] {"Enables using the bit shop and", "obtaining bits via cheers"},
				"bitshop",
				true, Material.PRISMARINE_SHARD), list);
		register(new Config("Bit drops",
				new String[] {"Enables obtaining additional bits", "as you play"},
				"bitdrops",
				false, Material.PRISMARINE_CRYSTALS), list);
		configContainers.add(new ConfigContainer("Adventure", list));
	}
	
	public static void register(Config c, List<Config> cl) {
		cl.add(c);
		nameToID.put(c.getName(), c.getID());
	}
	
	public static String getIDbyName(String name) {
		return nameToID.get(name);
	}
	
	public static List<ConfigContainer> getConfigContainers() {
		return configContainers;
	}
	
	public static Config[] getConfigList() {
		int total = 0;
		for (ConfigContainer cc : configContainers) {
			total += cc.getConfigs().size();
		}
		Config[] cl = new Config[total];
		int i = 0;
		for (ConfigContainer cc : configContainers) {
			for (Config c : cc.getConfigs()) {
				cl[i] = c;
				i++;
			}
		}
		return cl;
	}
	
	public static Map<String, Boolean> getDefaults() {
		Map<String, Boolean> map = new HashMap<>();
		
		for (ConfigContainer cc : configContainers) {
			for (Config c : cc.getConfigs()) {
				map.put(c.getID(), c.getDefault());
			}
		}
		
		return map;
	}
}

class ConfigContainer {
	
	private String name;
	private List<Config> configs;
	
	public ConfigContainer(String n, List<Config> l) {
		name = n;
		configs = l;
	}
	
	public String getName() { return name; }
	public List<Config> getConfigs() { return configs; }
	
}
