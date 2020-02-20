package com.manelnavola.mcinteractive.core.managers.command;

import com.manelnavola.mcinteractive.core.wrappers.WPlayer;

/**
 * Runnable interface for a command implementation
 * @author Manel Navola
 *
 */
public interface CommandRunnable {
	
	public abstract void run(WPlayer<?> wp, String[] args);
	
}