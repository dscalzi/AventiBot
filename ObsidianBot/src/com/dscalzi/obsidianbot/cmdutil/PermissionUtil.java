package com.dscalzi.obsidianbot.cmdutil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.console.ConsoleUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.SimpleLog;

public final class PermissionUtil {
	
	private static final String ALLOWEDKEY = "allowedRoles";
	private static final String BLACKLISTKEY = "blacklistedUsers";
	private static final String GATEKEY = "requiresPermission";
	private static final String FRIENDLYNAME = "friendlyName";
	private static final SimpleLog log = SimpleLog.getLog("PermissionUtil");
	
	private static final File permissionFile;

	//Key is node:guildid
	private static final Map<String, List<String>> permissionMap;
	private static final Map<String, List<String>> blacklistMap;
	
	static {
		permissionFile = new File(ObsidianBot.getDataPath(), "permissions.json");
		permissionMap = new HashMap<String, List<String>>();
		blacklistMap = new HashMap<String, List<String>>();
	}
	
	private static boolean verifyFile() throws IOException{
		if(!permissionFile.exists()) return permissionFile.createNewFile();
		return true;
	}
	
	public static boolean hasPermission(User user, Guild g, String node){
		return hasPermission(user, g, node, false);
	}
	
	public static boolean hasPermission(User user, Guild g, String node, boolean allowPrivate){
		if(!(user instanceof ConsoleUser)){
			
			if(g == null) return allowPrivate;
			
			String key = String.join(":", node, g.getId());
			
			if(getBlacklistedUsers(key) != null)
				if(getBlacklistedUsers(key).contains(user.getId())) return false;
			
			List<String> userRoleIds = new ArrayList<String>();
			g.getMember(user).getRoles().forEach(r -> userRoleIds.add(r.getId()));
			
			if(getAllowedRoles(key) != null)
				if(Collections.disjoint(userRoleIds, getAllowedRoles(node))) return false;
		}
		return true;
		
	}
	
	public static List<String> getAllowedRoles(String node){
		return permissionMap.get(node);
	}
	
	public static List<String> getBlacklistedUsers(String node){
		return blacklistMap.get(node);
	}
	
	public static boolean blacklistUser(User u, Guild g, String node) throws FileNotFoundException, IOException{
		if(g == null) throw new IllegalArgumentException();
		return writeBlacklistChange(u, g, node, true);
	}
	
	public static boolean unBlacklistUser(User u, Guild g, String node) throws FileNotFoundException, IOException{
		if(g == null) throw new IllegalArgumentException();
		return writeBlacklistChange(u, g, node, false);
	}
	
	//Untested - will be implemented as command in future commit.
	private static boolean writeBlacklistChange(User u, Guild g, String node, boolean add) throws FileNotFoundException, IOException{
		String key = String.join(":", node, g.getId());
		List<String> blacklisted = blacklistMap.get(key);
		
		if(add){
			if(blacklisted.contains(u.getId())) return false;
			blacklisted.add(u.getId());
		} else {
			if(!blacklisted.contains(u.getId())) return false;
			blacklisted.remove(u.getId());
		}
		
		blacklistMap.put(key, blacklisted);
		
		JsonParser p = new JsonParser();
		try(JsonReader file = new JsonReader(new FileReader(permissionFile))){
			JsonObject result = null;
			JsonElement parsed = p.parse(file);
			if(parsed.isJsonObject()){
				result = parsed.getAsJsonObject();
				JsonObject section = result.get(node).getAsJsonObject();
				JsonObject guild = section.get(g.getId()).getAsJsonObject();
				JsonArray arr = guild.get(PermissionUtil.BLACKLISTKEY).getAsJsonArray();
				if(add)
					arr.add(u.getId());
				else {
					for(int i=0; i<arr.size(); ++i){
					    String val = arr.get(i).getAsString();
					    if(val.equals(u.getId())){            
					        arr.remove(i);
					        break;
					    }
					}
				}
			}
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try(JsonWriter writer = gson.newJsonWriter(new FileWriter(permissionFile))){
				gson.toJson(result, writer);
			}
			
		}
		
		return true;
	}
	
