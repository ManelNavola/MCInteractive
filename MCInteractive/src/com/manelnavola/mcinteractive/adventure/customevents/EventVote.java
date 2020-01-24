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
	private VoteRunnable voteRunnable;

	public EventVote(VoteType vt, List<Player> pl, String ch, String title, String subtitle,
			List<String> opt, VoteRunnable vr) {
		super(vt, pl, ch, EventManager.VOTING_LENGTH_S, title, subtitle, opt);
		voteRunnable = vr;
	}
	
	@Override
	public boolean timeStep() {
		if (duringEvent) {
			updateEventActionBar();
			time++;
			if (time > EventManager.EVENT_LENGTH_S) {
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
			}
		}
		return false;
	}
	
	private void updateEventActionBar() {
		int i = (int) (((double) time/EventManager.EVENT_LENGTH_S)*10.0);
		String send = "Event time: " + "||||||||||".substring(i) + ChatColor.BLACK + "||||||||||".substring(10-i);
		BaseComponent[] bc = new ComponentBuilder(send).create();
		for (Player p : getPlayerList()) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, bc);
		}
	}

	@Override
	public String finish() {
		String resultText = super.finish();
		voteRunnable.run(getPlayerList(), resultText);
		return resultText;
	}

}
