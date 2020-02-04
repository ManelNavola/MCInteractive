package com.manelnavola.mcinteractive.adventure.customevents;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;


public class PlayerMove extends CustomEvent {

	public PlayerMove() {
		super("How does the player move?", new String[] {"jump", "fast", "auto"}, 3);
	}
	
	@Override
	public void run(List<Player> playerList, String option) {
		switch(option) {
		case "jump":
			setWalkspeed(playerList, 0F);
			break;
		case "fast":
			setWalkspeed(playerList, 0.4F);
			break;
		case "auto":
			setWalkspeed(playerList, 0F);
			setPotionEffects(playerList, PotionEffectType.JUMP, 128);
			runTaskTimer(new Runnable() {
				@Override
				public void run() {
					for (Player p : playerList) {
						if (p.getWalkSpeed() == 0.0F) {
							Block b = p.getLocation().add(0, -2, 0).getBlock();
							if (b == null || b.getType() == Material.AIR) continue;
							p.setVelocity(p.getLocation().getDirection());
						}
					}
				}
			}, 0, 10L);
			break;
		}
	}
	
}
