/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.cmdutil;

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

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.console.ConsoleUser;
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
	private static final String guildRegex;
	private static final String permissionNameTemplate;
	private static final Map<Guild, Boolean> initialized;

	//Key is node:guildid
	private static final Map<String, List<String>> permissionMap;
	private static final Map<String, List<String>> blacklistMap;
	
	static {
		permissionFolder = new File(AventiBot.getDataPath(), "permissions");
		guildRegex = "{g}";
		permissionNameTemplate = "perms" + guildRegex + ".json";
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
	
	/**
	 * The following is equivalent to calling {@link #hasPermission(User, PermissionNode, Guild, boolean) PermissionUtil.hasPermission(user, node, g, false)}
	 */
	public static boolean hasPermission(User user, PermissionNode node, Guild g){
		return hasPermission(user, node, g, false);
	}
	
	/**
	 * A user only has permission for the specified node in the specified guild if all of the following are true.
	 * 
	 * <ul>
	 * <li>The user is <strong>not blacklisted</strong> from the node.</li>
	 * <li>The user belongs to <strong>at least one</strong> role that has permission for the node <strong>if it requires permission</strong>.</li>
	 * </ul>
	 * 
	 * <em>If the guild is null (ie private channel), a user only has permission if the parameter allowPrivate is true.</em>
	 * 
	 * @param user The user to check permission on.
	 * @param node The permission node to check permission on.
	 * @param g The guild in which permission is being checked.
	 * @param allowPrivate If this command is allowed in private chat (permissions do not exist for private).
	 * @return True if all of the above criteria is met, false otherwise.
	 */
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
	
	/**
	 * Add the specified role to the specified Permission Node, granting permission to that role to use the command
	 * that the node represents. 
	 * 
	 * @param node Permission Node to add a role to.
	 * @param g Guild whose permissions will be changed.
	 * @param role Role to add to the specified Permission Node.
	 * @return True if successful, that is if the role was not previously allowed for the node and it was successfully
	 * added. False if unsuccessful. Null if the Permission Node given is invalid or does not require permission.
	 * @throws IOException If there was an issue writing the changes to the permission file.
	 */
	public static Boolean permissionAdd(PermissionNode node, Guild g, Role role) throws IOException {
		return writePermissionChange(node, g, role, true);
	}
	
	/**
	 * Removes the specified role from the specified Permission Node, revoking permission from that role to use the 
	 * command that the node represents. 
	 * 
	 * @param node Permission Node to remove a role to.
	 * @param g Guild whose permissions will be changed.
	 * @param role Role to remove from the specified Permission Node.
	 * @return True if successful, that is if the role was previously allowed for the node and it was successfully
	 * removed. False if unsuccessful. Null if the Permission Node given is invalid or does not require permission.
	 * @throws IOException If there was an issue writing the changes to the permission file.
	 */
	public static Boolean permissionRemove(PermissionNode node, Guild g, Role role) throws IOException {
		return writePermissionChange(node, g, role, false);
	}
	
	/**
	 * Not safe for use outside of PermissionUtil class.
	 * <br>
	 * If adding permission, see {@link #permissionAdd(PermissionNode, Guild, Role)}
	 * <br>
	 * If removing permission, see {@link #permissionRemove(PermissionNode, Guild, Role)}
	 */
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
	
	/**
	 * Adds a set of roles to the specified permission node, effectively granting permission to those roles to
	 * use the command that the node represents. 
	 * 
	 * @param node Permission Node to add the roles to.
	 * @param g Guild whose permissions will be changed.
	 * @param roles A set of roles to add to the specified node.
	 * @return A set of roles which failed the operation. A role fails if it was already added to the specified node. 
	 * A set of size zero indicates complete success. Returns null if the Permission Node was invalid or did not require
	 * permission.
	 * @throws IOException If there was an issue writing the changes to the permission file.
	 */
	public static Set<Role> bulkPermissionAdd(PermissionNode node, Guild g, Set<Role> roles) throws IOException{
		return writeBulkPermissionChange(node, g, roles, true);
	}
	
	/**
	 * Removes a set of roles from the specified permission node, effectively revoking permission from those roles to
	 * use the command that the node represents. 
	 * 
	 * @param node Permission Node to remove the roles from.
	 * @param g Guild whose permissions will be changed.
	 * @param roles A set of roles to remove from the specified node.
	 * @return A set of roles which failed the operation. A role fails if it was not already added to the specified node. 
	 * A set of size zero indicates complete success. Returns null if the Permission Node was invalid or did not require
	 * permission.
	 * @throws IOException If there was an issue writing the changes to the permission file.
	 */
	public static Set<Role> bulkPermissionRemove(PermissionNode node, Guild g, Set<Role> roles) throws IOException{
		return writeBulkPermissionChange(node, g, roles, false);
	}
	
	/**
	 * Not safe for use outside of PermissionUtil class.
	 * <br>
	 * If adding permission, see {@link #bulkPermissionAdd(PermissionNode, Guild, Set)}
	 * <br>
	 * If removing permission, see {@link #bulkPermissionRemove(PermissionNode, Guild, Set)}
	 */
	private static Set<Role> writeBulkPermissionChange(PermissionNode node, Guild g, Set<Role> roles, boolean add) throws IOException{
		File target = verifyFile(getPermFileName(g));
		if(target == null) throw new IOException();
		
		Set<Role> failed = new HashSet<Role>();
		
		String key = String.join(":", node.toString(), g.getId());
		if(permissionMap.get(key) == null) return null;
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
	
	/**
	 * Grants a given role permission to the specified set of nodes in the specified guild. This will effectively
	 * grant that role permission to use the commands the nodes represent.
	 * 
	 * @param r Role to grant permissions to.
	 * @param g Guild whose permissions will be changed.
	 * @param nodes A set of nodes to grant to the specified role.
	 * @return A set of Permission Nodes that failed the operation. A node fails if the specified role already was
	 * granted permission for it. A set of size zero indicates complete success.
	 * @throws IOException If there was an issue writing the changes to the permission file.
	 */
	public static Set<PermissionNode> bulkPermissionGrant(Role r, Guild g, Set<PermissionNode> nodes) throws IOException{
		return writeBulkNodeChange(r, g, nodes, true);
	}
	
	/**
	 * Revokes permission for the specified set of nodes for a given role in the specified guild. This will effectively
	 * revoke permission from that role to use the commands the nodes represent.
	 * 
	 * @param r Role to revoke permissions from.
	 * @param g Guild whose permissions will be changed.
	 * @param nodes A set of nodes to revoke from the specified role.
	 * @return A set of Permission Nodes that failed the operation. A node fails if the specified role was already not
	 * granted permission for it. A set of size zero indicates complete success.
	 * @throws IOException If there was an issue writing the changes to the permission file.
	 */
	public static Set<PermissionNode> bulkPermissionRevoke(Role r, Guild g, Set<PermissionNode> nodes) throws IOException{
		return writeBulkNodeChange(r, g, nodes, false);
	}
	
	/**
	 * Not safe for use outside of PermissionUtil class.
	 * <br>
	 * If adding permission, see {@link #bulkPermissionGrant(Role, Guild, Set)}
	 * <br>
	 * If removing permission, see {@link #bulkPermissionRevoke(Role, Guild, Set)}
	 */
	private static Set<PermissionNode> writeBulkNodeChange(Role r, Guild g, Set<PermissionNode> nodes, boolean add) throws IOException{
		
		File target = verifyFile(getPermFileName(g));
		if(target == null) throw new IOException();
		
		Set<PermissionNode> failed = new HashSet<PermissionNode>();
		Set<PermissionNode> queued = new HashSet<PermissionNode>();
		
		for(PermissionNode n : nodes){
			String key = String.join(":", n.toString(), g.getId());
			
			if(permissionMap.get(key) == null) {
				failed.add(n);
				continue;
			}
			List<String> permissions = permissionMap.get(key);
			if(add ? !permissions.contains(r.getId()) : permissions.contains(r.getId())){
				if(add)	permissions.add(r.getId());
				else permissions.remove(r.getId());
				queued.add(n);
				permissionMap.put(key, permissions);
			} else {
				failed.add(n);
			}
		}
		
		if(queued.size() == 0) return failed;
		
		JsonParser p = new JsonParser();
		try(JsonReader file = new JsonReader(new FileReader(target))){
			JsonObject result = null;
			JsonElement parsed = p.parse(file);
			if(parsed.isJsonObject()){
				result = parsed.getAsJsonObject();
				for(PermissionNode n : queued){
					JsonObject section = result.get(n.toString()).getAsJsonObject();
					JsonArray arr = section.get(PermissionUtil.ALLOWEDKEY).getAsJsonArray();
					if(add)
						arr.add(r.getId());
					else {
						inner:
						for(int i=arr.size()-1; i>=0; --i){
						    String val = arr.get(i).getAsString();
						    if(val.equals(r.getId())){
						    	arr.remove(i);
						    	break inner;
						    }
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
			Set<PermissionNode> registered = new HashSet<PermissionNode>(AventiBot.getInstance().getCommandRegistry().getAllRegisteredNodes());
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
