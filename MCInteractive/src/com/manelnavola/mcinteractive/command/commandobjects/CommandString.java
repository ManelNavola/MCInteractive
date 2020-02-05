package com.manelnavola.mcinteractive.command.commandobjects;

import java.util.List;

public class CommandString extends CommandObject {
	
	String string;
	
	public CommandString(String str) {
		string = str.toLowerCase();
	}

	public String getString() {
		return string;
	}

	@Override
	public void validate(String input, List<String> list) {
		if (string.startsWith(input.toLowerCase())) {
			list.add(string);
		}
	}
	
	@Override
	public String pass(String input) {
		if (isNotA(input)) return "Invalid argument";
		if (string.equals(input.toLowerCase())) {
			return null;
		} else {
			return "Incorrect command option!";
		}
	}
	
	@Override
	public CommandObject clone() {
		return new CommandString(string);
	}
	
}
