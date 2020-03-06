package com.manelnavola.mcinteractive.core.managers.command.objects;

import java.util.List;

/**
 * Abstract implementation of a command token
 * @author Manel Navola
 *
 */
public abstract class CommandToken {
	
	private CommandToken[] exclusions;
	private String[] defaults = new String[0];
	
	/**
	 * Validates the current command options
	 * @param input The string to validate
	 * @param optionList A list containing command options
	 */
	public abstract void validate(String input, List<String> optionList);
	
	/**
	 * Attempts to process a command
	 * @param input The string to process
	 * @return A non-null string that contains the error in case of command execution failure
	 */
	public abstract String pass(String input);
	
	/**
	 * Clones the current command token
	 * @return A copy of the current command token
	 */
	public abstract CommandToken clone();
	
	/**
	 * Sets default command options manually
	 * @param defaults An array containing command options
	 */
	public void setDefaults(String[] defaults) {
		this.defaults = defaults;
	}
	
	/**
	 * Sets which command tokens to exclude from the parsing
	 * If any excluded command token is valid while parsing this token, this token's validation
	 * will fail
	 * @param exclusions
	 */
	public void setExclusions(CommandToken[] exclusions) {
		this.exclusions = exclusions;
	}
	
	/**
	 * Obtains the default options of this command token
	 * @return An array containing the default options
	 */
	protected String[] getDefaults() {
		return defaults;
	}
	
	/**
	 * Checks whether no excluded command token is successfully passed
	 * @param input The string to parse
	 * @return True if any excluded command is successfully parsed
	 */
	protected boolean commandIsNotA(String input) {
		if (exclusions == null) return false;
		
		for (CommandToken ct : exclusions) {
			if (ct.pass(input) == null) {
				return true;
			}
		}
		return false;
	}

}
