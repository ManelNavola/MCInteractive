package com.manelnavola.mcinteractive.core.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.manelnavola.mcinteractive.core.managers.command.CommandRunnable;
import com.manelnavola.mcinteractive.core.managers.command.objects.*;
import com.manelnavola.mcinteractive.core.wrappers.WPlayer;

public class CommandManager extends Manager {
	
	private static CommandManager INSTANCE;
	private CommandManager() {}
	
	private boolean enabled;
	private CommandValidator commandValidator;
	private Map<String, CommandInfo> commandStringToDescription;
	
	/**
	 * Gets the singleton object
	 * @return The singleton object
	 */
	public static CommandManager getInstance() {
		if (INSTANCE == null) INSTANCE = new CommandManager();
		return INSTANCE;
	}
	
	@Override
	public void start() {
		commandStringToDescription = new HashMap<>();
		
		commandStringToDescription.put("mci reload", new CommandInfo("/mci reload",
				"Reloads the MC Interactive plugin and attempts reconnecting to Twitch servers",
				"mci.reload"));
		CommandValidator mciReload = new CommandValidator(
				new CommandStringToken("reload"),
				new CommandRunnable() {
					@Override
					public void run(WPlayer<?> wp, String[] args) {
						ActionManager.getInstance().reloadBot();
					}
				});
		
		commandStringToDescription.put("mci", new CommandInfo("/mci",
				"Reloads the MC Interactive plugin and attempts reconnecting to Twitch servers",
				"mci.info"));
		commandValidator = new CommandValidator(
				new CommandStringToken("mci"),
				new CommandValidator[] {
					mciReload
				});
		commandValidator.setShowInformation(true);
		
		enabled = true;
	}
	
	/**
	 * Initializes the command manager, inserting the reload command only
	 */
	public void startReloadOnly() {
		commandStringToDescription = new HashMap<>();
		
		commandStringToDescription.put("mci reload", new CommandInfo("/mci reload",
				"Reloads the MC Interactive plugin and attempts reconnecting to Twitch servers",
				"mci.reload"));
		CommandValidator mciReload = new CommandValidator(
				new CommandStringToken("reload"),
				new CommandRunnable() {
					@Override
					public void run(WPlayer<?> wp, String[] args) {
						ActionManager.getInstance().reloadBot();
					}
				});
		
		commandStringToDescription.put("mci", new CommandInfo("/mci",
				"Reloads the MC Interactive plugin and attempts reconnecting to Twitch servers",
				"mci.info"));
		commandValidator = new CommandValidator(
				new CommandStringToken("mci"),
				new CommandValidator[] {
					mciReload
				});
		commandValidator.setShowInformation(true);
		
		enabled = true;
	}
	
	@Override
	public void stop() {
		commandValidator = null;
		commandStringToDescription = null;
		enabled = false;
		INSTANCE = null;
	}
	
	/**
	 * Sets the main command validator of the manager
	 * @param commandValidator The command validator to set
	 */
	public void setCommandValidator(CommandValidator commandValidator) {
		this.commandValidator = commandValidator;
	}
	
	/**
	 * Processes a command string with arguments as a string array
	 * @param wp The player to process the command on, or null if performed from the console
	 * @param inputText The string array to process
	 */
	private void processCommand(WPlayer<?> wp, String[] tokens) {
		for (String s : tokens) {
			s = s.toLowerCase();
		}
		String error = this.commandValidator.run(wp, tokens);
		if (error != null && !error.isEmpty() && wp != null) {
			wp.sendMessage(error);
		}
	}
	
	/**
	 * Attempts to tab complete a command string with arguments as a string array
	 * @param wp The player to process the command on, or null if performed from the console
	 * @param inputText The string array to process
	 */
	private List<String> tabCompleteCommand(WPlayer<?> wp, String[] tokens) {
		for (String s : tokens) {
			s = s.toLowerCase();
		}
		List<String> validOptions = new ArrayList<>();
		this.commandValidator.validate(wp, tokens, validOptions);
		return validOptions;
	}
	
	/**
	 * Processes a command string with arguments separated by spaces
	 * @param wp The player to process the command on
	 * @param inputText The string array to process
	 */
	public boolean processPlayerCommand(WPlayer<?> wp, String[] tokens) {
		if (!enabled) return false;
		
		CommandManager.getInstance().processCommand(wp, tokens);
		return true;
	}
	
	/**
	 * Processes a command string with arguments separated by spaces
	 * @param inputText The string array to process
	 */
	public boolean processConsoleCommand(String[] tokens) {
		if (!enabled) return false;
		
		CommandManager.getInstance().processCommand(null, tokens);
		return true;
	}
	
	/**
	 * Attempts to tab complete a command string with arguments as a string array
	 * @param wp The player to tab complete the command on
	 * @param inputText The string array to tab complete
	 */
	public List<String> tabCompletePlayerCommand(WPlayer<?> wp, String[] tokens) {
		if (!enabled) return null;
		
		return CommandManager.getInstance().tabCompleteCommand(wp, tokens);
	}
	
	/**
	 * Attempts to tab complete a command string with arguments separated by spaces
	 * @param inputText The string array to tab complete
	 */
	public List<String> tabCompleteConsoleCommand(String[] tokens) {
		if (!enabled) return null;
		
		return CommandManager.getInstance().tabCompleteCommand(null, tokens);
	}
	
	/**
	 * Gets a CommandInfo object of a command
	 * @param commandString The command to get the CommandInfo object from
	 * @return A CommandInfo object
	 */
	public CommandInfo getCommandInfo(String commandString) {
		return commandStringToDescription.get(commandString);
	}
	
}
