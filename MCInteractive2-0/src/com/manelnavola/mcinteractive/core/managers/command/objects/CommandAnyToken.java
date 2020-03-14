package com.manelnavola.mcinteractive.core.managers.command.objects;

import java.util.List;

public class CommandAnyToken extends CommandToken {
	
	public CommandAnyToken() {}

	@Override
	public void validate(String input, List<String> list) {
		for (String s : getDefaults()) {
			list.add(s);
		}
		return;
	}

	@Override
	public String pass(String input) {
		if (commandIsNotA(input)) return "Invalid argument";
		return null;
	}

	@Override
	public CommandAnyToken clone() {
		return new CommandAnyToken();
	}
	
}
