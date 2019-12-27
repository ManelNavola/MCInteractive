package com.manelnavola.mcinteractive.command;

import org.bukkit.command.CommandSender;

public abstract class CommandRunnable {
	
	public abstract void run(CommandSender sender, String[] args);
	
}