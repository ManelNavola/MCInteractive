package com.manelnavola.mcinteractive.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.manelnavola.mcinteractive.utils.MessageSender;

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

	public CommandObject getCommandObject() {
		return commandObject;
	}
	
	private String getLongestPath() {
		String builtParentCommand = null;
		CommandValidator check = this;
		while (check != null) {
			if (check.commandObject instanceof CommandString) {
				CommandString cs = (CommandString) check.commandObject;
				if (builtParentCommand == null) {
					builtParentCommand = cs.string;
				} else {
					builtParentCommand = cs.string + " " + builtParentCommand;
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
			if (!sender.hasPermission(commandByName.get(longestCommandPath).getPermission())) {
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
								builtParentCommand = cs.string;
							} else {
								builtParentCommand = cs.string + " " + builtParentCommand;
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
							MessageSender.error(sender, "Incorrect command usage!");
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
									String newBuiltCommand = builtParentCommand + " " + cs.string;
									Command cmd = commandByName.get(newBuiltCommand);
									if (cmd != null && sender.hasPermission(cmd.getPermission())) {
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
				MessageSender.error(sender, "Invalid argument!");
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
			if (!sender.hasPermission(commandByName.get(longestCommandPath).getPermission())) {
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

abstract class CommandObject {
	
	private String[] defaults = new String[0];
	
	public abstract void validate(String input, List<String> list);
	public abstract String pass(String input);
	public abstract CommandObject clone();
	
	public void setDefaults(String[] defs) {
		defaults = defs;
	}
	
	protected String[] getDefaults() {
		return defaults;
	}
}

class CommandAny extends CommandObject {
	
	public CommandAny() {}

	@Override
	public void validate(String input, List<String> list) {
		for (String s : getDefaults()) {
			list.add(s);
		}
		return;
	}

	@Override
	public String pass(String input) {
		return null;
	}

	@Override
	public CommandObject clone() {
		return new CommandAny();
	}
	
}

class CommandTime extends CommandObject {
	
	private int min = 0;
	private int max = Integer.MAX_VALUE;
	
	public CommandTime(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	@Override
	public void validate(String input, List<String> list) {
		for (String s : getDefaults()) {
			list.add(s);
		}
		return;
	}

	@Override
	public String pass(String input) {
		Integer ttt = textToTime(input);
		if (ttt == null) return "Incorrect time format! (XhXmXs)";
		if (ttt < min) {
			return "Time cannot be lower than " + timeToText(min);
		} else if (ttt > max) {
			return "Time cannot be greater than " + timeToText(max);
		}
		return null;
	}
	
	@Override
	public CommandObject clone() {
		return new CommandTime(min, max);
	}
	
	public static String timeToText(int t) {
		int h, m, s;
		s = t%60;
		m = t/60;
		h = t/3600;
		if (h == 0) {
			if (m == 0) {
				return s + "s";
			} else {
				return m + "m" + s + "s";
			}
		} else {
			return h + "h" + m + "m" + s + "s";
		}
	}
	
	public static Integer textToTime(String input) {
		Integer tr = null;
		try {
			tr = Integer.parseInt(input);
		} catch(NumberFormatException nfe) {
			Pattern p = Pattern.compile("^([0-9]+h)?([0-9]+m)?([0-9]+s)?$");
			Matcher mat = p.matcher(input);
			if (mat.matches()) {
				int h = 0, m = 0, s = 0, hi, mi, si, sss;
				hi = input.indexOf('h');
				mi = input.indexOf('m');
				si = input.indexOf('s');
				sss = 0;
				if (hi != -1) {
					h = Integer.parseInt(input.substring(0, hi));
					sss = hi + 1;
				}
				if (mi != -1) {
					m = Integer.parseInt(input.substring(sss, mi));
					sss = mi + 1;
				}
				if (si != -1) {
					s = Integer.parseInt(input.substring(sss, si));
					sss = si + 1;
				}
				tr = (h*60 + m)*60 + s;
			}
		}
		return tr;
	}
	
}

class CommandChoose extends CommandObject {
	
	private String[] toChoose = new String[0];
	
	public CommandChoose(String... choose) {
		toChoose = new String[choose.length];
		for (int i = 0; i < choose.length; i++) {
			toChoose[i] = choose[i].toLowerCase();
		}
	}
	
	@Override
	public void validate(String input, List<String> list) {
		input = input.toLowerCase();
		for (String s : toChoose) {
			if (s.startsWith(input))
				list.add(s);
		}
		return;
	}
	
	@Override
	public String pass(String input) {
		input = input.toLowerCase();
		for (String s : toChoose) {
			if (s.equals(input)) {
				return null;
			}
		}
		return "Incorrect argment option!";
	}

	@Override
	public CommandObject clone() {
		return new CommandChoose(toChoose);
	}
	
}

class CommandPlayer extends CommandObject {
	
	public CommandPlayer() {}
	
	@Override
	public void validate(String input, List<String> list) {
		input = input.toLowerCase();
		for (String s : CommandValidator.getPlayerList()) {
			if (s.startsWith(input))
				list.add(s);
		}
		return;
	}
	
	@Override
	public String pass(String input) {
		if (CommandValidator.getPlayerList().contains(input.toLowerCase())) {
			return null;
		} else {
			return "Player is not online!";
		}
	}

	@Override
	public CommandObject clone() {
		return new CommandPlayer();
	}
	
}

class CommandString extends CommandObject {
	
	String string;
	
	public CommandString(String str) {
		string = str.toLowerCase();
	}

	public String getString() {
		return string;
	}

	@Override
	public void validate(String input, List<String> list) {
		if (string.startsWith(input.toLowerCase())) {
			list.add(string);
		}
	}
	
	@Override
	public String pass(String input) {
		if (string.equals(input.toLowerCase())) {
			return null;
		} else {
			return "Incorrect command option!";
		}
	}
	
	@Override
	public CommandObject clone() {
		return new CommandString(string);
	}
	
}

class CommandList extends CommandObject {
	
	private CommandObject commandObject;
	private int min, max;
	
	public CommandList(CommandObject co, int min, int max) {
		this.min = min;
		this.max = max;
		commandObject = co;
	}
	
	@Override
	public void validate(String input, List<String> list) {
		return;
	}

	@Override
	public String pass(String input) {
		return "???";
	}
	
	@Override
	public CommandObject clone() {
		return new CommandList(commandObject, min, max);
	}
	
	public CommandObject getCommandObject() {
		return commandObject;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
}