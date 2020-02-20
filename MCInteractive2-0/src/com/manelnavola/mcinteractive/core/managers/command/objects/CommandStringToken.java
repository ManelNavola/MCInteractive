package com.manelnavola.mcinteractive.core.managers.command.objects;

import java.util.List;

/**
 * Class to represent a string token in a command
 * @author Manel Navola
 *
 */
public class CommandStringToken extends CommandToken {
	
	private String string;
	
	/**
	 * The constructor for a string token
	 * @param string The string to set
	 */
	public CommandStringToken(String string) {
		this.string = string.toLowerCase();
	}
	
	/**
	 * Get the string of the token
	 * @return The token's string
	 */
	public String getString() {
		return string;
	}

	@Override
	public void validate(String input, List<String> optionList) {
		if (string.startsWith(input.toLowerCase())) {
			optionList.add(string);
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
	public CommandToken clone() {
		return new CommandStringToken(string);
	}

}
