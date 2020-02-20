package com.manelnavola.mcinteractive.core.managers.command;

import com.manelnavola.mcinteractive.core.managers.command.objects.CommandValidator;
import com.manelnavola.mcinteractive.core.wrappers.WPlayer;
import com.manelnavola.mcinteractive.core.wrappers.Wrapper;

public class CommandManager {
	
	private static CommandManager INSTANCE;
	
	private CommandValidator commandValidator;
	
	/**
	 * Gets the singleton object
	 * @return The singleton object
	 */
	public static CommandManager getInstance() {
		if (INSTANCE == null) INSTANCE = new CommandManager();
		return INSTANCE;
	}
	
	/**
	 * Sets the main command validator of the manager
	 * @param commandValidator The command validator to set
	 */
	public void setCommandValidator(CommandValidator commandValidator) {
		this.commandValidator = commandValidator;
	}
	
	/**
	 * Processes a command string with arguments separated by spaces
	 * @param wp The player to process the command on, or null if performed from the console
	 * @param inputText The string command to process
	 */
	public void processPlayerCommand(WPlayer<?> wp, String inputText) {
		if (this.commandValidator == null) return;
		
		String[] argumentList = inputText.split(" ");
		String error = this.commandValidator.run(wp, argumentList);
		if (error != null && !error.isEmpty()) {
			wp.sendMessage(error);
		}
	}
	
	/**
	 * Processes a command string with arguments separated by spaces
	 * @param inputText The string command to process
	 */
	public void processConsoleCommand(String inputText) {
		if (this.commandValidator == null) return;
		
		String[] argumentList = inputText.split(" ");
		String error = this.commandValidator.run(null, argumentList);
		if (error != null && !error.isEmpty()) {
			Wrapper.getInstance().getServer().sendConsoleMessage(error);
		}
	}
	
}
