package com.manelnavola.mcinteractiveold.command.commandobjects;

import java.util.List;

public class CommandNumber extends CommandObject {
	
	private boolean isInteger;
	private Number min;
	private Number max;
	
	public CommandNumber(boolean ii, Number min, Number max) {
		isInteger = ii;
		if (ii) {
			this.min = (int) min;
			this.max = (int) max;
		} else {
			this.min = (double) min;
			this.max = (double) max;
		}
	}
	
	public CommandNumber(boolean ii) {
		isInteger = ii;
		if (ii) {
			min = Integer.MIN_VALUE;
			max = Integer.MAX_VALUE;
		} else {
			min = Double.MIN_VALUE;
			max = Double.MAX_VALUE;
		}
	}
	
	@Override
	public void validate(String input, List<String> list) {
		for (String s : getDefaults()) {
			list.add(s);
		}
	}
	
	@Override
	public String pass(String input) {
		if (isNotA(input)) return "Invalid argument";
		if (isInteger) {
			try {
				int i = Integer.parseInt(input);
				int omin = min.intValue();
				int omax = max.intValue();
				if (i >= omin) {
					if (i <= omax) {
						return null;
					} else {
						return "Value cannot be higher than " + omax + "!";
					}
				} else {
					return "Value cannot be lower than " + omin + "!";
				}
			} catch (NumberFormatException nfe) {
				try {
					Double.parseDouble(input);
					return "Value cannot be decimal!";
				} catch (NumberFormatException nfe2) {
					return "Value is not a valid number!";
				}
			}
		} else {
			try {
				double i = Double.parseDouble(input);
				double omin = min.doubleValue();
				double omax = max.doubleValue();
				if (i >= omin) {
					if (i <= omax) {
						return null;
					} else {
						return "Value cannot be higher than " + omax + "!";
					}
				} else {
					return "Value cannot be lower than " + omin + "!";
				}
			} catch (NumberFormatException nfe) {
				return "Value is not a valid number!";
			}
		}
	}
	
	@Override
	public CommandObject clone() {
		return new CommandNumber(isInteger, min, max);
	}
	
}
