package com.manelnavola.mcinteractive.bukkit;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.manelnavola.mcinteractive.bukkit.wrapper.BukkitPlayer;
import com.manelnavola.mcinteractive.bukkit.wrapper.BukkitServer;
import com.manelnavola.mcinteractive.core.LifeCycle;
import com.manelnavola.mcinteractive.core.managers.CommandManager;
import com.manelnavola.mcinteractive.core.wrappers.Wrapper;

/**
 * Main starting point for Bukkit plugins
 * @author Manel Navola
 *
 */
public class Main extends JavaPlugin {
	
	@Override
	public void onEnable() {
		Wrapper.getInstance().setServer(new BukkitServer(this));
		getCommand("mci").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				String[] tokens = new String[args.length + 1];
				tokens[0] = label;
				for (int i = 0; i < args.length; i++) {
					tokens[i+1] = args[i];
				}
				if (sender instanceof ConsoleCommandSender) {
					return CommandManager.getInstance().processConsoleCommand(tokens);
				} else {
					return CommandManager.getInstance().processPlayerCommand(new BukkitPlayer((Player) sender),
							tokens);
				}
			}
			
		});
		getCommand("mci").setTabCompleter(new TabCompleter() {

			@Override
			public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
				String[] tokens = new String[args.length + 1];
				tokens[0] = alias;
				for (int i = 0; i < args.length; i++) {
					tokens[i+1] = args[i];
				}
				if (sender instanceof ConsoleCommandSender) {
					return CommandManager.getInstance().tabCompleteConsoleCommand(tokens);
				} else {
					return CommandManager.getInstance().tabCompletePlayerCommand(new BukkitPlayer((Player) sender),
							tokens);
				}
			}
			
		});
		LifeCycle.getInstance().enableCommandManager();
		LifeCycle.getInstance().start();
	}
	
	@Override
	public void onDisable() {
		LifeCycle.getInstance().stop();
	}
	
}
