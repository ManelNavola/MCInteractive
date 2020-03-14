package com.manelnavola.mcinteractive.core.managers;

import com.manelnavola.mcinteractive.core.wrappers.WPlayer;

public interface ManagerListener {
	
	/**
	 * Triggered when a player joins
	 * @param wp The player that joined
	 */
	public void onPlayerJoin(WPlayer<?> wp);
	
	/**
	 * Triggered when a player leaves
	 * @param wp The player that left
	 */
	public void onPlayerLeave(WPlayer<?> wp);
	
}
