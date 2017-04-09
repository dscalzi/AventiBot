/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.settings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.dscalzi.aventibot.AventiBot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

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
	
	private static GlobalConfig configCache;
	
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
	
	public static File getGlobalConfigurationFile(){
		File f = new File(getBaseSettingsDirectory(), "configuration.json");
		if(!f.exists()){
			try {
				if(f.createNewFile()) return f;
				else {
					LOG.fatal("Unable to create global configuration file!");
					return null;
				}
			} catch (IOException e) {
				LOG.fatal("Unable to create global configuration file!");
				e.printStackTrace();
				return null;
			}
		}
		return f;
	}
	
	public static void saveGlobalConfig(GlobalConfig g) throws IOException{
		File target = SettingsManager.getGlobalConfigurationFile();
		if(target == null) throw new IOException();
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try(FileWriter w = new FileWriter(target)){
			gson.toJson(g, w);
		}
		configCache = g;
	}
	
	public static GlobalConfig getGlobalConfig(){
		try {
			return configCache == null ? loadGlobalConfig() : configCache;
		} catch (IOException e) {
			LOG.warn("IOException when loading the global config.");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Loads Global Config from Json, caches the result.
	 */
	public static GlobalConfig loadGlobalConfig() throws IOException{
		File target = SettingsManager.getGlobalConfigurationFile();
		if(target == null) throw new IOException();
		
		Gson gson = new Gson();
		
		try(FileReader r = new FileReader(target)){
			GlobalConfig g = gson.fromJson(r, GlobalConfig.class);
			configCache = g == null ? generateDefault() : g;
			return configCache;
		} catch (JsonIOException e){
			LOG.fatal("JsonIOException occurred while reading the global config.");
			e.printStackTrace();
		} catch (JsonSyntaxException e){
			LOG.fatal("Global config contains invalid JSON syntax. Double check your changes.");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Generates a default configuration file and saves it to
	 * JSON format.
	 * 
	 * @return The GlobalConfig object with default assigned values which has been saved.
	 * @throws IOException If the target file could not be created.
	 */
	private static GlobalConfig generateDefault() throws IOException{
		GlobalConfig def = new GlobalConfig("NULL", "Developed by Dan", "#0f579d");
		saveGlobalConfig(def);
		return def;
	}
	
}
