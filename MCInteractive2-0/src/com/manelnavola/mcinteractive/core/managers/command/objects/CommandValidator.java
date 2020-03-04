package com.manelnavola.mcinteractive.core.managers.command.objects;

import java.util.List;
import org.bukkit.ChatColor;

import com.manelnavola.mcinteractive.core.managers.CommandManager;
import com.manelnavola.mcinteractive.core.managers.command.CommandRunnable;
import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.mcinteractive.core.wrappers.WPlayer;

/**
 * Class for defining a command verification and execution functionality
 * @author Manel Navola
 *
 */
public class CommandValidator {
	
	private CommandValidator parent;
	private boolean playerOnly;
	private boolean computedCommandInfo;
	private CommandInfo commandInfo;
	private CommandRunnable runnable;
	private CommandToken token;
	private CommandValidator[] validatorList;
	private boolean showInformation;
	
	/**
	 * Command validator constructor
	 * @param token The main command token instance
	 * @param validatorList A list of validators to fork from the original command
	 * @param runnable The runnable to run if the command is successful
	 * @param playerOnly If the command is not available for consoles
	 */
	public CommandValidator(CommandToken token, CommandValidator[] validatorList, 
			CommandRunnable runnable, boolean playerOnly) {
		// Command list treatment
//		if (commandObject instanceof CommandList) {
//			CommandList csl = (CommandList) commandObject;
//			commandObject = csl.getCommandObject().clone();
//			if (csl.getMin() == csl.getMax()) {
//				// Last one
//				commandValidatorList = cvl;
//				commandRunnable = r;
//			} else if (csl.getMin() > 1) {
//				// Invalid
//				commandValidatorList = new CommandValidator[] {
//					new CommandValidator(
//						new CommandList(csl.getCommandObject(), csl.getMin() - 1, csl.getMax() - 1),
//						cvl, r)
//				};
//			} else {
//				// Valid
//				commandValidatorList = addToArray(cvl,
//					new CommandValidator(
//						new CommandList(csl.getCommandObject(), csl.getMin(), csl.getMax() - 1),
//						cvl, r));
//				commandRunnable = r;
//			}
//		} else {
//			commandValidatorList = cvl;
//			commandRunnable = r;
//		}
		
		// Set all class variables
		this.playerOnly = playerOnly;
		this.token = token;
		this.runnable = runnable;
		this.validatorList = validatorList;
		
		// Loop all command validators
		for (CommandValidator cv : validatorList) {
			cv.setParent(this);
		}
	}
	
	/**
	 * Another command validator constructor
	 * @param token The main command token instance
	 * @param validatorList A list of validators to fork from the original command
	 */
	public CommandValidator(CommandToken token, CommandValidator[] commandValidators) {
		this(token, commandValidators, null, false);
	}
	
	/**
	 * Another command validator constructor
	 * @param token The main command token instance
	 * @param runnable The runnable to run if the command is successful
	 */
	public CommandValidator(CommandToken token, CommandRunnable runnable) {
		this(token, new CommandValidator[0], runnable, false);
	}
	
	public CommandInfo getCommandInfo() {
		if (!computedCommandInfo) {
			// Computes the commandInfo for this command, searching for the uppermost parent
			String commandString = null;
			CommandValidator check = this;
			while (check != null) {
				if (check.token instanceof CommandStringToken) {
					CommandStringToken cs = (CommandStringToken) check.token;
					if (commandString == null) {
						commandString = cs.getString();
					} else {
						commandString = cs.getString() + " " + commandString;
					}
				} else {
					commandString = null;
				}
				check = check.parent;
			}
			if (commandString != null) {
				commandInfo = CommandManager.getInstance().getCommandInfo(commandString);
			}
			computedCommandInfo = true;
		}
		return commandInfo;
	}

	/**
	 * Sets the parent of the current validator
	 * @param validator The parent of the current validator to set
	 */
	private void setParent(CommandValidator validator) {
		parent = validator;
	}
	
	/**
	 * Runs the command validator
	 * @param wPlayer The player who executed the command or null if the issuer was a console
	 * @param argumentList The list of command arguments
	 * @return A string denoting the error of the command
	 */
	public String run(WPlayer<?> wPlayer, String[] argumentList) {
		return run(wPlayer, argumentList, 0);
	}
	
