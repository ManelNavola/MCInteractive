package com.manelnavola.mcinteractive.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.manelnavola.mcinteractive.Main;
import com.manelnavola.mcinteractive.adventure.RewardManager;
import com.manelnavola.mcinteractive.utils.Log;
import com.manelnavola.mcinteractive.utils.MessageSender;
import com.manelnavola.mcinteractive.voting.VoteManager;
import com.manelnavola.twitchbotx.TwitchBotX;
import com.manelnavola.twitchbotx.TwitchBotXListenerAdapter;
import com.manelnavola.twitchbotx.events.*;

public class TwitchBotMCI extends TwitchBotXListenerAdapter {

	private TwitchBotX twitchBotX;
	private Map<Player, String> playerToChannel = new HashMap<>();
	private Map<String, List<Player>> channelPlayers = new HashMap<>();
	private Object safetyLock = new Object();

	public TwitchBotMCI() throws Exception {
		twitchBotX = new TwitchBotX();
		twitchBotX.setListenerAdapter(this);
		twitchBotX.connect();
	}
	
	public void dispose() {
		twitchBotX.disconnect();
	}
	
	public Set<String> getConnectedChannels() {
		return channelPlayers.keySet();
	}

	private String moms(int a) {
		return (a > 1) ? "months" : "month";
	}

	public List<Player> getChannelPlayers(String channel) { 
		synchronized (safetyLock) {
			List<Player> tr = channelPlayers.get(channel);
			if (tr == null)
				return new ArrayList<Player>();
			return new ArrayList<Player>(tr);
		}
	}

	public String getPlayerChannel(Player p) {
		synchronized (safetyLock) {
			return playerToChannel.get(p);
		}
	}

	public void connect(Player p, String ch) {
		//LoggingManager.l("Attempting to connect " + p.getName() + " to " + ch);
		String pc = getPlayerChannel(p);
		if (pc != null) {
			if (pc.equals(ch)) {
				MessageSender.warn(p, "You are already connected to this channel!");
				//LoggingManager.l("Player was already connected to the channel!");
				return;
			}
			//LoggingManager.l("Player was connected to another channel...");
			disconnect(p);
		}

		synchronized (safetyLock) {
			//LoggingManager.l("Inserted player to channel " + ch);
			playerToChannel.put(p, ch);

			List<Player> pl = channelPlayers.get(ch);
			if (pl == null) {
				pl = new ArrayList<Player>();
				pl.add(p);
				channelPlayers.put(ch, pl);
				Log.info("TwitchBotX joined " + ch);
				twitchBotX.joinChannel(ch);
			} else {
				//LoggingManager.l("TwitchBotX was already listening to that channel...");
				pl.add(p);
			}
		}

		MessageSender.nice(p, "Connected to " + ch);
	}

	public void disconnect(Player p) {
		//LoggingManager.l("Attempting to disconnect " + p.getName());
		synchronized (safetyLock) {
			String ch = playerToChannel.remove(p);

			if (ch == null) {
				MessageSender.warn(p, "You aren't connected to any channel!");
				return;
			}
			
			//LoggingManager.l("Disconnecting player from " + ch);
			MessageSender.nice(p, "Disconnected from " + ch);
			if (twitchBotX.isConnectedTo(ch)) {
				List<Player> pl = channelPlayers.get(ch);
				pl.remove(p);
				//LoggingManager.l("Player disconnected!");
				if (pl.isEmpty()) {
					Log.info("TwitchBotX left " + ch);
					twitchBotX.partChannel(ch);
					channelPlayers.remove(ch);
				}
			}
		}
	}
	
	@Override
	public void onConnectFail(Exception e) {
		super.onConnectFail(e);
		Log.error("Could not start bot! Restart the plugin and if the issue persists contact the developer");
	}

