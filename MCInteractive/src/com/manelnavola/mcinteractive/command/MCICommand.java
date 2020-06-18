package com.manelnavola.mcinteractive.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.manelnavola.mcinteractive.Main;
import com.manelnavola.mcinteractive.adventure.BitsGUI;
import com.manelnavola.mcinteractive.adventure.CustomItemsGUI;
import com.manelnavola.mcinteractive.adventure.RewardManager;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem.CustomItemTier;
import com.manelnavola.mcinteractive.command.commandobjects.*;
import com.manelnavola.mcinteractive.generic.Config;
import com.manelnavola.mcinteractive.generic.ConfigGUI;
import com.manelnavola.mcinteractive.generic.ConfigManager;
import com.manelnavola.mcinteractive.generic.ConnectionManager;
import com.manelnavola.mcinteractive.generic.PlayerData;
import com.manelnavola.mcinteractive.generic.PlayerManager;
import com.manelnavola.mcinteractive.utils.MessageSender;
import com.manelnavola.mcinteractive.voting.Vote;
import com.manelnavola.mcinteractive.voting.VoteManager;
import com.manelnavola.twitchbotx.events.TwitchSubscriptionEvent.SubPlan;

public class MCICommand implements CommandExecutor {
	
	private CommandValidator main;
	
