package com.manelnavola.mcinteractive.chat;

import java.util.List;

import org.bukkit.entity.Player;

public interface VoteRunnable {
	
	public abstract void run(List<Player> playerList, int option);
	
}