	@Override
	public void onTwitchMessage(final TwitchMessageEvent tm) {
		try {
			Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
				@Override
				public void run() {
					List<Player> cp = getChannelPlayers(tm.getChannelName());
					ChatManager.sendMessage(cp, tm);
					VoteManager.process(tm.getTwitchUser(), tm.getChannelName(), tm.getMessage());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onTwitchCheer(final TwitchCheerEvent tc) {
		try {
			Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
				@Override
				public void run() {
					//LoggingManager.l(tm.getUser().getNickname() + " cheered " + tm.getBits());
					List<Player> cp = getChannelPlayers(tc.getChannelName());
					RewardManager.processBits(cp, tc.getBits(), tc.getTwitchUser().getDisplayName());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void onTwitchSubscription(final TwitchSubscriptionEvent te) {
		try {
			Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
				@Override
				public void run() {
					//LoggingManager.l("Processing subscription on " + te.getChannel());
					List<Player> channelPlayers = getChannelPlayers(te.getChannelName());
					String receiver = te.getReceiverDisplayName();
					if (receiver == null)
						receiver = "Anon";
					if (te.isGift()) {
						if (te.isAnonymous()) {
							ChatManager.sendNotice(channelPlayers,
									String.join(" ", ChatColor.AQUA + receiver,
											ChatColor.WHITE + "has been gifted", ChatColor.GREEN + "" + te.getSubMonths(),
											ChatColor.WHITE + "sub " + moms(te.getSubMonths()) + "!"));
						} else {
							ChatManager.sendNotice(channelPlayers,
									String.join(" ", ChatColor.LIGHT_PURPLE + te.getGifterDisplayName(),
											ChatColor.WHITE + "gifted", ChatColor.GREEN + "" + te.getSubMonths(),
											ChatColor.WHITE + "sub " + moms(te.getSubMonths()) + " to",
											ChatColor.AQUA + receiver));
						}
					} else {
						if (te.isResub()) {
							ChatManager.sendNotice(channelPlayers,
									String.join(" ", ChatColor.AQUA + receiver,
											ChatColor.WHITE + "has resubbed for", ChatColor.GREEN + "" + te.getSubMonths(),
											ChatColor.WHITE + moms(te.getSubMonths()) + "!"));
						} else {
							ChatManager.sendNotice(channelPlayers,
									String.join(" ", ChatColor.AQUA + receiver,
											ChatColor.WHITE + "has subbed for", ChatColor.GREEN + "" + te.getSubMonths(),
											ChatColor.WHITE + moms(te.getSubMonths()) + "!"));
						}
					}
					RewardManager.process(channelPlayers, te.getSubMonths(), te.getSubPlan(), receiver);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTwitchMysteryGift(final TwitchMysteryGiftEvent tmge) {
		/*
		 * INFO SUBMYSTERYGIFT is when someone anon gifts a sub iirc
		 */
	}

	@Override
	public void onTwitchGiftUpgrade(final TwitchGiftUpgradeEvent tgue) {
		try {
			Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
				@Override
				public void run() {
					if (tgue.isAnon()) {
						ChatManager.sendNotice(getChannelPlayers(tgue.getChannelName()), String.join(" ",
								ChatColor.AQUA + tgue.getReceiverName(), ChatColor.WHITE + "Has been gifted an upgrade!"));
					} else {
						ChatManager.sendNotice(getChannelPlayers(tgue.getChannelName()),
								String.join(" ", ChatColor.LIGHT_PURPLE + tgue.getGifterName(),
										ChatColor.WHITE + "has gifted an upgrade to",
										ChatColor.AQUA + tgue.getReceiverName() + ChatColor.WHITE + "!"));
					}
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void onTwitchReward(final TwitchRewardEvent tre) {
		/*
		 * INFO: rewardgift typically comes with
		 * "A Cheer shared Rewards to 10 others in Chat!" this is typically emotes that
		 * get added to another user in the chat. You'll see it often in
		 * overwatchleague's chat
		 */
	}

	@Override
	public void onTwitchRaid(final TwitchRaidEvent tre) {
		try {
			Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
				@Override
				public void run() {
					if (tre.hasRaidEnded()) {
						ChatManager.sendNotice(getChannelPlayers(tre.getChannelName()),
								String.join(" ", ChatColor.RED + tre.getRaiderDisplayName() + ChatColor.WHITE + "'s",
										ChatColor.WHITE + "raid has ended!"));
					} else {
						ChatManager.sendNotice(getChannelPlayers(tre.getChannelName()),
								String.join(" ", ChatColor.RED + tre.getRaiderDisplayName(), ChatColor.WHITE + "is raiding with",
										ChatColor.GREEN + "" + tre.getRaidSize(), ChatColor.WHITE + "viewers!"));
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTwitchRitual(final TwitchRitualEvent tre) {
		// Pass
	}

	@Override
	public void onTwitchBitsBadge(final TwitchBitsBadgeEvent tbbe) {
		// Pass
	}

}