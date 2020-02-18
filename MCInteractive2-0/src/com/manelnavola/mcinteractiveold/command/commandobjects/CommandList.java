package com.manelnavola.mcinteractiveold.command.commandobjects;

import java.util.List;

public class CommandList extends CommandObject {
	
	private CommandObject commandObject;
	private int min, max;
	
	public CommandList(CommandObject co, int min, int max) {
		this.min = min;
		this.max = max;
		commandObject = co;
	}
	
	@Override
	public void validate(String input, List<String> list) {
		return;
	}

	@Override
	public String pass(String input) {
		if (isNotA(input)) return "Invalid argument";
		return "???";
	}
	
	@Override
	public CommandObject clone() {
		return new CommandList(commandObject, min, max);
	}
	
	public CommandObject getCommandObject() {
		return commandObject;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
}
