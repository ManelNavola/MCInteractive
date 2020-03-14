package com.manelnavola.mcinteractive.core.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.manelnavola.mcinteractive.core.managers.command.CommandRunnable;
import com.manelnavola.mcinteractive.core.managers.command.objects.*;
import com.manelnavola.mcinteractive.core.wrappers.WPlayer;

public class CommandManager extends Manager {
	
	private static CommandManager INSTANCE;
	private CommandManager() {}
	
	private CommandValidator commandValidator;
	private ConcurrentHashMap<String, CommandValidatorInfo> commandStringToDescription;
	
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
		commandStringToDescription = new ConcurrentHashMap<>();
		
		// MCI STREAM
		// stream register
		commandStringToDescription.put("mci stream register",
				new CommandValidatorInfo("/mci stream register [channelName]",
						"Registers a Twitch channel on your player", "mci.stream.verified"));
		CommandValidator mciStreamRegister = new CommandValidator(new CommandStringToken("register"),
				new CommandValidator[] { new CommandValidator(new CommandAnyToken(), new CommandRunnable() {
					@Override
					public void run(WPlayer<?> wp, String[] args) {
						StreamManager.getInstance().cmdRegister(wp, args);
					}
				}) });
		// stream unregister
		commandStringToDescription.put("mci stream unregister", new CommandValidatorInfo("/mci stream unregister",
				"Unregisters a Twitch channel on your player", "mci.stream.verified"));
		CommandValidator mciStreamUnregister = new CommandValidator(new CommandStringToken("unregister"),
				new CommandRunnable() {
					@Override
					public void run(WPlayer<?> wp, String[] args) {
						StreamManager.getInstance().cmdUnregister(wp, args);
					}
				});
		// stream join
		commandStringToDescription.put("mci stream join",
				new CommandValidatorInfo("/mci stream join", "Joins a running stream", "mci.stream.issue"));
		CommandValidator mciStreamJoin = new CommandValidator(new CommandStringToken("join"),
				new CommandValidator[] { new CommandValidator(new CommandChannelToken(), new CommandRunnable() {
					@Override
					public void run(WPlayer<?> wp, String[] args) {
						StreamManager.getInstance().cmdJoin(wp, args);
					}
				}) });
		// stream leave
		commandStringToDescription.put("mci stream leave",
				new CommandValidatorInfo("/mci stream leave", "Leaves a stream", "mci.stream.issue"));
		CommandValidator mciStreamLeave = new CommandValidator(new CommandStringToken("leave"),
				new CommandValidator[] { new CommandValidator(new CommandChannelToken(), new CommandRunnable() {
					@Override
					public void run(WPlayer<?> wp, String[] args) {
						StreamManager.getInstance().cmdLeave(wp, args);
					}
				}) });
		// stream start
		commandStringToDescription.put("mci stream start", new CommandValidatorInfo("/mci stream start",
				"Broadcasts streaming message and allows other players to join you", "mci.stream.verified"));
		CommandValidator mciStreamStart = new CommandValidator(new CommandStringToken("start"), new CommandRunnable() {
			@Override
			public void run(WPlayer<?> wp, String[] args) {
				StreamManager.getInstance().cmdStart(wp, args);
			}
		});
		// stream end
		commandStringToDescription.put("mci stream end",
				new CommandValidatorInfo("/mci stream end", "Ends a stream", "mci.stream.verified"));
		CommandValidator mciStreamEnd = new CommandValidator(new CommandStringToken("end"), new CommandRunnable() {
			@Override
			public void run(WPlayer<?> wp, String[] args) {
				StreamManager.getInstance().cmdEnd(wp, args);
			}
		});
		// mci stream
		commandStringToDescription.put("mci stream", new CommandValidatorInfo("/mci stream",
				"Command for managing stream management",
				"mci.stream"));
		CommandValidator mciStream = new CommandValidator(
				new CommandStringToken("stream"),
				new CommandValidator[] {
						mciStreamJoin,
						mciStreamLeave,
						mciStreamRegister,
						mciStreamUnregister,
						mciStreamStart,
						mciStreamEnd
				}, true);
		mciStream.setShowInformation(true);
		
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
		
		setEnabled(true);
	}
	
	/**
	 * Initializes the command manager, inserting the reload command only
	 */
	public void startReloadOnly() {
		commandStringToDescription = new ConcurrentHashMap<>();
		
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
		
		setEnabled(true);
	}
	
	@Override
	public void stop() {
		setEnabled(false);
		commandValidator = null;
		commandStringToDescription = null;
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
		requireEnabled();
		
		CommandManager.getInstance().processCommand(wp, tokens);
		return true;
	}
	
	/**
	 * Processes a command string with arguments separated by spaces
	 * @param inputText The string array to process
	 */
	public boolean processConsoleCommand(String[] tokens) {
		requireEnabled();
		
		CommandManager.getInstance().processCommand(null, tokens);
		return true;
	}
	
	/**
	 * Attempts to tab complete a command string with arguments as a string array
	 * @param wp The player to tab complete the command on
	 * @param inputText The string array to tab complete
	 */
	public List<String> tabCompletePlayerCommand(WPlayer<?> wp, String[] tokens) {
		requireEnabled();
		
		return CommandManager.getInstance().tabCompleteCommand(wp, tokens);
	}
	
	/**
	 * Attempts to tab complete a command string with arguments separated by spaces
	 * @param inputText The string array to tab complete
	 */
	public List<String> tabCompleteConsoleCommand(String[] tokens) {
		requireEnabled();
		
		return CommandManager.getInstance().tabCompleteCommand(null, tokens);
	}
	
	/**
	 * Gets a CommandInfo object of a command
	 * @param commandString The command to get the CommandInfo object from
	 * @return A CommandInfo object
	 */
	public CommandValidatorInfo getCommandInfo(String commandString) {
		requireEnabled();
		
		return commandStringToDescription.get(commandString);
	}
	
}
