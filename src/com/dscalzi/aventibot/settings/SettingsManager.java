/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.settings;

import java.io.File;
import java.io.IOException;

import com.dscalzi.aventibot.AventiBot;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.utils.SimpleLog;

/**
 * 
 * Utility class to handle the retrieval of settings directories.
 * 
 * @author Daniel D. Scalzi
 *
 */
public class SettingsManager {
	
	private static final SimpleLog LOG = SimpleLog.getLog("SettingsManager");
	
	/**
	 * Returns the base settings directory.
	 * 
	 * @return The base settings directory if it exists, otherwise null.
	 */
	public static File getBaseSettingsDirectory(){
		File f = new File(AventiBot.getDataPath(), "settings");
		if(!f.exists()){
			if(f.mkdirs()) return f;
			else {
				LOG.fatal("Unable to create settings directory!");
				return null;
			}
		}
		return f;
	}
	
	/**
	 * Returns the settings directory for the specified Guild. The name of the
	 * directory will be equal to the ID of the Guild.
	 * 
	 * @param g The Guild to get the settings directory for.
	 * @return The settings directory for the specified guild if it exists, otherwise null.
	 */
	public static File getSettingsDirectory(Guild g){
		File f = new File(getBaseSettingsDirectory(), g.getId());
		if(!f.exists()){
			if(f.mkdirs()) return f;
			else {
				LOG.fatal("Unable to create settings directory for guild "+g.getName()+" ("+g.getId()+")!");
				return null;
			}
		}
		return f;
	}
	
	/**
	 * Returns the permission file for the specified Guild. The name of the
	 * directory will be equal to the ID of the Guild.
	 * 
	 * @param g The Guild to get the permission file for.
	 * @return The permission file for the specified guild if it exists, otherwise null.
	 */
	public static File getPermissionFile(Guild g){
		File f = new File(getSettingsDirectory(g), "permissions.json");
		if(!f.exists()){
			try {
				if(f.createNewFile()) return f;
				else {
					LOG.fatal("Unable to create permission file for guild "+g.getName()+" ("+g.getId()+")!");
					return null;
				}
			} catch (IOException e) {
				LOG.fatal("Unable to create permission file for guild "+g.getName()+" ("+g.getId()+")!");
				e.printStackTrace();
				return null;
			}
		}
		return f;
	}
	
	/**
	 * Returns the configuration file for the specified Guild. The name of the
	 * directory will be equal to the ID of the Guild.
	 * 
	 * @param g The Guild to get the configuration file for.
	 * @return The configuration file for the specified guild if it exists, otherwise null.
	 */
	public static File getConfigurationFile(Guild g){
		File f = new File(getSettingsDirectory(g), "configuration.json");
		if(!f.exists()){
			try {
				if(f.createNewFile()) return f;
				else {
					LOG.fatal("Unable to create configuration file for guild "+g.getName()+" ("+g.getId()+")!");
					return null;
				}
			} catch (IOException e) {
				LOG.fatal("Unable to create configuration file for guild "+g.getName()+" ("+g.getId()+")!");
				e.printStackTrace();
				return null;
			}
		}
		return f;
	}
	
}
