package com.manelnavola.mcinteractive.command.commandobjects;

import java.util.List;

import com.manelnavola.mcinteractive.generic.ConnectionManager;

public class CommandChannel extends CommandObject {
	
	public CommandChannel() {}
	
	@Override
	public void validate(String input, List<String> list) {
		input = input.toLowerCase();
		for (String s : ConnectionManager.getConnectedChannels()) {
			if (s.startsWith(input))
				list.add(s);
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
