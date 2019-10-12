package com.manelnavola.mcinteractive.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.manelnavola.mcinteractive.chat.VoteManager;
import com.manelnavola.mcinteractive.generic.ConfigGUI;
import com.manelnavola.mcinteractive.generic.ConnectionManager;
import com.manelnavola.mcinteractive.utils.MessageSender;

import net.md_5.bungee.api.ChatColor;

public class MCICommand implements CommandExecutor {
	
	private List<String> actions;
	
	public MCICommand() {
		actions = MCITabCompleter.getActions();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Check permissions
		if (sender instanceof Player) {
			if (!sender.isOp()) {
				MessageSender.error(sender, "Only operators can execute this command!");
				return true;
			}
		}
		
		args = CommandParser.parseCommas(args, sender);
		if (args == null) {
			MessageSender.warn(sender, "Invalid command parsing!");
			MessageSender.warn(sender, "Ensure sentences with \"\" are correctly wrapped!");
		}
		
		if (args.length == 0) {
			MessageSender.send(sender, ChatColor.YELLOW + "--------- " + ChatColor.WHITE + "MCInteractive Help " + ChatColor.YELLOW + "------------------");
			MessageSender.send(sender, ChatColor.GRAY + "Type /mci [option] to get help about a command");
			for (int i = 0; i < actions.size(); i++) {
				MessageSender.send(sender, ChatColor.GOLD + "/mci " + actions.get(i) + ": "
						+ ChatColor.RESET + Bukkit.getPluginCommand("mci " + actions.get(i)).getDescription());
			}
			return true;
		}
		
		// Check command
		switch (args[0].toLowerCase()) {
		case "listen":
			return listen(sender, args);
		case "leave":
			return leave(sender, args);
		case "startvote":
			return startVote(sender, args);
		case "endvote":
			return endVote(sender, args);
		case "cancelvote":
			return cancelVote(sender, args);
		case "config":
			return config(sender, args);
		}
		
		MessageSender.error(sender, "Only operators can execute this command!");

		return false;
	}
	
	private boolean config(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			MessageSender.error(sender, "This command cannot be run from the console!");
			return true;
		}
		
		if (args.length == 1) {
			// One arg
			ConfigGUI.open((Player) sender);
			return true;
		} else {
			MessageSender.error(sender, "This command takes no arguments!");
			return true;
		}
	}

	private boolean endVote(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			MessageSender.error(sender, "This command cannot be run from the console!");
			return true;
		}
		
		if (args.length > 1) {
			MessageSender.error(sender, "This command takes no arguments!");
			return true;
		}
		VoteManager.endVote((Player) sender);
		return true;
	}

	private boolean cancelVote(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			MessageSender.error(sender, "This command cannot be run from the console!");
			return true;
		}
		
		if (args.length > 1) {
			MessageSender.error(sender, "This command takes no arguments!");
			return true;
		}
		VoteManager.cancelVote((Player) sender);
		return true;
	}
	
	private boolean startVote(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			MessageSender.error(sender, "This command cannot be run from the console!");
			return true;
		}
		
		Player p = (Player) sender;
		
		if (!ConnectionManager.isConnected(p)) {
			MessageSender.error(sender, "You must be connected to a Twitch channel first!");
			MessageSender.error(sender, CommandParser.getCommandUsage("mci listen"));
			return true;
		}
		
		if (VoteManager.isActive(p)) {
			MessageSender.error(sender, "A vote is already operative!");
			return true;
		}
		
		if (args.length < 5) {
			MessageSender.error(sender, "A vote needs a description, a duration and at least two options!");
			MessageSender.error(sender, CommandParser.getCommandUsage("mci startvote"));
			return true;
		}
		
		if (args.length > 9) {
			MessageSender.error(sender, "Too many options! The currently allowed maximum is 6");
			return true;
		}
		
		float duration;
		try {
			duration = Float.parseFloat(args[2]);
		} catch (NumberFormatException nfe) {
			MessageSender.error(sender, "Duration must be a number!");
			// Quick check if there's a number later
			for (int i = 3; i < args.length; i++) {
				try {
					Float.parseFloat(args[i]);
					MessageSender.warn(sender, "Are you wrapping sentences with \"\" ?");
					break;
				} catch (NumberFormatException nfe1) {
				}
			}
			return true;
		}

		if (duration < 0) {
			MessageSender.error(sender, "Duration must be higher than 0!");
			return true;
		}

		// Fill options array
		List<String> options = new ArrayList<>();
		for (int i = 3; i < args.length; i++) {
			options.add(args[i].toLowerCase());
		}
		VoteManager.createPlayerVote(p, args[1], duration, options);
		return true;
	}
	
	private boolean listen(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			MessageSender.error(sender, "This command cannot be run from the console!");
			return true;
		}
		
		if (args.length > 1) {
			ConnectionManager.listen((Player) sender, "#" + args[1].toLowerCase());
			return true;
		} else {
			MessageSender.error(sender, "You must enter the channel name to connect to!");
			MessageSender.error(sender, CommandParser.getCommandUsage("mci listen"));
			return true;
		}
	}
	
	private boolean leave(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			MessageSender.error(sender, "This command cannot be run from the console!");
			return true;
		}
		
		if (args.length > 1) {
			MessageSender.error(sender, "This command takes no arguments!");
			return true;
		}
		
		if (!ConnectionManager.isConnected((Player) sender)) {
			MessageSender.warn(sender, "You are not connected to any channel!");
			return true;
		}
		
		ConnectionManager.leave((Player) sender);
		return true;
	}
	
}
