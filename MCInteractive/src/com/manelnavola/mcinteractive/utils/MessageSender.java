package com.manelnavola.mcinteractive.utils;

import org.bukkit.command.CommandSender;
import net.md_5.bungee.api.ChatColor;

public class MessageSender {
	
	private static final String PLUGIN_TAG = "[MCI]";
	
	public static void error(CommandSender sender, String msg) {
		sender.sendMessage(ChatColor.RED + PLUGIN_TAG + " " + msg);
	}
	
	public static void warn(CommandSender sender, String msg) {
		sender.sendMessage(ChatColor.YELLOW + PLUGIN_TAG + " " + msg);
	}
	
	public static void nice(CommandSender sender, String msg) {
		sender.sendMessage(ChatColor.GREEN + PLUGIN_TAG + " " + msg);
	}
	
	public static void info(CommandSender sender, String msg) {
		sender.sendMessage(ChatColor.GOLD + PLUGIN_TAG + " " + msg);
	}
	
	public static void send(CommandSender sender, String msg) {
		sender.sendMessage(msg);
	}
	
}
