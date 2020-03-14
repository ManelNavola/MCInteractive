package com.manelnavola.mcinteractive.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.manelnavola.mcinteractive.bukkit.wrapper.BukkitPlayer;
import com.manelnavola.mcinteractive.core.managers.EventManager;

public class BukkitListener implements Listener {
	
	@EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
		EventManager.getInstance().onPlayerJoin(new BukkitPlayer(event.getPlayer()));
    }
	
	@EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
		EventManager.getInstance().onPlayerLeave(new BukkitPlayer(event.getPlayer()));
    }
	
}
