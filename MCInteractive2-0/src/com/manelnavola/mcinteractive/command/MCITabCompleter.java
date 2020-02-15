package com.manelnavola.mcinteractive.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class MCITabCompleter implements TabCompleter {
	
	private MCICommand mciCommand;
	
	public MCITabCompleter(MCICommand mcic) {
		mciCommand = mcic;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> toReturn = new ArrayList<String>();
		String[] argsList = new String[args.length + 1];
		argsList[0] = label;
		for (int i = 0; i < args.length; i++) {
			argsList[i+1] = args[i];
		}
		mciCommand.getMainValidator().validate(sender, argsList, 0, toReturn);
		return toReturn;
	}
	
}