	public MCICommand(Plugin plugin) {
		// Common commands
		CommandObject commandAny = new CommandAny();
		CommandChannel commandChannel = new CommandChannel();
		
		// Channel lock
		CommandRunnable mciChannelLock = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				String ch = args[3].toLowerCase();
				PlayerManager.setConfigString("channellock", args[3].toLowerCase());
				MessageSender.nice(sender, "Channel lock has been set to listen "
						+ ChatColor.AQUA + ch);
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					@Override
					public void run() {
						for (Player p : Bukkit.getOnlinePlayers()) {
							String cc = ConnectionManager.getPlayerChannel(p);
							if (cc != null && cc.equals(ch)) {
								// Good
							} else {
								if (VoteManager.isActive(p)) {
									Vote v = VoteManager.getVote(p);
									switch(v.getVoteType()) {
									case PLAYER:
										VoteManager.endPlayerVote(p);
										break;
									case CHANNEL:
										VoteManager.endChannelVote(p);
										break;
									case EVENT:
										VoteManager.endEventVote(p);
										break;
									default:
										break;
									}
								}
								ConnectionManager.leave(p);
								ConnectionManager.listen(p, ch);
							}
						}
					}
				}, 20L);
			}
		};
		CommandValidator channelLock = new CommandValidator(new CommandString("lock"),
			new CommandValidator(commandAny,
					new CommandValidator[] {},
					mciChannelLock));
		
		// Channel unlock
		CommandRunnable mciChannelUnlock = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				if (PlayerManager.getConfigString("channellock") != null) {
					PlayerManager.setConfigString("channellock", null);
					MessageSender.nice(sender, "Channel lock has been removed!");
				} else {
					MessageSender.err(sender, "There is no current channel lock!");
				}
			}
		};
		CommandValidator channelUnlock = new CommandValidator(new CommandString("unlock"),
			new CommandValidator[] {},
			mciChannelUnlock);
		
		// Channel listen
		CommandRunnable mciChannelListen = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				String ch = PlayerManager.getConfigString("channellock");
				if (ch != null) {
					MessageSender.err(sender, "This server has been locked to listen to " + ChatColor.AQUA + ch
						+ ChatColor.GOLD + "!");
					return;
				}
				ConnectionManager.listen((Player) sender, args[3].toLowerCase());
			}
		};
		CommandValidator channelListen = new CommandValidator(new CommandString("listen"),
			new CommandValidator[] {
				new CommandValidator(commandAny,
					mciChannelListen
					)
			}, true);
		
		// Channel leave
		CommandRunnable mciChannelLeave = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				String ch = PlayerManager.getConfigString("channellock");
				if (ch != null) {
					MessageSender.err(sender, "This server has been locked to listen to " + ChatColor.AQUA + ch
						+ ChatColor.GOLD + "!");
					return;
				}
				//ConnectionManager.leave((Player) sender, false);
				ConnectionManager.leave((Player) sender);
			}
		};
		CommandValidator channelLeave =
			new CommandValidator(new CommandString("leave"),
				new CommandValidator[] {},
				mciChannelLeave,
				true);
		
		// Channel
		CommandValidator channel =
			new CommandValidatorInfo(
				new CommandString("channel"), new CommandValidator[] {
					channelListen,
					channelLeave,
					channelLock,
					channelUnlock
				});
		
		// Vote times
		CommandTime voteStartTime = new CommandTime(10, 60*60*24);
		voteStartTime.setDefaults(new String[] {
			"5m", "10m", "15m", "30m", "1h", "2h"
		});
		
		// Eventvote start
		CommandRunnable mciEventvoteStartwcn = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				VoteManager.startEventVote(sender, args[3]);
			}
		};
		CommandRunnable mciEventvoteStart = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				VoteManager.startEventVote((Player) sender);
			}
		};
		CommandValidator eventvoteStart =
			new CommandValidator(new CommandString("start"),
				new CommandValidator[] {
					new CommandValidator(commandChannel, new CommandValidator[] {}, mciEventvoteStartwcn)
				}, mciEventvoteStart
			);
		
		// Eventvote end
		CommandRunnable mciEventvoteEnd = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				if (args.length == 3) {
					if (sender instanceof Player) {
						VoteManager.endEventVote((Player) sender);
					} else {
						MessageSender.err(sender, "You must specify a channel!");
					}
				} else {
					VoteManager.endEventVote(sender, args[3]);
				}
			}
		};
		CommandValidator eventvoteEnd =
			new CommandValidator(new CommandString("end"),
				new CommandValidator(commandAny, new CommandValidator[] {}, mciEventvoteEnd),
				mciEventvoteEnd);
		
		// Eventvote cancel
		CommandRunnable mciEventvoteCancel = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				if (args.length == 3) {
					if (sender instanceof Player) {
						VoteManager.cancelEventVote((Player) sender);
					} else {
						MessageSender.err(sender, "You must specify a channel!");
					}
				} else {
					VoteManager.cancelEventVote(sender, args[3]);
				}
			}
		};
		CommandValidator eventvoteCancel =
			new CommandValidator(new CommandString("cancel"),
				new CommandValidator(commandAny, new CommandValidator[] {}, mciEventvoteCancel),
				mciEventvoteCancel);
		
		// Channelvote
		CommandValidator eventvote = 
			new CommandValidatorInfo(
				new CommandString("eventvote"), new CommandValidator[] {
					eventvoteStart,
					eventvoteEnd,
					eventvoteCancel
				});
		
		// Channelvote forcestart
		CommandRunnable mciChannelvoteForcestartwcn = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				int time = CommandTime.textToTime(args[4]);
				List<String> options = new ArrayList<>();
				for (int i = 5; i < args.length; i++) options.add(args[i]);
				VoteManager.startChannelVote(sender, args[3], time, options, true);
			}
		};
		CommandRunnable mciChannelvoteForcestart = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				int time = CommandTime.textToTime(args[3]);
				List<String> options = new ArrayList<>();
				for (int i = 4; i < args.length; i++) options.add(args[i]);
				VoteManager.startChannelVote((Player) sender, time, options, true);
			}
		};
		commandChannel.setNotA(new CommandObject[] {voteStartTime});
		CommandValidator channelvoteForcestart =
			new CommandValidator(new CommandString("forcestart"),
				new CommandValidator[] {
					new CommandValidator(voteStartTime,
						new CommandValidator(new CommandList(commandAny, 2, 6),
							mciChannelvoteForcestart), true),
					new CommandValidator(commandChannel,
						new CommandValidator(voteStartTime,
							new CommandValidator(new CommandList(commandAny, 2, 6),
									mciChannelvoteForcestartwcn)))
					
				}
					
			);
		
		// Channelvote start
		CommandRunnable mciChannelvoteStartwcn = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				int time = CommandTime.textToTime(args[4]);
				List<String> options = new ArrayList<>();
				for (int i = 5; i < args.length; i++) options.add(args[i]);
				VoteManager.startChannelVote(sender, args[3], time, options, false);
			}
		};
		CommandRunnable mciChannelvoteStart = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				int time = CommandTime.textToTime(args[3]);
				List<String> options = new ArrayList<>();
				for (int i = 4; i < args.length; i++) options.add(args[i]);
				VoteManager.startChannelVote((Player) sender, time, options, false);
			}
		};
		CommandValidator channelvoteStart =
			new CommandValidator(new CommandString("start"),
				new CommandValidator[] {
					new CommandValidator(voteStartTime,
						new CommandValidator(new CommandList(commandAny, 2, 6),
							mciChannelvoteStart), true),
					new CommandValidator(commandChannel,
						new CommandValidator(voteStartTime,
							new CommandValidator(new CommandList(commandAny, 2, 6),
									mciChannelvoteStartwcn)))
					
				}
					
			);
		
		// Channelvote end
		CommandRunnable mciChannelvoteEnd = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				if (args.length == 3) {
					if (sender instanceof Player) {
						VoteManager.endChannelVote((Player) sender);
					} else {
						MessageSender.err(sender, "You must specify a channel!");
					}
				} else {
					VoteManager.endChannelVote(sender, args[3]);
				}
			}
		};
		CommandValidator channelvoteEnd =
			new CommandValidator(new CommandString("end"),
				new CommandValidator(commandAny, new CommandValidator[] {}, mciChannelvoteEnd),
				mciChannelvoteEnd);
		
		// Channelvote cancel
		CommandRunnable mciChannelvoteCancel = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				if (args.length == 3) {
					if (sender instanceof Player) {
						VoteManager.cancelChannelVote((Player) sender);
					} else {
						MessageSender.err(sender, "You must specify a channel!");
					}
				} else {
					VoteManager.cancelChannelVote(sender, args[3]);
				}
			}
		};
		CommandValidator channelvoteCancel =
			new CommandValidator(new CommandString("cancel"),
				new CommandValidator(commandAny, new CommandValidator[] {}, mciChannelvoteCancel),
				mciChannelvoteCancel);
		
		// Channelvote
		CommandValidator channelvote = 
			new CommandValidatorInfo(
				new CommandString("channelvote"), new CommandValidator[] {
					channelvoteStart,
					channelvoteEnd,
					channelvoteCancel,
					channelvoteForcestart
				});
		
		// Vote start
		CommandRunnable mciVoteStart = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				int time = CommandTime.textToTime(args[3]);
				List<String> options = new ArrayList<>();
				for (int i = 4; i < args.length; i++) options.add(args[i]);
				VoteManager.startPlayerVote((Player) sender, time, options);
			}
		};
		CommandValidator voteStart =
			new CommandValidator(new CommandString("start"),
					new CommandValidator(voteStartTime,
						new CommandValidator(new CommandList(commandAny, 2, 6),
							mciVoteStart))
			);
		
		// Vote end
		CommandRunnable mciVoteEnd = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				VoteManager.endPlayerVote((Player) sender);
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
				VoteManager.cancelPlayerVote((Player) sender);
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
					voteCancel
				}, true);
		
		// Fetch configs
		Config[] configs = ConfigManager.getConfigList();
		
		// Config pre-setup
		CommandValidator[] configList = new CommandValidator[configs.length];
		CommandValidator[] globalConfigList = new CommandValidator[configs.length];
		for (int i = 0; i < configList.length; i++) {
			configList[i] = new ConfigCommandValidator(configs[i]);
			globalConfigList[i] = new GlobalConfigCommandValidator(configs[i]);
		}
		
		// Customitems
		CommandRunnable mciCustomitems = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				CustomItemsGUI.open((Player) sender, 0);
			}
		};
		CommandValidator customitems = 
			new CommandValidatorInfo(
				new CommandString("customitems"),
				new CommandValidator[] {},
				mciCustomitems,
				true);
		
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
				configList,
				mciConfig,
				true);
		
		// GlobalConfig
		CommandRunnable mciGlobalconfig = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				if (sender instanceof Player) {
					ConfigGUI.openGlobal((Player) sender);
				} else {
					// Info HACK
					MessageSender.info(sender, ChatColor.GREEN + "-- mci globalconfig --");
					for (Config cfg : configs) {
						String usage = cfg.getID();
						String description = "";
						for (String dp : cfg.getDescription()) {
							description += dp + " ";
						}
						description = description.substring(0, description.length() - 1);
						MessageSender.info(sender,
								ChatColor.AQUA + "/mci globalconfig " + usage + ": " + ChatColor.GRAY + description);
					}
				}
			}
		};
		CommandValidator globalconfig = 
			new CommandValidatorInfo(
				new CommandString("globalconfig"),
				globalConfigList,
				mciGlobalconfig);
		
		// Gift
		CommandRunnable mciGift = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				if (args.length == 3) {
					if (!(sender instanceof Player)) {
						MessageSender.err(sender, "You must specify a player to gift!");
						return;
					}
					MessageSender.nice(sender, "Gift given!");
					List<Player> pl = new ArrayList<>();
					pl.add((Player) sender);
					RewardManager.process(pl, CustomItemTier.find(args[2]).getValue()*2,
							SubPlan.LEVEL_1, sender.getName());
				} else {
					Player other = Bukkit.getPlayer(args[3]);
					if (other == null) {
						MessageSender.err(sender, "This player is not online anymore!");
					} else {
						MessageSender.nice(other, "You were sent a gift!");
						List<Player> pl = new ArrayList<>();
						pl.add(other);
						RewardManager.process(pl, CustomItemTier.find(args[2]).getValue()*2,
								SubPlan.LEVEL_1, sender.getName());
					}
				}
			}
		};
		CommandValidator gift = new CommandValidator(new CommandString("gift"),
				new CommandValidator[] {
					new CommandValidator(new CommandChoose(
							CustomItemTier.COMMON.getName(), CustomItemTier.UNCOMMON.getName(),
							CustomItemTier.RARE.getName(), CustomItemTier.LEGENDARY.getName()),
						new CommandValidator[] {
								new CommandValidator(new CommandPlayer(), new CommandValidator[] {}, mciGift)	
						}, mciGift)
				});
		
		// Bits remove
		CommandNumber bitsAmount = new CommandNumber(true, 0, Integer.MAX_VALUE);
		bitsAmount.setDefaults(new String[] {
			"500", "1000", "2500", "5000"
		});
		CommandRunnable mciBitsRemove = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				if (args.length == 4) {
					if (!(sender instanceof Player)) {
						MessageSender.err(sender, "You must specify a player to give bits to!");
						return;
					}
					int n = Integer.parseInt(args[3]);
					MessageSender.nice(sender, "Removed " + n + " bits from " + sender.getName());
					List<Player> pl = new ArrayList<>();
					Player p = (Player) sender;
					pl.add(p);
					PlayerData pd = PlayerManager.getPlayerData(p);
					if (pd.getBits() - n < 0) {
						pd.setBits(0);
					} else {
						pd.addBits(-n);
					}
					if (p.getOpenInventory().getTitle().equals(BitsGUI.getTitle())) {
						BitsGUI.open(p);
					}
				} else {
					int n = Integer.parseInt(args[3]);
					Player other = Bukkit.getPlayer(args[4]);
					if (other == null) {
						MessageSender.err(sender, "This player is not online anymore!");
					} else {
						List<Player> pl = new ArrayList<>();
						pl.add(other);
						Player p = (Player) sender;
						PlayerData pd = PlayerManager.getPlayerData(p);
						if (pd.getBits() - n < 0) {
							pd.setBits(0);
						} else {
							pd.addBits(-n);
						}
						if (p.getOpenInventory().getTitle().equals(BitsGUI.getTitle())) {
							BitsGUI.open(p);
						}
					}
				}
			}
		};
		CommandValidator bitsRemove = new CommandValidator(new CommandString("remove"),
				new CommandValidator[] {
					new CommandValidator(bitsAmount, new CommandValidator[] {
						new CommandValidator(new CommandPlayer(), new CommandValidator[] {}, mciBitsRemove)
					}, mciBitsRemove)
				});
		
		// Bits give
		CommandRunnable mciBitsGive = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				// mci bits give [amount] <player>
				if (args.length == 4) {
					if (!(sender instanceof Player)) {
						MessageSender.err(sender, "You must specify a player to give bits to!");
						return;
					}
					int n = Integer.parseInt(args[3]);
					MessageSender.nice(sender, "Given " + n + " bits to " + sender.getName());
					List<Player> pl = new ArrayList<>();
					Player p = (Player) sender;
					pl.add(p);
					PlayerData pd = PlayerManager.getPlayerData(p);
					pd.addBits(n);
					if (p.getOpenInventory().getTitle().equals(BitsGUI.getTitle())) {
						BitsGUI.open(p);
					}
				} else {
					int n = Integer.parseInt(args[3]);
					Player other = Bukkit.getPlayer(args[4]);
					if (other == null) {
						MessageSender.err(sender, "This player is not online anymore!");
					} else {
						MessageSender.nice(other, "You were sent " + n + " bits!");
						MessageSender.nice(sender, "Given " + n + " bits to " + other.getName());
						List<Player> pl = new ArrayList<>();
						pl.add(other);
						Player p = (Player) sender;
						PlayerData pd = PlayerManager.getPlayerData(other);
						pd.addBits(n);
						if (p.getOpenInventory().getTitle().equals(BitsGUI.getTitle())) {
							BitsGUI.open(p);
						}
					}
				}
			}
		};
		CommandValidator bitsGive = new CommandValidator(new CommandString("give"),
				new CommandValidator[] {
					new CommandValidator(bitsAmount, new CommandValidator[] {
						new CommandValidator(new CommandPlayer(), new CommandValidator[] {}, mciBitsGive)
					}, mciBitsGive)
				});
		
		// Bits
		CommandRunnable mciBits = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				BitsGUI.open((Player) sender);
			}
		};
		CommandValidator bits = new CommandValidator(new CommandString("bits"),
				new CommandValidator[] {
					bitsGive,
					bitsRemove
				},
				mciBits, true);
		
		// Version
		CommandRunnable mciVersion = new CommandRunnable() {
			@Override
			public void run(CommandSender sender, String[] args) {
				if (Main.versionMismatch != null) {
					MessageSender.warn(sender, Main.versionMismatch);
				} else {
					MessageSender.nice(sender, "Running on latest version " + Main.INTERNAL_NAME);
				}
			}
		};
		CommandValidator version = new CommandValidator(new CommandString("version"), mciVersion);
		
		// Main
		main = new CommandValidatorInfo(
			new CommandString("mci"), new CommandValidator[] {
				channel,
				vote,
				gift,
				config,
				globalconfig,
				bits,
				customitems,
				channelvote,
				eventvote,
				version
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
			MessageSender.err(sender, error);
		}
		return true;
	}
	
}

