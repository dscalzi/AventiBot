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
import com.dscalzi.obsidianbot.console.Console.ConsoleUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public final class PermissionUtil {
	
	private static final String ALLOWEDKEY = "allowedRoles";
	private static final String BLACKLISTKEY = "blacklistedUsers";
	private static final String GATEKEY = "requiresPermission";
	
	private static final File permissionFile;

	private static final Map<String, List<String>> permissionMap;
	private static final Map<String, List<String>> blacklistMap;
	
	static {
		permissionFile = new File(System.getProperty("user.dir"), "permissions.json");
		permissionMap = new HashMap<String, List<String>>();
		blacklistMap = new HashMap<String, List<String>>();
	}
	
	private static boolean verifyFile() throws IOException{
		if(!permissionFile.exists()) return permissionFile.createNewFile();
		return true;
	}
	
	public static boolean hasPermission(User user, String node){
		if(!(user instanceof ConsoleUser)){
			if(!ObsidianBot.getInstance().getGuild().isMember(user)) return false;
			
			if(getBlacklistedUsers(node) != null)
				if(getBlacklistedUsers(node).contains(user.getId())) return false;
			
			Member m = ObsidianBot.getInstance().getGuild().getMember(user);
			
			List<String> userRoleIds = new ArrayList<String>();
			m.getRoles().forEach(r -> userRoleIds.add(r.getId()));
			
			if(getAllowedRoles(node) != null)
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
	
	public static void loadJson() throws FileNotFoundException, IOException {
		if(!verifyFile()) return;
		JsonParser p = new JsonParser();
		try(JsonReader file = new JsonReader(new FileReader(permissionFile))){
			JsonObject result = null;
			JsonElement parsed = p.parse(file);
			List<String> nodes = new ArrayList<String>(ObsidianBot.getInstance().getCommandRegistry().getAllRegisteredNodes());
			if(parsed.isJsonObject()){
				result = parsed.getAsJsonObject();
				for(Map.Entry<String, JsonElement> e : result.entrySet()){
					if(nodes.contains(e.getKey())) nodes.remove(e.getKey());
					if(e.getValue().isJsonObject()){
						boolean ignorePerms = false;
						for(Map.Entry<String, JsonElement> e2 : e.getValue().getAsJsonObject().entrySet()){
							if(e2.getKey().equals(PermissionUtil.GATEKEY) && !e2.getValue().getAsBoolean()) {
								permissionMap.put(e.getKey(), null);
								ignorePerms = true;
								continue;
							} else if(e2.getKey().equals(PermissionUtil.ALLOWEDKEY) && e2.getValue().isJsonArray() && !ignorePerms){
								JsonArray roles = e2.getValue().getAsJsonArray();
								List<String> roleIds = new ArrayList<String>();
								for(JsonElement e3 : roles){
									roleIds.add(e3.getAsString());
								}
								permissionMap.put(e.getKey(), roleIds);
							} else if(e2.getKey().equals(PermissionUtil.BLACKLISTKEY) && e2.getValue().isJsonArray()){
								JsonArray blacklist = e2.getValue().getAsJsonArray();
								List<String> userIds = new ArrayList<String>();
								for(JsonElement e3 : blacklist){
									userIds.add(e3.getAsString());
								}
								blacklistMap.put(e.getKey(), userIds);
							}
						}
					}
				}
			}
			
			if(nodes.size() == 0) return;
			
			//If permissions is empty
			if(result == null) result = new JsonObject();
			
			Gson g = new GsonBuilder().setPrettyPrinting().create();
			List<String> leftover = new ArrayList<String>(nodes);
			for(String s : leftover){
				JsonObject container = new JsonObject();
				container.addProperty(PermissionUtil.GATEKEY, false);
				container.add(PermissionUtil.ALLOWEDKEY, new JsonArray());
				container.add(PermissionUtil.BLACKLISTKEY, new JsonArray());
				result.add(s, container);
			}
			try(JsonWriter writer = g.newJsonWriter(new FileWriter(permissionFile))){
				g.toJson(result, writer);
			}
		}
	}
	
}
