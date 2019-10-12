package com.manelnavola.mcinteractive.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.manelnavola.mcinteractive.generic.PlayerData;
import com.manelnavola.mcinteractive.generic.PlayerManager;
import com.manelnavola.twitchbotx.TwitchUser;
import com.manelnavola.twitchbotx.events.TwitchMessageEvent;

public class ChatManager {
	
	private static final String DEFAULT_TAG = ChatColor.GRAY + "[MCI]";
	private static final int[] TITLE_DURATION = {10, 70, 10};
	private static final Map<String, ChatColor> USER_CHAT_COLORS = new HashMap<>();
	private static final ChatColor[] ALLOWED_CHAT_COLORS = new ChatColor[] {
			ChatColor.AQUA, ChatColor.BLUE, ChatColor.DARK_AQUA, ChatColor.DARK_GREEN, ChatColor.DARK_PURPLE, ChatColor.DARK_RED,
			ChatColor.GOLD, ChatColor.GREEN, ChatColor.LIGHT_PURPLE, ChatColor.RED, ChatColor.YELLOW
	};
	private static int randomChatColorIndex = 0;
	
	// Optional tag
	public static String parseUserTag(TwitchUser tu) {
		String tag = DEFAULT_TAG;

		if (tu.hasBadge()) {
			if (tu.isBroadcaster()) {
				tag = ChatColor.AQUA + "[Streamer]";
			} else if (tu.isAdmin()) {
				tag = ChatColor.YELLOW + "[Admin]";
			} else if (tu.isModerator()) {
				tag = ChatColor.RED + "[Mod]";
			} else if (tu.isSubscriber()) {
				tag = ChatColor.LIGHT_PURPLE + "[Sub]";
			}
		}

		return tag;
	}

	// Username tag
	public static String parseUsername(PlayerData pd, TwitchUser tu, boolean tags) {
		if (!USER_CHAT_COLORS.containsKey(tu.getUUID())) {
			randomChatColorIndex++;
			if (randomChatColorIndex >= ALLOWED_CHAT_COLORS.length) randomChatColorIndex = 0;
			USER_CHAT_COLORS.put(tu.getUUID(), ALLOWED_CHAT_COLORS[randomChatColorIndex]);
		}
		String name = USER_CHAT_COLORS.get(tu.getUUID()) + tu.getNickname();
		if (tags) {
			ChatColor tagsColor;
			if (tu.hasBadge()) {
				if (tu.isSubscriber()) {
					if (pd.getConfig("highlight")) {
						tagsColor = ChatColor.WHITE;
					} else {
						tagsColor = ChatColor.GRAY;
					}
				} else {
					tagsColor = ChatColor.WHITE;
				}
			} else {
				tagsColor = ChatColor.GRAY;
			}
			return tagsColor + "<" + name + "" + tagsColor + ">";
		} else {
			return name;
		}
	}

	// Message color
	public static String parseMessage(PlayerData pd, TwitchUser tu, String message) {
		if (tu.hasBadge()) {
			if (tu.isSubscriber()) {
				if (pd.getConfig("highlight")) {
					return ChatColor.WHITE + message;
				} else {
					return ChatColor.GRAY + message;
				}
			} else {
				return ChatColor.WHITE + message;
			}
		} else {
			return ChatColor.GRAY + message;
		}
	}
	
	public static void sendMessage(List<Player> pl, TwitchMessageEvent tme) {
		for (Player p : pl) {
			PlayerData pd = PlayerManager.getPlayerData(p);
			
			if (VoteManager.isActive(p)) {
				Vote v = VoteManager.getVote(p);
				String add = v.getOptionChatColor(tme.getContents());
				if (add != null) {
					// A  vote, processed automatically, no need to show
					return;
				}
			}
			
			/*if (VoteManager.isActive(p) && pd.getConfig("showvotes")) {
				Vote v = VoteManager.getVote(p);
				String add = v.getOptionChatColor(tme.getContents());
				TwitchUser tu = tme.getUser();
				String message = tme.getContents().toLowerCase();
				String prefix = getTag(tu);
				ChatColor messageColor = ChatColor.GRAY;
				if (!prefix.equals(defaultTag)) {
					if (pd.getConfig("highlight")) messageColor = ChatColor.WHITE;
				}
				String username = tu.getNickname();
				if (add.equals("")) {
					// Not a vote, abort
				} else {
					// Vote
					if (!pd.getConfig("showvotes")) continue;
					if (v.getTwitchUserVote(tu) != null) {
						if (!v.getTwitchUserVote(tu).equals(message)) {
							p.sendMessage(prefix + " " + ChatColor.WHITE + getUserChatColor(tu) + username
									+ messageColor + " changed vote to " + add + message.toLowerCase());
						}
					} else {
						p.sendMessage(prefix + " " + ChatColor.WHITE + getUserChatColor(tu) + username
								+ messageColor + " voted " + add + message.toLowerCase());
					}
					continue; // Next iteration
				}
			}*/
			
			if (pd.getConfig("showchat")) {
				TwitchUser tu = tme.getUser();
				String tag = parseUserTag(tu);
				String user = parseUsername(pd, tu, true);
				String msg = parseMessage(pd, tu, tme.getContents());
				p.sendMessage(tag + " " + user + " " + msg);
			}
		}
	}

	public static void sendNotice(List<Player> pl, String msg) {
		for (Player p : pl) {
			p.sendMessage(msg);
			if (PlayerManager.getPlayerData(p).getConfig("noticetitle")) {
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
				p.sendTitle("", msg, TITLE_DURATION[0], TITLE_DURATION[1], TITLE_DURATION[2]);
			}
		}
	}
	
}
