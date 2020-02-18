package com.manelnavola.mcinteractiveold.command.commandobjects;

import java.util.List;

public class CommandAny extends CommandObject {
	
	public CommandAny() {}

	@Override
	public void validate(String input, List<String> list) {
		for (String s : getDefaults()) {
			list.add(s);
		}
		return;
	}

	@Override
	public String pass(String input) {
		if (isNotA(input)) return "Invalid argument";
		return null;
	}

	@Override
	public CommandObject clone() {
		return new CommandAny();
	}
	
}
