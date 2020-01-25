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
			updateEventActionBar();
			time++;
			if (time > EventManager.EVENT_LENGTH_S - 1) {
				duringEvent = false;
				time = -9999;
				dispose();
				return true;
			}
		} else {
			time++;
			if (time >= 0) {
				updateTitle();
				updateActionBar();
			}
			if (time >= duration) {
				finish();
				duringEvent = true;
				time = 0;
				return true;
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

	@Override
	public String finish() {
		String resultText = super.finish();
		customEvent.run(getPlayerList(), resultText);
		return resultText;
	}

	public boolean finishedVoting() {
		return duringEvent;
	}
	
	public void dispose() {
		customEvent.dispose(getPlayerList());
	}

}
