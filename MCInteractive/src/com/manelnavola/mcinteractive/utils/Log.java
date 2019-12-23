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
	
	public static void error(Object msg) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + PLUGIN_TAG + " " + msg.toString());
	}
	
	public static void warn(Object msg) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + PLUGIN_TAG + " " + msg.toString());
	}
	
	public static void nice(Object msg) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PLUGIN_TAG + " " + msg.toString());
	}
	
	public static void info(Object msg) {
		Bukkit.getConsoleSender().sendMessage(PLUGIN_TAG + " " + msg.toString());
	}

}
