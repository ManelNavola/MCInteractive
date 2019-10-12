package com.manelnavola.mcinteractive.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

public class ConfigManager {
	
	private static Map<String, String> nameToID;
	private static Map<String, List<Config>> commandPairs;
	
	public static void init() {
		commandPairs = new HashMap<>();
		nameToID = new HashMap<>();
		List<Config> list;
		
		// Basic
		list = new ArrayList<Config>();
		register(new Config("Show votes", "Displays votes in Minecraft chat", "showvotes",
				true, Material.PAPER), list);
		register(new Config("Show chat", "Displays Twitch chat in Minecraft chat", "showchat",
				true, Material.FILLED_MAP), list);
		register(new Config("Highlight messages", "Highlights sub/mod Twitch messages", "highlight",
				true, Material.DIAMOND), list);
		register(new Config("Show notices", "Displays important Twitch events as Minecraft titles", "noticetitle",
				true, Material.OAK_SIGN), list);
		commandPairs.put("Chat", list);
		
		// Adventure
		list = new ArrayList<Config>();
		register(new Config("Sub rewards", "Enables chest rewards from subscriptions", "subgifts",
				true, Material.BARREL), list);
		commandPairs.put("Adventure", list);
	}
	
	public static void register(Config c, List<Config> cl) {
		cl.add(c);
		nameToID.put(c.getName(), c.getID());
	}
	
	public static String getIDbyName(String name) {
		return nameToID.get(name);
	}
	
	public static Map<String, List<Config>> getCommandPairs() {
		return commandPairs;
	}
	
	public static Map<String, Boolean> getDefaults() {
		Map<String, Boolean> map = new HashMap<>();
		
		for (List<Config> l : commandPairs.values()) {
			for (Config c : l) {
				map.put(c.getID(), c.getDefault());
			}
		}
		
		return map;
	}
}

class Config {
	
	private String name;
	private String description;
	private String id;
	private boolean def;
	private Material icon;
	
	public Config(String l_name, String desc, String l_id, boolean l_def, Material mat) {
		name = l_name;
		description = desc;
		id = l_id;
		def = l_def;
		icon = mat;
	}
	
	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getID() { return id; }
	public boolean getDefault() { return def; }
	public Material getIcon() { return icon; }
	
}