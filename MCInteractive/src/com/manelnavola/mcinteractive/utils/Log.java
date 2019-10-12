package com.manelnavola.mcinteractive.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Utils class for quick logging
 * @author Manel
 *
 */
public class Log {

	private static final String PLUGIN_TAG = "[MCInteractive]";
	
	public static void error(String msg) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + PLUGIN_TAG + " " + msg);
	}
	
	public static void warn(String msg) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + PLUGIN_TAG + " " + msg);
	}
	
	public static void nice(String msg) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PLUGIN_TAG + " " + msg);
	}
	
	public static void info(String msg) {
		Bukkit.getConsoleSender().sendMessage(PLUGIN_TAG + " " + msg);
	}

}
