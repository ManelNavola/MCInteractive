package com.manelnavola.mcinteractiveold.command.commandobjects;

import java.util.List;

import com.manelnavola.mcinteractiveold.generic.ConnectionManager;

public class CommandChannel extends CommandObject {
	
	public CommandChannel() {}
	
	@Override
	public void validate(String input, List<String> list) {
		input = input.toLowerCase();
		for (String s : ConnectionManager.getAnonConnectedChannels()) {
			String ss = s.substring(1);
			if (ss.startsWith(input))
				list.add(ss);
		}
	}
	
	@Override
	public String pass(String input) {
		if (isNotA(input)) return "Invalid argument";
		return null;
	}

	@Override
	public CommandObject clone() {
		return new CommandPlayer();
	}
	
}
