package com.manelnavola.mcinteractive.command;

import com.manelnavola.mcinteractive.command.commandobjects.CommandObject;

public class CommandValidatorInfo extends CommandValidator {
	
	public CommandValidatorInfo(CommandObject co, CommandValidator[] cvl, CommandRunnable r, boolean p) {
		super(co, cvl, r, p);
		setShowInfo(true);
	}
	
	public CommandValidatorInfo(CommandObject co, CommandRunnable r) {
		this(co, new CommandValidator[] { }, r, false);
	}
	
	public CommandValidatorInfo(CommandObject co, CommandValidator[] cvl) {
		this(co, cvl, null, false);
	}
	
	public CommandValidatorInfo(CommandObject co, CommandRunnable r, boolean p) {
		this(co, new CommandValidator[] { }, r, p);
	}
	
	public CommandValidatorInfo(CommandObject co, CommandValidator[] cvl, boolean p) {
		this(co, cvl, null, p);
	}
	
	public CommandValidatorInfo(CommandObject co, CommandValidator cv, CommandRunnable r) {
		this(co, new CommandValidator[] { cv }, r, false);
	}
	
	public CommandValidatorInfo(CommandObject co, CommandValidator[] cvl, CommandRunnable r) {
		this(co, cvl, r, false);
	}
	
	public CommandValidatorInfo(CommandObject co, CommandValidator cv, CommandRunnable r, boolean p) {
		this(co, new CommandValidator[] { cv }, r, p);
	}
	
}
