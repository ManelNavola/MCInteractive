package com.manelnavola.mcinteractive.core.managers;

import java.util.Collection;
import java.util.HashSet;

import com.manelnavola.mcinteractive.core.utils.ChatUtils;
import com.manelnavola.mcinteractive.core.utils.ChatUtils.LogMessageType;
import com.manelnavola.mcinteractive.core.wrappers.WPlayer;

public class EventManager extends Manager {
	
	private static EventManager INSTANCE;
	
	private Collection<ManagerListener> managerListeners;
	private Collection<WPlayer<?>> bufferedConnectedPlayers = new HashSet<>();
	private Collection<WPlayer<?>> connectedPlayers;
	private Object lock = new Object();

	private EventManager() {
	}
	
	/**
	 * Gets the singleton object
	 * 
	 * @return The singleton object
	 */
	public static EventManager getInstance() {
		if (INSTANCE == null)
			INSTANCE = new EventManager();
		return INSTANCE;
	}
	
	/**
	 * Event when a player joins the server
	 * @param wp The player that joined
	 */
	public void onPlayerJoin(WPlayer<?> wp) {
		synchronized (lock) {
			if (isEnabled()) {
				ChatUtils.logOperators("Player joined", LogMessageType.INFO);
				if (!connectedPlayers.contains(wp)) {
					connectedPlayers.add(wp);
					for (ManagerListener manager : managerListeners) {
						manager.onPlayerJoin(wp);
					}
				}
			} else {
				ChatUtils.logOperators("Buffered player", LogMessageType.INFO);
				bufferedConnectedPlayers.add(wp);
			}
		}
	}
	
	/**
	 * Event when a player leaves the server
	 * @param wp The player that left
	 */
	public void onPlayerLeave(WPlayer<?> wp) {
		synchronized (lock) {
			if (isEnabled()) {
				connectedPlayers.remove(wp);
				for (ManagerListener manager : managerListeners) {
					manager.onPlayerLeave(wp);
				}
			} else {
				bufferedConnectedPlayers.remove(wp);
			}
		}
	}
	
	/**
	 * Adds a manager listener
	 * @param listener The listener to hook
	 */
	public void addListener(ManagerListener listener) {
		synchronized (lock) {
			managerListeners.add(listener);
		}
	}
	
	/**
	 * Removes a manager listener
	 * @param listener The listener to unhook
	 */
	public void removeListener(ManagerListener listener) {
		synchronized (lock) {
			managerListeners.remove(listener);
		}
	}
	
	/**
	 * Enables the manager
	 */
	public void enable() {
		synchronized (lock) {
			setEnabled(true);
			if (bufferedConnectedPlayers != null) {
				for (WPlayer<?> wp : bufferedConnectedPlayers) {
					onPlayerJoin(wp);
				}
			}
			bufferedConnectedPlayers = null;
		}
	}
	
	/**
	 * Disables the manager
	 */
	public void disable() {
		synchronized (lock) {
			for (WPlayer<?> wp : connectedPlayers) {
				for (ManagerListener manager : managerListeners) {
					manager.onPlayerLeave(wp);
				}
			}
			setEnabled(false);
			bufferedConnectedPlayers = new HashSet<WPlayer<?>>();
		}
	}
	
	@Override
	public void start() {
		synchronized (lock) {
			managerListeners = new HashSet<ManagerListener>();
			connectedPlayers = new HashSet<WPlayer<?>>();
		}
	}

	@Override
	public void stop() {
		synchronized (lock) {
			managerListeners = null;
			connectedPlayers = null;
			INSTANCE = null;
		}
	}

}
