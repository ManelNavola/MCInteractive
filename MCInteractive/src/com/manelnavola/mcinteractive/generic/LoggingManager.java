package com.manelnavola.mcinteractive.generic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.plugin.Plugin;

public class LoggingManager {
	
	private static final boolean LOG = true;
	
	private static Plugin plugin;
	private static PrintWriter pw;
	private static int saveEvery;
	private static FileWriter fw;
	private static DateFormat timeFormat;
	
	public static void init(Plugin p) {
		if (!LOG) return;
		
		saveEvery = 20;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		plugin = p;
		
		File dataFolder = plugin.getDataFolder();
        if(!dataFolder.exists())
        {
            dataFolder.mkdir();
        }

        File saveTo = new File(plugin.getDataFolder(), dateFormat.format(date) + ".log");
        if (!saveTo.exists())
        {
            try {
				saveTo.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
		try {
			fw = new FileWriter(saveTo, true);
			pw = new PrintWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void l(String message) {
		if (!LOG) return;
		
		if (pw != null) {
			pw.println("[" + timeFormat.format(new Date()) + "] " + message);
			saveEvery--;
			if (saveEvery <= 0) {
				saveEvery = 20;
				pw.flush();
			}
		}
	}
	
	public static void dispose() {
		if (!LOG) return;
		
		pw.flush();
		pw.close();
		try {
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
