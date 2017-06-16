/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.cmdutil.PermissionResult;
import com.dscalzi.aventibot.util.IconUtil;
import com.dscalzi.aventibot.util.InputUtils;

import javafx.util.Pair;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class CmdPermissionsControl implements CommandExecutor{
	
	private static final char BULLET = (char)8226;
	private static final char ANGLE = (char)8735;
	
	private final PermissionNode permGrant = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "grant");
	private final PermissionNode permRevoke = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "revoke");
	private final PermissionNode permBlacklist = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "blacklist");
	private final PermissionNode permUnblacklist = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "unblacklist");
	private final PermissionNode permEnable = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "enable");
	private final PermissionNode permDisable = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "disable");
	private final PermissionNode permInfo = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "info");
	private final PermissionNode permUserInfo = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "userinfo");
	private final PermissionNode permRoleInfo = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "roleinfo");
	
	public final Set<PermissionNode> nodes;
	
	public CmdPermissionsControl(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
				permGrant,
				permRevoke,
				permBlacklist,
				permUnblacklist,
				permEnable,
				permDisable,
				permInfo,
				permUserInfo,
				permRoleInfo
				));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(args.length == 0){
			e.getChannel().sendMessage(constructSubcommandTree(e.getGuild())).queue();
			return CommandResult.ERROR;
		}
		
		String sub = args[0].toLowerCase();
		
		switch(sub){
		case "grant":
			return this.cmdWritePermissionChange(e, sub, true, rawArgs);
		case "revoke":
			return this.cmdWritePermissionChange(e, sub, false, rawArgs);
		case "blacklist":
			return this.cmdWriteBlacklistChange(e, sub, true, rawArgs);
		case "unblacklist":
			return this.cmdWriteBlacklistChange(e, sub, false, rawArgs);
		case "enable":
			return this.cmdWriteNodePermissionChange(e, sub, true, rawArgs);
		case "disable":
			return this.cmdWriteNodePermissionChange(e, sub, false, rawArgs);
		case "info":
			return this.cmdInfo(e, rawArgs);
		case "userinfo":
			return this.cmdUserInfo(e, rawArgs);
		case "roleinfo":
			return this.cmdRoleInfo(e, rawArgs);
		}
		
		e.getChannel().sendMessage("Unknown subcommand: *" + args[0] + "*.").queue();
		return CommandResult.ERROR;
	}
	
	private CommandResult cmdWritePermissionChange(MessageReceivedEvent e, String label, boolean add, String[] rawArgs){
		if(!PermissionUtil.hasPermission(e.getAuthor(), add ? permGrant : permRevoke, e.getGuild(), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		String[] terms = new String[rawArgs.length-1];
		for(int i=0; i<terms.length; ++i) terms[i] = rawArgs[i+1];
		if(rawArgs.length < 3) return cmdWritePermissionFormat(e, label, add);
		
		Pair<String[], String[]> ps = partition(terms, add ? "to" : "from");
		if(ps == null) return cmdWritePermissionFormat(e, label, add);
			
		Pair<Set<Role>,Set<String>> roles = InputUtils.parseBulkRoles(ps.getValue(), e.getGuild());
		Pair<Set<PermissionNode>,Set<String>> nodes = PermissionUtil.validateNodes(new HashSet<String>(Arrays.asList(ps.getKey())));
		try {
			PermissionResult r = PermissionUtil.writePermissionChange(e.getGuild(), roles.getKey(), nodes.getKey(), add);
			for(String s : roles.getValue()) r.addInvalidRole(s);
			for(String s : nodes.getValue()) r.addInvalidNode(s);
			for(Role ro : roles.getKey()) r.addMentionable(ro);
			e.getChannel().sendMessage(r.construct(true)).queue();
			if(r.hasLog()){
				List<String> log = r.constructLog();
				for(String s : log) e.getChannel().sendMessage(s).queue();
			}
		} catch (IOException e1) {
			e.getChannel().sendMessage("Unexpected error, operation failed").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		}
		
		return CommandResult.SUCCESS;
	}
	
	private CommandResult cmdWritePermissionFormat(MessageReceivedEvent e, String label, boolean add){
		e.getChannel().sendMessage("Proper format is `" + SettingsManager.getCommandPrefix(e.getGuild()) + "permissions " + label + " <node(s)> " + (add ? "to" : "from") + " <role(s)>`").queue();
		return CommandResult.ERROR;
	}
	
	private CommandResult cmdWriteBlacklistChange(MessageReceivedEvent e, String label, boolean add, String[] rawArgs){
		if(!PermissionUtil.hasPermission(e.getAuthor(), add ? permBlacklist : permUnblacklist, e.getGuild(), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		String[] terms = new String[rawArgs.length-1];
		for(int i=0; i<terms.length; ++i) terms[i] = rawArgs[i+1];
		if(rawArgs.length < 3) return cmdWriteBlacklistFormat(e, label, add);
		
		Pair<String[], String[]> ps = partition(terms, "for");
		if(ps == null) return cmdWriteBlacklistFormat(e, label, add);
		
		Pair<Set<User>,Set<String>> users = InputUtils.parseBulkMembers(ps.getValue(), e.getGuild());
		Pair<Set<PermissionNode>,Set<String>> nodes = PermissionUtil.validateNodes(new HashSet<String>(Arrays.asList(ps.getKey())));
		
		try {
			PermissionResult r = PermissionUtil.writeBlacklistChange(e.getGuild(), users.getKey(), nodes.getKey(), add);
			for(String s : users.getValue()) r.addInvalidUser(s);
			for(String s : nodes.getValue()) r.addInvalidNode(s);
			for(User u : users.getKey()) r.addMentionable(u);
			e.getChannel().sendMessage(r.construct(true)).queue();
			if(r.hasLog()){
				List<String> log = r.constructLog();
				for(String s : log) e.getChannel().sendMessage(s).queue();
			}
		} catch (IOException e1) {
			e.getChannel().sendMessage("Unexpected error, operation failed").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		}
		
		return CommandResult.SUCCESS;
	}
	
	private CommandResult cmdWriteBlacklistFormat(MessageReceivedEvent e, String label, boolean add){
		e.getChannel().sendMessage("Proper format is `" + SettingsManager.getCommandPrefix(e.getGuild()) + "permissions " + label + " <node(s)> for <users(s)>`").queue();
		return CommandResult.ERROR;
	}
	
	private Pair<String[], String[]> partition(String[] args, String term){
		for(int i=0; i<args.length; ++i){
			if(args[i].equalsIgnoreCase(term)){
				String[] first = new String[i];
				String[] second = new String[args.length-i-1];
				for(int k=0,g=0; k<args.length; ++k){
					if(k<i){
						first[k] = args[k];
					} else if(k>i){
						second[g] = args[k];
						++g;
					}
				}
				return new Pair<String[], String[]>(first, second);
			}
		}
		return null;
	}
	
	private CommandResult cmdWriteNodePermissionChange(MessageReceivedEvent e, String label, boolean enable, String[] rawArgs){
		if(!PermissionUtil.hasPermission(e.getAuthor(), enable ? permEnable : permDisable, e.getGuild(), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		String[] terms = new String[rawArgs.length-1];
		for(int i=0; i<terms.length; ++i) terms[i] = rawArgs[i+1];
		if(rawArgs.length < 2){
			e.getChannel().sendMessage("Proper format is `" + SettingsManager.getCommandPrefix(e.getGuild()) + "permissions " + label + " <node(s)>`").queue();
			return CommandResult.ERROR;
		}
		
		Pair<Set<PermissionNode>,Set<String>> nodes = PermissionUtil.validateNodes(new HashSet<String>(Arrays.asList(terms)));
		
		try {
			PermissionResult r = PermissionUtil.writeNodeChange(e.getGuild(), nodes.getKey(), enable);
			for(String s : nodes.getValue()) r.addInvalidNode(s);
			e.getChannel().sendMessage(r.construct(true)).queue();
			if(r.hasLog()){
				List<String> log = r.constructLog();
				for(String s : log) e.getChannel().sendMessage(s).queue();
			}
		} catch (IOException e1) {
			e.getChannel().sendMessage("Unexpected error, operation failed").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		}
		
		return CommandResult.SUCCESS;
	}
	
	private CommandResult cmdInfo(MessageReceivedEvent e, String[] rawArgs){
		if(!PermissionUtil.hasPermission(e.getAuthor(), permInfo, e.getGuild(), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		if(rawArgs.length < 2){
			e.getChannel().sendMessage("Proper format is `" + SettingsManager.getCommandPrefix(e.getGuild()) + "permissions info <node>`").queue();
			return CommandResult.ERROR;
		}
		
		String node = rawArgs[1];
		if(PermissionUtil.validateSingleNode(node)){
			PermissionNode n = PermissionNode.get(node);
			EmbedBuilder eb = new EmbedBuilder();
			
			eb.setAuthor(node, null, IconUtil.INFO.getURL());
			eb.setColor(SettingsManager.getColorAWT(e.getGuild()));
			
			List<String> roles = PermissionUtil.getAllowedRoles(n, e.getGuild());
			List<String> blacklisted = PermissionUtil.getBlacklistedUsers(n, e.getGuild());
			
			eb.setDescription(BULLET + " " + (roles == null ? "`Does not require`" : "`Requires`") + " permission.\n"
					+ (roles != null ? "  " + ANGLE + " Currently `" + roles.size() + "` role" + (roles.size() == 1 ? "" : "s") + " allowed.\n" : "")
					+ BULLET + " Currently `" + blacklisted.size() + "` blacklisted user" + (blacklisted.size() == 1 ? "" : "s") + ".");
			
			if(roles != null && roles.size() > 0){
				String roleStr = "[";
				for(String s : roles) roleStr += "<@&" + s + ">, ";
				if(roleStr.length() >= 2) roleStr = roleStr.substring(0, roleStr.length()-2) + "]";
				eb.addField("Allowed Roles", roleStr, true);
			}
			
			if(blacklisted.size() > 0){
				String blStr = "[";
				for(String s : blacklisted) blStr += "<@" + s + ">, ";
				if(blStr.length() >= 2) blStr = blStr.substring(0, blStr.length()-2) + "]";
				eb.addField("Blacklisted Users", blStr, true);
			}
			
			e.getChannel().sendMessage(eb.build()).queue();
			return CommandResult.SUCCESS;
		} else {
			e.getChannel().sendMessage("Invalid permission node: `" + node + "`").queue();
			return CommandResult.ERROR;
		}
	}
	
	private CommandResult cmdUserInfo(MessageReceivedEvent e, String[] rawArgs){
		if(!PermissionUtil.hasPermission(e.getAuthor(), permUserInfo, e.getGuild(), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		if(rawArgs.length < 2){
			e.getChannel().sendMessage("Proper format is `" + SettingsManager.getCommandPrefix(e.getGuild()) + "permissions userinfo <user>`").queue();
			return CommandResult.ERROR;
		}
		
		String[] terms = new String[rawArgs.length-1];
		for(int i=0; i<terms.length; ++i) terms[i] = rawArgs[i+1];
		
		Pair<Set<User>, Set<String>> temp = InputUtils.parseBulkMembers(rawArgs, e.getGuild());
		if(temp.getKey().isEmpty()){
			e.getChannel().sendMessage("Unknown User: `" + temp.getValue().iterator().next() + "`.").queue();
			return CommandResult.ERROR;
		}
		
		User target = temp.getKey().iterator().next();
		List<String> r = PermissionUtil.getNodesForUser(target, e.getGuild());
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor("User Information", null, IconUtil.INFO.getURL());
		eb.setColor(SettingsManager.getColorAWT(e.getGuild()));
		
		eb.setDescription(target.getAsMention() + "\n" + BULLET + " Currently blacklisted from `" + r.size() + "` permission" + (r.size() == 1 ? "" : "s") + ".");
		
		if(r.size() > 0){
			r.replaceAll(s -> "`" + s + "`");
			eb.addField("Blacklisted Permissions", r.toString(), false);
		}
		
		e.getChannel().sendMessage(eb.build()).queue();
		
		return CommandResult.SUCCESS;
	}
	
	private CommandResult cmdRoleInfo(MessageReceivedEvent e, String[] rawArgs){
		if(!PermissionUtil.hasPermission(e.getAuthor(), permRoleInfo, e.getGuild(), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		if(rawArgs.length < 2){
			e.getChannel().sendMessage("Proper format is `" + SettingsManager.getCommandPrefix(e.getGuild()) + "permissions roleinfo <role>`").queue();
			return CommandResult.ERROR;
		}
		
		String[] terms = new String[rawArgs.length-1];
		for(int i=0; i<terms.length; ++i) terms[i] = rawArgs[i+1];
		
		Pair<Set<Role>, Set<String>> temp = InputUtils.parseBulkRoles(terms, e.getGuild());
		if(temp.getKey().isEmpty()){
			e.getChannel().sendMessage("Unknown Role: `" + temp.getValue().iterator().next() + "`.").queue();
			return CommandResult.ERROR;
		}
		
		Role target = temp.getKey().iterator().next();
		Set<PermissionNode> r = PermissionUtil.getNodesForRole(target, e.getGuild());
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor("Role Information", null, IconUtil.INFO.getURL());
		eb.setColor(SettingsManager.getColorAWT(e.getGuild()));
		
		eb.setDescription(target.getAsMention() + "\n" + BULLET + " Currently granted `" + r.size() + "` permission" + (r.size() == 1 ? "" : "s") + ".");
		
		if(r.size() > 0){
			List<String> nodeLst = new ArrayList<String>();
			for(PermissionNode n : r) nodeLst.add("`" + n.toString() + "`");
			eb.addField("Granted Permissions", nodeLst.toString(), false);
		}
		
		e.getChannel().sendMessage(eb.build()).queue();
		
		return CommandResult.SUCCESS;
	}
	
	private MessageEmbed constructSubcommandTree(Guild g){
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setAuthor("Please choose a subcommand.", null, null);
		eb.setDescription("`grant` - Grant permissions to roles.\n"
				+ "`revoke` - Revoke permissions from roles.\n"
				+ "`blacklist` - Add users to a node's blacklist.\n"
				+ "`unblacklist` - Remove users from a node's blacklist.\n"
				+ "`enable` - Make a specific node require permission.\n"
				+ "`disable` - Disable a node's permission requirement.\n"
				+ "`info` - View information about a permission node.\n"
				+ "`userinfo` - View permission information about a user.\n"
				+ "`roleinfo` - View permission information about a role.");
		eb.setFooter("Usage | " + SettingsManager.getCommandPrefix(g) + "permissions <subcommand>", IconUtil.INFO.getURL());
		eb.setColor(SettingsManager.getColorAWT(g));
		
		return eb.build();
	}
	
	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}

}
