package com.manelnavola.mcinteractive.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.manelnavola.mcinteractive.adventure.RewardManager;
import com.manelnavola.mcinteractive.utils.Log;
import com.manelnavola.mcinteractive.utils.MessageSender;
import com.manelnavola.mcinteractive.voting.VoteManager;
import com.manelnavola.twitchbotx.TwitchBotX;
import com.manelnavola.twitchbotx.events.*;

public class TwitchBotMCI extends TwitchBotX {
	
	private Map<Player, String> playerToChannel = new HashMap<>();
	private Map<String, List<Player>> channelPlayers = new HashMap<>();
	private Object safetyLock = new Object();
	
	public TwitchBotMCI() {
		super();
	}
	
	private String moms(int a) {
		return (a > 1) ? "months" : "month";
	}
	
	public List<Player> getChannelPlayers(String channel) {
		synchronized(safetyLock) {
			List<Player> tr = channelPlayers.get(channel);
			if (tr == null) return new ArrayList<Player>();
			return new ArrayList<Player>(channelPlayers.get(channel));
		}
	}
	
	public String getPlayerChannel(Player p) {
		synchronized(safetyLock) {
			return playerToChannel.get(p);
		}
	}
	
	public void connect(Player p, String ch) {
		String pc = getPlayerChannel(p);
		if (pc != null) {
			if (pc.equals(ch)) {
				MessageSender.warn(p, "You are already connected to this channel!");
				return;
			}
			disconnect(p);
		}
		
		synchronized(safetyLock) {
			playerToChannel.put(p, ch);
			
			List<Player> pl = channelPlayers.get(ch);
			if (pl == null) {
				pl = new ArrayList<Player>();
				pl.add(p);
				channelPlayers.put(ch, pl);
				Log.info("Joined " + ch);
				joinChannel(ch);
			} else {
				pl.add(p);
			}
		}
		
		MessageSender.nice(p, "Connected to " + ch);
	}
	
	public void disconnect(Player p) {
		synchronized(safetyLock) {
			String ch = playerToChannel.remove(p);

			if (ch == null) {
				Log.warn("Cannot disconnect player, not connected!");
				return;
			}

			MessageSender.nice(p, "Disconnected from " + ch);
			if (isConnectedTo(ch)) {
				List<Player> pl = channelPlayers.get(ch);
				pl.remove(p);
				if (pl.isEmpty()) {
					Log.info("Left " + ch);
					leaveChannel(ch);
					channelPlayers.remove(ch);
				}
			}
		}
	}
	
	@Override
	public void onTwitchMessage(final TwitchMessageEvent tm) {
		List<Player> channelPlayers = getChannelPlayers(tm.getChannelName());
		ChatManager.sendMessage(channelPlayers, tm);
		VoteManager.process(tm.getUser(), tm.getChannelName(), tm.getContents());
		if (tm.hasBits()) {
			RewardManager.processBits(channelPlayers, tm.getBits(), tm.getUser().getNickname());
		}
	}
	
	@Override
	public void onTwitchSubscription(final TwitchSubscriptionEvent te) {
		List<Player> channelPlayers = getChannelPlayers(te.getChannel());
		String receiver = te.getReceiverName();
		if (receiver == null) receiver = "Anon";
		RewardManager.process(channelPlayers, te.getSubMonths(), te.getSubPlan(), receiver);
		if (te.isGifted()) {
			if (te.isAnon()) {
				ChatManager.sendNotice(channelPlayers,
						String.join(" ",
								ChatColor.AQUA + te.getReceiverName(),
								ChatColor.WHITE + "has been gifted",
								ChatColor.GREEN + "" + te.getSubMonths(),
								ChatColor.WHITE + "sub " + moms(te.getSubMonths()) + "!"));
			} else {
				ChatManager.sendNotice(channelPlayers,
						String.join(" ",
								ChatColor.LIGHT_PURPLE + te.getGifterName(),
								ChatColor.WHITE + "gifted",
								ChatColor.GREEN + "" + te.getSubMonths(),
								ChatColor.WHITE + "sub " + moms(te.getSubMonths()) + " to",
								ChatColor.AQUA + te.getReceiverName()));
			}
		} else {
			if (te.isResub()) {
				ChatManager.sendNotice(channelPlayers,
						String.join(" ",
								ChatColor.AQUA + te.getReceiverName(),
								ChatColor.WHITE + "has resubbed for",
								ChatColor.GREEN + "" + te.getSubMonths(),
								ChatColor.WHITE + moms(te.getSubMonths()) + "!"));
			} else {
				ChatManager.sendNotice(channelPlayers,
						String.join(" ",
								ChatColor.AQUA + te.getReceiverName(),
								ChatColor.WHITE + "has subbed for",
								ChatColor.GREEN + "" + te.getSubMonths(),
								ChatColor.WHITE + moms(te.getSubMonths()) + "!"));
			}
		}
	}
	
	@Override
	public void onTwitchMysteryGift(final TwitchMysteryGiftEvent tmge) {
		/* INFO
		 * SUBMYSTERYGIFT is when someone anon gifts a sub iirc
		 */
	}
	
	@Override
	public void onTwitchGiftUpgrade(final TwitchGiftUpgradeEvent tgue) {
		if (tgue.isAnon()) {
			ChatManager.sendNotice(getChannelPlayers(tgue.getChannel()),
					String.join(" ",
							ChatColor.AQUA + tgue.getReceiverName(),
							ChatColor.WHITE + "Has been gifted an ugprade!"));
		} else {
			ChatManager.sendNotice(getChannelPlayers(tgue.getChannel()),
					String.join(" ",
							ChatColor.LIGHT_PURPLE + tgue.getGifterName(),
							ChatColor.WHITE + "has gifted an ugprade to",
							ChatColor.AQUA + tgue.getReceiverName() + ChatColor.WHITE + "!"));
		}
	}
	
	@Override
	public void onTwitchReward(final TwitchRewardEvent tre) {
		/* INFO:
		 * rewardgift typically comes with "A Cheer shared Rewards to 10 others in Chat!"
		 * this is typically emotes that get added to another user in the chat.
		 * You'll see it often in overwatchleague's chat */
	}
	
	@Override
	public void onTwitchRaid(final TwitchRaidEvent tre) {
		if (tre.hasRaidEnded()) {
			ChatManager.sendNotice(getChannelPlayers(tre.getChannel()),
					String.join(" ",
							ChatColor.RED + tre.getRaiderName() + ChatColor.WHITE + "'s",
							ChatColor.WHITE + "raid has ended!"));
		} else {
			ChatManager.sendNotice(getChannelPlayers(tre.getChannel()),
					String.join(" ",
							ChatColor.RED + tre.getRaiderName(),
							ChatColor.WHITE + "is raiding with",
							ChatColor.GREEN + "" + tre.getRaidSize(),
							ChatColor.WHITE + "viewers!"));
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
