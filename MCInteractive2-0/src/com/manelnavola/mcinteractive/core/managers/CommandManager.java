package com.manelnavola.mcinteractive.core.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.manelnavola.mcinteractive.core.managers.command.CommandRunnable;
import com.manelnavola.mcinteractive.core.managers.command.objects.*;
import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.mcinteractive.core.wrappers.WPlayer;

public class CommandManager extends Manager {
	
	private static CommandManager INSTANCE;
	private CommandManager() {}
	
	private boolean enabled;
	private CommandValidator commandValidator;
	private Map<String, CommandValidatorInfo> commandStringToDescription;
	
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
		
		// MCI STREAM
		commandStringToDescription.put("mci stream register",
				new CommandValidatorInfo("/mci stream register", "Registers a Twitch channel on your player",
						"mci.stream.register"));
		CommandValidator mciStreamRegister = new CommandValidator(new CommandStringToken("register"),
				new CommandValidator[] { new CommandValidator(new CommandChannelToken(), new CommandRunnable() {
					@Override
					public void run(WPlayer<?> wp, String[] args) {
						if (StreamManager.isRegistered(wp)) {
							
						} else {
							
						}
					}
				}) });
		
		commandStringToDescription.put("mci stream join",
				new CommandValidatorInfo("/mci stream join", "Joins a running stream", "mci.stream.join"));
		CommandValidator mciStreamJoin = new CommandValidator(new CommandStringToken("join"),
				new CommandValidator[] { new CommandValidator(new CommandChannelToken(), new CommandRunnable() {
					@Override
					public void run(WPlayer<?> wp, String[] args) {
						String channelName = args[3].toLowerCase();
						if (ConfigurationManager.ConfigField.STREAM_FREEJOIN.getConfigValue(Boolean.class)) {
							// Free join is enabled, join channel
							StreamManager.getInstance().joinChannel(wp, channelName);
							ChatUtils.sendSuccess(wp, "Joined " + channelName + "!");
						} else {
							// Join channel only if the channel is available
							if (StreamManager.getInstance().streamExists(channelName)) {
								StreamManager.getInstance().joinChannel(wp, channelName);
								ChatUtils.sendSuccess(wp, "Joined " + channelName + "!");
							} else {
								ChatUtils.sendError(wp, "The channel is not currently streaming!");
							}
						}
					}
				}) });
		
		commandStringToDescription.put("mci stream leave",
				new CommandValidatorInfo("/mci stream leave", "Leaves a stream", "mci.stream.leaveº"));
		CommandValidator mciStreamLeave = new CommandValidator(new CommandStringToken("leave"),
				new CommandValidator[] { new CommandValidator(new CommandChannelToken(), new CommandRunnable() {
					@Override
					public void run(WPlayer<?> wp, String[] args) {
						if (StreamManager.getInstance().isPlayerConnected(wp)) {
							ChatUtils.sendSuccess(wp, "Left the stream successfully.");
						} else {
							ChatUtils.sendWarn(wp, "You haven't joined a stream yet!");
						}
					}
				}) });
		
		commandStringToDescription.put("mci stream", new CommandValidatorInfo("/mci stream",
				"Command for managing stream management",
				"mci.stream"));
		CommandValidator mciStream = new CommandValidator(
				new CommandStringToken("stream"),
				new CommandValidator[] {
						mciStreamJoin,
						mciStreamLeave
				});
		
		// MCI RELOAD
		commandStringToDescription.put("mci reload", new CommandValidatorInfo("/mci reload",
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
		
		// MCI
		commandStringToDescription.put("mci", new CommandValidatorInfo("/mci",
				"Reloads the MC Interactive plugin and attempts reconnecting to Twitch servers",
				"mci.info"));
		commandValidator = new CommandValidator(
				new CommandStringToken("mci"),
				new CommandValidator[] {
					mciReload,
					mciStream
				});
		commandValidator.setShowInformation(true);
		
		enabled = true;
	}
	
	/**
	 * Initializes the command manager, inserting the reload command only
	 */
	public void startReloadOnly() {
		commandStringToDescription = new HashMap<>();
		
		commandStringToDescription.put("mci reload", new CommandValidatorInfo("/mci reload",
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
		
		commandStringToDescription.put("mci", new CommandValidatorInfo("/mci",
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
	public CommandValidatorInfo getCommandInfo(String commandString) {
		return commandStringToDescription.get(commandString);
	}
	
}
