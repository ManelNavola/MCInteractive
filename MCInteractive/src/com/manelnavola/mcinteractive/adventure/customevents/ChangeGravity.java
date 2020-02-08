package com.manelnavola.mcinteractive.adventure.customevents;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class ChangeGravity extends CustomEvent {

	public ChangeGravity() {
		super("What's the gravity like?", new String[] {"low", "high"}, 2);
	}
	
	@Override
	public void run(List<Player> playerList, String option) {
		switch(option) {
		case "low":
			setPotionEffects(playerList,
					new PotionEffectType[] {PotionEffectType.JUMP, PotionEffectType.SLOW_FALLING}, new int[] {2, 1});
			break;
		case "high":
			setWalkspeed(playerList, 0.14F);
			setPotionEffects(playerList, PotionEffectType.JUMP, 128);
			break;
		}
	}

	@Override
	public CustomEvent clone() {
		return new ChangeGravity();
	}
	
}
