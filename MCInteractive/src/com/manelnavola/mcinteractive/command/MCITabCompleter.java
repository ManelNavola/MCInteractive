package com.manelnavola.mcinteractive.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

public class MCITabCompleter implements TabCompleter {
	
	private static List<String> actions = new ArrayList<>();
	private static List<String> enableDisable = new ArrayList<>();
	
	public MCITabCompleter(Plugin plugin) {
		// Actions
		List<Command> cmdList = PluginCommandYamlParser.parse(plugin);
		for(int i = 0; i < cmdList.size(); i++){
			String cmdName = cmdList.get(i).getName();
			if (cmdName.startsWith("mci ")) {
				actions.add(cmdName.substring(4));
			}
        }
		
		// Enabledisable
		enableDisable.add("enable"); enableDisable.add("disable");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("mci")) {
			if (args.length < 2) {
				return checkMatch(args[0], actions);
			} else {
				return new ArrayList<>();
			}
		}
		return null;
	}

	public static List<String> getActions() {
		return actions;
	}
	
	private static List<String> checkMatch(String incomplete, List<String> complete) {
		incomplete = incomplete.toLowerCase();
		List<String> available = new ArrayList<>();
		for (int i = 0; i < complete.size(); i++) {
			String tc = complete.get(i);
			if (tc.startsWith(incomplete)) {
				available.add(tc);
			}
		}
		Collections.sort(available);
		return available;
	}
	
}
