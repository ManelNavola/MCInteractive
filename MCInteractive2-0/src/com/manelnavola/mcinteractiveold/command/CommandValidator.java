package com.manelnavola.mcinteractiveold.command;

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

import com.manelnavola.mcinteractiveold.command.commandobjects.CommandList;
import com.manelnavola.mcinteractiveold.command.commandobjects.CommandObject;
import com.manelnavola.mcinteractiveold.command.commandobjects.CommandString;
import com.manelnavola.mcinteractiveold.utils.Log;
import com.manelnavola.mcinteractiveold.utils.MessageSender;

public class CommandValidator {
	
	private static Map<String, Command> commandByName;
	private static List<String> playerList;
	
	private CommandValidator parent;
	private CommandObject commandObject;
	private CommandValidator[] commandValidatorList;
	private CommandRunnable commandRunnable;
	private boolean onlyPlayer;
	private boolean showInfo = false;
	
	public boolean isShowInfo() {
		return showInfo;
	}

	public void setShowInfo(boolean showInfo) {
		this.showInfo = showInfo;
	}

	public static void init(Plugin plugin) {
		playerList = new ArrayList<String>();
		commandByName = new HashMap<String, Command>();
		List<Command> cmdList = PluginCommandYamlParser.parse(plugin);
		for(int i = 0; i < cmdList.size(); i++) {
			Command cmd = cmdList.get(i);
			commandByName.put(cmd.getName(), cmd);
        }
	}
	
	public static void addPlayer(Player p) {
		playerList.add(p.getName().toLowerCase());
	}

	public static void removePlayer(Player p) {
		playerList.remove(p.getName().toLowerCase());
	}

	public static void dispose() {
		playerList.clear();
		commandByName.clear();
	}
	
	private CommandValidator[] addToArray(CommandValidator[] cvl, CommandValidator cv) {
		CommandValidator[] tr = new CommandValidator[cvl.length + 1];
		for (int i = 0; i < cvl.length; i++) {
			tr[i] = cvl[i];
		}
		tr[tr.length - 1] = cv;
		return tr;
	}
	
	public CommandValidator(CommandObject co, CommandValidator[] cvl, CommandRunnable r, boolean p) {
		commandObject = co;
		
		// Command list treatment
		if (commandObject instanceof CommandList) {
			CommandList csl = (CommandList) commandObject;
			commandObject = csl.getCommandObject().clone();
			if (csl.getMin() == csl.getMax()) {
				// Last one
				commandValidatorList = cvl;
				commandRunnable = r;
			} else if (csl.getMin() > 1) {
				// Invalid
				commandValidatorList = new CommandValidator[] {
					new CommandValidator(
						new CommandList(csl.getCommandObject(), csl.getMin() - 1, csl.getMax() - 1),
						cvl, r)
				};
			} else {
				// Valid
				commandValidatorList = addToArray(cvl,
					new CommandValidator(
						new CommandList(csl.getCommandObject(), csl.getMin(), csl.getMax() - 1),
						cvl, r));
				commandRunnable = r;
			}
		} else {
			commandValidatorList = cvl;
			commandRunnable = r;
		}
		
		for (CommandValidator cv : commandValidatorList) {
			cv.setParent(this);
		}
		onlyPlayer = p;
	}

	public CommandValidator(CommandObject co, CommandRunnable r) {
		this(co, new CommandValidator[] { }, r, false);
	}
	
	public CommandValidator(CommandObject co, CommandValidator[] cvl) {
		this(co, cvl, null, false);
	}
	
	public CommandValidator(CommandObject co, CommandRunnable r, boolean p) {
		this(co, new CommandValidator[] { }, r, p);
	}
	
	public CommandValidator(CommandObject co, CommandValidator[] cvl, boolean p) {
		this(co, cvl, null, p);
	}
	
	public CommandValidator(CommandObject co, CommandValidator cv, CommandRunnable r) {
		this(co, new CommandValidator[] { cv }, r, false);
	}
	
	public CommandValidator(CommandObject co, CommandValidator[] cvl, CommandRunnable r) {
		this(co, cvl, r, false);
	}
	
	public CommandValidator(CommandObject co, CommandValidator cv, CommandRunnable r, boolean p) {
		this(co, new CommandValidator[] { cv }, r, p);
	}

	public CommandValidator(CommandObject co, CommandValidator cv) {
		this(co, cv, null, false);
	}

