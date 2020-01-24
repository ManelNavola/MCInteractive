package com.manelnavola.mcinteractive.adventure.customevents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomEvent {
	
	private String description;
	private VoteRunnable runnable;
	private List<String> options;
	
	public CustomEvent(String d, VoteRunnable r, String[] op, int amount) {
		description = d;
		runnable = r;
		options = new ArrayList<>();
		for (String s : op) {
			options.add(s);
		}
		Collections.shuffle(options);
		options = options.subList(0, amount);
	}
	
	public String getDescription() { return description; }
	public VoteRunnable getRunnable() { return runnable; }
	public List<String> getOptions() { return options; }

}
