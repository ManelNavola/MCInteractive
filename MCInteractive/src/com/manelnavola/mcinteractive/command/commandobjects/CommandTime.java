package com.manelnavola.mcinteractive.command.commandobjects;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandTime extends CommandObject {
	
	private int min = 0;
	private int max = Integer.MAX_VALUE;
	
	public CommandTime(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	@Override
	public void validate(String input, List<String> list) {
		for (String s : getDefaults()) {
			list.add(s);
		}
		return;
	}

	@Override
	public String pass(String input) {
		if (isNotA(input)) return "Invalid argument";
		Integer ttt = textToTime(input);
		if (ttt == null) return "Incorrect time format! (XhXmXs)";
		if (ttt < min) {
			return "Time cannot be lower than " + timeToText(min);
		} else if (ttt > max) {
			return "Time cannot be greater than " + timeToText(max);
		}
		return null;
	}
	
	@Override
	public CommandObject clone() {
		return new CommandTime(min, max);
	}
	
	public static String timeToText(int t) {
		int h, m, s;
		s = t%60;
		m = t/60;
		h = t/3600;
		if (h == 0) {
			if (m == 0) {
				return s + "s";
			} else {
				return m + "m" + s + "s";
			}
		} else {
			return h + "h" + m + "m" + s + "s";
		}
	}
	
	public static Integer textToTime(String input) {
		Integer tr = null;
		try {
			tr = Integer.parseInt(input);
		} catch(NumberFormatException nfe) {
			Pattern p = Pattern.compile("^([0-9]+h)?([0-9]+m)?([0-9]+s)?$");
			input = input.toLowerCase();
			Matcher mat = p.matcher(input);
			if (mat.matches()) {
				int h = 0, m = 0, s = 0, hi, mi, si, sss;
				hi = input.indexOf('h');
				mi = input.indexOf('m');
				si = input.indexOf('s');
				sss = 0;
				if (hi != -1) {
					h = Integer.parseInt(input.substring(0, hi));
					sss = hi + 1;
				}
				if (mi != -1) {
					m = Integer.parseInt(input.substring(sss, mi));
					sss = mi + 1;
				}
				if (si != -1) {
					s = Integer.parseInt(input.substring(sss, si));
					sss = si + 1;
				}
				tr = (h*60 + m)*60 + s;
			}
		}
		return tr;
	}
	
}
