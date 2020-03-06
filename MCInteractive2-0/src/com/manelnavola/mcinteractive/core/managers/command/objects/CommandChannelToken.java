package com.manelnavola.mcinteractive.core.managers.command.objects;

import java.util.List;

import com.manelnavola.mcinteractive.core.managers.StreamManager;

/**
 * Class to represent a command channel token
 * @author Manel Navola
 *
 */
public class CommandChannelToken extends CommandToken {
	
	public CommandChannelToken() {}
	
	@Override
	public void validate(final String input, List<String> list) {
		String inputLower = input.toLowerCase();
		for (String stream : StreamManager.getInstance().getRunningStreams()) {
			if (stream.startsWith(inputLower))
				list.add(stream);
		}
	}
	
	@Override
	public String pass(String input) {
		if (commandIsNotA(input)) return "Invalid argument";
		return null;
	}

	@Override
	public CommandChannelToken clone() {
		return new CommandChannelToken();
	}
	
}