	public static void loadJson() throws FileNotFoundException, IOException {
		if(!verifyFile()) return;
		log.info("Using permission file located at " + permissionFile.getAbsolutePath());
		JsonParser p = new JsonParser();
		try(JsonReader file = new JsonReader(new FileReader(permissionFile))){
			boolean needsUpdate = false;
			JsonObject result = null;
			JsonElement parsed = p.parse(file);
			List<String> nodes = new ArrayList<String>(ObsidianBot.getInstance().getCommandRegistry().getAllRegisteredNodes());
			List<Guild> expectedGuilds = ObsidianBot.getInstance().getJDA().getGuilds();
			if(parsed.isJsonObject()){
				result = parsed.getAsJsonObject();
				
				//get nodes
				for(Map.Entry<String, JsonElement> e : result.entrySet()){
					if(nodes.contains(e.getKey())) nodes.remove(e.getKey());
					
					//get guilds
					if(e.getValue().isJsonObject()){
						List<String> eGuilds = new ArrayList<String>();
						expectedGuilds.forEach(val -> eGuilds.add(val.getId()));
						for(Map.Entry<String, JsonElement> gu : e.getValue().getAsJsonObject().entrySet()){
							if(gu.getValue().isJsonObject()){
								if(eGuilds.contains(gu.getKey())) eGuilds.remove(gu.getKey());
								boolean ignorePerms = false;
								String key = String.join(":", e.getKey(), gu.getKey());
								for(Map.Entry<String, JsonElement> e2 : gu.getValue().getAsJsonObject().entrySet()){
									if(e2.getKey().equals(PermissionUtil.GATEKEY) && !e2.getValue().getAsBoolean()) {
										permissionMap.put(key, null);
										ignorePerms = true;
										continue;
									} else if(e2.getKey().equals(PermissionUtil.ALLOWEDKEY) && e2.getValue().isJsonArray() && !ignorePerms){
										JsonArray roles = e2.getValue().getAsJsonArray();
										List<String> roleIds = new ArrayList<String>();
										for(JsonElement e3 : roles){
											roleIds.add(e3.getAsString());
										}
										permissionMap.put(key, roleIds);
									} else if(e2.getKey().equals(PermissionUtil.BLACKLISTKEY) && e2.getValue().isJsonArray()){
										JsonArray blacklist = e2.getValue().getAsJsonArray();
										List<String> userIds = new ArrayList<String>();
										for(JsonElement e3 : blacklist){
											userIds.add(e3.getAsString());
										}
										blacklistMap.put(key, userIds);
									}
								}
							}
						}
						if(eGuilds.size() > 0) {
							needsUpdate = true;
							for(String leftover : eGuilds){
								JsonObject container = new JsonObject();
								container.addProperty(PermissionUtil.FRIENDLYNAME, ObsidianBot.getInstance().getJDA().getGuildById(leftover).getName());
								container.addProperty(PermissionUtil.GATEKEY, false);
								container.add(PermissionUtil.ALLOWEDKEY, new JsonArray());
								container.add(PermissionUtil.BLACKLISTKEY, new JsonArray());
								e.getValue().getAsJsonObject().add(leftover, container);
							}
						}
					}
				}
			}
			
			if(nodes.size() > 0){
				//If permissions is empty
				if(result == null) result = new JsonObject();
				
				Gson g = new GsonBuilder().setPrettyPrinting().create();
				List<String> leftover = new ArrayList<String>(nodes);
				for(String s : leftover){
					JsonObject guilds = new JsonObject();
					for(Guild guild : ObsidianBot.getInstance().getJDA().getGuilds()){
						JsonObject container = new JsonObject();
						container.addProperty(PermissionUtil.FRIENDLYNAME, guild.getName());
						container.addProperty(PermissionUtil.GATEKEY, false);
						container.add(PermissionUtil.ALLOWEDKEY, new JsonArray());
						container.add(PermissionUtil.BLACKLISTKEY, new JsonArray());
						guilds.add(guild.getId(), container);
					}
					result.add(s, guilds);
				}
				try(JsonWriter writer = g.newJsonWriter(new FileWriter(permissionFile))){
					needsUpdate = false;
					g.toJson(result, writer);
				}
			}
			
			if(needsUpdate){
				Gson g = new GsonBuilder().setPrettyPrinting().create();
				try(JsonWriter writer = g.newJsonWriter(new FileWriter(permissionFile))){
					g.toJson(result, writer);
				}
			}
		}
	}
	
}
