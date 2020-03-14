package com.manelnavola.mcinteractive.core.managers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

import org.bukkit.ChatColor;

import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.mcinteractive.core.utils.ChatUtils.LogMessageType;
import com.manelnavola.mcinteractive.core.utils.ChatUtils.MessageColor;
import com.manelnavola.twitchbotx.domain.TwitchUser;
import com.manelnavola.twitchbotx.domain.TwitchUser.Badge;

/**
 * Singleton class for managing twitch messages
 * 
 * @author Manel Navola
 *
 */
public class ChatManager extends Manager {

	private static ChatManager INSTANCE;
	
	private static final String VIEWER_TAG = MessageColor.GRAY + "[MCI]";
	private static final String SUB_TAG = MessageColor.LIGHT_PURPLE + "[Sub]";
	private static final String MOD_TAG = MessageColor.RED + "[Mod]";
	private static final String GLOBAL_MOD_TAG = MessageColor.DARK_RED + "[GlobalMod]";
	private static final String STAFF_TAG = MessageColor.GREEN + "[Staff]";
	private static final String ADMIN_TAG = MessageColor.YELLOW + "[Admin]";
	private static final String BROADCASTER_TAG = MessageColor.AQUA + "[Broadcaster]";
	private static final MessageColor[] USER_COLORS = new MessageColor[] { MessageColor.AQUA, MessageColor.BLUE,
			MessageColor.DARK_AQUA, MessageColor.DARK_GREEN, MessageColor.DARK_PURPLE, MessageColor.DARK_RED,
			MessageColor.GOLD, MessageColor.GREEN, MessageColor.LIGHT_PURPLE, MessageColor.RED, MessageColor.YELLOW };
	
	private ConcurrentHashMap<String, MessageColor> assignedColor;
	private AtomicInteger counter = new AtomicInteger(0);

	private ChatManager() {
	}

	/**
	 * Gets the singleton object
	 * 
	 * @return The singleton object
	 */
	public static ChatManager getInstance() {
		if (INSTANCE == null)
			INSTANCE = new ChatManager();
		return INSTANCE;
	}

	@Override
	public void start() {
		assignedColor = new ConcurrentHashMap<>();
		setEnabled(true);
	}

	@Override
	public void stop() {
		setEnabled(false);
		assignedColor = null;
		INSTANCE = null;
	}
	
	private MessageColor getNextRandomColor() {
		return USER_COLORS[counter.getAndUpdate(new IntUnaryOperator() {
			@Override
			public int applyAsInt(int operand) {
				return (operand + 1)%USER_COLORS.length;
			}
		})];
	}

	/**
	 * Processes a Twitch message
	 * 
	 * @param twitchUser The Twitch User who issued the message
	 * @param message    The message issued
	 */
	public void onTwitchMessage(String channelName, TwitchUser twitchUser, String message) {
		requireEnabled();
		
		String tag = VIEWER_TAG;
		if (twitchUser.hasBadge(Badge.BROADCASTER)) {
			tag = BROADCASTER_TAG;
		} else if (twitchUser.hasBadge(Badge.ADMIN)) {
			tag = ADMIN_TAG;
		} else if (twitchUser.hasBadge(Badge.STAFF)) {
			tag = STAFF_TAG;
		} else if (twitchUser.hasBadge(Badge.GLOBAL_MOD)) {
			tag = GLOBAL_MOD_TAG;
		} else if (twitchUser.hasBadge(Badge.MODERATOR)) {
			tag = MOD_TAG;
		} else if (twitchUser.hasBadge(Badge.SUBSCRIBER)) {
			tag = SUB_TAG;
		}
		
		MessageColor color = assignedColor.get(twitchUser.getUserId());
		if (color == null) {
			color = getNextRandomColor();
			assignedColor.put(twitchUser.getUserId(), color);
		}
		
		ChatUtils.broadcastRaw(StreamManager.getInstance().getChannelPlayers(channelName), tag + MessageColor.RESET + " <" + color + twitchUser.getDisplayName() + MessageColor.RESET + "> " + message);
	}

}
