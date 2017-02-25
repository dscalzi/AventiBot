package com.dscalzi.obsidianbot.cmdutil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import net.dv8tion.jda.core.entities.Role;
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
	
	public static boolean hasPermission(User user, PermissionNode node, Guild g){
		return hasPermission(user, node, g, false);
	}
	
	public static boolean hasPermission(User user, PermissionNode node, Guild g, boolean allowPrivate){
		if(!(user instanceof ConsoleUser)){
			
			if(g == null) return allowPrivate;
			
			if(getBlacklistedUsers(node, g) != null)
				if(getBlacklistedUsers(node, g).contains(user.getId())) return false;
			
			List<String> userRoleIds = new ArrayList<String>();
			g.getMember(user).getRoles().forEach(r -> userRoleIds.add(r.getId()));
			
			if(getAllowedRoles(node, g) != null)
				if(Collections.disjoint(userRoleIds, getAllowedRoles(node, g))) return false;
		}
		return true;
		
	}
	
	public static List<String> getAllowedRoles(PermissionNode node, Guild g){
		return permissionMap.get(String.join(":", node.toString(), g.getId()));
	}
	
	public static List<String> getBlacklistedUsers(PermissionNode node, Guild g){
		return blacklistMap.get(String.join(":", node.toString(), g.getId()));
	}
	
	public static Set<Role> bulkPermissionAdd(PermissionNode node, Guild g, Set<Role> roles) throws FileNotFoundException, IOException{
		return writeBulkPermissionChange(node, g, true, roles);
	}
	
	public static Set<Role> bulkPermissionRemove(PermissionNode node, Guild g, Set<Role> roles) throws FileNotFoundException, IOException{
		return writeBulkPermissionChange(node, g, false, roles);
	}
	
	private static Set<Role> writeBulkPermissionChange(PermissionNode node, Guild g, boolean add, Set<Role> roles) throws FileNotFoundException, IOException{
		Set<Role> failed = new HashSet<Role>();
		
		String key = String.join(":", node.toString(), g.getId());
		if(permissionMap.containsKey(key)) if(permissionMap.get(key) == null) return null;
		List<String> permissions = permissionMap.get(key);
		
		Set<String> queued = new HashSet<String>();
		
		for(Role r : roles){
			if(add ? !permissions.contains(r.getId()) : permissions.contains(r.getId())){
				queued.add(r.getId());
				if(add) permissions.add(r.getId());
				else permissions.remove(r.getId());
			} else {
				failed.add(r);
			}
		}
		
		permissionMap.put(key, permissions);
		

		JsonParser p = new JsonParser();
		try(JsonReader file = new JsonReader(new FileReader(permissionFile))){
			JsonObject result = null;
			JsonElement parsed = p.parse(file);
			if(parsed.isJsonObject()){
				result = parsed.getAsJsonObject();
				JsonObject section = result.get(node.toString()).getAsJsonObject();
				JsonObject guild = section.get(g.getId()).getAsJsonObject();
				JsonArray arr = guild.get(PermissionUtil.ALLOWEDKEY).getAsJsonArray();
				if(add)
					queued.forEach(r -> arr.add(r));
				else {
					for(int i=0; i<arr.size(); ++i){
					    String val = arr.get(i).getAsString();
					    if(queued.contains(val)){            
					        arr.remove(i);
					        queued.remove(val);
					        break;
					    }
					}
				}
			}
		}
		
		return failed;
	}
	
	public static boolean blacklistUser(User u, PermissionNode node, Guild g) throws FileNotFoundException, IOException{
		if(g == null) throw new IllegalArgumentException();
		return writeBlacklistChange(u, node, g, true);
	}
	
	public static boolean unBlacklistUser(User u, PermissionNode node, Guild g) throws FileNotFoundException, IOException{
		if(g == null) throw new IllegalArgumentException();
		return writeBlacklistChange(u, node, g, false);
	}
	
	private static boolean writeBlacklistChange(User u, PermissionNode node, Guild g, boolean add) throws FileNotFoundException, IOException{
		String key = String.join(":", node.toString(), g.getId());
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
				JsonObject section = result.get(node.toString()).getAsJsonObject();
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
			Set<String> stringNodes = new HashSet<String>();
			ObsidianBot.getInstance().getCommandRegistry().getAllRegisteredNodes().forEach(pn -> stringNodes.add(pn.toString()));
			List<Guild> expectedGuilds = ObsidianBot.getInstance().getJDA().getGuilds();
			if(parsed.isJsonObject()){
				result = parsed.getAsJsonObject();
				
				//get nodes
				for(Map.Entry<String, JsonElement> e : result.entrySet()){
					if(stringNodes.contains(e.getKey())) stringNodes.remove(e.getKey());
					
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
			
			if(stringNodes.size() > 0){
				//If permissions is empty
				if(result == null) result = new JsonObject();
				
				Gson g = new GsonBuilder().setPrettyPrinting().create();
				Set<String> leftover = new HashSet<String>(stringNodes);
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
