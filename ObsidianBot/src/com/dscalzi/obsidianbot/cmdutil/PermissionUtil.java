package com.dscalzi.obsidianbot.cmdutil;

import java.io.File;
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
	private static final SimpleLog log = SimpleLog.getLog("PermissionUtil");
	
	private static final File permissionFolder;
	private static final String permissionNameTemplate;
	private static final String guildRegex;
	private static final Map<Guild, Boolean> initialized;

	//Key is node:guildid
	private static final Map<String, List<String>> permissionMap;
	private static final Map<String, List<String>> blacklistMap;
	
	static {
		permissionFolder = new File(ObsidianBot.getDataPath(), "permissions");
		permissionNameTemplate = "perms{g}.json";
		guildRegex = "{g}";
		permissionMap = new HashMap<String, List<String>>();
		blacklistMap = new HashMap<String, List<String>>();
		initialized = new HashMap<Guild, Boolean>();
	}
	
	private static File verifyFile(String name){
		if(!permissionFolder.exists()) {
			boolean status = permissionFolder.mkdirs();
			if(!status) return null;
		}
		File target = new File(permissionFolder, name);
		if(!target.exists()) {
			boolean status;
			try {
				status = target.createNewFile();
			} catch (IOException e) {
				status = false;
			}
			return status ? target : null;
		}
		return target;
	}
	
	public static boolean isInitialized(Guild g){
		return initialized.get(g) == null ? false : initialized.get(g);
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
	
	public static String getPermFileName(Guild g){
		return permissionNameTemplate.replace(guildRegex, g.getId());
	}
	
	public static List<String> getAllowedRoles(PermissionNode node, Guild g){
		return permissionMap.get(String.join(":", node.toString(), g.getId()));
	}
	
	public static List<String> getBlacklistedUsers(PermissionNode node, Guild g){
		return blacklistMap.get(String.join(":", node.toString(), g.getId()));
	}
	
	public static Boolean permissionAdd(PermissionNode node, Guild g, Role role) throws IOException {
		return writePermissionChange(node, g, role, true);
	}
	
	public static Boolean permissionRemove(PermissionNode node, Guild g, Role role) throws IOException {
		return writePermissionChange(node, g, role, false);
	}
	
	private static Boolean writePermissionChange(PermissionNode node, Guild g, Role role, boolean add) throws IOException{
		File target = verifyFile(getPermFileName(g));
		if(target == null) throw new IOException();
		
		String key = String.join(":", node.toString(), g.getId());
		
		if(permissionMap.get(key) == null) return null;
		List<String> permissions = permissionMap.get(key);
		
		if(add ? !permissions.contains(role.getId()) : permissions.contains(role.getId())){
			if(add) permissions.add(role.getId());
			else permissions.remove(role.getId());
		} else
			return false;
		
		permissionMap.put(key, permissions);
		
		JsonParser p = new JsonParser();
		try(JsonReader file = new JsonReader(new FileReader(target))){
			JsonObject result = null;
			JsonElement parsed = p.parse(file);
			if(parsed.isJsonObject()){
				result = parsed.getAsJsonObject();
				JsonObject section = result.get(node.toString()).getAsJsonObject();
				JsonArray arr = section.get(PermissionUtil.ALLOWEDKEY).getAsJsonArray();
				if(add)
					arr.add(role.getId());
				else {
					for(int i=arr.size()-1; i>=0; --i){
					    String val = arr.get(i).getAsString();
					    if(val.equals(role.getId())){            
					        arr.remove(i);
					        break;
					    }
					}
				}
			}
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try(JsonWriter writer = gson.newJsonWriter(new FileWriter(target))){
				gson.toJson(result, writer);
			}
		}
		
		return true;
	}
	
	public static Set<Role> bulkPermissionAdd(PermissionNode node, Guild g, Set<Role> roles) throws IOException{
		return writeBulkPermissionChange(node, g, roles, true);
	}
	
	public static Set<Role> bulkPermissionRemove(PermissionNode node, Guild g, Set<Role> roles) throws IOException{
		return writeBulkPermissionChange(node, g, roles, false);
	}
	
	private static Set<Role> writeBulkPermissionChange(PermissionNode node, Guild g, Set<Role> roles, boolean add) throws IOException{
		File target = verifyFile(getPermFileName(g));
		if(target == null) throw new IOException();
		
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
		
		if(queued.size() == 0) return failed;
		
		permissionMap.put(key, permissions);
		

		JsonParser p = new JsonParser();
		try(JsonReader file = new JsonReader(new FileReader(target))){
			JsonObject result = null;
			JsonElement parsed = p.parse(file);
			if(parsed.isJsonObject()){
				result = parsed.getAsJsonObject();
				JsonObject section = result.get(node.toString()).getAsJsonObject();
				JsonArray arr = section.get(PermissionUtil.ALLOWEDKEY).getAsJsonArray();
				if(add)
					queued.forEach(r -> arr.add(r));
				else {
					for(int i=arr.size()-1; i>=0; --i){
					    String val = arr.get(i).getAsString();
					    if(queued.contains(val)){            
					        arr.remove(i);
					        queued.remove(val);
					    }
					}
				}
			}
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try(JsonWriter writer = gson.newJsonWriter(new FileWriter(target))){
				gson.toJson(result, writer);
			}
			
		}
		
		return failed;
	}
	
	public static boolean blacklistUser(User u, PermissionNode node, Guild g) throws IOException{
		if(g == null) throw new IllegalArgumentException();
		return writeBlacklistChange(u, node, g, true);
	}
	
	public static boolean unBlacklistUser(User u, PermissionNode node, Guild g) throws IOException{
		if(g == null) throw new IllegalArgumentException();
		return writeBlacklistChange(u, node, g, false);
	}
	
	private static boolean writeBlacklistChange(User u, PermissionNode node, Guild g, boolean add) throws IOException{
		File target = verifyFile(getPermFileName(g));
		if(target == null) throw new IOException();
		
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
		try(JsonReader file = new JsonReader(new FileReader(target))){
			JsonObject result = null;
			JsonElement parsed = p.parse(file);
			if(parsed.isJsonObject()){
				result = parsed.getAsJsonObject();
				JsonObject section = result.get(node.toString()).getAsJsonObject();
				JsonArray arr = section.get(PermissionUtil.BLACKLISTKEY).getAsJsonArray();
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
			try(JsonWriter writer = gson.newJsonWriter(new FileWriter(target))){
				gson.toJson(result, writer);
			}
			
		}
		
		return true;
	}
	
	public static void loadJson(Guild g) throws IOException {
		File target = verifyFile(getPermFileName(g));
		if(target == null) throw new IOException();
		
		log.info("Using permission file located at " + target.getAbsolutePath() + " for guild " + g.getName() + "(" + g.getId() + ").");
		JsonParser p = new JsonParser();
		try(JsonReader file = new JsonReader(new FileReader(target))){
			JsonObject result = null;
			JsonElement parsed = p.parse(file);
			Set<PermissionNode> registered = new HashSet<PermissionNode>(ObsidianBot.getInstance().getCommandRegistry().getAllRegisteredNodes());
			if(parsed.isJsonObject()){
				result = parsed.getAsJsonObject();
				
				//get nodes
				for(Map.Entry<String, JsonElement> e : result.entrySet()){
					PermissionNode current = PermissionNode.get(e.getKey());
					if(registered.contains(current)) registered.remove(current);
					
					if(e.getValue().isJsonObject()){
						boolean ignorePerms = false;
						String key = String.join(":", e.getKey(), g.getId());
						for(Map.Entry<String, JsonElement> e2 : e.getValue().getAsJsonObject().entrySet()){
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
			}
			
			if(registered.size() > 0){
				//If permissions is empty
				if(result == null) result = new JsonObject();
				
				Set<PermissionNode> leftover = new HashSet<PermissionNode>(registered);
				
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				for(PermissionNode pn : leftover){
					JsonObject container = new JsonObject();
					container.addProperty(PermissionUtil.GATEKEY, pn.isOp());
					container.add(PermissionUtil.ALLOWEDKEY, new JsonArray());
					container.add(PermissionUtil.BLACKLISTKEY, new JsonArray());
					result.add(pn.toString(), container);
				}
				try(JsonWriter writer = gson.newJsonWriter(new FileWriter(target))){
					gson.toJson(result, writer);
				}
			}
		}
		
		initialized.put(g, true);
		
	}
	
}
