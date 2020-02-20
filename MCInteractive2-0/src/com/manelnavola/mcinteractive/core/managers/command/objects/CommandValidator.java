package com.manelnavola.mcinteractive.core.managers.command.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
	private String permission;
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
		
		// Computes the permission for this command, searching for the uppermost parent
		permission = null;
		CommandValidator check = this;
		while (check != null) {
			if (check.token instanceof CommandStringToken) {
				CommandStringToken cs = (CommandStringToken) check.token;
				if (permission == null) {
					permission = cs.getString();
				} else {
					permission = cs.getString() + "." + permission;
				}
			} else {
				permission = null;
			}
			check = check.parent;
		}
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
		if (wPlayer != null && !wPlayer.checkPermission(permission)) {
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
							String usage = commandByName.get(builtParentCommand).getUsage();
							if (usage != null) {
								ChatUtils.sendInfo(wPlayer, usage);
							}
							return "";
						} else {
							// Info
							ChatUtils.sendInfo(wPlayer, ChatColor.GREEN + "-- " + builtParentCommand + " --");
							for (CommandValidator cv : lastValidator.validatorList) {
								if (cv.token instanceof CommandStringToken) {
									CommandStringToken cs = (CommandStringToken) cv.token;
									String newBuiltCommand = builtParentCommand + " " + cs.getString();
									Command cmd = commandByName.get(newBuiltCommand);
									if (cmd != null && (wPlayer == null || wPlayer.checkPermission(permission))) {
										String usage = cmd.getUsage();
										String description = cmd.getDescription();
										ChatUtils.sendInfo(wPlayer,
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
	public void validate(WPlayer<?> wPlayer, String[] argumentList, int index, List<String> optionList) {
		if (wPlayer == null && playerOnly) {
			return;
		}
		String longestCommandPath = getLongestPath();
		if (longestCommandPath != null) {
			Command cmd = commandByName.get(longestCommandPath);
			if (cmd == null) {
				// TODO check error?
				return;
			}
			if (wPlayer != null && wPlayer.checkPermission(cmd.getPermission())) {
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
	
//	private static Map<String, Command> commandByName;
//	private static List<String> playerList;
//	
//	private CommandValidator parent;
//	private CommandObject commandObject;
//	private CommandValidator[] commandValidatorList;
//	private CommandRunnable commandRunnable;
//	private boolean onlyPlayer;
//	private boolean showInfo = false;
//	
//	public boolean isShowInfo() {
//		return showInfo;
//	}
//
//	public void setShowInfo(boolean showInfo) {
//		this.showInfo = showInfo;
//	}
//
//	public static void init(Plugin plugin) {
//		playerList = new ArrayList<String>();
//		commandByName = new HashMap<String, Command>();
//		List<Command> cmdList = PluginCommandYamlParser.parse(plugin);
//		for(int i = 0; i < cmdList.size(); i++) {
//			Command cmd = cmdList.get(i);
//			commandByName.put(cmd.getName(), cmd);
//        }
//	}
//	
//	public static void addPlayer(Player p) {
//		playerList.add(p.getName().toLowerCase());
//	}
//
//	public static void removePlayer(Player p) {
//		playerList.remove(p.getName().toLowerCase());
//	}
//
//	public static void dispose() {
//		playerList.clear();
//		commandByName.clear();
//	}
//	
//	private CommandValidator[] addToArray(CommandValidator[] cvl, CommandValidator cv) {
//		CommandValidator[] tr = new CommandValidator[cvl.length + 1];
//		for (int i = 0; i < cvl.length; i++) {
//			tr[i] = cvl[i];
//		}
//		tr[tr.length - 1] = cv;
//		return tr;
//	}
//	
//	public CommandValidator(CommandObject co, CommandValidator[] cvl, CommandRunnable r, boolean p) {
//		commandObject = co;
//		
//		// Command list treatment
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
//		
//		for (CommandValidator cv : commandValidatorList) {
//			cv.setParent(this);
//		}
//		onlyPlayer = p;
//	}
//
//	public CommandValidator(CommandObject co, CommandRunnable r) {
//		this(co, new CommandValidator[] { }, r, false);
//	}
//	
//	public CommandValidator(CommandObject co, CommandValidator[] cvl) {
//		this(co, cvl, null, false);
//	}
//	
//	public CommandValidator(CommandObject co, CommandRunnable r, boolean p) {
//		this(co, new CommandValidator[] { }, r, p);
//	}
//	
//	public CommandValidator(CommandObject co, CommandValidator[] cvl, boolean p) {
//		this(co, cvl, null, p);
//	}
//	
//	public CommandValidator(CommandObject co, CommandValidator cv, CommandRunnable r) {
//		this(co, new CommandValidator[] { cv }, r, false);
//	}
//	
//	public CommandValidator(CommandObject co, CommandValidator[] cvl, CommandRunnable r) {
//		this(co, cvl, r, false);
//	}
//	
//	public CommandValidator(CommandObject co, CommandValidator cv, CommandRunnable r, boolean p) {
//		this(co, new CommandValidator[] { cv }, r, p);
//	}
//
//	public CommandValidator(CommandObject co, CommandValidator cv) {
//		this(co, cv, null, false);
//	}
//
//	public CommandValidator(CommandObject co, CommandValidator cv, boolean b) {
//		this(co, cv, null, b);
//	}
//
//	public CommandObject getCommandObject() {
//		return commandObject;
//	}
//	
//	protected boolean checkPermission(CommandSender sender, String perm) {
//		if (sender instanceof ConsoleCommandSender) {
//			return true;
//		} else {
//			return ((Player) sender).hasPermission(perm);
//		}
//	}
//	
//	private String getLongestPath() {
//		String builtParentCommand = null;
//		CommandValidator check = this;
//		while (check != null) {
//			if (check.commandObject instanceof CommandString) {
//				CommandString cs = (CommandString) check.commandObject;
//				if (builtParentCommand == null) {
//					builtParentCommand = cs.getString();
//				} else {
//					builtParentCommand = cs.getString() + " " + builtParentCommand;
//				}
//			} else {
//				builtParentCommand = null;
//			}
//			check = check.parent;
//		}
//		return builtParentCommand;
//	}
//	
//	public String run(CommandSender sender, String[] args, int i) {
//		if (!(sender instanceof Player) && onlyPlayer) {
//			return "Only players can execute this command!";
//		}
//		// Check permission
//		String longestCommandPath = getLongestPath();
//		if (longestCommandPath != null) {
//			if (!checkPermission(sender, commandByName.get(longestCommandPath).getPermission())) {
//				return "You do not have the required permission!";
//			}
//		}
//		if (args.length == i + 1) {
//			// Check pass
//			String passError = commandObject.pass(args[i]);
//			if (passError == null) {
//				if (commandRunnable != null) {
//					commandRunnable.run(sender, args);
//					return null;
//				} else {
//					boolean incorrectUsage = false;
//					String builtParentCommand = null;
//					CommandValidator lastValidator = null;
//					CommandValidator check = this;
//					while (check != null) {
//						if (check.commandObject instanceof CommandString) {
//							CommandString cs = (CommandString) check.commandObject;
//							if (builtParentCommand == null) {
//								lastValidator = check;
//								builtParentCommand = cs.getString();
//							} else {
//								builtParentCommand = cs.getString() + " " + builtParentCommand;
//							}
//						} else {
//							incorrectUsage = true;
//							builtParentCommand = null;
//						}
//						check = check.parent;
//					}
//					
//					if (builtParentCommand == null) {
//						return "Too few arguments provided!";
//					} else {
//						if (incorrectUsage || !showInfo) {
//							// Show command type
//							MessageSender.err(sender, "Incorrect command usage!");
//							String usage = commandByName.get(builtParentCommand).getUsage();
//							if (usage != null) {
//								MessageSender.info(sender, usage);
//							}
//							return "";
//						} else {
//							// Info
//							MessageSender.info(sender, ChatColor.GREEN + "-- " + builtParentCommand + " --");
//							for (CommandValidator cv : lastValidator.commandValidatorList) {
//								if (cv.commandObject instanceof CommandString) {
//									CommandString cs = (CommandString) cv.commandObject;
//									String newBuiltCommand = builtParentCommand + " " + cs.getString();
//									Command cmd = commandByName.get(newBuiltCommand);
//									if (cmd != null && checkPermission(sender, cmd.getPermission())) {
//										String usage = cmd.getUsage();
//										String description = cmd.getDescription();
//										MessageSender.info(sender,
//												ChatColor.AQUA + usage + ": " + ChatColor.GRAY + description);
//									}
//								}
//							}
//							return "";
//						}
//					}
//				}
//			} else {
//				return passError;
//			}
//		} else {
//			String passError = commandObject.pass(args[i]);
//			if (passError == null) {
//				if (commandValidatorList.length == 0) {
//					return "Too many arguments!";
//				} else if (commandValidatorList.length == 1) {
//					CommandValidator cv = commandValidatorList[0];
//					passError = cv.getCommandObject().pass(args[i + 1]);
//					if (passError == null) {
//						return cv.run(sender, args, i + 1);
//					} else {
//						return passError;
//					}
//				} else {
//					for (CommandValidator cv : commandValidatorList) {
//						if (cv.getCommandObject().pass(args[i + 1]) == null) {
//							return cv.run(sender, args, i + 1);
//						}
//					}
//				}
//				return "Invalid command option!";
//			} else {
//				MessageSender.err(sender, "Invalid argument!");
//				return passError;
//			}
//		}
//	}
//	
//	public void validate(CommandSender sender, String[] args, int i, List<String> list) {
//		if (!(sender instanceof Player) && onlyPlayer) {
//			return;
//		}
//		String longestCommandPath = getLongestPath();
//		if (longestCommandPath != null) {
//			Command cmd = commandByName.get(longestCommandPath);
//			if (cmd == null) {
//				Log.error(">" + longestCommandPath + "< does not exist in plugin.yml!");
//				return;
//			}
//			if (!checkPermission(sender, cmd.getPermission())) {
//				return;
//			}
//		}
//		if (args.length == i + 1) {
//			commandObject.validate(args[i], list);
//		} else {
//			if (commandObject.pass(args[i]) == null) {
//				for (CommandValidator cv : commandValidatorList) {
//					cv.validate(sender, args, i + 1, list);
//				}
//			}
//		}
//	}
//	
//	private void setParent(CommandValidator cv) {
//		parent = cv;
//	}
//
//	public static List<String> getPlayerList() {
//		return playerList;
//	}
}