class ConfigCommandValidator extends CommandValidator {
	
	private Config config;
	
	public ConfigCommandValidator(Config c) {
		super(new CommandStringNC(c.getID()),
			new CommandValidator[] {
				new CommandValidator(new CommandStringNC("true"), new CommandRunnable() {
					@Override
					public void run(CommandSender sender, String[] args) {
						Player p = (Player) sender;
						if (PlayerManager.getLock(c.getID()) != null) {
							MessageSender.err(p, "This configuration is locked!");
							return;
						}
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
						MessageSender.nice(p, ChatColor.AQUA + c.getID()
						+ ChatColor.GOLD + " set to "
							+ ChatColor.GREEN + "true");
						PlayerManager.getPlayerData(p).setConfig(c.getID(), true);
					}
				}),
				new CommandValidator(new CommandStringNC("false"), new CommandRunnable() {
					@Override
					public void run(CommandSender sender, String[] args) {
						Player p = (Player) sender;
						if (PlayerManager.getLock(c.getID()) != null) {
							MessageSender.err(p, "This configuration is locked!");
							return;
						}
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
						MessageSender.nice(p, ChatColor.AQUA + c.getID()
							+ ChatColor.GOLD + " set to "
								+ ChatColor.RED + "false");
						PlayerManager.getPlayerData(p).setConfig(c.getID(), false);
					}
				})
			},
			new CommandRunnable() {
				@Override
				public void run(CommandSender sender, String[] args) {
					MessageSender.info(sender, ChatColor.AQUA + c.getID()
						+ ChatColor.GOLD + ": " + c.getDescription());
					if (PlayerManager.getPlayerData((Player) sender).getConfig(c.getID())) {
						MessageSender.info(sender, "Current value: " + ChatColor.GREEN + "true");
					} else {
						MessageSender.info(sender, "Current value: " + ChatColor.RED + "false");
					}
				}
			},
			true);
		
		config = c;
	}
	
