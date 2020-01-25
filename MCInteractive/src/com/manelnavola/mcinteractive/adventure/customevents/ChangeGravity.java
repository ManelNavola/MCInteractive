package com.manelnavola.mcinteractive.adventure.customevents;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class ChangeGravity extends CustomEvent {

	public ChangeGravity() {
		super("What's the gravity like?", new String[] {"low", "high", "inverse"}, 3);
	}
	
	@Override
	public void run(List<Player> playerList, String option) {
		switch(option) {
		case "low":
			setPotionEffects(playerList,
					new PotionEffectType[] {PotionEffectType.JUMP, PotionEffectType.SLOW_FALLING}, new int[] {1, 1});
			break;
		case "high":
			setWalkspeed(playerList, 0.15F);
			setPotionEffects(playerList, PotionEffectType.JUMP, 128);
			break;
		case "inverse":
			setWalkspeed(playerList, 0F);
			setPotionEffects(playerList, PotionEffectType.LEVITATION, 1);

			break;
		}
	}
	
}
