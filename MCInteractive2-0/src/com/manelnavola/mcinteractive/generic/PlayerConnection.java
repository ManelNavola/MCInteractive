package com.manelnavola.mcinteractive.generic;

public class PlayerConnection {
	
	private TwitchBotMCI twitchBotMCI;
	private String channel;
	
	public PlayerConnection(TwitchBotMCI tbmci, String ch) {
		twitchBotMCI = tbmci;
		channel = ch;
	}
	
	public TwitchBotMCI getTwitchBotMCI() { return twitchBotMCI; }
	public String getChannel() { return channel; }
	
}