	@Override
	protected boolean checkPermission(CommandSender sender, String perm) {
		if (sender instanceof ConsoleCommandSender) {
			return true;
		} else {
			Player p = (Player) sender;
			if (config == null) return false;
			if (PlayerManager.getLock(config.getID()) != null) {
				return false;
			} else {
				return p.hasPermission(perm);
			}
		}
	}
}

class GlobalConfigCommandValidator extends CommandValidator {
	
	private Config config;
	
	public GlobalConfigCommandValidator(Config c) {
		super(new CommandStringNC(c.getID()),
			new CommandValidator[] {
				new CommandValidator(new CommandStringNC("locktrue"), new CommandRunnable() {
					@Override
					public void run(CommandSender sender, String[] args) {
						MessageSender.nice(sender, ChatColor.AQUA + c.getID()
						+ ChatColor.GOLD + " locked to "
							+ ChatColor.GREEN + "true");
						if (sender instanceof Player) {
							Player p = (Player) sender;
							p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0.8F);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
						}
						PlayerManager.setLock(c.getID(), new Boolean(true));
					}
				}),
				new CommandValidator(new CommandStringNC("lockfalse"), new CommandRunnable() {
					@Override
					public void run(CommandSender sender, String[] args) {
						MessageSender.nice(sender, ChatColor.AQUA + c.getID()
						+ ChatColor.GOLD + " locked to "
							+ ChatColor.RED + "false");
						if (sender instanceof Player) {
							Player p = (Player) sender;
							p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1, 0.8F);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
						}
						PlayerManager.setLock(c.getID(), new Boolean(false));
					}
				}),
				new CommandValidator(new CommandStringNC("unlock"), new CommandRunnable() {
					@Override
					public void run(CommandSender sender, String[] args) {
						MessageSender.nice(sender, ChatColor.AQUA + c.getID()
						+ ChatColor.GOLD + " unlocked!");
						if (sender instanceof Player) {
							Player p = (Player) sender;
							p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1, 1.2F);
						}
						PlayerManager.setLock(c.getID(), null);
					}
				})
			},
			new CommandRunnable() {
				@Override
				public void run(CommandSender sender, String[] args) {
					MessageSender.info(sender, ChatColor.AQUA + c.getID()
						+ ChatColor.GOLD + ": " + c.getDescription());
					Boolean b = PlayerManager.getLock(c.getID());
					if (b != null) {
						if (b.booleanValue()) {
							MessageSender.info(sender, "Currently locked to: " + ChatColor.GREEN + "true");
						} else {
							MessageSender.info(sender, "Currently locked to: " + ChatColor.RED + "false");
						}
					} else {
						MessageSender.info(sender, "Currently unlocked");
					}
				}
			});
		
		config = c;
	}
	
	@Override
	protected boolean checkPermission(CommandSender sender, String perm) {
		if (sender instanceof ConsoleCommandSender) {
			return true;
		} else {
			Player p = (Player) sender;
			if (config == null) return false;
			if (PlayerManager.getLock(config.getID()) != null) {
				return false;
			} else {
				return p.hasPermission(perm);
			}
		}
	}
}
