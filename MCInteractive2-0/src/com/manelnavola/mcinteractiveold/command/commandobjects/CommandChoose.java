package com.manelnavola.mcinteractiveold.command.commandobjects;

import java.util.List;

public class CommandChoose extends CommandObject {
	
	private String[] toChoose = new String[0];
	
	public CommandChoose(String... choose) {
		toChoose = new String[choose.length];
		for (int i = 0; i < choose.length; i++) {
			toChoose[i] = choose[i].toLowerCase();
		}
	}
	
	@Override
	public void validate(String input, List<String> list) {
		input = input.toLowerCase();
		for (String s : toChoose) {
			if (s.startsWith(input))
				list.add(s);
		}
		return;
	}
	
	@Override
	public String pass(String input) {
		if (isNotA(input)) return "Invalid argument";
		input = input.toLowerCase();
		for (String s : toChoose) {
			if (s.equals(input)) {
				return null;
			}
		}
		return "Incorrect argment option!";
	}

	@Override
	public CommandObject clone() {
		return new CommandChoose(toChoose);
	}
	
}
