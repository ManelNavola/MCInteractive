package com.manelnavola.mcinteractive.core.managers;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.manelnavola.mcinteractive.core.managers.ConfigurationManager.ConfigField;
import com.manelnavola.mcinteractive.core.managers.ConfigurationManager.PlayerConfigField;
import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.mcinteractive.core.utils.ChatUtils.LogMessageType;
import com.manelnavola.mcinteractive.core.utils.ChatUtils.MessageColor;
import com.manelnavola.mcinteractive.core.wrappers.WPlayer;
import com.manelnavola.mcinteractive.core.wrappers.WServer;
import com.manelnavola.mcinteractive.core.wrappers.Wrapper;
import com.manelnavola.twitchbotx.domain.TwitchUser;

/**
 * Singleton class for managing player-TwitchBotX connections
 * 
 * @author Manel Navola
 *
 */
public class StreamManager extends Manager {

	private static StreamManager INSTANCE;

	private StreamManager() {
	}

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final char[] CHARACTERS = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
			'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9' };

	private Timer timer;
	private Map<String, Collection<WPlayer<?>>> channelToPlayers;
	private Map<WPlayer<?>, String> playerToChannel;
	private ConcurrentHashMap<String, RegisterCheck> registerCheckChannels;
	private Object lock = new Object();
	private Object reigsteredStreamListLock = new Object();

	/**
	 * Gets the singleton object
	 * 
	 * @return The singleton object
	 */
	public static StreamManager getInstance() {
		if (INSTANCE == null)
			INSTANCE = new StreamManager();
		return INSTANCE;
	}

	/**
	 * Connects a player to a Twitch channel
	 * 
	 * @param wp      The player to connect
	 * @param channel The name of the Twitch channel, in lowercase
	 */
	public void joinStream(WPlayer<?> wp, String channel) {
		requireEnabled();
		
		boolean joinChannel = false;
		synchronized (lock) {
			Collection<WPlayer<?>> currentPlayers = channelToPlayers.get(channel);
			if (currentPlayers == null) {
				currentPlayers = new HashSet<WPlayer<?>>();
				channelToPlayers.put(channel, currentPlayers);
				joinChannel = true;
			}
			currentPlayers.add(wp);
			playerToChannel.put(wp, channel);
		}
		
		if (joinChannel) {
			BotManager.getInstance().joinChannel(channel);
		}
	}

	/**
	 * Disconnects a player from any Twitch channel
	 * 
	 * @param wp The player to disconnect
	 */
	public void leaveStream(WPlayer<?> wp) {
		requireEnabled();

		String playerChannel = null;
		boolean leaveChannel = false;
		synchronized (lock) {
			playerChannel = playerToChannel.get(wp);
			if (playerChannel == null)
				return;
			Collection<WPlayer<?>> currentPlayers = channelToPlayers.get(playerChannel);
			if (currentPlayers != null) {
				currentPlayers.remove(wp);
				playerToChannel.remove(wp);
				if (currentPlayers.isEmpty()) {
					channelToPlayers.remove(playerChannel);
					leaveChannel = true;
				}
			}
		}
		
		if (leaveChannel) {
			BotManager.getInstance().leaveChannel(playerChannel);
		}
	}

	/**
	 * Gets the channel of a connected player
	 * 
	 * @return The name of the connected channel
	 */
	public String getPlayerChannel(WPlayer<?> wp) {
		requireEnabled();

		synchronized (lock) {
			String channel = playerToChannel.get(wp);
			if (channel == null) return null;
			return new String(channel);
		}
	}

	/**
	 * Checks if the player is connected
	 * 
	 * @return True if the player is connected
	 */
	public boolean isPlayerConnected(WPlayer<?> wp) {
		requireEnabled();

		synchronized (lock) {
			return playerToChannel.containsKey(wp);
		}
	}

	/**
	 * Gets a collection of players currently listening to a channel
	 * 
	 * @param channelName The name of the channel
	 * @return A collection of players
	 */
	public Collection<WPlayer<?>> getChannelPlayers(String channelName) {
		requireEnabled();
		
		synchronized (lock) {
			Collection<WPlayer<?>> players = channelToPlayers.get(channelName);
			if (players == null) return new HashSet<WPlayer<?>>();
			return new HashSet<WPlayer<?>>(players);
		}
	}

	/**
	 * Returns a lowercase collection of the currently running streams
	 * 
	 * @return Collection of strings
	 */
	public String[] getRunningStreams() {
		requireEnabled();

		synchronized (lock) {
			Collection<String> streams = channelToPlayers.keySet();
			return streams.toArray(new String[streams.size()]);
		}
	}

	/**
	 * Returns whether a stream is currently ongoing or not
	 * 
	 * @param channelName The stream to check
	 * @return True if the stream is ongoing
	 */
	public boolean streamExists(String channelName) {
		requireEnabled();

		synchronized (lock) {
			return channelToPlayers.containsKey(channelName);
		}
	}

	/**
	 * Attempts to register a stream
	 * 
	 * @param channelName The channel to register
	 * @return The registration confirmation code
	 */
	 public String tryRegisterStream(WPlayer<?> wp, String channelName) {
		 requireEnabled();
		 
		 RegisterCheck rc = new RegisterCheck(wp);
		 if (registerCheckChannels.containsKey(channelName)) return null;
		 registerCheckChannels.put(channelName, rc);
		 BotManager.getInstance().joinChannel(channelName);
		 return rc.code;
	 }
	 

	/**
	 * Returns whether a player has a registered stream
	 * 
	 * @param wp The player to check
	 * @return True if the player has registered a stream
	 */
	public boolean hasRegisteredStream(WPlayer<?> wp) {
		requireEnabled();
		
		synchronized (reigsteredStreamListLock) {
			return PlayerConfigField.REGISTERED_STREAM.getConfigValue(wp, String.class) == null;
		}
		
	}
	 

	/**
	 * Returns the stream a player has registered to
	 * 
	 * @param wp The player to check
	 * @return The channel name or null if unexistant
	 */
	public String getRegisteredStream(WPlayer<?> wp) {
		requireEnabled();
		
		return PlayerConfigField.REGISTERED_STREAM.getConfigValue(wp, String.class);
	}
	 

	/**
	 * Starts streaming a player's channel
	 * 
	 * @param wp The player to stream from
	 */
	public void startStream(WPlayer<?> wp) {
		requireEnabled();

		String channelName = getRegisteredStream(wp);
		joinStream(wp, channelName);
		if (PlayerConfigField.STREAM_BROADCAST.getConfigValue(wp, Boolean.class)) {
			Wrapper.getInstance().runOnServer(new Consumer<WServer<?>>() {
				@Override
				public void accept(WServer<?> server) {
					for (WPlayer<?> wp2 : server.getOnlinePlayers()) {
						if (!StreamManager.getInstance().isPlayerConnected(wp2) && wp != wp2) {
							wp2.sendTitle(
									MessageColor.AQUA + channelName + LogMessageType.NICE.getColor()
											+ " has started streaming!",
									LogMessageType.INFO.getColor() + "You can join with /mci stream join");
						}
					}
					wp.sendTitle(LogMessageType.NICE.getColor() + "Stream started!",
							LogMessageType.INFO.getColor() + "Remember to end the stream with /mci stream end");
				}
			});
		} else {
			Wrapper.getInstance().runOnServer(new Consumer<WServer<?>>() {
				@Override
				public void accept(WServer<?> server) {
					wp.sendTitle(LogMessageType.NICE.getColor() + "Stream started!",
							LogMessageType.INFO.getColor() + "Remember to end the stream with /mci stream end");
				}
			});
		}
	}

	/**
	 * Ends streaming a player's channel
	 * 
	 * @param wp The player to end the stream from
	 */
	public void endStream(WPlayer<?> wp) {
		requireEnabled();
		
		String channelName = getRegisteredStream(wp);
		leaveStream(wp);
		Collection<WPlayer<?>> cp = channelToPlayers.get(channelName);
		ChatUtils.broadcast(cp, "The stream you joined has ended!", LogMessageType.WARN);
		for (WPlayer<?> wp2 : cp) {
			leaveStream(wp2);
		}
	}

	/**
	 * Returns whether a channel has already been registered
	 * 
	 * @param string The channel to check
	 * @return True if the channel has already been registered
	 */
	public boolean isRegistered(String channel) {
		synchronized(reigsteredStreamListLock) {
			return ConfigField.STREAM_LIST.getConfigValue(ArrayList.class).contains(channel);
		}
	}
	
	/**
	 * Processes a Twitch message
	 * 
	 * @param channelName The name of the channel
	 * @param twitchUser  The Twitch User who issued the message
	 * @param message     The message issued
	 */
	public void onTwitchMessage(@NonNull String channelName, @NonNull TwitchUser twitchUser, @Nullable String message) {
		RegisterCheck registerCheck = registerCheckChannels.get(channelName);
		if (registerCheck != null && registerCheck.code.equalsIgnoreCase(message) && twitchUser.getDisplayName().equalsIgnoreCase(channelName)) {
			synchronized (reigsteredStreamListLock) {
				ArrayList<String> registeredStreamList = ConfigField.STREAM_LIST.getConfigValueAsArrayList(String.class);
				registeredStreamList.add(channelName);
				ConfigField.STREAM_LIST.setConfigValue(registeredStreamList);
				PlayerConfigField.REGISTERED_STREAM.setConfigValue(registerCheck.wPlayer, channelName);
				registerCheckChannels.remove(channelName);
			}
			ChatUtils.send(registerCheck.wPlayer,
					"Registered as " + MessageColor.AQUA + channelName + LogMessageType.NICE.getColor() + " successfully!",
					LogMessageType.NICE);
			BotManager.getInstance().leaveChannel(channelName);
		}
	}

	@Override
	public void start() {
		channelToPlayers = new HashMap<>();
		playerToChannel = new HashMap<>();
		registerCheckChannels = new ConcurrentHashMap<>();
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				synchronized (reigsteredStreamListLock) {
					long millis = System.currentTimeMillis();
					Collection<String> remove = new HashSet<>();
					registerCheckChannels.forEach(new BiConsumer<String, RegisterCheck>() {
						@Override
						public void accept(String channelName, RegisterCheck registerCheck) {
							if (registerCheck.expirationTime < millis) {
								remove.add(channelName);
								ChatUtils.send(registerCheck.wPlayer,
										"Your registration to channel " + MessageColor.AQUA + channelName
												+ LogMessageType.WARN.getColor() + " has been cancelled!",
										LogMessageType.WARN);
							}
						}
					});
					for (String channelName : remove) {
						registerCheckChannels.remove(channelName);
						BotManager.getInstance().leaveChannel(channelName);
					}
				}
			}
		}, 2000, 2000);
		setEnabled(true);
	}

	@Override
	public void stop() {
		setEnabled(false);
		synchronized (lock) {
			ChatUtils.broadcast(playerToChannel.keySet(), "You have been disconnected from Twitch!",
					LogMessageType.ERROR);
		}
		channelToPlayers = null;
		playerToChannel = null;
		registerCheckChannels = null;
		timer.cancel();
		timer = null;
		INSTANCE = null;
	}

	/**
	 * Class for managing register checks as objects
	 * 
	 * @author Manel Navola
	 *
	 */
	public class RegisterCheck {
		protected String code;
		protected long expirationTime;
		protected WPlayer<?> wPlayer;

		/**
		 * Class constructor
		 */
		public RegisterCheck(WPlayer<?> wp) {
			wPlayer = wp;
			expirationTime = System.currentTimeMillis() + 30000;
			code = "";
			for (int i = 0; i < 5; i++) {
				code += CHARACTERS[SECURE_RANDOM.nextInt(CHARACTERS.length)];
			}
		}
	}
	
	public void cmdRegister(WPlayer<?> wp, String[] args) {
		String channelName = args[3].toLowerCase();
		if (ConfigField.STREAM_FREEJOIN.getConfigValue(Boolean.class)) {
			ChatUtils.send(wp, "You cannot register a channel while on FreeJoin mode!",
					LogMessageType.ERROR);
			return;
		}
		if (isRegistered(channelName)) {
			ChatUtils.send(wp, MessageColor.AQUA + channelName + LogMessageType.ERROR.getColor()
					+ " is already registered!", LogMessageType.ERROR);
			return;
		}
		String registeringChannel = getRegisteredStream(wp);
		if (registeringChannel != null) {
			ChatUtils.send(wp, "You have already registered with " + MessageColor.AQUA
					+ registeringChannel + LogMessageType.ERROR.getColor() + "!", LogMessageType.ERROR);
		} else {
			String code = tryRegisterStream(wp, channelName);
			if (code == null) {
				ChatUtils.send(wp, "This channel is already being registered!", LogMessageType.ERROR);
			} else {
				ChatUtils.send(wp,
						"To complete the registration type the following code in your channel's "
								+ "chat using your Twitch account: " + MessageColor.AQUA + code,
						LogMessageType.NICE);
			}
		}
	}

	public void cmdUnregister(WPlayer<?> wp, String[] args) {
		String registeredChannel = getRegisteredStream(wp);
		if (registeredChannel == null) {
			ChatUtils.send(wp, "You haven't registered to any channel yet!", LogMessageType.ERROR);
		} else {
			ChatUtils.send(wp, "You have unregistered from " + MessageColor.AQUA + registeredChannel
					+ LogMessageType.NICE.getColor() + "!", LogMessageType.NICE);
			PlayerConfigField.REGISTERED_STREAM.setConfigValue(wp, null);
			ArrayList<String> streamList = ConfigField.STREAM_LIST.getConfigValueAsArrayList(String.class);
			streamList.remove(registeredChannel);
			ConfigField.STREAM_LIST.setConfigValue(streamList);
		}
	}

	public void cmdJoin(WPlayer<?> wp, String[] args) {
		String channelName = args[3].toLowerCase();
		String currentChannel = getPlayerChannel(wp);
		if (currentChannel != null) {
			if (currentChannel.equals(channelName)) {
				ChatUtils.send(wp, "You have already joined " + channelName + "!", LogMessageType.WARN);
				return;
			} else {
				leaveStream(wp);
				ChatUtils.send(wp, "Left the stream successfully.", LogMessageType.NICE);
			}
		}
		if (ConfigField.STREAM_FREEJOIN.getConfigValue(Boolean.class)) {
			// Free join is enabled, join channel
			joinStream(wp, channelName);
			ChatUtils.send(wp, "Joined " + channelName + "!", LogMessageType.NICE);
		} else {
			// Join channel only if the channel is available
			if (streamExists(channelName)) {
				joinStream(wp, channelName);
				ChatUtils.send(wp, "Joined " + channelName + "!", LogMessageType.NICE);
			} else {
				ChatUtils.send(wp, "The channel is not currently streaming!", LogMessageType.ERROR);
			}
		}
	}

	public void cmdLeave(WPlayer<?> wp, String[] args) {
		if (isPlayerConnected(wp)) {
			leaveStream(wp);
			ChatUtils.send(wp, "Left the stream successfully.", LogMessageType.NICE);
		} else {
			ChatUtils.send(wp, "You haven't joined a stream yet!", LogMessageType.ERROR);
		}
	}

	public void cmdStart(WPlayer<?> wp, String[] args) {
		if (ConfigField.STREAM_FREEJOIN.getConfigValue(Boolean.class)) {
			ChatUtils.send(wp, "You cannot start streaming while on FreeJoin mode!", LogMessageType.ERROR);
		} else {
			String channelName = getRegisteredStream(wp);
			if (channelName != null) {
				ChatUtils.send(wp, "You haven't registered a channel yet!", LogMessageType.ERROR);
				ChatUtils.send(wp, "Register one using /mci stream register", LogMessageType.WARN);
			} else {
				if (streamExists(channelName)) {
					ChatUtils.send(wp, "The stream has already started!", LogMessageType.ERROR);
				} else {
					startStream(wp);
				}
			}
		}
	}

	public void cmdEnd(WPlayer<?> wp, String[] args) {
		String connectedChannel = getRegisteredStream(wp);
		if (connectedChannel == null || !connectedChannel.equals(getPlayerChannel(wp))) {
			ChatUtils.send(wp, "You aren't hosting a stream!", LogMessageType.ERROR);
		} else {
			endStream(wp);
			ChatUtils.send(wp, "Stream ended successfully.", LogMessageType.NICE);
		}
	}

}
