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
import com.dscalzi.aventibot.settings.SettingsManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javafx.util.Pair;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.SimpleLog;

public final class PermissionUtil {
	
	private static final String ALLOWEDKEY = "allowedRoles";
	private static final String BLACKLISTKEY = "blacklistedUsers";
	private static final String GATEKEY = "requiresPermission";
	private static final SimpleLog log = SimpleLog.getLog("PermissionUtil");
	
	private static final Map<Guild, Boolean> initialized;

	private static final Map<String, Map<String, List<String>>> permissionMap;
	private static final Map<String, Map<String, List<String>>> blacklistMap;
	
	static {
		permissionMap = new HashMap<String, Map<String, List<String>>>();
		blacklistMap = new HashMap<String, Map<String, List<String>>>();
		initialized = new HashMap<Guild, Boolean>();
	}
	
	public static boolean isInitialized(Guild g){
		return initialized.get(g) == null ? false : initialized.get(g);
	}
	
	public static void reload(Guild g){
		initialized.remove(g);
	}
	
	public static void reload(){
		initialized.clear();
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
	
	public static List<String> getAllowedRoles(PermissionNode node, Guild g){
		return permissionMap.get(g.getId()).get(node.toString());
	}
	
	public static List<String> getBlacklistedUsers(PermissionNode node, Guild g){
		return blacklistMap.get(g.getId()).get(node.toString());
	}
	
	public static Set<PermissionNode> getNodesForRole(Role r, Guild g){
		Map<String, List<String>> perms = permissionMap.get(g.getId());
		Set<PermissionNode> registered = new HashSet<PermissionNode>(AventiBot.getInstance().getCommandRegistry().getAllRegisteredNodes());
		String id = r.getId();
		
		outer:
		for(Map.Entry<String, List<String>> entry : perms.entrySet()){
			for(String s : entry.getValue()){
				if(s.equals(id)){
					continue outer;
				}
			}
			registered.remove(PermissionNode.get(entry.getKey()));
		}
		
		return registered;
	}
	
	/**
	 * Validate that a set of nodes are actually registered, valid nodes.
	 * 
	 * @param nodes A set of nodes to validate.
	 * @return A pair whose key is a set of validate PermissionNodes and whose value is a set of invalid nodes.
	 */
	public static Pair<Set<PermissionNode>, Set<String>> validateNodes(Set<String> nodes){
		Set<PermissionNode> valids = new HashSet<PermissionNode>();
		Set<String> invalids = new HashSet<String>();
		Set<PermissionNode> known = AventiBot.getInstance().getCommandRegistry().getAllRegisteredNodes();
		
		for(String s : nodes){
			PermissionNode n = PermissionNode.get(s);
			if(known.contains(n)){
				known.remove(n);
				valids.add(n);
			} else {
				invalids.add(n.toString());
			}
		}
		return new Pair<Set<PermissionNode>, Set<String>>(valids, invalids);
	}
	
	/**
	 * Update permissions. Parameters must be validated.
	 * 
	 * @param g The target guild.
	 * @param roles A set of roles to update.
	 * @param nodes A Set of nodes to update.
	 * @param add If the nodes should be granted to or revoked from the roles.
	 * @return A PermissionResult object with details about the result.
	 * @throws IOException If there was an issue writing the changes to the permission file.
	 */
	public static PermissionResult writePermissionChange(Guild g, Set<Role> roles, Set<PermissionNode> nodes, boolean add) throws IOException{
		File target = SettingsManager.getPermissionFile(g);
		if(target == null) throw new IOException();
		
		PermissionResult result = new PermissionResult(add ? PermissionResult.Type.GRANT : PermissionResult.Type.REVOKE, g);
		
		Map<String, List<String>> perms = permissionMap.get(g.getId());
		Map<String, List<String>> queue = new HashMap<String, List<String>>();
		for(PermissionNode n : nodes){
			List<String> rP = perms.get(n.toString());
			if(rP == null){
				result.addFailedNode(n.toString()); //Node doesn't require permission.
				continue;
			}
			result.addNode(n.toString()); //Record it as processed
			List<String> q = new ArrayList<String>();
			for(Role r : roles){
				if((add && rP.contains(r.getId())) || (!add && !rP.contains(r.getId()))){
					result.logResult("'ERR \"" + n.toString() + "\" already " + (add ? "granted to" : "revoked from") + " \"" + r.getName() + "\"(" + r.getId() + ").");
					continue;
				}
				rP.add(r.getId());
				q.add(r.getId());
			}
			if(q.size() > 0) queue.put(n.toString(), q);
		}
		
		//Write the changes in queue map.
		if(queue.size() == 0) return result;

		JsonParser p = new JsonParser();
		try(JsonReader file = new JsonReader(new FileReader(target))){
			JsonObject job = null;
			JsonElement parsed = p.parse(file);
			if(parsed.isJsonObject()){
				job = parsed.getAsJsonObject();
				for(Map.Entry<String, List<String>> entry : queue.entrySet()){
					JsonObject section = job.get(entry.getKey()).getAsJsonObject();
					JsonArray arr = section.get(PermissionUtil.ALLOWEDKEY).getAsJsonArray();
					if(add)
						entry.getValue().forEach(r -> arr.add(r));
					else {
						for(int i=arr.size()-1; i>=0; --i){
						    String val = arr.get(i).getAsString();
						    if(entry.getValue().contains(val)){            
						        arr.remove(i);
						        entry.getValue().remove(val);
						    }
						}
					}
				}
			}
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try(JsonWriter writer = gson.newJsonWriter(new FileWriter(target))){
				gson.toJson(job, writer);
			}
			
		}
		
		return result;
	}
	
	/**
	 * Update the blacklist. Parameters must be validated.
	 * 
	 * @param g The target guild.
	 * @param users A set of target users.
	 * @param nodes A set of nodes to update.
	 * @param add If the users should be added or removed from the blacklist.
	 * @return A PermissionResult object with details about the result.
	 * @throws IOException If there was an issue writing the changes to the permission file.
	 */
	public static PermissionResult writeBlacklistChangeNew(Guild g, Set<User> users, Set<PermissionNode> nodes, boolean add) throws IOException {
		File target = SettingsManager.getPermissionFile(g);
		if(target == null) throw new IOException();
		
		PermissionResult result = new PermissionResult(add ? PermissionResult.Type.BLACKLIST : PermissionResult.Type.UNBLACKLIST, g);
		
		Map<String, List<String>> blacklist = blacklistMap.get(g.getId());
		Map<String, List<String>> queue = new HashMap<String, List<String>>();
		
		for(PermissionNode n : nodes){
			List<String> rP = blacklist.get(n.toString());
			result.addNode(n.toString()); //Record it as processed
			List<String> q = new ArrayList<String>();
			for(User u : users){
				String id = u.getId();
				if((add && rP.contains(id)) || (!add && !rP.contains(id))){
					result.logResult("'ERR \"" + n.toString() + "\" " + (add ? "already blacklists" : "does not blacklist") + " \"" + u.getName() + "#" + u.getDiscriminator() + "\"(" + id + ").");
					continue;
				}
				rP.add(id);
				q.add(id);
			}
			if(q.size() > 0) queue.put(n.toString(), q);
		}
		
		//Write the changes in queue map.
		if(queue.size() == 0) return result;

		JsonParser p = new JsonParser();
		try(JsonReader file = new JsonReader(new FileReader(target))){
			JsonObject job = null;
			JsonElement parsed = p.parse(file);
			if(parsed.isJsonObject()){
				job = parsed.getAsJsonObject();
				for(Map.Entry<String, List<String>> entry : queue.entrySet()){
					JsonObject section = job.get(entry.getKey()).getAsJsonObject();
					JsonArray arr = section.get(PermissionUtil.BLACKLISTKEY).getAsJsonArray();
					if(add)
						entry.getValue().forEach(r -> arr.add(r));
					else {
						for(int i=arr.size()-1; i>=0; --i){
						    String val = arr.get(i).getAsString();
						    if(entry.getValue().contains(val)){            
						        arr.remove(i);
						        entry.getValue().remove(val);
						    }
						}
					}
				}
			}
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try(JsonWriter writer = gson.newJsonWriter(new FileWriter(target))){
				gson.toJson(job, writer);
			}
			
		}
		
		return result;
	}
	
	public static void loadJson(Guild g) throws IOException {
		File target = SettingsManager.getPermissionFile(g);
		if(target == null) throw new IOException();
		
		log.info("Using permission file located at " + target.getAbsolutePath() + " for guild " + g.getName() + "(" + g.getId() + ").");
		Map<String, List<String>> perms = new HashMap<String, List<String>>();
		Map<String, List<String>> blist = new HashMap<String, List<String>>();
		
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
						for(Map.Entry<String, JsonElement> e2 : e.getValue().getAsJsonObject().entrySet()){
							if(e2.getKey().equals(PermissionUtil.GATEKEY) && !e2.getValue().getAsBoolean()) {
								perms.put(e.getKey(), null);
								ignorePerms = true;
								continue;
							} else if(e2.getKey().equals(PermissionUtil.ALLOWEDKEY) && e2.getValue().isJsonArray() && !ignorePerms){
								JsonArray roles = e2.getValue().getAsJsonArray();
								List<String> roleIds = new ArrayList<String>();
								for(JsonElement e3 : roles){
									roleIds.add(e3.getAsString());
								}
								perms.put(e.getKey(), roleIds);
							} else if(e2.getKey().equals(PermissionUtil.BLACKLISTKEY) && e2.getValue().isJsonArray()){
								JsonArray blacklist = e2.getValue().getAsJsonArray();
								List<String> userIds = new ArrayList<String>();
								for(JsonElement e3 : blacklist){
									userIds.add(e3.getAsString());
								}
								blist.put(e.getKey(), userIds);
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
					if(pn.isOp()) perms.put(pn.toString(), new ArrayList<String>());
				}
				try(JsonWriter writer = gson.newJsonWriter(new FileWriter(target))){
					gson.toJson(result, writer);
				}
			}
		}
		
		permissionMap.put(g.getId(), perms);
		blacklistMap.put(g.getId(), blist);
		initialized.put(g, true);
		
	}
	
}
