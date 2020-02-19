package com.manelnavola.mcinteractive.bukkit.wrapper;

import org.bukkit.entity.Player;

import com.manelnavola.mcinteractive.core.wrappers.WPlayer;

public class Conversion {
	
	public static WPlayer convert(Player p) {
		return new WPlayer(p, p.isOp());
	}
	
}
