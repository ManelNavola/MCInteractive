package com.manelnavola.mcinteractive.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CommandParser {
	
	public static String[] parseCommas(String[] args, CommandSender sender) {
		List<String> argList = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			if (s.startsWith("\"")) {
				boolean firstPass = true;
				s = s.substring(1);
				i++;
				if (s.endsWith("\"")) {
					s = s.substring(0, s.length()-1);
					i--;
				} else {
					while (i <= args.length) {
						if (i == args.length) {
							return null;
						}
						String a = args[i];
						if (a.endsWith("\"")) {
							a = a.substring(0, a.length()-1);
							if (!firstPass) { s += " " + a; }
							break;
						} else {
							s += " " + a;
						}
						i++;
						firstPass = false;
					}
				}
			}
			argList.add(s);
		}
		
		String[] list = new String[argList.size()];
		for (int i = 0; i < argList.size(); i++) {
			list[i] = argList.get(i);
		}
		return list;
	}
	
	public static String getCommandUsage(String msg) {
		return Bukkit.getPluginCommand(msg).getUsage().replaceFirst("<command>", msg);
	}
	
}
