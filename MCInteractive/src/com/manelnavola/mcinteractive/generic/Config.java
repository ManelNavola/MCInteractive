package com.manelnavola.mcinteractive.generic;

import org.bukkit.Material;

public class Config {
	
	private String name;
	private String description;
	private String id;
	private boolean def;
	private Material icon;
	private Config prequisite;
	
	public Config(String l_name, String desc, String l_id, boolean l_def, Material mat, Config p) {
		name = l_name;
		description = desc;
		id = l_id;
		def = l_def;
		icon = mat;
		prequisite = p;
	}
	
	public Config(String l_name, String desc, String l_id, boolean l_def, Material mat) {
		this(l_name, desc, l_id, l_def, mat, null);
	}
	
	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getID() { return id; }
	public boolean getDefault() { return def; }
	public Material getIcon() { return icon; }
	public Config getPrequisite() { return prequisite; }
	
}
