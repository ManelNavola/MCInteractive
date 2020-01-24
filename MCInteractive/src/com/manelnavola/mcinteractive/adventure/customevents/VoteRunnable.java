package com.manelnavola.mcinteractive.adventure.customevents;

import java.util.List;

import org.bukkit.entity.Player;

public interface VoteRunnable {
	
	public abstract void run(List<Player> playerList, String option);
	
}