	/**
	 * Runs the command validator
	 * @param wPlayer The player who executed the command or null if the issuer was a console
	 * @param argumentList The list of command arguments
	 * @param index The currently index of the iterating method
	 * @return A string denoting the error of the command
	 */
	private String run(WPlayer<?> wPlayer, String[] argumentList, int index) {
		// Check if the command cannot be run on the console
		if (wPlayer == null && playerOnly) {
			return "Only players can execute this command!";
		}
		
		// Check permissions
		if (wPlayer != null && !wPlayer.checkPermission(getCommandInfo().getPermission())) {
			return "You do not have the required permission!";
		}
		
		if (argumentList.length == index + 1) {
			// Check pass
			String passError = token.pass(argumentList[index]);
			if (passError == null) {
				if (runnable != null) {
					runnable.run(wPlayer, argumentList);
					return null;
				} else {
					boolean incorrectUsage = false;
					String builtParentCommand = null;
					CommandValidator lastValidator = null;
					CommandValidator check = this;
					while (check != null) {
						if (check.token instanceof CommandStringToken) {
							CommandStringToken cs = (CommandStringToken) check.token;
							if (builtParentCommand == null) {
								lastValidator = check;
								builtParentCommand = cs.getString();
							} else {
								builtParentCommand = cs.getString() + " " + builtParentCommand;
							}
						} else {
							incorrectUsage = true;
							builtParentCommand = null;
						}
						check = check.parent;
					}
					
					if (builtParentCommand == null) {
						return "Too few arguments provided!";
					} else {
						if (incorrectUsage || !showInformation) {
							// Show command type
							ChatUtils.sendError(wPlayer, "Incorrect command usage!");
							String usage = CommandManager.getInstance().getCommandInfo(builtParentCommand).getUsage();
							if (usage != null) {
								ChatUtils.sendInfo(wPlayer, usage);
							}
							return "";
						} else {
							// Info
							ChatUtils.sendRaw(wPlayer, ChatColor.GREEN + "-- " + builtParentCommand + " --");
							for (CommandValidator cv : lastValidator.validatorList) {
								if (cv.token instanceof CommandStringToken) {
									CommandStringToken cs = (CommandStringToken) cv.token;
									String newBuiltCommand = builtParentCommand + " " + cs.getString();
									CommandInfo cmd = CommandManager.getInstance().getCommandInfo(newBuiltCommand);
									if (cmd != null && (wPlayer == null || wPlayer.checkPermission(cmd.getPermission()))) {
										String usage = cmd.getUsage();
										String description = cmd.getDescription();
										ChatUtils.sendRaw(wPlayer,
												ChatColor.AQUA + usage + ": " + ChatColor.GRAY + description);
									}
								}
							}
							return "";
						}
					}
				}
			} else {
				return passError;
			}
		} else {
			String passError = token.pass(argumentList[index]);
			if (passError == null) {
				if (validatorList.length == 0) {
					return "Too many arguments!";
				} else if (validatorList.length == 1) {
					CommandValidator cv = validatorList[0];
					passError = cv.token.pass(argumentList[index + 1]);
					if (passError == null) {
						return cv.run(wPlayer, argumentList, index + 1);
					} else {
						return passError;
					}
				} else {
					for (CommandValidator cv : validatorList) {
						if (cv.token.pass(argumentList[index + 1]) == null) {
							return cv.run(wPlayer, argumentList, index + 1);
						}
					}
				}
				return "Invalid command option!";
			} else {
				ChatUtils.sendError(wPlayer, "Invalid argument!");
				return passError;
			}
		}
	}
	
	/**
	 * Validates the current command
	 * @param wPlayer The player who executed the command or null if the issuer was a console
	 * @param argumentList The list of command arguments
	 * @param index The currently index of the iterating method
	 * @param optionList The list of options
	 */
	private void validate(WPlayer<?> wPlayer, String[] argumentList, int index, List<String> optionList) {
		if (wPlayer == null && playerOnly) {
			return;
		}
		if (getCommandInfo() != null) {
			if (wPlayer != null && wPlayer.checkPermission(getCommandInfo().getPermission())) {
				return;
			}
		}
		if (argumentList.length == index + 1) {
			token.validate(argumentList[index], optionList);
		} else {
			if (token.pass(argumentList[index]) == null) {
				for (CommandValidator cv : validatorList) {
					cv.validate(wPlayer, argumentList, index + 1, optionList);
				}
			}
		}
	}
	
	/**
	 * Validates the current command
	 * @param wPlayer The player who executed the command or null if the issuer was a console
	 * @param argumentList The list of command arguments
	 * @param optionList The list of options
	 */
	public void validate(WPlayer<?> wPlayer, String[] argumentList, List<String> optionList) {
		validate(wPlayer, argumentList, 0, optionList);
	}
	
	/**
	 * Sets whether the command should show information on failure
	 * @param showInformation True if the command should show command info on failure
	 */
	public void setShowInformation(boolean showInformation) {
		this.showInformation = showInformation;
	}
	
}
