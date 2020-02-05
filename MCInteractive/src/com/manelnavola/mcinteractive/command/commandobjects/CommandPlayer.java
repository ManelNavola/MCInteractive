package com.manelnavola.mcinteractive.command.commandobjects;

import java.util.List;

import com.manelnavola.mcinteractive.command.CommandValidator;

public class CommandPlayer extends CommandObject {
	
	public CommandPlayer() {}
	
	@Override
	public void validate(String input, List<String> list) {
		input = input.toLowerCase();
		for (String s : CommandValidator.getPlayerList()) {
			if (s.startsWith(input))
				list.add(s);
		}
		return;
	}
	
	@Override
	public String pass(String input) {
		if (isNotA(input)) return "Invalid argument";
		if (CommandValidator.getPlayerList().contains(input.toLowerCase())) {
			return null;
		} else {
			return "Player is not online!";
		}
	}

	@Override
	public CommandObject clone() {
		return new CommandPlayer();
	}
	
}
