package com.manelnavola.mcinteractive.adventure.customevents;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.manelnavola.mcinteractive.adventure.EventManager;
import com.manelnavola.mcinteractive.voting.Vote;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class EventVote extends Vote {
	
	private boolean duringEvent = false;
	private CustomEvent customEvent;

	public EventVote(List<Player> pl, String ch, CustomEvent ce) {
		super(VoteType.EVENT, pl, ch, EventManager.VOTING_LENGTH_S,
				ChatColor.ITALIC + "" + ChatColor.AQUA + "Event Vote",
				ChatColor.GREEN + ce.getDescription(), ce.getOptions());
		customEvent = ce;
	}
	
	@Override
	public boolean timeStep() {
		if (duringEvent) {
			time++;
			if (time > EventManager.EVENT_LENGTH_S - 1) {
				duringEvent = false;
				time = -9999;
				dispose();
				return true;
			}
			updateEventActionBar();
		} else {
			time++;
			if (time >= duration) {
				finish();
				return true;
			}
			if (time >= 0) {
				updateTitle();
				updateActionBar();
			}
		}
		return false;
	}
	
	private void updateEventActionBar() {
		int i = (int) (((double) time/(EventManager.EVENT_LENGTH_S - 1))*25.0);
		String send = "Event time: " + "|||||||||||||||||||||||||".substring(i)
				+ ChatColor.BLACK + "|||||||||||||||||||||||||".substring(25-i);
		BaseComponent[] bc = new ComponentBuilder(send).create();
		for (Player p : getPlayerList()) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, bc);
		}
	}
	
	public void hackTimeIncrease() {
		time = 999999;
	}
	
	public void hackTimeIncreaseFinal() {
		hackTimeIncrease();
		duringEvent = true;
	}

	@Override
	public String finish() {
		String resultText = super.finish();
		duringEvent = true;
		time = 0;
		customEvent.run(getPlayerList(), resultText);
		return resultText;
	}

	public boolean finishedVoting() {
		return duringEvent;
	}
	
	public void dispose() {
		duringEvent = false;
		time = -9999;
		customEvent.dispose(getPlayerList());
	}

}
