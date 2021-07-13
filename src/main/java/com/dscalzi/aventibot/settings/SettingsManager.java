/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2021 Daniel D. Scalzi
 *
 * https://github.com/dscalzi/AventiBot
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.dscalzi.aventibot.settings;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.dscalzi.aventibot.util.Pair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import net.dv8tion.jda.api.entities.Guild;

/**
 * 
 * Utility class to handle the retrieval of settings directories.
 * 
 * @author Daniel D. Scalzi
 *
 */
public class SettingsManager {
	
	private static final Logger log = LoggerFactory.getLogger(SettingsManager.class);
	
	private static GlobalConfig configCache = null;
	private static Map<String, GuildConfig> gConfigCache = new HashMap<>();
	
	public static void reload(){
		configCache = null;
		gConfigCache.clear();
	}
	
	public static void reload(Guild g){
		gConfigCache.remove(g.getId());
	}
	
	/* * * * *
	 * 
	 * Retrieval Methods
	 * 
	 * * * * */
	
	/**
	 * Returns the base settings directory.
	 * 
	 * @return The base settings directory if it exists/was created, otherwise null.
	 */
	public static File getBaseSettingsDirectory(){
		File f = new File("settings");
		if(!f.exists()){
			if(f.mkdirs()) return f;
			else {
				log.error(MarkerFactory.getMarker("FATAL"), "Unable to create settings directory!");
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
	 * @return The settings directory for the specified guild if it exists/was created, otherwise null.
	 */
	public static File getSettingsDirectory(Guild g){
		File f = new File(getBaseSettingsDirectory(), g.getId());
		if(!f.exists()){
			if(f.mkdirs()) return f;
			else {
				log.error(MarkerFactory.getMarker("FATAL"), "Unable to create settings directory for guild "+g.getName()+" ("+g.getId()+")!");
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
	 * @return The permission file for the specified guild if it exists/was created, otherwise null.
	 */
	public static File getPermissionFile(Guild g){
		File f = new File(getSettingsDirectory(g), "permissions.json");
		if(!f.exists()){
			try {
				if(f.createNewFile()) return f;
				else {
					log.error(MarkerFactory.getMarker("FATAL"), "Unable to create permission file for guild "+g.getName()+" ("+g.getId()+")!");
					return null;
				}
			} catch (IOException e) {
				log.error(MarkerFactory.getMarker("FATAL"), "Unable to create permission file for guild "+g.getName()+" ("+g.getId()+")!");
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
	 * @return The configuration file for the specified guild if it exists/was created, otherwise null.
	 */
	public static File getConfigurationFile(Guild g){
		File f = new File(getSettingsDirectory(g), "configuration.json");
		if(!f.exists()){
			try {
				if(f.createNewFile()) return f;
				else {
					log.error(MarkerFactory.getMarker("FATAL"), "Unable to create configuration file for guild "+g.getName()+" ("+g.getId()+")!");
					return null;
				}
			} catch (IOException e) {
				log.error(MarkerFactory.getMarker("FATAL"), "Unable to create configuration file for guild "+g.getName()+" ("+g.getId()+")!");
				e.printStackTrace();
				return null;
			}
		}
		return f;
	}
	
	/**
	 * Returns the global configuration file.
	 * 
	 * @return Returns the global configuration file if it exists/was created, otherwise null.
	 */
	public static File getGlobalConfigurationFile(){
		File f = new File(getBaseSettingsDirectory(), "configuration.json");
		if(!f.exists()){
			try {
				if(f.createNewFile()) return f;
				else {
					log.error(MarkerFactory.getMarker("FATAL"), "Unable to create global configuration file!");
					return null;
				}
			} catch (IOException e) {
				log.error(MarkerFactory.getMarker("FATAL"), "Unable to create global configuration file!");
				e.printStackTrace();
				return null;
			}
		}
		return f;
	}
	
	/* * * * *
	 * 
	 * Global Config Saving/Loading
	 * 
	 * * * * */
	
	/**
	 * Retrieves the cached GlobalConfig value that was stored the last time
	 * it was loaded. If no value is already cached, it will load the value
	 * and cache it.
	 * 
	 * In order to load the GlobalConfig directly from the serialized JSON form,
	 * see {@link #loadGlobalConfig()}
	 * 
	 * @return The latest cached GlobalConfig object.
	 */
	public static GlobalConfig getGlobalConfig(){
		try {
			return configCache == null ? loadGlobalConfig() : configCache;
		} catch (IOException e) {
			log.warn("IOException when loading the global config.");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serializes the GlobalConfig object to JSON.
	 * 
	 * @param g GlobalConfig object to serialize.
	 * @throws IOException If the target file was not found/could not be created.
	 */
	public static void saveGlobalConfig(GlobalConfig g) throws IOException {
		File target = SettingsManager.getGlobalConfigurationFile();
		if(target == null) throw new IOException();
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try(FileWriter w = new FileWriter(target)){
			gson.toJson(g, w);
		}
		configCache = g;
	}
	
	
	/**
	 * Deserializes the GlobalConfig object from JSON.
	 * 
	 * @return The GlobalConfig data directly from JSON.
	 * @throws IOException If the target file was not found/could not be created.
	 */
	public static GlobalConfig loadGlobalConfig() throws IOException{
		File target = SettingsManager.getGlobalConfigurationFile();
		if(target == null) throw new IOException();

		GlobalConfig g = new GlobalConfig();
		boolean requiresSave = false;
		
		try(JsonReader file = new JsonReader(new FileReader(target))){
			JsonObject result;
			JsonElement parsed = JsonParser.parseReader(file);
			if(parsed.isJsonNull()) return generateDefaultGlobal();
			if(parsed.isJsonObject()){
				result = parsed.getAsJsonObject();
				Gson gson = new Gson();
				for(Map.Entry<Pair<String, Object>, Method> e : GlobalConfig.keyMap.entrySet()){
					JsonElement v = result.get(e.getKey().getKey());
					Method m = e.getValue();
					Class<?> required = m.getParameterTypes()[0];
					try {
						if(v == null || v.isJsonNull()) {
							requiresSave = true;
							m.invoke(g, e.getKey().getValue());
						}else
							m.invoke(g, gson.fromJson(v, required));
					} catch (JsonSyntaxException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
						log.error(MarkerFactory.getMarker("FATAL"), "Exception while parsing global config on value '" + e.getKey() + "'.");
						e1.printStackTrace();
					}
				}
			}
		}
		
		if(requiresSave) saveGlobalConfig(g);
		else configCache = g;
		
		return g;
	}
	
	/**
	 * Generates a default configuration file and serializes it to JSON.
	 * 
	 * @return The GlobalConfig object with default assigned values which was serialized.
	 * @throws IOException If the target file was not found/could not be created.
	 */
	private static GlobalConfig generateDefaultGlobal() throws IOException{
		GlobalConfig def = new GlobalConfig();
		for(Map.Entry<Pair<String, Object>, Method> e : GlobalConfig.keyMap.entrySet()){
			try {
				e.getValue().invoke(def, e.getKey().getValue());
			} catch (Throwable t){
				log.error(MarkerFactory.getMarker("FATAL"), "Error while creating default configuration:");
				t.printStackTrace();
			}
		}
		saveGlobalConfig(def);
		return def;
	}
	
	/* * * * *
	 * 
	 * Guild Config Saving/Loading
	 * 
	 * * * * */
	
	/**
	 * Retrieves the cached GlobalConfig value that was stored the last time
	 * it was loaded. If no value is already cached, it will load the value
	 * and cache it.
	 * 
	 * In order to load the GlobalConfig directly from the serialized JSON form,
	 * see {@link #loadGlobalConfig()}
	 * 
	 * @return The latest cached GlobalConfig object.
	 */
	public static GuildConfig getGuildConfig(Guild g){
		try {
			if(gConfigCache.containsKey(g.getId())) return gConfigCache.get(g.getId());
			else return loadGuildConfig(g);
		} catch (IOException e) {
			log.warn("IOException when loading the global config.");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serializes the GuildConfig object to JSON.
	 * 
	 * @param id Guild to save the settings for.
	 * @param g GuildConfig object to serialize.
	 * @throws IOException If the target file was not found/could not be created.
	 */
	public static void saveGuildConfig(Guild id, GuildConfig g) throws IOException{
		File target = SettingsManager.getConfigurationFile(id);
		if(target == null) throw new IOException();
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try(FileWriter w = new FileWriter(target)){
			gson.toJson(g, w);
		}
		gConfigCache.put(id.getId(), g);
	}
	
	/**
	 * Deserializes the configuration for a specific guild from JSON.
	 * 
	 * @param id Guild to load the settings for.
	 * 
	 * @return The GuildConfig data directly from JSON.
	 * @throws IOException If the target file was not found/could not be created.
	 */
	public static GuildConfig loadGuildConfig(Guild id) throws IOException{
		File target = SettingsManager.getConfigurationFile(id);
		if(target == null) throw new IOException();

		GuildConfig g = new GuildConfig();
		boolean requiresSave = false;
		
		try(JsonReader file = new JsonReader(new FileReader(target))){
			JsonObject result;
			JsonElement parsed = JsonParser.parseReader(file);
			if(parsed.isJsonNull()) return generateDefaultGuild(id);
			if(parsed.isJsonObject()){
				result = parsed.getAsJsonObject();
				Gson gson = new Gson();
				for(Map.Entry<Pair<String, Object>, Method> e : GuildConfig.keyMap.entrySet()){
					JsonElement v = result.get(e.getKey().getKey());
					Method m = e.getValue();
					Class<?> required = m.getParameterTypes()[0];
					try {
						if(v == null || v.isJsonNull()) {
							requiresSave = true;
							m.invoke(g, e.getKey().getValue());
						}else
							m.invoke(g, gson.fromJson(v, required));
					} catch (JsonSyntaxException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
						log.error(MarkerFactory.getMarker("FATAL"), "Exception while parsing config for guild "+id.getName()+" ("+id.getId()+") on value '" + e.getKey() + "'.");
						e1.printStackTrace();
					}
				}
			}
		}
		
		if(requiresSave) saveGuildConfig(id, g);
		else gConfigCache.put(id.getId(), g);
		
		return g;
	}
	
	/**
	 * Generates a default configuration file for the specified guild and serializes it to JSON.
	 * 
	 * @param g Guild to generate the settings for.
	 * 
	 * @return The GuildConfig object with default assigned values which was serialized.
	 * @throws IOException If the target file was not found/could not be created.
	 */
	private static GuildConfig generateDefaultGuild(Guild g) throws IOException{
		GuildConfig def = new GuildConfig();
		for(Map.Entry<Pair<String, Object>, Method> e : GuildConfig.keyMap.entrySet()){
			try {
				e.getValue().invoke(def, e.getKey().getValue());
			} catch (Throwable t){
				log.error(MarkerFactory.getMarker("FATAL"), "Error while creating default configuration:");
				t.printStackTrace();
			}
		}
		saveGuildConfig(g, def);
		return def;
	}
	
	/* * * * *
	 * 
	 * Utility Methods
	 * 
	 * * * * */
	
	public static String getCommandPrefix(){
		return getCommandPrefix(null);
	}
	
	public static String getCommandPrefix(Guild g){
		if(g == null) return getGlobalConfig().getCommandPrefix();
		else return getGuildConfig(g).getCommandPrefix(g);
	}
	
	public static Color getColorAWT(){
		return getColorAWT(null);
	}
	
	public static Color getColorAWT(Guild g){
		if(g == null) return getGlobalConfig().getDefaultColorAWT();
		else return getGuildConfig(g).getColorAWT();
	}
	
}
