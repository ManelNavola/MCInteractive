package com.manelnavola.mcinteractive.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.manelnavola.mcinteractive.adventure.CustomItemManager;
import com.manelnavola.mcinteractive.adventure.RewardManager;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem.CustomItemTier;
import com.manelnavola.mcinteractive.chat.VoteManager;
import com.manelnavola.mcinteractive.generic.ConfigGUI;
import com.manelnavola.mcinteractive.generic.ConnectionManager;
import com.manelnavola.mcinteractive.utils.MessageSender;

public class MCICommand implements CommandExecutor {
	
	private CommandValidator main;
	
	public MCICommand() {
		// Channel listen
		CommandRunnable mciChannelListen = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				ConnectionManager.listen((Player) sender, "#" + args[3].toLowerCase());
			}
		};
		CommandValidator channelListen = new CommandValidator(new CommandString("listen"),
			new CommandValidator[] {
				new CommandValidator(new CommandAny(),
					mciChannelListen)
			});
		
		// Channel leave
		CommandRunnable mciChannelLeave = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				ConnectionManager.leave((Player) sender);
			}
		};
		CommandValidator channelLeave =
			new CommandValidator(new CommandString("leave"),
				new CommandValidator[] {},
				mciChannelLeave);
		
		// Channel
		CommandValidator channel =
			new CommandValidatorInfo(
				new CommandString("channel"), new CommandValidator[] {
					channelListen,
					channelLeave
				}, true);
		
		// Vote start
		CommandRunnable mciVoteStart = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				int time = CommandTime.textToTime(args[3]);
				List<String> options = new ArrayList<>();
				for (int i = 4; i < args.length; i++) options.add(args[i]);
				VoteManager.createPlayerVote((Player) sender, time, options);
			}
		};
		CommandTime voteStartTime = new CommandTime(10, 60*60*24);
		voteStartTime.setDefaults(new String[] {
			"5m", "10m", "15m", "30m", "1h", "2h"
		});
		CommandValidator voteStart =
			new CommandValidator(new CommandString("start"),
					new CommandValidator(voteStartTime,
						new CommandValidator(new CommandList(new CommandAny(), 2, 6),
							mciVoteStart))
			);
		
		// Vote end
		CommandRunnable mciVoteEnd = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				VoteManager.endVote((Player) sender);
			}
		};
		CommandValidator voteEnd =
			new CommandValidator(new CommandString("end"),
				new CommandValidator[] {},
				mciVoteEnd);
		
		// Vote cancel
		CommandRunnable mciVoteCancel = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				VoteManager.cancelVote((Player) sender);
			}
		};
		CommandValidator voteCancel =
			new CommandValidator(new CommandString("cancel"),
				new CommandValidator[] {},
				mciVoteCancel);
		
		// Vote
		CommandValidator vote = 
			new CommandValidatorInfo(
				new CommandString("vote"), new CommandValidator[] {
					voteStart,
					voteEnd,
					voteCancel,
				}, true);
		
		// Config
		CommandRunnable mciConfig = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				ConfigGUI.open((Player) sender);
			}
		};
		CommandValidator config = 
			new CommandValidatorInfo(
				new CommandString("config"),
				new CommandValidator[] {},
				mciConfig,
				true);
		
		// Config
		CommandRunnable mciGlobalconfig = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				ConfigGUI.openGlobal((Player) sender);
			}
		};
		CommandValidator globalconfig = 
			new CommandValidatorInfo(
				new CommandString("globalconfig"),
				new CommandValidator[] {},
				mciGlobalconfig,
				true);
		
		// Gift
		CommandRunnable mciGift = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				if (args.length == 3) {
					if (!(sender instanceof Player)) {
						MessageSender.error(sender, "You must specify a player to gift!");
						return;
					}
					MessageSender.nice(sender, "Gift given!");
					RewardManager.giftCustomItem((Player) sender, CustomItemManager.getSubGift(),
							CustomItemTier.find(args[2]).getValue(), sender.getName());
				} else {
					Player other = Bukkit.getPlayer(args[3]);
					if (other == null) {
						MessageSender.error(sender, "This player is not online anymore!");
					} else {
						MessageSender.nice(other, "You were sent a gift!");
						RewardManager.giftCustomItem(other, CustomItemManager.getSubGift(),
								CustomItemTier.find(args[2]).getValue(), sender.getName());
					}
				}
			}
		};
		CommandValidator gift = 
			new CommandValidatorInfo(
				new CommandString("gift"),
				new CommandValidator[] {
					new CommandValidator(new CommandChoose(
							CustomItemTier.COMMON.getName(), CustomItemTier.UNCOMMON.getName(),
							CustomItemTier.RARE.getName(), CustomItemTier.LEGENDARY.getName()),
						new CommandValidator[] {
								new CommandValidator(new CommandPlayer(), new CommandValidator[] {}, mciGift)	
						}, mciGift)
				});
		
		// Main
		main = new CommandValidatorInfo(
			new CommandString("mci"), new CommandValidator[] {
				channel,
				vote,
				gift,
				config,
				globalconfig
			});
	}
	
	public CommandValidator getMainValidator() {
		return main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String[] argsList = new String[args.length + 1];
		argsList[0] = label;
		for (int i = 0; i < args.length; i++) {
			argsList[i+1] = args[i];
		}
		String error = main.run(sender, argsList, 0);
		if (error != null && !error.isEmpty()) {
			MessageSender.error(sender, error);
		}
		return true;
		/*
		// Check permissions
		if (sender instanceof Player) {
			if (!sender.isOp()) {
				MessageSender.error(sender, "Only operators can execute this command!");
				return true;
			}
		}
		
		args = CommandParser.parseCommas(args, sender);
		if (args == null) {
			MessageSender.warn(sender, "Invalid command parsing!");
			MessageSender.warn(sender, "Ensure sentences with \"\" are correctly wrapped!");
		}
		
		if (args.length == 0) {
			MessageSender.send(sender, ChatColor.YELLOW + "--------- " + ChatColor.WHITE + "MCInteractive Help " + ChatColor.YELLOW + "------------------");
			MessageSender.send(sender, ChatColor.GRAY + "Type /mci [option] to get help about a command");
			for (int i = 0; i < actions.size(); i++) {
				MessageSender.send(sender, ChatColor.GOLD + "/mci " + actions.get(i) + ": "
						+ ChatColor.RESET + Bukkit.getPluginCommand("mci " + actions.get(i)).getDescription());
			}
			return true;
		}
		
		// Check command
		switch (args[0].toLowerCase()) {
		case "channel":
			return channel(sender, args);
		case "vote":
			return vote(sender, args);
		case "config":
			return config(sender, args);
		case "gift":
			return gift(sender, args);
		case "listen":
			return listen(sender, args);
		case "leave":
			return leave(sender, args);
		case "startvote":
			return startVote(sender, args);
		case "endvote":
			return endVote(sender, args);
		case "cancelvote":
			return cancelVote(sender, args);
		case "config":
			return config(sender, args);
		case "reward":
			return reward(sender, args);
		}
		
		MessageSender.error(sender, "Only operators can execute this command!");*/
	}
	
	/*private boolean channel(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		
		if (args.length > 1) {
			int months = Integer.parseInt(args[1]);
			List<Player> pl = new ArrayList<Player>();
			pl.add((Player) sender);
			RewardManager.process(pl, months, SubPlan.LEVEL_1, "<command>");
			return true;
		}
		
		return false;
	}

	private boolean gift(CommandSender sender, String[] args) {
		// mci [gift common player]
		if (args.length == 2 || args.length == 3) {
			Player toSend = null;
			if (args.length == 2) {
				if (sender instanceof Player) {
					toSend = (Player) sender;
				} else {
					MessageSender.error(sender, "You must specify the player to be gifted!");
					return true;
				}
			} else {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.getName().equals(args[2])) {
						toSend = p;
						break;
					}
				}
				if (toSend == null) {
					MessageSender.error(sender, "Player is not online!");
					return true;
				}
			}
			
			CustomItemTier cit = CustomItemTier.find(args[1]);
			if (cit == null) {
				MessageSender.error(sender, "Unknown gift rarity!");
				return true;
			}
			RewardManager.giftCustomItem(toSend, CustomItemManager.getSubGift(), cit.getValue(), sender.getName());
			MessageSender.nice(sender, cit.getName() + " gift successfully sent to " + toSend.getName());
			return true;
		} else {
			MessageSender.error(sender, "This command takes 2 arguments!");
			return true;
		}
	}
	
	private boolean config(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			MessageSender.error(sender, "This command cannot be run from the console!");
			return true;
		}
		
		if (args.length == 1) {
			// One arg
			ConfigGUI.open((Player) sender);
			return true;
		} else {
			MessageSender.error(sender, "This command takes no arguments!");
			return true;
		}
	}

	private boolean endVote(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			MessageSender.error(sender, "This command cannot be run from the console!");
			return true;
		}
		
		if (args.length > 1) {
			MessageSender.error(sender, "This command takes no arguments!");
			return true;
		}
		VoteManager.endVote((Player) sender);
		return true;
	}

	private boolean cancelVote(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			MessageSender.error(sender, "This command cannot be run from the console!");
			return true;
		}
		
		if (args.length > 1) {
			MessageSender.error(sender, "This command takes no arguments!");
			return true;
		}
		VoteManager.cancelVote((Player) sender);
		return true;
	}
	
	private boolean startVote(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			MessageSender.error(sender, "This command cannot be run from the console!");
			return true;
		}
		
		Player p = (Player) sender;
		
		if (!ConnectionManager.isConnected(p)) {
			MessageSender.error(sender, "You must be connected to a Twitch channel first!");
			MessageSender.error(sender, CommandParser.getCommandUsage("mci listen"));
			return true;
		}
		
		if (VoteManager.isActive(p)) {
			MessageSender.error(sender, "A vote is already operative!");
			return true;
		}
		
		if (args.length < 5) {
			MessageSender.error(sender, "A vote needs a description, a duration and at least two options!");
			MessageSender.error(sender, CommandParser.getCommandUsage("mci startvote"));
			return true;
		}
		
		if (args.length > 9) {
			MessageSender.error(sender, "Too many options! The currently allowed maximum is 6");
			return true;
		}
		
		float duration;
		try {
			duration = Float.parseFloat(args[2]);
		} catch (NumberFormatException nfe) {
			MessageSender.error(sender, "Duration must be a number!");
			// Quick check if there's a number later
			for (int i = 3; i < args.length; i++) {
				try {
					Float.parseFloat(args[i]);
					MessageSender.warn(sender, "Are you wrapping sentences with \"\" ?");
					break;
				} catch (NumberFormatException nfe1) {
				}
			}
			return true;
		}

		if (duration < 0) {
			MessageSender.error(sender, "Duration must be higher than 0!");
			return true;
		}

		// Fill options array
		List<String> options = new ArrayList<>();
		for (int i = 3; i < args.length; i++) {
			options.add(args[i].toLowerCase());
		}
		VoteManager.createPlayerVote(p, args[1], duration, options);
		return true;
	}
	
	private boolean listen(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			MessageSender.error(sender, "This command cannot be run from the console!");
			return true;
		}
		
		if (args.length > 1) {
			ConnectionManager.listen((Player) sender, "#" + args[1].toLowerCase());
			return true;
		} else {
			MessageSender.error(sender, "You must enter the channel name to connect to!");
			MessageSender.error(sender, CommandParser.getCommandUsage("mci listen"));
			return true;
		}
	}
	
	private boolean leave(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			MessageSender.error(sender, "This command cannot be run from the console!");
			return true;
		}
		
		if (args.length > 1) {
			MessageSender.error(sender, "This command takes no arguments!");
			return true;
		}
		
		if (!ConnectionManager.isConnected((Player) sender)) {
			MessageSender.warn(sender, "You are not connected to any channel!");
			return true;
		}
		
		ConnectionManager.leave((Player) sender);
		return true;
	}*/
	
}
