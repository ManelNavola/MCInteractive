package com.manelnavola.mcinteractive.bukkit.wrapper;

import org.bukkit.entity.Player;

import com.manelnavola.mcinteractive.core.wrappers.WPlayer;

/**
 * Class that represents a bukkit player
 * @author Manel Navola
 *
 */
public class BukkitPlayer extends WPlayer<Player> {
	
	/**
	 * BukkitPlayer constructor
	 * @param player The player to base the BukkitPlayer on
	 */
	public BukkitPlayer(Player player) {
		super(player);
	}
	
	@Override
	public boolean isOp() {
		return getPlayer().isOp();
	}

	@Override
	public boolean checkPermission(String permission) {
		return getPlayer().hasPermission(permission);
	}

	@Override
	public void sendMessage(String message) {
		getPlayer().sendMessage(message);
	}

	@Override
	public String getUUID() {
		return getPlayer().getUniqueId().toString();
	}

	@Override
	public void sendTitle(String title, String subtitle) {
		getPlayer().sendTitle(title, subtitle, 10, 70, 20);
	}

}
