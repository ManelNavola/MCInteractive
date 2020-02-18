package com.manelnavola.mcinteractiveold.command.commandobjects;

import java.util.List;

public abstract class CommandObject {
	
	private CommandObject[] notA;
	private String[] defaults = new String[0];
	
	public abstract void validate(String input, List<String> list);
	public abstract String pass(String input);
	public abstract CommandObject clone();
	
	public void setDefaults(String[] defs) {
		defaults = defs;
	}
	
	public void setNotA(CommandObject[] co) {
		notA = co;
	}
	
	protected String[] getDefaults() {
		return defaults;
	}
	
	protected boolean isNotA(String input) {
		if (notA == null) return false;
		for (CommandObject co : notA) {
			if (co.pass(input) == null) {
				return true;
			}
		}
		return false;
	}

}
