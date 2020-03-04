package com.manelnavola.mcinteractive.core.managers.command.objects;

/**
 * Class that represents the information of a command
 * @author Manel Navola
 *
 */
public class CommandInfo {
	
	private String usage;
	private String description;
	private String permission;
	
	/**
	 * Class constructor
	 * @param object The JsonObject to build the information upon
	 */
	public CommandInfo(String usage, String description, String permission) {
		this.usage = usage;
		this.description = description;
		this.permission = permission;
	}
	
	/**
	 * Gets the usage of the command
	 * @return The command's usage
	 */
	public String getUsage() {
		return usage;
	}

	/**
	 * Gets the description of the command
	 * @return The command's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the permission of the command
	 * @return The command's permission
	 */
	public String getPermission() {
		return permission;
	}
	
	
	
}