	public CommandValidator(CommandObject co, CommandValidator cv, boolean b) {
		this(co, cv, null, b);
	}

	public CommandObject getCommandObject() {
		return commandObject;
	}
	
	protected boolean checkPermission(CommandSender sender, String perm) {
		if (sender instanceof ConsoleCommandSender) {
			return true;
		} else {
			return ((Player) sender).hasPermission(perm);
		}
	}
	
	private String getLongestPath() {
		String builtParentCommand = null;
		CommandValidator check = this;
		while (check != null) {
			if (check.commandObject instanceof CommandString) {
				CommandString cs = (CommandString) check.commandObject;
				if (builtParentCommand == null) {
					builtParentCommand = cs.getString();
				} else {
					builtParentCommand = cs.getString() + " " + builtParentCommand;
				}
			} else {
				builtParentCommand = null;
			}
			check = check.parent;
		}
		return builtParentCommand;
	}
	
	public String run(CommandSender sender, String[] args, int i) {
		if (!(sender instanceof Player) && onlyPlayer) {
			return "Only players can execute this command!";
		}
		// Check permission
		String longestCommandPath = getLongestPath();
		if (longestCommandPath != null) {
			if (!checkPermission(sender, commandByName.get(longestCommandPath).getPermission())) {
				return "You do not have the required permission!";
			}
		}
		if (args.length == i + 1) {
			// Check pass
			String passError = commandObject.pass(args[i]);
			if (passError == null) {
				if (commandRunnable != null) {
					commandRunnable.run(sender, args);
					return null;
				} else {
					boolean incorrectUsage = false;
					String builtParentCommand = null;
					CommandValidator lastValidator = null;
					CommandValidator check = this;
					while (check != null) {
						if (check.commandObject instanceof CommandString) {
							CommandString cs = (CommandString) check.commandObject;
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
						if (incorrectUsage || !showInfo) {
							// Show command type
							MessageSender.err(sender, "Incorrect command usage!");
							String usage = commandByName.get(builtParentCommand).getUsage();
							if (usage != null) {
								MessageSender.info(sender, usage);
							}
							return "";
						} else {
							// Info
							MessageSender.info(sender, ChatColor.GREEN + "-- " + builtParentCommand + " --");
							for (CommandValidator cv : lastValidator.commandValidatorList) {
								if (cv.commandObject instanceof CommandString) {
									CommandString cs = (CommandString) cv.commandObject;
									String newBuiltCommand = builtParentCommand + " " + cs.getString();
									Command cmd = commandByName.get(newBuiltCommand);
									if (cmd != null && checkPermission(sender, cmd.getPermission())) {
										String usage = cmd.getUsage();
										String description = cmd.getDescription();
										MessageSender.info(sender,
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
			String passError = commandObject.pass(args[i]);
			if (passError == null) {
				if (commandValidatorList.length == 0) {
					return "Too many arguments!";
				} else if (commandValidatorList.length == 1) {
					CommandValidator cv = commandValidatorList[0];
					passError = cv.getCommandObject().pass(args[i + 1]);
					if (passError == null) {
						return cv.run(sender, args, i + 1);
					} else {
						return passError;
					}
				} else {
					for (CommandValidator cv : commandValidatorList) {
						if (cv.getCommandObject().pass(args[i + 1]) == null) {
							return cv.run(sender, args, i + 1);
						}
					}
				}
				return "Invalid command option!";
			} else {
				MessageSender.err(sender, "Invalid argument!");
				return passError;
			}
		}
	}
	
	public void validate(CommandSender sender, String[] args, int i, List<String> list) {
		if (!(sender instanceof Player) && onlyPlayer) {
			return;
		}
		String longestCommandPath = getLongestPath();
		if (longestCommandPath != null) {
			Command cmd = commandByName.get(longestCommandPath);
			if (cmd == null) {
				Log.error(">" + longestCommandPath + "< does not exist in plugin.yml!");
				return;
			}
			if (!checkPermission(sender, cmd.getPermission())) {
				return;
			}
		}
		if (args.length == i + 1) {
			commandObject.validate(args[i], list);
		} else {
			if (commandObject.pass(args[i]) == null) {
				for (CommandValidator cv : commandValidatorList) {
					cv.validate(sender, args, i + 1, list);
				}
			}
		}
	}
	
	private void setParent(CommandValidator cv) {
		parent = cv;
	}

	public static List<String> getPlayerList() {
		return playerList;
	}
}
